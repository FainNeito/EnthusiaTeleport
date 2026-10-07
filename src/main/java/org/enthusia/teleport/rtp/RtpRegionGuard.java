package org.enthusia.teleport.rtp;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import java.util.List;
import java.util.Locale;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.enthusia.teleport.EnthusiaTeleportPlugin;

/** Main-thread adapter using authoritative live region geometry, not permission flags. */
public final class RtpRegionGuard {
    private final EnthusiaTeleportPlugin plugin;

    public RtpRegionGuard(EnthusiaTeleportPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean allows(Location location) {
        if (location == null || location.getWorld() == null) {
            return false;
        }
        List<String> ids = plugin.getConfig().isSet("rtp.excluded-regions")
                ? plugin.getConfig().getStringList("rtp.excluded-regions")
                : List.of("warzone", "spawn", "market");
        if (ids.isEmpty()) {
            return true;
        }
        Plugin provider = plugin.getServer().getPluginManager().getPlugin("WorldGuard");
        if (provider == null || !provider.isEnabled()) {
            return false;
        }
        try {
            RegionManager manager = WorldGuard.getInstance().getPlatform().getRegionContainer()
                    .get(BukkitAdapter.adapt(location.getWorld()));
            return allows(manager, location, ids);
        } catch (RuntimeException | LinkageError failure) {
            return false;
        }
    }

    static boolean allows(RegionManager manager, Location location, List<String> ids) {
        if (manager == null || location == null) {
            return false;
        }
        var point = com.sk89q.worldedit.math.BlockVector3.at(
                location.getBlockX(), location.getBlockY(), location.getBlockZ());
        for (String id : ids) {
            var region = manager.getRegion(id.toLowerCase(Locale.ROOT));
            if (region == null || region.contains(point)) {
                return false;
            }
        }
        return true;
    }
}
