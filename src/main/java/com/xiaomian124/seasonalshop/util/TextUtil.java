package com.xiaomian124.seasonalshop.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;

public final class TextUtil {
    private TextUtil() {}

    public static Component legacy(String s) {
        if (s == null) return Component.empty();
        return LegacyComponentSerializer.legacyAmpersand().deserialize(s);
    }

    public static Component centerTitle(String raw, NamedTextColor color, boolean bold) {
        int left = Math.max(0, (32 - raw.length()) / 2);
        Component c = Component.text(raw, color);
        if (bold) c = c.decoration(TextDecoration.BOLD, true);
        return Component.text(" ".repeat(left)).append(c);
    }

    private static Component prefix() {
        return Component.text("Seasonal-Shop ", NamedTextColor.GOLD, TextDecoration.BOLD);
    }

    public static void sendInfo(CommandSender sender, Component msg) {
        Component body = msg.colorIfAbsent(NamedTextColor.GRAY)
                .decoration(TextDecoration.BOLD, false);
        sender.sendMessage(prefix().append(body));
    }

    public static void sendWarn(CommandSender sender, Component msg) {
        Component body = msg.colorIfAbsent(NamedTextColor.RED)
                .decoration(TextDecoration.BOLD, false);
        sender.sendMessage(prefix().append(body));
    }

    public static void sendPlain(CommandSender sender, Component msg) {
        sender.sendMessage(msg.colorIfAbsent(NamedTextColor.GRAY)
                .decoration(TextDecoration.BOLD, false));
    }
}