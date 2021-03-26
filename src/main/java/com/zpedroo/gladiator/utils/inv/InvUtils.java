package com.zpedroo.gladiator.utils.inv;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class InvUtils {

    public static final int MAX_INVENTORY_TITLE_SIZE = 32;

    public static void isTrue(boolean expression, String message) {
        if (expression) {
            throw new IllegalArgumentException(message);
        }
    }

    public String cut(String title) {
        return StringUtils.substring(title, 0, MAX_INVENTORY_TITLE_SIZE);
    }

    public boolean compareItem(ItemStack one, ItemStack two) {
        Validate.noNullElements(new Object[] {one, two}, "The comparing items cannot be null.");
        if (!one.getType().equals(two.getType())) return false;

        boolean amount = (one.getAmount() == two.getAmount());
        boolean durability = (one.getDurability() == two.getDurability());
        boolean enchants = (one.getEnchantments().equals(two.getEnchantments()));
        boolean meta = Bukkit.getItemFactory().equals(one.getItemMeta(), two.getItemMeta());
        boolean type = (one.getTypeId() == two.getTypeId());

        return (amount && durability && enchants && meta && type);
    }

    public void fill(ItemStack item, Inventory inventory, int[] slotsBypass) {
        Validate.noNullElements(new Object[] {item, inventory}, "Elements: {item & inventory} cannot be null.");

        List<Integer> stay = Arrays.stream(slotsBypass).boxed().collect(Collectors.toList());
        ItemStack inventoryItem;

        for (int i = 0; i < inventory.getSize(); i++) {
            if (stay.contains(i)) continue;
            inventoryItem = inventory.getItem(i);

            if ((inventoryItem == null) || inventoryItem.getType().equals(Material.AIR)) inventory.setItem(i, item);
        }
    }
}