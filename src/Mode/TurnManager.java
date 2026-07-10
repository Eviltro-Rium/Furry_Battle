import java.util.ArrayList;
import java.util.List;

public class TurnManager {
    private final List<Participant> participants;
    private int currentIndex;

    TurnManager(List<Participant> participants) {
        this.participants = participants;
        this.currentIndex = 0;
    }

    Participant getCurrent() {
        return participants.get(currentIndex);
    }

    boolean isPlayerTurn() {
        return getCurrent().isHuman;
    }

    Participant advance() {
        int start = currentIndex;
        do {
            currentIndex = (currentIndex + 1) % participants.size();
        } while (!getCurrent().isAlive() && currentIndex != start);
        return getCurrent();
    }

    void resetToPlayer() {
        for (int i = 0; i < participants.size(); i++) {
            if (participants.get(i).isHuman) {
                currentIndex = i;
                return;
            }
        }
        currentIndex = 0;
    }

    void setToFirstAliveAI() {
        for (int i = 0; i < participants.size(); i++) {
            if (participants.get(i).isAI() && participants.get(i).isAlive()) {
                currentIndex = i;
                return;
            }
        }
    }

    List<Participant> getAllParticipants() { return participants; }

    Participant getHuman() {
        for (Participant p : participants) {
            if (p.isHuman) return p;
        }
        return null;
    }

    List<Participant> getAliveOpponents(Participant self) {
        List<Participant> result = new ArrayList<>();
        for (Participant p : participants) {
            if (p != self && p.isAlive()) result.add(p);
        }
        return result;
    }

    List<Participant> getAliveAIs() {
        List<Participant> result = new ArrayList<>();
        for (Participant p : participants) {
            if (p.isAI() && p.isAlive()) result.add(p);
        }
        return result;
    }

    boolean anyAIAlive() {
        for (Participant p : participants) {
            if (p.isAI() && p.isAlive()) return true;
        }
        return false;
    }

    boolean allAIDead() { return !anyAIAlive(); }

    boolean playerAlive() {
        Participant h = getHuman();
        return h != null && h.isAlive();
    }
}