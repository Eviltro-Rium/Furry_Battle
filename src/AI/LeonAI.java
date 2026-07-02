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
        if (character == null) return false;
        if (card.isPotion()) return false;
        int v = card.getValue();
        if (character.getCurrentHp() >= character.getMaxHp() && v == 2) return true;
        if (v == 1 && opponent != null && opponent.getBurnStacks() >= 4) return true;
        return false;
    }

    @Override
    public Card chooseDefend(Card top, boolean excludePotion) {
        Card zeroCard = null;
        Card colorMatch = null;
        Card numberMatch = null;
        Card blackCard = null;
        Card whiteCard = null;

        Card.CardColor effective = top.getEffectiveColor();

        for (Card c : hand) {
            if (!isDefendCard(c, top)) continue;
            if (c.isWhite() && c.getValue() == 0 && !c.isPotion() && !c.isDrawThree()) {
                if (zeroCard == null) zeroCard = c;
                continue;
            }
            if (c.isBlack()) {
                if (blackCard == null) blackCard = c;
                continue;
            }
            if (c.isWhite()) {
                if (whiteCard == null) whiteCard = c;
                continue;
            }
            if (c.getColor() == effective && colorMatch == null) {
                colorMatch = c;
            }
            if (c.getValue() == top.getValue() && numberMatch == null) {
                numberMatch = c;
            }
        }

        if (zeroCard != null && character != null && character.getCurrentHp() <= character.getMaxHp() / 2) {
            return zeroCard;
        }
        if (colorMatch != null) return colorMatch;
        if (numberMatch != null) return numberMatch;
        if (zeroCard != null) return zeroCard;
        if (whiteCard != null) return whiteCard;
        if (blackCard != null) {
            Card.CardColor chosen = chooseBlackColorForDefend(top);
            blackCard.setChosenColor(chosen);
        }
        return blackCard;
    }

    @Override
    public boolean chooseFiveDamage(int aiHp, int maxHp, int opponentHp) {
        return true;
    }
}