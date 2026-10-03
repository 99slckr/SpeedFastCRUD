package cl.speedfast.vista;

import cl.speedfast.dao.PedidoDAO;
import cl.speedfast.modelo.Pedido;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.sql.SQLException;
import java.util.List;

/**
 * Panel de gestion de pedidos.
 * Permite registrar, editar, eliminar, listar y filtrar pedidos,
 * delegando el acceso a datos en PedidoDAO.
 */
public class PanelPedidos extends JPanel {

    private static final String[] TIPOS = {"COMIDA", "ENCOMIENDA", "EXPRESS"};
    private static final String[] ESTADOS = {"PENDIENTE", "EN_REPARTO", "ENTREGADO"};

    private final PedidoDAO dao = new PedidoDAO();

    private final JTextField campoDireccion = new JTextField(18);
    private final JComboBox<String> comboTipo = new JComboBox<>(TIPOS);
    private final JComboBox<String> comboEstado = new JComboBox<>(ESTADOS);

    private final JComboBox<String> filtroEstado = new JComboBox<>();
    private final JComboBox<String> filtroTipo = new JComboBox<>();

    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new Object[]{"ID", "Direccion", "Tipo", "Estado"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modeloTabla);

    /** Id del pedido seleccionado en la tabla; 0 si no hay seleccion. */
    private int idSeleccionado = 0;

    public PanelPedidos() {
        setLayout(new BorderLayout(10, 10));

        // --- Formulario ---
        JPanel formulario = new JPanel(new FlowLayout(FlowLayout.LEFT));
        formulario.add(new JLabel("Direccion:"));
        formulario.add(campoDireccion);
        formulario.add(new JLabel("Tipo:"));
        formulario.add(comboTipo);
        formulario.add(new JLabel("Estado:"));
        formulario.add(comboEstado);

        JButton btnAgregar = new JButton("Agregar");
        JButton btnActualizar = new JButton("Actualizar");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnLimpiar = new JButton("Limpiar");

        formulario.add(btnAgregar);
        formulario.add(btnActualizar);
        formulario.add(btnEliminar);
        formulario.add(btnLimpiar);

        // --- Filtros ---
        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filtroEstado.addItem("Todos");
        for (String estado : ESTADOS) {
            filtroEstado.addItem(estado);
        }
        filtroTipo.addItem("Todos");
        for (String tipo : TIPOS) {
            filtroTipo.addItem(tipo);
        }

        JButton btnFiltrar = new JButton("Filtrar");
        JButton btnVerTodos = new JButton("Ver todos");

        panelFiltros.add(new JLabel("Filtrar por estado:"));
        panelFiltros.add(filtroEstado);
        panelFiltros.add(new JLabel("y tipo:"));
        panelFiltros.add(filtroTipo);
        panelFiltros.add(btnFiltrar);
        panelFiltros.add(btnVerTodos);

        JPanel zonaSuperior = new JPanel(new BorderLayout());
        zonaSuperior.add(formulario, BorderLayout.NORTH);
        zonaSuperior.add(panelFiltros, BorderLayout.SOUTH);

        add(zonaSuperior, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        // --- Eventos ---
        btnAgregar.addActionListener(e -> agregar());
        btnActualizar.addActionListener(e -> actualizar());
        btnEliminar.addActionListener(e -> eliminar());
        btnLimpiar.addActionListener(e -> limpiar());
        btnFiltrar.addActionListener(e -> filtrar());
        btnVerTodos.addActionListener(e -> {
            filtroEstado.setSelectedIndex(0);
            filtroTipo.setSelectedIndex(0);
            cargarTabla();
        });

        tabla.getSelectionModel().addListSelectionListener(e -> cargarSeleccion());

        cargarTabla();
    }

    /** Registra un nuevo pedido con los datos del formulario. */
    private void agregar() {
        String direccion = campoDireccion.getText().trim();

        if (direccion.isEmpty()) {
            mostrarAdvertencia("Debe ingresar la direccion de entrega.");
            return;
        }

        try {
            dao.create(new Pedido(
                    direccion,
                    (String) comboTipo.getSelectedItem(),
                    (String) comboEstado.getSelectedItem()));
            mostrarInfo("Pedido registrado correctamente.");
            limpiar();
            cargarTabla();
        } catch (SQLException ex) {
            mostrarError("No se pudo registrar el pedido.", ex);
        }
    }

    /** Actualiza el pedido seleccionado con los datos del formulario. */
    private void actualizar() {
        if (idSeleccionado == 0) {
            mostrarAdvertencia("Seleccione un pedido de la tabla.");
            return;
        }

        String direccion = campoDireccion.getText().trim();
        if (direccion.isEmpty()) {
            mostrarAdvertencia("Debe ingresar la direccion de entrega.");
            return;
        }

        try {
            dao.update(new Pedido(
                    idSeleccionado,
                    direccion,
                    (String) comboTipo.getSelectedItem(),
                    (String) comboEstado.getSelectedItem()));
            mostrarInfo("Pedido actualizado correctamente.");
            limpiar();
            cargarTabla();
        } catch (SQLException ex) {
            mostrarError("No se pudo actualizar el pedido.", ex);
        }
    }

    /** Elimina el pedido seleccionado, previa confirmacion del usuario. */
    private void eliminar() {
        if (idSeleccionado == 0) {
            mostrarAdvertencia("Seleccione un pedido de la tabla.");
            return;
        }

        int confirmacion = JOptionPane.showConfirmDialog(
                this,
                "Esta seguro de eliminar el pedido #" + idSeleccionado + "?",
                "Confirmar eliminacion",
                JOptionPane.YES_NO_OPTION);

        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            dao.delete(idSeleccionado);
            mostrarInfo("Pedido eliminado correctamente.");
            limpiar();
            cargarTabla();
        } catch (SQLException ex) {
            mostrarError("No se pudo eliminar el pedido. "
                    + "Es posible que tenga entregas asociadas.", ex);
        }
    }

