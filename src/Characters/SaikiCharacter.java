import java.util.List;

public class SaikiCharacter extends GameCharacter {

    public SaikiCharacter() {
        super("Saiki", 80);
    }

    @Override
    public void applyPassive() {
        // passive handled in Game when yellow card played
    }

    @Override
    public AttackResult resolveAttack(Card card, CardDeck deck, List<Card> hand,
                                       int handValueSum, List<Card> opponentHand) {
        AttackResult r = new AttackResult();

        int v = card.getValue();

        switch (v) {
            case 1:
                r.damage = 4;
                r.desc = "1️⃣ 4点伤害";
                break;
            case 2:
                r.damage = 3;
                r.selfHeal = 1;
                r.desc = "2️⃣ 3点伤害+恢复1";
                break;
            case 3:
                r.damage = 2;
                r.addFollowUp(FollowUp.SAIKI_THREE_DRAW);
                r.desc = "3️⃣ 2点伤害+抽对手1牌";
                break;
            case 4:
                r.damage = 5;
                r.skipDefenseIfBleed = true;
                r.desc = "4️⃣ 5点伤害（有流血不可防御）";
                break;
            case 5:
                if (currentHp <= 20) {
                    r.selfHeal = 4;
                    r.skipDefense = true;
                    r.desc = "5️⃣ HP≤20 → 恢复4（跳过防御）";
                } else if (currentHp <= 50) {
                    r.damage = 4;
                    r.skipDefense = true;
                    r.desc = "5️⃣ 20<HP≤50 → 4点伤害（跳过防御）";
                } else {
                    r.damage = 4;
                    r.drawOpponentCard = true;
                    r.desc = "5️⃣ HP>50 → 4点伤害+抽对手1牌";
                }
                break;
            case 6:
                r.addFollowUp(FollowUp.SAIKI_SIX_JUDGE);
                r.desc = "6️⃣ 判定数字牌";
                break;
            case 7:
                r.damage = 2;
                r.damagePerBleed = 2;
                r.healEqualsDamage = true;
                r.desc = "7️⃣ 2+2×血伤+恢复等量命";
                break;
            case 0:
                r.addBleed = 1;

                r.desc = "0️⃣ 流血+伤害+恢复";
                break;
        }

        if (card.getEffectiveColor() == Card.CardColor.YELLOW) {
            r.passiveBleed = 1;
            if (v != 4 && v != 0) {
                r.desc += " ([被动]+1流血)";
            }
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
                r.blocked = Math.min(3, incomingDamage);
                r.desc = "1️⃣ 防御至多3点";
                break;
            case 2:
                r.counterDmg = 3;
                r.addBleed = 1;
                r.desc = "2️⃣ 反击3+1层流血";
                break;
            case 3:
                r.revealTopDeck = true;
                r.desc = "3️⃣ 翻牌判定";
                break;
            case 0:
                r.immuneAll = true;
                r.immuneDebuff = true;
                r.reflectDebuff = true;
                r.sharedDamage = (int) Math.ceil(incomingDamage / 2.0);
                r.desc = "0️⃣ 免疫+均摊+反弹debuff";
                break;
        }
        return r;
    }
}