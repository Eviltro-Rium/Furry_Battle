@echo off
chcp 65001 >nul 2>&1
cd /d "%~dp0"

if not exist out mkdir out

echo 正在编译...
javac -sourcepath src -d out src/Core/Card.java src/Core/CardDeck.java src/Core/GameCharacter.java src/Core/Participant.java src/Characters/RyanCharacter.java src/Characters/LeonCharacter.java src/Characters/ChanCharacter.java src/Characters/SaikiCharacter.java src/Characters/BlazeCharacter.java src/Characters/SerenityCharacter.java src/AI/AIPlayer.java src/AI/RyanAI.java src/AI/LeonAI.java src/AI/ChanAI.java src/AI/SaikiAI.java src/AI/BlazeAI.java src/AI/SerenityAI.java src/UI/GameIcons.java src/UI/GameUI.java src/UI/GameUI1v2.java src/UI/GameAnim.java src/UI/EffectEngine.java src/UI/CharacterSelectPanel.java src/UI/ModeSelectPanel.java src/UI/SkillDesc.java src/Mode/GameMode.java src/Mode/Mode1v1.java src/Mode/Mode1v2.java src/Mode/TurnManager.java src/Engine/AttackEngine.java src/Engine/DefenseEngine.java src/Engine/TurnEngine.java src/Handlers/CharacterHandler.java src/Handlers/RyanHandler.java src/Handlers/LeonHandler.java src/Handlers/ChanHandler.java src/Handlers/SaikiHandler.java src/Handlers/BlazeHandler.java src/Handlers/SerenityHandler.java src/Dialogs/ChanFiveReorderDialog.java src/Dialogs/PurifyDialog.java src/Dialogs/ColorChooserDialog.java src/Dialogs/CardTooltipDialog.java src/Game.java
if errorlevel 1 (
    echo 编译失败！
    pause
    exit /b 1
)

mkdir out\icons\card_icons 2>nul
mkdir out\icons\buff_icons 2>nul
mkdir out\icons\ui_icons 2>nul
copy src\icons\card_icons\*.png out\icons\card_icons\ >nul
copy src\icons\buff_icons\*.png out\icons\buff_icons\ >nul
copy src\icons\ui_icons\*.png out\icons\ui_icons\ >nul

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