package net.saturn.elementSmp.elements;

import org.bukkit.ChatColor;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Centralized registry for element information.
 * All element descriptions, abilities, and benefits in one place.
 */
public final class ElementInfoRegistry {
    private static final Map<ElementType, ElementInfo> INFO = new EnumMap<>(ElementType.class);

    static {
        // Air
        INFO.put(ElementType.AIR, new ElementInfo(
                "Air",
                ChatColor.WHITE,
                List.of(
                        "Slow Falling while shifting in air",
                        "No fall damage (except on dripstone) (Upgrade II)"
                ),
                new ElementInfo.AbilityInfo("Air Dash", "Dash forward swiftly, pushing enemies aside", 75, 1),
                new ElementInfo.AbilityInfo("Air Blast", "Launch into the air, then slam down to damage and fling enemies", 50, 2)
        ));

        // Fire
        INFO.put(ElementType.FIRE, new ElementInfo(
                "Fire",
                ChatColor.RED,
                List.of(
                        "Auto smelt ores",
                        "Fire aspect on any tool or fist (4s) (Upgrade II)"
                ),
                new ElementInfo.AbilityInfo("Scorch", "The next entity you hit clears their Fire Resistance", 50, 1),
                new ElementInfo.AbilityInfo("Inferno Blast", "Causes an explosion on you dealing 5 hearts to nearby entities", 75, 2)
        ));

        // Water
        INFO.put(ElementType.WATER, new ElementInfo(
                "Water",
                ChatColor.BLUE,
                List.of(
                        "Permanent Conduit Power",
                        "Loyalty Tridents pull entities (Upgrade II)"
                ),
                new ElementInfo.AbilityInfo("Water Prison", "Trap a target in a drowning water bubble for 3 seconds", 50, 1),
                new ElementInfo.AbilityInfo("Leeching Trident", "Throw a trident that orbits a target and deals true damage over time", 75, 2)
        ));

        // Earth
        INFO.put(ElementType.EARTH, new ElementInfo(
                "Earth",
                ChatColor.YELLOW,
                List.of(
                        "Hero of The Village permanently",
                        "Double ore drops (Upgrade II)"
                ),
                new ElementInfo.AbilityInfo("Earth Tunnel", "Dig tunnels through stone and dirt", 50, 1),
                new ElementInfo.AbilityInfo("Earthquake", "Stomp the ground to damage and slow nearby enemies", 75, 2)
        ));

        // Life
        INFO.put(ElementType.LIFE, new ElementInfo(
                "Life",
                ChatColor.GREEN,
                List.of(
                        "Crop growth aura around you",
                        "15 Hearts (30 HP) max health (Upgrade II)"
                ),
                new ElementInfo.AbilityInfo("Nature's Eye", "Reveal nearby living entities through walls", 50, 1),
                new ElementInfo.AbilityInfo("Entangling Roots", "Pull enemies into the ground and suffocate them", 75, 2)
        ));

        // Death
        INFO.put(ElementType.DEATH, new ElementInfo(
                "Death",
                ChatColor.DARK_PURPLE,
                List.of(
                        "10% chance to apply Wither effect on hit",
                        "Invisibility and equipment hiding at 2 hearts (Upgrade II)"
                ),
                new ElementInfo.AbilityInfo("Summon Undead", "Summon a powerful undead ally", 50, 1),
                new ElementInfo.AbilityInfo("Shadow Step", "Teleport forward to the first surface you aim at", 75, 2)
        ));

        // Metal
        INFO.put(ElementType.METAL, new ElementInfo(
                "Metal",
                ChatColor.GRAY,
                List.of(
                        "Haste I permanently",
                        "Immunity to all arrows (Upgrade II)"
                ),
                new ElementInfo.AbilityInfo("Metal Dash", "Swiftly dash through enemies, damaging them", 50, 1),
                new ElementInfo.AbilityInfo("Magnetic Accumulation", "Accumulate damage on a target, then deal it all at once after 3s", 75, 2)
        ));

        // Frost
        INFO.put(ElementType.FROST, new ElementInfo(
                "Frost",
                ChatColor.AQUA,
                List.of(
                        "Speed II on snow, Speed III on ice",
                        "Freeze water walking (Shift) (Upgrade II)"
                ),
                new ElementInfo.AbilityInfo("Frozen Punch", "Punch an enemy to freeze them solid", 75, 1),
                new ElementInfo.AbilityInfo("Frost Nova", "Create a burst of frost, damaging and freezing nearby enemies", 50, 2)
        ));
    }

    public static Optional<ElementInfo> getInfo(ElementType type) {
        return Optional.ofNullable(INFO.get(type));
    }

    public static Map<ElementType, ElementInfo> getAllInfo() {
        return Map.copyOf(INFO);
    }
}
