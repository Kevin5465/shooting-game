import java.awt.Container;

import javax.swing.JFrame;

public class planeFrame extends JFrame{

    public planeFrame(){
        setTitle("星際防衛隊");
        GamePanel panel = new GamePanel();
        Container contentPane = getContentPane();
        contentPane.add(panel);
        pack();
    }

    
    public static void main(String[] args) {
        planeFrame e1 = new planeFrame();
        e1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        e1.setVisible(true);
    }

}