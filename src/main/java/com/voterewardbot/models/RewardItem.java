package com.voterewardbot.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class RewardItem {
    private String id;
    private String material;
    private int amount;
    private String displayName;
    private List<String> lore;
    private Map<String, Integer> enchants;
    private int customModelData;
}