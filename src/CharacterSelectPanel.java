import javax.swing.*;
import java.awt.*;

public class CharacterSelectPanel extends JPanel {

    private static final String[][] CHARACTERS = {
        {"🐼 Ryan", "70", "被动：回合开始恢复1❤️"},
        {"🐻‍❄️ Leon", "100", "被动：免疫灼烧"},
        {"� Chan", "80", "被动：回合开始抽1🃏"},
        {"🐺 Saiki", "90", "被动：打出黄牌+1流血"},
        {"🐶 Blaze", "75", "被动：灼烧时攻击+1🗡️"}
    };

    private int playerChoice = -1;
    private int aiChoice = -1;
    private JButton startBtn;
    private JLabel statusLabel;
    private JPanel[] playerCards;
    private JPanel[] aiCards;
    private Game game;


    public CharacterSelectPanel(Game game) {
        this.game = game;
        setLayout(new BorderLayout(0, 0));

        JPanel bg = new JPanel(new BorderLayout(0, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int w = getWidth(), h = getHeight();
                GradientPaint gp = new GradientPaint(0, 0, new Color(60, 30, 20), w * 0.3f, h, new Color(40, 20, 15));
                g2.setPaint(gp);
                g2.fillRect(0, 0, w, h);
                g2.setPaint(new RadialGradientPaint(w / 2f, h * 0.3f, w * 0.6f,
                    new float[]{0f, 1f}, new Color[]{new Color(120, 60, 30, 60), new Color(0, 0, 0, 0)}));
                g2.fillRect(0, 0, w, h);
            }
        };
        bg.setLayout(new BorderLayout(30, 20));
        bg.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        JLabel title = new JLabel("⚔  Furry Battle  ⚔", SwingConstants.CENTER);
        title.setFont(new Font("微软雅黑", Font.BOLD, 36));
        title.setForeground(new Color(255, 220, 180));
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        bg.add(title, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridLayout(1, 2, 30, 0));
        center.setOpaque(false);
        center.add(buildColumn("选择你的角色", true));
        center.add(buildColumn("选择AI的角色", false));
        bg.add(center, BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout(0, 8));
        south.setOpaque(false);
        south.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        statusLabel = new JLabel("请选择双方角色", SwingConstants.CENTER);
        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        statusLabel.setForeground(new Color(200, 180, 150));
        south.add(statusLabel, BorderLayout.NORTH);

        startBtn = new JButton("⚔  开始战斗") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                super.paintComponent(g);
            }
        };
        startBtn.setFont(new Font("微软雅黑", Font.BOLD, 20));
        startBtn.setBackground(new Color(180, 50, 40));
        startBtn.setForeground(new Color(255, 240, 230));
        startBtn.setFocusPainted(false);
        startBtn.setBorderPainted(false);
        startBtn.setContentAreaFilled(false);
        startBtn.setOpaque(false);
        startBtn.setEnabled(false);
        startBtn.setPreferredSize(new Dimension(220, 50));
        startBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        startBtn.addActionListener(e -> onStart());
        JPanel btnWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        btnWrap.setOpaque(false);
        btnWrap.add(startBtn);
        south.add(btnWrap, BorderLayout.CENTER);

        bg.add(south, BorderLayout.SOUTH);
        add(bg, BorderLayout.CENTER);
    }

    private JPanel buildColumn(String titleText, boolean isPlayer) {
        JPanel col = new JPanel(new BorderLayout(8, 8));
        col.setOpaque(false);

        JLabel titleLabel = new JLabel(titleText, SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        titleLabel.setForeground(new Color(255, 200, 150));
        col.add(titleLabel, BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(CHARACTERS.length, 1, 10, 10));
        cards.setOpaque(false);

        JPanel[] cardArr = isPlayer ? (playerCards = new JPanel[CHARACTERS.length]) : (aiCards = new JPanel[CHARACTERS.length]);

        for (int i = 0; i < CHARACTERS.length; i++) {
            final int idx = i;
            JPanel card = buildCard(CHARACTERS[i][0], CHARACTERS[i][1], CHARACTERS[i][2]);
            card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            card.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (isPlayer) selectPlayer(idx);
                    else selectAI(idx);
                }
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    if (!((isPlayer && playerChoice == idx) || (!isPlayer && aiChoice == idx)))
                        card.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(160, 130, 100), 2, true),
                            BorderFactory.createEmptyBorder(14, 18, 14, 18)));
                }
                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    updateCardBorders();
                }
            });
            cardArr[i] = card;
            cards.add(card);
        }

        col.add(cards, BorderLayout.CENTER);
        return col;
    }

    private JPanel buildCard(String name, String hp, String skills) {
        JPanel card = new JPanel(new BorderLayout(10, 6)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBackground(new Color(50, 35, 28));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(120, 90, 60), 2, true),
            BorderFactory.createEmptyBorder(14, 18, 14, 18)));

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("微软雅黑", Font.BOLD, 22));
        nameLabel.setForeground(new Color(255, 220, 170));
        card.add(nameLabel, BorderLayout.NORTH);

        JLabel hpLabel = new JLabel("❤️ " + hp);
        hpLabel.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        hpLabel.setForeground(new Color(240, 100, 80));
        card.add(hpLabel, BorderLayout.WEST);

        JLabel skillLabel = new JLabel(skills);
        skillLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        skillLabel.setForeground(new Color(200, 180, 150));
        card.add(skillLabel, BorderLayout.CENTER);

        return card;
    }

    private void selectPlayer(int idx) {
        playerChoice = idx;
        updateCardBorders();
        checkReady();
    }

    private void selectAI(int idx) {
        aiChoice = idx;
        updateCardBorders();
        checkReady();
    }

    private void updateCardBorders() {
        Color selectedBorder = new Color(255, 160, 60);
        Color normalBorder = new Color(120, 90, 60);
        Color selectedBg = new Color(80, 50, 35);
        Color normalBg = new Color(50, 35, 28);

        for (int i = 0; i < CHARACTERS.length; i++) {
            if (playerCards[i] != null) {
                boolean sel = playerChoice == i;
                playerCards[i].setBackground(sel ? selectedBg : normalBg);
                playerCards[i].setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(sel ? selectedBorder : normalBorder, sel ? 3 : 2, true),
                    BorderFactory.createEmptyBorder(14, 18, 14, 18)));
                playerCards[i].repaint();
            }
            if (aiCards[i] != null) {
                boolean sel = aiChoice == i;
                aiCards[i].setBackground(sel ? selectedBg : normalBg);
                aiCards[i].setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(sel ? selectedBorder : normalBorder, sel ? 3 : 2, true),
                    BorderFactory.createEmptyBorder(14, 18, 14, 18)));
                aiCards[i].repaint();
            }
        }
    }

    private void checkReady() {
        boolean ready = playerChoice >= 0 && aiChoice >= 0;
        startBtn.setEnabled(ready);
        startBtn.setBackground(ready ? new Color(200, 60, 40) : new Color(100, 60, 50));
        if (ready) {
            statusLabel.setText(CHARACTERS[playerChoice][0] + "  vs  " + CHARACTERS[aiChoice][0]);
            statusLabel.setForeground(new Color(255, 200, 120));
        } else {
            statusLabel.setText("请选择双方角色");
            statusLabel.setForeground(new Color(200, 180, 150));
        }
    }

    private void onStart() {
        if (playerChoice < 0 || aiChoice < 0) return;
        if (game != null) {
            game.onCharacterSelected(playerChoice, aiChoice);

        }
    }
}
