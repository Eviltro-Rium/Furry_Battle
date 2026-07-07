import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SerenityHandler extends CharacterHandler {

    SerenityHandler(Game game) {
        super(game);
    }

    void handleSerenityFiveReveal(GameCharacter self, GameCharacter opponent,
                                   List<Card> selfHand, Runnable onDone) {
        game.refillDeckIfNeeded();
        if (game.deck.remaining() == 0) { onDone.run(); return; }

        Card revealed = game.deck.draw();
        game.showAIRevealCard(revealed);

        boolean isYellowGreen = revealed.getColor() == Card.CardColor.YELLOW || revealed.getColor() == Card.CardColor.GREEN
            || (revealed.isWhite() && revealed.getChosenColor() != null &&
                (revealed.getChosenColor() == Card.CardColor.YELLOW || revealed.getChosenColor() == Card.CardColor.GREEN));

        String who = self == game.playerChar ? "你" : "AI";

        if (isYellowGreen) {
            self.heal(4);
            game.showAttackDesc("5 判定" + revealed + " → " + who + "恢复4点命");
            GameAnim.playFloatingText(game, "+4", new Color(60, 220, 60),
                self == game.playerChar
                    ? new Point(game.getWidth() / 2, game.getHeight() * 3 / 4 - 30)
                    : new Point(game.getWidth() / 2, game.getHeight() / 3 - 30));
        } else {
            int dmg = 5;
            opponent.takeDamage(dmg);
            game.showAttackDesc("5 判定" + revealed + " → " + who + "对对手造成5点伤");
            GameAnim.playFloatingText(game, "-" + dmg, new Color(255, 60, 60),
                opponent == game.playerChar
                    ? new Point(game.getWidth() / 2, game.getHeight() * 3 / 4)
                    : new Point(game.getWidth() / 2, game.getHeight() / 3));
        }

        game.discardPile.addLast(revealed);
        Timer t = new Timer(800, e -> {
            ((Timer)e.getSource()).stop();
            onDone.run();
        });
        t.setRepeats(false);
        t.start();
    }

    void handleSerenityZeroDiscard(GameCharacter self, GameCharacter opponent,
                                    List<Card> selfHand, Runnable onDone) {
        int discardCount = selfHand.size();
        int healPerCard = 3;
        int maxHeal = 9;
        int totalHeal = Math.min(maxHeal, discardCount * healPerCard);

        for (Card c : new ArrayList<>(selfHand)) {
            game.discardPile.addLast(c);
        }
        selfHand.clear();

        self.heal(totalHeal);
        String who = self == game.playerChar ? "你" : "AI";
        game.showAttackDesc("0 " + who + "弃" + discardCount + "张 → 恢复" + totalHeal + "点命");
        GameAnim.playFloatingText(game, "+" + totalHeal, new Color(60, 220, 60),
            self == game.playerChar
                ? new Point(game.getWidth() / 2, game.getHeight() * 3 / 4 - 30)
                : new Point(game.getWidth() / 2, game.getHeight() / 3 - 30));

        boolean bt = self.getCurrentHp() < 30;
        if (bt) {
            List<Card> oppHand = opponent == game.playerChar ? game.getPlayerHand() : game.getAIHand();
            int oppCount = oppHand.size();
            for (Card c : new ArrayList<>(oppHand)) {
                game.discardPile.addLast(c);
            }
            oppHand.clear();
            int newOppCount = Math.max(0, oppCount - 1);
            List<Card> drawn = game.drawFromDeck(newOppCount);
            oppHand.addAll(drawn);
            game.showAttackDesc("0 嗜血: 对手弃全部手牌,重抽" + newOppCount + "张");
        }

        List<Card> newHand = game.drawFromDeck(4);
        selfHand.addAll(newHand);

        Timer t = new Timer(600, e -> {
            ((Timer)e.getSource()).stop();
            onDone.run();
        });
        t.setRepeats(false);
        t.start();
    }

    void handleSerenityDefendTwoDrain(GameCharacter self, GameCharacter opponent,
                                       List<Card> selfHand, Runnable onDone) {
        int bleedStacks = opponent.getBleedStacks();
        int drain = bleedStacks * 2;
        if (drain > 0) {
            int actualDrain = Math.min(drain, opponent.getCurrentHp());
            opponent.takeDamage(actualDrain);
            self.heal(actualDrain);
            String who = self == game.playerChar ? "你" : "AI";
            game.showDefendDesc("2 " + who + "吸血" + actualDrain + "点(血" + bleedStacks + "层)");
            GameAnim.playFloatingText(game, "-" + actualDrain, new Color(255, 60, 60),
                opponent == game.playerChar
                    ? new Point(game.getWidth() / 2, game.getHeight() * 3 / 4)
                    : new Point(game.getWidth() / 2, game.getHeight() / 3));
            GameAnim.playFloatingText(game, "+" + actualDrain, new Color(60, 220, 60),
                self == game.playerChar
                    ? new Point(game.getWidth() / 2, game.getHeight() * 3 / 4 - 30)
                    : new Point(game.getWidth() / 2, game.getHeight() / 3 - 30));
        }

        Timer t = new Timer(600, e -> {
            ((Timer)e.getSource()).stop();
            onDone.run();
        });
        t.setRepeats(false);
        t.start();
    }

    void handleSerenityDefendZeroReveal(GameCharacter self, GameCharacter opponent,
                                         List<Card> selfHand, Runnable onDone) {
        game.refillDeckIfNeeded();
        if (game.deck.remaining() == 0) {
            onDone.run();
            return;
        }

        Card revealed = game.deck.draw();
        game.showAIRevealCard(revealed);

        boolean isYellow = revealed.getColor() == Card.CardColor.YELLOW
            || (revealed.isWhite() && revealed.getChosenColor() == Card.CardColor.YELLOW);

        if (isYellow) {
            int halvedHp = (int) Math.ceil(opponent.getCurrentHp() / 2.0);
            int reduction = opponent.getCurrentHp() - halvedHp;
            opponent.takeDamage(reduction);
            game.showDefendDesc("0 判定" + revealed + " → 黄牌! 对手命量减半");
            GameAnim.playFloatingText(game, "-" + reduction, new Color(255, 60, 60),
                opponent == game.playerChar
                    ? new Point(game.getWidth() / 2, game.getHeight() * 3 / 4)
                    : new Point(game.getWidth() / 2, game.getHeight() / 3));
        } else {
            game.pendingDefendResult.immuneAll = true;
            game.pendingDefendResult.immuneDebuff = true;
            game.showDefendDesc("0 判定" + revealed + " → 非黄牌! 免疫所有伤害和debuff");
        }

        game.discardPile.addLast(revealed);

        Timer t = new Timer(800, e -> {
            ((Timer)e.getSource()).stop();
            onDone.run();
        });
        t.setRepeats(false);
        t.start();
    }
}