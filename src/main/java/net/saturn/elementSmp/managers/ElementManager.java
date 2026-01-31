package net.saturn.elementSmp.managers;

import net.saturn.elementSmp.ElementSmp;
import net.saturn.elementSmp.config.Constants;
import net.saturn.elementSmp.data.DataStore;
import net.saturn.elementSmp.data.PlayerData;
import net.saturn.elementSmp.elements.*;
import net.saturn.elementSmp.elements.impl.air.AirElement;
import net.saturn.elementSmp.elements.impl.death.DeathElement;
import net.saturn.elementSmp.elements.impl.earth.EarthElement;
import net.saturn.elementSmp.elements.impl.fire.FireElement;
import net.saturn.elementSmp.elements.impl.frost.FrostElement;
import net.saturn.elementSmp.elements.impl.life.LifeElement;
import net.saturn.elementSmp.elements.impl.metal.MetalElement;
import net.saturn.elementSmp.elements.impl.water.WaterElement;
import net.saturn.elementSmp.gui.ElementSelectionGUI;
import net.saturn.elementSmp.items.AdvancedRerollerItem;
import net.saturn.elementSmp.items.ItemKeys;
import net.saturn.elementSmp.items.RerollerItem;
import net.saturn.elementSmp.services.EffectService;
import net.saturn.elementSmp.util.scheduling.TaskScheduler;
import org.bukkit.ChatColor;
import org.bukkit.Material;
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
        registerAllElements();
    }

    public ElementSmp getPlugin() { return plugin; }
    public EffectService getEffectService() { return effectService; }

    private void registerAllElements() {
        registry.put(ElementType.AIR, new AirElement(plugin));
        registry.put(ElementType.WATER, new WaterElement(plugin));
        registry.put(ElementType.FIRE, new FireElement(plugin));
        registry.put(ElementType.EARTH, new EarthElement(plugin));
        registry.put(ElementType.LIFE, new LifeElement(plugin));
        registry.put(ElementType.DEATH, new DeathElement(plugin));
        registry.put(ElementType.METAL, new MetalElement(plugin));
        registry.put(ElementType.FROST, new FrostElement(plugin));
    }

    public PlayerData data(UUID uuid) {
        return store.getPlayerData(uuid);
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
        PlayerData pd = data(player.getUniqueId());
        ElementType old = pd.getCurrentElement();

        if (old != null && old != type) {
            handleElementSwitch(player, old);
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
        PlayerData pd = data(player.getUniqueId());
        ElementType type = pd.getCurrentElement();
        Element element = registry.get(type);

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
