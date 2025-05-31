import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MainFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private StageModePanel stageMode;
    private InfiniteModePanel infiniteMode;

    public MainFrame() {
        setTitle("射擊遊戲");
        setSize(320, 480);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // 主畫面與遊戲模式面板
        MainMenuPanel menuPanel = new MainMenuPanel(this);
        infiniteMode = new InfiniteModePanel();
	infiniteMode.setName("Infinite");
        
        // 先只添加主選單，遊戲模式面板按需創建
        mainPanel.add(menuPanel, "Menu");
        mainPanel.add(infiniteMode, "Infinite");

        add(mainPanel);
        
        // 一開始顯示主選單
        cardLayout.show(mainPanel, "Menu");
        setVisible(true);
        
        // 確保主選單獲得焦點
        SwingUtilities.invokeLater(() -> {
            menuPanel.requestFocusInWindow();
            System.out.println("Main menu displayed");
        });
    }
    
    private void createStageModePanel() {
        // 移除舊的面板（如果存在）
        if (stageMode != null) {
            mainPanel.remove(stageMode);
        }
        
        // 創建新的關卡模式面板
        stageMode = new StageModePanel();
        stageMode.setName("Stage");
        stageMode.setMainFrame(this);
        mainPanel.add(stageMode, "Stage");
        
        System.out.println("New StageModePanel created");
    }

    public void showScreen(String name) {
        // 如果要顯示關卡模式，創建新的面板實例
        if (name.equals("Stage")) {
            createStageModePanel();
            mainPanel.revalidate();
        }
        
    	cardLayout.show(mainPanel, name);
    	Component component = mainPanel.getComponent(getComponentIndexByName(name));
    	
    	// 確保組件獲得焦點
    	SwingUtilities.invokeLater(() -> {
    	    component.requestFocusInWindow();
    	    System.out.println("Focus requested for: " + name);
    	});
    }

    private int getComponentIndexByName(String name) {
    	for (int i = 0; i < mainPanel.getComponentCount(); i++) {
        	Component comp = mainPanel.getComponent(i);
        	if (comp.getName() != null && comp.getName().equals(name))
            	return i;
    	}
    	return 0;
    }

    public static void main(String[] args) {
        new MainFrame();
    }
}