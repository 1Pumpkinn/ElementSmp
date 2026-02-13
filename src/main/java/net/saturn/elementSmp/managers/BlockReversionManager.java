package net.saturn.elementSmp.managers;

import net.saturn.elementSmp.ElementSmp;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;

import java.util.HashMap;
import java.util.Map;

public class BlockReversionManager {
    private final ElementSmp plugin;
    private final Map<Location, BlockData> originalBlocks = new HashMap<>();

    public BlockReversionManager(ElementSmp plugin) {
        this.plugin = plugin;
    }

    public void addBlock(Location location, BlockData originalBlockData) {
        originalBlocks.putIfAbsent(location, originalBlockData);
    }

    public void revertBlock(Location location) {
        BlockData originalBlockData = originalBlocks.remove(location);
        if (originalBlockData != null) {
            location.getBlock().setBlockData(originalBlockData, false);
        }
    }

    public void revertAll() {
        for (Map.Entry<Location, BlockData> entry : new HashMap<>(originalBlocks).entrySet()) {
            revertBlock(entry.getKey());
        }
    }
}