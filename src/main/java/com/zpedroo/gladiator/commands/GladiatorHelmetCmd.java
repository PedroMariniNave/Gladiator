package com.zpedroo.gladiator.commands;

import com.zpedroo.gladiator.Main;
import com.zpedroo.gladiator.utils.builder.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.spigotmc.SpigotConfig;

import java.util.regex.Pattern;

public class GladiatorHelmetCmd implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(SpigotConfig.unknownCommandMessage);
            return true;
        }

        if (args.length < 3 || !isNumeric(args[2])) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Main.get().getFiles().get("CONFIG").get().getString("Messages.gladiator-helmet-usage")));
            return true;
        }


        if (args[0].toUpperCase().equals("GIVE")) {
            Player player = Bukkit.getPlayer(args[1]);
            if (player == null) return true;

            ItemStack item = ItemBuilder.build(Main.get().getFiles().get("CONFIG"), "HelmetItem").build();
            item.setAmount(Integer.parseInt(args[2]));
            player.getInventory().addItem(item);
        }
        return false;
    }

    private boolean isNumeric(String strNum) {
        if (strNum == null) return true;

        return Pattern.compile("-?\\d+(\\.\\d+)?").matcher(strNum).matches();
    }
}
