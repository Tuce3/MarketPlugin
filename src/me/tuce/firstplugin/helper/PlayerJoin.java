package me.tuce.firstplugin.helper;

import me.tuce.firstplugin.Main;
import me.tuce.firstplugin.commands.Collect;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class PlayerJoin implements Listener {
    private Main plugin;
    private MySQL database;
    private String prefix;

    public PlayerJoin(Main pluginToSet) {
        plugin = pluginToSet;
        if(plugin.getCustomConfig().getBoolean("database-info.enabled"))
            database = plugin.getDatabase();
        // Prefix used for messages
        prefix = ChatColor.translateAlternateColorCodes('&', plugin.getCustomConfig().getString("messages.prefix"));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        try {
            PreparedStatement ps = database.getConnection().prepareStatement(
                    "SELECT id FROM offlineItems WHERE uuid=? AND (diamond > 0 OR diamond_block > 0)"
            );
            ps.setString(1, player.getUniqueId().toString());
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                player.sendMessage(
                        prefix +
                                ChatColor.WHITE + "You've obtained diamonds while offline from selling on market."
                );
                Collect.collect(player);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
