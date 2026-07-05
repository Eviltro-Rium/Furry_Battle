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
        setSize(320, 240);
        setLocationRelativeTo(owner);
        setModal(true);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(30, 30, 45));
        panel.setBorder(BorderFactory.createLineBorder(new Color(100, 180, 255), 2));

        JLabel title = new JLabel("选择移除一层buff", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setForeground(Color.WHITE);
        panel.add(title, BorderLayout.NORTH);

        List<Object[]> buffs = new ArrayList<>();
        if (ch.getBurnStacks() > 0) buffs.add(new Object[]{"灼烧", "灼烧 x" + ch.getBurnStacks(), new Color(255, 100, 0)});
        if (ch.isFrozen()) buffs.add(new Object[]{"冷冻", "冷冻", new Color(100, 180, 255)});
        if (ch.getBleedStacks() > 0) buffs.add(new Object[]{"流血", "流血 x" + ch.getBleedStacks(), new Color(180, 0, 0)});

        JPanel btnPanel = new JPanel(new GridLayout(buffs.size(), 1, 5, 5));
        btnPanel.setBackground(new Color(30, 30, 45));
        btnPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        for (Object[] buff : buffs) {
            JButton btn = new JButton((String) buff[1]);
            btn.setFont(new Font("SansSerif", Font.PLAIN, 14));
            btn.setBackground(new Color(50, 50, 70));
            btn.setForeground((Color) buff[2]);
            btn.setFocusPainted(false);
            btn.addActionListener(e -> {
                result = (String) buff[0];
                dispose();
            });
            btnPanel.add(btn);
        }

        if (buffs.isEmpty()) {
            JLabel none = new JLabel("无buff可移除", SwingConstants.CENTER);
            none.setForeground(Color.GRAY);
            none.setFont(new Font("SansSerif", Font.PLAIN, 14));
            btnPanel.add(none);
        }

        panel.add(btnPanel, BorderLayout.CENTER);

        JButton cancelBtn = new JButton("取消");
        cancelBtn.setFont(new Font("SansSerif", Font.PLAIN, 13));
        cancelBtn.setBackground(new Color(60, 40, 40));
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setFocusPainted(false);
        cancelBtn.addActionListener(e -> dispose());
        JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER));
        south.setBackground(new Color(30, 30, 45));
        south.add(cancelBtn);
        panel.add(south, BorderLayout.SOUTH);

        setContentPane(panel);
    }

    public String getResult() {
        return result;
    }
}
