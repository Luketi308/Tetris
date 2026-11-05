package main;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.Random;
import mino.Block;
import mino.Mino;
import mino.Mino_Bar;
import mino.Mino_L1;
import mino.Mino_L2;
import mino.Mino_Square;
import mino.Mino_T;
import mino.Mino_Z1;
import mino.Mino_Z2;

public class PlayManager {
    // Área de juego principal
    private static final int WIDTH = 360;
    private static final int HEIGHT = 600;
    public static int left_x;
    public static int right_x;
    public static int top_y;
    public static int bottom_y;

    // Dificultad y puntaje
    private static final int INITIAL_DROP_INTERVAL = 60; // frames por fila en el nivel 1
    private static final int DROP_STEP = 6;              // frames menos por fila en cada nivel
    private static final int MIN_DROP_INTERVAL = 4;      // velocidad máxima
    private static final int LINES_PER_LEVEL = 10;
    private static final int[] LINE_POINTS = {0, 100, 300, 500, 800}; // por 0..4 líneas; se multiplica por el nivel
    public static int dropInterval = INITIAL_DROP_INTERVAL;
    public static int score = 0;
    public static int level = 1;
    public static int lines = 0;

    // Mino
    private Mino currentMino;
    private Mino nextMino;
    private final int MINO_START_X;
    private final int MINO_START_Y;
    private final int NEXTMINO_X;
    private final int NEXTMINO_Y;
    public static final ArrayList<Block> staticBlocks = new ArrayList<>();
    private static final Random RANDOM = new Random();

    // Efecto de parpadeo al completar líneas
    private boolean effect = false;
    private int effectCounter = 0;
    private final ArrayList<Integer> effectY = new ArrayList<>(); // Y de las líneas a borrar

    public static boolean gameOver = false;

    // Recursos de dibujo (se crean una vez, no en cada frame)
    private static final BasicStroke FRAME_STROKE = new BasicStroke(4f);
    private static final Font FONT_TEXT = new Font("Arial", Font.PLAIN, 30);
    private static final Font FONT_BIG = new Font("Arial", Font.PLAIN, 50);
    private static final Font FONT_LABEL = new Font("Arial", Font.PLAIN, 24);
    private static final Font FONT_VALUE = new Font("Arial", Font.BOLD, 36);
    private static final Color OVERLAY = new Color(0, 0, 0, 180);
    private static final int PANEL_X_OFFSET = 100; // distancia entre el tablero y los paneles de la derecha
    private static final int PANEL_W = 200;
    private static final int STATS_H = 340;

    public PlayManager() {
        left_x = (GamePanel.WIDTH / 2) - (WIDTH / 2);
        right_x = left_x + WIDTH;
        top_y = 50;
        bottom_y = top_y + HEIGHT;
        MINO_START_X = left_x + (WIDTH / 2) - Block.SIZE;
        MINO_START_Y = top_y + Block.SIZE;

        NEXTMINO_X = right_x + 175;
        NEXTMINO_Y = top_y + 500;

        reset();
    }

    /** Deja todo como en una partida nueva. */
    private void reset() {
        staticBlocks.clear();
        effectY.clear();
        effect = false;
        effectCounter = 0;
        gameOver = false;
        score = 0;
        lines = 0;
        level = 1;
        dropInterval = INITIAL_DROP_INTERVAL;
        KeyHandler.clearMovement();
        KeyHandler.restartPressed = false;

        currentMino = pickMino();
        currentMino.setXY(MINO_START_X, MINO_START_Y);
        nextMino = pickMino();
        nextMino.setXY(NEXTMINO_X, NEXTMINO_Y);
    }

    private Mino pickMino() {
        switch (RANDOM.nextInt(7)) {
            case 0:  return new Mino_L1();
            case 1:  return new Mino_L2();
            case 2:  return new Mino_Square();
            case 3:  return new Mino_Bar();
            case 4:  return new Mino_T();
            case 5:  return new Mino_Z1();
            default: return new Mino_Z2();
        }
    }

    /** Pasa la pieza "siguiente" al tablero y sortea una nueva. */
    private void spawnNext() {
        currentMino = nextMino;
        currentMino.setXY(MINO_START_X, MINO_START_Y);
        nextMino = pickMino();
        nextMino.setXY(NEXTMINO_X, NEXTMINO_Y);
        KeyHandler.clearMovement();

        // Si la pieza nueva ya pisa bloques fijos, no hay lugar: fin del juego
        if (currentMino.isBlocked()) {
            gameOver = true;
        }
    }

    // synchronized: update() corre en el hilo del juego y draw() en el de Swing
    public synchronized void update() {
        if (gameOver) {
            if (KeyHandler.restartPressed) {
                reset();
            }
            return;
        }

        // Mientras parpadean las líneas, el juego queda en pausa
        if (effect) {
            effectCounting();
            return;
        }

        if (currentMino.active) {
            currentMino.update();
        } else {
            lockCurrentMino();
        }
    }

