package com.zpedroo.gladiator.data;

import com.zpedroo.gladiator.Main;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class DataManager{

    public HashMap<Player, PlayerData> playerDataCache;

    public DataManager() {
        this.playerDataCache = new HashMap<>();
    }

    public PlayerData loadPlayer(Player player) {
        if (player == null) return null;

        if (playerDataCache.containsKey(player)) {
            return playerDataCache.get(player);
        }

        PlayerData playerData = Main.get().getSQLiteConnector().loadPlayer(player.getUniqueId());
        playerDataCache.put(player, playerData);
        return playerData;
    }

    public boolean savePlayer(Player player) {
        if (playerDataCache.containsKey(player)) {
            PlayerData playerData = playerDataCache.get(player);
            boolean s = Main.get().getSQLiteConnector().savePlayer(playerData);
            if (s) {
                playerDataCache.remove(player);
            }
            return s;
        }
        return false;
    }

    public void saveAll() {
        for (PlayerData playerData : playerDataCache.values()) {
            try {
                Main.get().getSQLiteConnector().savePlayer(playerData);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
