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
echo [1/3] 编译中...
javac -sourcepath src -d "%OUT_DIR%" src/Card.java src/CardDeck.java src/GameCharacter.java src/Characters/RyanCharacter.java src/Characters/LeonCharacter.java src/Characters/ChanCharacter.java src/AI/AIPlayer.java src/AI/RyanAI.java src/AI/LeonAI.java src/AI/ChanAI.java src/GameUI.java src/GameAnim.java src/EffectEngine.java src/CharacterSelectPanel.java src/Game.java src/Handlers/CharacterHandler.java src/Handlers/RyanHandler.java src/Handlers/LeonHandler.java src/Handlers/ChanHandler.java
if errorlevel 1 (
    echo 编译失败！
    pause
    exit /b 1
)

rem 3. 打包 JAR
echo [2/3] 打包JAR...
echo Main-Class: %MAIN_CLASS%> "%OUT_DIR%\MANIFEST.MF"
jar cfm "%JAR_NAME%" "%OUT_DIR%\MANIFEST.MF" -C "%OUT_DIR%" .
if errorlevel 1 (
    echo 打包失败！
    pause
    exit /b 1
)

rem 4. 用 jpackage 生成 EXE
echo [3/3] 生成EXE...
jpackage --input . ^
    --name "%APP_NAME%" ^
    --main-jar "%JAR_NAME%" ^
    --main-class %MAIN_CLASS% ^
    --type app-image ^
    --dest "%EXE_DIR%" ^
    --app-version "1.0" ^
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