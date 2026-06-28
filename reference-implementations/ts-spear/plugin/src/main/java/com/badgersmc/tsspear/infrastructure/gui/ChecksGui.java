package com.badgersmc.tsspear.infrastructure.gui;

import com.badgersmc.tsspear.api.check.CheckCategory;
import com.badgersmc.tsspear.application.TSSpearRuntime;
import com.badgersmc.tsspear.domain.check.Check;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

final class ChecksGui {
    private ChecksGui() {}

    static GuiManager.BuiltGui build(TSSpearRuntime runtime) {
        GuiSession session = GuiSession.checks();
        List<ItemStack> items = GuiManager.blankInventory(Material.GRAY_STAINED_GLASS_PANE);
        List<Check> checks = sortedChecks(runtime);

        GuiManager.set(items, 0, GuiManager.item(Material.WRITABLE_BOOK, "Check Registry", List.of(
            "Registered: " + checks.size(),
            "Click a check to toggle enabled/disabled"
        )));

        int slot = 9;
        CheckCategory lastCategory = null;
        for (Check check : checks) {
            if (slot >= 45) break;
            boolean enabled = runtime.checkEngine().isCheckEnabled(check.id().value());
            Material mat = enabled ? Material.LIME_DYE : Material.GRAY_DYE;
            List<String> lore = new ArrayList<>();
            lore.add("Category: " + check.category());
            lore.add("Dimension: " + check.dimension());
            lore.add("Status: " + (enabled ? "ENABLED" : "DISABLED"));
            if (lastCategory != null && lastCategory != check.category()) {
                lore.add("—");
            }
            lastCategory = check.category();
            GuiManager.set(items, slot++, GuiManager.item(mat, check.id().value(), lore));
        }

        GuiManager.set(items, 49, GuiManager.item(Material.BOOK, "Close", List.of("Close GUI")));
        return new GuiManager.BuiltGui(session, Component.text("TS Checks"), items);
    }

    static List<Check> sortedChecks(TSSpearRuntime runtime) {
        List<Check> checks = new ArrayList<>(runtime.checkEngine().registeredChecks());
        checks.sort(Comparator.comparing(c -> c.category().name() + c.id().value()));
        return checks;
    }
}