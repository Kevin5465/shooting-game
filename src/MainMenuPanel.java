import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class MainMenuPanel extends JPanel {
    private MainFrame parentFrame;
    private JButton infiniteModeButton;
    private JButton stageModeButton;
    private JButton settingsButton;
    private JButton exitButton;

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
        titleLabel.setBounds(0, 30, 400, 40);
        add(titleLabel);

        // 按鈕配置
        int buttonWidth = 200;
        int buttonHeight = 50;
        int startY = 120;
        int gap = 20;
        int centerX = (400 - buttonWidth) / 2;

        infiniteModeButton = createButton("無限模式", buttonWidth, buttonHeight);
        infiniteModeButton.setBounds(centerX, startY, buttonWidth, buttonHeight);
        add(infiniteModeButton);

        stageModeButton = createButton("關卡模式", buttonWidth, buttonHeight);
        stageModeButton.setBounds(centerX, startY + buttonHeight + gap, buttonWidth, buttonHeight);
        add(stageModeButton);

        settingsButton = createButton("排行榜", buttonWidth, buttonHeight);
        settingsButton.setBounds(centerX, startY + (buttonHeight + gap) * 2, buttonWidth, buttonHeight);
        add(settingsButton);

        exitButton = createButton("退出", buttonWidth, buttonHeight);
        exitButton.setBounds(centerX, startY + (buttonHeight + gap) * 3, buttonWidth, buttonHeight);
        add(exitButton);

        setupEventListeners();
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
        settingsButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(parentFrame, "設置功能尚未實現");
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

        g2d.dispose();
    }
}
