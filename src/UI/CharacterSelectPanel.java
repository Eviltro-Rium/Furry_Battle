import javax.swing.*;
import java.awt.*;

public class CharacterSelectPanel extends JPanel {

    private static final String[][] CHARACTERS = {
        {"Ryan", "70", "回合开始恢复1"},
        {"Leon", "100", "免疫灼烧"},
        {"Chan", "80", "回合开始抽1"},
        {"Saiki", "80", "黄牌+1流血"},
        {"Blaze", "85", "灼烧时攻击+1"},
        {"Serenity", "80", "免疫冷冻,嗜血,恢复+1"},
        {"Moze", "100", "守护免伤"}
    };

    private int playerChoice = -1;
    private int aiChoice = -1;
    private int ai2Choice = -1;
    private boolean is1v2;
    private int assignMode = 0;
    private JButton startBtn;
    private JLabel statusLabel;
    private JPanel[] charCards;
    private JLabel[] roleLabels;
    private Game game;

    public CharacterSelectPanel(Game game, boolean is1v2) {
        this.game = game;
        this.is1v2 = is1v2;
        setLayout(new BorderLayout());

        JPanel bg = new JPanel(new BorderLayout(0, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int w = getWidth(), h = getHeight();
                GradientPaint gp = new GradientPaint(0, 0, new Color(55, 28, 18), w * 0.3f, h, new Color(38, 18, 12));
                g2.setPaint(gp);
                g2.fillRect(0, 0, w, h);
                g2.setPaint(new RadialGradientPaint(w / 2f, h * 0.25f, w * 0.55f,
                    new float[]{0f, 1f}, new Color[]{new Color(110, 55, 25, 50), new Color(0, 0, 0, 0)}));
                g2.fillRect(0, 0, w, h);
            }
        };
        bg.setLayout(new BorderLayout(16, 12));
        bg.setBorder(BorderFactory.createEmptyBorder(18, 30, 18, 30));

        JLabel title = new JLabel("Furry Battle", SwingConstants.CENTER);
        title.setFont(new Font("微软雅黑", Font.BOLD, 28));
        title.setForeground(new Color(255, 210, 170));
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        bg.add(title, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.setOpaque(false);

        JPanel modeBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 4));
        modeBar.setOpaque(false);

        JButton playerBtn = buildModeButton("Player", new Color(60, 140, 220));
        JButton botBtn = buildModeButton("Bot", new Color(200, 80, 60));
        if (is1v2) {
            JButton bot2Btn = buildModeButton("Bot2", new Color(180, 60, 160));
            bot2Btn.addActionListener(e -> { assignMode = 3; updateModeButtons(playerBtn, botBtn, bot2Btn); updateStatus(); });
            modeBar.add(bot2Btn);
        }

        playerBtn.addActionListener(e -> { assignMode = 1; updateModeButtons(playerBtn, botBtn, is1v2 ? (JButton)modeBar.getComponent(2) : null); updateStatus(); });
        botBtn.addActionListener(e -> { assignMode = 2; updateModeButtons(playerBtn, botBtn, is1v2 ? (JButton)modeBar.getComponent(2) : null); updateStatus(); });

        modeBar.add(playerBtn);
        modeBar.add(botBtn);

        center.add(modeBar, BorderLayout.NORTH);

        int rows = (CHARACTERS.length + 3) / 4;
        JPanel grid = new JPanel(new GridLayout(rows, 4, 8, 8));
        grid.setOpaque(false);

        charCards = new JPanel[CHARACTERS.length];
        roleLabels = new JLabel[CHARACTERS.length];

