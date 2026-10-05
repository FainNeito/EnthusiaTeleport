package org.enthusia.teleport.rtp;

import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.enthusia.teleport.EnthusiaTeleportPlugin;

/** Main-thread prompts only: never creates a home or invokes a command directly. */
public final class SurvivalOnboarding {
    private final EnthusiaTeleportPlugin plugin;

    public SurvivalOnboarding(EnthusiaTeleportPlugin plugin) {
        this.plugin = plugin;
    }

    public void firstJoin(Player player) {
        if (!enabled() || !player.isOnline() || !player.hasPermission("enthusia.teleport.rtp")
                || !plugin.getPluginConfigManager().current().rtp().enabled()
                || !plugin.getRtpManager().canUse(player)) {
            return;
        }
        send(player, "onboarding.start-survival",
                "&a&l[Start Survival] &fUse /rtp to find wilderness and build a home.",
                ClickEvent.runCommand("/rtp"));
    }

    public void firstSuccessfulRtp(Player player, long now) {
        long first = player.getFirstPlayed();
        if (!enabled() || !player.isOnline() || first <= 0 || now < first || now - first >= 86_400_000L
                || !player.hasPermission("enthusia.teleport.sethome")
                || plugin.getHomeManager().getHomeCount(player.getUniqueId()) != 0
                || plugin.getHomeManager().getHomeLimit(player) <= 0) {
            return;
        }
        send(player, "onboarding.first-home",
                "&aFound a place you like? &fUse &e/sethome base &fto save it, then &e/home base &fto return. &7You can explore first.",
                ClickEvent.suggestCommand("/sethome base"));
    }

    private boolean enabled() {
        return plugin.getConfig().getBoolean("onboarding.enabled", false);
    }

    private void send(Player player, String key, String fallback, ClickEvent click) {
        String message = plugin.getMessages().rawOr(key, fallback);
        if (!message.isBlank()) {
            player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(message).clickEvent(click));
        }
    }
}
