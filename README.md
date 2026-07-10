# Furry Battle v2.0

一款基于 Java Swing 的回合制卡牌对战游戏。选择你的角色，与 AI 展开策略博弈！

## v2.0 更新内容

- **1v2 双雄模式**：玩家同时对抗两个 AI，8张手牌
  - 回合顺序：玩家 → AI1 → AI2 循环
  - 进攻/交换需选择目标（游戏内按钮选择，非弹窗）
  - 玩家回合结束只给AI补牌，AI回合全部结束才给玩家补牌
  - 一位AI出局后另一AI自动升级（手牌上限8张）
- **新角色 � Moze**：100血，守护型坦克
  - 🛡️【守护】buff：受伤害时弹窗选择消耗守护减免（1层减1🗡️），上限5层，不可减免🩸流血伤害
  - 进攻1️⃣3�️ / 2️⃣2🗡️+1🛡️(🟢额外+1) / 3️⃣1🗡️+1❤️+1🛡️ / 4️⃣出数字牌获对应🛡️弃掉该牌(跳防) / 5️⃣抽对手1牌判定(🟢/⚪/⚫→4🗡️;其他→2❤️+1🛡️跳防) / 6️⃣2+🛡️层数🗡️(无�️不可防御) / 7️⃣3🗡️+清debuff每层+1�️ / 0️⃣3🛡️+5🗡️+抽1🃏
  - 防御1️⃣防½+1�️ / 2️⃣反击1+½�️层数🗡️ / 3️⃣1�️+恢复½×🛡️层数❤️ / 0️⃣防½+2�️+反击2×�️层数🗡️
- **Leon 0️⃣重做**：AOE效果——对所有对手+1🔥+弃对手合计2牌+7�️(不可防御)+每有1个对手存活自伤2点
- **Serenity 5️⃣修改**：恢复分支（🟡🟢判定）跳过防御
- **Saiki 0️⃣修改**：恢复量改为场上所有🩸层数之和（1v2含AI2）
- **架构重构**：GameMode 策略模式，添加新模式只需实现接口
  - `Participant` 统一玩家/AI抽象
  - `TurnManager` 回合轮转管理
  - `AttackEngine` / `DefenseEngine` / `TurnEngine` 职责拆分
- **AI出牌优化**：角色专属AI策略，感知守护/灼烧/流血/血量百分比等上下文
- **灼伤结算修正**：有灼伤标记的一方在进攻回合结束后结算（玩家回合结束→玩家结算，AI回合结束→AI结算）
- **✨✨超级净化**：清除所有buff，包括正面buff（🛡️守护）
- **洗入牌库黑牌**：4张新黑牌，打出后指定颜色+弃牌库洗入牌库+搭桥
- **角色选择重做**：角色只罗列一遍，Player/Bot模式按钮切换，支持同一角色被双方选择
- **白/黑牌指定颜色渲染**：对角线渐变（左上原色→右下指定颜色），三层发光边框
- **Buff图标优化**：图标放大，单图标+数字叠加替代多图标堆叠
- **卡牌渲染统一**：手牌/出牌区/飞入动画都基于 `renderCardImage()`
- **数字卡牌勾线**：偏移±1半透明黑描边
- **技能描述系统**：中括号颜色标签（`[生命]`绿 `[伤害]`红 `[灼烧]`橙 等）
- **卡牌选中弹窗**：半透明深色弹窗，显示卡牌名+技能描述，持续到取消选中
- **飘字颜色标签**：分段着色渲染（`+5[生命]`、`-3[伤害]`等）
- **选中高亮紫色**：`new Color(160, 80, 220)`
- **防御搭桥修复**：AI黑牌始终可作为搭桥牌使用
- **弃牌跳回出牌修复**：确认弃牌后正确切换phase，防止跳回出牌
- **AI回合补牌修复**：补牌动画完成后才切换到玩家回合，防止手牌未补上

## 特色

