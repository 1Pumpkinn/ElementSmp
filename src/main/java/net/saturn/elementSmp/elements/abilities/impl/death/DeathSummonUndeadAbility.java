package net.saturn.elementSmp.elements.abilities.impl.death;

import net.saturn.elementSmp.config.MetadataKeys;
import net.saturn.elementSmp.elements.ElementContext;
import net.saturn.elementSmp.elements.abilities.BaseAbility;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.WitherSkeleton;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

public class DeathSummonUndeadAbility extends BaseAbility {

    private final net.saturn.elementSmp.ElementSmp plugin;

    public DeathSummonUndeadAbility(net.saturn.elementSmp.ElementSmp plugin) {
        super();
        this.plugin = plugin;
    }

    @Override
    public boolean execute(ElementContext context) {
        Player player = context.getPlayer();
        
        // Debug message to player and console to help track spawning
        player.sendMessage(ChatColor.GRAY + "Attempting to summon undead...");
        plugin.getLogger().info("Attempting to summon Undead Servant for " + player.getName() + " in world " + player.getWorld().getName());

        // Use World.spawn with a consumer for more reliable spawning across all worlds
        // We use the player's location but ensure it's slightly above ground if needed
        WitherSkeleton skeleton = player.getWorld().spawn(player.getLocation(), WitherSkeleton.class, s -> {
            // Ensure it doesn't despawn naturally
            s.setRemoveWhenFarAway(false);
            s.setPersistent(false);
            s.setCanPickupItems(false); // Prevent it from taking player loot
            
            // Armor
            s.getEquipment().setHelmet(createEnchantedItem(Material.IRON_HELMET, Enchantment.PROTECTION, 2));
            s.getEquipment().setChestplate(createEnchantedItem(Material.IRON_CHESTPLATE, Enchantment.PROTECTION, 2));
            s.getEquipment().setLeggings(createEnchantedItem(Material.IRON_LEGGINGS, Enchantment.PROTECTION, 2));
            s.getEquipment().setBoots(createEnchantedItem(Material.IRON_BOOTS, Enchantment.PROTECTION, 2));
            
            // Weapon
            s.getEquipment().setItemInMainHand(createEnchantedItem(Material.IRON_SWORD, Enchantment.SHARPNESS, 1));
            
            // Set drop chances to 0 so players can't farm the gear
            s.getEquipment().setHelmetDropChance(0);
            s.getEquipment().setChestplateDropChance(0);
            s.getEquipment().setLeggingsDropChance(0);
            s.getEquipment().setBootsDropChance(0);
            s.getEquipment().setItemInMainHandDropChance(0);
            
            s.setCustomName(ChatColor.DARK_GRAY + player.getName() + "'s Undead Servant");
            s.setCustomNameVisible(true);
            
            // Use metadata for DeathFriendlyMobListener
            long durationMs = 120000; // 2 minutes (120,000 ms)
            s.setMetadata(MetadataKeys.Death.SUMMONED_OWNER, new FixedMetadataValue(plugin, player.getUniqueId().toString()));
            s.setMetadata(MetadataKeys.Death.SUMMONED_UNTIL, new FixedMetadataValue(plugin, System.currentTimeMillis() + durationMs));
            
            // Make it faster - using GENERIC_MOVEMENT_SPEED for 1.21 compatibility
            if (s.getAttribute(Attribute.MOVEMENT_SPEED) != null) {
                s.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0.35);
            }
        });

        if (skeleton == null || !skeleton.isValid()) {
            player.sendMessage(ChatColor.RED + "Failed to summon servant. Please make sure there is enough space!");
            plugin.getLogger().warning("Summon failed for " + player.getName() + " (skeleton was null or invalid)");
            return false;
        }

        player.getWorld().spawnParticle(Particle.SQUID_INK, skeleton.getLocation().add(0, 1, 0), 50, 0.5, 0.5, 0.5, 0.1);
        player.getWorld().playSound(skeleton.getLocation(), Sound.ENTITY_WITHER_SKELETON_AMBIENT, 1.0f, 0.5f);
        player.sendMessage(ChatColor.DARK_PURPLE + "Summoned an Undead Servant for 2 minutes!");
        
        // The following and attacking logic is handled by DeathFriendlyMobListener.java
        
        // Remove after 2 minutes
        new BukkitRunnable() {
            @Override
            public void run() {
                if (skeleton.isValid()) {
                    player.getWorld().spawnParticle(Particle.SQUID_INK, skeleton.getLocation().add(0, 1, 0), 30, 0.3, 0.3, 0.3, 0.05);
                    skeleton.remove();
                }
            }
        }.runTaskLater(plugin, 2400L); // 2 minutes (120 seconds * 20 ticks)
        
        return true;
    }

    private ItemStack createEnchantedItem(Material material, Enchantment enchantment, int level) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.addEnchant(enchantment, level, true);
            item.setItemMeta(meta);
        }
        return item;
    }
}
