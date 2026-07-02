import javax.swing.*;
import java.awt.*;
import java.util.List;

public class LeonHandler extends CharacterHandler {

    LeonHandler(Game game) {
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
            if (self != game.playerChar) game.resumeAITurn();
            return;
        }
        Card revealed = game.deck.draw();
        GameCharacter.AttackResult revealResult = self.resolveReveal(revealed, true);

        Point from = GameAnim.getDeckCenter(game.ui, game);
        Point to = GameAnim.getRevealPanelCenter(game.ui, game);
        GameAnim.playFlyAnimation(game, revealed, from, to, () -> {
            game.showAIRevealCard(revealed);

            Timer revealTimer = new Timer(1500, e -> {
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
                    GameAnim.playFloatingText(game, "🔥+" + revealResult.addBurn, new Color(255, 140, 0),
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
                        game.aiTimer = new Timer(800, e2 -> {
                            game.aiTimer.stop();
                            game.doAIDefend();
                        });
                        game.aiTimer.start();
                    } else {
                        game.currentPhase = Game.Phase.PLAYER_DEFEND;
                        game.selectedSingle = -1;
                        game.updateDisplay();
                    }
                } else if (revealResult.damage > 0 && revealResult.skipDefense) {
                    game.pendingAttack = revealResult;
                    if (self == game.playerChar) {
                        game.currentPhase = Game.Phase.AI_DEFEND;
                        game.updateDisplay();
                        game.showDefendDesc("跳过防御");
                        Timer skipTimer = new Timer(1500, e2 -> {
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
                        Timer skipTimer = new Timer(1500, e2 -> {
                            ((Timer)e2.getSource()).stop();
                            game.resolvePostDefense(game.aiChar, game.playerChar);
                            game.clearAIZones();
                            game.updateDisplay();
                            game.resumeAITurn();
                        });
                        skipTimer.start();
                    }
                } else {
                    game.pendingAttack = null;
                    game.currentPhase = self == game.playerChar ? Game.Phase.PLAYER_PLAY : Game.Phase.AI_TURN;
                    game.clearAIZones();
                    game.updateDisplay();
                    if (self != game.playerChar) {
                        Timer delay = new Timer(800, e2 -> {
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