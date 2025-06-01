import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Collections;
import java.util.Arrays;
import javax.sound.sampled.*;
import java.util.List;
import java.io.File;

public class StageModePanel extends JPanel implements ActionListener, KeyListener {
    private Timer timer;
    private final int PANEL_WIDTH = 400, PANEL_HEIGHT = 800;
    private MainFrame mainFrame;
    
    // 遊戲階段控制
    private enum GameStage { NORMAL, BOSS }
    private GameStage currentStage = GameStage.NORMAL;
    private int bossNumber = 0;  // 0=小怪階段, 1=Boss1, 2=Boss2, 3=Boss3
    private int enemiesKilled = 0;
    private final int ENEMIES_BEFORE_BOSS = 20;  // 打10隻小怪後出現Boss
    
    // 玩家屬性
    private int playerX, playerY;
    private static final int PLAYER_WIDTH = 50, PLAYER_HEIGHT = 30;
    private double playerMaxHealth = 200, playerHealth = playerMaxHealth;
    private double playerAttack = 26, playerDefense = 10, playerAttackSpeed = 1.0;
    private long lastFireTime = 0, lastFireballTime = 0;
    private final int BASE_FIREBALL_COOLDOWN = 1000;
    private boolean left, right, up, down, space, fireballKey;
    private long pauseStartTime = 0;
    private long pausedRemainingCd = 0;

    // 特殊技能等級
    private int multiShotLevel = 0, chainAttackLevel = 0,
            fireballSkillLevel = 0, diagonalShotLevel = 0, deathChainLevel = 0;

    // 經驗與等級
    private int playerLevel = 1, playerXP = 0, xpToNext = 50;
    private boolean leveling = false;

    private enum SpecialAbility { MULTI_SHOT, CHAIN_ATTACK, FIREBALL, DIAGONAL_SHOT, DEATH_CHAIN }

    // Boss類別
    private class Boss {
        Rectangle rect;
        double maxHealth, health, attack, defense;
        int level;
        int dirX = 1, dirY = 1;
        long lastAttackTime = 0;
        int attackPattern = 0;
        
        public Boss(int level) {
            this.level = level;
            // Boss規格根據等級調整
            switch(level) {
                case 1:
                    rect = new Rectangle(PANEL_WIDTH/2 - 60, 50, 120, 80);
                    maxHealth = health = 1000;
                    attack = 25;
                    defense = 20;
                    break;
                case 2:
                    rect = new Rectangle(PANEL_WIDTH/2 - 70, 40, 140, 90);
                    maxHealth = health = 2000;
                    attack = 50;
                    defense = 40;
                    break;
                case 3:
                    rect = new Rectangle(PANEL_WIDTH/2 - 80, 30, 160, 100);
                    maxHealth = health = 3000;
                    attack = 100;
                    defense = 80;
                    break;
            }
        }
        
        public void update() {
            // Boss移動模式
            if(rect.x <= 0 || rect.x >= PANEL_WIDTH - rect.width) dirX = -dirX;
            if(rect.y <= 30 || rect.y >= 200) dirY = -dirY;
            
            rect.x += dirX * (1 + level);
            rect.y += dirY * (1 + level);
            
            // Boss攻擊模式
            long now = System.currentTimeMillis();
            int attackCooldown = Math.max(800, 1500 - level * 200);
            
            if(now - lastAttackTime >= attackCooldown) {
                performAttack();
                lastAttackTime = now;
                attackPattern = (attackPattern + 1) % 3;
            }
        }
        
        private void performAttack() {
            double centerX = rect.x + rect.width / 2.0;
            double centerY = rect.y + rect.height;
            double playerCenterX = playerX + PLAYER_WIDTH / 2.0;
            double playerCenterY = playerY + PLAYER_HEIGHT / 2.0;
            
            switch(attackPattern) {
                case 0: // 直射玩家
                    shootAtPlayer(centerX, centerY, playerCenterX, playerCenterY);
                    break;
                case 1: // 散射
                    for(int i = -2; i <= 2; i++) {
                        double angle = Math.PI/2 + i * Math.PI/8;
                        double vx = Math.sin(angle) * 4;
                        double vy = Math.cos(angle) * 4;
                        enemyBullets.add(new EnemyBullet(centerX, centerY, vx, vy));
                    }
                    break;
                case 2: // 圓形射擊 (僅Boss2和Boss3)
                    if(level >= 2) {
                        int bulletCount = 8 + level * 2;
                        for(int i = 0; i < bulletCount; i++) {
                            double angle = 2 * Math.PI * i / bulletCount;
                            double vx = Math.sin(angle) * 3;
                            double vy = Math.cos(angle) * 3;
                            enemyBullets.add(new EnemyBullet(centerX, centerY, vx, vy));
                        }
                    }
                    break;
            }
        }
        
