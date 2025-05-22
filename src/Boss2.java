import java.awt.*;
import java.util.Random;
import javax.swing.*;
import java.util.Vector;

public class Boss2 {
    public int x = 100, y = 40;
    public int hp = 150;
    private Image img;
    private int direction = 1;
    private int speed = 7;
    private Random random = new Random();
    
    // Movement pattern variables
    private int movementPattern = 0; // 0 = horizontal, 1 = teleport, 2 = bounce
    private int patternTimer = 0;
    private int patternDuration = 250;
    private int targetX = 100;
    private int targetY = 40;
    private boolean isVisible = true;
    private long teleportStartTime = 0;
    private int bounceDirectionY = 1;
    
    // Attack pattern variables
    private int attackPattern = 0; // 0 = single, 1 = wave, 2 = circular
    private int attackTimer = 0;
    private int attackDuration = 350;
    
    // Special ability
    private boolean isInvulnerable = false;
    private long invulnerabilityStartTime = 0;
    private int invulnerabilityDuration = 3000; // 3 seconds

    public Vector<EnemyBullet> bullets = new Vector<>();
    private long lastFireTime = System.currentTimeMillis();
    private long lastSpecialTime = System.currentTimeMillis();

    public Boss2() {
        img = Toolkit.getDefaultToolkit().getImage("C:/Users/User/Desktop/shooting-game/image/boss2.png");
    }

    public void update() {
        updateMovementPattern();
        move();
        updateAttackPattern();
        fire();
        updateBullets();
        updateSpecialAbility();
    }
    
    private void updateMovementPattern() {
        patternTimer++;
        if (patternTimer >= patternDuration) {
            patternTimer = 0;
            movementPattern = random.nextInt(3); // Choose a new pattern
            
            // Reset values for new pattern
            if (movementPattern == 1) { // Teleport
                teleportStartTime = System.currentTimeMillis();
                isVisible = false;
                targetX = random.nextInt(240);
                targetY = random.nextInt(80) + 20;
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
    
    private void updateSpecialAbility() {
        // Every 10 seconds, become invulnerable for 3 seconds
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSpecialTime > 10000 && !isInvulnerable) {
            isInvulnerable = true;
            invulnerabilityStartTime = currentTime;
            lastSpecialTime = currentTime;
        }
        
        // Check if invulnerability period is over
        if (isInvulnerable && currentTime - invulnerabilityStartTime > invulnerabilityDuration) {
            isInvulnerable = false;
        }
    }

    public void move() {
        switch (movementPattern) {
            case 0: // Horizontal movement with random speed changes
                if (random.nextInt(30) > 28) direction *= -1;
                if (random.nextInt(100) > 95) speed = random.nextInt(5) + 5;
                x += direction * speed;
                if (x < 0 || x > 240) direction *= -1;
                break;
                
            case 1: // Teleport movement
                long currentTime = System.currentTimeMillis();
                if (!isVisible && currentTime - teleportStartTime > 1000) {
                    x = targetX;
                    y = targetY;
                    isVisible = true;
                }
                break;
                
            case 2: // Bounce movement
                x += direction * speed;
                y += bounceDirectionY * speed / 2;
                
                if (x < 0 || x > 240) direction *= -1;
                if (y < 20 || y > 120) bounceDirectionY *= -1;
                break;
        }
    }

    public void fire() {
        long currentTime = System.currentTimeMillis();
        int fireDelay = 1800;
        
        // Make boss fire faster when HP is low
        if (hp < 75) {
            fireDelay = 1400;
        }
        if (hp < 40) {
            fireDelay = 1000;
        }
        
        if (currentTime - lastFireTime > fireDelay && isVisible) {
            switch (attackPattern) {
                case 0: // Single line
                    bullets.add(new EnemyBullet(x + 40, y + 60));
                    break;
                    
                case 1: // Wave pattern
                    for (int i = 0; i < 5; i++) {
                        EnemyBullet bullet = new EnemyBullet(x + 40, y + 60);
                        bullet.dy = 5;
                        bullet.dx = (i - 2) * 2;
                        bullets.add(bullet);
                    }
                    break;
                    
                case 2: // Circular pattern
                    for (int i = 0; i < 8; i++) {
                        double angle = Math.PI * 2 * i / 8;
                        EnemyBullet bullet = new EnemyBullet(x + 40, y + 60);
                        bullet.dx = (int)(5 * Math.cos(angle));
                        bullet.dy = (int)(5 * Math.sin(angle));
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
            if (!b.isAlive || b.y > 480 || b.x < 0 || b.x > 320) bullets.remove(i--);
        }
    }

    public boolean checkCollisionWithPlayer(int px, int py) {
        if (!isVisible || isInvulnerable) return false;
        
        Rectangle bossRect = new Rectangle(x, y, 80, 80);
        Rectangle playerRect = new Rectangle(px, py, 30, 30);
        return bossRect.intersects(playerRect);
    }
    
    public void takeDamage(int damage) {
        if (!isInvulnerable) {
            hp -= damage;
        }
    }

    public void draw(Graphics g, JPanel panel) {
        if (!isVisible) return;
        
        g.drawImage(img, x, y, 80, 80, panel);

        // Health bar
        g.setColor(Color.RED);
        g.fillRect(x, y - 10, 80, 5);
        g.setColor(Color.GREEN);
        g.fillRect(x, y - 10, (int)(80 * ((double)hp / 150)), 5);

        // Draw bullets
        for (EnemyBullet b : bullets) {
            b.draw(g, panel);
        }
        
        // Draw shield effect when invulnerable
        if (isInvulnerable) {
            g.setColor(new Color(255, 215, 0, 100));
            g.fillOval(x - 10, y - 10, 100, 100);
        }
    }
}