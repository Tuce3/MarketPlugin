package me.tuce.firstplugin.commands;

import me.tuce.firstplugin.Main;
import me.tuce.firstplugin.SellingItem;
import me.tuce.firstplugin.helper.InputCheck;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import me.tuce.firstplugin.ItemsOnSale;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class Cost implements CommandExecutor {
    final static int MIN_ARGS = 1;

    private final Main plugin;
    public Cost(Main plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (commandSender instanceof Player){
            Player player = (Player)commandSender;

            String prefix = ChatColor.translateAlternateColorCodes('&', plugin.getCustomConfig().getString("messages.prefix"));

            if (plugin.getCustomConfig().getBoolean("commands.cost.permission-required") && !player.hasPermission(plugin.getCustomConfig().getString("commands.cost.permission-node"))){
                player.sendMessage(
                        prefix +
                                ChatColor.WHITE + "You don't have permission for cost command on market!"
                );
                return true;
            }

            if (args.length < MIN_ARGS){
                String notEnoughArgsMessage = ChatColor.translateAlternateColorCodes('&', plugin.getCustomConfig().getString("messages.not-enough-arguments"));
                player.sendMessage(prefix + notEnoughArgsMessage);
                return false;
            }

            Material key = InputCheck.checkMaterial(args[0]);
            if (key == Material.AIR){
                String improperNameOfItem = ChatColor.translateAlternateColorCodes('&', plugin.getCustomConfig().getString("messages.improper-item-name"));
                player.sendMessage(prefix + improperNameOfItem);
                return false;
            }

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

                // Checking how much items are in stock
                int amount = 0;
                for (Iterator<SellingItem> iter = map.get(key).iterator(); iter.hasNext(); ) {
                    SellingItem it = iter.next();
                    if (it.stack == sellingItem.stack)
                        amount += it.amount;
                    else if(it.stack > sellingItem.stack)
                        amount += it.amount * 2;
                    else
                        amount += it.amount / 2;

                }

                // Tell player how much the cheapest item is
                player.sendMessage(
                        prefix +
                                ChatColor.WHITE + sellingItem.name + " is selling " +
                                ChatColor.GREEN + stack + sellingItem.material +
                                ChatColor.WHITE + " for " +
                                ChatColor.BLUE + sellingItem.priceAmount + " " + sellingItem.priceItem +
                                ChatColor.WHITE + " (" + amount + " in stock).");
            }
            else{
                // Item that player wants is not sold by anyone
                player.sendMessage(
                        prefix +
                                ChatColor.WHITE + "No one is selling " +
                                ChatColor.GREEN + key +
                                ChatColor.WHITE + " at the moment.");
            }
            return true;
        }

        return false;
    }
}
