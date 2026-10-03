package cl.speedfast.vista;

import cl.speedfast.dao.RepartidorDAO;
import cl.speedfast.modelo.Repartidor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.sql.SQLException;
import java.util.List;

/**
 * Panel de gestion de repartidores.
 * Permite registrar, editar, eliminar y listar repartidores,
 * delegando el acceso a datos en RepartidorDAO.
 */
public class PanelRepartidores extends JPanel {

    private final RepartidorDAO dao = new RepartidorDAO();

    private final JTextField campoNombre = new JTextField(20);
    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new Object[]{"ID", "Nombre"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modeloTabla);

    /** Id del repartidor seleccionado en la tabla; 0 si no hay seleccion. */
    private int idSeleccionado = 0;

    public PanelRepartidores() {
        setLayout(new BorderLayout(10, 10));

        // --- Formulario superior ---
        JPanel formulario = new JPanel(new FlowLayout(FlowLayout.LEFT));
        formulario.add(new JLabel("Nombre:"));
        formulario.add(campoNombre);

        JButton btnAgregar = new JButton("Agregar");
        JButton btnActualizar = new JButton("Actualizar");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnLimpiar = new JButton("Limpiar");

        formulario.add(btnAgregar);
        formulario.add(btnActualizar);
        formulario.add(btnEliminar);
        formulario.add(btnLimpiar);

        add(formulario, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        // --- Eventos ---
        btnAgregar.addActionListener(e -> agregar());
        btnActualizar.addActionListener(e -> actualizar());
        btnEliminar.addActionListener(e -> eliminar());
        btnLimpiar.addActionListener(e -> limpiar());

        tabla.getSelectionModel().addListSelectionListener(e -> cargarSeleccion());

        cargarTabla();
    }

    /** Registra un nuevo repartidor con el nombre ingresado. */
    private void agregar() {
        String nombre = campoNombre.getText().trim();

        if (nombre.isEmpty()) {
            mostrarAdvertencia("Debe ingresar el nombre del repartidor.");
            return;
        }

        try {
            dao.create(new Repartidor(nombre));
            mostrarInfo("Repartidor registrado correctamente.");
            limpiar();
            cargarTabla();
        } catch (SQLException ex) {
            mostrarError("No se pudo registrar el repartidor.", ex);
        }
    }

    /** Actualiza el nombre del repartidor seleccionado en la tabla. */
    private void actualizar() {
        if (idSeleccionado == 0) {
            mostrarAdvertencia("Seleccione un repartidor de la tabla.");
            return;
        }

        String nombre = campoNombre.getText().trim();
        if (nombre.isEmpty()) {
            mostrarAdvertencia("Debe ingresar el nombre del repartidor.");
            return;
        }

        try {
            dao.update(new Repartidor(idSeleccionado, nombre));
            mostrarInfo("Repartidor actualizado correctamente.");
            limpiar();
            cargarTabla();
        } catch (SQLException ex) {
            mostrarError("No se pudo actualizar el repartidor.", ex);
        }
    }

    /** Elimina el repartidor seleccionado, previa confirmacion del usuario. */
    private void eliminar() {
        if (idSeleccionado == 0) {
            mostrarAdvertencia("Seleccione un repartidor de la tabla.");
            return;
        }

        int confirmacion = JOptionPane.showConfirmDialog(
                this,
                "Esta seguro de eliminar el repartidor #" + idSeleccionado + "?",
                "Confirmar eliminacion",
                JOptionPane.YES_NO_OPTION);

        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            dao.delete(idSeleccionado);
            mostrarInfo("Repartidor eliminado correctamente.");
            limpiar();
            cargarTabla();
        } catch (SQLException ex) {
            mostrarError("No se pudo eliminar el repartidor. "
                    + "Es posible que tenga entregas asociadas.", ex);
        }
    }

    /** Carga en la tabla todos los repartidores registrados. */
    private void cargarTabla() {
        try {
            List<Repartidor> repartidores = dao.readAll();
            modeloTabla.setRowCount(0);

            for (Repartidor r : repartidores) {
                modeloTabla.addRow(new Object[]{r.getId(), r.getNombre()});
            }
        } catch (SQLException ex) {
            mostrarError("No se pudo cargar la lista de repartidores.", ex);
        }
    }

    /** Copia al formulario los datos de la fila seleccionada en la tabla. */
    private void cargarSeleccion() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            return;
        }
        idSeleccionado = (int) modeloTabla.getValueAt(fila, 0);
        campoNombre.setText(modeloTabla.getValueAt(fila, 1).toString());
    }

    /** Limpia el formulario y descarta la seleccion actual. */
    private void limpiar() {
        campoNombre.setText("");
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