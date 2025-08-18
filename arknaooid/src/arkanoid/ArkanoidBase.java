package arkanoid;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.ArrayList;

public class ArkanoidBase extends JPanel implements KeyListener, ActionListener {
    // private final int WIDTH = 800, HEIGHT = 600;
    private BufferedImage imgPelota, imgPaleta, imgBloque1, imgFondo;
    private int ballX = 0, ballY = 0, ballDiameter = 20;
    private double ballDX = 2, ballDY = -3;
    private final double BALL_SPEED_INITIAL = 3.0;
    private final double BALL_SPEED_INCREMENT = 0.2;
    private int paddleX = 0, paddleY = 0, paddleWidth = 100, paddleHeight = 20, paddleDX = 0;
    private Timer timer;
    private ArrayList<Bloque> bloques = new ArrayList<>();
    private int nivelActual = 0;
    private int vidas = 3;
    private int puntos = 0;
    private long tiempoInicio = System.currentTimeMillis();
    private int puntosParaVida = 5000;
    private int puntosUltimaVida = 0;
    private boolean esperandoInicio = true;

    // Factores proporcionales
    private double blockWidthFactor = 0.075, blockHeightFactor = 0.05;
    private double paddleWidthFactor = 0.15, paddleHeightFactor = 0.033;
    private double ballDiameterFactor = 0.033;

    private void recalcularDimensiones() {
        // Paddle
        paddleWidth = (int) (getWidth() * paddleWidthFactor);
        paddleHeight = (int) (getHeight() * paddleHeightFactor);
        paddleY = getHeight() - 50;
        paddleX = getWidth() / 2 - paddleWidth / 2;
        // Ball
        ballDiameter = (int) (getWidth() * ballDiameterFactor);
        if (ballDiameter < 10) ballDiameter = 10;
        // Bloques: recalcula posición y tamaño para centrar
        if (!bloques.isEmpty()) {
            // Obtener dimensiones del mapa actual
            int cols = 0, rows = 0;
            if (nivelActual < Nivel.niveles.length) {
                int[][] mapa = Nivel.niveles[nivelActual].mapa;
                cols = mapa[0].length;
                rows = mapa.length;
            }
            int blockW = (int) (getWidth() * blockWidthFactor);
            int blockH = (int) (getHeight() * blockHeightFactor);
            int totalWidth = blockW * cols;
            int startX = (getWidth() - totalWidth) / 2;
            int startY = (int) (getHeight() * 0.1);
            int i = 0;
            for (int fila = 0; fila < rows; fila++) {
                for (int col = 0; col < cols; col++) {
                    if (nivelActual < Nivel.niveles.length && Nivel.niveles[nivelActual].mapa[fila][col] != 0) {
                        if (i < bloques.size()) {
                            Bloque b = bloques.get(i);
                            b.x = startX + col * blockW;
                            b.y = startY + fila * blockH;
                            b.ancho = blockW;
                            b.alto = blockH;
                            i++;
                        }
                    }
                }
            }
        }
    }

    private void cargarNivel(int n) {
        bloques.clear();
        if (n < Nivel.niveles.length) {
            int[][] mapa = Nivel.niveles[n].mapa;
            int cols = mapa[0].length;
            int rows = mapa.length;
            int blockW = (int) (getWidth() * blockWidthFactor);
            int blockH = (int) (getHeight() * blockHeightFactor);
            int totalWidth = blockW * cols;
            int startX = (getWidth() - totalWidth) / 2;
            int startY = (int) (getHeight() * 0.1);
            for (int fila = 0; fila < rows; fila++) {
                for (int col = 0; col < cols; col++) {
                    if (mapa[fila][col] != 0) {
                        int x = startX + col * blockW;
                        int y = startY + fila * blockH;
                        bloques.add(new Bloque(x, y, blockW, blockH, mapa[fila][col]));
                    }
                }
            }
        }
    }

