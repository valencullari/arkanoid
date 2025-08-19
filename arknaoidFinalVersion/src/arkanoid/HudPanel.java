package arkanoid;

import javax.swing.*;
import java.awt.*;

public class HudPanel extends JPanel {
    private ArkanoidBase juego;

    public HudPanel(ArkanoidBase juego) {
        this.juego = juego;
        setPreferredSize(new Dimension(800, 40));
        setBackground(Color.BLACK);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 18));

        String vidas = "Vidas: " + juego.getVidas();
        String puntos = "Puntos: " + juego.getPuntos();
        long tiempo = (System.currentTimeMillis() - juego.getTiempoInicio()) / 1000;
        String tiempoStr = "Tiempo: " + tiempo + "s";
        String nivel = "Nivel: " + (juego.getNivelActual() + 1);
        String hudText = vidas + "    " + puntos + "    " + tiempoStr + "    " + nivel;

        int textWidth = g.getFontMetrics().stringWidth(hudText);
        int x = (getWidth() - textWidth) / 2;
        int y = 28;
        g.drawString(hudText, x, y);
    }
}