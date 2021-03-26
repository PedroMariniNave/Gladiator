package com.zpedroo.gladiator.utils.item;

import com.zpedroo.gladiator.Main;
import com.zpedroo.gladiator.utils.inv.InvUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ItemUtils {

    private Map<Integer, ItemStack> items = new HashMap<>(54);
    public Map<Integer, ItemStack> getItems() { return items; }

    private Map<Integer, Runnable> actions = new HashMap<>(54);
    public Map<Integer, Runnable> getActions() { return actions; }

    private Set<Integer> async = new HashSet<>(54);

    public boolean isAsync(int slot) {
        return async.contains(slot);
    }

    public void setItemAction(int slot, Runnable action, boolean runAsync) {
        Validate.notNull(action, "Item Action cannot be null.");

        // Remove if already have an action on this current slot.
        getActions().remove(slot);

        if (runAsync) async.add(slot);
        getActions().put(slot, action);
    }

    public void setItemAction(ItemStack item, Runnable action, boolean runAsync) {
        Validate.notNull(item, "Item cannot be null.");

        ItemStack storedItem;
        for (int i : getItems().keySet()) {
            storedItem = getItems().get(i);

            if (Main.get().getInvUtils().compareItem(storedItem, item)) {
                setItemAction(i, action, runAsync);
                break;
            }
        }
    }

    public Runnable getAction(int slot) throws NullPointerException {
        return getActions().get(slot);
    }

    public Runnable getAction(ItemStack item) throws NullPointerException {
        ItemStack storedItem;
        for (int i : getItems().keySet()) {
            storedItem = getItems().get(i);

            if (Main.get().getInvUtils().compareItem(storedItem, item)) {
                return getAction(i);
            }
        }

        return null;
    }

    public void addItem(ItemStack item, Inventory inventory, int slot) {
        Validate.noNullElements(new Object[] {item, inventory}, "Elements - {item & inventory} cannot be null.");
        InvUtils.isTrue(getItems().size() >= inventory.getSize(), "Inventory pages can have at max 54 items per page.");
        InvUtils.isTrue((slot < 0), "The item slot need to be higher than 0 and lower than 54.");

        getItems().remove(slot);
        getItems().put(slot, item);

        inventory.setItem(slot, item);
    }

    public void addItem(ItemStack item, Inventory inventory) {
        Validate.noNullElements(new Object[] {item, inventory}, "Elements - {item & inventory} cannot be null.");
        InvUtils.isTrue(getItems().size() >= inventory.getSize(), "Inventory pages can have at max 54 items per page.");

        int slot = -1;
        ItemStack inventoryItem;

        while (slot++ < inventory.getSize()) {
            inventoryItem = inventory.getItem(slot);

            if ((inventoryItem == null) || inventoryItem.getType().equals(Material.AIR)) {
                addItem(item, inventory, slot);
                break;
            }
        }
    }

    public void removeItem(Inventory inventory, Object obj) {
        Validate.noNullElements(new Object[] {inventory, obj}, "Elements - {inventory & obj} cannot be null.");

        int deleted = -1;

        switch (obj.getClass().getSimpleName().toUpperCase()) {
            case "INTEGER":
            case "INT":
                int slot = Integer.parseInt(String.valueOf(obj));

                if (inventory.getSize() >= slot) {
                    ItemStack item = inventory.getItem(slot);

                    if ((item == null) || item.getType().equals(Material.AIR)) return;

                    deleted = slot;
                    inventory.clear(slot);
                }
                break;
            case "STRING":
                String name = String.valueOf(obj);

                ItemStack inventoryItem;
                for (int i = 0; i < inventory.getSize(); i++) {
                    inventoryItem = inventory.getItem(i);

                    if ((inventoryItem == null) || inventoryItem.getType().equals(Material.AIR)) continue;

                    if (inventoryItem.hasItemMeta() && inventoryItem.getItemMeta().hasDisplayName() && inventoryItem.getItemMeta().getDisplayName().equals(name)) {
                        deleted = i;
                        inventory.clear(i);
                        break;
                    }
                }
                break;
            case "ITEMSTACK":
                ItemStack item = (ItemStack) obj;

                if ((item == null) || item.getType().equals(Material.AIR)) return;

                ItemStack inventoryItems;
                for (int i = 0; i < inventory.getSize(); i++) {
                    inventoryItems = inventory.getItem(i);

                    if ((inventoryItems == null) || inventoryItems.getType().equals(Material.AIR)) continue;

                    if (Main.get().getInvUtils().compareItem(inventoryItems, item)) {
                        deleted = i;
                        inventory.clear(i);
                        break;
                    }
                }
                break;
        }

        if (deleted == -1) return;

        // Remove completely this item.
        getItems().remove(deleted);
        getActions().remove(deleted);
        async.remove(deleted);
    }
}