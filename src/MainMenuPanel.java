import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MainMenuPanel extends JPanel {
    private MainFrame parentFrame;
    private JButton infiniteModeButton;
    private JButton stageModeButton;
    private JButton settingsButton;
    private JButton exitButton;
    
    public MainMenuPanel(MainFrame parent) {
        this.parentFrame = parent;
        setName("Menu");
        setBackground(new Color(15, 17, 26)); // 深色背景，接近截圖中的顏色
        setLayout(null); // 使用絕對定位以精確控制按鈕位置
        
        // 創建遊戲標題
        JLabel titleLabel = new JLabel("射擊遊戲");
        titleLabel.setFont(new Font("微軟正黑體", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBounds(0, 30, 320, 40);
        add(titleLabel);
        
        // 創建按鈕
        int buttonWidth = 200;
        int buttonHeight = 50;
        int startY = 120;
        int gap = 20;
        
        infiniteModeButton = createButton("無限模式", buttonWidth, buttonHeight);
        infiniteModeButton.setBounds((300 - buttonWidth) / 2, startY, buttonWidth, buttonHeight);
        add(infiniteModeButton);
        
        stageModeButton = createButton("關卡模式", buttonWidth, buttonHeight);
        stageModeButton.setBounds((300 - buttonWidth) / 2, startY + buttonHeight + gap, buttonWidth, buttonHeight);
        add(stageModeButton);
        
        settingsButton = createButton("排行榜", buttonWidth, buttonHeight);
        settingsButton.setBounds((300 - buttonWidth) / 2, startY + (buttonHeight + gap) * 2, buttonWidth, buttonHeight);
        add(settingsButton);
        
        exitButton = createButton("退出", buttonWidth, buttonHeight);
        exitButton.setBounds((300 - buttonWidth) / 2, startY + (buttonHeight + gap) * 3, buttonWidth, buttonHeight);
        add(exitButton);
        
        // 設置按鈕事件
        setupEventListeners();
    }
    
    private JButton createButton(String text, int width, int height) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // 創建按鈕漸變背景
                if (getModel().isPressed()) {
                    // 按下時的顏色
                    g2d.setPaint(new GradientPaint(
                        0, 0, new Color(30, 70, 150),
                        0, height, new Color(60, 100, 180)
                    ));
                } else if (getModel().isRollover()) {
                    // 懸停時的顏色
                    g2d.setPaint(new GradientPaint(
                        0, 0, new Color(70, 120, 230),
                        0, height, new Color(100, 150, 255)
                    ));
                } else {
                    // 正常狀態的顏色 - 接近截圖中的藍色
                    g2d.setPaint(new GradientPaint(
                        0, 0, new Color(50, 100, 200),
                        0, height, new Color(70, 120, 220)
                    ));
                }
                
                // 繪製圓角矩形按鈕
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                
                // 繪製按鈕邊框
                g2d.setColor(new Color(120, 160, 255, 150));
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                
                // 繪製文字
                FontMetrics fm = g2d.getFontMetrics();
                Rectangle stringBounds = fm.getStringBounds(text, g2d).getBounds();
                
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("微軟正黑體", Font.BOLD, 18));
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
        
        // 設置按鈕屬性
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
        
        // 繪製背景
        g2d.setColor(new Color(15, 17, 26)); // 深色背景，接近截圖中的顏色
        g2d.fillRect(0, 0, getWidth(), getHeight());
        
        // 繪製簡單的小星星作為背景裝飾
        g2d.setColor(new Color(255, 255, 255, 60));
        for (int i = 0; i < 40; i++) {
            int x = (int)(Math.random() * getWidth());
            int y = (int)(Math.random() * getHeight());
            int size = (int)(Math.random() * 2) + 1;
            g2d.fillOval(x, y, size, size);
        }
        
        g2d.dispose();
    }
}