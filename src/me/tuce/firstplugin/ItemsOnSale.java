package me.tuce.firstplugin;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;

public class ItemsOnSale {
    public static HashMap<Material, ArrayList<SellingItem>> map = new HashMap<>();

    // Comparator for SellingItem class so that it can be sorted for lowest price
    static Comparator<SellingItem> smallestPrice = new Comparator<SellingItem>() {
        @Override
        public int compare(SellingItem o1, SellingItem o2) {
            int price1 = (o1.priceItem == Material.DIAMOND_BLOCK) ? o1.priceAmount * 9 : o1.priceAmount;
            int price2 = (o2.priceItem == Material.DIAMOND_BLOCK) ? o2.priceAmount * 9 : o2.priceAmount;
            return Integer.compare(price1, price2);
        }
    };

    // Put new item on the market
    public static void addNewItemOnSale(SellingItem item){
        if (map.containsKey(item.material)){
            ArrayList<SellingItem> list = map.get(item.material);
            list.add(item);
            list.sort(smallestPrice);
            System.out.println(list);
        }
        else{
            ArrayList<SellingItem> list = new ArrayList<>();
            list.add(item);
            map.put(item.material, list);
        }
    }

    // Remove items from market when bought
    public static void removeItemFromSale(Material material, int amount, int stack){
        ArrayList<SellingItem> list = map.get(material);
        for (Iterator<SellingItem> iterator = list.iterator(); iterator.hasNext(); ) {
            SellingItem item = iterator.next();

            // Multipliers used to convert prices and amount of half stacks to full stacks and other way around
            float stackMultiplier = (float) item.stack / stack;
            float removeMultiplier = 1;
            int amountToRemove;
            if (stackMultiplier == 0.5) {
                removeMultiplier = 2;
                if (item.amount % 2 == 1)
                    // Can't buy an odd numbered amount of half stack when player wants full stack
                    amountToRemove = (int) ((item.amount - 1) * stackMultiplier);
                else
                    amountToRemove = (int) (item.amount * stackMultiplier);
            } else {
                amountToRemove = (int) (item.amount * stackMultiplier);
                if (stackMultiplier == 2)
                    removeMultiplier = 0.5f;
            }

            // Removing items from market
            if (amountToRemove > amount) {
                if (removeMultiplier == 0.5 && amount % 2 == 1) {
                    item.amount -= (int) ((amount - 1) * removeMultiplier);
                    amount = 1;
                } else {
                    item.amount = (int) (amount * removeMultiplier);
                    amount = 0;
                }
            } else {
                amount -= amountToRemove;
                iterator.remove();
            }

            if (amount == 0)
                break;
        }
        if (list.size() == 0)
            map.remove(material);
    }
}
