
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AIPlayer {
    protected List<Card> hand;
    protected boolean aiHasDebuff;
    protected int aiDebuffCount;
    protected boolean aiFullHp;
    protected int aiOpponentHandSize;

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

    protected int attackPriority(Card card, Card top) {
        if (card.isBlack()) return 1;
        if (card.isSuperPurify()) return aiHasDebuff ? (aiDebuffCount >= 3 ? 52 : 48) : 2;
        if (card.isPurify()) return aiHasDebuff ? (aiDebuffCount <= 2 ? 51 : 49) : 2;
        if (card.isPotion()) return 3;
        if (card.isSwapHand()) return (hand.size() <= aiOpponentHandSize) ? 6 : 0;
        if (card.isDrawThree()) return 4;
        if (card.isWhite()) return 5;
        if (shouldSkipCard(card)) return 0;
        Card.CardColor effective = top.getEffectiveColor();
        boolean colorMatch = card.getColor() == effective;
        boolean numberMatch = card.getValue() == top.getValue();
        if (!colorMatch && !numberMatch) return 0;
        int base = colorMatch ? 20 : 15;
        base += cardAttackValue(card) * 2;
        return base;
    }

    protected int cardAttackValue(Card card) {
        if (card.isItemCard()) return 0;
        return card.getValue();
    }

    public Card choosePlay(Card top, boolean aiHasDebuff) {
        this.aiHasDebuff = aiHasDebuff;
        Card best = null;
        int bestPri = -1;

        for (Card c : hand) {
            if (!canPlayOn(c, top)) continue;
            int pri = attackPriority(c, top);
            if (pri > bestPri) {
                bestPri = pri;
                best = c;
            }
        }

        if (best != null) {
            if (best.isBlack()) best.setChosenColor(chooseBlackColor());
            if (best.isWhite()) best.setChosenColor(top.getEffectiveColor());
        }
        return best;
    }

    public Card choosePlay(Card top) {
        return choosePlay(top, false);
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
            if (!card.isBlack() && !card.isWhite() && counts.containsKey(card.getColor())) {
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
        if (card.isPurify() || card.isSuperPurify()) return aiHasDebuff;
        if (card.isWhite()) return card.getValue() <= 3 && canPlayOn(card, top);
        return card.getValue() <= 3 && canPlayOn(card, top);
    }

    protected boolean hasLowCard() {
        for (Card c : hand) {
            if (!c.isBlack() && c.getValue() <= 3) return true;
        }
        return false;
    }

    protected int defendPriority(Card card, Card top) {
        if (card.isPotion()) return aiFullHp ? 2 : 60;
        if (card.isSuperPurify()) return aiHasDebuff ? (aiDebuffCount >= 3 ? 57 : 53) : 5;
        if (card.isPurify()) return aiHasDebuff ? (aiDebuffCount <= 2 ? 56 : 54) : 5;
        if (card.isDrawThree()) return 50;
        if (card.isSwapHand()) return (hand.size() <= aiOpponentHandSize) ? 8 : 3;
        if (card.isBlack()) return 10;
        if (card.isWhite()) return 15;
        if (!isDefendCard(card, top)) return 0;
        int base = 20;
        base += (4 - card.getValue()) * 5;
        return base;
    }

    public Card chooseDefend(Card top) {
        return chooseDefend(top, false);
    }

    public Card chooseDefend(Card top, boolean excludePotion) {
        Card best = null;
        int bestPri = -1;

        for (Card c : hand) {
            if (excludePotion && c.isPotion()) continue;
            if (!isDefendCard(c, top)) continue;
            int pri = defendPriority(c, top);
            if (pri > bestPri) {
                bestPri = pri;
                best = c;
            }
        }

        if (best != null) {
            if (best.isBlack()) best.setChosenColor(chooseBlackColorForDefend(top));
            if (best.isWhite()) best.setChosenColor(top.getEffectiveColor());
        }
        return best;
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
