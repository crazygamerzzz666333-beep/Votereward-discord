package com.voterewardbot.managers;

import com.voterewardbot.VoteRewardBot;
import com.voterewardbot.models.VoteRecord;

import java.util.concurrent.TimeUnit;

public class CooldownManager {
    
    private final VoteRewardBot plugin;
    
    public CooldownManager(VoteRewardBot plugin) {
        this.plugin = plugin;
    }
    
    public boolean isOnCooldown(String discordId) {
        VoteRecord record = plugin.getDataManager().getVoteRecord(discordId);
        if (record == null) {
            return false;
        }
        
        long cooldownHours = plugin.getConfigManager().getConfig().getLong("vote-cooldown-hours", 24);
        long cooldownMillis = TimeUnit.HOURS.toMillis(cooldownHours);
        long timeSinceLastVote = System.currentTimeMillis() - record.getLastVoteTime();
        
        return timeSinceLastVote < cooldownMillis;
    }
    
    public long getRemainingCooldown(String discordId) {
        VoteRecord record = plugin.getDataManager().getVoteRecord(discordId);
        if (record == null) {
            return 0;
        }
        
        long cooldownHours = plugin.getConfigManager().getConfig().getLong("vote-cooldown-hours", 24);
        long cooldownMillis = TimeUnit.HOURS.toMillis(cooldownHours);
        long timeSinceLastVote = System.currentTimeMillis() - record.getLastVoteTime();
        long remaining = cooldownMillis - timeSinceLastVote;
        
        return remaining > 0 ? remaining : 0;
    }
    
    public String formatRemainingTime(long millis) {
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60;
        
        if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds);
        } else {
            return String.format("%ds", seconds);
        }
    }
}