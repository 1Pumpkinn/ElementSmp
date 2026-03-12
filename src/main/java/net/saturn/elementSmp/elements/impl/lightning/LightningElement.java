package net.saturn.elementSmp.elements.impl.lightning;

import net.saturn.elementSmp.ElementSmp;
import net.saturn.elementSmp.elements.BaseElement;
import net.saturn.elementSmp.elements.ElementContext;
import net.saturn.elementSmp.elements.ElementType;
import net.saturn.elementSmp.elements.abilities.Ability;
import net.saturn.elementSmp.elements.abilities.impl.lightning.LightningSpeedAbility;
import net.saturn.elementSmp.elements.abilities.impl.lightning.ThunderstormAbility;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class LightningElement extends BaseElement {
    private final Ability ability1;
    private final Ability ability2;

    public LightningElement(ElementSmp plugin) {
        super(plugin);
        this.ability1 = new LightningSpeedAbility(plugin);
        this.ability2 = new ThunderstormAbility(plugin);
    }

    @Override
    public ElementType getType() {
        return ElementType.LIGHTNING;
    }

    @Override
    public void applyUpsides(Player player, int upgradeLevel) {
        // passive 1 Perm speed (Speed I)
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, PotionEffect.INFINITE_DURATION, 0, true, false, false));

        // passive 2 Haste 2 (Upgrade II)
        if (upgradeLevel >= 2) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, PotionEffect.INFINITE_DURATION, 1, true, false, false));
        }
    }

    @Override
    public void clearEffects(Player player) {
        player.removePotionEffect(PotionEffectType.SPEED);
        player.removePotionEffect(PotionEffectType.HASTE);
        ability1.setActive(player, false);
        ability2.setActive(player, false);
    }

    @Override
    protected boolean executeAbility1(ElementContext context) {
        return ability1.execute(context);
    }

    @Override
    protected boolean executeAbility2(ElementContext context) {
        return ability2.execute(context);
    }
}
