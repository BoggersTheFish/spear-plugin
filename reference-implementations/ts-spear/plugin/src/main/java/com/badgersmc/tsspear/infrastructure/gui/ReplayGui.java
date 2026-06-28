package com.badgersmc.tsspear.infrastructure.gui;

import com.badgersmc.tsspear.application.TSSpearRuntime;
import com.badgersmc.tsspear.domain.model.replay.ReplayFrame;
import com.badgersmc.tsspear.domain.model.replay.ReplaySequence;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

final class ReplayGui {
    private ReplayGui() {}

    static GuiManager.BuiltGui build(TSSpearRuntime runtime, Player target, UUID sequenceId, int frameIndex) {
        Optional<ReplaySequence> sequence = runtime.replayEngine().resolveSequence(
            target.getUniqueId(), target.getName(), sequenceId
        );
        GuiSession session = GuiSession.replay(target.getUniqueId(), target.getName(), sequenceId, frameIndex);
        List<ItemStack> items = GuiManager.blankInventory(Material.GRAY_STAINED_GLASS_PANE);

        if (sequence.isEmpty() || sequence.get().frames().isEmpty()) {
            GuiManager.set(items, 0, GuiManager.item(Material.BARRIER, "No replay data", List.of(
                "Target: " + target.getName()
            )));
            GuiManager.set(items, 49, GuiManager.item(Material.ARROW, "Back", List.of("Return to inspector")));
            return new GuiManager.BuiltGui(session, Component.text("TS Replay — " + target.getName()), items);
        }

        ReplaySequence replay = sequence.get();
        int index = Math.max(0, Math.min(frameIndex, replay.frames().size() - 1));
        ReplayFrame frame = replay.frames().get(index);
        double speed = frame.velocity().length();

        GuiManager.set(items, 0, GuiManager.item(Material.ENDER_EYE, "Replay — " + target.getName(), List.of(
            "Trigger: " + replay.triggerReason(),
            "Frame " + (index + 1) + " / " + replay.frames().size(),
            "Ticks " + replay.startTick() + " → " + replay.endTick(),
            runtime.replayEngine().isContinuousCapture(target.getUniqueId()) ? "● Continuous capture" : "Ring buffer"
        )));

        Material speedMat = speed > 0.65 ? Material.RED_WOOL : speed > 0.35 ? Material.ORANGE_WOOL : Material.LIME_WOOL;
        GuiManager.set(items, 9, GuiManager.item(speedMat, "Tick " + frame.tick(), List.of(
            String.format("Position: %.1f, %.1f, %.1f", frame.position().x(), frame.position().y(), frame.position().z()),
            String.format("Velocity: %.2f", speed),
            "On ground: " + frame.onGround(),
            "Ping: " + frame.pingMs() + "ms"
        )));

        paintPath(items, replay.frames(), index);

        GuiManager.set(items, 45, GuiManager.item(Material.ARROW, "◀ Prev", List.of()));
        GuiManager.set(items, 46, GuiManager.item(Material.SPECTRAL_ARROW, "▶ Next", List.of()));
        GuiManager.set(items, 47, GuiManager.item(Material.FEATHER, "⏪ -10", List.of()));
        GuiManager.set(items, 48, GuiManager.item(Material.FEATHER, "⏩ +10", List.of()));
        GuiManager.set(items, 49, GuiManager.item(Material.COMPASS, "Back", List.of("Inspector")));
        return new GuiManager.BuiltGui(
            new GuiSession(GuiType.REPLAY, target.getUniqueId(), target.getName(), 0, session.graphKind(), index, replay.sequenceId()),
            Component.text("TS Replay — " + target.getName()),
            items
        );
    }

    private static void paintPath(List<ItemStack> items, List<ReplayFrame> frames, int currentIndex) {
        int start = Math.max(0, currentIndex - 18);
        int slot = 18;
        for (int i = start; i < frames.size() && slot < 45; i++) {
            ReplayFrame f = frames.get(i);
            Material mat = i == currentIndex ? Material.BEACON : Material.LIGHT_BLUE_STAINED_GLASS_PANE;
            GuiManager.set(items, slot++, GuiManager.item(mat, "t" + f.tick(), List.of(
                String.format("%.1f, %.1f, %.1f", f.position().x(), f.position().y(), f.position().z())
            )));
        }
    }
}