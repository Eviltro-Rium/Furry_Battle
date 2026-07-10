import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DefenseEngine {
    Game game;

    DefenseEngine(Game game) { this.game = game; }

    void doAIDefend() {
        Card top = game.discardPile.getFirst();
        AIPlayer defAI = game.is1v2 ? game.getCurrentTargetAI() : game.ai;
        GameCharacter defChar = game.is1v2 ? game.getCurrentAttackTarget() : game.aiChar;

        // === 已打出黑牌搭桥，这是第二步：出真正的防御牌 ===
        if (game.aiHasPlayedBlackDefend) {
            game.aiHasPlayedBlackDefend = false;
            defAI.aiHasDebuff = defChar.hasDebuff();
            defAI.aiDebuffCount = defChar.getBurnStacks() + (defChar.isFrozen() ? 1 : 0) + defChar.getBleedStacks();
            defAI.aiFullHp = defChar.getCurrentHp() >= defChar.getMaxHp();
            defAI.aiOpponentHandSize = game.playerHand.size();
            Card defCard = defAI.chooseDefend(top, true);
            if (defCard != null) {
                if (defCard.isWhite()) {
                    defCard.setChosenColor(top.getEffectiveColor());
                }
                Point from = GameAnim.getAIHandCenter(game.ui, game);
                Point to = GameAnim.getAttackPanelCenter(game.ui, game);

                defAI.removeCard(defCard);
                game.discardPile.addFirst(defCard);
                game.pendingDefendCard = null;

                // 继续搭桥
                if (defCard.isBlack()) {
                    game.aiHasPlayedBlackDefend = true;
                    GameAnim.playFlyAnimation(game, defCard, from, to, () -> {
                        if (defCard.isDrawTwo()) {
                            List<Card> drawn = game.drawFromDeck(2);
                            if (!drawn.isEmpty()) {
                                GameAnim.playDrawAnimations(game, drawn.size(), false, () -> {
                                    defAI.addCards(drawn);
                                    game.updateDisplay();
                                    Timer bridgeTimer = new Timer(Game.DELAY_STEP, e2 -> {
                                        ((Timer)e2.getSource()).stop();
                                        game.defenseEngine.doAIDefend();
                                    });
                                    bridgeTimer.start();
                                });
                            } else {
                                Timer bridgeTimer = new Timer(Game.DELAY_STEP, e2 -> {
                                        ((Timer)e2.getSource()).stop();
                                        game.defenseEngine.doAIDefend();
                                    });
                                    bridgeTimer.start();
                            }
                        } else {
                            Timer bridgeTimer = new Timer(Game.DELAY_STEP, e2 -> {
                                ((Timer)e2.getSource()).stop();
                                game.defenseEngine.doAIDefend();
                            });
                            bridgeTimer.start();
                        }
                    });
                    return;
                }

                if (defCard.isPotion()) {
                    game.aiHasPlayedBlackDefend = true;
                    defChar.heal(5);
                    GameAnim.playFlyAnimation(game, defCard, from, to, () -> {
                        GameAnim.playFloatingText(game, "+5[生命]", new Color(60, 220, 60),
                            new Point(game.getWidth() / 2, game.getHeight() / 3));
                        Timer bridgeTimer = new Timer(Game.DELAY_STEP, e2 -> {
                            ((Timer)e2.getSource()).stop();
                            game.defenseEngine.doAIDefend();
                        });
                        bridgeTimer.start();
                    });
                    return;
                }

                if (defCard.isPurify()) {
                    game.aiHasPlayedBlackDefend = true;
                    GameAnim.playFlyAnimation(game, defCard, from, to, () -> {
                        game.attackEngine.applyPurifyEffect(defChar, false);
                        Timer bridgeTimer = new Timer(Game.DELAY_STEP, e2 -> {
                            ((Timer)e2.getSource()).stop();
                            game.defenseEngine.doAIDefend();
                        });
                        bridgeTimer.start();
                    });
                    return;
                }

                if (defCard.isSuperPurify()) {
                    game.aiHasPlayedBlackDefend = true;
                    GameAnim.playFlyAnimation(game, defCard, from, to, () -> {
                        game.attackEngine.applySuperPurifyEffect(defChar, false);
                        Timer bridgeTimer = new Timer(Game.DELAY_STEP, e2 -> {
                            ((Timer)e2.getSource()).stop();
                            game.defenseEngine.doAIDefend();
                        });
                        bridgeTimer.start();
                    });
                    return;
                }

                if (defCard.isDrawThree()) {
                    game.aiHasPlayedBlackDefend = true;
                    List<Card> drawn = game.drawFromDeck(3);
                    GameAnim.playFlyAnimation(game, defCard, from, to, () -> {
                        if (!drawn.isEmpty()) {
                            GameAnim.playDrawAnimations(game, drawn.size(), false, () -> {
                                defAI.addCards(drawn);
                                game.updateDisplay();
                                Timer bridgeTimer = new Timer(Game.DELAY_STEP, e2 -> {
                                    ((Timer)e2.getSource()).stop();
                                    game.defenseEngine.doAIDefend();
                                });
                                bridgeTimer.start();
                            });
                        } else {
                            Timer bridgeTimer = new Timer(Game.DELAY_STEP, e2 -> {
                                ((Timer)e2.getSource()).stop();
                                game.defenseEngine.doAIDefend();
                            });
                            bridgeTimer.start();
                        }
                    });
                    return;
                }

                if (defCard.isSwapHand()) {
                    game.aiHasPlayedBlackDefend = true;
                    GameAnim.playFlyAnimation(game, defCard, from, to, () -> {
                        List<Card> tmp = new ArrayList<>(game.playerHand);
                        game.playerHand.clear();
                        game.playerHand.addAll(defAI.getHand());
                        defAI.getHand().clear();
                        defAI.getHand().addAll(tmp);
                        GameAnim.playFloatingText(game, "[交换]", new Color(100, 80, 200),
                            new Point(game.getWidth() / 2, game.getHeight() / 2));
                        game.updateDisplay();
                        Timer bridgeTimer = new Timer(Game.DELAY_STEP, e2 -> {
                            ((Timer)e2.getSource()).stop();
                            game.defenseEngine.doAIDefend();
                        });
                        bridgeTimer.start();
                    });
                    return;
                }

                if (defCard.isShuffleToDeck()) {
                    game.aiHasPlayedBlackDefend = true;
                    GameAnim.playFlyAnimation(game, defCard, from, to, () -> {
                        Card topCard = game.discardPile.isEmpty() ? null : game.discardPile.removeFirst();
                        List<Card> toShuffle = new ArrayList<>(game.discardPile);
                        for (Card c : toShuffle) {
                            if (c.isBlack() || c.isWhite()) c.setChosenColor(null);
                        }
                        game.discardPile.clear();
                        if (topCard != null) game.discardPile.addFirst(topCard);
                        game.deck.addCards(toShuffle);
                        game.deck.shuffle();
                        GameAnim.playFloatingText(game, "[洗入]" + toShuffle.size() + "张", new Color(100, 80, 200),
                            new Point(game.getWidth() / 2, game.getHeight() / 2 - 30));
                        game.updateDisplay();
                        Timer bridgeTimer = new Timer(Game.DELAY_STEP, e2 -> {
                            ((Timer)e2.getSource()).stop();
                            game.defenseEngine.doAIDefend();
                        });
                        bridgeTimer.start();
                    });
                    return;
                }

                // 非搭桥牌 → 完成防御
                game.pendingDefendCard = defCard;
                game.defenseEngine.finishAIDefend();
                GameAnim.playFlyAnimation(game, defCard, from, to, () -> {
                    game.showDefendCard(defCard);
                    game.updateDisplay();
                });
            } else {
                game.defenseEngine.finishAIDefend();
            }
            return;
        }

        // === 第一步：正常防御 ===
        if (top.isBlack()) {
            game.showDefendDesc("跳过防御");
            game.defenseEngine.finishAIDefend();
            return;
        }

        boolean isBlueAttack = top.getEffectiveColor() == Card.CardColor.BLUE;
        if (defChar.isFrozen() && isBlueAttack) {
            game.showDefendDesc("冷冻 → 无法防御蓝色攻击");
            game.defenseEngine.finishAIDefend();
            return;
        }

        defAI.aiHasDebuff = defChar.hasDebuff();
        defAI.aiFullHp = defChar.getCurrentHp() >= defChar.getMaxHp();
        defAI.aiOpponentHandSize = game.playerHand.size();
        Card defCard = defAI.chooseDefend(top);
        if (defCard != null) {
            if (defCard.isBlack()) {
                Card.CardColor chosen = defAI.chooseBlackColor();
                defCard.setChosenColor(chosen);
            }
            if (defCard.isWhite()) {
                defCard.setChosenColor(top.getEffectiveColor());
            }

            Point from = GameAnim.getAIHandCenter(game.ui, game);
            Point to = GameAnim.getAttackPanelCenter(game.ui, game);

            defAI.removeCard(defCard);
            game.discardPile.addFirst(defCard);
            game.pendingDefendCard = defCard;

            game.aiDefendSuccess = true;
            game.updateDisplay();
            game.aiDefendSuccess = false;


            // 🧪牌搭桥 → 恢复5点+留在防御阶段
            if (defCard.isPotion()) {
                game.aiHasPlayedBlackDefend = true;
                game.pendingDefendCard = null;
                defChar.heal(5);
                GameAnim.playFlyAnimation(game, defCard, from, to, () -> {
                    GameAnim.playFloatingText(game, "+5[生命]", new Color(60, 220, 60),
                        new Point(game.getWidth() / 2, game.getHeight() / 3));
                    Timer bridgeTimer = new Timer(Game.DELAY_STEP, e2 -> {
                        ((Timer)e2.getSource()).stop();
                        game.defenseEngine.doAIDefend();
                    });
                    bridgeTimer.start();
                });
            }
            // ✨净化牌搭桥 → 净化1层buff+留在防御阶段
            else if (defCard.isPurify()) {
                game.aiHasPlayedBlackDefend = true;
                game.pendingDefendCard = null;
                GameAnim.playFlyAnimation(game, defCard, from, to, () -> {
                    game.attackEngine.applyPurifyEffect(defChar, false);
                    Timer bridgeTimer = new Timer(Game.DELAY_STEP, e2 -> {
                        ((Timer)e2.getSource()).stop();
                        game.defenseEngine.doAIDefend();
                    });
                    bridgeTimer.start();
                });
            }
            // ✨✨超级净化牌搭桥 → 清除所有buff+留在防御阶段
            else if (defCard.isSuperPurify()) {
                game.aiHasPlayedBlackDefend = true;
                game.pendingDefendCard = null;
                GameAnim.playFlyAnimation(game, defCard, from, to, () -> {
                    game.attackEngine.applySuperPurifyEffect(defChar, false);
                    Timer bridgeTimer = new Timer(Game.DELAY_STEP, e2 -> {
                        ((Timer)e2.getSource()).stop();
                        game.defenseEngine.doAIDefend();
                    });
                    bridgeTimer.start();
                });
            }
            // +3牌搭桥 → 抽3张+留在防御阶段
            else if (defCard.isDrawThree()) {
                game.aiHasPlayedBlackDefend = true;
                game.pendingDefendCard = null;
                List<Card> drawn = game.drawFromDeck(3);
                GameAnim.playFlyAnimation(game, defCard, from, to, () -> {
                    if (!drawn.isEmpty()) {
                        GameAnim.playDrawAnimations(game, drawn.size(), false, () -> {
                            defAI.addCards(drawn);
                            game.updateDisplay();
                            Timer bridgeTimer = new Timer(Game.DELAY_STEP, e2 -> {
                                ((Timer)e2.getSource()).stop();
                                game.defenseEngine.doAIDefend();
                            });
                            bridgeTimer.start();
                        });
                    } else {
                        Timer bridgeTimer = new Timer(Game.DELAY_STEP, e2 -> {
                            ((Timer)e2.getSource()).stop();
                            game.defenseEngine.doAIDefend();
                        });
                        bridgeTimer.start();
                    }
                });
            }
            // 交换牌搭桥 → 交换双方手牌+留在防御阶段
            else if (defCard.isSwapHand()) {
                game.aiHasPlayedBlackDefend = true;
                game.pendingDefendCard = null;
                GameAnim.playFlyAnimation(game, defCard, from, to, () -> {
                    List<Card> tmp = new ArrayList<>(game.playerHand);
                    game.playerHand.clear();
                    game.playerHand.addAll(defAI.getHand());
                    defAI.getHand().clear();
                    defAI.getHand().addAll(tmp);
                    GameAnim.playFloatingText(game, "[交换]", new Color(100, 80, 200),
                        new Point(game.getWidth() / 2, game.getHeight() / 2));
                    game.updateDisplay();
                    Timer bridgeTimer = new Timer(Game.DELAY_STEP, e2 -> {
                        ((Timer)e2.getSource()).stop();
                        game.defenseEngine.doAIDefend();
                    });
                    bridgeTimer.start();
                });
            }
            // 黑牌搭桥 → 留在防御阶段，等timer再次调用doAIDefend
            else if (defCard.isBlack()) {
                game.aiHasPlayedBlackDefend = true;
                game.pendingDefendCard = null;
                GameAnim.playFlyAnimation(game, defCard, from, to, () -> {
                    if (defCard.isDrawTwo()) {
                        List<Card> drawn = game.drawFromDeck(2);
                        if (!drawn.isEmpty()) {
                            game.updateDisplay();
                            GameAnim.playDrawAnimations(game, drawn.size(), false, () -> {
                                defAI.addCards(drawn);
                                game.updateDisplay();
                                Timer bridgeTimer = new Timer(Game.DELAY_STEP, e2 -> {
                                    ((Timer)e2.getSource()).stop();
                                    game.defenseEngine.doAIDefend();
                                });
                                bridgeTimer.start();
                            });
                            return;
                        }
                    }
                    game.updateDisplay();
                    Timer bridgeTimer = new Timer(Game.DELAY_STEP, e2 -> {
                        ((Timer)e2.getSource()).stop();
                        game.defenseEngine.doAIDefend();
                    });
                    bridgeTimer.start();
                });
            } else {
                game.defenseEngine.finishAIDefend();
                GameAnim.playFlyAnimation(game, defCard, from, to, () -> {
                    game.showDefendCard(defCard);
                    game.updateDisplay();
                });
            }
        } else {
            game.pendingDefendCard = null;
            game.showDefendDesc("跳过防御");
            Timer delay = new Timer(Game.DELAY_STEP, e2 -> {
                ((Timer)e2.getSource()).stop();
                game.defenseEngine.finishAIDefend();
            });
            delay.start();
        }
    }

    void doPlayerDefend() {
        if (game.busy) return;
        if (game.currentPhase != Game.Phase.PLAYER_DEFEND) return;
        game.busy = true;
        if (game.selectedSingle < 0 || game.selectedSingle >= game.playerHand.size()) {
            game.busy = false;
            game.showMessage("请选择一张牌进行防御！");
            return;
        }
        if (game.discardPile.isEmpty()) { game.busy = false; return; }

        Card card = game.playerHand.get(game.selectedSingle);
        Card top = game.discardPile.getFirst();

        // === 已打出黑牌搭桥，这是第二步：出真正的防御牌 ===
        if (game.hasPlayedBlackDefend) {
            if (card.isBlack()) {
                Card.CardColor chosen = GameUI.showColorChooser(game);
                if (chosen == null) { game.busy = false; return; }
                card.setChosenColor(chosen);

                int cardIdx = game.selectedSingle;
                Point from = GameAnim.getPlayerHandCardCenter(game.ui, game, cardIdx);
                Point to = GameAnim.getAttackPanelCenter(game.ui, game);

                game.playerHand.remove(cardIdx);
                game.discardPile.addFirst(card);
                game.selectedSingle = -1;
                game.pendingDefendCard = null;

                GameAnim.playFlyAnimation(game, card, from, to, () -> {
                    if (card.isDrawTwo()) {
                        List<Card> drawn = game.drawFromDeck(2);
                        if (!drawn.isEmpty()) {
                            GameAnim.playDrawAnimations(game, drawn.size(), true, () -> {
                                game.playerHand.addAll(drawn);
                                game.busy = false;
                                game.updateDisplay();
                            });
                            return;
                        }
                    }
                    game.busy = false;
                    game.updateDisplay();
                });
                return;
            }

            if (card.isPotion()) {
                card.setChosenColor(top.getEffectiveColor());

                int cardIdx = game.selectedSingle;
                Point from = GameAnim.getPlayerHandCardCenter(game.ui, game, cardIdx);
                Point to = GameAnim.getAttackPanelCenter(game.ui, game);

                game.playerHand.remove(cardIdx);
                game.discardPile.addFirst(card);
                game.selectedSingle = -1;
                game.pendingDefendCard = null;
                game.hasPlayedBlackDefend = true;

                game.playerChar.heal(5);
                GameAnim.playFlyAnimation(game, card, from, to, () -> {
                    GameAnim.playFloatingText(game, "+5[生命]", new Color(60, 220, 60),
                        new Point(game.getWidth() / 2, game.getHeight() * 3 / 4));
                    game.busy = false;
                    game.updateDisplay();
                });
                return;
            }

            if (card.isDrawThree()) {
                card.setChosenColor(top.getEffectiveColor());

                int cardIdx = game.selectedSingle;
                Point from = GameAnim.getPlayerHandCardCenter(game.ui, game, cardIdx);
                Point to = GameAnim.getAttackPanelCenter(game.ui, game);

                game.playerHand.remove(cardIdx);
                game.discardPile.addFirst(card);
                game.selectedSingle = -1;
                game.pendingDefendCard = null;
                game.hasPlayedBlackDefend = true;

                List<Card> drawn = game.drawFromDeck(3);
                GameAnim.playFlyAnimation(game, card, from, to, () -> {
                    if (!drawn.isEmpty()) {
                        GameAnim.playDrawAnimations(game, drawn.size(), true, () -> {
                            game.playerHand.addAll(drawn);
                            game.busy = false;
                            game.updateDisplay();
                        });
                        return;
                    }
                    game.busy = false;
                    game.updateDisplay();
                });
                return;
            }

            if (card.isPurify()) {
                card.setChosenColor(top.getEffectiveColor());

                int cardIdx = game.selectedSingle;
                Point from = GameAnim.getPlayerHandCardCenter(game.ui, game, cardIdx);
                Point to = GameAnim.getAttackPanelCenter(game.ui, game);

                game.playerHand.remove(cardIdx);
                game.discardPile.addFirst(card);
                game.selectedSingle = -1;
                game.pendingDefendCard = null;

                GameAnim.playFlyAnimation(game, card, from, to, () -> {
                    game.attackEngine.applyPurifyEffect(game.playerChar, true);
                    game.busy = false;
                    game.updateDisplay();
                });
                return;
            }

            if (card.isSuperPurify()) {
                card.setChosenColor(top.getEffectiveColor());

                int cardIdx = game.selectedSingle;
                Point from = GameAnim.getPlayerHandCardCenter(game.ui, game, cardIdx);
                Point to = GameAnim.getAttackPanelCenter(game.ui, game);

                game.playerHand.remove(cardIdx);
                game.discardPile.addFirst(card);
                game.selectedSingle = -1;
                game.pendingDefendCard = null;

                GameAnim.playFlyAnimation(game, card, from, to, () -> {
                    game.attackEngine.applySuperPurifyEffect(game.playerChar, true);
                    game.busy = false;
                    game.updateDisplay();
                });
                return;
            }

            if (card.isSwapHand()) {
                card.setChosenColor(top.getEffectiveColor());

                int cardIdx = game.selectedSingle;
                Point from = GameAnim.getPlayerHandCardCenter(game.ui, game, cardIdx);
                Point to = GameAnim.getAttackPanelCenter(game.ui, game);

                game.playerHand.remove(cardIdx);
                game.discardPile.addFirst(card);
                game.selectedSingle = -1;
                game.pendingDefendCard = null;
                game.hasPlayedBlackDefend = true;

                List<Card> atkHand = game.getCurrentTurnAI().getHand();
                List<Card> tmp = new ArrayList<>(game.playerHand);
                game.playerHand.clear();
                game.playerHand.addAll(atkHand);
                atkHand.clear();
                atkHand.addAll(tmp);

                GameAnim.playFlyAnimation(game, card, from, to, () -> {
                    GameAnim.playFloatingText(game, "[交换]", new Color(100, 80, 200),
                        new Point(game.getWidth() / 2, game.getHeight() / 2));
                    game.busy = false;
                    game.updateDisplay();
                });
                return;
            }

            if (!game.canDefend(card, top)) {
                game.busy = false;
                game.showMessage("该牌无法防御！需数字≤3且颜色匹配" + game.colorName(top.getEffectiveColor()));
                return;
            }

            if (card.isWhite()) {
                card.setChosenColor(top.getEffectiveColor());
            }

            int cardIdx = game.selectedSingle;
            Point from = GameAnim.getPlayerHandCardCenter(game.ui, game, cardIdx);
            Point to = GameAnim.getAttackPanelCenter(game.ui, game);

            game.playerHand.remove(cardIdx);
            game.discardPile.addFirst(card);
            game.selectedSingle = -1;
            game.hasPlayedBlackDefend = false;
            game.pendingDefendCard = card;

            GameAnim.playFlyAnimation(game, card, from, to, () -> {
                game.showDefendCard(card);
                game.defenseEngine.finishPlayerDefend();
            });
            return;
        }

        // === 第一步：正常防御 ===


        // 出黑牌 → 改变颜色搭桥，留在防御阶段
        if (card.isBlack()) {
            Card.CardColor chosen = GameUI.showColorChooser(game);
            if (chosen == null) { game.busy = false; return; }
            card.setChosenColor(chosen);

            int cardIdx = game.selectedSingle;
            Point from = GameAnim.getPlayerHandCardCenter(game.ui, game, cardIdx);
            Point to = GameAnim.getAttackPanelCenter(game.ui, game);

            game.playerHand.remove(cardIdx);
            game.discardPile.addFirst(card);
            game.selectedSingle = -1;
            game.pendingDefendCard = null;
            game.hasPlayedBlackDefend = true;

            GameAnim.playFlyAnimation(game, card, from, to, () -> {
                if (card.isDrawTwo()) {
                    List<Card> drawn = game.drawFromDeck(2);
                    if (!drawn.isEmpty()) {
                        GameAnim.playDrawAnimations(game, drawn.size(), true, () -> {
                            game.playerHand.addAll(drawn);
                            game.busy = false;
                            game.updateDisplay();
                        });
                        return;
                    }
                }
                game.busy = false;
                game.updateDisplay();
            });
            return;
        }

        // 出🧪牌 → 恢复5点+搭桥，留在防御阶段
        if (card.isPotion()) {
            card.setChosenColor(top.getEffectiveColor());

            int cardIdx = game.selectedSingle;
            Point from = GameAnim.getPlayerHandCardCenter(game.ui, game, cardIdx);
            Point to = GameAnim.getAttackPanelCenter(game.ui, game);

            game.playerHand.remove(cardIdx);
            game.discardPile.addFirst(card);
            game.selectedSingle = -1;
            game.pendingDefendCard = null;
            game.hasPlayedBlackDefend = true;

            game.playerChar.heal(5);
            GameAnim.playFlyAnimation(game, card, from, to, () -> {
                GameAnim.playFloatingText(game, "+5[生命]", new Color(60, 220, 60),
                    new Point(game.getWidth() / 2, game.getHeight() * 3 / 4));
                game.busy = false;
                game.updateDisplay();
            });
            return;
        }

        // 出✨净化牌 → 净化1层buff+搭桥，留在防御阶段
        if (card.isPurify()) {
            card.setChosenColor(top.getEffectiveColor());

            int cardIdx = game.selectedSingle;
            Point from = GameAnim.getPlayerHandCardCenter(game.ui, game, cardIdx);
            Point to = GameAnim.getAttackPanelCenter(game.ui, game);

            game.playerHand.remove(cardIdx);
            game.discardPile.addFirst(card);
            game.selectedSingle = -1;
            game.pendingDefendCard = null;
            game.hasPlayedBlackDefend = true;

            GameAnim.playFlyAnimation(game, card, from, to, () -> {
                game.attackEngine.applyPurifyEffect(game.playerChar, true);
                game.busy = false;
                game.updateDisplay();
            });
            return;
        }

        // 出✨✨超级净化牌 → 清除所有buff+搭桥，留在防御阶段
        if (card.isSuperPurify()) {
            card.setChosenColor(top.getEffectiveColor());

            int cardIdx = game.selectedSingle;
            Point from = GameAnim.getPlayerHandCardCenter(game.ui, game, cardIdx);
            Point to = GameAnim.getAttackPanelCenter(game.ui, game);

            game.playerHand.remove(cardIdx);
            game.discardPile.addFirst(card);
            game.selectedSingle = -1;
            game.pendingDefendCard = null;
            game.hasPlayedBlackDefend = true;

            GameAnim.playFlyAnimation(game, card, from, to, () -> {
                game.attackEngine.applySuperPurifyEffect(game.playerChar, true);
                game.busy = false;
                game.updateDisplay();
            });
            return;
        }

        // 出+3牌 → 抽3张+搭桥，留在防御阶段
        if (card.isDrawThree()) {
            card.setChosenColor(top.getEffectiveColor());

            int cardIdx = game.selectedSingle;
            Point from = GameAnim.getPlayerHandCardCenter(game.ui, game, cardIdx);
            Point to = GameAnim.getAttackPanelCenter(game.ui, game);

            game.playerHand.remove(cardIdx);
            game.discardPile.addFirst(card);
            game.selectedSingle = -1;
            game.pendingDefendCard = null;
            game.hasPlayedBlackDefend = true;

            List<Card> drawn = game.drawFromDeck(3);
            GameAnim.playFlyAnimation(game, card, from, to, () -> {
                if (!drawn.isEmpty()) {
                    GameAnim.playDrawAnimations(game, drawn.size(), true, () -> {
                        game.playerHand.addAll(drawn);
                        game.busy = false;
                        game.updateDisplay();
                    });
                    return;
                }
                game.busy = false;
                game.updateDisplay();
            });
            return;
        }

        // 出交换牌 → 交换双方手牌+搭桥，留在防御阶段
        if (card.isSwapHand()) {
            card.setChosenColor(top.getEffectiveColor());

            int cardIdx = game.selectedSingle;
            Point from = GameAnim.getPlayerHandCardCenter(game.ui, game, cardIdx);
            Point to = GameAnim.getAttackPanelCenter(game.ui, game);

            game.playerHand.remove(cardIdx);
            game.discardPile.addFirst(card);
            game.selectedSingle = -1;
            game.pendingDefendCard = null;
            game.hasPlayedBlackDefend = true;

            List<Card> atkHand = game.getCurrentTurnAI().getHand();
            List<Card> tmp = new ArrayList<>(game.playerHand);
            game.playerHand.clear();
            game.playerHand.addAll(atkHand);
            atkHand.clear();
            atkHand.addAll(tmp);

            GameAnim.playFlyAnimation(game, card, from, to, () -> {
                GameAnim.playFloatingText(game, "[交换]", new Color(100, 80, 200),
                    new Point(game.getWidth() / 2, game.getHeight() / 2));
                game.busy = false;
                game.updateDisplay();
            });
            return;
        }

        if (!game.canDefend(card, top)) {
            game.busy = false;
            game.showMessage("该牌无法防御！需数字≤3且颜色或数字匹配弃牌库顶");
            return;
        }

        // 出白牌 → 自动指定为弃牌库顶颜色
        if (card.isWhite()) {
            card.setChosenColor(top.getEffectiveColor());
        }

        // 出非黑非白牌 → 直接防御成功
        int cardIdx = game.selectedSingle;
        Point from = GameAnim.getPlayerHandCardCenter(game.ui, game, cardIdx);
        Point to = GameAnim.getAttackPanelCenter(game.ui, game);

        game.playerHand.remove(cardIdx);
        game.discardPile.addFirst(card);
        game.selectedSingle = -1;
        game.pendingDefendCard = card;

        GameAnim.playFlyAnimation(game, card, from, to, () -> {
            game.showDefendCard(card);
            game.defenseEngine.finishPlayerDefend();
        });
    }

    void doPlayerSkipDefend() {
        if (game.busy) return;
        game.busy = true;
        if (game.chanSevenKeepMode) {
            CharacterHandler h = game.getHandler(game.playerChar);
            if (h instanceof ChanHandler) ((ChanHandler) h).doChanSevenDiscard();
            return;
        }
        if (game.chanFourSwapMode) {
            CharacterHandler h = game.getHandler(game.playerChar);
            if (h instanceof ChanHandler) ((ChanHandler) h).doChanFourDiscard();
            return;
        }
        game.hasPlayedBlackDefend = false;
        game.selectedSingle = -1;
        game.pendingDefendCard = null;
        game.showDefendDesc("跳过防御");
        game.defenseEngine.finishPlayerDefend();
    }

    void enterPlayerDefend() {
        boolean isBlueAtk = !game.discardPile.isEmpty() && game.discardPile.getFirst().getEffectiveColor() == Card.CardColor.BLUE;
        if (game.playerChar.isFrozen() && isBlueAtk) {
            game.showDefendDesc("冷冻 → 无法防御蓝色攻击");
            Timer frozenTimer = new Timer(Game.DELAY_EFFECT, e -> {
                ((Timer)e.getSource()).stop();
                game.defenseEngine.finishPlayerDefend();
            });
            frozenTimer.start();
        } else {
            game.currentPhase = Game.Phase.PLAYER_DEFEND;
            game.selectedSingle = -1;
            game.busy = false;
            game.updateDisplay();
        }
    }

    /** End AI defense → resolve damage → back to player turn */
    void finishAIDefend() {
        game.aiHasPlayedBlackDefend = false;
        GameCharacter defChar = game.is1v2 ? game.getCurrentAttackTarget() : game.aiChar;
        game.attackEngine.resolvePostDefense(game.playerChar, defChar);
        if (!game.playerChar.isAlive() || (game.is1v2 && !game.isAnyAIAlive()) || (!game.is1v2 && !game.aiChar.isAlive())) return;
        if (game.pendingDefendResult != null && game.pendingDefendResult.endAttackerTurn) {
            game.pendingDefendResult = null;
            game.clearAIZones();
            game.finishPlayerTurn();
            return;
        }
        if (game.pendingDefendResult != null && game.pendingDefendResult.revealTopDeck) {
            GameCharacter.DefenseResult def = game.pendingDefendResult;
            game.pendingDefendResult = null;
            List<Card> defHand = defChar == game.playerChar ? game.playerHand : (defChar == game.aiChar ? game.ai.getHand() : game.ai2.getHand());
            CharacterHandler h = game.getHandler(defChar);
            if (h instanceof SaikiHandler) {
                game.attackEngine.handleSaikiThreeDefendReveal(defChar, game.playerChar, defHand, () -> {
                    game.currentPhase = Game.Phase.PLAYER_PLAY;
                    game.clearAIZones();
                    game.updateDisplay();
                });
            } else {
                game.attackEngine.handleChanThreeDefendReveal(defChar, game.playerChar, defHand, () -> {
                    game.currentPhase = Game.Phase.PLAYER_PLAY;
                    game.clearAIZones();
                    game.updateDisplay();
                });
            }
            return;
        }
        if (game.pendingDefendResult != null && !game.pendingDefendResult.followUps.isEmpty()) {
            GameCharacter.DefenseResult def = game.pendingDefendResult;
            game.pendingDefendResult = null;
            List<Card> defHand = defChar == game.playerChar ? game.playerHand : (defChar == game.aiChar ? game.ai.getHand() : game.ai2.getHand());
            GameCharacter.FollowUp fu = def.followUps.get(0);
            game.effectEngine.executeFollowUp(fu, null, defChar, game.playerChar, defHand, () -> {
                game.currentPhase = Game.Phase.PLAYER_PLAY;
                game.clearAIZones();
                game.updateDisplay();
            });
            return;
        }
        game.pendingDefendResult = null;
        Timer pause = new Timer(Game.DELAY_EFFECT, ev -> {
            ((Timer)ev.getSource()).stop();
            if (!game.playerChar.isAlive() || (game.is1v2 && !game.isAnyAIAlive()) || (!game.is1v2 && !game.aiChar.isAlive())) return;
            game.clearAIZones();
            game.currentPhase = Game.Phase.PLAYER_PLAY;
            game.updateDisplay();
        });
        pause.start();
    }

    /** End player defense → resolve damage → back to AI turn */
    void finishPlayerDefend() {
        game.hasPlayedBlackDefend = false;
        GameCharacter atkChar = game.is1v2 ? game.getCurrentTurnAIChar() : game.aiChar;
        game.attackEngine.resolvePostDefense(atkChar, game.playerChar);
        if (!game.playerChar.isAlive() || (game.is1v2 && !game.isAnyAIAlive()) || (!game.is1v2 && !game.aiChar.isAlive())) return;
        if (game.pendingDefendResult != null && game.pendingDefendResult.endAttackerTurn) {
            game.pendingDefendResult = null;
            game.clearAIZones();
            game.turnEngine.advanceToNextAIOrPlayer();
            return;
        }
        if (game.pendingDefendResult != null && game.pendingDefendResult.revealTopDeck) {
            GameCharacter.DefenseResult def = game.pendingDefendResult;
            game.pendingDefendResult = null;
            List<Card> defHand = game.playerHand;
            CharacterHandler h = game.getHandler(game.playerChar);
            if (h instanceof SaikiHandler) {
                game.attackEngine.handleSaikiThreeDefendReveal(game.playerChar, atkChar, defHand, () -> {
                    game.currentPhase = Game.Phase.AI_TURN;
                    game.clearAIZones();
                    game.updateDisplay();
                    game.turnEngine.resumeAITurn();
                });
            } else {
                game.attackEngine.handleChanThreeDefendReveal(game.playerChar, atkChar, defHand, () -> {
                    game.currentPhase = Game.Phase.AI_TURN;
                    game.clearAIZones();
                    game.updateDisplay();
                    game.turnEngine.resumeAITurn();
                });
            }
            return;
        }
        if (game.pendingDefendResult != null && !game.pendingDefendResult.followUps.isEmpty()) {
            GameCharacter.DefenseResult def = game.pendingDefendResult;
            game.pendingDefendResult = null;
            List<Card> defHand = game.playerHand;
            GameCharacter.FollowUp fu = def.followUps.get(0);
            game.effectEngine.executeFollowUp(fu, null, game.playerChar, atkChar, defHand, () -> {
                game.currentPhase = Game.Phase.AI_TURN;
                game.clearAIZones();
                game.updateDisplay();
                game.turnEngine.resumeAITurn();
            });
            return;
        }
        game.pendingDefendResult = null;

        game.currentPhase = Game.Phase.AI_TURN;
        game.updateDisplay();
        Timer pause = new Timer(Game.DELAY_EFFECT, ev -> {
            ((Timer)ev.getSource()).stop();
            if (!game.playerChar.isAlive() || (game.is1v2 && !game.isAnyAIAlive()) || (!game.is1v2 && !game.aiChar.isAlive())) return;
            game.clearAIZones();
            game.turnEngine.resumeAITurn();
        });
        pause.start();
    }

    void doAIDefendFor(GameCharacter defender) {
        AIPlayer defAI = (defender == game.aiChar2) ? game.ai2 : game.ai;
        GameCharacter atkChar = game.getCurrentTurnAIChar();
        AIPlayer atkAI = game.getCurrentTurnAI();
        Card top = game.discardPile.getFirst();

        if (top.isBlack() && !game.aiHasPlayedBlackDefend) {
            game.showDefendDesc(defender.getName() + " 跳过防御");
            game.attackEngine.resolvePostDefense(atkChar, defender);
            game.clearAIZones();
            game.turnEngine.advanceToNextAIOrPlayer();
            return;
        }

        boolean isBlueAttack = top.getEffectiveColor() == Card.CardColor.BLUE;
        if (defender.isFrozen() && isBlueAttack && !game.aiHasPlayedBlackDefend) {
            game.showDefendDesc(defender.getName() + " 冷冻 → 无法防御蓝色攻击");
            game.attackEngine.resolvePostDefense(atkChar, defender);
            game.clearAIZones();
            game.turnEngine.advanceToNextAIOrPlayer();
            return;
        }

        defAI.aiHasDebuff = defender.hasDebuff();
        defAI.aiDebuffCount = defender.getBurnStacks() + (defender.isFrozen() ? 1 : 0) + defender.getBleedStacks();
        defAI.aiFullHp = defender.getCurrentHp() >= defender.getMaxHp();
        defAI.aiOpponentHandSize = atkAI.getHand().size();
        Card defCard = defAI.chooseDefend(top, game.aiHasPlayedBlackDefend);
        if (defCard != null) {
            if (defCard.isBlack()) {
                Card.CardColor chosen = defAI.chooseBlackColor();
                defCard.setChosenColor(chosen);
            }
            if (defCard.isWhite()) {
                defCard.setChosenColor(top.getEffectiveColor());
            }

            Point from = GameAnim.getAIHandCenter(game.ui, game);
            Point to = GameAnim.getAttackPanelCenter(game.ui, game);

            defAI.removeCard(defCard);
            game.discardPile.addFirst(defCard);
            game.pendingDefendCard = null;

            if (defCard.isBlack()) {
                game.aiHasPlayedBlackDefend = true;
                GameAnim.playFlyAnimation(game, defCard, from, to, () -> {
                    if (defCard.isDrawTwo()) {
                        List<Card> drawn = game.drawFromDeck(2);
                        if (!drawn.isEmpty()) {
                            GameAnim.playDrawAnimations(game, drawn.size(), false, () -> {
                                defAI.addCards(drawn);
                                game.updateDisplay();
                                Timer bt = new Timer(Game.DELAY_STEP, e2 -> {
                                    ((Timer)e2.getSource()).stop();
                                    doAIDefendFor(defender);
                                });
                                bt.start();
                            });
                        } else {
                            Timer bt = new Timer(Game.DELAY_STEP, e2 -> {
                                ((Timer)e2.getSource()).stop();
                                doAIDefendFor(defender);
                            });
                            bt.start();
                        }
                    } else {
                        game.updateDisplay();
                        Timer bt = new Timer(Game.DELAY_STEP, e2 -> {
                            ((Timer)e2.getSource()).stop();
                            doAIDefendFor(defender);
                        });
                        bt.start();
                    }
                });
                return;
            }

            if (defCard.isPotion()) {
                game.aiHasPlayedBlackDefend = true;
                defender.heal(5);
                GameAnim.playFlyAnimation(game, defCard, from, to, () -> {
                    GameAnim.playFloatingText(game, "+5[生命]", new Color(60, 220, 60),
                        new Point(game.getWidth() / 2, game.getHeight() / 3));
                    Timer bt = new Timer(Game.DELAY_STEP, e2 -> {
                        ((Timer)e2.getSource()).stop();
                        doAIDefendFor(defender);
                    });
                    bt.start();
                });
                return;
            }

            if (defCard.isPurify()) {
                game.aiHasPlayedBlackDefend = true;
                GameAnim.playFlyAnimation(game, defCard, from, to, () -> {
                    game.attackEngine.applyPurifyEffect(defender, false);
                    Timer bt = new Timer(Game.DELAY_STEP, e2 -> {
                        ((Timer)e2.getSource()).stop();
                        doAIDefendFor(defender);
                    });
                    bt.start();
                });
                return;
            }

            if (defCard.isSuperPurify()) {
                game.aiHasPlayedBlackDefend = true;
                GameAnim.playFlyAnimation(game, defCard, from, to, () -> {
                    game.attackEngine.applySuperPurifyEffect(defender, false);
                    Timer bt = new Timer(Game.DELAY_STEP, e2 -> {
                        ((Timer)e2.getSource()).stop();
                        doAIDefendFor(defender);
                    });
                    bt.start();
                });
                return;
            }

            if (defCard.isDrawThree()) {
                game.aiHasPlayedBlackDefend = true;
                List<Card> drawn = game.drawFromDeck(3);
                GameAnim.playFlyAnimation(game, defCard, from, to, () -> {
                    if (!drawn.isEmpty()) {
                        GameAnim.playDrawAnimations(game, drawn.size(), false, () -> {
                            defAI.addCards(drawn);
                            game.updateDisplay();
                            Timer bt = new Timer(Game.DELAY_STEP, e2 -> {
                                ((Timer)e2.getSource()).stop();
                                doAIDefendFor(defender);
                            });
                            bt.start();
                        });
                    } else {
                        Timer bt = new Timer(Game.DELAY_STEP, e2 -> {
                            ((Timer)e2.getSource()).stop();
                            doAIDefendFor(defender);
                        });
                        bt.start();
                    }
                });
                return;
            }

            if (defCard.isSwapHand()) {
                game.aiHasPlayedBlackDefend = true;
                List<Card> atkHand = atkAI.getHand();
                List<Card> tmp = new ArrayList<>(atkHand);
                atkHand.clear();
                atkHand.addAll(defAI.getHand());
                defAI.getHand().clear();
                defAI.getHand().addAll(tmp);
                GameAnim.playFlyAnimation(game, defCard, from, to, () -> {
                    GameAnim.playFloatingText(game, "[交换]", new Color(100, 80, 200),
                        new Point(game.getWidth() / 2, game.getHeight() / 2));
                    game.updateDisplay();
                    Timer bt = new Timer(Game.DELAY_STEP, e2 -> {
                        ((Timer)e2.getSource()).stop();
                        doAIDefendFor(defender);
                    });
                    bt.start();
                });
                return;
            }

            if (defCard.isShuffleToDeck()) {
                game.aiHasPlayedBlackDefend = true;
                GameAnim.playFlyAnimation(game, defCard, from, to, () -> {
                    Card topCard = game.discardPile.isEmpty() ? null : game.discardPile.removeFirst();
                    List<Card> toShuffle = new ArrayList<>(game.discardPile);
                    for (Card c : toShuffle) {
                        if (c.isBlack() || c.isWhite()) c.setChosenColor(null);
                    }
                    game.discardPile.clear();
                    if (topCard != null) game.discardPile.addFirst(topCard);
                    game.deck.addCards(toShuffle);
                    game.deck.shuffle();
                    GameAnim.playFloatingText(game, "[洗入]" + toShuffle.size() + "张", new Color(100, 80, 200),
                        new Point(game.getWidth() / 2, game.getHeight() / 2 - 30));
                    game.updateDisplay();
                    Timer bt = new Timer(Game.DELAY_STEP, e2 -> {
                        ((Timer)e2.getSource()).stop();
                        doAIDefendFor(defender);
                    });
                    bt.start();
                });
                return;
            }

            game.pendingDefendCard = defCard;
            game.aiHasPlayedBlackDefend = false;
            GameAnim.playFlyAnimation(game, defCard, from, to, () -> {
                game.showDefendCard(defCard);
                game.attackEngine.resolvePostDefense(atkChar, defender);
                game.clearAIZones();
                game.turnEngine.advanceToNextAIOrPlayer();
            });
        } else {
            game.pendingDefendCard = null;
            game.aiHasPlayedBlackDefend = false;
            game.showDefendDesc(defender.getName() + " 跳过防御");
            Timer delay = new Timer(Game.DELAY_STEP, e2 -> {
                ((Timer)e2.getSource()).stop();
                game.attackEngine.resolvePostDefense(atkChar, defender);
                game.clearAIZones();
                game.turnEngine.advanceToNextAIOrPlayer();
            });
            delay.start();
        }
    }
}