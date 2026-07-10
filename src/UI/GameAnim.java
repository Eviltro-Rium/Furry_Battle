import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class GameAnim {

    private static final Map<String, Integer> floatOffsetMap = new HashMap<>();
    private static final Map<String, Long> floatTimeMap = new HashMap<>();
    private static final int FLOAT_ROW_HEIGHT = 32;
    private static final long FLOAT_RESET_MS = 800;

    private static final Queue<Runnable> floatQueue = new LinkedList<>();
    private static boolean floatPlaying = false;
    private static final int FLOAT_DURATION_MS = 900;

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

    static void playFloatingText(Game game, String text, Color color, Point location) {
        floatQueue.add(() -> doPlayFloatingText(game, text, color, location));
        tryNextFloat();
    }

    private static void tryNextFloat() {
        if (floatPlaying || floatQueue.isEmpty()) return;
        floatPlaying = true;
        Runnable r = floatQueue.poll();
        r.run();
    }

    private static void onFloatDone() {
        floatPlaying = false;
        tryNextFloat();
    }

    private static final Map<String, Color> TAG_COLORS = new LinkedHashMap<>();
    static {
        TAG_COLORS.put("[生命]", new Color(68, 221, 68));
        TAG_COLORS.put("[伤害]", new Color(255, 68, 68));
        TAG_COLORS.put("[灼烧]", new Color(255, 136, 0));
        TAG_COLORS.put("[冷冻]", new Color(68, 170, 255));
        TAG_COLORS.put("[流血]", new Color(204, 34, 34));
        TAG_COLORS.put("[牌]", new Color(170, 170, 255));
        TAG_COLORS.put("[战斗]", new Color(255, 170, 68));
        TAG_COLORS.put("[交换]", new Color(100, 80, 200));
        TAG_COLORS.put("[洗入]", new Color(100, 80, 200));
        TAG_COLORS.put("[净化]", new Color(200, 180, 255));
        TAG_COLORS.put("[解冻]", new Color(100, 180, 255));
        TAG_COLORS.put("[红]", new Color(255, 68, 68));
        TAG_COLORS.put("[黄]", new Color(221, 204, 0));
        TAG_COLORS.put("[蓝]", new Color(68, 136, 255));
        TAG_COLORS.put("[绿]", new Color(68, 204, 68));
        TAG_COLORS.put("[白]", new Color(187, 187, 187));
        TAG_COLORS.put("[黑]", new Color(153, 153, 153));
    }

    private static java.util.List<Object[]> parseSegments(String text, Color defaultColor) {
        java.util.List<Object[]> segments = new java.util.ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '[') {
                int end = text.indexOf(']', i);
                if (end >= 0) {
                    String tag = text.substring(i, end + 1);
                    if (sb.length() > 0) {
                        segments.add(new Object[]{sb.toString(), defaultColor});
                        sb = new StringBuilder();
                    }
                    Color tc = TAG_COLORS.getOrDefault(tag, defaultColor);
                    segments.add(new Object[]{tag, tc});
                    i = end;
                    continue;
                }
            }
            sb.append(text.charAt(i));
        }
        if (sb.length() > 0) segments.add(new Object[]{sb.toString(), defaultColor});
        return segments;
    }

    private static void doPlayFloatingText(Game game, String text, Color color, Point location) {
        String key = location.x / 80 + "_" + location.y / 60;
        int row = getFloatOffset(key);
        int offsetY = -row * FLOAT_ROW_HEIGHT;

        java.util.List<Object[]> segments = parseSegments(text, color);

        JPanel glassPane = (JPanel) game.getGlassPane();

        Font font = new Font("微软雅黑", Font.BOLD, 24);
        FontMetrics fm = game.getFontMetrics(font);
        int totalW = 0;
        for (Object[] seg : segments) totalW += fm.stringWidth((String) seg[0]);
        int panelW = Math.max(220, totalW + 20);
        int finalTotalW = totalW;

        JPanel textPanel = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int h = getHeight();
                g2.setFont(font);
                FontMetrics lfm = g2.getFontMetrics();
                int ty = (h + lfm.getAscent() - lfm.getDescent()) / 2;
                int cx = (getWidth() - finalTotalW) / 2;
                for (Object[] seg : segments) {
                    String s = (String) seg[0];
                    Color c = (Color) seg[1];
                    g2.setColor(new Color(0, 0, 0, 120));
                    g2.drawString(s, cx + 2, ty + 2);
                    g2.setColor(c);
                    g2.drawString(s, cx, ty);
                    cx += lfm.stringWidth(s);
                }
                g2.dispose();
            }
        };
        textPanel.setSize(panelW, 44);
        int startY = location.y + offsetY;
        textPanel.setLocation(location.x - 110, startY);
        textPanel.setOpaque(false);
        glassPane.add(textPanel);
        glassPane.setComponentZOrder(textPanel, 0);

        int[] tick = {0};
        int frames = 32;
        javax.swing.Timer t = new javax.swing.Timer(28, e -> {
            tick[0]++;
            double pct = (double) tick[0] / frames;
            double ease = 1.0 - Math.pow(1.0 - pct, 2);
            int moveY = (int) (70 * ease);
            textPanel.setLocation(textPanel.getX(), startY - moveY);
            double alpha = 1.0 - pct * pct;
            textPanel.setOpaque(false);
            if (pct < 0.15) {
                double scaleP = pct / 0.15;
                int sw = (int) (220 * (0.8 + 0.2 * scaleP));
                int sh = (int) (44 * (0.8 + 0.2 * scaleP));
                textPanel.setSize(sw, sh);
            } else {
                textPanel.setSize(220, 44);
            }
            glassPane.repaint(textPanel.getBounds());
            if (tick[0] >= frames) {
                ((javax.swing.Timer) e.getSource()).stop();
                glassPane.remove(textPanel);
                glassPane.repaint();
                onFloatDone();
            }
        });
        t.start();
    }

    // ── Pre-rendered card images (reused) ──
    private static final int SW = 80, SH = 120;
    private static final int BW = 104, BH = 154;

    static BufferedImage renderCardImage(Card card, int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color top = GameUI.getSwingColor(card.getColor());
        Color bot = darken(top);
        boolean hasChosen = card.getChosenColor() != null && (card.isBlack() || card.isWhite());
        Color chosen = hasChosen ? GameUI.getSwingColor(card.getChosenColor()) : null;
        Color chosenBot = hasChosen ? darken(chosen) : null;

        GradientPaint gp = new GradientPaint(0, 0, top, w, h, bot);
        g2.setPaint(gp);
        g2.fillRoundRect(2, 2, w - 5, h - 5, 16, 16);

        if (hasChosen) {
            java.awt.geom.RoundRectangle2D cardClip = new java.awt.geom.RoundRectangle2D.Float(2, 2, w - 5, h - 5, 16, 16);
            g2.setClip(cardClip);

            if (card.isBlack()) {
                g2.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.45f));
                GradientPaint chosenGp = new GradientPaint(0, 0, chosen, w, h, chosenBot);
                g2.setPaint(chosenGp);
                g2.fillRoundRect(2, 2, w - 5, h - 5, 16, 16);
                g2.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 1.0f));

                int sashW = (int)(w * 0.38);
                int cx = w / 2, cy = h / 2;
                java.awt.geom.AffineTransform orig = g2.getTransform();
                g2.rotate(Math.toRadians(-30), cx, cy);
                GradientPaint sashGp = new GradientPaint(cx - sashW, cy - (int)(h * 0.14), chosen.brighter(), cx + sashW, cy + (int)(h * 0.14), chosenBot);
                g2.setPaint(sashGp);
                g2.fillRoundRect(cx - sashW, cy - (int)(h * 0.14), sashW * 2, (int)(h * 0.28), 6, 6);
                g2.setColor(new Color(255, 255, 255, 80));
                g2.fillRoundRect(cx - sashW, cy - (int)(h * 0.14), sashW * 2, (int)(h * 0.10), 6, 6);
                g2.setColor(new Color(255, 255, 255, 35));
                g2.drawRoundRect(cx - sashW, cy - (int)(h * 0.14), sashW * 2, (int)(h * 0.28), 6, 6);
                g2.setTransform(orig);

                g2.setColor(new Color(0, 0, 0, 25));
                g2.setStroke(new BasicStroke(1f));
                int cornerR = 12;
                g2.drawRoundRect(5, 5, w - 12, h - 12, cornerR, cornerR);
            } else if (card.isWhite()) {
                g2.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.30f));
                GradientPaint chosenGp = new GradientPaint(0, 0, chosen, w, h, chosenBot);
                g2.setPaint(chosenGp);
                g2.fillRoundRect(2, 2, w - 5, h - 5, 16, 16);
                g2.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 1.0f));

                int bandH = (int)(h * 0.16);
                int bandY = h - bandH - 6;
                GradientPaint bandGp = new GradientPaint(0, bandY, chosen.brighter(), 0, bandY + bandH, chosenBot);
                g2.setPaint(bandGp);
                g2.fillRoundRect(5, bandY, w - 12, bandH, 8, 8);
                g2.setColor(new Color(255, 255, 255, 70));
                g2.fillRoundRect(5, bandY, w - 12, bandH / 2, 8, 8);
                g2.setColor(new Color(255, 255, 255, 40));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(5, bandY, w - 12, bandH, 8, 8);

                int topBandH = (int)(h * 0.10);
                int topBandY = 6;
                g2.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 0.55f));
                GradientPaint topBandGp = new GradientPaint(0, topBandY, chosen, 0, topBandY + topBandH, chosenBot);
                g2.setPaint(topBandGp);
                g2.fillRoundRect(5, topBandY, w - 12, topBandH, 8, 8);
                g2.setColor(new Color(255, 255, 255, 50));
                g2.fillRoundRect(5, topBandY, w - 12, topBandH / 2, 8, 8);
                g2.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 1.0f));
            }

            g2.setClip(null);
        }

        if (hasChosen) {
            g2.setColor(new Color(chosen.getRed(), chosen.getGreen(), chosen.getBlue(), 50));
            g2.setStroke(new BasicStroke(8f));
            g2.drawRoundRect(2, 2, w - 5, h - 5, 16, 16);
            g2.setColor(chosen);
            g2.setStroke(new BasicStroke(2.5f));
            g2.drawRoundRect(3, 3, w - 7, h - 7, 15, 15);
            g2.setColor(new Color(255, 255, 255, 60));
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(5, 5, w - 11, h - 11, 13, 13);
        } else {
            g2.setColor(new Color(255, 255, 255, 150));
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(2, 2, w - 5, h - 5, 16, 16);
        }

        if (!hasChosen || !card.isBlack()) {
            GradientPaint shine = new GradientPaint(0, 0, new Color(255, 255, 255, 120), 0, h / 2, new Color(255, 255, 255, 0));
            g2.setPaint(shine);
            g2.fillRoundRect(4, 4, w - 10, h / 2, 14, 14);
        } else {
            GradientPaint shine = new GradientPaint(0, 0, new Color(255, 255, 255, 50), 0, h / 3, new Color(255, 255, 255, 0));
            g2.setPaint(shine);
            g2.fillRoundRect(4, 4, w - 10, h / 3, 14, 14);
        }

        ImageIcon icon = null;
        String text = null;
        if (card.isBlack()) {
            if (card.isShuffleToDeck()) {
                icon = GameIcons.cardShuffleToDeck();
            } else if (card.isDrawTwo()) {
                icon = GameIcons.cardDrawThree();
            } else {
                icon = GameIcons.cardBlack();
            }
        } else if (card.isSuperPurify()) {
            icon = GameIcons.cardSuperPurify();
        } else if (card.isPurify()) {
            icon = GameIcons.cardPurify();
        } else if (card.isPotion()) {
            icon = GameIcons.cardPotion();
        } else if (card.isDrawThree()) {
            icon = GameIcons.cardDrawThree();
        } else if (card.isSwapHand()) {
            icon = GameIcons.cardSwapHand();
        } else {
            text = String.valueOf(card.getValue());
        }

        if (icon != null) {
            int iw = icon.getIconWidth(), ih = icon.getIconHeight();
            icon.paintIcon(null, g2, (w - iw) / 2, (h - ih) / 2);
        } else if (text != null) {
            int fontSize = w > 90 ? 56 : 40;
            g2.setFont(new Font("Arial", Font.BOLD, fontSize));
            FontMetrics fm = g2.getFontMetrics();
            int tx = (w - fm.stringWidth(text)) / 2;
            int ty = (h + fm.getAscent() - fm.getDescent()) / 2;
            g2.setColor(new Color(0, 0, 0, 160));
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (dx != 0 || dy != 0) g2.drawString(text, tx + dx, ty + dy);
                }
            }
            g2.setColor(Color.WHITE);
            g2.drawString(text, tx, ty);
        }

        ImageIcon cornerIcon = null;
        String cornerText = null;
        if (card.isBlack()) {
            cornerIcon = GameIcons.scaled("/icons/card_icons/color_palette.png", 18, 18);
        } else if (card.isSuperPurify()) {
            cornerIcon = GameIcons.scaled("/icons/card_icons/super_purify.png", 18, 18);
        } else if (card.isPurify()) {
            cornerIcon = GameIcons.scaled("/icons/card_icons/purify.png", 18, 18);
        } else if (card.isPotion()) {
            cornerIcon = GameIcons.scaled("/icons/card_icons/potion.png", 18, 18);
        } else if (card.isSwapHand()) {
            cornerIcon = GameIcons.scaled("/icons/card_icons/swap_cards.png", 18, 18);
        } else if (card.isDrawThree()) {
            cornerIcon = GameIcons.scaled("/icons/card_icons/draw_cards.png", 18, 18);
        } else if (card.isWhite()) {
            cornerText = String.valueOf(card.getValue());
        } else {
            cornerText = String.valueOf(card.getValue());
        }

        if (cornerIcon != null) {
            cornerIcon.paintIcon(null, g2, 6, 4);
        } else if (cornerText != null) {
            g2.setFont(new Font("Arial", Font.BOLD, w > 90 ? 16 : 12));
            FontMetrics cfm = g2.getFontMetrics();
            boolean isWhite = card.isWhite() && !card.isItemCard();
            g2.setColor(isWhite ? new Color(100, 90, 110) : new Color(255, 255, 255, 220));
            g2.drawString(cornerText, 8, 6 + cfm.getAscent());
        }


        g2.dispose();
        return img;
    }

    // ── Lightweight flat card for fast flight (no gradient) ──
    private static JPanel createFlyPanel(Card card, int w, int h) {
        BufferedImage img = renderCardImage(card, w, h);
        JPanel p = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (img != null) {
                    g.drawImage(img, 0, 0, getWidth(), getHeight(), null);
                }
            }
        };
        p.setSize(w, h);
        p.setOpaque(false);
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
        int flyFrames = 10;
        int holdFrames = 14;

        javax.swing.Timer t = new javax.swing.Timer(22, null);
        t.addActionListener(e -> {
            tick[0]++;

            if (phase[0] == 0) {
                double pct = Math.min((double) tick[0] / flyFrames, 1.0);
                double ease = elasticOut(pct);
                int targetX = center.x + (BW - SW) / 2;
                int targetY = center.y + (BH - SH) / 2;
                double cx = (from.x + targetX) / 2.0;
                double arcY = Math.min(from.y, targetY) - 60;
                double oneMinusT = 1.0 - ease;
                int x = (int) (oneMinusT * oneMinusT * from.x + 2 * oneMinusT * ease * cx + ease * ease * targetX);
                int y = (int) (oneMinusT * oneMinusT * from.y + 2 * oneMinusT * ease * arcY + ease * ease * targetY);
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
                double ease = backIn(pct);
                int sx = center.x + (BW - SW) / 2;
                int sy = center.y + (BH - SH) / 2;
                double cx = (sx + to.x) / 2.0;
                double arcY = Math.min(sy, to.y) - 40;
                double oneMinusT = 1.0 - ease;
                int x = (int) (oneMinusT * oneMinusT * sx + 2 * oneMinusT * ease * cx + ease * ease * to.x);
                int y = (int) (oneMinusT * oneMinusT * sy + 2 * oneMinusT * ease * arcY + ease * ease * to.y);
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

    // ── Simple fly A → B with arc ──
    static void playFlyAnimation(Game game, Card card, Point from, Point to, Runnable onDone) {
        JPanel glassPane = (JPanel) game.getGlassPane();
        JPanel[] fc = {createFlyPanel(card, SW, SH)};
        fc[0].setLocation(from);
        glassPane.add(fc[0]);

        int[] tick = {0};
        int frames = 16;
        double sx = from.x, sy = from.y;
        double dx = to.x - sx, dy = to.y - sy;

        javax.swing.Timer t = new javax.swing.Timer(22, null);
        t.addActionListener(e -> {
            tick[0]++;
            double pct = Math.min((double) tick[0] / frames, 1.0);
            double ease = elasticOut(pct);
            double cx = (sx + to.x) / 2.0;
            double arcY = Math.min(sy, to.y) - 50;
            double oneMinusT = 1.0 - ease;
            int x = (int) (oneMinusT * oneMinusT * sx + 2 * oneMinusT * ease * cx + ease * ease * to.x);
            int y = (int) (oneMinusT * oneMinusT * sy + 2 * oneMinusT * ease * arcY + ease * ease * to.y);
            fc[0].setLocation(x, y);

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
        int[] completed = {0};

        javax.swing.Timer spawn = new javax.swing.Timer(75, null);
        spawn.addActionListener(e -> {
            if (idx[0] >= count) { spawn.stop(); return; }

            JPanel[] fc = {new JPanel(null) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int pw = getWidth(), ph = getHeight();
                    g2.setColor(new Color(0, 0, 0, 30));
                    g2.fillRoundRect(3, 3, pw - 4, ph - 4, 10, 10);
                    GradientPaint gp = new GradientPaint(0, 0, new Color(70, 140, 240), 0, ph, new Color(30, 80, 200));
                    g2.setPaint(gp);
                    g2.fillRoundRect(1, 1, pw - 3, ph - 3, 10, 10);
                    g2.setColor(new Color(255, 255, 255, 100));
                    g2.setStroke(new BasicStroke(1f));
                    g2.drawRoundRect(1, 1, pw - 3, ph - 3, 10, 10);
                    g2.setColor(new Color(255, 255, 255, 60));
                    g2.fillRoundRect(3, 3, pw - 7, ph / 2, 8, 8);
                    g2.dispose();
                }
            }};
            fc[0].setSize(cw, ch);
            fc[0].setOpaque(false);
            int ox = (int) (Math.random() * 18 - 9);
            int oy = (int) (Math.random() * 14 - 7);
            fc[0].setLocation(from.x + ox, from.y + oy);
            glassPane.add(fc[0]);

            double sx = fc[0].getX(), sy = fc[0].getY();
            double ddx = to.x - sx, ddy = to.y - sy;
            int[] tick = {0};
            int frames = 14;

            javax.swing.Timer fly = new javax.swing.Timer(22, e2 -> {
                tick[0]++;
                double pct = Math.min((double) tick[0] / frames, 1.0);
                double ease = elasticOut(pct);
                double cx = (sx + to.x) / 2.0;
                double arcY = Math.min(sy, to.y) - 30;
                double oneMinusT = 1.0 - ease;
                int x = (int) (oneMinusT * oneMinusT * sx + 2 * oneMinusT * ease * cx + ease * ease * to.x);
                int y = (int) (oneMinusT * oneMinusT * sy + 2 * oneMinusT * ease * arcY + ease * ease * to.y);
                fc[0].setLocation(x, y);
                if (pct >= 1.0) {
                    ((javax.swing.Timer) e2.getSource()).stop();
                    glassPane.remove(fc[0]);
                    glassPane.repaint();
                    completed[0]++;
                    if (completed[0] >= count) onDone.run();
                }
            });
            fly.start();
            idx[0]++;
        });
        spawn.start();
    }

    // ── Screen shake effect ──
    static void playScreenShake(Game game, int intensity) {
        JComponent content = (JComponent) game.getContentPane();
        int originalX = content.getX();
        int originalY = content.getY();
        int[] tick = {0};
        int frames = 12;
        javax.swing.Timer t = new javax.swing.Timer(20, e -> {
            tick[0]++;
            double pct = (double) tick[0] / frames;
            double decay = 1.0 - pct;
            int dx = (int) (Math.sin(tick[0] * 2.5) * intensity * decay);
            int dy = (int) (Math.cos(tick[0] * 3.0) * intensity * decay * 0.6);
            content.setLocation(originalX + dx, originalY + dy);
            if (tick[0] >= frames) {
                content.setLocation(originalX, originalY);
                ((javax.swing.Timer) e.getSource()).stop();
            }
        });
        t.start();
    }

    // ── Particle burst effect ──
    static void playParticleBurst(Game game, Point center, Color color, int count) {
        JPanel glassPane = (JPanel) game.getGlassPane();
        java.util.List<JPanel> particles = new java.util.ArrayList<>();
        java.util.List<double[]> velocities = new java.util.ArrayList<>();

        for (int i = 0; i < count; i++) {
            double angle = Math.random() * Math.PI * 2;
            double speed = 2 + Math.random() * 4;
            double vx = Math.cos(angle) * speed;
            double vy = Math.sin(angle) * speed;
            velocities.add(new double[]{vx, vy});

            JPanel dot = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(color);
                    g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
                    g2.dispose();
                }
            };
            int size = 4 + (int) (Math.random() * 6);
            dot.setSize(size, size);
            dot.setLocation(center.x - size / 2, center.y - size / 2);
            dot.setOpaque(false);
            glassPane.add(dot);
            glassPane.setComponentZOrder(dot, 0);
            particles.add(dot);
        }

        int[] tick = {0};
        int frames = 24;
        javax.swing.Timer t = new javax.swing.Timer(25, e -> {
            tick[0]++;
            double pct = (double) tick[0] / frames;
            double alpha = 1.0 - pct;
            for (int i = 0; i < particles.size(); i++) {
                JPanel p = particles.get(i);
                double[] v = velocities.get(i);
                v[1] += 0.15;
                p.setLocation(p.getX() + (int) v[0], p.getY() + (int) v[1]);
                p.setSize(Math.max(1, (int) (p.getWidth() * (1.0 - pct * 0.5))),
                          Math.max(1, (int) (p.getHeight() * (1.0 - pct * 0.5))));
            }
            glassPane.repaint();
            if (tick[0] >= frames) {
                for (JPanel p : particles) glassPane.remove(p);
                glassPane.repaint();
                ((javax.swing.Timer) e.getSource()).stop();
            }
        });
        t.start();
    }

    // ── Easing functions ──
    private static double elasticOut(double t) {
        if (t <= 0) return 0;
        if (t >= 1) return 1;
        // Gentler elasticity: wider period (0.4) → fewer oscillations → less shake
        return Math.pow(2, -10 * t) * Math.sin((t - 0.075) * (2 * Math.PI) / 0.4) + 1;
    }

    private static double backIn(double t) {
        double s = 1.70158;
        return t * t * ((s + 1) * t - s);
    }

    private static double bounceOut(double t) {
        if (t < 1 / 2.75) return 7.5625 * t * t;
        else if (t < 2 / 2.75) { t -= 1.5 / 2.75; return 7.5625 * t * t + 0.75; }
        else if (t < 2.5 / 2.75) { t -= 2.25 / 2.75; return 7.5625 * t * t + 0.9375; }
        else { t -= 2.625 / 2.75; return 7.5625 * t * t + 0.984375; }
    }

    // Light overshoot easing — card pops in with a subtle bounce
    private static double easeOutBack(double t) {
        double s = 1.3;
        return 1 + (--t) * t * ((s + 1) * t + s);
    }

    // ── Card pop-in animation for play/judgment zones ──
    static void playCardPopIn(Game game, Card card, Point target, Runnable onDone) {
        JPanel glassPane = (JPanel) game.getGlassPane();
        int w = SW, h = SH;

        JPanel[] fc = {createFlyPanel(card, w, h)};
        int startW = (int)(w * 0.5);
        int startH = (int)(h * 0.5);
        fc[0].setSize(startW, startH);
        fc[0].setLocation(target.x + (w - startW) / 2, target.y + (h - startH) / 2);
        glassPane.add(fc[0]);
        glassPane.setComponentZOrder(fc[0], 0);

        int[] tick = {0};
        int frames = 10;

        javax.swing.Timer t = new javax.swing.Timer(16, e -> {
            tick[0]++;
            double pct = Math.min((double) tick[0] / frames, 1.0);
            double scale = 0.5 + 0.5 * easeOutBack(pct);
            int newW = (int)(w * scale);
            int newH = (int)(h * scale);
            fc[0].setSize(Math.max(1, newW), Math.max(1, newH));
            fc[0].setLocation(target.x + (w - newW) / 2, target.y + (h - newH) / 2);
            glassPane.repaint();

            if (pct >= 1.0) {
                ((javax.swing.Timer) e.getSource()).stop();
                glassPane.remove(fc[0]);
                glassPane.repaint();
                if (onDone != null) onDone.run();
            }
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

    private static Color blend(Color base, Color overlay, float ratio) {
        int r = (int) (base.getRed() * (1 - ratio) + overlay.getRed() * ratio);
        int g = (int) (base.getGreen() * (1 - ratio) + overlay.getGreen() * ratio);
        int b = (int) (base.getBlue() * (1 - ratio) + overlay.getBlue() * ratio);
        return new Color(Math.min(r, 255), Math.min(g, 255), Math.min(b, 255));
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
        JPanel targetPanel = ui.aiHandPanel;
        if (game.is1v2 && game.currentTurnTarget == 1 && ui instanceof GameUI1v2) {
            targetPanel = ((GameUI1v2) ui).ai2HandPanel;
        }
        Point p = targetPanel.getLocationOnScreen();
        SwingUtilities.convertPointFromScreen(p, game.getContentPane());
        p.x += targetPanel.getWidth() / 2 - 28;
        p.y += targetPanel.getHeight() / 2 - 41;
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
