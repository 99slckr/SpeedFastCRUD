package cl.speedfast;

import cl.speedfast.vista.VentanaPrincipal;

import javax.swing.SwingUtilities;

/**
 * Punto de entrada del sistema SpeedFast.
 * Lanza la ventana principal en el hilo de eventos de Swing.
 */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VentanaPrincipal().setVisible(true));
    }
}