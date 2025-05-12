import java.awt.*;
import java.util.Random;
import javax.swing.*;
import java.util.Vector;

public class Boss3 {
    public int x = 100, y = 40;
    public int hp = 150;
    private Image img;
    private int direction = 1;
    private int speed = 30;

    public Vector<EnemyBulletTrack> bullets = new Vector<>();
    private long lastFireTime = System.currentTimeMillis();

    public Boss3() {
        img = Toolkit.getDefaultToolkit().getImage("C:/Users/User/Desktop/shooting-game/image/boss3.png");
    }

    public void update(int playerX, int playerY) {
        move();
        fire(playerX, playerY);
        updateBullets();
    }

    public void move() {
        if (new Random().nextInt(10) > 7) direction *= -1;
        x += direction * speed;
        if (x < 0 || x > 240) direction *= -1;
    }

    public void fire(int playerX, int playerY) {
        if (System.currentTimeMillis() - lastFireTime > 2000) {
            bullets.add(new EnemyBulletTrack(x + 40, y + 60, playerX, playerY));
            lastFireTime = System.currentTimeMillis();
        }
    }

    public void updateBullets() {
        for (int i = 0; i < bullets.size(); i++) {
            EnemyBulletTrack b = bullets.get(i);
            b.update();
            if (!b.isAlive || b.y > 480) bullets.remove(i--);
        }
    }

    public boolean checkCollisionWithPlayer(int px, int py) {
        Rectangle bossRect = new Rectangle(x, y, 80, 80);
        Rectangle playerRect = new Rectangle(px, py, 30, 30);
        return bossRect.intersects(playerRect);
    }

    public void draw(Graphics g, JPanel panel) {
        g.drawImage(img, x, y, 80, 80, panel);

        g.setColor(Color.RED);
        g.fillRect(x, y - 10, 80, 5);
        g.setColor(Color.GREEN);
        g.fillRect(x, y - 10, (int)(80 * ((double)hp / 150)), 5);

        for (EnemyBulletTrack b : bullets) {
            b.draw(g, panel);
        }
    }
}
