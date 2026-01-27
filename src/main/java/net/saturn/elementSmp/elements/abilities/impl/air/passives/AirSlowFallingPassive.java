package net.saturn.elementSmp.elements.abilities.impl.air.passives;

import net.saturn.elementSmp.elements.ElementType;
import net.saturn.elementSmp.managers.ElementManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class AirSlowFallingPassive implements Listener {
    private final ElementManager elementManager;

    public AirSlowFallingPassive(ElementManager elementManager) {
        this.elementManager = elementManager;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        
        // Check if player has Air element
        var pd = elementManager.data(player.getUniqueId());
        if (pd == null || pd.getCurrentElement() != ElementType.AIR) return;

        // If shifting and in air, give slow falling
        if (player.isSneaking() && !player.isOnGround()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 40, 0, true, false, true));
        } else if (!player.isSneaking()) {
            // If not shifting, remove it immediately
            if (player.hasPotionEffect(PotionEffectType.SLOW_FALLING)) {
                player.removePotionEffect(PotionEffectType.SLOW_FALLING);
            }
        }
    }

    @EventHandler
    public void onToggleSneak(PlayerToggleSneakEvent event) {
        // If they STOP sneaking, remove slow falling straight away
        if (!event.isSneaking()) {
            Player player = event.getPlayer();
            var pd = elementManager.data(player.getUniqueId());
            if (pd != null && pd.getCurrentElement() == ElementType.AIR) {
                player.removePotionEffect(PotionEffectType.SLOW_FALLING);
            }
        }
    }
}
