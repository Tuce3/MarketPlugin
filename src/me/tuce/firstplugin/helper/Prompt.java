package me.tuce.firstplugin.helper;

import me.tuce.firstplugin.SellingItem;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.UUID;

public class Prompt {
    public PromptType promptType;
    public long time;

    // For sell prompts
    public SellingItem sellingItem;

    // For buy prompts
    public Material material;
    public int ENTERED_AMOUNT_TO_BUY;
    public int stack;
    public PriceToPay cost;
    public HashMap<UUID, PriceToPay> sellers;

    // Sell prompt constructor
    public Prompt (PromptType promptType, SellingItem sellingItem) {
        this.promptType = promptType;
        this.sellingItem = sellingItem;
        this.time = System.currentTimeMillis();
    }

    // Buy prompt constructor
    public Prompt (PromptType promptType, Material material, int enteredAmount, int stack, PriceToPay cost, HashMap<UUID, PriceToPay> sellers) {
        this.promptType = promptType;
        this.time = System.currentTimeMillis();
        this.material = material;
        this.ENTERED_AMOUNT_TO_BUY = enteredAmount;
        this.stack = stack;
        this.cost = cost;
        this.sellers = sellers;
    }
}
