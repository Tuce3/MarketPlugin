package me.tuce.firstplugin.helper;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;

public class CheckInventorySpace {
    // Check space for only one type of item
    public static boolean checkSpace(Inventory inventory, Material material, int amountOfItem) {
        int neededSlots = 0;
        // First fill up the stacks that aren't full
        HashMap<Integer, ? extends ItemStack> map = inventory.all(material);
        for(ItemStack stack : map.values()) {
            if (stack.getAmount() < stack.getMaxStackSize()) {
                if(amountOfItem > stack.getAmount())
                    amountOfItem -= stack.getAmount();
                else {
                    amountOfItem = 0;
                    return true;
                }
            }
        }

        // Fill up free slots
        if(amountOfItem > 0) {
            ItemStack stack = new ItemStack(material);
            if (amountOfItem % stack.getMaxStackSize() != 0) {
                amountOfItem -= amountOfItem % stack.getMaxStackSize();
                neededSlots++;
            }
            neededSlots += amountOfItem / stack.getMaxStackSize();
            for(ItemStack s : inventory.getStorageContents()) {
                if (s == null)
                    neededSlots--;
                if(neededSlots < 1)
                    return true;
            }
        }

        return false;
    }

    // Check space for multiple types of items
    public static boolean checkSpace(Inventory inventory, ArrayList<Material> materials, ArrayList<Integer> amountOfItem){
        int neededSlots = 0;
        int index = 0;
        // First fill up the stacks that aren't full
        for(int amount : amountOfItem) {
            Material material = materials.get(index);
            HashMap<Integer, ? extends ItemStack> map = inventory.all(material);
            for(ItemStack stack : map.values()) {
                if(stack.getAmount() < stack.getMaxStackSize()) {
                    if(amount > stack.getAmount()) {
                        amount -= stack.getAmount();
                    } else {
                        amountOfItem.set(index, 0);
                        continue;
                    }
                }
            }
            // Set how many items still need to be put into inventory
            amountOfItem.set(index, amount);

            // Calculate how many free slots are needed
            if(amount > 0) {
                ItemStack stack = new ItemStack(materials.get(index));
                if(amount % stack.getMaxStackSize() != 0) {
                    amount -= amount % stack.getMaxStackSize();
                    neededSlots++;
                }
                neededSlots += amount / stack.getMaxStackSize();
            }
            index++;
        }

        // Fill up free slots
        if(neededSlots > 0) {
            for(ItemStack stack : inventory.getStorageContents()) {
                if (stack == null)
                    neededSlots--;
                if(neededSlots < 1)
                    return true;
            }
        }

        return false;
    }
}
