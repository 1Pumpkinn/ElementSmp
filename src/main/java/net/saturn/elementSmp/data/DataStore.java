package net.saturn.elementsmp.data;

import net.saturn.elementsmp.ElementSmp;
import net.saturn.elementsmp.elements.core.ElementType;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class DataStore {
    private final ElementSmp plugin;
    private final File playerFile;
    private final FileConfiguration playerCfg;
    private final File serverFile;
    private final FileConfiguration serverCfg;
    private final Map<UUID, PlayerData> cache = new HashMap<>();

    public DataStore(ElementSmp plugin) {
        this.plugin = plugin;
        File dataDir = new File(plugin.getDataFolder(), "data");
        ensureDirectoryExists(dataDir);

        this.playerFile = initializeFile(dataDir, "players.yml");
        this.playerCfg = YamlConfiguration.loadConfiguration(playerFile);

        this.serverFile = initializeFile(dataDir, "server.yml");
        this.serverCfg = YamlConfiguration.loadConfiguration(serverFile);
    }

    private void ensureDirectoryExists(File dir) {
        if (!dir.exists() && !dir.mkdirs()) {
            throw new RuntimeException("Could not create data directory: " + dir.getAbsolutePath());
        }
    }

    private File initializeFile(File dir, String name) {
        File file = new File(dir, name);
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    throw new IOException("Failed to create " + name);
                }
            } catch (IOException e) {
                throw new RuntimeException("Could not initialize file: " + name, e);
            }
        }
        return file;
    }

    public PlayerData getPlayerData(UUID uuid) {
        return cache.computeIfAbsent(uuid, this::loadPlayerDataFromFile);
    }

    private PlayerData loadPlayerDataFromFile(UUID uuid) {
        String uuidString = uuid.toString();
        ConfigurationSection section = findSection(uuidString);
        
        if (section == null) {
            return new PlayerData(uuid);
        }

        PlayerData pd = new PlayerData(uuid, section);
        resolveTrustList(pd, section);
        return pd;
    }

    private ConfigurationSection findSection(String uuid) {
        ConfigurationSection playersSec = playerCfg.getConfigurationSection("players");
        if (playersSec != null) {
            for (String key : playersSec.getKeys(false)) {
                ConfigurationSection s = playersSec.getConfigurationSection(key);
                if (s != null && uuid.equalsIgnoreCase(s.getString("uuid"))) {
                    return s;
                }
            }
        }
        return playerCfg.getConfigurationSection("players." + uuid);
    }

    private void resolveTrustList(PlayerData pd, ConfigurationSection section) {
        ConfigurationSection trustSection = section.getConfigurationSection("trust");
        if (trustSection == null) return;

        Set<UUID> resolved = new HashSet<>(pd.getTrustedPlayers());
        for (String key : trustSection.getKeys(false)) {
            try {
                resolved.add(UUID.fromString(key));
            } catch (IllegalArgumentException ex) {
                var off = plugin.getServer().getOfflinePlayer(key);
                if (off.getUniqueId() != null) {
                    resolved.add(off.getUniqueId());
                }
            }
        }
        pd.setTrustedPlayers(resolved);
    }

    public synchronized void save(PlayerData pd) {
        try {
            var off = plugin.getServer().getOfflinePlayer(pd.getUuid());
            String name = off.getName();
            String key = "players." + (name != null && !name.isEmpty() ? name : pd.getUuid().toString());
            
            ConfigurationSection sec = playerCfg.getConfigurationSection(key);
            if (sec == null) sec = playerCfg.createSection(key);

            sec.set("uuid", pd.getUuid().toString());
            sec.set("element", pd.getCurrentElement() == null ? null : pd.getCurrentElement().name());
            sec.set("mana", pd.getMana());
            sec.set("currentUpgradeLevel", pd.getCurrentElementUpgradeLevel());
            sec.set("altarElement", pd.isAltarElement());
            sec.set("needsReroll", pd.needsReroll());

            saveTrustList(pd, sec);
            cache.put(pd.getUuid(), pd);
            flushPlayerData();
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save player data for " + pd.getUuid(), e);
        }
    }

    private void saveTrustList(PlayerData pd, ConfigurationSection sec) {
        sec.set("trust", null);
        if (pd.getTrustedPlayers().isEmpty()) return;

        ConfigurationSection trustSec = sec.createSection("trust");
        for (UUID trustedUuid : pd.getTrustedPlayers()) {
            var t = plugin.getServer().getOfflinePlayer(trustedUuid);
            String tName = t.getName();
            String trustKey = (tName != null && !tName.isEmpty()) ? tName : trustedUuid.toString();
            trustSec.set(trustKey, true);
        }
    }

    public synchronized void invalidateCache(UUID uuid) {
        cache.remove(uuid);
    }

    public synchronized void flushAll() {
        flushPlayerData();
        flushServerData();
    }

    private void flushPlayerData() {
        try {
            playerCfg.save(playerFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save players.yml", e);
        }
    }

    private void flushServerData() {
        try {
            serverCfg.save(serverFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save server.yml", e);
        }
    }

    // Server Settings
    public boolean areAbilitiesEnabled() { return getBoolean("abilities_enabled", true); }
    public void setAbilitiesEnabled(boolean val) { setBoolean("abilities_enabled", val); }
    
    public boolean isElementRollEnabled() { return getBoolean("element_roll_enabled", true); }
    public void setElementRollEnabled(boolean val) { setBoolean("element_roll_enabled", val); }

    public boolean isElementEnabled(ElementType type) { 
        return getBoolean("element_" + type.name().toLowerCase(), true); 
    }
    public void setElementEnabled(ElementType type, boolean val) { 
        setBoolean("element_" + type.name().toLowerCase(), val); 
    }

    public boolean isRecipeEnabled(String name) { return getBoolean("recipe_" + name, true); }
    public void setRecipeEnabled(String name, boolean val) { setBoolean("recipe_" + name, val); }

    public boolean isElementUnlocked(ElementType type) {
        if (type == ElementType.LIGHTNING) {
            return getBoolean("element_unlocked_" + type.name().toLowerCase(), false);
        }
        return true;
    }
    public void setElementUnlocked(ElementType type, boolean val) { 
        setBoolean("element_unlocked_" + type.name().toLowerCase(), val); 
    }

    public boolean isAltarGenerated() {
        return getBoolean("altar_generated", false);
    }

    public void setAltarGenerated(boolean val) {
        setBoolean("altar_generated", val);
    }

    private boolean getBoolean(String path, boolean def) { return serverCfg.getBoolean(path, def); }
    private void setBoolean(String path, boolean val) { 
        serverCfg.set(path, val); 
        flushServerData();
    }

    public void saveAltarProgress(String key, Map<Material, Integer> deposited) {
        ConfigurationSection altarSec = serverCfg.getConfigurationSection("altars");
        if (altarSec == null) altarSec = serverCfg.createSection("altars");

        ConfigurationSection entry = altarSec.createSection(key);
        for (Map.Entry<Material, Integer> e : deposited.entrySet()) {
            entry.set(e.getKey().name(), e.getValue());
        }
        flushServerData();
    }

    public Map<Material, Integer> loadAltarProgress(String key) {
        Map<Material, Integer> deposited = new HashMap<>();
        ConfigurationSection altarSec = serverCfg.getConfigurationSection("altars." + key);
        if (altarSec != null) {
            for (String matName : altarSec.getKeys(false)) {
                try {
                    Material mat = Material.valueOf(matName);
                    deposited.put(mat, altarSec.getInt(matName));
                } catch (IllegalArgumentException ignored) {}
            }
        }
        return deposited;
    }

    public void removeAltarProgress(String key) {
        serverCfg.set("altars." + key, null);
        flushServerData();
    }
}
