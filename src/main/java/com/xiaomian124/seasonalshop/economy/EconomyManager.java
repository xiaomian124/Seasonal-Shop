package com.xiaomian124.seasonalshop.economy;

import com.xiaomian124.seasonalshop.SeasonalShop;
import net.milkbowl.vault.economy.Economy;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EconomyManager {

    private final SeasonalShop plugin;
    private EconomyProvider active;
    private EconomyProvider vault;
    private EconomyProvider points;

    public EconomyManager(SeasonalShop plugin) {
        this.plugin = plugin;
    }

    public boolean setup() {
        vault = null; points = null;

        if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Economy> rsp =
                    Bukkit.getServicesManager().getRegistration(Economy.class);
            if (rsp != null && rsp.getProvider() != null)
                vault = new VaultProvider(rsp.getProvider());
        }
        if (Bukkit.getPluginManager().getPlugin("PlayerPoints") != null) {
            try {
                points = new PlayerPointsProvider(PlayerPoints.getInstance().getAPI());
            } catch (Throwable ignored) {}
        }

        String configured = plugin.getConfig()
                .getString("currency", "vault").toLowerCase();
        if ("vault".equals(configured)) active = vault;
        else if ("playerpoints".equals(configured)) active = points;
        else active = null;

        if (active == null) {
            plugin.getLogger().severe("至少需要一种货币才能启用插件，提供 [vault, playerpoints]");
            Bukkit.getPluginManager().disablePlugin(plugin);
            return false;
        }
        plugin.getLogger().info("当前使用货币: " + active.name());
        return true;
    }

    public void reload() {
        setup();
    }

    public boolean purchase(Player player, double price) {
        if (active == null) return false;
        return active.withdraw(player, price);
    }

    public EconomyProvider getActive() { return active; }
}