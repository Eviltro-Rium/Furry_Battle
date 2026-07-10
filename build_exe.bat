@echo off
chcp 65001 >nul 2>&1
cd /d "%~dp0"

set APP_NAME=FurryBattle
set MAIN_CLASS=Game
set OUT_DIR=out
set JAR_NAME=%APP_NAME%.jar
set EXE_DIR=exe_output

echo === Furry Battle 打包 EXE ===

rem 1. 清理
if exist "%OUT_DIR%" rmdir /s /q "%OUT_DIR%"
if exist "%EXE_DIR%" rmdir /s /q "%EXE_DIR%"
mkdir "%OUT_DIR%"

rem 2. 编译
echo [1/4] 编译中...
javac -sourcepath src -d "%OUT_DIR%" src/Core/Card.java src/Core/CardDeck.java src/Core/GameCharacter.java src/Core/Participant.java src/Characters/RyanCharacter.java src/Characters/LeonCharacter.java src/Characters/ChanCharacter.java src/Characters/SaikiCharacter.java src/Characters/BlazeCharacter.java src/Characters/SerenityCharacter.java src/AI/AIPlayer.java src/AI/RyanAI.java src/AI/LeonAI.java src/AI/ChanAI.java src/AI/SaikiAI.java src/AI/BlazeAI.java src/AI/SerenityAI.java src/UI/GameIcons.java src/UI/GameUI.java src/UI/GameUI1v2.java src/UI/GameAnim.java src/UI/EffectEngine.java src/UI/CharacterSelectPanel.java src/UI/ModeSelectPanel.java src/UI/SkillDesc.java src/Mode/GameMode.java src/Mode/Mode1v1.java src/Mode/Mode1v2.java src/Mode/TurnManager.java src/Engine/AttackEngine.java src/Engine/DefenseEngine.java src/Engine/TurnEngine.java src/Handlers/CharacterHandler.java src/Handlers/RyanHandler.java src/Handlers/LeonHandler.java src/Handlers/ChanHandler.java src/Handlers/SaikiHandler.java src/Handlers/BlazeHandler.java src/Handlers/SerenityHandler.java src/Dialogs/ChanFiveReorderDialog.java src/Dialogs/PurifyDialog.java src/Dialogs/ColorChooserDialog.java src/Dialogs/CardTooltipDialog.java src/Game.java
if errorlevel 1 (
    echo 编译失败！
    pause
    exit /b 1
)

rem 3. 复制图标资源
echo [2/4] 复制图标资源...
mkdir "%OUT_DIR%\icons\card_icons" 2>nul
mkdir "%OUT_DIR%\icons\buff_icons" 2>nul
mkdir "%OUT_DIR%\icons\ui_icons" 2>nul
copy src\icons\card_icons\*.png "%OUT_DIR%\icons\card_icons\" >nul
copy src\icons\buff_icons\*.png "%OUT_DIR%\icons\buff_icons\" >nul
copy src\icons\ui_icons\*.png "%OUT_DIR%\icons\ui_icons\" >nul

rem 4. 打包 JAR
echo [3/4] 打包JAR...
echo Main-Class: %MAIN_CLASS%> "%OUT_DIR%\MANIFEST.MF"
jar cfm "%JAR_NAME%" "%OUT_DIR%\MANIFEST.MF" -C "%OUT_DIR%" .
if errorlevel 1 (
    echo 打包失败！
    pause
    exit /b 1
)

rem 5. 用 jpackage 生成 EXE
echo [4/4] 生成EXE...
jpackage --input . ^
    --name "%APP_NAME%" ^
    --main-jar "%JAR_NAME%" ^
    --main-class %MAIN_CLASS% ^
    --type app-image ^
    --dest "%EXE_DIR%" ^
    --app-version "2.0" ^
    --vendor "FurryBattle" ^
    --win-dir-chooser ^
    --win-menu ^
    --win-shortcut ^
    --win-console ^
    --icon NONE

if errorlevel 1 (
    echo.
    echo jpackage 失败！请确认：
    echo   1. JDK 17+ 已安装且 javac/jpackage 在 PATH 中
    echo   2. 此脚本需在 Windows 上运行
    echo   3. WIX Toolset 未安装时无法生成 msi，但 app-image 模式不需要
    pause
    exit /b 1
)

echo.
echo ✅ 打包完成！
echo   EXE目录: %EXE_DIR%\%APP_NAME%\
echo   启动程序: %EXE_DIR%\%APP_NAME%\%APP_NAME%.exe
echo.
echo 将 %EXE_DIR%\%APP_NAME% 文件夹整体复制即可分发，无需安装 JRE
pause
