package org.enthusia.teleport.home;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.UUID;
import java.util.List;

public class HomeGuiHolder implements InventoryHolder {

    private final UUID owner;

    record Destination(String name, String world, double x, double y, double z, boolean bed) {}
    final List<Destination> visible;
    final int page;
    final boolean hasNext;

    public HomeGuiHolder(UUID owner) {
        this(owner, List.of(), 0, false);
    }

    HomeGuiHolder(UUID owner, List<Destination> visible, int page, boolean hasNext) {
        this.visible = List.copyOf(visible);
        this.page = page;
        this.hasNext = hasNext;
        this.owner = owner;
    }

    public UUID getOwner() {
        return owner;
    }

    @Override
    public Inventory getInventory() {
        return null; // will be set by Bukkit when inventory is created
    }
}
