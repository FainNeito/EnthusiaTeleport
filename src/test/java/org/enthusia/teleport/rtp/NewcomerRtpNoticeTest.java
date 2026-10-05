package org.enthusia.teleport.rtp;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.UUID;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;
import org.enthusia.teleport.EnthusiaTeleportPlugin;
import org.enthusia.teleport.config.PluginConfig;
import org.enthusia.teleport.config.PluginConfigManager;
import org.enthusia.teleport.util.Messages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

final class NewcomerRtpNoticeTest {
    private static final long FIRST = 1_000_000L;
    private NewcomerRtpNotice notice;
    private PluginConfig.RtpSettings settings;
    private Player player;
    private RtpManager rtp;

    @BeforeEach
    void setUp() {
        var plugin = mock(EnthusiaTeleportPlugin.class);
        var manager = mock(PluginConfigManager.class);
        var config = mock(PluginConfig.class);
        settings = mock(PluginConfig.RtpSettings.class);
        player = mock(Player.class);
        rtp = mock(RtpManager.class);
        var messages = mock(Messages.class);
        when(plugin.getPluginConfigManager()).thenReturn(manager);
        when(plugin.getRtpManager()).thenReturn(rtp);
        when(plugin.getMessages()).thenReturn(messages);
        when(messages.rawOr(anyString(), anyString())).thenAnswer(i -> i.getArgument(1));
        when(manager.current()).thenReturn(config);
        when(config.rtp()).thenReturn(settings);
        when(settings.enabled()).thenReturn(true);
        when(settings.newcomer()).thenReturn(new PluginConfig.NewcomerRtpSettings(true, 3, 86400));
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        when(player.getFirstPlayed()).thenReturn(FIRST);
        when(player.isOnline()).thenReturn(true);
        when(player.hasPermission("enthusia.teleport.rtp")).thenReturn(true);
        when(rtp.getLimit(player)).thenReturn(3);
        notice = new NewcomerRtpNotice(plugin);
    }

    @Test
    void remainingUsesUpdateInSameBarAndExhaustionRemovesIt() {
        notice.refresh(player, FIRST);
        var capture = ArgumentCaptor.forClass(BossBar.class);
        verify(player).showBossBar(capture.capture());
        BossBar bar = capture.getValue();
        assertTrue(text(bar).contains("3 left"));
        assertTrue(text(bar).contains("/rtp"));
        assertTrue(text(bar).contains("24h 0m"));
        when(rtp.getUses(player.getUniqueId())).thenReturn(1);
        notice.refresh(player, FIRST + 1000);
        assertTrue(text(bar).contains("2 left"));
        verify(player, times(1)).showBossBar(any());
        when(rtp.getUses(player.getUniqueId())).thenReturn(3);
        notice.refresh(player, FIRST + 2000);
        verify(player).hideBossBar(bar);
    }

    @Test
    void actualHigherAndUnlimitedLimitsAreNotAdvertisedAsThree() {
        when(rtp.getLimit(player)).thenReturn(10);
        when(rtp.getUses(player.getUniqueId())).thenReturn(2);
        notice.refresh(player, FIRST);
        var capture = ArgumentCaptor.forClass(BossBar.class);
        verify(player).showBossBar(capture.capture());
        assertTrue(text(capture.getValue()).contains("8 left"));
        when(rtp.getLimit(player)).thenReturn(-1);
        notice.refresh(player, FIRST + 1);
        assertTrue(text(capture.getValue()).contains("unlimited"));
    }

    @Test
    void permissionLossAndExactExpiryRemoveBar() {
        notice.refresh(player, FIRST);
        when(player.hasPermission("enthusia.teleport.rtp")).thenReturn(false);
        notice.refresh(player, FIRST + 1);
        verify(player).hideBossBar(any());
        when(player.hasPermission("enthusia.teleport.rtp")).thenReturn(true);
        notice.refresh(player, FIRST + 86_399_999L);
        var capture = ArgumentCaptor.forClass(BossBar.class);
        verify(player, times(2)).showBossBar(capture.capture());
        assertTrue(text(capture.getValue()).contains("1s"));
        notice.refresh(player, FIRST + 86_400_000L);
        verify(player, times(2)).hideBossBar(any());
    }

    @Test
    void disabledNoticeRtpPilotAndInvalidTimestampsNeverShowBar() {
        when(settings.newcomer()).thenReturn(new PluginConfig.NewcomerRtpSettings(true, 3, 86400, false));
        notice.refresh(player, FIRST);
        when(settings.newcomer()).thenReturn(new PluginConfig.NewcomerRtpSettings(false, 3, 86400));
        notice.refresh(player, FIRST);
        when(settings.newcomer()).thenReturn(new PluginConfig.NewcomerRtpSettings(true, 3, 86400));
        when(settings.enabled()).thenReturn(false);
        notice.refresh(player, FIRST);
        when(settings.enabled()).thenReturn(true);
        when(player.getFirstPlayed()).thenReturn(0L);
        notice.refresh(player, FIRST);
        when(player.getFirstPlayed()).thenReturn(FIRST + 1);
        notice.refresh(player, FIRST);
        verify(player, never()).showBossBar(any());
    }

    @Test
    void quitAndClearHideOnlyOwnedBarsAndReleaseDisplay() {
        notice.refresh(player, FIRST);
        var quit = mock(PlayerQuitEvent.class);
        when(quit.getPlayer()).thenReturn(player);
        notice.onQuit(quit);
        verify(player).hideBossBar(any());
        notice.refresh(player, FIRST + 1);
        notice.clear();
        notice.clear();
        verify(player, times(2)).hideBossBar(any());
    }

    private String text(BossBar bar) {
        return ChatColor.stripColor(LegacyComponentSerializer.legacySection().serialize(bar.name()));
    }

    @Test
    void reconnectWithSameUuidReplacesOldPlayerDisplayAndClearsNewOne() {
        notice.refresh(player, FIRST);
        var oldBar = ArgumentCaptor.forClass(BossBar.class);
        verify(player).showBossBar(oldBar.capture());
        var rejoined = mock(Player.class);
        UUID id = player.getUniqueId();
        when(rejoined.getUniqueId()).thenReturn(id);
        when(rejoined.getFirstPlayed()).thenReturn(FIRST);
        when(rejoined.isOnline()).thenReturn(true);
        when(rejoined.hasPermission("enthusia.teleport.rtp")).thenReturn(true);
        when(rtp.getLimit(rejoined)).thenReturn(3);
        notice.refresh(rejoined, FIRST + 1);
        verify(player).hideBossBar(oldBar.getValue());
        verify(rejoined).showBossBar(any());
        notice.clear();
        verify(rejoined).hideBossBar(any());
        verify(player, times(1)).hideBossBar(any());
    }
}
