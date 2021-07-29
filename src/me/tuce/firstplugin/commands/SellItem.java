package me.tuce.firstplugin.commands;

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

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if(commandSender instanceof Player){
            Player player = (Player)commandSender;

            player.sendMessage(s);

            // Check whether player has inputted count of item to sell properly
            int sellItemCount = 1;
            try{
                sellItemCount = Integer.parseInt(args[0]);
            }
            catch (NumberFormatException ex){
                player.sendMessage(ChatColor.YELLOW + "[Market]" + ChatColor.WHITE + " You didn't input count number correctly");
                //System.out.println(player.getName() + " didn't input number correctly");
                return false;
            }
            catch (Exception e){
                e.printStackTrace();
                return false;
            }

            // Check whether player has inputted sell price correctly
            int sellPrice = 1;
            try{
                sellPrice = Integer.parseInt(args[2]);
            }
            catch (NumberFormatException ex){
                player.sendMessage(ChatColor.YELLOW + "[Market]" + ChatColor.WHITE + " You didn't input sell price correctly");
                return false;
            }
            catch (Exception e){
                e.printStackTrace();
                return false;
            }

            Inventory inventory = player.getInventory();
            Material material = Material.valueOf(args[1].toUpperCase());

            System.out.println("before checking command name");

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
