package net.saturn.elementSmp.services;

import net.saturn.elementSmp.ElementSmp;
import net.saturn.elementSmp.config.Constants;
import net.saturn.elementSmp.data.PlayerData;
import net.saturn.elementSmp.elements.Element;
import net.saturn.elementSmp.elements.ElementType;
import net.saturn.elementSmp.managers.ElementManager;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.EnumMap;
import java.util.Map;

/**
 * Centralized service for managing element passive effects.
 * Single source of truth for all effect-related operations.
 */
public class EffectService implements Listener {
    private static final int INFINITE_DURATION = 1000000; // Large enough to be considered infinite
    private final ElementSmp plugin;
    private final ElementManager elementManager;

    // Cache of required effects per element
    private final Map<ElementType, EffectRequirement[]> requiredEffects = new EnumMap<>(ElementType.class);

    public EffectService(ElementSmp plugin, ElementManager elementManager) {
        this.plugin = plugin;
        this.elementManager = elementManager;
        initializeRequirements();
        startMonitoring();
    }

    private void initializeRequirements() {
        requiredEffects.put(ElementType.WATER, new EffectRequirement[] {
                new EffectRequirement(PotionEffectType.CONDUIT_POWER, 0, false)
        });

        requiredEffects.put(ElementType.FIRE, new EffectRequirement[] {
                new EffectRequirement(PotionEffectType.FIRE_RESISTANCE, 0, false)
        });

        requiredEffects.put(ElementType.EARTH, new EffectRequirement[] {
                new EffectRequirement(PotionEffectType.HERO_OF_THE_VILLAGE, 0, false)
        });

        requiredEffects.put(ElementType.METAL, new EffectRequirement[] {
                new EffectRequirement(PotionEffectType.HASTE, 0, false)
        });
    }

    /**
     * Clear ALL element effects from a player.
     * Used when switching elements or logging out.
     */
    public void clearAllElementEffects(Player player) {
        PlayerData pd = elementManager.data(player.getUniqueId());
        ElementType currentElement = pd.getCurrentElement();

        // Hardcoded removal of element-granted effects
        player.removePotionEffect(PotionEffectType.CONDUIT_POWER);
        player.removePotionEffect(PotionEffectType.FIRE_RESISTANCE);
        player.removePotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE);
        player.removePotionEffect(PotionEffectType.HASTE);

        // Clear individual element state/tasks
        for (ElementType type : ElementType.values()) {
            Element element = elementManager.get(type);
            if (element != null) {
                element.clearEffects(player);
            }
        }

        // Reset health if not Life element
        resetHealthIfNeeded(player, currentElement);
    }

    private boolean isElementEffect(PotionEffect effect) {
        // Element effects are applied with a very long duration and specific flags
        return effect.getDuration() > 100000 || (effect.isAmbient() && !effect.hasParticles());
    }

    /**
     * Apply passive effects for player's current element.
     * Single source of truth for effect application.
     */
    public void applyPassiveEffects(Player player) {
        PlayerData pd = elementManager.data(player.getUniqueId());
        ElementType type = pd.getCurrentElement();

        if (type == null) return;

        Element element = elementManager.get(type);
        if (element != null) {
            element.applyUpsides(player, pd.getUpgradeLevel(type));
        }
    }

    /**
     * Validate and restore effects if needed.
     * Called periodically and after certain events.
     */
    public void validateEffects(Player player) {
        PlayerData pd = elementManager.data(player.getUniqueId());
        ElementType currentElement = pd.getCurrentElement();

        if (currentElement == null) return;

        int upgradeLevel = pd.getUpgradeLevel(currentElement);

        // Check required effects
        EffectRequirement[] requirements = requiredEffects.get(currentElement);
        if (requirements != null) {
            for (EffectRequirement req : requirements) {
                if (!req.upgradeRequired || upgradeLevel >= 2) {
                    if (!hasValidEffect(player, req.type, req.level)) {
                        player.addPotionEffect(new PotionEffect(
                                req.type, INFINITE_DURATION, req.level, true, false
                        ));
                    }
                }
            }
        }

        // Special handling for Water upgrade 2
        if (currentElement == ElementType.WATER && upgradeLevel >= 2) {
            if (!hasValidEffect(player, PotionEffectType.DOLPHINS_GRACE, 4)) {
                player.addPotionEffect(new PotionEffect(
                        PotionEffectType.DOLPHINS_GRACE, INFINITE_DURATION, 4, true, false
                ));
            }
        }

        // Validate health
        resetHealthIfNeeded(player, currentElement);
    }

    private boolean hasValidEffect(Player player, PotionEffectType type, int requiredLevel) {
        PotionEffect effect = player.getPotionEffect(type);
        if (effect == null) return false;
        
        // If it's an element effect, check if it's the right level
        if (isElementEffect(effect)) {
            return effect.getAmplifier() == requiredLevel;
        }
        
        // If it's a normal potion, only consider it valid if it's at least the same level
        // and has more than 5 seconds remaining
        return effect.getAmplifier() >= requiredLevel && effect.getDuration() > 100;
    }

    private void resetHealthIfNeeded(Player player, ElementType currentElement) {
        var attr = player.getAttribute(Attribute.MAX_HEALTH);
        if (attr == null) return;

        double targetHealth = currentElement == ElementType.LIFE ?
                Constants.Health.LIFE_MAX : Constants.Health.NORMAL_MAX;

        if (attr.getBaseValue() != targetHealth) {
            attr.setBaseValue(targetHealth);
            if (!player.isDead() && player.getHealth() > targetHealth) {
                player.setHealth(targetHealth);
            }
        }
    }

    /**
     * Start periodic monitoring of effects
     */
    private void startMonitoring() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    validateEffects(player);
                }
            }
        }.runTaskTimer(plugin, Constants.Timing.TWO_SECONDS, Constants.Timing.TWO_SECONDS);
    }

    // Event handlers
    @EventHandler(priority = EventPriority.MONITOR)
    public void onMilkConsume(PlayerItemConsumeEvent event) {
        if (event.getItem().getType() != org.bukkit.Material.MILK_BUCKET) return;

        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                applyPassiveEffects(player);
            }
        }, 1L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEffectRemove(EntityPotionEffectEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (event.getCause() != EntityPotionEffectEvent.Cause.COMMAND &&
                event.getCause() != EntityPotionEffectEvent.Cause.PLUGIN) return;

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                validateEffects(player);
            }
        }, 2L);
    }

    /**
     * Helper class for effect requirements
     */
    private static class EffectRequirement {
        final PotionEffectType type;
        final int level;
        final boolean upgradeRequired;

        EffectRequirement(PotionEffectType type, int level, boolean upgradeRequired) {
            this.type = type;
            this.level = level;
            this.upgradeRequired = upgradeRequired;
        }
    }
}
