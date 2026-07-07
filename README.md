# Furry Battle v1.4

一款基于 Java Swing 的回合制卡牌对战游戏。选择你的角色，与 AI 展开策略博弈！

## v1.4 更新内容

- **新角色 🐶 Serenity**：80血，免疫❄️冷冻，低血嗜血态，正常态恢复+1❤️
  - 进攻5️⃣翻牌判定(🟡🟢恢复4❤️/其他5点🗡️) · 7️⃣嗜血无自伤 · 0️⃣弃手牌恢复+重抽4🃏+嗜血对手弃牌
  - 防御2️⃣吸血(🩸层数×2❤️) · 0️⃣翻牌判定(🟡对手❤️减半/免疫所有伤害和debuff)
- **🐶 Blaze技能重做**：85血，2️⃣不可防御 · 5️⃣自身+1🔥→2×🔥层数🗡️(含被动) · 6️⃣1.5×🔥恢复❤️+清🔥+跳防 · 7️⃣双方+2🔥→1.5×场上🔥🗡️(含被动)
- **防御1️⃣**：恢复2+🔥层数❤️+双方各+1🔥 · 防御2️⃣道具牌额外+1🔥
- **PNG图标系统**：卡牌/buff/UI按钮全部替换为PNG图标，嗜血态图标
- **游戏内弹窗**：黑牌选色、净化选择改为深色游戏内弹窗，替换JOptionPane
- **Windows兼容**：Java代码中文字替换emoji，Card.toString()用中文标识道具牌
- **飘字顺序化**：伤害/恢复/buff飘字按时间先后顺序出现
- **防御搭桥修复**：🔄交换牌/✨净化牌可在防御搭桥中使用，canDefend检查移至搭桥牌之后
- **弃牌库洗入保留顶牌**：牌库空时弃牌库洗回，保留顶上一张
- **🩸流血上限改为2层**（原3层）
- **🐺 Saiki**：防御1️⃣改为至多3点 · 7️⃣实现2+2×🩸🗡️+恢复等量❤️ · AI无🩸不出7️⃣
- **AI优化**：Blaze 5️⃣不再跳过 · 防御只剩⚫牌时也会打出 · 🔄防御可用


## 特色

- **六角色系统**：🐼 Ryan（回复型）、🐻‍❄️ Leon（灼烧型）、🐱 Chan（冷冻型）、🐺 Saiki（流血型）、🐶 Blaze（灼烧狂战）、🐶 Serenity（嗜血型），技能风格迥异
- **搭桥机制**：⚫ / 🧪 / +3🃏 / ✨ / ✨✨ / 🔄 可连续搭桥，策略深度拉满
- **攻防分区**：出牌区左攻右防，防御结束后统一清理
- **AI 对手**：角色专属 AI 策略，会根据局势搭桥、跳防、优先出牌
- **动画效果**：卡牌飞入、HP 平滑过渡、飘字伤害/恢复、🔥/❄️/🩸/嗜血标记

## 角色一览

### 🐼 Ryan — 回复型战士

| 血量 | 被动 |
|---|---|
| **70** | 回合开始恢复1❤️ |

**进攻**：1️⃣恢复3❤️ · 2️⃣3🗡️+恢复1❤️ · 3️⃣2🗡️+恢复2❤️ · 4️⃣判定(🟢/⚪/⚫→恢复1❤️+4🗡️) · 5️⃣再打1牌(恢复或1.5倍🗡️) · 6️⃣4🗡️+清debuff+抽1🃏 · 7️⃣手牌数字之和½🗡️ · 0️⃣4❤️+清debuff+抽2公示牌🗡️

**防御**：1️⃣格挡½ · 2️⃣反击2🗡️+恢复2❤️ · 3️⃣🔴免疫/其他恢复3❤️ · 0️⃣清debuff+免疫+恢复3❤️

### 🐻‍❄️ Leon — 灼烧型暴君

| 血量 | 被动 |
|---|---|
| **100** | 免疫🔥灼烧伤害（正常挂标记） |

**进攻**：1️⃣2层🔥(跳防) · 2️⃣4🗡️ · 3️⃣3🗡️+1🔥 · 4️⃣5🗡️(有🔥跳防) · 5️⃣4🗡️(有🔥+2) · 6️⃣判定(数字→对应🗡️;0/⚪/⚫→2🔥跳防) · 7️⃣弃对手1牌+6🗡️+2🔥 · 0️⃣敌方弃2+1🔥+7🗡️(跳防)+自伤2

**防御**：1️⃣1🔥+恢复2❤️ · 2️⃣反击½+抽1🃏 · 3️⃣格挡½+抽1🃏 · 0️⃣攻击方弃所有牌+双方各受等量🗡️

### 🐱 Chan — 冷冻型操控者

| 血量 | 被动 |
|---|---|
| **80** | 回合开始抽1🃏 |

