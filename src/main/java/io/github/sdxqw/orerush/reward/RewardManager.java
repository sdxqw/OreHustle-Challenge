package io.github.sdxqw.orerush.reward;

import io.github.sdxqw.orerush.OreRush;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages the rewards for the plugin.
 */
@Getter
public class RewardManager {
    private final Map<Integer, Player> playerToReward = new HashMap<>();
    private final FileConfiguration rewardConfig = OreRush.getInstance().getRewardConfig();

    /**
     * Adds the given player to the playerToReward map at the given position.
     * @param player the player to add
     * @param position the position to add the player at
     */
    public void addPlayer(Player player, int position) {
        playerToReward.put(position, player);
    }

    /**
     * Clears the playerToReward map.
     */
    public void clearMap() {
        playerToReward.clear();
    }

    /**
     * Gives the reward to the player at the given position.
     */
    public void giveReward() {
        var rewardsSection = OreRush.getInstance().getRewardConfig().getConfigurationSection("rewards");
        if (rewardsSection != null) {
            rewardsSection.getKeys(false).forEach(key -> {
                List<String> reward = rewardsSection.getStringList(key);
                if (key.contains("..")) {
                    String[] range = key.split("..");
                    int start = Integer.parseInt(range[0]);
                    int end = Integer.parseInt(range[1]);
                    for (int i = start; i <= end; i++) {
                        assignReward(i, reward);
                    }
                } else {
                    assignReward(Integer.parseInt(key), reward);
                }
            });
        }
    }

    /**
     * Assigns the reward to the player at the given position.
     * @param position the position to assign the reward to
     * @param reward the reward to assign
     */
    private void assignReward(int position, List<String> reward) {
        Player player = playerToReward.get(position);
        if (player != null) {
            reward.forEach(rewardLine -> {
                String replaced = rewardLine.replace("{player}", player.getName());
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), replaced);
                String message = OreRush.getInstance().getConf().getString("messages.event_reward");
                assert message != null;
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            });
        }
    }
}
