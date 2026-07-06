import javax.swing.*;
import java.awt.*;
import java.util.List;

public class BlazeHandler extends CharacterHandler {

    BlazeHandler(Game game) {
        super(game);
    }

    @Override
    void doSevenChoice(int aiCardIndex) {
        if (game.currentPhase != Game.Phase.PLAYER_SEVEN_CHOICE) return;
        List<Card> oppHand = game.ai.getHand();
        if (aiCardIndex < 0 || aiCardIndex >= oppHand.size()) return;
        Card chosen = oppHand.remove(aiCardIndex);
        game.discardPile.addLast(chosen);
        game.pendingFiveChoice = false;
        game.forceOpponentDiscardOne = false;
        game.showAttackDesc("7️⃣ 弃掉AI的" + chosen);
        GameAnim.playFloatingText(game, "弃" + chosen, new Color(255, 60, 60),
            new Point(game.getWidth() / 2, game.getHeight() / 3 - 30));
        if (game.pendingAttack != null && game.pendingAttack.skipDefense) {
            game.showDefendDesc("跳过防御");
            Timer skipTimer = new Timer(Game.DELAY_SKIP, e -> {
                ((Timer)e.getSource()).stop();
                game.resolvePostDefense(game.playerChar, game.aiChar);
                game.clearAIZones();
                game.currentPhase = Game.Phase.PLAYER_PLAY;
                game.updateDisplay();
            });
            skipTimer.start();
        } else {
            game.currentPhase = Game.Phase.PLAYER_PLAY;
            game.clearAIZones();
            game.updateDisplay();
        }
    }

    @Override
    void doFiveChoiceHeal() {
        if (game.pendingFiveChoice) {
            game.pendingFiveChoice = false;
            game.forceOpponentDiscardOne = false;
            game.currentPhase = Game.Phase.PLAYER_PLAY;
            game.clearAIZones();
            game.updateDisplay();
        }
    }

    @Override
    void doFiveChoiceDamage() {
        if (game.pendingFiveChoice) {
            game.pendingFiveChoice = false;
            game.forceOpponentDiscardOne = false;
            game.currentPhase = Game.Phase.PLAYER_PLAY;
            game.clearAIZones();
            game.updateDisplay();
        }
    }

    void handleBlazeFourDraw(GameCharacter self, GameCharacter opponent,
                              List<Card> selfHand, List<Card> oppHand, Runnable onDone) {
        if (oppHand.isEmpty()) {
            game.showAttackDesc("4️⃣ 对手无手牌 → 2点伤害");
            game.pendingAttack = new GameCharacter.AttackResult();
            game.pendingAttack.damage = 2;
            game.pendingAttack.desc = "4️⃣ 对手无手牌 → 2点伤害";
            onDone.run();
            return;
        }

        if (self != game.playerChar) {
            handleAIFourDraw(self, opponent, selfHand, oppHand, onDone);
        } else {
            handlePlayerFourDraw(self, opponent, selfHand, oppHand, onDone);
        }
    }

    private void handleAIFourDraw(GameCharacter self, GameCharacter opponent,
                                    List<Card> selfHand, List<Card> oppHand, Runnable onDone) {
        int idx = (int) (Math.random() * oppHand.size());
        Card drawn = oppHand.remove(idx);

        int dmg;
        if (drawn.getValue() == 0 && !drawn.isItemCard()) {
            selfHand.add(drawn);
            opponent.addBurn(1);
            self.addBurn(1);
            game.showAttackDesc("4️⃣ 抽到" + drawn + " → 0️⃣加入手牌，双方🔥1，跳过防御");
            game.pendingAttack = new GameCharacter.AttackResult();
            game.pendingAttack.skipDefense = true;
            game.pendingAttack.damage = 0;
            game.pendingAttack.desc = "4️⃣ 抽到0️⃣ → 加入手牌+双方🔥1";
            GameAnim.playFloatingText(game, "抽" + drawn, new Color(100, 180, 255),
                new Point(game.getWidth() / 2, game.getHeight() / 3 - 30));
            GameAnim.playFloatingText(game, "🔥+1", new Color(255, 140, 0),
                opponent == game.playerChar
                    ? new Point(game.getWidth() / 2, game.getHeight() * 3 / 4 - 60)
                    : new Point(game.getWidth() / 2, game.getHeight() / 3 - 60));
            onDone.run();
        } else {
            dmg = drawn.isItemCard() ? 4 : drawn.getValue();
            int burnBonus = self.getBurnStacks() > 0 ? 1 : 0;
            dmg += burnBonus;
            game.discardPile.addLast(drawn);
            game.showAttackDesc("4️⃣ 抽到" + drawn + " → " + dmg + "点伤害");
            game.pendingAttack = new GameCharacter.AttackResult();
            game.pendingAttack.damage = dmg;
            game.pendingAttack.desc = "4️⃣ " + dmg + "点伤害";
            GameAnim.playFloatingText(game, "弃" + drawn, new Color(255, 60, 60),
                new Point(game.getWidth() / 2, game.getHeight() / 3 - 30));
            onDone.run();
        }
    }

