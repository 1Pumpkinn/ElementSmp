package net.saturn.elementSmp.util;

import org.bukkit.EntityEffect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class DamageUtil {
    public static void setHealthWithTotemCheck(LivingEntity victim, double newHealth) {
        if (newHealth <= 0) {
            if (victim instanceof Player) {
                Player player = (Player) victim;
                if (tryUseTotem(player)) {
                    return;
                }
            }
        }
        victim.setHealth(Math.max(0, newHealth));
    }

    private static boolean tryUseTotem(Player player) {
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();

        boolean used = false;
        if (mainHand.getType() == Material.TOTEM_OF_UNDYING) {
            mainHand.setAmount(mainHand.getAmount() - 1);
            used = true;
        } else if (offHand.getType() == Material.TOTEM_OF_UNDYING) {
            offHand.setAmount(offHand.getAmount() - 1);
            used = true;
        }

        if (used) {
            triggerTotemEffect(player);
        }
        return used;
    }

    private static void triggerTotemEffect(Player player) {
        player.setHealth(1.0);

        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 900, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 100, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 800, 0));

        player.playEffect(EntityEffect.TOTEM_RESURRECT);
        
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 1.0f, 1.0f);
    }
}
