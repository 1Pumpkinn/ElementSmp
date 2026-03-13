package net.saturn.elementsmp.elements.impl.death;

import net.saturn.elementsmp.ElementSmp;
import net.saturn.elementsmp.elements.core.BaseElement;
import net.saturn.elementsmp.elements.core.ElementContext;
import net.saturn.elementsmp.elements.core.ElementType;
import net.saturn.elementsmp.elements.abilities.Ability;
import net.saturn.elementsmp.elements.abilities.impl.death.DeathSummonUndeadAbility;
import net.saturn.elementsmp.elements.abilities.impl.death.ShadowStepAbility;
import net.saturn.elementsmp.elements.passives.death.DeathInvisibilityPassive;
import org.bukkit.entity.Player;

public class DeathElement extends BaseElement {
    private final ElementSmp plugin;
    private final Ability ability1;
    private final Ability ability2;

    public DeathElement(ElementSmp plugin) {
        super(plugin);
        this.plugin = plugin;
        this.ability1 = new DeathSummonUndeadAbility(plugin);
        this.ability2 = new ShadowStepAbility(plugin);
    }

    @Override
    public ElementType getType() {
        return ElementType.DEATH;
    }

    @Override
    public void applyUpsides(Player player, int upgradeLevel) {
        // Upside 1: 10% Wither effect on hit (handled in DeathPassive)
        // Upside 2: Invisibility and equipment hiding at 2 hearts (handled in DeathPassive)
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
        DeathInvisibilityPassive.clearState(player);
    }
}
