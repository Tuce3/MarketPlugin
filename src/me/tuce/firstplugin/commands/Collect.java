package me.tuce.firstplugin.commands;

import me.tuce.firstplugin.Main;
import me.tuce.firstplugin.helper.CheckInventorySpace;
import me.tuce.firstplugin.helper.GiveItems;
import me.tuce.firstplugin.helper.MySQL;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

public class Collect implements CommandExecutor {
    private static Main plugin;
    public static void setPlugin(Main pluginToSet) {plugin = pluginToSet;}
    //public Collect(Main plugin) {this.plugin = plugin;}

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if(commandSender instanceof Player){
            Player player = (Player) commandSender;

            // Prefix used for messages
            String prefix = ChatColor.translateAlternateColorCodes('&', plugin.getCustomConfig().getString("messages.prefix"));

            if(!plugin.getCustomConfig().getBoolean("database-info.enabled")){
                player.sendMessage(
                        prefix +
                                ChatColor.WHITE + " Command can't be used without database!"
                );
                return true;
            }

            collect(player);
        }
        return true;
    }

    public static void collect(Player player) {
        // Prefix used for messages
        String prefix = ChatColor.translateAlternateColorCodes('&', plugin.getCustomConfig().getString("messages.prefix"));

        MySQL database = plugin.getDatabase();
        try {
            PreparedStatement ps = database.getConnection().prepareStatement(
                    "SELECT * FROM offlineItems WHERE uuid=? AND diamond > 0 AND diamond_block > 0"
            );
            ps.setString(1, player.getUniqueId().toString());
            ResultSet rs = ps.executeQuery();
            int receivedDiamond = 0, receivedDiamondBlock = 0;
            int neededSpace = 0, allNeededDiamond = 0, allNeededDiamondBlock = 0;
            ArrayList<Long> ids = new ArrayList<>();
            while(rs.next()) {
                long id = rs.getLong("id");
                int diamond = rs.getInt("diamond");
                int diamondBlock = rs.getInt("diamond_block");

                if(allNeededDiamond != 0 || allNeededDiamondBlock != 0){
                    allNeededDiamond += diamond;
                    allNeededDiamondBlock += diamondBlock;
                    continue;
                }

                // Check if player has enough inventory space
                ArrayList<Material> list = new ArrayList<>();
                list.add(0, Material.DIAMOND);
                list.add(1, Material.DIAMOND_BLOCK);
                ArrayList<Integer> amountList = new ArrayList<>();
                amountList.add(0, diamond);
                amountList.add(1, diamondBlock);
                if(CheckInventorySpace.checkSpace(player.getInventory(), list, amountList)){
                    ids.add(id);
                    GiveItems.give(player.getInventory(), Material.DIAMOND, diamond);
                    GiveItems.give(player.getInventory(), Material.DIAMOND_BLOCK, diamondBlock);
                    receivedDiamond += diamond;
                    receivedDiamondBlock += diamondBlock;
                } else {
                    int neededDiamond = diamond / 64;
                    int neededDiamondBlock = diamondBlock / 64;
                    neededSpace = neededDiamond + neededDiamondBlock;
                    if (diamond % 64 != 0)
                        neededSpace += 1;
                    if (diamondBlock % 64 != 0)
                        neededSpace += 1;

                    // Tell player how many diamonds he collected
                    if (receivedDiamond != 0 || receivedDiamondBlock != 0) {
                        player.sendMessage(
                                prefix +
                                        ChatColor.WHITE + "You've obtained " +
                                        ChatColor.BLUE + receivedDiamond + " Diamonds " +
                                        ChatColor.WHITE + "and " +
                                        ChatColor.BLUE + receivedDiamondBlock + " Diamond blocks"
                        );
                    }
                }
            }

            if (neededSpace != 0 || allNeededDiamond != 0 || allNeededDiamondBlock != 0) {
                if (ids.size() > 0){
                    String numberOfIds = "(?";
                    for (Long id : ids) {
                        numberOfIds += ",?";
                    }
                    numberOfIds += ")";
                    ps = database.getConnection().prepareStatement(
                            "UPDATE offlineItems SET diamond = 0, diamond_block = 0 WHERE id=" + numberOfIds
                    );
                    int index = 1;
                    for(Long id : ids) {
                        ps.setLong(index, id);
                        index++;
                    }
                    ps.executeUpdate();
                }

                // Tell player he doesn't have enough inventory space
                if(allNeededDiamond % 64 != 0) {
                    allNeededDiamond -= allNeededDiamond % 64;
                    allNeededDiamond /= 64;
                    allNeededDiamond += 1;
                } else
                    allNeededDiamond /= 64;
                if(allNeededDiamondBlock % 64 != 0) {
                    allNeededDiamondBlock -= allNeededDiamondBlock % 64;
                    allNeededDiamondBlock /= 64;
                    allNeededDiamondBlock += 1;
                } else
                    allNeededDiamondBlock /= 64;

                int allNeededSpace = allNeededDiamond + allNeededDiamondBlock;
                if(allNeededSpace > neededSpace) {
                    player.sendMessage(
                            prefix +
                                    ChatColor.WHITE + "You don't have enough inventory space to collect all diamonds, you need at least " +
                                    ChatColor.BLUE + neededSpace +
                                    ChatColor.WHITE + " free inventory slots!(" +
                                    ChatColor.BLUE + allNeededSpace +
                                    ChatColor.WHITE + " free space to collect all diamonds)(use /mcollect when you have enough inventory space)"
                    );
                } else {
                    player.sendMessage(
                            prefix +
                                    ChatColor.WHITE + "You don't have enough inventory space to collect all diamonds, you need " +
                                    ChatColor.BLUE + neededSpace +
                                    ChatColor.WHITE + " free inventory slots!(use /mcollect when you have enough inventory space)"
                    );
                }
            } else if (receivedDiamond != 0 || receivedDiamondBlock != 0) {
                ps = database.getConnection().prepareStatement(
                        "UPDATE offlineItems SET diamond = 0, diamond_block = 0 WHERE uuid=?"
                );
                ps.setString(1, player.getUniqueId().toString());
                ps.executeUpdate();
                // Tell player what he collected
                player.sendMessage(
                        prefix +
                                ChatColor.WHITE + "You obtained " +
                                ChatColor.BLUE + receivedDiamond + " Diamonds " +
                                ChatColor.WHITE + "and " +
                                ChatColor.BLUE + receivedDiamondBlock + " Diamond blocks"
                );
            } else {
                player.sendMessage(
                        prefix +
                                ChatColor.WHITE + "There's nothing to collect."
                );
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
