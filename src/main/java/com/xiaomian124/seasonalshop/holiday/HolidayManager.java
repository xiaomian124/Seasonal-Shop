package com.xiaomian124.seasonalshop.holiday;

import com.xiaomian124.seasonalshop.SeasonalShop;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.time.LocalDate;
import java.util.*;

public class HolidayManager {
    private final SeasonalShop plugin;
    private final Map<String, Holiday> holidays = new LinkedHashMap<>();

    public HolidayManager(SeasonalShop plugin) { this.plugin = plugin; }

    public void load() {
        holidays.clear();
        File f = new File(plugin.getDataFolder(), "holiday.yml");
        if (!f.exists()) plugin.saveResource("holiday.yml", false);
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(f);
        ConfigurationSection root = cfg.getConfigurationSection("holidays");
        if (root == null) return;

        for (String id : root.getKeys(false)) {
            ConfigurationSection s = root.getConfigurationSection(id);
            if (s == null) continue;
            try {
                NamedTextColor color = NamedTextColor.NAMES.value(
                        s.getString("color", "gold").toLowerCase());
                if (color == null) color = NamedTextColor.GOLD;

                List<Material> glass = new ArrayList<>();
                for (String g : s.getStringList("glass-colors")) {
                    Material m = Material.matchMaterial(g);
                    if (m != null) glass.add(m);
                }
                if (glass.isEmpty()) glass.add(Material.WHITE_STAINED_GLASS_PANE);

                String upperId = id.toUpperCase();
                holidays.put(upperId, new Holiday(upperId,
                        s.getString("name", id),
                        s.getString("icon", "🎉"),
                        s.getInt("month"),
                        s.getInt("day"),
                        color, glass));
            } catch (Exception e) {
                plugin.getLogger().warning("无法加载节日 " + id + ": " + e.getMessage());
            }
        }
        plugin.getLogger().info("已加载 " + holidays.size() + " 个节日");
    }

    public Holiday getFor(LocalDate date) {
        for (Holiday h : holidays.values()) if (h.matches(date)) return h;
        return null;
    }

    public Holiday getById(String idOrName) {
        if (idOrName == null) return null;
        String upper = idOrName.toUpperCase();
        for (Holiday h : holidays.values()) {
            if (h.getId().equals(upper) || h.getDisplayName().equals(idOrName))
                return h;
        }
        return null;
    }

    public Collection<Holiday> all() { return holidays.values(); }
}