import javax.swing.JFrame;
import java.awt.Dimension;

public class MainFrame extends JFrame {
    private static MainFrame instance;
    private MainMenuPanel mainMenuPanel;
    private UpgradePanel upgradePanel;

    public MainFrame() {
        instance = this;
        setTitle("Shooting Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainMenuPanel = new MainMenuPanel(this);
        mainMenuPanel.setPreferredSize(new Dimension(400, 800));
    	setContentPane(mainMenuPanel);
	    pack();
        setLocationRelativeTo(null);
        setVisible(true);
        upgradePanel = new UpgradePanel(this);
    }

    public static MainFrame getInstance() {
        return instance;
    }

    // 修正：重命名方法以匹配调用
    public void showScreen(String screenName) {
        switch (screenName) {
            case "Stage":
                StageModePanel stagePanel = new StageModePanel(mainMenuPanel, this);
                setContentPane(stagePanel);
                stagePanel.requestFocusInWindow(); // 确保面板获得焦点
                break;
            case "Infinite":
                InfiniteModePanel InfinitePanel = new InfiniteModePanel();
                InfinitePanel.setMainFrame(this); // 设置主框架引用
                setContentPane(InfinitePanel);
                InfinitePanel.requestFocusInWindow(); // 确保面板获得焦点
                break;
            case "Upgrade":
                setContentPane(upgradePanel);
                upgradePanel.requestFocusInWindow();
            break;

            case "Menu":
            /*case "Upgrade":
                setContentPane(new UpgradePanel(this));
                break;*/
            default:
                setContentPane(mainMenuPanel);
                break;
        }
        revalidate();
        repaint();
    }

    public static void main(String[] args) {
        // 在事件分发线程中创建GUI
        javax.swing.SwingUtilities.invokeLater(() -> new MainFrame());
    }
}