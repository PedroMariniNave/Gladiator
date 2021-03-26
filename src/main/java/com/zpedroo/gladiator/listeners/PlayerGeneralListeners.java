package com.zpedroo.gladiator.listeners;

import com.zpedroo.gladiator.Main;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerGeneralListeners implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Main.get().getServer().getScheduler().runTaskLaterAsynchronously(Main.get(), () -> Main.get().getDataManager().loadPlayer(event.getPlayer()), 1L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Main.get().getServer().getScheduler().runTaskLaterAsynchronously(Main.get(), () -> Main.get().getDataManager().savePlayer(event.getPlayer()), 1L);
    }
}