        for (int i = 0; i < CHARACTERS.length; i++) {
            final int idx = i;
            JPanel card = buildCard(CHARACTERS[i][0], CHARACTERS[i][1], CHARACTERS[i][2]);
            card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            card.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) { onCardClick(idx); }
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) { updateCardStyles(); }
                @Override
                public void mouseExited(java.awt.event.MouseEvent e) { updateCardStyles(); }
            });
            charCards[i] = card;
            grid.add(card);
        }

        center.add(grid, BorderLayout.CENTER);
        bg.add(center, BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout(4, 6));
        south.setOpaque(false);
        south.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        statusLabel = new JLabel("请先点击 Player 或 Bot，再点击角色", SwingConstants.CENTER);
        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        statusLabel.setForeground(new Color(190, 170, 140));
        south.add(statusLabel, BorderLayout.NORTH);

        startBtn = new JButton(" 确认开始") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                Color bg1 = getBackground();
                GradientPaint gp = new GradientPaint(0, 0, bg1, 0, h, bg1.darker());
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, w - 1, h - 1, 12, 12);
                g2.setColor(new Color(255, 255, 255, 35));
                g2.fillRoundRect(2, 2, w - 5, h / 3, 10, 10);
                super.paintComponent(g);
            }
        };
        startBtn.setFont(new Font("微软雅黑", Font.BOLD, 16));
        startBtn.setBackground(new Color(180, 50, 40));
        startBtn.setForeground(new Color(255, 240, 230));
        startBtn.setFocusPainted(false);
        startBtn.setBorderPainted(false);
        startBtn.setContentAreaFilled(false);
        startBtn.setOpaque(false);
        startBtn.setEnabled(false);
        startBtn.setPreferredSize(new Dimension(160, 38));
        startBtn.setIcon(GameIcons.uiBattle());
        startBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        startBtn.addActionListener(e -> onStart());
        JPanel btnWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        btnWrap.setOpaque(false);
        btnWrap.add(startBtn);
        south.add(btnWrap, BorderLayout.CENTER);

        bg.add(south, BorderLayout.SOUTH);
        add(bg, BorderLayout.CENTER);
    }

    private JButton buildModeButton(String text, Color accent) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                g2.setColor(getModel().isRollover() || getBackground().equals(accent) ? accent : new Color(60, 45, 38));
                g2.fillRoundRect(0, 0, w - 1, h - 1, 10, 10);
                g2.setColor(new Color(255, 255, 255, 25));
                g2.fillRoundRect(2, 2, w - 5, h / 3, 8, 8);
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("微软雅黑", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setPreferredSize(new Dimension(90, 32));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBackground(new Color(60, 45, 38));
        return btn;
    }

    private void updateModeButtons(JButton playerBtn, JButton botBtn, JButton bot2Btn) {
        Color pColor = new Color(60, 140, 220);
        Color bColor = new Color(200, 80, 60);
        Color b2Color = new Color(180, 60, 160);
        Color off = new Color(60, 45, 38);

        playerBtn.setBackground(assignMode == 1 ? pColor : off);
        botBtn.setBackground(assignMode == 2 ? bColor : off);
        if (bot2Btn != null) bot2Btn.setBackground(assignMode == 3 ? b2Color : off);
        playerBtn.repaint();
        botBtn.repaint();
        if (bot2Btn != null) bot2Btn.repaint();
    }

    private void onCardClick(int idx) {
        if (assignMode == 0) {
            statusLabel.setText("请先点击 Player 或 Bot，再点击角色");
            return;
        }
        if (assignMode == 1) {
            playerChoice = idx;
        } else if (assignMode == 2) {
            aiChoice = idx;
        } else if (assignMode == 3) {
            ai2Choice = idx;
        }
        updateCardStyles();
        checkReady();
    }

    private JPanel buildCard(String name, String hp, String skills) {
        JPanel card = new JPanel(new BorderLayout(4, 2)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                Color bg = getBackground();
                g2.setColor(new Color(0, 0, 0, 20));
                g2.fillRoundRect(2, 3, w - 2, h - 2, 12, 12);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, w - 1, h - 1, 12, 12);
                g2.setColor(new Color(255, 255, 255, 18));
                g2.fillRoundRect(2, 2, w - 5, h / 3, 10, 10);
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBackground(new Color(50, 35, 28));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(110, 85, 55), 1, true),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)));

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        nameLabel.setForeground(new Color(255, 215, 165));
        card.add(nameLabel, BorderLayout.NORTH);

        JLabel hpLabel = new JLabel("HP " + hp);
        hpLabel.setFont(new Font("Arial", Font.BOLD, 11));
        hpLabel.setForeground(new Color(235, 95, 75));
        JPanel infoRow = new JPanel(new BorderLayout(2, 0));
        infoRow.setOpaque(false);
        infoRow.add(hpLabel, BorderLayout.WEST);

        JLabel skillLabel = new JLabel(skills);
        skillLabel.setFont(new Font("微软雅黑", Font.PLAIN, 10));
        skillLabel.setForeground(new Color(190, 170, 145));
        infoRow.add(skillLabel, BorderLayout.CENTER);

        card.add(infoRow, BorderLayout.CENTER);

        JLabel roleLabel = new JLabel("", SwingConstants.CENTER);
        roleLabel.setFont(new Font("微软雅黑", Font.BOLD, 11));
        roleLabel.setPreferredSize(new Dimension(120, 16));
        JPanel roleWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 0));
        roleWrap.setOpaque(false);
        roleWrap.add(roleLabel);
        card.add(roleWrap, BorderLayout.SOUTH);

        int idx = -1;
        for (int i = 0; i < CHARACTERS.length; i++) {
            if (CHARACTERS[i][0].equals(name)) { idx = i; break; }
        }
        if (idx >= 0) roleLabels[idx] = roleLabel;

        return card;
    }

    private void updateCardStyles() {
        Color normBorder = new Color(110, 85, 55);
        Color normBg = new Color(50, 35, 28);
        Color playerBorder = new Color(60, 140, 220);
        Color playerBg = new Color(35, 55, 80);
        Color botBorder = new Color(200, 80, 60);
        Color botBg = new Color(80, 40, 35);
        Color bot2Border = new Color(180, 60, 160);
        Color bot2Bg = new Color(70, 35, 65);

        for (int i = 0; i < CHARACTERS.length; i++) {
            if (charCards[i] == null) continue;
            boolean isPlayer = playerChoice == i;
            boolean isBot = aiChoice == i;
            boolean isBot2 = ai2Choice == i;
            boolean anySelected = isPlayer || isBot || isBot2;

            Color border = normBorder;
            Color bg = normBg;

            if (isPlayer) { border = playerBorder; bg = playerBg; }
            else if (isBot2) { border = bot2Border; bg = bot2Bg; }
            else if (isBot) { border = botBorder; bg = botBg; }

            charCards[i].setBackground(bg);
            charCards[i].setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(border, anySelected ? 2 : 1, true),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
            charCards[i].repaint();

            if (roleLabels[i] != null) {
                StringBuilder sb = new StringBuilder();
                if (isPlayer) sb.append("Player");
                if (isBot) {
                    if (sb.length() > 0) sb.append(" ");
                    sb.append("Bot");
                }
                if (isBot2) {
                    if (sb.length() > 0) sb.append(" ");
                    sb.append("Bot2");
                }
                roleLabels[i].setText(sb.toString());
                if (isPlayer && !isBot && !isBot2) roleLabels[i].setForeground(new Color(100, 180, 255));
                else if (isBot && !isPlayer && !isBot2) roleLabels[i].setForeground(new Color(255, 120, 100));
                else if (isBot2 && !isPlayer && !isBot) roleLabels[i].setForeground(new Color(230, 100, 210));
                else roleLabels[i].setForeground(new Color(220, 200, 160));
            }
        }
    }

    private void updateStatus() {
        String mode = assignMode == 1 ? "Player" : assignMode == 2 ? "Bot" : assignMode == 3 ? "Bot2" : "";
        statusLabel.setText("当前选择模式: " + mode + "  —  点击角色分配");
        statusLabel.setForeground(new Color(220, 200, 160));
    }

    private void checkReady() {
        boolean ready = playerChoice >= 0 && aiChoice >= 0 && (!is1v2 || ai2Choice >= 0);
        startBtn.setEnabled(ready);
        startBtn.setBackground(ready ? new Color(200, 60, 40) : new Color(100, 60, 50));
        if (ready) {
            statusLabel.setText(is1v2
                ? CHARACTERS[playerChoice][0] + " (Player)  vs  " + CHARACTERS[aiChoice][0] + " (Bot) & " + CHARACTERS[ai2Choice][0] + " (Bot2)"
                : CHARACTERS[playerChoice][0] + " (Player)  vs  " + CHARACTERS[aiChoice][0] + " (Bot)");
            statusLabel.setForeground(new Color(255, 200, 120));
        }
    }

    private void onStart() {
        if (playerChoice < 0 || aiChoice < 0) return;
        if (is1v2 && ai2Choice < 0) return;
        if (game != null) {
            game.onCharacterSelected(playerChoice, aiChoice, is1v2 ? ai2Choice : -1);
        }
    }
}
