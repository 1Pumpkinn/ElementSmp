package net.saturn.elementSmp.altar;

import net.saturn.elementSmp.ElementSmp;
import net.saturn.elementSmp.items.altar.LightningElementItem;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class AltarManager implements Listener {
    private final ElementSmp plugin;
    private final Map<String, AltarRecipe> recipes = new HashMap<>();
    private final Map<String, AltarState> activeAltars = new HashMap<>();
    private int ticksElapsed = 0;

    public AltarManager(ElementSmp plugin) {
        this.plugin = plugin;
        registerDefaultRecipes();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        startSpinningTask();
    }

    private void startSpinningTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                // Rotation and floating for all ItemDisplays tagged as 'altar_display'
                for (org.bukkit.World world : plugin.getServer().getWorlds()) {
                    for (ItemDisplay display : world.getEntitiesByClass(ItemDisplay.class)) {
                        if (display.getScoreboardTags().contains("altar_display")) {
                            // Rotation
                            display.setRotation(display.getLocation().getYaw() + 5.0f, 0);
                            
                            // Floating bobbing effect
                            double offset = Math.sin(ticksElapsed * 0.1) * 0.1;
                            display.setTransformation(new org.bukkit.util.Transformation(
                                    new org.joml.Vector3f(0, (float) offset, 0),
                                    new org.joml.Quaternionf(),
                                    new org.joml.Vector3f(0.6f, 0.6f, 0.6f),
                                    new org.joml.Quaternionf()
                            ));
                        }
                    }
                }
                
                // Particles for active altars (every 3 ticks)
                if (ticksElapsed % 3 == 0) {
                    for (AltarState state : activeAltars.values()) {
                        Location loc = state.getLocation();
                        loc.getWorld().spawnParticle(org.bukkit.Particle.ELECTRIC_SPARK, loc.clone().add(0.5, 1.7, 0.5), 1, 0.1, 0.1, 0.1, 0.05);
                        loc.getWorld().spawnParticle(org.bukkit.Particle.GLOW, loc.clone().add(0.5, 1.7, 0.5), 1, 0.2, 0.2, 0.2, 0.02);
                    }
                }

                // Keep holograms updated and visible
                for (AltarState state : activeAltars.values()) {
                    if (!state.isProcessing()) {
                        state.updateHolograms();
                    }
                }

                ticksElapsed++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private String toKey(Location loc) {
        return loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
    }

    public AltarRecipe getRecipe(String name) {
        return recipes.get(name.toLowerCase());
    }

    public Set<String> getRecipeNames() {
        return recipes.keySet();
    }

    public boolean placeAltar(Location loc, String recipeName) {
        AltarRecipe recipe = recipes.get(recipeName.toLowerCase());
        if (recipe == null) return false;

        loc.getBlock().setType(Material.LODESTONE);
        
        // CLEAN UP ANY EXISTING ALTAR ENTITIES FIRST
        for (Entity entity : loc.getWorld().getNearbyEntities(loc.clone().add(0.5, 1.5, 0.5), 1.0, 2.5, 1.0)) {
            if (entity.getScoreboardTags().contains("altar_display") || entity.getScoreboardTags().contains("altar_hologram")) {
                entity.remove();
            }
        }
        
        Location displayLoc = loc.clone().add(0.5, 1.6, 0.5);
        displayLoc.getWorld().spawn(displayLoc, ItemDisplay.class, display -> {
            display.setItemStack(recipe.result());
            display.addScoreboardTag("altar_display");
            display.addScoreboardTag("altar_recipe_" + recipeName.toLowerCase());
            display.setDisplayHeight(0.5f);
            display.setDisplayWidth(0.5f);
            // No billboard - so it doesn't move with player
        });
        
        // Initialize global state for this altar
        activeAltars.put(toKey(loc), new AltarState(loc, recipe));
        
        return true;
    }

    private void registerDefaultRecipes() {
        Map<Material, Integer> lightningIngredients = new HashMap<>();
        lightningIngredients.put(Material.REDSTONE_TORCH, 4);
        lightningIngredients.put(Material.TNT, 1);
        lightningIngredients.put(Material.REDSTONE_LAMP, 2);
        lightningIngredients.put(Material.LIGHTNING_ROD, 1);
        lightningIngredients.put(Material.COPPER_BLOCK, 4);

        recipes.put("lightning_element", new AltarRecipe("LIGHTNING ELEMENT", lightningIngredients, LightningElementItem.make(plugin)));
    }

    @EventHandler
    public void onAltarInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null || event.getClickedBlock().getType() != Material.LODESTONE) return;

        Player player = event.getPlayer();
        Location loc = event.getClickedBlock().getLocation();
        String key = toKey(loc);

        // Check if Lightning is already unlocked globally
        if (plugin.getDataStore().isElementUnlocked(net.saturn.elementSmp.elements.ElementType.LIGHTNING)) {
            player.sendMessage(ChatColor.YELLOW + "The Lightning Element has already been unlocked globally! You can now obtain it through the Advanced Reroller.");
            return;
        }

        AltarState state = activeAltars.get(key);
        if (state == null) {
            // Check if there is an ItemDisplay nearby that indicates which recipe this should be
            // This allows persistent altars across restarts if the blocks remain
            boolean found = false;
            for (Entity entity : loc.getWorld().getNearbyEntities(loc.clone().add(0.5, 1.2, 0.5), 0.5, 0.5, 0.5)) {
                if (entity instanceof ItemDisplay display) {
                    for (String tag : display.getScoreboardTags()) {
                        if (tag.startsWith("altar_recipe_")) {
                            String recipeName = tag.substring("altar_recipe_".length());
                            AltarRecipe recipe = recipes.get(recipeName);
                            if (recipe != null) {
                                state = new AltarState(loc, recipe);
                                activeAltars.put(key, state);
                                found = true;
                                break;
                            }
                        }
                    }
                    if (found) break; // Break outer entity loop
                }
            }
        }

        if (state == null) return;

        event.setCancelled(true);
        
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand.getType() != Material.AIR && state.needsIngredient(hand.getType())) {
            // Deposit ingredient
            int needed = state.getNeeded(hand.getType());
            int amount = Math.min(hand.getAmount(), needed);
            
            state.addIngredient(hand.getType(), amount);
            hand.setAmount(hand.getAmount() - amount);
            
            player.getWorld().playSound(loc, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1.0f, 1.5f);
            state.updateHolograms();

            if (state.isComplete()) {
                completeCraft(state);
                activeAltars.remove(key);
            }
        } else {
            // Just play a sound if not holding an ingredient
            player.getWorld().playSound(loc, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 1.0f);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() != Material.LODESTONE) return;
        
        Location loc = event.getBlock().getLocation();
        boolean isAltar = false;
        
        // Find if there's an altar display at this location
        for (Entity entity : loc.getWorld().getNearbyEntities(loc.clone().add(0.5, 1.2, 0.5), 0.5, 0.5, 0.5)) {
            if (entity instanceof ItemDisplay display && display.getScoreboardTags().contains("altar_display")) {
                isAltar = true;
                
                if (event.getPlayer().getGameMode() == org.bukkit.GameMode.CREATIVE) {
                    display.remove();
                    AltarState state = activeAltars.remove(toKey(loc));
                    if (state != null) {
                        state.clearHolograms();
                    }
                    
                    // Also clear any lingering holograms by tag just in case
                    for (Entity nearby : loc.getWorld().getNearbyEntities(loc.clone().add(0.5, 2.5, 0.5), 1.0, 3.0, 1.0)) {
                        if (nearby instanceof ArmorStand as && as.getScoreboardTags().contains("altar_hologram")) {
                            as.remove();
                        }
                    }
                    
                    event.getPlayer().sendMessage(ChatColor.YELLOW + "Altar and associated text removed.");
                } else {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(ChatColor.RED + "This altar is ancient and cannot be destroyed in survival!");
                }
                break;
            }
        }
    }

    private void completeCraft(AltarState state) {
        if (state.isProcessing()) return;
        
        Location loc = state.getLocation().clone().add(0.5, 1.5, 0.5);
        state.setProcessing(true);
        state.clearHolograms();
        
        // Also clear any lingering holograms by tag just in case
        for (Entity nearby : state.getLocation().getWorld().getNearbyEntities(state.getLocation().add(0.5, 1.5, 0.5), 2.0, 5.0, 2.0)) {
            if (nearby instanceof ArmorStand as && as.getScoreboardTags().contains("altar_hologram")) {
                as.remove();
            }
        }

        // Unlock Lightning globally
        if (state.getRecipe().name().equalsIgnoreCase("LIGHTNING ELEMENT")) {
            plugin.getDataStore().setElementUnlocked(net.saturn.elementSmp.elements.ElementType.LIGHTNING, true);
            plugin.getServer().broadcastMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "The Lightning Element has been globally unlocked!");
        }

        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks >= 40) {
                    loc.getWorld().strikeLightningEffect(loc);
                    
                    // Drop the soul item
                    loc.getWorld().dropItemNaturally(loc, state.getRecipe().result());
                    
                    loc.getWorld().playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                    
                    // Remove the ItemDisplay
                    for (Entity entity : loc.getWorld().getNearbyEntities(state.getLocation().clone().add(0.5, 1.4, 0.5), 0.5, 0.5, 0.5)) {
                        if (entity instanceof ItemDisplay display && display.getScoreboardTags().contains("altar_display")) {
                            display.remove();
                        }
                    }

                    // Final cleanup of holograms
                    state.clearHolograms();
                    for (Entity nearby : state.getLocation().getWorld().getNearbyEntities(state.getLocation().clone().add(0.5, 1.5, 0.5), 2.0, 5.0, 2.0)) {
                        if (nearby instanceof ArmorStand as && as.getScoreboardTags().contains("altar_hologram")) {
                            as.remove();
                        }
                    }

                    // BREAK THE ALTAR BLOCK
                    state.getLocation().getBlock().setType(Material.AIR);
                    state.getLocation().getWorld().playSound(state.getLocation(), Sound.BLOCK_ANVIL_BREAK, 1.0f, 1.0f);
                    state.getLocation().getWorld().spawnParticle(org.bukkit.Particle.BLOCK, state.getLocation().clone().add(0.5, 0.5, 0.5), 50, 0.3, 0.3, 0.3, Material.LODESTONE.createBlockData());
                    
                    cancel();
                    return;
                }

                loc.getWorld().spawnParticle(org.bukkit.Particle.ELECTRIC_SPARK, loc, 5, 0.5, 0.5, 0.5, 0.1);
                if (ticks % 5 == 0) {
                    loc.getWorld().playSound(loc, Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 0.5f + (ticks / 40.0f));
                }
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private class AltarState {
        private final Location location;
        private final AltarRecipe recipe;
        private final Map<Material, Integer> deposited = new HashMap<>();
        private final List<ArmorStand> holograms = new ArrayList<>();
        private boolean isProcessing = false;

        public AltarState(Location location, AltarRecipe recipe) {
            this.location = location;
            this.recipe = recipe;
        }

        public void setProcessing(boolean processing) {
            this.isProcessing = processing;
        }

        public boolean isProcessing() {
            return isProcessing;
        }

        public boolean needsIngredient(Material mat) {
            return recipe.ingredients().containsKey(mat) && getNeeded(mat) > 0;
        }

        public int getNeeded(Material mat) {
            return recipe.ingredients().getOrDefault(mat, 0) - deposited.getOrDefault(mat, 0);
        }

        public void addIngredient(Material mat, int amount) {
            deposited.put(mat, deposited.getOrDefault(mat, 0) + amount);
        }

        public boolean isComplete() {
            for (Map.Entry<Material, Integer> entry : recipe.ingredients().entrySet()) {
                if (deposited.getOrDefault(entry.getKey(), 0) < entry.getValue()) {
                    return false;
                }
            }
            return true;
        }

        public void updateHolograms() {
            if (isProcessing) return;

            // Only update if someone is nearby
            boolean someoneNearby = false;
            for (Player p : location.getWorld().getPlayers()) {
                if (p.getLocation().distanceSquared(location) < 100) { // 10 blocks
                    someoneNearby = true;
                    break;
                }
            }
            
            if (!someoneNearby) {
                clearHolograms();
                return;
            }

            // If holograms already exist, just update names instead of respawning (less flickering)
            if (!holograms.isEmpty()) {
                // Periodically check if all holograms are still valid
                if (holograms.stream().allMatch(Entity::isValid)) {
                    updateHologramTexts();
                    return;
                } else {
                    // One or more holograms were removed externally, clear and recreate
                    clearHolograms();
                }
            }

            // CLEAR LINGERING HOLOGRAMS FROM PREVIOUS SESSIONS
            // This is the most likely cause for overlaps (entities from before restart)
            for (Entity nearby : location.getWorld().getNearbyEntities(location.clone().add(0.5, 2.5, 0.5), 1.0, 3.0, 1.0)) {
                if (nearby instanceof ArmorStand as && as.getScoreboardTags().contains("altar_hologram")) {
                    as.remove();
                }
            }

            Location spawnLoc = location.clone().add(0.5, 1.6, 0.5);
            double spacing = 0.23;
            
            // Result name
            createHologram(spawnLoc.clone().add(0, spacing * (recipe.ingredients().size() + 1), 0), 
                    ChatColor.BOLD + recipe.name());

            int i = 0;
            // Use a LinkedHashMap or similar if order matters, but here we just need consistency.
            // HashMap order is stable after population, and recipes are static.
            for (Map.Entry<Material, Integer> entry : recipe.ingredients().entrySet()) {
                String line = getIngredientLine(entry.getKey(), entry.getValue());
                createHologram(spawnLoc.clone().add(0, spacing * (recipe.ingredients().size() - i), 0), line);
                i++;
            }
        }

        private String getIngredientLine(Material mat, int total) {
            int needed = total - deposited.getOrDefault(mat, 0);
            boolean done = needed <= 0;
            return (done ? ChatColor.STRIKETHROUGH.toString() + ChatColor.GRAY : ChatColor.WHITE.toString()) 
                    + total + "x " + formatName(mat.name());
        }

        private void updateHologramTexts() {
            // First one is result name
            if (holograms.size() > 0) {
                holograms.get(0).setCustomName(ChatColor.BOLD + recipe.name());
            }
            
            int i = 1;
            for (Map.Entry<Material, Integer> entry : recipe.ingredients().entrySet()) {
                if (i < holograms.size()) {
                    holograms.get(i).setCustomName(getIngredientLine(entry.getKey(), entry.getValue()));
                }
                i++;
            }
        }

        private void createHologram(Location loc, String text) {
            ArmorStand as = loc.getWorld().spawn(loc, ArmorStand.class, stand -> {
                stand.setVisible(false);
                stand.setGravity(false);
                stand.setCustomName(text);
                stand.setCustomNameVisible(true);
                stand.setMarker(true);
                stand.setSmall(true);
                stand.addScoreboardTag("altar_hologram");
            });
            holograms.add(as);
        }

        public void clearHolograms() {
            holograms.forEach(Entity::remove);
            holograms.clear();
        }

        private String formatName(String name) {
            String[] split = name.toLowerCase().split("_");
            StringBuilder sb = new StringBuilder();
            for (String s : split) {
                sb.append(Character.toUpperCase(s.charAt(0))).append(s.substring(1)).append(" ");
            }
            return sb.toString().trim();
        }

        public Location getLocation() { return location; }
        public AltarRecipe getRecipe() { return recipe; }
    }
}
