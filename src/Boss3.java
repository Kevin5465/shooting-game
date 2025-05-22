import java.awt.*;
import java.util.Random;
import javax.swing.*;
import java.util.Vector;

public class Boss3 {
    public int x = 100, y = 40;
    public int hp = 200;
    private Image img;
    private int direction = 1;
    private int speed = 8;
    private Random random = new Random();
    
    // Movement pattern variables
    private int movementPattern = 0; // 0 = swoop, 1 = figure8, 2 = chase
    private int patternTimer = 0;
    private int patternDuration = 300;
    private double angle = 0;
    private int swoopPhase = 0;
    private int targetX = 160;
    private int targetY = 60;
    
    // Attack pattern variables
    private int attackPattern = 0; // 0 = tracking, 1 = barrage, 2 = laser
    private int attackTimer = 0;
    private int attackDuration = 400;
    
    // Special abilities
    private boolean isCharging = false;
    private long chargeStartTime = 0;
    private int chargeDuration = 2000;
    private int minionsSpawned = 0;
    private long lastMinionSpawnTime = 0;
    
    // Laser attack
    private boolean isLaserActive = false;
    private int laserWidth = 0;
    private int laserTarget = 160;
    private long laserStartTime = 0;
    private int laserDuration = 3000;

    public Vector<EnemyBulletTrack> trackingBullets = new Vector<>();
    public Vector<EnemyBullet> normalBullets = new Vector<>();
    private long lastFireTime = System.currentTimeMillis();
    private long lastSpecialTime = System.currentTimeMillis();

    public Boss3() {
        img = Toolkit.getDefaultToolkit().getImage("C:/Users/User/Desktop/shooting-game/image/boss3.png");
    }

    public void update(int playerX, int playerY) {
        updateMovementPattern(playerX, playerY);
        move(playerX, playerY);
        updateAttackPattern();
        fire(playerX, playerY);
        updateBullets();
        updateSpecialAbility();
    }
    
    private void updateMovementPattern(int playerX, int playerY) {
        patternTimer++;
        if (patternTimer >= patternDuration) {
            patternTimer = 0;
            movementPattern = random.nextInt(3); // Choose a new pattern
            
            // Reset values for new pattern
            angle = 0;
            swoopPhase = 0;
            targetX = playerX;
            targetY = playerY;
        }
    }
    
    private void updateAttackPattern() {
        attackTimer++;
        if (attackTimer >= attackDuration) {
            attackTimer = 0;
            attackPattern = random.nextInt(3); // Choose a new attack pattern
            
            if (attackPattern == 2) {  // Laser attack
                isLaserActive = true;
                laserWidth = 0;
                laserStartTime = System.currentTimeMillis();
            }
        }
    }
    
    private void updateSpecialAbility() {
        long currentTime = System.currentTimeMillis();
        
        // Special 1: Spawn minions every 15 seconds (max 3)
        if (currentTime - lastMinionSpawnTime > 15000 && minionsSpawned < 3) {
            minionsSpawned++;
            lastMinionSpawnTime = currentTime;
            // You'll need to implement the minion spawning in your game loop
        }
        
        // Special 2: Charging attack - becomes faster and invulnerable briefly
        if (currentTime - lastSpecialTime > 20000 && !isCharging) {
            isCharging = true;
            chargeStartTime = currentTime;
            lastSpecialTime = currentTime;
            speed *= 2;  // Double speed during charge
        }
        
        // Check if charging period is over
        if (isCharging && currentTime - chargeStartTime > chargeDuration) {
            isCharging = false;
            speed = 8;  // Reset speed
        }
        
        // Update laser attack
        if (isLaserActive) {
            if (currentTime - laserStartTime < 1000) {
                // Charging phase
                laserWidth = (int)((currentTime - laserStartTime) / 1000.0 * 40);
            } else if (currentTime - laserStartTime < laserDuration) {
                // Active phase
                laserWidth = 40;
            } else {
                // End phase
                isLaserActive = false;
            }
        }
    }

    public void move(int playerX, int playerY) {
        switch (movementPattern) {
            case 0: // Swoop movement
                if (swoopPhase == 0) {
                    // Move to top of screen
                    if (y > 30) y -= speed;
                    else swoopPhase = 1;
                } else if (swoopPhase == 1) {
                    // Move horizontally to target
                    if (Math.abs(x - targetX) > speed) {
                        x += (x < targetX) ? speed : -speed;
                    } else {
                        swoopPhase = 2;
                    }
                } else if (swoopPhase == 2) {
                    // Swoop down
                    y += speed * 2;
                    if (y > 240) swoopPhase = 3;
                } else {
                    // Return to top
                    y -= speed;
                    if (y <= 40) {
                        swoopPhase = 0;
                        targetX = random.nextInt(240);
                    }
                }
                break;
                
            case 1: // Figure 8 movement
                angle += 0.05;
                x = 160 + (int)(80 * Math.sin(angle));
                y = 60 + (int)(40 * Math.sin(angle * 2));
                break;
                
            case 2: // Chase player with delay
                // Only update target position occasionally
                if (patternTimer % 30 == 0) {
                    targetX = playerX;
                    targetY = playerY - 100; // Stay above player
                    if (targetY < 20) targetY = 20;
                }
                
                // Move toward target
                if (Math.abs(x - targetX) > speed) {
                    x += (x < targetX) ? speed : -speed;
                }
                
                if (Math.abs(y - targetY) > speed) {
                    y += (y < targetY) ? speed : -speed;
                }
                break;
        }
        
        // Ensure boss stays on screen
        if (x < 0) x = 0;
        if (x > 240) x = 240;
        if (y < 20) y = 20;
        if (y > 240) y = 240;
    }

