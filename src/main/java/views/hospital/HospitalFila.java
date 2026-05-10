package views.hospital;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Representa una fila en la tabla de hospitales.
 * Usa StringProperty para que JavaFX pueda observar cambios en los valores.
 */
public class HospitalFila {

    private final StringProperty codigo;
    private final StringProperty nombre;
    private final StringProperty direccion;
    private final StringProperty ciudad;
    private final StringProperty telefono;
    private final StringProperty estado;
    private final String codigoCiudad;

    /** Campo en el que se produjo la coincidencia de búsqueda (no observable). */
    private String campoCoincidente = "";

    public HospitalFila(String codigo, String nombre, String direccion,
                        String ciudad, String telefono, String estado,
                        String codigoCiudad) {
        this.codigo       = new SimpleStringProperty(codigo);
        this.nombre       = new SimpleStringProperty(nombre);
        this.direccion    = new SimpleStringProperty(direccion);
        this.ciudad       = new SimpleStringProperty(ciudad);
        this.telefono     = new SimpleStringProperty(telefono);
        this.estado       = new SimpleStringProperty(estado);
        this.codigoCiudad = codigoCiudad;
    }

    public String getCodigo()       { return codigo.get(); }
    public String getNombre()       { return nombre.get(); }
    public String getDireccion()    { return direccion.get(); }
    public String getCiudad()       { return ciudad.get(); }
    public String getTelefono()     { return telefono.get(); }
    public String getEstado()       { return estado.get(); }
    public String getCodigoCiudad() { return codigoCiudad; }

    public StringProperty codigoProperty()    { return codigo; }
    public StringProperty nombreProperty()    { return nombre; }
    public StringProperty direccionProperty() { return direccion; }
    public StringProperty ciudadProperty()    { return ciudad; }
    public StringProperty telefonoProperty()  { return telefono; }
    public StringProperty estadoProperty()    { return estado; }

    public String getCampoCoincidente()           { return campoCoincidente; }
    public void   setCampoCoincidente(String c)   { this.campoCoincidente = c == null ? "" : c; }
}