    private void lockCurrentMino() {
        for (Block blk : currentMino.b) {
            staticBlocks.add(blk);
        }

        // Game over si algún bloque quedó fijado en la fila del techo
        for (Block blk : currentMino.b) {
            if (blk.y <= top_y) {
                gameOver = true;
                return;
            }
        }

        checkDelete();

        // Si hay líneas, la pieza siguiente sale cuando termina el parpadeo
        if (!effect) {
            spawnNext();
        }
    }

    private void checkDelete() {
        int rows = HEIGHT / Block.SIZE;
        int cols = WIDTH / Block.SIZE;

        // Una sola pasada: cuántos bloques hay en cada fila
        int[] blocksInRow = new int[rows];
        for (Block s : staticBlocks) {
            int row = (s.y - top_y) / Block.SIZE;
            if (row >= 0 && row < rows) {
                blocksInRow[row]++;
            }
        }

        for (int row = 0; row < rows; row++) {
            if (blocksInRow[row] == cols) {
                effectY.add(top_y + row * Block.SIZE);
            }
        }

        if (!effectY.isEmpty()) {
            effect = true;
            addClearedLines(effectY.size());
        }
    }

    /** Suma puntos (con el nivel actual) y recalcula nivel y velocidad. */
    private void addClearedLines(int cleared) {
        score += LINE_POINTS[Math.min(cleared, LINE_POINTS.length - 1)] * level;
        lines += cleared;
        level = lines / LINES_PER_LEVEL + 1;
        dropInterval = Math.max(MIN_DROP_INTERVAL, INITIAL_DROP_INTERVAL - (level - 1) * DROP_STEP);
    }

    private void effectCounting() {
        effectCounter++;

        // El efecto dura 20 fotogramas
        if (effectCounter > 20) {
            effectCounter = 0;
            effect = false;

            // Se borran las filas completas...
            staticBlocks.removeIf(s -> effectY.contains(s.y));

            // ...y cada bloque que queda baja una fila por cada fila borrada que tiene debajo
            for (Block s : staticBlocks) {
                int below = 0;
                for (int y : effectY) {
                    if (y > s.y) {
                        below++;
                    }
                }
                s.y += below * Block.SIZE;
            }

            effectY.clear();
            spawnNext();
        }
    }

    public synchronized void draw(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Marco del área de juego
        g2.setColor(Color.white);
        g2.setStroke(FRAME_STROKE);
        g2.drawRect(left_x - 4, top_y - 4, WIDTH + 8, HEIGHT + 8);

        // Panel de nivel, puntaje y líneas
        int panelX = right_x + PANEL_X_OFFSET;
        g2.drawRect(panelX, top_y, PANEL_W, STATS_H);
        drawStat(g2, "NIVEL", level, panelX, 0);
        drawStat(g2, "PUNTAJE", score, panelX, 1);
        drawStat(g2, "L\u00cdNEAS", lines, panelX, 2); // \u00cd = Í, escrito así para no depender de la codificación

        // Marco de la pieza siguiente
        int x = panelX;
        int y = bottom_y - 200;
        g2.setColor(Color.white);
        g2.drawRect(x, y, 200, 200);
        g2.setFont(FONT_TEXT);
        g2.drawString("SIGUIENTE", x + 20, y + 60);

        currentMino.draw(g2);
        nextMino.draw(g2);

        // Bloques fijos; durante el parpadeo, las filas completas alternan con blanco
        boolean flashOn = effect && effectCounter / 3 % 2 == 0;
        for (Block s : staticBlocks) {
            if (flashOn && effectY.contains(s.y)) {
                g2.setColor(Color.white);
                g2.fillRect(s.x + 1, s.y + 1, Block.SIZE - 2, Block.SIZE - 2);
            } else {
                s.draw(g2);
            }
        }

        int centerX = left_x + WIDTH / 2;

        if (KeyHandler.pausePressed) {
            g2.setColor(Color.yellow);
            g2.setFont(FONT_BIG);
            drawCentered(g2, "PAUSA", centerX, top_y + 300);
        }

        if (gameOver) {
            g2.setColor(OVERLAY);
            g2.fillRect(left_x, top_y, WIDTH, HEIGHT);

            g2.setColor(Color.red);
            g2.setFont(FONT_BIG);
            drawCentered(g2, "GAME OVER", centerX, top_y + HEIGHT / 2);

            g2.setColor(Color.white);
            g2.setFont(FONT_LABEL);
            drawCentered(g2, "ENTER para reiniciar", centerX, top_y + HEIGHT / 2 + 45);
        }
    }

    /** Una estadística (etiqueta + valor) dentro del panel; slot 0, 1, 2 de arriba hacia abajo. */
    private void drawStat(Graphics2D g2, String label, int value, int panelX, int slot) {
        int centerX = panelX + PANEL_W / 2;
        int base = top_y + slot * 110;
        g2.setColor(Color.lightGray);
        g2.setFont(FONT_LABEL);
        drawCentered(g2, label, centerX, base + 40);
        g2.setColor(Color.white);
        g2.setFont(FONT_VALUE);
        drawCentered(g2, String.valueOf(value), centerX, base + 85);
    }

    private void drawCentered(Graphics2D g2, String text, int centerX, int baselineY) {
        int w = g2.getFontMetrics().stringWidth(text);
        g2.drawString(text, centerX - w / 2, baselineY);
    }
}
