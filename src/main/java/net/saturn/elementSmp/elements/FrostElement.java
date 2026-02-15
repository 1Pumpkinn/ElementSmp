package net.saturn.elementSmp.elements;

import net.saturn.elementSmp.ElementSmp;
import org.bukkit.entity.Player;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class FrostElement implements Element {
    private final ElementSmp plugin;

    public FrostElement(ElementSmp plugin) {
        this.plugin = plugin;
    }

    @Override
    public ElementType getType() {
        return ElementType.FROST;
    }

    @Override
    public void applyUpsides(Player player, int upgradeLevel) {
        if (player.getLocation().getBlock().getType().name().contains("SNOW")) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20, 1, false, false));
        } else if (player.getLocation().getBlock().getType().name().contains("ICE")) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20, 2, false, false));
        }

        if (upgradeLevel >= 2 && player.isSneaking() && player.getLocation().getBlock().getType().name().contains("WATER")) {
            player.getLocation().getBlock().setType(org.bukkit.Material.FROSTED_ICE);
        }
    }

    @Override
    public boolean ability1(ElementContext context) {
        return plugin.getAbilityRegistry().getAbility(ElementType.FROST, 1).execute(context);
    }

    @Override
    public boolean ability2(ElementContext context) {
        return plugin.getAbilityRegistry().getAbility(ElementType.FROST, 2).execute(context);
    }

    @Override
    public void clearEffects(Player player) {
        player.removePotionEffect(PotionEffectType.SPEED);
    }
}
