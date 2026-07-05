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

    void handleChanFourSwap(GameCharacter self, GameCharacter opponent,
                             List<Card> selfHand, List<Card> oppHand, Runnable onDone) {}

    void handleChanFiveReorder(GameCharacter self, GameCharacter opponent,
                                List<Card> selfHand, Runnable onDone) {}

    void handleChanSevenJudge(GameCharacter self, GameCharacter opponent,
                               List<Card> oppHand, Runnable onDone) {}

    void handleChanThreeDefendReveal(GameCharacter self, GameCharacter opponent,
                                      List<Card> selfHand, Runnable onDone) {}

    void handleChanSixReveal(GameCharacter self, GameCharacter opponent,
                              List<Card> selfHand, Runnable onDone) {}

    void handleSaikiThreeDraw(GameCharacter self, GameCharacter opponent,
                               List<Card> selfHand, List<Card> oppHand, Runnable onDone) {}

    void handleSaikiSixJudge(GameCharacter self, GameCharacter opponent,
                              List<Card> selfHand, Runnable onDone) {}

    void handleSaikiSevenAttack(GameCharacter self, GameCharacter opponent,
                                  List<Card> selfHand, Runnable onDone) {}

    void handleSaikiZeroAttack(GameCharacter self, GameCharacter opponent,
                                 List<Card> selfHand, Runnable onDone) {}

    void handleSaikiThreeDefendReveal(GameCharacter self, GameCharacter opponent,
                                        List<Card> selfHand, Runnable onDone) {}
}