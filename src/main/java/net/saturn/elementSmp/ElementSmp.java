package net.saturn.elementsmp;

import net.saturn.elementsmp.altar.AltarManager;
import net.saturn.elementsmp.data.DataStore;
import net.saturn.elementsmp.elements.registry.AbilityRegistry;
import net.saturn.elementsmp.listeners.ability.AbilityListener;
import net.saturn.elementsmp.managers.*;
import net.saturn.elementsmp.services.EffectService;
import net.saturn.elementsmp.util.bukkit.MetadataHelper;
import net.saturn.elementsmp.util.scheduling.TaskScheduler;
import net.saturn.elementsmp.recipes.UtilRecipes;
import org.bukkit.Bukkit;
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
    private AltarManager altarManager;
    private CommandManager commandManager;
    private ListenerManager listenerManager;
    private StructureGenerationManager structureGenerationManager;

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

            // Attempt to generate the altar if it doesn't exist
            structureGenerationManager.attemptGeneration();

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
    }

    private void initializeManagers() {
        getLogger().info("Initializing managers...");
        this.trustManager = new TrustManager(this);
        this.manaManager = new ManaManager(this, dataStore, configManager);
        this.blockReversionManager = new BlockReversionManager(this);
        this.abilityRegistry = new AbilityRegistry(this, blockReversionManager);
        this.elementManager = new ElementManager(this, dataStore, manaManager, trustManager, configManager);
        this.itemManager = new ItemManager(this, manaManager, configManager);
        this.altarManager = new AltarManager(this);
        this.commandManager = new CommandManager(this);
        this.listenerManager = new ListenerManager(this);
        this.structureGenerationManager = new StructureGenerationManager(this);
    }

    private void initializeServices() {
        getLogger().info("Initializing services...");
        this.effectService = elementManager.getEffectService();
    }

    private void initializeUtilities() {
        getLogger().info("Initializing utilities...");
        this.taskScheduler = new TaskScheduler(this);
        this.metadataHelper = new MetadataHelper(this);
        this.abilityListener = new AbilityListener(this, elementManager);
    }

    private void registerComponents() {
        commandManager.registerCommands();
        listenerManager.registerListeners();
        registerRecipes();
    }

    private void registerRecipes() {
        taskScheduler.runLater(() -> {
            getLogger().info("Registering recipes...");
            UtilRecipes.registerRecipes(this);
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

    // Getters
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
    public AltarManager getAltarManager() { return altarManager; }
    public AbilityListener getAbilityListener() { return abilityListener; }
    public StructureGenerationManager getStructureGenerationManager() { return structureGenerationManager; }
}
