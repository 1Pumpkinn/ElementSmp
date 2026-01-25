package net.saturn.elementSmp.elements.abilities.impl.fire;

import net.saturn.elementSmp.ElementSmp;
import net.saturn.elementSmp.elements.ElementContext;
import net.saturn.elementSmp.elements.abilities.BaseAbility;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ScorchAbility extends BaseAbility implements Listener {
    private final ElementSmp plugin;
    private final Set<UUID> activeScorches = new HashSet<>();
    private static final String META_SCORCH = "fire_scorch_active";

    public ScorchAbility(ElementSmp plugin) {
        super();
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean execute(ElementContext context) {
        Player player = context.getPlayer();
        activeScorches.add(player.getUniqueId());
        player.setMetadata(META_SCORCH, new FixedMetadataValue(plugin, true));
        
        player.sendMessage(ChatColor.RED + "Your next hit will clear the enemy's Fire Resistance!");
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_FIRECHARGE_USE, 1.0f, 1.2f);
        player.getWorld().spawnParticle(Particle.FLAME, player.getLocation().add(0, 1, 0), 20, 0.3, 0.5, 0.3, 0.05);
        
        return true;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof LivingEntity victim)) return;

        if (attacker.hasMetadata(META_SCORCH)) {
            attacker.removeMetadata(META_SCORCH, plugin);
            activeScorches.remove(attacker.getUniqueId());

            if (victim.hasPotionEffect(PotionEffectType.FIRE_RESISTANCE)) {
                victim.removePotionEffect(PotionEffectType.FIRE_RESISTANCE);
                victim.getWorld().playSound(victim.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1.0f, 0.8f);
                victim.getWorld().spawnParticle(Particle.LARGE_SMOKE, victim.getLocation().add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0.05);
                attacker.sendMessage(ChatColor.GOLD + "You cleared " + victim.getName() + "'s Fire Resistance!");
            } else {
                attacker.sendMessage(ChatColor.GRAY + victim.getName() + " didn't have Fire Resistance.");
            }
        }
    }
}
