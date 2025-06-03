import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class UpgradePanel extends JPanel {
    private MainFrame mainFrame;

    private JButton backButton;
    private JButton upgradeAttackButton;
    private JButton upgradeattackSpeedButton;
    private JButton upgradedefenseButton;
    private JButton upgradehpButton;

    

    public UpgradePanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(null);
        setBackground(Color.BLACK);

        backButton = new JButton("返回主選單");
        backButton.setBounds(130, 700, 140, 40);
        backButton.addActionListener(e -> mainFrame.showScreen("Menu"));
        add(backButton);

        upgradeAttackButton = new JButton("升級攻擊 (100金幣)");
        upgradeAttackButton.setBounds(100, 200, 200, 50);
        upgradeAttackButton.addActionListener(e -> upgradeAttack());
        add(upgradeAttackButton);

        upgradeattackSpeedButton = new JButton("升級速度 (100金幣)");
        upgradeattackSpeedButton.setBounds(100, 300, 200, 50);
        upgradeattackSpeedButton.addActionListener(e -> upgradeattackSpeed());
        add(upgradeattackSpeedButton);

        upgradedefenseButton = new JButton("升級防禦 (100金幣)");
        upgradedefenseButton.setBounds(100, 400, 200, 50);
        upgradedefenseButton.addActionListener(e -> upgradedefense());
        add(upgradedefenseButton);

        upgradehpButton = new JButton("升級血量 (100金幣)");
        upgradehpButton.setBounds(100, 500, 200, 50);
        upgradehpButton.addActionListener(e -> upgradehealth());
        add(upgradehpButton);
    }

    private void upgradeAttack() {
        if (PlayerData.coins >= 100) {
            PlayerData.coins -= 100;
            PlayerData.atkLevel++;
            JOptionPane.showMessageDialog(this, "攻擊力升級到 Lv." + PlayerData.atkLevel);
            repaint();
        } else {
            JOptionPane.showMessageDialog(this, "金幣不足！");
        }
    }

    private void upgradeattackSpeed() {
        if (PlayerData.coins >= 100) {
            PlayerData.coins -= 100;
            PlayerData.atkspeedLevel++;
            JOptionPane.showMessageDialog(this, "攻速升級到 Lv." + PlayerData.atkspeedLevel);
            repaint();
        } else {
            JOptionPane.showMessageDialog(this, "金幣不足！");
        }
    
    }
    private void upgradedefense(){
        if (PlayerData.coins >= 100) {
            PlayerData.coins -= 100;
            PlayerData.defenseLevel++;
            JOptionPane.showMessageDialog(this, "防禦升級到 Lv." + PlayerData.defenseLevel);
            repaint();
        } else {
            JOptionPane.showMessageDialog(this, "金幣不足！");
        }
    }
    private void upgradehealth(){
        if (PlayerData.coins >= 100) {
            PlayerData.coins -= 100;
            PlayerData.hpLevel++;
            JOptionPane.showMessageDialog(this, "血量升級到 Lv." + PlayerData.hpLevel);
            repaint();
        } else {
            JOptionPane.showMessageDialog(this, "金幣不足！");
        }
    }
    

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Dialog", Font.BOLD, 18));
        g.drawString("升級介面", 140, 100);

        g.setFont(new Font("Dialog", Font.PLAIN, 14));
        g.drawString("攻擊等級: Lv. " + PlayerData.atkLevel, 100, 180);
        g.drawString("攻速等級: Lv. " + PlayerData.atkspeedLevel, 100, 280);
        g.drawString("防禦等級: Lv. " + PlayerData.defenseLevel, 100, 380);
        g.drawString("血量等級: Lv. " + PlayerData.hpLevel, 100, 480);


        // 顯示金幣
        String coinText = "金幣: " + PlayerData.coins;
        FontMetrics fm = g.getFontMetrics();
        int boxWidth = 120;
        int boxHeight = 25;
        int x = 140;
        int y = 130;

        g.setColor(new Color(0, 0, 0, 150));
        g.fillRoundRect(x, y, boxWidth, boxHeight, 15, 15);

        int textWidth = fm.stringWidth(coinText);
        int textX = x + (boxWidth - textWidth) / 2;
        int textY = y + ((boxHeight + fm.getAscent()) / 2) - 3;

        g.setColor(Color.YELLOW);
        g.drawString(coinText, textX, textY);
    }
}