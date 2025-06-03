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
    private MainMenuPanel mainMenuPanel;
        
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
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
        }
        public void update() { x += vx; y += vy; }
        public Rectangle getRect() { return new Rectangle((int)x, (int)y, W, H); }
    }
    
    private class BossBullet {
        double x, y, vx, vy;
        static final int SIZE = 8;
        double attack;
        public BossBullet(double x, double y, double vx, double vy, double atk) {
            this.x = x; this.y = y; this.vx = vx; this.vy = vy; this.attack = atk;
        }
        public void update() { x += vx; y += vy; }
        public Rectangle getRect() { return new Rectangle((int)x, (int)y, SIZE, SIZE); }
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
    
    // Boss2 专用 Fireball：homing 玩家，距离≤80 切换直线
    private class BossFireball {
        double x, y, vx, vy;
        static final int SIZE = 16;
        boolean straight = false;
        public BossFireball(double x, double y, double vx, double vy) {
            this.x = x; this.y = y; this.vx = vx; this.vy = vy;
        }
        public void update(double playerX, double playerY) {
            if (!straight) {
                double dx = playerX - x, dy = playerY - y;
                double dist = Math.hypot(dx, dy);
                if (dist > 80) {
                    double spd = 8.0;
                    vx = dx / dist * spd;
                    vy = dy / dist * spd;
                } else {
                    straight = true;
                }
            }
            x += vx; y += vy;
        }
        public Rectangle getRect() { return new Rectangle((int)x, (int)y, SIZE, SIZE); }
    }
    private class EnemyBullet { 
        double x, y, vx, vy;
        double attack;            // 原始攻擊力
        static final int W = 5, H = 10;
        public EnemyBullet(double x, double y, double vx, double vy, double atk) {
            this.x = x; this.y = y; this.vx = vx; this.vy = vy; this.attack = atk;
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

    private Image playerImg, enemyImg, bgImg, laserImg, fireballImg;
    private Image flashImg;// 閃電特效圖
    private class Lightning {
        public double x, y;
        public int width, height;
        public long spawnTime;

        public Lightning(double x, double y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.spawnTime = System.currentTimeMillis();
        }

        public Rectangle getRect() {
            return new Rectangle((int)x, (int)y, width, height);
        }
    }
    
    private Image bossImgs[] = new Image[3];

    // 關卡與 Boss 相關
    private int currentBossIndex = 0;         // 0,1,2
    private Boss[] bosses = new Boss[3];
    private int waveNumber = 1;               // 1 到 5
    private long waveStartTime = 0;
    private long wavePauseStartTime = 0;
    private long pausedWaveRemaining = 0;
    private boolean wavePaused = false;
    private boolean bossActive = false;
    private int roundCount = 1;               // 第幾輪循環
    private final int WAVES_PER_ROUND = 5;
    private final long WAVE_INTERVAL_MS = 10_000; // 每波間隔 10 秒
    
    public StageModePanel(MainMenuPanel menu, MainFrame frame) {
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setFocusable(true);
        addKeyListener(this);
        playerX = PANEL_WIDTH / 2 - PLAYER_WIDTH / 2;
        playerY = PANEL_HEIGHT - PLAYER_HEIGHT - 10;
        // 載入圖片
        playerImg    = new ImageIcon("resources/player2.png").getImage();
        enemyImg     = new ImageIcon("resources/enemy.png").getImage();
        bgImg        = new ImageIcon("resources/background.png").getImage();
        laserImg     = new ImageIcon("resources/laser.png").getImage();
        fireballImg  = new ImageIcon("resources/fireball.png").getImage();
        flashImg     = new ImageIcon("resources/flash.png").getImage();
        // 載入 Boss 圖片
        bossImgs[0] = new ImageIcon("resources/boss1.png").getImage();
        bossImgs[1] = new ImageIcon("resources/boss2.png").getImage();
        bossImgs[2] = new ImageIcon("resources/boss3.png").getImage();
        // 初始化 Boss 實例
        bosses[0] = new Boss1(bossImgs[0], 1000, 40, 50, 2.0);
        bosses[1] = new Boss2(bossImgs[1], 1500, 45, 30, 1.5);
        bosses[2] = new Boss3(bossImgs[2], 2000, 30, 100, 1.2);
        // 第一波小怪開始
        waveStartTime = System.currentTimeMillis();

        // 背景音樂
        timer = new Timer(15, this);
        try {
            java.net.URL bgmURL = new File("resources/8hp8q-bq1d0.wav").toURI().toURL();
            if (bgmURL != null) {
                AudioInputStream ais = AudioSystem.getAudioInputStream(bgmURL);
                Clip c = AudioSystem.getClip();
                c.open(ais);
                c.loop(Clip.LOOP_CONTINUOUSLY);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // ── 背景与玩家部分保持不变 ──
        g.drawImage(bgImg, 0, 0, PANEL_WIDTH, PANEL_HEIGHT, null);
        g.drawImage(playerImg, playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT, null);
        int hpW = (int)(PLAYER_WIDTH * playerHealth / playerMaxHealth);
        g.setColor(Color.DARK_GRAY);
        g.fillRect(playerX, playerY - 8, PLAYER_WIDTH, 5);
        g.setColor(Color.GREEN);
        g.fillRect(playerX, playerY - 8, hpW, 5);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.drawString(String.format("HP:%.0f/%.0f", playerHealth, playerMaxHealth), 10, 20);
        g.drawString(String.format("ATK:%.0f DEF:%.0f SPD:%.1f", playerAttack, playerDefense, playerAttackSpeed),
                10, 40);
        g.drawString(String.format("LV:%d XP:%d/%d", playerLevel, playerXP, xpToNext), 10, 60);

        long now = System.currentTimeMillis();

        // ── 如果还没进入 Boss，显示“Wave: x/5” 以及本波剩余秒数 ──
        if (!bossActive) {
            long sinceThisWave = now - waveStartTime;
            long remainMs = wavePaused
                    ? pausedWaveRemaining
                    : Math.max(0, WAVE_INTERVAL_MS - sinceThisWave);
            double remainSec = remainMs / 1000.0;

            g.drawString("Wave: " + waveNumber + "/" + WAVES_PER_ROUND, 10, 80);
            g.drawString(String.format("Next In: %.1fs", remainSec), 10, 100);
        }

        // ── 火球冷却部分不变 ──
        if (fireballSkillLevel > 0) {
            String cdText;
            if (leveling) {
                double sec = pausedRemainingCd / 1000.0;
                cdText = (pausedRemainingCd <= 0
                        ? "Fireball CD: READY"
                        : String.format("Fireball CD: %.1fs", sec));
            } else {
                int fbCd = Math.max(10000, BASE_FIREBALL_COOLDOWN - fireballSkillLevel * 1000);
                long since = now - lastFireballTime;
                if (since >= fbCd) cdText = "Fireball CD: READY";
                else {
                    double sec = (fbCd - since) / 1000.0;
                    cdText = String.format("Fireball CD: %.1fs", sec);
                }
            }
            g.drawString(cdText, 10, 120);
        }

        // ── 其余绘制：玩家子弹 / 火球 / 小怪 / 小怪子弹 / 伤害文字 ──
        for (Bullet b : bullets) {
            g.drawImage(laserImg, b.getRect().x, b.getRect().y, Bullet.W, Bullet.H, null);
        }
        for (Fireball f : fireballs) {
            g.drawImage(fireballImg, f.getRect().x, f.getRect().y, Fireball.SIZE, Fireball.SIZE, null);
        }
        for (Enemy e : enemies) {
            g.drawImage(enemyImg, e.rect.x, e.rect.y, e.rect.width, e.rect.height, null);
            int eb = (int)(e.rect.width * e.health / 50);
            g.setColor(Color.DARK_GRAY);
            g.fillRect(e.rect.x, e.rect.y - 6, e.rect.width, 5);
            g.setColor(Color.RED);
            g.fillRect(e.rect.x, e.rect.y - 6, eb, 5);
        }
        for (EnemyBullet eb : enemyBullets) {
            g.drawImage(laserImg, eb.getRect().x, eb.getRect().y, EnemyBullet.W, EnemyBullet.H, null);
        }
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        for (DamageText dt : damageTexts) {
            g.drawString(dt.text, dt.x, dt.y);
        }

        // ── 如果正在 Boss 階段，渲染當前 Boss ──
        if (bossActive) {
            bosses[currentBossIndex].render(g);
        }
    }


    private void updateGame() {
        Rectangle prect = new Rectangle(
                playerX + 5,
                playerY + 5,
                PLAYER_WIDTH - 10,
                PLAYER_HEIGHT - 10
        );

        if (leveling) return;

        // 玩家移動
        if (left && playerX > 0) playerX -= 5;
        if (right && playerX < PANEL_WIDTH - PLAYER_WIDTH) playerX += 5;
        if (up && playerY > 0) playerY -= 5;
        if (down && playerY < PANEL_HEIGHT - PLAYER_HEIGHT) playerY += 5;

        long now = System.currentTimeMillis();
        long elapsed = now - waveStartTime;

        // ── 一、波次與 Boss 切換 ──
        if (!bossActive) {
            // 每 10 秒跳到下一波並重置 waveStartTime
            if (!wavePaused) {
                long sinceThisWave = now - waveStartTime;
                if (sinceThisWave >= WAVE_INTERVAL_MS) {
                    waveNumber++;
                    waveStartTime = now;
                    if (waveNumber > WAVES_PER_ROUND) {
                        bossActive = true;
                        bosses[currentBossIndex].reset();
                    }
                }
            }
            // 隨機生成小怪（即使進入 Boss 階段也繼續生成）
            if (random.nextInt(80) == 0) {
                double addHP = 50 * (roundCount - 1);
                double addAT = 5 * (roundCount - 1);
                double addDF = 10 * (roundCount - 1);
                double hp  = 50 + addHP;
                double atk = 15 + addAT;
                double def = 10 + addDF;
                enemies.add(new Enemy(
                        random.nextInt(PANEL_WIDTH - 40),
                        -30,
                        hp, atk, def
                ));
            }
        } else {
            // 正在對戰 Boss
            Boss current = bosses[currentBossIndex];
            current.update(now,
                    playerX + PLAYER_WIDTH / 2.0,
                    playerY + PLAYER_HEIGHT / 2.0
            );
            if (current.isDead()) {
                if (currentBossIndex < bosses.length - 1) {
                    currentBossIndex++;
                    bossActive = false;
                    waveNumber = 1;
                    waveStartTime = now;
                    roundCount++;
                    enemies.clear();
                } else {
                    timer.stop();
                    JOptionPane.showMessageDialog(this, "過關成功", "恭喜", JOptionPane.PLAIN_MESSAGE);
                    System.exit(0);
                    return;
                }
            }
        }

        // ── 二、玩家射擊 (多重 + 斜射) ──
        if (space && now - lastFireTime >= 1000 / playerAttackSpeed) {
            int shots = 1 + multiShotLevel;
            double bx = playerX + PLAYER_WIDTH / 2.0 - Bullet.W / 2.0;
            double by = playerY;
            for (int i = 0; i < shots; i++) {
                bullets.add(new Bullet(bx, by, 0, -10));
                for (int k = 1; k <= diagonalShotLevel; k++) {
                    bullets.add(new Bullet(bx, by, -k * 1.0, -10));
                    bullets.add(new Bullet(bx, by,  k * 1.0, -10));
                }
            }
            lastFireTime = now;
            space = false;
            new Thread(() -> {
                try {
		    File soundFile = new File("resources/xf9c1-23hih.wav");
		    AudioInputStream ais = AudioSystem.getAudioInputStream(soundFile);
                    Clip c = AudioSystem.getClip();
                    c.open(ais);
                    c.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
        bullets.removeIf(b -> {
            b.update();
            return (b.y < 0 || b.x < 0 || b.x > PANEL_WIDTH);
        });

        // ── 三、玩家火球冷卻與發射 ──
        int fbCd = Math.max(10000, BASE_FIREBALL_COOLDOWN - fireballSkillLevel * 1000);
        if (fireballSkillLevel > 0 && fireballKey && now - lastFireballTime >= fbCd) {
            fireballs.add(new Fireball(
                    playerX + PLAYER_WIDTH / 2.0 - Fireball.SIZE / 2.0,
                    playerY,
                    0, -8
            ));
            lastFireballTime = now;
            fireballKey = false;
        }
        for (Iterator<Fireball> it = fireballs.iterator(); it.hasNext();) {
            Fireball f = it.next();
            f.update();
            if (f.y < 0) {
                it.remove();
            }
        }

        // ── 四、小怪移動與攻擊 ──
        int midY = PANEL_HEIGHT / 2, range = 30;
        int minY = midY - range, maxY = midY + range;
        for (Iterator<Enemy> ei = enemies.iterator(); ei.hasNext();) {
            Enemy e = ei.next();
            if (e.rect.y < minY) {
                e.rect.y += 3;
            } else {
                if (random.nextInt(30) == 0) e.dirX = random.nextBoolean()?1:-1;
                if (random.nextInt(30) == 0) e.dirY = random.nextBoolean()?1:-1;
                e.rect.x = Math.max(0, Math.min(e.rect.x + e.dirX * 2, PANEL_WIDTH - e.rect.width));
                e.rect.y = Math.max(minY, Math.min(e.rect.y + e.dirY * 2, maxY));
                // 小怪在任何階段都持續隨機射擊
                if (random.nextInt(150) == 0) {
                    double sx = e.rect.x + e.rect.width / 2.0;
                    double sy = e.rect.y + e.rect.height;
                    double dx = (playerX + PLAYER_WIDTH / 2.0) - sx;
                    double dy = (playerY + PLAYER_HEIGHT / 2.0) - sy;
                    double dist = Math.hypot(dx, dy), spd = 5;
                    enemyBullets.add(new EnemyBullet(
                            sx, sy,
                            dx / dist * spd, dy / dist * spd,
                            e.attack
                    ));
                }
            }
        }
        enemyBullets.removeIf(eb -> {
            eb.update();
            return (eb.y > PANEL_HEIGHT || eb.x < 0 || eb.x > PANEL_WIDTH);
        });

        // ── 五、玩家子彈擊中小怪 & Chain/Death Chain ──
        for (Iterator<Bullet> bi = bullets.iterator(); bi.hasNext();) {
            Bullet b = bi.next();
            boolean hit = false;
            for (Enemy e : new ArrayList<>(enemies)) {
                if (b.getRect().intersects(e.rect)) {
                    bi.remove();
                    hit = true;
                    double raw = playerAttack;
                    double actual = raw * (100.0 / (100.0 + e.defense));
                    int dmg = (int) actual;
                    if (dmg > 0) {
                        e.health -= dmg;
                        damageTexts.add(new DamageText(
                                "-" + dmg,
                                e.rect.x + e.rect.width / 2,
                                e.rect.y
                        ));
                        int heal = (int) Math.round(actual * 0.1);
                        playerHealth = Math.min(playerMaxHealth, playerHealth + heal);
                        if (heal > 0) {
                            damageTexts.add(new DamageText(
                                    "+" + heal,
                                    playerX + PLAYER_WIDTH / 2,
                                    playerY
                            ));
                        }
                    }
                    // Chain Attack（單次彈射）
                    if (chainAttackLevel > 0 && dmg > 0) {
                        List<Enemy> snap = new ArrayList<>(enemies);
                        Point src = new Point(
                                e.rect.x + e.rect.width / 2,
                                e.rect.y + e.rect.height / 2
                        );
                        Enemy closest = null; double minDist = Double.MAX_VALUE;
                        for (Enemy o : snap) {
                            if (o != e) {
                                double dx = o.rect.getCenterX() - src.x;
                                double dy = o.rect.getCenterY() - src.y;
                                double d2 = Math.hypot(dx, dy);
                                if (d2 < minDist) {
                                    minDist = d2; closest = o;
                                }
                            }
                        }
                        if (closest != null) {
                            double rawBounce = playerAttack * chainAttackLevel;
                            double actBounce = rawBounce * (100.0 / (100.0 + closest.defense));
                            int bounceDmg = (int) actBounce;
                            if (bounceDmg > 0) {
                                closest.health -= bounceDmg;
                                damageTexts.add(new DamageText(
                                        "*" + bounceDmg,
                                        closest.rect.x + closest.rect.width / 2,
                                        closest.rect.y
                                ));
                                if (closest.health <= 0) {
                                    enemies.remove(closest);
                                    playerXP += 50;
                                }
                            }
                        }
                    }
                    // Death Chain（無上限連鎖）
                    if (e.health <= 0) {
                        enemies.remove(e);
                        playerXP += 50;
                        if (deathChainLevel > 0) {
                            List<Enemy> snap2 = new ArrayList<>(enemies);
                            Queue<Point> q = new LinkedList<>();
                            q.add(new Point(
                                    e.rect.x + e.rect.width / 2,
                                    e.rect.y + e.rect.height / 2
                            ));
                            double radius = 100;
                            while (!q.isEmpty()) {
                                Point center = q.poll();
                                Enemy c2 = null; double md = Double.MAX_VALUE;
                                for (Enemy o2 : snap2) {
                                    double dx = o2.rect.getCenterX() - center.x;
                                    double dy = o2.rect.getCenterY() - center.y;
                                    double dist2 = Math.hypot(dx, dy);
                                    if (dist2 > 0 && dist2 <= radius && dist2 < md) {
                                        md = dist2; c2 = o2;
                                    }
                                }
                                if (c2 == null) break;
                                double rawC = playerAttack * 0.8 * deathChainLevel;
                                double actC = rawC * (100.0 / (100.0 + c2.defense));
                                int dD = (int) actC;
                                if (dD > 0) {
                                    c2.health -= dD;
                                    damageTexts.add(new DamageText(
                                            "#" + dD,
                                            c2.rect.x + c2.rect.width / 2,
                                            c2.rect.y
                                    ));
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

            // ── 六、玩家子彈擊中 Boss ──
            if (bossActive) {
                Boss current = bosses[currentBossIndex];
                Rectangle bossRect = new Rectangle(
                        (int) current.x,
                        (int) current.y,
                        current.width,
                        current.height
                );
                if (b.getRect().intersects(bossRect)) {
                    bi.remove();
                    double raw = playerAttack;
                    double actual = raw * (100.0 / (100.0 + current.defense));
                    int dmg = (int) actual;
                    if (dmg > 0) {
                        current.curHP -= dmg;
                        damageTexts.add(new DamageText(
                                "-" + dmg,
                                (int) current.x + current.width / 2,
                                (int) current.y
                        ));
                    }
                    if (current.curHP <= 0) {
                        if (currentBossIndex < bosses.length - 1) {
                            currentBossIndex++;
                            bossActive = false;
                            waveNumber = 1;
                            waveStartTime = now;
                            roundCount++;
                            enemies.clear();
                        } else {
                            timer.stop();
                            JOptionPane.showMessageDialog(
                                    this,
                                    "過關成功",
                                    "恭喜",
                                    JOptionPane.PLAIN_MESSAGE
                            );
                            System.exit(0);
                            return;
                        }
                    }
                    continue;
                }
            }
        }

        // ── 七、玩家火球對小怪 & Boss 爆炸判定 ──
        for (Iterator<Fireball> fi = fireballs.iterator(); fi.hasNext();) {
            Fireball f = fi.next();
            boolean exploded = false;
            // 7.1 對小怪爆炸
            for (Enemy e : new ArrayList<>(enemies)) {
                if (f.getRect().intersects(e.rect)) {
                    fi.remove();
                    exploded = true;
                    double radius = 50 + 20 * fireballSkillLevel;
                    double eRaw = playerAttack * 3 * fireballSkillLevel;
                    Point epic = new Point(
                            f.getRect().x + Fireball.SIZE / 2,
                            f.getRect().y + Fireball.SIZE / 2
                    );
                    for (Enemy a : new ArrayList<>(enemies)) {
                        double dx = a.rect.getCenterX() - epic.x;
                        double dy = a.rect.getCenterY() - epic.y;
                        if (Math.hypot(dx, dy) <= radius) {
                            double act = eRaw * (100.0 / (100.0 + a.defense));
                            int d = (int) act;
                            if (d > 0) {
                                a.health -= d;
                                damageTexts.add(new DamageText(
                                        "🔥" + d,
                                        a.rect.x + a.rect.width / 2,
                                        a.rect.y
                                ));
                                if (a.health <= 0) {
                                    enemies.remove(a);
                                    playerXP += 50;
                                }
                            }
                        }
                    }
                    new Thread(() -> {
                        try {
			    File soundFile = new File("resource/explosion.wav");
			    AudioInputStream ais = AudioSystem.getAudioInputStream(soundFile);
                            Clip c = AudioSystem.getClip();
                            c.open(ais);              
                            c.start();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }).start();
                    break;
                }
            }
            if (exploded) continue;

            // 7.2 對 Boss 直接命中
            if (bossActive) {
                Boss current = bosses[currentBossIndex];
                Rectangle bossRect = new Rectangle(
                        (int) current.x,
                        (int) current.y,
                        current.width,
                        current.height
                );
                if (f.getRect().intersects(bossRect)) {
                    fi.remove();
                    double rawB = playerAttack * 3 * fireballSkillLevel; // 火球對 Boss 3 倍
                    double actualB = rawB * (100.0 / (100.0 + current.defense));
                    int dmgB = (int) actualB;
                    if (dmgB > 0) {
                        current.curHP -= dmgB;
                        damageTexts.add(new DamageText(
                                "-" + dmgB,
                                (int) current.x + current.width / 2,
                                (int) current.y
                        ));
                    }
                    if (current.curHP <= 0) {
                        if (currentBossIndex < bosses.length - 1) {
                            currentBossIndex++;
                            bossActive = false;
                            waveNumber = 1;
                            waveStartTime = now;
                            roundCount++;
                            enemies.clear();
                        } else {
                            timer.stop();
                            JOptionPane.showMessageDialog(
                                    this,
                                    "過關成功",
                                    "恭喜",
                                    JOptionPane.PLAIN_MESSAGE
                            );
                            System.exit(0);
                            return;
                        }
                    }
                    continue;
                }
            }
        }

        // ── 八、小怪子彈打到玩家 + Boss2 火球／閃電 判斷 ──


        // 1) 小怪的子彈判斷
        for (Iterator<EnemyBullet> ei = enemyBullets.iterator(); ei.hasNext();) {
            EnemyBullet eb = ei.next();
            eb.update();
            if (eb.getRect().intersects(prect)) {
                ei.remove();
                double raw = eb.attack;
                double actual = raw * (100.0 / (100.0 + playerDefense));
                int d_ = (int) actual;
                if (d_ > 0) {
                    playerHealth = Math.max(0, playerHealth - d_);
                    damageTexts.add(new DamageText(
                            "-" + d_,
                            playerX + PLAYER_WIDTH / 2,
                            playerY
                    ));
                    if (playerHealth <= 0) {
                        timer.stop();
                        JOptionPane.showMessageDialog(
                                this,
                                "Game Over",
                                "結束",
                                JOptionPane.PLAIN_MESSAGE
                        );
                        System.exit(0);
                    }
                }
            }
        }
// ── **新增：Boss3 子彈打到玩家** ──
        if (bossActive && currentBossIndex == 2) {
            Boss3 b3 = (Boss3) bosses[2];
            for (Iterator<BossBullet> it = b3.bossBullets.iterator(); it.hasNext();) {
                BossBullet bb = it.next();
                // 每幀記得更新子彈位置
                bb.update();
                // 如果碰到玩家的碰撞箱，就扣血並移除這顆子彈
                if (bb.getRect().intersects(prect)) {
                    it.remove();
                    int dmg = (int) bb.attack;
                    playerHealth = Math.max(0, playerHealth - dmg);
                    damageTexts.add(new DamageText(
                            "-" + dmg,
                            playerX + PLAYER_WIDTH / 2,
                            playerY
                    ));
                    if (playerHealth <= 0) {
                        timer.stop();
                        JOptionPane.showMessageDialog(
                                this,
                                "Game Over",
                                "結束",
                                JOptionPane.PLAIN_MESSAGE
                        );
                        System.exit(0);
                    }
                }
            }
            // 再把畫面外的子彈也清掉
            b3.bossBullets.removeIf(bb -> bb.x < 0 || bb.x > PANEL_WIDTH || bb.y < 0 || bb.y > PANEL_HEIGHT);
        }
        // 2) Boss2 火球判斷
        if (bossActive && currentBossIndex == 1) {
            Boss2 b2 = (Boss2) bosses[1];
            for (Iterator<BossFireball> bfi = b2.bossFireballs.iterator(); bfi.hasNext();) {
                BossFireball bf = bfi.next();
                // bf.update(...) 已在 Boss2.update(...) 里调用
                if (bf.getRect().intersects(prect)) {
                    bfi.remove();
                    int dmg = (int) (b2.attack * 1.5);
                    playerHealth = Math.max(playerHealth - dmg, 0);
                    damageTexts.add(new DamageText(
                            "-" + dmg,
                            playerX + PLAYER_WIDTH / 2,
                            playerY
                    ));
                    if (playerHealth <= 0) {
                        timer.stop();
                        JOptionPane.showMessageDialog(
                                this,
                                "Game Over",
                                "結束",
                                JOptionPane.PLAIN_MESSAGE
                        );
                        System.exit(0);
                    }
                }
            }

            // 3) Boss2 閃電判斷
            for (Iterator<Lightning> li = b2.lightnings.iterator(); li.hasNext();) {
                Lightning l = li.next();
                if (l.getRect().intersects(prect)) {
                    li.remove();
                    int dmg = (int) (b2.attack * 5);
                    playerHealth = Math.max(playerHealth - dmg, 0);
                    damageTexts.add(new DamageText(
                            "⚡" + dmg,
                            playerX + PLAYER_WIDTH / 2,
                            playerY
                    ));
                    if (playerHealth <= 0) {
                        timer.stop();
                        JOptionPane.showMessageDialog(
                                this,
                                "Game Over",
                                "結束",
                                JOptionPane.PLAIN_MESSAGE
                        );
                        System.exit(0);
                    }
                }
            }
        }

        // Boss3 子彈打到玩家
        if (bossActive && currentBossIndex == 2) {
            Boss3 b3 = (Boss3) bosses[2];

            for (Iterator<BossBullet> it = b3.bossBullets.iterator(); it.hasNext();) {
                BossBullet bb = it.next();
                // 已經在 Boss3.update(...) 內呼叫 bb.update()，這裡只做碰撞檢查
                if (bb.getRect().intersects(prect)) {
                    it.remove();
                    int dmg = (int) bb.attack;
                    playerHealth = Math.max(0, playerHealth - dmg);
                    damageTexts.add(new DamageText(
                            "-" + dmg,
                            playerX + PLAYER_WIDTH / 2,
                            playerY
                    ));
                    if (playerHealth <= 0) {
                        timer.stop();
                        JOptionPane.showMessageDialog(
                                this,
                                "Game Over",
                                "結束",
                                JOptionPane.PLAIN_MESSAGE
                        );
                        System.exit(0);
                    }
                }
            }
        }
        // ── 九、更新傷害文字生命 ──
        for (Iterator<DamageText> di = damageTexts.iterator(); di.hasNext();) {
            DamageText dt = di.next();
            dt.y--;
            if (--dt.life <= 0) di.remove();
        }

        // ── 十、經驗與升級觸發 ──
        if (playerXP >= xpToNext) {
            playerXP -= xpToNext;
            playerLevel++;
            xpToNext *= 1.5;
            leveling = true;
            SwingUtilities.invokeLater(this::showLevelUpDialog);
        }
    }



    // 顯示升級對話框
    private void showLevelUpDialog() {
        long now = System.currentTimeMillis();

        // 【1】準備暫停波次：計算當前波距離下一波還剩多少毫秒
        if (!wavePaused && !bossActive) {
            long elapsed = now - waveStartTime;
            int currentWave = (int) (elapsed / WAVE_INTERVAL_MS) + 1;
            if (currentWave > waveNumber) currentWave = waveNumber;
            long nextWaveTime = WAVE_INTERVAL_MS * currentWave - elapsed;
            pausedWaveRemaining = nextWaveTime;
            wavePauseStartTime = now;
            wavePaused = true;
        }

        // 【2】計算火球剩餘冷卻
        int fbCd = Math.max(10000, BASE_FIREBALL_COOLDOWN - fireballSkillLevel * 1000);
        pausedRemainingCd = Math.max(0, fbCd - (now - lastFireballTime));
        pauseStartTime = now;

        // 重置按鍵
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

        // 移除 SPACE/ENTER 綁定
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

        dialog.setVisible(true);
        mgr.removeKeyEventDispatcher(blockSpace);

        // 【3】升級結束後，補償波次時間
        if (wavePaused && !bossActive) {
            long paused = System.currentTimeMillis() - wavePauseStartTime;
            waveStartTime = System.currentTimeMillis() - (WAVE_INTERVAL_MS - pausedWaveRemaining);
            wavePaused = false;
        }

        // 【4】升級結束後，補償火球冷卻
        long pausedFb = System.currentTimeMillis() - pauseStartTime;
        lastFireballTime += pausedFb;

        Object val = pane.getValue();
        int sel = Arrays.asList(options).indexOf(val);
        if (sel < 0) sel = 0;
        String choice = opts.get(sel);

        switch (choice) {
            case "Max Health" -> {
                playerMaxHealth += 40;
                playerHealth = Math.min(playerHealth + 40, playerMaxHealth);
            }
            case "Attack" -> playerAttack += 10;
            case "Attack Speed" -> playerAttackSpeed *= 1.2;
            case "Defense" -> playerDefense += 10;
            case "MULTI_SHOT" -> multiShotLevel++;
            case "CHAIN_ATTACK" -> chainAttackLevel++;
            case "FIREBALL" -> fireballSkillLevel++;
            case "DIAGONAL_SHOT" -> diagonalShotLevel++;
            case "DEATH_CHAIN" -> deathChainLevel++;
        }

        left = right = up = down = false;
        space = fireballKey = false;
        leveling = false;
    }
    
    @Override public void actionPerformed(ActionEvent e){
        updateGame(); 
	repaint();
    }

    @Override 
    public void keyPressed(KeyEvent e){
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT, KeyEvent.VK_A -> left = true;
            case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> right = true;
            case KeyEvent.VK_UP, KeyEvent.VK_W -> up = true;
            case KeyEvent.VK_DOWN, KeyEvent.VK_S -> down = true;
            case KeyEvent.VK_SPACE -> {
                if (!leveling) space = true;
            }
            case KeyEvent.VK_F -> {
                if (!leveling) fireballKey = true;
            }
        }
    }

    @Override 
    public void keyReleased(KeyEvent e){
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT, KeyEvent.VK_A -> left = false;
            case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> right = false;
            case KeyEvent.VK_UP, KeyEvent.VK_W -> up = false;
            case KeyEvent.VK_DOWN, KeyEvent.VK_S -> down = false;
            case KeyEvent.VK_SPACE -> space = false;
            case KeyEvent.VK_F -> fireballKey = false;
        }
    }
    @Override
    public void keyTyped(KeyEvent e) {
    	if (!leveling && e.getKeyChar() == ' ') space = true;
    }
    
    // -------------------- 新增：Boss 抽象父類 --------------------
    private abstract class Boss {
        Image img;
        int maxHP, curHP;
        double attack, defense;
        double attackSpeed;
        long lastAttackTime;
        int width, height;
        double x, y;

        public Boss(Image img, int hp, double atk, double def, double atkSpd, int w, int h) {
            this.img = img;
            this.maxHP = hp;
            this.curHP = hp;
            this.attack = atk;
            this.defense = def;
            this.attackSpeed = atkSpd;
            this.width = w;
            this.height = h;
            this.lastAttackTime = 0;
            this.x = PANEL_WIDTH / 2 - w / 2;
            this.y = 80;
        }

        public void reset() {
            this.curHP = this.maxHP;
            this.lastAttackTime = 0;
            this.x = PANEL_WIDTH / 2 - width / 2;
            this.y = 80;
        }

        public void render(Graphics g) {
            g.drawImage(img, (int) x, (int) y, width, height, null);
            int barW = (int) (width * (curHP / (double) maxHP));
            g.setColor(Color.DARK_GRAY);
            g.fillRect((int) x, (int) y - 10, width, 6);
            g.setColor(Color.MAGENTA);
            g.fillRect((int) x, (int) y - 10, barW, 6);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 12));
            g.drawString("Boss HP:" + curHP + "/" + maxHP, (int) x, (int) y - 14);
        }

        public boolean isDead() {
            return curHP <= 0;
        }

        public abstract void update(long now, double playerCenterX, double playerCenterY);

        protected void shootAtPlayer(double pX, double pY) {
            double sx = x + width / 2;
            double sy = y + height;
            double dx = pX - sx, dy = pY - sy;
            double dist = Math.hypot(dx, dy);
            double spd = 5;
            enemyBullets.add(new EnemyBullet(sx, sy, dx / dist * spd, dy / dist * spd,this.attack));
            new Thread(() -> {
                try {
		    File soundFile = new File("resources/xf9c1-23hih.wav");
		    AudioInputStream ais = AudioSystem.getAudioInputStream(soundFile);
                    Clip c = AudioSystem.getClip();
                    c.open(ais);
                    c.start();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }).start();
        }

        protected void shootRadials(int count, double speed) {
            double sx = x + width / 2, sy = y + height / 2;
            for (int i = 0; i < count; i++) {
                double angle = 2 * Math.PI * i / count;
                double vx = Math.cos(angle) * speed;
                double vy = Math.sin(angle) * speed;
                enemyBullets.add(new EnemyBullet(sx, sy, vx, vy,this.attack));
            }
        }
    }

    // -------------------- Boss1 --------------------
    private class Boss1 extends Boss {
        private int burstCount = 0;             // 正在第幾顆子彈要發（1~3）
        private long burstStageStart = 0;       // 這波三連射開始的時間
        private long lastBurstTime = 0;         // 上一次整批三連射開始的時間
        private long lastFullRadialTime = 0;    // 全方位攻擊計時器

        public Boss1(Image img, int hp, double atk, double def, double atkSpd) {
            super(img, hp, atk, def, atkSpd, 60, 80);
        }

        @Override
        public void update(long now, double playerX, double playerY) {
            // Boss 左右小幅擺動
            x += Math.sin(now / 500.0) * 0.5;


            // 如果目前不在三連射階段，且到達下一次觸發連射的時間，就啟動一波三連射
            if (burstCount == 0 && now - lastBurstTime >= 1000 / 1) {
                burstCount = 1;
                burstStageStart = now;
                lastBurstTime = now;
            }
            // 如果正在三連射階段（burstCount = 1,2,3），按照 0.1 秒間隔發出下一顆
            if (burstCount >= 1 && burstCount <= 3) {
                // burstStageStart + (burstCount-1)*100 + 小一點容差
                if (now - burstStageStart >= (burstCount - 1) * 100) {
                    shootAtPlayer(playerX, playerY);
                    burstCount++;
                    // 如果已經發完第三顆，就結束此次三連射
                    if (burstCount > 3) {
                        burstCount = 0;
                    }
                }
            }


            // 每 2 秒發一次 36 發全方位
            if (now - lastFullRadialTime >= 2000) {
                shootRadials(36, 3);
                lastFullRadialTime = now;
            }
        }
    }

    // -------------------- 新增：Boss2 --------------------
    private class Boss2 extends Boss {
        public List<Lightning> lightnings = new ArrayList<>();
        // 用于存放 Boss2 丢出的“BossFireball”
        private final List<BossFireball> bossFireballs = new ArrayList<>();
        private long lastAttackTimeFire = 0;

        // 闪电相关
        private long lastLightningTime = 0;
        private boolean lightningWarning = false;
        private long lightningStart = 0;

        private boolean lightningModeBalls = false;   // false = 直条模式, true = 球形模式
        private boolean lightningActive = false;
        private long lightningEndTime = 0;
        private double lightningTargetX = -1, lightningTargetY = -1;

        // 警告时算出的“6 个球形闪电位置”，存到这里
        private final List<Point> ballPositions = new ArrayList<>(6);

        // 警告图与闪电图资源
        private final Image warningImg = new ImageIcon("resources/warning.png").getImage();
        private final Image flashImg   = new ImageIcon("resources/flash.png").getImage();

        public Boss2(Image img, int hp, double atk, double def, double atkSpd) {
            super(img, hp, atk, def, atkSpd, 100, 70);
            // 传给父类的 width=100, height=70（示例，可以根据实际调整）
        }

        @Override
        public void update(long now, double playerX, double playerY) {
            // —— 1. Boss 轻微左右摆动 ——
            x += Math.cos(now / 600.0) * 0.5;

            // —— 2. 丢火球（3 秒一次，每次 5 颗 BossFireball） ——
            if (now - lastAttackTimeFire >= 3000) {
                for (int i = 0; i < 5; i++) {
                    double sx = x + width / 2 + (Math.random() - 0.5) * 30;
                    double sy = y + height;
                    // 初始速度仍指向玩家
                    double dx = playerX - sx;
                    double dy = playerY - sy;
                    double dist = Math.hypot(dx, dy);
                    double spd = 8.0;
                    bossFireballs.add(new BossFireball(
                            sx, sy,
                            dx / dist * spd,
                            dy / dist * spd
                    ));
                }
                lastAttackTimeFire = now;
            }

            // 更新 BossFireball，并检测是否击中玩家
            for (Iterator<BossFireball> it = bossFireballs.iterator(); it.hasNext();) {
                BossFireball bf = it.next();
                // 传入玩家位置，让它在内部决定是继续 homing 还是切换直线
                bf.update(playerX, playerY);

                // 碰撞判定：如果这颗火球打到玩家就扣血并移除
                Rectangle fireRect = bf.getRect();
                Rectangle playerRect = new Rectangle(
                        (int) playerX, (int) playerY, PLAYER_WIDTH, PLAYER_HEIGHT
                );
                if (fireRect.intersects(playerRect)) {
                    int dmg = (int) (attack * 1.5);  // 火球伤害 = 1.5 倍 boss 攻击
                    playerHealth = Math.max(playerHealth - dmg, 0);
                    damageTexts.add(new DamageText(
                            "-" + dmg,
                            (int) playerX + PLAYER_WIDTH / 2,
                            (int) playerY
                    ));
                    it.remove();
                    if (playerHealth <= 0) {
                        timer.stop();
                        JOptionPane.showMessageDialog(
                                StageModePanel.this,
                                "Game Over",
                                "結束",
                                JOptionPane.PLAIN_MESSAGE
                        );
                        System.exit(0);
                    }
                    continue;
                }

                // 超出屏幕底部就移除
                if (bf.y > PANEL_HEIGHT) {
                    it.remove();
                }
            }

            // —— 3. 闪电预警 & 生效逻辑 ——
            if (!lightningWarning && !lightningActive && now - lastLightningTime >= 7000) {
                // 进入预警阶段
                lightningWarning = true;
                lightningStart = now;
                lightningTargetX = playerX;
                lightningTargetY = playerY;
                lightningModeBalls = random.nextBoolean();

                // 若是球形模式，就先计算并缓存 6 个球的坐标
                if (lightningModeBalls) {
                    ballPositions.clear();
                    double cx = playerX + PLAYER_WIDTH / 2.0;
                    double cy = playerY + PLAYER_HEIGHT / 2.0;
                    // (1) 玩家中心那颗
                    ballPositions.add(new Point((int) cx, (int) cy));
                    // (2) 五角星顶点 5 颗
                    double radius = 60;
                    for (int i = 0; i < 5; i++) {
                        double angle = Math.toRadians(-90 + i * 72);
                        int bx = (int) (cx + Math.cos(angle) * radius);
                        int by = (int) (cy + Math.sin(angle) * radius);
                        ballPositions.add(new Point(bx, by));
                    }
                }
            }

            // 预警 2 秒后，切到“闪电生效”阶段
            if (lightningWarning && !lightningActive) {
                if (now - lightningStart >= 2000) {
                    lightningActive = true;
                    lightningEndTime = now + 500; // 生效 500ms
                }
            }

            // 如果处于“闪电生效”阶段，根据模式对玩家造成伤害
            if (lightningActive) {
                Rectangle playerRect = new Rectangle(
                        (int) playerX, (int) playerY, PLAYER_WIDTH, PLAYER_HEIGHT
                );
                if (!lightningModeBalls) {
                    // —— 直条闪电 ——
                    int lw = 35;
                    int lx = (int) (lightningTargetX + PLAYER_WIDTH / 2.0) - lw / 2;
                    int ly = (int) (y + height);
                    Rectangle lightRect = new Rectangle(
                            lx, ly, lw, PANEL_HEIGHT - ly
                    );
                    if (lightRect.intersects(playerRect)) {
                        int dmg = (int) (attack * 5);
                        playerHealth = Math.max(playerHealth - dmg, 0);
                        damageTexts.add(new DamageText(
                                "-" + dmg,
                                (int) playerX + PLAYER_WIDTH / 2,
                                (int) playerY
                        ));
                        if (playerHealth <= 0) {
                            timer.stop();
                            JOptionPane.showMessageDialog(
                                    StageModePanel.this,
                                    "Game Over",
                                    "結束",
                                    JOptionPane.PLAIN_MESSAGE
                            );
                            System.exit(0);
                        }
                    }
                } else {
                    // —— 球形闪电 ——
                    for (Point p : ballPositions) {
                        Rectangle ballRect = new Rectangle(p.x - 4, p.y - 4, 20, 20);
                        if (ballRect.intersects(playerRect)) {
                            int dmg = (int) (attack * 5);
                            playerHealth = Math.max(playerHealth - dmg, 0);
                            damageTexts.add(new DamageText(
                                    "-" + dmg,
                                    (int) playerX + PLAYER_WIDTH / 2,
                                    (int) playerY
                            ));
                            if (playerHealth <= 0) {
                                timer.stop();
                                JOptionPane.showMessageDialog(
                                        StageModePanel.this,
                                        "Game Over",
                                        "結束",
                                        JOptionPane.PLAIN_MESSAGE
                                );
                                System.exit(0);
                            }
                        }
                    }
                }

                // 生效时间到后，重置状态
                if (now >= lightningEndTime) {
                    lightningActive = false;
                    lightningWarning = false;
                    lastLightningTime = now;
                }
            }
        }

        @Override
        public void render(Graphics g) {
            super.render(g);

            // —— 1. 绘制 Boss2 的火球 ——
            for (BossFireball bf : bossFireballs) {
                g.drawImage(
                        fireballImg,
                        (int) bf.x, (int) bf.y,
                        BossFireball.SIZE, BossFireball.SIZE,
                        null
                );
            }

            // —— 2. 绘制闪电预警 ——
            if (lightningWarning && !lightningActive) {
                if (!lightningModeBalls) {
                    // 直条警告：和闪电区域对齐
                    int lw = 40;
                    int ly = (int) (y + height);
                    int lh = PANEL_HEIGHT - ly;
                    int lx = (int) (lightningTargetX + PLAYER_WIDTH / 2.0) - lw / 2;
                    g.drawImage(
                            warningImg,
                            lx, ly,
                            lw, lh,
                            null
                    );
                } else {
                    // 球形警告：依次绘制缓存的 6 个位置，每颗 32×32
                    for (Point p : ballPositions) {
                        g.drawImage(
                                warningImg,
                                p.x - 4, p.y - 4,
                                32, 32,
                                null
                        );
                    }
                }
            }

            // —— 3. 绘制闪电生效效果 ——
            if (lightningActive) {
                if (!lightningModeBalls) {
                    int lw = 40;
                    int lx = (int) (lightningTargetX + PLAYER_WIDTH / 2.0) - lw / 2;
                    int ly = (int) (y + height);
                    g.drawImage(
                            flashImg,
                            lx, ly,
                            lw, PANEL_HEIGHT - ly,
                            null
                    );
                } else {
                    // 球形闪电：使用缓存的 6 个坐标，每颗 32×32
                    for (Point p : ballPositions) {
                        g.drawImage(
                                flashImg,
                                p.x, p.y,
                                32, 32,
                                null
                        );
                    }
                }
            }
        }
    }





    // -------------------- 新增：Boss3 --------------------
    private class Boss3 extends Boss {
        private final double baseAttack;
        public java.util.List<BossBullet> bossBullets = new java.util.ArrayList<>();
        private long lastSummonTime = 0;
        private final int SUMMON_INTERVAL = 5000;

        private long lastHealTime = 0;     // 記錄上次回血時間

        public Boss3(Image img, int hp, double atk, double def, double atkSpd) {
            super(img, hp, atk, def, atkSpd, 120, 80);
            this.baseAttack = atk;
            this.lastHealTime = System.currentTimeMillis();
        }

        @Override
        public void reset() {
            super.reset();
            this.lastSummonTime = 0;
            this.lastHealTime = System.currentTimeMillis();
        }

        @Override
        public void update(long now, double playerX, double playerY) {
            // 水平小幅擺動
            x += Math.sin(now / 400.0) * 0.5;

            // ────────────────
            // 1) 計算「基礎攻擊間隔」(ms)，以及是否要加速／強化攻擊力
            double baseInterval = 500.0; // 原始間隔 (ms)
            long interval = (long) baseInterval;

            // 如果玩家跑到畫面上半部，間隔乘 0.6 → 攻速約 *1.66
            if (playerY < PANEL_HEIGHT / 2.0) {
                interval = (long) (interval * 0.1);
            }
            if (curHP < maxHP/2.0) {
                interval = (long) (interval * 0.3);
            }

                // 普通對玩家射擊
            if (now - lastAttackTime >= interval) {
                shootAtPlayer(playerX, playerY);
                lastAttackTime = now;
            }

            // ────────────────
            // 2) 每 5 秒召喚 10 隻小怪（使用 roundCount 計算屬性增長）
            if (now - lastSummonTime >= SUMMON_INTERVAL) {
                for (int i = 0; i < 10; i++) {
                    double addHP = 50 * (roundCount - 1);
                    double addAT = 5 * (roundCount - 1);
                    double addDF = 10 * (roundCount - 1);
                    double hp = 50 + addHP;
                    double atk = 15 + addAT;
                    double def = 10 + addDF;
                    double ex = Math.random() * (PANEL_WIDTH - 40);
                    double ey = y + height + Math.random() * 30;
                    enemies.add(new Enemy((int) ex, (int) ey, hp, atk, def));
                }
                lastSummonTime = now;
            }

            // ────────────────
            // 3) 每 30 秒回血一次（回復「已損失生命的一半」），可無限次
            if (now - lastHealTime >= 30000) {
                int lost = (int) (maxHP - curHP);      // 已損失生命
                int healAmt = lost / 2;               // 回一半
                curHP = Math.min(curHP + healAmt, maxHP);
                lastHealTime = now;
                // 顯示「回血文字」
                damageTexts.add(new DamageText(
                        "+" + healAmt,
                        (int) x + width / 2,
                        (int) y
                ));
            }
            for (Iterator<BossBullet> it = bossBullets.iterator(); it.hasNext(); ) {
                BossBullet bb = it.next();
                bb.update();
                // 如果飛出螢幕，就把它從清單裡移掉
                if (bb.x < 0 || bb.x > PANEL_WIDTH || bb.y < 0 || bb.y > PANEL_HEIGHT) {
                    it.remove();
                }
            }
        }

        @Override
        public void render(Graphics g) {
            super.render(g); // 先畫 Boss3 的血條、外框

            // 給 Boss3 的所有子彈上色、繪製出來
            for (BossBullet bb : bossBullets) {
                g.drawImage(
                        laserImg,
                        (int) bb.x,
                        (int) bb.y,
                        BossBullet.SIZE,
                        BossBullet.SIZE,
                        null
                );
            }
        }

        /**
         * 注意：此處必須與父類 Boss 裡的宣告存取權限一致（protected）
         */
        @Override
        protected void shootAtPlayer(double px, double py) {
            double sx = x + width / 2.0;
            double sy = y + height;
            double dx = px - sx;
            double dy = py - sy;
            double dist = Math.hypot(dx, dy);
            double spd = 8.0;

            // 判斷：如果目前血量 < 半血，就讓這顆子彈的攻擊力 = baseAttack *1.5；否則用 baseAttack
            double bulletAtk = (curHP < maxHP / 2.0)
                    ? baseAttack * 1.5
                    : baseAttack;

            bossBullets.add(new BossBullet(
                    sx, sy,
                    dx / dist * spd,
                    dy / dist * spd,
                    bulletAtk  // ← 只有這顆子彈用加乘攻擊
            ));
            lastAttackTime = System.currentTimeMillis();
        }
    }
}// end class GamePanel