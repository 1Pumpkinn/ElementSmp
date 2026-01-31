package net.saturn.elementSmp.elements.abilities.impl.water;

import net.saturn.elementSmp.ElementSmp;
import net.saturn.elementSmp.config.MetadataKeys;
import net.saturn.elementSmp.elements.ElementContext;
import net.saturn.elementSmp.elements.abilities.BaseAbility;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class WaterLeechTridentAbility extends BaseAbility implements Listener {
    private final ElementSmp plugin;
    private static boolean listenerRegistered = false;

    public WaterLeechTridentAbility(ElementSmp plugin) {
        super();
        this.plugin = plugin;
        if (!listenerRegistered) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
            listenerRegistered = true;
        }
    }

    @Override
    public boolean execute(ElementContext context) {
        Player player = context.getPlayer();
        
        // Launch trident
        Trident trident = player.launchProjectile(Trident.class);
        trident.setMetadata(MetadataKeys.Water.LEECHING_TRIDENT, new FixedMetadataValue(plugin, player.getUniqueId()));
        trident.setVelocity(player.getLocation().getDirection().multiply(2.0));
        trident.setPickupStatus(Trident.PickupStatus.DISALLOWED);
        
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_TRIDENT_THROW, 1.0f, 1.0f);
        
        return true;
    }


    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Trident trident)) return;
        if (!trident.hasMetadata(MetadataKeys.Water.LEECHING_TRIDENT)) return;

        if (event.getHitEntity() instanceof LivingEntity target) {
            event.setCancelled(true); // Prevent default sticking behavior
            trident.remove(); // Remove the original projectile

            // Spawn an ItemDisplay for the effect (visual only)
            ItemDisplay visualDisplay = (ItemDisplay) target.getWorld().spawn(target.getLocation(), ItemDisplay.class);
            visualDisplay.setItemStack(new ItemStack(Material.TRIDENT));
            visualDisplay.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.THIRDPERSON_RIGHTHAND); // Use 3D "held" model
            
            // Scale up (1.5x) and Rotate to point forward
            // THIRDPERSON_RIGHTHAND holds item upright. -90 degrees on X points it forward.
            visualDisplay.setTransformation(new Transformation(
                new Vector3f(0, 0, 0),
                new AxisAngle4f((float)Math.toRadians(-90), 1, 0, 0),
                new Vector3f(1.0f, 1.0f, 1.0f), // Normal scale (1.0x) as per reference
                new AxisAngle4f()
            ));
            
            visualDisplay.setMetadata(MetadataKeys.Water.LEECHING_TRIDENT, new FixedMetadataValue(plugin, true));

            // Apply leeching effect
            new BukkitRunnable() {
                int ticks = 0;
                final int maxTicks = 100; // 5 seconds
                double angle = 0;

                @Override
                public void run() {
                    if (ticks >= maxTicks || target.isDead() || !target.isValid() || !visualDisplay.isValid()) {
                        visualDisplay.remove();
                        cancel();
                        return;
                    }

                    // Circle logic
                    angle += 0.15; // Slower rotation speed
                    double radius = 3.0; // Wide radius to stay out of the entity (in the air)
                    double x = target.getLocation().getX() + radius * Math.cos(angle);
                    double z = target.getLocation().getZ() + radius * Math.sin(angle);
                    
                    // Move up: Higher up (2.5 blocks) to be clearly "in the air"
                    double y = target.getLocation().getY() + 2.5;

                    org.bukkit.Location newLoc = new org.bukkit.Location(target.getWorld(), x, y, z);
                    
                    // Face the display towards the entity's center
                    org.bukkit.Location targetCenter = target.getLocation().add(0, 1.5, 0);
                    // Invert direction so the trident model points towards the center
                    Vector direction = newLoc.toVector().subtract(targetCenter.toVector());
                    newLoc.setDirection(direction);

                    visualDisplay.teleport(newLoc);

                    if (ticks % 20 == 0) { // Every second
                        target.damage(9.0); // Trident damage
                        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_DROWNED_HURT_WATER, 0.5f, 1.0f);
                        target.getWorld().spawnParticle(Particle.DRIPPING_WATER, target.getLocation().add(0, 1, 0), 10, 0.3, 0.3, 0.3, 0.1);
                        
                        // Leeching visual
                        target.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, target.getLocation().add(0, 1.5, 0), 5, 0.3, 0.3, 0.3, 0.1);
                    }

                    ticks++;
                }
            }.runTaskTimer(plugin, 0L, 1L);
        } else {
             // If it hits a block, remove it after a short delay
             new BukkitRunnable() {
                 @Override
                 public void run() {
                     trident.remove();
                 }
             }.runTaskLater(plugin, 100L);
        }
    }
}
