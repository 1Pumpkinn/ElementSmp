package net.saturn.elementSmp;

import net.saturn.elementSmp.commands.*;
import net.saturn.elementSmp.config.Constants;
import net.saturn.elementSmp.data.DataStore;
import net.saturn.elementSmp.elements.abilities.AbilityRegistry;
import net.saturn.elementSmp.listeners.ability.AbilityListener;
import net.saturn.elementSmp.listeners.combat.CombatListener;
import net.saturn.elementSmp.listeners.GUIListener;
import net.saturn.elementSmp.listeners.item.*;
import net.saturn.elementSmp.listeners.player.*;
import net.saturn.elementSmp.managers.*;
import net.saturn.elementSmp.services.EffectService;
import net.saturn.elementSmp.services.ValidationService;
import net.saturn.elementSmp.util.bukkit.MetadataHelper;
import net.saturn.elementSmp.util.scheduling.TaskScheduler;
import net.saturn.elementSmp.elements.impl.air.listeners.*;
import net.saturn.elementSmp.elements.impl.water.listeners.*;
import net.saturn.elementSmp.elements.impl.fire.listeners.*;
import net.saturn.elementSmp.elements.impl.earth.listeners.*;
import net.saturn.elementSmp.elements.impl.life.listeners.*;
import net.saturn.elementSmp.elements.impl.death.listeners.*;
import net.saturn.elementSmp.elements.impl.metal.listeners.*;
import net.saturn.elementSmp.elements.impl.frost.listeners.*;
import net.saturn.elementSmp.elements.impl.life.LifeElementCraftListener;
import net.saturn.elementSmp.elements.impl.death.DeathElementCraftListener;
import net.saturn.elementSmp.recipes.UtilRecipes;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class ElementSmp extends JavaPlugin {
    private DataStore dataStore;
    private ConfigManager configManager;
    private ElementManager elementManager;
    private ManaManager manaManager;
    private TrustManager trustManager;
    private ItemManager itemManager;
    private AbilityRegistry abilityRegistry;
    private EffectService effectService;
    private ValidationService validationService;
    private TaskScheduler taskScheduler;
    private MetadataHelper metadataHelper;

    @Override
    public void onEnable() {
        try {
            saveDefaultConfig();
            initializeCore();
            initializeManagers();
            initializeServices();
            initializeUtilities();
            registerComponents();
            startBackgroundTasks();

            getLogger().info("ElementSmp v" + getDescription().getVersion() + " enabled successfully!");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to enable plugin", e);
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        try {
            stopBackgroundTasks();
            saveAllData();
            getLogger().info("ElementSmp disabled successfully!");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Error during plugin shutdown", e);
        }
    }

    private void initializeCore() {
        getLogger().info("Initializing core components...");
        this.configManager = new ConfigManager(this);
        this.dataStore = new DataStore(this);
        getLogger().info("Core components initialized");
    }

    private void initializeManagers() {
        getLogger().info("Initializing managers...");

        this.trustManager = new TrustManager(this);
        this.manaManager = new ManaManager(this, dataStore, configManager);
        this.elementManager = new ElementManager(this, dataStore, manaManager, trustManager, configManager);
        this.itemManager = new ItemManager(this, manaManager, configManager);

        getLogger().info("Managers initialized");
    }

    private void initializeServices() {
        getLogger().info("Initializing services...");

        this.effectService = new EffectService(this, elementManager);
        this.validationService = new ValidationService(trustManager);
        this.abilityRegistry = new AbilityRegistry(this);

        getLogger().info("Services initialized");
    }

    private void initializeUtilities() {
        getLogger().info("Initializing utilities...");

        this.taskScheduler = new TaskScheduler(this);
        this.metadataHelper = new MetadataHelper(this);

        getLogger().info("Utilities initialized");
    }

    private void registerComponents() {
        registerCommands();
        registerListeners();
        registerRecipes();
    }

    private void registerCommands() {
        getLogger().info("Registering commands...");

        // For Paper plugins, we need to use the Bukkit command map directly
        // This is done after the server is fully loaded
        Bukkit.getScheduler().runTask(this, () -> {
            try {
                var commandMap = Bukkit.getCommandMap();

                // Create and register commands
                var elementsCmd = new org.bukkit.command.defaults.BukkitCommand("elements") {
                    private final ElementInfoCommand executor = new ElementInfoCommand(ElementSmp.this);

                    @Override
                    public boolean execute(org.bukkit.command.CommandSender sender, String label, String[] args) {
                        return executor.onCommand(sender, this, label, args);
                    }

                    @Override
                    public java.util.List<String> tabComplete(org.bukkit.command.CommandSender sender, String alias, String[] args) {
                        return executor.onTabComplete(sender, this, alias, args);
                    }
                };
                elementsCmd.setDescription("View element info");

                var trustCmd = new org.bukkit.command.defaults.BukkitCommand("trust") {
                    private final TrustCommand executor = new TrustCommand(ElementSmp.this, trustManager);

                    @Override
                    public boolean execute(org.bukkit.command.CommandSender sender, String label, String[] args) {
                        return executor.onCommand(sender, this, label, args);
                    }

                    @Override
                    public java.util.List<String> tabComplete(org.bukkit.command.CommandSender sender, String alias, String[] args) {
                        return executor.onTabComplete(sender, this, alias, args);
                    }
                };
                trustCmd.setDescription("Manage trust list");

                var elementCmd = new org.bukkit.command.defaults.BukkitCommand("element") {
                    private final ElementCommand executor = new ElementCommand(ElementSmp.this);

                    @Override
                    public boolean execute(org.bukkit.command.CommandSender sender, String label, String[] args) {
                        return executor.onCommand(sender, this, label, args);
                    }

                    @Override
                    public java.util.List<String> tabComplete(org.bukkit.command.CommandSender sender, String alias, String[] args) {
                        return executor.onTabComplete(sender, this, alias, args);
                    }
                };
                elementCmd.setDescription("Admin element commands");

                var manaCmd = new org.bukkit.command.defaults.BukkitCommand("mana") {
                    private final ManaCommand executor = new ManaCommand(manaManager, configManager);

                    @Override
                    public boolean execute(org.bukkit.command.CommandSender sender, String label, String[] args) {
                        return executor.onCommand(sender, this, label, args);
                    }
                };
                manaCmd.setDescription("Manage mana");

                var utilCmd = new org.bukkit.command.defaults.BukkitCommand("util") {
                    private final UtilCommand executor = new UtilCommand(ElementSmp.this);

                    @Override
                    public boolean execute(org.bukkit.command.CommandSender sender, String label, String[] args) {
                        return executor.onCommand(sender, this, label, args);
                    }
                };
                utilCmd.setDescription("Utility commands");

                var toggleRecipeCmd = new org.bukkit.command.defaults.BukkitCommand("togglerecipe") {
                    private final ToggleRecipeCommand executor = new ToggleRecipeCommand(ElementSmp.this);

                    @Override
                    public boolean execute(org.bukkit.command.CommandSender sender, String label, String[] args) {
                        return executor.onCommand(sender, this, label, args);
                    }

                    @Override
                    public java.util.List<String> tabComplete(org.bukkit.command.CommandSender sender, String alias, String[] args) {
                        return executor.onTabComplete(sender, this, alias, args);
                    }
                };
                toggleRecipeCmd.setDescription("Toggle recipes");

                // Register all commands
                commandMap.register("elementsmp", elementsCmd);
                commandMap.register("elementsmp", trustCmd);
                commandMap.register("elementsmp", elementCmd);
                commandMap.register("elementsmp", manaCmd);
                commandMap.register("elementsmp", utilCmd);
                commandMap.register("elementsmp", toggleRecipeCmd);

                getLogger().info("Commands registered successfully");
            } catch (Exception e) {
                getLogger().log(Level.SEVERE, "Failed to register commands", e);
            }
        });

        getLogger().info("Commands registration scheduled");
    }

    private void registerListeners() {
        getLogger().info("Registering listeners...");
        PluginManager pm = Bukkit.getPluginManager();

        pm.registerEvents(new PlayerLifecycleListener(this, elementManager, manaManager, effectService), this);
        pm.registerEvents(effectService, this);
        pm.registerEvents(new GameModeListener(manaManager, configManager), this);
        pm.registerEvents(new CombatListener(trustManager, elementManager), this);
        pm.registerEvents(new AbilityListener(this, elementManager), this);
        registerItemListeners(pm);
        pm.registerEvents(new GUIListener(this), this);
        registerElementListeners(pm);

        getLogger().info("Listeners registered");
    }

    private void registerItemListeners(PluginManager pm) {
        pm.registerEvents(new ElementItemUseListener(this, elementManager, itemManager), this);
        pm.registerEvents(new ElementItemCraftListener(this, elementManager), this);
        pm.registerEvents(new ElementItemDeathListener(this, elementManager), this);
        pm.registerEvents(new ElementItemDropListener(this), this);
        pm.registerEvents(new ElementItemPickupListener(this, elementManager), this);
        pm.registerEvents(new ElementInventoryProtectionListener(this, elementManager), this);
        pm.registerEvents(new ElementCombatProjectileListener(itemManager), this);
        pm.registerEvents(new RerollerListener(this), this);
        pm.registerEvents(new AdvancedRerollerListener(this), this);
        pm.registerEvents(new UpgraderListener(this, elementManager), this);
    }

    private void registerElementListeners(PluginManager pm) {
        pm.registerEvents(new FallDamageListener(elementManager), this);
        pm.registerEvents(new AirCombatListener(elementManager), this);
        pm.registerEvents(new WaterDrowningImmunityListener(elementManager), this);
        pm.registerEvents(new FireImmunityListener(elementManager), this);
        pm.registerEvents(new FireCombatListener(elementManager, trustManager), this);
        pm.registerEvents(new FireballProtectionListener(), this);
        pm.registerEvents(new EarthCharmListener(elementManager, this), this);
        pm.registerEvents(new EarthFriendlyMobListener(this, trustManager), this);
        pm.registerEvents(new EarthOreDropListener(elementManager), this);
        pm.registerEvents(new LifeRegenListener(elementManager), this);
        pm.registerEvents(new LifeElementCraftListener(this, elementManager), this);
        pm.registerEvents(new DeathRawFoodListener(elementManager), this);
        pm.registerEvents(new DeathFriendlyMobListener(this, trustManager), this);
        pm.registerEvents(new DeathElementCraftListener(this, elementManager), this);
        pm.registerEvents(new MetalArrowImmunityListener(elementManager), this);
        pm.registerEvents(new MetalChainStunListener(), this);
        pm.registerEvents(new FrostPassiveListener(this, elementManager), this);
        pm.registerEvents(new FrostFrozenPunchListener(this, elementManager), this);
    }

    private void registerRecipes() {
        taskScheduler.runLaterSeconds(() -> {
            getLogger().info("Registering recipes...");
            UtilRecipes.registerRecipes(this);
            getLogger().info("Recipes registered");
        }, 1);
    }

    private void startBackgroundTasks() {
        manaManager.start();
    }

    private void stopBackgroundTasks() {
        if (manaManager != null) {
            manaManager.stop();
        }
    }

    private void saveAllData() {
        if (dataStore != null) {
            dataStore.flushAll();
        }
    }

    public DataStore getDataStore() { return dataStore; }
    public ConfigManager getConfigManager() { return configManager; }
    public ElementManager getElementManager() { return elementManager; }
    public ManaManager getManaManager() { return manaManager; }
    public TrustManager getTrustManager() { return trustManager; }
    public ItemManager getItemManager() { return itemManager; }
    public AbilityRegistry getAbilityRegistry() { return abilityRegistry; }
    public EffectService getEffectService() { return effectService; }
    public ValidationService getValidationService() { return validationService; }
    public TaskScheduler getTaskScheduler() { return taskScheduler; }
    public MetadataHelper getMetadataHelper() { return metadataHelper; }
}