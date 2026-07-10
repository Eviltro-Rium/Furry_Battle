import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class AttackEngine {
    Game game;

    AttackEngine(Game game) { this.game = game; }

    void applyAttackEffect(Card card, GameCharacter self, GameCharacter opponent,
                                    List<Card> selfHand, Runnable onDone) {
        int handSum = 0;
        for (Card c : selfHand) {
            if (c.isNumberCard()) handSum += c.getValue();
        }
        game.pendingAttack = self.resolveAttack(card, game.deck, selfHand, handSum,
            game.getHandFor(opponent));

        if (self instanceof SaikiCharacter && card.getValue() == 0) {
            CharacterHandler h = game.getHandler(self);
            if (h instanceof SaikiHandler) {
                game.effectEngine.applyImmediateEffects(game.pendingAttack, self, opponent);
                if (!self.isAlive()) {
                    String winner = self == game.playerChar ? "AI" : "你";
                    Timer t = new Timer(500, ev -> { ((Timer)ev.getSource()).stop(); game.endGame(winner); });
                    t.setRepeats(false);
                    t.start();
                    return;
                }
                ((SaikiHandler) h).handleSaikiZeroAttack(self, opponent, selfHand, onDone);
                return;
            }
        }

        if (self instanceof SaikiCharacter && card.getValue() == 7) {
            CharacterHandler h = game.getHandler(self);
            if (h instanceof SaikiHandler) {
                game.effectEngine.applyImmediateEffects(game.pendingAttack, self, opponent);
                if (!self.isAlive()) {
                    String winner = self == game.playerChar ? "AI" : "你";
                    Timer t = new Timer(500, ev -> { ((Timer)ev.getSource()).stop(); game.endGame(winner); });
                    t.setRepeats(false);
                    t.start();
                    return;
                }
                ((SaikiHandler) h).handleSaikiSevenAttack(self, opponent, selfHand, onDone);
                return;
            }
        }

        game.effectEngine.applyImmediateEffects(game.pendingAttack, self, opponent);

        if (!self.isAlive()) {
            String winner = self == game.playerChar ? "AI" : "你";
            Timer t = new Timer(500, ev -> { ((Timer)ev.getSource()).stop(); game.endGame(winner); });
            t.setRepeats(false);
            t.start();
            return;
        }

        if (game.effectEngine.hasFollowUp(game.pendingAttack)) {
            game.effectEngine.executeFollowUp(game.effectEngine.getFirstFollowUp(game.pendingAttack),
                card, self, opponent, selfHand, onDone);
            return;
        }

        if (game.pendingAttack.drawCount > 0) {
            List<Card> drawn = game.drawFromDeck(game.pendingAttack.drawCount);
            if (!drawn.isEmpty()) {
                selfHand.addAll(drawn);
                if (game.pendingAttack.recalcDamageAfterDraw) {
                    int newSum = 0;
                    for (Card c : selfHand) {
                        if (c.isNumberCard()) newSum += c.getValue();
                    }
                    game.pendingAttack.damage = (int) Math.ceil(newSum / 2.0);
                    game.showAttackDesc("7 抽牌后手牌之和" + newSum + "，造成" + game.pendingAttack.damage + "点伤害");
                }
                game.updateDisplay();
                GameAnim.playDrawAnimations(game, drawn.size(), self == game.playerChar, () -> {
                    game.updateDisplay();
                    onDone.run();
                });
                return;
            }
        }
        game.updateDisplay();
        onDone.run();
    }

    void resolvePostDefense(GameCharacter attacker, GameCharacter defender) {
        if (game.pendingAttack == null) return;
        int dmg = game.pendingAttack.damage;
        boolean isRed = false;
        if (!game.discardPile.isEmpty()) {
            Card top = game.discardPile.getFirst();
            isRed = top.getColor() == Card.CardColor.RED || top.getEffectiveColor() == Card.CardColor.RED;
        }

        if (dmg > 0 && game.pendingDefendCard != null) {
            GameCharacter.DefenseResult def = defender.resolveDefense(game.pendingDefendCard, dmg, isRed);
            game.pendingDefendResult = def;

            if (def.immuneAll) {
                dmg = 0;
                if (def.healFromDamage && game.pendingAttack.damage > 0) {
                    defender.heal(game.pendingAttack.damage);
                    Point loc = defender == game.playerChar
                        ? new Point(game.getWidth() / 2 - 60, game.getHeight() * 3 / 4 - 30)
                        : new Point(game.getWidth() / 2 - 60, game.getHeight() / 3 - 30);
                    GameAnim.playFloatingText(game, "+" + game.pendingAttack.damage + "[生命]", new Color(60, 220, 60), loc);
                    GameAnim.playParticleBurst(game, loc, new Color(60, 220, 60, 180), 8);
                }
            } else {
                dmg -= def.blocked;
                if (dmg < 0) dmg = 0;
            }

            if (def.selfHeal > 0) {
                defender.heal(def.selfHeal);
                Point loc = defender == game.playerChar
                    ? new Point(game.getWidth() / 2 - 60, game.getHeight() * 3 / 4 - 30)
                    : new Point(game.getWidth() / 2 - 60, game.getHeight() / 3 - 30);
                GameAnim.playFloatingText(game, "+" + def.selfHeal + "[生命]", new Color(60, 220, 60), loc);
            }

            if (def.addBurn > 0) {
                attacker.addBurn(def.addBurn);
                GameAnim.playFloatingText(game, "+" + def.addBurn + "[灼烧]", new Color(255, 140, 0),
                    attacker == game.playerChar
                        ? new Point(game.getWidth() / 2, game.getHeight() * 3 / 4 - 60)
                        : new Point(game.getWidth() / 2, game.getHeight() / 3 - 60));
            }

            if (def.addBurnSelf > 0) {
                defender.addBurn(def.addBurnSelf);
                GameAnim.playFloatingText(game, "+" + def.addBurnSelf + "[灼烧]", new Color(255, 140, 0),
                    defender == game.playerChar
                        ? new Point(game.getWidth() / 2, game.getHeight() * 3 / 4 - 60)
                        : new Point(game.getWidth() / 2, game.getHeight() / 3 - 60));
            }

            if (def.healAllBurnPlus > 0) {
                int totalBurn = game.playerChar.getBurnStacks() + game.aiChar.getBurnStacks() + (game.is1v2 && game.aiChar2 != null ? game.aiChar2.getBurnStacks() : 0);
                int healAmt = totalBurn + def.healAllBurnPlus;
                defender.heal(healAmt);
                Point loc = defender == game.playerChar
                    ? new Point(game.getWidth() / 2 - 60, game.getHeight() * 3 / 4 - 30)
                    : new Point(game.getWidth() / 2 - 60, game.getHeight() / 3 - 30);
                GameAnim.playFloatingText(game, "+" + healAmt + "[生命]", new Color(60, 220, 60), loc);
            }

            if (def.counterDmg > 0) {
                attacker.takeDamage(def.counterDmg);
                Point loc = attacker == game.playerChar
                    ? new Point(game.getWidth() / 2 + 60, game.getHeight() * 3 / 4)
                    : new Point(game.getWidth() / 2 + 60, game.getHeight() / 3);
                GameAnim.playFloatingText(game, "-" + def.counterDmg + "[伤害]", new Color(255, 60, 60), loc);
            }

            if (def.counterDmgFromAttackerBurn) {
                int burnCounter = attacker.getBurnStacks();
                if (burnCounter > 0) {
                    attacker.takeDamage(burnCounter);
                    Point loc = attacker == game.playerChar
                        ? new Point(game.getWidth() / 2 + 60, game.getHeight() * 3 / 4)
                        : new Point(game.getWidth() / 2 + 60, game.getHeight() / 3);
                    GameAnim.playFloatingText(game, "-" + burnCounter + "[伤害]", new Color(255, 60, 60), loc);
                }
            }

            if (def.counterDmgFromFieldBurn) {
                int fieldBurn = game.playerChar.getBurnStacks() + game.aiChar.getBurnStacks() + (game.is1v2 && game.aiChar2 != null ? game.aiChar2.getBurnStacks() : 0);
                if (fieldBurn > 0) {
                    attacker.takeDamage(fieldBurn);
                    Point loc = attacker == game.playerChar
                        ? new Point(game.getWidth() / 2 + 60, game.getHeight() * 3 / 4)
                        : new Point(game.getWidth() / 2 + 60, game.getHeight() / 3);
                    GameAnim.playFloatingText(game, "-" + fieldBurn + "[伤害]", new Color(255, 60, 60), loc);
                }
            }

            if (def.counterFromDamage > 0) {
                attacker.takeDamage(def.counterFromDamage);
                Point loc = attacker == game.playerChar
                    ? new Point(game.getWidth() / 2 + 60, game.getHeight() * 3 / 4)
                    : new Point(game.getWidth() / 2 + 60, game.getHeight() / 3);
                GameAnim.playFloatingText(game, "-" + def.counterFromDamage + "[伤害]", new Color(255, 60, 60), loc);
            }

            if (def.addFreeze) {
                attacker.setFrozen(true);
                GameAnim.playFloatingText(game, "[冷冻]", new Color(100, 180, 255),
                    attacker == game.playerChar
                        ? new Point(game.getWidth() / 2, game.getHeight() * 3 / 4 - 60)
                        : new Point(game.getWidth() / 2, game.getHeight() / 3 - 60));
            }

            if (def.addBleed > 0) {
                attacker.addBleed(def.addBleed);
                GameAnim.playFloatingText(game, "+" + def.addBleed + "[流血]", new Color(180, 0, 0),
                    attacker == game.playerChar
                        ? new Point(game.getWidth() / 2, game.getHeight() * 3 / 4 - 90)
                        : new Point(game.getWidth() / 2, game.getHeight() / 3 - 90));
            }

            if (def.immuneDebuff && game.pendingAttack != null) {
                if (game.pendingAttack.addBurn > 0) game.pendingAttack.addBurn = 0;
                if (game.pendingAttack.addFreeze) game.pendingAttack.addFreeze = false;
                if (game.pendingAttack.addBleed > 0) game.pendingAttack.addBleed = 0;
            }

            if (def.reflectDebuff && game.pendingAttack != null) {
                if (game.pendingAttack.addBleed > 0) {
                    defender.addBleed(game.pendingAttack.addBleed);
                    attacker.addBleed(game.pendingAttack.addBleed);
                    game.pendingAttack.addBleed = 0;
                    GameAnim.playFloatingText(game, "[流血]反弹", new Color(180, 0, 0),
                        attacker == game.playerChar
                            ? new Point(game.getWidth() / 2, game.getHeight() * 3 / 4 - 90)
                            : new Point(game.getWidth() / 2, game.getHeight() / 3 - 90));
                }
                if (game.pendingAttack.addBurn > 0) {
                    attacker.addBurn(game.pendingAttack.addBurn);
                    game.pendingAttack.addBurn = 0;
                }
                if (game.pendingAttack.addFreeze) {
                    attacker.setFrozen(true);
                    game.pendingAttack.addFreeze = false;
                }
            }

            if (def.sharedDamage > 0) {
                int shared = def.sharedDamage;
                attacker.takeDamage(shared);
                defender.takeDamage(shared);
                GameAnim.playFloatingText(game, "-" + shared + "[伤害]", new Color(255, 60, 60),
                    attacker == game.playerChar
                        ? new Point(game.getWidth() / 2 + 60, game.getHeight() * 3 / 4)
                        : new Point(game.getWidth() / 2 + 60, game.getHeight() / 3));
                GameAnim.playFloatingText(game, "-" + shared + "[伤害]", new Color(255, 60, 60),
                    defender == game.playerChar
                        ? new Point(game.getWidth() / 2 - 60, game.getHeight() * 3 / 4)
                        : new Point(game.getWidth() / 2 - 60, game.getHeight() / 3));
            }

            if (def.forceDiscardAll) {
                List<Card> oppHand = game.getHandFor(attacker);
                for (Card c : new ArrayList<>(oppHand)) {
                    game.discardPile.addLast(c);
                }
                oppHand.clear();
            }

            if (def.clearSelfBuffs) {
                defender.clearDebuffsOnly();
            }

            if (def.drawCount > 0) {
                List<Card> defHand = game.getHandFor(defender);
                List<Card> drawn = game.drawFromDeck(def.drawCount);
                if (!drawn.isEmpty()) {
                    defHand.addAll(drawn);
                    GameAnim.playDrawAnimations(game, drawn.size(), defender == game.playerChar, () -> game.updateDisplay());
                }
            }

            if (def.addGuard > 0) {
                defender.addGuard(def.addGuard);
                GameAnim.playFloatingText(game, "+" + def.addGuard + "[守护]", new Color(100, 200, 255),
                    defender == game.playerChar
                        ? new Point(game.getWidth() / 2, game.getHeight() * 3 / 4 - 90)
                        : new Point(game.getWidth() / 2, game.getHeight() / 3 - 90));
            }

            if (def.healPerGuard > 0) {
                int healAmt = (defender.getGuardStacks()) * def.healPerGuard;
                defender.heal(healAmt);
                if (healAmt > 0) {
                    GameAnim.playFloatingText(game, "+" + healAmt + "[生命]", new Color(60, 220, 60),
                        defender == game.playerChar
                            ? new Point(game.getWidth() / 2, game.getHeight() * 3 / 4)
                            : new Point(game.getWidth() / 2, game.getHeight() / 3));
                }
            }

            if (def.counterPerGuard > 0) {
                int counterDmg = def.counterPerGuard * defender.getGuardStacks();
                if (counterDmg > 0) {
                    attacker.takeDamage(counterDmg);
                    GameAnim.playFloatingText(game, "-" + counterDmg + "[伤害]", new Color(255, 60, 60),
                        attacker == game.playerChar
                            ? new Point(game.getWidth() / 2 + 60, game.getHeight() * 3 / 4)
                            : new Point(game.getWidth() / 2 + 60, game.getHeight() / 3));
                }
            }

            game.showDefendDesc(def.desc);
        }

        if (dmg > 0 && defender.getGuardStacks() > 0) {
            int guardBlock;
            if (defender instanceof MozeCharacter) {
                guardBlock = chooseGuardUse(defender, dmg);
            } else {
                guardBlock = Math.min(dmg, defender.getGuardStacks());
            }
            if (guardBlock > 0) {
                dmg -= guardBlock;
                defender.removeGuard(guardBlock);
                GameAnim.playFloatingText(game, "[守护]-" + guardBlock, new Color(100, 200, 255),
                    defender == game.playerChar
                        ? new Point(game.getWidth() / 2 - 60, game.getHeight() * 3 / 4 - 60)
                        : new Point(game.getWidth() / 2 - 60, game.getHeight() / 3 - 60));
            }
        }

        if (game.pendingDefendCard != null && defender.getBleedStacks() > 0
                && game.pendingDefendCard.getValue() <= 3 && !game.pendingDefendCard.isItemCard()) {
            int bleedDmg = defender.getBleedStacks();
            for (int i = 0; i < bleedDmg; i++) {
                defender.takeDamage(1);
                GameAnim.playFloatingText(game, "-1[流血]", new Color(180, 0, 0),
                    defender == game.playerChar
                        ? new Point(game.getWidth() / 2 - 30 + i * 30, game.getHeight() * 3 / 4 - 30)
                        : new Point(game.getWidth() / 2 - 30 + i * 30, game.getHeight() / 3 - 30));
            }
        }

        if (dmg > 0) {
            defender.takeDamage(dmg);
            Point loc = defender == game.playerChar
                ? new Point(game.getWidth() / 2, game.getHeight() * 3 / 4)
                : new Point(game.getWidth() / 2, game.getHeight() / 3);
            GameAnim.playFloatingText(game, "-" + dmg + "[伤害]", new Color(255, 60, 60), loc);
            GameAnim.playScreenShake(game, Math.min(dmg * 2, 10));
            GameAnim.playParticleBurst(game, loc, new Color(255, 60, 60, 200), Math.min(dmg * 3, 20));
        }

        if (game.pendingAttack != null && game.pendingAttack.forceOpponentDiscard > 0) {
            List<Card> oppHand = game.getHandFor(attacker);
            int toDiscard = Math.min(game.pendingAttack.forceOpponentDiscard, oppHand.size());
            for (int i = 0; i < toDiscard; i++) {
                int idx = (int)(Math.random() * oppHand.size());
                Card discarded = oppHand.remove(idx);
                game.discardPile.addLast(discarded);
                GameAnim.playFloatingText(game, "弃" + discarded + "[牌]", new Color(255, 60, 60),
                    attacker == game.playerChar
                        ? new Point(game.getWidth() / 2, game.getHeight() / 3 - 30)
                        : new Point(game.getWidth() / 2, game.getHeight() * 3 / 4 - 30));
            }
        }

        if (game.pendingAttack != null && game.pendingAttack.drawOpponentCard) {
            List<Card> oppHand = game.getHandFor(attacker);
            List<Card> selfHand = game.getHandFor(defender);
            if (!oppHand.isEmpty()) {
                int idx = (int)(Math.random() * oppHand.size());
                Card drawn = oppHand.remove(idx);
                selfHand.add(drawn);
                GameAnim.playFloatingText(game, "抽" + drawn + "[牌]", new Color(100, 180, 255),
                    attacker == game.playerChar
                        ? new Point(game.getWidth() / 2, game.getHeight() / 3 - 30)
                        : new Point(game.getWidth() / 2, game.getHeight() * 3 / 4 - 30));
            }
        }

        game.pendingAttack = null;
        game.pendingDefendCard = null;
        game.updateDisplay();

        checkDeath();
    }

    void applyBurnDamage(GameCharacter target) {
        if (target.getBurnStacks() > 0) {
            if (!target.isImmuneToBurn()) {
                int burnDmg = target.getBurnStacks();
                target.takeDamage(burnDmg);
                Point loc = target == game.playerChar
                    ? new Point(game.getWidth() / 2, game.getHeight() * 3 / 4)
                    : new Point(game.getWidth() / 2, game.getHeight() / 3);
                GameAnim.playFloatingText(game, "[灼烧]-" + burnDmg, new Color(255, 100, 0), loc);
            }
            target.removeBurn(1);
        }
    }

    boolean checkDeath() {
        if (!game.playerChar.isAlive()) {
            Timer t = new Timer(500, ev -> { ((Timer)ev.getSource()).stop(); game.endGame("AI"); });
            t.setRepeats(false);
            t.start();
            return true;
        }
        if (game.gameMode.checkPlayerWin(game.participants)) {
            Timer t = new Timer(500, ev -> { ((Timer)ev.getSource()).stop(); game.endGame("你"); });
            t.setRepeats(false);
            t.start();
            return true;
        }
        if (game.is1v2) {
            AIPlayer aliveAI = null;
            if (!game.aiChar.isAlive() && game.aiChar2 != null && game.aiChar2.isAlive()) {
                aliveAI = game.ai2;
            } else if (game.aiChar2 != null && !game.aiChar2.isAlive() && game.aiChar.isAlive()) {
                aliveAI = game.ai;
            }
            if (aliveAI != null) {
                int need = game.turnEngine.aiFillTarget() - aliveAI.getHand().size();
                if (need > 0) {
                    List<Card> drawn = game.turnEngine.drawFromDeck(need);
                    if (!drawn.isEmpty()) {
                        aliveAI.addCards(drawn);
                        game.updateDisplay();
                    }
                }
            }
        }
        return false;
    }

    void checkHandLimit(List<Card> hand, boolean isPlayer) {
        int limit = isPlayer ? game.maxPlayerHand : game.turnEngine.aiFillTarget();
        while (hand.size() > limit) {
            if (isPlayer) {
                game.forcedDiscard = true;
                game.hasPlayedThisTurn = false;
                game.selectedSingle = -1;
                game.selectedMulti.clear();
                game.currentPhase = Game.Phase.PLAYER_DISCARD;
                game.updateDisplay();
                game.showMessage("手牌超过" + game.maxPlayerHand + "张，请弃牌至不超过" + game.maxPlayerHand + "张！");
                return;
            } else {
                Card worst = null;
                for (Card c : hand) {
                    if (worst == null || c.getValue() < worst.getValue()) worst = c;
                }
                if (worst != null) {
                    hand.remove(worst);
                    game.discardPile.addLast(worst);
                }
            }
        }
    }

    void applyPurifyEffect(GameCharacter ch, boolean isPlayer) {
        if (isPlayer) {
            if (!ch.hasDebuff()) {
                game.showAttackDesc("净化 无buff可净化，继续出牌");
                return;
            }
            PurifyDialog dialog = new PurifyDialog(game, ch);
            dialog.setVisible(true);
            String choice = dialog.getResult();
            if (choice != null) {
                if (choice.contains("灼烧")) {
                    ch.removeBurn(1);
                    GameAnim.playFloatingText(game, "+1[灼烧]", new Color(255, 140, 0),
                        new Point(game.getWidth() / 2, game.getHeight() * 3 / 4 - 60));
                    game.showAttackDesc("净化 净化1层灼烧，继续出牌");
                } else if (choice.contains("冷冻")) {
                    ch.setFrozen(false);
                    GameAnim.playFloatingText(game, "[解冻]", new Color(100, 180, 255),
                        new Point(game.getWidth() / 2, game.getHeight() * 3 / 4 - 60));
                    game.showAttackDesc("净化 解除冷冻，继续出牌");
                } else if (choice.contains("流血")) {
                    ch.removeBleed(1);
                    GameAnim.playFloatingText(game, "-1[流血]", new Color(180, 0, 0),
                        new Point(game.getWidth() / 2, game.getHeight() * 3 / 4 - 60));
                    game.showAttackDesc("净化 净化1层流血，继续出牌");
                }
            } else {
                game.showAttackDesc("净化 取消净化，继续出牌");
            }
        } else {
            if (!ch.hasDebuff()) {
                game.showAttackDesc("净化 AI无buff可净化，继续出牌");
                return;
            }
            List<String> debuffs = new ArrayList<>();
            if (ch.getBurnStacks() > 0) debuffs.add("灼烧");
            if (ch.isFrozen()) debuffs.add("冷冻");
            if (ch.getBleedStacks() > 0) debuffs.add("流血");
            String chosen = debuffs.get((int)(Math.random() * debuffs.size()));
            if (chosen.equals("灼烧")) {
                ch.removeBurn(1);
                GameAnim.playFloatingText(game, "+1[灼烧]", new Color(255, 140, 0),
                    new Point(game.getWidth() / 2, game.getHeight() / 3 - 60));
                game.showAttackDesc("净化 AI净化1层灼烧，继续出牌");
            } else if (chosen.equals("冷冻")) {
                ch.setFrozen(false);
                GameAnim.playFloatingText(game, "[解冻]", new Color(100, 180, 255),
                    new Point(game.getWidth() / 2, game.getHeight() / 3 - 60));
                game.showAttackDesc("净化 AI解除冷冻，继续出牌");
            } else if (chosen.equals("流血")) {
                ch.removeBleed(1);
                GameAnim.playFloatingText(game, "-1[流血]", new Color(180, 0, 0),
                    new Point(game.getWidth() / 2, game.getHeight() / 3 - 60));
                game.showAttackDesc("净化 AI净化1层流血，继续出牌");
            }
        }
    }

    void applySuperPurifyEffect(GameCharacter ch, boolean isPlayer) {
        if (!ch.hasDebuff() && ch.getGuardStacks() == 0) {
            game.showAttackDesc(isPlayer ? "超净 无buff可净化，继续出牌" : "超净 AI无buff可净化，继续出牌");
            return;
        }
        int guardCleared = ch.getGuardStacks();
        ch.clearAllDebuffs();
        Point loc = isPlayer
            ? new Point(game.getWidth() / 2, game.getHeight() * 3 / 4 - 60)
            : new Point(game.getWidth() / 2, game.getHeight() / 3 - 60);
        GameAnim.playFloatingText(game, "[净化]", new Color(200, 180, 255), loc);
        if (guardCleared > 0) {
            GameAnim.playFloatingText(game, "-" + guardCleared + "[守护]", new Color(100, 200, 255),
                new Point(loc.x + 60, loc.y + 20));
        }
        game.showAttackDesc(isPlayer ? "超净 清除所有buff，继续出牌" : "超净 AI清除所有buff，继续出牌");
    }

    void handleRevealAndJudge(GameCharacter self, GameCharacter opponent, List<Card> selfHand, Runnable onDone) {
        CharacterHandler h = game.getHandler(self);
        if (h != null) h.handleRevealAndJudge(self, opponent, selfHand, onDone);
    }

    void resolveAIFiveChoice(GameCharacter self, GameCharacter opponent, List<Card> selfHand, Runnable onDone) {
        CharacterHandler h = game.getHandler(self);
        if (h != null) h.resolveAIFiveChoice(self, opponent, selfHand, onDone);
    }

    void handleChanFourSwap(GameCharacter self, GameCharacter opponent, List<Card> selfHand, List<Card> oppHand, Runnable onDone) {
        CharacterHandler h = game.getHandler(self);
        if (h != null) h.handleChanFourSwap(self, opponent, selfHand, oppHand, onDone);
    }

    void handleChanFiveReorder(GameCharacter self, GameCharacter opponent, List<Card> selfHand, Runnable onDone) {
        CharacterHandler h = game.getHandler(self);
        if (h != null) h.handleChanFiveReorder(self, opponent, selfHand, onDone);
    }

    void handleChanSevenJudge(GameCharacter self, GameCharacter opponent, List<Card> oppHand, Runnable onDone) {
        CharacterHandler h = game.getHandler(self);
        if (h != null) h.handleChanSevenJudge(self, opponent, oppHand, onDone);
    }

    void handleChanThreeDefendReveal(GameCharacter self, GameCharacter opponent, List<Card> selfHand, Runnable onDone) {
        CharacterHandler h = game.getHandler(self);
        if (h != null) h.handleChanThreeDefendReveal(self, opponent, selfHand, onDone);
    }

    void handleChanSixReveal(GameCharacter self, GameCharacter opponent, List<Card> selfHand, Runnable onDone) {
        CharacterHandler h = game.getHandler(self);
        if (h != null) h.handleChanSixReveal(self, opponent, selfHand, onDone);
    }

    void handleSaikiThreeDraw(GameCharacter self, GameCharacter opponent, List<Card> selfHand, List<Card> oppHand, Runnable onDone) {
        CharacterHandler h = game.getHandler(self);
        if (h != null) h.handleSaikiThreeDraw(self, opponent, selfHand, oppHand, onDone);
    }

    void handleSaikiSixJudge(GameCharacter self, GameCharacter opponent, List<Card> selfHand, Runnable onDone) {
        CharacterHandler h = game.getHandler(self);
        if (h != null) h.handleSaikiSixJudge(self, opponent, selfHand, onDone);
    }

    void handleSaikiSevenAttack(GameCharacter self, GameCharacter opponent, List<Card> selfHand, Runnable onDone) {
        CharacterHandler h = game.getHandler(self);
        if (h != null) h.handleSaikiSevenAttack(self, opponent, selfHand, onDone);
    }

    void handleBlazeFourDraw(GameCharacter self, GameCharacter opponent, List<Card> selfHand, List<Card> oppHand, Runnable onDone) {
        CharacterHandler h = game.getHandler(self);
        if (h instanceof BlazeHandler) ((BlazeHandler) h).handleBlazeFourDraw(self, opponent, selfHand, oppHand, onDone);
    }

    void doBlazeFourOpponentSelected(int aiCardIndex) {
        CharacterHandler h = game.getHandler(game.playerChar);
        if (h instanceof BlazeHandler) ((BlazeHandler) h).doBlazeFourOpponentSelected(aiCardIndex);
    }

    void handleBlazeDefendTwoDraw(GameCharacter self, GameCharacter opponent, List<Card> selfHand, Runnable onDone) {
        CharacterHandler h = game.getHandler(self);
        if (h instanceof BlazeHandler) ((BlazeHandler) h).handleBlazeDefendTwoDraw(self, opponent, selfHand, onDone);
    }

    void handleSerenityDefendTwoDrain(GameCharacter self, GameCharacter opponent, List<Card> selfHand, Runnable onDone) {
        CharacterHandler h = game.getHandler(self);
        if (h instanceof SerenityHandler) ((SerenityHandler) h).handleSerenityDefendTwoDrain(self, opponent, selfHand, onDone);
    }

    void handleSerenityDefendZeroReveal(GameCharacter self, GameCharacter opponent, List<Card> selfHand, Runnable onDone) {
        CharacterHandler h = game.getHandler(self);
        if (h instanceof SerenityHandler) ((SerenityHandler) h).handleSerenityDefendZeroReveal(self, opponent, selfHand, onDone);
    }

    void handleSaikiZeroAttack(GameCharacter self, GameCharacter opponent, List<Card> selfHand, Runnable onDone) {
        CharacterHandler h = game.getHandler(self);
        if (h != null) h.handleSaikiZeroAttack(self, opponent, selfHand, onDone);
    }

    void handleSaikiThreeDefendReveal(GameCharacter self, GameCharacter opponent, List<Card> selfHand, Runnable onDone) {
        CharacterHandler h = game.getHandler(self);
        if (h != null) h.handleSaikiThreeDefendReveal(self, opponent, selfHand, onDone);
    }

    void handleRevealTopDeck(GameCharacter self, GameCharacter opponent, List<Card> selfHand) {
        CharacterHandler h = game.getHandler(self);
        if (h != null) h.handleRevealTopDeck(self, opponent, selfHand);
    }

    void handleAIRevealTopDeck(GameCharacter self, GameCharacter opponent, List<Card> selfHand) {
        CharacterHandler h = game.getHandler(self);
        if (h != null) h.handleAIRevealTopDeck(self, opponent, selfHand);
    }

    void handleRevealDraw(GameCharacter self, GameCharacter opponent, List<Card> selfHand, boolean isPlayer) {
        CharacterHandler h = game.getHandler(self);
        if (h != null) h.handleRevealDraw(self, opponent, selfHand, isPlayer);
    }

    private int chooseGuardUse(GameCharacter defender, int incomingDamage) {
        if (defender == game.playerChar) {
            GuardChooserDialog dialog = new GuardChooserDialog(game, defender.getGuardStacks(), incomingDamage);
            dialog.setVisible(true);
            return dialog.getResult();
        } else {
            AIPlayer defAI = game.getAIFor(defender);
            if (defAI instanceof MozeAI) {
                return ((MozeAI) defAI).chooseGuardUse(defender.getGuardStacks(), incomingDamage, defender.getCurrentHp());
            }
            return Math.min(incomingDamage, defender.getGuardStacks());
        }
    }
}