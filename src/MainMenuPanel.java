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
    private String playerName = "player"; // 玩家名稱
    private Image playerAvatar; // 玩家頭像
    private BufferedImage coinIcon; // 金幣圖像



    public MainMenuPanel(MainFrame parent) {
        this.parentFrame = parent;
        setName("Menu");
        setBackground(new Color(15, 17, 26));
        setLayout(null);
        

        // 建立標題
        JLabel titleLabel = new JLabel("射擊遊戲");
        titleLabel.setFont(new Font("Dialog", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBounds(0, 100, 400, 40);
        add(titleLabel);

        // 按鈕配置
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

        

        upgradeButton=createButton("升級", buttonWidth, buttonHeight);
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
    int boxWidth = 120; // 資料框寬度
    int boxHeight = 50; // 資料框高度
    int x = 0; // 資料框左上角 X 坐標
    int y = 0; // 資料框左上角 Y 坐標

    try
    {
        playerAvatar = ImageIO.read(new File("image/player2.png"));
    } catch (IOException e) 
    {
        e.printStackTrace();
    }

    
    // 資料框背景
    g.setColor(new Color(125,111,52, 130)); // 半透明黑色
    g.fillRect(x, y, boxWidth, boxHeight);

    // 玩家頭像
    g.setColor(Color.WHITE); // 線框顏色
    g.drawRect(x + 10, y + 10, 30, 30);
    g.setColor(Color.BLACK); // 設定背景為白色
    g.fillRect(x + 11, y + 11, 29, 28);
    if (playerAvatar != null) {
        g.drawImage(playerAvatar, x + 10, y + 10, 30, 30, this);
    }

    g.setColor(Color.WHITE);
    g.setFont(new Font("Dialog", Font.PLAIN, 16));
    
    // 玩家名稱
    g.setColor(Color.WHITE);
    g.setFont(new Font("Arial", Font.BOLD, 12));
    g.drawString(playerName, x + 55, y + 17);

    // 玩家等級
    g.setFont(new Font("Arial", Font.PLAIN, 12));
    g.drawString("Lv. " + PlayerData.level, x + 60, y + 30);

    // 經驗值進度條
    int barWidth = 60; // 進度條寬度
    int barHeight = 10; // 進度條高度
    int barX = x + 50; // 進度條 X 坐標
    int barY = y + 35; // 進度條 Y 坐標


    // 經驗值條背景
    g.setColor(Color.GRAY);
    g.fillRect(barX, barY, barWidth, barHeight);

    // 經驗值條進度
    int currentBarWidth = (int) ((double) PlayerData.currentExp / PlayerData.maxExp * barWidth);
    g.setColor(Color.BLUE);
    g.fillRect(barX, barY, currentBarWidth, barHeight);

    // 經驗值文字
    g.setColor(Color.WHITE);
    g.setFont(new Font("Arial", Font.PLAIN, 10));
    g.drawString(PlayerData.currentExp + " / " + PlayerData.maxExp, barX + 5, barY + 9);


    int coinBoxX = x + boxWidth +50; // 緊接在資料框右邊
    int coinBoxY = y + 9;
    int coinBoxWidth = 74;
    int coinBoxHeight = 18;

    // 金幣框背景
    g.setColor(new Color(125,111,52, 150));
    g.fillRect(coinBoxX, coinBoxY, coinBoxWidth, coinBoxHeight);
    g.setColor(Color.white);
    g.drawRect(coinBoxX, coinBoxY, coinBoxWidth, coinBoxHeight);
    try {
    coinIcon = ImageIO.read(new File("image/coin.png")); // 請放 coin.png 到專案資料夾中
} catch (IOException e) {
    e.printStackTrace();
}

    // 金幣圖示
    if (coinIcon != null) {
        g.drawImage(coinIcon, coinBoxX-23, coinBoxY, 20, 20, this);
    }

    // 金幣數字
    g.setColor(Color.WHITE);
    g.setFont(new Font("Arial", Font.PLAIN, 11));
    String coinText = " " + PlayerData.coins;
    FontMetrics fm = g.getFontMetrics();
    int textWidth = fm.stringWidth(coinText);
    int textHeight = fm.getAscent(); // 文字高度的推薦參考
    // 水平置中
    int textX = coinBoxX + (coinBoxWidth - textWidth) / 2-1;
    // 垂直位置微調（讓文字看起來居中）
    int textY = coinBoxY + (coinBoxHeight + textHeight) / 2 -1;
    g.drawString(coinText, textX, textY);
}
    private JButton createButton(String text, int width, int height) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                

                if (getModel().isPressed()) {
                    g2d.setPaint(new GradientPaint(
                        0, 0, new Color(30, 70, 150),
                        0, height, new Color(60, 100, 180)
                    ));
                } else if (getModel().isRollover()) {
                    g2d.setPaint(new GradientPaint(
                        0, 0, new Color(70, 120, 230),
                        0, height, new Color(100, 150, 255)
                    ));
                } else {
                    g2d.setPaint(new GradientPaint(
                        0, 0, new Color(50, 100, 200),
                        0, height, new Color(70, 120, 220)
                    ));
                }

                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

                g2d.setColor(new Color(120, 160, 255, 150));
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);

                FontMetrics fm = g2d.getFontMetrics();
                Rectangle stringBounds = fm.getStringBounds(text, g2d).getBounds();

                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Dialog", Font.BOLD, 18));
                g2d.drawString(text,
                    (getWidth() - stringBounds.width) / 2,
                    (getHeight() - stringBounds.height) / 2 + fm.getAscent());

                g2d.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(width, height);
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

        settingsButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(parentFrame, "最高分數:"+PlayerData.getHighScore());
        });
        exitButton.addActionListener(e -> System.exit(0));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();

        g2d.setColor(new Color(15, 17, 26));
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // 星星背景
        g2d.setColor(new Color(255, 255, 255, 60));
        Random random = new Random(12345);
        for (int i = 0; i < 40; i++) {
            int x = random.nextInt(getWidth());
            int y = random.nextInt(getHeight());
            int size = random.nextInt(2) + 1;
            g2d.fillOval(x, y, size, size);
        }
        drawPlayerInfo(g);
        g2d.dispose();
    }
}
