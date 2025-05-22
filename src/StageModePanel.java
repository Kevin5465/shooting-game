import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.Random;

public class StageModePanel extends JPanel implements Runnable, KeyListener, MouseListener {
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
    private boolean gamePaused = false;
    private int enemySpeed = 2;
    private int stage = 1;
    private int killCount = 0;
    private long lastShotTime = System.currentTimeMillis();
    private long lastEnemySpawnTime = System.currentTimeMillis();
    private boolean running = true;
    private Thread gameThread;
    
    // 按鈕區域
    private Rectangle pauseButtonRect = new Rectangle(280, 10, 30, 30);
    private Rectangle backToMenuButtonRect = new Rectangle(110, 320, 100, 40);
    private Rectangle continueButtonRect = new Rectangle(80, 280, 80, 40);
    private Rectangle pauseBackToMenuButtonRect = new Rectangle(160, 280, 80, 40);
    
    // 對MainFrame的引用
    private MainFrame mainFrame;

    public StageModePanel() {
        setPreferredSize(new Dimension(screenWidth, screenHeight));
        setFocusable(true);
        init();
        System.out.println("StageModePanel constructor called");
    }
    
    public void setMainFrame(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    @Override
    public void addNotify() {
        super.addNotify();
        System.out.println("addNotify called, running: " + running + ", gameThread alive: " + (gameThread != null && gameThread.isAlive()));
        
        setFocusable(true);
        requestFocusInWindow();
        
        // 移除舊的監聽器，避免重複添加
        removeKeyListener(this);
        removeMouseListener(this);
        
        addKeyListener(this);
        addMouseListener(this);
        
        // 啟動遊戲線程
        startGameThread();
    }
    
    @Override
    public void removeNotify() {
        System.out.println("removeNotify called - stopping game");
        running = false;
        if (gameThread != null && gameThread.isAlive()) {
            gameThread.interrupt();
        }
        super.removeNotify();
    }
    
    private void startGameThread() {
        System.out.println("startGameThread called");
        
        // 停止舊線程
        if (gameThread != null && gameThread.isAlive()) {
            System.out.println("Stopping old thread");
            running = false;
            gameThread.interrupt();
            try {
                gameThread.join(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        // 啟動新線程
        running = true;
        gameThread = new Thread(this);
        gameThread.setDaemon(true); // 設置為守護線程
        gameThread.start();
        System.out.println("New game thread started: " + gameThread.isAlive());
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
        
        // 遊戲元素總是繪製，暫停時只是不更新
        g.drawImage(player, playerX, playerY, 30, 30, this);

        for (Bullet b : playerBullets)
            if (b.mFacus) b.DrawBullet(g, this);

        for (Enemy e : enemies)
            e.DrawEnemy(g, this);

        if (currentBoss instanceof Boss1) ((Boss1) currentBoss).draw(g, this);
        if (currentBoss instanceof Boss2) ((Boss2) currentBoss).draw(g, this);
        if (currentBoss instanceof Boss3) ((Boss3) currentBoss).draw(g, this);

        // 右上角暫停按鈕
        g.setColor(Color.WHITE);
        g.fillRect(pauseButtonRect.x, pauseButtonRect.y, pauseButtonRect.width, pauseButtonRect.height);
        g.setColor(Color.BLACK);
        g.drawRect(pauseButtonRect.x, pauseButtonRect.y, pauseButtonRect.width, pauseButtonRect.height);
        g.setFont(new Font("Arial", Font.BOLD, 12));
        g.drawString("||", pauseButtonRect.x + 10, pauseButtonRect.y + 20);

        // 顯示血量
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 14));
        g.drawString("HP: " + playerHp, 10, 20);

        // 暫停畫面
        if (gamePaused && !gameEnded) {
            g.setColor(new Color(0, 0, 0, 200));
            g.fillRect(0, 0, screenWidth, screenHeight);
            
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.drawString("PAUSED", 120, 200);
            
            // 繼續遊戲按鈕
            g.setColor(Color.GREEN);
            g.fillRect(continueButtonRect.x, continueButtonRect.y, continueButtonRect.width, continueButtonRect.height);
            g.setColor(Color.BLACK);
            g.drawRect(continueButtonRect.x, continueButtonRect.y, continueButtonRect.width, continueButtonRect.height);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 16));
            g.drawString("Continue", continueButtonRect.x + 10, continueButtonRect.y + 25);
            
            // 返回主畫面按鈕
            g.setColor(Color.RED);
            g.fillRect(pauseBackToMenuButtonRect.x, pauseBackToMenuButtonRect.y, pauseBackToMenuButtonRect.width, pauseBackToMenuButtonRect.height);
            g.setColor(Color.BLACK);
            g.drawRect(pauseBackToMenuButtonRect.x, pauseBackToMenuButtonRect.y, pauseBackToMenuButtonRect.width, pauseBackToMenuButtonRect.height);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 14));
            g.drawString("Menu", pauseBackToMenuButtonRect.x + 25, pauseBackToMenuButtonRect.y + 25);
        }

        // 結束畫面
        if (gameEnded) {
            g.setColor(new Color(0, 0, 0, 200));
            g.fillRect(0, 0, screenWidth, screenHeight);
            
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 30));
            if (playerHp <= 0)
                g.drawString("GAME OVER", 80, 240);
            else
                g.drawString("YOU WIN!", 100, 240);
            
            // 返回主畫面按鈕
            g.setColor(Color.BLUE);
            g.fillRect(backToMenuButtonRect.x, backToMenuButtonRect.y, backToMenuButtonRect.width, backToMenuButtonRect.height);
            g.setColor(Color.WHITE);
            g.drawRect(backToMenuButtonRect.x, backToMenuButtonRect.y, backToMenuButtonRect.width, backToMenuButtonRect.height);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 14));
            g.drawString("Back to Menu", backToMenuButtonRect.x + 5, backToMenuButtonRect.y + 25);
        }
    }

    private void update() {
        if (gameEnded || gamePaused) return;

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
	if (currentBoss == null) return;
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
            for (EnemyBulletTrack b : ((Boss3) currentBoss).trackingBullets) {
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
	    for (EnemyBullet b : ((Boss3) currentBoss).normalBullets) {
    	    	Rectangle bulletRect = new Rectangle(b.x, b.y, 10, 20);
    	    	if (bulletRect.intersects(playerRect)) {
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
	
	if (bx >= bossX && bx <= bossX + bossW && by >= bossY && by <= bossY + bossH) {
            b.mFacus = false;
            
            if (currentBoss instanceof Boss1) {
                ((Boss1) currentBoss).hp -= 1;
            } else if (currentBoss instanceof Boss2) {
                ((Boss2) currentBoss).hp -= 1;
            } else if (currentBoss instanceof Boss3) {
                ((Boss3) currentBoss).hp -= 1;
            }
        }
    }
    
    private void resetGame() {
        System.out.println("resetGame called");
        
        // 重置遊戲狀態
        playerX = 150;
        playerY = 400;
        playerHp = 3;
        stage = 1;
        killCount = 0;
        targetKillCount = 10;
        enemySpeed = 2;
        enemySpawnDelay = 1000;
        gameEnded = false;
        gamePaused = false;
        bossActive = false;
        currentBoss = null;
        playerBullets.clear();
        enemies.clear();
        
        // 重置背景位置
        bgY1 = 0;
        bgY2 = -screenHeight;
        
        // 重置時間
        lastShotTime = System.currentTimeMillis();
        lastEnemySpawnTime = System.currentTimeMillis();
        
        System.out.println("Game state reset complete");
    }
    
    private int UtilRandom(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min + 1) + min;
    }

    @Override public void run() {
        System.out.println("Game loop started");
        while (running) {
            update();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                System.out.println("Game thread interrupted");
                break;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("Game loop ended");
    }

    @Override public void keyPressed(KeyEvent e) {
        if (gamePaused || gameEnded) return;
        
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT) playerX = Math.max(0, playerX - 10);
        if (key == KeyEvent.VK_RIGHT) playerX = Math.min(screenWidth - 30, playerX + 10);
        if (key == KeyEvent.VK_UP) playerY = Math.max(0, playerY - 10);
        if (key == KeyEvent.VK_DOWN) playerY = Math.min(screenHeight - 30, playerY + 10);
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
    
    @Override
    public void mouseClicked(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        System.out.println("Mouse clicked at: " + x + ", " + y + ", running: " + running);
        
        // 暫停按鈕
        if (pauseButtonRect.contains(x, y) && !gameEnded) {
            gamePaused = !gamePaused;
            System.out.println("Pause clicked: " + gamePaused);
        }
        
        // 遊戲結束時的返回主畫面按鈕
        if (gameEnded && backToMenuButtonRect.contains(x, y)) {
            System.out.println("Back to menu clicked from game over");
            resetGame();
            if (mainFrame != null) {
                SwingUtilities.invokeLater(() -> {
                    mainFrame.showScreen("Menu");
                });
            }
        }
        
        // 暫停畫面的按鈕
        if (gamePaused && !gameEnded) {
            System.out.println("Pause menu clicked at: " + x + ", " + y);
            if (continueButtonRect.contains(x, y)) {
                System.out.println("Continue clicked");
                gamePaused = false;
            } else if (pauseBackToMenuButtonRect.contains(x, y)) {
                System.out.println("Pause back to menu clicked");
                resetGame();
                if (mainFrame != null) {
                    SwingUtilities.invokeLater(() -> {
                        mainFrame.showScreen("Menu");
                    });
                }
            }
        }
        
        repaint();
    }

    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
}