import java.util.ArrayList;
import java.util.List;

public class Participant {
    final GameCharacter character;
    final AIPlayer ai;
    final List<Card> hand;
    final boolean isHuman;
    int handLimit;

    Participant(GameCharacter character, AIPlayer ai, List<Card> hand, boolean isHuman, int handLimit) {
        this.character = character;
        this.ai = ai;
        this.hand = hand;
        this.isHuman = isHuman;
        this.handLimit = handLimit;
    }

    static Participant createHuman(GameCharacter character, int handLimit) {
        return new Participant(character, null, new ArrayList<>(), true, handLimit);
    }

    static Participant createAI(GameCharacter character, AIPlayer ai, int handLimit) {
        return new Participant(character, ai, new ArrayList<>(), false, handLimit);
    }

    boolean isAlive() { return character.isAlive(); }

    boolean isAI() { return !isHuman; }

    AIPlayer getAI() { return ai; }

    int handSize() { return hand.size(); }

    void addCards(List<Card> cards) { hand.addAll(cards); }

    void removeCard(Card card) { hand.remove(card); }

    void clearHand() { hand.clear(); }
}