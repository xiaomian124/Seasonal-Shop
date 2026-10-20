package com.xiaomian124.seasonalshop.shop;

import com.xiaomian124.seasonalshop.SeasonalShop;
import com.xiaomian124.seasonalshop.season.Season;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

public class ItemLoader {
    private final SeasonalShop plugin;
    private final Map<Season, List<ShopItem>> items = new EnumMap<>(Season.class);
    private final Map<String, List<ShopItem>> holidayItems = new HashMap<>();

    public ItemLoader(SeasonalShop plugin) {
        this.plugin = plugin;
        for (Season s : Season.values()) items.put(s, new ArrayList<>());
    }

    public void load() {
        items.values().forEach(List::clear);
        holidayItems.clear();

        File file = new File(plugin.getDataFolder(), "item.yml");
        if (!file.exists()) plugin.saveResource("item.yml", false);
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection root = cfg.getConfigurationSection("items");
        if (root == null) return;

        for (String key : root.getKeys(false)) {
            ConfigurationSection s = root.getConfigurationSection(key);
            if (s == null) continue;
            try {
                String holidayId = s.getString("holiday");
                if (holidayId != null) {
                    String upperId = holidayId.toUpperCase();
                    ShopItem item = parse(key, s, null);
                    holidayItems.computeIfAbsent(upperId, k -> new ArrayList<>()).add(item);
                } else {
                    String seasonStr = s.getString("season", "SPRING").toUpperCase();
                    Season season;
                    try {
                        season = Season.valueOf(seasonStr);
                    } catch (IllegalArgumentException ex) {
                        plugin.getLogger().warning("商品 " + key + " 配置错误: 找不到季节 '" + seasonStr + "'. 如果是节日商品，请使用 'holiday: 节日id' 字段！");
                        continue;
                    }
                    ShopItem item = parse(key, s, season);
                    items.get(season).add(item);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("无法加载商品 " + key + ": " + e.getMessage());
            }
        }
    }

    private ShopItem parse(String key, ConfigurationSection s, Season season) {
        Material mat = Material.matchMaterial(s.getString("material", "STONE"));
        if (mat == null) mat = Material.STONE;
        return new ShopItem(key, season, mat,
                s.getInt("amount", 1),
                s.getString("name", ""),
                s.getStringList("lore"),
                s.getDouble("price", 100),
                s.getDouble("discount", 0),
                s.getStringList("enchantments"),
                s.getStringList("attributes"),
                s.getStringList("commands"));
    }

    public List<ShopItem> getItems(Season season) {
        return items.getOrDefault(season, List.of());
    }

    public List<ShopItem> getHolidayItems(String id) {
        if (id == null) return List.of();
        return holidayItems.getOrDefault(id.toUpperCase(), List.of());
    }

    public boolean hasHolidayItems(String id) {
        if (id == null) return false;
        return !getHolidayItems(id.toUpperCase()).isEmpty();
    }
}