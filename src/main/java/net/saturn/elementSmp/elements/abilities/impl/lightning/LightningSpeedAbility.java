package net.saturn.elementsmp.elements.abilities.impl.lightning;

import net.saturn.elementsmp.ElementSmp;
import net.saturn.elementsmp.elements.core.ElementContext;
import net.saturn.elementsmp.elements.abilities.BaseAbility;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class LightningSpeedAbility extends BaseAbility {
    private final ElementSmp plugin;

    public LightningSpeedAbility(ElementSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean execute(ElementContext context) {
        Player player = context.getPlayer();

        // Speed II for 10 seconds (200 ticks)
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 1));
        
        // One-time effects
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.5f, 2.0f);
        
        // Persistent effects for 10 seconds
        new BukkitRunnable() {
            int ticks = 0;
            final int maxTicks = 200;

            @Override
            public void run() {
                if (!player.isOnline() || ticks >= maxTicks) {
                    cancel();
                    return;
                }

                // Spawn particles around the player's feet/body
                if (ticks % 2 == 0) {
                    // Yellow particles (using DUST with yellow color)
                    player.getWorld().spawnParticle(Particle.DUST, player.getLocation().add(0, 0.5, 0), 3, 0.3, 0.5, 0.3, new Particle.DustOptions(Color.YELLOW, 1.0f));
                    // Electric spark particles
                    player.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, player.getLocation().add(0, 1, 0), 2, 0.4, 0.8, 0.4, 0.1);
                }

                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);

        return true;
    }
}
