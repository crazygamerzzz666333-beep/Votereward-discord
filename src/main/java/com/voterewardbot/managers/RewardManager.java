package com.voterewardbot.managers;

import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XMaterial;
import com.voterewardbot.VoteRewardBot;
import com.voterewardbot.models.RewardItem;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class RewardManager {
    
    private final VoteRewardBot plugin;
    private final Map<String, RewardItem> rewards;
    
    public RewardManager(VoteRewardBot plugin) {
        this.plugin = plugin;
        this.rewards = new HashMap<>();
        loadRewards();
    }
    
    private void loadRewards() {
        FileConfiguration rewardsConfig = plugin.getConfigManager().getRewards();
        ConfigurationSection rewardsSection = rewardsConfig.getConfigurationSection("rewards");
        
        if (rewardsSection != null) {
            for (String rewardId : rewardsSection.getKeys(false)) {
                ConfigurationSection rewardSection = rewardsSection.getConfigurationSection(rewardId);
                if (rewardSection != null) {
                    RewardItem reward = loadRewardFromSection(rewardId, rewardSection);
                    rewards.put(rewardId, reward);
                }
            }
        }
        
        plugin.getLogger().info("Loaded " + rewards.size() + " rewards");
    }
    
    private RewardItem loadRewardFromSection(String id, ConfigurationSection section) {
        String materialName = section.getString("material", "DIAMOND");
        int amount = section.getInt("amount", 1);
        String displayName = section.getString("display-name");
        List<String> lore = section.getStringList("lore");
        int customModelData = section.getInt("custom-model-data", -1);
        
        Map<String, Integer> enchants = new HashMap<>();
        ConfigurationSection enchantsSection = section.getConfigurationSection("enchants");
        if (enchantsSection != null) {
            for (String enchantName : enchantsSection.getKeys(false)) {
                enchants.put(enchantName, enchantsSection.getInt(enchantName));
            }
        }
        
        return new RewardItem(id, materialName, amount, displayName, lore, enchants, customModelData);
    }
    
    public void reload() {
        rewards.clear();
        loadRewards();
    }
    
    public ItemStack createItemStack(RewardItem reward) {
        Optional<XMaterial> xMaterial = XMaterial.matchXMaterial(reward.getMaterial());
        if (!xMaterial.isPresent()) {
            plugin.getLogger().warning("Invalid material: " + reward.getMaterial());
            return null;
        }
        
        ItemStack item = xMaterial.get().parseItem();
        if (item == null) {
            return null;
        }
        
        item.setAmount(reward.getAmount());
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            if (reward.getDisplayName() != null) {
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', reward.getDisplayName()));
            }
            
            if (reward.getLore() != null && !reward.getLore().isEmpty()) {
                List<String> coloredLore = new ArrayList<>();
                for (String line : reward.getLore()) {
                    coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
                }
                meta.setLore(coloredLore);
            }
            
            if (reward.getCustomModelData() > 0) {
                meta.setCustomModelData(reward.getCustomModelData());
            }
            
            item.setItemMeta(meta);
        }
        
        if (reward.getEnchants() != null && !reward.getEnchants().isEmpty()) {
            for (Map.Entry<String, Integer> entry : reward.getEnchants().entrySet()) {
                XEnchantment.matchXEnchantment(entry.getKey()).ifPresent(xEnchant -> {
                    item.addUnsafeEnchantment(xEnchant.getEnchant(), entry.getValue());
                });
            }
        }
        
        return item;
    }
    
    public void giveReward(Player player, String rewardId) {
        RewardItem reward = rewards.get(rewardId);
        if (reward == null) {
            plugin.getLogger().warning("Reward not found: " + rewardId);
            return;
        }
        
        ItemStack item = createItemStack(reward);
        if (item != null) {
            player.getInventory().addItem(item);
            player.sendMessage(ChatColor.GREEN + "You received a vote reward!");
        }
    }
    
    public void saveItemAsReward(String rewardId, ItemStack item) {
        FileConfiguration rewardsConfig = plugin.getConfigManager().getRewards();
        
        rewardsConfig.set("rewards." + rewardId + ".material", item.getType().name());
        rewardsConfig.set("rewards." + rewardId + ".amount", item.getAmount());
        
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (meta.hasDisplayName()) {
                rewardsConfig.set("rewards." + rewardId + ".display-name", meta.getDisplayName());
            }
            
            if (meta.hasLore()) {
                rewardsConfig.set("rewards." + rewardId + ".lore", meta.getLore());
            }
            
            if (meta.hasCustomModelData()) {
                rewardsConfig.set("rewards." + rewardId + ".custom-model-data", meta.getCustomModelData());
            }
            
            if (!meta.getEnchants().isEmpty()) {
                for (Map.Entry<Enchantment, Integer> entry : meta.getEnchants().entrySet()) {
                    rewardsConfig.set("rewards." + rewardId + ".enchants." + entry.getKey().getKey().getKey(), entry.getValue());
                }
            }
        }
        
        plugin.getConfigManager().saveRewards();
        reload();
    }
    
    public boolean removeReward(String rewardId) {
        if (rewards.containsKey(rewardId)) {
            FileConfiguration rewardsConfig = plugin.getConfigManager().getRewards();
            rewardsConfig.set("rewards." + rewardId, null);
            plugin.getConfigManager().saveRewards();
            rewards.remove(rewardId);
            return true;
        }
        return false;
    }
    
    public Map<String, RewardItem> getAllRewards() {
        return new HashMap<>(rewards);
    }
    
    public RewardItem getReward(String rewardId) {
        return rewards.get(rewardId);
    }
}