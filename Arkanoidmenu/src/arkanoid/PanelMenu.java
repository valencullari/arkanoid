package arkanoid;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class PanelMenu extends JPanel {
	private static final long serialVersionUID = 1L;

    private Image fondo;

    public PanelMenu(VentanaJuego ventana) {
        // Cargar imagen de fondo
        ImageIcon img = new ImageIcon("peak.png");
        fondo = img.getImage();

        setLayout(new GridBagLayout()); // para centrar los botones

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);
        gbc.gridx = 0;

        // Botón JUGAR
        JButton btnJugar = new JButton(new ImageIcon("play.png"));
        btnJugar.setBorderPainted(false);
        btnJugar.setContentAreaFilled(false);
        btnJugar.setFocusPainted(false);
        btnJugar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        gbc.gridy = 0;
        add(btnJugar, gbc);

        // Botón SALIR
        JButton btnSalir = new JButton(new ImageIcon("exit.png"));
        btnSalir.setBorderPainted(false);
        btnSalir.setContentAreaFilled(false);
        btnSalir.setFocusPainted(false);
        btnSalir.setCursor(new Cursor(Cursor.HAND_CURSOR));
        gbc.gridy = 1;
        add(btnSalir, gbc);

        // Acciones
        btnJugar.addActionListener(e -> ventana.mostrarJuego());
        btnSalir.addActionListener(e -> System.exit(0));
        
        ImageIcon iconNormalPlay = new ImageIcon("play.png");
        ImageIcon iconHoverPlay = new ImageIcon("playhover.png"); // versión más grande del icono
        btnJugar.setIcon(iconNormalPlay);

        btnJugar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnJugar.setIcon(iconHoverPlay);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btnJugar.setIcon(iconNormalPlay);
            }
        });
        
        ImageIcon iconNormalExit = new ImageIcon("exit.png");
        ImageIcon iconHoverExit = new ImageIcon("exithover.png");
        btnSalir.setIcon(iconNormalExit);

        btnSalir.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnSalir.setIcon(iconHoverExit);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btnSalir.setIcon(iconNormalExit);
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // dibujar la imagen de fondo escalada al tamaño del panel
        g.drawImage(fondo, 0, 0, getWidth(), getHeight(), this);
    }
  
}
