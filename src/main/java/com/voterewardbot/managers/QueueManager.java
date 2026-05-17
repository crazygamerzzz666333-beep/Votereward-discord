package com.voterewardbot.managers;

import com.voterewardbot.VoteRewardBot;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;

public class QueueManager {
    
    private final VoteRewardBot plugin;
    private final Map<UUID, List<String>> pendingRewards;
    
    public QueueManager(VoteRewardBot plugin) {
        this.plugin = plugin;
        this.pendingRewards = new HashMap<>();
        loadQueue();
    }
    
    private void loadQueue() {
        FileConfiguration queue = plugin.getConfigManager().getQueue();
        
        if (queue.contains("pending-rewards")) {
            for (String uuidString : queue.getConfigurationSection("pending-rewards").getKeys(false)) {
                UUID playerUUID = UUID.fromString(uuidString);
                List<String> rewards = queue.getStringList("pending-rewards." + uuidString);
                pendingRewards.put(playerUUID, new ArrayList<>(rewards));
            }
        }
    }
    
    public void addToQueue(UUID playerUUID, String rewardId) {
        pendingRewards.computeIfAbsent(playerUUID, k -> new ArrayList<>()).add(rewardId);
        saveQueue();
    }
    
    public void deliverPendingRewards(Player player) {
        UUID playerUUID = player.getUniqueId();
        List<String> rewards = pendingRewards.get(playerUUID);
        
        if (rewards != null && !rewards.isEmpty()) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                for (String rewardId : rewards) {
                    plugin.getRewardManager().giveReward(player, rewardId);
                }
                
                player.sendMessage("§a§lVote Rewards! §7You received §e" + rewards.size() + " §7pending reward(s)!");
            });
            
            pendingRewards.remove(playerUUID);
            saveQueue();
        }
    }
    
    private void saveQueue() {
        FileConfiguration queue = plugin.getConfigManager().getQueue();
        queue.set("pending-rewards", null);
        
        for (Map.Entry<UUID, List<String>> entry : pendingRewards.entrySet()) {
            queue.set("pending-rewards." + entry.getKey().toString(), entry.getValue());
        }
        
        plugin.getConfigManager().saveQueue();
    }
    
    public boolean hasPendingRewards(UUID playerUUID) {
        List<String> rewards = pendingRewards.get(playerUUID);
        return rewards != null && !rewards.isEmpty();
    }
    
    public int getPendingRewardCount(UUID playerUUID) {
        List<String> rewards = pendingRewards.get(playerUUID);
        return rewards != null ? rewards.size() : 0;
    }
}