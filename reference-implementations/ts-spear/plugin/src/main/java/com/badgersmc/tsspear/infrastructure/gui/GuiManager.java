package com.badgersmc.tsspear.infrastructure.gui;

import com.badgersmc.tsspear.api.graph.GraphKind;
import com.badgersmc.tsspear.application.TSSpearRuntime;
import com.badgersmc.tsspear.domain.check.Check;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class GuiManager {
    private final TSSpearRuntime runtime;

    public GuiManager(TSSpearRuntime runtime) {
        this.runtime = runtime;
    }

    public void openInspect(Player staff, Player target) {
        open(staff, InspectGui.build(runtime, target));
    }

    public void openEvidence(Player staff, Player target, int page) {
        open(staff, EvidenceGui.build(runtime, target, page));
    }

    public void openTimeline(Player staff, Player target) {
        open(staff, TimelineGui.build(runtime, target));
    }

    public void openGraph(Player staff, Player target, GraphKind kind) {
        open(staff, GraphGui.build(runtime, target, kind));
    }

    public void openReplay(Player staff, Player target, UUID sequenceId, int frameIndex) {
        open(staff, ReplayGui.build(runtime, target, sequenceId, frameIndex));
    }

    public void openChecks(Player staff) {
        open(staff, ChecksGui.build(runtime));
    }

    public void handleClick(Player staff, GuiSession session, int slot) {
        switch (session.type()) {
            case CHECKS -> handleChecksClick(staff, session, slot);
            case REPLAY -> handleReplayClick(staff, session, slot);
            case GRAPH -> handleGraphClick(staff, session, slot);
            default -> handleTargetedClick(staff, session, slot);
        }
    }

    private void handleTargetedClick(Player staff, GuiSession session, int slot) {
        if (session.targetId() == null) {
            return;
        }
        Optional<Player> target = Optional.ofNullable(Bukkit.getPlayer(session.targetId()));
        if (target.isEmpty()) {
            staff.sendMessage(Component.text("Target offline."));
            return;
        }
        switch (session.type()) {
            case INSPECT -> handleInspectNav(staff, target.get(), slot);
            case EVIDENCE -> {
                if (slot == 45) openEvidence(staff, target.get(), Math.max(0, session.page() - 1));
                if (slot == 53) openEvidence(staff, target.get(), session.page() + 1);
            }
            default -> {}
        }
    }

    private void handleInspectNav(Player staff, Player target, int slot) {
        if (slot == 45) openGraph(staff, target, GraphKind.TENSION);
        if (slot == 46) openTimeline(staff, target);
        if (slot == 47) openEvidence(staff, target, 0);
        if (slot == 48) openReplay(staff, target, null, Math.max(0, runtime.replayEngine().liveBuffer(target.getUniqueId()).size() - 1));
        if (slot == 49) staff.performCommand("ts freeze " + target.getName());
        if (slot == 50) staff.performCommand("ts export " + target.getName());
    }

    private void handleReplayClick(Player staff, GuiSession session, int slot) {
        if (session.targetId() == null) {
            return;
        }
        Player target = Bukkit.getPlayer(session.targetId());
        if (target == null) {
            staff.sendMessage(Component.text("Target offline."));
            return;
        }
        int frame = session.replayFrameIndex();
        if (slot == 45) openReplay(staff, target, session.replaySequenceId(), Math.max(0, frame - 1));
        if (slot == 46) openReplay(staff, target, session.replaySequenceId(), frame + 1);
        if (slot == 47) openReplay(staff, target, session.replaySequenceId(), Math.max(0, frame - 10));
        if (slot == 48) openReplay(staff, target, session.replaySequenceId(), frame + 10);
        if (slot == 49) openInspect(staff, target);
    }

    private void handleGraphClick(Player staff, GuiSession session, int slot) {
        if (session.targetId() == null) {
            return;
        }
        Player target = Bukkit.getPlayer(session.targetId());
        if (target == null) {
            staff.sendMessage(Component.text("Target offline."));
            return;
        }
        GraphKind kind = graphTabKind(slot);
        if (kind != null) {
            openGraph(staff, target, kind);
        }
        if (slot == 53) openInspect(staff, target);
    }

    private void handleChecksClick(Player staff, GuiSession session, int slot) {
        if (slot >= 9 && slot < 45) {
            List<Check> checks = ChecksGui.sortedChecks(runtime);
            int index = slot - 9;
            if (index < checks.size()) {
                Check check = checks.get(index);
                boolean next = !runtime.checkEngine().isCheckEnabled(check.id().value());
                runtime.checkEngine().setCheckEnabled(check.id().value(), next);
                staff.sendMessage(Component.text(check.id().value() + " → " + (next ? "enabled" : "disabled")));
                openChecks(staff);
            }
        }
    }

    private static GraphKind graphTabKind(int slot) {
        return switch (slot) {
            case 45 -> GraphKind.EVIDENCE;
            case 46 -> GraphKind.CONSTRAINT;
            case 47 -> GraphKind.TENSION;
            case 48 -> GraphKind.RISK;
            case 49 -> GraphKind.HISTORY;
            default -> null;
        };
    }

    private void open(Player staff, BuiltGui built) {
        GuiHolder holder = new GuiHolder(built.session());
        Inventory inv = Bukkit.createInventory(holder, 54, built.title());
        holder.bind(inv);
        for (int i = 0; i < built.items().size() && i < 54; i++) {
            inv.setItem(i, built.items().get(i));
        }
        staff.openInventory(inv);
    }

    static List<ItemStack> blankInventory(Material filler) {
        ItemStack pane = new ItemStack(filler);
        List<ItemStack> items = new ArrayList<>(54);
        for (int i = 0; i < 54; i++) {
            items.add(pane.clone());
        }
        return items;
    }

    static void set(List<ItemStack> items, int slot, ItemStack stack) {
        if (slot >= 0 && slot < items.size()) {
            items.set(slot, stack);
        }
    }

    static ItemStack item(Material material, String name, List<String> lore) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(Component.text(name));
        if (lore != null && !lore.isEmpty()) {
            meta.lore(lore.stream().map(Component::text).toList());
        }
        stack.setItemMeta(meta);
        return stack;
    }

    public record BuiltGui(GuiSession session, Component title, List<ItemStack> items) {}
}