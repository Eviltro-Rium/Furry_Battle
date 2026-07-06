# Furry Battle v1.3

一款基于 Java Swing 的回合制卡牌对战游戏。选择你的角色，与 AI 展开策略博弈！

## v1.3 更新内容

- **新角色 🐶 Blaze**：灼烧型狂战士，75血，被动灼烧时攻击+1🗡️
- **🔄交换牌**：2张白色道具牌，打出后交换双方所有手牌，可搭桥
- **浅色按钮UI**：所有按钮改为浅色系+深色文字，禁用态灰色显示
- **防误触机制**：300ms按钮冷却 + 动画期间按钮自动禁用
- **飘字防重叠**：同区域连续飘字自动垂直错开，避免伤害/恢复/buff重叠
- **AI优化**：满血不用🧪、手牌≤对手时才出🔄交换牌
- **防御结算顺序**：先施加灼伤再结算反击伤害/回血（Blaze防御3️⃣/0️⃣）

## v1.2 更新内容

- **新角色 🐺 Saiki**：流血型猎手，90血，被动打出黄牌施加流血
- **流血机制**：防御时每层流血造成1点独立伤害，最多3层，可净化
- **净化牌**：✨净化1层debuff+搭桥（6张）、✨✨清除所有debuff+搭桥（2张）
- **🧪药水牌**：4张，恢复5❤️+搭桥
- **+3牌**：2张，抽3张+搭桥
- **道具牌value=-1**：从根本上避免道具牌误触0️⃣技能
- **AI策略优化**：优先级系统替代简单匹配，角色专属策略
- **流血伤害区分显示**：每层流血独立显示🩸-1，被动流血单独显示🩸+1(🟡)

## 特色

- **五角色系统**：Ryan（回复型）、Leon（灼烧型）、Chan（冷冻型）、Saiki（流血型）、Blaze（灼烧狂战），技能风格迥异
- **搭桥机制**：黑牌 / 🧪 / +3 / ✨ / ✨✨ / 🔄 可连续搭桥，策略深度拉满
- **攻防分区**：出牌区左攻右防，防御结束后统一清理
- **AI 对手**：角色专属 AI 策略，会根据局势搭桥、跳防、优先出牌
- **动画效果**：卡牌飞入、HP 平滑过渡、飘字伤害/恢复、灼烧/冷冻/流血标记

## 角色一览

### 🐼 Ryan — 回复型战士

| 血量 | 被动 |
|---|---|
| **70** | 回合开始恢复 1 ❤️ |

**进攻**：1️⃣4伤害 · 2️⃣1恢复+3伤害 · 3️⃣1恢复+抽1 · 4️⃣判定(绿/白/黑→1恢复+4伤害) · 5️⃣再打1牌(恢复或1.5倍伤害) · 6️⃣4伤害+清debuff+抽1 · 7️⃣手牌数字之和½伤害 · 0️⃣4恢复+清debuff+抽2公示牌伤害

**防御**：1️⃣格挡½ · 2️⃣反击2+恢复2 · 3️⃣红色免疫/其他恢复3 · 0️⃣清debuff+免疫+恢复3

### 🐻‍❄️ Leon — 灼烧型暴君

| 血量 | 被动 |
|---|---|
| **100** | 免疫灼烧伤害（正常挂标记） |

**进攻**：1️⃣2层灼伤(跳防) · 2️⃣4伤害 · 3️⃣3伤害+1灼伤 · 4️⃣5伤害(有灼伤跳防) · 5️⃣4伤害(有灼伤+2) · 6️⃣判定(数字→对应伤害;0/白/黑→2灼伤跳防) · 7️⃣弃对手1牌+6伤害+2灼伤 · 0️⃣敌方弃2+1灼伤+7伤害(跳防)+自伤2

**防御**：1️⃣1灼伤+恢复2 · 2️⃣反击½+抽1 · 3️⃣格挡½+抽1 · 0️⃣攻击方弃所有牌+双方各受等量伤害

### 🐼 Chan — 冷冻型操控者

