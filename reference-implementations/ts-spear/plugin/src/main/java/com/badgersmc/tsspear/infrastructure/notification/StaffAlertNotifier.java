package com.badgersmc.tsspear.infrastructure.notification;

import com.badgersmc.tsspear.application.TSSpearRuntime;
import com.badgersmc.tsspear.application.engine.NotificationEngine.RichAlertPayload;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

public final class StaffAlertNotifier {
    private final TSSpearRuntime runtime;

    public StaffAlertNotifier(TSSpearRuntime runtime) {
        this.runtime = runtime;
    }

    public void notify(RichAlertPayload payload) {
        Component message = buildMessage(payload);
        for (Player staff : Bukkit.getOnlinePlayers()) {
            if (staff.hasPermission("tsspear.inspect")
                && runtime.staffPreferences().alertsEnabled(staff.getUniqueId())) {
                staff.sendMessage(message);
            }
        }
    }

    private static Component buildMessage(RichAlertPayload payload) {
        NamedTextColor dimColor = confidenceColor(payload.confidence());
        Component header = Component.text("[TS-Spear] ", NamedTextColor.GOLD)
            .append(Component.text(payload.playerName(), NamedTextColor.YELLOW, TextDecoration.BOLD))
            .append(Component.text(" — ", NamedTextColor.GRAY))
            .append(Component.text(payload.dimension().name(), dimColor))
            .append(Component.text(String.format(" %.0f%%", payload.confidence() * 100), dimColor));

        Component risk = Component.text(String.format(
            "Risk %.2f (Δ%.2f) · Net %.2f · Trend %s",
            payload.currentRisk(),
            payload.riskDelta(),
            payload.netConfidence(),
            payload.trend()
        ), NamedTextColor.GRAY);

        Component reason = Component.text(payload.reason(), NamedTextColor.WHITE);

        Component actions = Component.text()
            .append(action("[Inspect]", "/ts inspect " + payload.playerName(), "Open inspector"))
            .append(action(" [Replay]", "/ts replay " + payload.playerName(), "View replay"))
            .append(action(" [Freeze]", "/ts freeze " + payload.playerName(), "Start continuous capture"))
            .build();

        Component body = Component.text()
            .append(header)
            .append(Component.newline())
            .append(risk)
            .append(Component.newline())
            .append(reason)
            .append(formatEvidenceList("Supporting", payload.supportingEvidence(), NamedTextColor.GREEN))
            .append(formatEvidenceList("Conflicting", payload.conflictingEvidence(), NamedTextColor.BLUE))
            .append(Component.newline())
            .append(actions)
            .build();

        return body;
    }

    private static Component action(String label, String command, String hover) {
        return Component.text(label, NamedTextColor.AQUA, TextDecoration.UNDERLINED)
            .clickEvent(ClickEvent.runCommand(command))
            .hoverEvent(HoverEvent.showText(Component.text(hover)));
    }

    private static Component formatEvidenceList(String label, List<String> items, NamedTextColor color) {
        if (items.isEmpty()) {
            return Component.empty();
        }
        Component line = Component.text(label + ": ", NamedTextColor.DARK_GRAY);
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) {
                line = line.append(Component.text(", ", NamedTextColor.DARK_GRAY));
            }
            line = line.append(Component.text(items.get(i), color));
        }
        return Component.newline().append(line);
    }

    private static NamedTextColor confidenceColor(double confidence) {
        if (confidence >= 0.80) return NamedTextColor.RED;
        if (confidence >= 0.65) return NamedTextColor.GOLD;
        if (confidence >= 0.35) return NamedTextColor.YELLOW;
        return NamedTextColor.GREEN;
    }
}