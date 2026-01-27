package net.saturn.elementSmp.elements.impl.life;

import net.saturn.elementSmp.ElementSmp;
import net.saturn.elementSmp.config.Constants;
import net.saturn.elementSmp.elements.BaseElement;
import net.saturn.elementSmp.elements.ElementContext;
import net.saturn.elementSmp.elements.ElementType;
import net.saturn.elementSmp.elements.abilities.Ability;
import net.saturn.elementSmp.elements.abilities.impl.life.EntanglingRootsAbility;
import net.saturn.elementSmp.elements.abilities.impl.life.NaturesEyeAbility;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

public class LifeElement extends BaseElement {

    private final ElementSmp plugin;
    private final Ability ability1;
    private final Ability ability2;



    public LifeElement(ElementSmp plugin) {
        super(plugin);
        this.plugin = plugin;
        this.ability1 = new NaturesEyeAbility(plugin);
        this.ability2 = new EntanglingRootsAbility(plugin);
    }

    @Override
    public ElementType getType() {
        return ElementType.LIFE;
    }

    // APPLY UPSIDES
    @Override
    public void applyUpsides(Player player, int upgradeLevel) {
        // Upside 1: 15 hearts (30 HP)
        var attr = player.getAttribute(Attribute.MAX_HEALTH);
        if (attr != null) {
            if (attr.getBaseValue() < Constants.Health.LIFE_MAX) attr.setBaseValue(Constants.Health.LIFE_MAX);
            if (player.getHealth() > attr.getBaseValue()) {
                player.setHealth(attr.getBaseValue());
            }
        }

        // Upside 2: crop growth aura (handled in LifePassive)
    }

    // Abilities
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
        // Reset health if they are not Life element anymore
        var attr = player.getAttribute(Attribute.MAX_HEALTH);
        if (attr != null) {
            attr.setBaseValue(20.0);
        }

        ability1.setActive(player, false);
        ability2.setActive(player, false);
    }
}

