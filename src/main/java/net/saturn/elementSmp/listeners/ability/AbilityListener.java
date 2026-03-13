package net.saturn.elementsmp.listeners.ability;

import net.saturn.elementsmp.ElementSmp;
import net.saturn.elementsmp.data.PlayerData;
import net.saturn.elementsmp.managers.ElementManager;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class AbilityListener implements Listener {
    private final ElementSmp plugin;
    private final ElementManager elements;

    public AbilityListener(ElementSmp plugin, ElementManager elements) {
        this.plugin = plugin;
        this.elements = elements;
    }

    public boolean triggerAbility(Player player, int abilityNum) {
        PlayerData pd = elements.data(player.getUniqueId());
        if (pd.getCurrentElement() == null) return false;

        return abilityNum == 2 ? elements.useAbility2(player) : elements.useAbility1(player);
    }
}


