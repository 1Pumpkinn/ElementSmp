package net.saturn.elementSmp.elements.abilities.impl.death;

import net.saturn.elementSmp.elements.ElementContext;
import net.saturn.elementSmp.elements.abilities.BaseAbility;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.WitherSkeleton;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class DeathSummonUndeadAbility extends BaseAbility {

    public DeathSummonUndeadAbility(net.saturn.elementSmp.ElementSmp plugin) {
        super("death_summon_undead", 100, 45, 1);
    }

    @Override
    public boolean execute(ElementContext context) {
        Player player = context.getPlayer();
        
        WitherSkeleton skeleton = (WitherSkeleton) player.getWorld().spawnEntity(player.getLocation(), EntityType.WITHER_SKELETON);
        
        // Armor
        skeleton.getEquipment().setHelmet(createEnchantedItem(Material.IRON_HELMET, Enchantment.PROTECTION, 2));
        skeleton.getEquipment().setChestplate(createEnchantedItem(Material.IRON_CHESTPLATE, Enchantment.PROTECTION, 2));
        skeleton.getEquipment().setLeggings(createEnchantedItem(Material.IRON_LEGGINGS, Enchantment.PROTECTION, 2));
        skeleton.getEquipment().setBoots(createEnchantedItem(Material.IRON_BOOTS, Enchantment.PROTECTION, 2));
        
        // Weapon
        skeleton.getEquipment().setItemInMainHand(createEnchantedItem(Material.IRON_SWORD, Enchantment.SHARPNESS, 1));
        
        // Set drop chances to 0 so players can't farm the gear
        skeleton.getEquipment().setHelmetDropChance(0);
        skeleton.getEquipment().setChestplateDropChance(0);
        skeleton.getEquipment().setLeggingsDropChance(0);
        skeleton.getEquipment().setBootsDropChance(0);
        skeleton.getEquipment().setItemInMainHandDropChance(0);
        
        skeleton.setCustomName(ChatColor.DARK_GRAY + player.getName() + "'s Undead Servant");
        skeleton.setCustomNameVisible(true);
        
        // Make it not attack the owner (handled by DeathFriendlyMobListener already in the project)
        
        player.getWorld().spawnParticle(Particle.SQUID_INK, skeleton.getLocation().add(0, 1, 0), 50, 0.5, 0.5, 0.5, 0.1);
        player.getWorld().playSound(skeleton.getLocation(), Sound.ENTITY_WITHER_SKELETON_AMBIENT, 1.0f, 0.5f);
        player.sendMessage(ChatColor.DARK_PURPLE + "Summoned an Undead Servant!");
        
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

    @Override
    public String getName() {
        return ChatColor.DARK_PURPLE + "Undead Servant";
    }

    @Override
    public String getDescription() {
        return "Summon a Wither Skeleton with Protection 2 Iron Armor and a Sharpness Iron Sword.";
    }
}
