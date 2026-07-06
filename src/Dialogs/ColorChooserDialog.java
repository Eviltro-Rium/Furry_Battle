import javax.swing.*;
import java.awt.*;

public class ColorChooserDialog extends JDialog {

    private Card.CardColor result = null;

    public ColorChooserDialog(JFrame owner) {
        super(owner, true);
        setTitle("选择颜色");
        setUndecorated(true);
        setSize(360, 140);
        setLocationRelativeTo(owner);
        setModal(true);

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(new Color(30, 30, 45));
        panel.setBorder(BorderFactory.createLineBorder(new Color(100, 180, 255), 2));

        JLabel title = new JLabel("选择黑牌指定颜色", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 15));
        title.setForeground(Color.WHITE);
        panel.add(title, BorderLayout.NORTH);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
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
                    GradientPaint gp = new GradientPaint(0, 0, btnColors[idx], 0, getHeight(), btnColors[idx].darker().darker());
                    g2.setPaint(gp);
                    g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                    g2.setColor(new Color(255, 255, 255, 80));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            b.setFont(new Font("SansSerif", Font.BOLD, 14));
            b.setForeground(Color.WHITE);
            b.setFocusPainted(false);
            b.setBorderPainted(false);
            b.setContentAreaFilled(false);
            b.setPreferredSize(new Dimension(70, 40));
            b.setCursor(new Cursor(Cursor.HAND_CURSOR));
            b.addActionListener(e -> {
                result = colors[idx];
                dispose();
            });
            btnPanel.add(b);
        }

        panel.add(btnPanel, BorderLayout.CENTER);
        setContentPane(panel);
    }

    public Card.CardColor getResult() {
        return result;
    }
}