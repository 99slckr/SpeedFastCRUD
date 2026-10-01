package cl.speedfast.modelo;

/**
 * Representa un pedido registrado en el sistema.
 * El tipo y el estado corresponden a los valores ENUM definidos en la base de datos.
 */
public class Pedido {

    private int id;
    private String direccion;
    private String tipo;
    private String estado;

    public Pedido(int id, String direccion, String tipo, String estado) {
        this.id = id;
        this.direccion = direccion;
        this.tipo = tipo;
        this.estado = estado;
    }

    /** Constructor para pedidos nuevos: el id lo asigna la base de datos. */
    public Pedido(String direccion, String tipo, String estado) {
        this(0, direccion, tipo, estado);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    /** Texto que se muestra en los JComboBox de la interfaz. */
    @Override
    public String toString() {
        return id + " - " + direccion;
    }
}