import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.awt.image.BufferedImage;

public class MainMenuPanel extends JPanel {
    private MainFrame parentFrame;
    private JButton infiniteModeButton;
    private JButton stageModeButton;
    private JButton settingsButton;
    private JButton exitButton;
    private JButton upgradeButton;
    private String playerName = "player";
    private Image playerAvatar;
    private BufferedImage coinIcon;

    public MainMenuPanel(MainFrame parent) {
        this.parentFrame = parent;
        setName("Menu");
        setBackground(new Color(15, 17, 26));
        setLayout(null);

        JLabel titleLabel = new JLabel("射擊遊戲");
        titleLabel.setFont(new Font("Dialog", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBounds(0, 100, 400, 40);
        add(titleLabel);

        int buttonWidth = 200;
        int buttonHeight = 50;
        int startY = 160;
        int gap = 20;
        int centerX = (400 - buttonWidth) / 2;

        stageModeButton = createButton("關卡模式", buttonWidth, buttonHeight);
        stageModeButton.setBounds(centerX, startY, buttonWidth, buttonHeight);
        add(stageModeButton);

        infiniteModeButton = createButton("無限模式", buttonWidth, buttonHeight);
        infiniteModeButton.setBounds(centerX, startY + buttonHeight + gap, buttonWidth, buttonHeight);
        add(infiniteModeButton);

        upgradeButton = createButton("升級", buttonWidth, buttonHeight);
        upgradeButton.setBounds(centerX, startY + (buttonHeight + gap) * 2, buttonWidth, buttonHeight);
        add(upgradeButton);

        settingsButton = createButton("歷史紀錄", buttonWidth, buttonHeight);
        settingsButton.setBounds(centerX, startY + (buttonHeight + gap) * 3, buttonWidth, buttonHeight);
        add(settingsButton);

        exitButton = createButton("退出", buttonWidth, buttonHeight);
        exitButton.setBounds(centerX, startY + (buttonHeight + gap) * 4, buttonWidth, buttonHeight);
        add(exitButton);

        setupEventListeners();
    }

    public void update() {
        repaint();
    }

    private void drawPlayerInfo(Graphics g) {
        int boxWidth = 120;
        int boxHeight = 50;
        int x = 0;
        int y = 0;

        try {
            playerAvatar = ImageIO.read(new File("image/player2.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        g.setColor(new Color(125, 111, 52, 130));
        g.fillRect(x, y, boxWidth, boxHeight);

        g.setColor(Color.WHITE);
        g.drawRect(x + 10, y + 10, 30, 30);
        g.setColor(Color.BLACK);
        g.fillRect(x + 11, y + 11, 29, 28);
        if (playerAvatar != null) {
            g.drawImage(playerAvatar, x + 10, y + 10, 30, 30, this);
        }

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 12));
        g.drawString(playerName, x + 55, y + 17);

        g.setFont(new Font("Arial", Font.PLAIN, 12));
        g.drawString("Lv. " + PlayerData.level, x + 60, y + 30);

        int barWidth = 60;
        int barHeight = 10;
        int barX = x + 50;
        int barY = y + 35;

        g.setColor(Color.GRAY);
        g.fillRect(barX, barY, barWidth, barHeight);

        int currentBarWidth = (int) ((double) PlayerData.currentExp / PlayerData.maxExp * barWidth);
        g.setColor(Color.BLUE);
        g.fillRect(barX, barY, currentBarWidth, barHeight);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 10));
        g.drawString(PlayerData.currentExp + " / " + PlayerData.maxExp, barX + 5, barY + 9);

        int coinBoxX = x + boxWidth + 50;
        int coinBoxY = y + 9;
        int coinBoxWidth = 74;
        int coinBoxHeight = 18;

        g.setColor(new Color(125, 111, 52, 150));
        g.fillRect(coinBoxX, coinBoxY, coinBoxWidth, coinBoxHeight);
        g.setColor(Color.white);
        g.drawRect(coinBoxX, coinBoxY, coinBoxWidth, coinBoxHeight);

        try {
            coinIcon = ImageIO.read(new File("image/coin.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (coinIcon != null) {
            g.drawImage(coinIcon, coinBoxX - 23, coinBoxY, 20, 20, this);
        }

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 11));
        String coinText = " " + PlayerData.coins;
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(coinText);
        int textHeight = fm.getAscent();
        int textX = coinBoxX + (coinBoxWidth - textWidth) / 2 - 1;
        int textY = coinBoxY + (coinBoxHeight + textHeight) / 2 - 1;
        g.drawString(coinText, textX, textY);
    }

    private JButton createButton(String text, int width, int height) {
        final String buttonText = text;
        final int buttonWidth = width;
        final int buttonHeight = height;

        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isPressed()) {
                    g2d.setPaint(new GradientPaint(0, 0, new Color(30, 70, 150), 0, getHeight(), new Color(60, 100, 180)));
                } else if (getModel().isRollover()) {
                    g2d.setPaint(new GradientPaint(0, 0, new Color(70, 120, 230), 0, getHeight(), new Color(100, 150, 255)));
                } else {
                    g2d.setPaint(new GradientPaint(0, 0, new Color(50, 100, 200), 0, getHeight(), new Color(70, 120, 220)));
                }

                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

                g2d.setColor(new Color(120, 160, 255, 150));
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);

                g2d.setFont(new Font("Dialog", Font.BOLD, 18));
                FontMetrics fm = g2d.getFontMetrics();
                Rectangle stringBounds = fm.getStringBounds(buttonText, g2d).getBounds();
                int textX = (getWidth() - stringBounds.width) / 2;
                int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;

                g2d.setColor(Color.WHITE);
                g2d.drawString(buttonText, textX, textY);

                g2d.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(buttonWidth, buttonHeight);
            }
        };

        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void setupEventListeners() {
        infiniteModeButton.addActionListener(e -> parentFrame.showScreen("Infinite"));
        stageModeButton.addActionListener(e -> parentFrame.showScreen("Stage"));
        upgradeButton.addActionListener(e -> parentFrame.showScreen("Upgrade"));
        settingsButton.addActionListener(e -> JOptionPane.showMessageDialog(parentFrame, "最高分數:" + PlayerData.getHighScore()));
        exitButton.addActionListener(e -> System.exit(0));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // 別忘了這行
        drawPlayerInfo(g); // 顯示玩家資訊
    }
}
