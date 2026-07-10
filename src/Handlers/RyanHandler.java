import javax.swing.*;
import java.awt.*;
import java.util.List;

public class RyanHandler extends CharacterHandler {

    RyanHandler(Game game) {
        super(game);
    }

    @Override
    void handleRevealTopDeck(GameCharacter self, GameCharacter opponent, List<Card> selfHand) {
        game.refillDeckIfNeeded();
        if (game.deck.isEmpty()) {
            game.pendingAttack = null;
            game.currentPhase = Game.Phase.PLAYER_PLAY;
            game.clearAIZones();
            game.updateDisplay();
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

                self.heal(revealResult.selfHeal);
                if (revealResult.selfHeal > 0) {
                    GameAnim.playFloatingText(game, "+" + revealResult.selfHeal, new Color(60, 220, 60),
                        new Point(game.getWidth() / 2, game.getHeight() * 3 / 4));
                }

                if (revealResult.drawCount == -1) {
                    selfHand.add(revealed);
                    if (revealResult.damage > 0) {
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
                    } else {
                        game.pendingAttack = null;
                        game.currentPhase = Game.Phase.PLAYER_PLAY;
                        game.clearAIZones();
                        game.updateDisplay();
                    }
                } else if (revealResult.discardRevealed) {
                    game.discardPile.addLast(revealed);
                    if (revealResult.damage > 0) {
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
                    } else {
                        game.pendingAttack = null;
                        game.currentPhase = Game.Phase.PLAYER_PLAY;
                        game.clearAIZones();
                        game.updateDisplay();
                    }
                } else if (revealResult.drawCount > 0) {
                    game.discardPile.addLast(revealed);
                    List<Card> drawn = game.drawFromDeck(revealResult.drawCount);
                    if (!drawn.isEmpty()) {
                        selfHand.addAll(drawn);
                        GameAnim.playDrawAnimations(game, drawn.size(), self == game.playerChar, () -> {
                            game.pendingAttack = null;
                            game.currentPhase = Game.Phase.PLAYER_PLAY;
                            game.clearAIZones();
                            game.updateDisplay();
                        });
                    } else {
                        game.pendingAttack = null;
                        game.currentPhase = Game.Phase.PLAYER_PLAY;
                        game.clearAIZones();
                        game.updateDisplay();
                    }
                } else {
                    game.discardPile.addLast(revealed);
                    if (revealResult.damage > 0) {
                        game.pendingAttack = revealResult;
                        game.currentPhase = Game.Phase.AI_DEFEND;
                        game.updateDisplay();
                        game.aiTimer = new Timer(Game.DELAY_STEP, e2 -> {
                            game.aiTimer.stop();
                            game.doAIDefend();
                        });
                        game.aiTimer.start();
                    } else {
                        game.pendingAttack = null;
                        game.currentPhase = Game.Phase.PLAYER_PLAY;
                        game.clearAIZones();
                        game.updateDisplay();
                    }
                }
            });
            revealTimer.start();
        });
    }

    @Override
    void handleAIRevealTopDeck(GameCharacter self, GameCharacter opponent, List<Card> selfHand) {
        game.refillDeckIfNeeded();
        if (game.deck.isEmpty()) {
            game.pendingAttack = null;
            game.updateDisplay();
            game.resumeAITurn();
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

                self.heal(revealResult.selfHeal);
                if (revealResult.selfHeal > 0) {
                    GameAnim.playFloatingText(game, "+" + revealResult.selfHeal, new Color(60, 220, 60),
                        new Point(game.getWidth() / 2, game.getHeight() / 3));
                }

                if (revealResult.drawCount == -1) {
                    selfHand.add(revealed);
                    if (revealResult.damage > 0) {
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
                    } else {
                        game.pendingAttack = null;
                        game.clearAIZones();
                        game.updateDisplay();
                        if (self != game.playerChar) game.resumeAITurn();
                    }
                } else if (revealResult.discardRevealed) {
                    game.discardPile.addLast(revealed);
                    if (revealResult.damage > 0) {
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
                    } else {
                        game.pendingAttack = null;
                        game.clearAIZones();
                        game.updateDisplay();
                        if (self != game.playerChar) game.resumeAITurn();
                    }
                } else if (revealResult.drawCount > 0) {
                    game.discardPile.addLast(revealed);
                    List<Card> drawn = game.drawFromDeck(revealResult.drawCount);
                    if (!drawn.isEmpty()) {
                        selfHand.addAll(drawn);
                        GameAnim.playDrawAnimations(game, drawn.size(), false, () -> {
                            game.pendingAttack = null;
                            game.clearAIZones();
                            game.updateDisplay();
                            game.resumeAITurn();
                        });
                    } else {
                        game.pendingAttack = null;
                        game.clearAIZones();
                        game.updateDisplay();
                        game.resumeAITurn();
                    }
                } else {
                    game.discardPile.addLast(revealed);
                    if (revealResult.damage > 0) {
                        game.pendingAttack = revealResult;
                        game.enterPlayerDefend();
                    } else {
                        game.pendingAttack = null;
                        game.clearAIZones();
                        game.updateDisplay();
                        game.resumeAITurn();
                    }
                }
            });
            revealTimer.start();
        });
    }

    @Override
    void handleRevealDraw(GameCharacter self, GameCharacter opponent, List<Card> selfHand, boolean isPlayer) {
        int count = game.pendingAttack.revealDrawCount > 0 ? game.pendingAttack.revealDrawCount : game.pendingAttack.drawCount;
        List<Card> drawn = game.drawFromDeck(count);
        if (drawn.isEmpty()) {
            game.pendingAttack = null;
            game.currentPhase = isPlayer ? Game.Phase.PLAYER_PLAY : Game.Phase.AI_TURN;
            game.clearAIZones();
            game.updateDisplay();
            if (!isPlayer) game.resumeAITurn();
            return;
        }

        if (!isPlayer) {
            Point from = GameAnim.getDeckCenter(game.ui, game);
            Point to = GameAnim.getRevealPanelCenter(game.ui, game);
            Card firstDrawn = drawn.get(0);
            GameAnim.playFlyAnimation(game, firstDrawn, from, to, () -> {
                game.showAIRevealCards(drawn);
                Timer revealTimer = new Timer(Game.DELAY_REVEAL, e -> {
                    ((Timer)e.getSource()).stop();
                    selfHand.addAll(drawn);
                    int revealDmg = 0;
                    for (Card dc : drawn) {
                        if (dc.isNumberCard()) revealDmg += dc.getValue();
                        else revealDmg += 4;
                    }
                    if (revealDmg > 0) {
                        game.pendingAttack.damage = revealDmg;
                        game.showAttackDesc("公示牌合计" + revealDmg + "点伤害");
                        game.enterPlayerDefend();
                    } else {
                        game.pendingAttack = null;
                        game.currentPhase = Game.Phase.AI_TURN;
                        game.clearAIZones();
                        game.updateDisplay();
                        game.resumeAITurn();
                    }
                });
                revealTimer.start();
            });
            return;
        }

        Point from = GameAnim.getDeckCenter(game.ui, game);
        Point to = GameAnim.getRevealPanelCenter(game.ui, game);
        Card firstDrawn = drawn.get(0);
        GameAnim.playFlyAnimation(game, firstDrawn, from, to, () -> {
            game.showAIRevealCards(drawn);
            Timer revealTimer = new Timer(Game.DELAY_REVEAL, e -> {
                ((Timer)e.getSource()).stop();
                selfHand.addAll(drawn);
                int revealDmg = 0;
                for (Card dc : drawn) {
                    if (dc.isNumberCard()) revealDmg += dc.getValue();
                    else revealDmg += 4;
                }
                if (revealDmg > 0) {
                    game.pendingAttack.damage = revealDmg;
                    game.showAttackDesc("公示牌合计" + revealDmg + "点伤害");
                    game.currentPhase = Game.Phase.AI_DEFEND;
                    game.updateDisplay();
                    game.aiTimer = new Timer(Game.DELAY_STEP, e2 -> {
                        game.aiTimer.stop();
                        game.doAIDefend();
                    });
                    game.aiTimer.start();
                } else {
                    game.pendingAttack = null;
                    game.currentPhase = Game.Phase.PLAYER_PLAY;
                    game.clearAIZones();
                    game.updateDisplay();
                }
            });
            revealTimer.start();
        });
    }

    @Override
    void doSevenChoice(int aiCardIndex) {
        if (game.currentPhase != Game.Phase.PLAYER_SEVEN_CHOICE) return;
        List<Card> oppHand = game.getAIHand();
        if (aiCardIndex < 0 || aiCardIndex >= oppHand.size()) return;
        Card chosen = oppHand.remove(aiCardIndex);
        game.discardPile.addLast(chosen);
        game.pendingFiveChoice = false;
        game.forceOpponentDiscardOne = false;
        game.showAttackDesc("7 弃掉AI的" + chosen);
        GameAnim.playFloatingText(game, "弃" + chosen, new Color(255, 60, 60),
            new Point(game.getWidth() / 2, game.getHeight() / 3 - 30));
        if (game.pendingAttack != null && game.pendingAttack.damage > 0) {
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
    void doFiveChoiceHeal() {
        if (!game.pendingFiveChoice || game.currentPhase != Game.Phase.PLAYER_FIVE_CHOICE) return;
        if (game.selectedSingle < 0 || game.selectedSingle >= game.playerHand.size()) {
            game.showMessage("请先选择一张数字牌！");
            return;
        }
        Card second = game.playerHand.get(game.selectedSingle);
        if (second.isBlack()) {
            game.showMessage("5️⃣效果需出数字牌，不能出黑牌！");
            return;
        }
        finishFiveChoice(second, false);
    }

    @Override
    void doFiveChoiceDamage() {
        if (!game.pendingFiveChoice || game.currentPhase != Game.Phase.PLAYER_FIVE_CHOICE) return;
        if (game.selectedSingle < 0 || game.selectedSingle >= game.playerHand.size()) {
            game.showMessage("请先选择一张数字牌！");
            return;
        }
        Card second = game.playerHand.get(game.selectedSingle);
        if (second.isBlack()) {
            game.showMessage("5️⃣效果需出数字牌，不能出黑牌！");
            return;
        }
        finishFiveChoice(second, true);
    }

    private void finishFiveChoice(Card second, boolean chooseDamage) {
        int cardIdx = game.selectedSingle;
        Point from = GameAnim.getPlayerHandCardCenter(game.ui, game, cardIdx);
        Point to = GameAnim.getRevealPanelCenter(game.ui, game);
        game.playerHand.remove(cardIdx);
        game.discardPile.addLast(second);
        game.selectedSingle = -1;
        GameCharacter.AttackResult fiveResult = game.playerChar.resolveFive(second, chooseDamage);
        game.playerChar.heal(fiveResult.selfHeal);
        if (fiveResult.selfHeal > 0) {
            GameAnim.playFloatingText(game, "+" + fiveResult.selfHeal, new Color(60, 220, 60),
                new Point(game.getWidth() / 2, game.getHeight() * 3 / 4));
        }
        if (fiveResult.damage > 0 && chooseDamage) {
            game.pendingAttack = new GameCharacter.AttackResult();
            game.pendingAttack.damage = fiveResult.damage;
            game.pendingAttack.desc = fiveResult.desc;
        }
        game.showAttackDesc(fiveResult.desc);
        game.pendingFiveChoice = false;
        game.fiveChoiceCard = null;
        if (!game.aiChar.isAlive()) {
            GameAnim.playFlyAnimation(game, second, from, to, () -> game.endGame("你"));
            return;
        }
        if (chooseDamage && fiveResult.damage > 0) {
            game.currentPhase = Game.Phase.AI_DEFEND;
            GameAnim.playFlyAnimation(game, second, from, to, () -> {
                game.showAIRevealCard(second);
                game.updateDisplay();
                game.aiTimer = new Timer(Game.DELAY_PLAY, e -> {
                    game.aiTimer.stop();
                    game.doAIDefend();
                });
                game.aiTimer.start();
            });
        } else {
            if (fiveResult.damage > 0) {
                game.aiChar.takeDamage(fiveResult.damage);
                GameAnim.playFloatingText(game, "-" + fiveResult.damage, new Color(255, 60, 60),
                    new Point(game.getWidth() / 2, game.getHeight() / 3));
                if (!game.aiChar.isAlive()) {
                    GameAnim.playFlyAnimation(game, second, from, to, () -> game.endGame("你"));
                    return;
                }
            }
            game.currentPhase = Game.Phase.PLAYER_PLAY;
            GameAnim.playFlyAnimation(game, second, from, to, () -> {
                game.showAIRevealCard(second);
                game.updateDisplay();
                Timer clearDelay = new Timer(Game.DELAY_STEP, ev -> {
                    ((Timer)ev.getSource()).stop();
                    game.clearAIZones();
                    game.updateDisplay();
                });
                clearDelay.setRepeats(false);
                clearDelay.start();
            });
        }
    }

    @Override
    void resolveAIFiveChoice(GameCharacter self, GameCharacter opponent, List<Card> selfHand, Runnable onDone) {
        Card second = null;
        for (Card c : selfHand) {
            if (!c.isBlack()) { second = c; break; }
        }
        if (second == null) {
            game.pendingFiveChoice = false;
            game.fiveChoiceCard = null;
            game.pendingAttack = null;
            game.clearAIZones();
            game.updateDisplay();
            onDone.run();
            return;
        }
        Card[] sec = {second};
        selfHand.remove(second);
        game.discardPile.addLast(second);
        boolean chooseDamage = game.getCurrentTurnAI().chooseFiveDamage(self.getCurrentHp(), self.getMaxHp(), opponent.getCurrentHp());
        GameCharacter.AttackResult fiveResult = self.resolveFive(second, chooseDamage);
        Point from = GameAnim.getAIHandCenter(game.ui, game);
        Point to = GameAnim.getRevealPanelCenter(game.ui, game);
        GameAnim.playFlyAnimation(game, second, from, to, () -> {
            game.showAIRevealCard(sec[0]);
            self.heal(fiveResult.selfHeal);
            if (fiveResult.selfHeal > 0) {
                GameAnim.playFloatingText(game, "+" + fiveResult.selfHeal, new Color(60, 220, 60),
                    new Point(game.getWidth() / 2, game.getHeight() / 3));
            }
            game.showAttackDesc(fiveResult.desc);
            game.pendingFiveChoice = false;
            game.fiveChoiceCard = null;
            if (chooseDamage && fiveResult.damage > 0) {
                game.pendingAttack = new GameCharacter.AttackResult();
                game.pendingAttack.damage = fiveResult.damage;
                game.pendingAttack.desc = fiveResult.desc;
                game.enterPlayerDefend();
            } else {
                if (fiveResult.damage > 0) {
                    opponent.takeDamage(fiveResult.damage);
                    GameAnim.playFloatingText(game, "-" + fiveResult.damage, new Color(255, 60, 60),
                        new Point(game.getWidth() / 2, game.getHeight() * 3 / 4));
                }
                game.pendingAttack = null;
                Timer clearDelay = new Timer(Game.DELAY_STEP, ev -> {
                    ((Timer)ev.getSource()).stop();
                    game.clearAIZones();
                    game.updateDisplay();
                });
                clearDelay.setRepeats(false);
                clearDelay.start();
                if (!opponent.isAlive()) {
                    String winner = opponent == game.playerChar ? "AI" : "你";
                    Timer deathDelay = new Timer(500, ev -> {
                        ((Timer)ev.getSource()).stop();
                        game.endGame(winner);
                    });
                    deathDelay.setRepeats(false);
                    deathDelay.start();
                    return;
                }
                Timer delay = new Timer(Game.DELAY_STEP, e -> {
                    ((Timer)e.getSource()).stop();
                    onDone.run();
                });
                delay.setRepeats(false);
                delay.start();
            }
        });
    }
}