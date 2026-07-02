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
        if (card.isPotion()) return false;
        if (card.isDrawThree()) return false;
        if (card.isBlack()) return false;
        int v = card.getValue();
        if (v == 1 && character != null && character.getCurrentHp() > character.getMaxHp() / 2) return true;
        return false;
    }

    @Override
    public Card chooseDefend(Card top, boolean excludePotion) {
        Card zeroCard = null;
        Card colorMatch = null;
        Card numberMatch = null;
        Card blackCard = null;
        Card whiteCard = null;

        Card.CardColor effective = top.getEffectiveColor();

        for (Card c : hand) {
            if (!isDefendCard(c, top)) continue;
            if (c.isWhite() && c.getValue() == 0 && !c.isPotion() && !c.isDrawThree()) {
                if (zeroCard == null) zeroCard = c;
                continue;
            }
            if (c.isBlack()) {
                if (blackCard == null) blackCard = c;
                continue;
            }
            if (c.isWhite()) {
                if (whiteCard == null) whiteCard = c;
                continue;
            }
            if (c.getColor() == effective && colorMatch == null) {
                colorMatch = c;
            }
            if (c.getValue() == top.getValue() && numberMatch == null) {
                numberMatch = c;
            }
        }

        if (zeroCard != null && character != null && character.getCurrentHp() <= character.getMaxHp() / 2) {
            return zeroCard;
        }
        if (colorMatch != null) return colorMatch;
        if (numberMatch != null) return numberMatch;
        if (zeroCard != null) return zeroCard;
        if (whiteCard != null) return whiteCard;
        if (blackCard != null) {
            Card.CardColor chosen = chooseBlackColorForDefend(top);
            blackCard.setChosenColor(chosen);
        }
        return blackCard;
    }

    Card chooseFourSwap(Card drawn, List<Card> aiHand) {
        if (drawn.isBlack() || drawn.isWhite() || drawn.getValue() == 0) {
            Card smallest = null;
            for (Card c : aiHand) {
                if (c.getValue() == 0 || c.isBlack() || c.isWhite()) continue;
                if (smallest == null || c.getValue() < smallest.getValue()) {
                    smallest = c;
                }
            }
            return smallest;
        }
        Card sameColorSmaller = null;
        for (Card c : aiHand) {
            if (c.getValue() == 0 || c.isBlack() || c.isWhite()) continue;
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
        List<Card> whites = new ArrayList<>();
        List<Card> zeros = new ArrayList<>();
        List<Card> others = new ArrayList<>();

        for (Card c : topFive) {
            if (c.isBlack()) blacks.add(c);
            else if (c.isWhite() && !c.isPotion() && !c.isDrawThree() && c.getValue() == 0) zeros.add(c);
            else if (c.isWhite()) whites.add(c);
            else others.add(c);
        }

        others.sort(Comparator.comparingInt(Card::getValue).reversed());

        List<Card> result = new ArrayList<>();
        result.addAll(blacks);
        result.addAll(whites);
        result.addAll(zeros);
        result.addAll(others);
        return result;
    }

    boolean chooseSevenKeep(Card card) {
        if (card.isBlack() || card.isWhite() || card.getValue() == 0) return true;
        return card.getValue() >= 4;
    }
}