| 血量 | 被动 |
|---|---|
| **80** | 回合开始抽 1 🃏 |

**进攻**：1️⃣1伤害+冷冻(跳防) · 2️⃣4伤害 · 3️⃣2伤害+抽1 · 4️⃣抽对手1牌交换或弃掉+2伤害(跳防) · 5️⃣消耗2❤️+排序牌库顶5张+抽2(跳防) · 6️⃣5伤害+判定(🔵/⚪/⚫跳防) · 7️⃣6伤害+选对手1牌判定 · 0️⃣7伤害+冷冻+抽1

**防御**：1️⃣格挡½ · 2️⃣反击2+冷冻 · 3️⃣翻牌库顶恢复½点数+加入手牌 · 0️⃣免疫+反击½+结束攻击方回合

### 🐺 Saiki — 流血型猎手

| 血量 | 被动 |
|---|---|
| **80** | ⚔️阶段打出🟡牌（含指定为黄色的白牌/黑牌），对对手施加1层🩸 |

**进攻**：1️⃣4伤害 · 2️⃣3伤害+恢复1 · 3️⃣2伤害+抽对手1牌(可弃掉) · 4️⃣5伤害(对手有流血不可防御) · 5️⃣HP分段(≤20恢复4/20<HP≤50 4伤害跳防/>50 4伤害+抽对手1牌) · 6️⃣判定数字牌×1.5伤害(黄牌+流血) · 7️⃣2×流血层数伤害+恢复等量 · 0️⃣1层流血+3×流血层数+1伤害+恢复双方流血之和

**防御**：1️⃣防御至多4点 · 2️⃣反击3+1层流血 · 3️⃣翻牌判定(黄/黑/白→防御所有不免疫debuff/否则加入手牌) · 0️⃣防御所有+免疫debuff+双方均摊½伤害+debuff反弹

### 🐶 Blaze — 灼烧型狂战士

| 血量 | 被动 |
|---|---|
| **75** | 灼烧时⚔️阶段所有攻击+1🗡️（参与🛡️计算），不叠加 |

**进攻**：1️⃣4🗡️+🔥1 · 2️⃣自身🔥2→2🗡️ · 3️⃣移除所有灼伤恢复(1+层数)❤️+🔥1 · 4️⃣抽对手1牌判定(道具=4点;0️⃣→🔥2+加入手牌) · 5️⃣移除所有灼伤×2🗡️(无被动) · 6️⃣1+2×目标灼伤层数🗡️ · 7️⃣打1牌×1.5🗡️(不可防御) · 0️⃣6🗡️(不可防御,🔴属性)

**防御**：1️⃣恢复灼伤层数❤️ · 2️⃣抽1牌反击对应点数+加入手牌 · 3️⃣+2🔥+反击灼伤层数🗡️ · 0️⃣+4🔥+恢复场上灼烧数+3❤️

## 卡牌系统

| 牌面 | 数量 | 说明 |
|------|------|------|
| 🔴🟡🔵🟢 1~3 | 各3张 | 四色数字牌 |
| 🔴🟡🔵🟢 4~6 | 各2张 | 四色数字牌 |
| 🔴🟡🔵🟢 7, 0 | 各1张 | 四色数字牌 |
| ⚫ 黑牌 | 2张 | 指定颜色，搭桥 |
| ⚫ 黑+2 | 2张 | 指定颜色，抽2，搭桥 |
| ⚪ 白1~白7 | 各1张 | 匹配任意颜色 |
| ⚪ 🧪 | 4张 | 恢复5❤️，搭桥 |
| ⚪ +3🃏 | 2张 | 抽3张，搭桥 |
| ⚪ ✨ | 6张 | 净化1层debuff，搭桥 |
| ⚪ ✨✨ | 2张 | 清除所有debuff，搭桥 |
| ⚪ 🔄 | 2张 | 交换双方所有手牌，搭桥 |

**总计 91 张**

### 核心规则

