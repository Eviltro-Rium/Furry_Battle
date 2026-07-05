
public class RyanCharacter extends GameCharacter {

    public RyanCharacter() {
        this(false);
    }

    public RyanCharacter(boolean isAI) {
        super(isAI ? "🐼 AI Ryan" : "🐼 Ryan", 70);
    }

    @Override
    public AttackResult resolveAttack(Card card, CardDeck deck, java.util.List<Card> hand,
                                       int handValueSum, java.util.List<Card> opponentHand) {
        AttackResult r = new AttackResult();


        int v = card.getValue();

        switch (v) {
            case 1:
                r.damage = 4;
                r.desc = "1️⃣ 造成4点伤害";
                break;
            case 2:
                r.selfHeal = 1;
                r.damage = 3;
                r.desc = "2️⃣ 恢复1点生命，造成3点伤害";
                break;
            case 3:
                r.selfHeal = 1;
                r.drawCount = 1;
                r.mayDiscardAny = true;
                r.skipDefense = true;
                r.desc = "3️⃣ 恢复1点生命，抽1张牌，可选择弃牌";
                break;
            case 4:
                r.addFollowUp(FollowUp.REVEAL_TOP_DECK);
                r.desc = "4️⃣ 翻开牌库顶牌";
                break;
            case 5:
                r.addFollowUp(FollowUp.FIVE_CHOICE);
                r.desc = "5️⃣ 再打一张牌，恢复对应数字1½倍生命或造成1½倍伤害";
                break;
            case 6:
                r.damage = 4;
                r.drawCount = 1;
                r.mayDiscardAny = true;
                r.clearSelfBuffs = true;
                r.desc = "6️⃣ 造成4点伤害，清除debuff，抽1张牌";
                break;
            case 7:
                r.drawCount = 1;
                r.damage = (int) Math.ceil(handValueSum / 2.0);
                r.mayDiscardAny = true;
                r.recalcDamageAfterDraw = true;
                r.desc = "7️⃣ 抽1张牌，造成手牌数字之和1/2🗡️（向上取整）";
                break;
            case 0:
                r.selfHeal = 4;
                r.drawCount = 0;
                r.damage = 0;
                r.greenAttack = true;
                r.addFollowUp(FollowUp.REVEAL_DRAW);
                r.revealDrawCount = 2;
                r.clearSelfBuffs = true;
                r.desc = "0️⃣ 恢复4点生命，清除debuff，抽2张公示";
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
        if (revealed.isBlack() || revealed.getColor() == Card.CardColor.GREEN || revealed.isWhite()) {
            r.selfHeal = 1;
            r.damage = 4;
            r.drawCount = -1;
            r.desc = "判定🟢/⚪/⚫ → 恢复1❤️ + 造成4🗡️，加入手牌";
        } else {
            r.drawCount = -1;
            r.desc = "判定其他牌 → 加入手牌";
        }
        return r;
    }

    @Override
    public AttackResult resolveFive(Card secondCard, boolean chooseDamage) {
        AttackResult r = new AttackResult();
        int v = secondCard.getValue();
        if (chooseDamage) {
            r.damage = (int) Math.ceil(v * 1.5);
            r.desc = "5️⃣ 再打" + v + " → 造成" + r.damage + "点伤害(1½倍)";
        } else {
            r.selfHeal = v;
            r.skipDefense = true;
            r.desc = "5️⃣ 再打" + v + " → 恢复" + r.selfHeal + "点生命";
        }
        return r;
    }

    @Override
    public DefenseResult resolveDefense(Card card, int incomingDamage, boolean isRed) {
        DefenseResult r = new DefenseResult();

        int v = card.getValue();

        switch (v) {
            case 1:
                r.blocked = (incomingDamage + 1) / 2;
                r.desc = "1️⃣ 格挡" + r.blocked + "点伤害";
                break;
            case 2:
                r.counterDmg = 2;
                r.selfHeal = 2;
                r.desc = "2️⃣ 反击2点 + 恢复2点生命";
                break;
            case 3:
                if (isRed) {
                    r.immuneAll = true;
                    r.blocked = incomingDamage;
                    r.desc = "3️⃣ 无视红色攻击伤害";
                } else {
                    r.selfHeal = 3;
                    r.desc = "3️⃣ 恢复3点生命";
                }
                break;
            case 0:
                r.immuneAll = true;
                r.selfHeal = 3;
                r.clearSelfBuffs = true;
                r.desc = "0️⃣ 免疫伤害 + 恢复3点 + 清除debuff";
                break;
            default:
                r.desc = "";
                break;
        }
        return r;
    }
}
