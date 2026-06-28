package com.badgersmc.tsspear.infrastructure.gui;

import com.badgersmc.tsspear.application.TSSpearRuntime;
import com.badgersmc.tsspear.api.graph.GraphKind;
import com.badgersmc.tsspear.domain.model.graph.GraphEdge;
import com.badgersmc.tsspear.domain.model.graph.GraphNode;
import com.badgersmc.tsspear.domain.model.player.PlayerState;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

final class GraphGui {
    private GraphGui() {}

    static GuiManager.BuiltGui build(TSSpearRuntime runtime, Player target, GraphKind kind) {
        GuiSession session = GuiSession.graph(target.getUniqueId(), target.getName(), kind);
        List<ItemStack> items = GuiManager.blankInventory(Material.GRAY_STAINED_GLASS_PANE);
        GuiManager.set(items, 0, GuiManager.item(Material.COMPASS, kind.name() + " Graph", List.of(
            "Target: " + target.getName()
        )));
        runtime.playerState(target.getUniqueId()).ifPresent(state -> populate(state, kind, items));
        fillTabs(items, kind);
        return new GuiManager.BuiltGui(session, Component.text("TS Graph — " + target.getName()), items);
    }

    private static void fillTabs(List<ItemStack> items, GraphKind active) {
        GuiManager.set(items, 45, tabItem(GraphKind.EVIDENCE, active));
        GuiManager.set(items, 46, tabItem(GraphKind.CONSTRAINT, active));
        GuiManager.set(items, 47, tabItem(GraphKind.TENSION, active));
        GuiManager.set(items, 48, tabItem(GraphKind.RISK, active));
        GuiManager.set(items, 49, tabItem(GraphKind.HISTORY, active));
        GuiManager.set(items, 53, GuiManager.item(Material.ARROW, "Back", List.of("Inspector")));
    }

    private static ItemStack tabItem(GraphKind kind, GraphKind active) {
        Material mat = kind == active ? Material.LIME_STAINED_GLASS_PANE : Material.GRAY_STAINED_GLASS_PANE;
        return GuiManager.item(mat, kind.name(), List.of(kind == active ? "● Active" : "Switch graph"));
    }

    private static void populate(PlayerState state, GraphKind kind, List<ItemStack> items) {
        int slot = 9;
        for (GraphNode node : state.graph(kind).nodes()) {
            if (slot >= 45) break;
            List<String> lore = new ArrayList<>();
            lore.add(String.format("Activation: %.2f", node.activation()));
            lore.add(String.format("Confidence: %.2f", node.confidence()));
            lore.add(node.reason());
            GuiManager.set(items, slot++, GuiManager.item(Material.MAP, node.nodeId(), lore));
        }
        for (GraphEdge edge : state.graph(kind).edges()) {
            if (slot >= 45) break;
            GuiManager.set(items, slot++, GuiManager.item(Material.STRING, edge.kind().name(), List.of(
                edge.fromNodeId() + " → " + edge.toNodeId(),
                String.format("strength: %.2f", edge.strength())
            )));
        }
    }
}