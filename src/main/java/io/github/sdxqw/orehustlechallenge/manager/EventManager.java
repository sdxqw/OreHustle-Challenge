package io.github.sdxqw.orehustlechallenge.manager;

import io.github.sdxqw.orehustlechallenge.OreHustleChallenge;
import io.github.sdxqw.orehustlechallenge.reward.RewardManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Manages the events for the plugin.
 *
 * @see DataManager
 */
public class EventManager implements Listener {
    @Getter
    private final Map<UUID, Integer> playerScore = new HashMap<>();

    private final DataManager dataManager;
    private final FileConfiguration config = OreHustleChallenge.getInstance().getConf();
    private final RewardManager rewardManager = new RewardManager();
    @Getter
    private boolean isEventRunning = false;
    private BukkitTask eventTask;
    private BukkitTask leaderboardUpdateTask;

    /**
     * Constructs a new EventManager with the given DataManager.
     * @param dataManager the DataManager to use
     */
    public EventManager(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    /**
     * Replaces the time patterns in t
     * e given message.
     *
     * <p>Example: 1h 30m 10s -> 1 hour 30 minutes 10 seconds
     *
     * @param message the message to replace the time patterns in
     * @return the message with the time patterns replaced
     */
    public static String replaceTimePatterns(String message) {
        String[] words = message.split(" ");
        Pattern pattern = Pattern.compile("(\\d+[hms])");

        for (String word : words) {
            Matcher matcher = pattern.matcher(word);
            if (matcher.find()) {
                String matched = matcher.group();
                char unit = matched.charAt(matched.length() - 1);
                String replacement = switch (unit) {
                    case 'h' -> matched.replace("h", " hours");
                    case 'm' -> matched.replace("m", " minutes");
                    case 's' -> matched.replace("s", " seconds");
                    default -> matched;
                };
                message = message.replace(matched, replacement);
            }
        }
        return message;
    }

    /**
     * Starts the event with a specified duration.
     * @param durationTicks the duration of the event in ticks
     */
    private void startEvent(int durationTicks) {
        if (isEventRunning) return;

        isEventRunning = true;
        rewardManager.clearMap();
        dataManager.clearMap();
        playerScore.clear();
        scheduleLeaderboardUpdates();

        String durationPlaceholder = Objects.requireNonNull(config.getString("messages.event_start"));
        String broadcastMessage = durationPlaceholder.replace("{duration}", replaceTimePatterns(Objects.requireNonNull(config.getString("scheduler.event_duration"))));

        eventTask = new BukkitRunnable() {
            @Override
            public void run() {
                stopEvent();
            }
        }.runTaskLater(OreHustleChallenge.getInstance(), durationTicks);

        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', broadcastMessage));
    }

    /**
     * Handles the PlayerJoinEvent.
     * @param event the PlayerJoinEvent to handle
     */
    @EventHandler
    public void on(PlayerJoinEvent event) {
        if (isEventRunning) {
            event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(config.getString("messages.event_started")).replace("{duration}", replaceTimePatterns(Objects.requireNonNull(config.getString("scheduler.event_duration"))))));
        }
    }

    /**
     * Stops the event.
     */
    private void stopEvent() {
        if (!isEventRunning) return;

        isEventRunning = false;
        updateLeaderboard();
        cancelLeaderboardUpdates();

        if (eventTask != null) {
            eventTask.cancel();
        }

        String durationPlaceholder = Objects.requireNonNull(config.getString("messages.event_end"));
        String broadcastMessage = durationPlaceholder.replace("{duration}", Objects.requireNonNull(config.getString("scheduler.event_spacing_interval")));

        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', replaceTimePatterns(broadcastMessage)));

        List<Map.Entry<UUID, Integer>> sortedEntries = playerScore.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).limit(5).toList();

        if (sortedEntries.isEmpty()) return;

        List<String> winnerHeader = config.getStringList("messages.event_winners.header");
        for (String message : winnerHeader)
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', message));

        int rank = 1;
        for (Map.Entry<UUID, Integer> entry : sortedEntries) {
            String winnerMessage = Objects.requireNonNull(config.getString("messages.event_winners.body")).replace("{winners}", Objects.requireNonNull(Bukkit.getPlayer(entry.getKey())).getName()).replace("{score}", entry.getValue().toString()).replace("{rank}", String.valueOf(rank));
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', winnerMessage));
            rank++;
        }

        List<String> winnerBottom = config.getStringList("messages.event_winners.bottom");
        for (String message : winnerBottom)
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', message));

        int pos = 0;
        for (Map.Entry<UUID, Integer> entry : playerScore.entrySet()) {
            pos++;
            rewardManager.addPlayer(Bukkit.getPlayer(entry.getKey()), pos);
        }

        rewardManager.giveReward();
    }

    /**
     * Schedules the leaderboard updates.
     */
    private void scheduleLeaderboardUpdates() {
        leaderboardUpdateTask = new BukkitRunnable() {
            @Override
            public void run() {
                updateLeaderboard();
            }
        }.runTaskTimer(OreHustleChallenge.getInstance(), 0L, parseTimeStringToTicks(Objects.requireNonNull(OreHustleChallenge.getInstance().getConf().getString("scheduler.leaderboard_update_interval"))));
    }

    /**
     * Cancels the leaderboard updates.
     */
    private void cancelLeaderboardUpdates() {
        if (leaderboardUpdateTask != null) {
            leaderboardUpdateTask.cancel();
        }
    }

    /**
     * Updates the leaderboard.
     */
    private void updateLeaderboard() {
        playerScore.putAll(dataManager.getPlayerOreCount());

        List<Map.Entry<UUID, Integer>> sortedEntries = playerScore.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).limit(5).toList();

        if (sortedEntries.isEmpty()) return;

        List<String> header = config.getStringList("leaderboard.header");
        for (String message : header)
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', message));

        int rank = 1;
        for (Map.Entry<UUID, Integer> entry : sortedEntries) {
            String message = Objects.requireNonNull(config.getString("leaderboard.body")).replace("{rank}", String.valueOf(rank)).replace("{player}", Objects.requireNonNull(Bukkit.getPlayer(entry.getKey())).getName()).replace("{score}", entry.getValue().toString());
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', message));
            rank++;
        }

        List<String> bottom = config.getStringList("leaderboard.bottom");
        for (String message : bottom)
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    /**
     * Schedules the events.
     */
    public void scheduleEvents() {
        int dailyEvents = config.getInt("scheduler.max_daily_events");

        String eventSpacingTimeString = config.getString("scheduler.event_spacing_interval");
        assert eventSpacingTimeString != null;
        int eventSpacingTicks = parseTimeStringToTicks(eventSpacingTimeString);

        int duration = parseTimeStringToTicks(Objects.requireNonNull(config.getString("scheduler.event_duration")));

        for (int i = 0; i < dailyEvents; i++) {
            int delay = eventSpacingTicks * i;

            Bukkit.getScheduler().scheduleSyncDelayedTask(OreHustleChallenge.getInstance(), () -> startEvent(duration), delay);
        }

        startDailyTask();
    }

    /**
     * Starts the daily task to reset for a new day.
     */
    private void startDailyTask() {
        int secondsInDay = 86400;
        int ticksInDay = secondsInDay * 20;

        Bukkit.getScheduler().runTaskTimerAsynchronously(OreHustleChallenge.getInstance(), this::scheduleEvents, ticksInDay, ticksInDay);
    }


    /**
     * Parses the given time string to tick.
     * @param timeString the time string to parse
     * @return the number of ticks in the time string
     */
    private int parseTimeStringToTicks(String timeString) {
        String[] components = timeString.split(" ");
        int totalTicks = 0;

        for (String component : components) {
            char unit = component.charAt(component.length() - 1);
            int value = Integer.parseInt(component.substring(0, component.length() - 1));

            switch (unit) {
                case 'h':
                    totalTicks += value * 72000;
                    break;
                case 'm':
                    totalTicks += value * 1200;
                    break;
                case 's':
                    totalTicks += value * 20;
                    break;
                default:
                    break;
            }
        }

        return totalTicks;
    }
}
