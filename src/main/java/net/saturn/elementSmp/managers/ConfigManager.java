package net.saturn.elementSmp.managers;

import net.saturn.elementSmp.ElementSmp;
import net.saturn.elementSmp.config.AbilityCosts;
import net.saturn.elementSmp.config.Constants;
import net.saturn.elementSmp.elements.ElementType;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.logging.Level;

public class ConfigManager {
    private final ElementSmp plugin;
    private FileConfiguration config;

    public ConfigManager(ElementSmp plugin) {
        this.plugin = plugin;
        try {
            this.config = plugin.getConfig();
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load plugin configuration", e);
            throw new RuntimeException("Could not load plugin configuration", e);
        }
    }

    public void reload() {
        try {
            plugin.reloadConfig();
            this.config = plugin.getConfig();
            plugin.getLogger().info("Configuration reloaded successfully");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to reload configuration", e);
        }
    }

    // Mana settings
    public int getMaxMana() {
        try {
            return config.getInt("mana.max", Constants.Mana.DEFAULT_MAX);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error reading mana.max from config, using default value " + Constants.Mana.DEFAULT_MAX, e);
            return Constants.Mana.DEFAULT_MAX;
        }
    }

    public int getManaRegenPerSecond() {
        try {
            return config.getInt("mana.regen_per_second", Constants.Mana.DEFAULT_REGEN);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error reading mana.regen_per_second from config, using default value " + Constants.Mana.DEFAULT_REGEN, e);
            return Constants.Mana.DEFAULT_REGEN;
        }
    }

    // Ability costs
    public int getAbility1Cost(ElementType type) {
        try {
            String path = "costs." + type.name().toLowerCase() + ".ability1";
            return config.getInt(path, AbilityCosts.getDefaults(type).ability1());
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error reading ability1 cost for " + type + " from config, using default value", e);
            return AbilityCosts.getDefaults(type).ability1();
        }
    }

    public int getAbility2Cost(ElementType type) {
        try {
            String path = "costs." + type.name().toLowerCase() + ".ability2";
            return config.getInt(path, AbilityCosts.getDefaults(type).ability2());
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error reading ability2 cost for " + type + " from config, using default value", e);
            return AbilityCosts.getDefaults(type).ability2();
        }
    }

    public boolean isAdvancedRerollerRecipeEnabled() {
        try {
            return config.getBoolean("recipes.advanced_reroller_enabled", Constants.Default.ADVANCED_REROLLER_ENABLED);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error reading advanced_reroller_enabled from config, using default value " + Constants.Default.ADVANCED_REROLLER_ENABLED, e);
            return Constants.Default.ADVANCED_REROLLER_ENABLED;
        }
    }

    public void setAdvancedRerollerRecipeEnabled(boolean enabled) {
        config.set("recipes.advanced_reroller_enabled", enabled);
        plugin.saveConfig();
    }
}
