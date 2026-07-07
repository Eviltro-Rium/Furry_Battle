# Furry Battle v1.4

一款基于 Java Swing 的回合制卡牌对战游戏。选择你的角色，与 AI 展开策略博弈！

## v1.4 更新内容

- **新角色 Serenity**：80血，免疫冷冻，低血嗜血态，正常态恢复+1
  - 进攻5️⃣翻牌判定(黄/绿恢复，其他5点伤) · 7️⃣嗜血无自伤 · 0️⃣弃手牌恢复+重抽4+嗜血对手弃牌
  - 防御2️⃣吸血(流血层数×2) · 0️⃣翻牌判定(黄牌对手血量减半)
- **Blaze技能重做**：85血，2️⃣不可防御 · 5️⃣自身+1灼伤→2×灼伤层数伤(含被动) · 6️⃣1.5×灼伤恢复+清灼伤+跳防 · 7️⃣双方+2灼伤→1.5×场上灼伤伤(含被动)
- **防御1️⃣**：恢复2+灼伤层数命+双方各+1灼伤 · 防御2️⃣道具牌额外+1灼伤
- **PNG图标系统**：卡牌/buff/UI按钮全部替换为PNG图标，嗜血态图标
- **游戏内弹窗**：黑牌选色、净化选择改为深色游戏内弹窗，替换JOptionPane
- **Windows兼容**：所有文字替换emoji，Card.toString()用中文标识道具牌
- **飘字顺序化**：伤害/恢复/buff飘字按时间先后顺序出现
- **防御搭桥修复**：交换牌/净化牌可在防御搭桥中使用，canDefend检查移至搭桥牌之后
- **弃牌库洗入保留顶牌**：牌库空时弃牌库洗回，保留顶上一张
- **流血上限改为2层**（原3层）
- **Saiki**：防御1️⃣改为至多3点 · 7️⃣实现2+2×流血伤+恢复等量命 · AI无流血不出7️⃣
- **AI优化**：Blaze 5️⃣不再跳过 · 防御只剩黑牌时也会打出 · 交换牌防御可用

## v1.3 更新内容

- **Blaze角色**：灼烧型狂战士，被动灼烧时攻击+1
- **交换牌**：2张白色道具牌，交换双方所有手牌，可搭桥
- **浅色按钮UI**：所有按钮改为浅色系+深色文字，禁用态灰色显示
- **防误触机制**：300ms按钮冷却 + 动画期间按钮自动禁用
- **飘字防重叠**：同区域连续飘字自动垂直错开
- **AI优化**：满血不用药水、手牌不大于对手时才出交换牌
- **防御结算顺序**：先施加灼伤再结算反击伤害/回血

## v1.2 更新内容

- **Saiki角色**：流血型猎手，被动打出黄牌施加流血
- **流血机制**：防御时每层流血造成1点独立伤害，可净化
- **净化牌**：净化1层debuff+搭桥（6张）、超级净化清除所有debuff+搭桥（2张）
- **药水牌**：4张，恢复5命+搭桥
- **+3牌**：2张，抽3张+搭桥
- **道具牌value=-1**：避免道具牌误触0️⃣技能
- **AI策略优化**：优先级系统替代简单匹配，角色专属策略

## 特色

- **六角色系统**：Ryan（回复型）、Leon（灼烧型）、Chan（冷冻型）、Saiki（流血型）、Blaze（灼烧狂战）、Serenity（嗜血型），技能风格迥异
- **搭桥机制**：黑牌 / 药水 / +3 / 净化 / 超净 / 交换 可连续搭桥，策略深度拉满
- **攻防分区**：出牌区左攻右防，防御结束后统一清理
- **AI 对手**：角色专属 AI 策略，会根据局势搭桥、跳防、优先出牌
- **动画效果**：卡牌飞入、HP 平滑过渡、飘字伤害/恢复、灼烧/冷冻/流血/嗜血标记

## 角色一览

### Ryan — 回复型战士

| 血量 | 被动 |
|---|---|
| **70** | 回合开始恢复1命 |

**进攻**：1恢复3 · 2伤3+恢复1 · 3伤2+恢复2 · 4判定(绿/白/黑→恢复1+4伤) · 5再打1牌(恢复或1.5倍伤) · 6伤4+清debuff+抽1 · 7手牌数字之和半伤 · 0恢复4+清debuff+抽2公示牌伤

**防御**：1格挡半 · 2反击2+恢复2 · 3红色免疫/其他恢复3 · 0清debuff+免疫+恢复3

### Leon — 灼烧型暴君

