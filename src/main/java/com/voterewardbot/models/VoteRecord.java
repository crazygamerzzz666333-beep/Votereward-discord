package com.voterewardbot.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VoteRecord {
    private String discordId;
    private String minecraftUsername;
    private long lastVoteTime;
    private int totalVotes;
}