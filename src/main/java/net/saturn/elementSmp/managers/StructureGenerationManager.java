package net.saturn.elementsmp.managers;

import net.saturn.elementsmp.ElementSmp;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.structure.Structure;
import org.bukkit.structure.StructureManager;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.block.structure.Mirror;

import java.io.InputStream;
import java.util.Random;
import java.util.logging.Level;

public class StructureGenerationManager {
    private final ElementSmp plugin;
    private final Random random = new Random();

    public StructureGenerationManager(ElementSmp plugin) {
        this.plugin = plugin;
    }

    public void attemptGeneration() {
        if (plugin.getDataStore().isAltarGenerated()) {
            return;
        }

        // Delay generation to ensure worlds are fully loaded
        Bukkit.getScheduler().runTaskLater(plugin, this::generateAltar, 100L);
    }

    private void generateAltar() {
        World world = Bukkit.getWorld("world");
        if (world == null) {
            plugin.getLogger().warning("Could not find overworld for altar generation!");
            return;
        }

        if (plugin.getDataStore().isAltarGenerated()) return;

        // Pick ONE random coordinate
        int startX = random.nextInt(4000) - 2000;
        int startZ = random.nextInt(4000) - 2000;
        
        // Stay away from spawn
        if (Math.abs(startX) < 500 && Math.abs(startZ) < 500) {
            Bukkit.getScheduler().runTaskLater(plugin, this::generateAltar, 20L);
            return;
        }

        // Load chunk asynchronously
        world.getChunkAtAsync(startX >> 4, startZ >> 4).thenAccept(chunk -> {
            // Now we are in a callback, potentially off-thread or on-thread depending on Paper version
            // Ensure we check suitability on the main thread for block access
            Bukkit.getScheduler().runTask(plugin, () -> {
                Location loc = checkLocation(world, startX, startZ);
                if (loc != null) {
                    if (placeStructure(loc)) {
                        plugin.getDataStore().setAltarGenerated(true);
                        plugin.getLogger().info("Lightning Altar generated successfully at " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
                        findAndInitializeAltar(loc);
                    } else {
                        plugin.getLogger().severe("Failed to place Lightning Altar structure!");
                        // Try again in 1 minute if placement failed for some reason
                        Bukkit.getScheduler().runTaskLater(plugin, this::generateAltar, 1200L);
                    }
                } else {
                    // Not a good spot, try another one in 1 second (to avoid spamming)
                    Bukkit.getScheduler().runTaskLater(plugin, this::generateAltar, 20L);
                }
            });
        });
    }

    private Location checkLocation(World world, int startX, int startZ) {
        int minHeight = Integer.MAX_VALUE;
        int maxHeight = Integer.MIN_VALUE;
        boolean invalid = false;

        // Check 4 corners and center
        int[][] points = {{0,0}, {11,0}, {0,10}, {11,10}, {5,5}};
        for (int[] p : points) {
            Block b = world.getHighestBlockAt(startX + p[0], startZ + p[1]);
            Material type = b.getType();
            // Expanded invalid materials list (removed SAND and RED_SAND to allow placement in those biomes)
            if (type == Material.WATER || type == Material.LAVA || type == Material.SNOW || 
                type == Material.ICE || type == Material.SNOW_BLOCK || type == Material.BLUE_ICE ||
                type == Material.PACKED_ICE) {
                invalid = true;
                break;
            }
            int h = b.getY();
            if (h < minHeight) minHeight = h;
            if (h > maxHeight) maxHeight = h;
        }

        if (invalid) return null;

        // Relax flatness criteria to 2 blocks
        if (maxHeight - minHeight > 2) return null;

        return new Location(world, startX, maxHeight, startZ);
    }

    // findSuitableLocation is no longer used, we'll remove it in the next step or keep it as a private helper if needed.
    // For now, I've replaced its functionality with checkLocation.

    private boolean placeStructure(Location loc) {
        StructureManager manager = Bukkit.getStructureManager();
        
        try (InputStream in = plugin.getResource("structure/lightning_altar.nbt")) {
            if (in == null) {
                plugin.getLogger().severe("Could not find lightning_altar.nbt in resources!");
                return false;
            }
            
            // Clear volume before placement (12x6x11)
            clearVolume(loc, 12, 6, 11);

            Structure structure = manager.loadStructure(in);
            structure.place(loc, false, StructureRotation.NONE, Mirror.NONE, 0, 1.0f, random);
            
            // Grounding pass: fill gaps below with natural materials
            fillGapsBelow(loc);
            
            return true;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load/place structure", e);
            return false;
        }
    }

    private void clearVolume(Location loc, int width, int height, int depth) {
        World world = loc.getWorld();
        int startX = loc.getBlockX();
        int startY = loc.getBlockY();
        int startZ = loc.getBlockZ();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    world.getBlockAt(startX + x, startY + y, startZ + z).setType(Material.AIR);
                }
            }
        }
    }

    private void fillGapsBelow(Location loc) {
        World world = loc.getWorld();
        int startX = loc.getBlockX();
        int startY = loc.getBlockY();
        int startZ = loc.getBlockZ();

        for (int x = 0; x < 12; x++) {
            for (int z = 0; z < 11; z++) {
                Location columnLoc = new Location(world, startX + x, startY - 1, startZ + z);
                Biome biome = columnLoc.getBlock().getBiome();
                
                Material surfaceMat = getSurfaceMaterialForBiome(biome);
                Material fillMat = getFillMaterialForBiome(biome);

                // Check blocks below the structure base
                for (int y = 1; y <= 3; y++) {
                    Block b = world.getBlockAt(startX + x, startY - y, startZ + z);
                    Material type = b.getType();
                    if (type.isAir() || isFlora(type)) {
                        // Top layer uses surface block, below uses fill block
                        if (y == 1) {
                            b.setType(surfaceMat);
                        } else {
                            b.setType(fillMat);
                        }
                    } else {
                        // Found solid ground, stop going down for this (x,z) column
                        break;
                    }
                }
            }
        }
    }

    private boolean isFlora(Material type) {
        return type == Material.SHORT_GRASS || type == Material.TALL_GRASS || 
               type == Material.SNOW || type == Material.DEAD_BUSH || 
               type == Material.FERN || type == Material.LARGE_FERN;
    }

    private Material getSurfaceMaterialForBiome(Biome biome) {
        String name = biome.name();
        if (name.contains("DESERT")) return Material.SAND;
        if (name.contains("BADLANDS")) return Material.RED_SAND;
        if (name.contains("SNOWY")) return Material.SNOW_BLOCK;
        if (name.contains("OLD_GROWTH")) return Material.PODZOL;
        if (name.contains("SAVANNA")) return Material.GRASS_BLOCK; // Savanna still uses grass
        return Material.GRASS_BLOCK;
    }

    private Material getFillMaterialForBiome(Biome biome) {
        String name = biome.name();
        if (name.contains("DESERT")) return Material.SANDSTONE;
        if (name.contains("BADLANDS")) return Material.TERRACOTTA;
        if (name.contains("SNOWY")) return Material.SNOW_BLOCK;
        return Material.DIRT;
    }

    private void findAndInitializeAltar(Location origin) {
        // The structure size is 12x6x11. We search for the Lodestone.
        World world = origin.getWorld();
        for (int x = 0; x < 12; x++) {
            for (int y = 0; y < 6; y++) {
                for (int z = 0; z < 11; z++) {
                    Location current = origin.clone().add(x, y, z);
                    if (current.getBlock().getType() == Material.LODESTONE) {
                        plugin.getAltarManager().placeAltar(current, "lightning_element");
                        return;
                    }
                }
            }
        }
        plugin.getLogger().warning("Altar structure placed but LODESTONE not found! Manual initialization required.");
    }
}
