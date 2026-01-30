package net.saturn.elementSmp.listeners.ability;

import net.saturn.elementSmp.ElementSmp;
import net.saturn.elementSmp.data.PlayerData;
import net.saturn.elementSmp.managers.ElementManager;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AbilityListener implements Listener {
    private static final long DEBOUNCE_MS = 150;

    private final ElementSmp plugin;
    private final ElementManager elements;
    private final Map<UUID, Long> lastActivation = new ConcurrentHashMap<>();

    public AbilityListener(ElementSmp plugin, ElementManager elements) {
        this.plugin = plugin;
        this.elements = elements;
    }

    public void triggerAbility(Player player, int abilityNum) {
        if (!hasElement(player)) return;

        long currentTime = System.currentTimeMillis();
        long lastTime = lastActivation.getOrDefault(player.getUniqueId(), 0L);

        if (currentTime - lastTime < DEBOUNCE_MS) {
            return;
        }

        lastActivation.put(player.getUniqueId(), currentTime);

        if (abilityNum == 2) {
            elements.useAbility2(player);
        } else {
            elements.useAbility1(player);
        }
    }

    private boolean hasElement(Player player) {
        PlayerData pd = elements.data(player.getUniqueId());
        return pd.getCurrentElement() != null;
    }
}


