package net.saturn.elementSmp.elements.abilities.impl.air.passives;

import net.saturn.elementSmp.elements.ElementType;
import net.saturn.elementSmp.managers.ElementManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
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
        
        // Quick check for sneaking and air before doing expensive database/map lookups
        if (!player.isSneaking() || player.isOnGround()) return;

        // Check if player has Air element
        var pd = elementManager.data(player.getUniqueId());
        if (pd == null || pd.getCurrentElement() != ElementType.AIR) return;

        // Give slow falling
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 40, 0, true, false, true));
    }
}
