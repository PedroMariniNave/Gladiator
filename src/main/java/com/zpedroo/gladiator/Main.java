package com.zpedroo.gladiator;

import com.zpedroo.gladiator.commands.GladiatorCmd;
import com.zpedroo.gladiator.commands.GladiatorCoinCmd;
import com.zpedroo.gladiator.commands.GladiatorHelmetCmd;
import com.zpedroo.gladiator.data.DataManager;
import com.zpedroo.gladiator.data.SQLiteConnector;
import com.zpedroo.gladiator.listeners.InvActionListener;
import com.zpedroo.gladiator.listeners.PlayerGeneralListeners;
import com.zpedroo.gladiator.managers.FileManager;
import com.zpedroo.gladiator.utils.inv.InvUtils;
import com.zpedroo.gladiator.utils.item.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Random;

public class Main extends JavaPlugin {

    private static Main main;
    public static Main get() { return main; }

    private SQLiteConnector sqLiteConnector;
    private DataManager dataManager;
    private InvUtils invUtils;
    private ItemUtils itemUtils;
    private int randomNumber;

    private HashMap<String, FileManager> files = new HashMap<>(1);

    public void onEnable() {
        main = this;
        // Loading instances
        sqLiteConnector = new SQLiteConnector("gladiator");
        dataManager = new DataManager();
        invUtils = new InvUtils();
        itemUtils = new ItemUtils();
        // Loading files
        getFiles().put("CONFIG", new FileManager("", "config", "configuration-files/config"));
        // Loading commands
        getCommand("gladiator").setExecutor(new GladiatorCmd(getFiles().get("CONFIG")));
        getCommand("gladiatorcoin").setExecutor(new GladiatorCoinCmd());
        getCommand("gladiatorhelmet").setExecutor(new GladiatorHelmetCmd());
        // Loading listeners
        getServer().getPluginManager().registerEvents(new InvActionListener(this, getItemUtils(), getFiles().get("CONFIG")), this);
        getServer().getPluginManager().registerEvents(new PlayerGeneralListeners(), this);
        setupTasks();
    }

    public void onDisable() {
        getDataManager().saveAll();
        getSQLiteConnector().closeConnection();
    }

    public SQLiteConnector getSQLiteConnector() {
        return sqLiteConnector;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public InvUtils getInvUtils() {
        return invUtils;
    }

    public ItemUtils getItemUtils() {
        return itemUtils;
    }

    public HashMap<String, FileManager> getFiles() {
        return files;
    }

    public int getRandomNumber() {
        return randomNumber;
    }

    private void setupTasks() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (getFiles().get("CONFIG").get().getString("Random-Number") == null) {
                    return;
                }

                int min = Integer.parseInt(getFiles().get("CONFIG").get().getString("Random-Number").split(",")[0]);
                int max = Integer.parseInt(getFiles().get("CONFIG").get().getString("Random-Number").split(",")[1]);
                randomNumber = new Random().nextInt(max + 1 - min) + min;

                if (getFiles().get("CONFIG").get().getStringList("Messages.new-items") != null) {
                    for (String msg : getFiles().get("CONFIG").get().getStringList("Messages.new-items")) {
                        Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg)));
                    }
                }

                if (getFiles().get("CONFIG").get().getStringList("Sounds.new-items") != null) {
                    Sound sound = Sound.valueOf(getFiles().get("CONFIG").get().getString("Sounds.new-items.sound"));
                    float volume = getFiles().get("CONFIG").get().getInt("Sounds.new-items.volume");
                    float pitch = getFiles().get("CONFIG").get().getInt("Sounds.new-items.pitch");

                    Bukkit.getOnlinePlayers().forEach(player -> player.playSound(player.getLocation(), sound, volume, pitch));
                }
            }
        }.runTaskTimerAsynchronously(this, 0L, 20L * 60L * getFiles().get("CONFIG").get().getInt("TimeToChange"));
    }
}