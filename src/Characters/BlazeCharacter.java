
public class BlazeCharacter extends GameCharacter {

    public BlazeCharacter() {
        this(false);
    }

    public BlazeCharacter(boolean isAI) {
        super(isAI ? "🐶 AI Blaze" : "🐶 Blaze", 75);
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
                r.addBurn = 1;
                r.desc = "1️⃣ " + r.damage + "点🗡️ + 🔥1" + (burnBonus > 0 ? "(灼烧+1)" : "");
                break;
            case 2:
                r.addBurnSelf = 2;
                r.damage = 2 + burnBonus;
                r.desc = "2️⃣ 自身🔥2 → " + r.damage + "点🗡️" + (burnBonus > 0 ? "(灼烧+1)" : "");
                break;
            case 3:
                int burnRemoved = getBurnStacks();
                r.selfHeal = 1 + burnRemoved;
                r.clearSelfBuffs = true;
                r.addBurn = 1;
                r.desc = "3️⃣ 移除" + burnRemoved + "层灼伤 → 恢复" + r.selfHeal + "点 + 🔥1";
                break;
            case 4:
                r.addFollowUp(FollowUp.BLAZE_FOUR_DRAW);
                r.desc = "4️⃣ 抽对手1牌判定";
                break;
            case 5:
                int burnRemoved5 = getBurnStacks();
                r.clearSelfBuffs = true;
                r.damage = burnRemoved5 * 2;
                r.desc = "5️⃣ 移除" + burnRemoved5 + "层灼伤 → " + r.damage + "点🗡️(无被动)";
                break;
            case 6:
                r.damage = 1 + burnBonus;
                r.damagePerBurn = 2;
                r.desc = "6️⃣ 1+2×灼伤伤害";
                break;
            case 7:
                r.addFollowUp(FollowUp.BLAZE_SEVEN_PLAY);
                r.skipDefense = true;
                r.desc = "7️⃣ 打1牌×1.5伤害(不可防御)";
                break;
            case 0:
                r.damage = 6;
                r.skipDefense = true;
                r.redAttack = true;
                r.desc = "0️⃣ 6点🗡️(不可防御,🔴属性)";
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
                r.selfHeal = getBurnStacks();
                r.desc = "1️⃣ 恢复" + r.selfHeal + "点(=灼伤层数)";
                break;
            case 2:
                r.addFollowUp(FollowUp.BLAZE_DEFEND_TWO_DRAW);
                r.desc = "2️⃣ 抽1牌反击对应点数";
                break;
            case 3:
                r.addBurn = 2;
                r.counterDmgFromAttackerBurn = true;
                r.desc = "3️⃣ +2层灼伤 + 反击攻击方灼伤层数点";
                break;
            case 0:
                r.addBurn = 4;
                r.healAllBurnPlus = 3;
                r.desc = "0️⃣ +4层灼伤 + 恢复场上所有灼烧+3点";
                break;
            default:
                r.desc = "";
                break;
        }
        return r;
    }
}