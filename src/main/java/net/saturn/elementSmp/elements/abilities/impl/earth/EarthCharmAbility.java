package net.saturn.elementSmp.elements.abilities.impl.earth;

import net.saturn.elementSmp.elements.ElementContext;
import net.saturn.elementSmp.elements.abilities.BaseAbility;
import net.saturn.elementSmp.elements.impl.earth.EarthElement;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

public class EarthCharmAbility extends BaseAbility {

    private final net.saturn.elementSmp.ElementSmp plugin;
    
    public EarthCharmAbility(net.saturn.elementSmp.ElementSmp plugin) {
        super("earth_charm", 75, 30, 1);
        this.plugin = plugin;
    }

    @Override
    public boolean execute(ElementContext context) {
        Player player = context.getPlayer();
        long until = System.currentTimeMillis() + 30_000L;
        player.setMetadata(EarthElement.META_CHARM_NEXT_UNTIL, new FixedMetadataValue(plugin, until));
        player.sendMessage(ChatColor.GOLD + "Punch a mob to charm it for 30s - it will follow you!");
        return true;
    }

    @Override
    public String getName() {
        return ChatColor.YELLOW + "Mob Charm";
    }

    @Override
    public String getDescription() {
        return "Punch a mob to make it follow you for 30 seconds.";
    }
}
