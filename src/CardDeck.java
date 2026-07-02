import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CardDeck {
    private final List<Card> cards;

    public CardDeck() {
        cards = new ArrayList<>();
        initDeck();
        shuffle();
    }

    private void initDeck() {
        cards.clear();
        addCards(1, 12);
        addCards(2, 12);
        addCards(3, 12);
        addCards(4, 8);
        addCards(5, 8);
        addCards(6, 8);
        addCards(0, 4);
        addCards(7, 4);
        for (int i = 0; i < 2; i++) {
            cards.add(new Card(0, Card.CardColor.BLACK));
        }
        for (int i = 0; i < 2; i++) {
            cards.add(new Card(0, Card.CardColor.BLACK, true));
        }
        for (int v = 1; v <= 7; v++) {
            cards.add(new Card(v, Card.CardColor.WHITE));
        }
        for (int i = 0; i < 4; i++) {
            cards.add(new Card(0, Card.CardColor.WHITE, false, true));
        }
        for (int i = 0; i < 2; i++) {
            cards.add(new Card(0, Card.CardColor.WHITE, false, true, false));
        }
    }

    private void addCards(int value, int count) {
        Card.CardColor[] colors = {Card.CardColor.RED, Card.CardColor.YELLOW, Card.CardColor.BLUE, Card.CardColor.GREEN};
        int perColor = count / colors.length;
        for (Card.CardColor color : colors) {
            for (int i = 0; i < perColor; i++) {
                cards.add(new Card(value, color));
            }
        }
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    public Card draw() {
        if (cards.isEmpty()) {
            return null;
        }
        return cards.remove(0);
    }

    public List<Card> draw(int count) {
        List<Card> drawn = new ArrayList<>();
        for (int i = 0; i < count && !cards.isEmpty(); i++) {
            drawn.add(cards.remove(0));
        }
        return drawn;
    }

    public int remaining() {
        return cards.size();
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    public void addCards(List<Card> newCards) {
        cards.addAll(newCards);
    }

    public void reset() {
        initDeck();
        shuffle();
    }

    @Override
    public String toString() {
        return "牌堆剩余: " + cards.size() + " 张";
    }
}
