package me.tuce.firstplugin.helper;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class GiveItems {
    public static void give(Inventory inventory, Material material, int amount){
        ItemStack item = new ItemStack(material);
        final int MAX_STACK = item.getMaxStackSize();
        if (MAX_STACK == 1){
            for (int i = 0; i < amount; i++) {
                ItemStack stack = new ItemStack(material);
                inventory.addItem(stack);
            }
        }
        else{
            // If player doesn't have a full stack of item we fill it
            HashMap<Integer, ? extends ItemStack> map = inventory.all(material);
            for (ItemStack stack : map.values()){
                int stackAmount = stack.getAmount();
                if (MAX_STACK > stackAmount){
                    int free = MAX_STACK - stackAmount;
                    if (free > amount){
                        stack.setAmount(stackAmount + amount);
                        amount = 0;
                    }
                    else{
                        amount -= free;
                        stack.setAmount(MAX_STACK);
                    }

                    if (amount == 0)
                        break;
                }
            }

            // If we still have items to give to player we make new stacks
            while (amount > 0){
                ItemStack stack = new ItemStack(material);
                if (amount > MAX_STACK){
                    stack.setAmount(MAX_STACK);
                    amount -= MAX_STACK;
                }
                else{
                    stack.setAmount(amount);
                    amount = 0;
                }
                inventory.addItem(stack);
            }
        }
    }
}
