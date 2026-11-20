package org.enthusia.teleport.command;

import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.enthusia.teleport.EnthusiaTeleportPlugin;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class TpIgnoreOfflineTest {
    private final EnthusiaTeleportPlugin plugin = mock(EnthusiaTeleportPlugin.class, RETURNS_DEEP_STUBS);
    private final Player receiver = mock(Player.class);
    private final UUID owner = UUID.randomUUID();
    private final UUID sender = UUID.randomUUID();

    @Test
    void offlineToggleUsesTheSameUuidInBothDirections() {
        when(receiver.getUniqueId()).thenReturn(owner);
        OfflinePlayer offline = mock(OfflinePlayer.class);
        when(offline.getUniqueId()).thenReturn(sender);
        when(offline.getName()).thenReturn("Traveler");
        when(offline.hasPlayedBefore()).thenReturn(true);
        when(plugin.getIgnoreManager().isIgnoring(owner, sender)).thenReturn(false, true);
        try (var server = mockStatic(Bukkit.class)) {
            server.when(() -> Bukkit.getOfflinePlayerIfCached("traveler")).thenReturn(offline);
            var command = new TpIgnoreCommand(plugin);
            command.onCommand(receiver, null, "tpignore", new String[]{"traveler"});
            command.onCommand(receiver, null, "tpignore", new String[]{"traveler"});
            verify(plugin.getIgnoreManager()).setIgnoring(owner, sender, true);
            verify(plugin.getIgnoreManager()).setIgnoring(owner, sender, false);
            command.onCommand(receiver, null, "tpignore", new String[]{"Typo"});
            verify(plugin.getIgnoreManager(), times(2)).setIgnoring(any(), any(), anyBoolean());
        }
    }

    @Test
    void completionIncludesOfflineNames() {
        Command command = mock(Command.class);
        when(command.getName()).thenReturn("tpignore");
        when(receiver.getName()).thenReturn("Tester");
        when(plugin.getOfflineNameCache().suggest("tra", false)).thenReturn(List.of("Traveler"));
        try (var server = mockStatic(Bukkit.class)) {
            assertEquals(List.of("Traveler"), new TeleportTabCompleter(plugin)
                    .onTabComplete(receiver, command, "tpignore", new String[]{"Tra"}));
        }
    }
}
