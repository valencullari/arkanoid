package arkanoid;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.ArrayList;

public class ArkanoidBase extends JPanel implements KeyListener, ActionListener {
    private final int WIDTH = 800, HEIGHT = 600;
    private BufferedImage imgPelota, imgPaleta, imgBloque1;
    private int ballX = 400, ballY = 300, ballDiameter = 20;
    private double ballDX = 2, ballDY = -3;
    private int paddleX = 350, paddleY = 550, paddleWidth = 100, paddleHeight = 20, paddleDX = 0;
    private Timer timer;
    private ArrayList<Bloque> bloques = new ArrayList<>();
    private int nivelActual = 0;
    private int vidas = 3;
    private int puntos = 0;
    private long tiempoInicio = System.currentTimeMillis();
    private int puntosParaVida = 5000;
    private int puntosUltimaVida = 0;

    private void cargarNivel(int n) {
        bloques.clear();
        if (n < Nivel.niveles.length) {
            int[][] mapa = Nivel.niveles[n].mapa;
            for (int fila = 0; fila < mapa.length; fila++) {
                for (int col = 0; col < mapa[fila].length; col++) {
                    if (mapa[fila][col] != 0) {
                        bloques.add(new Bloque(60 + col*65, 60 + fila*35, mapa[fila][col]));
                    }
                }
            }
        }
    }

    public ArkanoidBase() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        imgPelota = cargarImagen("/img/foco.png");
        imgPaleta = cargarImagen("/img/sustin.png");
        imgBloque1 = cargarImagen("/img/pibep.png");

        cargarNivel(nivelActual);

        timer = new Timer(10, this);
        timer.start();
    }

    private BufferedImage cargarImagen(String path) {
        try {
            return javax.imageio.ImageIO.read(getClass().getResource(path));
        } catch (Exception e) {
            // Si no hay imagen, crea un placeholder
            BufferedImage img = new BufferedImage(60, 30, BufferedImage.TYPE_INT_ARGB);
            Graphics g = img.getGraphics();
            g.setColor(Color.GRAY);
            g.fillRect(0, 0, img.getWidth(), img.getHeight());
            g.dispose();
            return img;
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Dibuja la pelota
        g.drawImage(imgPelota, ballX, ballY, ballDiameter, ballDiameter, null);
        // Dibuja la paleta
        g.drawImage(imgPaleta, paddleX, paddleY, paddleWidth, paddleHeight, null);
        // Dibuja los bloques
        for (Bloque b : bloques) {
            if (b.tipo == 1 && imgBloque1 != null) {
                g.drawImage(imgBloque1, b.x, b.y, b.ancho, b.alto, null);
            } else {
                g.setColor(Color.GRAY);
                g.fillRect(b.x, b.y, b.ancho, b.alto);
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Movimiento de la pelota
        ballX += ballDX;
        ballY += ballDY;

        // Rebote en bordes laterales
        if (ballX <= 0) {
            ballX = 0;
            ballDX = Math.abs(ballDX); // Siempre rebota hacia la derecha
        }
        if (ballX + ballDiameter >= WIDTH) {
            ballX = WIDTH - ballDiameter;
            ballDX = -Math.abs(ballDX); // Siempre rebota hacia la izquierda
        }
        if (ballY <= 0) {
            ballY = 0;
            ballDY = Math.abs(ballDY); // Siempre rebota hacia abajo
        }

        // Rebote en la paleta con efecto angular
        if (ballY + ballDiameter >= paddleY &&
            ballX + ballDiameter >= paddleX &&
            ballX <= paddleX + paddleWidth) {

            int impact = (ballX + ballDiameter / 2) - (paddleX + paddleWidth / 2);
            double factor = Math.max(-1, Math.min(1, (double) impact / (paddleWidth / 2)));
            double angle = factor * Math.toRadians(60);
            double speed = Math.sqrt(ballDX * ballDX + ballDY * ballDY);

            // Si el impacto es muy cerca del centro, fuerza un ángulo mínimo para evitar rebote vertical puro
            if (Math.abs(factor) < 0.1) {
                angle = Math.copySign(Math.toRadians(15), angle == 0 ? 1 : angle);
            }

            ballDY = -Math.abs(speed * Math.cos(angle));
            ballDX = speed * Math.sin(angle);

            ballY = paddleY - ballDiameter;
        }

        // Movimiento de la paleta
        paddleX += paddleDX;
        if (paddleX < 0) paddleX = 0;
        if (paddleX + paddleWidth > WIDTH) paddleX = WIDTH - paddleWidth;

        // Colisión con bloques
        for (int i = 0; i < bloques.size(); i++) {
            Bloque b = bloques.get(i);
            Rectangle rectPelota = new Rectangle(ballX, ballY, ballDiameter, ballDiameter);
            Rectangle rectBloque = new Rectangle(b.x, b.y, b.ancho, b.alto);
            if (rectPelota.intersects(rectBloque)) {
                // Rebote: determina el lado de colisión
                int prevBallX = ballX - (int)ballDX;
                int prevBallY = ballY - (int)ballDY;
                Rectangle prevRectPelota = new Rectangle(prevBallX, prevBallY, ballDiameter, ballDiameter);
                boolean fromLeftOrRight = prevRectPelota.y + ballDiameter > b.y && prevRectPelota.y < b.y + b.alto;
                boolean fromTopOrBottom = prevRectPelota.x + ballDiameter > b.x && prevRectPelota.x < b.x + b.ancho;
                if (fromLeftOrRight && !fromTopOrBottom) {
                    ballDX *= -1;
                } else {
                    ballDY *= -1;
                }
                bloques.remove(i);
                i--;
                puntos += 100;
                // Sumar vida cada 5000 puntos
                if (puntos - puntosUltimaVida >= puntosParaVida) {
                    vidas++;
                    puntosUltimaVida = puntos;
                }
                break;
            }
        }

        // Game Over y gestión de vidas
        if (ballY > HEIGHT) {
            vidas--;
            if (vidas > 0) {
                // Reinicia pelota y paleta
                ballX = 400;
                ballY = 300;
                paddleX = 350;
                paddleDX = 0;
            } else {
                timer.stop();
                JOptionPane.showMessageDialog(this, "¡Game Over!");
            }
        }

        // Cambia de nivel si no hay bloques
        if (bloques.isEmpty()) {
            nivelActual++;
            if (nivelActual < Nivel.niveles.length) {
                cargarNivel(nivelActual);
                // Reinicia pelota y paleta
                ballX = 400;
                ballY = 300;
                paddleX = 350;
                paddleDX = 0;
            } else {
                timer.stop();
                JOptionPane.showMessageDialog(this, "¡Ganaste todos los niveles!");
            }
        }
        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT) paddleDX = -5;
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) paddleDX = 5;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        paddleDX = 0;
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    // Getters para HUD
    public int getVidas() { return vidas; }
    public int getPuntos() { return puntos; }
    public long getTiempoInicio() { return tiempoInicio; }
    public int getNivelActual() { return nivelActual; }

    // Clase Bloque con id/tipo
    class Bloque {
        int x, y, ancho = 60, alto = 30, tipo;
        public Bloque(int x, int y, int tipo) {
            this.x = x; this.y = y; this.tipo = tipo;
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Arkanoid con Imágenes");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        ArkanoidBase juego = new ArkanoidBase();
        HudPanel hud = new HudPanel(juego);
        frame.setLayout(new BorderLayout());
        frame.add(hud, BorderLayout.NORTH);
        frame.add(juego, BorderLayout.CENTER);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        // Redibuja el HUD cada 100ms
        new Timer(100, e -> hud.repaint()).start();
    }
}