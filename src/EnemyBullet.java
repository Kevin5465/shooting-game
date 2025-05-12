import java.awt.*;
import javax.swing.*;

public class EnemyBullet {
    public int x, y;
    public boolean isAlive = true;
    private Image img;

    public EnemyBullet(int x, int y) {
        this.x = x;
        this.y = y;
        img = Toolkit.getDefaultToolkit().getImage("C:/Users/User/Desktop/shooting-game/image/bullet_0.png");
    }

    public void update() {
        y += 6;
        if (y > 480) isAlive = false;
    }

    public void draw(Graphics g, JPanel panel) {
        g.drawImage(img, x, y, 10, 20, panel);
    }
}
