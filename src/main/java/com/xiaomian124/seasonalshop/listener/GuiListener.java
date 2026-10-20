package com.xiaomian124.seasonalshop.listener;

import com.xiaomian124.seasonalshop.SeasonalShop;
import com.xiaomian124.seasonalshop.economy.EconomyProvider;
import com.xiaomian124.seasonalshop.shop.AnimatedGui;
import com.xiaomian124.seasonalshop.shop.PurchaseGui;
import com.xiaomian124.seasonalshop.shop.ShopGui;
import com.xiaomian124.seasonalshop.shop.ShopItem;
import com.xiaomian124.seasonalshop.util.TextUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class GuiListener implements Listener {

    private final SeasonalShop plugin;

    public GuiListener(SeasonalShop plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        InventoryHolder holder = e.getInventory().getHolder();

        if (holder instanceof ShopGui shop) {
            handleShop(e, player, shop);
        } else if (holder instanceof PurchaseGui purchase) {
            handlePurchase(e, player, purchase);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        InventoryHolder holder = e.getInventory().getHolder();
        if (holder instanceof ShopGui || holder instanceof PurchaseGui) {
            e.setCancelled(true);
        }
    }

    private void handleShop(InventoryClickEvent e, Player player, ShopGui gui) {
        e.setCancelled(true);
        int slot = e.getRawSlot();
        if (slot < 0 || slot >= 54) return;

        // 上一页
        if (slot == ShopGui.PREV_SLOT && gui.getPage() > 0) {
            gui.open(player, gui.getPage() - 1);
            return;
        }
        // 下一页
        if (slot == ShopGui.NEXT_SLOT && gui.hasNextPage()) {
            gui.open(player, gui.getPage() + 1);
            return;
        }
        ShopItem item = gui.getItemAt(slot);
        if (item == null) return;
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1f, 1f);
        new PurchaseGui(plugin, item, gui.getPage()).open(player);
    }

    private void handlePurchase(InventoryClickEvent e, Player player, PurchaseGui gui) {
        e.setCancelled(true);
        int slot = e.getRawSlot();

        if (slot == PurchaseGui.CONFIRM_SLOT) {
            attemptPurchase(player, gui);
        } else if (slot == PurchaseGui.CANCEL_SLOT) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1f, 1f);
            new ShopGui(plugin).open(player, gui.getReturnPage());
        }
    }

    private void attemptPurchase(Player player, PurchaseGui gui) {
        ShopItem item = gui.getItem();
        EconomyProvider eco = plugin.getEconomyManager().getActive();

        if (eco == null) {
            TextUtil.sendWarn(player, Component.text("经济系统不可用"));
            return;
        }

        double price = item.effectivePrice();
        double balance = eco.getBalance(player);

        if (balance < price) {
            double need = price - balance;
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
            TextUtil.sendWarn(player, Component.text("你的" + eco.displayName() + "不足以购买此商品，还差 ", NamedTextColor.RED)
                    .append(Component.text(String.format("%.2f", need), NamedTextColor.YELLOW))
                    .append(Component.text(" " + eco.displayName(), NamedTextColor.RED)));
            return;
        }

        if (!eco.withdraw(player, price)) {
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
            TextUtil.sendWarn(player, Component.text("扣除" + eco.displayName() + "失败"));
            return;
        }

        if (item.commands != null) {
            for (String cmd : item.commands) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                        cmd.replace("{player}", player.getName()));
            }
        }
        if (item.material != Material.AIR) {
            ItemStack stack = item.buildRealItem();
            var leftover = player.getInventory().addItem(stack);
            if (!leftover.isEmpty()) {
                leftover.values().forEach(s ->
                        player.getWorld().dropItem(player.getLocation(), s));
            }
        }

        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);

        Component itemName = (item.name != null && !item.name.isEmpty())
                ? TextUtil.legacy(item.name)
                : Component.translatable(item.material.translationKey());

        Component msg = Component.text("你购买了 ", NamedTextColor.GRAY)
                .append(Component.text("[", NamedTextColor.DARK_GRAY));

        if (item.amount > 1) {
            msg = msg.append(Component.text(String.valueOf(item.amount), NamedTextColor.GRAY))
                    .append(Component.text("x", NamedTextColor.DARK_GRAY));
        }

        msg = msg.append(itemName)
                .append(Component.text("] ，花费了 ", NamedTextColor.GRAY))
                .append(Component.text(String.format("%.2f", price), NamedTextColor.YELLOW))
                .append(Component.text(" " + eco.displayName(), NamedTextColor.GRAY));

        TextUtil.sendInfo(player, msg);

        new ShopGui(plugin).open(player, gui.getReturnPage());
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        InventoryHolder holder = e.getInventory().getHolder();
        if (!(holder instanceof AnimatedGui)) return;

        Bukkit.getScheduler().runTask(plugin, () -> {
            if (e.getPlayer().getOpenInventory().getTopInventory().getHolder() != holder) {
                plugin.getGuiRegistry().remove(holder);
            }
        });
    }
}