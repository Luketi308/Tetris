package mino;

import java.awt.Color;

public class Mino_Square extends Mino {
    // Por rotación: desplazamientos (dx, dy) en bloques de b[0] a b[3]; b[0] es el pivote
    private static final int[][][] SHAPES = {
        {{0, 0}, {0, 1}, {1, 0}, {1, 1}}, // una sola forma: no rota
    };

    public Mino_Square() {
        super(Color.yellow, SHAPES);
    }
}
