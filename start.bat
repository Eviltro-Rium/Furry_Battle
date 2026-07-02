@echo off
chcp 65001 >nul 2>&1
cd /d "%~dp0"

if not exist out mkdir out

echo 正在编译...
javac -sourcepath src -d out src/Card.java src/CardDeck.java src/GameCharacter.java src/Characters/RyanCharacter.java src/Characters/LeonCharacter.java src/AI/AIPlayer.java src/AI/RyanAI.java src/AI/LeonAI.java src/GameUI.java src/GameAnim.java src/EffectEngine.java src/CharacterSelectPanel.java src/Game.java src/Handlers/CharacterHandler.java src/Handlers/RyanHandler.java src/Handlers/LeonHandler.java
if errorlevel 1 (
    echo 编译失败！
    pause
    exit /b 1
)

echo Main-Class: Game> out\MANIFEST.MF
echo 正在打包...
jar cfm FurryBattle.jar out\MANIFEST.MF -C out .
if errorlevel 1 (
    echo 打包失败！
    pause
    exit /b 1
)

echo 打包完成: FurryBattle.jar
echo 启动游戏...
java -jar FurryBattle.jar