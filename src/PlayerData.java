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
        return 20 + atkLevel * 6;
    }

    public static int getAttackSpeed() {
        return 1 + (atkspeedLevel - 1);
    }
    public static int getDefense() {
    return 5 + defenseLevel * 1; // 每升一級加3防禦
    }
    public static int getMaxHP() {
        return 100 + hpLevel * 20; // 每升一級加20HP
    }

    public static double getLifesteal() {
        return lifestealLevel * 0.02; // 每升一級吸血+2%
    }
}

