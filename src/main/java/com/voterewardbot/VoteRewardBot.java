package com.voterewardbot;

import com.voterewardbot.commands.VoteRewardCommand;
import com.voterewardbot.listeners.PlayerJoinListener;
import com.voterewardbot.managers.*;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

@Getter
public class VoteRewardBot extends JavaPlugin {
    
    private ConfigManager configManager;
    private DataManager dataManager;
    private RewardManager rewardManager;
    private CooldownManager cooldownManager;
    private QueueManager queueManager;
    private StreakManager streakManager;
    private DiscordBotManager discordBotManager;
    
    @Override
    public void onEnable() {
        createDataFolders();
        
        this.configManager = new ConfigManager(this);
        this.configManager.loadAllConfigs();
        
        this.dataManager = new DataManager(this);
        this.rewardManager = new RewardManager(this);
        this.cooldownManager = new CooldownManager(this);
        this.queueManager = new QueueManager(this);
        this.streakManager = new StreakManager(this);
        
        getCommand("votereward").setExecutor(new VoteRewardCommand(this));
        
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        
        this.discordBotManager = new DiscordBotManager(this);
        this.discordBotManager.startBot();
        
        getLogger().info("VoteRewardBot enabled successfully!");
    }
    
    @Override
    public void onDisable() {
        if (this.discordBotManager != null) {
            this.discordBotManager.shutdownBot();
        }
        
        if (this.dataManager != null) {
            this.dataManager.saveAll();
        }
        
        getLogger().info("VoteRewardBot disabled successfully!");
    }
    
    private void createDataFolders() {
        File dataFolder = getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        
        File logsFolder = new File(dataFolder, "logs");
        if (!logsFolder.exists()) {
            logsFolder.mkdirs();
        }
    }
    
    public void reloadPlugin() {
        this.configManager.loadAllConfigs();
        this.dataManager.reload();
        this.rewardManager.reload();
        
        if (this.discordBotManager != null) {
            this.discordBotManager.shutdownBot();
            this.discordBotManager.startBot();
        }
    }
}