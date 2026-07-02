
public class LeonCharacter extends GameCharacter {

    public LeonCharacter() {
        this(false);
    }

    public LeonCharacter(boolean isAI) {
        super(isAI ? "🐻‍❄️ AI Leon" : "🐻‍❄️ Leon", 100);
    }

    @Override
    public boolean isImmuneToBurn() {
        return true;
    }

    @Override
    public void applyPassive() {
    }

    @Override
    public AttackResult resolveAttack(Card card, CardDeck deck, java.util.List<Card> hand,
                                       int handValueSum, java.util.List<Card> opponentHand) {
        AttackResult r = new AttackResult();

        if (card.isBlack()) {
            if (card.isDrawTwo()) {
                r.desc = "⚫+2 抽2张牌";
            } else {
                r.desc = "⚫ 改变颜色";
            }
            return r;
        }


        int v = card.getValue();

        switch (v) {
            case 1:
                r.addBurn = 2;
                r.skipDefense = true;
                r.desc = "1️⃣ 施加2层灼伤（跳过防御）";
                break;
            case 2:
                r.damage = 4;
                r.desc = "2️⃣ 造成4点伤害";
                break;
            case 3:
                r.damage = 3;
                r.addBurn = 1;
                r.desc = "3️⃣ 造成3点伤害 + 施加1层灼伤";
                break;
            case 4:
                r.damage = 5;
                r.skipDefenseIfBurn = true;
                r.desc = "4️⃣ 造成5点伤害（目标有灼伤则跳过防御）";
                break;
            case 5:
                r.damage = 4;
                r.extraDamageIfBurn = 2;
                r.desc = "5️⃣ 造成4点伤害（目标有灼伤+2点）";
                break;
            case 6:
                r.addFollowUp(FollowUp.REVEAL_AND_JUDGE);
                r.desc = "6️⃣ 抽1张公示牌判定";
                break;
            case 7:
                r.damage = 6;
                r.addBurn = 2;
                r.forceOpponentDiscardOne = true;
                r.desc = "7️⃣ 弃掉对手1张牌 + 造成6点伤害 + 施加2层灼伤";
                break;
            case 0:
                r.damage = 7;
                r.addBurn = 1;
                r.forceOpponentDiscard = 2;
                r.selfDamage = 2;
                r.skipDefense = true;
                r.desc = "0️⃣ 敌方弃2牌 + 施加1层灼伤 + 7点伤害（跳过防御），自伤2点";
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
            r.addBurn = 2;
            r.skipDefense = true;
            r.drawCount = -1;
            r.revealExtraDraw = 1;
            r.desc = "判定0/道具牌 → 加入手牌 + 抽1张 + 施加2层灼伤（跳过防御）";
        }
        return r;
    }

    @Override
    public DefenseResult resolveDefense(Card card, int incomingDamage, boolean isRed) {
        DefenseResult r = new DefenseResult();
        int v = card.getValue();

        switch (v) {
            case 1:
                r.addBurn = 1;
                r.selfHeal = 2;
                r.desc = "1️⃣ 施加1层灼伤给攻击方 + 恢复2点生命";
                break;
            case 2:
                r.counterFromDamage = (incomingDamage + 1) / 2;
                r.drawCount = 1;
                r.desc = "2️⃣ 反击" + r.counterFromDamage + "点伤害 + 抽1张牌";
                break;
            case 3:
                r.blocked = (incomingDamage + 1) / 2;
                r.drawCount = 1;
                r.desc = "3️⃣ 格挡" + r.blocked + "点伤害 + 抽1张牌";
                break;
            case 0:
                r.forceDiscardAll = true;
                r.counterDmg = incomingDamage;
                r.desc = "0️⃣ 攻击方弃所有牌 + 双方各受" + r.counterDmg + "点伤害";
                break;
            default:
                r.desc = "";
                break;
        }
        return r;
    }
}