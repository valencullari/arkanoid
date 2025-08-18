package Principal;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.Timer;

public class Principal {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Arkanoid");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);

        ArkanoidPanel arkanoidPanel = new ArkanoidPanel();
        frame.setContentPane(arkanoidPanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        arkanoidPanel.requestFocusInWindow();
    }

    // Clase ArkanoidPanel movida fuera del método main
    public static class ArkanoidPanel extends JPanel {
        private static final int ANCHO = 800, ALTO = 600;
        private Timer timerJuego;
        private boolean enPausa = false;

        // Pelota
        private int pelotaX, pelotaY, pelotaSize = 16;
        private int pelotaVelX = 4, pelotaVelY = -4;
        private boolean pelotaEnEspera = true;

        // Paleta
        private int paletaX, paletaY, paletaWidth = 80, paletaHeight = 18;
        private int paletaVelX = 0;

        // Bloques
        private List<Bloque> bloques = new ArrayList<>();
        private int[][] niveles = new int[10][];
        private int nivelActual = 0;

        // Vidas y puntos
        private int vidas = 3;
        private int puntos = 0;

        public ArkanoidPanel() {
            setPreferredSize(new Dimension(ANCHO, ALTO));
            setFocusable(true);
            setLayout(null);

            addKeyListener(new KeyAdapter() {
                public void keyPressed(KeyEvent e) {
                    // Pausa con 'P'
                    if (e.getKeyCode() == KeyEvent.VK_P) {
                        togglePausa();
                        return;
                    }
                    // Reiniciar con 'R'
                    if (e.getKeyCode() == KeyEvent.VK_R) {
                        reiniciarJuego();
                        return;
                    }
                    if (e.getKeyCode() == KeyEvent.VK_LEFT) paletaVelX = -7;
                    if (e.getKeyCode() == KeyEvent.VK_RIGHT) paletaVelX = 7;
                    // Lanzar pelota si está esperando y el juego no está en pausa
                    if (!enPausa && pelotaEnEspera && (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_RIGHT)) {
                        pelotaEnEspera = false;
                        pelotaVelX = (e.getKeyCode() == KeyEvent.VK_LEFT) ? -4 : 4;
                        pelotaVelY = -4;
                    }
                }
                public void keyReleased(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_RIGHT) paletaVelX = 0;
                }
            });

            inicializarNiveles();
            iniciarNivel(nivelActual);

            timerJuego = new Timer(10, e -> {
                if (!enPausa) {
                    mover();
                    chequearColisiones();
                    repaint();
                }
            });
            timerJuego.start();

            reiniciarJuego();
        }

        private void inicializarNiveles() {
            for (int n = 0; n < 10; n++) {
                niveles[n] = new int[60]; // 6 filas x 10 columnas
                for (int i = 0; i < niveles[n].length; i++) {
                    niveles[n][i] = (i % (n + 2)) == 0 ? 1 : 0; // patrones distintos
                }
            }
        }

        private void iniciarNivel(int nivel) {
            bloques.clear();
            int[] config = niveles[nivel];
            boolean ponerCorazon = (nivel % 3 == 2); // cada 3 niveles
            int corazonCount = 0;
            for (int fila = 0; fila < 6; fila++) {
                for (int col = 0; col < 10; col++) {
                    if (config[fila * 10 + col] == 1) {
                        boolean dobleGolpe = ((fila + col) % 5 == 0); // patrón para doble golpe
                        boolean corazon = false;
                        if (ponerCorazon && corazonCount < 2 && (fila + col) % 7 == 0) {
                            corazon = true;
                            corazonCount++;
                        }
                        bloques.add(new Bloque(80 + col * 60, 50 + fila * 28, 55, 22, dobleGolpe ? 2 : 1, corazon));
                    }
                }
            }
            paletaX = ANCHO / 2 - paletaWidth / 2;
            paletaY = ALTO - 50;
            pelotaX = paletaX + paletaWidth / 2 - pelotaSize / 2;
            pelotaY = paletaY - pelotaSize;
            pelotaVelX = 0;
            pelotaVelY = 0;
            pelotaEnEspera = true;
        }

