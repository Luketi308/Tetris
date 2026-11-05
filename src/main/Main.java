package main;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // Swing se crea en su propio hilo (EDT)
        SwingUtilities.invokeLater(() -> {
            JFrame window = new JFrame("Tetris");
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.setResizable(false);

            GamePanel gp = new GamePanel();
            window.add(gp);
            window.pack();

            window.setLocationRelativeTo(null);
            window.setVisible(true);
            gp.requestFocusInWindow(); // sin foco el KeyListener no recibe teclas

            gp.launchGame();
        });
    }
}
