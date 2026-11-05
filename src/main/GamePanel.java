package main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;

public class GamePanel extends JPanel implements Runnable {
    public static final int WIDTH = 1280;
    public static final int HEIGHT = 720;
    private static final int FPS = 60;

    private Thread gameThread;
    private final PlayManager pm;

    public GamePanel() {
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setBackground(Color.black);
        this.setLayout(null);
        this.addKeyListener(new KeyHandler());
        this.setFocusable(true);

        pm = new PlayManager();
    }

    public void launchGame() {
        gameThread = new Thread(this, "game-loop");
        gameThread.start();
    }

    @Override
    public void run() {
        // Paso fijo de 1/60 s. Si un frame se atrasa se recupera, pero con tope
        // para no acelerar el juego tras una pausa larga del sistema.
        final long interval = 1_000_000_000L / FPS;
        long accumulated = 0;
        long last = System.nanoTime();

        while (gameThread != null) {
            long now = System.nanoTime();
            accumulated = Math.min(accumulated + (now - last), interval * 5);
            last = now;

            if (accumulated >= interval) {
                accumulated -= interval;
                update();
                repaint();
            } else {
                try {
                    Thread.sleep(1); // sin esto el while ocupa un núcleo entero
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    private void update() {
        if (!KeyHandler.pausePressed) {
            pm.update();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        pm.draw((Graphics2D) g);
    }
}
