import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class Game extends JFrame {
    static final int DELAY_STEP = 1500;
    static final int DELAY_PLAY = 1800;
    static final int DELAY_EFFECT = 2000;
    static final int DELAY_SKIP = 2500;
    static final int DELAY_REVEAL = 3000;

    enum Phase { PLAYER_PLAY, PLAYER_DISCARD, PLAYER_DEFEND, PLAYER_FIVE_CHOICE, PLAYER_SEVEN_CHOICE, SAIKI_THREE_CHOICE, SAIKI_SIX_JUDGE, AI_TURN, AI_DEFEND, GAME_OVER }

    protected Map<String, CharacterHandler> handlers = new HashMap<>();

    CharacterHandler getHandler(GameCharacter ch) {
        String name = ch.getName();
        for (Map.Entry<String, CharacterHandler> entry : handlers.entrySet()) {
            if (name.contains(entry.getKey())) return entry.getValue();
        }
        return null;
    }

    protected CardDeck deck;
    protected LinkedList<Card> discardPile;
    protected List<Card> playerHand;
    protected AIPlayer ai;
    protected GameCharacter playerChar;
    protected GameCharacter aiChar;
    protected int selectedSingle;
    protected List<Integer> selectedMulti;

    protected Phase currentPhase;
    protected boolean hasPlayedThisTurn;
    protected boolean aiHasPlayed;
    protected boolean aiDefendSuccess;
    protected boolean forcedDiscard;
    protected boolean hasPlayedBlackDefend;
    protected boolean aiHasPlayedBlackDefend;

    protected GameCharacter.AttackResult pendingAttack;
    protected Card pendingDefendCard;
    protected GameCharacter.DefenseResult pendingDefendResult;
    protected boolean pendingFiveChoice;
    protected Card fiveChoiceCard;
    protected boolean forceOpponentDiscardOne;
    protected int turnCount;
    protected Timer aiTimer;
    protected Timer fadeTimer;

    protected boolean chanFourSwapMode;
    protected Card chanFourSwapDrawn;
    protected Runnable chanFourSwapCallback;

    protected boolean chanSevenMode;
    protected Runnable chanSevenCallback;
    protected Card chanSevenChosenCard;
    protected boolean chanSevenKeepMode;

    protected Card saikiThreeDrawn;
    protected Runnable saikiThreeOnDone;
    protected Runnable blazeFourOnDone;

    protected int selectedAICard;
    protected boolean chanFourSelectOpponent;

    protected GameUI ui;
    protected EffectEngine effectEngine;

    private boolean busy = false;
    private long lastBtnClickTime = 0;
    private static final int BTN_COOLDOWN_MS = 300;

    void setBusy(boolean b) { busy = b; }

    boolean canClickBtn() {
        long now = System.currentTimeMillis();
        if (now - lastBtnClickTime < BTN_COOLDOWN_MS) return false;
        lastBtnClickTime = now;
        return true;
    }

    public Game() {
        deck = new CardDeck();
        discardPile = new LinkedList<>();
        playerHand = new ArrayList<>();
        selectedMulti = new ArrayList<>();
        selectedSingle = -1;
    }

    protected void initUI() {
        ui = new GameUI();
        ui.buildUI(this);
        effectEngine = new EffectEngine(this);
        setContentPane(ui.getRootPanel());
        setSize(1100, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    void onCharacterSelected(int playerChoice, int aiChoice) {
        String[] charNames = {"Ryan", "Leon", "Chan", "Saiki", "Blaze"};
        playerChar = createCharacter(charNames[playerChoice], false);
        aiChar = createCharacter(charNames[aiChoice], true);
        ai = createAI(charNames[aiChoice], aiChar);
        if (ai instanceof LeonAI) ((LeonAI) ai).setOpponent(playerChar);
        if (ai instanceof SaikiAI) ((SaikiAI) ai).setCharacters(aiChar, playerChar);
        if (ai instanceof BlazeAI) ((BlazeAI) ai).setOpponent(playerChar);

        handlers.put("Ryan", new RyanHandler(this));
        handlers.put("Leon", new LeonHandler(this));
        handlers.put("Chan", new ChanHandler(this));
        handlers.put("Saiki", new SaikiHandler(this));
        handlers.put("Blaze", new BlazeHandler(this));

        initUI();
        startGame();
    }

    protected GameCharacter createCharacter(String name, boolean isAI) {
        switch (name) {
            case "Leon": return new LeonCharacter(isAI);
            case "Chan": return new ChanCharacter(isAI);
            case "Saiki": return new SaikiCharacter();
            case "Blaze": return new BlazeCharacter(isAI);
            default: return new RyanCharacter(isAI);
        }
    }

    protected AIPlayer createAI(String name, GameCharacter character) {
        switch (name) {
            case "Leon": return new LeonAI(character);
            case "Chan": return new ChanAI(character);
            case "Saiki": {
                SaikiAI sai = new SaikiAI();
                sai.setCharacters(character, null);
                return sai;
            }
            case "Blaze": return new BlazeAI(character);
            default: return new RyanAI(character);
        }
    }

    void backToSelect() {
        if (aiTimer != null && aiTimer.isRunning()) aiTimer.stop();
        if (fadeTimer != null && fadeTimer.isRunning()) fadeTimer.stop();
        busy = false;
        CharacterSelectPanel selectPanel = new CharacterSelectPanel(this);
        setContentPane(selectPanel);
        revalidate();
        repaint();
    }

    void startGame() {
        deck.reset();
        discardPile.clear();
        playerHand.clear();
        ai.clear();
        selectedMulti.clear();
        selectedSingle = -1;
        hasPlayedThisTurn = false;
        aiHasPlayed = false;
        aiDefendSuccess = false;
        forcedDiscard = false;
        hasPlayedBlackDefend = false;
        aiHasPlayedBlackDefend = false;
        GameAnim.resetFloatOffsets();

        pendingFiveChoice = false;
        fiveChoiceCard = null;
        forceOpponentDiscardOne = false;
        chanFourSwapMode = false;
        chanFourSwapDrawn = null;
        chanFourSwapCallback = null;

        chanSevenMode = false;
        chanSevenCallback = null;
        chanSevenChosenCard = null;
        chanSevenKeepMode = false;
        saikiThreeDrawn = null;
        saikiThreeOnDone = null;
        blazeFourOnDone = null;

        selectedAICard = -1;
        chanFourSelectOpponent = false;
        pendingDefendCard = null;
        playerChar.reset();
        aiChar.reset();
        turnCount = 1;

        Card first = deck.draw();
        while (first.isBlack() || first.isWhite()) {
            discardPile.addLast(first);
            first = deck.draw();
        }
        discardPile.addFirst(first);
        playerHand.addAll(deck.draw(5));
        ai.addCards(deck.draw(5));

        if (playerChar instanceof ChanCharacter) {
            List<Card> chanDraw = deck.draw(1);
            playerHand.addAll(chanDraw);
        }

        currentPhase = Phase.PLAYER_PLAY;
        updateDisplay();
    }

    List<Card> getPlayerHand() { return playerHand; }

    List<Card> getAIHand() { return ai.getHand(); }
    AIPlayer getAI() { return ai; }
    GameUI getUI() { return ui; }
    Phase getCurrentPhase() { return currentPhase; }
    int getSelectedSingle() { return selectedSingle; }
    boolean hasPlayedThisTurn() { return hasPlayedThisTurn; }
    LinkedList<Card> getDiscardPile() { return discardPile; }
    void setCurrentPhase(Phase p) { currentPhase = p; }
    void setPendingFiveChoice(boolean v) { pendingFiveChoice = v; }
    void setFiveChoiceCard(Card c) { fiveChoiceCard = c; }
    void setForceOpponentDiscardOne(boolean v) { forceOpponentDiscardOne = v; }

    void showAIAttackCard(Card card) {
        ui.atkCardRow.removeAll();
        ui.atkCardRow.add(GameUI.createCardView(card, false, -1, false, currentPhase, this));
        ui.atkCardRow.revalidate();
        ui.atkCardRow.repaint();
        ui.defCardRow.removeAll();
        JLabel emptyDef = new JLabel("等待防御", SwingConstants.CENTER);
        emptyDef.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        emptyDef.setForeground(new Color(160, 180, 200));
        ui.defCardRow.add(emptyDef);
        ui.defendDescLabel.setText("");
        ui.defCardRow.revalidate();
        ui.defCardRow.repaint();
    }

    void showDefendCard(Card card) {
        ui.defCardRow.removeAll();
        ui.defCardRow.add(GameUI.createCardView(card, false, -1, false, currentPhase, this));
        ui.defCardRow.revalidate();
        ui.defCardRow.repaint();
    }

    void showAttackDesc(String desc) {
        ui.attackDescLabel.setText(desc);
        ui.attackDescLabel.repaint();
    }

    void showDefendDesc(String desc) {
        ui.defendDescLabel.setText(desc);
        ui.defendDescLabel.repaint();
    }

    void showAIRevealCard(Card card) {
        ui.aiRevealPanel.removeAll();
        ui.aiRevealPanel.add(GameUI.createCardView(card, false, -1, false, currentPhase, this));
        ui.aiRevealPanel.revalidate();
        ui.aiRevealPanel.repaint();
    }

    void showAIRevealCards(List<Card> cards) {
        ui.aiRevealPanel.removeAll();
        for (Card c : cards) {
            ui.aiRevealPanel.add(GameUI.createCardView(c, false, -1, false, currentPhase, this));
        }
        ui.aiRevealPanel.revalidate();
        ui.aiRevealPanel.repaint();
    }

    void clearAIZones() {
        ui.atkCardRow.removeAll();
        JLabel emptyAtk = new JLabel("等待出牌", SwingConstants.CENTER);
        emptyAtk.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        emptyAtk.setForeground(new Color(200, 160, 140));
        ui.atkCardRow.add(emptyAtk);
        ui.attackDescLabel.setText("");
        ui.defCardRow.removeAll();
        JLabel emptyDef = new JLabel("等待防御", SwingConstants.CENTER);
        emptyDef.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        emptyDef.setForeground(new Color(160, 180, 200));
        ui.defCardRow.add(emptyDef);
        ui.defendDescLabel.setText("");
        ui.aiRevealPanel.removeAll();
        JLabel emptyLabel = new JLabel("等待判定", SwingConstants.CENTER);
        emptyLabel.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        emptyLabel.setForeground(new Color(140, 160, 200));
        ui.aiRevealPanel.add(emptyLabel);
        ui.atkCardRow.revalidate();
        ui.defCardRow.revalidate();
        ui.aiRevealPanel.revalidate();
        ui.atkCardRow.repaint();
        ui.defCardRow.repaint();
        ui.aiRevealPanel.repaint();
    }

    void onCardClicked(int handIndex, Phase phase) {
        if (busy) return;
        if (chanFourSwapMode && phase == Phase.PLAYER_SEVEN_CHOICE) {
            selectedSingle = (selectedSingle == handIndex) ? -1 : handIndex;
            updateDisplay();
            return;
        }
        if (phase == Phase.PLAYER_PLAY || phase == Phase.PLAYER_DEFEND || phase == Phase.PLAYER_FIVE_CHOICE || phase == Phase.PLAYER_SEVEN_CHOICE || phase == Phase.SAIKI_THREE_CHOICE || phase == Phase.SAIKI_SIX_JUDGE) {
            selectedSingle = (selectedSingle == handIndex) ? -1 : handIndex;
        } else if (phase == Phase.PLAYER_DISCARD) {
            if (selectedMulti.contains(handIndex)) {
                selectedMulti.remove(Integer.valueOf(handIndex));
            } else {
                selectedMulti.add(handIndex);
            }
        }
        updateDisplay();
    }

    GameCharacter getPlayerChar() { return playerChar; }
    GameCharacter getAIChar() { return aiChar; }

    // ===== Game logic =====

    protected boolean canPlayOn(Card card, Card top) {
        if (card.isBlack()) return true;
        if (card.isWhite()) return true;
        Card.CardColor effective = top.getEffectiveColor();
        return card.getColor() == effective || card.getValue() == top.getValue();
    }

    protected boolean canDefend(Card card, Card top) {
        if (card.isBlack()) return true;
        if (card.isDrawThree()) return true;
        if (card.isPotion()) return true;
        boolean isBlueAttack = top.getEffectiveColor() == Card.CardColor.BLUE;
        if (isBlueAttack && pendingAttack != null && pendingAttack.blueUnblockable) return false;
        if (card.isWhite()) return card.getValue() <= 3 && canPlayOn(card, top);
        return card.getValue() <= 3 && canPlayOn(card, top);
    }

    protected boolean playerCanDefend() {
        if (discardPile.isEmpty()) return false;
        Card top = discardPile.getFirst();
        if (top.isBlack() && !hasPlayedBlackDefend) return false;
        boolean isBlueAttack = top.getEffectiveColor() == Card.CardColor.BLUE;
        if (playerChar.isFrozen() && isBlueAttack) return false;
        if (pendingAttack != null && pendingAttack.blueUnblockable) return false;
        for (Card c : playerHand) {
            if (canDefend(c, top)) return true;
        }
        return false;
    }

    protected boolean isBlackDefend(Card top) {
        return top.isBlack();
    }

    void doPlay() {
        if (busy) return;
        busy = true;
        if (currentPhase != Phase.PLAYER_PLAY) { busy = false; return; }
        if (selectedSingle < 0 || selectedSingle >= playerHand.size()) {
            busy = false;
            showMessage("请先点击选择一张牌！");
            return;
        }
        if (discardPile.isEmpty()) { busy = false; return; }

        Card card = playerHand.get(selectedSingle);
        Card top = discardPile.getFirst();
        if (!canPlayOn(card, top)) {
            busy = false;
            showMessage(card + " 无法匹配弃牌库顶 " + top + "\n(需颜色或数字相同)");
            return;
        }

        if (card.isBlack()) {
            Card.CardColor chosen = GameUI.showColorChooser(this);
            if (chosen == null) { busy = false; return; }
            card.setChosenColor(chosen);
        }

        if (card.isWhite()) {
            card.setChosenColor(top.getEffectiveColor());
        }

        int cardIdx = selectedSingle;
        Point from = GameAnim.getPlayerHandCardCenter(ui, this, cardIdx);
        Point to = GameAnim.getAttackPanelCenter(ui, this);

        playerHand.remove(cardIdx);
        discardPile.addFirst(card);
        hasPlayedThisTurn = true;
        selectedSingle = -1;
        updateDisplay();


        if (card.isPotion()) {
             playerChar.heal(5);
             GameAnim.playFlyAnimation(this, card, from, to, () -> {
                 showAIAttackCard(card);
                 showAttackDesc("🧪 恢复5点生命，可继续出牌");
                 GameAnim.playFloatingText(this, "+5", new Color(60, 220, 60),
                     new Point(getWidth() / 2, getHeight() * 3 / 4));
                currentPhase = Phase.PLAYER_PLAY;
                updateDisplay();
            });
            return;
        }

        if (card.isPurify()) {
            GameAnim.playFlyAnimation(this, card, from, to, () -> {
                showAIAttackCard(card);
                applyPurifyEffect(playerChar, true);
                currentPhase = Phase.PLAYER_PLAY;
                updateDisplay();
            });
            return;
        }

        if (card.isSuperPurify()) {
            GameAnim.playFlyAnimation(this, card, from, to, () -> {
                showAIAttackCard(card);
                applySuperPurifyEffect(playerChar, true);
                currentPhase = Phase.PLAYER_PLAY;
                updateDisplay();
            });
            return;
        }

        if (card.isSwapHand()) {
            GameAnim.playFlyAnimation(this, card, from, to, () -> {
                showAIAttackCard(card);
                List<Card> temp = new ArrayList<>(playerHand);
                playerHand.clear();
                playerHand.addAll(ai.getHand());
                ai.getHand().clear();
                ai.getHand().addAll(temp);
                showAttackDesc("🔄 交换双方手牌，可继续出牌");
                GameAnim.playFloatingText(this, "🔄交换", new Color(100, 80, 200),
                    new Point(getWidth() / 2, getHeight() / 2 - 30));
                currentPhase = Phase.PLAYER_PLAY;
                updateDisplay();
            });
            return;
        }

        if (card.isDrawThree()) {
            GameAnim.playFlyAnimation(this, card, from, to, () -> {
                showAIAttackCard(card);
                showAttackDesc("+3 抽3张牌，可继续出牌");
                List<Card> drawn = drawFromDeck(3);
                if (!drawn.isEmpty()) {
                    updateDisplay();
                    GameAnim.playDrawAnimations(this, drawn.size(), true, () -> {
                        playerHand.addAll(drawn);
                        currentPhase = Phase.PLAYER_PLAY;

                        updateDisplay();
                    });
                } else {
                    currentPhase = Phase.PLAYER_PLAY;
                    updateDisplay();
                }
            });
            return;
        }

        if (card.isBlack()) {
            GameAnim.playFlyAnimation(this, card, from, to, () -> {
                showAIAttackCard(card);
                Runnable afterEffect = () -> {
                    currentPhase = Phase.PLAYER_PLAY;
                    updateDisplay();
                };
                if (card.isDrawTwo()) {
                    List<Card> drawn = drawFromDeck(2);
                    if (!drawn.isEmpty()) {
                        updateDisplay();
                        GameAnim.playDrawAnimations(this, drawn.size(), true, () -> {
                            playerHand.addAll(drawn);
                            applyAttackEffect(card, playerChar, aiChar, playerHand, afterEffect);
                        });
                        return;
                    }
                }
                applyAttackEffect(card, playerChar, aiChar, playerHand, afterEffect);
            });
        } else {
            GameAnim.playFlyAnimation(this, card, from, to, () -> {
                showAIAttackCard(card);
                applyAttackEffect(card, playerChar, aiChar, playerHand, () -> {
                    if (forceOpponentDiscardOne && !ai.getHand().isEmpty()) {
                        forceOpponentDiscardOne = false;
                        pendingFiveChoice = true;
                        currentPhase = Phase.PLAYER_SEVEN_CHOICE;
                        updateDisplay();
                    } else if (pendingAttack != null && pendingAttack.skipDefense) {
                        forceOpponentDiscardOne = false;
                        showDefendDesc("跳过防御");
                        Timer skipTimer = new Timer(DELAY_SKIP, ev -> {
                            ((Timer)ev.getSource()).stop();
                            resolvePostDefense(playerChar, aiChar);
                            clearAIZones();
                            currentPhase = Phase.PLAYER_PLAY;
                            updateDisplay();
                        });
                        skipTimer.start();
                    } else if (pendingAttack != null) {
                        forceOpponentDiscardOne = false;
                        currentPhase = Phase.AI_DEFEND;
                        updateDisplay();
                        aiTimer = new Timer(DELAY_PLAY, e -> {
                            aiTimer.stop();
                            doAIDefend();
                        });
                        aiTimer.start();
                    } else {
                        forceOpponentDiscardOne = false;
                        currentPhase = Phase.PLAYER_PLAY;
                        updateDisplay();
                    }
                });
            });
        }
    }

    void handleRevealTopDeck(GameCharacter self, GameCharacter opponent, List<Card> selfHand) {
        CharacterHandler h = getHandler(self);
        if (h != null) h.handleRevealTopDeck(self, opponent, selfHand);
    }

    void handleAIRevealTopDeck(GameCharacter self, GameCharacter opponent, List<Card> selfHand) {
        CharacterHandler h = getHandler(self);
        if (h != null) h.handleAIRevealTopDeck(self, opponent, selfHand);
    }

    void handleRevealDraw(GameCharacter self, GameCharacter opponent, List<Card> selfHand, boolean isPlayer) {
        CharacterHandler h = getHandler(self);
        if (h != null) h.handleRevealDraw(self, opponent, selfHand, isPlayer);
    }


    void doEnterDiscard() {
        if (busy) return;
        if (hasPlayedThisTurn) {
            showMessage("本回合已出牌，不能再弃牌！");
            return;
        }
        selectedSingle = -1;
        selectedMulti.clear();
        currentPhase = Phase.PLAYER_DISCARD;
        updateDisplay();
    }

    void doConfirmDiscard() {
        if (busy) return;
        if (selectedMulti.isEmpty()) {
            showMessage("请选择要弃掉的牌！");
            return;
        }
        List<Integer> sorted = new ArrayList<>(selectedMulti);
        sorted.sort((a, b) -> b - a);
        for (int idx : sorted) {
            if (idx < playerHand.size()) {
                discardPile.addLast(playerHand.remove(idx));
            }
        }
        selectedMulti.clear();

        if (forcedDiscard) {
            if (playerHand.size() > 5) {
                currentPhase = Phase.PLAYER_DISCARD;
                updateDisplay();
                showMessage("仍需弃牌！手牌必须 ≤ 5 张");
                return;
            }
            forcedDiscard = false;
            proceedAfterTurnEnd();
            return;
        }

        finishPlayerTurn();
    }

    void doCancelDiscard() {
        if (forcedDiscard) {
            showMessage("手牌超过5张，不能取消弃牌！");
            return;
        }
        selectedMulti.clear();
        currentPhase = Phase.PLAYER_PLAY;
        updateDisplay();
    }

    void doEndTurn() {
        if (busy) return;
        if (currentPhase != Phase.PLAYER_PLAY) return;
        if (playerHand.size() > 5) {
            forcedDiscard = true;
            hasPlayedThisTurn = false;
            selectedSingle = -1;
            selectedMulti.clear();
            currentPhase = Phase.PLAYER_DISCARD;
            updateDisplay();
            showMessage("手牌超过5张，请弃牌至不超过5张！");
            return;
        }
        busy = true;
        currentPhase = Phase.AI_TURN;
        finishPlayerTurn();
    }

    protected void doAIDefend() {
        Card top = discardPile.getFirst();

        // === 已打出黑牌搭桥，这是第二步：出真正的防御牌 ===
        if (aiHasPlayedBlackDefend) {
            aiHasPlayedBlackDefend = false;
            ai.aiHasDebuff = aiChar.hasDebuff();
            ai.aiDebuffCount = aiChar.getBurnStacks() + (aiChar.isFrozen() ? 1 : 0) + aiChar.getBleedStacks();
            ai.aiFullHp = aiChar.getCurrentHp() >= aiChar.getMaxHp();
            ai.aiOpponentHandSize = playerHand.size();
            Card defCard = ai.chooseDefend(top, true);
            if (defCard != null) {
                if (defCard.isWhite()) {
                    defCard.setChosenColor(top.getEffectiveColor());
                }
                Point from = GameAnim.getAIHandCenter(ui, this);
                Point to = GameAnim.getAttackPanelCenter(ui, this);

                ai.removeCard(defCard);
                discardPile.addFirst(defCard);
                pendingDefendCard = null;

                // 继续搭桥
                if (defCard.isBlack()) {
                    aiHasPlayedBlackDefend = true;
                    GameAnim.playFlyAnimation(this, defCard, from, to, () -> {
                        if (defCard.isDrawTwo()) {
                            List<Card> drawn = drawFromDeck(2);
                            if (!drawn.isEmpty()) {
                                GameAnim.playDrawAnimations(this, drawn.size(), false, () -> {
                                    ai.addCards(drawn);
                                    updateDisplay();
                                    Timer bridgeTimer = new Timer(DELAY_STEP, e2 -> {
                                        ((Timer)e2.getSource()).stop();
                                        doAIDefend();
                                    });
                                    bridgeTimer.start();
                                });
                            } else {
                                Timer bridgeTimer = new Timer(DELAY_STEP, e2 -> {
                                        ((Timer)e2.getSource()).stop();
                                        doAIDefend();
                                    });
                                    bridgeTimer.start();
                            }
                        } else {
                            Timer bridgeTimer = new Timer(DELAY_STEP, e2 -> {
                                ((Timer)e2.getSource()).stop();
                                doAIDefend();
                            });
                            bridgeTimer.start();
                        }
                    });
                    return;
                }

                if (defCard.isPotion()) {
                    aiHasPlayedBlackDefend = true;
                    aiChar.heal(5);
                    GameAnim.playFlyAnimation(this, defCard, from, to, () -> {
                        GameAnim.playFloatingText(this, "+5", new Color(60, 220, 60),
                            new Point(getWidth() / 2, getHeight() / 3));
                        Timer bridgeTimer = new Timer(DELAY_STEP, e2 -> {
                            ((Timer)e2.getSource()).stop();
                            doAIDefend();
                        });
                        bridgeTimer.start();
                    });
                    return;
                }

                if (defCard.isPurify()) {
                    aiHasPlayedBlackDefend = true;
                    GameAnim.playFlyAnimation(this, defCard, from, to, () -> {
                        applyPurifyEffect(aiChar, false);
                        Timer bridgeTimer = new Timer(DELAY_STEP, e2 -> {
                            ((Timer)e2.getSource()).stop();
                            doAIDefend();
                        });
                        bridgeTimer.start();
                    });
                    return;
                }

                if (defCard.isSuperPurify()) {
                    aiHasPlayedBlackDefend = true;
                    GameAnim.playFlyAnimation(this, defCard, from, to, () -> {
                        applySuperPurifyEffect(aiChar, false);
                        Timer bridgeTimer = new Timer(DELAY_STEP, e2 -> {
                            ((Timer)e2.getSource()).stop();
                            doAIDefend();
                        });
                        bridgeTimer.start();
                    });
                    return;
                }

                if (defCard.isDrawThree()) {
                    aiHasPlayedBlackDefend = true;
                    List<Card> drawn = drawFromDeck(3);
                    GameAnim.playFlyAnimation(this, defCard, from, to, () -> {
                        if (!drawn.isEmpty()) {
                            GameAnim.playDrawAnimations(this, drawn.size(), false, () -> {
                                ai.addCards(drawn);
                                updateDisplay();
                                Timer bridgeTimer = new Timer(DELAY_STEP, e2 -> {
                                    ((Timer)e2.getSource()).stop();
                                    doAIDefend();
                                });
                                bridgeTimer.start();
                            });
                        } else {
                            Timer bridgeTimer = new Timer(DELAY_STEP, e2 -> {
                                ((Timer)e2.getSource()).stop();
                                doAIDefend();
                            });
                            bridgeTimer.start();
                        }
                    });
                    return;
                }

                // 非搭桥牌 → 完成防御
                pendingDefendCard = defCard;
                finishAIDefend();
                GameAnim.playFlyAnimation(this, defCard, from, to, () -> {
                    showDefendCard(defCard);
                    updateDisplay();
                });
            } else {
                finishAIDefend();
            }
            return;
        }

        // === 第一步：正常防御 ===
        if (top.isBlack()) {
            showDefendDesc("跳过防御");
            finishAIDefend();
            return;
        }

        boolean isBlueAttack = top.getEffectiveColor() == Card.CardColor.BLUE;
        if (aiChar.isFrozen() && isBlueAttack) {
            showDefendDesc("❄️冷冻 → 无法防御蓝色攻击");
            finishAIDefend();
            return;
        }

        ai.aiHasDebuff = aiChar.hasDebuff();
        ai.aiFullHp = aiChar.getCurrentHp() >= aiChar.getMaxHp();
        ai.aiOpponentHandSize = playerHand.size();
        Card defCard = ai.chooseDefend(top);
        if (defCard != null) {
            if (defCard.isBlack()) {
                Card.CardColor chosen = ai.chooseBlackColor();
                defCard.setChosenColor(chosen);
            }
            if (defCard.isWhite()) {
                defCard.setChosenColor(top.getEffectiveColor());
            }

            Point from = GameAnim.getAIHandCenter(ui, this);
            Point to = GameAnim.getAttackPanelCenter(ui, this);

            ai.removeCard(defCard);
            discardPile.addFirst(defCard);
            pendingDefendCard = defCard;

            aiDefendSuccess = true;
            updateDisplay();
            aiDefendSuccess = false;


            // 🧪牌搭桥 → 恢复5点+留在防御阶段
            if (defCard.isPotion()) {
                aiHasPlayedBlackDefend = true;
                pendingDefendCard = null;
                aiChar.heal(5);
                GameAnim.playFlyAnimation(this, defCard, from, to, () -> {
                    GameAnim.playFloatingText(this, "+5", new Color(60, 220, 60),
                        new Point(getWidth() / 2, getHeight() / 3));
                    Timer bridgeTimer = new Timer(DELAY_STEP, e2 -> {
                        ((Timer)e2.getSource()).stop();
                        doAIDefend();
                    });
                    bridgeTimer.start();
                });
            }
            // ✨净化牌搭桥 → 净化1层buff+留在防御阶段
            else if (defCard.isPurify()) {
                aiHasPlayedBlackDefend = true;
                pendingDefendCard = null;
                GameAnim.playFlyAnimation(this, defCard, from, to, () -> {
                    applyPurifyEffect(aiChar, false);
                    Timer bridgeTimer = new Timer(DELAY_STEP, e2 -> {
                        ((Timer)e2.getSource()).stop();
                        doAIDefend();
                    });
                    bridgeTimer.start();
                });
            }
            // ✨✨超级净化牌搭桥 → 清除所有buff+留在防御阶段
            else if (defCard.isSuperPurify()) {
                aiHasPlayedBlackDefend = true;
                pendingDefendCard = null;
                GameAnim.playFlyAnimation(this, defCard, from, to, () -> {
                    applySuperPurifyEffect(aiChar, false);
                    Timer bridgeTimer = new Timer(DELAY_STEP, e2 -> {
                        ((Timer)e2.getSource()).stop();
                        doAIDefend();
                    });
                    bridgeTimer.start();
                });
            }
            // +3牌搭桥 → 抽3张+留在防御阶段
            else if (defCard.isDrawThree()) {
                aiHasPlayedBlackDefend = true;
                pendingDefendCard = null;
                List<Card> drawn = drawFromDeck(3);
                GameAnim.playFlyAnimation(this, defCard, from, to, () -> {
                    if (!drawn.isEmpty()) {
                        GameAnim.playDrawAnimations(this, drawn.size(), false, () -> {
                            ai.addCards(drawn);
                            updateDisplay();
                            Timer bridgeTimer = new Timer(DELAY_STEP, e2 -> {
                                ((Timer)e2.getSource()).stop();
                                doAIDefend();
                            });
                            bridgeTimer.start();
                        });
                    } else {
                        Timer bridgeTimer = new Timer(DELAY_STEP, e2 -> {
                            ((Timer)e2.getSource()).stop();
                            doAIDefend();
                        });
                        bridgeTimer.start();
                    }
                });
            }
            // 黑牌搭桥 → 留在防御阶段，等timer再次调用doAIDefend
            else if (defCard.isBlack()) {
                aiHasPlayedBlackDefend = true;
                pendingDefendCard = null;
                GameAnim.playFlyAnimation(this, defCard, from, to, () -> {
                    if (defCard.isDrawTwo()) {
                        List<Card> drawn = drawFromDeck(2);
                        if (!drawn.isEmpty()) {
                            updateDisplay();
                            GameAnim.playDrawAnimations(this, drawn.size(), false, () -> {
                                ai.addCards(drawn);
                                updateDisplay();
                                Timer bridgeTimer = new Timer(DELAY_STEP, e2 -> {
                                    ((Timer)e2.getSource()).stop();
                                    doAIDefend();
                                });
                                bridgeTimer.start();
                            });
                            return;
                        }
                    }
                    updateDisplay();
                    Timer bridgeTimer = new Timer(DELAY_STEP, e2 -> {
                        ((Timer)e2.getSource()).stop();
                        doAIDefend();
                    });
                    bridgeTimer.start();
                });
            } else {
                finishAIDefend();
                GameAnim.playFlyAnimation(this, defCard, from, to, () -> {
                    showDefendCard(defCard);
                    updateDisplay();
                });
            }
        } else {
            pendingDefendCard = null;
            showDefendDesc("跳过防御");
            Timer delay = new Timer(DELAY_STEP, e2 -> {
                ((Timer)e2.getSource()).stop();
                finishAIDefend();
            });
            delay.start();
        }
    }

    void doPlayerDefend() {
        if (busy) return;
        if (currentPhase != Phase.PLAYER_DEFEND) return;
        busy = true;
        if (selectedSingle < 0 || selectedSingle >= playerHand.size()) {
            busy = false;
            showMessage("请选择一张牌进行防御！");
            return;
        }
        if (discardPile.isEmpty()) { busy = false; return; }

        Card card = playerHand.get(selectedSingle);
        Card top = discardPile.getFirst();

        // === 已打出黑牌搭桥，这是第二步：出真正的防御牌 ===
        if (hasPlayedBlackDefend) {
            if (card.isBlack()) {
                Card.CardColor chosen = GameUI.showColorChooser(this);
                if (chosen == null) { busy = false; return; }
                card.setChosenColor(chosen);

                int cardIdx = selectedSingle;
                Point from = GameAnim.getPlayerHandCardCenter(ui, this, cardIdx);
                Point to = GameAnim.getAttackPanelCenter(ui, this);

                playerHand.remove(cardIdx);
                discardPile.addFirst(card);
                selectedSingle = -1;
                pendingDefendCard = null;

                GameAnim.playFlyAnimation(this, card, from, to, () -> {
                    if (card.isDrawTwo()) {
                        List<Card> drawn = drawFromDeck(2);
                        if (!drawn.isEmpty()) {
                            GameAnim.playDrawAnimations(this, drawn.size(), true, () -> {
                                playerHand.addAll(drawn);
                                busy = false;
                                updateDisplay();
                            });
                            return;
                        }
                    }
                    busy = false;
                    updateDisplay();
                });
                return;
            }

            if (card.isPotion()) {
                card.setChosenColor(top.getEffectiveColor());

                int cardIdx = selectedSingle;
                Point from = GameAnim.getPlayerHandCardCenter(ui, this, cardIdx);
                Point to = GameAnim.getAttackPanelCenter(ui, this);

                playerHand.remove(cardIdx);
                discardPile.addFirst(card);
                selectedSingle = -1;
                pendingDefendCard = null;
                hasPlayedBlackDefend = true;

                playerChar.heal(5);
                GameAnim.playFlyAnimation(this, card, from, to, () -> {
                    GameAnim.playFloatingText(this, "+5", new Color(60, 220, 60),
                        new Point(getWidth() / 2, getHeight() * 3 / 4));
                    busy = false;
                    updateDisplay();
                });
                return;
            }

            if (card.isDrawThree()) {
                card.setChosenColor(top.getEffectiveColor());

                int cardIdx = selectedSingle;
                Point from = GameAnim.getPlayerHandCardCenter(ui, this, cardIdx);
                Point to = GameAnim.getAttackPanelCenter(ui, this);

                playerHand.remove(cardIdx);
                discardPile.addFirst(card);
                selectedSingle = -1;
                pendingDefendCard = null;
                hasPlayedBlackDefend = true;

                List<Card> drawn = drawFromDeck(3);
                GameAnim.playFlyAnimation(this, card, from, to, () -> {
                    if (!drawn.isEmpty()) {
                        GameAnim.playDrawAnimations(this, drawn.size(), true, () -> {
                            playerHand.addAll(drawn);
                            busy = false;
                            updateDisplay();
                        });
                        return;
                    }
                    busy = false;
                    updateDisplay();
                });
                return;
            }

            if (card.isPurify()) {
                card.setChosenColor(top.getEffectiveColor());

                int cardIdx = selectedSingle;
                Point from = GameAnim.getPlayerHandCardCenter(ui, this, cardIdx);
                Point to = GameAnim.getAttackPanelCenter(ui, this);

                playerHand.remove(cardIdx);
                discardPile.addFirst(card);
                selectedSingle = -1;
                pendingDefendCard = null;

                GameAnim.playFlyAnimation(this, card, from, to, () -> {
                    applyPurifyEffect(playerChar, true);
                    busy = false;
                    updateDisplay();
                });
                return;
            }

            if (card.isSuperPurify()) {
                card.setChosenColor(top.getEffectiveColor());

                int cardIdx = selectedSingle;
                Point from = GameAnim.getPlayerHandCardCenter(ui, this, cardIdx);
                Point to = GameAnim.getAttackPanelCenter(ui, this);

                playerHand.remove(cardIdx);
                discardPile.addFirst(card);
                selectedSingle = -1;
                pendingDefendCard = null;

                GameAnim.playFlyAnimation(this, card, from, to, () -> {
                    applySuperPurifyEffect(playerChar, true);
                    busy = false;
                    updateDisplay();
                });
                return;
            }

            if (!canDefend(card, top)) {
                busy = false;
                showMessage("该牌无法防御！需数字≤3且颜色匹配" + colorName(top.getEffectiveColor()));
                return;
            }

            if (card.isWhite()) {
                card.setChosenColor(top.getEffectiveColor());
            }

            int cardIdx = selectedSingle;
            Point from = GameAnim.getPlayerHandCardCenter(ui, this, cardIdx);
            Point to = GameAnim.getAttackPanelCenter(ui, this);

            playerHand.remove(cardIdx);
            discardPile.addFirst(card);
            selectedSingle = -1;
            hasPlayedBlackDefend = false;
            pendingDefendCard = card;

            GameAnim.playFlyAnimation(this, card, from, to, () -> {
                showDefendCard(card);
                finishPlayerDefend();
            });
            return;
        }

        // === 第一步：正常防御 ===
        if (!canDefend(card, top)) {
            busy = false;
            showMessage("该牌无法防御！需数字≤3且颜色或数字匹配弃牌库顶");
            return;
        }

        // 出黑牌 → 改变颜色搭桥，留在防御阶段
        if (card.isBlack()) {
            Card.CardColor chosen = GameUI.showColorChooser(this);
            if (chosen == null) { busy = false; return; }
            card.setChosenColor(chosen);

            int cardIdx = selectedSingle;
            Point from = GameAnim.getPlayerHandCardCenter(ui, this, cardIdx);
            Point to = GameAnim.getAttackPanelCenter(ui, this);

            playerHand.remove(cardIdx);
            discardPile.addFirst(card);
            selectedSingle = -1;
            pendingDefendCard = null;
            hasPlayedBlackDefend = true;

            GameAnim.playFlyAnimation(this, card, from, to, () -> {
                if (card.isDrawTwo()) {
                    List<Card> drawn = drawFromDeck(2);
                    if (!drawn.isEmpty()) {
                        GameAnim.playDrawAnimations(this, drawn.size(), true, () -> {
                            playerHand.addAll(drawn);
                            busy = false;
                            updateDisplay();
                        });
                        return;
                    }
                }
                busy = false;
                updateDisplay();
            });
            return;
        }

        // 出🧪牌 → 恢复5点+搭桥，留在防御阶段
        if (card.isPotion()) {
            card.setChosenColor(top.getEffectiveColor());

            int cardIdx = selectedSingle;
            Point from = GameAnim.getPlayerHandCardCenter(ui, this, cardIdx);
            Point to = GameAnim.getAttackPanelCenter(ui, this);

            playerHand.remove(cardIdx);
            discardPile.addFirst(card);
            selectedSingle = -1;
            pendingDefendCard = null;
            hasPlayedBlackDefend = true;

            playerChar.heal(5);
            GameAnim.playFlyAnimation(this, card, from, to, () -> {
                GameAnim.playFloatingText(this, "+5", new Color(60, 220, 60),
                    new Point(getWidth() / 2, getHeight() * 3 / 4));
                busy = false;
                updateDisplay();
            });
            return;
        }

        // 出✨净化牌 → 净化1层buff+搭桥，留在防御阶段
        if (card.isPurify()) {
            card.setChosenColor(top.getEffectiveColor());

            int cardIdx = selectedSingle;
            Point from = GameAnim.getPlayerHandCardCenter(ui, this, cardIdx);
            Point to = GameAnim.getAttackPanelCenter(ui, this);

            playerHand.remove(cardIdx);
            discardPile.addFirst(card);
            selectedSingle = -1;
            pendingDefendCard = null;
            hasPlayedBlackDefend = true;

            GameAnim.playFlyAnimation(this, card, from, to, () -> {
                applyPurifyEffect(playerChar, true);
                busy = false;
                updateDisplay();
            });
            return;
        }

        // 出✨✨超级净化牌 → 清除所有buff+搭桥，留在防御阶段
        if (card.isSuperPurify()) {
            card.setChosenColor(top.getEffectiveColor());

            int cardIdx = selectedSingle;
            Point from = GameAnim.getPlayerHandCardCenter(ui, this, cardIdx);
            Point to = GameAnim.getAttackPanelCenter(ui, this);

            playerHand.remove(cardIdx);
            discardPile.addFirst(card);
            selectedSingle = -1;
            pendingDefendCard = null;
            hasPlayedBlackDefend = true;

            GameAnim.playFlyAnimation(this, card, from, to, () -> {
                applySuperPurifyEffect(playerChar, true);
                busy = false;
                updateDisplay();
            });
            return;
        }

        // 出+3牌 → 抽3张+搭桥，留在防御阶段
        if (card.isDrawThree()) {
            card.setChosenColor(top.getEffectiveColor());

            int cardIdx = selectedSingle;
            Point from = GameAnim.getPlayerHandCardCenter(ui, this, cardIdx);
            Point to = GameAnim.getAttackPanelCenter(ui, this);

            playerHand.remove(cardIdx);
            discardPile.addFirst(card);
            selectedSingle = -1;
            pendingDefendCard = null;
            hasPlayedBlackDefend = true;

            List<Card> drawn = drawFromDeck(3);
            GameAnim.playFlyAnimation(this, card, from, to, () -> {
                if (!drawn.isEmpty()) {
                    GameAnim.playDrawAnimations(this, drawn.size(), true, () -> {
                        playerHand.addAll(drawn);
                        busy = false;
                        updateDisplay();
                    });
                    return;
                }
                busy = false;
                updateDisplay();
            });
            return;
        }

        // 出白牌 → 自动指定为弃牌库顶颜色
        if (card.isWhite()) {
            card.setChosenColor(top.getEffectiveColor());
        }

        // 出非黑非白牌 → 直接防御成功
        int cardIdx = selectedSingle;
        Point from = GameAnim.getPlayerHandCardCenter(ui, this, cardIdx);
        Point to = GameAnim.getAttackPanelCenter(ui, this);

        playerHand.remove(cardIdx);
        discardPile.addFirst(card);
        selectedSingle = -1;
        pendingDefendCard = card;

        GameAnim.playFlyAnimation(this, card, from, to, () -> {
            showDefendCard(card);
            finishPlayerDefend();
        });
    }

    protected String colorName(Card.CardColor c) {
        switch (c) {
            case RED:    return "红";
            case YELLOW: return "黄";
            case BLUE:   return "蓝";
            case GREEN:  return "绿";
            case WHITE:  return "白";
            default:     return "";
        }
    }

    void doPlayerSkipDefend() {
        if (busy) return;
        busy = true;
        if (chanSevenKeepMode) {
            CharacterHandler h = getHandler(playerChar);
            if (h instanceof ChanHandler) ((ChanHandler) h).doChanSevenDiscard();
            return;
        }
        if (chanFourSwapMode) {
            CharacterHandler h = getHandler(playerChar);
            if (h instanceof ChanHandler) ((ChanHandler) h).doChanFourDiscard();
            return;
        }
        hasPlayedBlackDefend = false;
        selectedSingle = -1;
        pendingDefendCard = null;
        showDefendDesc("跳过防御");
        finishPlayerDefend();
    }

    void doChanSevenKeep() {
        CharacterHandler h = getHandler(playerChar);
        if (h instanceof ChanHandler) ((ChanHandler) h).doChanSevenKeep();
    }

    void doChanFourSwapConfirm() {
        if (selectedSingle < 0 || selectedSingle >= playerHand.size()) return;
        CharacterHandler h = getHandler(playerChar);
        if (h instanceof ChanHandler) ((ChanHandler) h).doChanFourSwapChoice(selectedSingle);
    }

    void doSaikiSixConfirm() {
        CharacterHandler h = getHandler(playerChar);
        if (h instanceof SaikiHandler) ((SaikiHandler) h).doSaikiSixConfirm();
    }

    void doChanFourOpponentConfirm() {
        if (selectedAICard < 0 || selectedAICard >= ai.handSize()) return;
        CharacterHandler h = getHandler(playerChar);
        if (currentPhase == Phase.SAIKI_THREE_CHOICE && h instanceof SaikiHandler) {
            ((SaikiHandler) h).doSaikiThreeOpponentSelected(selectedAICard);
        } else if (currentPhase == Phase.SAIKI_THREE_CHOICE && h instanceof BlazeHandler) {
            ((BlazeHandler) h).doBlazeFourOpponentSelected(selectedAICard);
        } else if (h instanceof ChanHandler) {
            ((ChanHandler) h).doChanFourOpponentSelected(selectedAICard);
        }
    }

    void doSevenChoiceConfirm() {
        if (selectedAICard < 0 || selectedAICard >= ai.handSize()) return;
        doSevenChoice(selectedAICard);
    }

    void doSevenChoice(int aiCardIndex) {
        if (chanSevenMode && !chanSevenKeepMode) {
            CharacterHandler h = getHandler(playerChar);
            if (h instanceof ChanHandler) ((ChanHandler) h).doChanSevenChoice(aiCardIndex);
            return;
        }
        if (chanFourSwapMode) {
            CharacterHandler h = getHandler(playerChar);
            if (h instanceof ChanHandler) ((ChanHandler) h).doChanFourSwapChoice(aiCardIndex);
            return;
        }

        CharacterHandler h = getHandler(playerChar);
        if (h != null) h.doSevenChoice(aiCardIndex);
    }

    void doFiveChoiceHeal() {
        if (currentPhase == Phase.SAIKI_THREE_CHOICE) {
            ((SaikiHandler) getHandler(playerChar)).doSaikiThreeKeep();
            return;
        }
        CharacterHandler h = getHandler(playerChar);
        if (h != null) h.doFiveChoiceHeal();
    }

    void doFiveChoiceDamage() {
        if (currentPhase == Phase.SAIKI_THREE_CHOICE) {
            ((SaikiHandler) getHandler(playerChar)).doSaikiThreeDiscard();
            return;
        }
        CharacterHandler h = getHandler(playerChar);
        if (h != null) h.doFiveChoiceDamage();
    }

    protected void enterPlayerDefend() {
        boolean isBlueAtk = !discardPile.isEmpty() && discardPile.getFirst().getEffectiveColor() == Card.CardColor.BLUE;
        if (playerChar.isFrozen() && isBlueAtk) {
            showDefendDesc("❄️冷冻 → 无法防御蓝色攻击");
            Timer frozenTimer = new Timer(DELAY_EFFECT, e -> {
                ((Timer)e.getSource()).stop();
                finishPlayerDefend();
            });
            frozenTimer.start();
        } else {
            currentPhase = Phase.PLAYER_DEFEND;
            selectedSingle = -1;
            busy = false;
            updateDisplay();
        }
    }

    protected void startAITurn() {
        currentPhase = Phase.AI_TURN;
        aiHasPlayed = false;

        updateDisplay();
        aiTimer = new Timer(DELAY_STEP, e -> executeAIStep());
        aiTimer.start();
    }

    protected void resumeAITurn() {
        if (!playerChar.isAlive() || !aiChar.isAlive()) return;
        aiTimer = new Timer(DELAY_STEP, e -> executeAIStep());
        aiTimer.start();
    }

    protected void executeAIStep() {
        if (!playerChar.isAlive() || !aiChar.isAlive()) return;
        if (discardPile.isEmpty()) {
            aiTimer.stop();
            finishAITurn();
            return;
        }

        Card top = discardPile.getFirst();

        if (ai.hasPlayableCard(top)) {
            ai.aiDebuffCount = aiChar.getBurnStacks() + (aiChar.isFrozen() ? 1 : 0) + aiChar.getBleedStacks();
            ai.aiOpponentHandSize = playerHand.size();
            Card toPlay = ai.choosePlay(top, aiChar.hasDebuff());
            if (toPlay != null) {
                if (toPlay.isBlack()) {
                    Card.CardColor chosen = ai.chooseBlackColor();
                    toPlay.setChosenColor(chosen);
                }
                if (toPlay.isWhite()) {
                    toPlay.setChosenColor(top.getEffectiveColor());
                }

                ai.removeCard(toPlay);
                discardPile.addFirst(toPlay);
                aiHasPlayed = true;
                aiTimer.stop();


                if (toPlay.isPotion()) {
                    Point from = GameAnim.getAIHandCenter(ui, this);
                    Point to = GameAnim.getAttackPanelCenter(ui, this);
                    aiChar.heal(5);
                    GameAnim.playFlyAnimation(this, toPlay, from, to, () -> {
                        showAIAttackCard(toPlay);
                        showAttackDesc("🧪 AI恢复5点生命，继续出牌");
                        GameAnim.playFloatingText(this, "+5", new Color(60, 220, 60),
                            new Point(getWidth() / 2, getHeight() / 3));
                        updateDisplay();
                        resumeAITurn();
                    });
                } else if (toPlay.isPurify()) {
                    Point from = GameAnim.getAIHandCenter(ui, this);
                    Point to = GameAnim.getAttackPanelCenter(ui, this);
                    GameAnim.playFlyAnimation(this, toPlay, from, to, () -> {
                        showAIAttackCard(toPlay);
                        applyPurifyEffect(aiChar, false);
                        updateDisplay();
                        resumeAITurn();
                    });
                } else if (toPlay.isSuperPurify()) {
                    Point from = GameAnim.getAIHandCenter(ui, this);
                    Point to = GameAnim.getAttackPanelCenter(ui, this);
                    GameAnim.playFlyAnimation(this, toPlay, from, to, () -> {
                        showAIAttackCard(toPlay);
                        applySuperPurifyEffect(aiChar, false);
                        updateDisplay();
                        resumeAITurn();
                    });
                } else if (toPlay.isSwapHand()) {
                    Point from = GameAnim.getAIHandCenter(ui, this);
                    Point to = GameAnim.getAttackPanelCenter(ui, this);
                    GameAnim.playFlyAnimation(this, toPlay, from, to, () -> {
                        showAIAttackCard(toPlay);
                        List<Card> temp = new ArrayList<>(playerHand);
                        playerHand.clear();
                        playerHand.addAll(ai.getHand());
                        ai.getHand().clear();
                        ai.getHand().addAll(temp);
                        showAttackDesc("🔄 AI交换双方手牌，继续出牌");
                        GameAnim.playFloatingText(this, "🔄交换", new Color(100, 80, 200),
                            new Point(getWidth() / 2, getHeight() / 2 - 30));
                        updateDisplay();
                        resumeAITurn();
                    });
                } else if (toPlay.isDrawThree()) {
                    Point from = GameAnim.getAIHandCenter(ui, this);
                    Point to = GameAnim.getAttackPanelCenter(ui, this);
                    GameAnim.playFlyAnimation(this, toPlay, from, to, () -> {
                        showAIAttackCard(toPlay);
                        showAttackDesc("+3 AI抽3张牌，继续出牌");
                        List<Card> drawn = drawFromDeck(3);
                        if (!drawn.isEmpty()) {
                            updateDisplay();
                            GameAnim.playDrawAnimations(this, drawn.size(), false, () -> {
                                ai.addCards(drawn);
                                checkHandLimit(ai.getHand(), false);
                                updateDisplay();
                                resumeAITurn();
                            });
                        } else {
                            updateDisplay();
                            resumeAITurn();
                        }
                    });
                } else if (toPlay.isBlack()) {
                    Point from = GameAnim.getAIHandCenter(ui, this);
                    Point to = GameAnim.getAttackPanelCenter(ui, this);
                    GameAnim.playFlyAnimation(this, toPlay, from, to, () -> {
                        showAIAttackCard(toPlay);
                        Runnable afterEffect = () -> {
                            updateDisplay();
                            resumeAITurn();
                        };
                        if (toPlay.isDrawTwo()) {
                            List<Card> drawn = drawFromDeck(2);
                            if (!drawn.isEmpty()) {
                                updateDisplay();
                                GameAnim.playDrawAnimations(this, drawn.size(), false, () -> {
                                    ai.addCards(drawn);
                                    applyAttackEffect(toPlay, aiChar, playerChar, ai.getHand(), afterEffect);
                                });
                                return;
                            }
                        }
                        applyAttackEffect(toPlay, aiChar, playerChar, ai.getHand(), afterEffect);
                    });
                } else {

                    Point from = GameAnim.getAIHandCenter(ui, this);
                    Point to = GameAnim.getAttackPanelCenter(ui, this);
                    GameAnim.playFlyAnimation(this, toPlay, from, to, () -> {
                        showAIAttackCard(toPlay);
                        applyAttackEffect(toPlay, aiChar, playerChar, ai.getHand(), () -> {
                            if (pendingAttack != null && pendingAttack.skipDefense) {
                                showDefendDesc("跳过防御");
                                Timer skipTimer = new Timer(DELAY_SKIP, ev -> {
                                    ((Timer)ev.getSource()).stop();
                                    resolvePostDefense(aiChar, playerChar);
                                    clearAIZones();
                                    checkHandLimit(ai.getHand(), false);
                                    updateDisplay();
                                    finishAITurn();
                                });
                                skipTimer.start();
                            } else if (pendingAttack != null) {
                                enterPlayerDefend();
                            } else {
                                checkHandLimit(ai.getHand(), false);
                                finishAITurn();
                            }
                        });
                    });
                }
                return;
            }
        }

        aiTimer.stop();

        if (!aiHasPlayed) {
            List<Card> discards = ai.chooseDiscards();
            for (Card c : discards) {
                discardPile.addLast(c);
            }
            ai.clear();
        }

        finishAITurn();
    }

    protected void refillDeckIfNeeded() {
        if (!deck.isEmpty() || discardPile.isEmpty()) return;
        Card topCard = discardPile.removeFirst();
        for (Card c : discardPile) {
            if (c.isBlack() || c.isWhite()) {
                c.setChosenColor(null);
            }
        }
        deck.addCards(discardPile);
        discardPile.clear();
        discardPile.addFirst(topCard);
        deck.shuffle();
    }

    /** Draw cards from deck (handling reshuffle), returns cards NOT yet added to hand */
    protected List<Card> drawFromDeck(int count) {
        List<Card> result = new ArrayList<>();
        while (result.size() < count) {
            refillDeckIfNeeded();
            List<Card> drawn = deck.draw(count - result.size());
            if (drawn.isEmpty()) break;
            result.addAll(drawn);
        }
        return result;
    }

    protected int needToFill(List<Card> hand, int targetSize) {
        return Math.max(0, targetSize - hand.size());
    }

    protected void finishPlayerTurn() {

        List<Card> pCards = drawFromDeck(needToFill(playerHand, 5));
        List<Card> aCards = drawFromDeck(needToFill(ai.getHand(), 5));
        int total = pCards.size() + aCards.size();

        if (total > 0) {
            updateDisplay();
            GameAnim.playDrawAnimations(this, total, true, () -> {
                playerHand.addAll(pCards);
                ai.getHand().addAll(aCards);
                updateDisplay();
                proceedAfterTurnEnd();
            });
        } else {
            proceedAfterTurnEnd();
        }
    }

    protected void proceedAfterTurnEnd() {
        hasPlayedThisTurn = false;
        selectedSingle = -1;
        selectedMulti.clear();

        applyBurnDamage(playerChar);
        if (checkDeath()) return;

        aiChar.applyPassive();
        updateDisplay();
        startAITurn();
    }

    protected void applyBurnDamage(GameCharacter target) {
        if (target.getBurnStacks() > 0) {
            if (!target.isImmuneToBurn()) {
                int burnDmg = target.getBurnStacks();
                target.takeDamage(burnDmg);
                Point loc = target == playerChar
                    ? new Point(getWidth() / 2, getHeight() * 3 / 4)
                    : new Point(getWidth() / 2, getHeight() / 3);
                GameAnim.playFloatingText(this, "🔥-" + burnDmg, new Color(255, 100, 0), loc);
            }
            target.removeBurn(1);
        }
    }

    protected boolean checkDeath() {
        if (!playerChar.isAlive()) {
            Timer t = new Timer(500, ev -> { ((Timer)ev.getSource()).stop(); endGame("AI"); });
            t.setRepeats(false);
            t.start();
            return true;
        }
        if (!aiChar.isAlive()) {
            Timer t = new Timer(500, ev -> { ((Timer)ev.getSource()).stop(); endGame("你"); });
            t.setRepeats(false);
            t.start();
            return true;
        }
        return false;
    }

    protected void applyPurifyEffect(GameCharacter ch, boolean isPlayer) {
        if (isPlayer) {
            if (!ch.hasDebuff()) {
                showAttackDesc("✨ 无buff可净化，继续出牌");
                return;
            }
            PurifyDialog dialog = new PurifyDialog(this, ch);
            dialog.setVisible(true);
            String choice = dialog.getResult();
            if (choice != null) {
                if (choice.contains("灼烧")) {
                    ch.removeBurn(1);
                    GameAnim.playFloatingText(this, "🔥-1", new Color(255, 140, 0),
                        new Point(getWidth() / 2, getHeight() * 3 / 4 - 60));
                    showAttackDesc("✨ 净化1层灼烧，继续出牌");
                } else if (choice.contains("冷冻")) {
                    ch.setFrozen(false);
                    GameAnim.playFloatingText(this, "❄️解除", new Color(100, 180, 255),
                        new Point(getWidth() / 2, getHeight() * 3 / 4 - 60));
                    showAttackDesc("✨ 解除冷冻，继续出牌");
                } else if (choice.contains("流血")) {
                    ch.removeBleed(1);
                    GameAnim.playFloatingText(this, "🩸-1", new Color(180, 0, 0),
                        new Point(getWidth() / 2, getHeight() * 3 / 4 - 60));
                    showAttackDesc("✨ 净化1层流血，继续出牌");
                }
            } else {
                showAttackDesc("✨ 取消净化，继续出牌");
            }
        } else {
            if (!ch.hasDebuff()) {
                showAttackDesc("✨ AI无buff可净化，继续出牌");
                return;
            }
            java.util.List<String> debuffs = new java.util.ArrayList<>();
            if (ch.getBurnStacks() > 0) debuffs.add("灼烧");
            if (ch.isFrozen()) debuffs.add("冷冻");
            if (ch.getBleedStacks() > 0) debuffs.add("流血");
            String chosen = debuffs.get((int)(Math.random() * debuffs.size()));
            if (chosen.equals("灼烧")) {
                ch.removeBurn(1);
                GameAnim.playFloatingText(this, "🔥-1", new Color(255, 140, 0),
                    new Point(getWidth() / 2, getHeight() / 3 - 60));
                showAttackDesc("✨ AI净化1层灼烧，继续出牌");
            } else if (chosen.equals("冷冻")) {
                ch.setFrozen(false);
                GameAnim.playFloatingText(this, "❄️解除", new Color(100, 180, 255),
                    new Point(getWidth() / 2, getHeight() / 3 - 60));
                showAttackDesc("✨ AI解除冷冻，继续出牌");
            } else if (chosen.equals("流血")) {
                ch.removeBleed(1);
                GameAnim.playFloatingText(this, "🩸-1", new Color(180, 0, 0),
                    new Point(getWidth() / 2, getHeight() / 3 - 60));
                showAttackDesc("✨ AI净化1层流血，继续出牌");
            }
        }
    }

    protected void applySuperPurifyEffect(GameCharacter ch, boolean isPlayer) {
        if (!ch.hasDebuff()) {
            showAttackDesc(isPlayer ? "✨✨ 无buff可净化，继续出牌" : "✨✨ AI无buff可净化，继续出牌");
            return;
        }
        ch.clearAllDebuffs();
        Point loc = isPlayer
            ? new Point(getWidth() / 2, getHeight() * 3 / 4 - 60)
            : new Point(getWidth() / 2, getHeight() / 3 - 60);
        GameAnim.playFloatingText(this, "全部净化", new Color(200, 180, 255), loc);
        showAttackDesc(isPlayer ? "✨✨ 清除所有buff，继续出牌" : "✨✨ AI清除所有buff，继续出牌");
    }

    protected void finishAITurn() {
        while (ai.getHand().size() > 5) {
            Card worst = null;
            for (Card c : ai.getHand()) {
                if (worst == null || c.getValue() < worst.getValue()) worst = c;
            }
            if (worst != null) {
                ai.getHand().remove(worst);
                discardPile.addLast(worst);
            }
        }

        List<Card> pCards = drawFromDeck(needToFill(playerHand, 5));
        List<Card> aCards = drawFromDeck(needToFill(ai.getHand(), 5));
        int total = pCards.size() + aCards.size();

        turnCount++;
        hasPlayedThisTurn = false;
        currentPhase = Phase.PLAYER_PLAY;
        playerChar.applyPassive();
        if (playerChar instanceof ChanCharacter) {
            List<Card> chanDraw = drawFromDeck(1);
            if (!chanDraw.isEmpty()) {
                playerHand.addAll(chanDraw);
            }
        }
        applyBurnDamage(aiChar);
        if (checkDeath()) return;
        selectedSingle = -1;
        selectedMulti.clear();
        updateDisplay();

        if (total > 0) {
            GameAnim.playDrawAnimations(this, total, false, () -> {
                playerHand.addAll(pCards);
                ai.getHand().addAll(aCards);
                updateDisplay();
            });
        }
    }

    protected void endGame(String winner) {
        currentPhase = Phase.GAME_OVER;
        updateDisplay();
        int choice = JOptionPane.showConfirmDialog(this,
            winner + "赢了！\n是否重新开始？",
            "游戏结束", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            if (aiTimer != null && aiTimer.isRunning()) aiTimer.stop();
            if (fadeTimer != null && fadeTimer.isRunning()) fadeTimer.stop();
            CharacterSelectPanel selectPanel = new CharacterSelectPanel(this);
            setContentPane(selectPanel);
            setSize(900, 600);
            revalidate();
            repaint();
            setLocationRelativeTo(null);
        }
    }

    // ── Character effect handlers ──

    /** Store attack damage as pending; applied after defense */
    protected void applyAttackEffect(Card card, GameCharacter self, GameCharacter opponent,
                                    List<Card> selfHand, Runnable onDone) {
        int handSum = 0;
        for (Card c : selfHand) {
            if (c.isNumberCard()) handSum += c.getValue();
        }
        pendingAttack = self.resolveAttack(card, deck, selfHand, handSum,
            self == playerChar ? ai.getHand() : playerHand);

        if (self instanceof SaikiCharacter && card.getValue() == 0) {
            CharacterHandler h = getHandler(self);
            if (h instanceof SaikiHandler) {
                effectEngine.applyImmediateEffects(pendingAttack, self, opponent);
                if (!self.isAlive()) {
                    String winner = self == playerChar ? "AI" : "你";
                    Timer t = new Timer(500, ev -> { ((Timer)ev.getSource()).stop(); endGame(winner); });
                    t.setRepeats(false);
                    t.start();
                    return;
                }
                ((SaikiHandler) h).handleSaikiZeroAttack(self, opponent, selfHand, onDone);
                return;
            }
        }

        if (self instanceof SaikiCharacter && card.getValue() == 7) {
            CharacterHandler h = getHandler(self);
            if (h instanceof SaikiHandler) {
                effectEngine.applyImmediateEffects(pendingAttack, self, opponent);
                if (!self.isAlive()) {
                    String winner = self == playerChar ? "AI" : "你";
                    Timer t = new Timer(500, ev -> { ((Timer)ev.getSource()).stop(); endGame(winner); });
                    t.setRepeats(false);
                    t.start();
                    return;
                }
                ((SaikiHandler) h).handleSaikiSevenAttack(self, opponent, selfHand, onDone);
                return;
            }
        }

        effectEngine.applyImmediateEffects(pendingAttack, self, opponent);

        if (!self.isAlive()) {
            String winner = self == playerChar ? "AI" : "你";
            Timer t = new Timer(500, ev -> { ((Timer)ev.getSource()).stop(); endGame(winner); });
            t.setRepeats(false);
            t.start();
            return;
        }

        if (effectEngine.hasFollowUp(pendingAttack)) {
            effectEngine.executeFollowUp(effectEngine.getFirstFollowUp(pendingAttack),
                card, self, opponent, selfHand, onDone);
            return;
        }

        if (pendingAttack.drawCount > 0) {
            List<Card> drawn = drawFromDeck(pendingAttack.drawCount);
            if (!drawn.isEmpty()) {
                selfHand.addAll(drawn);
                if (pendingAttack.recalcDamageAfterDraw) {
                    int newSum = 0;
                    for (Card c : selfHand) {
                        if (c.isNumberCard()) newSum += c.getValue();
                    }
                    pendingAttack.damage = (int) Math.ceil(newSum / 2.0);
                    showAttackDesc("⚔7️⃣ 抽牌后手牌之和" + newSum + "，造成" + pendingAttack.damage + "点伤害");
                }
                updateDisplay();
                GameAnim.playDrawAnimations(this, drawn.size(), self == playerChar, () -> {
                    updateDisplay();
                    onDone.run();
                });
                return;
            }
        }
        updateDisplay();
        onDone.run();
    }

    protected void checkHandLimit(List<Card> hand, boolean isPlayer) {
        while (hand.size() > 5) {
            if (isPlayer) {
                forcedDiscard = true;
                hasPlayedThisTurn = false;
                selectedSingle = -1;
                selectedMulti.clear();
                currentPhase = Phase.PLAYER_DISCARD;
                updateDisplay();
                showMessage("手牌超过5张，请弃牌至不超过5张！");
                return;
            } else {
                Card worst = null;
                for (Card c : hand) {
                    if (worst == null || c.getValue() < worst.getValue()) worst = c;
                }
                if (worst != null) {
                    hand.remove(worst);
                    discardPile.addLast(worst);
                }
            }
        }
    }

    void handleRevealAndJudge(GameCharacter self, GameCharacter opponent,
                                       List<Card> selfHand, Runnable onDone) {
        CharacterHandler h = getHandler(self);
        if (h != null) h.handleRevealAndJudge(self, opponent, selfHand, onDone);
    }

    void resolveAIFiveChoice(GameCharacter self, GameCharacter opponent,
                                       List<Card> selfHand, Runnable onDone) {
        CharacterHandler h = getHandler(self);
        if (h != null) h.resolveAIFiveChoice(self, opponent, selfHand, onDone);
    }

    void handleChanFourSwap(GameCharacter self, GameCharacter opponent,
                             List<Card> selfHand, List<Card> oppHand, Runnable onDone) {
        CharacterHandler h = getHandler(self);
        if (h != null) h.handleChanFourSwap(self, opponent, selfHand, oppHand, onDone);
    }

    void handleChanFiveReorder(GameCharacter self, GameCharacter opponent,
                                List<Card> selfHand, Runnable onDone) {
        CharacterHandler h = getHandler(self);
        if (h != null) h.handleChanFiveReorder(self, opponent, selfHand, onDone);
    }

    void handleChanSevenJudge(GameCharacter self, GameCharacter opponent,
                               List<Card> oppHand, Runnable onDone) {
        CharacterHandler h = getHandler(self);
        if (h != null) h.handleChanSevenJudge(self, opponent, oppHand, onDone);
    }

    void handleChanThreeDefendReveal(GameCharacter self, GameCharacter opponent,
                                      List<Card> selfHand, Runnable onDone) {
        CharacterHandler h = getHandler(self);
        if (h != null) h.handleChanThreeDefendReveal(self, opponent, selfHand, onDone);
    }

    void handleChanSixReveal(GameCharacter self, GameCharacter opponent,
                              List<Card> selfHand, Runnable onDone) {
        CharacterHandler h = getHandler(self);
        if (h != null) h.handleChanSixReveal(self, opponent, selfHand, onDone);
    }

    void handleSaikiThreeDraw(GameCharacter self, GameCharacter opponent,
                               List<Card> selfHand, List<Card> oppHand, Runnable onDone) {
        CharacterHandler h = getHandler(self);
        if (h != null) h.handleSaikiThreeDraw(self, opponent, selfHand, oppHand, onDone);
    }

    void handleSaikiSixJudge(GameCharacter self, GameCharacter opponent,
                              List<Card> selfHand, Runnable onDone) {
        CharacterHandler h = getHandler(self);
        if (h != null) h.handleSaikiSixJudge(self, opponent, selfHand, onDone);
    }

    void handleSaikiSevenAttack(GameCharacter self, GameCharacter opponent,
                                 List<Card> selfHand, Runnable onDone) {
        CharacterHandler h = getHandler(self);
        if (h != null) h.handleSaikiSevenAttack(self, opponent, selfHand, onDone);
    }

    void handleBlazeFourDraw(GameCharacter self, GameCharacter opponent,
                               List<Card> selfHand, List<Card> oppHand, Runnable onDone) {
        CharacterHandler h = getHandler(self);
        if (h instanceof BlazeHandler) ((BlazeHandler) h).handleBlazeFourDraw(self, opponent, selfHand, oppHand, onDone);
    }



    void doBlazeFourOpponentSelected(int aiCardIndex) {
        CharacterHandler h = getHandler(playerChar);
        if (h instanceof BlazeHandler) ((BlazeHandler) h).doBlazeFourOpponentSelected(aiCardIndex);
    }

    void handleBlazeDefendTwoDraw(GameCharacter self, GameCharacter opponent,
                                    List<Card> selfHand, Runnable onDone) {
        CharacterHandler h = getHandler(self);
        if (h instanceof BlazeHandler) ((BlazeHandler) h).handleBlazeDefendTwoDraw(self, opponent, selfHand, onDone);
    }

    void handleSaikiZeroAttack(GameCharacter self, GameCharacter opponent,
                                 List<Card> selfHand, Runnable onDone) {
        CharacterHandler h = getHandler(self);
        if (h != null) h.handleSaikiZeroAttack(self, opponent, selfHand, onDone);
    }

    void handleSaikiThreeDefendReveal(GameCharacter self, GameCharacter opponent,
                                        List<Card> selfHand, Runnable onDone) {
        CharacterHandler h = getHandler(self);
        if (h != null) h.handleSaikiThreeDefendReveal(self, opponent, selfHand, onDone);
    }

    /** Apply pending attack damage after defense */
    protected void resolvePostDefense(GameCharacter attacker, GameCharacter defender) {
        if (pendingAttack == null) return;
        int dmg = pendingAttack.damage;
        boolean isRed = false;
        if (!discardPile.isEmpty()) {
            Card top = discardPile.getFirst();
            isRed = top.getColor() == Card.CardColor.RED || top.getEffectiveColor() == Card.CardColor.RED;
        }

        if (dmg > 0 && pendingDefendCard != null) {
            GameCharacter.DefenseResult def = defender.resolveDefense(pendingDefendCard, dmg, isRed);
            pendingDefendResult = def;

            if (def.immuneAll) {
                dmg = 0;
                if (def.healFromDamage && pendingAttack.damage > 0) {
                    defender.heal(pendingAttack.damage);
                    Point loc = defender == playerChar
                        ? new Point(getWidth() / 2 - 60, getHeight() * 3 / 4 - 30)
                        : new Point(getWidth() / 2 - 60, getHeight() / 3 - 30);
                    GameAnim.playFloatingText(this, "+" + pendingAttack.damage, new Color(60, 220, 60), loc);
                }
            } else {
                dmg -= def.blocked;
                if (dmg < 0) dmg = 0;
            }

            if (def.selfHeal > 0) {
                defender.heal(def.selfHeal);
                Point loc = defender == playerChar
                    ? new Point(getWidth() / 2 - 60, getHeight() * 3 / 4 - 30)
                    : new Point(getWidth() / 2 - 60, getHeight() / 3 - 30);
                GameAnim.playFloatingText(this, "+" + def.selfHeal, new Color(60, 220, 60), loc);
            }

            if (def.addBurn > 0) {
                attacker.addBurn(def.addBurn);
                GameAnim.playFloatingText(this, "🔥+" + def.addBurn, new Color(255, 140, 0),
                    attacker == playerChar
                        ? new Point(getWidth() / 2, getHeight() * 3 / 4 - 60)
                        : new Point(getWidth() / 2, getHeight() / 3 - 60));
            }

            if (def.addBurnSelf > 0) {
                defender.addBurn(def.addBurnSelf);
                GameAnim.playFloatingText(this, "🔥+" + def.addBurnSelf, new Color(255, 140, 0),
                    defender == playerChar
                        ? new Point(getWidth() / 2, getHeight() * 3 / 4 - 60)
                        : new Point(getWidth() / 2, getHeight() / 3 - 60));
            }

            if (def.healAllBurnPlus > 0) {
                int totalBurn = playerChar.getBurnStacks() + aiChar.getBurnStacks();
                int healAmt = totalBurn + def.healAllBurnPlus;
                defender.heal(healAmt);
                Point loc = defender == playerChar
                    ? new Point(getWidth() / 2 - 60, getHeight() * 3 / 4 - 30)
                    : new Point(getWidth() / 2 - 60, getHeight() / 3 - 30);
                GameAnim.playFloatingText(this, "+" + healAmt, new Color(60, 220, 60), loc);
            }

            if (def.counterDmg > 0) {
                attacker.takeDamage(def.counterDmg);
                Point loc = attacker == playerChar
                    ? new Point(getWidth() / 2 + 60, getHeight() * 3 / 4)
                    : new Point(getWidth() / 2 + 60, getHeight() / 3);
                GameAnim.playFloatingText(this, "-" + def.counterDmg, new Color(255, 60, 60), loc);
            }

            if (def.counterDmgFromAttackerBurn) {
                int burnCounter = attacker.getBurnStacks();
                if (burnCounter > 0) {
                    attacker.takeDamage(burnCounter);
                    Point loc = attacker == playerChar
                        ? new Point(getWidth() / 2 + 60, getHeight() * 3 / 4)
                        : new Point(getWidth() / 2 + 60, getHeight() / 3);
                    GameAnim.playFloatingText(this, "-" + burnCounter, new Color(255, 60, 60), loc);
                }
            }

            if (def.counterDmgFromFieldBurn) {
                int fieldBurn = playerChar.getBurnStacks() + aiChar.getBurnStacks();
                if (fieldBurn > 0) {
                    attacker.takeDamage(fieldBurn);
                    Point loc = attacker == playerChar
                        ? new Point(getWidth() / 2 + 60, getHeight() * 3 / 4)
                        : new Point(getWidth() / 2 + 60, getHeight() / 3);
                    GameAnim.playFloatingText(this, "-" + fieldBurn, new Color(255, 60, 60), loc);
                }
            }

            if (def.counterFromDamage > 0) {
                attacker.takeDamage(def.counterFromDamage);
                Point loc = attacker == playerChar
                    ? new Point(getWidth() / 2 + 60, getHeight() * 3 / 4)
                    : new Point(getWidth() / 2 + 60, getHeight() / 3);
                GameAnim.playFloatingText(this, "-" + def.counterFromDamage, new Color(255, 60, 60), loc);
            }

            if (def.addFreeze) {
                attacker.setFrozen(true);
                GameAnim.playFloatingText(this, "❄️冷冻", new Color(100, 180, 255),
                    attacker == playerChar
                        ? new Point(getWidth() / 2, getHeight() * 3 / 4 - 60)
                        : new Point(getWidth() / 2, getHeight() / 3 - 60));
            }

            if (def.addBleed > 0) {
                attacker.addBleed(def.addBleed);
                GameAnim.playFloatingText(this, "🩸+" + def.addBleed, new Color(180, 0, 0),
                    attacker == playerChar
                        ? new Point(getWidth() / 2, getHeight() * 3 / 4 - 90)
                        : new Point(getWidth() / 2, getHeight() / 3 - 90));
            }

            if (def.immuneDebuff && pendingAttack != null) {
                if (pendingAttack.addBurn > 0) pendingAttack.addBurn = 0;
                if (pendingAttack.addFreeze) pendingAttack.addFreeze = false;
                if (pendingAttack.addBleed > 0) pendingAttack.addBleed = 0;
            }

            if (def.reflectDebuff && pendingAttack != null) {
                if (pendingAttack.addBleed > 0) {
                    defender.addBleed(pendingAttack.addBleed);
                    attacker.addBleed(pendingAttack.addBleed);
                    pendingAttack.addBleed = 0;
                    GameAnim.playFloatingText(this, "🩸反弹", new Color(180, 0, 0),
                        attacker == playerChar
                            ? new Point(getWidth() / 2, getHeight() * 3 / 4 - 90)
                            : new Point(getWidth() / 2, getHeight() / 3 - 90));
                }
                if (pendingAttack.addBurn > 0) {
                    attacker.addBurn(pendingAttack.addBurn);
                    pendingAttack.addBurn = 0;
                }
                if (pendingAttack.addFreeze) {
                    attacker.setFrozen(true);
                    pendingAttack.addFreeze = false;
                }
            }

            if (def.sharedDamage > 0) {
                int shared = def.sharedDamage;
                attacker.takeDamage(shared);
                defender.takeDamage(shared);
                GameAnim.playFloatingText(this, "-" + shared, new Color(255, 60, 60),
                    attacker == playerChar
                        ? new Point(getWidth() / 2 + 60, getHeight() * 3 / 4)
                        : new Point(getWidth() / 2 + 60, getHeight() / 3));
                GameAnim.playFloatingText(this, "-" + shared, new Color(255, 60, 60),
                    defender == playerChar
                        ? new Point(getWidth() / 2 - 60, getHeight() * 3 / 4)
                        : new Point(getWidth() / 2 - 60, getHeight() / 3));
            }

            if (def.forceDiscardAll) {
                List<Card> oppHand = attacker == playerChar ? playerHand : ai.getHand();
                for (Card c : new ArrayList<>(oppHand)) {
                    discardPile.addLast(c);
                }
                oppHand.clear();
            }

            if (def.clearSelfBuffs) {
                defender.clearAllDebuffs();
            }

            if (def.drawCount > 0) {
                List<Card> defHand = defender == playerChar ? playerHand : ai.getHand();
                List<Card> drawn = drawFromDeck(def.drawCount);
                if (!drawn.isEmpty()) {
                    defHand.addAll(drawn);
                    GameAnim.playDrawAnimations(this, drawn.size(), defender == playerChar, () -> updateDisplay());
                }
            }

            showDefendDesc(def.desc);
        }

        // 流血触发：防御者有流血且打出了防御牌(0/1/2/3)，受1点额外独立伤害
        if (pendingDefendCard != null && defender.getBleedStacks() > 0
                && pendingDefendCard.getValue() <= 3 && !pendingDefendCard.isItemCard()) {
            int bleedDmg = defender.getBleedStacks();
            for (int i = 0; i < bleedDmg; i++) {
                defender.takeDamage(1);
                GameAnim.playFloatingText(this, "🩸-1", new Color(180, 0, 0),
                    defender == playerChar
                        ? new Point(getWidth() / 2 - 30 + i * 30, getHeight() * 3 / 4 - 30)
                        : new Point(getWidth() / 2 - 30 + i * 30, getHeight() / 3 - 30));
            }
        }

        if (dmg > 0) {
            defender.takeDamage(dmg);
            Point loc = defender == playerChar
                ? new Point(getWidth() / 2, getHeight() * 3 / 4)
                : new Point(getWidth() / 2, getHeight() / 3);
            GameAnim.playFloatingText(this, "-" + dmg, new Color(255, 60, 60), loc);
        }

        if (pendingAttack != null && pendingAttack.forceOpponentDiscard > 0) {
            List<Card> oppHand = attacker == playerChar ? ai.getHand() : playerHand;
            int toDiscard = Math.min(pendingAttack.forceOpponentDiscard, oppHand.size());
            for (int i = 0; i < toDiscard; i++) {
                int idx = (int)(Math.random() * oppHand.size());
                Card discarded = oppHand.remove(idx);
                discardPile.addLast(discarded);
                GameAnim.playFloatingText(this, "弃" + discarded, new Color(255, 60, 60),
                    attacker == playerChar
                        ? new Point(getWidth() / 2, getHeight() / 3 - 30)
                        : new Point(getWidth() / 2, getHeight() * 3 / 4 - 30));
            }
        }

        if (pendingAttack != null && pendingAttack.drawOpponentCard) {
            List<Card> oppHand = attacker == playerChar ? ai.getHand() : playerHand;
            List<Card> selfHand = attacker == playerChar ? playerHand : ai.getHand();
            if (!oppHand.isEmpty()) {
                int idx = (int)(Math.random() * oppHand.size());
                Card drawn = oppHand.remove(idx);
                selfHand.add(drawn);
                GameAnim.playFloatingText(this, "抽" + drawn, new Color(100, 180, 255),
                    attacker == playerChar
                        ? new Point(getWidth() / 2, getHeight() / 3 - 30)
                        : new Point(getWidth() / 2, getHeight() * 3 / 4 - 30));
            }
        }

        pendingAttack = null;
        pendingDefendCard = null;
        updateDisplay();

        checkDeath();
    }

    /** End AI defense → resolve damage → back to player turn */
    protected void finishAIDefend() {
        resolvePostDefense(playerChar, aiChar);
        if (!playerChar.isAlive() || !aiChar.isAlive()) return;
        if (pendingDefendResult != null && pendingDefendResult.endAttackerTurn) {
            pendingDefendResult = null;
            clearAIZones();
            finishPlayerTurn();
            return;
        }
        if (pendingDefendResult != null && pendingDefendResult.revealTopDeck) {
            GameCharacter.DefenseResult def = pendingDefendResult;
            pendingDefendResult = null;
            List<Card> defHand = aiChar == playerChar ? playerHand : ai.getHand();
            CharacterHandler h = getHandler(aiChar);
            if (h instanceof SaikiHandler) {
                handleSaikiThreeDefendReveal(aiChar, playerChar, defHand, () -> {
                    currentPhase = Phase.PLAYER_PLAY;
                    clearAIZones();
                    updateDisplay();
                });
            } else {
                handleChanThreeDefendReveal(aiChar, playerChar, defHand, () -> {
                    currentPhase = Phase.PLAYER_PLAY;
                    clearAIZones();
                    updateDisplay();
                });
            }
            return;
        }
        if (pendingDefendResult != null && !pendingDefendResult.followUps.isEmpty()) {
            GameCharacter.DefenseResult def = pendingDefendResult;
            pendingDefendResult = null;
            List<Card> defHand = aiChar == playerChar ? playerHand : ai.getHand();
            GameCharacter.FollowUp fu = def.followUps.get(0);
            effectEngine.executeFollowUp(fu, null, aiChar, playerChar, defHand, () -> {
                currentPhase = Phase.PLAYER_PLAY;
                clearAIZones();
                updateDisplay();
            });
            return;
        }
        pendingDefendResult = null;
        Timer pause = new Timer(DELAY_EFFECT, ev -> {
            ((Timer)ev.getSource()).stop();
            if (!playerChar.isAlive() || !aiChar.isAlive()) return;
            clearAIZones();
            currentPhase = Phase.PLAYER_PLAY;
            updateDisplay();
        });
        pause.start();
    }

    /** End player defense → resolve damage → back to AI turn */
    protected void finishPlayerDefend() {
        resolvePostDefense(aiChar, playerChar);
        if (!playerChar.isAlive() || !aiChar.isAlive()) return;
        if (pendingDefendResult != null && pendingDefendResult.endAttackerTurn) {
            pendingDefendResult = null;
            clearAIZones();
            finishAITurn();
            return;
        }
        if (pendingDefendResult != null && pendingDefendResult.revealTopDeck) {
            GameCharacter.DefenseResult def = pendingDefendResult;
            pendingDefendResult = null;
            List<Card> defHand = playerChar == playerChar ? playerHand : ai.getHand();
            CharacterHandler h = getHandler(playerChar);
            if (h instanceof SaikiHandler) {
                handleSaikiThreeDefendReveal(playerChar, aiChar, defHand, () -> {
                    currentPhase = Phase.AI_TURN;
                    clearAIZones();
                    updateDisplay();
                    resumeAITurn();
                });
            } else {
                handleChanThreeDefendReveal(playerChar, aiChar, defHand, () -> {
                    currentPhase = Phase.AI_TURN;
                    clearAIZones();
                    updateDisplay();
                    resumeAITurn();
                });
            }
            return;
        }
        if (pendingDefendResult != null && !pendingDefendResult.followUps.isEmpty()) {
            GameCharacter.DefenseResult def = pendingDefendResult;
            pendingDefendResult = null;
            List<Card> defHand = playerChar == playerChar ? playerHand : ai.getHand();
            GameCharacter.FollowUp fu = def.followUps.get(0);
            effectEngine.executeFollowUp(fu, null, playerChar, aiChar, defHand, () -> {
                currentPhase = Phase.AI_TURN;
                clearAIZones();
                updateDisplay();
                resumeAITurn();
            });
            return;
        }
        pendingDefendResult = null;

        currentPhase = Phase.AI_TURN;
        updateDisplay();
        Timer pause = new Timer(DELAY_EFFECT, ev -> {
            ((Timer)ev.getSource()).stop();
            if (!playerChar.isAlive() || !aiChar.isAlive()) return;
            clearAIZones();
            resumeAITurn();
        });
        pause.start();
    }

    protected void showMessage(String msg) {
        showTransientHint(msg);
    }

    void showTransientHint(String msg) {
        if (fadeTimer != null && fadeTimer.isRunning()) {
            fadeTimer.stop();
        }
        ui.errorHintLabel.setText(msg);
        ui.errorHintLabel.setForeground(new Color(255, 80, 80));
        ui.errorHintLabel.setVisible(true);

        fadeTimer = new Timer(2000, e -> {
            ((Timer)e.getSource()).stop();
            Timer fade = new Timer(40, null);
            int[] alpha = {255};
            fade.addActionListener(e2 -> {
                alpha[0] -= 18;
                if (alpha[0] <= 0) {
                    fade.stop();
                    ui.errorHintLabel.setVisible(false);
                    ui.errorHintLabel.setText("");
                } else {
                    ui.errorHintLabel.setForeground(new Color(255, 80, 80, alpha[0]));
                }
            });
            fade.start();
        });
        fadeTimer.setRepeats(false);
        fadeTimer.start();
    }

    // ===== Display =====

    private boolean isPlayerAttacker() {
        return currentPhase == Phase.PLAYER_PLAY || currentPhase == Phase.PLAYER_DISCARD || currentPhase == Phase.AI_DEFEND || currentPhase == Phase.PLAYER_FIVE_CHOICE || currentPhase == Phase.PLAYER_SEVEN_CHOICE || currentPhase == Phase.SAIKI_THREE_CHOICE || currentPhase == Phase.SAIKI_SIX_JUDGE;
    }

    private boolean isAIAttacker() {
        return currentPhase == Phase.AI_TURN || currentPhase == Phase.PLAYER_DEFEND;
    }

    void updateDisplay() {
        switch (currentPhase) {
            case PLAYER_PLAY: case PLAYER_DISCARD:
            case PLAYER_FIVE_CHOICE: case PLAYER_SEVEN_CHOICE:
            case SAIKI_THREE_CHOICE: case SAIKI_SIX_JUDGE:
                busy = false; break;
            default: break;
        }
        ui.deckLabel.setText("牌堆: " + deck.remaining() + " 张 | 弃牌库: " + discardPile.size() + " 张 | 回合: " + turnCount);
        ui.playerHpBar.update(playerChar.getName(), playerChar.getCurrentHp(), playerChar.getMaxHp());
        ui.aiHpBar.update(aiChar.getName(), aiChar.getCurrentHp(), aiChar.getMaxHp());



        if (isPlayerAttacker()) {
            ui.attackerLabel.setText(">> 进攻方: 你 <<");
        } else if (isAIAttacker()) {
            ui.attackerLabel.setText(">> 进攻方: AI <<");
        } else {
            ui.attackerLabel.setText("");
        }

        switch (currentPhase) {
            case PLAYER_PLAY:
                if (hasPlayedThisTurn) {
                    ui.phaseLabel.setText("【你的回合 - 出牌中】可继续出牌，或点击「结束回合」");
                } else {
                    ui.phaseLabel.setText("【你的回合】选一张牌出牌，或点击「弃牌」");
                }
                break;
            case PLAYER_DISCARD:
                if (forcedDiscard) {
                    ui.phaseLabel.setText("【手牌超限 - 强制弃牌】手牌超过5张，请弃牌至≤5张");
                } else {
                    ui.phaseLabel.setText("【你的回合 - 选弃牌】选择要弃掉的牌，点击「确认弃牌」或「取消」");
                }
                break;
            case PLAYER_DEFEND:
                if (hasPlayedBlackDefend) {
                    Card ct = discardPile.getFirst();
                    String cn = colorName(ct.getEffectiveColor());
                    ui.phaseLabel.setText("【搭桥中】请再出一张" + cn + "色≤3的牌完成防御，或出黑牌/🧪/+3继续搭桥，或点击「跳过」");
                } else if (playerCanDefend()) {
                    ui.phaseLabel.setText("【防御机会】AI出牌了！选一张数字≤3的匹配牌防御，或出黑牌/🧪/+3搭桥，或点击「跳过」");
                } else {
                    ui.phaseLabel.setText("【防御机会】AI出牌了！你无合法防御牌，请点击「跳过」");
                }
                break;
            case PLAYER_FIVE_CHOICE:
                ui.phaseLabel.setText("【5️⃣效果】选一张数字牌，选择「1½倍回血」或「1½倍伤害」");
                break;
            case PLAYER_SEVEN_CHOICE:
                ui.phaseLabel.setText("【7️⃣效果】点击AI的一张手牌将其弃掉");
                break;
            case SAIKI_THREE_CHOICE:
                if (chanFourSelectOpponent) {
                    ui.phaseLabel.setText("【3️⃣效果】选择对手的一张手牌");
                } else {
                    ui.phaseLabel.setText("【3️⃣效果】选择「加入手牌」或「弃掉」");
                }
                break;
            case SAIKI_SIX_JUDGE:
                ui.phaseLabel.setText("【6️⃣判定】选一张数字牌入判定区，然后点击「确认判定」");
                break;
            case AI_TURN:
                ui.phaseLabel.setText("【AI 回合】AI 出牌中...");
                break;
            case AI_DEFEND:
                ui.phaseLabel.setText("【AI 防御中】AI 考虑是否防御...");
                break;
            case GAME_OVER:
                ui.phaseLabel.setText("【游戏结束】");
                break;
        }

        ui.discardCardPanel.removeAll();
        if (!discardPile.isEmpty()) {
            ui.discardCardPanel.add(GameUI.createCardView(discardPile.getFirst(), false, -1, false, currentPhase, this));
        } else {
            JLabel emptyLabel = new JLabel("空");
            emptyLabel.setForeground(new Color(120, 140, 120));
            ui.discardCardPanel.add(emptyLabel);
        }
        ui.discardCardPanel.revalidate();
        ui.discardCardPanel.repaint();

        ui.aiHandPanel.setBorder(isAIAttacker() ? GameUI.makeAttackerBorder("AI 手牌") : GameUI.makeGlowBorder("AI 手牌", new Color(80, 120, 180)));

        ui.aiHandPanel.removeAll();
        if (ai.handSize() == 0) {
            JLabel emptyLabel = new JLabel("AI 无手牌");
            emptyLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
            emptyLabel.setForeground(new Color(110, 150, 120));
            ui.aiHandPanel.add(emptyLabel);
        } else if (currentPhase == Phase.PLAYER_SEVEN_CHOICE || currentPhase == Phase.SAIKI_THREE_CHOICE || chanFourSelectOpponent) {
            for (int i = 0; i < ai.handSize(); i++) {
                int idx = i;
                boolean aiSelected = (selectedAICard == i);
                JPanel cardBack = GameUI.createCardBackView();
                if (aiSelected) {
                    cardBack.setBorder(BorderFactory.createLineBorder(new Color(255, 220, 60), 3));
                }
                cardBack.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                cardBack.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent e) {
                        selectedAICard = (selectedAICard == idx) ? -1 : idx;
                        updateDisplay();
                    }
                });
                ui.aiHandPanel.add(cardBack);
            }
        } else {
            for (int i = 0; i < ai.handSize(); i++) {
                ui.aiHandPanel.add(GameUI.createCardBackView());
            }
        }
        if (currentPhase == Phase.AI_DEFEND) {
            if (aiDefendSuccess) {
                JLabel hintLabel = new JLabel("AI 防御成功！");
                hintLabel.setFont(new Font("微软雅黑", Font.BOLD, 13));
                hintLabel.setForeground(new Color(100, 220, 100));
                ui.aiHandPanel.add(hintLabel);
            } else if (ai.canDefend(discardPile.getFirst())) {
                JLabel hintLabel = new JLabel("AI 正在考虑防御...");
                hintLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
                hintLabel.setForeground(new Color(255, 200, 100));
                ui.aiHandPanel.add(hintLabel);
            } else {
                JLabel hintLabel = new JLabel("AI 无法防御");
                hintLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
                hintLabel.setForeground(new Color(200, 120, 120));
                ui.aiHandPanel.add(hintLabel);
            }
        }
        if (aiChar.getBurnStacks() > 0) {
            JPanel burnPnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
            burnPnl.setOpaque(false);
            for (int i = 0; i < aiChar.getBurnStacks(); i++) {
                burnPnl.add(GameIcons.makeIconLabel(GameIcons.buffBurn()));
            }
            ui.aiHandPanel.add(burnPnl);
        }
        if (aiChar.isFrozen()) {
            ui.aiHandPanel.add(GameIcons.makeIconLabel(GameIcons.buffFreeze()));
        }
        if (aiChar.getBleedStacks() > 0) {
            JPanel bleedPnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
            bleedPnl.setOpaque(false);
            bleedPnl.add(GameIcons.makeIconLabel(GameIcons.buffBleed()));
            JLabel bleedNum = new JLabel("x" + aiChar.getBleedStacks());
            bleedNum.setFont(new Font("Arial", Font.BOLD, 12));
            bleedNum.setForeground(new Color(180, 0, 0));
            bleedPnl.add(bleedNum);
            ui.aiHandPanel.add(bleedPnl);
        }
        ui.aiHandPanel.revalidate();
        ui.aiHandPanel.repaint();

        ui.playerHandPanel.setBorder(isPlayerAttacker() ? GameUI.makeAttackerBorder("你的手牌") : GameUI.makeGlowBorder("你的手牌", new Color(120, 180, 120)));

        boolean playerActive = currentPhase == Phase.PLAYER_PLAY || currentPhase == Phase.PLAYER_DISCARD || currentPhase == Phase.PLAYER_DEFEND || currentPhase == Phase.PLAYER_FIVE_CHOICE || currentPhase == Phase.PLAYER_SEVEN_CHOICE || currentPhase == Phase.SAIKI_THREE_CHOICE || currentPhase == Phase.SAIKI_SIX_JUDGE;

        ui.playerHandPanel.removeAll();
        if (playerHand.isEmpty()) {
            JLabel emptyLabel = new JLabel("无手牌");
            emptyLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
            emptyLabel.setForeground(new Color(110, 150, 120));
            ui.playerHandPanel.add(emptyLabel);
        } else {
            for (int i = 0; i < playerHand.size(); i++) {
                boolean selected = false;
                if (playerActive && currentPhase == Phase.PLAYER_PLAY && i == selectedSingle) selected = true;
                if (playerActive && currentPhase == Phase.PLAYER_DISCARD && selectedMulti.contains(i)) selected = true;
                if (playerActive && currentPhase == Phase.PLAYER_DEFEND && i == selectedSingle) selected = true;
                if (playerActive && currentPhase == Phase.PLAYER_FIVE_CHOICE && i == selectedSingle) selected = true;
                if (playerActive && currentPhase == Phase.PLAYER_SEVEN_CHOICE && i == selectedSingle) selected = true;
                if (playerActive && currentPhase == Phase.SAIKI_THREE_CHOICE && i == selectedSingle) selected = true;
                if (playerActive && currentPhase == Phase.SAIKI_SIX_JUDGE && i == selectedSingle) selected = true;
                ui.playerHandPanel.add(GameUI.createCardView(playerHand.get(i), playerActive, i, selected, currentPhase, this));
            }
        }
        if (playerChar.getBurnStacks() > 0) {
            JPanel burnPnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
            burnPnl.setOpaque(false);
            for (int i = 0; i < playerChar.getBurnStacks(); i++) {
                burnPnl.add(GameIcons.makeIconLabel(GameIcons.buffBurn()));
            }
            ui.playerHandPanel.add(burnPnl);
        }
        if (playerChar.isFrozen()) {
            ui.playerHandPanel.add(GameIcons.makeIconLabel(GameIcons.buffFreeze()));
        }
        if (playerChar.getBleedStacks() > 0) {
            JPanel bleedPnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
            bleedPnl.setOpaque(false);
            bleedPnl.add(GameIcons.makeIconLabel(GameIcons.buffBleed()));
            JLabel bleedNum = new JLabel("x" + playerChar.getBleedStacks());
            bleedNum.setFont(new Font("Arial", Font.BOLD, 12));
            bleedNum.setForeground(new Color(180, 0, 0));
            bleedPnl.add(bleedNum);
            ui.playerHandPanel.add(bleedPnl);
        }
        ui.playerHandPanel.revalidate();
        ui.playerHandPanel.repaint();

        boolean isPlayerPlay = currentPhase == Phase.PLAYER_PLAY;
        boolean isPlayerDiscard = currentPhase == Phase.PLAYER_DISCARD;
        boolean isPlayerDefend = currentPhase == Phase.PLAYER_DEFEND;
        boolean isFiveChoice = currentPhase == Phase.PLAYER_FIVE_CHOICE;
        boolean isSevenChoice = currentPhase == Phase.PLAYER_SEVEN_CHOICE;

        ui.playBtn.setVisible(isPlayerPlay);
        ui.enterDiscardBtn.setVisible(isPlayerPlay && !hasPlayedThisTurn);
        ui.confirmDiscardBtn.setVisible(isPlayerDiscard);
        ui.cancelDiscardBtn.setVisible(isPlayerDiscard && !forcedDiscard);
        ui.endTurnBtn.setVisible(isPlayerPlay);
        ui.defendBtn.setVisible(isPlayerDefend);
        boolean isSaikiThree = currentPhase == Phase.SAIKI_THREE_CHOICE;
        boolean isSaikiThreeDone = isSaikiThree && !chanFourSelectOpponent;
        boolean isSaikiSix = currentPhase == Phase.SAIKI_SIX_JUDGE;
        ui.skipDefendBtn.setVisible(isPlayerDefend || (chanFourSwapMode && !chanFourSelectOpponent) || chanSevenKeepMode);
        if (chanFourSwapMode) {
            ui.skipDefendBtn.setText(" 弃掉(2伤害)");
        } else if (chanSevenKeepMode) {
            ui.skipDefendBtn.setText(" 弃掉");
        } else {
            ui.skipDefendBtn.setText(" 跳过");
        }

        ui.fiveHealBtn.setVisible(isFiveChoice || isSaikiThreeDone);
        ui.fiveDamageBtn.setVisible(isFiveChoice || isSaikiThreeDone);
        if (isSaikiThreeDone) {
            ui.fiveHealBtn.setText("📥 加入手牌");
            ui.fiveDamageBtn.setText("🗑 弃掉");
        } else {
            ui.fiveHealBtn.setText("❤️ 回血");
            ui.fiveDamageBtn.setText("🗡️ 伤害");
        }
        ui.sevenChoiceBtn.setVisible(chanSevenKeepMode || chanFourSwapMode || (isSaikiThree && chanFourSelectOpponent) || currentPhase == Phase.PLAYER_SEVEN_CHOICE || isSaikiSix);
        if (chanSevenKeepMode) {
            ui.sevenChoiceBtn.setText(" 加入手牌");
            ui.sevenChoiceBtn.setIcon(GameIcons.uiTick());
        } else if (chanFourSwapMode) {
            ui.sevenChoiceBtn.setText(" 确认交换");
            ui.sevenChoiceBtn.setIcon(GameIcons.uiTick());
        } else if (isSaikiThree && chanFourSelectOpponent) {
            ui.sevenChoiceBtn.setText(" 确认选择");
            ui.sevenChoiceBtn.setIcon(GameIcons.uiHand());
        } else if (isSaikiSix) {
            ui.sevenChoiceBtn.setText(" 确认判定");
            ui.sevenChoiceBtn.setIcon(GameIcons.uiJudge());
        } else if (currentPhase == Phase.PLAYER_SEVEN_CHOICE) {
            ui.sevenChoiceBtn.setText(" 确认选择");
            ui.sevenChoiceBtn.setIcon(GameIcons.uiHand());
        }
        ui.playBtn.setEnabled(isPlayerPlay && selectedSingle >= 0 && !busy);
        ui.enterDiscardBtn.setEnabled(isPlayerPlay && !hasPlayedThisTurn && !busy);
        ui.confirmDiscardBtn.setEnabled(isPlayerDiscard && !selectedMulti.isEmpty() && !busy);
        ui.defendBtn.setEnabled(isPlayerDefend && selectedSingle >= 0 && playerCanDefend() && !busy);
        ui.endTurnBtn.setEnabled(isPlayerPlay && !busy);
        ui.skipDefendBtn.setEnabled(!busy);
        ui.fiveHealBtn.setEnabled((isFiveChoice ? selectedSingle >= 0 : isSaikiThreeDone) && !busy);
        ui.fiveDamageBtn.setEnabled((isFiveChoice ? selectedSingle >= 0 : isSaikiThreeDone) && !busy);
        ui.sevenChoiceBtn.setEnabled(((chanSevenKeepMode) || (chanFourSwapMode && selectedSingle >= 0) || (chanFourSelectOpponent && selectedAICard >= 0) || (isSaikiSix && selectedSingle >= 0) || (currentPhase == Phase.PLAYER_SEVEN_CHOICE && !chanFourSwapMode && !chanSevenKeepMode && !chanFourSelectOpponent && selectedAICard >= 0)) && !busy);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Game game = new Game();
            CharacterSelectPanel selectPanel = new CharacterSelectPanel(game);
            game.setContentPane(selectPanel);
            game.setSize(900, 600);
            game.setLocationRelativeTo(null);
            game.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            game.setVisible(true);
        });
    }
}
