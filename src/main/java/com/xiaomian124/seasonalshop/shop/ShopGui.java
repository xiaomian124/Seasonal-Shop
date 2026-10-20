package com.xiaomian124.seasonalshop.shop;

import com.xiaomian124.seasonalshop.SeasonalShop;
import com.xiaomian124.seasonalshop.util.HeadUtil;
import com.xiaomian124.seasonalshop.util.TextUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopGui implements InventoryHolder, AnimatedGui {

    public static final int PREV_SLOT = 27;
    public static final int NEXT_SLOT = 35;

    private static final String PREV_VALUE =
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzM5MjYyM2NhZmRmNzQ1YmZjYjMxZDA1ZGJjZDI0N2Q1NzFkODk3ZjIyYWNlMzhlZTU0ZWVkM2ZiOTYzNiJ9fX0=";
    private static final String NEXT_VALUE =
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWJjYTkwZWJhZmRmNTdkZjRjMzgwY2YyNjU1YWE3YjRlYzZhNGJkYmQxNTUxNmViZmRlNDMyN2ExZTI3In19fQ==";

    private final SeasonalShop plugin;
    private final ShopContext context;
    private Inventory inv;
    private Player player;
    private int page;
    private int borderOffset = 0;
    private List<ShopItem> items;
    private final Map<Integer, ShopItem> slotMap = new HashMap<>();

    public ShopGui(SeasonalShop plugin) {
        this.plugin = plugin;
        this.context = plugin.getSeasonManager().getContext();
    }

    public void open(Player player, int page) {
        this.player = player;
        this.page = Math.max(0, page);
        this.items = context.isHoliday()
                ? plugin.getItemLoader().getHolidayItems(context.holiday().getId())
                : plugin.getItemLoader().getItems(context.season());

        String raw = context.icon() + " 商店 · " + context.label() + " " + context.icon();
        Component title = TextUtil.centerTitle(raw, context.themeColor(), true);
        inv = Bukkit.createInventory(this, 54, title);
        slotMap.clear();

        GuiBorder.paint(inv, context, borderOffset);
        placeNavigation();
        placeItems();

        plugin.getGuiRegistry().add(this);
        player.openInventory(inv);
    }

    private void placeNavigation() {
        if (page > 0) {
            inv.setItem(PREV_SLOT, HeadUtil.createHead(PREV_VALUE,
                    Component.text("上一页", NamedTextColor.YELLOW)));
        }
        if (hasNextPage()) {
            inv.setItem(NEXT_SLOT, HeadUtil.createHead(NEXT_VALUE,
                    Component.text("下一页", NamedTextColor.YELLOW)));
        }
    }

    private void placeItems() {
        var eco = plugin.getEconomyManager().getActive();
        List<Integer> content = contentSlots();
        int from = page * content.size();
        for (int i = 0; i < content.size(); i++) {
            int idx = from + i;
            if (idx >= items.size()) break;
            ShopItem item = items.get(idx);
            inv.setItem(content.get(i), item.buildDisplayIcon(player, eco, context));
            slotMap.put(content.get(i), item);
        }
    }

    @Override
    public void tickAnimation() {
        if (!context.isHoliday()) return;
        borderOffset++;
        GuiBorder.paint(inv, context, borderOffset);
        placeNavigation(); // 重新覆盖按钮，保证按钮不被玻璃盖住
    }

    private static List<Integer> contentSlots() {
        List<Integer> l = new ArrayList<>();
        for (int row = 1; row <= 4; row++) {
            int start = row * 9 + 1;
            for (int i = start; i < start + 7; i++) l.add(i);
        }
        return l;
    }

    public boolean hasNextPage() {
        return (page + 1) * contentSlots().size() < items.size();
    }

    public ShopItem getItemAt(int slot) { return slotMap.get(slot); }
    public int getPage() { return page; }
    public ShopContext getContext() { return context; }

    @Override
    public @NotNull Inventory getInventory() { return inv; }
}