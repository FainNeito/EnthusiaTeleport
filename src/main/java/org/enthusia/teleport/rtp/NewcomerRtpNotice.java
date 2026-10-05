package org.enthusia.teleport.rtp;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.enthusia.teleport.EnthusiaTeleportPlugin;
import org.enthusia.teleport.config.PluginConfig;

/** Main-thread UI adapter. Never writes protection state or the shared action bar. */
public final class NewcomerRtpNotice implements Listener {
    private final EnthusiaTeleportPlugin plugin;
    private final Map<UUID, Display> displays = new HashMap<>();

    public NewcomerRtpNotice(EnthusiaTeleportPlugin plugin) {
        this.plugin = plugin;
    }

    public void tick() {
        var online = new HashSet<UUID>();
        long now = System.currentTimeMillis();
        for (Player player : Bukkit.getOnlinePlayers()) {
            online.add(player.getUniqueId());
            refresh(player, now);
        }
        for (UUID id : new HashSet<>(displays.keySet())) {
            if (!online.contains(id)) {
                remove(id);
            }
        }
    }

    void refresh(Player player, long now) {
        PluginConfig.RtpSettings settings = plugin.getPluginConfigManager().current().rtp();
        PluginConfig.NewcomerRtpSettings newcomer = settings.newcomer();
        long first = player.getFirstPlayed();
        boolean eligible = player.isOnline() && player.hasPermission("enthusia.teleport.rtp")
                && settings.enabled() && newcomer != null && newcomer.enabled()
                && newcomer.bossBarEnabled() && newcomer.maxUses() > 0
                && newcomer.windowSeconds() > 0 && newcomer.windowSeconds() <= Long.MAX_VALUE / 1000L
                && first > 0 && now >= first && now - first < newcomer.windowSeconds() * 1000L;
        if (!eligible) {
            remove(player.getUniqueId());
            return;
        }
        int limit = plugin.getRtpManager().getLimit(player);
        int remaining = Math.max(0, limit - plugin.getRtpManager().getUses(player.getUniqueId()));
        if (limit >= 0 && remaining == 0) {
            remove(player.getUniqueId());
            return;
        }
        long millis = newcomer.windowSeconds() * 1000L - (now - first);
        long seconds = millis / 1000L + (millis % 1000L == 0 ? 0 : 1);
        String time = seconds >= 3600 ? (seconds / 3600) + "h " + ((seconds % 3600) / 60) + "m"
                : seconds >= 60 ? (seconds / 60) + "m " + (seconds % 60) + "s" : seconds + "s";
        String template = plugin.getMessages().rawOr("rtp.newcomer-boss-bar",
                "&aWilderness RTPs: &f{remaining} &7| &e/rtp &7| &fNewcomer window: {time}");
        if (template.isBlank()) {
            remove(player.getUniqueId());
            return;
        }
        var title = LegacyComponentSerializer.legacyAmpersand().deserialize(template
                .replace("{remaining}", limit < 0 ? "unlimited" : remaining + " left")
                .replace("{time}", time));
        Display display = displays.get(player.getUniqueId());
        if (display != null && display.player() != player) {
            remove(player.getUniqueId());
            display = null;
        }
        if (display == null) {
            BossBar bar = BossBar.bossBar(title, 1.0f, BossBar.Color.GREEN, BossBar.Overlay.PROGRESS);
            displays.put(player.getUniqueId(), new Display(player, bar));
            player.showBossBar(bar);
        } else {
            display.bar().name(title);
        }
    }

    private void remove(UUID id) {
        Display display = displays.remove(id);
        if (display != null) {
            display.player().hideBossBar(display.bar());
        }
    }

    public void clear() {
        for (UUID id : new HashSet<>(displays.keySet())) {
            remove(id);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        remove(event.getPlayer().getUniqueId());
    }

    private record Display(Player player, BossBar bar) { }
}
