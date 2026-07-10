import javax.swing.*;
import java.awt.*;

public class CardTooltipDialog {

    static void show(Game game, Card card, GameCharacter ch, boolean isDefend) {
        hide(game);

        String title = card.toString();
        String desc;
        if (isDefend) {
            desc = SkillDesc.getDefendDesc(ch, card);
        } else {
            desc = SkillDesc.getAttackDesc(ch, card);
        }
        if (desc == null || desc.isEmpty()) return;

        String charName = ch.getName().replace("AI ", "");
        String fullText = charName + " " + desc;
        String htmlText = colorize(fullText);

        JPanel panel = new JPanel(new BorderLayout(8, 4)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(20, 20, 35, 210));
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.setColor(new Color(180, 160, 120, 150));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        panel.setName("cardTooltip");
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        titleLabel.setForeground(new Color(255, 220, 100));

        JLabel descLabel = new JLabel("<html><div style='width:220px'>" + htmlText + "</div></html>");
        descLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        descLabel.setForeground(new Color(230, 230, 240));

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(descLabel, BorderLayout.CENTER);

        panel.setSize(panel.getPreferredSize());
        int px = game.getWidth() / 2 - panel.getWidth() / 2;
        int py = game.getHeight() / 2 - panel.getHeight() / 2 - 60;
        panel.setLocation(px, py);

        JPanel glassPane = (JPanel) game.getGlassPane();
        glassPane.add(panel);
        glassPane.setComponentZOrder(panel, 0);
        glassPane.repaint();
    }

    static void hide(Game game) {
        JPanel glassPane = (JPanel) game.getGlassPane();
        boolean removed = false;
        for (Component c : glassPane.getComponents()) {
            if (c.getName() != null && c.getName().equals("cardTooltip")) {
                glassPane.remove(c);
                removed = true;
            }
        }
        if (removed) glassPane.repaint();
    }

    private static String colorize(String text) {
        text = text.replace("[生命]", "<span style='color:#44dd44'>[生命]</span>");
        text = text.replace("[伤害]", "<span style='color:#ff4444'>[伤害]</span>");
        text = text.replace("[灼烧]", "<span style='color:#ff8800'>[灼烧]</span>");
        text = text.replace("[冷冻]", "<span style='color:#44aaff'>[冷冻]</span>");
        text = text.replace("[流血]", "<span style='color:#cc2222'>[流血]</span>");
        text = text.replace("[牌]", "<span style='color:#aaaaff'>[牌]</span>");
        text = text.replace("[战斗]", "<span style='color:#ffaa44'>[战斗]</span>");
        text = text.replace("[红]", "<span style='color:#ff4444'>[红]</span>");
        text = text.replace("[黄]", "<span style='color:#ddcc00'>[黄]</span>");
        text = text.replace("[蓝]", "<span style='color:#4488ff'>[蓝]</span>");
        text = text.replace("[绿]", "<span style='color:#44cc44'>[绿]</span>");
        text = text.replace("[白]", "<span style='color:#bbbbbb'>[白]</span>");
        text = text.replace("[黑]", "<span style='color:#999999'>[黑]</span>");
        return text;
    }
}
