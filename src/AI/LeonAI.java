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
        int v = card.getValue();
        if (aiFullHp && v == 2) return true;
        if (v == 1 && aiOpponentBurnStacks >= 4) return true;
        return false;
    }

    @Override
    protected int attackPriority(Card card, Card top) {
        if (card.isItemCard()) return super.attackPriority(card, top);
        int v = card.getValue();
        if (v == 4 && aiOpponentBurnStacks > 0) return 78;
        if (v == 7 && aiOpponentBurnStacks >= 2) return 73;
        if (v == 0) {
            if (aiHpPercent > 60) return 12;
            if (aiHpPercent > 40) return 25;
            return 40;
        }
        if (v == 2 && aiOpponentBurnStacks == 0) return 45;
        if (v == 3 && aiOpponentBurnStacks == 0) return 42;
        return super.attackPriority(card, top);
    }

    @Override
    protected int defendPriority(Card card, Card top) {
        if (card.isItemCard()) return super.defendPriority(card, top);
        int v = card.getValue();
        if (v == 0 && aiHpPercent <= 33) return 85;
        if (v == 1 && aiOpponentBurnStacks < 4) return 55;
        return super.defendPriority(card, top);
    }

    @Override
    public boolean chooseFiveDamage(int aiHp, int maxHp, int opponentHp) {
        return true;
    }
}
