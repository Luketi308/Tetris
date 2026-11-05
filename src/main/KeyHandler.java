package main;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyHandler implements KeyListener {

    // volatile: las escribe el hilo de Swing y las lee el hilo del juego
    public static volatile boolean upPressed, downPressed, leftPressed, rightPressed;
    public static volatile boolean pausePressed, restartPressed;

    private boolean spaceHeld; // el auto-repeat del teclado no debe alternar la pausa varias veces

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            // En pausa se ignoran las teclas de movimiento para que no se apliquen al reanudar
            case KeyEvent.VK_UP:    if (!pausePressed) upPressed = true; break;
            case KeyEvent.VK_LEFT:  if (!pausePressed) leftPressed = true; break;
            case KeyEvent.VK_DOWN:  if (!pausePressed) downPressed = true; break;
            case KeyEvent.VK_RIGHT: if (!pausePressed) rightPressed = true; break;
            case KeyEvent.VK_SPACE:
                if (!spaceHeld && !PlayManager.gameOver) {
                    pausePressed = !pausePressed;
                }
                spaceHeld = true;
                break;
            case KeyEvent.VK_ENTER:
                if (PlayManager.gameOver) {
                    restartPressed = true;
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            spaceHeld = false;
        }
    }

    /** Descarta teclas de movimiento que quedaron pendientes (p. ej. durante el parpadeo). */
    public static void clearMovement() {
        upPressed = false;
        downPressed = false;
        leftPressed = false;
        rightPressed = false;
    }
}
