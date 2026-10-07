package org.enthusia.teleport.rtp;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.enthusia.teleport.EnthusiaTeleportPlugin;
import org.junit.jupiter.api.Test;

final class RtpRegionGuardTest {
    private static final String WARZONE = "warzone";
    private static final String MARKET = "market";
    private final List<String> ids = List.of(WARZONE, "spawn", MARKET);

    private RegionManager regions() {
        var manager = mock(RegionManager.class);
        when(manager.getRegion(WARZONE)).thenReturn(new ProtectedCuboidRegion(WARZONE,
                BlockVector3.at(-219, -64, -405), BlockVector3.at(219, 319, 189)));
        when(manager.getRegion("spawn")).thenReturn(new ProtectedCuboidRegion("spawn",
                BlockVector3.at(-49, 78, -34), BlockVector3.at(69, 319, 84)));
        when(manager.getRegion(MARKET)).thenReturn(new ProtectedCuboidRegion(MARKET,
                BlockVector3.at(-73, -64, -282), BlockVector3.at(102, 319, -163)));
        return manager;
    }

    @Test
    void productionGeometryExcludesAllThreeAndIncludesBoundaryBlocks() {
        var manager = regions();
        assertFalse(RtpRegionGuard.allows(manager, new Location(null, 0, 100, 0), ids));
        assertFalse(RtpRegionGuard.allows(manager, new Location(null, 80, 120, -200), ids));
        assertFalse(RtpRegionGuard.allows(manager, new Location(null, -219, -64, -405), ids));
        assertFalse(RtpRegionGuard.allows(manager, new Location(null, 219.99, 319, 189.99), ids));
        assertTrue(RtpRegionGuard.allows(manager, new Location(null, 220, 100, 190), ids));
        assertTrue(RtpRegionGuard.allows(manager, new Location(null, -219.01, 100, 0), ids));
    }

    @Test
    void liveResizeAndSeparateRegionShapesAreAppliedWithoutPriorityBypass() {
        var manager = regions();
        when(manager.getRegion(WARZONE)).thenReturn(new ProtectedCuboidRegion(WARZONE,
                BlockVector3.at(1000, -64, 1000), BlockVector3.at(1100, 319, 1100)));
        assertFalse(RtpRegionGuard.allows(manager, new Location(null, 0, 100, 0), ids));
        assertFalse(RtpRegionGuard.allows(manager, new Location(null, 80, 120, -200), ids));
        assertFalse(RtpRegionGuard.allows(manager, new Location(null, 1050, 100, 1050), ids));
        assertTrue(RtpRegionGuard.allows(manager, new Location(null, 300, 100, 300), ids));
    }

    @Test
    void unavailableManagerOrMissingNamedRegionFailsClosed() {
        var point = new Location(null, 300, 100, 300);
        assertFalse(RtpRegionGuard.allows(null, point, ids));
        var manager = regions();
        when(manager.getRegion(MARKET)).thenReturn(null);
        assertFalse(RtpRegionGuard.allows(manager, point, ids));
    }

    @Test
    void missingProviderBlocksRtpWithoutLoadingWorldGuardAndExplicitEmptyListOptsOut() {
        var plugin = mock(EnthusiaTeleportPlugin.class);
        var server = mock(Server.class);
        var providers = mock(PluginManager.class);
        var config = new YamlConfiguration();
        when(plugin.getConfig()).thenReturn(config);
        when(plugin.getServer()).thenReturn(server);
        when(server.getPluginManager()).thenReturn(providers);
        var guard = new RtpRegionGuard(plugin);
        var point = new Location(mock(World.class), 300, 100, 300);
        assertFalse(guard.allows(point));
        config.set("rtp.excluded-regions", List.of());
        assertTrue(guard.allows(point));
        assertFalse(guard.allows(null));
    }
}