**进攻**：1️⃣1🗡️+❄️冷冻(跳防) · 2️⃣4🗡️ · 3️⃣2🗡️+抽1🃏 · 4️⃣抽对手1牌交换或弃掉+2🗡️(跳防) · 5️⃣消耗2❤️+排序牌库顶5张+抽2🃏(跳防) · 6️⃣5🗡️+判定(🔵/⚪/⚫跳防) · 7️⃣6🗡️+选对手1牌判定 · 0️⃣7🗡️+❄️+抽1🃏

**防御**：1️⃣格挡½ · 2️⃣反击2🗡️+❄️ · 3️⃣翻牌库顶恢复½点数❤️+加入手牌 · 0️⃣免疫+反击½🗡️+结束攻击方回合

### 🐺 Saiki — 流血型猎手

| 血量 | 被动 |
|---|---|
| **80** | ⚔️阶段打出🟡牌，对对手施加1层🩸 |

**进攻**：1️⃣4🗡️ · 2️⃣3🗡️+恢复1❤️ · 3️⃣2🗡️+抽对手1牌(可弃掉) · 4️⃣5🗡️(对手有🩸不可防御) · 5️⃣HP分段(≤20恢复4❤️/20<HP≤50 4🗡️跳防/>50 4🗡️+抽对手1牌) · 6️⃣判定数字牌×1.5🗡️(🟡牌+🩸) · 7️⃣2+2×🩸层数🗡️+恢复等量❤️ · 0️⃣1层🩸+3×🩸层数+1🗡️+恢复双方🩸之和❤️

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

**进攻**：1️⃣恢复3❤️ · 2️⃣3🗡️(嗜血5🗡️) · 3️⃣2🗡️+恢复2❤️ · 4️⃣5🗡️(嗜血先+1🩸) · 5️⃣翻牌判定(🟡🟢→恢复4❤️/其他→5🗡️) · 6️⃣6🗡️(嗜血不可防御) · 7️⃣自伤2🗡️+5🗡️不可防御(嗜血无自伤) · 0️⃣1❤️+弃全部手牌每张+3❤️(上限9)+重抽4🃏继续⚔️(嗜血对手也弃牌重抽)

**防御**：1️⃣防御3🗡️(嗜血恢复防御点数❤️) · 2️⃣1层🩸+吸血(🩸层数×2❤️) · 3️⃣格挡½🗡️(嗜血+2) · 0️⃣翻牌判定(🟡→对手⚔️后❤️减半/其他→免疫所有🗡️和debuff)

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
- **搭桥**：⚫ / 🧪 / +3🃏 / ✨ / ✨✨ / 🔄 打出后不结束回合，可连续搭桥
- **防御**：出数字≤3且颜色匹配的牌防御；也可用搭桥牌过渡
- **🔥灼烧**：回合结束按层数受伤并减1层，最多4层（🐻‍❄️ Leon免疫伤害但正常挂标记）
- **❄️冷冻**：被冷冻时无法防御🔵蓝色攻击（包括被指定为蓝色的⚪白牌）
- **🩸流血**：防御时每层流血造成1点独立伤害（最多2层），可净化
- **净化**：✨移除1层debuff（🔥/❄️/🩸），✨✨移除所有debuff
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
│   ├── RyanCharacter.java    # 🐼 Ryan 角色逻辑
│   ├── LeonCharacter.java    # 🐻‍❄️ Leon 角色逻辑
│   ├── ChanCharacter.java    # 🐱 Chan 角色逻辑
│   ├── SaikiCharacter.java   # 🐺 Saiki 角色逻辑
│   ├── BlazeCharacter.java   # 🐶 Blaze 角色逻辑
│   └── SerenityCharacter.java # 🐶 Serenity 角色逻辑
├── AI/
│   ├── AIPlayer.java         # AI 基类（优先级系统）
│   ├── RyanAI.java           # 🐼 Ryan AI
│   ├── LeonAI.java           # 🐻‍❄️ Leon AI
│   ├── ChanAI.java           # 🐱 Chan AI
│   ├── SaikiAI.java          # 🐺 Saiki AI
│   ├── BlazeAI.java          # 🐶 Blaze AI
│   └── SerenityAI.java       # 🐶 Serenity AI
├── Handlers/
│   ├── CharacterHandler.java # Handler 基类
│   ├── RyanHandler.java      # 🐼 Ryan 特殊交互
│   ├── LeonHandler.java      # 🐻‍❄️ Leon 判定交互
│   ├── ChanHandler.java      # 🐱 Chan 交互逻辑
│   ├── SaikiHandler.java     # 🐺 Saiki 交互逻辑
│   ├── BlazeHandler.java     # 🐶 Blaze 交互逻辑
│   └── SerenityHandler.java  # 🐶 Serenity 交互逻辑
├── Dialogs/
│   ├── ChanFiveReorderDialog.java # 5️⃣排序弹窗
│   ├── PurifyDialog.java          # ✨净化选择弹窗
│   └── ColorChooserDialog.java    # ⚫黑牌选色弹窗
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
