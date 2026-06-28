package com.badgersmc.tsspear.infrastructure.command;

import com.badgersmc.tsspear.api.graph.GraphKind;
import com.badgersmc.tsspear.application.TSSpearRuntime;
import com.badgersmc.tsspear.api.ml.AnnotationVerdict;
import com.badgersmc.tsspear.application.ml.ExportTarget;
import com.badgersmc.tsspear.application.ml.InferenceResult;
import com.badgersmc.tsspear.application.service.ExportService;
import com.badgersmc.tsspear.domain.model.evidence.Evidence;
import com.badgersmc.tsspear.domain.model.player.PlayerState;
import com.badgersmc.tsspear.domain.model.replay.ReplayFrame;
import com.badgersmc.tsspear.domain.model.replay.ReplaySequence;
import com.badgersmc.tsspear.domain.model.replay.ReplayTriggerReason;
import com.badgersmc.tsspear.domain.service.ExplainabilityService;
import com.badgersmc.tsspear.infrastructure.gui.GuiManager;
import com.badgersmc.tsspear.infrastructure.i18n.MessageService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public final class TSSpearCommand implements CommandExecutor, TabCompleter {
    private static final DateTimeFormatter EXPORT_STAMP =
        DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").withZone(ZoneOffset.UTC);

    private final TSSpearRuntime runtime;
    private final GuiManager guiManager;
    private final MessageService messages;
    private final Path dataFolder;

    public TSSpearCommand(TSSpearRuntime runtime, GuiManager guiManager, MessageService messages, Path dataFolder) {
        this.runtime = runtime;
        this.guiManager = guiManager;
        this.messages = messages;
        this.dataFolder = dataFolder;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("tsspear.use")) {
            sender.sendMessage(messages.component("command.no-permission", Map.of(), NamedTextColor.RED));
            return true;
        }
        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        return switch (sub) {
            case "inspect" -> handleInspect(sender, args);
            case "trust" -> handleTrust(sender, args);
            case "suspicion" -> handleSuspicion(sender, args);
            case "checks" -> handleChecks(sender, args);
            case "evidence" -> handleEvidence(sender, args);
            case "replay" -> handleReplay(sender, args);
            case "graph" -> handleGraph(sender, args);
            case "timeline" -> handleTimeline(sender, args);
            case "export" -> handleExport(sender, args);
            case "annotate" -> handleAnnotate(sender, args);
            case "infer" -> handleInfer(sender, args);
            case "freeze" -> handleFreeze(sender, args);
            case "unfreeze" -> handleUnfreeze(sender, args);
            case "alerts" -> handleAlerts(sender, args);
            case "debug" -> handleDebug(sender, args);
            default -> {
                sendUsage(sender);
                yield true;
            }
        };
    }

    private boolean handleInspect(CommandSender sender, String[] args) {
        if (!sender.hasPermission("tsspear.inspect")) {
            sender.sendMessage(Component.text("No permission.", NamedTextColor.RED));
            return true;
        }
        boolean textMode = args.length > 1 && "text".equalsIgnoreCase(args[1]);
        int targetIndex = textMode ? 2 : 1;
        Player target = resolveTarget(sender, args, targetIndex);
        if (target == null) {
            return true;
        }
        if (sender instanceof Player staff && !textMode) {
            guiManager.openInspect(staff, target);
            return true;
        }
        return sendTextInspect(sender, target);
    }

    private boolean sendTextInspect(CommandSender sender, Player target) {
        Optional<PlayerState> state = runtime.playerState(target.getUniqueId());
        if (state.isEmpty()) {
            sender.sendMessage(Component.text("No TS state for " + target.getName(), NamedTextColor.YELLOW));
            return true;
        }
        ExplainabilityService.ExplanationReport report = runtime.explainabilityService().explain(state.get());
        sender.sendMessage(Component.text("── TS-Spear Inspector: " + report.playerName() + " ──", NamedTextColor.GOLD));
        sender.sendMessage(Component.text(String.format(
            "Suspicion: %.2f  Trend: %s  Δ: %.2f",
            report.suspicion().currentRisk(),
            report.trust().trend(),
            report.suspicion().delta()
        ), NamedTextColor.GRAY));

        for (ExplainabilityService.DimensionExplanation dim : report.dimensions()) {
            if (dim.confidence() < 0.01 && dim.supporting().isEmpty()) {
                continue;
            }
            sender.sendMessage(Component.text(String.format(
                "%s — confidence %.2f, net %.2f, tension %.2f",
                dim.dimension(), dim.confidence(), dim.netConfidence(), dim.tension()
            ), NamedTextColor.AQUA));
            for (ExplainabilityService.EvidenceSummary s : dim.supporting()) {
                sender.sendMessage(Component.text("  ✓ " + s.kind() + ": " + s.reason()
                    + String.format(" (%.2f)", s.weight()), NamedTextColor.GREEN));
            }
            for (ExplainabilityService.EvidenceSummary c : dim.conflicting()) {
                sender.sendMessage(Component.text("  ✗ " + c.kind() + ": " + c.reason()
                    + String.format(" (%.2f)", c.weight()), NamedTextColor.BLUE));
            }
        }
        return true;
    }

    private boolean handleTrust(CommandSender sender, String[] args) {
        if (!sender.hasPermission("tsspear.trust")) {
            sender.sendMessage(Component.text("No permission.", NamedTextColor.RED));
            return true;
        }
        Player target = resolveTarget(sender, args, 1);
        if (target == null) {
            return true;
        }
        runtime.playerState(target.getUniqueId()).ifPresentOrElse(state -> {
            sender.sendMessage(Component.text(String.format(
                "%s trust — net confidence: %.2f, trend: %s",
                target.getName(),
                state.trust().netConfidence(),
                state.trust().trend()
            ), NamedTextColor.GREEN));
        }, () -> sender.sendMessage(Component.text("No state.", NamedTextColor.YELLOW)));
        return true;
    }

    private boolean handleSuspicion(CommandSender sender, String[] args) {
        if (!sender.hasPermission("tsspear.suspicion")) {
            sender.sendMessage(Component.text("No permission.", NamedTextColor.RED));
            return true;
        }
        Player target = resolveTarget(sender, args, 1);
        if (target == null) {
            return true;
        }
        runtime.playerState(target.getUniqueId()).ifPresentOrElse(state -> {
            sender.sendMessage(Component.text(String.format(
                "%s suspicion — risk: %.2f (was %.2f), trend: %s",
                target.getName(),
                state.suspicion().currentRisk(),
                state.suspicion().previousRisk(),
                state.suspicion().trend()
            ), NamedTextColor.RED));
        }, () -> sender.sendMessage(Component.text("No state.", NamedTextColor.YELLOW)));
        return true;
    }

    private boolean handleChecks(CommandSender sender, String[] args) {
        if (!sender.hasPermission("tsspear.checks")) {
            sender.sendMessage(messages.component("command.no-permission", Map.of(), NamedTextColor.RED));
            return true;
        }
        if (args.length >= 3 && "toggle".equalsIgnoreCase(args[1])) {
            String checkId = args[2].toLowerCase(Locale.ROOT);
            boolean next = !runtime.checkEngine().isCheckEnabled(checkId);
            runtime.checkEngine().setCheckEnabled(checkId, next);
            sender.sendMessage(Component.text(checkId + " → " + (next ? "enabled" : "disabled"), NamedTextColor.GREEN));
            return true;
        }
        if (sender instanceof Player staff) {
            guiManager.openChecks(staff);
            return true;
        }
        sender.sendMessage(Component.text("── Registered Checks ──", NamedTextColor.GOLD));
        runtime.checkEngine().registeredChecks().forEach(check ->
            sender.sendMessage(Component.text(
                check.id().value() + " [" + check.category() + " → " + check.dimension() + "] "
                    + (runtime.checkEngine().isCheckEnabled(check.id().value()) ? "✓" : "✗"),
                NamedTextColor.GRAY
            ))
        );
        return true;
    }

    private boolean handleEvidence(CommandSender sender, String[] args) {
        if (!sender.hasPermission("tsspear.evidence")) {
            sender.sendMessage(Component.text("No permission.", NamedTextColor.RED));
            return true;
        }
        boolean textMode = args.length > 1 && "text".equalsIgnoreCase(args[1]);
        int targetIndex = textMode ? 2 : 1;
        Player target = resolveTarget(sender, args, targetIndex);
        if (target == null) {
            return true;
        }
        if (sender instanceof Player staff && !textMode) {
            guiManager.openEvidence(staff, target, 0);
            return true;
        }
        List<Evidence> evidence = runtime.evidenceStore().findByPlayer(target.getUniqueId(), 10);
        if (evidence.isEmpty()) {
            sender.sendMessage(Component.text("No persisted evidence for " + target.getName(), NamedTextColor.YELLOW));
            return true;
        }
        sender.sendMessage(Component.text("── Evidence: " + target.getName() + " ──", NamedTextColor.GOLD));
        for (Evidence e : evidence) {
            sender.sendMessage(Component.text(String.format(
                "%s [%.2f] %s", e.kind(), e.weight(), e.reason()
            ), NamedTextColor.GRAY));
        }
        return true;
    }

    private boolean handleReplay(CommandSender sender, String[] args) {
        if (!sender.hasPermission("tsspear.replay")) {
            sender.sendMessage(messages.component("command.no-permission", Map.of(), NamedTextColor.RED));
            return true;
        }
        Player target = resolveTarget(sender, args, 1);
        if (target == null) {
            return true;
        }
        if (sender instanceof Player staff) {
            int last = Math.max(0, runtime.replayEngine().liveBuffer(target.getUniqueId()).size() - 1);
            guiManager.openReplay(staff, target, null, last);
            return true;
        }
        var replay = runtime.replayEngine().snapshot(
            target.getUniqueId(),
            target.getName(),
            ReplayTriggerReason.STAFF_REQUEST
        ).or(() -> runtime.replayEngine().latestStored(target.getUniqueId()));

        if (replay.isEmpty()) {
            sender.sendMessage(Component.text("No replay data for " + target.getName(), NamedTextColor.YELLOW));
            return true;
        }
        ReplaySequence sequence = replay.get();
        sender.sendMessage(Component.text("── Replay: " + sequence.playerName() + " ──", NamedTextColor.GOLD));
        sender.sendMessage(Component.text(String.format(
            "Frames: %d  Ticks: %d → %d  Trigger: %s",
            sequence.frames().size(),
            sequence.startTick(),
            sequence.endTick(),
            sequence.triggerReason()
        ), NamedTextColor.GRAY));
        int show = Math.min(5, sequence.frames().size());
        for (int i = sequence.frames().size() - show; i < sequence.frames().size(); i++) {
            ReplayFrame frame = sequence.frames().get(i);
            sender.sendMessage(Component.text(String.format(
                "  tick %d @ %.1f,%.1f,%.1f vel=%.2f ground=%s ping=%d",
                frame.tick(),
                frame.position().x(), frame.position().y(), frame.position().z(),
                frame.velocity().length(),
                frame.onGround(),
                frame.pingMs()
            ), NamedTextColor.DARK_AQUA));
        }
        return true;
    }

    private boolean handleGraph(CommandSender sender, String[] args) {
        if (!sender.hasPermission("tsspear.graph")) {
            sender.sendMessage(Component.text("No permission.", NamedTextColor.RED));
            return true;
        }
        if (!(sender instanceof Player staff)) {
            sender.sendMessage(Component.text("GUI commands require a player.", NamedTextColor.RED));
            return true;
        }
        Player target = resolveTarget(sender, args, 1);
        if (target == null) {
            return true;
        }
        GraphKind kind = GraphKind.TENSION;
        if (args.length > 2) {
            try {
                kind = GraphKind.valueOf(args[2].toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ignored) {
                sender.sendMessage(Component.text("Unknown graph kind: " + args[2], NamedTextColor.YELLOW));
            }
        }
        guiManager.openGraph(staff, target, kind);
        return true;
    }

    private boolean handleTimeline(CommandSender sender, String[] args) {
        if (!sender.hasPermission("tsspear.timeline")) {
            sender.sendMessage(Component.text("No permission.", NamedTextColor.RED));
            return true;
        }
        if (!(sender instanceof Player staff)) {
            sender.sendMessage(Component.text("GUI commands require a player.", NamedTextColor.RED));
            return true;
        }
        Player target = resolveTarget(sender, args, 1);
        if (target == null) {
            return true;
        }
        guiManager.openTimeline(staff, target);
        return true;
    }

    private boolean handleExport(CommandSender sender, String[] args) {
        if (!sender.hasPermission("tsspear.export")) {
            sender.sendMessage(Component.text("No permission.", NamedTextColor.RED));
            return true;
        }
        boolean jsonl = args.length > 1 && "jsonl".equalsIgnoreCase(args[1]);
        int targetIndex = jsonl ? 2 : 1;
        Player target = resolveTarget(sender, args, targetIndex);
        if (target == null) {
            return true;
        }
        Optional<PlayerState> state = runtime.playerState(target.getUniqueId());
        if (state.isEmpty()) {
            sender.sendMessage(Component.text("No TS state for " + target.getName(), NamedTextColor.YELLOW));
            return true;
        }
        String stamp = EXPORT_STAMP.format(Instant.now());
        String extension = jsonl ? "jsonl" : "json";
        Path exportPath = dataFolder.resolve("exports")
            .resolve(target.getName() + "-" + stamp + "." + extension);
        ExportTarget.ExportFormat format = jsonl
            ? ExportTarget.ExportFormat.JSONL
            : ExportTarget.ExportFormat.JSON;
        ExportService.PathResult result = runtime.exportService().exportPlayer(
            state.get(),
            new ExportTarget(exportPath, format),
            200
        );
        sender.sendMessage(Component.text(
            "Exported ML evidence batch " + result.exportId() + " → " + result.path(),
            NamedTextColor.GREEN
        ));
        return true;
    }

    private boolean handleAnnotate(CommandSender sender, String[] args) {
        if (!sender.hasPermission("tsspear.annotate")) {
            sender.sendMessage(Component.text("No permission.", NamedTextColor.RED));
            return true;
        }
        if (args.length < 3) {
            sender.sendMessage(Component.text(
                "Usage: /ts annotate <player> <LEGIT|SUSPICIOUS|CHEATING|INCONCLUSIVE> [notes]",
                NamedTextColor.YELLOW
            ));
            return true;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(Component.text("Player not found: " + args[1], NamedTextColor.RED));
            return true;
        }
        AnnotationVerdict verdict;
        try {
            verdict = AnnotationVerdict.parse(args[2]);
        } catch (IllegalArgumentException ex) {
            sender.sendMessage(Component.text("Invalid verdict: " + args[2], NamedTextColor.RED));
            return true;
        }
        String notes = args.length > 3
            ? String.join(" ", java.util.Arrays.copyOfRange(args, 3, args.length))
            : "";
        String staffName = sender.getName();
        var annotation = runtime.annotationService().annotate(
            target.getUniqueId(),
            staffName,
            verdict,
            notes,
            null
        );
        sender.sendMessage(Component.text(String.format(
            "Annotated %s as %s (id %s)",
            target.getName(),
            verdict.name(),
            annotation.annotationId()
        ), NamedTextColor.GREEN));
        return true;
    }

    private boolean handleInfer(CommandSender sender, String[] args) {
        if (!sender.hasPermission("tsspear.infer")) {
            sender.sendMessage(Component.text("No permission.", NamedTextColor.RED));
            return true;
        }
        Player target = resolveTarget(sender, args, 1);
        if (target == null) {
            return true;
        }
        Optional<PlayerState> state = runtime.playerState(target.getUniqueId());
        if (state.isEmpty()) {
            sender.sendMessage(Component.text("No TS state for " + target.getName(), NamedTextColor.YELLOW));
            return true;
        }
        InferenceResult result = runtime.inferenceService().infer(state.get(), null);
        if (result.abstain()) {
            sender.sendMessage(Component.text(
                "Inference abstained [" + result.providerId() + "]: " + result.reason(),
                NamedTextColor.YELLOW
            ));
            return true;
        }
        sender.sendMessage(Component.text(String.format(
            "── Inference: %s [%s] ──",
            target.getName(),
            result.providerId()
        ), NamedTextColor.GOLD));
        sender.sendMessage(Component.text(String.format(
            "Label: %s  Confidence: %.2f",
            result.label(),
            result.confidence()
        ), result.label().equals("CHEATING") ? NamedTextColor.RED : NamedTextColor.AQUA));
        sender.sendMessage(Component.text("Reason: " + result.reason(), NamedTextColor.GRAY));
        return true;
    }

    private @Nullable Player resolveTarget(CommandSender sender, String[] args, int index) {
        if (args.length > index) {
            Player target = Bukkit.getPlayer(args[index]);
            if (target == null) {
                sender.sendMessage(Component.text("Player not found: " + args[index], NamedTextColor.RED));
                return null;
            }
            return target;
        }
        if (sender instanceof Player player) {
            return player;
        }
        sender.sendMessage(Component.text("Specify a player.", NamedTextColor.RED));
        return null;
    }

    private boolean handleFreeze(CommandSender sender, String[] args) {
        if (!sender.hasPermission("tsspear.freeze")) {
            sender.sendMessage(messages.component("command.no-permission", Map.of(), NamedTextColor.RED));
            return true;
        }
        Player target = resolveTarget(sender, args, 1);
        if (target == null) {
            return true;
        }
        if (runtime.freezeService().isFrozen(target.getUniqueId())) {
            sender.sendMessage(messages.component("freeze.already-frozen",
                Map.of("player", target.getName()), NamedTextColor.YELLOW));
            return true;
        }
        runtime.freezeService().freeze(target.getUniqueId(), target.getName());
        sender.sendMessage(messages.component("freeze.enabled",
            Map.of("player", target.getName()), NamedTextColor.AQUA));
        return true;
    }

    private boolean handleUnfreeze(CommandSender sender, String[] args) {
        if (!sender.hasPermission("tsspear.freeze")) {
            sender.sendMessage(messages.component("command.no-permission", Map.of(), NamedTextColor.RED));
            return true;
        }
        Player target = resolveTarget(sender, args, 1);
        if (target == null) {
            return true;
        }
        if (!runtime.freezeService().unfreeze(target.getUniqueId())) {
            sender.sendMessage(messages.component("freeze.not-frozen",
                Map.of("player", target.getName()), NamedTextColor.YELLOW));
            return true;
        }
        sender.sendMessage(messages.component("freeze.disabled",
            Map.of("player", target.getName()), NamedTextColor.GREEN));
        return true;
    }

    private boolean handleAlerts(CommandSender sender, String[] args) {
        if (!sender.hasPermission("tsspear.alerts")) {
            sender.sendMessage(messages.component("command.no-permission", Map.of(), NamedTextColor.RED));
            return true;
        }
        if (!(sender instanceof Player staff)) {
            sender.sendMessage(messages.component("command.gui-requires-player", Map.of(), NamedTextColor.RED));
            return true;
        }
        if (args.length >= 2) {
            boolean enabled = "on".equalsIgnoreCase(args[1]) || "enable".equalsIgnoreCase(args[1]);
            runtime.staffPreferences().setAlertsEnabled(staff.getUniqueId(), enabled);
            sender.sendMessage(messages.component(enabled ? "alerts.enabled" : "alerts.disabled",
                Map.of(), NamedTextColor.GREEN));
            return true;
        }
        boolean enabled = runtime.staffPreferences().alertsEnabled(staff.getUniqueId());
        sender.sendMessage(messages.component(enabled ? "alerts.status-on" : "alerts.status-off",
            Map.of(), NamedTextColor.GRAY));
        return true;
    }

    private boolean handleDebug(CommandSender sender, String[] args) {
        if (!sender.hasPermission("tsspear.debug")) {
            sender.sendMessage(messages.component("command.no-permission", Map.of(), NamedTextColor.RED));
            return true;
        }
        if (args.length >= 2 && "profile".equalsIgnoreCase(args[1])) {
            var snap = runtime.performanceMetrics().snapshot();
            sender.sendMessage(Component.text("── TS-Spear Performance Profile ──", NamedTextColor.GOLD));
            sender.sendMessage(Component.text(String.format(
                "Checks: %d invocations, avg %.2f µs",
                snap.checkInvocations(), snap.avgCheckNanos() / 1000.0
            ), NamedTextColor.GRAY));
            sender.sendMessage(Component.text(String.format(
                "Graph propagation: %d calls, avg %.2f µs",
                snap.graphPropagationCalls(), snap.avgGraphPropagationNanos() / 1000.0
            ), NamedTextColor.GRAY));
            sender.sendMessage(Component.text(String.format(
                "Storage queue: %d depth, %d dropped, %d active workers",
                snap.storageQueueDepth(), snap.storageDroppedTasks(), snap.activeWorkerTasks()
            ), NamedTextColor.GRAY));
            sender.sendMessage(Component.text(String.format(
                "Registered checks: %d, pooling: %s",
                runtime.checkEngine().registeredChecks().size(),
                runtime.settings().objectPoolingEnabled() ? "ON" : "OFF"
            ), NamedTextColor.AQUA));
            return true;
        }
        Player target = resolveTarget(sender, args, 1);
        if (target == null) {
            return true;
        }
        runtime.playerState(target.getUniqueId()).ifPresentOrElse(state -> {
            sender.sendMessage(Component.text("── Debug: " + target.getName() + " ──", NamedTextColor.GOLD));
            sender.sendMessage(Component.text(String.format(
                "Version: %d  Suspicion: %.2f  Trust net: %.2f",
                state.version(),
                state.suspicion().currentRisk(),
                state.trust().netConfidence()
            ), NamedTextColor.GRAY));
            sender.sendMessage(Component.text(String.format(
                "Evidence recent: %d  Movement samples: %s  Packets: %d",
                state.recentEvidence().size(),
                state.movement().lastSample() == null ? "none" : "yes",
                state.networking().recentPackets().size()
            ), NamedTextColor.DARK_AQUA));
        }, () -> sender.sendMessage(messages.component("inspect.no-state",
            Map.of("player", target.getName()), NamedTextColor.YELLOW)));
        return true;
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(Component.text(
            "Usage: /ts <inspect|trust|suspicion|evidence|replay|graph|timeline|export|annotate|infer|freeze|unfreeze|alerts|checks|debug> [player]",
            NamedTextColor.YELLOW
        ));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return filter(List.of(
                "inspect", "trust", "suspicion", "evidence", "replay",
                "graph", "timeline", "export", "annotate", "infer",
                "freeze", "unfreeze", "alerts", "checks", "debug"
            ), args[0]);
        }
        if (args.length == 2) {
            String sub = args[0].toLowerCase(Locale.ROOT);
            if ("alerts".equals(sub)) {
                return filter(List.of("on", "off"), args[1]);
            }
            if ("debug".equals(sub)) {
                List<String> options = new ArrayList<>();
                options.add("profile");
                options.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList());
                return filter(options, args[1]);
            }
            if ("export".equals(sub)) {
                List<String> options = new ArrayList<>();
                options.add("jsonl");
                options.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList());
                return filter(options, args[1]);
            }
            if ("annotate".equals(sub)) {
                return filter(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList(), args[1]);
            }
            if ("checks".equals(sub)) {
                return filter(List.of("toggle"), args[1]);
            }
            if ("inspect".equals(sub) || "evidence".equals(sub)) {
                List<String> options = new ArrayList<>();
                options.add("text");
                options.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList());
                return filter(options, args[1]);
            }
            if ("graph".equals(sub)) {
                List<String> options = new ArrayList<>(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList());
                return filter(options, args[1]);
            }
            return filter(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList(), args[1]);
        }
        if (args.length == 3 && "annotate".equalsIgnoreCase(args[0])) {
            return filter(
                java.util.Arrays.stream(AnnotationVerdict.values()).map(Enum::name).toList(),
                args[2]
            );
        }
        if (args.length == 3 && "checks".equalsIgnoreCase(args[0]) && "toggle".equalsIgnoreCase(args[1])) {
            return filter(runtime.checkEngine().registeredChecks().stream().map(c -> c.id().value()).toList(), args[2]);
        }
        if (args.length == 3 && "graph".equalsIgnoreCase(args[0])) {
            return filter(
                java.util.Arrays.stream(GraphKind.values()).map(Enum::name).map(String::toLowerCase).toList(),
                args[2]
            );
        }
        return List.of();
    }

    private List<String> filter(List<String> options, String prefix) {
        String lower = prefix.toLowerCase(Locale.ROOT);
        return options.stream()
            .filter(o -> o.toLowerCase(Locale.ROOT).startsWith(lower))
            .collect(Collectors.toCollection(ArrayList::new));
    }
}