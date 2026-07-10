import java.util.ArrayList;
import java.util.List;

public class Mode1v1 implements GameMode {
    public int playerHandLimit() { return 5; }
    public int aiHandLimit() { return 5; }
    public int aiCount() { return 1; }
    public String modeName() { return "1v1"; }

    public List<Participant> createParticipants(Game game, int playerChoice, int aiChoice, int ai2Choice) {
        String[] charNames = {"Ryan", "Leon", "Chan", "Saiki", "Blaze", "Serenity", "Moze"};
        List<Participant> participants = new ArrayList<>();
        GameCharacter playerChar = game.createCharacter(charNames[playerChoice], false);
        participants.add(Participant.createHuman(playerChar, playerHandLimit()));

        GameCharacter aiChar = game.createCharacter(charNames[aiChoice], true);
        AIPlayer aiPlayer = game.createAI(charNames[aiChoice], aiChar);
        game.setupAIOpponent(aiPlayer, playerChar);
        participants.add(Participant.createAI(aiChar, aiPlayer, aiHandLimit()));

        return participants;
    }

    public Participant chooseAttackTarget(Game game, Participant attacker, List<Participant> all) {
        for (Participant p : all) {
            if (p != attacker && p.isAlive()) return p;
        }
        return null;
    }

    public boolean checkPlayerWin(List<Participant> all) {
        for (Participant p : all) {
            if (p.isAI() && p.isAlive()) return false;
        }
        return true;
    }

    public boolean checkAIWin(List<Participant> players) {
        for (Participant p : players) {
            if (p.isHuman && p.isAlive()) return false;
        }
        return true;
    }

    public GameUI createUI() {
        return new GameUI();
    }
}