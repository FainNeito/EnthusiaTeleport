package org.enthusia.teleport.rtp;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.enthusia.teleport.EnthusiaTeleportPlugin;
import org.enthusia.teleport.config.PluginConfig;
import org.enthusia.teleport.config.PluginConfigManager;
import org.enthusia.teleport.debug.PerformanceMonitor;
import org.enthusia.teleport.home.HomeManager;
import org.enthusia.teleport.util.Messages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;

final class SurvivalOnboardingTest {
    @TempDir Path directory;
    private EnthusiaTeleportPlugin plugin;
    private Player player;
    private HomeManager homes;
    private YamlConfiguration yaml;
    private RtpManager rtp;
    private SurvivalOnboarding onboarding;

    @BeforeEach
    void setup() {
        plugin = mock(EnthusiaTeleportPlugin.class);
        player = mock(Player.class);
        homes = mock(HomeManager.class);
        yaml = new YamlConfiguration();
        yaml.set("onboarding.enabled", true);
        var configManager = mock(PluginConfigManager.class);
        var config = mock(PluginConfig.class);
        var settings = mock(PluginConfig.RtpSettings.class);
        var messages = mock(Messages.class);
        when(plugin.getConfig()).thenReturn(yaml);
        when(plugin.getDataFolder()).thenReturn(directory.toFile());
        when(plugin.getPerformanceMonitor()).thenReturn(mock(PerformanceMonitor.class));
        when(plugin.getHomeManager()).thenReturn(homes);
        when(plugin.getPluginConfigManager()).thenReturn(configManager);
        when(configManager.current()).thenReturn(config);
        when(config.rtp()).thenReturn(settings);
        when(settings.enabled()).thenReturn(true);
        when(settings.maxUsesDefault()).thenReturn(3);
        when(settings.rankLimits()).thenReturn(Map.of());
        when(plugin.getMessages()).thenReturn(messages);
        when(messages.rawOr(anyString(), anyString())).thenAnswer(i -> i.getArgument(1));
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        when(player.isOnline()).thenReturn(true);
        when(player.getFirstPlayed()).thenReturn(System.currentTimeMillis() - 1000);
        when(player.hasPermission(anyString())).thenReturn(true);
        when(homes.getHomeLimit(player)).thenReturn(1);
        rtp = new RtpManager(plugin);
        when(plugin.getRtpManager()).thenReturn(rtp);
        onboarding = new SurvivalOnboarding(plugin);
    }

    @Test
    void successfulUsePromptsOnlyOnceAndPersistentCounterPreventsRepeatAfterReconstruction() {
        rtp.recordSuccessfulRtp(player);
        var capture = ArgumentCaptor.forClass(Component.class);
        verify(player).sendMessage(capture.capture());
        assertEquals(ClickEvent.suggestCommand("/sethome base"), capture.getValue().clickEvent());
        assertEquals(1, rtp.getUses(player.getUniqueId()));
        rtp.flushBlocking();
        new RtpManager(plugin).recordSuccessfulRtp(player);
        verify(player, times(1)).sendMessage(any(Component.class));
        verify(homes, never()).setHome(any(), anyString());
    }

    @Test
    void existingHomeAndMissingPermissionSuppressGuidance() {
        when(homes.getHomeCount(player.getUniqueId())).thenReturn(1);
        onboarding.firstSuccessfulRtp(player, System.currentTimeMillis());
        when(homes.getHomeCount(player.getUniqueId())).thenReturn(0);
        when(player.hasPermission("enthusia.teleport.sethome")).thenReturn(false);
        onboarding.firstSuccessfulRtp(player, System.currentTimeMillis());
        when(player.hasPermission("enthusia.teleport.sethome")).thenReturn(true);
        when(homes.getHomeLimit(player)).thenReturn(0);
        onboarding.firstSuccessfulRtp(player, System.currentTimeMillis());
        verify(player, never()).sendMessage(any(Component.class));
    }

    @Test
    void disabledExpiredUnknownAndFuturePlayersReceiveNoHomePrompt() {
        long now = System.currentTimeMillis();
        when(player.getFirstPlayed()).thenReturn(now - 86_400_000L);
        onboarding.firstSuccessfulRtp(player, now);
        when(player.getFirstPlayed()).thenReturn(0L);
        onboarding.firstSuccessfulRtp(player, now);
        when(player.getFirstPlayed()).thenReturn(now + 1);
        onboarding.firstSuccessfulRtp(player, now);
        when(player.getFirstPlayed()).thenReturn(now - 1);
        yaml.set("onboarding.enabled", false);
        onboarding.firstSuccessfulRtp(player, now);
        verify(player, never()).sendMessage(any(Component.class));
    }

    @Test
    void startActionUsesExistingRtpCommandAndRespectsEntitlement() {
        onboarding.firstJoin(player);
        var capture = ArgumentCaptor.forClass(Component.class);
        verify(player).sendMessage(capture.capture());
        assertEquals(ClickEvent.runCommand("/rtp"), capture.getValue().clickEvent());
        when(player.hasPermission("enthusia.teleport.rtp")).thenReturn(false);
        onboarding.firstJoin(player);
        verify(player, times(1)).sendMessage(any(Component.class));
        verify(player, never()).performCommand(anyString());
    }
}
