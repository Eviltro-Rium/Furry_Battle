@echo off
chcp 65001 >nul 2>&1
cd /d "%~dp0"

set OUT_DIR=out
set JAR_NAME=FurryBattle.jar
set DIST_DIR=dist

echo === Furry Battle 一键封装 ===

rem 1. 清理
if exist "%OUT_DIR%" rmdir /s /q "%OUT_DIR%"
if exist "%DIST_DIR%" rmdir /s /q "%DIST_DIR%"
mkdir "%OUT_DIR%"
mkdir "%DIST_DIR%"

rem 2. 编译
echo [1/4] 编译中...
javac -sourcepath src -d "%OUT_DIR%" src/Card.java src/CardDeck.java src/GameCharacter.java src/Characters/RyanCharacter.java src/Characters/LeonCharacter.java src/Characters/ChanCharacter.java src/Characters/SaikiCharacter.java src/Characters/BlazeCharacter.java src/AI/AIPlayer.java src/AI/RyanAI.java src/AI/LeonAI.java src/AI/ChanAI.java src/AI/SaikiAI.java src/AI/BlazeAI.java src/GameUI.java src/GameAnim.java src/EffectEngine.java src/CharacterSelectPanel.java src/Dialogs/ChanFiveReorderDialog.java src/Dialogs/PurifyDialog.java src/Game.java src/Handlers/CharacterHandler.java src/Handlers/RyanHandler.java src/Handlers/LeonHandler.java src/Handlers/ChanHandler.java src/Handlers/SaikiHandler.java src/Handlers/BlazeHandler.java
if errorlevel 1 (
    echo 编译失败！
    pause
    exit /b 1
)

rem 3. 打包JAR
echo [2/4] 打包JAR...
echo Main-Class: Game> "%OUT_DIR%\MANIFEST.MF"
jar cfm "%JAR_NAME%" "%OUT_DIR%\MANIFEST.MF" -C "%OUT_DIR%" .
if errorlevel 1 (
    echo 打包失败！
    pause
    exit /b 1
)

rem 4. 组装发布目录
echo [3/4] 组装发布包...
copy "%JAR_NAME%" "%DIST_DIR%\" >nul
xcopy /e /i /q draft "%DIST_DIR%\draft\" >nul

(
echo @echo off
echo chcp 65001 ^>nul 2^>^&1
echo cd /d "%%~dp0"
echo java -jar FurryBattle.jar
echo pause
) > "%DIST_DIR%\运行.bat"

(
echo Furry Battle - 卡牌对战游戏
echo ============================
echo.
echo 运行方式：
echo   双击「运行.bat」或在命令行执行 java -jar FurryBattle.jar
echo   需要安装 JDK 17 或更高版本
echo.
echo 规则说明见 draft\ 目录下的文件
) > "%DIST_DIR%\README.txt"

rem 5. 打zip
echo [4/4] 压缩发布包...
cd "%DIST_DIR%"
jar cfM "../FurryBattle.zip" .
cd ..

echo.
echo 封装完成！
echo   JAR: %JAR_NAME%
echo   发布包: FurryBattle.zip
echo   内容: FurryBattle.jar + 规则文档 + 启动脚本
pause