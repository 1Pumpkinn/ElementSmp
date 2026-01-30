package net.saturn.elementSmp.elements.abilities.impl.frost.passives;

import net.saturn.elementSmp.elements.ElementType;
import net.saturn.elementSmp.managers.ElementManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Handles Frost element passive 2: Freeze water while walking (only while shifting)
 */
public class FrostWaterFreezePassive implements Listener {
    private final ElementManager elementManager;

    public FrostWaterFreezePassive(ElementManager elementManager) {
        this.elementManager = elementManager;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        
        // Check if player has Frost element
        var pd = elementManager.data(player.getUniqueId());
        if (pd == null || pd.getCurrentElement() != ElementType.FROST) return;

        // Check if upgrade level 2
        if (pd.getUpgradeLevel(ElementType.FROST) < 2) return;

        // Must be shifting to freeze water (User request: "you have to be shifting to freeze water")
        if (!player.isSneaking()) return;

        // Find the block to freeze around
        Block baseBlock = null;
        
        // Search from player's current block down to 4 blocks below
        // This handles:
        // 1. Swimming: Starts at i=0 (player's block), so ice is "on top" of the water below
        // 2. Air: Searches down up to 4 blocks to find water and freeze it before landing
        for (int i = 0; i <= 4; i++) {
            Block b = player.getLocation().getBlock().getRelative(BlockFace.DOWN, i);
            Material type = b.getType();
            
            if (type == Material.WATER) {
                // Check if it's surface water (block above is air or already frozen ice)
                Block above = b.getRelative(BlockFace.UP);
                if (above.getType() == Material.AIR || above.getType() == Material.FROSTED_ICE || above.getType() == Material.ICE) {
                    baseBlock = b;
                    break;
                }
            } else if (type != Material.AIR && type != Material.FROSTED_ICE && type != Material.ICE) {
                // Hit solid ground that isn't water or ice, stop searching
                break;
            }
        }

        if (baseBlock == null) return;

        // Freeze water in a small radius around the found baseBlock
        int radius = 2;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                // Circle-ish shape
                if (x * x + z * z > 6) continue;
                
                Block b = baseBlock.getRelative(x, 0, z);
                if (b.getType() == Material.WATER) {
                    // Only freeze surface water in the radius too
                    Block above = b.getRelative(BlockFace.UP);
                    if (above.getType() == Material.AIR || above.getType() == Material.FROSTED_ICE || above.getType() == Material.ICE) {
                        // Use FROSTED_ICE so it melts naturally like Frost Walker
                        b.setType(Material.FROSTED_ICE);
                    }
                }
            }
        }
    }
}
