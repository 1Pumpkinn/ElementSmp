package net.saturn.elementSmp.elements.impl.water;

import net.saturn.elementSmp.ElementSmp;
import net.saturn.elementSmp.elements.BaseElement;
import net.saturn.elementSmp.elements.ElementContext;
import net.saturn.elementSmp.elements.ElementType;
import net.saturn.elementSmp.elements.abilities.Ability;
import net.saturn.elementSmp.elements.abilities.impl.water.WaterBeamAbility;
import net.saturn.elementSmp.elements.abilities.impl.water.WaterGeyserAbility;
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
        this.ability1 = new WaterGeyserAbility(plugin);
        this.ability2 = new WaterBeamAbility(plugin);
    }

    @Override
    public ElementType getType() { return ElementType.WATER; }

    @Override
    public void applyUpsides(Player player, int upgradeLevel) {
        // Upside 1: Infinite conduit power
        player.addPotionEffect(new PotionEffect(PotionEffectType.CONDUIT_POWER, Integer.MAX_VALUE, 0, true, false));
        
        if (upgradeLevel >= 2) {
            // Upside 2: Dolphins grace 5 (level 4 = dolphins grace 5)
            player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, Integer.MAX_VALUE, 4, true, false));
        }
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
    
    @Override
    public String getDisplayName() {
        return ChatColor.AQUA + "Water";
    }
    
    @Override
    public String getDescription() {
        return "Harness the flowing power of water to control the battlefield.";
    }
    
    @Override
    public String getAbility1Name() {
        return ability1.getName();
    }
    
    @Override
    public String getAbility1Description() {
        return ability1.getDescription();
    }
    
    @Override
    public String getAbility2Name() {
        return ability2.getName();
    }
    
    @Override
    public String getAbility2Description() {
        return ability2.getDescription();
    }
}
