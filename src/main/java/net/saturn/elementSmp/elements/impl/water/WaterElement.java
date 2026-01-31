package net.saturn.elementSmp.elements.impl.water;

import net.saturn.elementSmp.ElementSmp;
import net.saturn.elementSmp.elements.BaseElement;
import net.saturn.elementSmp.elements.ElementContext;
import net.saturn.elementSmp.elements.ElementType;
import net.saturn.elementSmp.elements.abilities.Ability;
import net.saturn.elementSmp.elements.abilities.impl.water.WaterLeechTridentAbility;
import net.saturn.elementSmp.elements.abilities.impl.water.WaterPrisonAbility;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class WaterElement extends BaseElement {
    private final ElementSmp plugin;
    private final Ability ability1;
    private final Ability ability2;

    public WaterElement(ElementSmp plugin) {
        super(plugin);
        this.plugin = plugin;
        this.ability1 = new WaterPrisonAbility(plugin);
        this.ability2 = new WaterLeechTridentAbility(plugin);
    }

    @Override
    public ElementType getType() { return ElementType.WATER; }

    @Override
    public void applyUpsides(Player player, int upgradeLevel) {
        // Upside 1: Infinite conduit power
        player.addPotionEffect(new PotionEffect(PotionEffectType.CONDUIT_POWER, PotionEffect.INFINITE_DURATION, 0, true, false));
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
        player.removePotionEffect(PotionEffectType.CONDUIT_POWER);
        ability1.setActive(player, false);
        ability2.setActive(player, false);
    }
}
