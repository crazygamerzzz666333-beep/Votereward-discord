package com.voterewardbot.managers;

import com.voterewardbot.VoteRewardBot;
import com.voterewardbot.discord.VoteCommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.bukkit.Bukkit;

public class DiscordBotManager {
    
    private final VoteRewardBot plugin;
    private JDA jda;
    
    public DiscordBotManager(VoteRewardBot plugin) {
        this.plugin = plugin;
    }
    
    public void startBot() {
        String token = plugin.getConfigManager().getConfig().getString("bot-token");
        
        if (token == null || token.equals("YOUR_BOT_TOKEN_HERE")) {
            plugin.getLogger().severe("Bot token not configured! Please set bot-token in config.yml");
            return;
        }
        
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                jda = JDABuilder.createDefault(token)
                        .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
                        .setActivity(Activity.playing("Vote with /vote"))
                        .build();
                
                jda.awaitReady();
                
                VoteCommand voteCommand = new VoteCommand(plugin);
                jda.addEventListener(voteCommand);
                jda.updateCommands().addCommands(voteCommand.getCommandData()).queue();
                
                plugin.getLogger().info("Discord bot connected successfully!");
                
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to start Discord bot: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    
    public void shutdownBot() {
        if (jda != null) {
            jda.shutdown();
            plugin.getLogger().info("Discord bot disconnected");
        }
    }
    
    public JDA getJDA() {
        return jda;
    }
}