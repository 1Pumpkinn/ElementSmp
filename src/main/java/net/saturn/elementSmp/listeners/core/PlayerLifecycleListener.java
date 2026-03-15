package net.saturn.elementsmp.listeners.core;

import net.saturn.elementsmp.ElementSmp;
import net.saturn.elementsmp.config.Constants;
import net.saturn.elementsmp.data.PlayerData;
import net.saturn.elementsmp.gui.ElementSelectionGUI;
import net.saturn.elementsmp.items.util.AdvancedRerollerItem;
import net.saturn.elementsmp.items.util.RerollerItem;
import net.saturn.elementsmp.items.util.Upgrader1Item;
import net.saturn.elementsmp.items.util.Upgrader2Item;
import net.saturn.elementsmp.managers.ElementManager;
import net.saturn.elementsmp.managers.ManaManager;
import net.saturn.elementsmp.services.EffectService;
import net.saturn.elementsmp.util.scheduling.TaskScheduler;
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

        if (pd.getCurrentElement() == null || pd.needsReroll()) {
            boolean isReroll = pd.needsReroll();
            scheduler.runAfterPlayerLoad(player, () -> new ElementSelectionGUI(plugin, player, isReroll).open());
        } else {
            scheduler.runAfterPlayerLoad(player, () -> {
                effectService.clearAllElementEffects(player);
                effectService.applyPassiveEffects(player);
            });
        }

        scheduler.runAfterPlayerLoad(player, () -> {
            try {
                player.discoverRecipe(new org.bukkit.NamespacedKey(plugin, Upgrader1Item.KEY));
                player.discoverRecipe(new org.bukkit.NamespacedKey(plugin, Upgrader2Item.KEY));
                player.discoverRecipe(new org.bukkit.NamespacedKey(plugin, RerollerItem.KEY));
                player.discoverRecipe(new org.bukkit.NamespacedKey(plugin, AdvancedRerollerItem.KEY));
            } catch (Exception ignored) {}
        });
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
        PlayerData pd = elementManager.data(player.getUniqueId());

        if (pd.getCurrentElement() == null || pd.needsReroll()) {
            boolean isReroll = pd.needsReroll();
            scheduler.runLater(() -> {
                if (player.isOnline()) {
                    new ElementSelectionGUI(plugin, player, isReroll).open();
                }
            }, 5L);
        } else {
            scheduler.runLater(() -> {
                if (player.isOnline()) {
                    effectService.applyPassiveEffects(player);
                }
            }, 5L);
        }
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
