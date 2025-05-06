import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;

import javax.swing.JPanel;


public class GamePanel extends JPanel implements Runnable,KeyListener {

    private int mScreenWidth = 320;//屏宽
    private int mScreenHeight = 480;//屏高
    private static final int STATE_GAME = 0;
    private int mState = STATE_GAME;
    private Image mBitMenuBG0 = null;//背景图1
    private Image mBitMenuBG1 = null;//背景图2
    private int mBitposY0 = 0;//背景1的y轴起点
    private int mBitposY1 = 0;//背景2的y轴起点
    
    private long createEnemyTime = System.currentTimeMillis();//用来监控敌机创建时间
    final static int BULLET_POOL_COUNT = 15;//最多同时存在的子弹数
    final static int PLAN_STEP = 10;//主角移动步长
    final static int PLAN_TIME = 500;//隔500ms发射子弹
    final static int ENEMY_POOL_COUNT = 5;//敌机最多同时存在个数
    final static int ENEMY_POS_OFF = 65;//敌机偏移量
    final static int BULLET_LEFT_OFFSET = 0;//子弹左偏移量
    final static int BULLET_UP_OFFSET = 10;//子弹上偏移量

    private Thread mThread = null;//主线程
    private boolean mIsRunning = false;//初始游戏状态未运行
    public int mAirPosX = 0;//主角x位置
    public int mAirPosY = 00;//主角y位置
    Vector<Enemy> mEnemy = new Vector<Enemy>();//存储敌机
    Vector<Bullet> mBullet = new Vector<Bullet>();//存储子弹

    public int mSendId = 0;
    public long mSendTime = 0L;
    Image myPlanePic[];
    public int myPlaneID = 0;

    public GamePanel() {
        setPreferredSize(new Dimension(mScreenWidth, mScreenHeight));
        setFocusable(true);
        addKeyListener(this);
        
        init();
        setGameState(STATE_GAME);
        mIsRunning = true;
        mThread = new Thread(this);
        mThread.start();
        setVisible(true);
    }

    public void init() {
        try {
            mBitMenuBG0 = Toolkit.getDefaultToolkit().getImage("C:/image/bg0.png");
            mBitMenuBG1 = Toolkit.getDefaultToolkit().getImage("C:/image/bg0.png");
        } catch (Exception e) {
            e.printStackTrace();
        }

        mBitposY0 = 0;
        mBitposY1 = -mScreenHeight;
        mAirPosX = 150;
        mAirPosY = 400;
        myPlanePic = new Image[1];
        for (int i = 0; i < myPlanePic.length; i++) {
            myPlanePic[i] = Toolkit.getDefaultToolkit().getImage("C:/image/player.png");
        }

        for (int i = 0; i < ENEMY_POOL_COUNT; i++) {
            Enemy tempEnemy = new Enemy();
            tempEnemy.init(i * ENEMY_POS_OFF, i * ENEMY_POS_OFF - 300);
            mEnemy.add(tempEnemy);
        }

        mBullet.add(new Bullet());

        mSendTime = System.currentTimeMillis();

    }

    public void draw() {
        switch (mState) {
            case STATE_GAME:
                renderBg();//更新主角飞机
                updateBg();//更新场景画布
                break;
        }
    }

    private void setGameState(int newState) {
        mState = newState;
    }

    public void renderBg() {
        myPlaneID++;
        if (myPlaneID == myPlanePic.length) {
            myPlaneID = 0;
        }
        repaint();
    }

