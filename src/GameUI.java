import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.GlyphVector;


public class GameUI {
    // High-saturation bright theme
    static final Color BG_COLOR = new Color(255, 245, 225);
    static final Color PANEL_BG = new Color(255, 250, 240);

    // Vivid card colors
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
    JButton resetBtn;
    JLabel errorHintLabel;
    HpBar playerHpBar;
    HpBar aiHpBar;
    JLabel playerBurnLabel;
    JLabel aiBurnLabel;
    private JPanel rootPanel;

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
    private JPanel buildTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout(6, 2));
        topPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("✦ Furry Battle ✦", SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                double pulse = 0.85 + 0.15 * Math.sin(System.currentTimeMillis() / 800.0);
                int r = (int)(240 * pulse), gr = (int)(80 * pulse), b = (int)(60 * pulse);
                g2.setColor(new Color(r, gr, b));
                g2.setFont(getFont());
                String text = getText();
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(text)) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.setColor(new Color(255, 200, 150, 60));
                g2.drawString(text, x + 1, y + 1);
                g2.setColor(new Color(r, gr, b));
                g2.drawString(text, x, y);
                g2.dispose();
            }
        };
        titleLabel.setFont(new Font("Arial", Font.BOLD, 34));
        titleLabel.setForeground(new Color(240, 80, 60));
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
    private JPanel buildCenterPanel() {
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
        aiHandPanel.setPreferredSize(new Dimension(0, 130));

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
        aiAttackPanel.setBorder(makeGlowBorder("⚔ 出牌区", new Color(220, 80, 60)));
        aiAttackPanel.setPreferredSize(new Dimension(0, 180));

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
        aiRevealPanel.setBorder(makeGlowBorder("📋 判定", new Color(60, 100, 200)));
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
    private JPanel buildBottomPanel(Game game) {
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

        playBtn         = makeBtn("⚔ 出牌",    new Color(240, 40, 50),  new Color(255, 80, 90));
        enterDiscardBtn = makeBtn("🗑 弃牌",    new Color(240, 140, 0),  new Color(255, 170, 30));
        confirmDiscardBtn=makeBtn("✔ 确认弃牌", new Color(240, 140, 0),  new Color(255, 170, 30));
        cancelDiscardBtn= makeBtn("✘ 取消",    new Color(150, 140, 130),new Color(180, 170, 160));
        endTurnBtn      = makeBtn("⏭ 结束回合",new Color(100, 60, 140), new Color(140, 90, 190));
        defendBtn       = makeBtn("🛡 防御",    new Color(0, 110, 230),  new Color(40, 150, 255));
        skipDefendBtn   = makeBtn("⏩ 跳过",  new Color(150, 140, 130),new Color(180, 170, 160));
        fiveHealBtn     = makeBtn("❤ 恢复", new Color(0, 180, 80),   new Color(40, 210, 100));
        fiveDamageBtn   = makeBtn("🗡 1.5倍",new Color(220, 40, 40), new Color(255, 70, 70));
        sevenChoiceBtn  = makeBtn("🎯 指定弃牌", new Color(255, 120, 0), new Color(255, 160, 40));

        playBtn.addActionListener(e -> game.doPlay());
        enterDiscardBtn.addActionListener(e -> game.doEnterDiscard());
        confirmDiscardBtn.addActionListener(e -> game.doConfirmDiscard());
        cancelDiscardBtn.addActionListener(e -> game.doCancelDiscard());
        endTurnBtn.addActionListener(e -> game.doEndTurn());
        defendBtn.addActionListener(e -> game.doPlayerDefend());
        skipDefendBtn.addActionListener(e -> game.doPlayerSkipDefend());
        fiveHealBtn.addActionListener(e -> game.doFiveChoiceHeal());
        fiveDamageBtn.addActionListener(e -> game.doFiveChoiceDamage());
        sevenChoiceBtn.addActionListener(e -> {});

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

        // Status area (right side)
        resetBtn = makeBtn("↻ 重新开始", new Color(100, 60, 140), new Color(140, 90, 190));
        resetBtn.addActionListener(e -> game.startGame());

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

    // ── Gradient button ──
    private JButton makeBtn(String text, Color base, Color hover) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                GradientPaint gp = new GradientPaint(0, 0, base, 0, h, base.darker().darker());
                if (getModel().isRollover()) {
                    gp = new GradientPaint(0, 0, hover, 0, h, hover.darker().darker());
                }
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, w - 1, h - 1, 10, 10);
                g2.setColor(new Color(255, 255, 255, 60));
                g2.drawRoundRect(0, 0, w - 1, h - 1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("微软雅黑", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setPreferredSize(new Dimension(115, 38));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
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
                title + " ⚡进攻方",
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
                GradientPaint gp = new GradientPaint(0, 0, new Color(70, 130, 240), w, h, new Color(30, 80, 200));
                g2.setPaint(gp);
                g2.fillRoundRect(1, 1, w - 3, h - 3, 14, 14);
                g2.setColor(new Color(255, 255, 255, 120));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, w - 3, h - 3, 14, 14);
                g2.setColor(new Color(255, 255, 255, 100));
                g2.drawRoundRect(8, 8, w - 18, h - 18, 10, 10);
                g2.dispose();
            }
        };
        cardPanel.setPreferredSize(new Dimension(56, 82));
        cardPanel.setOpaque(false);

        JLabel qLabel = new JLabel("✦", SwingConstants.CENTER);
        qLabel.setFont(new Font("Arial", Font.BOLD, 26));
        qLabel.setForeground(new Color(220, 235, 255));
        cardPanel.add(qLabel, BorderLayout.CENTER);
        return cardPanel;
    }

    // ── Player card view ──
    static JPanel createCardView(Card card, boolean clickable, int handIndex, boolean selected, Game.Phase phase, Game game) {
        Color top = getSwingColor(card.getColor());
        Color bot = getDarkColor(card.getColor());

        JPanel cardPanel = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();

                boolean hover = Boolean.TRUE.equals(getClientProperty("hover"));
                int offsetY = (hover && clickable && !selected) ? -4 : 0;

                Color useTop = selected ? top.brighter().brighter() : (hover ? top.brighter() : top);
                Color useBot = selected ? bot.brighter().brighter() : (hover ? bot.brighter() : bot);
                GradientPaint gp = new GradientPaint(0, offsetY, useTop, w, h + offsetY, useBot);
                g2.setPaint(gp);
                g2.fillRoundRect(2, 2 + offsetY, w - 5, h - 5, 16, 16);

                if (selected) {
                    g2.setColor(new Color(255, 220, 60, 200));
                    g2.setStroke(new BasicStroke(3f));
                    g2.drawRoundRect(2, 2 + offsetY, w - 5, h - 5, 16, 16);
                    g2.setColor(new Color(255, 220, 60, 60));
                    g2.setStroke(new BasicStroke(8f));
                    g2.drawRoundRect(2, 2 + offsetY, w - 5, h - 5, 16, 16);
                } else if (hover && clickable) {
                    g2.setColor(new Color(255, 255, 255, 100));
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawRoundRect(2, 2 + offsetY, w - 5, h - 5, 16, 16);
                    g2.setColor(new Color(255, 255, 255, 30));
                    g2.setStroke(new BasicStroke(6f));
                    g2.drawRoundRect(2, 2 + offsetY, w - 5, h - 5, 16, 16);
                } else {
                    g2.setColor(new Color(255, 255, 255, 40));
                    g2.setStroke(new BasicStroke(1f));
                    g2.drawRoundRect(2, 2, w - 5, h - 5, 16, 16);
                }

                GradientPaint shine = new GradientPaint(0, 0, new Color(255, 255, 255, 70), 0, h / 2, new Color(255, 255, 255, 0));
                g2.setPaint(shine);
                g2.fillRoundRect(4, 4 + offsetY, w - 10, h / 2, 14, 14);

                g2.dispose();
            }
        };
        cardPanel.setPreferredSize(new Dimension(80, 120));
        cardPanel.setOpaque(false);
        cardPanel.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

        JLabel numLabel;
        JLabel cornerLabel;
        if (card.isBlack()) {
            String display = card.getChosenColor() != null ? "✦" + colorSymbol(card.getChosenColor()) : "✦";
            numLabel = new JLabel(display, SwingConstants.CENTER);
            numLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 30));
            numLabel.setForeground(new Color(230, 230, 240));
            String corner = card.isDrawTwo() ? "⚡+2" : "✦";
            cornerLabel = new JLabel(corner, SwingConstants.LEFT);
            if (card.isDrawTwo()) {
                cornerLabel.setFont(new Font("Arial", Font.BOLD, 12));
                cornerLabel.setForeground(new Color(255, 210, 50));
            } else {
                cornerLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 10));
                cornerLabel.setForeground(new Color(200, 200, 210));
            }
        } else if (card.isPotion()) {
            String display = card.getChosenColor() != null ? "🧪" + colorSymbol(card.getChosenColor()) : "🧪";
            numLabel = new JLabel(display, SwingConstants.CENTER);
            numLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 30));
            numLabel.setForeground(new Color(80, 70, 90));
            cornerLabel = new JLabel("🧪", SwingConstants.LEFT);
            cornerLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 10));
            cornerLabel.setForeground(new Color(100, 90, 110));
        } else if (card.isDrawThree()) {
            String display = card.getChosenColor() != null ? "🃏" + colorSymbol(card.getChosenColor()) : "🃏";
            numLabel = new JLabel(display, SwingConstants.CENTER);
            numLabel.setFont(new Font("微软雅黑", Font.PLAIN, 30));
            numLabel.setForeground(new Color(60, 60, 80));
            cornerLabel = new JLabel("+3🃏", SwingConstants.LEFT);
            cornerLabel.setFont(new Font("微软雅黑", Font.BOLD, 9));
            cornerLabel.setForeground(new Color(100, 90, 110));
        } else if (card.isWhite()) {
            String display = card.getChosenColor() != null ? String.valueOf(card.getValue()) + colorSymbol(card.getChosenColor()) : String.valueOf(card.getValue());
            numLabel = new JLabel(display, SwingConstants.CENTER);
            numLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 30));
            numLabel.setForeground(new Color(80, 70, 90));
            cornerLabel = new OutlinedLabel(String.valueOf(card.getValue()), SwingConstants.LEFT, new Color(100, 90, 110), Color.WHITE, 1f);
            cornerLabel.setFont(new Font("Arial", Font.BOLD, 12));
            cornerLabel.setForeground(new Color(100, 90, 110));
        } else {
            numLabel = new OutlinedLabel(String.valueOf(card.getValue()), SwingConstants.CENTER, Color.WHITE, Color.BLACK, 2f);
            numLabel.setFont(new Font("Arial", Font.BOLD, 42));
            numLabel.setForeground(Color.WHITE);
            cornerLabel = new OutlinedLabel(String.valueOf(card.getValue()), SwingConstants.LEFT, new Color(255, 255, 255, 200), Color.BLACK, 1f);
            cornerLabel.setFont(new Font("Arial", Font.BOLD, 12));
            cornerLabel.setForeground(new Color(255, 255, 255, 200));
        }
        cardPanel.add(numLabel, BorderLayout.CENTER);


        JPanel cornerWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        cornerWrap.setOpaque(false);
        cornerWrap.add(cornerLabel);
        cardPanel.add(cornerWrap, BorderLayout.NORTH);

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
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 12));
        panel.setBackground(new Color(255, 245, 230));
        JButton[] btns = new JButton[4];
        String[] labels = {"🔴 红", "🟡 黄", "🔵 蓝", "🟢 绿"};
        Card.CardColor[] colors = {Card.CardColor.RED, Card.CardColor.YELLOW, Card.CardColor.BLUE, Card.CardColor.GREEN};
        Color[] btnColors = {CARD_RED, CARD_YELLOW, CARD_BLUE, CARD_GREEN};
        final Card.CardColor[] result = {null};

        for (int i = 0; i < 4; i++) {
            final int idx = i;
            JButton b = new JButton(labels[i]) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    GradientPaint gp = new GradientPaint(0, 0, btnColors[idx], 0, getHeight(), btnColors[idx].darker().darker());
                    g2.setPaint(gp);
                    g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                    g2.setColor(new Color(255, 255, 255, 80));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            b.setFont(new Font("微软雅黑", Font.BOLD, 14));
            b.setForeground(Color.WHITE);
            b.setFocusPainted(false);
            b.setBorderPainted(false);
            b.setContentAreaFilled(false);
            b.setPreferredSize(new Dimension(90, 44));
            b.setCursor(new Cursor(Cursor.HAND_CURSOR));
            final Card.CardColor cc = colors[i];
            b.addActionListener(e -> {
                result[0] = cc;
                Window w = SwingUtilities.getWindowAncestor(panel);
                if (w != null) w.dispose();
            });
            panel.add(b);
            btns[i] = b;
        }

        JOptionPane pane = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[]{}, null);
        JDialog dialog = pane.createDialog(parent, "✦ 选择黑牌颜色");
        dialog.setVisible(true);
        return result[0];
    }

    static String colorSymbol(Card.CardColor color) {
        switch (color) {
            case RED:    return "🔴";
            case YELLOW: return "🟡";
            case BLUE:   return "🔵";
            case GREEN:  return "🟢";
            case WHITE:  return "⚪";
            default:     return "?";
        }
    }

    // ══════════════════════════════════════════
    //  Custom components
    // ══════════════════════════════════════════

    /** Panel with vertical gradient background + floating particles */
    static class GradientPanel extends JPanel {
        private final Color top, bottom;
        GradientPanel(Color top, Color bottom) { this.top = top; this.bottom = bottom; }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setPaint(new GradientPaint(0, 0, top, 0, getHeight(), bottom));
            g2.fillRect(0, 0, getWidth(), getHeight());
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
            g2.setColor(new Color(200, 170, 130, 200));
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

    /** Simple HP bar with name and numeric display */
    static class HpBar extends JPanel {
        private String charName = "";
        private int hp = 100, maxHp = 100;
        private int displayHp = 100;
        private Timer animTimer;
        HpBar() {
            setPreferredSize(new Dimension(200, 28));
            setOpaque(false);
        }
        void update(String name, int hp, int maxHp) {
            this.charName = name; this.hp = hp; this.maxHp = maxHp;
            if (animTimer != null && animTimer.isRunning()) animTimer.stop();
            if (displayHp != hp) {
                int start = displayHp;
                int diff = hp - start;
                int steps = Math.max(8, Math.abs(diff));
                int[] step = {0};
                animTimer = new Timer(30, e -> {
                    step[0]++;
                    double t = (double) step[0] / steps;
                    displayHp = (int)(start + diff * t);
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
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            int barW = Math.max(60, w - 160);

            g2.setFont(new Font("微软雅黑", Font.BOLD, 14));
            g2.setColor(new Color(80, 40, 20));
            g2.drawString(charName, 4, h - 7);

            String hpText = displayHp + "/" + maxHp;
            g2.setFont(new Font("Arial", Font.BOLD, 13));
            g2.setColor(new Color(60, 30, 10));
            g2.drawString(hpText, w - 60, h - 7);

            int barX = Math.max(60, g2.getFontMetrics().stringWidth(charName) + 12);
            int barY = 4, barH = h - 10;

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
