package org.enthusia.teleport.command;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.enthusia.teleport.EnthusiaTeleportPlugin;
import org.enthusia.teleport.ignore.IgnoreManager;
import org.enthusia.teleport.util.Messages;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.enthusia.teleport.command.CommandStrings.ignoresEqualCase;

public class TpIgnoreCommand implements CommandExecutor {

    private final EnthusiaTeleportPlugin plugin;

    public TpIgnoreCommand(EnthusiaTeleportPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Messages msg = plugin.getMessages();

        if (!(sender instanceof Player player)) {
            msg.send(sender, "generic.no-console");
            return true;
        }

        IgnoreManager ignoreMgr = plugin.getIgnoreManager();

        if (args.length == 0) {
            player.sendMessage("§cUsage: /tpignore <player|list>");
            return true;
        }

        if (ignoresEqualCase(args[0], "list")) {
            Set<UUID> ignored = ignoreMgr.getIgnored(player.getUniqueId());
            if (ignored.isEmpty()) {
                msg.send(player, "ignore.list-empty");
                return true;
            }

            msg.send(player, "ignore.list-header");
            for (UUID id : ignored) {
                OfflinePlayer op = Bukkit.getOfflinePlayer(id);
                String name = op.getName() != null ? op.getName() : id.toString();
                msg.send(player, "ignore.list-entry", Map.of("player", name));
            }
            return true;
        }

        // /tpignore <player>
        String targetName = args[0];
        OfflinePlayer target = Bukkit.getPlayerExact(targetName);
        if (target == null) {
            target = Bukkit.getOfflinePlayerIfCached(targetName);
        }
        if (target == null || (!target.isOnline() && !target.hasPlayedBefore())) {
            msg.send(player, "ignore.unknown-player",
                    Map.of("target", targetName));
            return true;
        }

        if (target.getUniqueId().equals(player.getUniqueId())) {
            msg.send(player, "teleport.request.to-self");
            return true;
        }

        UUID receiverId = player.getUniqueId();
        UUID senderId = target.getUniqueId();

        boolean currentlyIgnoring = ignoreMgr.isIgnoring(receiverId, senderId);
        boolean nowIgnoring = !currentlyIgnoring;

        ignoreMgr.setIgnoring(receiverId, senderId, nowIgnoring);

        String displayName = target.getName() == null ? targetName : target.getName();
        msg.send(player, nowIgnoring ? "ignore.toggle-on" : "ignore.toggle-off", Map.of("target", displayName));

        return true;
    }
}
