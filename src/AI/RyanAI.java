public class RyanAI extends AIPlayer {

    private GameCharacter character;

    public RyanAI(GameCharacter character) {
        this.character = character;
    }

    @Override
    protected boolean shouldSkipCard(Card card) {
        if (character == null) return false;
        if (card.isItemCard()) return false;
        int v = card.getValue();
        if (aiFullHp && (v == 2 || v == 6)) return true;
        if (aiHpPercent <= 25 && v == 0) return true;
        return false;
    }

    @Override
    protected int attackPriority(Card card, Card top) {
        if (card.isItemCard()) return super.attackPriority(card, top);
        int v = card.getValue();
        if (v == 0 && aiHpPercent <= 33) return 85;
        if (v == 6) {
            int handSum = 0;
            for (Card c : hand) { if (c.isNumberCard()) handSum += c.getValue(); }
            if (handSum >= 15) return 72;
            if (handSum >= 10) return 50;
        }
        if (v == 1 && !aiFullHp) return 55;
        if (v == 4 && aiOpponentGuardStacks == 0) return 48;
        return super.attackPriority(card, top);
    }

    @Override
    protected int defendPriority(Card card, Card top) {
        if (card.isItemCard()) return super.defendPriority(card, top);
        int v = card.getValue();
        if (v == 0 && aiHpPercent <= 33) return 85;
        if (v == 3 && top != null && top.getEffectiveColor() == Card.CardColor.RED) return 72;
        return super.defendPriority(card, top);
    }

    @Override
    public boolean chooseFiveDamage(int aiHp, int maxHp, int opponentHp) {
        return aiHp >= maxHp || aiHp >= opponentHp;
    }
}
