#!/bin/bash
cd "$(dirname "$0")"
JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-26.jdk/Contents/Home
OUT_DIR=out
JAR_NAME=FurryBattle.jar
DIST_DIR=dist

echo "=== Furry Battle 一键封装 ==="

# 1. 清理
rm -rf "$OUT_DIR" "$DIST_DIR"
mkdir -p "$OUT_DIR" "$DIST_DIR"

# 2. 编译
echo "[1/4] 编译中..."
$JAVA_HOME/bin/javac -sourcepath src -d "$OUT_DIR" \
  src/Card.java src/CardDeck.java src/GameCharacter.java \
  src/Characters/RyanCharacter.java src/Characters/LeonCharacter.java src/Characters/ChanCharacter.java src/Characters/SaikiCharacter.java src/Characters/BlazeCharacter.java \
  src/AI/AIPlayer.java src/AI/RyanAI.java src/AI/LeonAI.java src/AI/ChanAI.java src/AI/SaikiAI.java src/AI/BlazeAI.java \
  src/GameIcons.java src/GameUI.java src/GameAnim.java src/EffectEngine.java \
  src/CharacterSelectPanel.java src/Dialogs/ChanFiveReorderDialog.java src/Dialogs/PurifyDialog.java src/Game.java \
  src/Handlers/CharacterHandler.java src/Handlers/RyanHandler.java src/Handlers/LeonHandler.java src/Handlers/ChanHandler.java src/Handlers/SaikiHandler.java src/Handlers/BlazeHandler.java

mkdir -p "$OUT_DIR/icons/card_icons" "$OUT_DIR/icons/buff_icons" "$OUT_DIR/icons/ui_icons"
cp src/icons/card_icons/*.png "$OUT_DIR/icons/card_icons/"
cp src/icons/buff_icons/*.png "$OUT_DIR/icons/buff_icons/"
cp src/icons/ui_icons/*.png "$OUT_DIR/icons/ui_icons/"

if [ $? -ne 0 ]; then
  echo "❌ 编译失败"
  exit 1
fi

# 3. 打包JAR
echo "[2/4] 打包JAR..."
echo "Main-Class: Game" > "$OUT_DIR/MANIFEST.MF"
$JAVA_HOME/bin/jar cfm "$JAR_NAME" "$OUT_DIR/MANIFEST.MF" -C "$OUT_DIR" .

if [ $? -ne 0 ]; then
  echo "❌ 打包失败"
  exit 1
fi

# 4. 组装发布目录
echo "[3/4] 组装发布包..."
cp "$JAR_NAME" "$DIST_DIR/"
cp -r draft "$DIST_DIR/"

cat > "$DIST_DIR/运行.command" << 'EOF'
#!/bin/bash
cd "$(dirname "$0")"
JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-26.jdk/Contents/Home
if [ -x "$JAVA_HOME/bin/java" ]; then
  "$JAVA_HOME/bin/java" -jar FurryBattle.jar
else
  java -jar FurryBattle.jar
fi
EOF
chmod +x "$DIST_DIR/运行.command"

cat > "$DIST_DIR/README.txt" << 'EOF'
Furry Battle - 卡牌对战游戏
============================

运行方式：
  macOS: 双击「运行.command」或在终端执行 java -jar FurryBattle.jar
  需要安装 JDK 17 或更高版本

规则说明见 draft/ 目录下的 Markdown 文件
EOF

# 5. 打zip
echo "[4/4] 压缩发布包..."
cd "$DIST_DIR"
zip -r -q "../FurryBattle.zip" .
cd ..

SIZE=$(du -h FurryBattle.zip | cut -f1)
echo ""
echo "✅ 封装完成！"
echo "   JAR: $JAR_NAME"
echo "   发布包: FurryBattle.zip ($SIZE)"
echo "   内容: FurryBattle.jar + 规则文档 + 启动脚本"