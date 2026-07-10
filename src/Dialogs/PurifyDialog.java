import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PurifyDialog extends JDialog {

    private String result = null;

    public PurifyDialog(JFrame owner, GameCharacter ch) {
        super(owner, true);
        setTitle("净化 - 选择移除一层buff");
        setUndecorated(true);
        setSize(340, 260);
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
                g2.setColor(new Color(100, 180, 255, 180));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(0, 0, w - 1, h - 1, 20, 20);
                g2.setColor(new Color(255, 255, 255, 15));
                g2.fillRoundRect(3, 3, w - 7, h / 3, 18, 18);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JLabel title = new JLabel("选择移除一层buff", SwingConstants.CENTER);
        title.setFont(new Font("微软雅黑", Font.BOLD, 16));
        title.setForeground(Color.WHITE);
        panel.add(title, BorderLayout.NORTH);

        List<Object[]> buffs = new ArrayList<>();
        if (ch.getBurnStacks() > 0) buffs.add(new Object[]{"灼烧", "灼烧 x" + ch.getBurnStacks(), new Color(255, 100, 0), GameIcons.buffBurn()});
        if (ch.isFrozen()) buffs.add(new Object[]{"冷冻", "冷冻", new Color(100, 180, 255), GameIcons.buffFreeze()});
        if (ch.getBleedStacks() > 0) buffs.add(new Object[]{"流血", "流血 x" + ch.getBleedStacks(), new Color(180, 0, 0), GameIcons.buffBleed()});

        JPanel btnPanel = new JPanel(new GridLayout(buffs.size(), 1, 5, 5));
        btnPanel.setOpaque(false);
        btnPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        for (Object[] buff : buffs) {
            JButton btn = new JButton((String) buff[1]) {
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
            btn.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            btn.setForeground((Color) buff[2]);
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);
            btn.setContentAreaFilled(false);
            btn.setOpaque(false);
            btn.setIcon((ImageIcon) buff[3]);
            btn.setHorizontalTextPosition(SwingConstants.RIGHT);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.addActionListener(e -> {
                result = (String) buff[0];
                dispose();
            });
            btnPanel.add(btn);
        }

        if (buffs.isEmpty()) {
            JLabel none = new JLabel("无buff可移除", SwingConstants.CENTER);
            none.setForeground(Color.GRAY);
            none.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            btnPanel.add(none);
        }

        panel.add(btnPanel, BorderLayout.CENTER);

        JButton cancelBtn = new JButton("取消") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                g2.setColor(new Color(60, 40, 40));
                g2.fillRoundRect(0, 0, w - 1, h - 1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        cancelBtn.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setFocusPainted(false);
        cancelBtn.setBorderPainted(false);
        cancelBtn.setContentAreaFilled(false);
        cancelBtn.setOpaque(false);
        cancelBtn.setIcon(GameIcons.uiSkip());
        cancelBtn.setHorizontalTextPosition(SwingConstants.RIGHT);
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelBtn.addActionListener(e -> dispose());
        JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER));
        south.setOpaque(false);
        south.add(cancelBtn);
        panel.add(south, BorderLayout.SOUTH);

        setContentPane(panel);
    }

    public String getResult() {
        return result;
    }
}
