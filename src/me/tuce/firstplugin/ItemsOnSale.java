package me.tuce.firstplugin;

import me.tuce.firstplugin.helper.MySQL;
import org.bukkit.Material;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class ItemsOnSale {
    public static HashMap<Material, ArrayList<SellingItem>> map = new HashMap<>();
    private static Main plugin;

    public static void setPlugin(Main pluginToSet) {
        plugin = pluginToSet;
    }

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
        if(plugin.getCustomConfig().getBoolean("database-info.enabled") && plugin.getDatabase().isConnected()) {
            MySQL database = plugin.getDatabase();
            try {
                PreparedStatement ps = database.getConnection().prepareStatement("insert into onsale(uuid, material, amount, priceItem, priceAmount, stack) values(?, ?, ?, ?, ?, ?)");
                ps.setString(1, item.uuid.toString());
                ps.setString(2, item.material.name());
                ps.setInt(3, item.amount);
                ps.setString(4, item.priceItem.name());
                ps.setInt(5, item.priceAmount);
                ps.setByte(6, item.stack);

                PreparedStatement ps2 = database.getConnection().prepareStatement("select last_insert_id()");
                ps.executeUpdate();
                ResultSet rs = ps2.executeQuery();
                if(rs.next()) {
                    item.id = rs.getLong("last_insert_id()");
                    System.out.println(item.id);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
            long id = item.id;

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
                item.amount = 0;
                iterator.remove();
            }

            // Updating database
            if(plugin.getCustomConfig().getBoolean("database-info.enabled")) {
                MySQL database = plugin.getDatabase();
                try {
                    PreparedStatement ps = database.getConnection().prepareStatement(
                            "UPDATE onsale SET amount = ? WHERE id = ?"
                    );
                    ps.setInt(1, item.amount);
                    ps.setLong(2, id);
                    ps.executeUpdate();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (amount == 0)
                break;
        }
        if (list.size() == 0)
            map.remove(material);
    }

    public static void loadItemsOnSale(){
        MySQL database = plugin.getDatabase();
        try {
            PreparedStatement ps = database.getConnection().prepareStatement(
                    "SELECT * FROM onsale WHERE amount > 0"
            );
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                long id = rs.getLong("id");
                UUID uuid = UUID.fromString(rs.getString("uuid"));
                Material material = Material.getMaterial(rs.getString("material"));
                int amount = rs.getInt("amount");
                Material priceItem = Material.getMaterial(rs.getString("priceItem"));
                int priceAmount = rs.getInt("priceAmount");
                byte stack = rs.getByte("stack");
                SellingItem item = new SellingItem(id, uuid, material, amount, priceItem, priceAmount, stack);

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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
