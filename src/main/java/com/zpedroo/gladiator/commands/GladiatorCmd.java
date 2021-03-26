package com.zpedroo.gladiator.commands;

import com.zpedroo.gladiator.Main;
import com.zpedroo.gladiator.data.PlayerData;
import com.zpedroo.gladiator.managers.FileManager;
import com.zpedroo.gladiator.utils.builder.ItemBuilder;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.spigotmc.SpigotConfig;

import java.util.ArrayList;
import java.util.List;

public class GladiatorCmd implements CommandExecutor {

    private FileManager file;
    private String TITLE;
    private int ROWS;

    public GladiatorCmd(FileManager file) {
        this.file = file;
        this.TITLE = file.get().getString("Inventory.title");
        this.ROWS = file.get().getInt("Inventory.rows");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            sender.sendMessage(SpigotConfig.unknownCommandMessage);
            return true;
        }
        if (args.length == 0) return true;

        Player player = Bukkit.getPlayer(args[0]);
        if (player == null) return true;

        if (args.length == 1) {
            openGladiatorInventory(player);
            return true;
        }

        return false;
    }

    private void openGladiatorInventory(Player player) {
        Inventory inventory = Bukkit.createInventory(null, ROWS*9, TITLE);
        PlayerData data = Main.get().getDataManager().loadPlayer(player);

        if (!data.isTrusted()) {
            for (String str : file.get().getConfigurationSection("Inventory.no-trust").getKeys(false)) {
                int slot = file.get().getInt("Inventory.no-trust." + str + ".slot");
                String action = file.get().getString("Inventory.no-trust." + str + ".action", "NULL");
                ItemStack item = ItemBuilder.build(file, "Inventory.no-trust." + str).build();
                if (item == null) return;

                if (!StringUtils.equalsIgnoreCase(action, "NULL")) {
                    Main.get().getItemUtils().setItemAction(slot, () -> {
                        if (player == null || inventory == null) return;

                        switch (action) {
                            case "GIVE_HELMET":
                                ItemStack helmet = ItemBuilder.build(Main.get().getFiles().get("CONFIG"), "HelmetItem").build();
                                if (getItemAmount(player, helmet) < 1) {
                                    Sound sound = Sound.valueOf(Main.get().getFiles().get("CONFIG").get().getString("Sounds.insufficient-helmet.sound"));
                                    float volume = Main.get().getFiles().get("CONFIG").get().getInt("Sounds.insufficient-helmet.volume");
                                    float pitch = Main.get().getFiles().get("CONFIG").get().getInt("Sounds.insufficient-helmet.pitch");
                                    player.playSound(player.getLocation(), sound, volume, pitch);
                                    return;
                                }

                                Sound sound = Sound.valueOf(Main.get().getFiles().get("CONFIG").get().getString("Sounds.trusted.sound"));
                                float volume = Main.get().getFiles().get("CONFIG").get().getInt("Sounds.trusted.volume");
                                float pitch = Main.get().getFiles().get("CONFIG").get().getInt("Sounds.trusted.pitch");
                                player.playSound(player.getLocation(), sound, volume, pitch);
                                player.getInventory().removeItem(helmet);
                                data.setTrusted(true);
                                player.closeInventory();

                                Main.get().getServer().getScheduler().runTaskLaterAsynchronously(Main.get(), () -> openGladiatorInventory(player), 1L);
                        }
                    }, true);
                }

                inventory.setItem(slot, item);
            }

            player.openInventory(inventory);
            return;
        }

        for (String str : file.get().getConfigurationSection("Inventory.items").getKeys(false)) {
            int slot = file.get().getInt("Inventory.items." + str + ".slot");
            String action = file.get().getString("Inventory.items." + str + ".action", "NULL");
            ItemStack itemToShow = ItemBuilder.build(file, "Inventory.items." + str, new String[]{
                    "%random-number%"
            }, new String[]{
                    String.valueOf(Main.get().getRandomNumber())
            }).build();
            if (itemToShow == null) return;
            ItemStack gladiatorCoin = ItemBuilder.build(Main.get().getFiles().get("CONFIG"), "TradeItem").build();

            if (!StringUtils.equalsIgnoreCase(action, "NULL")) {
                if (action.equals("BUY")) {

                    ItemMeta meta = itemToShow.getItemMeta();
                    ArrayList<String> lore;
                    if (meta.hasLore()) {
                        lore = (ArrayList<String>) meta.getLore();
                    } else {
                        lore = new ArrayList<>();
                    }

                    for (String extraLore : file.get().getStringList("ExtraLore")) {
                        if (extraLore == null) break;

                        lore.add(ChatColor.translateAlternateColorCodes('&', StringUtils.replaceEach(extraLore, new String[]{
                                "%cost%"
                        }, new String[]{
                                file.get().getString("Inventory.items." + str + ".cost")
                        })));
                    }

                    meta.setLore(lore);
                    itemToShow.setItemMeta(meta);
                }

                Main.get().getItemUtils().setItemAction(slot, () -> {
                    if (player == null || inventory == null) return;

                    switch (action) {
                        case "BUY":
                            if (getItemAmount(player, gladiatorCoin) < file.get().getInt("Inventory.items." + str + ".cost")) {
                                Sound sound = Sound.valueOf(Main.get().getFiles().get("CONFIG").get().getString("Sounds.insufficient-coins.sound"));
                                float volume = Main.get().getFiles().get("CONFIG").get().getInt("Sounds.insufficient-coins.volume");
                                float pitch = Main.get().getFiles().get("CONFIG").get().getInt("Sounds.insufficient-coins.pitch");
                                player.playSound(player.getLocation(), sound, volume, pitch);
                                return;
                            }

                            List<String> cmds = file.get().getStringList("Inventory.items." + str + ".commands");
                            if (cmds.size() > 0) {
                                for (String cmd : cmds) {
                                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), StringUtils.replaceEach(cmd, new String[]{
                                            "%player%"
                                    }, new String[]{
                                            player.getName()
                                    }));
                                }
                            } else {
                                player.getInventory().addItem(ItemBuilder.build(file, "Inventory.items." + str, new String[]{
                                        "%random-number%"
                                }, new String[]{
                                        String.valueOf(Main.get().getRandomNumber())
                                }).build());
                            }

                            gladiatorCoin.setAmount(file.get().getInt("Inventory.items." + str + ".cost"));
                            Sound sound = Sound.valueOf(Main.get().getFiles().get("CONFIG").get().getString("Sounds.bought.sound"));
                            float volume = Main.get().getFiles().get("CONFIG").get().getInt("Sounds.bought.volume");
                            float pitch = Main.get().getFiles().get("CONFIG").get().getInt("Sounds.bought.pitch");

                            player.playSound(player.getLocation(), sound, volume, pitch);
                            player.getInventory().removeItem(gladiatorCoin);
                            break;
                        case "CLOSE":
                            player.closeInventory();
                    }
                }, true);
            }

            inventory.setItem(slot, itemToShow);
        }
        player.openInventory(inventory);
    }

    private int getItemAmount(Player player, ItemStack item) {
        int amount = 0;
        for (ItemStack items : player.getInventory().getContents()) {
            if (items == null) continue;

            if (items.isSimilar(item)) {
                amount += items.getAmount();
            }
        }

        return amount;
    }
}