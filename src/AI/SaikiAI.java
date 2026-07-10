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
    protected boolean shouldSkipCard(Card card) {
        if (card.isItemCard()) return false;
        int v = card.getValue();
        if (v == 7 && aiOpponentBleedStacks == 0) return true;
        return false;
    }

    @Override
    protected int attackPriority(Card card, Card top) {
        if (card.isItemCard()) return super.attackPriority(card, top);
        int v = card.getValue();
        if (v == 0 && aiOpponentBleedStacks >= 2) return 82;
        if (v == 4 && aiOpponentBleedStacks > 0) return 78;
        if (v == 7 && aiOpponentBleedStacks >= 2) return 75;
        if (v == 5) {
            if (aiHpPercent <= 20) return 70;
            if (aiHpPercent <= 50) return 65;
            return 58;
        }
        if (v == 6 && hand != null) {
            Card sixCard = chooseSixCard(hand);
            if (sixCard != null && sixCard.getValue() >= 5) return 60;
        }
        if (v == 0 && aiOpponentBleedStacks == 1) return 55;
        if (v == 3 && aiOpponentHandSize > 0) return 48;
        if (v == 1) return 42;
        if (v == 2) return 40;
        return super.attackPriority(card, top);
    }

    @Override
    protected int defendPriority(Card card, Card top) {
        if (card.isItemCard()) return super.defendPriority(card, top);
        int v = card.getValue();
        if (v == 0) return 85;
        if (v == 2 && aiOpponentBleedStacks >= 1) return 60;
        if (v == 3) return 50;
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
