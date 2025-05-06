

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.ImageObserver;

import javax.swing.JPanel;

public class Enemy{
    public static final int ENEMY_ALIVE_STATE = 0;
    public static final int ENEMY_DEATH_STATE = 1;
    static final int ENEMY_STEP_Y = 5;
    public int m_posX = 0;
    public int m_posY = 0;
    public int mAnimState = ENEMY_ALIVE_STATE;
    private Image enemyExplorePic[] = new Image[4];
    public int mPlayID = 0;

    public Enemy(){
        for(int i=0;i<enemyExplorePic.length;i++){
            enemyExplorePic[i] = Toolkit.getDefaultToolkit().getImage("D:/Game/bomb_enemy_"+i+".png");
        }
    }

    public void init(int x,int y){
        m_posX = x;
        m_posY = y;
        mAnimState = ENEMY_ALIVE_STATE;
        mPlayID = 0;
    }

    public void DrawEnemy(Graphics g,JPanel i){
        if(mAnimState==ENEMY_DEATH_STATE&&mPlayID<enemyExplorePic.length){
            g.drawImage(enemyExplorePic[mPlayID], m_posX, m_posY,30,30, (ImageObserver)i);
            mPlayID++;
            return;
        }

        Image pic = Toolkit.getDefaultToolkit().getImage("C:/image/enemy_alive.png");
        g.drawImage(pic, m_posX, m_posY, 30,30,(ImageObserver)i);
    }

    public void UpdateEnemy(){
        m_posY += ENEMY_STEP_Y;
    }
}
