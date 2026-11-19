package org.enthusia.teleport.rtp;

import static org.mockito.Mockito.*;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.enthusia.teleport.EnthusiaTeleportPlugin;
import org.enthusia.teleport.combat.CombatTagManager;
import org.enthusia.teleport.config.PluginConfig;
import org.enthusia.teleport.config.PluginConfigManager;
import org.enthusia.teleport.teleport.TeleportManager;
import org.enthusia.teleport.util.Messages;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

final class RtpWarmupRegionTest {
    @Test
    void regionChangeDuringWarmupBlocksTeleportAndSuccessfulUseCallback() {
        var plugin = mock(EnthusiaTeleportPlugin.class);
        var configManager = mock(PluginConfigManager.class);
        var config = mock(PluginConfig.class);
        var settings = mock(PluginConfig.TeleportSettings.class);
        var messages = mock(Messages.class);
        var player = mock(Player.class);
        var scheduler = mock(BukkitScheduler.class);
        var task = mock(BukkitTask.class);
        var success = mock(Runnable.class);
        var point = new Location(mock(World.class), 300, 100, 300);
        when(plugin.getMessages()).thenReturn(messages);
        when(plugin.getPluginConfigManager()).thenReturn(configManager);
        when(plugin.getCombatManager()).thenReturn(mock(CombatTagManager.class));
        when(configManager.current()).thenReturn(config);
        when(config.teleport()).thenReturn(settings);
        when(settings.warmupSeconds()).thenReturn(5.0);
        when(settings.blockedTargetWorlds()).thenReturn(Set.of());
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        when(player.getLocation()).thenReturn(point);
        when(player.isOnline()).thenReturn(true);
        when(scheduler.runTaskLater(eq(plugin), any(Runnable.class), eq(100L))).thenReturn(task);
        try (var bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(Bukkit::getScheduler).thenReturn(scheduler);
            var teleport = new TeleportManager(plugin);
            var allowed = new AtomicBoolean(true);
            teleport.startTeleportGuarded(player, point, false, null, "teleport.warmup-start",
                    success, ignored -> allowed.get());
            var capture = ArgumentCaptor.forClass(Runnable.class);
            verify(scheduler).runTaskLater(eq(plugin), capture.capture(), eq(100L));
            allowed.set(false);
            capture.getValue().run();
            verify(player, never()).teleport(any(Location.class));
            verify(success, never()).run();
            verify(messages).send(player, "teleport.safe-fallback-failed");
        }
    }
}
