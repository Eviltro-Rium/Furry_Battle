public class Card {
    public enum CardColor { RED, YELLOW, BLUE, GREEN, BLACK, WHITE }

    private final int value;
    private final CardColor color;
    private CardColor chosenColor;
    private final boolean drawTwo;
    private final boolean drawThree;
    private final boolean potion;
    private final boolean purify;
    private final boolean superPurify;
    private final boolean swapHand;

    public Card(int value, CardColor color) {
        this(value, color, false, false, false, false, false, false);
    }

    public Card(int value, CardColor color, boolean drawTwo) {
        this(value, color, drawTwo, false, false, false, false, false);
    }

    public Card(int value, CardColor color, boolean drawTwo, boolean potion) {
        this(value, color, drawTwo, false, potion, false, false, false);
    }

    public Card(int value, CardColor color, boolean drawTwo, boolean drawThree, boolean potion) {
        this(value, color, drawTwo, drawThree, potion, false, false, false);
    }

    public Card(int value, CardColor color, boolean drawTwo, boolean drawThree, boolean potion, boolean purify) {
        this(value, color, drawTwo, drawThree, potion, purify, false, false);
    }

    public Card(int value, CardColor color, boolean drawTwo, boolean drawThree, boolean potion, boolean purify, boolean superPurify) {
        this(value, color, drawTwo, drawThree, potion, purify, superPurify, false);
    }

    public Card(int value, CardColor color, boolean drawTwo, boolean drawThree, boolean potion, boolean purify, boolean superPurify, boolean swapHand) {
        this.value = value;
        this.color = color;
        this.chosenColor = null;
        this.drawTwo = drawTwo;
        this.drawThree = drawThree;
        this.potion = potion;
        this.purify = purify;
        this.superPurify = superPurify;
        this.swapHand = swapHand;
    }

    public int getValue() {
        return value;
    }

    public CardColor getColor() {
        return color;
    }

    public boolean isBlack() {
        return color == CardColor.BLACK;
    }

    public boolean isWhite() {
        return color == CardColor.WHITE;
    }

    public boolean isDrawTwo() {
        return drawTwo;
    }

    public boolean isDrawThree() {
        return drawThree;
    }

    public boolean isPotion() {
        return potion;
    }

    public boolean isPurify() {
        return purify;
    }

    public boolean isSuperPurify() {
        return superPurify;
    }

    public boolean isSwapHand() {
        return swapHand;
    }

    public boolean isItemCard() {
        return isPotion() || isBlack() || isDrawThree() || isPurify() || isSuperPurify() || isSwapHand();
    }

    public boolean isNumberCard() {
        return !isItemCard();
    }

    public CardColor getChosenColor() {
        return chosenColor;
    }

    public void setChosenColor(CardColor chosenColor) {
        this.chosenColor = chosenColor;
    }

    public CardColor getEffectiveColor() {
        if (isBlack() && chosenColor != null) return chosenColor;
        if (isWhite() && chosenColor != null) return chosenColor;
        return color;
    }

    @Override
    public String toString() {
        if (isBlack()) {
            String chosen = chosenColor != null ? colorName(chosenColor) : "";
            String suffix = drawTwo ? "+2" : "";
            return "[黑" + suffix + chosen + "]";
        }
        if (isWhite()) {
            String chosen = chosenColor != null ? colorName(chosenColor) : "";
            String suffix;
            if (drawThree) suffix = "+3";
            else if (superPurify) suffix = "超净";
            else if (purify) suffix = "净化";
            else if (swapHand) suffix = "交换";
            else suffix = String.valueOf(value);
            return "[白" + suffix + chosen + "]";
        }
        return "[" + colorName(color) + value + "]";
    }

    private String colorName(CardColor c) {
        switch (c) {
            case RED:    return "红";
            case YELLOW: return "黄";
            case BLUE:   return "蓝";
            case GREEN:  return "绿";
            default:     return "";
        }
    }
}