- **出牌**：匹配弃牌库顶的颜色或数字
- **搭桥**：黑牌 / 🧪 / +3 / ✨ / ✨✨ / 🔄 打出后不结束回合，可连续搭桥
- **防御**：出数字 ≤ 3 且颜色匹配的牌防御；也可用搭桥牌过渡
- **灼烧**：回合结束按层数受伤并减1层（Leon 免疫伤害但正常挂标记）
- **冷冻**：被冷冻时无法防御蓝色攻击（包括被指定为蓝色的白牌）
- **流血**：防御时每层流血造成1点独立伤害（最多3层），可净化
- **净化**：✨移除1层debuff（灼烧/冷冻/流血），✨✨移除所有debuff
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
javac -sourcepath src -d out src/Card.java src/CardDeck.java src/GameCharacter.java src/Characters/RyanCharacter.java src/Characters/LeonCharacter.java src/Characters/ChanCharacter.java src/Characters/SaikiCharacter.java src/Characters/BlazeCharacter.java src/AI/AIPlayer.java src/AI/RyanAI.java src/AI/LeonAI.java src/AI/ChanAI.java src/AI/SaikiAI.java src/AI/BlazeAI.java src/GameUI.java src/GameAnim.java src/EffectEngine.java src/CharacterSelectPanel.java src/Dialogs/ChanFiveReorderDialog.java src/Dialogs/PurifyDialog.java src/Game.java src/Handlers/CharacterHandler.java src/Handlers/RyanHandler.java src/Handlers/LeonHandler.java src/Handlers/ChanHandler.java src/Handlers/SaikiHandler.java src/Handlers/BlazeHandler.java
java -cp out Game
```

## 打包发布

### macOS / Linux

```bash
chmod +x build.sh
./build.sh
```

### Windows

双击 `build.bat`，生成 `FurryBattle.zip` 发布包。

### Windows EXE

双击 `build_exe.bat`（需在 Windows 上运行，JDK 17+），生成自带 JRE 的独立 EXE。

## 项目结构

```
src/
├── Card.java                 # 卡牌类
├── CardDeck.java             # 牌堆（91张）
├── GameCharacter.java        # 角色基类
├── Characters/
│   ├── RyanCharacter.java    # Ryan 角色逻辑
│   ├── LeonCharacter.java    # Leon 角色逻辑
│   ├── ChanCharacter.java    # Chan 角色逻辑
│   ├── SaikiCharacter.java   # Saiki 角色逻辑
│   └── BlazeCharacter.java   # Blaze 角色逻辑
├── AI/
│   ├── AIPlayer.java         # AI 基类（优先级系统）
│   ├── RyanAI.java           # Ryan AI
│   ├── LeonAI.java           # Leon AI
│   ├── ChanAI.java           # Chan AI
│   ├── SaikiAI.java          # Saiki AI
│   └── BlazeAI.java          # Blaze AI
├── Handlers/
│   ├── CharacterHandler.java # Handler 基类
│   ├── RyanHandler.java      # Ryan 特殊交互
│   ├── LeonHandler.java      # Leon 判定交互
│   ├── ChanHandler.java      # Chan 交互逻辑
│   ├── SaikiHandler.java     # Saiki 交互逻辑
│   └── BlazeHandler.java     # Blaze 交互逻辑
├── Dialogs/
│   ├── ChanFiveReorderDialog.java # 5️⃣排序弹窗
│   └── PurifyDialog.java          # 净化选择弹窗
├── EffectEngine.java         # 效果引擎
├── GameUI.java               # Swing 界面
├── GameAnim.java             # 动画系统
├── CharacterSelectPanel.java # 角色选择
└── Game.java                 # 游戏主逻辑
```

## 扩展角色

1. 创建 `Characters/XCharacter.java` 继承 `GameCharacter`
2. 创建 `AI/XAI.java` 继承 `AIPlayer`
3. 创建 `Handlers/XHandler.java` 继承 `CharacterHandler`
4. 在 `Game.onCharacterSelected()` 中注册新角色

## License

MIT
