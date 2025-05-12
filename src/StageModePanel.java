import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class StageModePanel extends JPanel implements Runnable, KeyListener {
    private final int screenWidth = 320;
    private final int screenHeight = 480;
    private int targetKillCount = 10;
    private Image bg1, bg2;
    private int bgY1 = 0, bgY2 = -screenHeight;
    private int enemySpawnDelay = 1000; // 小怪生成間隔（毫秒）
    private Image player;
    private int playerX = 150, playerY = 400;
    private int playerHp = 3;

    private Vector<Bullet> playerBullets = new Vector<>();
    private Vector<Enemy> enemies = new Vector<>();

    private Object currentBoss = null;
    private boolean bossActive = false;
    private boolean gameEnded = false;
    private int enemySpeed = 2;
    private int stage = 1;
    private int killCount = 0;
    private long lastShotTime = System.currentTimeMillis();
    private long lastEnemySpawnTime = System.currentTimeMillis();
    private boolean running = true;
    private Thread gameThread;

    public StageModePanel() {
        setPreferredSize(new Dimension(screenWidth, screenHeight));
        setFocusable(true);
        init();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        setFocusable(true);
        requestFocusInWindow();
        addKeyListener(this);
        gameThread = new Thread(this);
        gameThread.start();
    }

    private void init() {
        bg1 = Toolkit.getDefaultToolkit().getImage("C:/Users/User/Desktop/shooting-game/image/bg0.png");
        bg2 = Toolkit.getDefaultToolkit().getImage("C:/Users/User/Desktop/shooting-game/image/bg0.png");
        player = Toolkit.getDefaultToolkit().getImage("C:/Users/User/Desktop/shooting-game/image/player.png");
    }

    @Override
    public void paint(Graphics g) {
        g.drawImage(bg1, 0, bgY1, this);
        g.drawImage(bg2, 0, bgY2, this);
        g.drawImage(player, playerX, playerY, 30, 30, this);

        for (Bullet b : playerBullets)
            if (b.mFacus) b.DrawBullet(g, this);

        for (Enemy e : enemies)
            e.DrawEnemy(g, this);

        if (currentBoss instanceof Boss1) ((Boss1) currentBoss).draw(g, this);
        if (currentBoss instanceof Boss2) ((Boss2) currentBoss).draw(g, this);
        if (currentBoss instanceof Boss3) ((Boss3) currentBoss).draw(g, this);

        // 顯示血量
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 14));
        g.drawString("HP: " + playerHp, 10, 20);

        // 結束畫面
        if (gameEnded) {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 30));
            if (playerHp <= 0)
                g.drawString("GAME OVER", 80, 240);
            else
                g.drawString("YOU WIN!", 100, 240);
        }
    }

    private void update() {
        if (gameEnded) return;

        // 背景滾動
        bgY1 += 4;
        bgY2 += 4;
        if (bgY1 >= screenHeight) bgY1 = -screenHeight;
        if (bgY2 >= screenHeight) bgY2 = -screenHeight;

        // 自動發射子彈
        if (System.currentTimeMillis() - lastShotTime >= 500) {
            Bullet b = new Bullet();
            b.init(playerX + 10, playerY - 10); 
            playerBullets.add(b);
            lastShotTime = System.currentTimeMillis();
        }
	
	if (currentBoss instanceof Boss1 && ((Boss1) currentBoss).checkCollisionWithPlayer(playerX, playerY)) {
    	    playerHp--;
    	    if (playerHp <= 0) {
        	gameEnded = true;
        	running = false;
    	    }
	}
	
	if (currentBoss instanceof Boss2 && ((Boss2) currentBoss).checkCollisionWithPlayer(playerX, playerY)) {
    	    playerHp--;
    	    if (playerHp <= 0) {
        	gameEnded = true;
        	running = false;
    	    }
	}

	if (currentBoss instanceof Boss3 && ((Boss3) currentBoss).checkCollisionWithPlayer(playerX, playerY)) {
    	    playerHp--;
    	    if (playerHp <= 0) {
        	gameEnded = true;
        	running = false;
    	    }
	}

        for (int i = 0; i < playerBullets.size(); i++) {
    	    Bullet b = playerBullets.get(i);
    	    if (b.m_posY <= 0) {
        	b.mFacus = false;
    	    } else {
        	b.UpdateBullet();
        	if (b.mFacus) checkBossHit(b);
    	    }
	}


        // 子彈打中敵人
        for (int i = 0; i < playerBullets.size(); i++) {
            Bullet b = playerBullets.get(i);
            for (int j = 0; j < enemies.size(); j++) {
                Enemy e = enemies.get(j);
                if (b.mFacus &&
                    b.m_posX > e.m_posX - 30 && b.m_posX < e.m_posX + 30 &&
                    b.m_posY >= e.m_posY && b.m_posY <= e.m_posY + 30) {
                    b.mFacus = false;
                    enemies.remove(j--);
                    killCount++;
                    break;
                }
            }
        }
	
	// 玩家碰到敵人
        checkPlayerEnemyCollision();

        for (Enemy e : enemies) {
            e.UpdateEnemy();
    	}
	 
	if (currentBoss instanceof Boss1) {
    	    ((Boss1) currentBoss).update();
    	    if (((Boss1) currentBoss).hp <= 0) {
        	currentBoss = null;
        	bossActive = false;
        	killCount = 0;
        	targetKillCount = 15;
        	enemySpeed = 4;
        	enemySpawnDelay = 800; // 小怪出現加快
        	stage = 3;
    	    }
	} else if (currentBoss instanceof Boss2) {
    	    ((Boss2) currentBoss).update();
    	    if (((Boss2) currentBoss).hp <= 0) {
        	currentBoss = null;
        	bossActive = false;
        	killCount = 0;
        	targetKillCount = 20;
        	enemySpeed = 6;
        	enemySpawnDelay = 600; // 小怪出現更快
        	stage = 5;
    	   }
	}

    	// 小怪出現（第一、第二、第三波）
    	if ((stage == 1 || stage == 3 || stage == 5) && !bossActive) {
            if (System.currentTimeMillis() - lastEnemySpawnTime >= enemySpawnDelay) {
            	Enemy e = new Enemy();
            	e.init(UtilRandom(0, screenWidth - 30), 0);
            	e.setSpeed(enemySpeed); // 設定速度
            	enemies.add(e);
            	lastEnemySpawnTime = System.currentTimeMillis();
            }
    	}

    	// 判斷是否達到擊殺目標 → 進入下一位 Boss
    	if ((stage == 1 || stage == 3 || stage == 5) && !bossActive && killCount >= targetKillCount) {
            enemies.clear();
            bossActive = true;

            if (stage == 1) {
            	currentBoss = new Boss1();
            	stage = 2;
            } else if (stage == 3) {
            	currentBoss = new Boss2();
            	stage = 4;
            } else if (stage == 5) {
            	currentBoss = new Boss3();
            	stage = 6;
            }
    	}

    	// Boss 更新 & 死亡後流程控制
    	if (currentBoss != null) {
            if (currentBoss instanceof Boss1) {
            	((Boss1) currentBoss).update();
            	if (((Boss1) currentBoss).hp <= 0) {
                    currentBoss = null;
                    bossActive = false;
                    killCount = 0;
                    targetKillCount = 15;
                    enemySpeed = 4;
                    stage = 3;
            	}
            } else if (currentBoss instanceof Boss2) {
            	((Boss2) currentBoss).update();
            	if (((Boss2) currentBoss).hp <= 0) {
                    currentBoss = null;
                    bossActive = false;
                    killCount = 0;
                    targetKillCount = 20;
                    enemySpeed = 6;
                    stage = 5;
            	}
            } else if (currentBoss instanceof Boss3) {
            	((Boss3) currentBoss).update(playerX, playerY);
            	if (((Boss3) currentBoss).hp <= 0) {
                    gameEnded = true;
                    running = false;
            	}
            }
    	}
	checkPlayerHitByBossBullet();
        repaint();
    }
   
    private void checkPlayerEnemyCollision() {
        Rectangle playerRect = new Rectangle(playerX, playerY, 30, 30);
        for (int i = 0; i < enemies.size(); i++) {
            Enemy e = enemies.get(i);
            Rectangle enemyRect = new Rectangle(e.m_posX, e.m_posY, 30, 30);
            if (playerRect.intersects(enemyRect)) {
                playerHp--;
                enemies.remove(i--);
                if (playerHp <= 0) {
                    gameEnded = true;
                    running = false;
                    break;
                }
            }
        }
    }

    private void checkPlayerHitByBossBullet() {
    	Rectangle playerRect = new Rectangle(playerX, playerY, 30, 30);
    	if (currentBoss instanceof Boss1) {
            for (EnemyBullet b : ((Boss1) currentBoss).bullets) {
            	Rectangle bRect = new Rectangle(b.x, b.y, 10, 20);
            	if (playerRect.intersects(bRect)) {
                    playerHp--;
                    b.isAlive = false;
                    if (playerHp <= 0) {
                    	gameEnded = true;
                    	running = false;
                    }
            	}
           }
    	}
        if (currentBoss instanceof Boss2) {
            for (EnemyBullet b : ((Boss2) currentBoss).bullets) {
            	Rectangle bRect = new Rectangle(b.x, b.y, 10, 20);
            	if (playerRect.intersects(bRect)) {
                    playerHp--;
                    b.isAlive = false;
                    if (playerHp <= 0) {
                    	gameEnded = true;
                    	running = false;
                    }
            	}
            }
        }
        if (currentBoss instanceof Boss3) {
            for (EnemyBulletTrack b : ((Boss3) currentBoss).bullets) {
            	Rectangle bRect = new Rectangle(b.x, b.y, 10, 20);
            	if (playerRect.intersects(bRect)) {
                    playerHp--;
                    b.isAlive = false;
                    if (playerHp <= 0) {
                    	gameEnded = true;
                    	running = false;
                    }
            	}
            }
    	}
    }


    private void checkBossHit(Bullet b) {
    	if (currentBoss == null) return;

    	int bx = b.m_posX;
    	int by = b.m_posY;
    	int bossX = 0, bossY = 0, bossW = 80, bossH = 80;

    	if (currentBoss instanceof Boss1) {
            bossX = ((Boss1) currentBoss).x;
            bossY = ((Boss1) currentBoss).y;
    	} else if (currentBoss instanceof Boss2) {
            bossX = ((Boss2) currentBoss).x;
            bossY = ((Boss2) currentBoss).y;
    	} else if (currentBoss instanceof Boss3) {
            bossX = ((Boss3) currentBoss).x;
            bossY = ((Boss3) currentBoss).y;
    	}

    // 印出實際座標以偵錯
    	System.out.println("子彈: (" + bx + ", " + by + ") | Boss: (" + bossX + ", " + bossY + ")");

    	if (bx >= bossX && bx <= bossX + bossW &&
            by >= bossY && by <= bossY + bossH) {

            System.out.println(">>> 命中！扣血");

            b.mFacus = false;
            if (currentBoss instanceof Boss1) ((Boss1) currentBoss).hp -= 10;
            else if (currentBoss instanceof Boss2) ((Boss2) currentBoss).hp -= 10;
            else if (currentBoss instanceof Boss3) ((Boss3) currentBoss).hp -= 10;
    	}
    }




    private int UtilRandom(int min, int max) {
        return new Random().nextInt(max - min) + min;
    }

    @Override public void run() {
        while (running) {
            update();
            try {
                Thread.sleep(100);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT) playerX = Math.max(0, playerX - 10);
        if (key == KeyEvent.VK_RIGHT) playerX = Math.min(screenWidth - 30, playerX + 10);
        if (key == KeyEvent.VK_UP) playerY = Math.max(0, playerY - 10);
        if (key == KeyEvent.VK_DOWN) playerY = Math.min(screenHeight - 30, playerY + 10);
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
}
