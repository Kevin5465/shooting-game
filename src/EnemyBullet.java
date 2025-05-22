import java.awt.*;
import javax.swing.*;

public class EnemyBullet {
    public int x, y;
    public int dx = 0, dy = 6;  // Added dx for horizontal movement
    public boolean isAlive = true;
    private Image img;
    private int animationFrame = 0;
    private int animationDelay = 5;
    private int animationCounter = 0;
    private Image[] bulletImages = new Image[2];

    public EnemyBullet(int x, int y) {
        this.x = x;
        this.y = y;
        bulletImages[0] = Toolkit.getDefaultToolkit().getImage("C:/Users/User/Desktop/shooting-game/image/bullet_0.png");
        bulletImages[1] = Toolkit.getDefaultToolkit().getImage("C:/Users/User/Desktop/shooting-game/image/bullet_0.png");
    }

    public void update() {
        // Update position based on velocity
        x += dx;
        y += dy;
        
        // Check if bullet is out of bounds
        if (y > 480 || y < 0 || x < 0 || x > 320) {
            isAlive = false;
        }
        
        // Update animation
        animationCounter++;
        if (animationCounter >= animationDelay) {
            animationCounter = 0;
            animationFrame = (animationFrame + 1) % bulletImages.length;
        }
    }

    public void draw(Graphics g, JPanel panel) {
        g.drawImage(bulletImages[animationFrame], x, y, 10, 20, panel);
    }
    
    // Check collision with player
    public boolean checkCollisionWithPlayer(int px, int py, int pWidth, int pHeight) {
        Rectangle bulletRect = new Rectangle(x, y, 10, 20);
        Rectangle playerRect = new Rectangle(px, py, pWidth, pHeight);
        return bulletRect.intersects(playerRect);
    }
}