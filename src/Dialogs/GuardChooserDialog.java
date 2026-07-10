import javax.swing.*;
import java.awt.*;

public class GuardChooserDialog extends JDialog {

    private int result = 0;

    public GuardChooserDialog(JFrame owner, int guardStacks, int incomingDamage) {
        super(owner, true);
        setTitle("守护 - 选择使用守护层数");
        setUndecorated(true);
        int maxGuard = Math.min(guardStacks, incomingDamage);
        setSize(360, 80 + maxGuard * 48 + 60);
        setLocationRelativeTo(owner);
        setModal(true);

        JPanel panel = new JPanel(new BorderLayout(10, 10)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                g2.setColor(new Color(0, 0, 0, 40));
                g2.fillRoundRect(4, 4, w - 4, h - 4, 20, 20);
                g2.setColor(new Color(30, 30, 45));
                g2.fillRoundRect(0, 0, w - 1, h - 1, 20, 20);
                g2.setColor(new Color(100, 200, 255, 180));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(0, 0, w - 1, h - 1, 20, 20);
                g2.setColor(new Color(255, 255, 255, 15));
                g2.fillRoundRect(3, 3, w - 7, h / 3, 18, 18);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JLabel title = new JLabel("守护 x" + guardStacks + "  即将受到" + incomingDamage + "点伤害", SwingConstants.CENTER);
        title.setFont(new Font("微软雅黑", Font.BOLD, 15));
        title.setForeground(Color.WHITE);
        panel.add(title, BorderLayout.NORTH);

        JPanel btnPanel = new JPanel(new GridLayout(maxGuard + 1, 1, 5, 5));
        btnPanel.setOpaque(false);
        btnPanel.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));

        for (int i = 0; i <= maxGuard; i++) {
            final int guardUse = i;
            String label = i == 0 ? "不使用守护" : "使用" + i + "层守护（剩余伤害" + (incomingDamage - i) + "）";
            JButton btn = new JButton(label) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int w = getWidth(), h = getHeight();
                    g2.setColor(new Color(50, 50, 70));
                    g2.fillRoundRect(0, 0, w - 1, h - 1, 10, 10);
                    g2.setColor(new Color(255, 255, 255, 20));
                    g2.fillRoundRect(2, 2, w - 5, h / 3, 8, 8);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            btn.setFont(new Font("微软雅黑", Font.PLAIN, 13));
            btn.setForeground(i == 0 ? Color.GRAY : new Color(100, 200, 255));
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);
            btn.setContentAreaFilled(false);
            btn.setOpaque(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.addActionListener(e -> {
                result = guardUse;
                dispose();
            });
            btnPanel.add(btn);
        }

        panel.add(btnPanel, BorderLayout.CENTER);

        setContentPane(panel);
    }

    public int getResult() {
        return result;
    }
}