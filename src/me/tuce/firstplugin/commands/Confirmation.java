package me.tuce.firstplugin.commands;

import me.tuce.firstplugin.Main;
import me.tuce.firstplugin.ItemsOnSale;
import me.tuce.firstplugin.helper.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class Confirmation implements CommandExecutor {

    private final Main plugin;
    public Confirmation(Main plugin) {this.plugin = plugin;}

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;

            // Prefix used for messages
            String prefix = ChatColor.translateAlternateColorCodes('&', plugin.getCustomConfig().getString("messages.prefix"));

            if (s.equals("yesmarket")) {
                if (!Prompts.prompts.containsKey(player.getName())) {
                    player.sendMessage(
                            prefix +
                                    ChatColor.WHITE + "This command can only be used for confirming your choice when you want to sell or buy something on market."
                    );
                    return true;
                }

                Prompt prompt = Prompts.prompts.get(player.getName());

                // Remove prompt so that player doesn't accidentally confirm twice
                Prompts.RemovePrompt(player);

                if (prompt.promptType == PromptType.SELL) {
                    // Remove items that player wants to sell from his inventory
                    TakeItems.take(player.getInventory(), prompt.sellingItem.material, prompt.sellingItem.amount * prompt.sellingItem.stack);

                    // Put item on sale
                    ItemsOnSale.addNewItemOnSale(prompt.sellingItem);

                    player.sendMessage(
                            prefix +
                                    ChatColor.WHITE + "Successfully put " +
                                    ChatColor.BLUE + prompt.sellingItem.material +
                                    ChatColor.WHITE + " on market!"
                    );
                }else if (prompt.promptType == PromptType.BUY) {
                    // Give buyer items
                    GiveItems.give(player.getInventory(), prompt.material, prompt.ENTERED_AMOUNT_TO_BUY * prompt.stack);

                    // Take buyer's diamonds/diamond_blocks
                    TakeItems.take(player.getInventory(), Material.DIAMOND, prompt.cost.diamond);
                    TakeItems.take(player.getInventory(), Material.DIAMOND_BLOCK, prompt.cost.diamond_block);

                    // Remove items from sale
                    ItemsOnSale.removeItemFromSale(prompt.material, prompt.ENTERED_AMOUNT_TO_BUY, prompt.stack);

                    // Give sellers their diamonds/diamond_blocks
                    for (HashMap.Entry<String, PriceToPay> entry : prompt.sellers.entrySet()){
                        Player seller = Bukkit.getPlayer(entry.getKey());
                        if (seller == null)
                            continue;
                        GiveItems.give(seller.getInventory(), Material.DIAMOND, entry.getValue().diamond);
                        GiveItems.give(seller.getInventory(), Material.DIAMOND_BLOCK, entry.getValue().diamond_block);
                    }

                    // Check whether player paid in diamonds or diamond_blocks or both
                    String paid = "";
                    if (prompt.cost.diamond > 0 && prompt.cost.diamond_block > 0)
                        paid = paid + prompt.cost.diamond + " " + Material.DIAMOND + ChatColor.WHITE + " and " + ChatColor.BLUE + prompt.cost.diamond_block + " " + Material.DIAMOND_BLOCK;
                    else if(prompt.cost.diamond > 0)
                        paid = paid + prompt.cost.diamond + " " + Material.DIAMOND;
                    else
                        paid = paid + prompt.cost.diamond_block + " " + Material.DIAMOND_BLOCK;

                    // Tell player what he bought and for how much
                    player.sendMessage(
                            prefix +
                                    ChatColor.WHITE + "You bought " +
                                    ChatColor.GREEN + prompt.ENTERED_AMOUNT_TO_BUY + " " + prompt.material +
                                    ChatColor.WHITE + " for " +
                                    ChatColor.BLUE + paid +
                                    ChatColor.WHITE + "!"
                    );
                }
                return true;
            }else if (s.equals("nomarket")) {
                if (!Prompts.prompts.containsKey(player.getName())) {
                    player.sendMessage(
                            prefix +
                                    ChatColor.WHITE + "This command can only be used for confirming your choice when you want to sell or buy something on market."
                    );
                    return true;
                }

                Prompt prompt = Prompts.prompts.get(player.getName());
                Prompts.RemovePrompt(player);
                player.sendMessage(
                        prefix +
                                ChatColor.WHITE + prompt.promptType + " command has been revoked!"
                );
                return true;
            }
        }
        return false;
    }
}
