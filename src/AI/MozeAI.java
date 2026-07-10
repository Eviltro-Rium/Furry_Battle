public class MozeAI extends AIPlayer {

    private GameCharacter character;

    public MozeAI(GameCharacter character) {
        this.character = character;
    }

    @Override
    protected boolean shouldSkipCard(Card card) {
        if (card.isItemCard()) return false;
        int v = card.getValue();
        if (v == 6 && aiGuardStacks == 0) return true;
        return false;
    }

    @Override
    protected int attackPriority(Card card, Card top) {
        if (card.isItemCard()) return super.attackPriority(card, top);
        int v = card.getValue();
        if (v == 0) return 82;
        if (v == 6 && aiGuardStacks >= 3) return 75;
        if (v == 7 && aiHasDebuff && aiDebuffCount >= 2) return 70;
        if (v == 2 && card.getColor() == Card.CardColor.GREEN && aiGuardStacks < 3) return 68;
        if (v == 4 && aiGuardStacks == 0) return 65;
        if (v == 3 && aiGuardStacks < 2) return 60;
        if (v == 1) return 45;
        if (v == 5) return 50;
        if (v == 6 && aiGuardStacks >= 1) return 55;
        if (v == 7) return 48;
        if (v == 2) return 42;
        return super.attackPriority(card, top);
    }

    @Override
    protected int defendPriority(Card card, Card top) {
        if (card.isItemCard()) return super.defendPriority(card, top);
        int v = card.getValue();
        if (v == 0) return 85;
        if (v == 3 && aiGuardStacks >= 1) return 68;
        if (v == 2 && aiGuardStacks >= 1) return 62;
        if (v == 1) return 50;
        return super.defendPriority(card, top);
    }

    int chooseGuardUse(int guardStacks, int incomingDamage, int currentHp) {
        if (incomingDamage >= currentHp) return Math.min(guardStacks, incomingDamage);
        if (incomingDamage <= 2) return 0;
        if (guardStacks >= 3 && incomingDamage >= 5) return Math.min(guardStacks, incomingDamage);
        if (guardStacks >= 2 && incomingDamage >= 4) return Math.min(guardStacks, incomingDamage);
        if (currentHp <= 30) return Math.min(guardStacks, incomingDamage);
        return 0;
    }
}
