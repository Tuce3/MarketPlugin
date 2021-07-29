package me.tuce.firstplugin.helper;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class CheckInventorySpace {
    public static boolean checkSpace(Inventory inventory, Material material, int amountOfItem){
        ItemStack item = new ItemStack(material);
        final int MAX_STACK = item.getMaxStackSize();
        System.out.println("max stack is " + MAX_STACK);
        final int SPACE_NEEDED = amountOfItem;
        int freeSpace = 0;

        for (ItemStack stack : inventory) {

            if (stack == null) {
                // Everyone is saying that material should be air but its instead giving null so i put both just in case
                freeSpace += MAX_STACK;
            }
            else if (stack.getType() == Material.AIR){
                freeSpace += MAX_STACK;
            }
            else if (stack.getType() == material) {
                // if player doesn't have full stack of that item its also free space
                int amount = stack.getAmount();
                if (amount < MAX_STACK)
                    freeSpace += MAX_STACK - amount;
            }

            if (freeSpace >= SPACE_NEEDED)
                return true;
        }

        return false;
    }
}
