package me.tuce.firstplugin.commands;

import me.tuce.firstplugin.Main;
import me.tuce.firstplugin.helper.*;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import me.tuce.firstplugin.SellingItem;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class SellItem implements CommandExecutor {
    final static int MIN_ARGS = 3;


    private final Main plugin;
    public SellItem(Main plugin){
        this.plugin = plugin;
    }



    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if(commandSender instanceof Player){
            Player player = (Player)commandSender;

            // Prefix used for messages
            String prefix = ChatColor.translateAlternateColorCodes('&', plugin.getCustomConfig().getString("messages.prefix"));

            if (plugin.getCustomConfig().getBoolean("commands.sell.permission-required") && !player.hasPermission(plugin.getCustomConfig().getString("commands.sell.permission-node"))){
                String noPermissionMessage = ChatColor.translateAlternateColorCodes('&',plugin.getCustomConfig().getString("messages.no-sell-permission"));
                player.sendMessage(prefix + noPermissionMessage);
                return true;
            }

            if (args.length < MIN_ARGS){
                String notEnoughArgsMessage = ChatColor.translateAlternateColorCodes('&', plugin.getCustomConfig().getString("messages.not-enough-arguments"));
                player.sendMessage(prefix + notEnoughArgsMessage);
                return false;
            }

            // Check whether player has inputted count of item to sell properly
            int sellItemCount = InputCheck.checkAmount(args[0]);
            if (sellItemCount < 1){
                String improperItemNumCount = ChatColor.translateAlternateColorCodes('&', plugin.getCustomConfig().getString("messages.improper-item-number-count"));
                player.sendMessage(prefix + improperItemNumCount);
                return false;
            }

            // Check whether player has inputted selling item correctly
            Material material = InputCheck.checkMaterial(args[1]);
            List<?> blacklist = plugin.getCustomConfig().getList("blacklist");
            if (blacklist.contains(material.name())){
                player.sendMessage(
                        prefix +
                                ChatColor.BLUE + material +
                                ChatColor.WHITE + " can't be sold."
                );
                return true;
            }
            if (material == Material.AIR){
                String improperNameOfItem = ChatColor.translateAlternateColorCodes('&', plugin.getCustomConfig().getString("messages.improper-item-name"));
                player.sendMessage(prefix + improperNameOfItem);
                return false;
            }

            // Check whether player has inputted sell price correctly
            int sellPrice = InputCheck.checkAmount(args[2]);
            if (sellPrice < 1){
                player.sendMessage(prefix + ChatColor.WHITE + "You didn't input sell price correctly");
                return false;
            }

            Material sellMaterial = Material.DIAMOND;
            if (args.length > 3){
                Material materialCheck = InputCheck.checkMaterial(args[3]);
                if (materialCheck == Material.DIAMOND_BLOCK || materialCheck == Material.DIAMOND){
                    sellMaterial = materialCheck;
                }
                else{
                    player.sendMessage(
                            prefix +
                                    ChatColor.WHITE + "Only " +
                                    ChatColor.BLUE + "DIAMONDS " +
                                    ChatColor.WHITE + "and " +
                                    ChatColor.BLUE + "DIAMOND_BLOCKS " +
                                    ChatColor.WHITE + "are accepted as price!"
                    );
                    return false;
                }
            }

            // Check whether such sell price is allowed
            ConfigurationSection cs = plugin.getCustomConfig().getConfigurationSection("price");
            if (cs.contains(material.name())) {
                cs = plugin.getCustomConfig().getConfigurationSection("price." + material.name());
                if (cs.contains("min")){
                    double min = (double)cs.getInt("min");
                    min = (sellMaterial == Material.DIAMOND) ? min : min/9;
                    if (min > sellPrice) {
                        player.sendMessage(
                                prefix +
                                        ChatColor.WHITE + "Minimum sell price for " +
                                        ChatColor.BLUE + material.name() +
                                        ChatColor.WHITE + " is " +
                                        ChatColor.BLUE + min + " " + sellMaterial
                        );
                        return true;
                    }
                }
                if (cs.contains("max")) {
                    double max = (double)cs.getInt("max");
                    max = (sellMaterial == Material.DIAMOND) ? max : max/9;
                    if (max < sellPrice) {
                        player.sendMessage(
                                prefix +
                                        ChatColor.WHITE + "Maximum sell price for " +
                                        ChatColor.BLUE + material.name() +
                                        ChatColor.WHITE + " is " +
                                        ChatColor.BLUE + max + " " + sellMaterial
                        );
                        return true;
                    }
                }
            }else{
                // Check whether sell price is higher than default max price
                int price = (sellMaterial == Material.DIAMOND) ? sellPrice : sellPrice/9;
                int maxPrice = plugin.getCustomConfig().getInt("max-price");
                if (price > maxPrice) {
                    maxPrice = (sellMaterial == Material.DIAMOND) ? maxPrice : maxPrice/9;
                    player.sendMessage(
                            prefix +
                                    ChatColor.WHITE + "Maximum sell price for " +
                                    ChatColor.BLUE + material.name() +
                                    ChatColor.WHITE + " is " +
                                    ChatColor.BLUE + maxPrice + " " + sellMaterial
                    );
                    return true;
                }
            }

            Inventory inventory = player.getInventory();

            // Check whether player wants to sell stack of item
            ItemStack itemStack = new ItemStack(material);
            final int MAX_STACK = itemStack.getMaxStackSize();
            int stack = MAX_STACK;

            if (s.equals("sellhstack") && MAX_STACK > 1)
                stack = MAX_STACK / 2;

            else if (s.equals("sellhstack")){
                // Can't sell half stack of item whose max stack is 1
                player.sendMessage(
                        prefix +
                                ChatColor.WHITE + "Can't sell " +
                                ChatColor.GREEN + material +
                                ChatColor.WHITE + " as half stack!"
                );
                return true;
            }

            // Check whether player has that amount of item
            if (inventory.contains(material, sellItemCount * stack)){
                SellingItem sellingItem = new SellingItem(player.getName(), material, sellItemCount, sellMaterial, sellPrice, stack);

                // Check whether player really wants to sell with a prompt
                Prompt prompt = new Prompt(PromptType.SELL, sellingItem);

                // If player already has a prompt remove it
                Prompts.RemovePrompt(player);

                // Add the new prompt
                Prompts.AddPrompt(player, prompt);
            }
            else{
                player.sendMessage(
                        prefix +
                                ChatColor.WHITE + " You don't have enough amount of " +
                                ChatColor.GREEN + material +
                                ChatColor.WHITE + "!");
            }
            return true;
        }
        return false;
    }
}
