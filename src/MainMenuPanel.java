import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MainMenuPanel extends JPanel {
    public MainMenuPanel(MainFrame parent) {
        setLayout(new GridBagLayout());
        JButton infiniteBtn = new JButton("無限模式");
        JButton stageBtn = new JButton("關卡模式");

        infiniteBtn.addActionListener(e -> parent.showScreen("Infinite"));
        stageBtn.addActionListener(e -> parent.showScreen("Stage"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);
        gbc.gridy = 0;
        add(infiniteBtn, gbc);
        gbc.gridy = 1;
        add(stageBtn, gbc);
    }
}
