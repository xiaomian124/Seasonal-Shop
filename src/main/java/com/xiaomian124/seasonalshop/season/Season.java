package com.xiaomian124.seasonalshop.season;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;

import java.util.List;

public enum Season {
    SPRING("春季", "🌳", List.of(3, 4, 5),
            NamedTextColor.GREEN, Material.LIME_STAINED_GLASS_PANE,
            Material.GREEN_STAINED_GLASS_PANE),
    SUMMER("夏季", "🔥", List.of(6, 7, 8),
            NamedTextColor.RED, Material.ORANGE_STAINED_GLASS_PANE,
            Material.RED_STAINED_GLASS_PANE),
    AUTUMN("秋季", "🍂", List.of(9, 10, 11),
            NamedTextColor.GOLD, Material.YELLOW_STAINED_GLASS_PANE,
            Material.ORANGE_STAINED_GLASS_PANE),
    WINTER("冬季", "❄", List.of(12, 1, 2),
            NamedTextColor.AQUA, Material.BLUE_STAINED_GLASS_PANE,
            Material.LIGHT_BLUE_STAINED_GLASS_PANE);

    public final String cnName;
    public final String icon;
    public final List<Integer> months;
    public final NamedTextColor themeColor;
    public final Material glassA;
    public final Material glassB;

    Season(String cnName, String icon, List<Integer> months,
           NamedTextColor themeColor, Material glassA, Material glassB) {
        this.cnName = cnName;
        this.icon = icon;
        this.months = months;
        this.themeColor = themeColor;
        this.glassA = glassA;
        this.glassB = glassB;
    }

    public static Season fromMonth(int month) {
        return switch (month) {
            case 3, 4, 5 -> SPRING;
            case 6, 7, 8 -> SUMMER;
            case 9, 10, 11 -> AUTUMN;
            default -> WINTER; // 12, 1, 2
        };
    }
}