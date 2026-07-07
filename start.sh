#!/bin/bash
cd "$(dirname "$0")"
JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-26.jdk/Contents/Home
mkdir -p out
$JAVA_HOME/bin/javac -sourcepath src -d out src/Card.java src/CardDeck.java src/GameCharacter.java src/Characters/RyanCharacter.java src/Characters/LeonCharacter.java src/Characters/ChanCharacter.java src/Characters/SaikiCharacter.java src/Characters/BlazeCharacter.java src/Characters/SerenityCharacter.java src/AI/AIPlayer.java src/AI/RyanAI.java src/AI/LeonAI.java src/AI/ChanAI.java src/AI/SaikiAI.java src/AI/BlazeAI.java src/AI/SerenityAI.java src/GameIcons.java src/GameUI.java src/GameAnim.java src/EffectEngine.java src/CharacterSelectPanel.java src/Game.java src/Handlers/CharacterHandler.java src/Handlers/RyanHandler.java src/Handlers/LeonHandler.java src/Handlers/ChanHandler.java src/Handlers/SaikiHandler.java src/Handlers/BlazeHandler.java src/Handlers/SerenityHandler.java src/Dialogs/ChanFiveReorderDialog.java src/Dialogs/PurifyDialog.java src/Dialogs/ColorChooserDialog.java && \
mkdir -p out/icons/card_icons out/icons/buff_icons out/icons/ui_icons && \
cp src/icons/card_icons/*.png out/icons/card_icons/ && \
cp src/icons/buff_icons/*.png out/icons/buff_icons/ && \
cp src/icons/ui_icons/*.png out/icons/ui_icons/ && \
echo "Main-Class: Game" > out/MANIFEST.MF && \
$JAVA_HOME/bin/jar cfm FurryBattle.jar out/MANIFEST.MF -C out . && \
echo "打包完成: FurryBattle.jar" && \
$JAVA_HOME/bin/java -jar FurryBattle.jar
