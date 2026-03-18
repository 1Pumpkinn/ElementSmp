package net.saturn.elementsmp.elements.impl.lightning;

import net.saturn.elementsmp.ElementSmp;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class AltarLightningElement extends LightningElement {
    public AltarLightningElement(ElementSmp plugin) {
        super(plugin);
    }

    @Override
    public void applyUpsides(Player player, int upgradeLevel) {
        // Altar Lightning also gets Speed I (amplifier 0)
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, PotionEffect.INFINITE_DURATION, 0, true, false, false));
    }
}