        private void shootAtPlayer(double bx, double by, double px, double py) {
            double dx = px - bx;
            double dy = py - by;
            double dist = Math.hypot(dx, dy);
            double speed = 4 + level;
            
            if(dist > 0) {
                double vx = dx / dist * speed;
                double vy = dy / dist * speed;
                enemyBullets.add(new EnemyBullet(bx, by, vx, vy));
                
                // Boss2和Boss3會發射多發子彈
                if(level >= 2) {
                    enemyBullets.add(new EnemyBullet(bx - 10, by, vx, vy));
                    enemyBullets.add(new EnemyBullet(bx + 10, by, vx, vy));
                }
            }
        }
    }
    
    private Boss currentBoss = null;

    // 敵人類別
    private class Enemy {
        Rectangle rect; 
        double health, attack, defense;
        int dirX, dirY;
        
        public Enemy(int x, int y, double hp, double atk, double def) {
            rect = new Rectangle(x, y, 40, 30);
            health = hp; 
            attack = atk; 
            defense = def;
            dirX = random.nextBoolean() ? 1 : -1;
            dirY = random.nextBoolean() ? 1 : -1;
        }
    }
    private final ArrayList<Enemy> enemies = new ArrayList<>();

    // 子彈類別保持不變
    private class Bullet { 
        double x, y, vx, vy; 
        static final int W = 5, H = 10;
        
        public Bullet(double x, double y, double vx, double vy) { 
            this.x = x; this.y = y; this.vx = vx; this.vy = vy; 
        }
        
        public void update() { x += vx; y += vy; }
        public Rectangle getRect() { return new Rectangle((int)x, (int)y, W, H); }
    }
    
    private class Fireball { 
        double x, y, vx, vy; 
        static final int SIZE = 16;
        
        public Fireball(double x, double y, double vx, double vy) { 
            this.x = x; this.y = y; this.vx = vx; this.vy = vy; 
        }
        
        public void update() { x += vx; y += vy; }
        public Rectangle getRect() { return new Rectangle((int)x, (int)y, SIZE, SIZE); }
    }
    
    private class EnemyBullet { 
        double x, y, vx, vy; 
        static final int W = 5, H = 10;
        
        public EnemyBullet(double x, double y, double vx, double vy) { 
            this.x = x; this.y = y; this.vx = vx; this.vy = vy; 
        }
        
        public void update() { x += vx; y += vy; }
        public Rectangle getRect() { return new Rectangle((int)x, (int)y, W, H); }
    }
    
    private class DamageText { 
        String text; 
        int x, y, life;
        
        public DamageText(String t, int x, int y) { 
            text = t; this.x = x; this.y = y; life = 60; 
        }
    }

    private final ArrayList<Bullet> bullets = new ArrayList<>();
    private final ArrayList<Fireball> fireballs = new ArrayList<>();
    private final ArrayList<EnemyBullet> enemyBullets = new ArrayList<>();
    private final ArrayList<DamageText> damageTexts = new ArrayList<>();
    private final Random random = new Random();

    private Image playerImg, enemyImg, bgImg, laserImg, fireballImg, boss1Img, boss2Img, boss3Img;

    public StageModePanel() {
        setPreferredSize(new Dimension(400, 800));
        setFocusable(true); 
        addKeyListener(this);
        
        playerX = PANEL_WIDTH / 2 - PLAYER_WIDTH / 2;
        playerY = PANEL_HEIGHT - PLAYER_HEIGHT - 10;
        
        // 載入圖片
        playerImg = loadImageWithFallback("image/ufo.png", PLAYER_WIDTH, PLAYER_HEIGHT, Color.BLUE);
        enemyImg = loadImageWithFallback("image/enemy.png", 40, 30, Color.RED);
        bgImg = loadImageWithFallback("image/background.png", PANEL_WIDTH, PANEL_HEIGHT, null);
        laserImg = loadImageWithFallback("image/laser.png", 5, 10, Color.YELLOW);
        fireballImg = loadImageWithFallback("image/fireball.png", 16, 16, Color.ORANGE);
        boss1Img = loadImageWithFallback("image/boss1.png", 100, 60, Color.MAGENTA);
	boss2Img = loadImageWithFallback("image/boss2.png", 100, 60, Color.MAGENTA);
	boss3Img = loadImageWithFallback("image/boss3.png", 100, 60, Color.MAGENTA);
        // 背景音樂載入
        try {
            File soundFile = new File("image/8hp8q-bq1d0.wav");
            if (soundFile.exists()) {
                AudioInputStream ais = AudioSystem.getAudioInputStream(soundFile);
                Clip c = AudioSystem.getClip();
                c.open(ais);
                c.loop(Clip.LOOP_CONTINUOUSLY);
            } else {
                System.out.println("Background music file not found.");
            }
        } catch (Exception e) {
            System.out.println("Failed to load background music: " + e.getMessage());
        }
        
        timer = new Timer(15, this);
        timer.start();
    }
    