- **七角色系统**：🐼 Ryan（回复型）、🐻‍❄️ Leon（灼烧型）、� Chan（冷冻型）、🐺 Saiki（流血型）、🐶 Blaze（灼烧狂战）、🐶 Serenity（嗜血型）、🐼 Moze（守护型）
- **双模式**：1v1经典对战 / 1v2双雄模式
- **搭桥机制**：⚫ / 🧪 / +3🃏 / ✨ / ✨✨ / 🔄 / 洗入 可连续搭桥
- **攻防分区**：出牌区左攻右防，防御结束后统一清理
- **AI 对手**：角色专属 AI 策略，会根据局势搭桥、跳防、优先出牌
- **动画效果**：卡牌飞入、HP 平滑过渡、飘字伤害/恢复、🔥/❄️/🩸/🛡️/嗜血标记

## 角色一览

### 🐼 Ryan — 回复型战士

| 血量 | 被动 |
|---|---|
| **70** | 回合开始恢复1❤️ |

**进攻**：1️⃣恢复3❤️ · 2️⃣3🗡️+恢复1❤️ · 3️⃣恢复1❤️+抽1🃏(可弃1牌跳防) · 4️⃣判定(🟢/⚪/⚫→恢复1❤️+4🗡️;其他→加入手牌) · 5️⃣再打1牌(恢复或1.5倍🗡️) · 6️⃣4🗡️+清debuff+抽1🃏 · 7️⃣手牌数字之和½🗡️ · 0️⃣4❤️+清debuff+抽2公示牌🗡️

**防御**：1️⃣格挡½ · 2️⃣反击2🗡️+恢复2❤️ · 3️⃣🔴免疫/其他恢复3❤️ · 0️⃣清debuff+免疫+恢复3❤️

### 🐻‍❄️ Leon — 灼烧型暴君

| 血量 | 被动 |
|---|---|
| **100** | 免疫🔥灼烧伤害（正常挂标记） |

**进攻**：1️⃣2层🔥(跳防) · 2️⃣4🗡️ · 3️⃣3🗡️+1🔥 · 4️⃣5🗡️(对手有🔥跳防) · 5️⃣4🗡️(对手有🔥+2) · 6️⃣判定(数字→对应🗡️;0/⚪/⚫→2🔥跳防) · 7️⃣弃对手1牌+6🗡️+2🔥 · 0️⃣对所有对手+1🔥+弃对手合计2牌+7🗡️(不可防御)+每有1个对手存活自伤2点

**防御**：1️⃣1🔥+恢复2❤️ · 2️⃣反击½+抽1🃏 · 3️⃣格挡½+抽1🃏 · 0️⃣攻击方弃所有牌+双方各受等量🗡️

### � Chan — 冷冻型操控者

| 血量 | 被动 |
|---|---|
| **80** | 回合开始抽1🃏 |

**进攻**：1️⃣1🗡️+❄️冷冻(跳防) · 2️⃣4🗡️ · 3️⃣2🗡️+抽1🃏 · 4️⃣抽对手1牌交换或弃掉+2🗡️(跳防) · 5️⃣消耗2❤️+排序牌库顶5张+抽2🃏(跳防) · 6️⃣5🗡️+判定(🔵/⚪/⚫跳防) · 7️⃣6🗡️+选对手1牌判定 · 0️⃣7🗡️+❄️+抽1🃏

**防御**：1️⃣格挡½ · 2️⃣反击2🗡️+❄️ · 3️⃣翻牌库顶恢复½点数❤️+加入手牌 · 0️⃣免疫+反击½🗡️+结束攻击方回合

### 🐺 Saiki — 流血型猎手

| 血量 | 被动 |
|---|---|
| **80** | ⚔️阶段打出🟡牌，对对手施加1层🩸 |

**进攻**：1️⃣4🗡️ · 2️⃣3🗡️+恢复1❤️ · 3️⃣2🗡️+抽对手1牌(可弃掉) · 4️⃣5🗡️(对手有🩸不可防御) · 5️⃣HP分段(≤20恢复4❤️/20<HP≤50 4🗡️跳防/>50 4🗡️+抽对手1牌) · 6️⃣判定数字牌×1.5🗡️(🟡牌+🩸) · 7️⃣2+2×🩸层数🗡️+恢复等量❤️ · 0️⃣1层🩸+3×对手🩸层数+1🗡️+恢复场上所有🩸层数之和❤️

