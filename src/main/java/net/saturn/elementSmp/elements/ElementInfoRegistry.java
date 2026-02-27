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
        INFO.put(ElementType.AIR, new ElementInfo(
                "Air",
                ChatColor.WHITE,
                List.of(
                        "Slow Falling while sneaking",
                        "No fall damage at Upgrade II (except pointed dripstone)"
                ),
                new ElementInfo.AbilityInfo("Air Dash", "Dash forward and knock back nearby entities", 50, 1),
                new ElementInfo.AbilityInfo("Air Blast", "Leap and slam, launching nearby entities and dealing true damage", 75, 2)
        ));

        INFO.put(ElementType.FIRE, new ElementInfo(
                "Fire",
                ChatColor.RED,
                List.of(
                        "Auto-smelt ore drops",
                        "10% chance to set targets on fire on hit (Upgrade II)"
                ),
                new ElementInfo.AbilityInfo("Scorch", "Your next hit removes Fire Resistance", 50, 1),
                new ElementInfo.AbilityInfo("Inferno Blast", "Explosion around you that deals true damage", 75, 2)
        ));

        INFO.put(ElementType.WATER, new ElementInfo(
                "Water",
                ChatColor.BLUE,
                List.of(
                        "Conduit Power",
                        "Tridents pull hit targets toward you (Upgrade II)"
                ),
                new ElementInfo.AbilityInfo("Water Prison", "Trap a nearby target in a suffocating bubble for a short time", 50, 1),
                new ElementInfo.AbilityInfo("Leeching Trident", "A trident orbits a hit target and deals periodic true damage", 75, 2)
        ));

        INFO.put(ElementType.EARTH, new ElementInfo(
                "Earth",
                ChatColor.YELLOW,
                List.of(
                        "Hero of the Village",
                        "Double ore drops (Upgrade II; no Silk Touch)"
                ),
                new ElementInfo.AbilityInfo("Earth Tunnel", "Create a short 3×3 tunnel ahead of you", 50, 1),
                new ElementInfo.AbilityInfo("Earthquake", "Shockwave that slows and weakens nearby enemies", 75, 2)
        ));

        INFO.put(ElementType.LIFE, new ElementInfo(
                "Life",
                ChatColor.GREEN,
                List.of(
                        "Crop growth aura around you",
                        "Increased max health at Upgrade II"
                ),
                new ElementInfo.AbilityInfo("Nature's Eye", "Highlight nearby creatures through walls for a short duration", 50, 1),
                new ElementInfo.AbilityInfo("Entangling Roots", "Immobilize a nearby target briefly", 75, 2)
        ));

        INFO.put(ElementType.DEATH, new ElementInfo(
                "Death",
                ChatColor.DARK_PURPLE,
                List.of(
                        "Chance to apply Wither on hit",
                        "Become invisible at low health (Upgrade II)"
                ),
                new ElementInfo.AbilityInfo("Summon Undead", "Summon a Wither Skeleton ally for a short time", 50, 1),
                new ElementInfo.AbilityInfo("Shadow Step", "Teleport to the surface your shadow reaches", 75, 2)
        ));

        INFO.put(ElementType.METAL, new ElementInfo(
                "Metal",
                ChatColor.GRAY,
                List.of(
                        "Haste",
                        "Arrow damage immunity (Upgrade II)"
                ),
                new ElementInfo.AbilityInfo("Metal Dash", "Dash forward, damaging and knocking back what you hit; missing briefly stuns you", 50, 1),
                new ElementInfo.AbilityInfo("Magnetic Accumulation", "Tag a target; your damage is stored then released as a burst", 75, 2)
        ));

        INFO.put(ElementType.FROST, new ElementInfo(
                "Frost",
                ChatColor.AQUA,
                List.of(
                        "Speed boost on ice (Upgrade II)",
                        "Freeze nearby surface water while sneaking (Upgrade II)"
                ),
                new ElementInfo.AbilityInfo("Frost Circle", "Chilling area around you that slows and harms nearby enemies over time", 50, 1),
                new ElementInfo.AbilityInfo("Frost Nova", "Instant frost blast that damages and launches nearby enemies", 75, 2)
        ));
    }

    public static Optional<ElementInfo> getInfo(ElementType type) {
        return Optional.ofNullable(INFO.get(type));
    }

    public static Map<ElementType, ElementInfo> getAllInfo() {
        return Map.copyOf(INFO);
    }
}
