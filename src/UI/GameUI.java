import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.GlyphVector;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class GameUI {
    static final Color BG_COLOR = new Color(255, 245, 225);
    static final Color PANEL_BG = new Color(255, 250, 240);

    static final Color CARD_RED    = new Color(255, 30, 40);
    static final Color CARD_RED_DK    = new Color(220, 20, 30);
    static final Color CARD_YELLOW = new Color(255, 195, 0);
    static final Color CARD_YELLOW_DK = new Color(230, 170, 0);
    static final Color CARD_BLUE   = new Color(0, 130, 255);
    static final Color CARD_BLUE_DK   = new Color(0, 100, 210);
    static final Color CARD_GREEN  = new Color(0, 200, 60);
    static final Color CARD_GREEN_DK  = new Color(0, 165, 40);
    static final Color CARD_BLACK  = new Color(50, 45, 55);
    static final Color CARD_BLACK_DK  = new Color(30, 25, 35);
    static final Color CARD_WHITE  = new Color(230, 230, 235);
    static final Color CARD_WHITE_DK  = new Color(190, 190, 200);

    JLabel deckLabel;
    JLabel phaseLabel;
    JLabel attackerLabel;
    JPanel discardCardPanel;
    JPanel playerHandPanel;
    JPanel aiHandPanel;
    JPanel aiAttackPanel;
    JPanel aiRevealPanel;
    JPanel atkCardRow;
    JPanel defCardRow;
    JLabel attackDescLabel;
    JLabel defendDescLabel;
    JButton playBtn;
    JButton enterDiscardBtn;
    JButton confirmDiscardBtn;
    JButton cancelDiscardBtn;
    JButton endTurnBtn;
    JButton defendBtn;
    JButton skipDefendBtn;
    JButton fiveHealBtn;
    JButton fiveDamageBtn;
    JButton sevenChoiceBtn;
    JButton targetAI1Btn;
    JButton targetAI2Btn;
    JButton resetBtn;
    JLabel errorHintLabel;
    HpBar playerHpBar;
    HpBar aiHpBar;
    JLabel playerBurnLabel;
    JLabel aiBurnLabel;
    protected JPanel rootPanel;

    void buildUI(Game game) {
        game.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        game.setMinimumSize(new Dimension(860, 700));
        game.setResizable(true);

        rootPanel = new GradientPanel(new Color(255, 240, 210), new Color(255, 220, 180));
        rootPanel.setLayout(new BorderLayout(12, 10));
        rootPanel.setBorder(BorderFactory.createEmptyBorder(12, 14, 14, 14));

        rootPanel.add(buildTopPanel(), BorderLayout.NORTH);
        rootPanel.add(buildCenterPanel(), BorderLayout.CENTER);
        rootPanel.add(buildBottomPanel(game), BorderLayout.SOUTH);

        JPanel glassPane = new JPanel(null);
        glassPane.setOpaque(false);
        game.setGlassPane(glassPane);
        glassPane.setVisible(true);
    }

    JPanel getRootPanel() { return rootPanel; }

    // ── Top panel: title + deck info ──
    protected JPanel buildTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout(6, 2));
        topPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Furry Battle", SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                double pulse = 0.85 + 0.15 * Math.sin(System.currentTimeMillis() / 800.0);
                int r = (int)(240 * pulse), gr = (int)(80 * pulse), b = (int)(60 * pulse);
                String text = getText();
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(text)) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.setColor(new Color(0, 0, 0, 40));
                g2.drawString(text, x + 2, y + 2);
                g2.setColor(new Color(255, 200, 150, 80));
                g2.drawString(text, x + 1, y + 1);
                g2.setColor(new Color(r, gr, b));
                g2.drawString(text, x, y);
                g2.dispose();
            }
        };
        titleLabel.setFont(new Font("Arial", Font.BOLD, 34));
        titleLabel.setForeground(new Color(240, 80, 60));
        titleLabel.setIcon(GameIcons.uiSparkling());
        titleLabel.setHorizontalTextPosition(SwingConstants.CENTER);
        titleLabel.setVerticalTextPosition(SwingConstants.BOTTOM);
        topPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 24, 2));
        infoPanel.setOpaque(false);
        deckLabel = new JLabel();
        deckLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        deckLabel.setForeground(new Color(120, 60, 40));
        infoPanel.add(deckLabel);
        topPanel.add(infoPanel, BorderLayout.SOUTH);

        return topPanel;
    }

    // ── Center: discard pile + AI area ──
    protected JPanel buildCenterPanel() {
        JPanel centerPanel = new JPanel(new BorderLayout(14, 8));
        centerPanel.setOpaque(false);

        // Discard area
        JPanel discardArea = new JPanel(new BorderLayout(4, 4));
        discardArea.setOpaque(false);
        JLabel discardTopLabel = new JLabel("弃牌库顶", SwingConstants.CENTER);
        discardTopLabel.setFont(new Font("微软雅黑", Font.BOLD, 12));
        discardTopLabel.setForeground(new Color(180, 60, 40));
        discardArea.add(discardTopLabel, BorderLayout.NORTH);

        discardCardPanel = new JPanel();
        discardCardPanel.setOpaque(false);
        discardCardPanel.setPreferredSize(new Dimension(100, 140));
        discardArea.add(discardCardPanel, BorderLayout.CENTER);
        centerPanel.add(discardArea, BorderLayout.WEST);

        // AI HP bar + zones
        aiHpBar = new HpBar();
        JPanel aiTopWrap = new JPanel(new BorderLayout(4, 2));
        aiTopWrap.setOpaque(false);
        aiTopWrap.add(aiHpBar, BorderLayout.NORTH);

        // Left column: AI hand (top) + play area (bottom)
        aiHandPanel = new JPanel();
        aiHandPanel.setLayout(new WrapLayout(FlowLayout.LEFT, 8, 6));
        aiHandPanel.setBackground(PANEL_BG);
        aiHandPanel.setBorder(makeGlowBorder("AI 手牌", new Color(0, 120, 220)));
        aiHandPanel.setPreferredSize(new Dimension(0, 110));

        aiBurnLabel = new JLabel();
        aiBurnLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        aiBurnLabel.setForeground(new Color(255, 80, 0));
        aiBurnLabel.setVisible(false);

        JPanel aiHandWrap = new JPanel(new BorderLayout(2, 0));
        aiHandWrap.setOpaque(false);
        aiHandWrap.add(aiHandPanel, BorderLayout.CENTER);

        aiAttackPanel = new JPanel();
        aiAttackPanel.setLayout(new BorderLayout(4, 2));
        aiAttackPanel.setBackground(new Color(255, 240, 230));
        aiAttackPanel.setBorder(makeGlowBorder("出牌区", new Color(220, 80, 60)));
        aiAttackPanel.setPreferredSize(new Dimension(0, 210));

        JPanel atkZone = new JPanel(new BorderLayout(2, 2));
        atkZone.setOpaque(false);
        JLabel atkZoneLabel = new JLabel("进攻", SwingConstants.CENTER);
        atkZoneLabel.setFont(new Font("微软雅黑", Font.BOLD, 11));
        atkZoneLabel.setForeground(new Color(220, 60, 40));
        this.atkCardRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
        this.atkCardRow.setOpaque(false);
        JLabel emptyAtk = new JLabel("等待出牌", SwingConstants.CENTER);
        emptyAtk.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        emptyAtk.setForeground(new Color(200, 160, 140));
        this.atkCardRow.add(emptyAtk);
        atkZone.add(atkZoneLabel, BorderLayout.NORTH);
        atkZone.add(this.atkCardRow, BorderLayout.CENTER);

        JPanel defZone = new JPanel(new BorderLayout(2, 2));
        defZone.setOpaque(false);
        JLabel defZoneLabel = new JLabel("防御", SwingConstants.CENTER);
        defZoneLabel.setFont(new Font("微软雅黑", Font.BOLD, 11));
        defZoneLabel.setForeground(new Color(0, 100, 200));
        this.defCardRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
        this.defCardRow.setOpaque(false);
        JLabel emptyDef = new JLabel("等待防御", SwingConstants.CENTER);
        emptyDef.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        emptyDef.setForeground(new Color(160, 180, 200));
        this.defCardRow.add(emptyDef);
        defZone.add(defZoneLabel, BorderLayout.NORTH);
        defZone.add(this.defCardRow, BorderLayout.CENTER);

        JPanel playZone = new JPanel(new GridLayout(1, 2, 8, 0));
        playZone.setOpaque(false);
        playZone.add(atkZone);
        playZone.add(defZone);

        attackDescLabel = new JLabel("", SwingConstants.CENTER);
        attackDescLabel.setFont(new Font("微软雅黑", Font.BOLD, 13));
        attackDescLabel.setForeground(new Color(200, 60, 40));

        defendDescLabel = new JLabel("", SwingConstants.CENTER);
        defendDescLabel.setFont(new Font("微软雅黑", Font.BOLD, 13));
        defendDescLabel.setForeground(new Color(0, 100, 200));

        JPanel descRow = new JPanel(new GridLayout(1, 2, 8, 0));
        descRow.setOpaque(false);
        descRow.add(attackDescLabel);
        descRow.add(defendDescLabel);

        aiAttackPanel.add(playZone, BorderLayout.CENTER);
        aiAttackPanel.add(descRow, BorderLayout.SOUTH);

        JPanel leftCol = new JPanel(new BorderLayout(4, 4));
        leftCol.setOpaque(false);
        leftCol.add(aiHandWrap, BorderLayout.NORTH);
        leftCol.add(aiAttackPanel, BorderLayout.CENTER);

        // Right column: reveal (full height)
        aiRevealPanel = new JPanel();
        aiRevealPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 4, 4));
        aiRevealPanel.setBackground(new Color(230, 240, 255));
        aiRevealPanel.setBorder(makeGlowBorder("判定", new Color(60, 100, 200)));
        aiRevealPanel.setPreferredSize(new Dimension(180, 0));

        JPanel aiAreaWrap = new JPanel(new BorderLayout(8, 0));
        aiAreaWrap.setOpaque(false);
        aiAreaWrap.add(leftCol, BorderLayout.CENTER);
        aiAreaWrap.add(aiRevealPanel, BorderLayout.EAST);

        aiTopWrap.add(aiAreaWrap, BorderLayout.CENTER);
        centerPanel.add(aiTopWrap, BorderLayout.CENTER);

        return centerPanel;
    }

    // ── Bottom: player hand + controls + status ──
    protected JPanel buildBottomPanel(Game game) {
        playerHandPanel = new JPanel();
        playerHandPanel.setLayout(new WrapLayout(FlowLayout.LEFT, 6, 6));
        playerHandPanel.setBackground(new Color(255, 248, 235));
        playerHandPanel.setBorder(makeGlowBorder("你的手牌", new Color(255, 140, 0)));

        JScrollPane scrollPane = new JScrollPane(playerHandPanel);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(24);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.getHorizontalScrollBar().setUI(new DarkScrollBarUI());
        scrollPane.getVerticalScrollBar().setUI(new DarkScrollBarUI());
        scrollPane.setMinimumSize(new Dimension(0, 150));

        JPanel handArea = new JPanel(new BorderLayout());
        handArea.setOpaque(false);

        playerHpBar = new HpBar();
        JPanel handTopBar = new JPanel(new BorderLayout());
        handTopBar.setOpaque(false);
        handTopBar.add(playerHpBar, BorderLayout.NORTH);
        errorHintLabel = new JLabel("", SwingConstants.CENTER);
        errorHintLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        errorHintLabel.setForeground(new Color(220, 20, 30));
        errorHintLabel.setVisible(false);
        handTopBar.add(errorHintLabel, BorderLayout.SOUTH);
        handArea.add(handTopBar, BorderLayout.NORTH);

        playerBurnLabel = new JLabel();
        playerBurnLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        playerBurnLabel.setForeground(new Color(255, 80, 0));
        playerBurnLabel.setVisible(false);

        handArea.add(scrollPane, BorderLayout.CENTER);

        // Control buttons
        JPanel controlPanel = new JPanel(new WrapLayout(FlowLayout.CENTER, 10, 6));
        controlPanel.setOpaque(false);
        controlPanel.setPreferredSize(new Dimension(860, 46));
        controlPanel.setMinimumSize(new Dimension(460, 46));

        playBtn         = makeBtn(" 出牌",    new Color(255, 235, 235), new Color(255, 200, 200), new Color(200, 50, 50));
        enterDiscardBtn = makeBtn(" 弃牌",    new Color(255, 245, 225), new Color(255, 230, 195), new Color(180, 110, 0));
        confirmDiscardBtn=makeBtn(" 确认弃牌", new Color(255, 245, 225), new Color(255, 230, 195), new Color(180, 110, 0));
        cancelDiscardBtn= makeBtn("✘ 取消",    new Color(240, 240, 240), new Color(225, 225, 225), new Color(120, 110, 100));
        endTurnBtn      = makeBtn(" 结束回合",new Color(240, 235, 250), new Color(225, 215, 245), new Color(90, 50, 130));
        defendBtn       = makeBtn(" 防御",    new Color(230, 240, 255), new Color(200, 225, 255), new Color(0, 80, 180));
        skipDefendBtn   = makeBtn(" 跳过",  new Color(240, 240, 240), new Color(225, 225, 225), new Color(120, 110, 100));
        fiveHealBtn     = makeBtn("恢复", new Color(230, 250, 235), new Color(200, 240, 210), new Color(0, 140, 60));
        fiveDamageBtn   = makeBtn(" 1.5倍",new Color(255, 235, 235), new Color(255, 215, 215), new Color(180, 30, 30));
        sevenChoiceBtn  = makeBtn(" 指定弃牌", new Color(255, 242, 225), new Color(255, 228, 195), new Color(200, 90, 0));
        targetAI1Btn    = makeBtn(" 目标:AI1", new Color(230, 240, 255), new Color(200, 225, 255), new Color(0, 80, 180));
        targetAI2Btn    = makeBtn(" 目标:AI2", new Color(240, 220, 255), new Color(225, 200, 255), new Color(120, 0, 180));

        playBtn.setIcon(GameIcons.uiBattle());
        enterDiscardBtn.setIcon(GameIcons.uiTrash());
        confirmDiscardBtn.setIcon(GameIcons.uiTick());
        endTurnBtn.setIcon(GameIcons.uiSkip());
        defendBtn.setIcon(GameIcons.uiShield());
        skipDefendBtn.setIcon(GameIcons.uiSkip());
        fiveDamageBtn.setIcon(GameIcons.uiSword());

        playBtn.addActionListener(e -> { if (game.canClickBtn()) game.doPlay(); });
        enterDiscardBtn.addActionListener(e -> { if (game.canClickBtn()) game.doEnterDiscard(); });
        confirmDiscardBtn.addActionListener(e -> { if (game.canClickBtn()) game.doConfirmDiscard(); });
        cancelDiscardBtn.addActionListener(e -> { if (game.canClickBtn()) game.doCancelDiscard(); });
        endTurnBtn.addActionListener(e -> { if (game.canClickBtn()) game.doEndTurn(); });
        defendBtn.addActionListener(e -> { if (game.canClickBtn()) game.doPlayerDefend(); });
        skipDefendBtn.addActionListener(e -> { if (game.canClickBtn()) game.doPlayerSkipDefend(); });
        fiveHealBtn.addActionListener(e -> { if (game.canClickBtn()) game.doFiveChoiceHeal(); });
        fiveDamageBtn.addActionListener(e -> { if (game.canClickBtn()) game.doFiveChoiceDamage(); });
        sevenChoiceBtn.addActionListener(e -> {
            if (!game.canClickBtn()) return;
            if (game.chanSevenKeepMode) game.doChanSevenKeep();
            else if (game.chanFourSwapMode) game.doChanFourSwapConfirm();
            else if (game.chanFourSelectOpponent) game.doChanFourOpponentConfirm();
            else if (game.currentPhase == Game.Phase.SAIKI_SIX_JUDGE) {
                game.doSaikiSixConfirm();
            }
            else if (game.currentPhase == Game.Phase.PLAYER_SEVEN_CHOICE) game.doSevenChoiceConfirm();
            else if (game.currentPhase == Game.Phase.SAIKI_THREE_CHOICE) game.doChanFourOpponentConfirm();
        });

        targetAI1Btn.addActionListener(e -> { if (game.canClickBtn()) game.onTargetSelected(0); });
        targetAI2Btn.addActionListener(e -> { if (game.canClickBtn()) game.onTargetSelected(1); });

        controlPanel.add(playBtn);
        controlPanel.add(enterDiscardBtn);
        controlPanel.add(confirmDiscardBtn);
        controlPanel.add(cancelDiscardBtn);
        controlPanel.add(endTurnBtn);
        controlPanel.add(defendBtn);
        controlPanel.add(skipDefendBtn);
        controlPanel.add(fiveHealBtn);
        controlPanel.add(fiveDamageBtn);
        controlPanel.add(sevenChoiceBtn);
        controlPanel.add(targetAI1Btn);
        controlPanel.add(targetAI2Btn);

        // Status area (right side)
        resetBtn = makeBtn(" 重新开始", new Color(240, 235, 250), new Color(225, 215, 245), new Color(90, 50, 130));
        resetBtn.setIcon(GameIcons.uiRestart());
        resetBtn.addActionListener(e -> game.backToSelect());

        JPanel resetPanel = new JPanel(new BorderLayout(4, 4));
        resetPanel.setOpaque(false);
        resetPanel.setPreferredSize(new Dimension(170, 0));

        JPanel resetBtnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        resetBtnPanel.setOpaque(false);
        resetBtnPanel.add(resetBtn);
        resetPanel.add(resetBtnPanel, BorderLayout.NORTH);

        attackerLabel = new JLabel();
        attackerLabel.setFont(new Font("微软雅黑", Font.BOLD, 15));
        attackerLabel.setForeground(new Color(220, 30, 30));

        phaseLabel = new JLabel();
        phaseLabel.setFont(new Font("微软雅黑", Font.BOLD, 13));
        phaseLabel.setForeground(new Color(200, 80, 20));

        JPanel hintPanel = new JPanel();
        hintPanel.setOpaque(false);
        hintPanel.setLayout(new BoxLayout(hintPanel, BoxLayout.Y_AXIS));
        hintPanel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        attackerLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        phaseLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        hintPanel.add(attackerLabel);
        hintPanel.add(Box.createVerticalStrut(2));
        hintPanel.add(phaseLabel);
        resetPanel.add(hintPanel, BorderLayout.SOUTH);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.add(handArea, BorderLayout.CENTER);
        bottomPanel.add(controlPanel, BorderLayout.SOUTH);
        bottomPanel.add(resetPanel, BorderLayout.EAST);

        return bottomPanel;
    }

    // ── Gradient button with press animation ──
    private JButton makeBtn(String text, Color base, Color hover, Color textColor) {
        JButton btn = new JButton(text) {
            private boolean pressed = false;
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                Color useBase = base;
                Color useText = textColor;
                if (!isEnabled()) {
                    useBase = new Color(230, 230, 230);
                    useText = new Color(180, 180, 180);
                } else if (pressed) {
                    useBase = darken(hover, 0.88);
                } else if (getModel().isRollover()) {
                    useBase = hover;
                }
                g2.setColor(new Color(0, 0, 0, 25));
                g2.fillRoundRect(2, 3, w - 2, h - 2, 10, 10);
                GradientPaint gp = new GradientPaint(0, 0, useBase, 0, h, darken(useBase, 0.92));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, w - 1, h - 1, 10, 10);
                g2.setColor(new Color(0, 0, 0, 20));
                g2.drawRoundRect(0, 0, w - 1, h - 1, 10, 10);
                g2.setColor(new Color(255, 255, 255, 90));
                g2.drawRoundRect(1, 1, w - 3, h / 2 - 1, 9, 9);
                g2.setColor(new Color(255, 255, 255, 40));
                g2.fillRoundRect(3, 2, w - 7, h / 3, 8, 8);
                g2.dispose();
                setForeground(useText);
                super.paintComponent(g);
            }
            private Color darken(Color c, double factor) {
                return new Color((int)(c.getRed()*factor), (int)(c.getGreen()*factor), (int)(c.getBlue()*factor));
            }
        };
        btn.setFont(new Font("微软雅黑", Font.BOLD, 13));
        btn.setForeground(textColor);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setPreferredSize(new Dimension(115, 38));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { btn.repaint(); }
            @Override public void mouseReleased(MouseEvent e) { btn.repaint(); }
        });
        return btn;
    }

    // ── Glow titled border ──
    static javax.swing.border.TitledBorder makeGlowBorder(String title, Color glow) {
        return BorderFactory.createTitledBorder(
                new GlowBorder(glow),
                title,
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                new Font("微软雅黑", Font.BOLD, 13),
                new Color(140, 60, 30));
    }

    static javax.swing.border.TitledBorder makeAttackerBorder(String title) {
        return BorderFactory.createTitledBorder(
                new GlowBorder(new Color(255, 30, 30), 3, true),
                title + " 进攻方",
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                new Font("微软雅黑", Font.BOLD, 14),
                new Color(200, 20, 20));
    }

    // ── AI card back ──
    static JPanel createCardBackView() {
        JPanel cardPanel = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                g2.setColor(new Color(0, 0, 0, 30));
                g2.fillRoundRect(4, 4, w - 3, h - 3, 14, 14);
                GradientPaint gp = new GradientPaint(0, 0, new Color(70, 130, 240), w, h, new Color(30, 80, 200));
                g2.setPaint(gp);
                g2.fillRoundRect(1, 1, w - 3, h - 3, 14, 14);
                g2.setColor(new Color(255, 255, 255, 120));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, w - 3, h - 3, 14, 14);
                g2.setColor(new Color(255, 255, 255, 50));
                g2.fillRoundRect(3, 3, w - 7, h / 2, 12, 12);
                g2.setColor(new Color(255, 255, 255, 100));
                g2.drawRoundRect(8, 8, w - 18, h - 18, 10, 10);
                g2.dispose();
            }
        };
        cardPanel.setPreferredSize(new Dimension(56, 82));
        cardPanel.setOpaque(false);

        JLabel qLabel = GameIcons.makeIconLabel(GameIcons.scaled("/icons/ui_icons/sparkling.png", 26, 26));
        cardPanel.add(qLabel, BorderLayout.CENTER);
        return cardPanel;
    }

    // ── Player card view ──
    static JPanel createCardView(Card card, boolean clickable, int handIndex, boolean selected, Game.Phase phase, Game game) {
        int cw = 80, ch = 120;
        BufferedImage baseImg = GameAnim.renderCardImage(card, cw, ch);
        boolean hasChosen = card.getChosenColor() != null && (card.isBlack() || card.isWhite());
        Color chosenColor = hasChosen ? getSwingColor(card.getChosenColor()) : null;

        JPanel cardPanel = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();

                boolean hover = Boolean.TRUE.equals(getClientProperty("hover"));
                int offsetY = (hover && clickable && !selected) ? -6 : 0;

                g2.setColor(new Color(0, 0, 0, selected ? 30 : (hover && clickable ? 45 : 25)));
                g2.fillRoundRect(4, 6 + offsetY, w - 5, h - 5, 16, 16);

                if (baseImg != null) {
                    g2.drawImage(baseImg, 0, offsetY, w, h, null);
                }

                if (selected) {
                    g2.setColor(new Color(160, 80, 220, 200));
                    g2.setStroke(new BasicStroke(3f));
                    g2.drawRoundRect(2, 2 + offsetY, w - 5, h - 5, 16, 16);
                    g2.setColor(new Color(160, 80, 220, 60));
                    g2.setStroke(new BasicStroke(8f));
                    g2.drawRoundRect(2, 2 + offsetY, w - 5, h - 5, 16, 16);
                } else if (hasChosen) {
                    g2.setColor(new Color(chosenColor.getRed(), chosenColor.getGreen(), chosenColor.getBlue(), 30));
                    g2.setStroke(new BasicStroke(12f));
                    g2.drawRoundRect(2, 2 + offsetY, w - 5, h - 5, 16, 16);
                    g2.setColor(new Color(chosenColor.getRed(), chosenColor.getGreen(), chosenColor.getBlue(), 70));
                    g2.setStroke(new BasicStroke(5f));
                    g2.drawRoundRect(3, 3 + offsetY, w - 7, h - 7, 15, 15);
                    g2.setColor(chosenColor);
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawRoundRect(4, 4 + offsetY, w - 9, h - 9, 14, 14);
                } else if (hover && clickable) {
                    g2.setColor(new Color(255, 255, 255, 30));
                    g2.setStroke(new BasicStroke(6f));
                    g2.drawRoundRect(2, 2 + offsetY, w - 5, h - 5, 16, 16);
                }

                g2.dispose();
            }
        };
        cardPanel.setPreferredSize(new Dimension(cw, ch));
        cardPanel.setOpaque(false);

        if (clickable) {
            cardPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            cardPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) { game.onCardClicked(handIndex, phase); }
                @Override
                public void mouseEntered(MouseEvent e) {
                    cardPanel.putClientProperty("hover", true);
                    cardPanel.repaint();
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    cardPanel.putClientProperty("hover", false);
                    cardPanel.repaint();
                }
            });
        }

        return cardPanel;
    }

    // ── Color helpers ──
    static Color getSwingColor(Card.CardColor color) {
        switch (color) {
            case RED:    return CARD_RED;
            case YELLOW: return CARD_YELLOW;
            case BLUE:   return CARD_BLUE;
            case GREEN:  return CARD_GREEN;
            case BLACK:  return CARD_BLACK;
            case WHITE:  return CARD_WHITE;
            default:     return Color.GRAY;
        }
    }

    private static Color getDarkColor(Card.CardColor color) {
        switch (color) {
            case RED:    return CARD_RED_DK;
            case YELLOW: return CARD_YELLOW_DK;
            case BLUE:   return CARD_BLUE_DK;
            case GREEN:  return CARD_GREEN_DK;
            case BLACK:  return CARD_BLACK_DK;
            case WHITE:  return CARD_WHITE_DK;
            default:     return Color.DARK_GRAY;
        }
    }

    // ── Color chooser (custom styled) ──
    static Card.CardColor showColorChooser(JFrame parent) {
        ColorChooserDialog dialog = new ColorChooserDialog(parent);
        dialog.setVisible(true);
        return dialog.getResult();
    }

    static String colorSymbol(Card.CardColor color) {
        switch (color) {
            case RED:    return "红";
            case YELLOW: return "黄";
            case BLUE:   return "蓝";
            case GREEN:  return "绿";
            case WHITE:  return "白";
            default:     return "?";
        }
    }

    // ══════════════════════════════════════════
    //  Custom components
    // ══════════════════════════════════════════

    /** Panel with vertical gradient background + floating particles */
    static class GradientPanel extends JPanel {
        private final Color top, bottom;
        private final java.util.List<int[]> particles = new ArrayList<>();
        private final Random rng = new Random();
        private Timer particleTimer;

        GradientPanel(Color top, Color bottom) {
            this.top = top;
            this.bottom = bottom;
            setOpaque(false);
            for (int i = 0; i < 18; i++) {
                particles.add(new int[]{
                    rng.nextInt(1200), rng.nextInt(800),
                    2 + rng.nextInt(4),
                    rng.nextInt(360),
                    1 + rng.nextInt(2)
                });
            }
            particleTimer = new Timer(80, e -> repaint());
            particleTimer.start();
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            int w = getWidth(), h = getHeight();
            GradientPaint gp = new GradientPaint(0, 0, top, 0, h, bottom);
            g2.setPaint(gp);
            g2.fillRect(0, 0, w, h);

            long now = System.currentTimeMillis();
            for (int[] p : particles) {
                int px = (p[0] + (int)(now / 40.0 * p[4])) % (w + 40) - 20;
                int py = (p[1] + (int)(Math.sin(now / 2000.0 + p[3]) * 15)) % h;
                int size = p[2];
                int alpha = 20 + (int)(15 * Math.sin(now / 1500.0 + p[3]));
                g2.setColor(new Color(255, 220, 180, alpha));
                g2.fillOval(px, py, size, size);
            }

            g2.dispose();
        }
    }

    /** Glow line border */
    static class GlowBorder implements javax.swing.border.Border {
        private final Color color;
        private final int thickness;
        private final boolean pulse;
        GlowBorder(Color color) { this(color, 2, false); }
        GlowBorder(Color color, int thickness, boolean pulse) { this.color = color; this.thickness = thickness; this.pulse = pulse; }
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int alpha = 180;
            if (pulse) {
                double phase = (System.currentTimeMillis() % 1500) / 1500.0;
                alpha = 80 + (int)(120 * (0.5 + 0.5 * Math.sin(phase * Math.PI * 2)));
            }
            for (int i = 0; i < thickness + 2; i++) {
                int a = (i < thickness) ? (40 + alpha * (thickness - i) / thickness) : Math.max(0, 30 - (i - thickness) * 20);
                g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), a));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(x + i, y + i, w - 1 - i * 2, h - 1 - i * 2, 12, 12);
            }
            g2.dispose();
        }
        @Override public Insets getBorderInsets(Component c) { return new Insets(thickness + 4, thickness + 4, thickness + 4, thickness + 4); }
        @Override public boolean isBorderOpaque() { return false; }
    }

    /** Minimal dark scrollbar */
    static class DarkScrollBarUI extends javax.swing.plaf.basic.BasicScrollBarUI {
        @Override protected void configureScrollBarColors() { thumbColor = new Color(200, 180, 150); trackColor = new Color(250, 245, 235); }
        @Override protected JButton createDecreaseButton(int o) { return zeroSize(); }
        @Override protected JButton createIncreaseButton(int o) { return zeroSize(); }
        private JButton zeroSize() { JButton b = new JButton(); b.setPreferredSize(new Dimension(0, 0)); return b; }
        @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            GradientPaint gp = new GradientPaint(r.x, r.y, new Color(210, 185, 150), r.x, r.y + r.height, new Color(185, 160, 125));
            g2.setPaint(gp);
            g2.fillRoundRect(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 6, 6);
            g2.dispose();
        }
        @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
            g.setColor(new Color(255, 248, 235));
            g.fillRect(r.x, r.y, r.width, r.height);
        }
    }

    /** WrapLayout for hand panels */
    static class WrapLayout extends FlowLayout {
        public WrapLayout(int align, int hgap, int vgap) { super(align, hgap, vgap); }
        @Override public Dimension preferredLayoutSize(Container target) {
            return layoutSize(target, true);
        }
        @Override public Dimension minimumLayoutSize(Container target) {
            Dimension d = layoutSize(target, false);
            d.width = Math.min(d.width, 200);
            return d;
        }
        private Dimension layoutSize(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                int w = target.getWidth();
                if (w <= 0) {
                    Container parent = target.getParent();
                    if (parent != null) w = parent.getWidth();
                }
                if (w <= 0) w = 900;
                int hgap = getHgap(), vgap = getVgap();
                Insets insets = target.getInsets();
                int maxW = w - insets.left - insets.right - hgap;
                if (maxW < 50) maxW = 800;
                int x = 0, y = insets.top, rowH = 0, totalH = 0;
                int count = target.getComponentCount();
                for (int i = 0; i < count; i++) {
                    Component c = target.getComponent(i);
                    if (!c.isVisible()) continue;
                    Dimension d = preferred ? c.getPreferredSize() : c.getMinimumSize();
                    if (x > 0 && x + d.width > maxW) {
                        x = 0;
                        y += rowH + vgap;
                        rowH = 0;
                    }
                    x += d.width + hgap;
                    rowH = Math.max(rowH, d.height);
                }
                totalH = y + rowH + insets.bottom;
                return new Dimension(w, totalH);
            }
        }
    }

    /** HP bar with name, numeric display, damage flash, and animated fill */
    static class HpBar extends JPanel {
        private String charName = "";
        private int hp = 100, maxHp = 100;
        private int displayHp = 100;
        private Timer animTimer;
        private float flashAlpha = 0f;
        private boolean flashRed = true;
        private Timer flashTimer;

        HpBar() {
            setPreferredSize(new Dimension(200, 38));
            setOpaque(false);
        }

        void update(String name, int hp, int maxHp) {
            this.charName = name; this.hp = hp; this.maxHp = maxHp;
            if (animTimer != null && animTimer.isRunning()) animTimer.stop();

            if (displayHp != hp) {
                boolean isDamage = hp < displayHp;
                if (isDamage) {
                    flashRed = true;
                    flashAlpha = 0.6f;
                    startFlashTimer();
                } else {
                    flashRed = false;
                    flashAlpha = 0.4f;
                    startFlashTimer();
                }

                int start = displayHp;
                int diff = hp - start;
                int steps = Math.max(8, Math.abs(diff));
                int[] step = {0};
                animTimer = new Timer(30, e -> {
                    step[0]++;
                    double t = (double) step[0] / steps;
                    double ease = t < 0.5 ? 2 * t * t : 1 - Math.pow(-2 * t + 2, 2) / 2;
                    displayHp = (int)(start + diff * ease);
                    repaint();
                    if (step[0] >= steps) {
                        displayHp = hp;
                        ((Timer)e.getSource()).stop();
                        repaint();
                    }
                });
                animTimer.start();
            }
            repaint();
        }

        private void startFlashTimer() {
            if (flashTimer != null && flashTimer.isRunning()) flashTimer.stop();
            flashTimer = new Timer(40, e -> {
                flashAlpha -= 0.05f;
                if (flashAlpha <= 0) {
                    flashAlpha = 0;
                    ((Timer)e.getSource()).stop();
                }
                repaint();
            });
            flashTimer.start();
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            int barW = Math.max(60, w - 80);

            g2.setFont(new Font("微软雅黑", Font.BOLD, 13));
            g2.setColor(new Color(80, 40, 20));
            g2.drawString(charName, 4, 13);

            String hpText = displayHp + "/" + maxHp;
            g2.setFont(new Font("Arial", Font.BOLD, 12));
            g2.setColor(new Color(60, 30, 10));
            g2.drawString(hpText, w - g2.getFontMetrics().stringWidth(hpText) - 4, 13);

            int barX = 4, barY = 17, barH = h - 20;

            g2.setColor(new Color(60, 40, 30, 80));
            g2.fillRoundRect(barX - 1, barY - 1, barW + 2, barH + 2, 8, 8);

            g2.setColor(new Color(200, 180, 170));
            g2.fillRoundRect(barX, barY, barW, barH, 7, 7);

            double pct = Math.max(0, Math.min(1, (double) displayHp / maxHp));
            Color fill;
            if (pct > 0.6) fill = new Color(40, 200, 60);
            else if (pct > 0.3) fill = new Color(240, 200, 30);
            else fill = new Color(240, 50, 50);

            int fillW = (int)(barW * pct);
            if (fillW > 0) {
                GradientPaint gp = new GradientPaint(barX, barY, fill.brighter(), barX, barY + barH, fill.darker());
                g2.setPaint(gp);
                g2.fillRoundRect(barX, barY, fillW, barH, 7, 7);

                g2.setColor(new Color(255, 255, 255, 80));
                g2.fillRoundRect(barX + 2, barY + 2, fillW - 4, barH / 2 - 2, 5, 5);

                if (pct <= 0.3) {
                    double pulse = 0.5 + 0.5 * Math.sin(System.currentTimeMillis() / 200.0);
                    g2.setColor(new Color(255, 0, 0, (int)(40 * pulse)));
                    g2.fillRoundRect(barX, barY, fillW, barH, 7, 7);
                }
            }

            if (flashAlpha > 0) {
                Color flashColor = flashRed ? new Color(255, 0, 0, (int)(flashAlpha * 255)) : new Color(0, 255, 0, (int)(flashAlpha * 255));
                g2.setColor(flashColor);
                g2.fillRoundRect(barX, barY, barW, barH, 7, 7);
            }

            g2.setColor(new Color(100, 70, 50));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(barX, barY, barW, barH, 7, 7);

            g2.dispose();
        }
    }

    static class OutlinedLabel extends JLabel {
        private Color outlineColor;
        private float outlineWidth;

        OutlinedLabel(String text, int alignment, Color fillColor, Color outlineColor, float outlineWidth) {
            super(text, alignment);
            this.outlineColor = outlineColor;
            this.outlineWidth = outlineWidth;
            setForeground(fillColor);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            String text = getText();
            int textWidth = fm.stringWidth(text);
            int x;
            if (getHorizontalAlignment() == SwingConstants.CENTER) {
                x = (getWidth() - textWidth) / 2;
            } else {
                x = 0;
            }
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();

            g2.setColor(outlineColor);
            g2.setStroke(new BasicStroke(outlineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            float sx = x, sy = y;
            GlyphVector gv = getFont().createGlyphVector(g2.getFontRenderContext(), text);
            Shape shape = gv.getOutline(sx, sy);
            g2.draw(shape);
            g2.setColor(getForeground());
            g2.fill(shape);

            g2.dispose();
        }
    }
}