**防御**：1️⃣防御至多3点🗡️ · 2️⃣反击3🗡️+1层🩸 · 3️⃣翻牌判定(🟡/⚫/⚪→防御所有🗡️不免疫debuff/否则加入手牌) · 0️⃣防御所有🗡️+免疫debuff+双方均摊½🗡️+debuff反弹

### 🐶 Blaze — 灼烧型狂战士

| 血量 | 被动 |
|---|---|
| **85** | 🔥灼烧时⚔️阶段所有攻击+1🗡️（参与🛡️计算），不叠加 |

**进攻**：1️⃣4🗡️(灼烧+1) · 2️⃣2🗡️(不可防御)+自身🔥2 · 3️⃣3🗡️+双方🔥1 · 4️⃣抽对手1牌判定(道具=4点;0️⃣→双方🔥1+加入手牌) · 5️⃣自身🔥1→2×🔥层数🗡️(含被动) · 6️⃣1.5×🔥层数恢复❤️+对手🔥1+清🔥(跳防) · 7️⃣双方🔥2→1.5×场上🔥🗡️(含被动) · 0️⃣6🗡️(不可防御)+🔥2

**防御**：1️⃣恢复2+🔥层数❤️+双方🔥1 · 2️⃣抽1牌反击点数(道具=4+🔥1)+加入手牌 · 3️⃣+2🔥+反击场上🔥层数🗡️ · 0️⃣+4🔥+格挡½+恢复场上🔥+3❤️

### 🐶 Serenity — 嗜血型战士

| 血量 | 被动 |
|---|---|
| **80** | 免疫❄️冷冻；❤️<30时嗜血态(不可净化)；正常态恢复+1❤️ |

**进攻**：1️⃣3🗡️(嗜血跳防) · 2️⃣3🗡️(嗜血5🗡️) · 3️⃣2🗡️+恢复2❤️ · 4️⃣5🗡️(嗜血先+1🩸) · 5️⃣翻牌判定(🟡🟢→恢复4❤️跳防/其他→5🗡️) · 6️⃣6🗡️(嗜血不可防御) · 7️⃣自伤2🗡️+5🗡️不可防御(嗜血无自伤) · 0️⃣弃全部手牌每张+3❤️(上限9)+重抽4🃏继续⚔️(嗜血跳防)

**防御**：1️⃣防御3🗡️(嗜血恢复防御点数❤️) · 2️⃣1层🩸+吸血(🩸层数×2❤️) · 3️⃣格挡½🗡️(嗜血+2) · 0️⃣翻牌判定(🟡→对手⚔️后❤️减半/其他→免疫所有🗡️和debuff)

### 🐼 Moze — 守护型坦克

| 血量 | 被动 |
|---|---|
| **100** | 受伤害时弹窗选择消耗【守护】减免（1层减1🗡️，上限5层，不可减免🩸流血伤害） |

**进攻**：1️⃣3🗡️ · 2️⃣2🗡️+1🛡️(🟢额外+1) · 3️⃣1🗡️+1❤️+1🛡️ · 4️⃣打出1张数字牌获对应层数🛡️弃掉该牌(跳防) · 5️⃣选对手1牌判定(🟢/⚪/⚫→4🗡️/其他→2❤️+1🛡️跳防)该牌加入手牌 · 6️⃣2+🛡️层数🗡️(无🛡️不可防御) · 7️⃣3🗡️+清debuff每层+1🗡️ · 0️⃣3🛡️+5🗡️+抽1🃏

**防御**：1️⃣防½+1🛡️ · 2️⃣反击1+½🛡️层数🗡️ · 3️⃣1🛡️+恢复½×🛡️层数❤️ · 0️⃣防½+2🛡️+反击2×🛡️层数🗡️

## 1v2 双雄模式

