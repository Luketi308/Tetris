# Tetris

Versión del Tetris clásico hecha en Java con NetBeans.

## ¿Qué es esto?

Es un proyecto de un videojuego basado en el Tetris clásico. El objetivo es acomodar las piezas que van cayendo para completar filas y evitar que los bloques lleguen hasta la parte superior del tablero.

La partida lleva un puntaje, un contador de líneas y niveles que aumentan la velocidad de caída.

## ¿Cómo lo veo?

Podés verlo en local clonando el repositorio:

```bash
git clone https://github.com/Luketi308/Tetris.git && cd Tetris
```

Necesitás tener Java 21 o superior y NetBeans. Después, abrí el proyecto desde NetBeans seleccionando la carpeta `Tetris` y ejecutalo con **Run Project**.

No necesita instalar dependencias externas.

## ¿Qué tiene?

Está armado de la siguiente manera:

- **Tablero**: área de juego donde caen y se acomodan las piezas.
- **Piezas**: incluye las siete formas clásicas, cada una formada por cuatro bloques y con sus rotaciones correspondientes.
- **Movimiento y colisiones**: permite mover y rotar las piezas, detectando los límites del tablero y los bloques que ya quedaron fijados.
- **Pieza siguiente**: muestra cuál va a aparecer después.
- **Puntaje**: suma puntos al completar filas y también al bajar una pieza manualmente.
- **Niveles**: cada diez líneas completadas aumenta el nivel y la velocidad de caída.
- **Pausa y reinicio**: permite pausar la partida y comenzar otra cuando termina el juego.

### Controles

- **← / →**: mover la pieza hacia los costados.
- **↓**: bajar la pieza una fila.
- **↑**: rotar la pieza.
- **Espacio**: pausar o reanudar la partida.
- **Enter**: reiniciar después de un Game Over.

## ¿Cómo está armado por dentro?

Lo armamos de esta manera:

- `src/` — contiene todo el código fuente, separado en dos paquetes:
  - `main/` — controla la ventana, el ciclo del juego, las entradas del teclado y la lógica general:
    - `Main.java` — crea la ventana e inicia el juego.
    - `GamePanel.java` — configura el panel, ejecuta el ciclo de actualización y dibuja la partida.
    - `KeyHandler.java` — detecta las teclas que se utilizan durante el juego.
    - `PlayManager.java` — administra el tablero, las piezas, las colisiones, el puntaje, los niveles y el fin de la partida.
  - `mino/` — contiene las clases relacionadas con las piezas y sus bloques:
    - `Block.java` — representa y dibuja cada bloque.
    - `Mino.java` — clase base que maneja el movimiento, las rotaciones y las colisiones de las piezas.
    - `Mino_Bar.java`, `Mino_L1.java`, `Mino_L2.java`, `Mino_Square.java`, `Mino_T.java`, `Mino_Z1.java` y `Mino_Z2.java` — definen las siete piezas y sus formas.
- `nbproject/` — configuración del proyecto de NetBeans.
- `build.xml` — archivo de construcción utilizado por NetBeans y Ant.
- `manifest.mf` — manifiesto del proyecto.

## ¿Quién lo hizo?

Lo hicimos Luca Lafuente y Alexis Gómez.
