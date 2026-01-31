package net.saturn.elementSmp.elements.abilities.impl.death.passives;

import io.papermc.paper.event.entity.EntityEquipmentChangedEvent;
import net.saturn.elementSmp.ElementSmp;
import net.saturn.elementSmp.elements.ElementType;
import net.saturn.elementSmp.managers.ElementManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class DeathInvisibilityPassive implements Listener {

    private static DeathInvisibilityPassive instance;
    private final ElementSmp plugin;
    private final ElementManager elementManager;
    private final Set<UUID> hiddenPlayers = new HashSet<>();

    public DeathInvisibilityPassive(ElementSmp plugin, ElementManager elementManager) {
        this.plugin = plugin;
        this.elementManager = elementManager;
        instance = this;
        startPeriodicCheck();
    }

    private void startPeriodicCheck() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (UUID uuid : new HashSet<>(hiddenPlayers)) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null && player.isOnline()) {
                    checkHealth(player);
                } else {
                    hiddenPlayers.remove(uuid);
                }
            }
        }, 40L, 40L); // Every 2 seconds
    }

    public static void clearState(Player player) {
        if (instance != null) {
            instance.stopHiding(player);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        stopHiding(event.getEntity());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        checkHealth(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (elementManager.getPlayerElement(player) != ElementType.DEATH) return;

        checkHealth(player);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onRegen(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (elementManager.getPlayerElement(player) != ElementType.DEATH) return;

        checkHealth(player);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTotemPop(EntityResurrectEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (elementManager.getPlayerElement(player) != ElementType.DEATH) return;

        checkHealth(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onArmorChange(com.destroystokyo.paper.event.player.PlayerArmorChangeEvent event) {
        Player player = event.getPlayer();
        if (hiddenPlayers.contains(player.getUniqueId())) {
            Bukkit.getScheduler().runTask(plugin, () -> hideEquipment(player));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPotionEffectEnd(EntityPotionEffectEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (event.getModifiedType() != PotionEffectType.INVISIBILITY) return;
        if (event.getAction() != EntityPotionEffectEvent.Action.REMOVED && 
            event.getAction() != EntityPotionEffectEvent.Action.CLEARED) return;

        if (hiddenPlayers.contains(player.getUniqueId())) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (!player.isOnline()) return;
                
                // Check if they still qualify before reapplying
                var pd = elementManager.data(player.getUniqueId());
                if (pd == null || pd.getCurrentElement() != ElementType.DEATH || pd.getUpgradeLevel(ElementType.DEATH) < 2 || player.getHealth() > 4.0) {
                    stopHiding(player);
                    return;
                }

                if (hiddenPlayers.contains(player.getUniqueId())) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, PotionEffect.INFINITE_DURATION, 0, false, false, true));
                }
            });
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player joining = event.getPlayer();
        for (UUID hiddenId : hiddenPlayers) {
            Player hidden = Bukkit.getPlayer(hiddenId);
            if (hidden != null && hidden.isOnline()) {
                hideEquipment(hidden, joining);
            }
        }
        
        if (elementManager.getPlayerElement(joining) == ElementType.DEATH) {
            checkHealth(joining);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        hiddenPlayers.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        if (hiddenPlayers.contains(player.getUniqueId())) {
            Bukkit.getScheduler().runTask(plugin, () -> hideEquipment(player));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (hiddenPlayers.contains(player.getUniqueId())) {
            Bukkit.getScheduler().runTask(plugin, () -> hideEquipment(player));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEquipmentChange(EntityEquipmentChangedEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (hiddenPlayers.contains(player.getUniqueId())) {
            Bukkit.getScheduler().runTask(plugin, () -> hideEquipment(player));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSwapHandItems(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        if (hiddenPlayers.contains(player.getUniqueId())) {
            Bukkit.getScheduler().runTask(plugin, () -> hideEquipment(player));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPickupItem(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (hiddenPlayers.contains(player.getUniqueId())) {
            Bukkit.getScheduler().runTask(plugin, () -> hideEquipment(player));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (hiddenPlayers.contains(player.getUniqueId())) {
            Bukkit.getScheduler().runTask(plugin, () -> hideEquipment(player));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        if (hiddenPlayers.contains(player.getUniqueId())) {
            Bukkit.getScheduler().runTask(plugin, () -> hideEquipment(player));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (hiddenPlayers.contains(player.getUniqueId())) {
            Bukkit.getScheduler().runTask(plugin, () -> hideEquipment(player));
        }
    }

    private void checkHealth(Player player) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!player.isOnline()) return;
            
            var pd = elementManager.data(player.getUniqueId());
            if (pd == null || pd.getCurrentElement() != ElementType.DEATH || pd.getUpgradeLevel(ElementType.DEATH) < 2) {
                if (hiddenPlayers.contains(player.getUniqueId())) {
                    stopHiding(player);
                }
                return;
            }

            double health = player.getHealth();
            if (health <= 4.0) { // 2 hearts
                if (!hiddenPlayers.contains(player.getUniqueId())) {
                    startHiding(player);
                }
            } else {
                if (hiddenPlayers.contains(player.getUniqueId())) {
                    stopHiding(player);
                }
            }
        });
    }

    private void startHiding(Player player) {
        hiddenPlayers.add(player.getUniqueId());
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, PotionEffect.INFINITE_DURATION, 0, false, false, true));
        
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 0.5f);
        player.getWorld().spawnParticle(
                Particle.SMOKE,
                player.getLocation().add(0, 1, 0),
                30, 0.5, 0.5, 0.5, 0.1
        );

        hideEquipment(player);
    }

    private void stopHiding(Player player) {
        hiddenPlayers.remove(player.getUniqueId());
        player.removePotionEffect(PotionEffectType.INVISIBILITY);
        showEquipment(player);
    }

    private void hideEquipment(Player player) {
        for (Player other : Bukkit.getOnlinePlayers()) {
            hideEquipment(player, other);
        }
    }

    private void hideEquipment(Player player, Player observer) {
        Map<EquipmentSlot, ItemStack> equipmentMap = new EnumMap<>(EquipmentSlot.class);
        ItemStack air = new ItemStack(Material.AIR);
        
        equipmentMap.put(EquipmentSlot.HEAD, air);
        equipmentMap.put(EquipmentSlot.CHEST, air);
        equipmentMap.put(EquipmentSlot.LEGS, air);
        equipmentMap.put(EquipmentSlot.FEET, air);
        equipmentMap.put(EquipmentSlot.HAND, air);
        equipmentMap.put(EquipmentSlot.OFF_HAND, air);

        if (!observer.equals(player)) {
            observer.sendEquipmentChange(player, equipmentMap);
        }
    }

    private void showEquipment(Player player) {
        for (Player other : Bukkit.getOnlinePlayers()) {
            showEquipment(player, other);
        }
    }

    private void showEquipment(Player player, Player observer) {
        if (observer.equals(player)) return;
        
        Map<EquipmentSlot, ItemStack> equipmentMap = new EnumMap<>(EquipmentSlot.class);
        var inv = player.getEquipment();
        equipmentMap.put(EquipmentSlot.HEAD, inv.getHelmet());
        equipmentMap.put(EquipmentSlot.CHEST, inv.getChestplate());
        equipmentMap.put(EquipmentSlot.LEGS, inv.getLeggings());
        equipmentMap.put(EquipmentSlot.FEET, inv.getBoots());
        equipmentMap.put(EquipmentSlot.HAND, inv.getItemInMainHand());
        equipmentMap.put(EquipmentSlot.OFF_HAND, inv.getItemInOffHand());

        observer.sendEquipmentChange(player, equipmentMap);
    }
}
