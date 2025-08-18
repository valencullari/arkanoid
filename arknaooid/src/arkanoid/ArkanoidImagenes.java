package arkanoid;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.ArrayList;

public class ArkanoidImagenes extends JPanel implements KeyListener, ActionListener {
    private final int WIDTH = 800, HEIGHT = 600;
    private BufferedImage imgPelota, imgPaleta, imgBloque1;
    private int ballX = 400, ballY = 300, ballDiameter = 20;
    private double ballDX = 2, ballDY = -3;
    private int paddleX = 350, paddleY = 550, paddleWidth = 100, paddleHeight = 20, paddleDX = 0;
    private Timer timer;
    private ArrayList<Bloque> bloques = new ArrayList<>();

    public ArkanoidImagenes() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        // Carga imágenes (usar rutas reales después)
        imgPelota = cargarImagen("/img/foco.png");
        imgPaleta = cargarImagen("/img/sustin.png");
        imgBloque1 = cargarImagen("/img/pibep.png");

        // Inicializa bloques (ejemplo, tipo 1)
        for (int i = 0; i < 8; i++) {
            bloques.add(new Bloque(100 + i * 80, 100, 1)); // tipo 1
        }

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
            if (b.tipo == 1) {
                g.drawImage(imgBloque1, b.x, b.y, b.ancho, b.alto, null);
            }
            // Aquí podrías agregar otros tipos
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Movimiento de la pelota
        ballX += ballDX;
        ballY += ballDY;

        // Rebote en bordes laterales
        if (ballX <= 0 || ballX + ballDiameter >= WIDTH) ballDX *= -1;
        if (ballY <= 0) ballDY *= -1;

        // Rebote en la paleta con efecto angular
        if (ballY + ballDiameter >= paddleY &&
            ballX + ballDiameter >= paddleX &&
            ballX <= paddleX + paddleWidth) {

            // Calcula punto de impacto relativo
            int impact = (ballX + ballDiameter / 2) - (paddleX + paddleWidth / 2);
            double factor = (double) impact / (paddleWidth / 2); // entre -1 y 1
            double angle = factor * Math.toRadians(60); // máximo +/-60 grados de rebote
            double speed = Math.sqrt(ballDX * ballDX + ballDY * ballDY);

            ballDY = -Math.abs(speed * Math.cos(angle));
            ballDX = speed * Math.sin(angle);

            ballY = paddleY - ballDiameter;
        }

        // Movimiento de la paleta
        paddleX += paddleDX;
        if (paddleX < 0) paddleX = 0;
        if (paddleX + paddleWidth > WIDTH) paddleX = WIDTH - paddleWidth;

        // Colisión con bloques (solo atraviesa, no rebota ni rompe)
        // Puedes expandir para romper o cambiar tipo, pero por ahora solo atraviesa

        // Game Over
        if (ballY > HEIGHT) {
            timer.stop();
            JOptionPane.showMessageDialog(this, "¡Game Over!");
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
        frame.add(new ArkanoidImagenes());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}