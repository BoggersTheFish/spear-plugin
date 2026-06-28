package com.badgersmc.tsspear.infrastructure.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class GuiHolder implements InventoryHolder {
    private final GuiSession session;
    private Inventory inventory;

    public GuiHolder(GuiSession session) {
        this.session = session;
    }

    public GuiSession session() {
        return session;
    }

    void bind(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}