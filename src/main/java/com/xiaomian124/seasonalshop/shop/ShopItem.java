package com.xiaomian124.seasonalshop.shop;

import com.xiaomian124.seasonalshop.economy.EconomyProvider;
import com.xiaomian124.seasonalshop.season.Season;
import com.xiaomian124.seasonalshop.util.TextUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ShopItem {

    public final String id;
    public final Season season;
    public final Material material;
    public final int amount;
    public final String name;
    public final List<String> lore;
    public final double price;
    public final double discount;
    public final List<String> enchants;
    public final List<String> attributes;
    public final List<String> commands;

    public ShopItem(String id, Season season, Material material, int amount,
                    String name, List<String> lore, double price, double discount,
                    List<String> enchants, List<String> attributes, List<String> commands) {
        this.id = id;
        this.season = season;
        this.material = material;
        this.amount = Math.max(1, amount);
        this.name = name;
        this.lore = lore;
        this.price = price;
        this.discount = discount;
        this.enchants = enchants;
        this.attributes = attributes;
        this.commands = commands;
    }

    public double effectivePrice() {
        return discount > 0 ? price * discount / 10.0 : price;
    }

    public ItemStack buildDisplayIcon(Player player, EconomyProvider eco, ShopContext ctx) {
        ItemStack stack = ItemStack.of(material, amount);
        stack.editMeta(meta -> {
            meta.displayName(buildName());
            meta.lore(buildCustomLore(player, eco, ctx));
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
        });
        return stack;
    }

    public ItemStack buildRealItem() {
        ItemStack stack = ItemStack.of(material, amount);
        stack.editMeta(meta -> {
            meta.displayName(buildName());
            if (lore != null && !lore.isEmpty()) {
                meta.lore(lore.stream()
                        .map(TextUtil::legacy)
                        .map(c -> c.decoration(TextDecoration.ITALIC, false))
                        .toList());
            }
            applyEnchants(meta);
            applyAttributes(meta);
        });
        return stack;
    }

    private Component buildName() {
        if (name != null && !name.isEmpty()) {
            return TextUtil.legacy(name).decoration(TextDecoration.ITALIC, false);
        }
        return Component.translatable(material.translationKey())
                .decoration(TextDecoration.ITALIC, false);
    }

    private List<Component> buildCustomLore(Player player, EconomyProvider eco, ShopContext ctx) {
        NamedTextColor themeColor = ctx.themeColor();
        boolean enough = eco.getBalance(player) >= effectivePrice();

        Component t1;
        if (discount > 0) {
            t1 = Component.text()
                    .append(Component.text("季节促销 ", NamedTextColor.GRAY))
                    .append(Component.text(fmtDiscount(discount), NamedTextColor.GOLD))
                    .append(Component.text(" 折", NamedTextColor.GRAY))
                    .build();
        } else {
            t1 = Component.text("季节原价", NamedTextColor.GRAY);
        }

        Component t2;
        if (discount > 0) {
            t2 = Component.text()
                    .append(Component.text(fmtPrice(price), NamedTextColor.DARK_GRAY, TextDecoration.STRIKETHROUGH))
                    .append(Component.text(" "))
                    .append(Component.text(fmtPrice(effectivePrice()), NamedTextColor.GRAY))
                    .build();
        } else {
            t2 = Component.text(fmtPrice(price), NamedTextColor.GRAY);
        }

        Component t3 = enough
                ? Component.text("你的" + eco.displayName() + "足以购买此商品", NamedTextColor.GREEN)
                : Component.text("你的" + eco.displayName() + "不足以购买此商品", NamedTextColor.RED);

        Component i3 = Component.text(enough ? "✔" : "✘",
                enough ? NamedTextColor.GREEN : NamedTextColor.RED);

        return List.of(
                Component.text("============", NamedTextColor.GRAY),
                Component.text()
                        .append(Component.text(ctx.icon(), themeColor))
                        .append(Component.text("：", NamedTextColor.GRAY))
                        .append(t1).build(),
                Component.text()
                        .append(eco.icon())
                        .append(Component.text("：", NamedTextColor.GRAY))
                        .append(t2).build(),
                Component.text()
                        .append(i3)
                        .append(Component.text("：", NamedTextColor.GRAY))
                        .append(t3).build(),
                Component.text("============", NamedTextColor.GRAY)
        ).stream().map(c -> (Component) c.decoration(TextDecoration.ITALIC, false)).toList();
    }

    private static String fmtPrice(double p) { return String.format("%.2f", p); }

    private static String fmtDiscount(double d) {
        return d == Math.floor(d) ? String.valueOf((int) d) : String.valueOf(d);
    }

    private void applyEnchants(ItemMeta meta) {
        if (enchants == null) return;
        for (String line : enchants) {
            String[] p = line.split(":");
            if (p.length < 2) continue;
            Enchantment ench = Registry.ENCHANTMENT.get(
                    NamespacedKey.minecraft(p[0].toLowerCase()));
            if (ench == null) continue;
            try { meta.addEnchant(ench, Integer.parseInt(p[1]), true); }
            catch (NumberFormatException ignored) {}
        }
    }

    private void applyAttributes(ItemMeta meta) {
        if (attributes == null) return;
        for (int i = 0; i < attributes.size(); i++) {
            String[] p = attributes.get(i).split(":");
            if (p.length < 3) continue;
            Attribute attr = Registry.ATTRIBUTE.get(
                    NamespacedKey.minecraft(p[0].toLowerCase()));
            if (attr == null) continue;
            try {
                AttributeModifier.Operation op = AttributeModifier.Operation.valueOf(p[2]);
                AttributeModifier mod = new AttributeModifier(
                        new NamespacedKey("seasonalshop", "attr_" + id + "_" + i),
                        Double.parseDouble(p[1]), op);
                meta.addAttributeModifier(attr, mod);
            } catch (Exception ignored) {}
        }
    }

    public boolean isCommandOnly() {
        return material == Material.AIR;
    }
}