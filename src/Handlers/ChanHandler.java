import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ChanHandler extends CharacterHandler {

    ChanHandler(Game game) {
        super(game);
    }

    @Override
    void handleRevealAndJudge(GameCharacter self, GameCharacter opponent,
                                       List<Card> selfHand, Runnable onDone) {
        game.refillDeckIfNeeded();
        if (game.deck.isEmpty()) {
            game.pendingAttack = null;
            game.currentPhase = self == game.playerChar ? Game.Phase.PLAYER_PLAY : Game.Phase.AI_TURN;
            game.clearAIZones();
            game.updateDisplay();
            if (self != game.playerChar) game.finishAITurn();
            return;
        }
        Card revealed = game.deck.draw();
        GameCharacter.AttackResult revealResult = self.resolveReveal(revealed, true);

        Point from = GameAnim.getDeckCenter(game.ui, game);
        Point to = GameAnim.getRevealPanelCenter(game.ui, game);
        GameAnim.playFlyAnimation(game, revealed, from, to, () -> {
            game.showAIRevealCard(revealed);

            Timer revealTimer = new Timer(Game.DELAY_SKIP, e -> {
                ((Timer)e.getSource()).stop();

                if (revealResult.addBurn > 0) {
                    opponent.addBurn(revealResult.addBurn);
                    GameAnim.playFloatingText(game, "灼烧+" + revealResult.addBurn, new Color(255, 140, 0),
                        opponent == game.playerChar
                            ? new Point(game.getWidth() / 2, game.getHeight() * 3 / 4 - 60)
                            : new Point(game.getWidth() / 2, game.getHeight() / 3 - 60));
                }

                if (revealResult.drawCount == -1) {
                    selfHand.add(revealed);
                } else {
                    game.discardPile.addLast(revealed);
                }

                game.showAttackDesc(revealResult.desc);

                if (revealResult.damage > 0 && !revealResult.skipDefense) {
                    game.pendingAttack = revealResult;
                    if (self == game.playerChar) {
                        game.currentPhase = Game.Phase.AI_DEFEND;
                        game.updateDisplay();
                        game.aiTimer = new Timer(Game.DELAY_STEP, e2 -> {
                            game.aiTimer.stop();
                            game.doAIDefend();
                        });
                        game.aiTimer.start();
                    } else {
                        game.enterPlayerDefend();
                    }
                } else if (revealResult.damage > 0 && revealResult.skipDefense) {
                    game.pendingAttack = revealResult;
                    if (self == game.playerChar) {
                        game.currentPhase = Game.Phase.AI_DEFEND;
                        game.updateDisplay();
                        game.showDefendDesc("跳过防御");
                        Timer skipTimer = new Timer(Game.DELAY_SKIP, e2 -> {
                            ((Timer)e2.getSource()).stop();
                            game.resolvePostDefense(game.playerChar, game.aiChar);
                            game.clearAIZones();
                            game.currentPhase = Game.Phase.PLAYER_PLAY;
                            game.updateDisplay();
                        });
                        skipTimer.start();
                    } else {
                        game.currentPhase = Game.Phase.PLAYER_DEFEND;
                        game.updateDisplay();
                        game.showDefendDesc("跳过防御");
                        Timer skipTimer = new Timer(Game.DELAY_SKIP, e2 -> {
                            ((Timer)e2.getSource()).stop();
                            game.resolvePostDefense(game.aiChar, game.playerChar);
                            game.clearAIZones();
                            game.updateDisplay();
                            game.finishAITurn();
                        });
                        skipTimer.start();
                    }
                } else {
                    game.pendingAttack = null;
                    game.currentPhase = self == game.playerChar ? Game.Phase.PLAYER_PLAY : Game.Phase.AI_TURN;
                    game.clearAIZones();
                    game.updateDisplay();
                    if (self != game.playerChar) {
                        Timer delay = new Timer(Game.DELAY_STEP, e2 -> {
                            ((Timer)e2.getSource()).stop();
                            onDone.run();
                        });
                        delay.setRepeats(false);
                        delay.start();
                    }
                }
            });
            revealTimer.start();
        });
    }

    @Override
    void handleChanFourSwap(GameCharacter self, GameCharacter opponent,
                             List<Card> selfHand, List<Card> oppHand, Runnable onDone) {
        if (oppHand.isEmpty()) {
            game.pendingAttack = new GameCharacter.AttackResult();
            game.pendingAttack.damage = 2;
            game.pendingAttack.skipDefense = true;
            game.showAttackDesc("4️⃣ 对手无手牌 → 2点伤害（跳过防御）");
            game.showDefendDesc("跳过防御");
            if (self == game.playerChar) {
                Timer skipTimer = new Timer(Game.DELAY_SKIP, e -> {
                    ((Timer)e.getSource()).stop();
                    game.resolvePostDefense(game.playerChar, game.aiChar);
                    game.clearAIZones();
                    game.currentPhase = Game.Phase.PLAYER_PLAY;
                    game.updateDisplay();
                });
                skipTimer.start();
            } else {
                Timer skipTimer = new Timer(Game.DELAY_SKIP, e -> {
                    ((Timer)e.getSource()).stop();
                    game.resolvePostDefense(game.aiChar, game.playerChar);
                    game.clearAIZones();
                    game.updateDisplay();
                    game.finishAITurn();
                });
                skipTimer.start();
            }
            return;
        }

        if (self != game.playerChar) {
            handleAIFourSwap(self, opponent, selfHand, oppHand, onDone);
        } else {
            handlePlayerFourSwap(self, opponent, selfHand, oppHand, onDone);
        }
    }

    private void handleAIFourSwap(GameCharacter self, GameCharacter opponent,
                                   List<Card> selfHand, List<Card> oppHand, Runnable onDone) {
        AIPlayer aiPlayer = game.getCurrentTurnAI();
        int idx = (int)(Math.random() * oppHand.size());
        Card drawn = oppHand.remove(idx);

        Card swapCard = null;
        if (aiPlayer instanceof ChanAI) swapCard = ((ChanAI) aiPlayer).chooseFourSwap(drawn, selfHand);
        if (swapCard != null) {
            selfHand.remove(swapCard);
            oppHand.add(swapCard);
            selfHand.add(drawn);
            game.showAttackDesc("4️⃣ AI抽走你的" + drawn + "，交换" + swapCard);
            GameAnim.playFloatingText(game, "交换" + drawn + "⇄" + swapCard, new Color(100, 180, 255),
                new Point(game.getWidth() / 2, game.getHeight() * 3 / 4 - 30));
        } else {
            game.discardPile.addLast(drawn);
            game.pendingAttack = new GameCharacter.AttackResult();
            game.pendingAttack.damage = 2;
            game.pendingAttack.skipDefense = true;
            game.showAttackDesc("4️⃣ AI弃掉你的" + drawn + " + 2点伤害（跳过防御）");
            GameAnim.playFloatingText(game, "弃" + drawn, new Color(255, 60, 60),
                new Point(game.getWidth() / 2, game.getHeight() * 3 / 4 - 30));
            game.showDefendDesc("跳过防御");
            Timer skipTimer = new Timer(Game.DELAY_SKIP, e -> {
                ((Timer)e.getSource()).stop();
                game.resolvePostDefense(game.aiChar, game.playerChar);
                game.clearAIZones();
                game.updateDisplay();
                onDone.run();
            });
            skipTimer.start();
            return;
        }
        game.updateDisplay();
        Timer t = new Timer(Game.DELAY_EFFECT, e -> { ((Timer)e.getSource()).stop(); onDone.run(); });
        t.start();
    }

    private void handlePlayerFourSwap(GameCharacter self, GameCharacter opponent,
                                       List<Card> selfHand, List<Card> oppHand, Runnable onDone) {
        game.showAttackDesc("4️⃣ 选择对手的一张手牌");
        game.currentPhase = Game.Phase.PLAYER_SEVEN_CHOICE;
        game.chanFourSelectOpponent = true;
        game.chanFourSwapCallback = onDone;
        game.selectedAICard = -1;
        game.updateDisplay();
    }

    void doChanFourOpponentSelected(int aiCardIndex) {
        if (!game.chanFourSelectOpponent) return;
        List<Card> oppHand = game.getAIHand();
        if (aiCardIndex < 0 || aiCardIndex >= oppHand.size()) return;

        Card drawn = oppHand.remove(aiCardIndex);
        game.showAIRevealCard(drawn);
        game.showAttackDesc("4️⃣ 选中" + drawn + "，选择自己的牌交换或弃掉");

        game.chanFourSelectOpponent = false;
        game.chanFourSwapMode = true;
        game.chanFourSwapDrawn = drawn;
        game.selectedAICard = -1;
        game.selectedSingle = -1;
        game.updateDisplay();
    }

    @Override
    void handleChanFiveReorder(GameCharacter self, GameCharacter opponent,
                                List<Card> selfHand, Runnable onDone) {
        game.refillDeckIfNeeded();
        int count = Math.min(5, game.deck.remaining());
        if (count == 0) {
            game.showAttackDesc("5️⃣ 牌库为空");
            Timer t = new Timer(Game.DELAY_EFFECT, e -> { ((Timer)e.getSource()).stop(); onDone.run(); });
            t.start();
            return;
        }

        List<Card> topCards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            topCards.add(game.deck.draw());
        }

        if (self != game.playerChar) {
            AIPlayer aiPlayer = game.getCurrentTurnAI();
            List<Card> reordered = null;
            if (aiPlayer instanceof ChanAI) reordered = ((ChanAI) aiPlayer).reorderFive(topCards);
            for (int i = reordered.size() - 1; i >= 0; i--) {
                game.deck.putBack(reordered.get(i));
            }
            game.showAttackDesc("5️⃣ AI查看了" + count + "张牌并排序放回");
        } else {
            ChanFiveReorderDialog dialog = new ChanFiveReorderDialog(game, topCards);
            dialog.setVisible(true);
            List<Card> reordered = dialog.getResult();
            for (int i = reordered.size() - 1; i >= 0; i--) {
                game.deck.putBack(reordered.get(i));
            }
            game.showAttackDesc("5️⃣ 排序完成，牌已放回牌库顶");
        }

        List<Card> drawn = game.drawFromDeck(2);
        if (!drawn.isEmpty()) {
            selfHand.addAll(drawn);
            GameAnim.playDrawAnimations(game, drawn.size(), self == game.playerChar, () -> {
                game.updateDisplay();
                onDone.run();
            });
        } else {
            game.updateDisplay();
            onDone.run();
        }
    }

    @Override
    void handleChanSevenJudge(GameCharacter self, GameCharacter opponent,
                               List<Card> oppHand, Runnable onDone) {
        if (oppHand.isEmpty()) {
            game.showAttackDesc("7️⃣ 对手无手牌");
            Timer t = new Timer(Game.DELAY_EFFECT, e -> { ((Timer)e.getSource()).stop(); onDone.run(); });
            t.start();
            return;
        }

        if (self != game.playerChar) {
            handleAISevenJudge(self, opponent, oppHand, onDone);
        } else {
            handlePlayerSevenJudge(self, opponent, oppHand, onDone);
        }
    }

    private void handleAISevenJudge(GameCharacter self, GameCharacter opponent,
                                     List<Card> oppHand, Runnable onDone) {
        AIPlayer aiPlayer = game.getCurrentTurnAI();
        int idx = (int)(Math.random() * oppHand.size());
        Card chosen = oppHand.remove(idx);

        game.showAIRevealCard(chosen);
        boolean[] keepArr = {false};
        if (aiPlayer instanceof ChanAI) keepArr[0] = ((ChanAI) aiPlayer).chooseSevenKeep(chosen);

        Timer t = new Timer(Game.DELAY_SKIP, e -> {
            ((Timer)e.getSource()).stop();
            if (keepArr[0]) {
                game.getAIHand().add(chosen);
                game.showAttackDesc("7️⃣ AI选择加入手牌: " + chosen);
            } else {
                game.discardPile.addLast(chosen);
                game.showAttackDesc("7️⃣ AI选择弃掉: " + chosen);
            }
            game.updateDisplay();
            Timer t2 = new Timer(Game.DELAY_EFFECT, e2 -> { ((Timer)e2.getSource()).stop(); onDone.run(); });
            t2.start();
        });
        t.start();
    }

    private void handlePlayerSevenJudge(GameCharacter self, GameCharacter opponent,
                                         List<Card> oppHand, Runnable onDone) {
        game.currentPhase = Game.Phase.PLAYER_SEVEN_CHOICE;
        game.chanSevenMode = true;
        game.chanSevenCallback = onDone;
        game.updateDisplay();
    }

    void doChanSevenChoice(int aiCardIndex) {
        if (game.chanSevenKeepMode) return;
        if (!game.chanSevenMode) return;
        List<Card> oppHand = game.getAIHand();
        if (aiCardIndex < 0 || aiCardIndex >= oppHand.size()) return;

        Card chosen = oppHand.remove(aiCardIndex);
        game.chanSevenChosenCard = chosen;
        game.chanSevenKeepMode = true;
        game.selectedAICard = -1;
        game.showAIRevealCard(chosen);
        game.showAttackDesc("7️⃣ 抽取的牌: " + chosen + " → 加入手牌或弃掉？");
        game.updateDisplay();
    }

    void doChanSevenKeep() {
        if (!game.chanSevenKeepMode) return;
        Card chosen = game.chanSevenChosenCard;

        game.chanSevenMode = false;
        game.chanSevenKeepMode = false;
        game.chanSevenChosenCard = null;
        Runnable onDone = game.chanSevenCallback;
        game.chanSevenCallback = null;

        game.playerHand.add(chosen);
        game.showAttackDesc("7️⃣ 加入手牌: " + chosen);
        game.updateDisplay();
        Timer t = new Timer(Game.DELAY_EFFECT, e -> { ((Timer)e.getSource()).stop(); onDone.run(); });
        t.start();
    }

    void doChanSevenDiscard() {
        if (!game.chanSevenKeepMode) return;
        Card chosen = game.chanSevenChosenCard;

        game.chanSevenMode = false;
        game.chanSevenKeepMode = false;
        game.chanSevenChosenCard = null;
        Runnable onDone = game.chanSevenCallback;
        game.chanSevenCallback = null;

        game.discardPile.addLast(chosen);
        game.showAttackDesc("7️⃣ 弃掉: " + chosen);
        game.updateDisplay();
        Timer t = new Timer(Game.DELAY_EFFECT, e -> { ((Timer)e.getSource()).stop(); onDone.run(); });
        t.start();
    }

    void doChanFourSwapChoice(int playerCardIndex) {
        if (!game.chanFourSwapMode) return;
        if (playerCardIndex < 0 || playerCardIndex >= game.playerHand.size()) return;

        Card swapCard = game.playerHand.remove(playerCardIndex);
        Card drawn = game.chanFourSwapDrawn;
        game.playerHand.add(drawn);
        game.getAIHand().add(swapCard);

        game.chanFourSwapMode = false;
        game.chanFourSwapDrawn = null;
        Runnable onDone = game.chanFourSwapCallback;
        game.chanFourSwapCallback = null;

        game.showAttackDesc("4️⃣ 用" + swapCard + "交换AI的" + drawn);
        GameAnim.playFloatingText(game, "交换" + drawn + "⇄" + swapCard, new Color(100, 180, 255),
            new Point(game.getWidth() / 2, game.getHeight() / 3 - 30));
        game.updateDisplay();
        Timer t = new Timer(Game.DELAY_EFFECT, e -> { ((Timer)e.getSource()).stop(); onDone.run(); });
        t.start();
    }

    void doChanFourDiscard() {
        if (!game.chanFourSwapMode) return;
        Card drawn = game.chanFourSwapDrawn;
        game.discardPile.addLast(drawn);

        game.chanFourSwapMode = false;
        game.chanFourSwapDrawn = null;
        Runnable onDone = game.chanFourSwapCallback;
        game.chanFourSwapCallback = null;

        game.pendingAttack = new GameCharacter.AttackResult();
        game.pendingAttack.damage = 2;
        game.pendingAttack.skipDefense = true;
        game.showAttackDesc("4️⃣ 弃掉" + drawn + " + 2点伤害");
        game.showDefendDesc("跳过防御");
        Timer skipTimer = new Timer(Game.DELAY_SKIP, e -> {
            ((Timer)e.getSource()).stop();
            game.resolvePostDefense(game.playerChar, game.aiChar);
            game.clearAIZones();
            game.currentPhase = Game.Phase.PLAYER_PLAY;
            game.updateDisplay();
        });
        skipTimer.start();
    }


    @Override
    void handleChanThreeDefendReveal(GameCharacter self, GameCharacter opponent,
                                      List<Card> selfHand, Runnable onDone) {
        game.refillDeckIfNeeded();
        if (game.deck.isEmpty()) {
            game.showDefendDesc("3️⃣ 牌库为空，防御失败");
            Timer t = new Timer(Game.DELAY_EFFECT, e -> { ((Timer)e.getSource()).stop(); onDone.run(); });
            t.start();
            return;
        }

        Card revealed = game.deck.draw();
        boolean isItem = revealed.isItemCard();

        Point from = GameAnim.getDeckCenter(game.ui, game);
        Point to = GameAnim.getRevealPanelCenter(game.ui, game);
        GameAnim.playFlyAnimation(game, revealed, from, to, () -> {
            game.showAIRevealCard(revealed);

            Timer t = new Timer(Game.DELAY_EFFECT, e -> {
                ((Timer)e.getSource()).stop();

                int healAmt = 0;
                if (isItem) {
                    healAmt = 0;
                } else {
                    healAmt = (int) Math.ceil(revealed.getValue() / 2.0);
                }

                if (healAmt > 0) {
                    self.heal(healAmt);
                    Point loc = self == game.playerChar
                        ? new Point(game.getWidth() / 2, game.getHeight() * 3 / 4)
                        : new Point(game.getWidth() / 2, game.getHeight() / 3);
                    GameAnim.playFloatingText(game, "+" + healAmt, new Color(60, 220, 60), loc);
                    game.showDefendDesc("3️⃣ 判定" + revealed + " → 恢复" + healAmt + "点");
                } else {
                    game.showDefendDesc("3️⃣ 判定" + revealed + " → 无恢复");
                }

                selfHand.add(revealed);
                game.updateDisplay();
                Timer t2 = new Timer(Game.DELAY_STEP, e2 -> { ((Timer)e2.getSource()).stop(); onDone.run(); });
                t2.start();
            });
            t.start();
        });
    }

    @Override
    void handleChanSixReveal(GameCharacter self, GameCharacter opponent,
                              List<Card> selfHand, Runnable onDone) {
        game.refillDeckIfNeeded();
        if (game.deck.isEmpty()) {
            game.showAttackDesc("6️⃣ 牌库为空，无判定");
            if (game.pendingAttack != null) {
                if (self == game.playerChar) {
                    game.currentPhase = Game.Phase.AI_DEFEND;
                    game.updateDisplay();
                    game.aiTimer = new Timer(Game.DELAY_STEP, e2 -> { game.aiTimer.stop(); game.doAIDefend(); });
                    game.aiTimer.start();
                } else {
                    game.enterPlayerDefend();
                }
            }
            return;
        }

        Card revealed = game.deck.draw();
        boolean isUnblockable = revealed.isBlack() || revealed.isWhite() ||
            revealed.getColor() == Card.CardColor.BLUE;

        Point from = GameAnim.getDeckCenter(game.ui, game);
        Point to = GameAnim.getRevealPanelCenter(game.ui, game);
        GameAnim.playFlyAnimation(game, revealed, from, to, () -> {
            game.showAIRevealCard(revealed);

            Timer t = new Timer(Game.DELAY_SKIP, e -> {
                ((Timer)e.getSource()).stop();

                selfHand.add(revealed);

                if (isUnblockable) {
                    game.pendingAttack.skipDefense = true;
                    game.showAttackDesc("6️⃣ 判定" + revealed + " → 蓝/白/黑跳过防御");
                    game.showDefendDesc("跳过防御");
                    if (self == game.playerChar) {
                        Timer skipTimer = new Timer(Game.DELAY_SKIP, e2 -> {
                            ((Timer)e2.getSource()).stop();
                            game.resolvePostDefense(game.playerChar, game.aiChar);
                            game.clearAIZones();
                            game.currentPhase = Game.Phase.PLAYER_PLAY;
                            game.updateDisplay();
                            onDone.run();
                        });
                        skipTimer.start();
                    } else {
                        Timer skipTimer = new Timer(Game.DELAY_SKIP, e2 -> {
                            ((Timer)e2.getSource()).stop();
                            game.resolvePostDefense(game.aiChar, game.playerChar);
                            game.clearAIZones();
                            game.updateDisplay();
                            onDone.run();
                        });
                        skipTimer.start();
                    }
                } else {
                    game.showAttackDesc("6️⃣ 判定" + revealed + " → 可防御");
                    if (self == game.playerChar) {
                        game.currentPhase = Game.Phase.AI_DEFEND;
                        game.updateDisplay();
                        game.aiTimer = new Timer(Game.DELAY_STEP, e2 -> { game.aiTimer.stop(); game.doAIDefend(); });
                        game.aiTimer.start();
                    } else {
                        game.enterPlayerDefend();
                    }
                }
            });
            t.start();
        });
    }
}
