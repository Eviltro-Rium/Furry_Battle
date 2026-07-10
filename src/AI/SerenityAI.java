public class SerenityAI extends AIPlayer {

    private GameCharacter character;
    private GameCharacter opponent;

    public SerenityAI(GameCharacter character) {
        this.character = character;
    }

    void setOpponent(GameCharacter opponent) {
        this.opponent = opponent;
    }

    private boolean isBloodthirsty() {
        return aiHpPercent < 30;
    }

    @Override
    protected boolean shouldSkipCard(Card card) {
        return false;
    }

    @Override
    protected int attackPriority(Card card, Card top) {
        if (card.isItemCard()) return super.attackPriority(card, top);
        int v = card.getValue();
        boolean bt = isBloodthirsty();
        if (v == 0) return bt ? 82 : 60;
        if (v == 7) return bt ? 78 : 50;
        if (v == 6) return bt ? 75 : 55;
        if (v == 4) return bt ? 72 : 48;
        if (v == 2) return bt ? 68 : 42;
        if (v == 1 && bt) return 65;
        if (v == 1 && aiHpPercent < 60) return 52;
        if (v == 5) return 45;
        if (v == 3) return 40;
        return super.attackPriority(card, top);
    }

    @Override
    protected int defendPriority(Card card, Card top) {
        if (card.isItemCard()) return super.defendPriority(card, top);
        int v = card.getValue();
        boolean bt = isBloodthirsty();
        if (v == 0) return 80;
        if (v == 2 && aiOpponentBleedStacks >= 1) return 62;
        if (v == 3 && bt) return 58;
        if (v == 1 && bt) return 55;
        return super.defendPriority(card, top);
    }
}
