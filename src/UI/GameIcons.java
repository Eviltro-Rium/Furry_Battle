import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;

public class GameIcons {

    private static ImageIcon load(String path) {
        URL url = GameIcons.class.getResource(path);
        if (url == null) return null;
        return new ImageIcon(url);
    }

    static ImageIcon scaled(String path, int w, int h) {
        ImageIcon icon = load(path);
        if (icon == null) return null;
        Image img = icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }

    static ImageIcon cardBlack() { return scaled("/icons/card_icons/color_palette.png", 36, 36); }
    static ImageIcon cardPotion() { return scaled("/icons/card_icons/potion.png", 36, 36); }
    static ImageIcon cardDrawThree() { return scaled("/icons/card_icons/draw_cards.png", 36, 36); }
    static ImageIcon cardPurify() { return scaled("/icons/card_icons/purify.png", 36, 36); }
    static ImageIcon cardSuperPurify() { return scaled("/icons/card_icons/super_purify.png", 36, 36); }
    static ImageIcon cardSwapHand() { return scaled("/icons/card_icons/swap_cards.png", 36, 36); }
    static ImageIcon cardShuffleToDeck() { return scaled("/icons/card_icons/shuffle.png", 36, 36); }

    static ImageIcon cardBlackSmall() { return scaled("/icons/card_icons/color_palette.png", 14, 14); }
    static ImageIcon cardPotionSmall() { return scaled("/icons/card_icons/potion.png", 14, 14); }
    static ImageIcon cardDrawThreeSmall() { return scaled("/icons/card_icons/draw_cards.png", 14, 14); }
    static ImageIcon cardPurifySmall() { return scaled("/icons/card_icons/purify.png", 14, 14); }
    static ImageIcon cardSuperPurifySmall() { return scaled("/icons/card_icons/super_purify.png", 14, 14); }
    static ImageIcon cardSwapHandSmall() { return scaled("/icons/card_icons/swap_cards.png", 14, 14); }
    static ImageIcon cardShuffleToDeckSmall() { return scaled("/icons/card_icons/shuffle.png", 14, 14); }

    static ImageIcon buffBurn() { return scaled("/icons/buff_icons/burn.png", 36, 36); }
    static ImageIcon buffFreeze() { return scaled("/icons/buff_icons/freeze.png", 36, 36); }
    static ImageIcon buffBleed() { return scaled("/icons/buff_icons/bleed.png", 36, 36); }
    static ImageIcon buffGuard() { return scaled("/icons/buff_icons/guard.png", 36, 36); }

    static ImageIcon buffBloodthirsty() { return scaled("/icons/ui_icons/blood_thirsty.png", 36, 36); }

    static ImageIcon uiBattle() { return scaled("/icons/ui_icons/battle.png", 18, 18); }
    static ImageIcon uiBattleBig() { return scaled("/icons/ui_icons/battle.png", 32, 32); }
    static ImageIcon uiShield() { return scaled("/icons/ui_icons/shield.png", 18, 18); }
    static ImageIcon uiSword() { return scaled("/icons/ui_icons/sword.png", 18, 18); }
    static ImageIcon uiTrash() { return scaled("/icons/ui_icons/trash.png", 18, 18); }
    static ImageIcon uiSkip() { return scaled("/icons/ui_icons/skip.png", 18, 18); }
    static ImageIcon uiTick() { return scaled("/icons/ui_icons/tick.png", 18, 18); }
    static ImageIcon uiSparkling() { return scaled("/icons/ui_icons/sparkling.png", 18, 18); }
    static ImageIcon uiHand() { return scaled("/icons/ui_icons/hand.png", 18, 18); }
    static ImageIcon uiJudge() { return scaled("/icons/ui_icons/judge.png", 18, 18); }
    static ImageIcon uiRestart() { return scaled("/icons/ui_icons/restart.png", 18, 18); }
    static ImageIcon uiJudgeBig() { return scaled("/icons/ui_icons/judge.png", 20, 20); }

    static JLabel makeIconLabel(ImageIcon icon) {
        if (icon == null) return new JLabel("?");
        JLabel lbl = new JLabel(icon);
        lbl.setVerticalTextPosition(SwingConstants.CENTER);
        lbl.setHorizontalTextPosition(SwingConstants.RIGHT);
        return lbl;
    }

    static JLabel makeBuffLabel(ImageIcon icon, int stacks, Color numColor) {
        if (icon == null) return new JLabel("?");
        int iw = icon.getIconWidth(), ih = icon.getIconHeight();
        BufferedImage buf = new BufferedImage(iw, ih, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = buf.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        icon.paintIcon(null, g2, 0, 0);
        String num = String.valueOf(stacks);
        int fontSize = iw > 24 ? 13 : 10;
        g2.setFont(new Font("Arial", Font.BOLD, fontSize));
        FontMetrics fm = g2.getFontMetrics();
        int tx = iw - fm.stringWidth(num) - 1;
        int ty = fm.getAscent() - 1;
        g2.setColor(new Color(0, 0, 0, 180));
        g2.drawString(num, tx + 1, ty + 1);
        g2.setColor(numColor);
        g2.drawString(num, tx, ty);
        g2.dispose();
        JLabel lbl = new JLabel(new ImageIcon(buf));
        return lbl;
    }
}