    public void paint(Graphics g) {
        g.drawImage(mBitMenuBG0, 0, mBitposY0, this);
        g.drawImage(mBitMenuBG1, 0, mBitposY1, this);
        g.drawImage(myPlanePic[myPlaneID], mAirPosX, mAirPosY, 30, 30, this);
        for (int i = 0; i < mBullet.size(); i++) {
            if (mBullet.get(i).mFacus == true)
                mBullet.get(i).DrawBullet(g, this);

        }
        for (int i = 0; i < mEnemy.size(); i++) {
            mEnemy.get(i).DrawEnemy(g, this);
        }
    }
    public void updateBg(){
        
        mBitposY0 += 4;
        mBitposY1 += 4;
        if(mBitposY0 == mScreenHeight){
            mBitposY0 = -mScreenHeight;
        }
        if(mBitposY1 == mScreenHeight){
            mBitposY1 = -mScreenHeight;
        }
        //检测子弹是否超出屏幕以及更新子弹位置
        for(int i=0;i<mBullet.size();i++){
            if(mBullet.get(i).m_posY<=0)
                mBullet.get(i).mFacus = false;

            mBullet.get(i).UpdateBullet();

        }

        for(int i=0;i<mEnemy.size();i++){
            mEnemy.get(i).UpdateEnemy();
            if(mEnemy.get(i).mAnimState==Enemy.ENEMY_DEATH_STATE&&mEnemy.get(i).mPlayID==4||mEnemy.get(i).m_posY>=mScreenHeight){
                mEnemy.remove(i);
            }
        }

        long nowTime = System.currentTimeMillis();
        if(mEnemy.size()<5&&nowTime-createEnemyTime>=1000)//至少要间隔1秒才生成飞机
        {
            createEnemyTime = nowTime;
            Enemy tempEnemy = new Enemy();
            tempEnemy.init(UtilRandom(0,mScreenWidth-30),0);
            mEnemy.add(tempEnemy);
        }


        if(mSendId<BULLET_POOL_COUNT){
            long now = System.currentTimeMillis();
            if(now-mSendTime>=PLAN_TIME){
                Bullet tempBullet = new Bullet();
                tempBullet.init(mAirPosX-BULLET_LEFT_OFFSET, mAirPosY-BULLET_UP_OFFSET);
                mBullet.add(tempBullet);
                mSendTime = now;
                mSendId++;
            }
        }else{
            mSendId= 0;
        }

        Collision();
    }
    public void Collision(){
        for(int i=0;i<mBullet.size();i++)
        {
            for(int j=0;j<mEnemy.size();j++)
            {
                if(mEnemy.get(j).mAnimState==Enemy.ENEMY_ALIVE_STATE&&mBullet.get(i).mFacus==true&&mBullet.get(i).m_posX>mEnemy.get(j).m_posX-30&&mBullet.get(i).m_posX<mEnemy.get(j).m_posX+30
                        &&mBullet.get(i).m_posY>=mEnemy.get(j).m_posY&&mBullet.get(i).m_posY<=mEnemy.get(j).m_posY+30){
                    mEnemy.get(j).mAnimState = Enemy.ENEMY_DEATH_STATE;
                    mBullet.get(i).mFacus = false;//如果子弹撞上敌机，敌机状态修改为死亡，子弹状态修改为失效
                }
            }
        }

    }

    //返回的敌机x坐标
    private int UtilRandom(int bottom,int top){

        return ((Math.abs(new Random().nextInt()) % (top - bottom)) + bottom);
    }


    @Override
    public void keyTyped(KeyEvent e) {
        // TODO Auto-generated method stub

    }
    @Override
    public void keyPressed(KeyEvent e) {

        int key = e.getKeyCode();
        //System.out.println(key);
        if(key == KeyEvent.VK_UP){
            mAirPosY -= PLAN_STEP;
            if (mAirPosY < 0) { 
                mAirPosY = 0;
            }
        }
        if(key == KeyEvent.VK_DOWN){
            mAirPosY += PLAN_STEP;
            if (mAirPosY > mScreenHeight - 30) {
                mAirPosY = mScreenHeight - 30;
            }
        }
        if(key == KeyEvent.VK_LEFT){
            mAirPosX -= PLAN_STEP;
            if(mAirPosX<0){
                mAirPosX = 0;
            }
        }

        if(key == KeyEvent.VK_RIGHT){
            mAirPosX += PLAN_STEP;
            if(mAirPosX > mScreenWidth-30){
                mAirPosX = mScreenWidth - 30;
            }
        }

//        System.out.println("飞机当前坐标是("+mAirPosX+","+mAirPosY+")");
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void run() {
        while(mIsRunning){
            draw();
            try{
                Thread.sleep(100);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}
