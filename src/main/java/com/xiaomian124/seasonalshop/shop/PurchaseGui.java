package com.xiaomian124.seasonalshop.shop;

import com.xiaomian124.seasonalshop.SeasonalShop;
import com.xiaomian124.seasonalshop.util.HeadUtil;
import com.xiaomian124.seasonalshop.util.TextUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class PurchaseGui implements InventoryHolder, AnimatedGui {

    public static final int CONFIRM_SLOT = 20;
    public static final int DISPLAY_SLOT = 22;
    public static final int CANCEL_SLOT  = 24;

    private static final String CONFIRM_VALUE =
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTllMWY2NTdhZjhmZWY5MTJmMjUzYTE2N2JkYzY4MWRjNTYwZTZjZWFjNzM1NTdkZTY4ZjY5MDE3ZWQxNWEyYSJ9fX0=";
    private static final String CANCEL_VALUE =
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTdhODE3ZDlhOGI1ZTJlZTI1ZWZiNDZiNGQ3YmI0NTEwOGVjY2FlMmRiMWQwOWEzYWY1Zjg5ZmI1MWU1In19fQ==";

    private final SeasonalShop plugin;
    private final ShopContext context;
    private final ShopItem item;
    private final int returnPage;

    private Inventory inv;
    private Player player;
    private int borderOffset = 0;

    public PurchaseGui(SeasonalShop plugin, ShopItem item, int returnPage) {
        this.plugin = plugin;
        this.context = plugin.getSeasonManager().getContext();
        this.item = item;
        this.returnPage = Math.max(0, returnPage);
    }

    public void open(Player player) {
        this.player = player;

        String raw = context.icon() + " 购买 · " + context.label() + " " + context.icon();
        Component title = TextUtil.centerTitle(raw, context.themeColor(), true);

        inv = Bukkit.createInventory(this, 54, title);

        GuiBorder.paint(inv, context, borderOffset);
        paintFunctions();

        plugin.getGuiRegistry().add(this);
        player.openInventory(inv);
    }

    private void paintFunctions() {
        inv.setItem(DISPLAY_SLOT, item.buildRealItem());

        inv.setItem(CONFIRM_SLOT, HeadUtil.createHead(CONFIRM_VALUE,
                Component.text("购买", NamedTextColor.GREEN, TextDecoration.BOLD)));

        inv.setItem(CANCEL_SLOT, HeadUtil.createHead(CANCEL_VALUE,
                Component.text("取消", NamedTextColor.RED, TextDecoration.BOLD)));
    }

    @Override
    public void tickAnimation() {
        if (!context.isHoliday()) return;
        borderOffset++;
        GuiBorder.paint(inv, context, borderOffset);
        paintFunctions();
    }

    public ShopItem getItem() { return item; }
    public int getReturnPage() { return returnPage; }
    public ShopContext getContext() { return context; }

    @Override
    public @NotNull Inventory getInventory() { return inv; }
}