import java.util.List;

public class SaikiAI extends AIPlayer {

    private GameCharacter character;
    private GameCharacter opponent;

    public SaikiAI() {
        super();
    }

    void setCharacters(GameCharacter self, GameCharacter opponent) {
        this.character = self;
        this.opponent = opponent;
    }

    @Override
    protected int attackPriority(Card card, Card top) {
        if (card.isItemCard()) return super.attackPriority(card, top);
        int v = card.getValue();
        if (v == 4 && opponent != null && opponent.getBleedStacks() > 0) return 80;
        if (v == 7 && opponent != null && opponent.getBleedStacks() >= 2) return 75;
        if (v == 0 && opponent != null && opponent.getBleedStacks() >= 2) return 70;
        if (v == 5 && character != null) {
            int hp = character.getCurrentHp();
            if (hp <= 20) return 65;
            if (hp <= 50) return 60;
        }
        if (v == 6 && hand != null) {
            Card sixCard = chooseSixCard(hand);
            if (sixCard != null && sixCard.getValue() >= 5) return 55;
        }
        Card.CardColor effective = top.getEffectiveColor();
        if (card.getEffectiveColor() == Card.CardColor.YELLOW || (card.getColor() == Card.CardColor.YELLOW)) {
            if (v >= 1 && v <= 7) return super.attackPriority(card, top) + 5;
        }
        return super.attackPriority(card, top);
    }

    @Override
    protected int defendPriority(Card card, Card top) {
        if (card.isItemCard()) return super.defendPriority(card, top);
        int v = card.getValue();
        if (v == 0) return 80;
        if (v == 2 && opponent != null) return 55;
        return super.defendPriority(card, top);
    }

    boolean chooseThreeDiscard(Card drawn) {
        if (drawn.isItemCard()) return true;
        if (drawn.getValue() == 0) return false;
        return drawn.getValue() <= 2;
    }

    boolean chooseFiveHeal(int aiHp) {
        return aiHp <= 20;
    }

    boolean chooseFiveDamage(int aiHp) {
        return aiHp > 20 && aiHp <= 50;
    }

    Card chooseSixCard(List<Card> hand) {
        Card best = null;
        for (Card c : hand) {
            if (c.isItemCard()) continue;
            if (c.getValue() == 0) continue;
            if (best == null || c.getValue() > best.getValue()) best = c;
        }
        return best;
    }
}
