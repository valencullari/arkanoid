package arkanoid;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.ArrayList;

public class ArkanoidBase extends JPanel implements KeyListener, ActionListener {

    private BufferedImage  imgPaleta, imgFondo;
    private int ballX = 0, ballY = 0, ballDiameter = 20;
    private double ballDX = 2, ballDY = -3;
    private final double BALL_SPEED_INITIAL = 8.0;
    private final double BALL_SPEED_INCREMENT = 0.05;
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
    private boolean evitarRecalculoBloques = false;
    private JButton btnReiniciar;
    private ImageIcon imgPelota, imgBloque1, imgBloque2_1, imgBloque2_2, imgBloque3_1, imgBloque3_2,imgBloque3_3, imgBloque4_1,imgBloque4_2,imgBloque4_3,imgBloque4_4, imgBloque5;


    private double blockWidthFactor = 0.075, blockHeightFactor = 0.05;
    private double paddleWidthFactor = 0.15, paddleHeightFactor = 0.044;
    private double ballDiameterFactor = 0.018;

    private void recalcularDimensiones() {
        // Paddle
        paddleWidth = (int) (getWidth() * paddleWidthFactor);
        paddleHeight = (int) (getHeight() * paddleHeightFactor);
        paddleY = getHeight() - 50;
        paddleX = getWidth() / 2 - paddleWidth / 2;
        // Ball
        ballDiameter = (int) (getWidth() * ballDiameterFactor);
        if (ballDiameter < 10) ballDiameter = 10;
        // bloques: recalcula posicion y tamaño para centrar solo si se está cambiando de nivel
        // (NO cuando se pierde una vida)
        if (!bloques.isEmpty() && !evitarRecalculoBloques) {
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

        
        imgPaleta = cargarImagen("/img/paleta.png");
        imgFondo = cargarImagen("/img/fondo.png"); // Fondo personalizado

        imgPelota = new ImageIcon(getClass().getResource("/img/pelota.gif"));
        
        imgBloque1 = new ImageIcon(getClass().getResource("/img/bloque1.gif"));
        
        imgBloque2_1 = new ImageIcon(getClass().getResource("/img/bloque2_1.gif"));
        imgBloque2_2 = new ImageIcon(getClass().getResource("/img/bloque2_2.gif"));
        
        imgBloque3_1 = new ImageIcon(getClass().getResource("/img/bloque3_1.gif"));
        imgBloque3_2 = new ImageIcon(getClass().getResource("/img/bloque3_2.gif"));
        imgBloque3_3 = new ImageIcon(getClass().getResource("/img/bloque3_3.gif"));
        
        imgBloque4_1 = new ImageIcon(getClass().getResource("/img/bloque4_1.gif"));
        imgBloque4_2 = new ImageIcon(getClass().getResource("/img/bloque4_2.gif"));
        imgBloque4_3 = new ImageIcon(getClass().getResource("/img/bloque4_3.gif"));
        imgBloque4_4 = new ImageIcon(getClass().getResource("/img/bloque4_4.gif"));
        
        imgBloque5 = new ImageIcon(getClass().getResource("/img/bloque5.png")); // Placeholder for irrompible

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

        btnReiniciar = new JButton("Reiniciar");
        btnReiniciar.setFocusable(false);
        btnReiniciar.setVisible(false);
        btnReiniciar.addActionListener(e -> reiniciarJuego());
        setLayout(null);
        add(btnReiniciar);
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
            // si no hay imagen, crea un cuadrado grits
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
        // dibuja el fondo ajustado al tamaño del panel
        if (imgFondo != null) {
            g.drawImage(imgFondo, 0, 0, getWidth(), getHeight(), null);
        }
        // dibuja la pelota animada escalada
        if (imgPelota != null && imgPelota.getImage() != null) {
            g.drawImage(imgPelota.getImage(), ballX, ballY, ballDiameter, ballDiameter, this);
        }
        // dibuja la paleta
        if (imgPaleta != null) {
            g.drawImage(imgPaleta, paddleX, paddleY, paddleWidth, paddleHeight, null);
        }
        // dibuja los bloques
        for (Bloque b : bloques) {
            ImageIcon icon = b.getEstadoActual();
            if (icon != null && icon.getImage() != null) {
                g.drawImage(icon.getImage(), b.x, b.y, b.ancho, b.alto, this);
            } else {
                g.setColor(Color.GRAY);
                g.fillRect(b.x, b.y, b.ancho, b.alto);
            }
        }
        // si está esperando inicio dibuja un mensaje para iniciar
        if (esperandoInicio) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.drawString("Presiona ESPACIO para lanzar la pelota", getWidth()/2 - 200, getHeight()/2);
        }
        if (!timer.isRunning() && vidas == 0) {
            btnReiniciar.setBounds(getWidth()/2 - 75, getHeight()/2 + 40, 150, 40);
            btnReiniciar.setVisible(true);
        } else {
            btnReiniciar.setVisible(false);
        }
    }

