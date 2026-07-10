import java.util.List;

public interface GameMode {
    int playerHandLimit();
    int aiHandLimit();
    int aiCount();
    List<Participant> createParticipants(Game game, int playerChoice, int aiChoice, int ai2Choice);
    Participant chooseAttackTarget(Game game, Participant attacker, List<Participant> all);
    boolean checkPlayerWin(List<Participant> all);
    boolean checkAIWin(List<Participant> player);
    String modeName();
    GameUI createUI();
}