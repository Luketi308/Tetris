package mino;

import java.awt.Color;
import java.awt.Graphics2D;
import main.KeyHandler;
import main.PlayManager;

/**
 * Pieza base. Cada subclase solo define su color y sus formas: una tabla
 * [rotación][bloque][dx, dy] con los desplazamientos (en bloques) respecto al
 * pivote b[0]. La rotación, las colisiones y el dibujo viven acá, una sola vez.
 */
public abstract class Mino {
    public final Block[] b = new Block[4];
    public boolean active = true;

    private final Block[] tempB = new Block[4]; // posición candidata al rotar
    private final int[][][] shapes;
    private int direction = 1;                  // rotación actual, de 1 a shapes.length
    private int autoDropCounter = 0;
    private boolean leftCollision, rightCollision, bottomCollision;
    private boolean deactivating;
    private int deactivateCounter = 0;

    protected Mino(Color c, int[][][] shapes) {
        this.shapes = shapes;
        for (int i = 0; i < 4; i++) {
            b[i] = new Block(c);
            tempB[i] = new Block(c);
        }
    }

    /** Ubica la pieza con el pivote b[0] en (x, y), en su rotación actual. */
    public void setXY(int x, int y) {
        place(b, shapes[direction - 1], x, y);
    }

    private static void place(Block[] target, int[][] shape, int px, int py) {
        for (int i = 0; i < 4; i++) {
            target[i].x = px + shape[i][0] * Block.SIZE;
            target[i].y = py + shape[i][1] * Block.SIZE;
        }
    }

    /** true si los bloques están dentro del tablero y no pisan ningún bloque fijo. */
    private static boolean fits(Block[] cells) {
        for (Block c : cells) {
            if (c.x < PlayManager.left_x
                    || c.x + Block.SIZE > PlayManager.right_x
                    || c.y + Block.SIZE > PlayManager.bottom_y) {
                return false;
            }
        }
        for (Block s : PlayManager.staticBlocks) {
            for (Block c : cells) {
                if (c.x == s.x && c.y == s.y) {
                    return false;
                }
            }
        }
        return true;
    }

    /** true si la pieza, tal como está, pisa un bloque fijo o se sale del tablero. */
    public boolean isBlocked() {
        return !fits(b);
    }

    private void rotate() {
        if (shapes.length == 1) {
            return; // el cuadrado no cambia al rotar
        }
        int next = direction % shapes.length + 1;
        place(tempB, shapes[next - 1], b[0].x, b[0].y);
        if (fits(tempB)) {
            direction = next;
            for (int i = 0; i < 4; i++) {
                b[i].x = tempB[i].x;
                b[i].y = tempB[i].y;
            }
        }
    }

    private void move(int dx, int dy) {
        for (Block blk : b) {
            blk.x += dx;
            blk.y += dy;
        }
    }

    private void checkMovementCollision() {
        leftCollision = false;
        rightCollision = false;
        bottomCollision = false;

        // Paredes y piso
        for (Block c : b) {
            if (c.x == PlayManager.left_x) leftCollision = true;
            if (c.x + Block.SIZE == PlayManager.right_x) rightCollision = true;
            if (c.y + Block.SIZE == PlayManager.bottom_y) bottomCollision = true;
        }
        // Bloques fijos pegados a la pieza
        for (Block s : PlayManager.staticBlocks) {
            for (Block c : b) {
                if (c.y + Block.SIZE == s.y && c.x == s.x) bottomCollision = true;
                if (c.x - Block.SIZE == s.x && c.y == s.y) leftCollision = true;
                if (c.x + Block.SIZE == s.x && c.y == s.y) rightCollision = true;
            }
        }
    }

    public void update() {
        if (deactivating) {
            lockDelay();
        }

        if (KeyHandler.upPressed) {
            rotate();
            KeyHandler.upPressed = false;
        }

        checkMovementCollision();

        if (KeyHandler.downPressed) {
            if (!bottomCollision) {
                move(0, Block.SIZE);
                autoDropCounter = 0;
                PlayManager.score++; // 1 punto por cada fila bajada a mano
            }
            KeyHandler.downPressed = false;
        }
        if (KeyHandler.leftPressed) {
            if (!leftCollision) {
                move(-Block.SIZE, 0);
            }
            KeyHandler.leftPressed = false;
        }
        if (KeyHandler.rightPressed) {
            if (!rightCollision) {
                move(Block.SIZE, 0);
            }
            KeyHandler.rightPressed = false;
        }

        if (bottomCollision) {
            deactivating = true;
        } else {
            autoDropCounter++;
            // >= y no ==: si dropInterval baja de nivel con el contador ya pasado, igual cae
            if (autoDropCounter >= PlayManager.dropInterval) {
                move(0, Block.SIZE);
                autoDropCounter = 0;
            }
        }
    }

    /** Margen de 45 frames para deslizar la pieza antes de que se fije. */
    private void lockDelay() {
        deactivateCounter++;

        if (deactivateCounter == 45) {
            deactivateCounter = 0;
            checkMovementCollision();
            if (bottomCollision) {
                active = false;
            } else {
                deactivating = false; // ya no toca abajo: se cancela la fijación
            }
        }
    }

    public void draw(Graphics2D g2) {
        for (Block blk : b) {
            blk.draw(g2);
        }
    }
}
