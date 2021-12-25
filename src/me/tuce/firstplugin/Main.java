package me.tuce.firstplugin;

import me.tuce.firstplugin.commands.Buy;
import me.tuce.firstplugin.commands.Confirmation;
import me.tuce.firstplugin.commands.Cost;
import me.tuce.firstplugin.commands.SellItem;
import me.tuce.firstplugin.helper.Prompt;
import me.tuce.firstplugin.helper.Prompts;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Main extends JavaPlugin implements Listener {

    private File customConfigFile;
    private FileConfiguration customConfig;

    @Override
    public void onEnable(){

        createCustomConfig();

        // check if plugin is disabled
        if (!this.getCustomConfig().getBoolean("enable-plugin")){
                this.getPluginLoader().disablePlugin(this);
        }

        // Set plugin for prompts class
        Prompts.SetPlugin(this);

        // Set command executors
        SellItem sellItem = new SellItem(this);
        this.getCommand("sell").setExecutor(sellItem);
        this.getCommand("sellhstack").setExecutor(sellItem);
        this.getCommand("cost").setExecutor(new Cost(this));
        this.getCommand("buy").setExecutor(new Buy(this));
        Confirmation confirmation = new Confirmation(this);
        this.getCommand("y").setExecutor(confirmation);
        this.getCommand("n").setExecutor(confirmation);

        // Check whether all blacklist item names are correct in config.yml
        List<?> blacklist = this.getCustomConfig().getList("blacklist");
        for (ListIterator<?> it = blacklist.listIterator(); it.hasNext(); ) {
            String itemName = (String) it.next();
            try {
                Material.valueOf(itemName);
            } catch (IllegalArgumentException argumentException) {
                System.out.println("[Market] " + itemName + " is not a valid item(blacklist)");
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
