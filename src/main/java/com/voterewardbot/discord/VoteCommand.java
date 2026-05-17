package com.voterewardbot.discord;

import com.voterewardbot.VoteRewardBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.awt.*;
import java.util.UUID;

public class VoteCommand extends ListenerAdapter {
    
    private final VoteRewardBot plugin;
    
    public VoteCommand(VoteRewardBot plugin) {
        this.plugin = plugin;
    }
    
    public CommandData getCommandData() {
        return Commands.slash("vote", "Vote for the server and receive rewards!");
    }
    
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("vote")) {
            return;
        }
        
        String voteChannelId = plugin.getConfigManager().getConfig().getString("vote-channel-id");
        
        if (event.getGuild() == null) {
            sendErrorEmbed(event, "This command can only be used in the server!");
            return;
        }
        
        if (!event.getChannel().getId().equals(voteChannelId)) {
            sendErrorEmbed(event, "This command can only be used in <#" + voteChannelId + ">!");
            return;
        }
        
        TextInput usernameInput = TextInput.create("minecraft_username", "Minecraft Username", TextInputStyle.SHORT)
                .setPlaceholder("Enter your Minecraft username")
                .setRequired(true)
                .setMinLength(3)
                .setMaxLength(16)
                .build();
        
        Modal modal = Modal.create("vote_modal", "Vote for the Server")
                .addComponents(ActionRow.of(usernameInput))
                .build();
        
        event.replyModal(modal).queue();
    }
    
    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        if (!event.getModalId().equals("vote_modal")) {
            return;
        }
        
        String minecraftUsername = event.getValue("minecraft_username").getAsString();
        String discordId = event.getUser().getId();
        
        event.deferReply(true).queue();
        
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            if (plugin.getCooldownManager().isOnCooldown(discordId)) {
                long remaining = plugin.getCooldownManager().getRemainingCooldown(discordId);
                String timeFormatted = plugin.getCooldownManager().formatRemainingTime(remaining);
                
                EmbedBuilder embed = new EmbedBuilder()
                        .setTitle("⏰ Cooldown Active")
                        .setDescription("You can vote again in **" + timeFormatted + "**")
                        .setColor(Color.decode("#ff5555"));
                
                event.getHook().sendMessageEmbeds(embed.build()).setEphemeral(true).queue();
                return;
            }
            
            Player player = Bukkit.getPlayer(minecraftUsername);
            
            if (player == null || !player.isOnline()) {
                if (!plugin.getConfigManager().getConfig().getBoolean("enable-offline-rewards", true)) {
                    EmbedBuilder embed = new EmbedBuilder()
                            .setTitle("❌ Player Offline")
                            .setDescription("Player **" + minecraftUsername + "** is not online!")
                            .setColor(Color.decode("#ff5555"));
                    
                    event.getHook().sendMessageEmbeds(embed.build()).setEphemeral(true).queue();
                    return;
                }
                
                Player offlinePlayer = Bukkit.getOfflinePlayer(minecraftUsername).getPlayer();
                UUID playerUUID = Bukkit.getOfflinePlayer(minecraftUsername).getUniqueId();
                
                plugin.getDataManager().createOrUpdateVoteRecord(discordId, minecraftUsername);
                plugin.getStreakManager().updateStreak(discordId);
                
                plugin.getQueueManager().addToQueue(playerUUID, "vote1");
                
                int streak = plugin.getStreakManager().getStreak(discordId);
                String streakReward = plugin.getStreakManager().getStreakReward(streak);
                if (streakReward != null) {
                    plugin.getQueueManager().addToQueue(playerUUID, streakReward);
                }
                
                EmbedBuilder embed = new EmbedBuilder()
                        .setTitle("✅ Vote Recorded!")
                        .setDescription("Thank you for voting, **" + minecraftUsername + "**!\n\n" +
                                "Your rewards will be delivered when you join the server.\n" +
                                "Current streak: **" + streak + " day(s)**")
                        .setColor(Color.decode(plugin.getConfigManager().getConfig().getString("embed-color", "#00aaff")));
                
                event.getHook().sendMessageEmbeds(embed.build()).setEphemeral(true).queue();
                return;
            }
            
            plugin.getDataManager().createOrUpdateVoteRecord(discordId, minecraftUsername);
            plugin.getStreakManager().updateStreak(discordId);
            
            Bukkit.getScheduler().runTask(plugin, () -> {
                plugin.getRewardManager().giveReward(player, "vote1");
                
                int streak = plugin.getStreakManager().getStreak(discordId);
                String streakReward = plugin.getStreakManager().getStreakReward(streak);
                if (streakReward != null) {
                    plugin.getRewardManager().giveReward(player, streakReward);
                }
            });
            
            int streak = plugin.getStreakManager().getStreak(discordId);
            
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("✅ Vote Successful!")
                    .setDescription("Thank you for voting, **" + minecraftUsername + "**!\n\n" +
                            "Your rewards have been delivered in-game.\n" +
                            "Current streak: **" + streak + " day(s)**")
                    .setColor(Color.decode(plugin.getConfigManager().getConfig().getString("embed-color", "#00aaff")));
            
            event.getHook().sendMessageEmbeds(embed.build()).setEphemeral(true).queue();
        });
    }
    
    private void sendErrorEmbed(SlashCommandInteractionEvent event, String message) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("❌ Error")
                .setDescription(message)
                .setColor(Color.decode("#ff5555"));
        
        event.replyEmbeds(embed.build()).setEphemeral(true).queue();
    }
}