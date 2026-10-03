package cl.speedfast.vista;

import cl.speedfast.dao.EntregaDAO;
import cl.speedfast.dao.PedidoDAO;
import cl.speedfast.dao.RepartidorDAO;
import cl.speedfast.modelo.Entrega;
import cl.speedfast.modelo.Pedido;
import cl.speedfast.modelo.Repartidor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Panel de gestion de entregas.
 * Asocia un pedido con un repartidor mediante combos cargados desde
 * la base de datos, y registra la fecha y hora de la entrega.
 */
public class PanelEntregas extends JPanel {

    /** Formato de fecha usado en la interfaz: dia-mes-anio. */
    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");

    /** Formato de hora usado en la interfaz: horas y minutos. */
    private static final DateTimeFormatter FORMATO_HORA =
            DateTimeFormatter.ofPattern("HH:mm");

    private final EntregaDAO entregaDAO = new EntregaDAO();
    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private final RepartidorDAO repartidorDAO = new RepartidorDAO();

    private final JComboBox<Pedido> comboPedidos = new JComboBox<>();
    private final JComboBox<Repartidor> comboRepartidores = new JComboBox<>();
    private final JTextField campoFecha = new JTextField(10);
    private final JTextField campoHora = new JTextField(6);

    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new Object[]{"ID", "Pedido", "Direccion", "Repartidor", "Fecha", "Hora"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modeloTabla);

    /** Id de la entrega seleccionada en la tabla; 0 si no hay seleccion. */
    private int idSeleccionado = 0;

    public PanelEntregas() {
        setLayout(new BorderLayout(10, 10));

        // --- Formulario ---
        JPanel formulario = new JPanel(new FlowLayout(FlowLayout.LEFT));
        formulario.add(new JLabel("Pedido:"));
        formulario.add(comboPedidos);
        formulario.add(new JLabel("Repartidor:"));
        formulario.add(comboRepartidores);
        formulario.add(new JLabel("Fecha (dd-mm-aaaa):"));
        formulario.add(campoFecha);
        formulario.add(new JLabel("Hora (hh:mm):"));
        formulario.add(campoHora);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAgregar = new JButton("Agregar");
        JButton btnActualizar = new JButton("Actualizar");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnLimpiar = new JButton("Limpiar");

        botones.add(btnAgregar);
        botones.add(btnActualizar);
        botones.add(btnEliminar);
        botones.add(btnLimpiar);

        JPanel zonaSuperior = new JPanel(new BorderLayout());
        zonaSuperior.add(formulario, BorderLayout.NORTH);
        zonaSuperior.add(botones, BorderLayout.SOUTH);

        add(zonaSuperior, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        // --- Eventos ---
        btnAgregar.addActionListener(e -> agregar());
        btnActualizar.addActionListener(e -> actualizar());
        btnEliminar.addActionListener(e -> eliminar());
        btnLimpiar.addActionListener(e -> limpiar());

        tabla.getSelectionModel().addListSelectionListener(e -> cargarSeleccion());

        refrescar();
    }

    /**
     * Recarga los combos y la tabla desde la base de datos.
     * La ventana principal lo invoca al mostrar esta pestana, de modo que
     * los pedidos y repartidores creados en las otras pestanas aparezcan aqui.
     */
    public void refrescar() {
        cargarCombos();
        cargarTabla();
    }

    /** Registra una nueva entrega con los datos del formulario. */
    private void agregar() {
        Entrega entrega = construirEntregaDesdeFormulario(0);
        if (entrega == null) {
            return;
        }

        try {
            entregaDAO.create(entrega);
            mostrarInfo("Entrega registrada correctamente.");
            limpiar();
            cargarTabla();
        } catch (SQLException ex) {
            mostrarError("No se pudo registrar la entrega.", ex);
        }
    }

    /** Actualiza la entrega seleccionada con los datos del formulario. */
    private void actualizar() {
        if (idSeleccionado == 0) {
            mostrarAdvertencia("Seleccione una entrega de la tabla.");
            return;
        }

        Entrega entrega = construirEntregaDesdeFormulario(idSeleccionado);
        if (entrega == null) {
            return;
        }

        try {
            entregaDAO.update(entrega);
            mostrarInfo("Entrega actualizada correctamente.");
            limpiar();
            cargarTabla();
        } catch (SQLException ex) {
            mostrarError("No se pudo actualizar la entrega.", ex);
        }
    }

    /** Elimina la entrega seleccionada, previa confirmacion del usuario. */
    private void eliminar() {
        if (idSeleccionado == 0) {
            mostrarAdvertencia("Seleccione una entrega de la tabla.");
            return;
        }

        int confirmacion = JOptionPane.showConfirmDialog(
                this,
                "Esta seguro de eliminar la entrega #" + idSeleccionado + "?",
                "Confirmar eliminacion",
                JOptionPane.YES_NO_OPTION);

        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            entregaDAO.delete(idSeleccionado);
            mostrarInfo("Entrega eliminada correctamente.");
            limpiar();
            cargarTabla();
        } catch (SQLException ex) {
            mostrarError("No se pudo eliminar la entrega.", ex);
        }
    }

