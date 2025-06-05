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

        upgradeAttackButton = new JButton("升級攻擊:"+PlayerData.getAttackUpgradeCost());
        upgradeAttackButton.setBounds(100, 200, 200, 50);
        upgradeAttackButton.addActionListener(e -> upgradeAttack());
        add(upgradeAttackButton);

        upgradeattackSpeedButton = new JButton("升級速度:"+PlayerData.getSpeedUpgradeCost());
        upgradeattackSpeedButton.setBounds(100, 300, 200, 50);
        upgradeattackSpeedButton.addActionListener(e -> upgradeattackSpeed());
        add(upgradeattackSpeedButton);

        upgradedefenseButton = new JButton("升級防禦:"+PlayerData.getDefenseUpgradeCost());
        upgradedefenseButton.setBounds(100, 400, 200, 50);
        upgradedefenseButton.addActionListener(e -> upgradedefense());
        add(upgradedefenseButton);

        upgradehpButton = new JButton("升級血量:"+PlayerData.getHealthUpgradeCost());
        upgradehpButton.setBounds(100, 500, 200, 50);
        upgradehpButton.addActionListener(e -> upgradehealth());
        add(upgradehpButton);
    }

    private void upgradeAttack() {
        if (PlayerData.coins >= PlayerData.getAttackUpgradeCost()) {
            PlayerData.coins -= PlayerData.getAttackUpgradeCost();
            PlayerData.atkLevel++;
            upgradeAttackButton.setText("升級攻擊:" + PlayerData.getAttackUpgradeCost()); // 更新金額
            repaint();
        } else {
            JOptionPane.showMessageDialog(this, "金幣不足！");
        }
    }

    private void upgradeattackSpeed() {
        if (PlayerData.coins >= PlayerData.getSpeedUpgradeCost()) {
            PlayerData.coins -= PlayerData.getSpeedUpgradeCost();
            PlayerData.atkspeedLevel++;
            upgradeattackSpeedButton.setText("升級速度:" + PlayerData.getSpeedUpgradeCost()); // 更新金額
            repaint();
        } else {
            JOptionPane.showMessageDialog(this, "金幣不足！");
        }
    
    }
    private void upgradedefense(){
        if (PlayerData.coins >= PlayerData.getDefenseUpgradeCost()) {
            PlayerData.coins -= PlayerData.getDefenseUpgradeCost();
            PlayerData.defenseLevel++;
            upgradedefenseButton.setText("升級防禦:" + PlayerData.getDefenseUpgradeCost()); // 更新金額
            repaint();
        } else {
            JOptionPane.showMessageDialog(this, "金幣不足！");
        }
    }
    private void upgradehealth(){
        if (PlayerData.coins >= PlayerData.getHealthUpgradeCost()) {
            PlayerData.coins -= PlayerData.getHealthUpgradeCost();
            PlayerData.hpLevel++;
            upgradehpButton.setText("升級血量:" + PlayerData.getHealthUpgradeCost()); // 更新金額
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
        g.drawString("攻擊等級: Lv. " + PlayerData.atkLevel+"         atk: "+PlayerData.getAttack(), 100, 180);
        g.drawString("攻速等級: Lv. " + PlayerData.atkspeedLevel+"         spd: "+PlayerData.getspd(), 100, 280);
        g.drawString("防禦等級: Lv. " + PlayerData.defenseLevel+"         dfn: "+PlayerData.getDefense(), 100, 380);
        g.drawString("血量等級: Lv. " + PlayerData.hpLevel+"         hp: "+PlayerData.getMaxHP(), 100, 480);


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