    // 圖片載入方法保持不變
    private Image loadImageWithFallback(String path, int width, int height, Color fallbackColor) {
        try {
            Image img = new ImageIcon(path).getImage();
            if (img.getWidth(null) > 0 && img.getHeight(null) > 0) {
                return img;
            }
        } catch (Exception e) {
            System.out.println("Failed to load image: " + path + " - " + e.getMessage());
        }
        
        if (fallbackColor != null) {
            return createPlaceholderImage(width, height, fallbackColor);
        }
        return null;
    }
    
    public void setMainFrame(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }
    
    private Image createPlaceholderImage(int width, int height, Color color) {
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setColor(color);
        g2d.fillRect(0, 0, width, height);
        g2d.setColor(Color.WHITE);
        g2d.drawRect(0, 0, width - 1, height - 1);
        g2d.dispose();
        return img;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // 背景繪製
        if (bgImg != null) {
            g.drawImage(bgImg, 0, 0, PANEL_WIDTH, PANEL_HEIGHT, null);
        } else {
            g.setColor(new Color(15, 17, 26));
            g.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);
            
            g.setColor(Color.WHITE);
            Random starRandom = new Random(12345);
            for (int i = 0; i < 50; i++) {
                int x = starRandom.nextInt(PANEL_WIDTH);
                int y = starRandom.nextInt(PANEL_HEIGHT);
                int size = starRandom.nextInt(2) + 1;
                g.fillOval(x, y, size, size);
            }
        }
        
        // 玩家繪製
        if (playerImg != null) {
            g.drawImage(playerImg, playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT, null);
        }
        
        // 玩家血條
        int hpW = (int)(PLAYER_WIDTH * playerHealth / playerMaxHealth);
        g.setColor(Color.DARK_GRAY); 
        g.fillRect(playerX, playerY - 8, PLAYER_WIDTH, 5);
        g.setColor(Color.GREEN); 
        g.fillRect(playerX, playerY - 8, hpW, 5);
        
        // 狀態信息
        g.setColor(Color.WHITE); 
        g.setFont(new Font("Arial", Font.BOLD, 12));
        g.drawString(String.format("HP:%.0f/%.0f", playerHealth, playerMaxHealth), 5, 15);
        g.drawString(String.format("ATK:%.0f DEF:%.0f SPD:%.1f", playerAttack, playerDefense, playerAttackSpeed), 5, 30);
        g.drawString(String.format("LV:%d XP:%d/%d", playerLevel, playerXP, xpToNext), 5, 45);
        
        // 遊戲階段顯示
        if(currentStage == GameStage.BOSS && currentBoss != null) {
            g.drawString("BOSS " + bossNumber + " FIGHT!", 5, 75);
        } else {
            g.drawString("Enemies killed: " + enemiesKilled + "/" + ENEMIES_BEFORE_BOSS, 5, 75);
        }
        
        // 火球冷卻顯示
        if (fireballSkillLevel > 0) {
            String cdText;
            if (leveling) {
                double sec = pausedRemainingCd / 1000.0;
                if (pausedRemainingCd <= 0) cdText = "Fireball CD: READY";
                else cdText = String.format("Fireball CD: %.1fs", sec);
            } else {
                long now = System.currentTimeMillis();
                int fbCd = Math.max(1000, BASE_FIREBALL_COOLDOWN - fireballSkillLevel * 200);
                long since = now - lastFireballTime;
                if (since >= fbCd) cdText = "Fireball CD: READY";
                else {
                    double sec = (fbCd - since) / 1000.0;
                    cdText = String.format("Fireball CD: %.1fs", sec);
                }
            }
            g.drawString(cdText, 5, 90);
        }
        
