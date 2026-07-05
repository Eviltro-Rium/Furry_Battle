
public class ChanCharacter extends GameCharacter {

    public ChanCharacter() {
        this(false);
    }

    public ChanCharacter(boolean isAI) {
        super(isAI ? "🔵 AI Chan" : "🔵 Chan", 80);
    }

    @Override
    public void applyPassive() {
    }

    public boolean isChanPassiveDraw() { return true; }

    @Override
    public AttackResult resolveAttack(Card card, CardDeck deck, java.util.List<Card> hand,
                                       int handValueSum, java.util.List<Card> opponentHand) {
        AttackResult r = new AttackResult();


        int v = card.getValue();

        switch (v) {
            case 1:
                r.damage = 1;
                r.addFreeze = true;
                r.skipDefense = true;
                r.desc = "1️⃣ 1点伤害 + 施加【冷冻】（跳过防御）";
                break;
            case 2:
                r.damage = 4;
                r.desc = "2️⃣ 4点伤害";
                break;
            case 3:
                r.damage = 2;
                r.drawCount = 1;
                r.desc = "3️⃣ 2点伤害 + 抽取1张牌";
                break;
            case 4:
                r.addFollowUp(FollowUp.CHAN_FOUR_SWAP);
                r.skipDefense = true;
                r.desc = "4️⃣ 抽取对手1张手牌，选择交换或弃掉（跳过防御）";
                break;
            case 5:
                r.addFollowUp(FollowUp.CHAN_FIVE_REORDER);
                r.drawCount = 2;
                r.skipDefense = true;
                r.selfDamage = 2;
                r.desc = "5️⃣ 消耗2❤️ + 查看牌库顶5张排序后放回 + 抽2张（跳过防御）";
                break;
            case 6:
                r.damage = 5;
                r.addFollowUp(FollowUp.CHAN_SIX_REVEAL);
                r.desc = "6️⃣ 5点伤害 + 抽1张判定（🔵/⚪/⚫跳过防御）";
                break;
            case 7:
                r.damage = 6;
                r.addFollowUp(FollowUp.CHAN_SEVEN_JUDGE);
                r.desc = "7️⃣ 6点伤害 + 选择对手1张牌判定";
                break;
            case 0:
                r.damage = 7;
                r.addFreeze = true;
                r.drawCount = 1;
                r.desc = "0️⃣ 7点伤害 + 施加【冷冻】 + 抽1张";
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
            r.damage = 0;
            r.drawCount = -1;
            r.desc = "判定0/白/黑 → 加入手牌";
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
                r.addFreeze = true;
                r.desc = "2️⃣ 反击2点伤害 + 施加【冷冻】";
                break;
            case 3:
                r.revealTopDeck = true;
                r.desc = "3️⃣ 翻开牌库顶判定恢复½点数（道具算0）";
                break;
            case 0:
                r.immuneAll = true;
                r.counterFromDamage = (incomingDamage + 1) / 2;
                r.endAttackerTurn = true;
                r.desc = "0️⃣ 免疫所有伤害 + 反击" + r.counterFromDamage + "点 + 进攻方结束回合";
                break;
            default:
                r.desc = "";
                break;
        }
        return r;
    }
}