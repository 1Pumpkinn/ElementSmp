package net.saturn.elementSmp.elements.abilities;

import net.saturn.elementSmp.ElementSmp;
import net.saturn.elementSmp.elements.ElementType;
import net.saturn.elementSmp.elements.abilities.impl.air.*;
import net.saturn.elementSmp.elements.abilities.impl.death.DeathSummonUndeadAbility;
import net.saturn.elementSmp.elements.abilities.impl.death.ShadowStepAbility;
import net.saturn.elementSmp.elements.abilities.impl.earth.EarthTunnelAbility;
import net.saturn.elementSmp.elements.abilities.impl.earth.EarthquakeAbility;
import net.saturn.elementSmp.elements.abilities.impl.fire.*;
import net.saturn.elementSmp.elements.abilities.impl.frost.*;
import net.saturn.elementSmp.elements.abilities.impl.life.*;
import net.saturn.elementSmp.elements.abilities.impl.metal.*;
import net.saturn.elementSmp.elements.abilities.impl.water.*;

import java.util.*;

/**
 * Centralized registry for all abilities.
 * Provides type-safe access to abilities and handles registration.
 */
public final class AbilityRegistry {
    private final ElementSmp plugin;
    private final Map<ElementType, AbilitySet> abilities = new EnumMap<>(ElementType.class);

    /**
     * Immutable record holding an element's two abilities
     */
    public record AbilitySet(Ability ability1, Ability ability2) {
        public AbilitySet {
            Objects.requireNonNull(ability1, "ability1 cannot be null");
            Objects.requireNonNull(ability2, "ability2 cannot be null");
        }
    }

    public AbilityRegistry(ElementSmp plugin) {
        this.plugin = plugin;
        registerAll();
    }

    /**
     * Register all abilities for all elements
     */
    private void registerAll() {
        // Air
        register(ElementType.AIR,
                new AirBlastAbility(plugin),
                new AirDashAbility(plugin)
        );

        // Water
        register(ElementType.WATER,
                new WaterGeyserAbility(plugin),
                new WaterBeamAbility(plugin)
        );

        // Fire
        register(ElementType.FIRE,
                new FireballAbility(plugin),
                new MeteorShowerAbility(plugin)
        );

        // Earth
        register(ElementType.EARTH,
                new EarthTunnelAbility(plugin),
                new EarthquakeAbility(plugin)
        );

        // Life
        register(ElementType.LIFE,
                new NaturesEyeAbility(plugin),
                new EntanglingRootsAbility(plugin)
        );

        // Death
        register(ElementType.DEATH,
                new DeathSummonUndeadAbility(plugin),
                new ShadowStepAbility(plugin)
        );

        // Metal
        register(ElementType.METAL,
                new MetalDashAbility(plugin),
                new MetalChainAbility(plugin)
        );

        // Frost
        register(ElementType.FROST,
                new FrostCircleAbility(plugin),
                new IcicleDropAbility(plugin)
        );

        plugin.getLogger().info("Registered " + abilities.size() + " element ability sets");
    }

    /**
     * Register abilities for an element
     */
    private void register(ElementType type, Ability ability1, Ability ability2) {
        abilities.put(type, new AbilitySet(ability1, ability2));
    }

    /**
     * Get abilities for an element
     */
    public Optional<AbilitySet> getAbilities(ElementType type) {
        return Optional.ofNullable(abilities.get(type));
    }

    /**
     * Get ability 1 for an element
     */
    public Optional<Ability> getAbility1(ElementType type) {
        return getAbilities(type).map(AbilitySet::ability1);
    }

    /**
     * Get ability 2 for an element
     */
    public Optional<Ability> getAbility2(ElementType type) {
        return getAbilities(type).map(AbilitySet::ability2);
    }

    /**
     * Get all registered element types
     */
    public Set<ElementType> getRegisteredElements() {
        return Collections.unmodifiableSet(abilities.keySet());
    }
}
