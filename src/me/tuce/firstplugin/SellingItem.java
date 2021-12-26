package me.tuce.firstplugin;

import org.bukkit.Material;

import java.util.UUID;

// Holds the data of the item on sale
public class SellingItem {
    public long id;
    public UUID uuid;
    public Material material;
    public int amount;
    public Material priceItem;
    public int priceAmount;
    public byte stack;
    public SellingItem(UUID uuid, Material material, int amount, Material priceItem, int priceAmount, int stack){
        this.uuid = uuid;
        this.material = material;
        this.amount = amount;
        this.priceItem = priceItem;
        this.priceAmount = priceAmount;
        this.stack = (byte) stack;
    }

    public SellingItem(long id, UUID uuid, Material material, int amount, Material priceItem, int priceAmount, int stack) {
        this.id = id;
        this.uuid = uuid;
        this.material = material;
        this.amount = amount;
        this.priceItem = priceItem;
        this.priceAmount = priceAmount;
        this.stack = (byte) stack;
    }
}
