import javax.swing.JFrame;
import java.awt.Dimension;

public class MainFrame extends JFrame {
    private static MainFrame instance;

    public MainFrame() {
        instance = this;
        setTitle("Shooting Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

   	MainMenuPanel menu = new MainMenuPanel(this);
   	menu.setPreferredSize(new Dimension(400, 800));
    	setContentPane(menu);
	pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static MainFrame getInstance() {
        return instance;
    }

    // 修正：重命名方法以匹配调用
    public void showScreen(String screenName) {
        switch (screenName) {
            case "Stage":
                StageModePanel stagePanel = new StageModePanel();
                stagePanel.setMainFrame(this); // 设置主框架引用
                setContentPane(stagePanel);
                stagePanel.requestFocusInWindow(); // 确保面板获得焦点
                break;
            case "Infinite":
                InfiniteModePanel InfinitePanel = new InfiniteModePanel();
                InfinitePanel.setMainFrame(this); // 设置主框架引用
                setContentPane(InfinitePanel);
                InfinitePanel.requestFocusInWindow(); // 确保面板获得焦点
                break;
            case "Menu":
            default:
                setContentPane(new MainMenuPanel(this));
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