    public void fire(int playerX, int playerY) {
        long currentTime = System.currentTimeMillis();
        int fireDelay = 1500;
        
        // Make boss fire faster when HP is low
        if (hp < 100) {
            fireDelay = 1200;
        }
        if (hp < 50) {
            fireDelay = 800;
        }
        
        if (currentTime - lastFireTime > fireDelay) {
            switch (attackPattern) {
                case 0: // Tracking bullets
                    trackingBullets.add(new EnemyBulletTrack(x + 40, y + 60, playerX, playerY));
                    break;
                    
                case 1: // Barrage pattern
                    for (int i = -3; i <= 3; i++) {
                        for (int j = 0; j < 2; j++) {
                            EnemyBullet bullet = new EnemyBullet(x + 40 + i * 10, y + 60 + j * 10);
                            bullet.dy = 6 + j;
                            bullet.dx = i;
                            normalBullets.add(bullet);
                        }
                    }
                    break;
                    
                case 2: // Laser attack is handled in the updateSpecialAbility method
                    laserTarget = playerX;
                    break;
            }
            lastFireTime = currentTime;
        }
    }

    public void updateBullets() {
        // Update tracking bullets
        for (int i = 0; i < trackingBullets.size(); i++) {
            EnemyBulletTrack b = trackingBullets.get(i);
            b.update();
            if (!b.isAlive || b.y > 480 || b.x < 0 || b.x > 320) trackingBullets.remove(i--);
        }
        
        // Update normal bullets
        for (int i = 0; i < normalBullets.size(); i++) {
            EnemyBullet b = normalBullets.get(i);
            b.update();
            if (!b.isAlive || b.y > 480 || b.x < 0 || b.x > 320) normalBullets.remove(i--);
        }
    }

    public boolean checkCollisionWithPlayer(int px, int py) {
        // Regular boss collision
        Rectangle bossRect = new Rectangle(x, y, 80, 80);
        Rectangle playerRect = new Rectangle(px, py, 30, 30);
        
        // Laser collision
        boolean laserCollision = false;
        if (isLaserActive && laserWidth > 10) {
            Rectangle laserRect = new Rectangle(
                laserTarget - laserWidth/2, y + 60,
                laserWidth, 480 - (y + 60)
            );
            laserCollision = laserRect.intersects(playerRect);
        }
        
        return bossRect.intersects(playerRect) || laserCollision;
    }
    
    public void takeDamage(int damage) {
        if (!isCharging) {
            hp -= damage;
        }
    }

    public void draw(Graphics g, JPanel panel) {
        g.drawImage(img, x, y, 80, 80, panel);

        // Health bar
        g.setColor(Color.RED);
        g.fillRect(x, y - 10, 80, 5);
        g.setColor(Color.GREEN);
        g.fillRect(x, y - 10, (int)(80 * ((double)hp / 200)), 5);

        // Draw bullets
        for (EnemyBulletTrack b : trackingBullets) {
            b.draw(g, panel);
        }
        
        for (EnemyBullet b : normalBullets) {
            b.draw(g, panel);
        }
        
        // Draw charging effect
        if (isCharging) {
            g.setColor(new Color(255, 50, 50, 150));
            g.fillOval(x - 5, y - 5, 90, 90);
        }
        
        // Draw laser
        if (isLaserActive && laserWidth > 0) {
            // Warning line
            if (System.currentTimeMillis() - laserStartTime < 1000) {
                g.setColor(Color.RED);
                g.drawLine(laserTarget, y + 80, laserTarget, 480);
            }
            
            // Actual laser
            if (laserWidth > 10) {
                g.setColor(new Color(255, 50, 50, 150));
                g.fillRect(laserTarget - laserWidth/2, y + 80, laserWidth, 480 - (y + 80));
                
                // Laser core
                g.setColor(new Color(255, 255, 255, 200));
                g.fillRect(laserTarget - 2, y + 80, 4, 480 - (y + 80));
            }
        }
    }
    
    // Method to get minion count for game management
    public int getMinionCount() {
        return minionsSpawned;
    }
    
    // Method to decrease minion count when one is destroyed
    public void decreaseMinionCount() {
        if (minionsSpawned > 0) {
            minionsSpawned--;
        }
    }
}