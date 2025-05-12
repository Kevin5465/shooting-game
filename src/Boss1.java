import java.awt.*;
import java.util.Random;
import javax.swing.*;
import java.util.Vector;

public class Boss1 {
    public int x = 100, y = 40;
    public int hp = 100;
    private Image img;
    private int direction = 1;
    private int speed = 10;

    public Vector<EnemyBullet> bullets = new Vector<>();
    private long lastFireTime = System.currentTimeMillis();

    public Boss1() {
        img = Toolkit.getDefaultToolkit().getImage("C:/Users/User/Desktop/shooting-game/image/boss1.png");
    }

    public void update() {
        move();
        fire();
        updateBullets();
    }

    public void move() {
        if (new Random().nextInt(10) > 7) direction *= -1;
        x += direction * speed;
        if (x < 0 || x > 240) direction *= -1;
    }

    public void fire() {
        if (System.currentTimeMillis() - lastFireTime > 2000) {
            bullets.add(new EnemyBullet(x + 40, y + 60));
            lastFireTime = System.currentTimeMillis();
        }
    }

    public void updateBullets() {
        for (int i = 0; i < bullets.size(); i++) {
            EnemyBullet b = bullets.get(i);
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

        // 血量條
        g.setColor(Color.RED);
        g.fillRect(x, y - 10, 80, 5);
        g.setColor(Color.GREEN);
        g.fillRect(x, y - 10, (int)(80 * ((double)hp / 100)), 5);

        // 畫出子彈
        for (EnemyBullet b : bullets) {
            b.draw(g, panel);
        }
    }
}
