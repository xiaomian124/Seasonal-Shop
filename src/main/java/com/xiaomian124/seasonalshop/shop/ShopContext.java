package com.xiaomian124.seasonalshop.shop;

import com.xiaomian124.seasonalshop.holiday.Holiday;
import com.xiaomian124.seasonalshop.season.Season;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Objects;

public record ShopContext(Holiday holiday, Season season) {

    public boolean isHoliday() { return holiday != null; }

    public String displayName() {
        return holiday != null ? holiday.getDisplayName() + "商店" : "季节商店";
    }

    public String label() {
        return holiday != null ? holiday.getDisplayName() : season.cnName;
    }

    public String icon() {
        return holiday != null ? holiday.getIcon() : season.icon;
    }

    public NamedTextColor themeColor() {
        return holiday != null ? holiday.getThemeColor() : season.themeColor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ShopContext other)) return false;
        if (season != other.season) return false;
        String h1 = holiday == null ? null : holiday.getId();
        String h2 = other.holiday == null ? null : other.holiday.getId();
        return Objects.equals(h1, h2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(holiday == null ? null : holiday.getId(), season);
    }
}