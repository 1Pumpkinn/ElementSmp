package net.saturn.elementsmp.managers;

import net.saturn.elementsmp.ElementSmp;
import net.saturn.elementsmp.elements.impl.air.listeners.*;
import net.saturn.elementsmp.elements.impl.death.listeners.*;
import net.saturn.elementsmp.elements.impl.earth.listeners.*;
import net.saturn.elementsmp.elements.impl.fire.listeners.*;
import net.saturn.elementsmp.elements.impl.frost.listeners.*;
import net.saturn.elementsmp.elements.impl.life.listeners.*;
import net.saturn.elementsmp.elements.impl.lightning.listeners.*;
import net.saturn.elementsmp.elements.impl.metal.listeners.*;
import net.saturn.elementsmp.elements.impl.water.listeners.*;
import net.saturn.elementsmp.listeners.core.GUIListener;
import net.saturn.elementsmp.listeners.core.CombatListener;
import net.saturn.elementsmp.listeners.item.*;
import net.saturn.elementsmp.listeners.core.GameModeListener;
import net.saturn.elementsmp.listeners.core.PlayerLifecycleListener;
import net.saturn.elementsmp.listeners.item.altar.LightningElementListener;
import net.saturn.elementsmp.listeners.item.core.AdvancedRerollerListener;
import net.saturn.elementsmp.listeners.item.core.RerollerListener;
import net.saturn.elementsmp.listeners.item.core.UpgraderListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

public class ListenerManager {
    private final ElementSmp plugin;

    public ListenerManager(ElementSmp plugin) {
        this.plugin = plugin;
    }

    public void registerListeners() {
        plugin.getLogger().info("Registering listeners...");
        PluginManager pm = Bukkit.getPluginManager();

        // Core Listeners
        pm.registerEvents(new PlayerLifecycleListener(plugin, plugin.getElementManager(), plugin.getManaManager(), plugin.getEffectService()), plugin);
        pm.registerEvents(plugin.getEffectService(), plugin);
        pm.registerEvents(new GameModeListener(plugin.getManaManager(), plugin.getConfigManager()), plugin);
        pm.registerEvents(new CombatListener(plugin.getTrustManager(), plugin.getElementManager()), plugin);
        
        pm.registerEvents(plugin.getAbilityListener(), plugin);
        pm.registerEvents(new GUIListener(plugin), plugin);

        // Item Listeners
        pm.registerEvents(new ElementItemCraftListener(plugin, plugin.getElementManager()), plugin);
        pm.registerEvents(new ElementItemDeathListener(plugin, plugin.getElementManager()), plugin);
        pm.registerEvents(new RerollerListener(plugin), plugin);
        pm.registerEvents(new AdvancedRerollerListener(plugin), plugin);
        pm.registerEvents(new LightningElementListener(plugin), plugin);
        pm.registerEvents(new UpgraderListener(plugin, plugin.getElementManager()), plugin);

        // Element Specific Listeners
        registerElementListeners(pm);

        plugin.getLogger().info("Listeners registered");
    }

    private void registerElementListeners(PluginManager pm) {
        var elementManager = plugin.getElementManager();
        var trustManager = plugin.getTrustManager();

        // Air
        pm.registerEvents(new AirSlowFallingPassive(elementManager), plugin);
        pm.registerEvents(new AirFallDamagePassive(elementManager), plugin);
        
        // Water
        pm.registerEvents(new WaterCombatPassive(elementManager), plugin);
        pm.registerEvents(new WaterPrisonListener(plugin), plugin);
        
        // Fire
        pm.registerEvents(new FireCombatPassive(elementManager, trustManager), plugin);
        pm.registerEvents(new FireAutoSmeltPassive(elementManager), plugin);
        
        // Earth
        pm.registerEvents(new EarthCombatPassive(elementManager, trustManager), plugin);
        pm.registerEvents(new EarthOreDropPassive(elementManager), plugin);
        
        // Life
        pm.registerEvents(new LifeCropPassive(plugin, elementManager), plugin);
        pm.registerEvents(new LifeHeartsPassive(plugin, elementManager), plugin);
        pm.registerEvents(new LifeListener(plugin, elementManager), plugin);
        
        // Death
        pm.registerEvents(new DeathWitherPassive(elementManager, trustManager), plugin);
        pm.registerEvents(new DeathFriendlyMobPassive(plugin, trustManager, elementManager), plugin);
        pm.registerEvents(new DeathInvisibilityPassive(plugin, elementManager), plugin);
        
        // Metal
        pm.registerEvents(new MetalCombatPassive(elementManager, trustManager), plugin);
        pm.registerEvents(new MetalArrowImmunityPassive(elementManager), plugin);
        
        // Frost
        pm.registerEvents(new FrostSpeedPassive(plugin, elementManager), plugin);
        pm.registerEvents(new FrostWaterFreezePassive(elementManager), plugin);

        // Lightning
        pm.registerEvents(new LightningPassive(plugin, elementManager), plugin);
    }
}