| 规则 | 说明 |
|---|---|
| 玩家血量 | 原角色血量（不翻倍） |
| 手牌上限 | 玩家8张，AI各5张 |
| 回合顺序 | 玩家 → AI1 → AI2 循环 |
| 目标选择 | 进攻/交换时选择攻击AI1或AI2 |
| 补牌时机 | 玩家回合结束→AI补牌；AI回合全部结束→玩家补牌 |
| AI互攻 | AI只攻击玩家，不攻击另一个AI |
| 出局处理 | 一位AI出局后，剩余AI立刻补齐8张手牌继续战斗 |

## 卡牌系统

| 牌面 | 数量 | 说明 |
|------|------|------|
| 🔴🟡🔵🟢 1~3 | 各3张 | 四色数字牌 |
| 🔴🟡🔵🟢 4~6 | 各2张 | 四色数字牌 |
| 🔴🟡🔵🟢 7, 0 | 各1张 | 四色数字牌 |
| ⚫ 黑牌 | 2张 | 指定颜色，搭桥 |
| ⚫ 黑+2 | 2张 | 指定颜色，抽2，搭桥 |
| ⚫ 洗入 | 4张 | 指定颜色+弃牌库洗入牌库+搭桥 |
| ⚪ 白1~白7 | 各1张 | 匹配任意颜色 |
| ⚪ 🧪 | 4张 | 恢复5❤️，搭桥 |
| ⚪ +3🃏 | 2张 | 抽3张，搭桥 |
| ⚪ ✨ | 6张 | 净化1层debuff，搭桥 |
| ⚪ ✨✨ | 2张 | 清除所有buff(含🛡️)，搭桥 |
| ⚪ 🔄 | 2张 | 交换双方所有手牌，搭桥 |

**总计 95 张**

### 核心规则

- **出牌**：匹配弃牌库顶的颜色或数字
- **搭桥**：⚫ / 🧪 / +3🃏 / ✨ / ✨✨ / 🔄 / 洗入 打出后不结束回合，可连续搭桥
- **防御**：出数字≤3且颜色匹配的牌防御；也可用搭桥牌过渡
- **🔥灼烧**：有灼伤标记的一方在进攻回合结束后按层数受伤并减1层，最多4层（🐻‍❄️ Leon免疫伤害但正常挂标记）
- **❄️冷冻**：被冷冻时无法防御🔵蓝色攻击（包括被指定为蓝色的⚪白牌）
- **🩸流血**：防御时每层流血造成1点独立伤害（最多2层），可净化
- **🛡️守护**：受伤害时弹窗选择消耗守护减免（1层减1🗡️），上限5层，不可减免🩸流血伤害
- **净化**：✨移除1层debuff（🔥/❄️/🩸），✨✨移除所有buff（含🛡️守护）
- **手牌上限**：回合结束时手牌超过上限需弃牌

## 运行方式

### 环境要求

- JDK 17 或更高版本

### macOS / Linux

```bash
chmod +x start.sh
./start.sh
```

### Windows

双击 `start.bat` 或在命令行执行：

```cmd
start.bat
```

### 手动编译运行

```bash
javac -sourcepath src -d out src/Core/Card.java src/Core/CardDeck.java src/Core/GameCharacter.java src/Core/Participant.java src/Characters/RyanCharacter.java src/Characters/LeonCharacter.java src/Characters/ChanCharacter.java src/Characters/SaikiCharacter.java src/Characters/BlazeCharacter.java src/Characters/SerenityCharacter.java src/Characters/MozeCharacter.java src/AI/AIPlayer.java src/AI/RyanAI.java src/AI/LeonAI.java src/AI/ChanAI.java src/AI/SaikiAI.java src/AI/BlazeAI.java src/AI/SerenityAI.java src/AI/MozeAI.java src/UI/GameIcons.java src/UI/GameUI.java src/UI/GameUI1v2.java src/UI/GameAnim.java src/UI/EffectEngine.java src/UI/CharacterSelectPanel.java src/UI/ModeSelectPanel.java src/UI/SkillDesc.java src/Mode/GameMode.java src/Mode/Mode1v1.java src/Mode/Mode1v2.java src/Mode/TurnManager.java src/Engine/AttackEngine.java src/Engine/DefenseEngine.java src/Engine/TurnEngine.java src/Game.java src/Handlers/CharacterHandler.java src/Handlers/RyanHandler.java src/Handlers/LeonHandler.java src/Handlers/ChanHandler.java src/Handlers/SaikiHandler.java src/Handlers/BlazeHandler.java src/Handlers/SerenityHandler.java src/Handlers/MozeHandler.java src/Dialogs/ChanFiveReorderDialog.java src/Dialogs/PurifyDialog.java src/Dialogs/ColorChooserDialog.java src/Dialogs/CardTooltipDialog.java src/Dialogs/GuardChooserDialog.java
java -cp out Game
```

