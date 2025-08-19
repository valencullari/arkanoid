package arkanoid;

	import javax.swing.*;
	import java.awt.*;

	public class VentanaJuego extends JFrame {
		private static final long serialVersionUID = 1L;
		

		private JPanel panelActual;

	    public VentanaJuego() {
	        setUndecorated(true);
	        setExtendedState(JFrame.MAXIMIZED_BOTH);
	        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	        setLayout(new BorderLayout());

	        // Mostrar panel menú al inicio
	        panelActual = new PanelMenu(this);
	        add(panelActual, BorderLayout.CENTER);

	        setVisible(true);
	    }

	    // Método para cambiar al panel del juego
	    public void mostrarJuego() {
	        remove(panelActual);
	        panelActual = new PanelJuego();
	        add(panelActual, BorderLayout.CENTER);
	        revalidate();
	        repaint();
	    }

	    public static void main(String[] args) {
	        SwingUtilities.invokeLater(() -> new VentanaJuego());
	    }
	}