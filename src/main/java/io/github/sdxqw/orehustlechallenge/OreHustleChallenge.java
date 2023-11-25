package io.github.sdxqw.orehustlechallenge;

import io.github.sdxqw.orehustlechallenge.listener.OreHustleListener;
import io.github.sdxqw.orehustlechallenge.manager.DataManager;
import io.github.sdxqw.orehustlechallenge.manager.EventManager;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

@Getter
public final class OreHustleChallenge extends JavaPlugin {

    @Getter
    private static OreHustleChallenge instance;

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
        getServer().getPluginManager().registerEvents(new OreHustleListener(dataManager, eventManager), this);
    }

    private void loadCustomConfig() {
        File rewardFile = new File(getDataFolder(), "rewards.yml");
        if (!rewardFile.exists()) {
            saveResource("rewards.yml", false);
        }

        rewardConfig = YamlConfiguration.loadConfiguration(rewardFile);
    }
}