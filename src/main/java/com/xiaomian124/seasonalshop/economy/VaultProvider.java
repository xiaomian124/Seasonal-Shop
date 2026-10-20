package com.xiaomian124.seasonalshop.economy;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;

public class VaultProvider implements EconomyProvider {

    private final Economy economy;

    public VaultProvider(Economy economy) { this.economy = economy; }

    @Override public String name() { return "vault"; }

    @Override public double getBalance(Player player) {
        return economy.getBalance(player);
    }

    @Override public boolean withdraw(Player player, double amount) {
        if (economy.getBalance(player) < amount) return false;
        return economy.withdrawPlayer(player, amount).transactionSuccess();
    }

    @Override public String displayName() { return "金币"; }

    @Override public Component icon() {
        return Component.text("💰", NamedTextColor.GOLD);
    }
}