    /**
     * Valida el formulario y construye una entrega a partir de sus datos.
     *
     * @param id id de la entrega, o 0 si se trata de una entrega nueva.
     * @return la entrega construida, o null si algun dato es invalido.
     */
    private Entrega construirEntregaDesdeFormulario(int id) {
        Pedido pedido = (Pedido) comboPedidos.getSelectedItem();
        Repartidor repartidor = (Repartidor) comboRepartidores.getSelectedItem();

        if (pedido == null || repartidor == null) {
            mostrarAdvertencia("Debe existir al menos un pedido y un repartidor registrados.");
            return null;
        }

        String textoFecha = campoFecha.getText().trim();
        String textoHora = campoHora.getText().trim();

        if (textoFecha.isEmpty() || textoHora.isEmpty()) {
            mostrarAdvertencia("Debe ingresar la fecha y la hora de la entrega.");
            return null;
        }

        LocalDate fecha;
        LocalTime hora;
        try {
            fecha = LocalDate.parse(textoFecha, FORMATO_FECHA);
            hora = LocalTime.parse(textoHora, FORMATO_HORA);
        } catch (DateTimeParseException ex) {
            mostrarAdvertencia("Formato invalido."
                    + "\nFecha: dd-mm-aaaa (por ejemplo 05-10-2026)"
                    + "\nHora: hh:mm (por ejemplo 14:30)");
            return null;
        }

        return new Entrega(id, pedido.getId(), repartidor.getId(), fecha, hora);
    }

    /** Recarga los combos de pedidos y repartidores desde la base de datos. */
    private void cargarCombos() {
        try {
            comboPedidos.removeAllItems();
            for (Pedido p : pedidoDAO.readAll()) {
                comboPedidos.addItem(p);
            }

            comboRepartidores.removeAllItems();
            for (Repartidor r : repartidorDAO.readAll()) {
                comboRepartidores.addItem(r);
            }
        } catch (SQLException ex) {
            mostrarError("No se pudieron cargar los pedidos y repartidores.", ex);
        }
    }

    /** Carga en la tabla todas las entregas registradas. */
    private void cargarTabla() {
        try {
            List<Entrega> entregas = entregaDAO.readAll();
            modeloTabla.setRowCount(0);

            for (Entrega e : entregas) {
                modeloTabla.addRow(new Object[]{
                        e.getId(),
                        e.getIdPedido(),
                        e.getDireccionPedido(),
                        e.getNombreRepartidor(),
                        e.getFecha().format(FORMATO_FECHA),
                        e.getHora().format(FORMATO_HORA)});
            }
        } catch (SQLException ex) {
            mostrarError("No se pudo cargar la lista de entregas.", ex);
        }
    }

    /** Copia al formulario los datos de la fila seleccionada en la tabla. */
    private void cargarSeleccion() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            return;
        }
        idSeleccionado = (int) modeloTabla.getValueAt(fila, 0);
        campoFecha.setText(modeloTabla.getValueAt(fila, 4).toString());
        campoHora.setText(modeloTabla.getValueAt(fila, 5).toString());

        seleccionarPedidoPorId((int) modeloTabla.getValueAt(fila, 1));
    }

    /** Deja seleccionado en el combo el pedido cuyo id coincide con el indicado. */
    private void seleccionarPedidoPorId(int idPedido) {
        for (int i = 0; i < comboPedidos.getItemCount(); i++) {
            if (comboPedidos.getItemAt(i).getId() == idPedido) {
                comboPedidos.setSelectedIndex(i);
                return;
            }
        }
    }

    /** Limpia el formulario y descarta la seleccion actual. */
    private void limpiar() {
        campoFecha.setText("");
        campoHora.setText("");
        idSeleccionado = 0;
        tabla.clearSelection();
    }

    private void mostrarInfo(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Operacion exitosa",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarAdvertencia(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Datos incompletos",
                JOptionPane.WARNING_MESSAGE);
    }

    private void mostrarError(String mensaje, SQLException ex) {
        JOptionPane.showMessageDialog(this,
                mensaje + "\n\nDetalle tecnico: " + ex.getMessage(),
                "Error de base de datos",
                JOptionPane.ERROR_MESSAGE);
    }
}