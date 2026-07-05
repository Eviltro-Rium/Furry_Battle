public class LeonAI extends AIPlayer {

    private GameCharacter character;
    private GameCharacter opponent;

    public LeonAI(GameCharacter character) {
        this.character = character;
    }

    void setOpponent(GameCharacter opponent) {
        this.opponent = opponent;
    }

    @Override
    protected boolean shouldSkipCard(Card card) {
        if (card.isItemCard()) return false;
        if (character == null) return false;
        int v = card.getValue();
        if (character.getCurrentHp() >= character.getMaxHp() && v == 2) return true;
        if (v == 1 && opponent != null && opponent.getBurnStacks() >= 4) return true;
        return false;
    }

    @Override
    protected int attackPriority(Card card, Card top) {
        if (card.isItemCard()) return super.attackPriority(card, top);
        int v = card.getValue();
        if (v == 4 && opponent != null && opponent.getBurnStacks() > 0) return 75;
        if (v == 7 && opponent != null && opponent.getBurnStacks() >= 2) return 70;
        if (v == 0 && character != null && character.getCurrentHp() > character.getMaxHp() / 2) return 10;
        return super.attackPriority(card, top);
    }

    @Override
    protected int defendPriority(Card card, Card top) {
        if (card.isItemCard()) return super.defendPriority(card, top);
        int v = card.getValue();
        if (v == 0 && character != null && character.getCurrentHp() <= character.getMaxHp() / 3) return 80;
        return super.defendPriority(card, top);
    }

    @Override
    public boolean chooseFiveDamage(int aiHp, int maxHp, int opponentHp) {
        return true;
    }
}
