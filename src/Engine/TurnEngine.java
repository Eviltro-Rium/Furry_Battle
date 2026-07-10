import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TurnEngine {
    Game game;

    TurnEngine(Game game) { this.game = game; }

    int aiFillTarget() {
        if (!game.is1v2) return 5;
        boolean ai1Dead = !game.aiChar.isAlive();
        boolean ai2Dead = game.aiChar2 == null || !game.aiChar2.isAlive();
        return (ai1Dead || ai2Dead) ? 8 : 5;
    }

    void startAITurn() {
        game.currentPhase = Game.Phase.AI_TURN;
        game.aiHasPlayed = false;

        if (game.is1v2) {
            GameCharacter curAI = game.getCurrentTurnAIChar();
            if (!curAI.isAlive()) {
                advanceToNextAIOrPlayer();
                return;
            }
        }

        game.updateDisplay();
        game.aiTimer = new Timer(Game.DELAY_STEP, e -> executeAIStep());
        game.aiTimer.start();
    }

    void resumeAITurn() {
        if (!game.playerChar.isAlive() || (game.is1v2 && !game.isAnyAIAlive()) || (!game.is1v2 && !game.aiChar.isAlive())) return;
        game.aiTimer = new Timer(Game.DELAY_STEP, e -> executeAIStep());
        game.aiTimer.start();
    }

    void advanceToNextAIOrPlayer() {
        if (!game.is1v2) {
            finishAITurn();
            return;
        }
        if (game.currentTurnTarget == 0 && game.aiChar2 != null && game.aiChar2.isAlive()) {
            game.currentTurnTarget = 1;
            game.attackEngine.applyBurnDamage(game.aiChar);
            if (game.attackEngine.checkDeath()) return;
            List<Card> a1Cards = drawFromDeck(needToFill(game.ai.getHand(), aiFillTarget()));
            List<Card> a2Cards = drawFromDeck(needToFill(game.ai2.getHand(), aiFillTarget()));
            int total = a1Cards.size() + a2Cards.size();
            game.aiChar2.applyPassive();
            if (total > 0) {
                game.updateDisplay();
                GameAnim.playDrawAnimations(game, total, false, () -> {
                    game.ai.getHand().addAll(a1Cards);
                    game.ai2.getHand().addAll(a2Cards);
                    game.updateDisplay();
                    startAITurn();
                });
            } else {
                startAITurn();
            }
        } else {
            if (game.currentTurnTarget == 0) {
                game.attackEngine.applyBurnDamage(game.aiChar);
            } else {
                game.attackEngine.applyBurnDamage(game.aiChar2);
            }
            if (game.attackEngine.checkDeath()) return;
            finishAITurn();
        }
    }

    void executeAIStep() {
        GameCharacter curAIChar = game.getCurrentTurnAIChar();
        AIPlayer curAI = game.getCurrentTurnAI();
        if (!game.playerChar.isAlive() || !curAIChar.isAlive()) return;
        if (game.discardPile.isEmpty()) {
            game.aiTimer.stop();
            advanceToNextAIOrPlayer();
            return;
        }

        Card top = game.discardPile.getFirst();

        if (curAI.hasPlayableCard(top)) {
            curAI.aiDebuffCount = curAIChar.getBurnStacks() + (curAIChar.isFrozen() ? 1 : 0) + curAIChar.getBleedStacks();
            curAI.aiFullHp = curAIChar.getCurrentHp() >= curAIChar.getMaxHp();
            curAI.aiGuardStacks = curAIChar.getGuardStacks();
            curAI.aiBurnStacks = curAIChar.getBurnStacks();
            curAI.aiHpPercent = curAIChar.getMaxHp() > 0 ? curAIChar.getCurrentHp() * 100 / curAIChar.getMaxHp() : 100;
            Card toPlay = curAI.choosePlay(top, curAIChar.hasDebuff());
            if (toPlay != null) {
                if (toPlay.isBlack()) {
                    Card.CardColor chosen = curAI.chooseBlackColor();
                    toPlay.setChosenColor(chosen);
                }
                if (toPlay.isWhite()) {
                    toPlay.setChosenColor(top.getEffectiveColor());
                }

                final GameCharacter aiTarget;
                if (game.is1v2) {
                    Participant attackerP = game.getParticipantFor(curAIChar);
                    Participant targetP = game.gameMode.chooseAttackTarget(game, attackerP, game.participants);
                    aiTarget = targetP != null ? targetP.character : game.playerChar;
                    game.currentAttackTarget = targetP;
                } else {
                    aiTarget = game.playerChar;
                    game.currentAttackTarget = game.getAI1();
                }

                curAI.aiOpponentHandSize = game.getHandFor(aiTarget).size();
                curAI.aiOpponentGuardStacks = aiTarget.getGuardStacks();
                curAI.aiOpponentBurnStacks = aiTarget.getBurnStacks();
                curAI.aiOpponentBleedStacks = aiTarget.getBleedStacks();
                curAI.aiOpponentHpPercent = aiTarget.getMaxHp() > 0 ? aiTarget.getCurrentHp() * 100 / aiTarget.getMaxHp() : 100;

                curAI.removeCard(toPlay);
                game.discardPile.addFirst(toPlay);
                game.aiHasPlayed = true;
                game.aiTimer.stop();


                if (toPlay.isPotion()) {
                    Point from = GameAnim.getAIHandCenter(game.ui, game);
                    Point to = GameAnim.getAttackPanelCenter(game.ui, game);
                    curAIChar.heal(5);
                    GameAnim.playFlyAnimation(game, toPlay, from, to, () -> {
                        game.showAIAttackCard(toPlay);
                        game.showAttackDesc("药水 " + curAIChar.getName() + "恢复5点生命，继续出牌");
                        GameAnim.playFloatingText(game, "+5[生命]", new Color(60, 220, 60),
                            new Point(game.getWidth() / 2, game.getHeight() / 3));
                        game.updateDisplay();
                        resumeAITurn();
                    });
                } else if (toPlay.isPurify()) {
                    Point from = GameAnim.getAIHandCenter(game.ui, game);
                    Point to = GameAnim.getAttackPanelCenter(game.ui, game);
                    GameAnim.playFlyAnimation(game, toPlay, from, to, () -> {
                        game.showAIAttackCard(toPlay);
                        game.attackEngine.applyPurifyEffect(curAIChar, false);
                        game.updateDisplay();
                        resumeAITurn();
                    });
                } else if (toPlay.isSuperPurify()) {
                    Point from = GameAnim.getAIHandCenter(game.ui, game);
                    Point to = GameAnim.getAttackPanelCenter(game.ui, game);
                    GameAnim.playFlyAnimation(game, toPlay, from, to, () -> {
                        game.showAIAttackCard(toPlay);
                        game.attackEngine.applySuperPurifyEffect(curAIChar, false);
                        game.updateDisplay();
                        resumeAITurn();
                    });
                } else if (toPlay.isSwapHand()) {
                    Point from = GameAnim.getAIHandCenter(game.ui, game);
                    Point to = GameAnim.getAttackPanelCenter(game.ui, game);
                    GameAnim.playFlyAnimation(game, toPlay, from, to, () -> {
                        game.showAIAttackCard(toPlay);
                        List<Card> targetHand = aiTarget == game.playerChar ? game.playerHand : (aiTarget == game.aiChar ? game.ai.getHand() : game.ai2.getHand());
                        List<Card> selfHand = curAI.getHand();
                        List<Card> temp = new ArrayList<>(selfHand);
                        selfHand.clear();
                        selfHand.addAll(targetHand);
                        targetHand.clear();
                        targetHand.addAll(temp);
                        game.showAttackDesc("交换 " + curAIChar.getName() + "交换手牌，继续出牌");
                        GameAnim.playFloatingText(game, "[交换]", new Color(100, 80, 200),
                            new Point(game.getWidth() / 2, game.getHeight() / 2 - 30));
                        game.updateDisplay();
                        resumeAITurn();
                    });
                } else if (toPlay.isDrawThree()) {
                    Point from = GameAnim.getAIHandCenter(game.ui, game);
                    Point to = GameAnim.getAttackPanelCenter(game.ui, game);
                    GameAnim.playFlyAnimation(game, toPlay, from, to, () -> {
                        game.showAIAttackCard(toPlay);
                        game.showAttackDesc("+3 " + curAIChar.getName() + "抽3张牌，继续出牌");
                        List<Card> drawn = drawFromDeck(3);
                        if (!drawn.isEmpty()) {
                            game.updateDisplay();
                            GameAnim.playDrawAnimations(game, drawn.size(), false, () -> {
                                curAI.addCards(drawn);
                                game.attackEngine.checkHandLimit(curAI.getHand(), false);
                                game.updateDisplay();
                                resumeAITurn();
                            });
                        } else {
                            game.updateDisplay();
                            resumeAITurn();
                        }
                    });
                } else if (toPlay.isBlack()) {
                    Point from = GameAnim.getAIHandCenter(game.ui, game);
                    Point to = GameAnim.getAttackPanelCenter(game.ui, game);
                    GameAnim.playFlyAnimation(game, toPlay, from, to, () -> {
                        game.showAIAttackCard(toPlay);
                        Runnable afterEffect = () -> {
                            game.updateDisplay();
                            resumeAITurn();
                        };
                        if (toPlay.isShuffleToDeck()) {
                            Card topCard = game.discardPile.isEmpty() ? null : game.discardPile.removeFirst();
                            List<Card> toShuffle = new ArrayList<>(game.discardPile);
                            for (Card c : toShuffle) {
                                if (c.isBlack() || c.isWhite()) c.setChosenColor(null);
                            }
                            game.discardPile.clear();
                            if (topCard != null) game.discardPile.addFirst(topCard);
                            game.deck.addCards(toShuffle);
                            game.deck.shuffle();
                            game.showAttackDesc("洗入牌库 " + curAIChar.getName() + "将弃牌库洗入牌库，继续出牌");
                            GameAnim.playFloatingText(game, "[洗入]" + toShuffle.size() + "张", new Color(100, 80, 200),
                                new Point(game.getWidth() / 2, game.getHeight() / 2 - 30));
                            afterEffect.run();
                            return;
                        }
                        if (toPlay.isDrawTwo()) {
                            List<Card> drawn = drawFromDeck(2);
                            if (!drawn.isEmpty()) {
                                game.updateDisplay();
                                GameAnim.playDrawAnimations(game, drawn.size(), false, () -> {
                                    curAI.addCards(drawn);
                                    game.attackEngine.applyAttackEffect(toPlay, curAIChar, aiTarget, curAI.getHand(), afterEffect);
                                });
                                return;
                            }
                        }
                        game.attackEngine.applyAttackEffect(toPlay, curAIChar, aiTarget, curAI.getHand(), afterEffect);
                    });
                } else {

                    Point from = GameAnim.getAIHandCenter(game.ui, game);
                    Point to = GameAnim.getAttackPanelCenter(game.ui, game);
                    GameAnim.playFlyAnimation(game, toPlay, from, to, () -> {
                        game.showAIAttackCard(toPlay);
                        game.attackEngine.applyAttackEffect(toPlay, curAIChar, aiTarget, curAI.getHand(), () -> {
                            if (game.pendingAttack != null && game.pendingAttack.skipDefense) {
                                game.showDefendDesc("跳过防御");
                                Timer skipTimer = new Timer(Game.DELAY_SKIP, ev -> {
                                    ((Timer)ev.getSource()).stop();
                                    game.attackEngine.resolvePostDefense(curAIChar, aiTarget);
                                    game.clearAIZones();
                                    game.attackEngine.checkHandLimit(curAI.getHand(), false);
                                    game.updateDisplay();
                                    advanceToNextAIOrPlayer();
                                });
                                skipTimer.start();
                            } else if (game.pendingAttack != null) {
                                if (aiTarget == game.playerChar) {
                                    game.defenseEngine.enterPlayerDefend();
                                } else {
                                    game.defenseEngine.doAIDefendFor(aiTarget);
                                }
                            } else {
                                game.attackEngine.checkHandLimit(curAI.getHand(), false);
                                advanceToNextAIOrPlayer();
                            }
                        });
                    });
                }
                return;
            }
        }

        game.aiTimer.stop();

        if (!game.aiHasPlayed) {
            List<Card> discards = curAI.chooseDiscards();
            for (Card c : discards) {
                game.discardPile.addLast(c);
            }
            curAI.clear();
        }

        advanceToNextAIOrPlayer();
    }

    void refillDeckIfNeeded() {
        if (!game.deck.isEmpty() || game.discardPile.isEmpty()) return;
        Card topCard = game.discardPile.removeFirst();
        for (Card c : game.discardPile) {
            if (c.isBlack() || c.isWhite()) {
                c.setChosenColor(null);
            }
        }
        game.deck.addCards(game.discardPile);
        game.discardPile.clear();
        game.discardPile.addFirst(topCard);
        game.deck.shuffle();
    }

    List<Card> drawFromDeck(int count) {
        List<Card> result = new ArrayList<>();
        while (result.size() < count) {
            refillDeckIfNeeded();
            List<Card> drawn = game.deck.draw(count - result.size());
            if (drawn.isEmpty()) break;
            result.addAll(drawn);
        }
        return result;
    }

    int needToFill(List<Card> hand, int targetSize) {
        return Math.max(0, targetSize - hand.size());
    }

    void finishPlayerTurn() {
        if (game.is1v2) {
            List<Card> pCards = drawFromDeck(needToFill(game.playerHand, game.maxPlayerHand));
            List<Card> aCards = drawFromDeck(needToFill(game.ai.getHand(), aiFillTarget()));
            List<Card> a2Cards = drawFromDeck(needToFill(game.ai2.getHand(), aiFillTarget()));
            int total = pCards.size() + aCards.size() + a2Cards.size();
            if (total > 0) {
                game.updateDisplay();
                GameAnim.playDrawAnimations(game, total, true, () -> {
                    game.playerHand.addAll(pCards);
                    game.ai.getHand().addAll(aCards);
                    game.ai2.getHand().addAll(a2Cards);
                    game.updateDisplay();
                    proceedAfterTurnEnd();
                });
            } else {
                proceedAfterTurnEnd();
            }
        } else {
            List<Card> pCards = drawFromDeck(needToFill(game.playerHand, 5));
            List<Card> aCards = drawFromDeck(needToFill(game.ai.getHand(), 5));
            int total = pCards.size() + aCards.size();
            if (total > 0) {
                game.updateDisplay();
                GameAnim.playDrawAnimations(game, total, true, () -> {
                    game.playerHand.addAll(pCards);
                    game.ai.getHand().addAll(aCards);
                    game.updateDisplay();
                    proceedAfterTurnEnd();
                });
            } else {
                proceedAfterTurnEnd();
            }
        }
    }

    void proceedAfterTurnEnd() {
        game.hasPlayedThisTurn = false;
        game.selectedSingle = -1;
        game.selectedMulti.clear();

        game.attackEngine.applyBurnDamage(game.playerChar);
        if (game.attackEngine.checkDeath()) return;

        if (game.is1v2) {
            game.aiChar.applyPassive();
            if (game.aiChar2 != null) game.aiChar2.applyPassive();
            game.currentTurnTarget = 0;
            if (!game.aiChar.isAlive() && game.aiChar2 != null && game.aiChar2.isAlive()) {
                game.currentTurnTarget = 1;
            }
        } else {
            game.aiChar.applyPassive();
        }
        game.updateDisplay();
        startAITurn();
    }

    void finishAITurn() {
        AIPlayer curAI = game.getCurrentTurnAI();
        GameCharacter curAIChar = game.getCurrentTurnAIChar();
        int aiLimit = aiFillTarget();
        while (curAI.getHand().size() > aiLimit) {
            Card worst = null;
            for (Card c : curAI.getHand()) {
                if (worst == null || c.getValue() < worst.getValue()) worst = c;
            }
            if (worst != null) {
                curAI.getHand().remove(worst);
                game.discardPile.addLast(worst);
            }
        }

        game.turnCount++;
        game.hasPlayedThisTurn = false;
        game.selectedSingle = -1;
        game.selectedMulti.clear();
        game.playerChar.applyPassive();
        if (game.playerChar instanceof ChanCharacter) {
            List<Card> chanDraw = drawFromDeck(1);
            if (!chanDraw.isEmpty()) {
                game.playerHand.addAll(chanDraw);
            }
        }
        game.attackEngine.applyBurnDamage(curAIChar);
        if (game.attackEngine.checkDeath()) return;
        game.updateDisplay();

        if (game.is1v2) {
            List<Card> pCards = drawFromDeck(needToFill(game.playerHand, game.maxPlayerHand));
            List<Card> a1Cards = drawFromDeck(needToFill(game.ai.getHand(), aiFillTarget()));
            List<Card> a2Cards = drawFromDeck(needToFill(game.ai2.getHand(), aiFillTarget()));
            int total = pCards.size() + a1Cards.size() + a2Cards.size();
            if (total > 0) {
                GameAnim.playDrawAnimations(game, total, false, () -> {
                    game.playerHand.addAll(pCards);
                    game.ai.getHand().addAll(a1Cards);
                    game.ai2.getHand().addAll(a2Cards);
                    game.currentPhase = Game.Phase.PLAYER_PLAY;
                    game.updateDisplay();
                });
            } else {
                game.currentPhase = Game.Phase.PLAYER_PLAY;
                game.updateDisplay();
            }
        } else {
            List<Card> pCards = drawFromDeck(needToFill(game.playerHand, 5));
            List<Card> a1Cards = drawFromDeck(needToFill(game.ai.getHand(), 5));
            int total = pCards.size() + a1Cards.size();
            if (total > 0) {
                GameAnim.playDrawAnimations(game, total, false, () -> {
                    game.playerHand.addAll(pCards);
                    game.ai.getHand().addAll(a1Cards);
                    game.currentPhase = Game.Phase.PLAYER_PLAY;
                    game.updateDisplay();
                });
            } else {
                game.currentPhase = Game.Phase.PLAYER_PLAY;
                game.updateDisplay();
            }
        }
    }

    void doPlay() {
        if (game.busy) return;
        game.busy = true;
        if (game.currentPhase != Game.Phase.PLAYER_PLAY) { game.busy = false; return; }
        if (game.selectedSingle < 0 || game.selectedSingle >= game.playerHand.size()) {
            game.busy = false;
            game.showMessage("请先点击选择一张牌！");
            return;
        }
        if (game.discardPile.isEmpty()) { game.busy = false; return; }

        Card card = game.playerHand.get(game.selectedSingle);
        Card top = game.discardPile.getFirst();
        if (!game.canPlayOn(card, top)) {
            game.busy = false;
            game.showMessage(card + " 无法匹配弃牌库顶 " + top + "\n(需颜色或数字相同)");
            return;
        }

        if (card.isBlack()) {
            Card.CardColor chosen = GameUI.showColorChooser(game);
            if (chosen == null) { game.busy = false; return; }
            card.setChosenColor(chosen);
        }

        if (card.isWhite()) {
            card.setChosenColor(top.getEffectiveColor());
        }

        int cardIdx = game.selectedSingle;
        Point from = GameAnim.getPlayerHandCardCenter(game.ui, game, cardIdx);
        Point to = GameAnim.getAttackPanelCenter(game.ui, game);

        if (game.is1v2 && (!card.isItemCard() || card.isSwapHand()) && !card.isBlack()) {
            final int fi = cardIdx;
            final Point ff = from, ft = to;
            game.enterTargetChoice(() -> doPlayContinue(card, fi, ff, ft));
            return;
        }

        doPlayContinue(card, cardIdx, from, to);
    }

    void doPlayContinue(Card card, int cardIdx, Point from, Point to) {

        game.playerHand.remove(cardIdx);
        game.discardPile.addFirst(card);
        game.hasPlayedThisTurn = true;
        game.selectedSingle = -1;
        game.updateDisplay();


        if (card.isPotion()) {
             game.playerChar.heal(5);
             GameAnim.playFlyAnimation(game, card, from, to, () -> {
                 game.showAIAttackCard(card);
                 game.showAttackDesc("药水 恢复5点生命，可继续出牌");
                 GameAnim.playFloatingText(game, "+5[生命]", new Color(60, 220, 60),
                     new Point(game.getWidth() / 2, game.getHeight() * 3 / 4));
                game.currentPhase = Game.Phase.PLAYER_PLAY;
                game.updateDisplay();
            });
            return;
        }

        if (card.isPurify()) {
            GameAnim.playFlyAnimation(game, card, from, to, () -> {
                game.showAIAttackCard(card);
                game.attackEngine.applyPurifyEffect(game.playerChar, true);
                game.currentPhase = Game.Phase.PLAYER_PLAY;
                game.updateDisplay();
            });
            return;
        }

        if (card.isSuperPurify()) {
            GameAnim.playFlyAnimation(game, card, from, to, () -> {
                game.showAIAttackCard(card);
                game.attackEngine.applySuperPurifyEffect(game.playerChar, true);
                game.currentPhase = Game.Phase.PLAYER_PLAY;
                game.updateDisplay();
            });
            return;
        }

        if (card.isSwapHand()) {
            GameAnim.playFlyAnimation(game, card, from, to, () -> {
                game.showAIAttackCard(card);
                GameCharacter targetAI = game.getCurrentAttackTarget();
                List<Card> targetHand = game.getTargetAIHand();
                List<Card> temp = new ArrayList<>(game.playerHand);
                game.playerHand.clear();
                game.playerHand.addAll(targetHand);
                targetHand.clear();
                targetHand.addAll(temp);
                game.showAttackDesc("交换 交换双方手牌，可继续出牌");
                GameAnim.playFloatingText(game, "[交换]", new Color(100, 80, 200),
                    new Point(game.getWidth() / 2, game.getHeight() / 2 - 30));
                game.currentPhase = Game.Phase.PLAYER_PLAY;
                game.updateDisplay();
            });
            return;
        }

        if (card.isDrawThree()) {
            GameAnim.playFlyAnimation(game, card, from, to, () -> {
                game.showAIAttackCard(card);
                game.showAttackDesc("+3 抽3张牌，可继续出牌");
                List<Card> drawn = drawFromDeck(3);
                if (!drawn.isEmpty()) {
                    game.updateDisplay();
                    GameAnim.playDrawAnimations(game, drawn.size(), true, () -> {
                        game.playerHand.addAll(drawn);
                        game.currentPhase = Game.Phase.PLAYER_PLAY;

                        game.updateDisplay();
                    });
                } else {
                    game.currentPhase = Game.Phase.PLAYER_PLAY;
                    game.updateDisplay();
                }
            });
            return;
        }

        if (card.isBlack()) {
            GameAnim.playFlyAnimation(game, card, from, to, () -> {
                game.showAIAttackCard(card);
                Runnable afterEffect = () -> {
                    game.currentPhase = Game.Phase.PLAYER_PLAY;
                    game.updateDisplay();
                };
                if (card.isShuffleToDeck()) {
                    Card topCard = game.discardPile.isEmpty() ? null : game.discardPile.removeFirst();
                    List<Card> toShuffle = new ArrayList<>(game.discardPile);
                    for (Card c : toShuffle) {
                        if (c.isBlack() || c.isWhite()) c.setChosenColor(null);
                    }
                    game.discardPile.clear();
                    if (topCard != null) game.discardPile.addFirst(topCard);
                    game.deck.addCards(toShuffle);
                    game.deck.shuffle();
                    game.showAttackDesc("洗入牌库 弃牌库洗入牌库，可继续出牌");
                    GameAnim.playFloatingText(game, "[洗入]" + toShuffle.size() + "张", new Color(100, 80, 200),
                        new Point(game.getWidth() / 2, game.getHeight() / 2 - 30));
                    afterEffect.run();
                    return;
                }
                if (card.isDrawTwo()) {
                    List<Card> drawn = drawFromDeck(2);
                    if (!drawn.isEmpty()) {
                        game.updateDisplay();
                        GameAnim.playDrawAnimations(game, drawn.size(), true, () -> {
                            game.playerHand.addAll(drawn);
                            game.attackEngine.applyAttackEffect(card, game.playerChar, game.getCurrentAttackTarget(), game.playerHand, afterEffect);
                        });
                        return;
                    }
                }
                game.attackEngine.applyAttackEffect(card, game.playerChar, game.getCurrentAttackTarget(), game.playerHand, afterEffect);
            });
        } else {
            GameAnim.playFlyAnimation(game, card, from, to, () -> {
                game.showAIAttackCard(card);
                GameCharacter atkTarget = game.getCurrentAttackTarget();
                game.attackEngine.applyAttackEffect(card, game.playerChar, atkTarget, game.playerHand, () -> {
                    AIPlayer targetAI = game.getCurrentTargetAI();
                    if (game.forceOpponentDiscardOne && !targetAI.getHand().isEmpty()) {
                        game.forceOpponentDiscardOne = false;
                        game.pendingFiveChoice = true;
                        game.currentPhase = Game.Phase.PLAYER_SEVEN_CHOICE;
                        game.updateDisplay();
                    } else if (game.pendingAttack != null && game.pendingAttack.skipDefense) {
                        game.forceOpponentDiscardOne = false;
                        game.showDefendDesc("跳过防御");
                        Timer skipTimer = new Timer(Game.DELAY_SKIP, ev -> {
                            ((Timer)ev.getSource()).stop();
                            game.attackEngine.resolvePostDefense(game.playerChar, atkTarget);
                            game.clearAIZones();
                            game.currentPhase = Game.Phase.PLAYER_PLAY;
                            game.updateDisplay();
                        });
                        skipTimer.start();
                    } else if (game.pendingAttack != null) {
                        game.forceOpponentDiscardOne = false;
                        game.currentPhase = Game.Phase.AI_DEFEND;
                        game.updateDisplay();
                        game.aiTimer = new Timer(Game.DELAY_PLAY, e -> {
                            game.aiTimer.stop();
                            game.defenseEngine.doAIDefend();
                        });
                        game.aiTimer.start();
                    } else {
                        game.forceOpponentDiscardOne = false;
                        game.currentPhase = Game.Phase.PLAYER_PLAY;
                        game.updateDisplay();
                    }
                });
            });
        }
    }

    void doEndTurn() {
        if (game.busy) return;
        if (game.currentPhase != Game.Phase.PLAYER_PLAY) return;
        if (game.playerHand.size() > game.maxPlayerHand) {
            game.forcedDiscard = true;
            game.hasPlayedThisTurn = false;
            game.selectedSingle = -1;
            game.selectedMulti.clear();
            game.currentPhase = Game.Phase.PLAYER_DISCARD;
            game.updateDisplay();
            game.showMessage("手牌超过" + game.maxPlayerHand + "张，请弃牌至不超过" + game.maxPlayerHand + "张！");
            return;
        }
        game.busy = true;
        game.currentPhase = Game.Phase.AI_TURN;
        finishPlayerTurn();
    }

    void doEnterDiscard() {
        if (game.busy) return;
        if (game.hasPlayedThisTurn) {
            game.showMessage("本回合已出牌，不能再弃牌！");
            return;
        }
        game.selectedSingle = -1;
        game.selectedMulti.clear();
        game.currentPhase = Game.Phase.PLAYER_DISCARD;
        game.updateDisplay();
    }

    void doConfirmDiscard() {
        if (game.busy) return;
        if (game.selectedMulti.isEmpty()) {
            game.showMessage("请选择要弃掉的牌！");
            return;
        }
        List<Integer> sorted = new ArrayList<>(game.selectedMulti);
        sorted.sort((a, b) -> b - a);
        for (int idx : sorted) {
            if (idx < game.playerHand.size()) {
                game.discardPile.addLast(game.playerHand.remove(idx));
            }
        }
        game.selectedMulti.clear();

        if (game.forcedDiscard) {
            if (game.playerHand.size() > game.maxPlayerHand) {
                game.currentPhase = Game.Phase.PLAYER_DISCARD;
                game.updateDisplay();
                game.showMessage("仍需弃牌！手牌必须 ≤ 5 张");
                return;
            }
            game.forcedDiscard = false;
            proceedAfterTurnEnd();
            return;
        }

        game.currentPhase = Game.Phase.AI_TURN;
        finishPlayerTurn();
    }

    void doCancelDiscard() {
        if (game.forcedDiscard) {
            game.showMessage("手牌超过" + game.maxPlayerHand + "张，不能取消弃牌！");
            return;
        }
        game.selectedMulti.clear();
        game.currentPhase = Game.Phase.PLAYER_PLAY;
        game.updateDisplay();
    }
}