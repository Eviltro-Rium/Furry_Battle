#!/bin/bash
cd "$(dirname "$0")"
JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-26.jdk/Contents/Home
mkdir -p out
$JAVA_HOME/bin/javac -sourcepath src -d out src/Card.java src/CardDeck.java src/GameCharacter.java src/Characters/RyanCharacter.java src/Characters/LeonCharacter.java src/Characters/ChanCharacter.java src/AI/AIPlayer.java src/AI/RyanAI.java src/AI/LeonAI.java src/AI/ChanAI.java src/GameUI.java src/GameAnim.java src/EffectEngine.java src/CharacterSelectPanel.java src/Game.java src/Handlers/CharacterHandler.java src/Handlers/RyanHandler.java src/Handlers/LeonHandler.java src/Handlers/ChanHandler.java src/Dialogs/ChanFiveReorderDialog.java && \
echo "Main-Class: Game" > out/MANIFEST.MF && \
$JAVA_HOME/bin/jar cfm FurryBattle.jar out/MANIFEST.MF -C out . && \
echo "打包完成: FurryBattle.jar" && \
$JAVA_HOME/bin/java -jar FurryBattle.jar
