package me.tuce.firstplugin.helper;

import me.tuce.firstplugin.Main;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.HashMap;

// Class that holds all the prompts
public class Prompts {
    private static Main plugin;
    public static HashMap<String, Prompt> prompts = new HashMap<>();
    public static void AddPrompt(Player player, Prompt prompt) {
        RemovePrompt(player);

        // Prefix used for messages
        String prefix = ChatColor.translateAlternateColorCodes('&', plugin.getCustomConfig().getString("messages.prefix"));
        prompts.put(player.getName(), prompt);
        if (prompt.promptType == PromptType.SELL) {
            player.sendMessage(
                    prefix +
                            ChatColor.WHITE + "Are you sure you want to sell " +
                            ChatColor.YELLOW + prompt.sellingItem.amount + " " +
                            ChatColor.BLUE + prompt.sellingItem.stack + "x " + prompt.sellingItem.material +
                            ChatColor.WHITE + " for " +
                            ChatColor.BLUE + prompt.sellingItem.priceAmount + " " + prompt.sellingItem.priceItem +
                            ChatColor.WHITE + " each?(type /y for yes or /n for no)"
            );
        }else if (prompt.promptType == PromptType.BUY) {
            // Check whether player paid in diamonds or diamond_blocks or both
            String paid = "";
            if (prompt.cost.diamond > 0 && prompt.cost.diamond_block > 0)
                paid = paid + prompt.cost.diamond + " " + Material.DIAMOND + ChatColor.WHITE + " and " + ChatColor.BLUE + prompt.cost.diamond_block + " " + Material.DIAMOND_BLOCK;
            else if(prompt.cost.diamond > 0)
                paid = paid + prompt.cost.diamond + " " + Material.DIAMOND;
            else
                paid = paid + prompt.cost.diamond_block + " " + Material.DIAMOND_BLOCK;

            player.sendMessage(
                    prefix +
                            ChatColor.WHITE + "Are you sure you want to buy " +
                            ChatColor.YELLOW + prompt.ENTERED_AMOUNT_TO_BUY + " " +
                            ChatColor.BLUE + prompt.stack + "x " + prompt.material +
                            ChatColor.WHITE + " for " +
                            ChatColor.BLUE + paid +
                            ChatColor.WHITE + " each?(type /yes or /no)"
            );
        }

    }
    public static void RemovePrompt(Player player) {
        if (prompts.containsKey(player.getName()))
            prompts.remove(player.getName());
    }

    public static void SetPlugin(Main pluginToSet) {
        plugin = pluginToSet;
    }
}
