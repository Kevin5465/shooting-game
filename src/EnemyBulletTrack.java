import java.awt.*;
import javax.swing.*;

public class EnemyBulletTrack {
    public int x, y;
    private int dx, dy;
    private Image img;
    public boolean isAlive = true;

    public EnemyBulletTrack(int startX, int startY, int targetX, int targetY) {
        x = startX;
        y = startY;
        ImageIcon icon = new ImageIcon("image/bullet_0.png");
	img = icon.getImage();

        double angle = Math.atan2(targetY - startY, targetX - startX);
        dx = (int)(5 * Math.cos(angle));
        dy = (int)(5 * Math.sin(angle));
    }

    public void update() {
        x += dx;
        y += dy;
        if (x < 0 || x > 320 || y < 0 || y > 480) isAlive = false;
    }

    public void draw(Graphics g, JPanel panel) {
        g.drawImage(img, x, y, 10, 20, panel);
    }
}
