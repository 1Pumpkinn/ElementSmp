package net.saturn.elementSmp.elements.impl.death;

import net.saturn.elementSmp.ElementSmp;
import net.saturn.elementSmp.elements.BaseElement;
import net.saturn.elementSmp.elements.ElementContext;
import net.saturn.elementSmp.elements.ElementType;
import net.saturn.elementSmp.elements.abilities.Ability;
import net.saturn.elementSmp.elements.abilities.impl.death.DeathSummonUndeadAbility;
import net.saturn.elementSmp.elements.abilities.impl.death.ShadowStepAbility;
import net.saturn.elementSmp.elements.abilities.impl.death.passives.DeathInvisibilityPassive;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class DeathElement extends BaseElement {
    private final ElementSmp plugin;
    private final Ability ability1;
    private final Ability ability2;

    public DeathElement(ElementSmp plugin) {
        super(plugin);
        this.plugin = plugin;
        this.ability1 = new DeathSummonUndeadAbility(plugin);
        this.ability2 = new ShadowStepAbility(plugin);
    }

    @Override
    public ElementType getType() {
        return ElementType.DEATH;
    }

    @Override
    public void applyUpsides(Player player, int upgradeLevel) {
        // Upside 1: 10% Wither effect on hit (handled in DeathPassive)
        // Upside 2: Invisibility and equipment hiding at 2 hearts (handled in DeathPassive)
    }

    @Override
    protected boolean executeAbility1(ElementContext context) {
        plugin.getLogger().info("DEBUG: DeathElement.executeAbility1 called for " + context.getPlayer().getName());
        return ability1.execute(context);
    }

    @Override
    protected boolean executeAbility2(ElementContext context) {
        plugin.getLogger().info("DEBUG: DeathElement.executeAbility2 called for " + context.getPlayer().getName());
        return ability2.execute(context);
    }

    @Override
    public void clearEffects(Player player) {
        ability1.setActive(player, false);
        ability2.setActive(player, false);
        DeathInvisibilityPassive.clearState(player);
    }
}
