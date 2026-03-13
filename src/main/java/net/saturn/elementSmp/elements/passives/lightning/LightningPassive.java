package net.saturn.elementsmp.elements.passives.lightning;

import net.saturn.elementsmp.ElementSmp;
import net.saturn.elementsmp.elements.core.ElementType;
import net.saturn.elementsmp.managers.ElementManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Handles Lightning element passive effects.
 * Passives: Permanent Speed I and Haste II (at Upgrade II).
 * 
 * Note: These are also monitored by EffectService for persistence.
 */
public class LightningPassive implements Listener {

    private final ElementSmp plugin;
    private final ElementManager elementManager;

    public LightningPassive(ElementSmp plugin, ElementManager elementManager) {
        this.plugin = plugin;
        this.elementManager = elementManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (elementManager.getPlayerElement(player) == ElementType.LIGHTNING) {
            applyPassives(player);
        }
    }

    private void applyPassives(Player player) {
        var playerData = elementManager.data(player.getUniqueId());
        int upgradeLevel = playerData.getUpgradeLevel(ElementType.LIGHTNING);

        // Speed I (Permanent)
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, PotionEffect.INFINITE_DURATION, 0, true, false, false));

        // Haste II (Permanent at Upgrade II)
        if (upgradeLevel >= 2) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, PotionEffect.INFINITE_DURATION, 1, true, false, false));
        }
    }
}
