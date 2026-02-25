package net.saturn.elementSmp.data;

import net.saturn.elementSmp.ElementSmp;
import net.saturn.elementSmp.elements.ElementType;
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
    private FileConfiguration playerCfg;
    private final File serverFile;
    private final FileConfiguration serverCfg;

    private final Map<UUID, PlayerData> playerDataCache = new HashMap<>();

    public PlayerData getPlayerData(UUID uuid) {
        // Check cache first
        if (playerDataCache.containsKey(uuid)) {
            return playerDataCache.get(uuid);
        }

        // Load from file if not in cache
        PlayerData data = loadPlayerDataFromFile(uuid);
        playerDataCache.put(uuid, data);
        return data;
    }

    private PlayerData loadPlayerDataFromFile(UUID uuid) {
        String uuidString = uuid.toString();
        ConfigurationSection section = null;
        ConfigurationSection playersSec = playerCfg.getConfigurationSection("players");
        if (playersSec != null) {
            for (String key : playersSec.getKeys(false)) {
                ConfigurationSection s = playersSec.getConfigurationSection(key);
                if (s == null) continue;
                String stored = s.getString("uuid");
                if (uuidString.equalsIgnoreCase(stored)) {
                    section = s;
                    break;
                }
            }
        }
        if (section == null) {
            section = playerCfg.getConfigurationSection("players." + uuidString);
        }
        if (section == null) {
            section = playerCfg.getConfigurationSection(uuidString);
        }
        if (section == null) {
            return new PlayerData(uuid);
        }

        PlayerData pd = new PlayerData(uuid, section);
        ConfigurationSection trustSection = section.getConfigurationSection("trust");
        if (trustSection != null) {
            Set<UUID> resolved = new HashSet<>(pd.getTrustedPlayers());
            for (String key : trustSection.getKeys(false)) {
                try {
                    resolved.add(UUID.fromString(key));
                } catch (IllegalArgumentException ex) {
                    org.bukkit.OfflinePlayer off = plugin.getServer().getOfflinePlayer(key);
                    if (off != null && off.getUniqueId() != null) {
                        resolved.add(off.getUniqueId());
                    }
                }
            }
            pd.setTrustedPlayers(resolved);
        }
        return pd;
    }

    public DataStore(ElementSmp plugin) {
        this.plugin = plugin;
        File dataDir = new File(plugin.getDataFolder(), "data");
        if (!dataDir.exists()) {
            if (!dataDir.mkdirs()) {
                plugin.getLogger().severe("Failed to create data directory: " + dataDir.getAbsolutePath());
                throw new RuntimeException("Could not create data directory");
            }
        }

        this.playerFile = new File(dataDir, "players.yml");
        if (!playerFile.exists()) {
            try {
                if (!playerFile.createNewFile()) {
                    plugin.getLogger().severe("Failed to create players.yml file");
                    throw new RuntimeException("Could not create players.yml file");
                }
            } catch (IOException e) {
                plugin.getLogger().severe("Error creating players.yml: " + e.getMessage());
                throw new RuntimeException("Could not create players.yml file", e);
            }
        }

        try {
            this.playerCfg = YamlConfiguration.loadConfiguration(playerFile);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to load players.yml configuration: " + e.getMessage());
            throw new RuntimeException("Could not load players.yml configuration", e);
        }

        this.serverFile = new File(dataDir, "server.yml");
        if (!serverFile.exists()) {
            try {
                if (!serverFile.createNewFile()) {
                    plugin.getLogger().severe("Failed to create server.yml file");
                    throw new RuntimeException("Could not create server.yml file");
                }
            } catch (IOException e) {
                plugin.getLogger().severe("Error creating server.yml: " + e.getMessage());
                throw new RuntimeException("Could not create server.yml file", e);
            }
        }

        try {
            this.serverCfg = YamlConfiguration.loadConfiguration(serverFile);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to load server.yml configuration: " + e.getMessage());
            throw new RuntimeException("Could not load server.yml configuration", e);
        }
    }

    public synchronized PlayerData load(UUID uuid) {
        return getPlayerData(uuid);
    }

    public synchronized void save(PlayerData pd) {
        try {
            String name = null;
            org.bukkit.OfflinePlayer off = plugin.getServer().getOfflinePlayer(pd.getUuid());
            if (off != null) {
                name = off.getName();
            }
            String keyName = name != null && !name.isEmpty() ? name : pd.getUuid().toString();
            String key = "players." + keyName;
            ConfigurationSection sec = playerCfg.getConfigurationSection(key);
            if (sec == null) sec = playerCfg.createSection(key);

            sec.set("uuid", pd.getUuid().toString());
            sec.set("element", pd.getCurrentElement() == null ? null : pd.getCurrentElement().name());
            sec.set("mana", pd.getMana());
            sec.set("currentUpgradeLevel", pd.getCurrentElementUpgradeLevel());

            // Save trust list
            sec.set("trust", null); // Clear existing
            if (!pd.getTrustedPlayers().isEmpty()) {
                ConfigurationSection trustSec = sec.createSection("trust");
                for (UUID trustedUuid : pd.getTrustedPlayers()) {
                    org.bukkit.OfflinePlayer t = plugin.getServer().getOfflinePlayer(trustedUuid);
                    String tName = t != null ? t.getName() : null;
                    String trustKey = tName != null && !tName.isEmpty() ? tName : trustedUuid.toString();
                    trustSec.set(trustKey, true);
                }
            }

            // Update cache
            playerDataCache.put(pd.getUuid(), pd);
            
            // CRITICAL: Flush to disk immediately after saving to config object
            flushPlayerData();
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save player data for " + pd.getUuid(), e);
        }
    }

    public synchronized void invalidateCache(UUID uuid) {
        playerDataCache.remove(uuid);
    }

    public synchronized void flushAll() {
        flushPlayerData();
    }

    private void flushPlayerData() {
        try {
            playerCfg.save(playerFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save players.yml to disk", e);
        }
    }

    // TRUST store - now delegates to PlayerData
    public synchronized Set<UUID> getTrusted(UUID owner) {
        PlayerData pd = getPlayerData(owner);
        return pd.getTrustedPlayers();
    }

    public synchronized void setTrusted(UUID owner, Set<UUID> trusted) {
        PlayerData pd = getPlayerData(owner);
        pd.setTrustedPlayers(trusted);
        save(pd);
    }


    private void flushServerData() {
        try {
            serverCfg.save(serverFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save server.yml to disk", e);
        }
    }

    public synchronized boolean getServerBoolean(String path, boolean def) {
        return serverCfg.getBoolean(path, def);
    }

    public synchronized void setServerBoolean(String path, boolean value) {
        serverCfg.set(path, value);
        flushServerData();
    }

    public synchronized boolean areAbilitiesEnabled() {
        return getServerBoolean("features.abilities_enabled", true);
    }

    public synchronized void setAbilitiesEnabled(boolean enabled) {
        setServerBoolean("features.abilities_enabled", enabled);
    }

    public synchronized boolean isElementRollEnabled() {
        return getServerBoolean("features.element_roll_enabled", true);
    }

    public synchronized void setElementRollEnabled(boolean enabled) {
        setServerBoolean("features.element_roll_enabled", enabled);
    }

    public synchronized boolean isElementEnabled(net.saturn.elementSmp.elements.ElementType type) {
        String path = "features.elements." + type.name().toLowerCase() + ".enabled";
        return getServerBoolean(path, true);
    }

    public synchronized void setElementEnabled(net.saturn.elementSmp.elements.ElementType type, boolean enabled) {
        String path = "features.elements." + type.name().toLowerCase() + ".enabled";
        setServerBoolean(path, enabled);
    }

    public synchronized boolean isRecipeEnabled(String name) {
        String path = "features.recipes." + name + ".enabled";
        return getServerBoolean(path, true);
    }

    public synchronized void setRecipeEnabled(String name, boolean enabled) {
        String path = "features.recipes." + name + ".enabled";
        setServerBoolean(path, enabled);
    }
}