| 血量 | 被动 |
|---|---|
| **100** | 免疫灼烧伤害（正常挂标记） |

**进攻**：1灼伤2层(跳防) · 2伤4 · 3伤3+1灼伤 · 4伤5(有灼伤跳防) · 5伤4(有灼伤+2) · 6判定(数字→对应伤;0/白/黑→2灼伤跳防) · 7弃对手1牌+6伤+2灼伤 · 0敌方弃2+1灼伤+7伤(跳防)+自伤2

**防御**：1灼伤1+恢复2 · 2反击半+抽1 · 3格挡半+抽1 · 0攻击方弃所有牌+双方各受等量伤

### Chan — 冷冻型操控者

| 血量 | 被动 |
|---|---|
| **80** | 回合开始抽1牌 |

**进攻**：1伤1+冷冻(跳防) · 2伤4 · 3伤2+抽1 · 4抽对手1牌交换或弃掉+2伤(跳防) · 5消耗2命+排序牌库顶5张+抽2(跳防) · 6伤5+判定(蓝/白/黑跳防) · 7伤6+选对手1牌判定 · 0伤7+冷冻+抽1

**防御**：1格挡半 · 2反击2+冷冻 · 3翻牌库顶恢复半点数+加入手牌 · 0免疫+反击半+结束攻击方回合

### Saiki — 流血型猎手

| 血量 | 被动 |
|---|---|
| **80** | 进攻阶段打出黄牌，对对手施加1层流血 |

**进攻**：1伤4 · 2伤3+恢复1 · 3伤2+抽对手1牌(可弃掉) · 4伤5(对手有流血不可防御) · 5HP分段(低血恢复/中血4伤跳防/高血4伤+抽对手1牌) · 6判定数字牌×1.5伤(黄牌+流血) · 7伤2+2×流血层数+恢复等量命 · 0流血1+3×流血层数+1伤+恢复双方流血之和

**防御**：1防御至多3点 · 2反击3+1层流血 · 3翻牌判定(黄/黑/白→防御所有不免疫debuff/否则加入手牌) · 0防御所有+免疫debuff+双方均摊半伤+debuff反弹

### Blaze — 灼烧型狂战士

| 血量 | 被动 |
|---|---|
| **85** | 灼烧时进攻阶段所有攻击+1伤（参与防御计算），不叠加 |

**进攻**：1伤4(灼烧+1) · 2伤2(不可防御)+自身灼烧2 · 3伤3+双方灼烧1 · 4抽对手1牌判定(道具=4点;0→双方灼烧1+加入手牌) · 5自身灼烧1→2×灼伤层数伤(含被动) · 6恢复1.5×灼伤层数命+对手灼烧1+清灼伤(跳防) · 7双方灼烧2→1.5×场上灼伤伤(含被动) · 0伤6(不可防御)+灼烧2

**防御**：1恢复2+灼伤层数命+双方灼烧1 · 2抽1牌反击点数(道具=4+灼烧1)+加入手牌 · 3灼烧2+反击场上灼伤层数伤 · 0灼烧4+格挡半+恢复场上灼烧+3命

### Serenity — 嗜血型战士

| 血量 | 被动 |
|---|---|
| **80** | 免疫冷冻；命<30时嗜血态(不可净化)；正常态恢复+1 |

**进攻**：1恢复3 · 2伤3(嗜血5) · 3伤2+恢复2 · 4伤5(嗜血先+1流血) · 5翻牌判定(黄/绿→恢复4/其他→5伤) · 6伤6(嗜血不可防御) · 7自伤2+5伤不可防御(嗜血无自伤) · 0恢复1+弃全部手牌每张+3命(上限9)+重抽4继续(嗜血对手也弃牌重抽)

**防御**：1防御3(嗜血恢复防御点数) · 2流血1+吸血(流血层数×2) · 3格挡半(嗜血+2) · 0翻牌判定(黄→对手命量减半/其他→免疫所有伤害和debuff)

## 卡牌系统

| 牌面 | 数量 | 说明 |
|------|------|------|
| 红黄蓝绿 1~3 | 各3张 | 四色数字牌 |
| 红黄蓝绿 4~6 | 各2张 | 四色数字牌 |
| 红黄蓝绿 7, 0 | 各1张 | 四色数字牌 |
| 黑牌 | 2张 | 指定颜色，搭桥 |
| 黑+2 | 2张 | 指定颜色，抽2，搭桥 |
| 白1~白7 | 各1张 | 匹配任意颜色 |
| 药水 | 4张 | 恢复5命，搭桥 |
| +3 | 2张 | 抽3张，搭桥 |
| 净化 | 6张 | 净化1层debuff，搭桥 |
| 超净 | 2张 | 清除所有debuff，搭桥 |
| 交换 | 2张 | 交换双方所有手牌，搭桥 |

