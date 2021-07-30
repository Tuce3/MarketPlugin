package me.tuce.firstplugin;

import me.tuce.firstplugin.commands.Buy;
import me.tuce.firstplugin.commands.Cost;
import me.tuce.firstplugin.commands.SellItem;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class Main extends JavaPlugin implements Listener {

    private File customConfigFile;
    private FileConfiguration customConfig;

    @Override
    public void onEnable(){

        createCustomConfig();

        // Set command executors
        SellItem sellItem = new SellItem();
        this.getCommand("sell").setExecutor(sellItem);
        this.getCommand("sellstack").setExecutor(sellItem);
        this.getCommand("sellhstack").setExecutor(sellItem);
        this.getCommand("cost").setExecutor(new Cost());
        this.getCommand("buy").setExecutor(new Buy());
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
}}
