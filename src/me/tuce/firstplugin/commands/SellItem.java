package me.tuce.firstplugin.commands;

import me.tuce.firstplugin.helper.InputCheck;
import me.tuce.firstplugin.helper.TakeItems;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import me.tuce.firstplugin.ItemsOnSale;
import me.tuce.firstplugin.SellingItem;
import org.bukkit.inventory.ItemStack;

public class SellItem implements CommandExecutor {
    ItemsOnSale itemsOnSale = new ItemsOnSale();
    final static int MIN_ARGS = 3;

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if(commandSender instanceof Player){
            Player player = (Player)commandSender;

            if (args.length < MIN_ARGS){
                player.sendMessage(
                        ChatColor.YELLOW + "[Market] " +
                                ChatColor.WHITE + "You haven't entered enough arguments!"
                );
                return false;
            }

            // Check whether player has inputted count of item to sell properly
            int sellItemCount = InputCheck.checkAmount(args[0]);
            if (sellItemCount < 1){
                player.sendMessage(ChatColor.YELLOW + "[Market]" + ChatColor.WHITE + " You didn't input count number correctly");
                return false;
            }

            // Check whether player has inputted selling item correctly
            Material material = InputCheck.checkMaterial(args[1]);
            if (material == Material.AIR){
                player.sendMessage(
                        ChatColor.YELLOW + "[Market] " +
                                ChatColor.WHITE + "You didn't input name of item to sell correctly!"
                );
                return false;
            }

            // Check whether player has inputted sell price correctly
            int sellPrice = InputCheck.checkAmount(args[2]);
            if (sellPrice < 1){
                player.sendMessage(ChatColor.YELLOW + "[Market]" + ChatColor.WHITE + " You didn't input sell price correctly");
                return false;
            }

            Inventory inventory = player.getInventory();

            // Check whether player wants to sell stack of item
            int stack = 1;
            ItemStack itemStack = new ItemStack(material);
            final int MAX_STACK = itemStack.getMaxStackSize();

            if (s.equals("sellstack"))
                stack = MAX_STACK;

            else if (s.equals("sellhstack") && MAX_STACK > 1)
                stack = MAX_STACK / 2;

            else if (s.equals("sellhstack")){
                // Can't sell half stack of item whose max stack is 1
                player.sendMessage(
                        ChatColor.YELLOW + "[Market] " +
                                ChatColor.WHITE + "Can't sell " +
                                ChatColor.GREEN + material +
                                ChatColor.WHITE + " as half stack!"
                );
                return true;
            }
            System.out.println("passed check for command name");

            // Check whether player has that amount of item
            if (inventory.contains(material, sellItemCount * stack)){
                System.out.println("Entered if ");

                SellingItem sellingItem = new SellingItem(player.getName(), material, sellItemCount, Material.DIAMOND, sellPrice, stack);

                // Remove items that player wants to sell from his inventory
                TakeItems.take(inventory, material, sellItemCount * stack);

                // Put item on sale
                itemsOnSale.addNewItemOnSale(sellingItem);
            }
            else{
                player.sendMessage(
                        ChatColor.YELLOW + "[Market]" +
                                ChatColor.WHITE + " You don't have enough amount of " +
                                ChatColor.GREEN + material +
                                ChatColor.WHITE + "!");
            }
            return true;
        }
        return false;
    }
}
