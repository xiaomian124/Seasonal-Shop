package com.xiaomian124.seasonalshop.economy;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public interface EconomyProvider {
    String name();
    String displayName();
    Component icon();
    double getBalance(Player player);
    boolean withdraw(Player player, double amount);
}