package me.tuce.firstplugin.helper;

import org.bukkit.Material;

public class InputCheck {
    public static int checkAmount(String s){
        try{
            return Integer.parseInt(s);
        }
        catch (NumberFormatException e){
            return 0;
        }
        catch (Exception e){
            e.printStackTrace();
            return 0;
        }
    }

    public static Material checkMaterial(String s){
        try{
            return Material.valueOf(s.toUpperCase());
        }
        catch (IllegalArgumentException e){
            return Material.AIR;
        }
        catch (Exception e){
            e.printStackTrace();
            return Material.AIR;
        }
    }
}
