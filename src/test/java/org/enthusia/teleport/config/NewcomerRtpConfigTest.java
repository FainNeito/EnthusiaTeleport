package org.enthusia.teleport.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.nio.file.Path;
import java.util.logging.Logger;
import org.bukkit.configuration.file.YamlConfiguration;
import org.enthusia.teleport.EnthusiaTeleportPlugin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class NewcomerRtpConfigTest {
    @TempDir Path directory;

    @Test
    void oldConfigKeepsItsFiniteLimitAndPilotOff() {
        var yaml = new YamlConfiguration();
        yaml.set("rtp.max-uses-default", 1);
        var rtp = parse(yaml);
        assertEquals(1, rtp.maxUsesDefault());
        assertFalse(rtp.newcomer().enabled());
        assertEquals(3, rtp.newcomer().maxUses());
        assertEquals(86400, rtp.newcomer().windowSeconds());
    }

    @Test
    void explicitPilotAndInvalidWindowAreParsedSafely() {
        var yaml = new YamlConfiguration();
        yaml.set("rtp.newcomer.enabled", true);
        yaml.set("rtp.newcomer.max-uses", 3);
        yaml.set("rtp.newcomer.window-seconds", Long.MAX_VALUE);
        var rtp = parse(yaml);
        assertTrue(rtp.newcomer().enabled());
        assertEquals(Long.MAX_VALUE / 1000L, rtp.newcomer().windowSeconds());
        yaml.set("rtp.newcomer.window-seconds", -1);
        yaml.set("rtp.newcomer.max-uses", -1);
        rtp = parse(yaml);
        assertEquals(0, rtp.newcomer().windowSeconds());
        assertEquals(0, rtp.newcomer().maxUses());
    }

    private PluginConfig.RtpSettings parse(YamlConfiguration yaml) {
        var plugin = mock(EnthusiaTeleportPlugin.class);
        when(plugin.getDataFolder()).thenReturn(directory.toFile());
        when(plugin.getLogger()).thenReturn(Logger.getLogger("newcomer-config-test"));
        when(plugin.getConfig()).thenReturn(yaml);
        return new PluginConfigManager(plugin).current().rtp();
    }
}
