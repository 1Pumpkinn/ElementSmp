package net.saturn.elementSmp.listeners.player;

import net.saturn.elementSmp.ElementSmp;
import net.saturn.elementSmp.config.Constants;
import net.saturn.elementSmp.data.PlayerData;
import net.saturn.elementSmp.gui.ElementSelectionGUI;
import net.saturn.elementSmp.managers.ElementManager;
import net.saturn.elementSmp.managers.ManaManager;
import net.saturn.elementSmp.services.EffectService;
import net.saturn.elementSmp.util.scheduling.TaskScheduler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import org.bukkit.event.player.PlayerRespawnEvent;
public class PlayerLifecycleListener implements Listener {
    private final ElementSmp plugin;
    private final ElementManager elementManager;
    private final ManaManager manaManager;
    private final EffectService effectService;
    private final TaskScheduler scheduler;

    public PlayerLifecycleListener(ElementSmp plugin, ElementManager elementManager,
                                   ManaManager manaManager, EffectService effectService) {
        this.plugin = plugin;
        this.elementManager = elementManager;
        this.manaManager = manaManager;
        this.effectService = effectService;
        this.scheduler = new TaskScheduler(plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerData pd = elementManager.data(player.getUniqueId());
        manaManager.get(player.getUniqueId());

        if (pd.getCurrentElement() == null) {
            scheduler.runAfterPlayerLoad(player, () -> new ElementSelectionGUI(plugin, player, false).open());
        } else {
            scheduler.runAfterPlayerLoad(player, () -> {
                effectService.clearAllElementEffects(player);
                effectService.applyPassiveEffects(player);
            });
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        elementManager.cancelRolling(player);
        manaManager.save(player.getUniqueId());
        effectService.clearAllElementEffects(player);
        plugin.getDataStore().save(elementManager.data(player.getUniqueId()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        scheduler.runLater(() -> {
            if (player.isOnline()) {
                effectService.applyPassiveEffects(player);
            }
        }, 5L);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTotemUse(EntityResurrectEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        scheduler.runLater(() -> {
            if (player.isOnline()) {
                effectService.applyPassiveEffects(player);
            }
        }, Constants.Timing.HALF_SECOND);
    }
}
