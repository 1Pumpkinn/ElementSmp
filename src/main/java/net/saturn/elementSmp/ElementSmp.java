package net.saturn.elementSmp;

import net.saturn.elementSmp.commands.*;
import net.saturn.elementSmp.data.DataStore;
import net.saturn.elementSmp.elements.abilities.AbilityRegistry;
import net.saturn.elementSmp.listeners.ability.AbilityListener;
import net.saturn.elementSmp.listeners.combat.CombatListener;
import net.saturn.elementSmp.listeners.GUIListener;
import net.saturn.elementSmp.listeners.item.*;
import net.saturn.elementSmp.listeners.player.*;
import net.saturn.elementSmp.managers.*;
import net.saturn.elementSmp.services.EffectService;
import net.saturn.elementSmp.util.bukkit.MetadataHelper;
import net.saturn.elementSmp.util.scheduling.TaskScheduler;
import net.saturn.elementSmp.elements.abilities.impl.air.passives.*;
import net.saturn.elementSmp.elements.abilities.impl.water.passives.WaterCombatPassive;
import net.saturn.elementSmp.elements.abilities.impl.fire.passives.FireAutoSmeltPassive;
import net.saturn.elementSmp.elements.abilities.impl.fire.passives.FireCombatPassive;
import net.saturn.elementSmp.elements.abilities.impl.earth.passives.*;
import net.saturn.elementSmp.elements.abilities.impl.life.passives.*;
import net.saturn.elementSmp.elements.abilities.impl.death.passives.*;
import net.saturn.elementSmp.elements.abilities.impl.metal.passives.MetalArrowImmunityPassive;
import net.saturn.elementSmp.elements.abilities.impl.metal.passives.MetalCombatPassive;
import net.saturn.elementSmp.elements.abilities.impl.frost.passives.FrostCombatPassive;
import net.saturn.elementSmp.elements.abilities.impl.frost.passives.FrostSpeedPassive;
import net.saturn.elementSmp.elements.abilities.impl.frost.passives.FrostWaterFreezePassive;
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
    private BlockReversionManager blockReversionManager;
    private AbilityRegistry abilityRegistry;
    private EffectService effectService;
    private TaskScheduler taskScheduler;
    private MetadataHelper metadataHelper;
    private AbilityListener abilityListener;

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
        this.blockReversionManager = new BlockReversionManager(this);
        this.abilityRegistry = new AbilityRegistry(this, blockReversionManager);
        this.elementManager = new ElementManager(this, dataStore, manaManager, trustManager, configManager);
        this.itemManager = new ItemManager(this, manaManager, configManager);

        getLogger().info("Managers initialized");
    }

    private void initializeServices() {
        getLogger().info("Initializing services...");

        this.effectService = elementManager.getEffectService();

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

                var ability1Cmd = new org.bukkit.command.defaults.BukkitCommand("ability1") {
                    @Override
                    public boolean execute(org.bukkit.command.CommandSender sender, String label, String[] args) {
                        if (!(sender instanceof org.bukkit.entity.Player player)) return false;
                        return abilityListener.triggerAbility(player, 1);
                    }
                };
                ability1Cmd.setDescription("Use Ability 1");

                var ability2Cmd = new org.bukkit.command.defaults.BukkitCommand("ability2") {
                    @Override
                    public boolean execute(org.bukkit.command.CommandSender sender, String label, String[] args) {
                        if (!(sender instanceof org.bukkit.entity.Player player)) return false;
                        return abilityListener.triggerAbility(player, 2);
                    }
                };
                ability2Cmd.setDescription("Use Ability 2");

                // Register all commands
                commandMap.register("elementsmp", elementsCmd);
                commandMap.register("elementsmp", trustCmd);
                commandMap.register("elementsmp", elementCmd);
                commandMap.register("elementsmp", manaCmd);
                commandMap.register("elementsmp", utilCmd);
                commandMap.register("elementsmp", toggleRecipeCmd);
                commandMap.register("elementsmp", ability1Cmd);
                commandMap.register("elementsmp", ability2Cmd);

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
        this.abilityListener = new AbilityListener(this, elementManager);
        pm.registerEvents(this.abilityListener, this);
        registerItemListeners(pm);
        pm.registerEvents(new GUIListener(this), this);
        registerElementListeners(pm);

        getLogger().info("Listeners registered");
    }

    private void registerItemListeners(PluginManager pm) {
        pm.registerEvents(new ElementItemCraftListener(this, elementManager), this);
        pm.registerEvents(new ElementItemDeathListener(this, elementManager), this);
        pm.registerEvents(new RerollerListener(this), this);
        pm.registerEvents(new AdvancedRerollerListener(this), this);
        pm.registerEvents(new UpgraderListener(this, elementManager), this);
    }

    private void registerElementListeners(PluginManager pm) {
        // Air
        pm.registerEvents(new AirSlowFallingPassive(elementManager), this);
        pm.registerEvents(new AirFallDamagePassive(elementManager), this);
        
        // Water
        pm.registerEvents(new WaterCombatPassive(elementManager), this);
        pm.registerEvents(new net.saturn.elementSmp.elements.impl.water.listeners.WaterPrisonListener(this), this);
        
        // Fire
        pm.registerEvents(new FireCombatPassive(elementManager, trustManager), this);
        pm.registerEvents(new FireAutoSmeltPassive(elementManager), this);
        
        // Earth
        pm.registerEvents(new EarthCombatPassive(elementManager, trustManager), this);
        pm.registerEvents(new EarthOreDropPassive(elementManager), this);
        
        // Life
        pm.registerEvents(new LifeCropPassive(this, elementManager), this);
        pm.registerEvents(new net.saturn.elementSmp.elements.impl.life.listeners.LifeListener(this, elementManager), this);
        
        // Death
        pm.registerEvents(new DeathWitherPassive(elementManager, trustManager), this);
        pm.registerEvents(new DeathInvisibilityPassive(this, elementManager), this);
        pm.registerEvents(new DeathFriendlyMobPassive(this, trustManager, elementManager), this);
        
        // Metal
        pm.registerEvents(new MetalCombatPassive(elementManager, trustManager), this);
        pm.registerEvents(new MetalArrowImmunityPassive(elementManager), this);
        // pm.registerEvents(new net.saturn.elementSmp.elements.impl.metal.listeners.MetalChainStunListener(), this);
        
        // Frost
        pm.registerEvents(new FrostSpeedPassive(this, elementManager), this);
        pm.registerEvents(new FrostCombatPassive(elementManager, trustManager), this);
        pm.registerEvents(new FrostWaterFreezePassive(elementManager), this);
    }

    private void registerRecipes() {
        taskScheduler.runLater(() -> {
            getLogger().info("Registering recipes...");
            UtilRecipes.registerRecipes(this);
            getLogger().info("Recipes registered");
        }, 20);
    }

    private void startBackgroundTasks() {
        manaManager.start();
        
        // Auto-save task every 5 minutes
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, this::saveAllData, 6000L, 6000L);
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
    public TaskScheduler getScheduler() { return taskScheduler; }
    public MetadataHelper getMetadataHelper() { return metadataHelper; }
}
