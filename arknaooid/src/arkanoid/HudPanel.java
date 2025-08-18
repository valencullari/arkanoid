package arkanoid;

import javax.swing.*;
import java.awt.*;

public class HudPanel extends JPanel {
    private ArkanoidBase juego;

    public HudPanel(ArkanoidBase juego) {
        this.juego = juego;
        setPreferredSize(new Dimension(800, 40));
        setBackground(Color.DARK_GRAY);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString("Vidas: " + juego.getVidas(), 20, 28);
        g.drawString("Puntos: " + juego.getPuntos(), 150, 28);
        long tiempo = (System.currentTimeMillis() - juego.getTiempoInicio()) / 1000;
        g.drawString("Tiempo: " + tiempo + "s", 300, 28);
        g.drawString("Nivel: " + (juego.getNivelActual() + 1), 450, 28);
    }
}