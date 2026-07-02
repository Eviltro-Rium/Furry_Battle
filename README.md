# Furry Battle v1.0

一款基于 Java Swing 的回合制卡牌对战游戏。选择你的角色，与 AI 展开策略博弈！

## 截图

> 选择角色后进入对战，出牌区左侧为进攻、右侧为防御，攻防一目了然。

## 特色

- **双角色系统**：Ryan（回复型）与 Leon（灼烧型），技能风格迥异
- **搭桥机制**：黑牌 / 🧪 / +3 可连续搭桥，策略深度拉满
- **攻防分区**：出牌区左攻右防，防御结束后统一清理
- **AI 对手**：角色专属 AI 策略，会根据局势搭桥、跳防、优先出牌
- **动画效果**：卡牌飞入、HP 平滑过渡、飘字伤害/恢复、灼烧标记

## 角色一览

### 🐼 Ryan — 回复型战士

| | 血量 | 被动 |
|---|---|---|
| **70** | 回合开始恢复 1 ❤️ |

**进攻**：1️⃣4伤害 · 2️⃣1恢复+3伤害 · 3️⃣1恢复+抽1 · 4️⃣判定(绿/白/黑→1恢复+4伤害) · 5️⃣再打1牌(恢复或1.5倍伤害) · 6️⃣4伤害+清debuff+抽1 · 7️⃣手牌数字之和½伤害 · 0️⃣4恢复+清debuff+抽2公示牌伤害

**防御**：1️⃣格挡½ · 2️⃣反击2+恢复2 · 3️⃣红色免疫/其他恢复3 · 0️⃣清debuff+免疫+恢复3

### 🐻‍❄️ Leon — 灼烧型暴君

| | 血量 | 被动 |
|---|---|---|
| **100** | 免疫灼烧伤害（正常挂标记） |

**进攻**：1️⃣2层灼伤(跳防) · 2️⃣4伤害 · 3️⃣3伤害+1灼伤 · 4️⃣5伤害(有灼伤跳防) · 5️⃣4伤害(有灼伤+2) · 6️⃣判定(数字→对应伤害;0/白/黑→2灼伤跳防) · 7️⃣弃对手1牌+6伤害+2灼伤 · 0️⃣敌方弃2+1灼伤+7伤害(跳防)+自伤2

**防御**：1️⃣1灼伤+恢复2 · 2️⃣反击½+抽1 · 3️⃣格挡½+抽1 · 0️⃣攻击方弃所有牌+双方各受等量伤害

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

**总计 83 张**

### 核心规则

- **出牌**：匹配弃牌库顶的颜色或数字
- **搭桥**：黑牌 / 🧪 / +3 打出后不结束回合，可连续搭桥
- **防御**：出数字 ≤ 3 且颜色匹配的牌防御；也可用搭桥牌过渡
- **灼烧**：回合结束按层数受伤并减1层（Leon 免疫伤害但正常挂标记）
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
javac -sourcepath src -d out src/Card.java src/CardDeck.java src/GameCharacter.java src/Characters/RyanCharacter.java src/Characters/LeonCharacter.java src/AI/AIPlayer.java src/AI/RyanAI.java src/AI/LeonAI.java src/GameUI.java src/GameAnim.java src/EffectEngine.java src/CharacterSelectPanel.java src/Game.java src/Handlers/CharacterHandler.java src/Handlers/RyanHandler.java src/Handlers/LeonHandler.java
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

## 项目结构

```
src/
├── Card.java                 # 卡牌类
├── CardDeck.java             # 牌堆（83张）
├── GameCharacter.java        # 角色基类
├── Characters/
│   ├── RyanCharacter.java    # Ryan 角色逻辑
│   └── LeonCharacter.java    # Leon 角色逻辑
├── AI/
│   ├── AIPlayer.java         # AI 基类
│   ├── RyanAI.java           # Ryan AI
│   └── LeonAI.java           # Leon AI
├── Handlers/
│   ├── CharacterHandler.java # Handler 基类
│   ├── RyanHandler.java      # Ryan 特殊交互
│   └── LeonHandler.java      # Leon 判定交互
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