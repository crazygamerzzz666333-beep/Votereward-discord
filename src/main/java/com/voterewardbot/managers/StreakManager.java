package com.voterewardbot.managers;

import com.voterewardbot.VoteRewardBot;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class StreakManager {
    
    private final VoteRewardBot plugin;
    private final Map<String, Integer> streaks;
    private final Map<String, Long> lastStreakUpdate;
    
    public StreakManager(VoteRewardBot plugin) {
        this.plugin = plugin;
        this.streaks = new HashMap<>();
        this.lastStreakUpdate = new HashMap<>();
        loadStreaks();
    }
    
    private void loadStreaks() {
        FileConfiguration data = plugin.getConfigManager().getData();
        
        if (data.contains("streaks")) {
            for (String discordId : data.getConfigurationSection("streaks").getKeys(false)) {
                int streak = data.getInt("streaks." + discordId + ".count");
                long lastUpdate = data.getLong("streaks." + discordId + ".last-update");
                
                streaks.put(discordId, streak);
                lastStreakUpdate.put(discordId, lastUpdate);
            }
        }
    }
    
    public void updateStreak(String discordId) {
        if (!plugin.getConfigManager().getConfig().getBoolean("enable-streaks", true)) {
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        Long lastUpdate = lastStreakUpdate.get(discordId);
        
        if (lastUpdate == null) {
            streaks.put(discordId, 1);
            lastStreakUpdate.put(discordId, currentTime);
        } else {
            long timeSinceLastVote = currentTime - lastUpdate;
            long oneDayMillis = TimeUnit.DAYS.toMillis(1);
            long twoDaysMillis = TimeUnit.DAYS.toMillis(2);
            
            if (timeSinceLastVote <= twoDaysMillis) {
                int currentStreak = streaks.getOrDefault(discordId, 0);
                streaks.put(discordId, currentStreak + 1);
                lastStreakUpdate.put(discordId, currentTime);
            } else {
                streaks.put(discordId, 1);
                lastStreakUpdate.put(discordId, currentTime);
            }
        }
        
        saveStreaks();
    }
    
    public int getStreak(String discordId) {
        return streaks.getOrDefault(discordId, 0);
    }
    
    public String getStreakReward(int streak) {
        if (streak >= 7) {
            return "streak7";
        } else if (streak >= 3) {
            return "streak3";
        }
        return null;
    }
    
    private void saveStreaks() {
        FileConfiguration data = plugin.getConfigManager().getData();
        
        for (Map.Entry<String, Integer> entry : streaks.entrySet()) {
            String discordId = entry.getKey();
            data.set("streaks." + discordId + ".count", entry.getValue());
            data.set("streaks." + discordId + ".last-update", lastStreakUpdate.get(discordId));
        }
        
        plugin.getConfigManager().saveData();
    }
}