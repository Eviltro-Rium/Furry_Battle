import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class EffectEngine {

    private Game game;

    EffectEngine(Game game) {
        this.game = game;
    }

    void applyImmediateEffects(GameCharacter.AttackResult ar, GameCharacter self, GameCharacter opponent) {
        self.heal(ar.selfHeal);
        if (ar.selfDamage > 0 && !self.isImmuneToBurn()) {
            self.takeDamage(ar.selfDamage);
            if (!self.isAlive()) return;
        }

        if (ar.addBurn > 0) {
            opponent.addBurn(ar.addBurn);
            GameAnim.playFloatingText(game, "🔥+" + ar.addBurn, new Color(255, 140, 0),
                opponent == game.getPlayerChar()
                    ? new Point(game.getWidth() / 2, game.getHeight() * 3 / 4 - 60)
                    : new Point(game.getWidth() / 2, game.getHeight() / 3 - 60));
        }

        if (ar.addFreeze) {
            opponent.setFrozen(true);
            GameAnim.playFloatingText(game, "❄️冷冻", new Color(100, 180, 255),
                opponent == game.getPlayerChar()
                    ? new Point(game.getWidth() / 2, game.getHeight() * 3 / 4 - 60)
                    : new Point(game.getWidth() / 2, game.getHeight() / 3 - 60));
        }

        if (ar.addBleed > 0) {
            opponent.addBleed(ar.addBleed);
            GameAnim.playFloatingText(game, "🩸+" + ar.addBleed, new Color(180, 0, 0),
                opponent == game.getPlayerChar()
                    ? new Point(game.getWidth() / 2, game.getHeight() * 3 / 4 - 90)
                    : new Point(game.getWidth() / 2, game.getHeight() / 3 - 90));
        }

        if (ar.passiveBleed > 0) {
            opponent.addBleed(ar.passiveBleed);
            GameAnim.playFloatingText(game, "🩸+" + ar.passiveBleed + "(🟡)", new Color(180, 0, 0),
                opponent == game.getPlayerChar()
                    ? new Point(game.getWidth() / 2 - 60, game.getHeight() * 3 / 4 - 120)
                    : new Point(game.getWidth() / 2 - 60, game.getHeight() / 3 - 120));
        }

        if (ar.addBleedSelf > 0) {
            self.addBleed(ar.addBleedSelf);
            GameAnim.playFloatingText(game, "🩸+" + ar.addBleedSelf, new Color(180, 0, 0),
                self == game.getPlayerChar()
                    ? new Point(game.getWidth() / 2, game.getHeight() * 3 / 4 - 90)
                    : new Point(game.getWidth() / 2, game.getHeight() / 3 - 90));
        }

        if (ar.skipDefenseIfBurn && opponent.getBurnStacks() > 0) {
            ar.skipDefense = true;
        }

        if (ar.skipDefenseIfBleed && opponent.getBleedStacks() > 0) {
            ar.skipDefense = true;
        }

        if (ar.extraDamageIfBurn > 0 && opponent.getBurnStacks() > 0) {
            ar.damage += ar.extraDamageIfBurn;
            ar.desc = ar.desc + " (灼伤+" + ar.extraDamageIfBurn + ")";
        }

        if (ar.doubleDamageIfBurn && opponent.getBurnStacks() > 0) {
            int old = ar.damage / 2;
            ar.damage *= 2;
            ar.desc = ar.desc + " (灼伤翻倍" + old + "→" + ar.damage + ")";
        }



        if (ar.forceOpponentDiscardOne) {
            if (self != game.getPlayerChar()) {
                List<Card> oppHand = game.getPlayerHand();
                if (!oppHand.isEmpty()) {
                    int idx = (int)(Math.random() * oppHand.size());
                    Card discarded = oppHand.remove(idx);
                    game.getDiscardPile().addLast(discarded);
                    GameAnim.playFloatingText(game, "弃" + discarded, new Color(255, 60, 60),
                        new Point(game.getWidth() / 2, game.getHeight() * 3 / 4 - 30));
                }
            } else {
                game.setPendingFiveChoice(true);
                game.setForceOpponentDiscardOne(true);
            }
        }

        if (ar.clearSelfBuffs) {
            self.removeBurn(self.getBurnStacks());
        }

        String msg = ar.desc;
        if (ar.selfHeal > 0) {
            msg += " (恢复" + ar.selfHeal + ")";
            Point loc = self == game.getPlayerChar()
                ? new Point(game.getWidth() / 2, game.getHeight() * 3 / 4)
                : new Point(game.getWidth() / 2, game.getHeight() / 3);
            GameAnim.playFloatingText(game, "+" + ar.selfHeal, new Color(60, 220, 60), loc);
        }
        if (ar.selfDamage > 0 && !self.isImmuneToBurn()) {
            Point loc = self == game.getPlayerChar()
                ? new Point(game.getWidth() / 2 - 60, game.getHeight() * 3 / 4)
                : new Point(game.getWidth() / 2 - 60, game.getHeight() / 3);
            GameAnim.playFloatingText(game, "-" + ar.selfDamage, new Color(255, 60, 60), loc);
        }
        if (ar.damage > 0) msg += " (即将造成" + ar.damage + "点伤害)";
        game.showAttackDesc(msg);
    }

    boolean hasFollowUp(GameCharacter.AttackResult ar) {
        return ar != null && !ar.followUps.isEmpty();
    }

    GameCharacter.FollowUp getFirstFollowUp(GameCharacter.AttackResult ar) {
        return ar != null && !ar.followUps.isEmpty() ? ar.followUps.get(0) : null;
    }

    void executeFollowUp(GameCharacter.FollowUp fu, Card card, GameCharacter self, GameCharacter opponent,
                           List<Card> selfHand, Runnable onDone) {
        switch (fu) {
            case FIVE_CHOICE:
                executeFiveChoice(card, self, opponent, selfHand, onDone);
                break;
            case REVEAL_TOP_DECK:
                executeRevealTopDeck(self, opponent, selfHand, onDone);
                break;
            case REVEAL_DRAW:
                executeRevealDraw(self, opponent, selfHand, onDone);
                break;
            case REVEAL_AND_JUDGE:
                executeRevealAndJudge(self, opponent, selfHand, onDone);
                break;
            case CHAN_FOUR_SWAP:
                game.handleChanFourSwap(self, opponent, selfHand, opponent == game.getPlayerChar() ? game.getPlayerHand() : game.getAIHand(), onDone);
                break;
            case CHAN_FIVE_REORDER:
                game.handleChanFiveReorder(self, opponent, selfHand, onDone);
                break;
            case CHAN_SEVEN_JUDGE:
                game.handleChanSevenJudge(self, opponent, opponent == game.getPlayerChar() ? game.getPlayerHand() : game.getAIHand(), onDone);
                break;
            case CHAN_SIX_REVEAL:
                game.handleChanSixReveal(self, opponent, selfHand, onDone);
                break;
            case SAIKI_THREE_DRAW:
                game.handleSaikiThreeDraw(self, opponent, selfHand, opponent == game.getPlayerChar() ? game.getPlayerHand() : game.getAIHand(), onDone);
                break;
            case SAIKI_SIX_JUDGE:
                game.handleSaikiSixJudge(self, opponent, selfHand, onDone);
                break;

            default:
                break;
        }
    }

    private void executeFiveChoice(Card card, GameCharacter self, GameCharacter opponent,
                                    List<Card> selfHand, Runnable onDone) {
        game.setPendingFiveChoice(true);
        game.setFiveChoiceCard(card);
        if (self == game.getPlayerChar()) {
            game.setCurrentPhase(Game.Phase.PLAYER_FIVE_CHOICE);
            game.updateDisplay();

        } else {
            game.resolveAIFiveChoice(self, opponent, selfHand, onDone);
        }
    }


    private void executeRevealTopDeck(GameCharacter self, GameCharacter opponent,
                                       List<Card> selfHand, Runnable onDone) {
        game.handleRevealTopDeck(self, opponent, selfHand);
    }

    private void executeRevealDraw(GameCharacter self, GameCharacter opponent,
                                    List<Card> selfHand, Runnable onDone) {
        game.handleRevealDraw(self, opponent, selfHand, self == game.getPlayerChar());
    }

    private void executeRevealAndJudge(GameCharacter self, GameCharacter opponent,
                                        List<Card> selfHand, Runnable onDone) {
        game.handleRevealAndJudge(self, opponent, selfHand, onDone);
    }
}