        // Boss繪製
	if(currentBoss != null) {
    	    Image currentBossImg = null;
    	    switch(currentBoss.level) {
        	case 1: currentBossImg = boss1Img; break;
        	case 2: currentBossImg = boss2Img; break;
        	case 3: currentBossImg = boss3Img; break;
    	    }
            if(currentBossImg != null) {
                g.drawImage(currentBossImg, currentBoss.rect.x, currentBoss.rect.y, 
                           currentBoss.rect.width, currentBoss.rect.height, null);
            } else {
                g.setColor(Color.MAGENTA);
                g.fillRect(currentBoss.rect.x, currentBoss.rect.y, 
                          currentBoss.rect.width, currentBoss.rect.height);
                g.setColor(Color.WHITE);
                g.drawRect(currentBoss.rect.x, currentBoss.rect.y, 
                          currentBoss.rect.width, currentBoss.rect.height);
            }
            
            // Boss血條
            int bossHpW = (int)(currentBoss.rect.width * currentBoss.health / currentBoss.maxHealth);
            g.setColor(Color.DARK_GRAY);
            g.fillRect(currentBoss.rect.x, currentBoss.rect.y - 12, currentBoss.rect.width, 8);
            g.setColor(Color.RED);
            g.fillRect(currentBoss.rect.x, currentBoss.rect.y - 12, bossHpW, 8);
            
            // Boss血量數字
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 10));
            g.drawString(String.format("%.0f/%.0f", currentBoss.health, currentBoss.maxHealth), 
                        currentBoss.rect.x, currentBoss.rect.y - 15);
        }
        
        // 子彈繪製
        for (Bullet b : bullets) {
            if (laserImg != null) {
                g.drawImage(laserImg, b.getRect().x, b.getRect().y, Bullet.W, Bullet.H, null);
            } else {
                g.setColor(Color.YELLOW);
                g.fillRect(b.getRect().x, b.getRect().y, Bullet.W, Bullet.H);
            }
        }
        
        // 火球繪製
        for (Fireball f : fireballs) {
            if (fireballImg != null) {
                g.drawImage(fireballImg, f.getRect().x, f.getRect().y, Fireball.SIZE, Fireball.SIZE, null);
            } else {
                g.setColor(Color.ORANGE);
                g.fillOval(f.getRect().x, f.getRect().y, Fireball.SIZE, Fireball.SIZE);
            }
        }
        
        // 敵人繪製
        for (Enemy e : enemies) {
            if (enemyImg != null) {
                g.drawImage(enemyImg, e.rect.x, e.rect.y, e.rect.width, e.rect.height, null);
            } else {
                g.setColor(Color.RED);
                g.fillRect(e.rect.x, e.rect.y, e.rect.width, e.rect.height);
                g.setColor(Color.WHITE);
                g.drawRect(e.rect.x, e.rect.y, e.rect.width, e.rect.height);
            }
            
            // 敵人血條
            int eb = (int)(e.rect.width * e.health / 50);
            g.setColor(Color.DARK_GRAY); 
            g.fillRect(e.rect.x, e.rect.y - 6, e.rect.width, 3);
            g.setColor(Color.RED);       
            g.fillRect(e.rect.x, e.rect.y - 6, eb, 3);
        }
        
        // 敵人子彈繪製
        for (EnemyBullet eb : enemyBullets) { 
            if (laserImg != null) {
                g.drawImage(laserImg, eb.getRect().x, eb.getRect().y, EnemyBullet.W, EnemyBullet.H, null);
            } else {
                g.setColor(Color.RED);
                g.fillRect(eb.getRect().x, eb.getRect().y, EnemyBullet.W, EnemyBullet.H);
            }
        }
        
        // 傷害文字繪製
        g.setColor(Color.WHITE); 
        g.setFont(new Font("Arial", Font.BOLD, 14));
        for (DamageText dt : damageTexts) {
            g.drawString(dt.text, dt.x, dt.y);
        }
    }
    
    private void updateGame() {
        if (leveling) return;

        // 玩家移動
        if (left && playerX > 0) playerX -= 5;
        if (right && playerX < PANEL_WIDTH - PLAYER_WIDTH) playerX += 5;
        if (up && playerY > 0) playerY -= 5;
        if (down && playerY < PANEL_HEIGHT - PLAYER_HEIGHT) playerY += 5;

        long now = System.currentTimeMillis();

        // 射擊
        if (now - lastFireTime >= 1000 / playerAttackSpeed) {
            int shots = 1 + multiShotLevel;
            double bx = playerX + PLAYER_WIDTH / 2.0 - Bullet.W / 2.0, by = playerY;
            for (int i = 0; i < shots; i++) {
                bullets.add(new Bullet(bx, by, 0, -10));
                for (int k = 1; k <= diagonalShotLevel; k++) {
                    bullets.add(new Bullet(bx, by, -k * 1.0, -10));
                    bullets.add(new Bullet(bx, by, k * 1.0, -10));
                }
            }
            lastFireTime = now;
            space = false;
            playSound("/resources/xf9c1-23hih.wav");
        }
        
        // 更新子彈
        bullets.removeIf(b -> { 
            b.update(); 
            return b.y < 0 || b.x < 0 || b.x > PANEL_WIDTH; 
        });

        // 火球
        int fbCd = Math.max(1000, BASE_FIREBALL_COOLDOWN - fireballSkillLevel * 200);
        if (fireballSkillLevel > 0 && fireballKey && now - lastFireballTime >= fbCd) {
            fireballs.add(new Fireball(playerX + PLAYER_WIDTH / 2 - Fireball.SIZE / 2, playerY, 0, -8));
            lastFireballTime = now;
            fireballKey = false;
        }
        fireballs.removeIf(f -> { 
            f.update(); 
            return f.y < 0; 
        });

        // 根據遊戲階段進行不同的更新
        if(currentStage == GameStage.NORMAL) {
            updateNormalStage();
        } else if(currentStage == GameStage.BOSS) {
            updateBossStage();
        }
        
        // 敵人子彈更新
        enemyBullets.removeIf(eb -> { 
            eb.update(); 
            return eb.y > PANEL_HEIGHT || eb.x < 0 || eb.x > PANEL_WIDTH; 
        });

        // 子彈碰撞檢測
        handleBulletCollisions();
        
        // 火球碰撞檢測
        handleFireballCollisions();

        // 敵人子彈打到玩家
        handlePlayerDamage();

        // 更新傷害文字
        for (Iterator<DamageText> di = damageTexts.iterator(); di.hasNext();) {
            DamageText dt = di.next(); 
            dt.y--; 
            if (--dt.life <= 0) di.remove();
        }

        // 經驗與升級觸發
        if (playerXP >= xpToNext) {
            playerXP -= xpToNext;
            playerLevel++;
            xpToNext = (int)(xpToNext * 1.5);
            leveling = true;
            SwingUtilities.invokeLater(this::showLevelUpDialog);
        }
    }
    
    private void updateNormalStage() {
        // 敵人生成
        if (random.nextInt(80) == 0 && enemies.size() < 10) {
            enemies.add(new Enemy(random.nextInt(PANEL_WIDTH - 40), -30, 50, 15, 5));
        }
        
        // 敵人移動與攻擊
        int midY = PANEL_HEIGHT / 2, range = 30, minY = midY - range, maxY = midY + range;
        for (Iterator<Enemy> ei = enemies.iterator(); ei.hasNext();) {
            Enemy e = ei.next();
            if (e.rect.y < minY) {
                e.rect.y += 3;
            } else {
                if (random.nextInt(30) == 0) e.dirX = random.nextBoolean() ? 1 : -1;
                if (random.nextInt(30) == 0) e.dirY = random.nextBoolean() ? 1 : -1;
                e.rect.x = Math.max(0, Math.min(e.rect.x + e.dirX * 1, PANEL_WIDTH - e.rect.width));
                e.rect.y = Math.max(minY, Math.min(e.rect.y + e.dirY * 1, maxY));
                
                if (random.nextInt(150) == 0) {
                    double sx = e.rect.x + e.rect.width / 2, sy = e.rect.y + e.rect.height;
                    double dx = (playerX + PLAYER_WIDTH / 2) - sx, dy = (playerY + PLAYER_HEIGHT / 2) - sy;
                    double dist = Math.hypot(dx, dy), spd = 5;
                    enemyBullets.add(new EnemyBullet(sx, sy, dx / dist * spd, dy / dist * spd));
                }
            }
        }
        
        // 檢查是否該出現Boss
        if(enemiesKilled >= ENEMIES_BEFORE_BOSS) {
            enterBossStage();
        }
    }
    
    private void updateBossStage() {
        if(currentBoss != null) {
            currentBoss.update();
        }
    }
    
    private void enterBossStage() {
        currentStage = GameStage.BOSS;
        bossNumber++;
        currentBoss = new Boss(bossNumber);
        enemies.clear(); // 清除所有小怪
        enemyBullets.clear(); // 清除敵人子彈
    }
    
    private void defeatBoss() {
        currentBoss = null;
        currentStage = GameStage.NORMAL;
        enemiesKilled = 0; // 重置擊殺計數
        
        if(bossNumber >= 3) {
            // 遊戲通關
            timer.stop();
            JOptionPane.showMessageDialog(this, "恭喜！您已經擊敗了所有Boss！\n遊戲通關！", "恭喜過關", JOptionPane.INFORMATION_MESSAGE);
            if(mainFrame != null) {
                mainFrame.showScreen("Menu");
            }
        }
    }
    
    private void handleBulletCollisions() {
        for (Iterator<Bullet> bi = bullets.iterator(); bi.hasNext();) {
            Bullet b = bi.next();
            boolean hit = false;

            // 子彈打Boss
            if(currentBoss != null && b.getRect().intersects(currentBoss.rect)) {
                bi.remove();
                hit = true;
                
                double raw = playerAttack;
                double actual = raw * (100.0 / (100.0 + currentBoss.defense));
                int dmg = (int) actual;
                if (dmg > 0) {
                    currentBoss.health -= dmg;
                    damageTexts.add(new DamageText("-" + dmg, 
                                  currentBoss.rect.x + currentBoss.rect.width / 2, 
                                  currentBoss.rect.y));
                    
                    // 回血
                    int heal = (int) Math.round(actual * 0.1);
                    playerHealth = Math.min(playerMaxHealth, playerHealth + heal);
                    if (heal > 0) {
                        damageTexts.add(new DamageText("+" + heal, playerX + PLAYER_WIDTH / 2, playerY));
                    }
                    
                    if(currentBoss.health <= 0) {
                        playerXP += 200; // Boss給更多經驗
                        defeatBoss();
                    }
                }
            }
            
            if(hit) continue;

            // 子彈打小怪 (原邏輯保持不變，但加上擊殺計數)
            for (Enemy e : new ArrayList<>(enemies)) {
                if (b.getRect().intersects(e.rect)) {
                    bi.remove();
                    hit = true;

                    double raw = playerAttack;
                    double actual = raw * (100.0 / (100.0 + e.defense));
                    int dmg = (int) actual;
                    if (dmg > 0) {
                        e.health -= dmg;
                        damageTexts.add(new DamageText("-" + dmg, e.rect.x + e.rect.width / 2, e.rect.y));
                        int heal = (int) Math.round(actual * 0.1);
                        playerHealth = Math.min(playerMaxHealth, playerHealth + heal);
                        if (heal > 0) {
                            damageTexts.add(new DamageText("+" + heal, playerX + PLAYER_WIDTH / 2, playerY));
                        }
                    }

                    // 連鎖攻擊邏輯保持不變...
                    if (chainAttackLevel > 0 && dmg > 0) {
                        List<Enemy> snap = new ArrayList<>(enemies);
                        Point src = new Point(e.rect.x + e.rect.width / 2, e.rect.y + e.rect.height / 2);
                        Enemy closest = null; 
                        double minDist = Double.MAX_VALUE;
                        
                        for (Enemy o : snap) {
                            if (o != e) {
                                double dx = o.rect.getCenterX() - src.x;
                                double dy = o.rect.getCenterY() - src.y;
                                double d = Math.hypot(dx, dy);
                                if (d < minDist) { 
                                    minDist = d; 
                                    closest = o; 
                                }
                            }
                        }
                        
                        if (closest != null) {
                            double rawBounce = playerAttack * chainAttackLevel;
                            double actBounce = rawBounce * (100.0 / (100.0 + closest.defense));
                            int bounceDmg = (int) actBounce;
                            if (bounceDmg > 0) {
                                closest.health -= bounceDmg;
                                damageTexts.add(new DamageText("*" + bounceDmg,
                                        closest.rect.x + closest.rect.width / 2,
                                        closest.rect.y));
                                if (closest.health <= 0) {
                                    enemies.remove(closest);
                                    playerXP += 50;
                                }
                            }
                        }
                    }

                    // 死亡連鎖
                    if (e.health <= 0) {
                        enemies.remove(e);
			enemiesKilled++;
                        playerXP += 50;
                        
                        if (deathChainLevel > 0) {
                            List<Enemy> snap2 = new ArrayList<>(enemies);
                            Queue<Point> q = new LinkedList<>();
                            q.add(new Point(e.rect.x + e.rect.width / 2, e.rect.y + e.rect.height / 2));
                            double radius = 100;
                            
                            while (!q.isEmpty()) {
                                Point center = q.poll();
                                Enemy c2 = null; 
                                double md = Double.MAX_VALUE;
                                
                                for (Enemy o2 : snap2) {
                                    double dx = o2.rect.getCenterX() - center.x;
                                    double dy = o2.rect.getCenterY() - center.y;
                                    double dist = Math.hypot(dx, dy);
                                    if (dist > 0 && dist <= radius && dist < md) {
                                        md = dist; 
                                        c2 = o2;
                                    }
                                }
                                
                                if (c2 == null) break;
                                
                                double rawC = playerAttack * 0.8 * deathChainLevel;
                                double actC = rawC * (100.0 / (100.0 + c2.defense));
                                int dD = (int) actC;
                                
                                if (dD > 0) {
                                    c2.health -= dD;
                                    damageTexts.add(new DamageText("#" + dD,
                                            c2.rect.x + c2.rect.width / 2,
                                            c2.rect.y));
                                    if (c2.health <= 0) {
                                        enemies.remove(c2);
                                        snap2.remove(c2);
                                        playerXP += 50;
                                        q.add(new Point(
                                                c2.rect.x + c2.rect.width / 2,
                                                c2.rect.y + c2.rect.height / 2
                                        ));
                                    }
                                }
                            }
                        }
                    }

                    break;
                }
            }
            if (hit) continue;
	}
    }
    
     private void playSound(String soundPath) {
        new Thread(() -> {
            try {
                java.net.URL soundURL = getClass().getResource(soundPath);
                if (soundURL != null) {
                    Clip c = AudioSystem.getClip();
                    c.open(AudioSystem.getAudioInputStream(soundURL));
                    c.start();
                }
            } catch (Exception e) {
                System.out.println("Sound not found: " + soundPath);
            }
        }).start();
    }
    
    // 顯示升級對話框的邏輯保持不變...
    private void showLevelUpDialog() {
        pauseStartTime = System.currentTimeMillis();
        
        // 計算此刻剩餘冷卻
        long now = System.currentTimeMillis();
        int fbCd = Math.max(1000, BASE_FIREBALL_COOLDOWN - fireballSkillLevel * 200);
        pausedRemainingCd = Math.max(0, fbCd - (now - lastFireballTime));

        // 開始計時凍結
        pauseStartTime = now;
        space = false;
        fireballKey = false;

        // 準備選項
        List<String> opts = new ArrayList<>();
        if (playerLevel % 5 == 0) {
            for (SpecialAbility s : SpecialAbility.values()) {
                boolean ok = switch (s) {
                    case MULTI_SHOT -> multiShotLevel < 3;
                    case CHAIN_ATTACK -> chainAttackLevel < 3;
                    case FIREBALL -> fireballSkillLevel < 3;
                    case DIAGONAL_SHOT -> diagonalShotLevel < 3;
                    case DEATH_CHAIN -> deathChainLevel < 3;
                };
                if (ok) opts.add(s.name());
            }
        }
        if (opts.size() < 3) {
            String[] base = {"Max Health", "Attack", "Attack Speed", "Defense"};
            Collections.shuffle(Arrays.asList(base));
            for (int i = 0; i < 3; i++) opts.add(base[i]);
        }
        Collections.shuffle(opts);
        Object[] options = opts.subList(0, 3).toArray();

        // 建立自訂 JOptionPane
        JOptionPane pane = new JOptionPane(
                "選擇提升項目",
                JOptionPane.INFORMATION_MESSAGE,
                JOptionPane.DEFAULT_OPTION,
                null,
                options,
                options[0]
        );

        JDialog dialog = pane.createDialog(this, "升級");
        dialog.getRootPane().setDefaultButton(null);

        JRootPane root = dialog.getRootPane();
        int[] contexts = {
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,
                JComponent.WHEN_IN_FOCUSED_WINDOW,
                JComponent.WHEN_FOCUSED
        };
        for (int ctx : contexts) {
            InputMap im = root.getInputMap(ctx);
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "none");
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "none");
        }

        KeyboardFocusManager mgr = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        KeyEventDispatcher blockSpace = new KeyEventDispatcher() {
            public boolean dispatchKeyEvent(KeyEvent e) {
                if (dialog.isVisible() && e.getKeyCode() == KeyEvent.VK_SPACE) {
                    return true;
                }
                return false;
            }
        };
        mgr.addKeyEventDispatcher(blockSpace);

        dialog.setVisible(true);  // Modal

        mgr.removeKeyEventDispatcher(blockSpace);

        long paused = System.currentTimeMillis() - pauseStartTime;
        lastFireballTime += paused;


        Object val = pane.getValue();

        // 找到使用者選了哪個
        int sel = Arrays.asList(options).indexOf(val);
        if (sel < 0) sel = 0;
        String choice = opts.get(sel);

        // 根據選項升級
        switch (choice) {
            case "Max Health"    -> { playerMaxHealth += 40; playerHealth = Math.min(playerHealth + 40, playerMaxHealth); }
            case "Attack"        -> playerAttack += 5;
            case "Attack Speed"  -> playerAttackSpeed *= 1.2;
            case "Defense"       -> playerDefense +=10;
            case "MULTI_SHOT"    -> multiShotLevel++;
            case "CHAIN_ATTACK"  -> chainAttackLevel++;
            case "FIREBALL"      -> fireballSkillLevel++;
            case "DIAGONAL_SHOT" -> diagonalShotLevel++;
            case "DEATH_CHAIN"   -> deathChainLevel++;
        }

        // 升級結束後再清一次按鍵狀態
        left = right = up = down = false;
        space = fireballKey = false;
        leveling = false;
    }


    @Override public void actionPerformed(ActionEvent e){
        updateGame(); repaint();
    }

    @Override public void keyPressed(KeyEvent e){
        switch(e.getKeyCode()){
            case KeyEvent.VK_LEFT, KeyEvent.VK_A   -> left = true;
            case KeyEvent.VK_RIGHT, KeyEvent.VK_D  -> right = true;
            case KeyEvent.VK_UP, KeyEvent.VK_W      -> up = true;
            case KeyEvent.VK_DOWN, KeyEvent.VK_S    -> down = true;
            case KeyEvent.VK_SPACE -> { if (!leveling) space = true; }
            case KeyEvent.VK_F     -> { if (!leveling) fireballKey = true; }
        }
    }

    @Override public void keyReleased(KeyEvent e){
        switch(e.getKeyCode()){
            case KeyEvent.VK_LEFT, KeyEvent.VK_A   -> left = false;
            case KeyEvent.VK_RIGHT,KeyEvent.VK_D   -> right = false;
            case KeyEvent.VK_UP,KeyEvent.VK_W      -> up = false;
            case KeyEvent.VK_DOWN,KeyEvent.VK_S    -> down = false;
            case KeyEvent.VK_SPACE                  -> space = false;
            case KeyEvent.VK_F                      -> fireballKey = false;
        }
    }
    @Override
    public void keyTyped(KeyEvent e) {
    	if (!leveling && e.getKeyChar() == ' ') space = true;
    }
    
    private void handleFireballCollisions() {
    	for (Iterator<Fireball> fi = fireballs.iterator(); fi.hasNext();) {
             Fireball f = fi.next();
             boolean exploded = false;
        
             for (Enemy e : new ArrayList<>(enemies)) {
            	if (f.getRect().intersects(e.rect)) {
                    fi.remove(); 
                    exploded = true;
                    double radius = 50 + 20 * fireballSkillLevel;
                    double eRaw = playerAttack * 3 * fireballSkillLevel;
                    Point epic = new Point(f.getRect().x + Fireball.SIZE / 2, f.getRect().y + Fireball.SIZE / 2);
                
                    for (Enemy a : new ArrayList<>(enemies)) {
                    	double dx = a.rect.getCenterX() - epic.x, dy = a.rect.getCenterY() - epic.y;
                    	if (Math.hypot(dx, dy) <= radius) {
                            double act = eRaw * (100 / (100 + a.defense));
                            int d = (int) act;
                            if (d > 0) {
                            	a.health -= d;
                            	damageTexts.add(new DamageText("🔥" + d, a.rect.x + a.rect.width / 2, a.rect.y));
                            	if (a.health <= 0) { 
                                    enemies.remove(a); 
                                    playerXP += 50; 
                            	}
                            }
                    	}
                    }
                
                    // 爆炸音效
                    playSound("/resources/explosion.wav");
                    break;
            	}
            }
            if (exploded) continue;
    	}
    }	

    private void handlePlayerDamage() {
    	Rectangle prect = new Rectangle(playerX + 10, playerY + 5, 30, 20);
    	for (Iterator<EnemyBullet> ei = enemyBullets.iterator(); ei.hasNext();) {
            EnemyBullet eb = ei.next();
            if (eb.getRect().intersects(prect)) {
            	ei.remove();
            	double raw = 15, act = raw * (100 / (100 + playerDefense));
            	int d = (int) act;
            	if (d > 0) {
                    playerHealth = Math.max(0, playerHealth - d);
                    damageTexts.add(new DamageText("-" + d, playerX + PLAYER_WIDTH / 2, playerY));
                    if (playerHealth <= 0) {
                    	timer.stop();
                    	int result = JOptionPane.showConfirmDialog(this, 
                            "Game Over! Return to main menu?", 
                            "遊戲結束", 
                            JOptionPane.YES_NO_OPTION);
                    	if (result == JOptionPane.YES_OPTION && mainFrame != null) {
                            mainFrame.showScreen("Menu");
                    	} else {
                            System.exit(0);
                    	}
                    	return;
                    }
                }
            }
    	}
    }
} // end class GamePanel