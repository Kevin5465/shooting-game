import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MainFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;

    public MainFrame() {
        setTitle("射擊遊戲");
        setSize(320, 480);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // 主畫面與遊戲模式面板
        MainMenuPanel menuPanel = new MainMenuPanel(this);
        InfiniteModePanel infiniteMode = new InfiniteModePanel();  // 原 GamePanel 重命名
	infiniteMode.setName("Infinite");
        StageModePanel stageMode = new StageModePanel();            // 關卡模式（稍後建立）
	stageMode.setName("Stage");
        mainPanel.add(menuPanel, "Menu");
        mainPanel.add(infiniteMode, "Infinite");
        mainPanel.add(stageMode, "Stage");

        add(mainPanel);
        setVisible(true);
    }

    public void showScreen(String name) {
    	cardLayout.show(mainPanel, name);
    	mainPanel.getComponent(getComponentIndexByName(name)).requestFocusInWindow(); // 強制 focus
}

    private int getComponentIndexByName(String name) {
    	for (int i = 0; i < mainPanel.getComponentCount(); i++) {
        	if (mainPanel.getComponent(i).getName() != null && mainPanel.getComponent(i).getName().equals(name))
            	return i;
    	}
    	return 0;
    }


    public static void main(String[] args) {
        new MainFrame();
    }
}
