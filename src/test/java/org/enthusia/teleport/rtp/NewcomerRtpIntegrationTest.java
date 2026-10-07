package org.enthusia.teleport.rtp;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.enthusia.teleport.EnthusiaTeleportPlugin;
import org.enthusia.teleport.config.PluginConfig;
import org.enthusia.teleport.config.PluginConfigManager;
import org.enthusia.teleport.debug.PerformanceMonitor;
import org.enthusia.teleport.util.Messages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class NewcomerRtpIntegrationTest {
    @TempDir Path directory;
    private EnthusiaTeleportPlugin plugin;
    private PluginConfig.RtpSettings settings;
    private Player player;
    private final UUID id = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        plugin = mock(EnthusiaTeleportPlugin.class);
        var manager = mock(PluginConfigManager.class);
        var config = mock(PluginConfig.class);
        settings = mock(PluginConfig.RtpSettings.class);
        player = mock(Player.class);
        when(plugin.getDataFolder()).thenReturn(directory.toFile());
        when(plugin.getPluginConfigManager()).thenReturn(manager);
        when(plugin.getPerformanceMonitor()).thenReturn(new PerformanceMonitor(plugin));
        when(manager.current()).thenReturn(config);
        when(config.rtp()).thenReturn(settings);
        when(settings.maxUsesDefault()).thenReturn(1);
        when(settings.rankLimits()).thenReturn(Map.of());
        when(settings.newcomer()).thenReturn(new PluginConfig.NewcomerRtpSettings(true, 3, 86400));
        when(player.getUniqueId()).thenReturn(id);
        when(player.getFirstPlayed()).thenReturn(System.currentTimeMillis() - 60_000L);
    }

    @Test
    void samePersistentCounterLimitsNewcomerAndSurvivesReconstruction() throws Exception {
        Files.writeString(directory.resolve("rtp_uses.yml"), id + ": 2\n");
        var rtp = new RtpManager(plugin);
        assertEquals(3, rtp.getLimit(player));
        assertTrue(rtp.canUse(player));
        rtp.incrementUse(id);
        assertFalse(rtp.canUse(player));
        rtp.flushBlocking();
        var restored = new RtpManager(plugin);
        assertEquals(3, restored.getUses(id));
        assertFalse(restored.canUse(player));
        when(settings.newcomer()).thenReturn(new PluginConfig.NewcomerRtpSettings(false, 3, 86400));
        assertEquals(1, restored.getLimit(player));
        assertEquals(3, restored.getUses(id));
        when(settings.newcomer()).thenReturn(new PluginConfig.NewcomerRtpSettings(true, 3, 86400));
        assertFalse(restored.canUse(player));
    }

    @Test
    void rankAndExpiryAreAppliedByActualManager() {
        var rtp = new RtpManager(plugin);
        when(settings.rankLimits()).thenReturn(Map.of("enthusia.rtp.10", 10));
        when(player.hasPermission("enthusia.rtp.10")).thenReturn(true);
        assertEquals(10, rtp.getLimit(player));
        when(player.hasPermission("enthusia.rtp.10")).thenReturn(false);
        when(player.getFirstPlayed()).thenReturn(System.currentTimeMillis() - 86_400_001L);
        assertEquals(1, rtp.getLimit(player));
    }

    @Test
    void legacyConstructorKeepsPilotDisabled() {
        var legacy = new PluginConfig.RtpSettings(true, "world", -5000, 5000, -5000, 5000,
                1, Map.of(), 30, null, null, null);
        assertFalse(legacy.newcomer().enabled());
    }

    @Test
    void queuedSearchRechecksExhaustedQuotaBeforeTouchingDestination() throws Exception {
        Files.writeString(directory.resolve("rtp_uses.yml"), id + ": 3\n");
        var rtp = new RtpManager(plugin);
        var messages = mock(Messages.class);
        when(plugin.getMessages()).thenReturn(messages);
        when(player.isOnline()).thenReturn(true);
        var search = new RtpManager.RtpSearch(id, System.currentTimeMillis());
        rtp.validateCandidate(search, player, null, 0, 0, settings);
        verify(messages).send(player, "rtp.limit-reached", Map.of("limit", "3"));
        verify(plugin, never()).getTeleportManager();
        assertEquals(3, rtp.getUses(id));
    }
}
