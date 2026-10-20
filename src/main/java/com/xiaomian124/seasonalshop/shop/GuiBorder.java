package com.xiaomian124.seasonalshop.shop;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public final class GuiBorder {

    public static final int[] CLOCKWISE = {
            0,1,2,3,4,5,6,7,8,
            17,26,35,44,
            53,52,51,50,49,48,47,46,45,
            36,27,18,9
    };

    private GuiBorder() {}

    public static ItemStack pane(Material mat) {
        ItemStack p = ItemStack.of(mat);
        p.editMeta(m -> m.displayName(Component.empty()));
        return p;
    }

    public static void paint(Inventory inv, ShopContext ctx, int offset) {
        if (ctx.isHoliday()) {
            List<Material> colors = ctx.holiday().getGlassColors();
            for (int i = 0; i < CLOCKWISE.length; i++) {
                inv.setItem(CLOCKWISE[i],
                        pane(colors.get((i + offset) % colors.size())));
            }
        } else {
            for (int i = 0; i < CLOCKWISE.length; i++) {
                Material mat = (i % 2 == 0) ? ctx.season().glassA : ctx.season().glassB;
                inv.setItem(CLOCKWISE[i], pane(mat));
            }
        }
    }
}