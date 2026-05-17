package com.voterewardbot.commands;

import com.voterewardbot.VoteRewardBot;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VoteRewardCommand implements CommandExecutor, TabCompleter {
    
    private final VoteRewardBot plugin;
    
    public VoteRewardCommand(VoteRewardBot plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "itemadd":
                return handleItemAdd(sender, args);
            case "remove":
                return handleRemove(sender, args);
            case "list":
                return handleList(sender);
            case "reload":
                return handleReload(sender);
            default:
                sendHelp(sender);
                return true;
        }
    }
    
    private boolean handleItemAdd(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return true;
        }
        
        if (!sender.hasPermission("votereward.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /votereward itemadd <reward-id>");
            return true;
        }
        
        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (item == null || item.getType() == Material.AIR) {
            player.sendMessage(ChatColor.RED + "You must hold an item in your hand!");
            return true;
        }
        
        String rewardId = args[1];
        plugin.getRewardManager().saveItemAsReward(rewardId, item);
        player.sendMessage(ChatColor.GREEN + "Reward '" + rewardId + "' saved successfully!");
        
        return true;
    }
    
    private boolean handleRemove(CommandSender sender, String[] args) {
        if (!sender.hasPermission("votereward.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /votereward remove <reward-id>");
            return true;
        }
        
        String rewardId = args[1];
        if (plugin.getRewardManager().removeReward(rewardId)) {
            sender.sendMessage(ChatColor.GREEN + "Reward '" + rewardId + "' removed successfully!");
        } else {
            sender.sendMessage(ChatColor.RED + "Reward '" + rewardId + "' not found!");
        }
        
        return true;
    }
    
    private boolean handleList(CommandSender sender) {
        if (!sender.hasPermission("votereward.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }
        
        sender.sendMessage(ChatColor.GOLD + "=== Vote Rewards ===");
        plugin.getRewardManager().getAllRewards().forEach((id, reward) -> {
            sender.sendMessage(ChatColor.YELLOW + id + ChatColor.GRAY + " - " + 
                    ChatColor.WHITE + reward.getMaterial() + " x" + reward.getAmount());
        });
        
        return true;
    }
    
    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("votereward.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }
        
        plugin.reloadPlugin();
        sender.sendMessage(ChatColor.GREEN + "VoteRewardBot reloaded successfully!");
        
        return true;
    }
    
    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== VoteRewardBot Commands ===");
        sender.sendMessage(ChatColor.YELLOW + "/votereward itemadd <id>" + ChatColor.GRAY + " - Save item in hand as reward");
        sender.sendMessage(ChatColor.YELLOW + "/votereward remove <id>" + ChatColor.GRAY + " - Remove a reward");
        sender.sendMessage(ChatColor.YELLOW + "/votereward list" + ChatColor.GRAY + " - List all rewards");
        sender.sendMessage(ChatColor.YELLOW + "/votereward reload" + ChatColor.GRAY + " - Reload configuration");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.addAll(Arrays.asList("itemadd", "remove", "list", "reload"));
        } else if (args.length == 2 && args[0].equalsIgnoreCase("remove")) {
            completions.addAll(plugin.getRewardManager().getAllRewards().keySet());
        }
        
        return completions;
    }
}