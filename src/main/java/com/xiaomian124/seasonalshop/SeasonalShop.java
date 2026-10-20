package com.xiaomian124.seasonalshop;

import com.xiaomian124.seasonalshop.command.SeasonalShopCommand;
import com.xiaomian124.seasonalshop.economy.EconomyManager;
import com.xiaomian124.seasonalshop.holiday.HolidayManager;
import com.xiaomian124.seasonalshop.listener.GuiListener;
import com.xiaomian124.seasonalshop.season.SeasonManager;
import com.xiaomian124.seasonalshop.shop.AnimatedGui;
import com.xiaomian124.seasonalshop.shop.ItemLoader;
import com.xiaomian124.seasonalshop.shop.ShopGui;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;

public final class SeasonalShop extends JavaPlugin {

    private SeasonManager seasonManager;
    private EconomyManager economyManager;
    private ItemLoader itemLoader;
    private HolidayManager holidayManager;
    private final Set<AnimatedGui> guiRegistry = new HashSet<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResource("item.yml", false);
        saveResource("holiday.yml", false);

        holidayManager = new HolidayManager(this);
        holidayManager.load();

        economyManager = new EconomyManager(this);
        if (!economyManager.setup()) return;

        itemLoader = new ItemLoader(this);
        itemLoader.load();

        seasonManager = new SeasonManager(this);
        seasonManager.start();

        SeasonalShopCommand cmd = new SeasonalShopCommand(this);
        getCommand("seasonalshop").setExecutor(cmd);
        getCommand("seasonalshop").setTabCompleter(cmd);
        getServer().getPluginManager().registerEvents(new GuiListener(this), this);

        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (AnimatedGui gui : new HashSet<>(guiRegistry)) gui.tickAnimation();
        }, 20L, 20L);

        getLogger().info("插件已启用，当前为："
                + seasonManager.getContext().displayName());
    }

    @Override
    public void onDisable() {
        if (seasonManager != null) seasonManager.stop();

        getLogger().info("插件已禁用");
    }

    public void reload() {
        reloadConfig();
        holidayManager.load();
        itemLoader.load();
        economyManager.reload();
    }

    public SeasonManager getSeasonManager() { return seasonManager; }
    public EconomyManager getEconomyManager() { return economyManager; }
    public ItemLoader getItemLoader() { return itemLoader; }
    public HolidayManager getHolidayManager() { return holidayManager; }
    public Set<AnimatedGui> getGuiRegistry() { return guiRegistry; }
}