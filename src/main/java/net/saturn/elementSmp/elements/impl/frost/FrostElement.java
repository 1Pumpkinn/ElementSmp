package net.saturn.elementSmp.elements.impl.frost;

import net.saturn.elementSmp.ElementSmp;
import net.saturn.elementSmp.elements.BaseElement;
import net.saturn.elementSmp.elements.ElementContext;
import net.saturn.elementSmp.elements.ElementType;
import net.saturn.elementSmp.elements.abilities.Ability;
import org.bukkit.entity.Player;

public class FrostElement extends BaseElement {

    private final ElementSmp plugin;
    private final Ability ability1;
    private final Ability ability2;

    public FrostElement(ElementSmp plugin) {
        super(plugin);
        this.plugin = plugin;
        this.ability1 = plugin.getAbilityRegistry().getAbility(ElementType.FROST, 1);
        this.ability2 = plugin.getAbilityRegistry().getAbility(ElementType.FROST, 2);
    }

    @Override
    public ElementType getType() {
        return ElementType.FROST;
    }

    @Override
    public void applyUpsides(Player player, int upgradeLevel) {
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
