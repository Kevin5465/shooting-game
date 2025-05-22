import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import java.util.Vector;
import javax.swing.*;

public class InfiniteModePanel extends JPanel implements Runnable, KeyListener {

    private int mScreenWidth = 320;
    private int mScreenHeight = 480;

    private static final int STATE_GAME = 0;
    private int mState = STATE_GAME;

    private Image mBitMenuBG0 = null;
    private Image mBitMenuBG1 = null;
    private int mBitposY0 = 0;
    private int mBitposY1 = 0;

    private long createEnemyTime = System.currentTimeMillis();
    final static int PLAN_STEP = 10;
    final static int PLAN_TIME = 500;
    final static int ENEMY_POOL_COUNT = 5;
    final static int ENEMY_POS_OFF = 65;

    private Thread mThread = null;
    private boolean mIsRunning = false;

    public int mAirPosX = 0;
    public int mAirPosY = 0;
    Vector<Enemy> mEnemy = new Vector<>();
    Vector<Bullet> mBullet = new Vector<>();

    public long mSendTime = 0L;
    Image myPlanePic[];
    public int myPlaneID = 0;

    public InfiniteModePanel() {
        setPreferredSize(new Dimension(mScreenWidth, mScreenHeight));
        setFocusable(true);
        init();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        setFocusable(true);
        requestFocusInWindow();
        addKeyListener(this);
        mIsRunning = true;
        mThread = new Thread(this);
        mThread.start();
    }

    public void init() {
        mBitMenuBG0 = Toolkit.getDefaultToolkit().getImage("image/bg0.png");
        mBitMenuBG1 = Toolkit.getDefaultToolkit().getImage("image/bg0.png");

        mBitposY0 = 0;
        mBitposY1 = -mScreenHeight;
        mAirPosX = 150;
        mAirPosY = 400;

        myPlanePic = new Image[1];
        myPlanePic[0] = Toolkit.getDefaultToolkit().getImage("image/player.png");

        for (int i = 0; i < ENEMY_POOL_COUNT; i++) {
            Enemy tempEnemy = new Enemy();
            tempEnemy.init(i * ENEMY_POS_OFF, i * ENEMY_POS_OFF - 300);
            mEnemy.add(tempEnemy);
        }

        mBullet.add(new Bullet());
        mSendTime = System.currentTimeMillis();
    }

    public void draw() {
        if (mState == STATE_GAME) {
            renderBg();
            updateBg();
        }
    }

    private void setGameState(int newState) {
        mState = newState;
    }

    public void renderBg() {
        myPlaneID = (myPlaneID + 1) % myPlanePic.length;
        repaint();
    }

    @Override
    public void paint(Graphics g) {
        g.drawImage(mBitMenuBG0, 0, mBitposY0, this);
        g.drawImage(mBitMenuBG1, 0, mBitposY1, this);
        g.drawImage(myPlanePic[myPlaneID], mAirPosX, mAirPosY, 30, 30, this);

        for (Bullet b : mBullet) {
            if (b.mFacus) b.DrawBullet(g, this);
        }

        for (Enemy e : mEnemy) {
            e.DrawEnemy(g, this);
        }
    }

    public void updateBg() {
        mBitposY0 += 4;
        mBitposY1 += 4;
        if (mBitposY0 >= mScreenHeight) mBitposY0 = -mScreenHeight;
        if (mBitposY1 >= mScreenHeight) mBitposY1 = -mScreenHeight;

        for (Bullet b : mBullet) {
            if (b.m_posY <= 0) b.mFacus = false;
            b.UpdateBullet();
        }

        for (int i = 0; i < mEnemy.size(); i++) {
            Enemy e = mEnemy.get(i);
            e.UpdateEnemy();
            if ((e.mAnimState == Enemy.ENEMY_DEATH_STATE && e.mPlayID == 4) || e.m_posY >= mScreenHeight) {
                mEnemy.remove(i);
                i--;
            }
        }

        long nowTime = System.currentTimeMillis();
        if (mEnemy.size() < 5 && nowTime - createEnemyTime >= 1000) {
            createEnemyTime = nowTime;
            Enemy tempEnemy = new Enemy();
            tempEnemy.init(UtilRandom(0, mScreenWidth - 30), 0);
            mEnemy.add(tempEnemy);
        }

        long now = System.currentTimeMillis();
        if (now - mSendTime >= PLAN_TIME) {
            Bullet tempBullet = new Bullet();
            tempBullet.init(mAirPosX + 10, mAirPosY - 10);
            mBullet.add(tempBullet);
            mSendTime = now;
        }

        Collision();
    }

    public void Collision() {
    	for (int i = 0; i < mBullet.size(); i++) {
            Bullet b = mBullet.get(i);
            for (int j = 0; j < mEnemy.size(); j++) {
            	Enemy e = mEnemy.get(j);
            	if (e.mAnimState == Enemy.ENEMY_ALIVE_STATE && b.mFacus &&
                    b.m_posX > e.m_posX - 30 && b.m_posX < e.m_posX + 30 &&
                    b.m_posY >= e.m_posY && b.m_posY <= e.m_posY + 30) {
                    mEnemy.remove(j--);  
                    b.mFacus = false;
                    break;
            	}
            }
    	}
    }

    private int UtilRandom(int bottom, int top) {
        return (Math.abs(new Random().nextInt()) % (top - bottom)) + bottom;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_UP) mAirPosY = Math.max(0, mAirPosY - PLAN_STEP);
        if (key == KeyEvent.VK_DOWN) mAirPosY = Math.min(mScreenHeight - 30, mAirPosY + PLAN_STEP);
        if (key == KeyEvent.VK_LEFT) mAirPosX = Math.max(0, mAirPosX - PLAN_STEP);
        if (key == KeyEvent.VK_RIGHT) mAirPosX = Math.min(mScreenWidth - 30, mAirPosX + PLAN_STEP);
    }

    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyReleased(KeyEvent e) {}

    @Override
    public void run() {
        while (mIsRunning) {
            draw();
            try {
                Thread.sleep(100);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}