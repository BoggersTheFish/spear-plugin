package com.badgersmc.tsspear.infrastructure.i18n;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public final class MessageService {
    private final Map<String, String> messages = new HashMap<>();

    public MessageService(JavaPlugin plugin, String locale) {
        loadDefaults(plugin, locale);
        var file = plugin.getDataFolder().toPath().resolve("messages").resolve(locale + ".yml");
        if (file.toFile().exists()) {
            FileConfiguration custom = YamlConfiguration.loadConfiguration(file.toFile());
            for (String key : custom.getKeys(true)) {
                if (custom.isString(key)) {
                    messages.put(key, custom.getString(key, key));
                }
            }
        }
    }

    private void loadDefaults(JavaPlugin plugin, String locale) {
        String resource = "messages/" + locale + ".yml";
        try (InputStream in = plugin.getResource(resource)) {
            if (in == null) {
                return;
            }
            FileConfiguration yaml = YamlConfiguration.loadConfiguration(
                new InputStreamReader(in, StandardCharsets.UTF_8)
            );
            for (String key : yaml.getKeys(true)) {
                if (yaml.isString(key)) {
                    messages.put(key, yaml.getString(key, key));
                }
            }
        } catch (Exception ignored) {
            // Fall back to keys as text
        }
    }

    public Component component(String key, Map<String, String> params, NamedTextColor color) {
        return Component.text(format(key, params), color);
    }

    public String format(String key, Map<String, String> params) {
        String template = messages.getOrDefault(key, key);
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                template = template.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }
        return template;
    }

    public String raw(String key) {
        return messages.getOrDefault(key, key);
    }
}