package com.zpedroo.gladiator.listeners;

import com.zpedroo.gladiator.managers.FileManager;
import com.zpedroo.gladiator.utils.item.ItemUtils;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class InvActionListener implements Listener {

    private Plugin plugin;
    private ItemUtils itemUtils;
    private String TITLE;

    public InvActionListener(Plugin plugin, ItemUtils itemUtils, FileManager file) {
        this.plugin = plugin;
        this.itemUtils = itemUtils;
        this.TITLE = file.get().getString("Inventory.title");
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getName().equals(TITLE)) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null || event.getCurrentItem().getType().equals(Material.AIR)) return;

            this.runAction(itemUtils.getAction(event.getSlot()), itemUtils.isAsync(event.getSlot()));
        }
    }

    private void runAction(Runnable action, boolean async) {
        if (action == null) return;

        if (async) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    action.run();
                }
            }.runTaskLaterAsynchronously(plugin, 0);
        } else {
            action.run();
        }
    }
}