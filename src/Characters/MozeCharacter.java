import java.util.List;

public class MozeCharacter extends GameCharacter {

    public MozeCharacter() {
        this(false);
    }

    public MozeCharacter(boolean isAI) {
        super(isAI ? "AI Moze" : "Moze", 100);
    }

    @Override
    public void applyPassive() {
    }

    @Override
    public AttackResult resolveAttack(Card card, CardDeck deck, List<Card> hand,
                                       int handValueSum, List<Card> opponentHand) {
        AttackResult r = new AttackResult();
        int v = card.getValue();

        switch (v) {
            case 1:
                r.damage = 3;
                r.desc = "1️⃣ 3点[伤害]";
                break;
            case 2:
                r.damage = 2;
                r.addGuard = 1;
                if (card.getEffectiveColor() == Card.CardColor.GREEN) r.addGuard = 2;
                r.desc = "2️⃣ 2点[伤害]+1层[守护]" + (card.getEffectiveColor() == Card.CardColor.GREEN ? "（[绿]+1）" : "");
                break;
            case 3:
                r.damage = 1;
                r.selfHeal = 1;
                r.addGuard = 1;
                r.desc = "3️⃣ 1点[伤害]+1[生命]+1层[守护]";
                break;
            case 4:
                r.addFollowUp(FollowUp.MOZE_FOUR_GUARD);
                r.skipDefense = true;
                r.desc = "4️⃣ 打出1张数字牌，获得对应层数[守护]，弃掉该牌（跳过防御）";
                break;
            case 5:
                r.addFollowUp(FollowUp.MOZE_FIVE_REVEAL);
                r.desc = "5️⃣ 抽对手1牌判定";
                break;
            case 6:
                r.damage = 2 + guardStacks;
                r.damagePerGuard = 1;
                r.unblockableIfNoGuard = guardStacks == 0;
                r.desc = "6️⃣ " + (2 + guardStacks) + "点[伤害]" + (guardStacks == 0 ? "（不可防御）" : "");
                break;
            case 7:
                r.addFollowUp(FollowUp.MOZE_SEVEN_CLEANSE);
                r.desc = "7️⃣ 3点[伤害]+清除debuff+每层debuff+1[伤害]";
                break;
            case 0:
                r.addGuard = 3;
                r.damage = 5;
                r.drawCount = 1;
                r.desc = "0️⃣ 3层[守护]+5点[伤害]+抽1张[牌]";
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
            r.addGuard = 1;
            r.drawCount = -1;
            r.desc = "判定0/道具牌 → 加入手牌+1层[守护]";
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
                r.addGuard = 1;
                r.desc = "1️⃣ 防御½[伤害]（向上取整）+1层[守护]";
                break;
            case 2:
                r.counterPerGuard = 1;
                r.counterDmg = 1 + (guardStacks + 1) / 2;
                r.desc = "2️⃣ 反击" + r.counterDmg + "点[伤害]";
                break;
            case 3:
                r.addGuard = 1;
                r.selfHeal = (guardStacks + 2) / 2;
                r.desc = "3️⃣ 1层[守护]+恢复" + r.selfHeal + "[生命]（½×[守护]向上取整）";
                break;
            case 0:
                r.blocked = (incomingDamage + 1) / 2;
                r.addGuard = 2;
                r.counterPerGuard = 2;
                r.counterDmg = 2 * (guardStacks + 2);
                r.desc = "0️⃣ 防御½[伤害]+2层[守护]+反击" + r.counterDmg + "点[伤害]";
                break;
            default:
                r.desc = "";
                break;
        }
        return r;
    }
}