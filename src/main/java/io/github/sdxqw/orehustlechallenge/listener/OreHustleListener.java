package io.github.sdxqw.orehustlechallenge.listener;

import io.github.sdxqw.orehustlechallenge.OreHustleChallenge;
import io.github.sdxqw.orehustlechallenge.manager.DataManager;
import io.github.sdxqw.orehustlechallenge.manager.EventManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.List;

/**
 * Listens for block break events and adds to the player's ore count if the block is in the config's blocklist.
 *
 * @see DataManager
 * @see EventManager
 */
public class OreHustleListener implements Listener {

    private final DataManager dataManager;
    private final EventManager eventManager;

    /**
     * Constructs a new OreHustleListener with the given DataManager and EventManager.
     * @param dataManager the DataManager to use
     * @param eventManager the EventManager to use
     */
    public OreHustleListener(DataManager dataManager, EventManager eventManager) {
        this.dataManager = dataManager;
        this.eventManager = eventManager;
    }

    /**
     * Adds to the player's ore count if the block is in the config's blocklist.
     * @param event the BlockBreakEvent to handle
     */
    @EventHandler
    public void on(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (getConfigOres().contains(event.getBlock().getType()) && eventManager.isEventRunning()) {
            dataManager.addOre(player.getUniqueId(), 1);
        }
    }

    /**
     * Gets the list of ores from the config.
     * @return the list of ores from the config
     */
    private List<Material> getConfigOres() {
        return OreHustleChallenge.getInstance().getConf().getStringList("ore_types").stream().map(Material::valueOf).toList();
    }

}