    public ArkanoidBase() {
        // setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        imgPelota = cargarImagen("/img/foco.png");
        imgPaleta = cargarImagen("/img/paleta.png");
        imgBloque1 = cargarImagen("/img/bloque1.png");
        imgFondo = cargarImagen("/img/fondo.png"); // Fondo personalizado

        // Inicializar paddle y pelota en el centro relativo
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                recalcularDimensiones();
                colocarPelotaSobrePaleta();
            }
        });
        recalcularDimensiones();
        colocarPelotaSobrePaleta();
        esperandoInicio = true;
        nivelActual = 0;
        cargarNivel(nivelActual);

        timer = new Timer(10, this);
        timer.start();
    }

    private void colocarPelotaSobrePaleta() {
        paddleY = getHeight() - 50;
        ballX = paddleX + paddleWidth / 2 - ballDiameter / 2;
        ballY = paddleY - ballDiameter;
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
        // Dibuja el fondo ajustado al tamaño del panel
        if (imgFondo != null) {
            g.drawImage(imgFondo, 0, 0, getWidth(), getHeight(), null);
        }
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
        // Si está esperando inicio, dibuja un mensaje
        if (esperandoInicio) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.drawString("Presiona ESPACIO para lanzar la pelota", getWidth()/2 - 200, getHeight()/2);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (esperandoInicio) {
            colocarPelotaSobrePaleta();
            repaint();
            return;
        }
        // Movimiento de la pelota
        ballX += ballDX;
        ballY += ballDY;

        // Rebote en bordes laterales
        if (ballX <= 0) {
            ballX = 0;
            ballDX = Math.abs(ballDX);
        }
        if (ballX + ballDiameter >= getWidth()) {
            ballX = getWidth() - ballDiameter;
            ballDX = -Math.abs(ballDX);
        }
        if (ballY <= 0) {
            ballY = 0;
            ballDY = Math.abs(ballDY);
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
        if (paddleX + paddleWidth > getWidth()) paddleX = getWidth() - paddleWidth;

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
                // Aumenta la velocidad de la pelota
                double speed = Math.sqrt(ballDX * ballDX + ballDY * ballDY) + BALL_SPEED_INCREMENT;
                double angle = Math.atan2(ballDY, ballDX);
                ballDX = speed * Math.cos(angle);
                ballDY = speed * Math.sin(angle);
                // Sumar vida cada 5000 puntos
                if (puntos - puntosUltimaVida >= puntosParaVida) {
                    vidas++;
                    puntosUltimaVida = puntos;
                }
                break;
            }
        }

        // Game Over y gestión de vidas
        if (ballY > getHeight()) {
            vidas--;
            if (vidas > 0) {
                colocarPelotaSobrePaleta();
                esperandoInicio = true;
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
                colocarPelotaSobrePaleta();
                esperandoInicio = true;
                paddleDX = 0;
                // Reinicia la velocidad de la pelota
                ballDX = 2;
                ballDY = -BALL_SPEED_INITIAL;
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
        if (e.getKeyCode() == KeyEvent.VK_SPACE && esperandoInicio) {
            esperandoInicio = false;
            ballDX = 2;
            ballDY = -BALL_SPEED_INITIAL;
        }
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
        int x, y, ancho, alto, tipo;
        public Bloque(int x, int y, int ancho, int alto, int tipo) {
            this.x = x; this.y = y; this.ancho = ancho; this.alto = alto; this.tipo = tipo;
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Arkanoid");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(true); // Ahora es redimensionable
        ArkanoidBase juego = new ArkanoidBase();
        HudPanel hud = new HudPanel(juego);
        frame.setLayout(new BorderLayout());
        frame.add(hud, BorderLayout.NORTH);
        frame.add(juego, BorderLayout.CENTER);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH); // Inicia maximizado
        frame.setVisible(true);
        // Redibuja el HUD cada 100ms
        new Timer(100, e -> hud.repaint()).start();
    }
}