package net.saturn.elementSmp.elements.impl.earth;

import net.saturn.elementSmp.ElementSmp;
import net.saturn.elementSmp.elements.BaseElement;
import net.saturn.elementSmp.elements.ElementContext;
import net.saturn.elementSmp.elements.ElementType;
import net.saturn.elementSmp.elements.abilities.Ability;
import net.saturn.elementSmp.elements.abilities.impl.earth.EarthquakeAbility;
import net.saturn.elementSmp.elements.abilities.impl.earth.EarthTunnelAbility;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class EarthElement extends BaseElement {
    public static final String META_MINE_UNTIL = "earth_mine_until";
    public static final String META_CHARM_NEXT_UNTIL = "earth_charm_next_until";
    public static final String META_TUNNELING = "earth_tunneling";

    private final ElementSmp plugin;
    private final Ability ability1;
    private final Ability ability2;

    public EarthElement(ElementSmp plugin) {
        super(plugin);
        this.plugin = plugin;
        this.ability1 = new EarthTunnelAbility(plugin);
        this.ability2 = new EarthquakeAbility(plugin);
    }

    @Override
    public ElementType getType() {
        return ElementType.EARTH;
    }

    @Override
    public void applyUpsides(Player player, int upgradeLevel) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, PotionEffect.INFINITE_DURATION, 0, true, false));
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
    protected boolean canCancelAbility1(ElementContext context) {
        // Check if the player has the tunneling metadata - if so, they can cancel
        boolean canCancel = context.getPlayer().hasMetadata(META_TUNNELING);
        if (canCancel) {

        }
        return canCancel;
    }

    @Override
    public void clearEffects(Player player) {
        player.removePotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE);
        player.removeMetadata(META_MINE_UNTIL, plugin);
        player.removeMetadata(META_CHARM_NEXT_UNTIL, plugin);
        player.removeMetadata(META_TUNNELING, plugin);
        ability1.setActive(player, false);
        ability2.setActive(player, false);
    }
}