        private void mover() {
            // Mover paleta
            paletaX += paletaVelX;
            paletaX = Math.max(0, Math.min(ANCHO - paletaWidth, paletaX));
            // Si la pelota está esperando, acompaña a la paleta
            if (pelotaEnEspera) {
                pelotaX = paletaX + paletaWidth / 2 - pelotaSize / 2;
                pelotaY = paletaY - pelotaSize;
            } else {
                // Mover pelota
                pelotaX += pelotaVelX;
                pelotaY += pelotaVelY;
            }

            // Rebote con paredes
            if (pelotaX < 0 || pelotaX + pelotaSize > ANCHO) pelotaVelX *= -1;
            if (pelotaY < 0) pelotaVelY *= -1;

            // Rebote con paleta
            Rectangle paleta = new Rectangle(paletaX, paletaY, paletaWidth, paletaHeight);
            Rectangle bola = new Rectangle(pelotaX, pelotaY, pelotaSize, pelotaSize);
            if (bola.intersects(paleta)) {
                pelotaVelY = -Math.abs(pelotaVelY);
            }

            // Caída abajo
            if (!pelotaEnEspera && pelotaY > ALTO) {
                vidas--;
                if (vidas <= 0) {
                    JOptionPane.showMessageDialog(this, "¡Game Over!");
                    reiniciarJuego();
                } else {
                    // Reiniciar pelota encima de la paleta
                    pelotaX = paletaX + paletaWidth / 2 - pelotaSize / 2;
                    pelotaY = paletaY - pelotaSize;
                    pelotaVelX = 0;
                    pelotaVelY = 0;
                    pelotaEnEspera = true;
                }
            }

            // Mover corazones activos
            Iterator<Bloque> it = bloques.iterator();
            while (it.hasNext()) {
                Bloque b = it.next();
                if (b.corazonActivo) {
                    b.yCorazon += 4;
                    // Colisión con paleta
                    Rectangle paletaRect = new Rectangle(paletaX, paletaY, paletaWidth, paletaHeight);
                    Rectangle corazonRect = new Rectangle(b.xCorazon, b.yCorazon, 24, 24);
                    if (corazonRect.intersects(paletaRect)) {
                        vidas++;
                        b.corazonActivo = false;
                        it.remove();
                    }
                    // Si cae fuera de pantalla
                    else if (b.yCorazon > ALTO) {
                        b.corazonActivo = false;
                        it.remove();
                    }
                }
            }
        }

        // Clase interna para bloques
        public static class Bloque {
            int x, y, width, height;
            int golpes;
            boolean tieneCorazon;
            boolean corazonActivo = false;
            int corazonY, xCorazon, yCorazon;
            Bloque(int x, int y, int width, int height, int golpes, boolean tieneCorazon) {
                this.x = x;
                this.y = y;
                this.width = width;
                this.height = height;
                this.golpes = golpes;
                this.tieneCorazon = tieneCorazon;
            }
            Rectangle getRect() {
                return new Rectangle(x, y, width, height);
            }
        }

