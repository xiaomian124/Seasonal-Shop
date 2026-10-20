package com.xiaomian124.seasonalshop.holiday;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;

import java.time.LocalDate;
import java.util.List;

public class Holiday {
    private final String id;
    private final String displayName;
    private final String icon;
    private final int month, day;
    private final NamedTextColor themeColor;
    private final List<Material> glassColors;

    public Holiday(String id, String displayName, String icon, int month, int day,
                   NamedTextColor themeColor, List<Material> glassColors) {
        this.id = id;
        this.displayName = displayName;
        this.icon = icon;
        this.month = month;
        this.day = day;
        this.themeColor = themeColor;
        this.glassColors = glassColors;
    }

    public boolean matches(LocalDate date) {
        return date.getMonthValue() == month && date.getDayOfMonth() == day;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getIcon() { return icon; }
    public NamedTextColor getThemeColor() { return themeColor; }
    public List<Material> getGlassColors() { return glassColors; }
}