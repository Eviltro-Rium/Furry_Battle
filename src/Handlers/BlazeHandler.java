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
            opponent.addBurn(2);
            game.showAttackDesc("4️⃣ 抽到" + drawn + " → 0️⃣加入手牌，🔥2，跳过防御");
            game.pendingAttack = new GameCharacter.AttackResult();
            game.pendingAttack.skipDefense = true;
            game.pendingAttack.damage = 0;
            game.pendingAttack.desc = "4️⃣ 抽到0️⃣ → 加入手牌+🔥2";
            GameAnim.playFloatingText(game, "抽" + drawn, new Color(100, 180, 255),
                new Point(game.getWidth() / 2, game.getHeight() / 3 - 30));
            GameAnim.playFloatingText(game, "🔥+2", new Color(255, 140, 0),
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
            game.aiChar.addBurn(2);
            game.showAttackDesc("4️⃣ 抽到" + drawn + " → 0️⃣加入手牌，🔥2，跳过防御");
            game.pendingAttack = new GameCharacter.AttackResult();
            game.pendingAttack.skipDefense = true;
            game.pendingAttack.damage = 0;
            game.pendingAttack.desc = "4️⃣ 抽到0️⃣ → 加入手牌+🔥2";
            GameAnim.playFloatingText(game, "抽" + drawn, new Color(100, 180, 255),
                new Point(game.getWidth() / 2, game.getHeight() / 3 - 30));
            GameAnim.playFloatingText(game, "🔥+2", new Color(255, 140, 0),
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

    void handleBlazeSevenPlay(GameCharacter self, GameCharacter opponent,
                               List<Card> selfHand, Runnable onDone) {
        if (self != game.playerChar) {
            handleAISevenPlay(self, opponent, selfHand, onDone);
        } else {
            handlePlayerSevenPlay(self, opponent, selfHand, onDone);
        }
    }

    private void handleAISevenPlay(GameCharacter self, GameCharacter opponent,
                                     List<Card> selfHand, Runnable onDone) {
        BlazeAI blazeAI = (BlazeAI) game.ai;
        Card playCard = blazeAI.chooseSevenPlay(selfHand);
        if (playCard == null) {
            game.showAttackDesc("7️⃣ 无可打出的牌");
            Timer t = new Timer(Game.DELAY_STEP, e -> { ((Timer)e.getSource()).stop(); onDone.run(); });
            t.start();
            return;
        }

        selfHand.remove(playCard);
        game.discardPile.addLast(playCard);
        int dmg = (int) Math.ceil(playCard.getValue() * 1.5);
        int burnBonus = self.getBurnStacks() > 0 ? 1 : 0;
        dmg += burnBonus;

        game.showAttackDesc("7️⃣ 打出" + playCard + " → " + dmg + "点伤害(不可防御)");
        GameAnim.playFloatingText(game, playCard + "→" + dmg, new Color(255, 80, 30),
            new Point(game.getWidth() / 2, game.getHeight() / 3 - 30));

        game.pendingAttack = new GameCharacter.AttackResult();
        game.pendingAttack.damage = dmg;
        game.pendingAttack.skipDefense = true;
        game.pendingAttack.desc = "7️⃣ " + dmg + "点伤害(不可防御)";
        onDone.run();
    }

    private void handlePlayerSevenPlay(GameCharacter self, GameCharacter opponent,
                                         List<Card> selfHand, Runnable onDone) {
        game.showAttackDesc("7️⃣ 选择一张牌打出(不触发技能)");
        game.currentPhase = Game.Phase.SAIKI_SIX_JUDGE;
        game.blazeSevenOnDone = onDone;
        game.selectedSingle = -1;
        game.updateDisplay();
    }

    void doBlazeSevenConfirm() {
        if (game.currentPhase != Game.Phase.SAIKI_SIX_JUDGE) return;
        if (game.selectedSingle < 0 || game.selectedSingle >= game.playerHand.size()) {
            game.showMessage("请先选择一张牌！");
            return;
        }
        Card playCard = game.playerHand.get(game.selectedSingle);
        if (playCard.isItemCard()) {
            game.showMessage("7️⃣需出数字牌！");
            return;
        }
        if (playCard.getValue() == 0) {
            game.showMessage("7️⃣不能出0️⃣！");
            return;
        }

        game.playerHand.remove(game.selectedSingle);
        game.selectedSingle = -1;
        game.discardPile.addLast(playCard);

        int dmg = (int) Math.ceil(playCard.getValue() * 1.5);
        int burnBonus = game.playerChar.getBurnStacks() > 0 ? 1 : 0;
        dmg += burnBonus;

        game.showAttackDesc("7️⃣ 打出" + playCard + " → " + dmg + "点伤害(不可防御)");
        GameAnim.playFloatingText(game, playCard + "→" + dmg, new Color(255, 80, 30),
            new Point(game.getWidth() / 2, game.getHeight() * 3 / 4 - 30));

        Runnable onDone = game.blazeSevenOnDone;
        game.blazeSevenOnDone = null;
        game.pendingAttack = new GameCharacter.AttackResult();
        game.pendingAttack.damage = dmg;
        game.pendingAttack.skipDefense = true;
        game.pendingAttack.desc = "7️⃣ " + dmg + "点伤害(不可防御)";

        game.currentPhase = Game.Phase.PLAYER_PLAY;
        game.clearAIZones();
        game.updateDisplay();
        Timer t = new Timer(Game.DELAY_STEP, e -> { ((Timer)e.getSource()).stop(); onDone.run(); });
        t.start();
    }

    @Override
    void handleRevealTopDeck(GameCharacter self, GameCharacter opponent, List<Card> selfHand) {
        game.refillDeckIfNeeded();
        if (game.deck.remaining() == 0) return;
        Card revealed = game.deck.draw();
        GameCharacter.AttackResult ar = self.resolveReveal(revealed, true);
        game.effectEngine.applyImmediateEffects(ar, self, opponent);
        game.showAttackDesc(ar.desc);
        if (ar.drawCount == -1) {
            selfHand.add(revealed);
        } else {
            game.discardPile.addLast(revealed);
        }
        game.updateDisplay();
    }

    @Override
    void handleAIRevealTopDeck(GameCharacter self, GameCharacter opponent, List<Card> selfHand) {
        handleRevealTopDeck(self, opponent, selfHand);
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
        selfHand.add(drawn);

        String who = self == game.playerChar ? "你" : "AI";
        game.showDefendDesc("2️⃣ " + who + "抽到" + drawn + " → 反击" + counterDmg + "点");
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