
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AIPlayer {
    protected List<Card> hand;

    public AIPlayer() {
        hand = new ArrayList<>();
    }

    public List<Card> getHand() {
        return hand;
    }

    public void addCards(List<Card> cards) {
        hand.addAll(cards);
    }

    public void removeCard(Card card) {
        hand.remove(card);
    }

    public int handSize() {
        return hand.size();
    }

    public boolean hasPlayableCard(Card top) {
        for (Card c : hand) {
            if (canPlayOn(c, top)) return true;
        }
        return false;
    }

    protected boolean canPlayOn(Card card, Card top) {
        if (card.isBlack()) return true;
        if (card.isWhite()) return true;
        Card.CardColor effective = top.getEffectiveColor();
        return card.getColor() == effective || card.getValue() == top.getValue();
    }

    public Card choosePlay(Card top) {
        Card colorMatch = null;
        Card numberMatch = null;
        Card blackCard = null;
        Card whiteCard = null;

        Card.CardColor effective = top.getEffectiveColor();

        for (Card c : hand) {
            if (c.isBlack()) {
                if (blackCard == null) blackCard = c;
                continue;
            }
            if (c.isWhite()) {
                if (whiteCard == null) whiteCard = c;
                continue;
            }
            if (shouldSkipCard(c)) continue;
            if (c.getColor() == effective && colorMatch == null) {
                colorMatch = c;
            }
            if (c.getValue() == top.getValue() && numberMatch == null) {
                numberMatch = c;
            }
        }

        if (colorMatch != null) return colorMatch;
        if (numberMatch != null) return numberMatch;
        if (whiteCard != null) {
            whiteCard.setChosenColor(effective);
            return whiteCard;
        }
        if (blackCard != null) return blackCard;

        return null;
    }

    protected boolean shouldSkipCard(Card card) {
        return false;
    }

    public Card.CardColor chooseBlackColor() {
        Map<Card.CardColor, Integer> counts = new HashMap<>();
        for (Card.CardColor c : new Card.CardColor[]{Card.CardColor.RED, Card.CardColor.YELLOW, Card.CardColor.BLUE, Card.CardColor.GREEN}) {
            counts.put(c, 0);
        }
        for (Card card : hand) {
            if (!card.isBlack() && counts.containsKey(card.getColor())) {
                counts.put(card.getColor(), counts.get(card.getColor()) + 1);
            }
        }
        Card.CardColor best = Card.CardColor.RED;
        int bestCount = -1;
        for (Map.Entry<Card.CardColor, Integer> entry : counts.entrySet()) {
            if (entry.getValue() > bestCount) {
                bestCount = entry.getValue();
                best = entry.getKey();
            }
        }
        return best;
    }

    public boolean canDefend(Card top) {
        for (Card c : hand) {
            if (isDefendCard(c, top)) return true;
        }
        return false;
    }

    protected boolean isDefendCard(Card card, Card top) {
        if (card.isBlack()) return hasLowCard();
        if (card.isDrawThree()) return true;
        if (card.isPotion()) return true;
        if (card.isWhite()) return card.getValue() <= 3 && canPlayOn(card, top);
        return card.getValue() <= 3 && canPlayOn(card, top);
    }

    protected boolean hasLowCard() {
        for (Card c : hand) {
            if (!c.isBlack() && c.getValue() <= 3) return true;
        }
        return false;
    }

    public Card chooseDefend(Card top) {
        return chooseDefend(top, false);
    }

    public Card chooseDefend(Card top, boolean excludePotion) {
        Card colorMatch = null;
        Card numberMatch = null;
        Card blackCard = null;
        Card whiteCard = null;

        Card.CardColor effective = top.getEffectiveColor();

        for (Card c : hand) {
            if (!isDefendCard(c, top)) continue;

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

        if (colorMatch != null) return colorMatch;
        if (numberMatch != null) return numberMatch;
        if (whiteCard != null) return whiteCard;
        if (blackCard != null) {
            Card.CardColor chosen = chooseBlackColorForDefend(top);
            blackCard.setChosenColor(chosen);
        }
        return blackCard;
    }

    protected Card.CardColor chooseBlackColorForDefend(Card top) {
        for (Card c : hand) {
            if (!c.isBlack() && c.getValue() <= 3) {
                return c.getColor();
            }
        }
        return chooseBlackColor();
    }

    public List<Card> chooseDiscards() {
        return new ArrayList<>(hand);
    }

    public void clear() {
        hand.clear();
    }

    public boolean chooseFiveDamage(int aiHp, int maxHp, int opponentHp) {
        return aiHp > opponentHp;
    }
}
