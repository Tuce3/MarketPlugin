package me.tuce.firstplugin;

import me.tuce.firstplugin.commands.*;
import me.tuce.firstplugin.helper.MySQL;
import me.tuce.firstplugin.helper.PlayerJoin;
import me.tuce.firstplugin.helper.Prompt;
import me.tuce.firstplugin.helper.Prompts;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Main extends JavaPlugin implements Listener {

    private File customConfigFile;
    private FileConfiguration customConfig;
    private MySQL database;

    @Override
    public void onEnable(){

        createCustomConfig();

        // check if plugin is disabled
        if (!customConfig.getBoolean("enable-plugin")){
                this.getPluginLoader().disablePlugin(this);
        }

        // Set plugin for prompts class
        Prompts.SetPlugin(this);
        ItemsOnSale.setPlugin(this);

        if(this.getCustomConfig().getBoolean("database-info.enabled")){
            database = new MySQL(
                    customConfig.getString("database-info.host"),
                    customConfig.getString("database-info.port"),
                    customConfig.getString("database-info.database"),
                    customConfig.getString("database-info.username"),
                    customConfig.getString("database-info.password")
            );
            try {
                database.connect();
                database.createTables();
                ItemsOnSale.loadItemsOnSale();
                this.getServer().getPluginManager().registerEvents(new PlayerJoin(this), this);
                System.out.println("[Market] Successfully connected to database.");
            } catch (Exception e) {
                System.out.println("[Market] COULD NOT CONNECT TO DATABASE: Invalid database-info.");
            }
        }

        // Set command executors
        SellItem sellItem = new SellItem(this);
        this.getCommand("msell").setExecutor(sellItem);
        this.getCommand("msellhalfstack").setExecutor(sellItem);
        this.getCommand("mcost").setExecutor(new Cost(this));
        this.getCommand("mbuy").setExecutor(new Buy(this));
        Confirmation confirmation = new Confirmation(this);
        this.getCommand("myes").setExecutor(confirmation);
        this.getCommand("mno").setExecutor(confirmation);
        Collect.setPlugin(this);
        this.getCommand("mcollect").setExecutor(new Collect());

        // Check whether all blacklist item names are correct in config.yml
        List<?> blacklist = customConfig.getList("blacklist");
        for (ListIterator<?> it = blacklist.listIterator(); it.hasNext(); ) {
            String itemName = (String) it.next();
            try {
                Material.valueOf(itemName);
            } catch (IllegalArgumentException argumentException) {
                System.out.println("[Market] " + itemName + " is not a valid item(blacklist)!");
            }
        }

        // Remove prompts if players didn't use yes or no command
        this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                for (Iterator<?> it = Prompts.prompts.entrySet().iterator(); it.hasNext();) {
                    Map.Entry pair = (Map.Entry) it.next();
                    Prompt prompt = (Prompt) pair.getValue();
                    if (System.currentTimeMillis() - prompt.time > 60000) { // 60 seconds
                        it.remove();
                        System.out.println("removed");
                    }
                }
            }
        }, 300 * 20, 300 * 20); // wait 5 minutes
    }

    @Override
    public void onDisable(){
        if(database != null) {
            System.out.println("[Market] Disconnecting database.");
            database.disconnect();
        }
    }

    public MySQL getDatabase() {
        return this.database;
    }

    public FileConfiguration getCustomConfig() {
        return this.customConfig;
    }

    private void createCustomConfig() {
        customConfigFile = new File(getDataFolder(), "config.yml");
        if (!customConfigFile.exists()) {
            customConfigFile.getParentFile().mkdirs();
            saveResource("config.yml", false);
        }

        customConfig = new YamlConfiguration();
        try {
            customConfig.load(customConfigFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }
}
