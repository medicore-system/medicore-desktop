package views.hospital;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Modelo de datos que representa una fila en la tabla de hospitales.
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

    /**
     * Crea una fila con la información de un hospital.
     *
     * @param codigo       Código único del hospital.
     * @param nombre       Nombre del hospital.
     * @param direccion    Dirección del hospital.
     * @param ciudad       Ciudad donde está ubicado.
     * @param telefono     Número de contacto.
     * @param estado       Estado del hospital ("Activo" o "Inactivo").
     * @param codigoCiudad Código de la ciudad asociada.
     */
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

    /** @return Código del hospital. */
    public String getCodigo()       { return codigo.get(); }

    /** @return Nombre del hospital. */
    public String getNombre()       { return nombre.get(); }

    /** @return Dirección del hospital. */
    public String getDireccion()    { return direccion.get(); }

    /** @return Ciudad del hospital. */
    public String getCiudad()       { return ciudad.get(); }

    /** @return Teléfono del hospital. */
    public String getTelefono()     { return telefono.get(); }

    /** @return Estado del hospital. */
    public String getEstado()       { return estado.get(); }

    /** @return Código de la ciudad asociada. */
    public String getCodigoCiudad() { return codigoCiudad; }

    /** @return Propiedad observable del código. */
    public StringProperty codigoProperty()    { return codigo; }

    /** @return Propiedad observable del nombre. */
    public StringProperty nombreProperty()    { return nombre; }

    /** @return Propiedad observable de la dirección. */
    public StringProperty direccionProperty() { return direccion; }

    /** @return Propiedad observable de la ciudad. */
    public StringProperty ciudadProperty()    { return ciudad; }

    /** @return Propiedad observable del teléfono. */
    public StringProperty telefonoProperty()  { return telefono; }

    /** @return Propiedad observable del estado. */
    public StringProperty estadoProperty()    { return estado; }

    /** @return Campo donde se encontró la coincidencia de búsqueda. */
    public String getCampoCoincidente()         { return campoCoincidente; }

    /** @param c Campo coincidente; si es null se guarda como cadena vacía. */
    public void setCampoCoincidente(String c)   { this.campoCoincidente = c == null ? "" : c; }
}