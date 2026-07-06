import java.util.List;

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
        if (character == null) return false;
        int v = card.getValue();
        if (v == 5) return false;
        if (v == 6 && character.getBurnStacks() == 0) return true;
        return false;
    }

    @Override
    protected int attackPriority(Card card, Card top) {
        if (card.isItemCard()) return super.attackPriority(card, top);
        int v = card.getValue();
        if (v == 2 && character != null && character.getBurnStacks() == 0) return 60;
        if (v == 7 && character != null && (character.getBurnStacks() + (opponent != null ? opponent.getBurnStacks() : 0)) >= 3) return 65;
        if (v == 5 && character != null && character.getBurnStacks() >= 2) return 55;
        if (v == 0) return 45;
        if (v == 6 && character != null && character.getBurnStacks() >= 2) return 50;
        return super.attackPriority(card, top);
    }

    @Override
    protected int defendPriority(Card card, Card top) {
        if (card.isItemCard()) return super.defendPriority(card, top);
        int v = card.getValue();
        if (v == 3 && character != null && character.getBurnStacks() >= 2) return 65;
        if (v == 0 && character != null && character.getBurnStacks() <= 1) return 70;
        if (v == 1 && character != null && character.getBurnStacks() >= 1) return 55;
        return super.defendPriority(card, top);
    }
}
