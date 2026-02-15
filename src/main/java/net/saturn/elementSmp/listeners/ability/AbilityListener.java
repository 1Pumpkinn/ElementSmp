package net.saturn.elementSmp.listeners.ability;

import net.saturn.elementSmp.ElementSmp;
import net.saturn.elementSmp.data.PlayerData;
import net.saturn.elementSmp.managers.ConfigManager;
import net.saturn.elementSmp.managers.ElementManager;
import net.saturn.elementSmp.managers.ManaManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AbilityListener implements Listener {
    private static final long DEBOUNCE_MS = 0;

    private final ElementSmp plugin;
    private final ElementManager elements;
    private final ManaManager manaManager;
    private final net.saturn.elementSmp.managers.ConfigManager configManager;
    private final Map<UUID, Long> lastActivation = new ConcurrentHashMap<>();

    public AbilityListener(ElementSmp plugin, ElementManager elements, ManaManager manaManager, ConfigManager configManager) {
        this.plugin = plugin;
        this.elements = elements;
        this.manaManager = manaManager;
        this.configManager = configManager;
    }

    public boolean triggerAbility(Player player, int abilityNum) {
        PlayerData pd = elements.data(player.getUniqueId());
        if (pd.getCurrentElement() == null) return false;

        long currentTime = System.currentTimeMillis();
        long lastTime = lastActivation.getOrDefault(player.getUniqueId(), 0L);

        if (currentTime - lastTime < DEBOUNCE_MS) {
            return false;
        }

        int cost = abilityNum == 1 ? configManager.getAbility1Cost(pd.getCurrentElement()) : configManager.getAbility2Cost(pd.getCurrentElement());
        if (manaManager.get(player.getUniqueId()).getMana() < cost) {
            player.sendMessage(ChatColor.RED + "Not enough mana!");
            return false;
        }

        lastActivation.put(player.getUniqueId(), currentTime);

        if (abilityNum == 2) {
            return elements.useAbility2(player);
        } else {
            return elements.useAbility1(player);
        }
    }
}


