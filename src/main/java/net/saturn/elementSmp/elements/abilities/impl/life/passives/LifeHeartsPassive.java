package net.saturn.elementSmp.elements.abilities.impl.life.passives;

import net.saturn.elementSmp.ElementSmp;
import net.saturn.elementSmp.config.Constants;
import net.saturn.elementSmp.elements.ElementType;
import net.saturn.elementSmp.managers.ElementManager;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
public class LifeHeartsPassive implements Listener {
    private final ElementSmp plugin;
    private final ElementManager elementManager;
    private final Set<UUID> tracked = new HashSet<>();

    public LifeHeartsPassive(ElementSmp plugin, ElementManager elementManager) {
        this.plugin = plugin;
        this.elementManager = elementManager;
        startTask();
    }

    private void startTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    var pd = elementManager.data(player.getUniqueId());
                    if (pd == null || pd.getCurrentElement() != ElementType.LIFE) {
                        resetIfNeeded(player);
                        tracked.remove(player.getUniqueId());
                        continue;
                    }

                    int upgrade = pd.getUpgradeLevel(ElementType.LIFE);
                    if (upgrade >= 2) {
                        applyMaxHealth(player, Constants.Health.LIFE_MAX);
                        tracked.add(player.getUniqueId());
                    } else {
                        resetIfNeeded(player);
                        tracked.remove(player.getUniqueId());
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, Constants.Timing.TWO_SECONDS);
    }

    private void applyMaxHealth(Player player, double target) {
        var attr = player.getAttribute(Attribute.MAX_HEALTH);
        if (attr == null) return;
        if (attr.getBaseValue() != target) {
            attr.setBaseValue(target);
            if (!player.isDead() && player.getHealth() > target) {
                player.setHealth(target);
            }
        }
    }

    private void resetIfNeeded(Player player) {
        applyMaxHealth(player, Constants.Health.NORMAL_MAX);
    }
}
