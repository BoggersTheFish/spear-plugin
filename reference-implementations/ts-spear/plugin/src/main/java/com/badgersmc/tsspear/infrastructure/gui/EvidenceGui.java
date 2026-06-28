package com.badgersmc.tsspear.infrastructure.gui;

import com.badgersmc.tsspear.application.TSSpearRuntime;
import com.badgersmc.tsspear.domain.model.evidence.Evidence;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

final class EvidenceGui {
    private static final int PAGE_SIZE = 28;

    private EvidenceGui() {}

    static GuiManager.BuiltGui build(TSSpearRuntime runtime, Player target, int page) {
        GuiSession session = GuiSession.evidence(target.getUniqueId(), target.getName(), page);
        List<Evidence> all = runtime.evidenceStore().findByPlayer(target.getUniqueId(), 200);
        int from = page * PAGE_SIZE;
        List<ItemStack> items = GuiManager.blankInventory(Material.GRAY_STAINED_GLASS_PANE);
        GuiManager.set(items, 0, GuiManager.item(Material.BOOK, "Evidence — " + target.getName(), List.of(
            "Page " + (page + 1),
            "Total loaded: " + all.size()
        )));
        int slot = 9;
        for (int i = from; i < Math.min(from + PAGE_SIZE, all.size()) && slot < 45; i++) {
            Evidence e = all.get(i);
            GuiManager.set(items, slot++, GuiManager.item(Material.PAPER, e.kind().name(), List.of(
                String.format("Weight: %.2f", e.weight()),
                e.reason()
            )));
        }
        GuiManager.set(items, 45, GuiManager.item(Material.ARROW, "Previous", List.of()));
        GuiManager.set(items, 53, GuiManager.item(Material.ARROW, "Next", List.of()));
        return new GuiManager.BuiltGui(session, Component.text("TS Evidence — " + target.getName()), items);
    }
}