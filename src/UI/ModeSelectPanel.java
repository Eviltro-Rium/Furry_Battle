import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class ModeSelectPanel extends JPanel {

    private Game game;
    private final Random rng = new Random();

    public ModeSelectPanel(Game game) {
        this.game = game;
        setLayout(new BorderLayout());

        JPanel bg = new JPanel(new BorderLayout(0, 30)) {
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
        bg.setBorder(BorderFactory.createEmptyBorder(60, 80, 60, 80));

        JLabel title = new JLabel("Furry Battle", SwingConstants.CENTER);
        title.setFont(new Font("微软雅黑", Font.BOLD, 42));
        title.setForeground(new Color(255, 220, 180));
        bg.add(title, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridLayout(1, 2, 40, 0));
        center.setOpaque(false);

        center.add(buildModeCard("1v1 单挑模式", "经典1对1对战\n选择你的角色和对手\n5张手牌", () -> game.onModeSelected(false)));
        center.add(buildModeCard("1v2 双雄模式", "1对2挑战\n选择你的角色和两个对手\n9张手牌，需选择攻击目标", () -> game.onModeSelected(true)));

        bg.add(center, BorderLayout.CENTER);

        JLabel hint = new JLabel("选择游戏模式开始", SwingConstants.CENTER);
        hint.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        hint.setForeground(new Color(180, 160, 130));
        bg.add(hint, BorderLayout.SOUTH);

        add(bg, BorderLayout.CENTER);
    }

    private JPanel buildModeCard(String name, String desc, Runnable onClick) {
        JPanel card = new JPanel(new BorderLayout(10, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                g2.setColor(new Color(0, 0, 0, 30));
                g2.fillRoundRect(4, 5, w - 2, h - 2, 20, 20);
                GradientPaint gp = new GradientPaint(0, 0, new Color(70, 45, 35), 0, h, new Color(50, 30, 22));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, w - 1, h - 1, 20, 20);
                g2.setColor(new Color(255, 255, 255, 25));
                g2.fillRoundRect(3, 3, w - 7, h / 3, 18, 18);
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(140, 100, 60), 2, true),
            BorderFactory.createEmptyBorder(30, 25, 30, 25)));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) { onClick.run(); }
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(255, 180, 80), 3, true),
                    BorderFactory.createEmptyBorder(30, 25, 30, 25)));
                card.repaint();
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(140, 100, 60), 2, true),
                    BorderFactory.createEmptyBorder(30, 25, 30, 25)));
                card.repaint();
            }
        });

        JLabel nameLabel = new JLabel(name, SwingConstants.CENTER);
        nameLabel.setFont(new Font("微软雅黑", Font.BOLD, 26));
        nameLabel.setForeground(new Color(255, 210, 160));
        card.add(nameLabel, BorderLayout.NORTH);

        JLabel descLabel = new JLabel("<html><div style='text-align:center;'>" + desc.replace("\n", "<br>") + "</div></html>");
        descLabel.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        descLabel.setForeground(new Color(200, 180, 150));
        card.add(descLabel, BorderLayout.CENTER);

        return card;
    }
}