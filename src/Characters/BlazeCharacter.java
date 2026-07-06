
public class BlazeCharacter extends GameCharacter {

    public BlazeCharacter() {
        this(false);
    }

    public BlazeCharacter(boolean isAI) {
        super(isAI ? "AI Blaze" : "Blaze", 85);
    }

    @Override

    public void applyPassive() {
    }

    @Override
    public AttackResult resolveAttack(Card card, CardDeck deck, java.util.List<Card> hand,
                                       int handValueSum, java.util.List<Card> opponentHand) {
        AttackResult r = new AttackResult();

        int v = card.getValue();
        int burnBonus = getBurnStacks() > 0 ? 1 : 0;

        switch (v) {
            case 1:
                r.damage = 4 + burnBonus;
                r.desc = "1️⃣ " + r.damage + "点伤" + (burnBonus > 0 ? "(灼烧+1)" : "");
                break;
            case 2:
                r.damage = 2 + burnBonus;
                r.addBurnSelf = 2;
                r.skipDefense = true;
                r.desc = "2️⃣ " + r.damage + "点伤(不可防御) → 自身灼烧2" + (burnBonus > 0 ? "(灼烧+1)" : "");
                break;
            case 3:
                r.damage = 3 + burnBonus;
                r.addBurn = 1;
                r.addBurnSelf = 1;
                r.desc = "3️⃣ " + r.damage + "点伤 + 双方灼烧1" + (burnBonus > 0 ? "(灼烧+1)" : "");
                break;
            case 4:
                r.addFollowUp(FollowUp.BLAZE_FOUR_DRAW);
                r.desc = "4️⃣ 抽对手1牌判定";
                break;
            case 5:
                int burn5 = getBurnStacks();
                r.addBurnSelf = 1;
                r.passiveAfterSelfBurn = true;
                r.damage = 2 * burn5;
                r.desc = "5️⃣ 自身灼烧1 → 2×" + burn5 + "点伤(含被动)";
                break;
            case 6:
                int burn6 = getBurnStacks();
                r.selfHeal = (int) Math.ceil(1.5 * burn6);
                r.clearSelfBuffs = true;
                r.addBurn = 1;
                r.skipDefense = true;
                r.desc = "6️⃣ 恢复" + r.selfHeal + "点命(1.5×灼烧" + burn6 + ") + 对手灼烧1 + 跳过防御";
                break;
            case 7:
                r.addBurn = 2;
                r.addBurnSelf = 2;
                r.damagePerFieldBurn = 1.5;
                r.passiveAfterSelfBurn = true;
                r.damage = 0;
                r.desc = "7️⃣ 双方灼烧2 + 1.5×场上灼烧伤(含被动+1)";
                break;
            case 0:
                r.damage = 6;
                r.skipDefense = true;
                r.addBurn = 2;
                r.desc = "0️⃣ 6点伤(不可防御) + �2";
                break;
            default:
                r.desc = "";
                break;
        }
        return r;
    }

    @Override
    public AttackResult resolveReveal(Card revealed, boolean isOwnTurn) {
        AttackResult r = new AttackResult();
        int v = revealed.getValue();
        if (v >= 1 && v <= 7 && !revealed.isBlack()) {
            r.damage = v;
            r.drawCount = -1;
            r.desc = "判定" + v + " → 造成" + v + "点伤害，加入手牌";
        } else {
            r.drawCount = -1;
            r.desc = "判定0/道具牌 → 加入手牌";
        }
        return r;
    }

    @Override
    public DefenseResult resolveDefense(Card card, int incomingDamage, boolean isRed) {
        DefenseResult r = new DefenseResult();

        int v = card.getValue();

        switch (v) {
            case 1:
                int burn1 = getBurnStacks();
                r.selfHeal = 2 + burn1;
                r.addBurn = 1;
                r.addBurnSelf = 1;
                r.desc = "1️⃣ 恢复" + r.selfHeal + "点命(2+灼烧" + burn1 + ") + 双方灼烧1";
                break;
            case 2:
                r.addFollowUp(FollowUp.BLAZE_DEFEND_TWO_DRAW);
                r.desc = "2️⃣ 抽1牌反击对应点数";
                break;
            case 3:
                r.addBurn = 2;
                r.counterDmgFromFieldBurn = true;
                r.desc = "3️⃣ +2灼 + 反击场上灼烧层数点伤";
                break;
            case 0:
                r.addBurn = 4;
                r.blocked = (int) Math.ceil(incomingDamage / 2.0);
                r.healAllBurnPlus = 3;
                r.desc = "0️⃣ +4灼 + 格挡½ + 恢复场上灼烧+3点命";
                break;
            default:
                r.desc = "";
                break;
        }
        return r;
    }
}