    /** Carga en la tabla todos los pedidos registrados. */
    private void cargarTabla() {
        try {
            mostrarEnTabla(dao.readAll());
        } catch (SQLException ex) {
            mostrarError("No se pudo cargar la lista de pedidos.", ex);
        }
    }

    /** Carga en la tabla los pedidos que cumplen los filtros seleccionados. */
    private void filtrar() {
        String estado = (String) filtroEstado.getSelectedItem();
        String tipo = (String) filtroTipo.getSelectedItem();

        try {
            mostrarEnTabla(dao.readFiltrado(
                    "Todos".equals(estado) ? null : estado,
                    "Todos".equals(tipo) ? null : tipo));
        } catch (SQLException ex) {
            mostrarError("No se pudo aplicar el filtro.", ex);
        }
    }

    /** Vuelca una lista de pedidos en el modelo de la tabla. */
    private void mostrarEnTabla(List<Pedido> pedidos) {
        modeloTabla.setRowCount(0);
        for (Pedido p : pedidos) {
            modeloTabla.addRow(new Object[]{
                    p.getId(), p.getDireccion(), p.getTipo(), p.getEstado()});
        }
    }

    /** Copia al formulario los datos de la fila seleccionada en la tabla. */
    private void cargarSeleccion() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            return;
        }
        idSeleccionado = (int) modeloTabla.getValueAt(fila, 0);
        campoDireccion.setText(modeloTabla.getValueAt(fila, 1).toString());
        comboTipo.setSelectedItem(modeloTabla.getValueAt(fila, 2));
        comboEstado.setSelectedItem(modeloTabla.getValueAt(fila, 3));
    }

    /** Limpia el formulario y descarta la seleccion actual. */
    private void limpiar() {
        campoDireccion.setText("");
        comboTipo.setSelectedIndex(0);
        comboEstado.setSelectedIndex(0);
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