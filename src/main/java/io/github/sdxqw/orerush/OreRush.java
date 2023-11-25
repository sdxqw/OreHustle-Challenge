package io.github.sdxqw.orerush;

import io.github.sdxqw.orerush.listener.OreRushListener;
import io.github.sdxqw.orerush.manager.DataManager;
import io.github.sdxqw.orerush.manager.EventManager;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

@Getter
public final class OreRush extends JavaPlugin {

    @Getter
    private static OreRush instance;

    private FileConfiguration conf;

    private EventManager eventManager;
    private DataManager dataManager;

    private FileConfiguration rewardConfig;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        loadCustomConfig();

        conf = getConfig();

        dataManager = new DataManager();
        eventManager = new EventManager(dataManager);
        eventManager.scheduleEvents();

        getServer().getPluginManager().registerEvents(eventManager, this);
        getServer().getPluginManager().registerEvents(new OreRushListener(dataManager, eventManager), this);
    }

    private void loadCustomConfig() {
        File rewardFile = new File(getDataFolder(), "rewards.yml");
        if (!rewardFile.exists()) {
            saveResource("rewards.yml", false);
        }

        rewardConfig = YamlConfiguration.loadConfiguration(rewardFile);
    }
}