        private void chequearColisiones() {
            Rectangle bola = new Rectangle(pelotaX, pelotaY, pelotaSize, pelotaSize);
            Iterator<Bloque> it = bloques.iterator();
            while (it.hasNext()) {
                Bloque bloque = it.next();
                if (bloque.golpes > 0 && bola.intersects(bloque.getRect())) {
                    Rectangle rect = bloque.getRect();
                    int bolaPrevX = pelotaX - pelotaVelX;
                    int bolaPrevY = pelotaY - pelotaVelY;
                    Rectangle bolaPrev = new Rectangle(bolaPrevX, bolaPrevY, pelotaSize, pelotaSize);
                    boolean reboteX = false, reboteY = false;
                    // Detectar colisión lateral (izquierda/derecha)
                    if (bolaPrev.x + pelotaSize <= rect.x || bolaPrev.x >= rect.x + rect.width) reboteX = true;
                    // Detectar colisión vertical (arriba/abajo)
                    if (bolaPrev.y + pelotaSize <= rect.y || bolaPrev.y >= rect.y + rect.height) reboteY = true;
                    // Rebote según el lado
                    if (reboteX && !reboteY) pelotaVelX *= -1;
                    else if (reboteY && !reboteX) pelotaVelY *= -1;
                    else {
                        // Si la colisión es en esquina o ambos lados, invertir ambos
                        pelotaVelX *= -1;
                        pelotaVelY *= -1;
                    }
                    bloque.golpes--;
                    puntos += 100;
                    if (bloque.golpes <= 0) {
                        if (bloque.tieneCorazon && !bloque.corazonActivo) {
                            bloque.corazonActivo = true;
                            bloque.corazonY = bloque.y;
                            bloque.xCorazon = bloque.x + bloque.width/2 - 12;
                            bloque.yCorazon = bloque.y;
                        } else {
                            it.remove();
                        }
                    }
                }
            }
            // Nivel completado
            if (bloques.stream().noneMatch(b -> b.golpes > 0 || b.corazonActivo)) {
                nivelActual++;
                if (nivelActual >= niveles.length) {
                    JOptionPane.showMessageDialog(this, "¡Has ganado el juego!");
                    reiniciarJuego();
                } else {
                    iniciarNivel(nivelActual);
                }
            }
        }

        private void togglePausa() {
            enPausa = !enPausa;
            // Ya no hay botón, solo cambia el estado
        }

        private void reiniciarJuego() {
            nivelActual = 0;
            vidas = 3;
            puntos = 0;
            iniciarNivel(nivelActual);
            enPausa = false;
            // Ya no hay botón, solo reinicia
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            // Fondo
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, ANCHO, ALTO);

            // Carteles de instrucciones
            g.setColor(Color.CYAN);
            g.setFont(new Font("Arial", Font.BOLD, 18));
            g.drawString("Presiona 'P' para pausar/continuar", ANCHO - 320, ALTO - 40);
            g.drawString("Presiona 'R' para reiniciar", ANCHO - 320, ALTO - 15);

            // Paleta
            g.setColor(Color.GREEN);
            g.fillRect(paletaX, paletaY, paletaWidth, paletaHeight);

            // Pelota
            g.setColor(Color.WHITE);
            g.fillOval(pelotaX, pelotaY, pelotaSize, pelotaSize);

            // Bloques
            for (Bloque bloque : bloques) {
                if (bloque.golpes > 0) {
                    if (bloque.golpes == 2) {
                        g.setColor(Color.ORANGE);
                    } else {
                        g.setColor(Color.RED);
                    }
                    g.fillRect(bloque.x, bloque.y, bloque.width, bloque.height);
                    // Corazón en bloque
                    if (bloque.tieneCorazon && bloque.golpes > 0) {
                        g.setColor(Color.PINK);
                        g.fillOval(bloque.x + bloque.width/2 - 8, bloque.y + 2, 16, 16);
                    }
                }
            }
            // Corazones cayendo
            for (Bloque bloque : bloques) {
                if (bloque.corazonActivo) {
                    g.setColor(Color.PINK);
                    g.fillOval(bloque.xCorazon, bloque.yCorazon, 24, 24);
                    g.setColor(Color.WHITE);
                    g.drawString("+1", bloque.xCorazon + 6, bloque.yCorazon + 18);
                }
            }

            // Puntos y vidas
            g.setColor(Color.YELLOW);
            g.setFont(new Font("Arial", Font.BOLD, 20));
            g.drawString("Puntos: " + puntos, 30, 30);
            g.drawString("Vidas: " + vidas, 30, 60);
            g.drawString("Nivel: " + (nivelActual+1), 30, 90);

            // Pausa
            if (enPausa) {
                g.setFont(new Font("Arial", Font.BOLD, 60));
                g.setColor(Color.CYAN);
                g.drawString("PAUSA", ANCHO / 2 - 120, ALTO / 2);
            }
        }
    }
}