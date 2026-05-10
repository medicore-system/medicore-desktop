package views.areaInterna;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Modelo de datos que representa una fila en la tabla de áreas internas.
 * Usa StringProperty para que JavaFX pueda observar cambios en los valores.
 */
public class AreaFila {
    private final StringProperty codigo;
    private final StringProperty nombre;
    private final StringProperty tipo;
    private final StringProperty descripcion;
    private final StringProperty estado;
    private final StringProperty codigoAreaInterna;

    /**
     * Crea una fila con la información de un área interna.
     *
     * @param codigo            Código único del área en este hospital.
     * @param nombre            Nombre del área en este hospital.
     * @param tipo              Tipo genérico de área (nombreAreaInterna del backend).
     * @param descripcion       Descripción detallada del área.
     * @param estado            Estado del área ("Activo" o "Inactivo").
     * @param codigoAreaInterna Código del área interna genérica (necesario para el PUT).
     */
    public AreaFila(String codigo, String nombre, String tipo,
                    String descripcion, String estado, String codigoAreaInterna) {
        this.codigo            = new SimpleStringProperty(codigo);
        this.nombre            = new SimpleStringProperty(nombre);
        this.tipo              = new SimpleStringProperty(tipo);
        this.descripcion       = new SimpleStringProperty(descripcion);
        this.estado            = new SimpleStringProperty(estado);
        this.codigoAreaInterna = new SimpleStringProperty(codigoAreaInterna);
    }

    /** @return Código del área en este hospital. */
    public String getCodigo()            { return codigo.get(); }

    /** @return Nombre del área. */
    public String getNombre()            { return nombre.get(); }

    /** @return Tipo genérico del área. */
    public String getTipo()              { return tipo.get(); }

    /** @return Descripción del área. */
    public String getDescripcion()       { return descripcion.get(); }

    /** @return Estado del área. */
    public String getEstado()            { return estado.get(); }

    /** @return Código del área interna genérica. */
    public String getCodigoAreaInterna() { return codigoAreaInterna.get(); }

    /** @return Propiedad observable del código. */
    public StringProperty codigoProperty()            { return codigo; }

    /** @return Propiedad observable del nombre. */
    public StringProperty nombreProperty()            { return nombre; }

    /** @return Propiedad observable del tipo. */
    public StringProperty tipoProperty()              { return tipo; }

    /** @return Propiedad observable de la descripción. */
    public StringProperty descripcionProperty()       { return descripcion; }

    /** @return Propiedad observable del estado. */
    public StringProperty estadoProperty()            { return estado; }

    /** @return Propiedad observable del código de área interna. */
    public StringProperty codigoAreaInternaProperty() { return codigoAreaInterna; }
}
