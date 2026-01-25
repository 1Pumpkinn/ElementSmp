package net.saturn.elementSmp.elements.abilities.impl.life;

import net.saturn.elementSmp.elements.ElementContext;
import net.saturn.elementSmp.elements.abilities.BaseAbility;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class NaturesEyeAbility extends BaseAbility {

    public NaturesEyeAbility(net.saturn.elementSmp.ElementSmp plugin) {
        super();
    }

    @Override
    public boolean execute(ElementContext context) {
        Player player = context.getPlayer();
        int radius = 25;

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ALLAY_AMBIENT_WITH_ITEM, 1.0f, 1.0f);
        player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, player.getEyeLocation(), 30, 0.5, 0.5, 0.5, 0.1);

        boolean found = false;
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof LivingEntity livingEntity && !entity.equals(player)) {
                livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 5 * 20, 0, false, false, false));
                found = true;
            }
        }

        if (found) {
            player.sendMessage(ChatColor.GREEN + "Nature's Eye has revealed nearby life forms!");
        } else {
            player.sendMessage(ChatColor.GRAY + "No life forms detected nearby.");
        }

        return true;
    }
}
