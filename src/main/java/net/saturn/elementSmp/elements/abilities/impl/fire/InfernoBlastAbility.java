package net.saturn.elementsmp.elements.abilities.impl.fire;

import net.saturn.elementsmp.ElementSmp;
import net.saturn.elementsmp.config.Constants;
import net.saturn.elementsmp.config.MetadataKeys;
import net.saturn.elementsmp.elements.core.ElementContext;
import net.saturn.elementsmp.elements.abilities.BaseAbility;
import net.saturn.elementsmp.util.DamageUtil;
import org.bukkit.ChatColor;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

public class InfernoBlastAbility extends BaseAbility {
    private final ElementSmp plugin;

    public InfernoBlastAbility(ElementSmp plugin) {
        super();
        this.plugin = plugin;
    }

    @Override
    public boolean execute(ElementContext context) {
        Player player = context.getPlayer();
        explode(player);
        return true;
    }

    private void explode(Player player) {
        Location loc = player.getLocation();
        
        // Visual and Sound effects
        loc.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, loc, 1, 0, 0, 0, 0);
        loc.getWorld().spawnParticle(Particle.FLAME, loc, 100, 3, 3, 3, 0.1);
        loc.getWorld().spawnParticle(Particle.LARGE_SMOKE, loc, 50, 2, 2, 2, 0.05);
        loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 0.8f);

        double radius = Constants.Distance.FIRE_EXPLOSION_RADIUS;

        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof LivingEntity victim && !entity.equals(player)) {
                // Check if the entity is a player and if they are trusted (using the manager)
                if (victim instanceof Player targetPlayer) {
                    if (plugin.getTrustManager().isTrusted(player.getUniqueId(), targetPlayer.getUniqueId())) {
                        continue;
                    }
                }

                // Deal TRUE damage
                // True damage ignores armor, enchantments, and effects
                double damage = Constants.Damage.FIRE_EXPLOSION_DAMAGE;

                if (!(victim instanceof Player p && (p.getGameMode() == org.bukkit.GameMode.CREATIVE || p.getGameMode() == org.bukkit.GameMode.SPECTATOR))) {
                    victim.playEffect(EntityEffect.HURT);
                    double newHealth = victim.getHealth() - damage;
                    DamageUtil.setHealthWithTotemCheck(victim, newHealth);
                }
            }
        }
    }
}