    private void reiniciarJuego() {
        vidas = 3;
        puntos = 0;
        puntosUltimaVida = 0;
        nivelActual = 0;
        cargarNivel(nivelActual);
        colocarPelotaSobrePaleta();
        esperandoInicio = true;
        ballDX = 2;
        ballDY = -BALL_SPEED_INITIAL;
        tiempoInicio = System.currentTimeMillis();
        timer.start();
        btnReiniciar.setVisible(false);
        repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (esperandoInicio) {
            colocarPelotaSobrePaleta();
            repaint();
            return;
        }
        // movimiento de la pelota
        ballX += ballDX;
        ballY += ballDY;

        // rebote en bordes laterales
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

        // rebote en la paleta con efecto angular
        if (ballY + ballDiameter >= paddleY &&
            ballX + ballDiameter >= paddleX &&
            ballX <= paddleX + paddleWidth) {

            int impact = (ballX + ballDiameter / 2) - (paddleX + paddleWidth / 2);
            double factor = Math.max(-1, Math.min(1, (double) impact / (paddleWidth / 2)));
            double angle = factor * Math.toRadians(60);
            double speed = Math.sqrt(ballDX * ballDX + ballDY * ballDY);

// si el impacto es muy cerca del centro fuerza un angulo minimo para evitar rebote vertical puro
            if (Math.abs(factor) < 0.1) {
                angle = Math.copySign(Math.toRadians(15), angle == 0 ? 1 : angle);
            }

            ballDY = -Math.abs(speed * Math.cos(angle));
            ballDX = speed * Math.sin(angle);

            ballY = paddleY - ballDiameter;
        }

        // movimiento de la paleta
        paddleX += paddleDX;
        if (paddleX < 0) paddleX = 0;
        if (paddleX + paddleWidth > getWidth()) paddleX = getWidth() - paddleWidth;

        // colision con bloques
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
  // si es bloque irrompible (tipo 5) cambiar el angulo para evitar rebotes infinitos
                if (b.tipo == 5) {
                    double randomAngle = Math.toRadians((Math.random() - 0.5) * 20); // entre -10 y +10 grados
                    double speed = Math.sqrt(ballDX * ballDX + ballDY * ballDY);
                    double angle = Math.atan2(ballDY, ballDX) + randomAngle;
                    ballDX = speed * Math.cos(angle);
                    ballDY = speed * Math.sin(angle);
                }
                if (b.golpear()) {
                    bloques.remove(i);
                    i--;
                    puntos += 100;
                }
                // aumenta la velocidad de la pelota
                double speed = Math.sqrt(ballDX * ballDX + ballDY * ballDY) + BALL_SPEED_INCREMENT;
                double angle = Math.atan2(ballDY, ballDX);
                ballDX = speed * Math.cos(angle);
                ballDY = speed * Math.sin(angle);
                // Sumar vida cada 5000 puntos
                if (puntos / puntosParaVida > puntosUltimaVida / puntosParaVida) {
                    vidas++;
                    puntosUltimaVida = puntos;
                }
                break;
            }
        }

        // game over y gestion de vidas
        if (ballY > getHeight()) {
            vidas--;
            if (vidas > 0) {
                evitarRecalculoBloques = true; // No mover bloques
                recalcularDimensiones(); // Solo centra paleta y pelota
                evitarRecalculoBloques = false;
                colocarPelotaSobrePaleta();
                esperandoInicio = true;
                paddleDX = 0;
            } else {
                timer.stop();
                JOptionPane.showMessageDialog(this, "¡Game Over!");
                btnReiniciar.setVisible(true);
            }
        }

        // cambia de nivel si no hay bloques (ignorando los de tipo 5)
        boolean soloIrrompibles = true;
        for (Bloque b : bloques) {
            if (b.tipo != 5) {
                soloIrrompibles = false;
                break;
            }
        }
        if (bloques.isEmpty() || soloIrrompibles) {
            nivelActual++;
            if (nivelActual < Nivel.niveles.length) {
                cargarNivel(nivelActual);
                recalcularDimensiones(); // Centrar paleta
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
        if (e.getKeyCode() == KeyEvent.VK_LEFT) paddleDX = -16;
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) paddleDX = 16;
        if (e.getKeyCode() == KeyEvent.VK_SPACE && esperandoInicio) {
            esperandoInicio = false;
            ballDX = 2;
            ballDY = -BALL_SPEED_INITIAL;
        }
        // boton provisional para pasar de nivel con la tecla 'P'
        if (e.getKeyCode() == KeyEvent.VK_P) {
            nivelActual++;
            if (nivelActual < Nivel.niveles.length) {
                cargarNivel(nivelActual);
                colocarPelotaSobrePaleta();
                esperandoInicio = true;
                paddleDX = 0;
                ballDX = 2;
                ballDY = -BALL_SPEED_INITIAL;
                repaint();
            } else {
                timer.stop();
                JOptionPane.showMessageDialog(this, "¡Ganaste todos los niveles!");
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        paddleDX = 0;
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    // getters para HUD
    public int getVidas() { return vidas; }
    public int getPuntos() { return puntos; }
    public long getTiempoInicio() { return tiempoInicio; }
    public int getNivelActual() { return nivelActual; }

    // clase bloque con id/tipo
    class Bloque {
        int x, y, ancho, alto, tipo, golpesRestantes;
        ImageIcon[] estados;
        public Bloque(int x, int y, int ancho, int alto, int tipo) {
            this.x = x; this.y = y; this.ancho = ancho; this.alto = alto; this.tipo = tipo;
            switch (tipo) {
                case 2:
                    golpesRestantes = 2;
                    estados = new ImageIcon[] { imgBloque2_1, imgBloque2_2 };
                    break;
                case 3:
                    golpesRestantes = 3;
                    estados = new ImageIcon[] { imgBloque3_1, imgBloque3_2, imgBloque3_3 };
                    break;
                case 4:
                    golpesRestantes = 4;
                    estados = new ImageIcon[] { imgBloque4_1, imgBloque4_2, imgBloque4_3, imgBloque4_4 };
                    break;
                case 5:
                    golpesRestantes = Integer.MAX_VALUE;
                    estados = new ImageIcon[] { imgBloque5 };
                    break;
                default:
                    golpesRestantes = 1;
                    estados = new ImageIcon[] { imgBloque1 };
            }
        }
        public ImageIcon getEstadoActual() {
            int idx = Math.max(0, estados.length - golpesRestantes);
            if (idx >= estados.length) idx = estados.length - 1;
            return estados[idx];
        }
        public boolean golpear() {
            if (tipo == 5) return false;
            golpesRestantes--;
            return golpesRestantes <= 0;
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
