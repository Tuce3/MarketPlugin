package me.tuce.firstplugin.commands;

import me.tuce.firstplugin.SellingItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import me.tuce.firstplugin.ItemsOnSale;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;

public class Cost implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (commandSender instanceof Player){
            Player player = (Player)commandSender;
            Material key = Material.valueOf(args[0].toUpperCase());

            // Get all items sold on the market
            HashMap<Material, ArrayList<SellingItem>> map = ItemsOnSale.map;

            // Check whether item player is searching for is sold on market
            if (map.containsKey(key)){
                SellingItem sellingItem = map.get(key).get(0);

                // Check whether item is sold in stacks
                String stack;
                if (sellingItem.stack > 1){
                    stack = sellingItem.stack + "x ";
                }else{
                    stack = "";
                }

                // Tell player how much the cheapest item is
                player.sendMessage(
                        ChatColor.YELLOW + "[Market] " +
                                ChatColor.WHITE + sellingItem.name + " is selling " +
                                ChatColor.GREEN + stack + sellingItem.material +
                                ChatColor.WHITE + " for " +
                                ChatColor.BLUE + sellingItem.priceAmount + " " + sellingItem.priceItem +
                                ChatColor.WHITE + " (" + sellingItem.amount + " in stock).");
            }
            else{
                // Item that player wants is not sold by anyone
                player.sendMessage(
                        ChatColor.YELLOW + "[Market] " +
                                ChatColor.WHITE + "No one is selling " +
                                ChatColor.GREEN + key +
                                ChatColor.WHITE + " at the moment.");
            }
            return true;
        }

        return false;
    }
}
