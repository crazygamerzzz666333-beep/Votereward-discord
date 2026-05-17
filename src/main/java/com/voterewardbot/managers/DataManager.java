package com.voterewardbot.managers;

import com.voterewardbot.VoteRewardBot;
import com.voterewardbot.models.VoteRecord;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DataManager {
    
    private final VoteRewardBot plugin;
    private final Map<String, VoteRecord> voteRecords;
    
    public DataManager(VoteRewardBot plugin) {
        this.plugin = plugin;
        this.voteRecords = new HashMap<>();
        loadData();
    }
    
    private void loadData() {
        FileConfiguration data = plugin.getConfigManager().getData();
        ConfigurationSection votesSection = data.getConfigurationSection("votes");
        
        if (votesSection != null) {
            for (String discordId : votesSection.getKeys(false)) {
                String minecraftUsername = votesSection.getString(discordId + ".minecraft-username");
                long lastVoteTime = votesSection.getLong(discordId + ".last-vote-time");
                int totalVotes = votesSection.getInt(discordId + ".total-votes");
                
                VoteRecord record = new VoteRecord(discordId, minecraftUsername, lastVoteTime, totalVotes);
                voteRecords.put(discordId, record);
            }
        }
    }
    
    public void saveAll() {
        FileConfiguration data = plugin.getConfigManager().getData();
        
        for (Map.Entry<String, VoteRecord> entry : voteRecords.entrySet()) {
            String discordId = entry.getKey();
            VoteRecord record = entry.getValue();
            
            data.set("votes." + discordId + ".minecraft-username", record.getMinecraftUsername());
            data.set("votes." + discordId + ".last-vote-time", record.getLastVoteTime());
            data.set("votes." + discordId + ".total-votes", record.getTotalVotes());
        }
        
        plugin.getConfigManager().saveData();
    }
    
    public void reload() {
        voteRecords.clear();
        loadData();
    }
    
    public VoteRecord getVoteRecord(String discordId) {
        return voteRecords.get(discordId);
    }
    
    public void createOrUpdateVoteRecord(String discordId, String minecraftUsername) {
        VoteRecord record = voteRecords.get(discordId);
        
        if (record == null) {
            record = new VoteRecord(discordId, minecraftUsername, System.currentTimeMillis(), 1);
        } else {
            record.setLastVoteTime(System.currentTimeMillis());
            record.setTotalVotes(record.getTotalVotes() + 1);
        }
        
        voteRecords.put(discordId, record);
        saveAll();
    }
    
    public Map<String, VoteRecord> getAllVoteRecords() {
        return new HashMap<>(voteRecords);
    }
}