package com.xiaomian124.seasonalshop.economy;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.entity.Player;

public class PlayerPointsProvider implements EconomyProvider {

    private final PlayerPointsAPI api;

    public PlayerPointsProvider(PlayerPointsAPI api) { this.api = api; }

    @Override public String name() { return "playerpoints"; }

    @Override public double getBalance(Player player) {
        return api.look(player.getUniqueId());
    }

    @Override public boolean withdraw(Player player, double amount) {
        int pts = (int) Math.ceil(amount);
        if (api.look(player.getUniqueId()) < pts) return false;
        return api.take(player.getUniqueId(), pts);
    }

    @Override public String displayName() { return "点券"; }

    @Override public Component icon() {
        return Component.text("🎟", NamedTextColor.DARK_PURPLE);
    }
}