    private void handlePlayerFourDraw(GameCharacter self, GameCharacter opponent,
                                        List<Card> selfHand, List<Card> oppHand, Runnable onDone) {
        game.showAttackDesc("4️⃣ 选择对手的一张手牌");
        game.currentPhase = Game.Phase.SAIKI_THREE_CHOICE;
        game.chanFourSelectOpponent = true;
        game.selectedAICard = -1;
        game.blazeFourOnDone = onDone;
        game.updateDisplay();
    }

    void doBlazeFourOpponentSelected(int aiCardIndex) {
        List<Card> oppHand = game.ai.getHand();
        if (aiCardIndex < 0 || aiCardIndex >= oppHand.size()) return;
        Card drawn = oppHand.remove(aiCardIndex);

        game.chanFourSelectOpponent = false;
        game.selectedAICard = -1;
        game.selectedSingle = -1;
        game.showAIRevealCard(drawn);

        Runnable onDone = game.blazeFourOnDone;
        game.blazeFourOnDone = null;

        if (drawn.getValue() == 0 && !drawn.isItemCard()) {
            game.playerHand.add(drawn);
            game.aiChar.addBurn(1);
            game.playerChar.addBurn(1);
            game.showAttackDesc("4️⃣ 抽到" + drawn + " → 0️⃣加入手牌，双方🔥1，跳过防御");
            game.pendingAttack = new GameCharacter.AttackResult();
            game.pendingAttack.skipDefense = true;
            game.pendingAttack.damage = 0;
            game.pendingAttack.desc = "4️⃣ 抽到0️⃣ → 加入手牌+双方🔥1";
            GameAnim.playFloatingText(game, "抽" + drawn, new Color(100, 180, 255),
                new Point(game.getWidth() / 2, game.getHeight() / 3 - 30));
            GameAnim.playFloatingText(game, "🔥+1", new Color(255, 140, 0),
                new Point(game.getWidth() / 2, game.getHeight() / 3 - 60));
            game.currentPhase = Game.Phase.PLAYER_PLAY;
            game.clearAIZones();
            game.updateDisplay();
            Timer t = new Timer(Game.DELAY_STEP, e -> { ((Timer)e.getSource()).stop(); onDone.run(); });
            t.start();
        } else {
            int dmg = drawn.isItemCard() ? 4 : drawn.getValue();
            int burnBonus = game.playerChar.getBurnStacks() > 0 ? 1 : 0;
            dmg += burnBonus;
            game.discardPile.addLast(drawn);
            game.showAttackDesc("4️⃣ 抽到" + drawn + " → " + dmg + "点伤害");
            game.pendingAttack = new GameCharacter.AttackResult();
            game.pendingAttack.damage = dmg;
            game.pendingAttack.desc = "4️⃣ " + dmg + "点伤害";
            GameAnim.playFloatingText(game, "弃" + drawn, new Color(255, 60, 60),
                new Point(game.getWidth() / 2, game.getHeight() / 3 - 30));
            game.currentPhase = Game.Phase.PLAYER_PLAY;
            game.clearAIZones();
            game.updateDisplay();
            Timer t = new Timer(Game.DELAY_STEP, e -> { ((Timer)e.getSource()).stop(); onDone.run(); });
            t.start();
        }
    }



    void handleBlazeDefendTwoDraw(GameCharacter self, GameCharacter opponent,
                                    List<Card> selfHand, Runnable onDone) {
        game.refillDeckIfNeeded();
        if (game.deck.remaining() == 0) {
            onDone.run();
            return;
        }
        Card drawn = game.deck.draw();
        int counterDmg = drawn.isItemCard() ? 4 : drawn.getValue();
        boolean isItem = drawn.isItemCard();
        selfHand.add(drawn);

        if (isItem) {
            opponent.addBurn(1);
        }

        String who = self == game.playerChar ? "你" : "AI";
        game.showDefendDesc("2️⃣ " + who + "抽到" + drawn + " → 反击" + counterDmg + "点" + (isItem ? " +对手🔥1" : ""));
        GameAnim.playFloatingText(game, drawn + "→" + counterDmg, new Color(255, 80, 30),
            self == game.playerChar
                ? new Point(game.getWidth() / 2, game.getHeight() * 3 / 4 - 30)
                : new Point(game.getWidth() / 2, game.getHeight() / 3 - 30));

        opponent.takeDamage(counterDmg);
        GameAnim.playFloatingText(game, "-" + counterDmg, new Color(255, 60, 60),
            opponent == game.playerChar
                ? new Point(game.getWidth() / 2, game.getHeight() * 3 / 4)
                : new Point(game.getWidth() / 2, game.getHeight() / 3));

        onDone.run();
    }
}