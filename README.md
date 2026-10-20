# 🌳 Seasonal-Shop 🍂
> 一款为 **Paper 26.3+** 打造的动态季节与节日商店插件。

**Seasonal-Shop** 是一款与现实时间同步的季节商店插件。它会根据真实日期自动切换春、夏、秋、冬四季，并在特定的节假日（如圣诞节、万圣节）无缝切换节日专属商店。插件不仅拥有炫酷的 GUI 跑马灯动画，还支持深度自定义物品、折扣系统、以及 Vault/PlayerPoints 双经济系统。

---

## ✨ 核心特性

### 🌤️ 真实时间驱动
- **自动季节检测**：根据系统真实日期（如 3-5 月为春季）自动切换商店。
- **自动节日覆盖**：到了节假日（如 12 月 25 日），自动覆盖当前季节商店，售卖节日专属物品。
- **智能兜底机制**：如果服主未配置某节日的商品，插件会**静默跳过**，继续供应当季商品，并在后台留下日志。
- **无缝倒计时**：切换前 10 秒，全服自动弹出 ActionBar 倒计时与敲击音效；切换时弹出 Title 标题提示与音效。

### 🎨 动态高颜值 GUI
- **自适应玻璃边框**：春季黄绿交替，夏季橙红交替，节日则采用多色玻璃。
- **顺时针跑马灯**：在节日期间，边框玻璃会像跑马灯一样**顺时针流动旋转**。
- **专属购买界面**：点击商品即可进入购买界面。界面不仅保持相同的跑马灯动画，还支持原版完美展示附魔、属性与自定义 NBT。

### 💰 灵活的经济与物品系统
- **货币支持**：支持 Vault（金币）与 PlayerPoints（点券），支持热重载（`/ss reload`），无需重启服务器即可切换货币。
- **多维度商品展示**：
  - 自动识别物品堆叠数量。
  - 附带完整的自定义 Lore 模板，展示余额、购买状态等。
  - **划线原价**：支持 `discount` 折扣配置，打折时原价显示为删除线。
- **多样化购买行为**：购买成功不仅可以直接给予物品，还可以执行后台指令（如给予权限、发放称号等）。

---

## 🕹️ 指令与权限

| 指令 | 说明 | 权限节点 | 默认权限 |
| :--- | :--- | :--- | :--- |
| `/ss shop` | 打开当前季节/节日的商店 | 无 | 所有人 |
| `/ss help` | 查看帮助列表 | 无 | 所有人 |
| `/ss season` | 查看当前季节/节日信息与剩余时间 | 无 | 所有人 |
| `/ss next <季节/节日>` | 强制切换至指定季节/节日商店 | `seasonalshop.admin` | OP |
| `/ss now` | 按真实时间强制同步/校准商店 | `seasonalshop.admin` | OP |
| `/ss reload` | 重新加载 config, item 与 holiday | `seasonalshop.admin` | OP |

---

## ⚙️ 配置文件示例

### 1. `config.yml` 配置文件
```yaml
# ===========================
#   SeasonalShop 插件配置文件
# ===========================

# 商店商品货币，必须提供一种
# 可选：vault / playerpoints
currency: playerpoints
```

