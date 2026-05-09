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
    private final StringProperty ciudad;
    private final StringProperty telefono;
    private final StringProperty estado;

    /**
     * Crea una fila con la informacion de un hospital.
     *
     * @param codigo   Código único del hospital.
     * @param nombre   Nombre del hospital.
     * @param ciudad   Ciudad donde está ubicado.
     * @param telefono Número de contacto.
     * @param estado   Estado del hospital ("Activo" o "Inactivo").
     */
    public HospitalFila(String codigo, String nombre, String ciudad,
                        String telefono, String estado) {
        this.codigo   = new SimpleStringProperty(codigo);
        this.nombre   = new SimpleStringProperty(nombre);
        this.ciudad   = new SimpleStringProperty(ciudad);
        this.telefono = new SimpleStringProperty(telefono);
        this.estado   = new SimpleStringProperty(estado);
    }

    /** @return Código del hospital. */
    public String getCodigo()   { return codigo.get(); }

    /** @return Nombre del hospital. */
    public String getNombre()   { return nombre.get(); }

    /** @return Ciudad del hospital. */
    public String getCiudad()   { return ciudad.get(); }

    /** @return Teléfono del hospital. */
    public String getTelefono() { return telefono.get(); }

    /** @return Estado del hospital. */
    public String getEstado()   { return estado.get(); }

    /** @return Propiedad observable del código. */
    public StringProperty codigoProperty()   { return codigo; }

    /** @return Propiedad observable del nombre. */
    public StringProperty nombreProperty()   { return nombre; }

    /** @return Propiedad observable de la ciudad. */
    public StringProperty ciudadProperty()   { return ciudad; }

    /** @return Propiedad observable del teléfono. */
    public StringProperty telefonoProperty() { return telefono; }

    /** @return Propiedad observable del estado. */
    public StringProperty estadoProperty()   { return estado; }
}