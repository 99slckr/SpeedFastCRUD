package cl.speedfast.vista;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

/**
 * Ventana principal del sistema SpeedFast.
 * Agrupa en pestanas la gestion de repartidores, pedidos y entregas.
 */
public class VentanaPrincipal extends JFrame {

    public VentanaPrincipal() {
        setTitle("SpeedFast - Sistema de Gestion de Entregas");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        PanelEntregas panelEntregas = new PanelEntregas();

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Repartidores", new PanelRepartidores());
        pestanas.addTab("Pedidos", new PanelPedidos());
        pestanas.addTab("Entregas", panelEntregas);

        // Al entrar a la pestana de entregas se recargan los combos y la tabla,
        // de modo que reflejen los pedidos y repartidores registrados hasta el momento.
        pestanas.addChangeListener(e -> {
            if (pestanas.getSelectedComponent() == panelEntregas) {
                panelEntregas.refrescar();
            }
        });

        add(pestanas);
    }
}