**总计 91 张**

### 核心规则

- **出牌**：匹配弃牌库顶的颜色或数字
- **搭桥**：黑牌 / 药水 / +3 / 净化 / 超净 / 交换 打出后不结束回合，可连续搭桥
- **防御**：出数字≤3且颜色匹配的牌防御；也可用搭桥牌过渡
- **灼烧**：回合结束按层数受伤并减1层，最多4层（Leon免疫伤害但正常挂标记）
- **冷冻**：被冷冻时无法防御蓝色攻击（包括被指定为蓝色的白牌）
- **流血**：防御时每层流血造成1点独立伤害（最多2层），可净化
- **净化**：净化移除1层debuff（灼烧/冷冻/流血），超净移除所有debuff
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
javac -sourcepath src -d out src/Card.java src/CardDeck.java src/GameCharacter.java src/Characters/RyanCharacter.java src/Characters/LeonCharacter.java src/Characters/ChanCharacter.java src/Characters/SaikiCharacter.java src/Characters/BlazeCharacter.java src/Characters/SerenityCharacter.java src/AI/AIPlayer.java src/AI/RyanAI.java src/AI/LeonAI.java src/AI/ChanAI.java src/AI/SaikiAI.java src/AI/BlazeAI.java src/AI/SerenityAI.java src/GameIcons.java src/GameUI.java src/GameAnim.java src/EffectEngine.java src/CharacterSelectPanel.java src/Dialogs/ChanFiveReorderDialog.java src/Dialogs/PurifyDialog.java src/Dialogs/ColorChooserDialog.java src/Game.java src/Handlers/CharacterHandler.java src/Handlers/RyanHandler.java src/Handlers/LeonHandler.java src/Handlers/ChanHandler.java src/Handlers/SaikiHandler.java src/Handlers/BlazeHandler.java src/Handlers/SerenityHandler.java
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
├── GameIcons.java            # 图标加载工具类
├── Characters/
│   ├── RyanCharacter.java    # Ryan 角色逻辑
│   ├── LeonCharacter.java    # Leon 角色逻辑
│   ├── ChanCharacter.java    # Chan 角色逻辑
│   ├── SaikiCharacter.java   # Saiki 角色逻辑
│   ├── BlazeCharacter.java   # Blaze 角色逻辑
│   └── SerenityCharacter.java # Serenity 角色逻辑
├── AI/
│   ├── AIPlayer.java         # AI 基类（优先级系统）
│   ├── RyanAI.java           # Ryan AI
│   ├── LeonAI.java           # Leon AI
│   ├── ChanAI.java           # Chan AI
│   ├── SaikiAI.java          # Saiki AI
│   ├── BlazeAI.java          # Blaze AI
│   └── SerenityAI.java       # Serenity AI
├── Handlers/
│   ├── CharacterHandler.java # Handler 基类
│   ├── RyanHandler.java      # Ryan 特殊交互
│   ├── LeonHandler.java      # Leon 判定交互
│   ├── ChanHandler.java      # Chan 交互逻辑
│   ├── SaikiHandler.java     # Saiki 交互逻辑
│   ├── BlazeHandler.java     # Blaze 交互逻辑
│   └── SerenityHandler.java  # Serenity 交互逻辑
├── Dialogs/
│   ├── ChanFiveReorderDialog.java # 5排序弹窗
│   ├── PurifyDialog.java          # 净化选择弹窗
│   └── ColorChooserDialog.java    # 黑牌选色弹窗
├── EffectEngine.java         # 效果引擎
├── GameUI.java               # Swing 界面
├── GameAnim.java             # 动画系统
├── CharacterSelectPanel.java # 角色选择
├── Game.java                 # 游戏主逻辑
└── icons/
    ├── card_icons/           # 卡牌图标PNG
    ├── buff_icons/           # buff标记PNG
    └── ui_icons/             # UI按钮图标PNG
```

## 扩展角色

1. 创建 `Characters/XCharacter.java` 继承 `GameCharacter`
2. 创建 `AI/XAI.java` 继承 `AIPlayer`
3. 创建 `Handlers/XHandler.java` 继承 `CharacterHandler`
4. 在 `Game.onCharacterSelected()` 中注册新角色

## 致谢

- 游戏图标来自 [Flaticon](https://www.flaticon.com/)，由 [pocike](https://www.flaticon.com/authors/pocike) 创作

## License

MIT
