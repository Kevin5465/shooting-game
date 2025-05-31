import javax.swing.*;

public class GameFrame extends JFrame {
    public GameFrame() {
        this.add(new GamePanel());
        this.setTitle("宇宙射擊遊戲");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
        this.pack();  // 自動調整視窗大小以符合內容
        this.setVisible(true);
        this.setLocationRelativeTo(null);  // 螢幕中央顯示
    }
}