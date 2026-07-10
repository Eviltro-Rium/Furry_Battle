import java.util.ArrayList;
import java.util.List;

public class GameCharacter {
    protected String name;
    protected int maxHp;
    protected int currentHp;
    protected int burnStacks;
    protected boolean frozen;
    protected int bleedStacks;
    protected int guardStacks;

    public enum FollowUp {
        FIVE_CHOICE,

        REVEAL_TOP_DECK,
        REVEAL_DRAW,
        REVEAL_AND_JUDGE,
        CHAN_FOUR_SWAP,
        CHAN_FIVE_REORDER,
        CHAN_SEVEN_JUDGE,
        CHAN_SIX_REVEAL,
        SAIKI_THREE_DRAW,
        SAIKI_SIX_JUDGE,
        BLAZE_FOUR_DRAW,

        BLAZE_DEFEND_TWO_DRAW,

        SERENITY_FIVE_REVEAL,
        SERENITY_ZERO_DISCARD,
        SERENITY_DEFEND_TWO_DRAIN,
        SERENITY_DEFEND_ZERO_REVEAL,
        LEON_ZERO_AOE,
        MOZE_FOUR_GUARD,
        MOZE_FIVE_REVEAL,
        MOZE_SEVEN_CLEANSE
    }

    public GameCharacter(String name, int maxHp) {
        this.name = name;
        this.maxHp = maxHp;
        this.currentHp = maxHp;
        this.burnStacks = 0;
        this.frozen = false;
        this.bleedStacks = 0;
        this.guardStacks = 0;
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

    public int getBleedStacks() { return bleedStacks; }

    public void addBleed(int stacks) {
        bleedStacks = Math.min(2, bleedStacks + stacks);
    }

    public void removeBleed(int stacks) {
        bleedStacks = Math.max(0, bleedStacks - stacks);
    }

    public boolean hasDebuff() { return burnStacks > 0 || frozen || bleedStacks > 0; }

    public void clearAllDebuffs() {
        burnStacks = 0;
        frozen = false;
        bleedStacks = 0;
        guardStacks = 0;
    }

    public void clearDebuffsOnly() {
        burnStacks = 0;
        frozen = false;
        bleedStacks = 0;
    }

    public int getGuardStacks() { return guardStacks; }

    public void addGuard(int stacks) {
        guardStacks = Math.min(5, guardStacks + stacks);
    }

    public void removeGuard(int stacks) {
        guardStacks = Math.max(0, guardStacks - stacks);
    }

    public void takeDamage(int amount) {
        currentHp = Math.max(0, currentHp - amount);
    }

    public void heal(int amount) {
        currentHp = Math.min(maxHp, currentHp + amount);
    }

    public void setDoubleHp() {
        maxHp *= 2;
        currentHp = maxHp;
    }

    public void reset() {
        currentHp = maxHp;
        burnStacks = 0;
        frozen = false;
        bleedStacks = 0;
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
        public boolean skipDefenseIfBleed = false;
        public int extraDamageIfBurn = 0;
        public boolean doubleDamageIfBurn = false;
        public int damagePerBurn = 0;
        public double damagePerFieldBurn = 0;
        public int revealExtraDraw = 0;
        public int forceOpponentDiscard = 0;
        public boolean forceOpponentDiscardOne = false;
        public boolean clearSelfBuffs = false;
        public boolean discardRevealed = false;
        public boolean addFreeze = false;
        public boolean blueUnblockable = false;
        public boolean drawOpponentCard = false;
        public int addBleed = 0;
        public int passiveBleed = 0;
        public int addBleedSelf = 0;
        public int damagePerBleed = 0;
        public boolean healEqualsDamage = false;
        public int addGuard = 0;
        public int damagePerGuard = 0;
        public boolean unblockableIfNoGuard = false;
        public int cleanseDamagePerDebuff = 0;
        public boolean immuneDebuff = false;
        public boolean reflectDebuff = false;
        public int sharedDamage = 0;
        public int addBurnSelf = 0;
        public boolean passiveAfterSelfBurn = false;
        public boolean redAttack = false;

        public boolean skipDefenseIfZero = false;
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
        public int addBurnSelf = 0;
        public boolean addFreeze = false;
        public boolean forceDiscardAll = false;
        public int counterFromDamage = 0;
        public int drawCount = 0;
        public boolean clearSelfBuffs = false;
        public boolean endAttackerTurn = false;
        public boolean revealTopDeck = false;
        public int addBleed = 0;

        public boolean immuneDebuff = false;
        public int addGuard = 0;
        public int healPerGuard = 0;
        public int counterPerGuard = 0;
        public boolean reflectDebuff = false;
        public int sharedDamage = 0;
        public boolean discardRevealed = false;
        public int healAllBurnPlus = 0;
        public boolean counterDmgFromAttackerBurn = false;
        public boolean counterDmgFromFieldBurn = false;
        public List<FollowUp> followUps = new ArrayList<>();
        public String desc = "";

        public void addFollowUp(FollowUp fu) {
            followUps.add(fu);
        }
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
