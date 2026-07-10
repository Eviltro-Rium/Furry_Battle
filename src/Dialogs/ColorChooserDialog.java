import javax.swing.*;
import java.awt.*;

public class ColorChooserDialog extends JDialog {

    private Card.CardColor result = null;

    public ColorChooserDialog(JFrame owner) {
        super(owner, true);
        setTitle("选择颜色");
        setUndecorated(true);
        setSize(420, 140);
        setLocationRelativeTo(owner);
        setModal(true);

        JPanel panel = new JPanel(new BorderLayout(10, 8)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                g2.setColor(new Color(0, 0, 0, 40));
                g2.fillRoundRect(4, 4, w - 4, h - 4, 20, 20);
                g2.setColor(new Color(30, 30, 45));
                g2.fillRoundRect(0, 0, w - 1, h - 1, 20, 20);
                g2.setColor(new Color(100, 180, 255, 180));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(0, 0, w - 1, h - 1, 20, 20);
                g2.setColor(new Color(255, 255, 255, 15));
                g2.fillRoundRect(3, 3, w - 7, h / 3, 18, 18);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

        JLabel title = new JLabel("选择黑牌指定颜色", SwingConstants.CENTER);
        title.setFont(new Font("微软雅黑", Font.BOLD, 15));
        title.setForeground(Color.WHITE);
        panel.add(title, BorderLayout.NORTH);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 6));
        btnPanel.setOpaque(false);

        String[] names = {"红", "黄", "蓝", "绿"};
        Card.CardColor[] colors = {Card.CardColor.RED, Card.CardColor.YELLOW, Card.CardColor.BLUE, Card.CardColor.GREEN};
        Color[] btnColors = {new Color(220, 60, 60), new Color(230, 200, 40), new Color(60, 120, 220), new Color(60, 180, 80)};

        for (int i = 0; i < 4; i++) {
            final int idx = i;
            JButton b = new JButton(names[idx]) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int w = getWidth(), h = getHeight();
                    g2.setColor(new Color(0, 0, 0, 30));
                    g2.fillRoundRect(2, 3, w - 1, h - 1, 12, 12);
                    GradientPaint gp = new GradientPaint(0, 0, btnColors[idx].brighter(), 0, h, btnColors[idx].darker().darker());
                    g2.setPaint(gp);
                    g2.fillRoundRect(0, 0, w - 1, h - 1, 12, 12);
                    g2.setColor(new Color(255, 255, 255, 80));
                    g2.drawRoundRect(0, 0, w - 1, h - 1, 12, 12);
                    g2.setColor(new Color(255, 255, 255, 40));
                    g2.fillRoundRect(2, 2, w - 5, h / 3, 10, 10);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            b.setFont(new Font("微软雅黑", Font.BOLD, 14));
            b.setForeground(Color.WHITE);
            b.setFocusPainted(false);
            b.setBorderPainted(false);
            b.setContentAreaFilled(false);
            b.setOpaque(false);
            b.setPreferredSize(new Dimension(70, 38));
            b.setCursor(new Cursor(Cursor.HAND_CURSOR));
            b.addActionListener(e -> {
                result = colors[idx];
                dispose();
            });
            btnPanel.add(b);
        }

        JButton cancelBtn = new JButton("取消") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                g2.setColor(new Color(60, 40, 40));
                g2.fillRoundRect(0, 0, w - 1, h - 1, 12, 12);
                g2.setColor(new Color(255, 255, 255, 20));
                g2.fillRoundRect(2, 2, w - 5, h / 3, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        cancelBtn.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        cancelBtn.setForeground(Color.GRAY);
        cancelBtn.setFocusPainted(false);
        cancelBtn.setBorderPainted(false);
        cancelBtn.setContentAreaFilled(false);
        cancelBtn.setOpaque(false);
        cancelBtn.setPreferredSize(new Dimension(70, 38));
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelBtn.addActionListener(e -> dispose());
        btnPanel.add(cancelBtn);

        panel.add(btnPanel, BorderLayout.CENTER);
        setContentPane(panel);
    }

    public Card.CardColor getResult() {
        return result;
    }
}
