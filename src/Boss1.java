import java.awt.*;
import java.util.Random;
import javax.swing.*;
import java.util.Vector;

public class Boss1 {
    public int x = 100, y = 40;
    public int hp = 100;
    private Image img;
    private int direction = 1;
    private int speed = 5;
    private Random random = new Random();
    
    // Movement pattern variables
    private int movementPattern = 0; // 0 = horizontal, 1 = zigzag, 2 = circle
    private int patternTimer = 0;
    private int patternDuration = 200;
    private double angle = 0;
    private int centerX = 160;
    private int centerY = 80;
    private int radius = 60;
    
    // Attack pattern variables
    private int attackPattern = 0; // 0 = single, 1 = triple, 2 = spray
    private int attackTimer = 0;
    private int attackDuration = 300;

    public Vector<EnemyBullet> bullets = new Vector<>();
    private long lastFireTime = System.currentTimeMillis();
    private long lastPatternChangeTime = System.currentTimeMillis();

    public Boss1() {
        img = Toolkit.getDefaultToolkit().getImage("C:/Users/User/Desktop/shooting-game/image/boss1.png");
    }

    public void update() {
        updateMovementPattern();
        move();
        updateAttackPattern();
        fire();
        updateBullets();
    }
    
    private void updateMovementPattern() {
        patternTimer++;
        if (patternTimer >= patternDuration) {
            patternTimer = 0;
            movementPattern = random.nextInt(3); // Choose a new pattern
            // Reset position for new pattern
            if (movementPattern == 2) { // Circle pattern
                centerX = random.nextInt(160) + 80;
                centerY = random.nextInt(40) + 60;
                angle = 0;
            }
        }
    }
    
    private void updateAttackPattern() {
        attackTimer++;
        if (attackTimer >= attackDuration) {
            attackTimer = 0;
            attackPattern = random.nextInt(3); // Choose a new attack pattern
        }
    }

    public void move() {
        switch (movementPattern) {
            case 0: // Horizontal movement
                if (random.nextInt(30) > 28) direction *= -1;
                x += direction * speed;
                if (x < 0 || x > 240) direction *= -1;
                break;
                
            case 1: // Zigzag movement
                x += direction * speed;
                y += (patternTimer % 40 < 20) ? 1 : -1;
                if (x < 0 || x > 240) direction *= -1;
                if (y < 20 || y > 100) y = (y < 20) ? 20 : 100;
                break;
                
            case 2: // Circle movement
                angle += 0.05;
                x = centerX + (int)(radius * Math.cos(angle));
                y = centerY + (int)(radius * Math.sin(angle));
                break;
        }
    }

    public void fire() {
        long currentTime = System.currentTimeMillis();
        int fireDelay = 2000;
        
        // Make boss fire faster when HP is low
        if (hp < 50) {
            fireDelay = 1500;
        }
        if (hp < 25) {
            fireDelay = 1000;
        }
        
        if (currentTime - lastFireTime > fireDelay) {
            switch (attackPattern) {
                case 0: // Single bullet
                    bullets.add(new EnemyBullet(x + 40, y + 60));
                    break;
                    
                case 1: // Triple bullet
                    bullets.add(new EnemyBullet(x + 40, y + 60));
                    bullets.add(new EnemyBullet(x + 30, y + 60));
                    bullets.add(new EnemyBullet(x + 50, y + 60));
                    break;
                    
                case 2: // Spray bullet
                    for (int i = -2; i <= 2; i++) {
                        EnemyBullet bullet = new EnemyBullet(x + 40, y + 60);
                        // Add some horizontal movement to make it spray
                        bullet.dx = i * 2;
                        bullets.add(bullet);
                    }
                    break;
            }
            lastFireTime = currentTime;
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

        // Health bar
        g.setColor(Color.RED);
        g.fillRect(x, y - 10, 80, 5);
        g.setColor(Color.GREEN);
        g.fillRect(x, y - 10, (int)(80 * ((double)hp / 100)), 5);

        // Draw bullets
        for (EnemyBullet b : bullets) {
            b.draw(g, panel);
        }
        
    }
}