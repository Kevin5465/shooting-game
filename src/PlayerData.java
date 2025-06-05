import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.swing.ImageIcon;

public class PlayerData {
    public static int coins = 0;
    public static int level = 1;
    public static int currentExp = 0;
    public static int maxExp = 100;

    public static int atkLevel = 1;
    public static int atkspeedLevel = 1;
    public static int defenseLevel = 1;
    public static int hpLevel = 1;
    public static int lifestealLevel = 1;
    public static int score = 0;
    public static int highScore = 0;
    public static double spd;
    
    

    private int getExpForNextLevel(int level) {
        return (int)(100 * Math.pow(level, 1.2));  // 例如：lv1 = 100, lv10 ≈ 630
    }

    
    // 加經驗值
    public static void addExp(int amount) {
        currentExp += amount;
        while (currentExp >= maxExp) {
            currentExp -= maxExp;
            level++;
            maxExp += 20; // 升級曲線
        }
    }
    public static void addCoins(int amount){
        coins += amount;
    }
    public static int getAttack() {
        return (int) Math.round(20 * Math.pow(1.2, atkLevel));
    }

    public static double getAttackSpeed() {
        return 1.0 * Math.pow(1.2, atkspeedLevel - 1);
    }
    public static int getDefense() {
    return 9 + defenseLevel * 15; // 每升一級加3防禦
    }
    public static int getMaxHP() {
        return 100 + hpLevel * 40; // 每升一級加20HP
    }

    public static void resetScore() {
        if (score > highScore) {
            highScore = score;
        }
        score = 0;
    }

    public static void addScore(int s) {
        score += s;
    }

    public static int getScore() {
        return score;
    }

    public static int getHighScore() {
        return highScore;
    }
    public static void rewardFromScore() {
        int coinsGained = (int)(score * 0.03);
        int expGained = (int)(score * 0.15);
        coins += coinsGained;
        resetScore();
        addExp(expGained);
    }
    public static double getspd() {
        return Math.round(getAttackSpeed() * 10) / 10.0;
    }
    public static int getAttackUpgradeCost() {
    return (int) Math.round(100 * Math.pow(1.13, atkLevel - 1));
}

    public static int getSpeedUpgradeCost() {
        return (int) Math.round(100 * Math.pow(1.13, atkspeedLevel - 1));
    }

    public static int getDefenseUpgradeCost() {
        return (int) Math.round(100 * Math.pow(1.13, defenseLevel - 1));
    }

    public static int getHealthUpgradeCost() {
        return (int) Math.round(100 * Math.pow(1.13, hpLevel - 1));
    }  


}

