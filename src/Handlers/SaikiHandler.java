import javax.swing.*;
import java.awt.*;
import java.util.List;

public class SaikiHandler extends CharacterHandler {

    SaikiHandler(Game game) {
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
        } else if (game.pendingAttack != null) {
            game.currentPhase = Game.Phase.AI_DEFEND;
            game.updateDisplay();
            game.aiTimer = new Timer(Game.DELAY_PLAY, e -> {
                game.aiTimer.stop();
                game.doAIDefend();
            });
            game.aiTimer.start();
        } else {
            game.currentPhase = Game.Phase.PLAYER_PLAY;
            game.updateDisplay();
        }
    }

    @Override
    void handleSaikiThreeDraw(GameCharacter self, GameCharacter opponent,
                               List<Card> selfHand, List<Card> oppHand, Runnable onDone) {
        if (oppHand.isEmpty()) {
            game.showAttackDesc("3️⃣ 对手无手牌");
            Timer t = new Timer(Game.DELAY_EFFECT, e -> { ((Timer)e.getSource()).stop(); onDone.run(); });
            t.start();
            return;
        }

        if (self != game.playerChar) {
            handleAIThreeDraw(self, opponent, selfHand, oppHand, onDone);
        } else {
            handlePlayerThreeDraw(self, opponent, selfHand, oppHand, onDone);
        }
    }

    private void handleAIThreeDraw(GameCharacter self, GameCharacter opponent,
                                    List<Card> selfHand, List<Card> oppHand, Runnable onDone) {
        SaikiAI saikiAI = (SaikiAI) game.ai;
        int idx = (int)(Math.random() * oppHand.size());
        Card drawn = oppHand.remove(idx);

        boolean discard = saikiAI.chooseThreeDiscard(drawn);
        if (discard) {
            game.discardPile.addLast(drawn);
            game.showAttackDesc("3️⃣ AI抽走你的" + drawn + "并弃掉");
            GameAnim.playFloatingText(game, "弃" + drawn, new Color(255, 60, 60),
                new Point(game.getWidth() / 2, game.getHeight() * 3 / 4 - 30));
        } else {
            selfHand.add(drawn);
            game.showAttackDesc("3️⃣ AI抽走你的" + drawn + "加入手牌");
            GameAnim.playFloatingText(game, "抽" + drawn, new Color(100, 180, 255),
                new Point(game.getWidth() / 2, game.getHeight() / 3 - 30));
        }
        game.updateDisplay();
        Timer t = new Timer(Game.DELAY_EFFECT, e -> { ((Timer)e.getSource()).stop(); onDone.run(); });
        t.start();
    }

    private void handlePlayerThreeDraw(GameCharacter self, GameCharacter opponent,
                                         List<Card> selfHand, List<Card> oppHand, Runnable onDone) {
        game.showAttackDesc("3️⃣ 选择对手的一张手牌");
        game.currentPhase = Game.Phase.SAIKI_THREE_CHOICE;
        game.chanFourSelectOpponent = true;
        game.saikiThreeOnDone = onDone;
        game.selectedAICard = -1;
        game.updateDisplay();
    }

    void doSaikiThreeOpponentSelected(int aiCardIndex) {
        List<Card> oppHand = game.ai.getHand();
        if (aiCardIndex < 0 || aiCardIndex >= oppHand.size()) return;
        Card drawn = oppHand.remove(aiCardIndex);

        game.chanFourSelectOpponent = false;
        game.saikiThreeDrawn = drawn;
        game.selectedAICard = -1;
        game.selectedSingle = -1;
        game.showAIRevealCard(drawn);
        game.showAttackDesc("3️⃣ 选中" + drawn + " → 加入手牌或弃掉？");
        game.updateDisplay();
    }

    void doSaikiThreeKeep() {
        if (game.saikiThreeDrawn == null) return;
        Card drawn = game.saikiThreeDrawn;
        game.playerHand.add(drawn);
        GameAnim.playFloatingText(game, "抽" + drawn, new Color(100, 180, 255),
            new Point(game.getWidth() / 2, game.getHeight() / 3 - 30));
        finishSaikiThreeChoice();
    }

    void doSaikiThreeDiscard() {
        if (game.saikiThreeDrawn == null) return;
        Card drawn = game.saikiThreeDrawn;
        game.discardPile.addLast(drawn);
        GameAnim.playFloatingText(game, "弃" + drawn, new Color(255, 60, 60),
            new Point(game.getWidth() / 2, game.getHeight() / 3 - 30));
        finishSaikiThreeChoice();
    }

    private void finishSaikiThreeChoice() {
        Runnable onDone = game.saikiThreeOnDone;
        game.saikiThreeDrawn = null;
        game.saikiThreeOnDone = null;
        game.currentPhase = Game.Phase.PLAYER_PLAY;
        game.clearAIZones();
        game.updateDisplay();
        Timer t = new Timer(Game.DELAY_STEP, e -> { ((Timer)e.getSource()).stop(); onDone.run(); });
        t.start();
    }

    @Override
    void handleSaikiSixJudge(GameCharacter self, GameCharacter opponent,
                              List<Card> selfHand, Runnable onDone) {
        if (self != game.playerChar) {
            handleAISixJudge(self, opponent, selfHand, onDone);
        } else {
            handlePlayerSixJudge(self, opponent, selfHand, onDone);
        }
    }

    private void handleAISixJudge(GameCharacter self, GameCharacter opponent,
                                    List<Card> selfHand, Runnable onDone) {
        SaikiAI saikiAI = (SaikiAI) game.ai;
        Card judgeCard = saikiAI.chooseSixCard(selfHand);
        if (judgeCard == null) {
            game.showAttackDesc("6️⃣ 无数字牌可判定");
            Timer t = new Timer(Game.DELAY_EFFECT, e -> { ((Timer)e.getSource()).stop(); onDone.run(); });
            t.start();
            return;
        }

        selfHand.remove(judgeCard);
        int dmg = (int) Math.ceil(judgeCard.getValue() * 1.5);
        boolean isYellow = judgeCard.getColor() == Card.CardColor.YELLOW;

        Point from = GameAnim.getAIHandCenter(game.ui, game);
        Point to = GameAnim.getRevealPanelCenter(game.ui, game);
        GameAnim.playFlyAnimation(game, judgeCard, from, to, () -> {
            game.showAIRevealCard(judgeCard);
            String desc = "6️⃣ 判定" + judgeCard + " → " + dmg + "点伤害";
            if (isYellow) {
                opponent.addBleed(1);
                desc += "+1层流血";
                GameAnim.playFloatingText(game, "血+1", new Color(180, 0, 0),
                    new Point(game.getWidth() / 2, game.getHeight() * 3 / 4 - 60));
            }
            game.pendingAttack = new GameCharacter.AttackResult();
            game.pendingAttack.damage = dmg;
            game.pendingAttack.desc = desc;
            game.showAttackDesc(desc);

            game.discardPile.addLast(judgeCard);

            Timer t = new Timer(Game.DELAY_SKIP, e -> {
                ((Timer)e.getSource()).stop();
                if (game.pendingAttack.damage > 0) {
                    game.enterPlayerDefend();
                } else {
                    game.clearAIZones();
                    game.updateDisplay();
                    onDone.run();
                }
            });
            t.start();
        });
    }

    private void handlePlayerSixJudge(GameCharacter self, GameCharacter opponent,
                                        List<Card> selfHand, Runnable onDone) {
        game.showAttackDesc("6️⃣ 选择一张数字牌入判定区");
        game.currentPhase = Game.Phase.SAIKI_SIX_JUDGE;
        game.saikiThreeOnDone = onDone;
        game.selectedSingle = -1;
        game.updateDisplay();
    }

    void doSaikiSixConfirm() {
        if (game.currentPhase != Game.Phase.SAIKI_SIX_JUDGE) return;
        if (game.selectedSingle < 0 || game.selectedSingle >= game.playerHand.size()) {
            game.showMessage("请先选择一张数字牌！");
            return;
        }
        Card judgeCard = game.playerHand.get(game.selectedSingle);
        if (judgeCard.isItemCard()) {
            game.showMessage("6️⃣需出数字牌！");
            return;
        }

        int cardIdx = game.selectedSingle;
        Point from = GameAnim.getPlayerHandCardCenter(game.ui, game, cardIdx);
        Point to = GameAnim.getRevealPanelCenter(game.ui, game);

        game.playerHand.remove(cardIdx);
        game.selectedSingle = -1;


        int dmg = (int) Math.ceil(judgeCard.getValue() * 1.5);
        boolean isYellow = judgeCard.getColor() == Card.CardColor.YELLOW;

        GameAnim.playFlyAnimation(game, judgeCard, from, to, () -> {
            game.showAIRevealCard(judgeCard);
            String desc = "6️⃣ 判定" + judgeCard + " → " + dmg + "点伤害";
            if (isYellow) {
                game.aiChar.addBleed(1);
                desc += "+1层流血";
                GameAnim.playFloatingText(game, "血+1", new Color(180, 0, 0),
                    new Point(game.getWidth() / 2, game.getHeight() / 3 - 60));
            }
            game.pendingAttack = new GameCharacter.AttackResult();
            game.pendingAttack.damage = dmg;
            game.pendingAttack.desc = desc;
            game.showAttackDesc(desc);

            game.discardPile.addLast(judgeCard);

            if (dmg > 0) {
                game.currentPhase = Game.Phase.AI_DEFEND;
                game.updateDisplay();
                game.aiTimer = new Timer(Game.DELAY_STEP, e -> {
                    game.aiTimer.stop();
                    game.doAIDefend();
                });
                game.aiTimer.start();
            } else {
                game.currentPhase = Game.Phase.PLAYER_PLAY;
                game.clearAIZones();
                game.updateDisplay();
            }
        });
    }

    @Override

    void handleSaikiZeroAttack(GameCharacter self, GameCharacter opponent,
                                 List<Card> selfHand, Runnable onDone) {
        int oppBleed = opponent.getBleedStacks();
        int selfBleed = self.getBleedStacks();
        int dmg = 3 * oppBleed + 1;
        int healAmt = oppBleed + selfBleed;

        opponent.addBleed(1);
        GameAnim.playFloatingText(game, "血+1", new Color(180, 0, 0),
            opponent == game.playerChar
                ? new Point(game.getWidth() / 2, game.getHeight() * 3 / 4 - 60)
                : new Point(game.getWidth() / 2, game.getHeight() / 3 - 60));

        game.pendingAttack = new GameCharacter.AttackResult();
        game.pendingAttack.damage = dmg;
        game.pendingAttack.desc = "0️⃣ " + dmg + "点伤害(3×" + oppBleed + "+1)+1层流血+恢复" + healAmt;
        game.showAttackDesc(game.pendingAttack.desc);

        self.heal(healAmt);
        if (healAmt > 0) {
            GameAnim.playFloatingText(game, "+" + healAmt, new Color(60, 220, 60),
                self == game.playerChar
                    ? new Point(game.getWidth() / 2, game.getHeight() * 3 / 4)
                    : new Point(game.getWidth() / 2, game.getHeight() / 3));
        }

        if (self == game.playerChar) {
            game.currentPhase = Game.Phase.AI_DEFEND;
            game.updateDisplay();
            game.aiTimer = new Timer(Game.DELAY_STEP, e -> {
                game.aiTimer.stop();
                game.doAIDefend();
            });
            game.aiTimer.start();
        } else {
            game.enterPlayerDefend();
        }
    }

    @Override
    void handleSaikiThreeDefendReveal(GameCharacter self, GameCharacter opponent,
                                        List<Card> selfHand, Runnable onDone) {
        game.refillDeckIfNeeded();
        if (game.deck.isEmpty()) {
            game.showDefendDesc("3️⃣ 牌库为空，防御失败");
            Timer t = new Timer(Game.DELAY_EFFECT, e -> { ((Timer)e.getSource()).stop(); onDone.run(); });
            t.start();
            return;
        }

        Card revealed = game.deck.draw();
        boolean success = revealed.isBlack() || revealed.isWhite() || revealed.getColor() == Card.CardColor.YELLOW;

        Point from = GameAnim.getDeckCenter(game.ui, game);
        Point to = GameAnim.getRevealPanelCenter(game.ui, game);
        GameAnim.playFlyAnimation(game, revealed, from, to, () -> {
            game.showAIRevealCard(revealed);

            Timer t = new Timer(Game.DELAY_EFFECT, e -> {
                ((Timer)e.getSource()).stop();

                if (success) {
                    game.showDefendDesc("3️⃣ 判定" + revealed + " → 防御所有伤害（不免疫debuff）");
                    game.pendingDefendResult = new GameCharacter.DefenseResult();
                    game.pendingDefendResult.immuneAll = true;
                    game.pendingDefendResult.desc = "3️⃣ 防御成功";
                    game.discardPile.addLast(revealed);
                } else {
                    selfHand.add(revealed);
                    game.showDefendDesc("3️⃣ 判定" + revealed + " → 无效，加入手牌");
                }

                game.updateDisplay();
                Timer t2 = new Timer(Game.DELAY_STEP, e2 -> { ((Timer)e2.getSource()).stop(); onDone.run(); });
                t2.start();
            });
            t.start();
        });
    }

    @Override
    void handleSaikiSevenAttack(GameCharacter self, GameCharacter opponent,
                                  List<Card> selfHand, Runnable onDone) {
        int bleed = opponent.getBleedStacks();
        int dmg = 2 * bleed;
        int healAmt = dmg;

        self.heal(healAmt);
        if (healAmt > 0) {
            GameAnim.playFloatingText(game, "+" + healAmt, new Color(60, 220, 60),
                self == game.playerChar
                    ? new Point(game.getWidth() / 2, game.getHeight() * 3 / 4)
                    : new Point(game.getWidth() / 2, game.getHeight() / 3));
        }

        String desc = "7️⃣ 2×" + bleed + "🩸=" + dmg + "点伤害+恢复" + healAmt;
        game.pendingAttack = new GameCharacter.AttackResult();
        game.pendingAttack.damage = dmg;
        game.pendingAttack.desc = desc;
        game.showAttackDesc(desc);

        if (dmg > 0) {
            if (self == game.playerChar) {
                game.currentPhase = Game.Phase.AI_DEFEND;
                game.updateDisplay();
                game.aiTimer = new Timer(Game.DELAY_STEP, e -> {
                    game.aiTimer.stop();
                    game.doAIDefend();
                });
                game.aiTimer.start();
            } else {
                game.enterPlayerDefend();
            }
        } else {
            game.clearAIZones();
            game.updateDisplay();
            onDone.run();
        }
    }
}