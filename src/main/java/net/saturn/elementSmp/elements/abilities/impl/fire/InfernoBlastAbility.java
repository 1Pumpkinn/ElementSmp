package net.saturn.elementSmp.elements.abilities.impl.fire;

import net.saturn.elementSmp.ElementSmp;
import net.saturn.elementSmp.elements.ElementContext;
import net.saturn.elementSmp.elements.abilities.BaseAbility;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

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

        double radius = 6.0;

        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof LivingEntity victim && !entity.equals(player)) {
                // Check if the entity is a player and if they are trusted (using the manager)
                if (victim instanceof Player targetPlayer) {
                    if (plugin.getTrustManager().isTrusted(player.getUniqueId(), targetPlayer.getUniqueId())) {
                        continue;
                    }
                }

                // Deal 3.5 hearts (7.0 damage) of TRUE damage
                // True damage ignores armor, enchantments, and effects
                double damage = 7.0;
                
                // Set health directly for true damage, but ensure we don't go below 0
                double newHealth = Math.max(0, victim.getHealth() - damage);
                victim.setHealth(newHealth);

                // Trigger hurt animation and attribute damage to the player
                // We use a tiny amount of damage to trigger the damage event for death attribution
                victim.damage(0.01, player);
            }
        }
        
        player.sendMessage(ChatColor.GOLD + "BOOM!");
    }
}
