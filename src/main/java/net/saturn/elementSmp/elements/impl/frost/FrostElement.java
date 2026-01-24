package net.saturn.elementSmp.elements.impl.frost;

import net.saturn.elementSmp.ElementSmp;
import net.saturn.elementSmp.elements.BaseElement;
import net.saturn.elementSmp.elements.ElementContext;
import net.saturn.elementSmp.elements.ElementType;
import net.saturn.elementSmp.elements.abilities.Ability;
import net.saturn.elementSmp.elements.abilities.impl.frost.FrostCircleAbility;
import net.saturn.elementSmp.elements.abilities.impl.frost.FrostPunchAbility;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class FrostElement extends BaseElement {
    public static final String META_FROZEN_PUNCH_READY = "frost_frozen_punch_ready";

    private final ElementSmp plugin;
    private final Ability ability1;
    private final Ability ability2;

    public FrostElement(ElementSmp plugin) {
        super(plugin);
        this.plugin = plugin;
        this.ability1 = new FrostCircleAbility(plugin);
        this.ability2 = new FrostPunchAbility(plugin);
    }

    @Override
    public ElementType getType() {
        return ElementType.FROST;
    }

    @Override
    public void applyUpsides(Player player, int upgradeLevel) {
        // Upsides are handled by FrostPassiveListener
        // Upside 1: Speed 2 when wearing leather boots (always active)
        // Upside 2: Speed 3 on ice (requires upgrade level 2)
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
        player.removeMetadata(META_FROZEN_PUNCH_READY, plugin);
        ability1.setActive(player, false);
        ability2.setActive(player, false);
    }
}
