package net.saturn.elementSmp.elements.abilities;

import net.saturn.elementSmp.elements.ElementContext;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Base implementation of the Ability interface that handles common functionality
 */
public abstract class BaseAbility implements Ability {
    private final Set<UUID> activePlayers = new HashSet<>();
    
    /**
     * Create a new ability
     */
    public BaseAbility() {
    }
    
    @Override
    public void setActive(Player player, boolean active) {
        if (active) {
            activePlayers.add(player.getUniqueId());
        } else {
            activePlayers.remove(player.getUniqueId());
        }
    }

    /**
     * Check if an entity is a valid target for an ability.
     * Prevents hitting self or trusted players.
     */
    protected boolean isValidTarget(ElementContext context, org.bukkit.entity.LivingEntity target) {
        if (target == null) return false;
        Player player = context.getPlayer();
        if (target.equals(player)) return false;

        if (target instanceof Player targetPlayer) {
            if (context.getTrustManager().isTrusted(player.getUniqueId(), targetPlayer.getUniqueId())) {
                return false;
            }
        }
        return true;
    }
}
