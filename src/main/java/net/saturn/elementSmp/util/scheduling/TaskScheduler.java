package net.saturn.elementSmp.util.scheduling;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.saturn.elementSmp.ElementSmp;
import net.saturn.elementSmp.config.Constants;

import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

public class TaskScheduler {
    private final ElementSmp plugin;

    public TaskScheduler(ElementSmp plugin) {
        this.plugin = plugin;
    }

    public ScheduledTask run(Runnable task) {
        return plugin.getServer().getGlobalRegionScheduler().run(plugin, scheduledTask -> task.run());
    }

    public ScheduledTask runLater(Runnable task, long delayTicks) {
        return plugin.getServer().getGlobalRegionScheduler().runDelayed(plugin, scheduledTask -> task.run(), delayTicks);
    }

    public ScheduledTask runLaterSeconds(Runnable task, int seconds) {
        return plugin.getServer().getAsyncScheduler().runDelayed(plugin, scheduledTask -> task.run(), seconds, TimeUnit.SECONDS);
    }

    public ScheduledTask runTimer(Runnable task, long delayTicks, long periodTicks) {
        return plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(plugin, scheduledTask -> task.run(), delayTicks, periodTicks);
    }

    public ScheduledTask runTimerSeconds(Runnable task, int delaySeconds, int periodSeconds) {
        return plugin.getServer().getAsyncScheduler().runAtFixedRate(plugin, scheduledTask -> task.run(), delaySeconds, periodSeconds, TimeUnit.SECONDS);
    }

    public ScheduledTask runAfterPlayerLoad(Player player, Runnable task) {
        return runLater(task, Constants.Timing.HALF_SECOND);
    }

    public ScheduledTask runCleanup(Runnable task) {
        return runLater(task, Constants.Animation.TAP_CLEANUP_DELAY);
    }
}


