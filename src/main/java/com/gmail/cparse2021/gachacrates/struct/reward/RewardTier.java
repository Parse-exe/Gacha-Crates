package com.gmail.cparse2021.gachacrates.struct.reward;

import com.gmail.cparse2021.gachacrates.util.ItemBuilder;
import com.gmail.cparse2021.gachacrates.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class RewardTier {
    private HashMap<Reward, Double> rewardProbabilityMap = new HashMap<>();
    private final String name;
    private int pityLimit = 0;
    private boolean pityEnabled = false;
    private ItemStack displayItem = new ItemBuilder(Material.WHITE_STAINED_GLASS_PANE).setDisplayName("&7Reward Tier").build();
    private Color color = Color.SILVER;

    public RewardTier(String name) {
        this.name = name;
    }

    /**
     * Generate a random reward based on set probability
     *
     * @return Generated RewardTier
     */
    public Reward generateReward() {
        double randDouble = Math.random();
        double count = 0.0;

        for (Map.Entry<Reward, Double> rewardProbability : rewardProbabilityMap.entrySet()) {
            count += rewardProbability.getValue();

            if (randDouble <= count) {
                return rewardProbability.getKey();
            }
        }

        return rewardProbabilityMap.entrySet().iterator().next().getKey();
    }

    public Color getColor() {
        return color;
    }

    public ItemStack getDisplayItem() {
        return displayItem;
    }

    public String getName() {
        return name;
    }

    public int getPityLimit() {
        return pityLimit;
    }

    public Set<Reward> getRewards() {
        return rewardProbabilityMap.keySet();
    }

    public boolean isPityEnabled() {
        return pityEnabled;
    }

    public void loadFrom(ConfigurationSection config) {
        ConfigurationSection rewards = config.getConfigurationSection("Rewards");

        this.pityEnabled = Boolean.parseBoolean(config.getString("Pity", "false"));
        this.pityLimit = config.getInt("Pity-Limit", 0);
        this.displayItem = Utils.decodeItem(config.getString("Display-Item", "WHITE_STAINED_GLASS_PANE name:&7" + name));
        this.color = Color.fromRGB(config.getInt("Color.R", 255), config.getInt("Color.G", 255), config.getInt("Color.B", 255));

        if (rewards != null) {
            for (String rewardName : rewards.getKeys(false)) {
                ConfigurationSection rewardsSection = rewards.getConfigurationSection(rewardName);
                Reward reward = new Reward(rewardName);
                double chance = rewards.getDouble(rewardName + ".Chance", 10) / 100;

                assert rewardsSection != null;
                reward.loadFrom(rewardsSection);
                rewardProbabilityMap.put(reward, chance);
            }

            sortProbabilityMap();
        } else {
            Bukkit.getLogger().log(Level.WARNING, "[GachaCrates] No rewards specified for reward tier `" + name + "`");
        }
    }

    /**
     * Sort the probability map
     */
    private void sortProbabilityMap() {
        List<Map.Entry<Reward, Double>> probabilityMapList = new LinkedList<>(rewardProbabilityMap.entrySet());

        probabilityMapList.sort(Map.Entry.comparingByValue());
        rewardProbabilityMap = probabilityMapList.stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (prev, next) -> next, HashMap::new));
    }
}
