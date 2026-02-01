package net.saturn.elementSmp.elements.impl.air;

import net.saturn.elementSmp.ElementSmp;
import net.saturn.elementSmp.elements.BaseElement;
import net.saturn.elementSmp.elements.ElementContext;
import net.saturn.elementSmp.elements.ElementType;
import net.saturn.elementSmp.elements.abilities.Ability;
import net.saturn.elementSmp.elements.abilities.impl.air.AirBlastAbility;
import net.saturn.elementSmp.elements.abilities.impl.air.AirDashAbility;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class AirElement extends BaseElement {
    private final ElementSmp plugin;
    private final Ability ability1;
    private final Ability ability2;

    public AirElement(ElementSmp plugin) {
        super(plugin);
        this.plugin = plugin;
        this.ability1 = new AirBlastAbility(plugin);
        this.ability2 = new AirDashAbility(plugin);
    }

    @Override
    public ElementType getType() {
        return ElementType.AIR;
    }

    @Override
    public void applyUpsides(Player player, int upgradeLevel) {
        // Upside 1: Slow falling while shifting in air (handled in AirSlowFallingPassive)
        // Upside 2: No fall damage (handled in AirFallDamagePassive)
        // No potion effects needed
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
        ability1.setActive(player, false);
        ability2.setActive(player, false);
    }
}
