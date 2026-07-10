import java.util.List;

public class SerenityCharacter extends GameCharacter {

    private boolean bloodthirst = false;

    public SerenityCharacter() {
        this(false);
    }

    public SerenityCharacter(boolean isAI) {
        super(isAI ? "AI Serenity" : "Serenity", 80);
    }

    public boolean isBloodthirst() {
        return currentHp < 30;
    }

    @Override
    public void setFrozen(boolean frozen) {
        // Serenity is immune to freeze
    }

    @Override
    public void heal(int amount) {
        int bonus = (!isBloodthirst() && amount > 0) ? 1 : 0;
        currentHp = Math.min(maxHp, currentHp + amount + bonus);
    }

    @Override
    public void applyPassive() {
    }

    @Override
    public AttackResult resolveAttack(Card card, CardDeck deck, List<Card> hand,
                                       int handValueSum, List<Card> opponentHand) {
        AttackResult r = new AttackResult();
        int v = card.getValue();
        boolean bt = isBloodthirst();

        switch (v) {
            case 1:
                r.damage = 3;
                if (bt) r.skipDefense = true;
                r.desc = bt ? "1 3点伤(嗜血跳防)" : "1 3点伤";
                break;
            case 2:
                r.damage = bt ? 5 : 3;
                r.desc = bt ? "2 5点伤(嗜血)" : "2 3点伤";
                break;
            case 3:
                r.damage = 2;
                r.selfHeal = 2;
                r.desc = "3 2点伤+恢复2";
                break;
            case 4:
                r.damage = 5;
                if (bt) r.addBleed = 1;
                r.desc = bt ? "4 1层血+5点伤(嗜血)" : "4 5点伤";
                break;
            case 5:
                r.addFollowUp(FollowUp.SERENITY_FIVE_REVEAL);
                r.desc = "5 翻牌判定";
                break;
            case 6:
                r.damage = 6;
                if (bt) r.skipDefense = true;
                r.desc = bt ? "6 6点伤(不可防御,嗜血)" : "6 6点伤";
                break;
            case 7:
                r.damage = 5;
                r.skipDefense = true;
                if (bt) {
                    r.selfDamage = 0;
                } else {
                    r.selfDamage = 2;
                }
                r.desc = bt ? "7 5点伤(不可防御,嗜血无自伤)" : "7 自伤2+5点伤(不可防御)";
                break;
            case 0:
                r.addFollowUp(FollowUp.SERENITY_ZERO_DISCARD);
                r.skipDefense = true;
                r.desc = "0 弃手牌恢复+重抽4";
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
        boolean bt = isBloodthirst();

        switch (v) {
            case 1:
                r.blocked = Math.min(3, incomingDamage);
                if (bt) r.healFromDamage = true;
                r.desc = bt ? "1 防御3+恢复防御点(嗜血)" : "1 防御至多3点";
                break;
            case 2:
                r.addBleed = 1;
                r.addFollowUp(FollowUp.SERENITY_DEFEND_TWO_DRAIN);
                r.desc = "2 施加1层血+吸血";
                break;
            case 3:
                r.blocked = (int) Math.ceil(incomingDamage / 2.0);
                if (bt) r.blocked = Math.min(incomingDamage, r.blocked + 2);
                r.desc = bt ? "3 格挡半+2(嗜血)" : "3 格挡半";
                break;
            case 0:
                r.immuneAll = true;
                r.immuneDebuff = true;
                r.addFollowUp(FollowUp.SERENITY_DEFEND_ZERO_REVEAL);
                r.desc = "0 翻牌判定(免疫)";
                break;
            default:
                r.desc = "";
                break;
        }
        return r;
    }
}