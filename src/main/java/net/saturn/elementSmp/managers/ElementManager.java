package net.saturn.elementsmp.managers;

import net.saturn.elementsmp.ElementSmp;
import net.saturn.elementsmp.data.DataStore;
import net.saturn.elementsmp.data.PlayerData;
import net.saturn.elementsmp.elements.core.*;
import net.saturn.elementsmp.elements.impl.air.AirElement;
import net.saturn.elementsmp.elements.impl.death.DeathElement;
import net.saturn.elementsmp.elements.impl.earth.EarthElement;
import net.saturn.elementsmp.elements.impl.fire.FireElement;
import net.saturn.elementsmp.elements.impl.frost.FrostElement;
import net.saturn.elementsmp.elements.impl.life.LifeElement;
import net.saturn.elementsmp.elements.impl.lightning.AltarLightningElement;
import net.saturn.elementsmp.elements.impl.lightning.LightningElement;
import net.saturn.elementsmp.elements.impl.metal.MetalElement;
import net.saturn.elementsmp.elements.impl.water.WaterElement;
import net.saturn.elementsmp.gui.ElementSelectionGUI;
import net.saturn.elementsmp.items.altar.AltarItem;
import net.saturn.elementsmp.items.util.AdvancedRerollerItem;
import net.saturn.elementsmp.items.ItemKeys;
import net.saturn.elementsmp.items.util.RerollerItem;
import net.saturn.elementsmp.services.EffectService;
import net.saturn.elementsmp.util.scheduling.TaskScheduler;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class ElementManager {
    private final ElementSmp plugin;
    private final DataStore store;
    private final ManaManager manaManager;
    private final TrustManager trustManager;
    private final ConfigManager configManager;
    private final EffectService effectService;
    private final TaskScheduler scheduler;
    private final Map<ElementType, Element> registry = new EnumMap<>(ElementType.class);
    private final Element altarLightning;
    private final Random random = new Random();

    public ElementManager(ElementSmp plugin, DataStore store, ManaManager manaManager,
                          TrustManager trustManager, ConfigManager configManager) {
        this.plugin = plugin;
        this.store = store;
        this.manaManager = manaManager;
        this.trustManager = trustManager;
        this.configManager = configManager;
        this.effectService = new EffectService(plugin, this);
        this.scheduler = new TaskScheduler(plugin);
        this.altarLightning = new AltarLightningElement(plugin);
        
        initializeRegistry();
    }

    private void initializeRegistry() {
        register(ElementType.AIR, new AirElement(plugin));
        register(ElementType.WATER, new WaterElement(plugin));
        register(ElementType.FIRE, new FireElement(plugin));
        register(ElementType.EARTH, new EarthElement(plugin));
        register(ElementType.LIFE, new LifeElement(plugin));
        register(ElementType.DEATH, new DeathElement(plugin));
        register(ElementType.METAL, new MetalElement(plugin));
        register(ElementType.FROST, new FrostElement(plugin));
        register(ElementType.LIGHTNING, new LightningElement(plugin));
    }

    private void register(ElementType type, Element element) {
        registry.put(type, element);
    }

    public ElementSmp getPlugin() { return plugin; }
    public EffectService getEffectService() { return effectService; }

    public PlayerData data(UUID uuid) {
        return store.getPlayerData(uuid);
    }

    public Element get(Player player, ElementType type) {
        PlayerData pd = data(player.getUniqueId());
        if (type == ElementType.LIGHTNING && pd.isAltarElement()) {
            return altarLightning;
        }
        return registry.get(type);
    }

    public Element get(ElementType type) {
        return registry.get(type);
    }

    public ElementType getPlayerElement(Player player) {
        return data(player.getUniqueId()).getCurrentElement();
    }

    public boolean isCurrentlyRolling(Player player) {
        return ElementSelectionGUI.getGUI(player.getUniqueId()) != null;
    }

    public void cancelRolling(Player player) {
        ElementSelectionGUI gui = ElementSelectionGUI.getGUI(player.getUniqueId());
        if (gui != null && !gui.isFinished()) {
            String rerollerType = gui.getRerollerType();
            if (rerollerType != null) {
                ItemStack refund = null;
                if (rerollerType.equals(ItemKeys.KEY_REROLLER)) {
                    refund = RerollerItem.make(plugin);
                } else if (rerollerType.equals(ItemKeys.KEY_ADVANCED_REROLLER)) {
                    refund = AdvancedRerollerItem.make(plugin);
                }

                if (refund != null) {
                    player.getInventory().addItem(refund).values().forEach(item -> 
                        player.getWorld().dropItemNaturally(player.getLocation(), item));
                    player.sendMessage(ChatColor.YELLOW + "Your reroller has been refunded because you left while rolling!");
                }
            }
        }
        ElementSelectionGUI.removeGUI(player.getUniqueId());
    }

    public void setElement(Player player, ElementType type) {
        assignElementInternal(player, type, "Element Changed!");
    }

    public void assignElement(Player player, ElementType type) {
        assignElementInternal(player, type, "Element Assigned!");
    }

    private void assignElementInternal(Player player, ElementType type, String titleText) {
        assignElementInternal(player, type, titleText, false);
    }

    private void assignElementInternal(Player player, ElementType type, String titleText, boolean resetLevel) {
        if (type != null && !store.isElementEnabled(type)) {
            player.sendMessage(org.bukkit.ChatColor.RED + "This element is disabled by the server.");
            return;
        }
        PlayerData pd = data(player.getUniqueId());
        ElementType old = pd.getCurrentElement();

        if (old != null && old != type) {
            handleElementSwitch(player, old);
            // Reset altar element status on switch
            pd.setAltarElement(false);
        }

        if (resetLevel) {
            pd.setCurrentElement(type);
        } else {
            int currentUpgrade = pd.getCurrentElementUpgradeLevel();
            pd.setCurrentElementWithoutReset(type);
            pd.setCurrentElementUpgradeLevel(currentUpgrade);
        }

        store.save(pd);
        showElementTitle(player, type, titleText);
        applyUpsides(player);
    }

    private void handleElementSwitch(Player player, ElementType oldElement) {
        effectService.clearAllElementEffects(player);
        
        PlayerData pd = data(player.getUniqueId());
        // Only give back the item if it was an altar element
        if (oldElement != null && pd.isAltarElement()) {
            ItemStack item = AltarItem.soulFor(oldElement, plugin);
            // Try adding to inventory first; any items that don't fit are returned in the map and then dropped
            Map<Integer, ItemStack> remaining = player.getInventory().addItem(item);
            if (remaining.isEmpty()) {
                player.sendMessage(ChatColor.YELLOW + "You received your " + oldElement.name().toLowerCase() + " altar item back!");
            } else {
                remaining.values().forEach(dropped -> 
                    player.getWorld().dropItemNaturally(player.getLocation(), dropped));
                player.sendMessage(ChatColor.YELLOW + "Your inventory was full, so your " + oldElement.name().toLowerCase() + " altar item dropped in front of you!");
            }
        }
    }

    public void applyUpsides(Player player) {
        effectService.applyPassiveEffects(player);
    }

    public boolean useAbility1(Player player) {
        return useAbility(player, 1);
    }

    public boolean useAbility2(Player player) {
        return useAbility(player, 2);
    }

    private boolean useAbility(Player player, int number) {
        if (!store.areAbilitiesEnabled()) {
            return false;
        }
        PlayerData pd = data(player.getUniqueId());
        ElementType type = pd.getCurrentElement();
        if (type == null) {
            return false;
        }
        if (!store.isElementEnabled(type)) {
            player.sendMessage(ChatColor.RED + "Your element " + type.name().toLowerCase() + " is disabled by the server.");
            return false;
        }
        Element element = get(player, type);

        if (element == null) return false;

        ElementContext ctx = ElementContext.builder()
                .player(player)
                .upgradeLevel(pd.getUpgradeLevel(type))
                .elementType(type)
                .manaManager(manaManager)
                .trustManager(trustManager)
                .configManager(configManager)
                .plugin(plugin)
                .build();

        return number == 1 ? element.ability1(ctx) : element.ability2(ctx);
    }

    private void showElementTitle(Player player, ElementType type, String title) {
        var titleObj = net.kyori.adventure.title.Title.title(
                net.kyori.adventure.text.Component.text(title).color(net.kyori.adventure.text.format.NamedTextColor.GOLD),
                net.kyori.adventure.text.Component.text(type.name()).color(net.kyori.adventure.text.format.NamedTextColor.AQUA),
                net.kyori.adventure.title.Title.Times.times(
                        java.time.Duration.ofMillis(500),
                        java.time.Duration.ofMillis(2000),
                        java.time.Duration.ofMillis(500)
                )
        );
        player.showTitle(titleObj);
        playSelectionJingle(player);
    }

    private void playSelectionJingle(Player player) {
        new BukkitRunnable() {
            int step = 0;
            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }

                switch (step) {
                    case 0 -> player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 0.8f);
                    case 1 -> player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.0f);
                    case 2 -> player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.2f);
                    case 3 -> {
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.5f);
                        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.5f, 1.5f);
                        cancel();
                    }
                }
                step++;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }
}
