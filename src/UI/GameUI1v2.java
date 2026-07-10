import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class GameUI1v2 extends GameUI {

    JPanel ai2HandPanel;
    HpBar ai2HpBar;
    JLabel ai2BurnLabel;

    @Override
    void buildUI(Game game) {
        game.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        game.setMinimumSize(new Dimension(1100, 800));
        game.setResizable(true);

        JPanel rp = new GradientPanel(new Color(255, 240, 210), new Color(255, 220, 180));
        rp.setLayout(new BorderLayout(12, 10));
        rp.setBorder(BorderFactory.createEmptyBorder(12, 14, 14, 14));

        rp.add(buildTopPanel(), BorderLayout.NORTH);
        rp.add(buildCenterPanel1v2(), BorderLayout.CENTER);
        rp.add(buildBottomPanel(game), BorderLayout.SOUTH);

        this.rootPanel = rp;
        game.setContentPane(rp);

        JPanel glassPane = new JPanel(null);
        glassPane.setOpaque(false);
        game.setGlassPane(glassPane);
        glassPane.setVisible(true);
    }

    private JPanel buildCenterPanel1v2() {
        JPanel centerPanel = new JPanel(new BorderLayout(14, 8));
        centerPanel.setOpaque(false);

        JPanel discardArea = new JPanel(new BorderLayout(4, 4));
        discardArea.setOpaque(false);
        JLabel discardTopLabel = new JLabel("弃牌库顶", SwingConstants.CENTER);
        discardTopLabel.setFont(new Font("微软雅黑", Font.BOLD, 12));
        discardTopLabel.setForeground(new Color(180, 60, 40));
        discardArea.add(discardTopLabel, BorderLayout.NORTH);

        discardCardPanel = new JPanel();
        discardCardPanel.setOpaque(false);
        discardCardPanel.setPreferredSize(new Dimension(100, 140));
        discardArea.add(discardCardPanel, BorderLayout.CENTER);
        centerPanel.add(discardArea, BorderLayout.WEST);

        JPanel aiAreaWrap = new JPanel(new GridLayout(2, 1, 4, 6));
        aiAreaWrap.setOpaque(false);

        aiHpBar = new HpBar();
        aiHandPanel = new JPanel();
        aiHandPanel.setLayout(new WrapLayout(FlowLayout.LEFT, 6, 4));
        aiHandPanel.setBackground(PANEL_BG);
        aiHandPanel.setBorder(makeGlowBorder("AI1 手牌", new Color(0, 120, 220)));
        aiHandPanel.setPreferredSize(new Dimension(0, 100));

        aiBurnLabel = new JLabel();
        aiBurnLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        aiBurnLabel.setForeground(new Color(255, 80, 0));
        aiBurnLabel.setVisible(false);

        JPanel ai1Wrap = new JPanel(new BorderLayout(2, 2));
        ai1Wrap.setOpaque(false);
        ai1Wrap.add(aiHpBar, BorderLayout.NORTH);
        ai1Wrap.add(aiHandPanel, BorderLayout.CENTER);

        ai2HpBar = new HpBar();
        ai2HandPanel = new JPanel();
        ai2HandPanel.setLayout(new WrapLayout(FlowLayout.LEFT, 6, 4));
        ai2HandPanel.setBackground(PANEL_BG);
        ai2HandPanel.setBorder(makeGlowBorder("AI2 手牌", new Color(160, 0, 160)));
        ai2HandPanel.setPreferredSize(new Dimension(0, 100));

        ai2BurnLabel = new JLabel();
        ai2BurnLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        ai2BurnLabel.setForeground(new Color(255, 80, 0));
        ai2BurnLabel.setVisible(false);

        JPanel ai2Wrap = new JPanel(new BorderLayout(2, 2));
        ai2Wrap.setOpaque(false);
        ai2Wrap.add(ai2HpBar, BorderLayout.NORTH);
        ai2Wrap.add(ai2HandPanel, BorderLayout.CENTER);

        aiAreaWrap.add(ai1Wrap);
        aiAreaWrap.add(ai2Wrap);

        aiAttackPanel = new JPanel();
        aiAttackPanel.setLayout(new BorderLayout(4, 2));
        aiAttackPanel.setBackground(new Color(255, 240, 230));
        aiAttackPanel.setBorder(makeGlowBorder("出牌区", new Color(220, 80, 60)));
        aiAttackPanel.setPreferredSize(new Dimension(0, 180));

        JPanel atkZone = new JPanel(new BorderLayout(2, 2));
        atkZone.setOpaque(false);
        JLabel atkZoneLabel = new JLabel("进攻", SwingConstants.CENTER);
        atkZoneLabel.setFont(new Font("微软雅黑", Font.BOLD, 11));
        atkZoneLabel.setForeground(new Color(220, 60, 40));
        this.atkCardRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
        this.atkCardRow.setOpaque(false);
        JLabel emptyAtk = new JLabel("等待出牌", SwingConstants.CENTER);
        emptyAtk.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        emptyAtk.setForeground(new Color(200, 160, 140));
        this.atkCardRow.add(emptyAtk);
        atkZone.add(atkZoneLabel, BorderLayout.NORTH);
        atkZone.add(this.atkCardRow, BorderLayout.CENTER);

        JPanel defZone = new JPanel(new BorderLayout(2, 2));
        defZone.setOpaque(false);
        JLabel defZoneLabel = new JLabel("防御", SwingConstants.CENTER);
        defZoneLabel.setFont(new Font("微软雅黑", Font.BOLD, 11));
        defZoneLabel.setForeground(new Color(0, 100, 200));
        this.defCardRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
        this.defCardRow.setOpaque(false);
        JLabel emptyDef = new JLabel("等待防御", SwingConstants.CENTER);
        emptyDef.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        emptyDef.setForeground(new Color(160, 180, 200));
        this.defCardRow.add(emptyDef);
        defZone.add(defZoneLabel, BorderLayout.NORTH);
        defZone.add(this.defCardRow, BorderLayout.CENTER);

        JPanel playZone = new JPanel(new GridLayout(1, 2, 8, 0));
        playZone.setOpaque(false);
        playZone.add(atkZone);
        playZone.add(defZone);

        attackDescLabel = new JLabel("", SwingConstants.CENTER);
        attackDescLabel.setFont(new Font("微软雅黑", Font.BOLD, 13));
        attackDescLabel.setForeground(new Color(200, 60, 40));

        defendDescLabel = new JLabel("", SwingConstants.CENTER);
        defendDescLabel.setFont(new Font("微软雅黑", Font.BOLD, 13));
        defendDescLabel.setForeground(new Color(0, 100, 200));

        JPanel descRow = new JPanel(new GridLayout(1, 2, 8, 0));
        descRow.setOpaque(false);
        descRow.add(attackDescLabel);
        descRow.add(defendDescLabel);

        aiAttackPanel.add(playZone, BorderLayout.CENTER);
        aiAttackPanel.add(descRow, BorderLayout.SOUTH);

        aiRevealPanel = new JPanel();
        aiRevealPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 4, 4));
        aiRevealPanel.setBackground(new Color(230, 240, 255));
        aiRevealPanel.setBorder(makeGlowBorder("判定", new Color(60, 100, 200)));
        aiRevealPanel.setPreferredSize(new Dimension(120, 0));

        JPanel mainArea = new JPanel(new BorderLayout(8, 4));
        mainArea.setOpaque(false);
        mainArea.add(aiAreaWrap, BorderLayout.NORTH);
        mainArea.add(aiAttackPanel, BorderLayout.CENTER);

        JPanel rightArea = new JPanel(new BorderLayout(8, 0));
        rightArea.setOpaque(false);
        rightArea.add(mainArea, BorderLayout.CENTER);
        rightArea.add(aiRevealPanel, BorderLayout.EAST);

        centerPanel.add(rightArea, BorderLayout.CENTER);

        return centerPanel;
    }
}