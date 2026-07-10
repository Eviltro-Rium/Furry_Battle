import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ChanAI extends AIPlayer {

    private GameCharacter character;

    public ChanAI(GameCharacter character) {
        this.character = character;
    }

    @Override
    protected boolean shouldSkipCard(Card card) {
        if (card.isItemCard()) return false;
        int v = card.getValue();
        if (v == 1 && aiHpPercent > 50) return true;
        if (v == 5 && aiHpPercent <= 30) return true;
        return false;
    }

    @Override
    protected int attackPriority(Card card, Card top) {
        if (card.isItemCard()) return super.attackPriority(card, top);
        int v = card.getValue();
        if (v == 0) return 72;
        if (v == 4 && (aiOpponentBleedStacks > 0 || aiOpponentBurnStacks > 0)) return 70;
        if (v == 6) return 62;
        if (v == 7 && hand != null && hand.size() >= 3) return 60;
        if (v == 5 && aiHpPercent > 30) return 55;
        if (v == 2) return 40;
        if (v == 3) return 38;
        if (v == 1 && aiHpPercent <= 50) return 50;
        return super.attackPriority(card, top);
    }

    @Override
    protected int defendPriority(Card card, Card top) {
        if (card.isItemCard()) return super.defendPriority(card, top);
        int v = card.getValue();
        if (v == 0) return 85;
        if (v == 2) return 55;
        if (v == 3) return 45;
        return super.defendPriority(card, top);
    }

    Card chooseFourSwap(Card drawn, List<Card> aiHand) {
        if (drawn.isItemCard() || drawn.getValue() == 0) {
            Card smallest = null;
            for (Card c : aiHand) {
                if (c.isItemCard() || c.getValue() == 0) continue;
                if (smallest == null || c.getValue() < smallest.getValue()) {
                    smallest = c;
                }
            }
            return smallest;
        }
        Card sameColorSmaller = null;
        for (Card c : aiHand) {
            if (c.isItemCard() || c.getValue() == 0) continue;
            if (c.getColor() == drawn.getColor() && c.getValue() < drawn.getValue()) {
                if (sameColorSmaller == null || c.getValue() < sameColorSmaller.getValue()) {
                    sameColorSmaller = c;
                }
            }
        }
        return sameColorSmaller;
    }

    List<Card> reorderFive(List<Card> topFive) {
        List<Card> blacks = new ArrayList<>();
        List<Card> items = new ArrayList<>();
        List<Card> zeros = new ArrayList<>();
        List<Card> others = new ArrayList<>();

        for (Card c : topFive) {
            if (c.isBlack()) blacks.add(c);
            else if (c.isItemCard()) items.add(c);
            else if (c.getValue() == 0) zeros.add(c);
            else others.add(c);
        }

        others.sort(Comparator.comparingInt(Card::getValue).reversed());

        List<Card> result = new ArrayList<>();
        result.addAll(blacks);
        result.addAll(items);
        result.addAll(zeros);
        result.addAll(others);
        return result;
    }

    boolean chooseSevenKeep(Card card) {
        if (card.isItemCard()) return true;
        if (card.getValue() == 0) return true;
        return card.getValue() >= 4;
    }
}
