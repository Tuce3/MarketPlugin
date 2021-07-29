package me.tuce.firstplugin;

import org.bukkit.Material;

// Holds the data of the item on sale
public class SellingItem {
    public String name;
    public Material material;
    public int amount;
    public Material priceItem;
    public int priceAmount;
    public int stack;
    public SellingItem(String name, Material material, int amount, Material priceItem, int priceAmount, int stack){
        this.name = name;
        this.material = material;
        this.amount = amount;
        this.priceItem = priceItem;
        this.priceAmount = priceAmount;
        this.stack = stack;
    }
}
