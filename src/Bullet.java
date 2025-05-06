import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.ImageObserver;

import javax.swing.JPanel;
public class Bullet {
    static final int BULLET_STEP_X = 3;
    static final int BULLET_STEP_Y = 15;
    static final int BULLET_WIDTH = 30;
    public int m_posX = 0;
    public int m_posY = -20;
    boolean mFacus = true;
    private Image pic[] = null;
    private int mPlayID = 0;
    public Bullet(){
        pic = new Image[1];
//        System.out.println(pic.length);
        for(int i=0;i<pic.length;i++){
            pic[i] = Toolkit.getDefaultToolkit().getImage("C:/image/bullet_"+i+".png");
        }
    }
    public void init(int x,int y){
        m_posX = x;
        m_posY = y;
        mFacus = true;
    }

    public void DrawBullet(Graphics g,JPanel i){
        g.drawImage(pic[mPlayID++], m_posX, m_posY,30,20, (ImageObserver)i);
        //    System.out.println("当前mPlayID是 "+mPlayID+",当前pic.length是"+pic.length);
        if(mPlayID>=pic.length)
            mPlayID  = 0;
    }

    public void UpdateBullet() {
        m_posY -= BULLET_STEP_Y;
    }
}
