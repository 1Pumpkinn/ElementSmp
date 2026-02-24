package net.saturn.elementSmp.elements.abilities.impl.death;

import net.saturn.elementSmp.config.Constants;
import net.saturn.elementSmp.config.MetadataKeys;
import net.saturn.elementSmp.elements.ElementContext;
import net.saturn.elementSmp.elements.abilities.BaseAbility;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.WitherSkeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class DeathSummonUndeadAbility extends BaseAbility implements Listener {

    private final net.saturn.elementSmp.ElementSmp plugin;
    // Tracks players who currently have an active summon — prevents double-spawn ghost bug
    private final Set<UUID> activeSummons = new HashSet<>();

    public DeathSummonUndeadAbility(net.saturn.elementSmp.ElementSmp plugin) {
        super();
        this.plugin = plugin;
        // Register so we catch skeleton death and free the slot
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean execute(ElementContext context) {
        Player player = context.getPlayer();

        // Prevent double-summoning — primary fix for the ghost/invisible skeleton bug.
        // Slot is reserved BEFORE spawning so same-tick double calls are also blocked.
        if (activeSummons.contains(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You already have an Undead Servant active!");
            return false;
        }
        activeSummons.add(player.getUniqueId());

        plugin.getLogger().info("Attempting to summon Undead Servant for " + player.getName() + " in world " + player.getWorld().getName());

        WitherSkeleton skeleton = player.getWorld().spawn(player.getLocation(), WitherSkeleton.class, s -> {
            s.setRemoveWhenFarAway(false);
            s.setPersistent(false);
            s.setCanPickupItems(false);

            s.getEquipment().setHelmet(createEnchantedItem(Material.IRON_HELMET, Enchantment.PROTECTION, 2));
            s.getEquipment().setChestplate(createEnchantedItem(Material.IRON_CHESTPLATE, Enchantment.PROTECTION, 2));
            s.getEquipment().setLeggings(createEnchantedItem(Material.IRON_LEGGINGS, Enchantment.PROTECTION, 2));
            s.getEquipment().setBoots(createEnchantedItem(Material.IRON_BOOTS, Enchantment.PROTECTION, 2));
            s.getEquipment().setItemInMainHand(createEnchantedItem(Material.IRON_SWORD, Enchantment.SHARPNESS, 1));

            s.getEquipment().setHelmetDropChance(0);
            s.getEquipment().setChestplateDropChance(0);
            s.getEquipment().setLeggingsDropChance(0);
            s.getEquipment().setBootsDropChance(0);
            s.getEquipment().setItemInMainHandDropChance(0);

            s.setCustomName(ChatColor.DARK_GRAY + player.getName() + "'s Undead Servant");
            s.setCustomNameVisible(true);

            long durationMs = Constants.Duration.DEATH_SUMMON_MS;
            s.setMetadata(MetadataKeys.Death.SUMMONED_OWNER, new FixedMetadataValue(plugin, player.getUniqueId().toString()));
            s.setMetadata(MetadataKeys.Death.SUMMONED_UNTIL, new FixedMetadataValue(plugin, System.currentTimeMillis() + durationMs));

            if (s.getAttribute(Attribute.MOVEMENT_SPEED) != null) {
                s.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0.35);
            }
        });

        if (skeleton == null || !skeleton.isValid()) {
            // Release slot so player can try again
            activeSummons.remove(player.getUniqueId());
            player.sendMessage(ChatColor.RED + "Failed to summon servant. Please make sure there is enough space!");
            plugin.getLogger().warning("Summon failed for " + player.getName() + " (skeleton was null or invalid)");
            return false;
        }

        player.getWorld().spawnParticle(Particle.SQUID_INK, skeleton.getLocation().add(0, 1, 0), 50, 0.5, 0.5, 0.5, 0.1);
        player.getWorld().playSound(skeleton.getLocation(), Sound.ENTITY_WITHER_SKELETON_AMBIENT, 1.0f, 0.5f);
        player.sendMessage(ChatColor.DARK_PURPLE + "Summoned an Undead Servant for " + (Constants.Duration.DEATH_SUMMON_MS / 60000) + " minutes!");

        // Remove after duration — also clears the activeSummons slot
        new BukkitRunnable() {
            @Override
            public void run() {
                activeSummons.remove(player.getUniqueId());
                if (skeleton.isValid()) {
                    skeleton.getWorld().spawnParticle(Particle.SQUID_INK, skeleton.getLocation().add(0, 1, 0), 30, 0.3, 0.3, 0.3, 0.05);
                    skeleton.remove();
                }
            }
        }.runTaskLater(plugin, Constants.Duration.DEATH_SUMMON_MS / 1000 * 20);

        return true;
    }

    /**
     * If the skeleton dies early (killed by player/mob), release the owner's
     * summon slot so they can summon again. Also strips all drops.
     */
    @EventHandler
    public void onSkeletonDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof WitherSkeleton skeleton)) return;
        if (!skeleton.hasMetadata(MetadataKeys.Death.SUMMONED_OWNER)) return;

        // No loot from summoned skeletons
        event.getDrops().clear();
        event.setDroppedExp(0);

        String ownerStr = skeleton.getMetadata(MetadataKeys.Death.SUMMONED_OWNER).get(0).asString();
        try {
            UUID ownerId = UUID.fromString(ownerStr);
            activeSummons.remove(ownerId);
        } catch (IllegalArgumentException ignored) {}
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