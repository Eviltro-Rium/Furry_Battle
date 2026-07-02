public class RyanAI extends AIPlayer {

    private GameCharacter character;

    public RyanAI(GameCharacter character) {
        this.character = character;
    }

    @Override
    protected boolean shouldSkipCard(Card card) {
        if (character == null) return false;
        if (character.getCurrentHp() >= character.getMaxHp()) {
            return card.getValue() == 2 || card.getValue() == 6;
        }
        return false;
    }

    @Override
    public boolean chooseFiveDamage(int aiHp, int maxHp, int opponentHp) {
        return aiHp >= maxHp || aiHp >= opponentHp;
    }
}
