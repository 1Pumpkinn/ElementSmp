package net.saturn.elementsmp.managers;

import net.saturn.elementsmp.ElementSmp;
import net.saturn.elementsmp.data.DataStore;
import net.saturn.elementsmp.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TrustManager {
    private final DataStore store;
    private final Map<UUID, Set<UUID>> pending = new ConcurrentHashMap<>(); // target -> requestors

    public TrustManager(ElementSmp plugin) {
        this.store = plugin.getDataStore();
    }

    public Set<UUID> getTrusted(UUID owner) {
        return store.getPlayerData(owner).getTrustedPlayers();
    }

    public boolean isTrusted(UUID owner, UUID other) {
        return store.getPlayerData(owner).isTrusted(other);
    }

    public void addTrust(UUID owner, UUID other) {
        PlayerData pd = store.getPlayerData(owner);
        pd.addTrustedPlayer(other);
        store.save(pd);
    }

    public void addMutualTrust(UUID a, UUID b) {
        addTrust(a, b);
        addTrust(b, a);
    }

    public void addPending(UUID target, UUID requestor) {
        pending.computeIfAbsent(target, k -> ConcurrentHashMap.newKeySet()).add(requestor);
    }

    public boolean hasPending(UUID target, UUID requestor) {
        return pending.getOrDefault(target, Set.of()).contains(requestor);
    }
    public void clearPending(UUID target, UUID requestor) {
        Set<UUID> set = pending.get(target);
        if (set != null) set.remove(requestor);
    }

    public void removeTrust(UUID owner, UUID other) {
        PlayerData pd = store.getPlayerData(owner);
        pd.removeTrustedPlayer(other);
        store.save(pd);
    }

    public void removeMutualTrust(UUID a, UUID b) {
        removeTrust(a, b);
        removeTrust(b, a);
    }

    public List<String> getTrustedNames(UUID owner) {
        List<String> out = new ArrayList<>();
        for (UUID u : getTrusted(owner)) {
            Player p = Bukkit.getPlayer(u);
            out.add(p != null ? p.getName() : u.toString());
        }
        return out;
    }
}