## 项目结构

```
src/
├── Game.java                  # 游戏主逻辑（协调器）
├── Core/
│   ├── Card.java              # 卡牌类
│   ├── CardDeck.java          # 牌堆（95张）
│   ├── GameCharacter.java     # 角色基类
│   └── Participant.java       # 参与者抽象（玩家/AI统一）
├── Mode/
│   ├── GameMode.java          # 模式策略接口
│   ├── Mode1v1.java           # 1v1模式
│   ├── Mode1v2.java           # 1v2双雄模式
│   └── TurnManager.java       # 回合轮转管理
├── Engine/
│   ├── AttackEngine.java      # 攻击结算+效果处理
│   ├── DefenseEngine.java     # 防御流程+搭桥
│   └── TurnEngine.java        # 回合管理+AI执行
├── UI/
│   ├── GameUI.java            # 1v1 Swing界面
│   ├── GameUI1v2.java         # 1v2 Swing界面
│   ├── GameAnim.java          # 动画系统
│   ├── EffectEngine.java      # 效果引擎
│   ├── GameIcons.java         # 图标加载
│   ├── CharacterSelectPanel.java # 角色选择
│   ├── ModeSelectPanel.java   # 模式选择
│   └── SkillDesc.java         # 技能描述
├── Characters/
│   ├── RyanCharacter.java     # 🐼 Ryan
│   ├── LeonCharacter.java     # 🐻‍❄️ Leon
│   ├── ChanCharacter.java     # � Chan
│   ├── SaikiCharacter.java    # 🐺 Saiki
│   ├── BlazeCharacter.java    # 🐶 Blaze
│   ├── SerenityCharacter.java # 🐶 Serenity
│   └── MozeCharacter.java     # 🐼 Moze
├── AI/
│   ├── AIPlayer.java          # AI基类（优先级系统）
│   ├── RyanAI.java ~ MozeAI.java
├── Handlers/
│   ├── CharacterHandler.java  # Handler基类
│   ├── RyanHandler.java ~ MozeHandler.java
├── Dialogs/
│   ├── ChanFiveReorderDialog.java
│   ├── PurifyDialog.java
│   ├── ColorChooserDialog.java
│   ├── CardTooltipDialog.java
│   └── GuardChooserDialog.java
└── icons/
    ├── card_icons/            # 卡牌图标PNG
    ├── buff_icons/            # buff标记PNG（🔥❄️🩸🛡️）
    └── ui_icons/              # UI按钮图标PNG
```

## 扩展新模式

1. 实现 `GameMode` 接口（手牌上限、AI数量、创建参与者、目标选择、胜负判定、UI创建）
2. 实现 `GameUI` 子类（如需要新布局）
3. 在 `ModeSelectPanel` 中添加入口

Game.java 核心逻辑零改动。

## 扩展角色

1. 创建 `Characters/XCharacter.java` 继承 `GameCharacter`
2. 创建 `AI/XAI.java` 继承 `AIPlayer`
3. 创建 `Handlers/XHandler.java` 继承 `CharacterHandler`
4. 在 `Game.createCharacter()` / `Game.createAI()` 中注册

## 致谢

- 游戏图标来自 [Flaticon](https://www.flaticon.com/)，由 [pocike](https://www.flaticon.com/authors/pocike) 创作

## License

MIT
