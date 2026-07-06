import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class GameAnim {

    private static final Map<String, Integer> floatOffsetMap = new HashMap<>();
    private static final Map<String, Long> floatTimeMap = new HashMap<>();
    private static final int FLOAT_ROW_HEIGHT = 32;
    private static final long FLOAT_RESET_MS = 800;

    private static int getFloatOffset(String key) {
        long now = System.currentTimeMillis();
        Long lastTime = floatTimeMap.get(key);
        if (lastTime != null && now - lastTime > FLOAT_RESET_MS) {
            floatOffsetMap.put(key, 0);
        }
        int offset = floatOffsetMap.getOrDefault(key, 0);
        floatOffsetMap.put(key, offset + 1);
        floatTimeMap.put(key, now);
        return offset;
    }

    static void resetFloatOffsets() {
        floatOffsetMap.clear();
        floatTimeMap.clear();
    }

    // ── Pre-rendered card images (reused) ──
    private static final int SW = 80, SH = 120;
    private static final int BW = 104, BH = 154;

    private static BufferedImage renderCardImage(Card card, int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color top = GameUI.getSwingColor(card.getColor());
        Color bot = darken(top);

        GradientPaint gp = new GradientPaint(0, 0, top, w, h, bot);
        g2.setPaint(gp);
        g2.fillRoundRect(2, 2, w - 5, h - 5, 16, 16);

        if (card.getChosenColor() != null) {
            Color borderC = GameUI.getSwingColor(card.getChosenColor());
            g2.setColor(borderC);
            g2.setStroke(new BasicStroke(3f));
            g2.drawRoundRect(2, 2, w - 5, h - 5, 16, 16);
        } else {
            g2.setColor(new Color(255, 255, 255, 150));
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(2, 2, w - 5, h - 5, 16, 16);
        }

        GradientPaint shine = new GradientPaint(0, 0, new Color(255, 255, 255, 120), 0, h / 2, new Color(255, 255, 255, 0));
        g2.setPaint(shine);
        g2.fillRoundRect(4, 4, w - 10, h / 2, 14, 14);

        g2.setFont(new Font("Arial", Font.BOLD, w > 90 ? 56 : 40));
        g2.setColor(Color.WHITE);
        String text;
        if (card.isBlack()) {
            text = "✦";
        } else {
            text = String.valueOf(card.getValue());
        }
        FontMetrics fm = g2.getFontMetrics();
        int tx = (w - fm.stringWidth(text)) / 2;
        int ty = (h + fm.getAscent() - fm.getDescent()) / 2;
        g2.drawString(text, tx, ty);

        g2.dispose();
        return img;
    }

    // ── Lightweight flat card for fast flight (no gradient) ──
    private static JPanel createFlyPanel(Card card, int w, int h) {
        Color c = GameUI.getSwingColor(card.getColor());
        JPanel p = new JPanel(null);
        p.setSize(w, h);
        p.setBackground(c);
        if (card.getChosenColor() != null) {
            p.setBorder(BorderFactory.createLineBorder(GameUI.getSwingColor(card.getChosenColor()), 3));
        } else {
            p.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 120), 2));
        }
        p.setOpaque(true);

        JLabel lbl = new JLabel("", SwingConstants.CENTER);
        if (card.isBlack()) {
            lbl.setText("✦");
            lbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, w > 90 ? 44 : 30));
        } else if (card.isSuperPurify()) {
            lbl.setText("✨✨");
            lbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, w > 90 ? 36 : 24));
        } else if (card.isPurify()) {
            lbl.setText("✨");
            lbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, w > 90 ? 44 : 30));
        } else if (card.isPotion()) {
            lbl.setText("🧪");
            lbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, w > 90 ? 44 : 30));
        } else if (card.isDrawThree()) {
            lbl.setText("+3");
            lbl.setFont(new Font("Arial", Font.BOLD, w > 90 ? 44 : 30));
            lbl.setForeground(new Color(60, 60, 80));
        } else {
            lbl.setText(String.valueOf(card.getValue()));
            lbl.setFont(new Font("Arial", Font.BOLD, w > 90 ? 56 : 40));
            lbl.setForeground(Color.WHITE);
        }
        lbl.setBounds(0, 0, w, h);
        p.add(lbl);
        return p;
    }

    // ── Main play-card animation ──
    static void playCardAnimation(Game game, Card card, Point from, Point to, Runnable onDone) {
        JPanel glassPane = (JPanel) game.getGlassPane();
        Point center = getScreenCenter(game);

        JPanel[] flyCard = {createFlyPanel(card, SW, SH)};
        flyCard[0].setLocation(from);
        glassPane.add(flyCard[0]);

        int[] phase = {0};
        int[] tick = {0};
        int flyFrames = 8;
        int holdFrames = 12;

        javax.swing.Timer t = new javax.swing.Timer(25, null);
        t.addActionListener(e -> {
            tick[0]++;

            if (phase[0] == 0) {
                double pct = Math.min((double) tick[0] / flyFrames, 1.0);
                double ease = 1.0 - Math.pow(1.0 - pct, 2);
                int x = from.x + (int) ((center.x + (BW - SW) / 2 - from.x) * ease);
                int y = from.y + (int) ((center.y + (BH - SH) / 2 - from.y) * ease);
                flyCard[0].setLocation(x, y);

                if (pct >= 1.0) {
                    phase[0] = 1; tick[0] = 0;
                    glassPane.remove(flyCard[0]);
                    JPanel big = createFlyPanel(card, BW, BH);
                    big.setLocation(center.x, center.y);
                    glassPane.add(big);
                    flyCard[0] = big;
                }
            } else if (phase[0] == 1) {
                if (tick[0] >= holdFrames) {
                    phase[0] = 2; tick[0] = 0;
                    glassPane.remove(flyCard[0]);
                    JPanel small = createFlyPanel(card, SW, SH);
                    small.setLocation(center.x + (BW - SW) / 2, center.y + (BH - SH) / 2);
                    glassPane.add(small);
                    flyCard[0] = small;
                }
            } else {
                double pct = Math.min((double) tick[0] / flyFrames, 1.0);
                double ease = pct * pct;
                int sx = center.x + (BW - SW) / 2;
                int sy = center.y + (BH - SH) / 2;
                int x = sx + (int) ((to.x - sx) * ease);
                int y = sy + (int) ((to.y - sy) * ease);
                flyCard[0].setLocation(x, y);

                if (pct >= 1.0) {
                    t.stop();
                    glassPane.remove(flyCard[0]);
                    glassPane.repaint();
                    onDone.run();
                }
            }
            glassPane.repaint();
        });
        t.start();
    }

    // ── Simple fly A → B ──
    static void playFlyAnimation(Game game, Card card, Point from, Point to, Runnable onDone) {
        JPanel glassPane = (JPanel) game.getGlassPane();
        JPanel[] fc = {createFlyPanel(card, SW, SH)};
        fc[0].setLocation(from);
        glassPane.add(fc[0]);

        int[] tick = {0};
        int frames = 14;
        double sx = from.x, sy = from.y;
        double dx = to.x - sx, dy = to.y - sy;

        javax.swing.Timer t = new javax.swing.Timer(25, null);
        t.addActionListener(e -> {
            tick[0]++;
            double pct = Math.min((double) tick[0] / frames, 1.0);
            double ease = 1.0 - Math.pow(1.0 - pct, 3);
            fc[0].setLocation((int) (sx + dx * ease), (int) (sy + dy * ease));

            if (pct >= 1.0) {
                t.stop();
                glassPane.remove(fc[0]);
                glassPane.repaint();
                onDone.run();
            } else {
                glassPane.repaint();
            }
        });
        t.start();
    }

    // ── Draw cards from deck ──
    static void playDrawAnimations(Game game, int count, boolean isPlayer, Runnable onDone) {
        if (count <= 0) { onDone.run(); return; }
        JPanel glassPane = (JPanel) game.getGlassPane();
        GameUI ui = game.getUI();
        Point from = getDeckCenter(ui, game);
        Point to = isPlayer ? getPlayerHandInsertPoint(ui, game) : getAIHandCenter(ui, game);

        int cw = 36, ch = 52;
        int[] idx = {0};
        Color deckBlue = new Color(50, 100, 200);

        javax.swing.Timer spawn = new javax.swing.Timer(90, null);
        spawn.addActionListener(e -> {
            if (idx[0] >= count) { spawn.stop(); onDone.run(); return; }

            JPanel[] fc = {new JPanel(null)};
            fc[0].setSize(cw, ch);
            fc[0].setBackground(deckBlue);
            fc[0].setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 100), 1));
            fc[0].setOpaque(true);
            int ox = (int) (Math.random() * 18 - 9);
            int oy = (int) (Math.random() * 14 - 7);
            fc[0].setLocation(from.x + ox, from.y + oy);
            glassPane.add(fc[0]);

            double sx = fc[0].getX(), sy = fc[0].getY();
            double ddx = to.x - sx, ddy = to.y - sy;
            int[] tick = {0};
            int frames = 12;

            javax.swing.Timer fly = new javax.swing.Timer(25, e2 -> {
                tick[0]++;
                double pct = Math.min((double) tick[0] / frames, 1.0);
                double ease = 1.0 - Math.pow(1.0 - pct, 3);
                fc[0].setLocation((int) (sx + ddx * ease), (int) (sy + ddy * ease));
                if (pct >= 1.0) {
                    ((javax.swing.Timer) e2.getSource()).stop();
                    glassPane.remove(fc[0]);
                    glassPane.repaint();
                }
            });
            fly.start();
            idx[0]++;
        });
        spawn.start();
    }

    static void playFloatingText(Game game, String text, Color color, Point location) {
        String key = location.x / 80 + "_" + location.y / 60;
        int row = getFloatOffset(key);
        int offsetY = -row * FLOAT_ROW_HEIGHT;

        JPanel glassPane = (JPanel) game.getGlassPane();
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 28));
        label.setForeground(color);
        label.setSize(220, 40);
        int startY = location.y + offsetY;
        label.setLocation(location.x - 110, startY);
        glassPane.add(label);
        glassPane.setComponentZOrder(label, 0);

        int[] tick = {0};
        int frames = 30;
        javax.swing.Timer t = new javax.swing.Timer(30, e -> {
            tick[0]++;
            double pct = (double) tick[0] / frames;
            label.setLocation(label.getX(), startY - (int) (60 * pct));
            int alpha = (int) (255 * (1.0 - pct * pct));
            label.setForeground(new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.max(0, alpha)));
            glassPane.repaint(label.getBounds());
            if (tick[0] >= frames) { ((javax.swing.Timer) e.getSource()).stop(); glassPane.remove(label); glassPane.repaint(); }
        });
        t.start();
    }

    // ── Helpers ──

    private static void repaintCard(JComponent c) {
        Rectangle r = c.getBounds();
        r.grow(4, 4);
        c.getParent().repaint(r.x, r.y, r.width, r.height);
    }

    private static Color darken(Color c) {
        int r = (int) (c.getRed() * 0.72);
        int g = (int) (c.getGreen() * 0.72);
        int b = (int) (c.getBlue() * 0.72);
        return new Color(r, g, b);
    }

    static Point getScreenCenter(Game game) {
        return new Point(game.getWidth() / 2 - 52, game.getHeight() / 2 - 77);
    }

    static Point getDiscardTopCenter(GameUI ui, Game game) {
        Point p = ui.discardCardPanel.getLocationOnScreen();
        SwingUtilities.convertPointFromScreen(p, game.getContentPane());
        p.x += (ui.discardCardPanel.getWidth() - 80) / 2;
        p.y += (ui.discardCardPanel.getHeight() - 120) / 2;
        return p;
    }

    static Point getAttackPanelCenter(GameUI ui, Game game) {
        Point p = ui.aiAttackPanel.getLocationOnScreen();
        SwingUtilities.convertPointFromScreen(p, game.getContentPane());
        p.x += ui.aiAttackPanel.getWidth() / 2 - 40;
        p.y += ui.aiAttackPanel.getHeight() / 2 - 60;
        return p;
    }

    static Point getRevealPanelCenter(GameUI ui, Game game) {
        Point p = ui.aiRevealPanel.getLocationOnScreen();
        SwingUtilities.convertPointFromScreen(p, game.getContentPane());
        p.x += ui.aiRevealPanel.getWidth() / 2 - 40;
        p.y += ui.aiRevealPanel.getHeight() / 2 - 60;
        return p;
    }

    static Point getAIHandCenter(GameUI ui, Game game) {
        Point p = ui.aiHandPanel.getLocationOnScreen();
        SwingUtilities.convertPointFromScreen(p, game.getContentPane());
        p.x += ui.aiHandPanel.getWidth() / 2 - 28;
        p.y += ui.aiHandPanel.getHeight() / 2 - 41;
        return p;
    }

    static Point getPlayerHandCardCenter(GameUI ui, Game game, int index) {
        if (ui.playerHandPanel.getComponentCount() == 0) {
            Point p = ui.playerHandPanel.getLocationOnScreen();
            SwingUtilities.convertPointFromScreen(p, game.getContentPane());
            return p;
        }
        int idx = Math.min(index, ui.playerHandPanel.getComponentCount() - 1);
        Component c = ui.playerHandPanel.getComponent(idx);
        Point p = c.getLocationOnScreen();
        SwingUtilities.convertPointFromScreen(p, game.getContentPane());
        return p;
    }

    static Point getDeckCenter(GameUI ui, Game game) {
        Point p = ui.deckLabel.getLocationOnScreen();
        SwingUtilities.convertPointFromScreen(p, game.getContentPane());
        p.x += ui.deckLabel.getWidth() / 2 - 40;
        p.y += ui.deckLabel.getHeight() / 2 - 60;
        return p;
    }

    static Point getPlayerHandInsertPoint(GameUI ui, Game game) {
        if (ui.playerHandPanel.getComponentCount() == 0) {
            Point p = ui.playerHandPanel.getLocationOnScreen();
            SwingUtilities.convertPointFromScreen(p, game.getContentPane());
            p.x += 10; p.y += 10;
            return p;
        }
        Component last = ui.playerHandPanel.getComponent(ui.playerHandPanel.getComponentCount() - 1);
        Point p = last.getLocationOnScreen();
        SwingUtilities.convertPointFromScreen(p, game.getContentPane());
        p.x += last.getWidth() + 6;
        return p;
    }
}
