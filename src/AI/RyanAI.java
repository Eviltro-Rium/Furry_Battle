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
        if (character.getCurrentHp() >= character.getMaxHp()) {
            if (v == 2 || v == 6) return true;
        }
        if (character.getCurrentHp() <= character.getMaxHp() / 4 && v == 0) return true;
        return false;
    }

    @Override
    protected int attackPriority(Card card, Card top) {
        if (card.isItemCard()) return super.attackPriority(card, top);
        int v = card.getValue();
        if (character != null && character.getCurrentHp() <= character.getMaxHp() / 3 && v == 0) return 80;
        if (v == 6 && character != null) {
            int handSum = 0;
            for (Card c : hand) {
                if (c.isNumberCard()) handSum += c.getValue();
            }
            if (handSum >= 15) return 70;
        }
        return super.attackPriority(card, top);
    }

    @Override
    protected int defendPriority(Card card, Card top) {
        if (card.isItemCard()) return super.defendPriority(card, top);
        int v = card.getValue();
        if (v == 0 && character != null && character.getCurrentHp() <= character.getMaxHp() / 3) return 80;
        if (v == 3 && top != null && top.getEffectiveColor() == Card.CardColor.RED) return 70;
        return super.defendPriority(card, top);
    }

    @Override
    public boolean chooseFiveDamage(int aiHp, int maxHp, int opponentHp) {
        return aiHp >= maxHp || aiHp >= opponentHp;
    }
}
