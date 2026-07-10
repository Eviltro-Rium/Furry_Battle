import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MozeHandler extends CharacterHandler {

    MozeHandler(Game game) {
        super(game);
    }

    @Override
    void doSevenChoice(int aiCardIndex) {
    }

    void handleMozeFourGuard(GameCharacter self, GameCharacter opponent,
                              List<Card> selfHand, Runnable onDone) {
        if (self == game.playerChar) {
            game.showAttackDesc("4️⃣ 选择1张数字牌获得对应层数[守护]");
            game.currentPhase = Game.Phase.SAIKI_SIX_JUDGE;
            game.saikiThreeOnDone = onDone;
            game.selectedSingle = -1;
            game.updateDisplay();
        } else {
            handleAIFourGuard(self, opponent, selfHand, onDone);
        }
    }

    void doMozeFourConfirm() {
        if (game.currentPhase != Game.Phase.SAIKI_SIX_JUDGE) return;
        if (game.selectedSingle < 0 || game.selectedSingle >= game.playerHand.size()) {
            game.showMessage("请先选择一张数字牌！");
            return;
        }
        Card chosen = game.playerHand.get(game.selectedSingle);
        if (chosen.isItemCard()) {
            game.showMessage("4️⃣需出数字牌！");
            return;
        }

        int guardGain = chosen.getValue();
        game.playerHand.remove(game.selectedSingle);
        game.discardPile.addLast(chosen);
        game.playerChar.addGuard(guardGain);
        game.selectedSingle = -1;

        GameAnim.playFloatingText(game, "[守护]+" + guardGain, new Color(100, 200, 255),
            new Point(game.getWidth() / 2, game.getHeight() * 3 / 4 - 60));
        game.showAttackDesc("4️⃣ 获得" + guardGain + "层[守护]，弃掉" + chosen);

        Runnable onDone = game.saikiThreeOnDone;
        game.saikiThreeOnDone = null;
        game.currentPhase = Game.Phase.PLAYER_PLAY;
        game.clearAIZones();
        game.updateDisplay();
        Timer t = new Timer(Game.DELAY_STEP, e -> { ((Timer)e.getSource()).stop(); onDone.run(); });
        t.start();
    }

    private void handleAIFourGuard(GameCharacter self, GameCharacter opponent,
                                    List<Card> selfHand, Runnable onDone) {
        AIPlayer aiPlayer = game.getCurrentTurnAI();
        Card best = null;
        int bestVal = -1;
        for (Card c : selfHand) {
            if (!c.isItemCard() && c.getValue() > bestVal) {
                bestVal = c.getValue();
                best = c;
            }
        }
        if (best == null) {
            game.showAttackDesc("4️⃣ 无数字牌可用");
            Timer t = new Timer(Game.DELAY_EFFECT, e -> { ((Timer)e.getSource()).stop(); onDone.run(); });
            t.start();
            return;
        }

        selfHand.remove(best);
        game.discardPile.addLast(best);
        self.addGuard(bestVal);
        GameAnim.playFloatingText(game, "[守护]+" + bestVal, new Color(100, 200, 255),
            new Point(game.getWidth() / 2, game.getHeight() / 3 - 60));
        game.showAttackDesc("4️⃣ " + self.getName() + "获得" + bestVal + "层[守护]，弃掉" + best);
        game.updateDisplay();
        Timer t = new Timer(Game.DELAY_EFFECT, e -> { ((Timer)e.getSource()).stop(); onDone.run(); });
        t.start();
    }

    void handleMozeFiveReveal(GameCharacter self, GameCharacter opponent,
                               List<Card> selfHand, List<Card> oppHand, Runnable onDone) {
        if (oppHand.isEmpty()) {
            game.showAttackDesc("5️⃣ 对手无手牌");
            Timer t = new Timer(Game.DELAY_EFFECT, e -> { ((Timer)e.getSource()).stop(); onDone.run(); });
            t.start();
            return;
        }

        if (self == game.playerChar) {
            game.showAttackDesc("5️⃣ 选择对手的一张手牌");
            game.currentPhase = Game.Phase.SAIKI_THREE_CHOICE;
            game.chanFourSelectOpponent = true;
            game.saikiThreeOnDone = onDone;
            game.selectedAICard = -1;
            game.updateDisplay();
        } else {
            handleAIFiveReveal(self, opponent, selfHand, oppHand, onDone);
        }
    }

    void doMozeFiveOpponentSelected(int aiCardIndex) {
        List<Card> oppHand = game.getAIHand();
        if (aiCardIndex < 0 || aiCardIndex >= oppHand.size()) return;
        Card drawn = oppHand.remove(aiCardIndex);

        game.chanFourSelectOpponent = false;
        game.selectedAICard = -1;
        game.selectedSingle = -1;

        boolean isGreenWhiteBlack = drawn.isBlack() || drawn.isWhite() || drawn.getEffectiveColor() == Card.CardColor.GREEN;

        Point from = GameAnim.getAIHandCenter(game.ui, game);
        Point to = GameAnim.getRevealPanelCenter(game.ui, game);

        GameAnim.playFlyAnimation(game, drawn, from, to, () -> {
            game.showAIRevealCard(drawn);

            if (isGreenWhiteBlack) {
                game.pendingAttack = new GameCharacter.AttackResult();
                game.pendingAttack.damage = 4;
                game.pendingAttack.desc = "5️⃣ 判定" + drawn + " → 4点[伤害]";
                game.showAttackDesc(game.pendingAttack.desc);
            } else {
                game.playerChar.heal(2);
                game.playerChar.addGuard(1);
                game.pendingAttack = new GameCharacter.AttackResult();
                game.pendingAttack.damage = 0;
                game.pendingAttack.skipDefense = true;
                game.pendingAttack.desc = "5️⃣ 判定" + drawn + " → 2[生命]+1层[守护]（跳过防御）";
                game.showAttackDesc(game.pendingAttack.desc);
                GameAnim.playFloatingText(game, "+2[生命]+1[守护]", new Color(60, 220, 60),
                    new Point(game.getWidth() / 2, game.getHeight() * 3 / 4));
            }

            game.playerHand.add(drawn);

            Timer t = new Timer(Game.DELAY_SKIP, e -> {
                ((Timer)e.getSource()).stop();
                if (game.pendingAttack.damage > 0 && !game.pendingAttack.skipDefense) {
                    game.currentPhase = Game.Phase.AI_DEFEND;
                    game.updateDisplay();
                    game.aiTimer = new Timer(Game.DELAY_STEP, e2 -> {
                        game.aiTimer.stop();
                        game.doAIDefend();
                    });
                    game.aiTimer.start();
                } else {
                    game.clearAIZones();
                    game.updateDisplay();
                    Runnable onDone = game.saikiThreeOnDone;
                    game.saikiThreeOnDone = null;
                    onDone.run();
                }
            });
            t.start();
        });
    }

    private void handleAIFiveReveal(GameCharacter self, GameCharacter opponent,
                                     List<Card> selfHand, List<Card> oppHand, Runnable onDone) {
        int idx = (int)(Math.random() * oppHand.size());
        Card drawn = oppHand.remove(idx);
        boolean isGreenWhiteBlack = drawn.isBlack() || drawn.isWhite() || drawn.getEffectiveColor() == Card.CardColor.GREEN;

        Point from = GameAnim.getPlayerHandCardCenter(game.ui, game, 0);
        Point to = GameAnim.getRevealPanelCenter(game.ui, game);

        GameAnim.playFlyAnimation(game, drawn, from, to, () -> {
            game.showAIRevealCard(drawn);

            if (isGreenWhiteBlack) {
                game.pendingAttack = new GameCharacter.AttackResult();
                game.pendingAttack.damage = 4;
                game.pendingAttack.desc = "5️⃣ 判定" + drawn + " → 4点[伤害]";
                game.showAttackDesc(game.pendingAttack.desc);
            } else {
                self.heal(2);
                self.addGuard(1);
                game.pendingAttack = new GameCharacter.AttackResult();
                game.pendingAttack.damage = 0;
                game.pendingAttack.skipDefense = true;
                game.pendingAttack.desc = "5️⃣ 判定" + drawn + " → 2[生命]+1层[守护]（跳过防御）";
                game.showAttackDesc(game.pendingAttack.desc);
                GameAnim.playFloatingText(game, "+2[生命]+1[守护]", new Color(60, 220, 60),
                    new Point(game.getWidth() / 2, game.getHeight() / 3));
            }

            selfHand.add(drawn);

            Timer t = new Timer(Game.DELAY_SKIP, e -> {
                ((Timer)e.getSource()).stop();
                if (game.pendingAttack.damage > 0 && !game.pendingAttack.skipDefense) {
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

    void handleMozeSevenCleanse(GameCharacter self, GameCharacter opponent,
                                 List<Card> selfHand, Runnable onDone) {
        int debuffCount = self.getBurnStacks() + (self.isFrozen() ? 1 : 0) + self.getBleedStacks();
        int bonusDmg = debuffCount;
        self.clearDebuffsOnly();

        game.pendingAttack = new GameCharacter.AttackResult();
        game.pendingAttack.damage = 3 + bonusDmg;
        game.pendingAttack.desc = "7️⃣ 3+" + bonusDmg + "点[伤害]（清除" + debuffCount + "层debuff）";
        game.showAttackDesc(game.pendingAttack.desc);

        if (debuffCount > 0) {
            GameAnim.playFloatingText(game, "[净化]" + debuffCount + "层", new Color(200, 180, 255),
                self == game.playerChar
                    ? new Point(game.getWidth() / 2, game.getHeight() * 3 / 4 - 60)
                    : new Point(game.getWidth() / 2, game.getHeight() / 3 - 60));
        }

        game.updateDisplay();
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
}