### 2. `item.yml` 自定义物品文件
```yaml
# ==========================================
# Seasonal-Shop 商品配置文件
# 
# 季节商品：使用 season 字段 (SPRING / SUMMER / AUTUMN / WINTER)
# 节日商品：使用 holiday 字段 (对应 holiday.yml 里的节点名，建议全大写)
# ==========================================

items:
  # ==================== 春季商品 ====================
  spring_diamond:
    season: SPRING
    material: DIAMOND
    amount: 50
    name: "&a春季钻石"
    lore:
      - "&7限时春季商品"
      - "&7购买后直接发放到背包"
    price: 500.0
    discount: 8.0        # 8折，实际扣款 400.00

  spring_fly:
    season: SPRING
    material: FEATHER
    amount: 1
    name: "&a飞行权限 (30天)"
    lore:
      - "&7限时春季商品"
      - "&7购买后自动获得飞行权限"
    price: 20.0
    commands:
      - "lp user {player} permission settemp essentials.fly true 30d"

  # ==================== 夏季商品 ====================
  summer_netherite:
    season: SUMMER
    material: NETHERITE_INGOT
    amount: 1
    name: "&c夏日下界合金"
    price: 15.0
    enchantments:
      - "UNBREAKING:3"   # 附魔: 等级
    attributes:
      - "GENERIC_ARMOR:2:ADD_NUMBER" # 属性: 数值:操作类型(ADD_NUMBER/ADD_SCALAR/MULTIPLY_SCALAR_1)

  # ==================== 秋季商品 ====================
  autumn_gold:
    season: AUTUMN
    material: GOLD_INGOT
    amount: 16
    name: "&6秋日金锭"
    price: 8.0

  tp_permission:
    season: AUTUMN
    material: NAME_TAG
    amount: 1
    name: "&e传送权限（2天）"
    price: 10
    discount: 8        # 8折
    lore:
      - "&7可以在玩家之间自由传送"
    commands:
      - "lp user {player} permission settemp minecraft.command.teleport true 2d"

  # ==================== 冬季商品 ====================
  winter_elytra:
    season: WINTER
    material: ELYTRA
    amount: 1
    name: "&b冬日鞘翅"
    price: 16.0
    discount: 9.5        # 9.5折，支持小数

  # ==================== 圣诞节商品 ====================
  christmas_gift:
    holiday: CHRISTMAS
    material: CHEST
    amount: 1
    name: "&c&l圣诞礼物"
    lore:
      - "&7打开可以获得随机奖励"
      - "&7节日限定"
    price: 14.0
    discount: 5.0        # 圣诞半价
    commands:
      - "give {player} diamond 5"
      - "give {player} emerald 10"

  christmas_sword:
    holiday: CHRISTMAS
    material: DIAMOND_SWORD
    amount: 1
    name: "&c&l圣诞之剑"
    price: 30.0
    enchantments:
      - "SHARPNESS:8"
      - "FIRE_ASPECT:5"
    attributes:
      - "GENERIC_ATTACK_DAMAGE:10:ADD_NUMBER"

  # ==================== 万圣节商品 ====================
  halloween_pumpkin:
    holiday: HALLOWEEN
    material: CARVED_PUMPKIN
    amount: 1
    name: "&6万圣南瓜灯"
    price: 3.0

  halloween_candy:
    holiday: HALLOWEEN
    material: GLOW_BERRIES
    amount: 64
    name: "&d万圣节糖果"
    price: 6.0

  # ==================== 新年商品 ====================
  vip_permission:
    holiday: NEW_YEAR
    material: NAME_TAG
    amount: 1
    name: "&6&l新年快乐"
    price: 10.0
```

### 3. `holiday.yml` 自定义节日文件
```yaml
holidays:
  CHRISTMAS:
    name: "圣诞节"
    month: 12
    day: 25
    icon: "🎄"
    color: "dark_green"
    glass-colors:
      - RED_STAINED_GLASS_PANE
      - WHITE_STAINED_GLASS_PANE
      - GREEN_STAINED_GLASS_PANE
  HALLOWEEN:
    name: "万圣节"
    month: 10
    day: 31
    icon: "🎃"
    color: "gold"
    glass-colors:
      - ORANGE_STAINED_GLASS_PANE
      - BLACK_STAINED_GLASS_PANE
      - PURPLE_STAINED_GLASS_PANE
  VALENTINE:
    name: "情人节"
    month: 2
    day: 14
    icon: "💕"
    color: "light_purple"
    glass-colors:
      - PINK_STAINED_GLASS_PANE
      - MAGENTA_STAINED_GLASS_PANE
  NEW_YEAR:
    name: "新年"
    month: 1
    day: 1
    icon: "🎆"
    color: "red"
    glass-colors:
      - YELLOW_STAINED_GLASS_PANE
      - ORANGE_STAINED_GLASS_PANE
      - RED_STAINED_GLASS_PANE
  EASTER:
    name: "复活节"
    month: 4
    day: 4
    icon: "🐰"
    color: "dark_purple"
    glass-colors:
      - PINK_STAINED_GLASS_PANE
      - LIME_STAINED_GLASS_PANE
      - LIGHT_BLUE_STAINED_GLASS_PANE
```

---

## 📦 环境要求

- **服务端**: Paper 26.3+（Java 25）
- **前置插件**: 
  - 必须安装 **Vault** (及任意经济插件) 或 **PlayerPoints** 这俩其中之一。
  - 若两者均未安装，插件将拒绝启动并禁用。

---

## 🚀 安装步骤

1. 下载 `Seasonal-Shop-1.0.jar` 并放入服务器 `plugins` 文件夹。
2. 重启服务器生成配置文件。
3. 根据需求编辑 `config.yml`、`item.yml` 和 `holiday.yml`。
4. 在控制台输入 `/ss reload` 即重载插件，无需重启服务器。

---

## 🙏 感谢使用我的插件

### 为我发电
你们的每一份支持，都是我项目前进动力。 无论金额大小，我都记在心里。>> [发电](https://ifdian.net/a/xiaomian124)
