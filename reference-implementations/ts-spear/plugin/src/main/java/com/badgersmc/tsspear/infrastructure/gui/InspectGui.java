package com.badgersmc.tsspear.infrastructure.gui;

import com.badgersmc.tsspear.application.TSSpearRuntime;
import com.badgersmc.tsspear.domain.service.ExplainabilityService;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

final class InspectGui {
    private InspectGui() {}

    static GuiManager.BuiltGui build(TSSpearRuntime runtime, Player target) {
        GuiSession session = GuiSession.inspect(target.getUniqueId(), target.getName());
        List<ItemStack> items = GuiManager.blankInventory(Material.GRAY_STAINED_GLASS_PANE);
        runtime.playerState(target.getUniqueId()).ifPresentOrElse(state -> {
            ExplainabilityService.ExplanationReport report = runtime.explainabilityService().explain(state);
            GuiManager.set(items, 0, GuiManager.item(Material.NETHER_STAR, "TS Inspector: " + target.getName(), List.of(
                "Suspicion: " + String.format("%.2f", report.suspicion().currentRisk()),
                "Trend: " + report.trust().trend(),
                "Ping: " + target.getPing() + "ms"
            )));
            int slot = 9;
            for (ExplainabilityService.DimensionExplanation dim : report.dimensions()) {
                if (dim.confidence() < 0.01 && dim.supporting().isEmpty()) continue;
                Material mat = confidenceMaterial(dim.confidence());
                List<String> lore = new ArrayList<>();
                lore.add("Net: " + String.format("%.2f", dim.netConfidence()));
                lore.add("Tension: " + String.format("%.2f", dim.tension()));
                dim.supporting().forEach(s -> lore.add("✓ " + s.kind()));
                dim.conflicting().forEach(c -> lore.add("✗ " + c.kind()));
                GuiManager.set(items, slot++, GuiManager.item(mat, dim.dimension().name(), lore));
                if (slot >= 45) break;
            }
            fillNav(items);
        }, () -> GuiManager.set(items, 0, GuiManager.item(Material.BARRIER, "No state", List.of())));
        return new GuiManager.BuiltGui(session, Component.text("TS Inspect — " + target.getName()), items);
    }

    private static Material confidenceMaterial(double c) {
        if (c >= 0.65) return Material.RED_CONCRETE;
        if (c >= 0.35) return Material.ORANGE_CONCRETE;
        return Material.GREEN_CONCRETE;
    }

    private static void fillNav(List<ItemStack> items) {
        for (int i = 0; i < 45; i++) {
            if (items.get(i).getType() == Material.GRAY_STAINED_GLASS_PANE) {
                items.set(i, new ItemStack(Material.BLACK_STAINED_GLASS_PANE));
            }
        }
        GuiManager.set(items, 45, GuiManager.item(Material.COMPASS, "Graph", List.of("Tension graph")));
        GuiManager.set(items, 46, GuiManager.item(Material.CLOCK, "Timeline", List.of("Receipt timeline")));
        GuiManager.set(items, 47, GuiManager.item(Material.PAPER, "Evidence", List.of("Evidence viewer")));
        GuiManager.set(items, 48, GuiManager.item(Material.ENDER_EYE, "Replay", List.of("Replay viewer")));
        GuiManager.set(items, 49, GuiManager.item(Material.PACKED_ICE, "Freeze", List.of("Continuous capture")));
        GuiManager.set(items, 50, GuiManager.item(Material.BOOK, "Export", List.of("ML evidence export")));
    }
}