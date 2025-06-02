import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.swing.ImageIcon;

public class PlayerData implements Serializable {
    private static final long serialVersionUID = 1L;

    public String playerName;
    public int playerLevel;
    public int currentExp;
    public int maxExp;
    public int coins;
    public String avatarPath;  // 圖片路徑

    public PlayerData(String playerName, int playerLevel, int currentExp, int maxExp, int coins, String avatarPath) {
        this.playerName = playerName;
        this.playerLevel = playerLevel;
        this.currentExp = currentExp;
        this.maxExp = maxExp;
        this.coins = coins;
        this.avatarPath = avatarPath;
    }
}
