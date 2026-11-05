package mino;

import java.awt.Color;

public class Mino_T extends Mino {
    // Por rotación: desplazamientos (dx, dy) en bloques de b[0] a b[3]; b[0] es el pivote
    private static final int[][][] SHAPES = {
        {{0, 0}, {0, -1}, {-1, 0}, {1, 0}},
        {{0, 0}, {1, 0}, {0, -1}, {0, 1}},
        {{0, 0}, {0, 1}, {1, 0}, {-1, 0}},
        {{0, 0}, {-1, 0}, {0, 1}, {0, -1}},
    };

    public Mino_T() {
        super(Color.magenta, SHAPES);
    }
}
