package me.tuce.firstplugin.helper;

public class PriceToPay {
    public int diamond;
    public int diamond_block;

    public PriceToPay(int diamonds, int diamond_blocks){
        this.diamond = diamonds;
        this.diamond_block = diamond_blocks;
    }

    public static PriceToPay add(PriceToPay p1, PriceToPay p2){
        return new PriceToPay(p1.diamond + p2.diamond, p1.diamond_block + p2.diamond_block);
    }
}
