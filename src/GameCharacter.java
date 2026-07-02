import java.util.ArrayList;
import java.util.List;

public class GameCharacter {
    protected String name;
    protected int maxHp;
    protected int currentHp;
    protected int burnStacks;
    protected boolean frozen;

    public enum FollowUp {
        FIVE_CHOICE,

        REVEAL_TOP_DECK,
        REVEAL_DRAW,
        REVEAL_AND_JUDGE,
        CHAN_FOUR_SWAP,
        CHAN_FIVE_REORDER,
        CHAN_SEVEN_JUDGE,
        CHAN_SIX_REVEAL
    }

    public GameCharacter(String name, int maxHp) {
        this.name = name;
        this.maxHp = maxHp;
        this.currentHp = maxHp;
        this.burnStacks = 0;
        this.frozen = false;
    }

    public String getName() { return name; }
    public int getMaxHp() { return maxHp; }
    public int getCurrentHp() { return currentHp; }
    public double getHpPercent() { return (double) currentHp / maxHp; }
    public boolean isAlive() { return currentHp > 0; }

    public int getBurnStacks() { return burnStacks; }

    public void addBurn(int stacks) {
        burnStacks = Math.min(4, burnStacks + stacks);
    }

    public void removeBurn(int stacks) {
        burnStacks = Math.max(0, burnStacks - stacks);
    }

    public boolean isFrozen() { return frozen; }

    public void setFrozen(boolean frozen) { this.frozen = frozen; }

    public boolean hasDebuff() { return burnStacks > 0 || frozen; }

    public void clearAllDebuffs() {
        burnStacks = 0;
        frozen = false;
    }

    public void takeDamage(int amount) {
        currentHp = Math.max(0, currentHp - amount);
    }

    public void heal(int amount) {
        currentHp = Math.min(maxHp, currentHp + amount);
    }

    public void reset() {
        currentHp = maxHp;
        burnStacks = 0;
        frozen = false;
    }

    public void applyPassive() {
        heal(1);
    }

    public boolean isImmuneToBurn() {
        return false;
    }

    // ═══════════════════════════════════════
    //  Shared result types for subclasses
    // ═══════════════════════════════════════

    public static class AttackResult {
        public int damage = 0;
        public int selfHeal = 0;
        public int selfDamage = 0;
        public int drawCount = 0;
        public int revealDrawCount = 0;
        public boolean mayDiscardAny = false;
        public boolean greenAttack = false;
        public boolean skipDefense = false;
        public boolean recalcDamageAfterDraw = false;
        public int addBurn = 0;
        public boolean skipDefenseIfBurn = false;
        public int extraDamageIfBurn = 0;
        public boolean doubleDamageIfBurn = false;
        public int revealExtraDraw = 0;
        public int forceOpponentDiscard = 0;
        public boolean forceOpponentDiscardOne = false;
        public boolean clearSelfBuffs = false;
        public boolean discardRevealed = false;
        public boolean addFreeze = false;
        public boolean blueUnblockable = false;
        public List<FollowUp> followUps = new ArrayList<>();
        public String desc = "";

        public void addFollowUp(FollowUp fu) {
            followUps.add(fu);
        }

        public boolean hasFollowUp(FollowUp fu) {
            return followUps.contains(fu);
        }
    }

    public static class DefenseResult {
        public int blocked = 0;
        public int counterDmg = 0;
        public int selfHeal = 0;
        public boolean immuneTarget = false;
        public boolean immuneRed = false;
        public boolean immuneAll = false;
        public boolean healFromDamage = false;
        public int addBurn = 0;
        public boolean addFreeze = false;
        public boolean forceDiscardAll = false;
        public int counterFromDamage = 0;
        public int drawCount = 0;
        public boolean clearSelfBuffs = false;
        public boolean endAttackerTurn = false;
        public boolean revealTopDeck = false;
        public String desc = "";
    }

    // ═══════════════════════════════════════
    //  Override these in character subclasses
    // ═══════════════════════════════════════

    public AttackResult resolveAttack(Card card, CardDeck deck, java.util.List<Card> hand,
                                       int handValueSum, java.util.List<Card> opponentHand) {
        return new AttackResult(); // no-op base
    }

    public AttackResult resolveReveal(Card revealed, boolean isOwnTurn) {
        return new AttackResult();
    }

    public AttackResult resolveFive(Card secondCard, boolean chooseDamage) {
        return new AttackResult();
    }

    public DefenseResult resolveDefense(Card card, int incomingDamage, boolean isRed) {
        return new DefenseResult();
    }
}
