package net.saturn.elementsmp.elements.impl.metal;

import net.saturn.elementsmp.ElementSmp;
import net.saturn.elementsmp.elements.core.BaseElement;
import net.saturn.elementsmp.elements.core.ElementContext;
import net.saturn.elementsmp.elements.core.ElementType;
import net.saturn.elementsmp.elements.abilities.Ability;
import net.saturn.elementsmp.elements.abilities.impl.metal.MetalDashAbility;
import net.saturn.elementsmp.elements.abilities.impl.metal.MagneticAccumulationAbility;
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
