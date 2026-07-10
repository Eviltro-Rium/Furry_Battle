import java.util.ArrayList;
import java.util.List;

public class Mode1v2 implements GameMode {
    public int playerHandLimit() { return 8; }
    public int aiHandLimit() { return 5; }
    public int aiCount() { return 2; }
    public String modeName() { return "1v2"; }

    public List<Participant> createParticipants(Game game, int playerChoice, int aiChoice, int ai2Choice) {
        String[] charNames = {"Ryan", "Leon", "Chan", "Saiki", "Blaze", "Serenity", "Moze"};
        List<Participant> participants = new ArrayList<>();
        GameCharacter playerChar = game.createCharacter(charNames[playerChoice], false);

        participants.add(Participant.createHuman(playerChar, playerHandLimit()));

        GameCharacter ai1Char = game.createCharacter(charNames[aiChoice], true);
        AIPlayer ai1 = game.createAI(charNames[aiChoice], ai1Char);
        game.setupAIOpponent(ai1, playerChar);
        participants.add(Participant.createAI(ai1Char, ai1, aiHandLimit()));

        if (ai2Choice >= 0) {
            GameCharacter ai2Char = game.createCharacter(charNames[ai2Choice], true);
            ai2Char.name = "AI2 " + charNames[ai2Choice];
            AIPlayer ai2 = game.createAI(charNames[ai2Choice], ai2Char);
            game.setupAIOpponent(ai2, playerChar);
            participants.add(Participant.createAI(ai2Char, ai2, aiHandLimit()));
        }

        return participants;
    }

    public Participant chooseAttackTarget(Game game, Participant attacker, List<Participant> all) {
        if (attacker.isHuman) {
            for (Participant p : all) {
                if (p != attacker && p.isAlive()) return p;
            }
            return null;
        } else {
            List<Participant> targets = new ArrayList<>();
            for (Participant p : all) {
                if (p != attacker && p.isAlive() && p.isHuman) targets.add(p);
            }
            return targets.isEmpty() ? null : targets.get(0);
        }
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
        return new GameUI1v2();
    }
}