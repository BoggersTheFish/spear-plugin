package com.badgersmc.tsspear.infrastructure.gui;

import com.badgersmc.tsspear.application.TSSpearRuntime;
import com.badgersmc.tsspear.application.service.TimelineService;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

final class TimelineGui {
    private TimelineGui() {}

    static GuiManager.BuiltGui build(TSSpearRuntime runtime, Player target) {
        GuiSession session = GuiSession.timeline(target.getUniqueId(), target.getName());
        TimelineService.TimelineReport report = runtime.timelineService().build(target.getUniqueId(), 45);
        List<ItemStack> items = GuiManager.blankInventory(Material.GRAY_STAINED_GLASS_PANE);
        GuiManager.set(items, 0, GuiManager.item(Material.CLOCK, "Timeline — " + target.getName(), List.of(
            "Entries: " + report.entries().size()
        )));
        int slot = 9;
        for (TimelineService.TimelineEntry entry : report.entries()) {
            if (slot >= 45) break;
            GuiManager.set(items, slot++, GuiManager.item(Material.PAPER, entry.dimension().name(), List.of(
                entry.event(),
                String.format("%.2f → %.2f", entry.previous(), entry.current()),
                entry.reason()
            )));
        }
        return new GuiManager.BuiltGui(session, Component.text("TS Timeline — " + target.getName()), items);
    }
}