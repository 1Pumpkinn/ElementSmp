package net.saturn.elementSmp.elements.impl.metal;

import net.saturn.elementSmp.ElementSmp;
import net.saturn.elementSmp.elements.BaseElement;
import net.saturn.elementSmp.elements.ElementContext;
import net.saturn.elementSmp.elements.ElementType;
import net.saturn.elementSmp.elements.abilities.Ability;
import net.saturn.elementSmp.elements.abilities.impl.metal.MagneticAccumulationAbility;
import net.saturn.elementSmp.elements.abilities.impl.metal.MetalDashAbility;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class MetalElement extends BaseElement {
    private final ElementSmp plugin;
    private final Ability ability1;
    private final Ability ability2;

    public MetalElement(ElementSmp plugin) {
        super(plugin);
        this.plugin = plugin;
        this.ability1 = new MetalDashAbility(plugin);
        this.ability2 = new MagneticAccumulationAbility(plugin);
    }

    @Override
    public ElementType getType() {
        return ElementType.METAL;
    }

    @Override
    public void applyUpsides(Player player, int upgradeLevel) {
        // Upside 1: haste 1 permanently
        player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, PotionEffect.INFINITE_DURATION, 0, true, false));

        // Upside 2: Arrow immunity (handled in listener)
        // No passive effect needed here
    }

    @Override
    protected boolean executeAbility1(ElementContext context) {
        return ability1.execute(context);
    }

    @Override
    protected boolean executeAbility2(ElementContext context) {
        return ability2.execute(context);
    }

    @Override
    public void clearEffects(Player player) {
        player.removePotionEffect(PotionEffectType.HASTE);
        ability1.setActive(player, false);
        ability2.setActive(player, false);
    }
}
