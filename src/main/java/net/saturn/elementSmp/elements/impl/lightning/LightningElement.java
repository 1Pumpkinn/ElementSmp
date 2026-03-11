package net.saturn.elementSmp.elements.impl.lightning;

import net.saturn.elementSmp.ElementSmp;
import net.saturn.elementSmp.elements.BaseElement;
import net.saturn.elementSmp.elements.ElementContext;
import net.saturn.elementSmp.elements.ElementType;
import net.saturn.elementSmp.elements.abilities.Ability;
import org.bukkit.entity.Player;

public class LightningElement extends BaseElement {
    private final ElementSmp plugin;
    private final Ability ability1;
    private final Ability ability2;

    public LightningElement(ElementSmp plugin) {
        super(plugin);
        this.plugin = plugin;
        this.ability1 = new (plugin);
        this.ability2 = new (plugin);
    }

    @Override
    public ElementType getType() {
        return null;
    }

    @Override
    public void applyUpsides(Player player, int upgradeLevel) {

    }

    @Override
    public void clearEffects(Player player) {
        ability1.setActive(player, false);
        ability2.setActive(player, false);

    }

    @Override
    protected boolean executeAbility1(ElementContext context) {
        return false;
    }

    @Override
    protected boolean executeAbility2(ElementContext context) {
        return false;
    }
}
