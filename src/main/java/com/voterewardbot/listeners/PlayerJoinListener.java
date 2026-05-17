package com.voterewardbot.listeners;

import com.voterewardbot.VoteRewardBot;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    
    private final VoteRewardBot plugin;
    
    public PlayerJoinListener(VoteRewardBot plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        if (plugin.getConfigManager().getConfig().getBoolean("enable-offline-rewards", true)) {
            if (plugin.getQueueManager().hasPendingRewards(player.getUniqueId())) {
                plugin.getQueueManager().deliverPendingRewards(player);
            }
        }
    }
}