package com.xiaomian124.seasonalshop.season;

import com.xiaomian124.seasonalshop.SeasonalShop;
import com.xiaomian124.seasonalshop.holiday.Holiday;
import com.xiaomian124.seasonalshop.shop.ShopContext;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitTask;

import java.time.*;
import java.time.temporal.ChronoUnit;

public class SeasonManager {

    private final SeasonalShop plugin;
    private volatile ShopContext currentContext;
    private volatile Instant nextChangeAt;
    private LocalDate lastSkipLogDate;
    private BukkitTask task;

    public SeasonManager(SeasonalShop plugin) { this.plugin = plugin; }

    public void start() {
        currentContext = computeContextFor(LocalDate.now());
        nextChangeAt = computeNextChange(currentContext);
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 20L, 20L);
        checkSkippedHoliday();
    }

    public void stop() { if (task != null) task.cancel(); }

    private ShopContext computeContextFor(LocalDate date) {
        Season season = Season.fromMonth(date.getMonthValue());
        Holiday h = plugin.getHolidayManager().getFor(date);
        if (h != null && plugin.getItemLoader().hasHolidayItems(h.getId()))
            return new ShopContext(h, season);
        return new ShopContext(null, season);
    }

    private Instant computeNextChange(ShopContext cur) {
        LocalDate today = LocalDate.now();
        for (int i = 1; i <= 400; i++) {
            LocalDate d = today.plusDays(i);
            if (!computeContextFor(d).equals(cur))
                return d.atStartOfDay(ZoneId.systemDefault()).toInstant();
        }
        return nextSeasonStart();
    }

    private Instant nextSeasonStart() {
        LocalDate today = LocalDate.now();
        int month = today.getMonthValue();
        int nextMonth;
        int yearOffset = 0;
        if (month >= 3 && month <= 5) nextMonth = 6;
        else if (month >= 6 && month <= 8) nextMonth = 9;
        else if (month >= 9 && month <= 11) nextMonth = 12;
        else { nextMonth = 3; if (month == 12) yearOffset = 1; }
        return LocalDate.of(today.getYear() + yearOffset, nextMonth, 1)
                .atStartOfDay(ZoneId.systemDefault()).toInstant();
    }

    private void tick() {
        Instant now = Instant.now();

        if (!now.isBefore(nextChangeAt)) {
            ShopContext newCtx = computeContextFor(LocalDate.now());
            ShopContext oldCtx = currentContext;
            currentContext = newCtx;
            nextChangeAt = computeNextChange(newCtx);
            if (!newCtx.equals(oldCtx)) broadcastChange(newCtx);
            checkSkippedHoliday();
            return;
        }

        long secs = Duration.between(now, nextChangeAt).getSeconds();
        if (secs >= 1 && secs <= 10) {
            sendCountdown((int) secs);
            playHat();
        }
    }

    private void checkSkippedHoliday() {
        LocalDate today = LocalDate.now();
        if (today.equals(lastSkipLogDate)) return;
        Holiday h = plugin.getHolidayManager().getFor(today);
        if (h != null && !plugin.getItemLoader().hasHolidayItems(h.getId())) {
            Season season = Season.fromMonth(today.getMonthValue());
            plugin.getLogger().info("今天为" + h.getDisplayName()
                    + "，但你没有提供此节日的商店物品，商店已自动跳过此节日并继续供应"
                    + season.cnName + "商店");
            lastSkipLogDate = today;
        }
    }

    private void sendCountdown(int s) {
        Component msg = Component.text()
                .append(Component.text("距离下一商店切换还剩 ", NamedTextColor.GOLD))
                .append(Component.text(s, NamedTextColor.RED))
                .append(Component.text(" 秒", NamedTextColor.GOLD))
                .build();
        Bukkit.getOnlinePlayers().forEach(p -> p.sendActionBar(msg));
    }

    private void playHat() {
        Bukkit.getOnlinePlayers().forEach(p ->
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1f, 1f));
    }

    private void broadcastChange(ShopContext ctx) {
        Component title = Component.text()
                .append(Component.text(ctx.icon() + " ", ctx.themeColor())
                        .decorate(TextDecoration.BOLD))
                .append(Component.text(ctx.label() + "商店已解锁 ", ctx.themeColor()))
                .append(Component.text(ctx.icon(), ctx.themeColor())
                        .decorate(TextDecoration.BOLD))
                .build();
        Component subtitle = Component.text("输入 /seasonalshop shop 查看", NamedTextColor.GRAY);

        Bukkit.getOnlinePlayers().forEach(p -> {
            p.showTitle(Title.title(title, subtitle,
                    Title.Times.times(Duration.ofMillis(500),
                            Duration.ofSeconds(3), Duration.ofMillis(500))));
            p.playSound(p.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1f);
        });
        plugin.getLogger().info("商店已切换为: " + ctx.displayName());
    }

    public boolean switchTo(ShopContext ctx) {
        ShopContext old = currentContext;
        if (ctx.equals(old)) return false;
        currentContext = ctx;
        broadcastChange(ctx);
        return true;
    }

    public record SyncResult(ShopContext oldContext, ShopContext context, long calibratedSeconds,
                             boolean changed, boolean calibrated) {}

    public SyncResult syncToCurrent() {
        LocalDate today = LocalDate.now();
        ShopContext real = computeContextFor(today);
        ShopContext old = currentContext;
        Instant realNext = computeNextChange(real);
        long delta = Math.abs(Duration.between(nextChangeAt, realNext).getSeconds());

        if (!real.equals(old)) {
            currentContext = real;
            nextChangeAt = realNext;
            broadcastChange(real);
            return new SyncResult(old, real, delta, true, false);
        }
        if (delta != 0) {
            nextChangeAt = realNext;
            return new SyncResult(old, real, delta, false, true);
        }
        return new SyncResult(old, real, 0, false, false);
    }

    public ShopContext getContext() { return currentContext; }
    public Season getCurrent() { return currentContext.season(); }
    public Holiday getCurrentHoliday() { return currentContext.holiday(); }

    public int getDaysUntilNextChange() {
        LocalDate today = LocalDate.now();
        LocalDate next = nextChangeAt.atZone(ZoneId.systemDefault()).toLocalDate();
        return (int) ChronoUnit.DAYS.between(today, next);
    }

    public long getSecondsUntilHolidayEnd() {
        Instant end = LocalDate.now().plusDays(1)
                .atStartOfDay(ZoneId.systemDefault()).toInstant();
        return Math.max(0, Duration.between(Instant.now(), end).getSeconds());
    }

    public static Season nextOf(Season s) {
        return switch (s) {
            case SPRING -> Season.SUMMER;
            case SUMMER -> Season.AUTUMN;
            case AUTUMN -> Season.WINTER;
            case WINTER -> Season.SPRING;
        };
    }
}