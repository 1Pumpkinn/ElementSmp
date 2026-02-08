package net.saturn.elementSmp.elements.impl.frost.listeners;

import net.saturn.elementSmp.ElementSmp;
import net.saturn.elementSmp.elements.ElementType;
import net.saturn.elementSmp.elements.abilities.impl.frost.IcicleDropAbility;
import net.saturn.elementSmp.managers.ElementManager;
import net.saturn.elementSmp.managers.TrustManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.data.type.PointedDripstone;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class IcicleDropListener implements Listener {

    private final ElementSmp plugin;
    private final ElementManager elementManager;
    private final TrustManager trustManager;

    public IcicleDropListener(ElementSmp plugin, ElementManager elementManager, TrustManager trustManager) {
        this.plugin = plugin;
        this.elementManager = elementManager;
        this.trustManager = trustManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof LivingEntity victim)) return;

        // Check if attacker has Frost element
        if (elementManager.getPlayerElement(attacker) != ElementType.FROST) {
            return;
        }

        // Check if icicle drop metadata exists
        if (!attacker.hasMetadata(IcicleDropAbility.META_ICICLE_DROP_READY)) {
            return;
        }

        long until = attacker.getMetadata(IcicleDropAbility.META_ICICLE_DROP_READY).get(0).asLong();
        if (System.currentTimeMillis() > until) {
            attacker.removeMetadata(IcicleDropAbility.META_ICICLE_DROP_READY, plugin);
            return;
        }

        // Don't drop on trusted players or self
        if (attacker.equals(victim) || (victim instanceof Player targetPlayer && trustManager.isTrusted(attacker.getUniqueId(), targetPlayer.getUniqueId()))) {
            return;
        }

        // Remove the ready state (consume ability)
        attacker.removeMetadata(IcicleDropAbility.META_ICICLE_DROP_READY, plugin);

        // Spawn dripstone 10 blocks above
        Location spawnLoc = victim.getLocation().clone().add(0, 10, 0);
        
        // Ensure the spawn location is not inside blocks if possible, but 10 blocks above is usually clear
        // We spawn it as a falling block to act like an icicle
        var fallingDripstone = victim.getWorld().spawnFallingBlock(spawnLoc, Material.POINTED_DRIPSTONE.createBlockData(blockData -> {
            if (blockData instanceof PointedDripstone dripstone) {
                dripstone.setVerticalDirection(org.bukkit.block.BlockFace.DOWN);
                dripstone.setThickness(PointedDripstone.Thickness.TIP);
            }
        }));
        
        fallingDripstone.setVelocity(new org.bukkit.util.Vector(0, -2, 0));
        fallingDripstone.setDropItem(false);
        fallingDripstone.setHurtEntities(true);
        fallingDripstone.setDamagePerBlock(2.0f);
        fallingDripstone.setMaxDamage(20);

        // Feedback
        Location hitLoc = victim.getEyeLocation();
        victim.getWorld().spawnParticle(Particle.SNOWFLAKE, hitLoc, 50, 0.3, 0.5, 0.3, 0.1, null, true);
        victim.getWorld().playSound(hitLoc, Sound.BLOCK_GLASS_BREAK, 1.0f, 0.5f);
        victim.getWorld().playSound(spawnLoc, Sound.BLOCK_POINTED_DRIPSTONE_BREAK, 1.0f, 1.0f);
    }
}
