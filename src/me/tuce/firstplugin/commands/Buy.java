package me.tuce.firstplugin.commands;

import me.tuce.firstplugin.ItemsOnSale;
import me.tuce.firstplugin.Main;
import me.tuce.firstplugin.SellingItem;
import me.tuce.firstplugin.helper.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class Buy implements CommandExecutor {
    final static int MIN_ARGS = 2;


    private final Main plugin;
    public Buy(Main plugin){
        this.plugin = plugin;
    }


    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (commandSender instanceof Player){
            Player player = (Player)commandSender;

            String prefix = ChatColor.translateAlternateColorCodes('&', plugin.getCustomConfig().getString("messages.prefix"));

            if (plugin.getCustomConfig().getBoolean("commands.buy.permission-required") && !player.hasPermission(plugin.getCustomConfig().getString("commands.buy.permission-node"))){
                player.sendMessage(
                        prefix +
                                ChatColor.WHITE + "You don't have permission to buy on market!"
                );
                return true;
            }


            if (args.length < MIN_ARGS){
                String notEnoughArgsMessage = ChatColor.translateAlternateColorCodes('&', plugin.getCustomConfig().getString("messages.not-enough-arguments"));
                player.sendMessage(prefix + notEnoughArgsMessage);
                return false;
            }

            // Check whether player entered valid amount of items to buy
            int amountToBuy = InputCheck.checkAmount(args[0]);
            if (amountToBuy < 1){
                player.sendMessage(
                        prefix +
                                ChatColor.WHITE + "You entered an invalid amount of items to buy!"
                );
                return false;
            }

            // Check whether player entered valid name of item
            Material material = InputCheck.checkMaterial(args[1]);
            if (material == Material.AIR){
                String improperNameOfItem = ChatColor.translateAlternateColorCodes('&', plugin.getCustomConfig().getString("messages.improper-item-name"));
                player.sendMessage(prefix + improperNameOfItem);
                return false;
            }

            // Get the cost of items and whether there are enough items on market
            PriceToPay cost = new PriceToPay(0, 0);
            ItemStack itemStack = new ItemStack(material);
            int stack  = itemStack.getMaxStackSize();
            if (args.length > 2){
                if (args[2].equals("halfstack")) {
                    if (stack <= 1)
                        return false;
                    stack /= 2;
                }
            }

            final int ENTERED_AMOUNT_TO_BUY = amountToBuy;
            HashMap<String, PriceToPay> sellers = new HashMap<>();
            if (ItemsOnSale.map.containsKey(material)){
                // Get how much you need to pay each seller
                ArrayList<SellingItem> list = ItemsOnSale.map.get(material);

                Iterator<SellingItem> it = list.iterator();
                while (it.hasNext()){
                    SellingItem item = it.next();
                    if (!sellers.containsKey(item.name))
                        sellers.put(item.name, new PriceToPay(0, 0));

                    // Multipliers used to convert prices and amount of half stacks to full stacks and other way around
                    float stackMultiplier = (float) item.stack/stack;
                    int amount;
                    float buyMultiplier = 1.0f;
                    if (stackMultiplier == 0.5){
                        buyMultiplier = 2;
                        if (item.amount % 2 == 1)
                            amount = (int) ((item.amount - 1) * stackMultiplier);
                        else
                            amount = (int) (item.amount * stackMultiplier);
                    }
                    else {
                        amount = (int) (item.amount * stackMultiplier);
                        if (stackMultiplier == 2)
                            buyMultiplier = 0.5f;
                    }

                    // How much items to take and how much to pay
                    if (amount >= amountToBuy){
                        PriceToPay increase = new PriceToPay(0, 0);
                        if (buyMultiplier == 0.5 && amountToBuy % 2 == 1) {
                            if (item.priceItem == Material.DIAMOND)
                                increase.diamond = (int) (item.priceAmount * (amountToBuy - 1) * buyMultiplier);
                            else
                                increase.diamond_block = (int) (item.priceAmount * (amountToBuy - 1) * buyMultiplier);
                            amountToBuy = 1;
                        }
                        else {
                            if (item.priceItem == Material.DIAMOND)
                                increase.diamond = (int) (item.priceAmount * amountToBuy * buyMultiplier);
                            else
                                increase.diamond_block = (int) (item.priceAmount * amountToBuy * buyMultiplier);
                            amountToBuy = 0;
                        }
                        sellers.put(item.name, PriceToPay.add(sellers.get(item.name), increase));
                        cost = PriceToPay.add(cost, increase);
                    }
                    else{
                        PriceToPay increase;
                        if (item.priceItem == Material.DIAMOND)
                            increase = new PriceToPay(item.priceAmount * item.amount, 0);
                        else
                            increase = new PriceToPay(0, item.priceAmount * item.amount);
                        sellers.put(item.name, PriceToPay.add(sellers.get(item.name), increase));
                        cost = PriceToPay.add(cost, increase);
                        amountToBuy -= amount;
                    }
                    if (amountToBuy == 0)
                        break;
                }

                // Tell player there aren't enough items in stock
                if (amountToBuy > 0){
                    int itemsInStock = ENTERED_AMOUNT_TO_BUY - amountToBuy;
                    player.sendMessage(
                            prefix +
                                    ChatColor.WHITE + "There is only " +
                                    ChatColor.GREEN + itemsInStock + " " + material +
                                    ChatColor.WHITE + " in stock at the moment."
                    );
                    return true;
                }
            }
            else{
                // Tell player there aren't any items in stock
                player.sendMessage(
                        prefix +
                                ChatColor.WHITE + "No one is selling " +
                                ChatColor.GREEN + material +
                                ChatColor.WHITE + " at the moment."
                );
                return true;
            }

            System.out.println(cost.diamond + " " + cost.diamond_block);

            // Check whether player has enough amount to buy items and inventory space
            if (player.getInventory().contains(Material.DIAMOND, cost.diamond) && player.getInventory().contains(Material.DIAMOND_BLOCK, cost.diamond_block)){
                buy(player, material, ENTERED_AMOUNT_TO_BUY, stack, cost, sellers);
                return true;
            }
            else{
                final boolean hasEnoughDiamonds = player.getInventory().contains(Material.DIAMOND, cost.diamond);
                final boolean hasEnoughDiamondBlocks = player.getInventory().contains(Material.DIAMOND_BLOCK, cost.diamond_block);
                if (!hasEnoughDiamonds && !hasEnoughDiamondBlocks){
                    player.sendMessage(
                            prefix +
                                    ChatColor.WHITE + "You don't have enough " +
                                    ChatColor.BLUE + Material.DIAMOND +
                                    ChatColor.WHITE + " and " +
                                    ChatColor.BLUE + Material.DIAMOND_BLOCK +
                                    ChatColor.WHITE + "!"
                    );
                }
                else if(!hasEnoughDiamonds){
                    // player might have enough diamond_blocks
                    // Amount of diamonds must be dividable by 9
                    if(hasEnoughDiamondBlocks && cost.diamond % 9 == 0){
                        // Transfer diamonds to blocks
                        cost.diamond_block += cost.diamond / 9;
                        cost.diamond = 0;
                    }

                    if(player.getInventory().contains(Material.DIAMOND_BLOCK, cost.diamond_block)){
                        buy(player, material, ENTERED_AMOUNT_TO_BUY, stack, cost, sellers);
                        return true;
                    }

                    player.sendMessage(
                            prefix +
                                    ChatColor.WHITE + "You don't have enough " +
                                    ChatColor.BLUE + Material.DIAMOND +
                                    ChatColor.WHITE + "!"
                    );
                }
                else{
                    // player might have enough diamonds
                    if(hasEnoughDiamonds){
                        // Transfer diamond_blocks to diamonds
                        cost.diamond += cost.diamond_block * 9;
                        cost.diamond_block = 0;
                    }

                    if(player.getInventory().contains(Material.DIAMOND_BLOCK, cost.diamond_block)){
                        buy(player, material, ENTERED_AMOUNT_TO_BUY, stack, cost, sellers);
                        return true;
                    }

                    player.sendMessage(
                            prefix +
                                    ChatColor.WHITE + "You don't have enough " +
                                    ChatColor.BLUE + Material.DIAMOND_BLOCK +
                                    ChatColor.WHITE + "!"
                    );
                }
            }
            return true;
        }
        return false;
    }

    private void buy(Player player, Material material, int ENTERED_AMOUNT_TO_BUY, int stack, PriceToPay cost, HashMap<String, PriceToPay> sellers){
        boolean hasSpace = CheckInventorySpace.checkSpace(player.getInventory(), material, ENTERED_AMOUNT_TO_BUY * stack);

        String prefix = ChatColor.translateAlternateColorCodes('&', plugin.getCustomConfig().getString("messages.prefix"));

        // Tell player that he doesn't have enough inventory space
        if (!hasSpace){
            player.sendMessage(
                    prefix +
                            ChatColor.WHITE + "You don't have enough inventory space!"
            );
        }

        // Give buyer items
        GiveItems.give(player.getInventory(), material, ENTERED_AMOUNT_TO_BUY * stack);

        // Take buyer's diamonds/diamond_blocks
        TakeItems.take(player.getInventory(), Material.DIAMOND, cost.diamond);
        TakeItems.take(player.getInventory(), Material.DIAMOND_BLOCK, cost.diamond_block);

        // Remove items from sale
        ItemsOnSale.removeItemFromSale(material, ENTERED_AMOUNT_TO_BUY, stack);

        // Give sellers their diamonds/diamond_blocks
        for (HashMap.Entry<String, PriceToPay> entry : sellers.entrySet()){
            Player seller = Bukkit.getPlayer(entry.getKey());
            if (seller == null)
                continue;
            GiveItems.give(seller.getInventory(), Material.DIAMOND, entry.getValue().diamond);
            GiveItems.give(seller.getInventory(), Material.DIAMOND_BLOCK, entry.getValue().diamond_block);
        }

        // Check whether player paid in diamonds or diamond_blocks or both
        String paid = "";
        if (cost.diamond > 0 && cost.diamond_block > 0)
            paid = paid + cost.diamond + " " + Material.DIAMOND + ChatColor.WHITE + " and " + ChatColor.BLUE + cost.diamond_block + " " + Material.DIAMOND_BLOCK;
        else if(cost.diamond > 0)
            paid = paid + cost.diamond + " " + Material.DIAMOND;
        else
            paid = paid + cost.diamond_block + " " + Material.DIAMOND_BLOCK;

        // Tell player what he bought and for how much
        player.sendMessage(
                prefix +
                        ChatColor.WHITE + "You bought " +
                        ChatColor.GREEN + ENTERED_AMOUNT_TO_BUY + " " + material +
                        ChatColor.WHITE + " for " +
                        ChatColor.BLUE + paid +
                        ChatColor.WHITE + "!"
        );
    }
}
