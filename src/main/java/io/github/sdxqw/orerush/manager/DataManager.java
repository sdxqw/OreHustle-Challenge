package io.github.sdxqw.orerush.manager;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages the data for the plugin.
 *
 */
@Getter
public class DataManager {
    private final Map<UUID, Integer> playerOreCount = new HashMap<>();


    /**
     * Adds the given amount to the player's ore count.
     * @param uuid the UUID of the player
     * @param amount the amount to add
     */
    public void addOre(UUID uuid, int amount) {
        playerOreCount.put(uuid, playerOreCount.getOrDefault(uuid, 0) + amount);
    }

    /**
     * Clears the playerOreCount map.
     */
    public void clearMap() {
        playerOreCount.clear();
    }
}
