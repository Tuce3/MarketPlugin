package me.tuce.firstplugin.helper;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Iterator;

public class TakeItems {
    public static void take(Inventory inventory, Material material, int amount){
        HashMap<Integer, ? extends ItemStack> stacks = inventory.all(material);

        for (ItemStack stack : stacks.values()){
            int stackAmount = stack.getAmount();

            System.out.println(stackAmount);
            System.out.println(amount);
            if (stackAmount > amount) {
                stack.setAmount(stackAmount - amount);
                amount = 0;
            } else {
                System.out.println("removing");
                amount -= stackAmount;
                inventory.removeItem(stack);
            }
            if (amount == 0)
                break;
        }
    }
}
