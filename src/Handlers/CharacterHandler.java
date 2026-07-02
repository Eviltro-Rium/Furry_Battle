import java.util.List;

public abstract class CharacterHandler {

    protected Game game;

    CharacterHandler(Game game) {
        this.game = game;
    }

    void handleRevealTopDeck(GameCharacter self, GameCharacter opponent, List<Card> selfHand) {}

    void handleAIRevealTopDeck(GameCharacter self, GameCharacter opponent, List<Card> selfHand) {}

    void handleRevealDraw(GameCharacter self, GameCharacter opponent, List<Card> selfHand, boolean isPlayer) {}

    void handleRevealAndJudge(GameCharacter self, GameCharacter opponent, List<Card> selfHand, Runnable onDone) {}

    void doSevenChoice(int aiCardIndex) {}

    void doFiveChoiceHeal() {}

    void doFiveChoiceDamage() {}

    void resolveAIFiveChoice(GameCharacter self, GameCharacter opponent, List<Card> selfHand, Runnable onDone) {}
}