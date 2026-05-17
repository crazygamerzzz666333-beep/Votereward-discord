package com.voterewardbot.managers;

import com.voterewardbot.VoteRewardBot;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigManager {
    
    private final VoteRewardBot plugin;
    private FileConfiguration config;
    private FileConfiguration rewards;
    private FileConfiguration data;
    private FileConfiguration queue;
    
    private File configFile;
    private File rewardsFile;
    private File dataFile;
    private File queueFile;
    
    public ConfigManager(VoteRewardBot plugin) {
        this.plugin = plugin;
    }
    
    public void loadAllConfigs() {
        this.configFile = new File(plugin.getDataFolder(), "config.yml");
        this.rewardsFile = new File(plugin.getDataFolder(), "rewards.yml");
        this.dataFile = new File(plugin.getDataFolder(), "data.yml");
        this.queueFile = new File(plugin.getDataFolder(), "queue.yml");
        
        createDefaultConfig();
        createDefaultRewards();
        createDefaultData();
        createDefaultQueue();
        
        this.config = YamlConfiguration.loadConfiguration(configFile);
        this.rewards = YamlConfiguration.loadConfiguration(rewardsFile);
        this.data = YamlConfiguration.loadConfiguration(dataFile);
        this.queue = YamlConfiguration.loadConfiguration(queueFile);
    }
    
    private void createDefaultConfig() {
        if (!configFile.exists()) {
            config = new YamlConfiguration();
            config.set("bot-token", "YOUR_BOT_TOKEN_HERE");
            config.set("guild-id", "YOUR_GUILD_ID_HERE");
            config.set("vote-channel-id", "YOUR_VOTE_CHANNEL_ID_HERE");
            config.set("vote-cooldown-hours", 24);
            config.set("embed-color", "#00aaff");
            config.set("enable-streaks", true);
            config.set("enable-offline-rewards", true);
            saveConfig(config, configFile);
        }
    }
    
    private void createDefaultRewards() {
        if (!rewardsFile.exists()) {
            rewards = new YamlConfiguration();
            rewards.set("rewards.vote1.material", "DIAMOND");
            rewards.set("rewards.vote1.amount", 5);
            rewards.set("rewards.vote1.display-name", "&bVote Reward");
            rewards.set("rewards.vote1.lore", java.util.Arrays.asList("&7Thanks for voting!", "&7Come back tomorrow!"));
            
            rewards.set("rewards.streak3.material", "EMERALD");
            rewards.set("rewards.streak3.amount", 10);
            rewards.set("rewards.streak3.display-name", "&a3-Day Streak Bonus");
            rewards.set("rewards.streak3.lore", java.util.Arrays.asList("&7Keep voting daily!"));
            
            rewards.set("rewards.streak7.material", "NETHER_STAR");
            rewards.set("rewards.streak7.amount", 1);
            rewards.set("rewards.streak7.display-name", "&67-Day Streak Bonus");
            rewards.set("rewards.streak7.lore", java.util.Arrays.asList("&7Amazing dedication!"));
            
            saveConfig(rewards, rewardsFile);
        }
    }
    
    private void createDefaultData() {
        if (!dataFile.exists()) {
            data = new YamlConfiguration();
            data.set("votes", new java.util.HashMap<>());
            data.set("streaks", new java.util.HashMap<>());
            saveConfig(data, dataFile);
        }
    }
    
    private void createDefaultQueue() {
        if (!queueFile.exists()) {
            queue = new YamlConfiguration();
            queue.set("pending-rewards", new java.util.HashMap<>());
            saveConfig(queue, queueFile);
        }
    }
    
    private void saveConfig(FileConfiguration config, File file) {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save " + file.getName() + ": " + e.getMessage());
        }
    }
    
    public FileConfiguration getConfig() {
        return config;
    }
    
    public FileConfiguration getRewards() {
        return rewards;
    }
    
    public FileConfiguration getData() {
        return data;
    }
    
    public FileConfiguration getQueue() {
        return queue;
    }
    
    public void saveData() {
        saveConfig(data, dataFile);
    }
    
    public void saveQueue() {
        saveConfig(queue, queueFile);
    }
    
    public void saveRewards() {
        saveConfig(rewards, rewardsFile);
    }
}