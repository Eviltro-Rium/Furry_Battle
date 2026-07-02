import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class ChanFiveReorderDialog extends JDialog {

    private List<Card> sourceCards;
    private Card[] slots;
    private JPanel[] slotPanels;
    private JLabel[] slotLabels;
    private JPanel sourcePanel;
    private JButton confirmBtn;
    private boolean confirmed = false;

    private int dragSourceIndex = -1;
    private boolean dragFromSlot = false;
    private int dragSlotIndex = -1;

    public ChanFiveReorderDialog(JFrame owner, List<Card> cards) {
        super(owner, true);
        this.sourceCards = new ArrayList<>(cards);
        this.slots = new Card[cards.size()];
        this.slotPanels = new JPanel[cards.size()];
        this.slotLabels = new JLabel[cards.size()];

        setTitle("5️⃣ 排序牌库顶");
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        ((JPanel)getContentPane()).setOpaque(false);

        JPanel bg = new JPanel(new BorderLayout(12, 12)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(30, 25, 40, 235));
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 24, 24);
                g2.setColor(new Color(120, 100, 180, 180));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 24, 24);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        bg.setOpaque(false);
        bg.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JLabel titleLabel = new JLabel("5️⃣ 拖动卡牌到空槽，排列牌库顶顺序", SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        titleLabel.setForeground(new Color(220, 200, 255));
        bg.add(titleLabel, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.setOpaque(false);

        JLabel topLabel = new JLabel("▲ 牌库顶（先抽到）", SwingConstants.CENTER);
        topLabel.setFont(new Font("微软雅黑", Font.BOLD, 13));
        topLabel.setForeground(new Color(100, 200, 255));
        center.add(topLabel, BorderLayout.NORTH);

        JPanel slotsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 6));
        slotsPanel.setOpaque(false);
        for (int i = 0; i < slots.length; i++) {
            final int slotIdx = i;
            JPanel sp = new JPanel(new BorderLayout()) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D)g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    if (slots[slotIdx] != null) {
                        Color c = GameUI.getSwingColor(slots[slotIdx].getColor());
                        g2.setColor(c);
                        g2.fillRoundRect(2, 2, getWidth()-5, getHeight()-5, 12, 12);
                        g2.setColor(new Color(255,255,255,60));
                        g2.setStroke(new BasicStroke(1f));
                        g2.drawRoundRect(2, 2, getWidth()-5, getHeight()-5, 12, 12);
                    } else {
                        g2.setColor(new Color(60, 50, 80, 180));
                        g2.fillRoundRect(2, 2, getWidth()-5, getHeight()-5, 12, 12);
                        g2.setColor(new Color(120, 100, 180, 120));
                        g2.setStroke(new BasicStroke(2f));
                        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{6,4}, 0));
                        g2.drawRoundRect(2, 2, getWidth()-5, getHeight()-5, 12, 12);
                    }
                    g2.dispose();
                }
            };
            sp.setOpaque(false);
            sp.setPreferredSize(new Dimension(72, 108));
            sp.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));

            JLabel sl = new JLabel("", SwingConstants.CENTER);
            sl.setFont(new Font("Arial", Font.BOLD, 24));
            sl.setForeground(Color.WHITE);
            sp.add(sl, BorderLayout.CENTER);

            JLabel idxLabel = new JLabel(String.valueOf(i+1), SwingConstants.CENTER);
            idxLabel.setFont(new Font("Arial", Font.PLAIN, 10));
            idxLabel.setForeground(new Color(160, 150, 200));
            sp.add(idxLabel, BorderLayout.SOUTH);

            sp.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    if (slots[slotIdx] != null) {
                        sourceCards.add(slots[slotIdx]);
                        slots[slotIdx] = null;
                        refreshAll();
                    }
                }
            });
            slotPanels[i] = sp;
            slotLabels[i] = sl;
            slotsPanel.add(sp);
        }
        center.add(slotsPanel, BorderLayout.CENTER);

        JLabel botLabel = new JLabel("▼ 牌库底（后抽到）", SwingConstants.CENTER);
        botLabel.setFont(new Font("微软雅黑", Font.BOLD, 13));
        botLabel.setForeground(new Color(180, 140, 100));
        center.add(botLabel, BorderLayout.SOUTH);

        bg.add(center, BorderLayout.CENTER);

        sourcePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        sourcePanel.setOpaque(false);
        refreshSourcePanel();
        bg.add(sourcePanel, BorderLayout.SOUTH);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 4));
        btnRow.setOpaque(false);

        confirmBtn = new JButton("✔ 确认排序") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        confirmBtn.setFont(new Font("微软雅黑", Font.BOLD, 15));
        confirmBtn.setForeground(Color.WHITE);
        confirmBtn.setBackground(new Color(60, 140, 80));
        confirmBtn.setFocusPainted(false);
        confirmBtn.setBorderPainted(false);
        confirmBtn.setContentAreaFilled(false);
        confirmBtn.setOpaque(false);
        confirmBtn.setPreferredSize(new Dimension(140, 38));
        confirmBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        confirmBtn.addActionListener(e -> {
            for (Card s : slots) {
                if (s == null) {
                    JOptionPane.showMessageDialog(this, "请将所有卡牌放入空槽！");
                    return;
                }
            }
            confirmed = true;
            dispose();
        });

        JButton resetBtn = new JButton("↺ 重置") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        resetBtn.setFont(new Font("微软雅黑", Font.BOLD, 14));
        resetBtn.setForeground(Color.WHITE);
        resetBtn.setBackground(new Color(140, 80, 60));
        resetBtn.setFocusPainted(false);
        resetBtn.setBorderPainted(false);
        resetBtn.setContentAreaFilled(false);
        resetBtn.setOpaque(false);
        resetBtn.setPreferredSize(new Dimension(100, 38));
        resetBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        resetBtn.addActionListener(e -> {
            for (int i = 0; i < slots.length; i++) {
                if (slots[i] != null) {
                    sourceCards.add(slots[i]);
                    slots[i] = null;
                }
            }
            refreshAll();
        });

        btnRow.add(resetBtn);
        btnRow.add(confirmBtn);

        JPanel southWrap = new JPanel(new BorderLayout(0, 8));
        southWrap.setOpaque(false);
        southWrap.add(sourcePanel, BorderLayout.NORTH);
        southWrap.add(btnRow, BorderLayout.SOUTH);
        bg.add(southWrap, BorderLayout.SOUTH);

        setContentPane(bg);
        setSize(580, 420);
        setLocationRelativeTo(owner);

        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                for (int i = 0; i < slots.length; i++) {
                    if (slots[i] != null) sourceCards.add(slots[i]);
                }
            }
        });
    }

    private void refreshSourcePanel() {
        sourcePanel.removeAll();
        if (sourceCards.isEmpty()) {
            JLabel empty = new JLabel("所有卡牌已放入空槽", SwingConstants.CENTER);
            empty.setFont(new Font("微软雅黑", Font.PLAIN, 12));
            empty.setForeground(new Color(160, 150, 200));
            sourcePanel.add(empty);
        } else {
            JLabel hint = new JLabel("点击卡牌放入空槽：");
            hint.setFont(new Font("微软雅黑", Font.PLAIN, 12));
            hint.setForeground(new Color(180, 170, 220));
            sourcePanel.add(hint);
            for (int i = 0; i < sourceCards.size(); i++) {
                final int cardIdx = i;
                Card card = sourceCards.get(i);
                JPanel cp = new JPanel(new BorderLayout()) {
                    @Override protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D)g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        Color c = GameUI.getSwingColor(card.getColor());
                        g2.setColor(c);
                        g2.fillRoundRect(2, 2, getWidth()-5, getHeight()-5, 10, 10);
                        g2.setColor(new Color(255,255,255,60));
                        g2.setStroke(new BasicStroke(1f));
                        g2.drawRoundRect(2, 2, getWidth()-5, getHeight()-5, 10, 10);
                        g2.dispose();
                    }
                };
                cp.setOpaque(false);
                cp.setPreferredSize(new Dimension(56, 80));
                cp.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                String text;
                if (card.isBlack()) text = "✦";
                else if (card.isPotion()) text = "🧪";
                else if (card.isDrawThree()) text = "+3";
                else text = String.valueOf(card.getValue());

                JLabel cl = new JLabel(text, SwingConstants.CENTER);
                cl.setFont(new Font("Arial", Font.BOLD, 20));
                cl.setForeground(Color.WHITE);
                cp.add(cl, BorderLayout.CENTER);

                cp.addMouseListener(new MouseAdapter() {
                    @Override public void mouseClicked(MouseEvent e) {
                        int firstEmpty = -1;
                        for (int j = 0; j < slots.length; j++) {
                            if (slots[j] == null) { firstEmpty = j; break; }
                        }
                        if (firstEmpty >= 0) {
                            slots[firstEmpty] = sourceCards.remove(cardIdx);
                            refreshAll();
                        }
                    }
                });
                sourcePanel.add(cp);
            }
        }
        sourcePanel.revalidate();
        sourcePanel.repaint();
    }

    private void refreshSlots() {
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] != null) {
                String text;
                if (slots[i].isBlack()) text = "✦";
                else if (slots[i].isPotion()) text = "🧪";
                else if (slots[i].isDrawThree()) text = "+3";
                else text = String.valueOf(slots[i].getValue());
                slotLabels[i].setText(text);
                slotLabels[i].setForeground(Color.WHITE);
                slotLabels[i].setFont(new Font("Arial", Font.BOLD, 24));
            } else {
                slotLabels[i].setText("");
            }
            slotPanels[i].repaint();
        }
    }

    private void refreshAll() {
        refreshSlots();
        refreshSourcePanel();
    }

    public List<Card> getResult() {
        if (!confirmed) {
            List<Card> all = new ArrayList<>(sourceCards);
            for (Card s : slots) if (s != null) all.add(s);
            return all;
        }
        List<Card> result = new ArrayList<>();
        for (Card s : slots) result.add(s);
        return result;
    }

    public boolean isConfirmed() { return confirmed; }
}