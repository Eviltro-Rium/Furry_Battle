import javax.swing.*;
import java.awt.*;
import java.util.List;

public class LeonHandler extends CharacterHandler {

    LeonHandler(Game game) {
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
        game.showAttackDesc("7 弃掉AI的" + chosen);
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

                self.heal(revealResult.selfHeal);
                if (revealResult.selfHeal > 0) {
                    Point loc = self == game.playerChar
                        ? new Point(game.getWidth() / 2, game.getHeight() * 3 / 4)
                        : new Point(game.getWidth() / 2, game.getHeight() / 3);
                    GameAnim.playFloatingText(game, "+" + revealResult.selfHeal, new Color(60, 220, 60), loc);
                }

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

                if (revealResult.revealExtraDraw > 0) {
                    List<Card> extraDrawn = game.drawFromDeck(revealResult.revealExtraDraw);
                    if (!extraDrawn.isEmpty()) {
                        selfHand.addAll(extraDrawn);
                        GameAnim.playDrawAnimations(game, extraDrawn.size(), self == game.playerChar, () -> game.updateDisplay());
                    }
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
}
