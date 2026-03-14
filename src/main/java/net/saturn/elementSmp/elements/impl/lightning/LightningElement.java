package net.saturn.elementsmp.elements.impl.lightning;

import net.saturn.elementsmp.ElementSmp;
import net.saturn.elementsmp.elements.core.BaseElement;
import net.saturn.elementsmp.elements.core.ElementContext;
import net.saturn.elementsmp.elements.core.ElementType;
import net.saturn.elementsmp.elements.abilities.Ability;
import net.saturn.elementsmp.elements.abilities.impl.lightning.LightningSpeedAbility;
import net.saturn.elementsmp.elements.abilities.impl.lightning.ThunderstormAbility;
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
    }

    @Override
    public void clearEffects(Player player) {
        player.removePotionEffect(PotionEffectType.SPEED);
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
