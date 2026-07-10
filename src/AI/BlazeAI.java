public class BlazeAI extends AIPlayer {

    private GameCharacter character;
    private GameCharacter opponent;

    public BlazeAI(GameCharacter character) {
        this.character = character;
    }

    void setOpponent(GameCharacter opponent) {
        this.opponent = opponent;
    }

    @Override
    protected boolean shouldSkipCard(Card card) {
        if (card.isItemCard()) return false;
        int v = card.getValue();
        if (v == 6 && aiBurnStacks == 0) return true;
        return false;
    }

    @Override
    protected int attackPriority(Card card, Card top) {
        if (card.isItemCard()) return super.attackPriority(card, top);
        int v = card.getValue();
        if (v == 5 && aiBurnStacks >= 3) return 80;
        if (v == 7 && (aiBurnStacks + aiOpponentBurnStacks) >= 4) return 75;
        if (v == 0) return 68;
        if (v == 5 && aiBurnStacks >= 2) return 65;
        if (v == 2 && aiOpponentGuardStacks > 0) return 62;
        if (v == 6 && aiBurnStacks >= 3) return 58;
        if (v == 3 && aiBurnStacks < 4) return 50;
        if (v == 1) return 45;
        if (v == 4 && aiOpponentHandSize > 0) return 48;
        if (v == 2 && aiBurnStacks == 0) return 42;
        return super.attackPriority(card, top);
    }

    @Override
    protected int defendPriority(Card card, Card top) {
        if (card.isItemCard()) return super.defendPriority(card, top);
        int v = card.getValue();
        if (v == 0 && aiBurnStacks >= 3) return 80;
        if (v == 3 && aiBurnStacks >= 2) return 68;
        if (v == 1 && aiBurnStacks >= 1) return 58;
        if (v == 0 && aiBurnStacks <= 1) return 55;
        if (v == 2) return 45;
        return super.defendPriority(card, top);
    }
}
