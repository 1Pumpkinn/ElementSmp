package net.saturn.elementSmp.elements.abilities.impl.frost;

import net.saturn.elementSmp.ElementSmp;
import net.saturn.elementSmp.config.MetadataKeys;
import net.saturn.elementSmp.elements.ElementContext;
import net.saturn.elementSmp.elements.abilities.BaseAbility;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

public class IcicleDropAbility extends BaseAbility {
    private final ElementSmp plugin;
    public static final String META_ICICLE_DROP_READY = MetadataKeys.Frost.ICICLE_DROP_READY;

    public IcicleDropAbility(ElementSmp plugin) {
        super();
        this.plugin = plugin;
    }

    @Override
    public boolean execute(ElementContext context) {
        Player player = context.getPlayer();

        // Set metadata indicating the next punch will drop an icicle
        long until = System.currentTimeMillis() + 10_000L; // 10 seconds to use it
        player.setMetadata(META_ICICLE_DROP_READY, new FixedMetadataValue(plugin, until));



        // Visual and audio feedback
        player.playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0f, 1.5f);

        Location loc = player.getLocation().add(0, 1, 0);
        player.getWorld().spawnParticle(Particle.SNOWFLAKE, loc, 30, 0.3, 0.3, 0.3, 0.1, null, true);
        player.getWorld().spawnParticle(Particle.CLOUD, loc, 15, 0.3, 0.3, 0.3, 0.05, null, true);

        return true;
    }
}
