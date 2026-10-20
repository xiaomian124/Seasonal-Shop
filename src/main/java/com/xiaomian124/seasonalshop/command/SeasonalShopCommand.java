package com.xiaomian124.seasonalshop.command;

import com.xiaomian124.seasonalshop.SeasonalShop;
import com.xiaomian124.seasonalshop.season.Season;
import com.xiaomian124.seasonalshop.season.SeasonManager;
import com.xiaomian124.seasonalshop.shop.ShopContext;
import com.xiaomian124.seasonalshop.shop.ShopGui;
import com.xiaomian124.seasonalshop.util.TextUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SeasonalShopCommand implements CommandExecutor, TabCompleter {

    private final SeasonalShop plugin;

    public SeasonalShopCommand(SeasonalShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd,
                             @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            help(sender);
            return true;
        }

        String subCmd = args[0].toLowerCase();

        if (subCmd.equals("help")) {
            help(sender);
            return true;
        }
        if (subCmd.equals("season")) {
            seasonInfo(sender);
            return true;
        }
        if (subCmd.equals("shop")) {
            if (!(sender instanceof Player p)) {
                TextUtil.sendWarn(sender, Component.text("该命令仅玩家可用！"));
                return true;
            }
            new ShopGui(plugin).open(p, 0);
            return true;
        }

        if (!sender.hasPermission("seasonalshop.admin")) {
            TextUtil.sendWarn(sender, Component.text("你没有使用此命令的权限！"));
            return true;
        }

        switch (subCmd) {
            case "next"   -> handleNext(sender, args);
            case "now"    -> handleNow(sender);
            case "reload" -> handleReload(sender);
            default       -> help(sender);
        }
        return true;
    }

    private void handleReload(CommandSender sender) {
        plugin.reload();
        TextUtil.sendInfo(sender, Component.text("配置文件已重载！"));
    }

    private void handleNext(CommandSender sender, String[] args) {
        if (args.length < 2) {
            TextUtil.sendWarn(sender, Component.text("缺少值，用法: /ss next <季节|节日>"));
            return;
        }
        String arg = args[1];

        var holiday = plugin.getHolidayManager().getById(arg);
        if (holiday != null) {
            if (!plugin.getItemLoader().hasHolidayItems(holiday.getId())) {
                Season cur = plugin.getSeasonManager().getCurrent();
                TextUtil.sendInfo(sender, Component.text(
                        "无变化，因为服务器没有提供此节日的商店商品，商店已自动跳过此节日并继续供应"
                                + cur.cnName + "商店商品"));
                return;
            }
            ShopContext target = new ShopContext(holiday,
                    Season.fromMonth(LocalDate.now().getMonthValue()));
            ShopContext old = plugin.getSeasonManager().getContext();
            if (plugin.getSeasonManager().switchTo(target)) {
                sendSwitch(sender, old, target);
            } else {
                TextUtil.sendInfo(sender, Component.text(
                        "无变化，已经是" + holiday.getDisplayName() + "了，商店供应着" + holiday.getDisplayName() + "的商品"));
            }
            return;
        }

        Season targetSeason;
        try {
            targetSeason = Season.valueOf(arg.toUpperCase());
        } catch (IllegalArgumentException ex) {
            TextUtil.sendWarn(sender, Component.text("无效的季节或节日：" + arg));
            return;
        }
        ShopContext target = new ShopContext(null, targetSeason);
        ShopContext old = plugin.getSeasonManager().getContext();
        if (plugin.getSeasonManager().switchTo(target)) {
            sendSwitch(sender, old, target);
        } else {
            TextUtil.sendInfo(sender, Component.text(
                    "无变化，已经是" + targetSeason.cnName + "了"));
        }
    }

    private void sendSwitch(CommandSender sender, ShopContext old, ShopContext target) {
        Component msg = Component.text()
                .append(Component.text("[", NamedTextColor.DARK_GRAY))
                .append(Component.text(old.icon(), old.themeColor()))
                .append(Component.text(" ⇨ ", NamedTextColor.DARK_GRAY))
                .append(Component.text(target.icon(), target.themeColor()))
                .append(Component.text("] ", NamedTextColor.DARK_GRAY))
                .append(Component.text("已切换为" + target.label()
                        + "，现在供应" + target.label() + "商店商品", NamedTextColor.GRAY))
                .build();
        TextUtil.sendInfo(sender, msg);
    }

    private void handleNow(CommandSender sender) {
        var r = plugin.getSeasonManager().syncToCurrent();
        String ctxLabel = r.context().label();
        String prefix = r.context().isHoliday() ? "节日" : "季节";

        if (r.changed()) {
            Component msg = Component.text()
                    .append(Component.text("[", NamedTextColor.DARK_GRAY))
                    .append(Component.text(r.oldContext().icon(), r.oldContext().themeColor()))
                    .append(Component.text(" ⇨ ", NamedTextColor.DARK_GRAY))
                    .append(Component.text(r.context().icon(), r.context().themeColor()))
                    .append(Component.text("] ", NamedTextColor.DARK_GRAY))
                    .append(Component.text(prefix + "已同步到现在的" + ctxLabel + "，现在供应" + ctxLabel + "商店商品", NamedTextColor.GRAY))
                    .build();

            TextUtil.sendInfo(sender, msg);
        } else if (r.calibrated()) {
            TextUtil.sendInfo(sender, Component.text(
                    "当前已经是" + ctxLabel + "，但校准了 "
                            + r.calibratedSeconds() + " 秒，商店仍然为"
                            + ctxLabel + "商店"));
        } else {
            TextUtil.sendInfo(sender, Component.text(
                    "无变化，已经是" + ctxLabel + "了"));
        }
    }


    private void seasonInfo(CommandSender sender) {
        SeasonManager sm = plugin.getSeasonManager();
        ShopContext ctx = sm.getContext();

        Component iconComp = Component.text(ctx.icon(), ctx.themeColor(), TextDecoration.BOLD);

        if (ctx.isHoliday()) {
            var h = ctx.holiday();
            long s = sm.getSecondsUntilHolidayEnd();
            String remain;
            if (s / 3600 > 0) remain = (s / 3600) + " 小时 " + ((s % 3600) / 60) + " 分钟";
            else if ((s % 3600) / 60 > 0) remain = ((s % 3600) / 60) + " 分钟";
            else remain = "不到 1 分钟";

            Component textComp = Component.text(" 现在是" + h.getDisplayName() + "，还有 " + remain + "结束", NamedTextColor.GRAY)
                    .decoration(TextDecoration.BOLD, false);

            TextUtil.sendPlain(sender, iconComp.append(textComp));

            if (sender instanceof Player p) {
                p.sendActionBar(Component.text(h.getIcon() + " " + h.getDisplayName() + " " + h.getIcon(), h.getThemeColor()));
            }
        } else {
            Season cur = ctx.season();
            Season next = SeasonManager.nextOf(cur);
            int days = sm.getDaysUntilNextChange();

            Component textComp = Component.text(" 现在是" + cur.cnName + "，还有 " + days
                            + " 天到" + next.cnName + "及切换" + next.cnName + "商店", NamedTextColor.GRAY)
                    .decoration(TextDecoration.BOLD, false);

            TextUtil.sendPlain(sender, iconComp.append(textComp));

            if (sender instanceof Player p) {
                p.sendActionBar(Component.text(cur.icon + " " + cur.cnName + " " + cur.icon, cur.themeColor));
            }
        }
    }

    private void help(CommandSender sender) {
        TextUtil.sendPlain(sender, Component.text("=== 季节商店帮助 ===", NamedTextColor.GOLD));
        TextUtil.sendPlain(sender, Component.text("/ss shop —— 打开季节商店", NamedTextColor.YELLOW));
        TextUtil.sendPlain(sender, Component.text("/ss season —— 查看当前季节/节日信息", NamedTextColor.YELLOW));

        if (sender.hasPermission("seasonalshop.admin")) {
            TextUtil.sendPlain(sender, Component.text("/ss next <season|holiday> —— 切换季节或节日", NamedTextColor.YELLOW));
            TextUtil.sendPlain(sender, Component.text("/ss now —— 同步到当前真实时间", NamedTextColor.YELLOW));
            TextUtil.sendPlain(sender, Component.text("/ss reload —— 重载配置", NamedTextColor.YELLOW));
        }
        TextUtil.sendPlain(sender, Component.text("/ss help —— 显示此帮助列表", NamedTextColor.YELLOW));
        TextUtil.sendPlain(sender, Component.text("================", NamedTextColor.GOLD));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                @NotNull Command cmd,
                                                @NotNull String alias,
                                                @NotNull String[] args) {
        if (args.length == 1) {
            List<String> base = new ArrayList<>();
            base.add("help");
            base.add("season");
            base.add("shop");

            if (sender.hasPermission("seasonalshop.admin")) {
                base.add("next");
                base.add("now");
                base.add("reload");
            }

            return base.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase())).toList();
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("next") && sender.hasPermission("seasonalshop.admin")) {
            List<String> opts = new ArrayList<>();
            for (Season s : Season.values()) {
                opts.add(s.name().toLowerCase());
            }
            for (var h : plugin.getHolidayManager().all()) {
                opts.add(h.getId().toLowerCase());
            }
            return opts.stream().filter(s -> s.startsWith(args[1].toLowerCase())).toList();
        }
        return List.of();
    }
}