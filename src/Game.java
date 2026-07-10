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

    enum Phase { PLAYER_PLAY, PLAYER_DISCARD, PLAYER_DEFEND, PLAYER_FIVE_CHOICE, PLAYER_SEVEN_CHOICE, SAIKI_THREE_CHOICE, SAIKI_SIX_JUDGE, AI_TURN, AI_DEFEND, TARGET_CHOICE, GAME_OVER }

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
    protected List<Participant> participants;
    protected TurnManager turnManager;
    protected GameMode gameMode;
    protected int selectedSingle;
    protected List<Integer> selectedMulti;
    protected Participant currentAttackTarget;

    // Cached refs from participants (set in onCharacterSelected)
    protected List<Card> playerHand;
    protected AIPlayer ai;
    protected AIPlayer ai2;
    protected GameCharacter playerChar;
    protected GameCharacter aiChar;
    protected GameCharacter aiChar2;
    protected boolean is1v2;
    protected int maxPlayerHand = 5;
    protected int currentTurnTarget;
    protected int attackTarget;
    protected Runnable pendingTargetAction;

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
    protected AttackEngine attackEngine;
    protected DefenseEngine defenseEngine;
    protected TurnEngine turnEngine;

    protected boolean busy = false;
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
        participants = new ArrayList<>();
        selectedMulti = new ArrayList<>();
        selectedSingle = -1;
    }

    protected void initUI() {
        ui = gameMode.createUI();
        ui.buildUI(this);
        effectEngine = new EffectEngine(this);
        attackEngine = new AttackEngine(this);
        defenseEngine = new DefenseEngine(this);
        turnEngine = new TurnEngine(this);
        setContentPane(ui.getRootPanel());
        setSize(is1v2 ? 1300 : 1100, is1v2 ? 920 : 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    void onModeSelected(boolean mode1v2) {
        gameMode = mode1v2 ? new Mode1v2() : new Mode1v1();
        is1v2 = mode1v2;
        maxPlayerHand = gameMode.playerHandLimit();
        setContentPane(new CharacterSelectPanel(this, is1v2));
        revalidate();
        repaint();
    }

    void onCharacterSelected(int playerChoice, int aiChoice, int ai2Choice) {
        participants = gameMode.createParticipants(this, playerChoice, aiChoice, ai2Choice);
        turnManager = new TurnManager(participants);

        // Set cached refs from participants
        Participant human = getHuman();
        playerChar = human.character;
        playerHand = human.hand;

        Participant ai1p = getAI1();
        aiChar = ai1p.character;
        ai = ai1p.ai;

        Participant ai2p = getAI2();
        if (ai2p != null) {
            aiChar2 = ai2p.character;
            ai2 = ai2p.ai;
        }

        handlers.put("Ryan", new RyanHandler(this));
        handlers.put("Leon", new LeonHandler(this));
        handlers.put("Chan", new ChanHandler(this));
        handlers.put("Saiki", new SaikiHandler(this));
        handlers.put("Blaze", new BlazeHandler(this));
        handlers.put("Serenity", new SerenityHandler(this));
        handlers.put("Moze", new MozeHandler(this));

        initUI();
        startGame();
    }

    protected GameCharacter createCharacter(String name, boolean isAI) {
        switch (name) {
            case "Leon": return new LeonCharacter(isAI);
            case "Chan": return new ChanCharacter(isAI);
            case "Saiki": return new SaikiCharacter();
            case "Blaze": return new BlazeCharacter(isAI);
            case "Serenity": return new SerenityCharacter(isAI);
            case "Moze": return new MozeCharacter(isAI);
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
            case "Serenity": return new SerenityAI(character);
            case "Moze": return new MozeAI(character);
            default: return new RyanAI(character);
        }
    }

    void setupAIOpponent(AIPlayer aiPlayer, GameCharacter opponent) {
        if (aiPlayer instanceof LeonAI) ((LeonAI) aiPlayer).setOpponent(opponent);
        if (aiPlayer instanceof SaikiAI) ((SaikiAI) aiPlayer).setCharacters(null, opponent);
        if (aiPlayer instanceof BlazeAI) ((BlazeAI) aiPlayer).setOpponent(opponent);
        if (aiPlayer instanceof SerenityAI) ((SerenityAI) aiPlayer).setOpponent(opponent);
    }

    void backToSelect() {
        if (aiTimer != null && aiTimer.isRunning()) aiTimer.stop();
        if (fadeTimer != null && fadeTimer.isRunning()) fadeTimer.stop();
        busy = false;
        CharacterSelectPanel selectPanel = new CharacterSelectPanel(this, is1v2);
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
        attackTarget = 0;
        currentTurnTarget = 0;
        playerChar.reset();
        aiChar.reset();
        if (is1v2 && aiChar2 != null) aiChar2.reset();
        turnCount = 1;

        Card first = deck.draw();
        while (first.isBlack() || first.isWhite()) {
            discardPile.addLast(first);
            first = deck.draw();
        }
        discardPile.addFirst(first);
        playerHand.addAll(deck.draw(maxPlayerHand));
        ai.addCards(deck.draw(5));
        if (is1v2 && ai2 != null) ai2.addCards(deck.draw(5));

        if (playerChar instanceof ChanCharacter) {
            List<Card> chanDraw = deck.draw(1);
            playerHand.addAll(chanDraw);
        }

        currentPhase = Phase.PLAYER_PLAY;
        updateDisplay();
    }

    List<Card> getPlayerHand() { return getHuman().hand; }
    List<Card> getAIHand() {
        if (is1v2 && currentAttackTarget != null && currentAttackTarget.isAI()) return currentAttackTarget.ai.getHand();
        return ai.getHand();
    }
    AIPlayer getAI() { return getAI1().ai; }
    GameUI getUI() { return ui; }
    Phase getCurrentPhase() { return currentPhase; }
    int getSelectedSingle() { return selectedSingle; }
    boolean hasPlayedThisTurn() { return hasPlayedThisTurn; }
    LinkedList<Card> getDiscardPile() { return discardPile; }
    void setCurrentPhase(Phase p) { currentPhase = p; }
    void setPendingFiveChoice(boolean v) { pendingFiveChoice = v; }
    void setFiveChoiceCard(Card c) { fiveChoiceCard = c; }
    void setForceOpponentDiscardOne(boolean v) { forceOpponentDiscardOne = v; }

    Participant getHuman() { return turnManager != null ? turnManager.getHuman() : null; }
    Participant getAI1() {
        if (participants == null) return null;
        for (Participant p : participants) {
            if (p.isAI()) return p;
        }
        return null;
    }
    Participant getAI2() {
        if (participants == null) return null;
        boolean foundFirst = false;
        for (Participant p : participants) {
            if (p.isAI()) {
                if (foundFirst) return p;
                foundFirst = true;
            }
        }
        return null;
    }

    GameCharacter getPlayerChar() { Participant h = getHuman(); return h != null ? h.character : null; }
    GameCharacter getAIChar() { Participant a = getAI1(); return a != null ? a.character : null; }
    GameCharacter getAIChar2() { Participant a = getAI2(); return a != null ? a.character : null; }
    AIPlayer getAI2Player() { Participant a = getAI2(); return a != null ? a.ai : null; }

    List<Card> getHandFor(GameCharacter ch) {
        for (Participant p : participants) {
            if (p.character == ch) return p.hand;
        }
        return getHuman().hand;
    }

    Participant getParticipantFor(GameCharacter ch) {
        for (Participant p : participants) {
            if (p.character == ch) return p;
        }
        return null;
    }

    void showAIAttackCard(Card card) {
        ui.atkCardRow.removeAll();
        JComponent cardView = GameUI.createCardView(card, false, -1, false, currentPhase, this);
        ui.atkCardRow.add(cardView);
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

        // Pop-in animation for card entering attack zone (出牌区)
        SwingUtilities.invokeLater(() -> {
            Point p = cardView.getLocationOnScreen();
            SwingUtilities.convertPointFromScreen(p, getGlassPane());
            p.x += cardView.getWidth() / 2 - 40;
            p.y += cardView.getHeight() / 2 - 60;
            GameAnim.playCardPopIn(this, card, p, null);
        });
    }

    void showDefendCard(Card card) {
        ui.defCardRow.removeAll();
        JComponent cardView = GameUI.createCardView(card, false, -1, false, currentPhase, this);
        ui.defCardRow.add(cardView);
        ui.defCardRow.revalidate();
        ui.defCardRow.repaint();

        // Pop-in animation for card entering defend zone
        SwingUtilities.invokeLater(() -> {
            Point p = cardView.getLocationOnScreen();
            SwingUtilities.convertPointFromScreen(p, getGlassPane());
            p.x += cardView.getWidth() / 2 - 40;
            p.y += cardView.getHeight() / 2 - 60;
            GameAnim.playCardPopIn(this, card, p, null);
        });
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
        JComponent cardView = GameUI.createCardView(card, false, -1, false, currentPhase, this);
        ui.aiRevealPanel.add(cardView);
        ui.aiRevealPanel.revalidate();
        ui.aiRevealPanel.repaint();

        // Pop-in animation for card entering judgment zone (判定区)
        SwingUtilities.invokeLater(() -> {
            Point p = cardView.getLocationOnScreen();
            SwingUtilities.convertPointFromScreen(p, getGlassPane());
            p.x += cardView.getWidth() / 2 - 40;
            p.y += cardView.getHeight() / 2 - 60;
            GameAnim.playCardPopIn(this, card, p, null);
        });
    }

    void showAIRevealCards(List<Card> cards) {
        ui.aiRevealPanel.removeAll();
        java.util.List<JComponent> cardViews = new ArrayList<>();
        for (Card c : cards) {
            JComponent cv = GameUI.createCardView(c, false, -1, false, currentPhase, this);
            cardViews.add(cv);
            ui.aiRevealPanel.add(cv);
        }
        ui.aiRevealPanel.revalidate();
        ui.aiRevealPanel.repaint();

        // Staggered pop-in animations for cards entering judgment zone (判定区)
        SwingUtilities.invokeLater(() -> {
            for (int i = 0; i < cardViews.size(); i++) {
                JComponent cv = cardViews.get(i);
                Card c = cards.get(i);
                // Stagger each card by 80ms
                javax.swing.Timer delay = new javax.swing.Timer(i * 80, ev -> {
                    ((javax.swing.Timer) ev.getSource()).stop();
                    Point p = cv.getLocationOnScreen();
                    SwingUtilities.convertPointFromScreen(p, getGlassPane());
                    p.x += cv.getWidth() / 2 - 40;
                    p.y += cv.getHeight() / 2 - 60;
                    GameAnim.playCardPopIn(Game.this, c, p, null);
                });
                delay.setRepeats(false);
                delay.start();
            }
        });
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
            if (selectedSingle >= 0 && selectedSingle < playerHand.size() && playerChar != null) {
                Card clicked = playerHand.get(selectedSingle);
                boolean isDefend = phase == Phase.PLAYER_DEFEND;
                CardTooltipDialog.show(this, clicked, playerChar, isDefend);
            } else {
                CardTooltipDialog.hide(this);
            }
        } else if (phase == Phase.PLAYER_DISCARD) {
            if (selectedMulti.contains(handIndex)) {
                selectedMulti.remove(Integer.valueOf(handIndex));
            } else {
                selectedMulti.add(handIndex);
            }
        }
        updateDisplay();
    }


    GameCharacter getCurrentAttackTarget() {
        if (currentAttackTarget != null) return currentAttackTarget.character;
        if (!is1v2) return aiChar;
        return aiChar;
    }

    AIPlayer getCurrentTargetAI() {
        if (currentAttackTarget != null) return currentAttackTarget.ai;
        if (!is1v2) return ai;
        return ai;
    }

    List<Card> getTargetAIHand() {
        return getCurrentTargetAI().getHand();
    }

    GameCharacter getCurrentTurnAIChar() {
        if (!is1v2) return aiChar;
        return currentTurnTarget == 0 ? aiChar : aiChar2;
    }

    AIPlayer getCurrentTurnAI() {
        if (!is1v2) return ai;
        return currentTurnTarget == 0 ? ai : ai2;
    }

    AIPlayer getAIFor(GameCharacter ch) {
        if (ch == aiChar) return ai;
        if (ch == aiChar2) return ai2;
        return ai;
    }

    GameCharacter chooseAttackTarget(Card card) {
        if (!is1v2) return aiChar;
        boolean ai1Alive = aiChar.isAlive();
        boolean ai2Alive = aiChar2 != null && aiChar2.isAlive();
        if (ai1Alive && ai2Alive) {
            String[] options = {aiChar.getName(), aiChar2.getName()};
            int choice = JOptionPane.showOptionDialog(this,
                "选择攻击目标", "目标选择",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);
            if (choice == 0) return aiChar;
            if (choice == 1) return aiChar2;
            return null;
        }
        if (ai1Alive) return aiChar;
        if (ai2Alive) return aiChar2;
        return aiChar;
    }

    boolean isAI1Alive() { return aiChar.isAlive(); }
    boolean isAI2Alive() { return aiChar2 != null && aiChar2.isAlive(); }
    boolean isAnyAIAlive() { return isAI1Alive() || isAI2Alive(); }




    protected void enterTargetChoice(Runnable onSelected) {
        List<Participant> aliveAIs = new ArrayList<>();
        for (Participant p : participants) {
            if (p.isAI() && p.isAlive()) aliveAIs.add(p);
        }
        if (aliveAIs.size() <= 1) {
            currentAttackTarget = aliveAIs.isEmpty() ? null : aliveAIs.get(0);
            onSelected.run();
            return;
        }
        pendingTargetAction = onSelected;
        currentPhase = Phase.TARGET_CHOICE;
        busy = false;
        updateDisplay();
    }

    void onTargetSelected(int targetIndex) {
        if (currentPhase != Phase.TARGET_CHOICE) return;
        List<Participant> aiParticipants = new ArrayList<>();
        for (Participant p : participants) {
            if (p.isAI() && p.isAlive()) aiParticipants.add(p);
        }
        if (targetIndex < 0 || targetIndex >= aiParticipants.size()) return;
        currentAttackTarget = aiParticipants.get(targetIndex);
        attackTarget = targetIndex;
        Runnable action = pendingTargetAction;
        pendingTargetAction = null;
        if (action != null) action.run();
    }

    protected void doAIDefendFor(GameCharacter defender) {
        defenseEngine.doAIDefendFor(defender);
    }

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
        turnEngine.doPlay();
    }

    protected void doPlayContinue(Card card, int cardIdx, Point from, Point to) {
        turnEngine.doPlayContinue(card, cardIdx, from, to);
    }

    void handleRevealTopDeck(GameCharacter self, GameCharacter opponent, List<Card> selfHand) {
        attackEngine.handleRevealTopDeck(self, opponent, selfHand);
    }

    void handleAIRevealTopDeck(GameCharacter self, GameCharacter opponent, List<Card> selfHand) {
        attackEngine.handleAIRevealTopDeck(self, opponent, selfHand);
    }

    void handleRevealDraw(GameCharacter self, GameCharacter opponent, List<Card> selfHand, boolean isPlayer) {
        attackEngine.handleRevealDraw(self, opponent, selfHand, isPlayer);
    }


    void doEnterDiscard() {
        turnEngine.doEnterDiscard();
    }

    void doConfirmDiscard() {
        turnEngine.doConfirmDiscard();
    }

    void doCancelDiscard() {
        turnEngine.doCancelDiscard();
    }

    void doEndTurn() {
        turnEngine.doEndTurn();
    }

    protected void doAIDefend() {
        defenseEngine.doAIDefend();
    }

    void doPlayerDefend() {
        defenseEngine.doPlayerDefend();
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
        defenseEngine.doPlayerSkipDefend();
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
        if (h instanceof MozeHandler) ((MozeHandler) h).doMozeFourConfirm();
    }

    void doChanFourOpponentConfirm() {
        AIPlayer targetAI = (currentAttackTarget != null && currentAttackTarget.isAI()) ? currentAttackTarget.ai : ai;
        if (selectedAICard < 0 || selectedAICard >= targetAI.handSize()) return;
        CharacterHandler h = getHandler(playerChar);
        if (currentPhase == Phase.SAIKI_THREE_CHOICE && h instanceof SaikiHandler) {
            ((SaikiHandler) h).doSaikiThreeOpponentSelected(selectedAICard);
        } else if (currentPhase == Phase.SAIKI_THREE_CHOICE && h instanceof BlazeHandler) {
            ((BlazeHandler) h).doBlazeFourOpponentSelected(selectedAICard);
        } else if (currentPhase == Phase.SAIKI_THREE_CHOICE && h instanceof MozeHandler) {
            ((MozeHandler) h).doMozeFiveOpponentSelected(selectedAICard);
        } else if (h instanceof ChanHandler) {
            ((ChanHandler) h).doChanFourOpponentSelected(selectedAICard);
        }
    }

    void doSevenChoiceConfirm() {
        AIPlayer targetAI = (currentAttackTarget != null && currentAttackTarget.isAI()) ? currentAttackTarget.ai : ai;
        if (selectedAICard < 0 || selectedAICard >= targetAI.handSize()) return;
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
        defenseEngine.enterPlayerDefend();
    }

    protected void startAITurn() {
        turnEngine.startAITurn();
    }

    protected void resumeAITurn() {
        turnEngine.resumeAITurn();
    }

    protected void advanceToNextAIOrPlayer() {
        turnEngine.advanceToNextAIOrPlayer();
    }

    protected void executeAIStep() {
        turnEngine.executeAIStep();
    }

    protected void refillDeckIfNeeded() {
        turnEngine.refillDeckIfNeeded();
    }

    /** Draw cards from deck (handling reshuffle), returns cards NOT yet added to hand */
    protected List<Card> drawFromDeck(int count) {
        return turnEngine.drawFromDeck(count);
    }

    protected int needToFill(List<Card> hand, int targetSize) {
        return turnEngine.needToFill(hand, targetSize);
    }

    protected void finishPlayerTurn() {
        turnEngine.finishPlayerTurn();
    }

    protected void proceedAfterTurnEnd() {
        turnEngine.proceedAfterTurnEnd();
    }

    protected void applyBurnDamage(GameCharacter target) {
        attackEngine.applyBurnDamage(target);
    }

    protected boolean checkDeath() {
        return attackEngine.checkDeath();
    }

    protected void applyPurifyEffect(GameCharacter ch, boolean isPlayer) {
        attackEngine.applyPurifyEffect(ch, isPlayer);
    }

    protected void applySuperPurifyEffect(GameCharacter ch, boolean isPlayer) {
        attackEngine.applySuperPurifyEffect(ch, isPlayer);
    }

    protected void finishAITurn() {
        turnEngine.finishAITurn();
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
            CharacterSelectPanel selectPanel = new CharacterSelectPanel(this, is1v2);
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
        attackEngine.applyAttackEffect(card, self, opponent, selfHand, onDone);
    }

    protected void checkHandLimit(List<Card> hand, boolean isPlayer) {
        attackEngine.checkHandLimit(hand, isPlayer);
    }

    void handleRevealAndJudge(GameCharacter self, GameCharacter opponent,
                                       List<Card> selfHand, Runnable onDone) {
        attackEngine.handleRevealAndJudge(self, opponent, selfHand, onDone);
    }

    void resolveAIFiveChoice(GameCharacter self, GameCharacter opponent,
                                       List<Card> selfHand, Runnable onDone) {
        attackEngine.resolveAIFiveChoice(self, opponent, selfHand, onDone);
    }

    void handleChanFourSwap(GameCharacter self, GameCharacter opponent,
                             List<Card> selfHand, List<Card> oppHand, Runnable onDone) {
        attackEngine.handleChanFourSwap(self, opponent, selfHand, oppHand, onDone);
    }

    void handleChanFiveReorder(GameCharacter self, GameCharacter opponent,
                                List<Card> selfHand, Runnable onDone) {
        attackEngine.handleChanFiveReorder(self, opponent, selfHand, onDone);
    }

    void handleChanSevenJudge(GameCharacter self, GameCharacter opponent,
                               List<Card> oppHand, Runnable onDone) {
        attackEngine.handleChanSevenJudge(self, opponent, oppHand, onDone);
    }

    void handleChanThreeDefendReveal(GameCharacter self, GameCharacter opponent,
                                      List<Card> selfHand, Runnable onDone) {
        attackEngine.handleChanThreeDefendReveal(self, opponent, selfHand, onDone);
    }

    void handleChanSixReveal(GameCharacter self, GameCharacter opponent,
                              List<Card> selfHand, Runnable onDone) {
        attackEngine.handleChanSixReveal(self, opponent, selfHand, onDone);
    }

    void handleSaikiThreeDraw(GameCharacter self, GameCharacter opponent,
                               List<Card> selfHand, List<Card> oppHand, Runnable onDone) {
        attackEngine.handleSaikiThreeDraw(self, opponent, selfHand, oppHand, onDone);
    }

    void handleSaikiSixJudge(GameCharacter self, GameCharacter opponent,
                              List<Card> selfHand, Runnable onDone) {
        attackEngine.handleSaikiSixJudge(self, opponent, selfHand, onDone);
    }

    void handleSaikiSevenAttack(GameCharacter self, GameCharacter opponent,
                                 List<Card> selfHand, Runnable onDone) {
        attackEngine.handleSaikiSevenAttack(self, opponent, selfHand, onDone);
    }

    void handleBlazeFourDraw(GameCharacter self, GameCharacter opponent,
                               List<Card> selfHand, List<Card> oppHand, Runnable onDone) {
        attackEngine.handleBlazeFourDraw(self, opponent, selfHand, oppHand, onDone);
    }



    void doBlazeFourOpponentSelected(int aiCardIndex) {
        attackEngine.doBlazeFourOpponentSelected(aiCardIndex);
    }

    void handleBlazeDefendTwoDraw(GameCharacter self, GameCharacter opponent,
                                    List<Card> selfHand, Runnable onDone) {
        attackEngine.handleBlazeDefendTwoDraw(self, opponent, selfHand, onDone);
    }

    void handleSerenityFiveReveal(GameCharacter self, GameCharacter opponent,
                                   List<Card> selfHand, Runnable onDone) {
        CharacterHandler h = getHandler(self);
        if (h instanceof SerenityHandler) ((SerenityHandler) h).handleSerenityFiveReveal(self, opponent, selfHand, onDone);
    }

    void handleSerenityZeroDiscard(GameCharacter self, GameCharacter opponent,
                                    List<Card> selfHand, Runnable onDone) {
        CharacterHandler h = getHandler(self);
        if (h instanceof SerenityHandler) ((SerenityHandler) h).handleSerenityZeroDiscard(self, opponent, selfHand, onDone);
    }

    void handleSerenityDefendTwoDrain(GameCharacter self, GameCharacter opponent,
                                       List<Card> selfHand, Runnable onDone) {
        attackEngine.handleSerenityDefendTwoDrain(self, opponent, selfHand, onDone);
    }

    void handleSerenityDefendZeroReveal(GameCharacter self, GameCharacter opponent,
                                         List<Card> selfHand, Runnable onDone) {
        attackEngine.handleSerenityDefendZeroReveal(self, opponent, selfHand, onDone);
    }

    void handleLeonZeroAoe(GameCharacter self, GameCharacter opponent,
                            List<Card> selfHand, Runnable onDone) {
        if (handlers.get("Leon") != null) {
            ((LeonHandler) handlers.get("Leon")).handleLeonZeroAoe(self, opponent, selfHand, onDone);
        } else {
            onDone.run();
        }
    }

    void handleMozeFourGuard(GameCharacter self, GameCharacter opponent,
                              List<Card> selfHand, Runnable onDone) {
        if (handlers.get("Moze") != null) {
            ((MozeHandler) handlers.get("Moze")).handleMozeFourGuard(self, opponent, selfHand, onDone);
        } else {
            onDone.run();
        }
    }

    void handleMozeFiveReveal(GameCharacter self, GameCharacter opponent,
                               List<Card> selfHand, List<Card> oppHand, Runnable onDone) {
        if (handlers.get("Moze") != null) {
            ((MozeHandler) handlers.get("Moze")).handleMozeFiveReveal(self, opponent, selfHand, oppHand, onDone);
        } else {
            onDone.run();
        }
    }

    void handleMozeSevenCleanse(GameCharacter self, GameCharacter opponent,
                                 List<Card> selfHand, Runnable onDone) {
        if (handlers.get("Moze") != null) {
            ((MozeHandler) handlers.get("Moze")).handleMozeSevenCleanse(self, opponent, selfHand, onDone);
        } else {
            onDone.run();
        }
    }

    void handleSaikiZeroAttack(GameCharacter self, GameCharacter opponent,
                                 List<Card> selfHand, Runnable onDone) {
        attackEngine.handleSaikiZeroAttack(self, opponent, selfHand, onDone);
    }

    void handleSaikiThreeDefendReveal(GameCharacter self, GameCharacter opponent,
                                        List<Card> selfHand, Runnable onDone) {
        attackEngine.handleSaikiThreeDefendReveal(self, opponent, selfHand, onDone);
    }

    /** Apply pending attack damage after defense */
    protected void resolvePostDefense(GameCharacter attacker, GameCharacter defender) {
        attackEngine.resolvePostDefense(attacker, defender);
    }

    /** End AI defense → resolve damage → back to player turn */
    protected void finishAIDefend() {
        defenseEngine.finishAIDefend();
    }

    /** End player defense → resolve damage → back to AI turn */
    protected void finishPlayerDefend() {
        defenseEngine.finishPlayerDefend();
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
        if (selectedSingle < 0) CardTooltipDialog.hide(this);
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
                if (selectedSingle >= 0 && selectedSingle < playerHand.size()) {
                    Card sel = playerHand.get(selectedSingle);
                    String skill = SkillDesc.getAttackDesc(playerChar, sel);
                    if (!skill.isEmpty()) ui.phaseLabel.setText(ui.phaseLabel.getText() + "  → " + skill);
                }
                break;
            case PLAYER_DISCARD:
                if (forcedDiscard) {
                    ui.phaseLabel.setText("【手牌超限 - 强制弃牌】手牌超过" + maxPlayerHand + "张，请弃牌至≤" + maxPlayerHand + "张");
                } else {
                    ui.phaseLabel.setText("【你的回合 - 选弃牌】选择要弃掉的牌，点击「确认弃牌」或「取消」");
                }
                break;
            case PLAYER_DEFEND:
                if (hasPlayedBlackDefend) {
                    Card ct = discardPile.getFirst();
                    String cn = colorName(ct.getEffectiveColor());
                    ui.phaseLabel.setText("【搭桥中】请再出一张" + cn + "色≤3的牌完成防御，或出黑牌/药水/+3继续搭桥，或点击「跳过」");
                } else if (playerCanDefend()) {
                    ui.phaseLabel.setText("【防御机会】AI出牌了！选一张数字≤3的匹配牌防御，或出黑牌/药水/+3搭桥，或点击「跳过」");
                } else {
                    ui.phaseLabel.setText("【防御机会】AI出牌了！你无合法防御牌，请点击「跳过」");
                }
                if (selectedSingle >= 0 && selectedSingle < playerHand.size()) {
                    Card sel = playerHand.get(selectedSingle);
                    String skill = SkillDesc.getDefendDesc(playerChar, sel);
                    if (!skill.isEmpty()) ui.phaseLabel.setText(ui.phaseLabel.getText() + "  → " + skill);
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
                if (playerChar instanceof MozeCharacter) {
                    ui.phaseLabel.setText("【4️⃣效果】选一张数字牌获得对应层数守护，然后点击「确认判定」");
                } else {
                    ui.phaseLabel.setText("【6️⃣判定】选一张数字牌入判定区，然后点击「确认判定」");
                }
                break;
            case AI_TURN:
                if (is1v2) {
                    ui.phaseLabel.setText("【" + getCurrentTurnAIChar().getName() + " 回合】出牌中...");
                } else {
                    ui.phaseLabel.setText("【AI 回合】AI 出牌中...");
                }
                break;
            case AI_DEFEND:
                if (is1v2) {
                    GameCharacter defTarget = getCurrentAttackTarget();
                    ui.phaseLabel.setText("【" + defTarget.getName() + " 防御中】考虑是否防御...");
                } else {
                    ui.phaseLabel.setText("【AI 防御中】AI 考虑是否防御...");
                }
                break;
            case TARGET_CHOICE:
                ui.phaseLabel.setText("【选择目标】点击按钮选择攻击目标");
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

        boolean ai1IsAttacker = isAIAttacker() && (!is1v2 || currentTurnTarget == 0);
        boolean ai2IsAttacker = is1v2 && isAIAttacker() && currentTurnTarget == 1;
        ui.aiHandPanel.setBorder(ai1IsAttacker ? GameUI.makeAttackerBorder(aiChar.getName() + " 手牌") : GameUI.makeGlowBorder(aiChar.getName() + " 手牌", new Color(80, 120, 180)));

        ui.aiHandPanel.removeAll();
        if (ai.handSize() == 0) {
            JLabel emptyLabel = new JLabel("AI 无手牌");
            emptyLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
            emptyLabel.setForeground(new Color(110, 150, 120));
            ui.aiHandPanel.add(emptyLabel);
        } else if ((currentPhase == Phase.PLAYER_SEVEN_CHOICE || currentPhase == Phase.SAIKI_THREE_CHOICE || chanFourSelectOpponent)
                && !(is1v2 && currentAttackTarget != null && currentAttackTarget == getAI2())) {
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
            burnPnl.add(GameIcons.makeBuffLabel(GameIcons.buffBurn(), aiChar.getBurnStacks(), new Color(255, 100, 0)));
            ui.aiHandPanel.add(burnPnl);
        }
        if (aiChar.isFrozen()) {
            JPanel freezePnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
            freezePnl.setOpaque(false);
            freezePnl.add(GameIcons.makeBuffLabel(GameIcons.buffFreeze(), 1, new Color(100, 180, 255)));
            ui.aiHandPanel.add(freezePnl);
        }
        if (aiChar.getBleedStacks() > 0) {
            JPanel bleedPnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
            bleedPnl.setOpaque(false);
            bleedPnl.add(GameIcons.makeBuffLabel(GameIcons.buffBleed(), aiChar.getBleedStacks(), new Color(180, 0, 0)));
            ui.aiHandPanel.add(bleedPnl);
        }
        if (aiChar.getGuardStacks() > 0) {
            JPanel guardPnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
            guardPnl.setOpaque(false);
            guardPnl.add(GameIcons.makeBuffLabel(GameIcons.buffGuard(), aiChar.getGuardStacks(), new Color(100, 200, 255)));
            ui.aiHandPanel.add(guardPnl);
        }
        if (aiChar instanceof SerenityCharacter && ((SerenityCharacter) aiChar).isBloodthirst()) {
            JPanel btPnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
            btPnl.setOpaque(false);
            btPnl.add(GameIcons.makeIconLabel(GameIcons.buffBloodthirsty()));
            ui.aiHandPanel.add(btPnl);
        }
        ui.aiHandPanel.revalidate();
        ui.aiHandPanel.repaint();

        // 1v2模式：更新AI2手牌面板
        if (is1v2 && ui instanceof GameUI1v2) {
            GameUI1v2 ui1v2 = (GameUI1v2) ui;
            ui1v2.ai2HpBar.update(aiChar2.getName(), aiChar2.getCurrentHp(), aiChar2.getMaxHp());
            ui1v2.ai2HandPanel.setBorder(ai2IsAttacker ? GameUI.makeAttackerBorder(aiChar2.getName() + " 手牌") : GameUI.makeGlowBorder(aiChar2.getName() + " 手牌", new Color(160, 0, 160)));
            ui1v2.ai2HandPanel.removeAll();
            if (!aiChar2.isAlive()) {
                JLabel deadLabel = new JLabel(aiChar2.getName() + " 已出局");
                deadLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
                deadLabel.setForeground(new Color(200, 80, 80));
                ui1v2.ai2HandPanel.add(deadLabel);
            } else if (ai2.handSize() == 0) {
                JLabel emptyLabel = new JLabel(aiChar2.getName() + " 无手牌");
                emptyLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
                emptyLabel.setForeground(new Color(110, 150, 120));
                ui1v2.ai2HandPanel.add(emptyLabel);
            } else if ((currentPhase == Phase.PLAYER_SEVEN_CHOICE || currentPhase == Phase.SAIKI_THREE_CHOICE || chanFourSelectOpponent)
                    && currentAttackTarget != null && currentAttackTarget == getAI2()) {
                for (int i = 0; i < ai2.handSize(); i++) {
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
                    ui1v2.ai2HandPanel.add(cardBack);
                }
            } else {
                for (int i = 0; i < ai2.handSize(); i++) {
                    ui1v2.ai2HandPanel.add(GameUI.createCardBackView());
                }
            }
            if (aiChar2.getBurnStacks() > 0) {
                JPanel burnPnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
                burnPnl.setOpaque(false);
                burnPnl.add(GameIcons.makeBuffLabel(GameIcons.buffBurn(), aiChar2.getBurnStacks(), new Color(255, 100, 0)));
                ui1v2.ai2HandPanel.add(burnPnl);
            }
            if (aiChar2.isFrozen()) {
                JPanel freezePnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
                freezePnl.setOpaque(false);
                freezePnl.add(GameIcons.makeBuffLabel(GameIcons.buffFreeze(), 1, new Color(100, 180, 255)));
                ui1v2.ai2HandPanel.add(freezePnl);
            }
            if (aiChar2.getBleedStacks() > 0) {
                JPanel bleedPnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
                bleedPnl.setOpaque(false);
                bleedPnl.add(GameIcons.makeBuffLabel(GameIcons.buffBleed(), aiChar2.getBleedStacks(), new Color(180, 0, 0)));
                ui1v2.ai2HandPanel.add(bleedPnl);
            }
            if (aiChar2.getGuardStacks() > 0) {
                JPanel guardPnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
                guardPnl.setOpaque(false);
                guardPnl.add(GameIcons.makeBuffLabel(GameIcons.buffGuard(), aiChar2.getGuardStacks(), new Color(100, 200, 255)));
                ui1v2.ai2HandPanel.add(guardPnl);
            }
            if (aiChar2 instanceof SerenityCharacter && ((SerenityCharacter) aiChar2).isBloodthirst()) {
                JPanel btPnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
                btPnl.setOpaque(false);
                btPnl.add(GameIcons.makeIconLabel(GameIcons.buffBloodthirsty()));
                ui1v2.ai2HandPanel.add(btPnl);
            }
            ui1v2.ai2HandPanel.revalidate();
            ui1v2.ai2HandPanel.repaint();
        }

        // 1v2模式：AI1出局时标记
        if (is1v2 && !aiChar.isAlive()) {
            ui.aiHandPanel.removeAll();
            JLabel deadLabel = new JLabel(aiChar.getName() + " 已出局");
            deadLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
            deadLabel.setForeground(new Color(200, 80, 80));
            ui.aiHandPanel.add(deadLabel);
            ui.aiHandPanel.revalidate();
            ui.aiHandPanel.repaint();
        }

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
            burnPnl.add(GameIcons.makeBuffLabel(GameIcons.buffBurn(), playerChar.getBurnStacks(), new Color(255, 100, 0)));
            ui.playerHandPanel.add(burnPnl);
        }
        if (playerChar.isFrozen()) {
            JPanel freezePnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
            freezePnl.setOpaque(false);
            freezePnl.add(GameIcons.makeBuffLabel(GameIcons.buffFreeze(), 1, new Color(100, 180, 255)));
            ui.playerHandPanel.add(freezePnl);
        }
        if (playerChar.getBleedStacks() > 0) {
            JPanel bleedPnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
            bleedPnl.setOpaque(false);
            bleedPnl.add(GameIcons.makeBuffLabel(GameIcons.buffBleed(), playerChar.getBleedStacks(), new Color(180, 0, 0)));
            ui.playerHandPanel.add(bleedPnl);
        }
        if (playerChar.getGuardStacks() > 0) {
            JPanel guardPnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
            guardPnl.setOpaque(false);
            guardPnl.add(GameIcons.makeBuffLabel(GameIcons.buffGuard(), playerChar.getGuardStacks(), new Color(100, 200, 255)));
            ui.playerHandPanel.add(guardPnl);
        }
        if (playerChar instanceof SerenityCharacter && ((SerenityCharacter) playerChar).isBloodthirst()) {
            JPanel btPnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
            btPnl.setOpaque(false);
            btPnl.add(GameIcons.makeIconLabel(GameIcons.buffBloodthirsty()));
            ui.playerHandPanel.add(btPnl);
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
            ui.fiveHealBtn.setText("加入手牌");
            ui.fiveDamageBtn.setText("弃掉");
        } else {
            ui.fiveHealBtn.setText("回血");
            ui.fiveDamageBtn.setText("伤害");
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

        boolean isTargetChoice = currentPhase == Phase.TARGET_CHOICE;
        ui.targetAI1Btn.setVisible(isTargetChoice);
        ui.targetAI2Btn.setVisible(isTargetChoice && is1v2 && aiChar2 != null && aiChar2.isAlive());
        if (isTargetChoice) {
            List<Participant> aiParticipants = new ArrayList<>();
            for (Participant p : participants) {
                if (p.isAI() && p.isAlive()) aiParticipants.add(p);
            }
            if (aiParticipants.size() > 0) ui.targetAI1Btn.setText(" " + aiParticipants.get(0).character.getName());
            if (aiParticipants.size() > 1) ui.targetAI2Btn.setText(" " + aiParticipants.get(1).character.getName());
            ui.targetAI1Btn.setEnabled(!busy);
            ui.targetAI2Btn.setEnabled(!busy);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Game game = new Game();
            game.gameMode = new Mode1v1();
            ModeSelectPanel selectPanel = new ModeSelectPanel(game);
            game.setContentPane(selectPanel);
            game.setSize(900, 600);
            game.setLocationRelativeTo(null);
            game.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            game.setVisible(true);
        });
    }
}
