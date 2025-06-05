import javax.swing.JFrame;
import java.awt.Dimension;
import javax.swing.JPanel;

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

    public void showScreen(String screenName) {
	JPanel panelToShow;
        switch (screenName) {
            case "Stage":
                panelToShow = new StageModePanel(this);
                break;
            case "Infinite":
                panelToShow = new InfiniteModePanel(mainMenuPanel, this);
                break;
            case "Upgrade":
                panelToShow = upgradePanel;
                break;
            case "Menu":
            default:
                panelToShow = mainMenuPanel;
                break;
	}
        setContentPane(panelToShow);
        panelToShow.requestFocusInWindow();
        revalidate();
        repaint();
    }

    public static void main(String[] args) {
        // 在事件分发线程中创建GUI
        javax.swing.SwingUtilities.invokeLater(() -> new MainFrame());
    }
}
