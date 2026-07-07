import java.util.List;

public class SerenityAI extends AIPlayer {

    private GameCharacter character;
    private GameCharacter opponent;

    public SerenityAI(GameCharacter character) {
        this.character = character;
    }

    void setOpponent(GameCharacter opponent) {
        this.opponent = opponent;
    }

    @Override
    protected boolean shouldSkipCard(Card card) {
        if (card.isItemCard()) return false;
        if (character == null) return false;
        return false;
    }

    @Override
    protected int attackPriority(Card card, Card top) {
        if (card.isItemCard()) return super.attackPriority(card, top);
        int v = card.getValue();
        boolean bt = character != null && character.getCurrentHp() < 30;
        if (v == 1 && character != null && character.getCurrentHp() < character.getMaxHp() * 0.6) return 55;
        if (v == 2 && bt) return 60;
        if (v == 4 && bt) return 58;
        if (v == 6 && bt) return 65;
        if (v == 7 && bt) return 62;
        if (v == 0) return 45;
        return super.attackPriority(card, top);
    }

    @Override
    protected int defendPriority(Card card, Card top) {
        if (card.isItemCard()) return super.defendPriority(card, top);
        int v = card.getValue();
        boolean bt = character != null && character.getCurrentHp() < 30;
        if (v == 2 && opponent != null && opponent.getBleedStacks() >= 1) return 60;
        if (v == 3 && bt) return 58;
        if (v == 0) return 50;
        return super.defendPriority(card, top);
    }
}