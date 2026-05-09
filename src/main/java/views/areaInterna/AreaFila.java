package views.areaInterna;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Modelo de datos que representa una fila en la tabla de áreas internas.
 * Usa StringProperty para que JavaFX pueda observar cambios en los valores.
 */
public class AreaFila {
    private final StringProperty nombre;
    private final StringProperty tipo;
    private final StringProperty capacidad;
    private final StringProperty disponibilidad;
    private final StringProperty estado;

    /**
     * Crea una fila con la información de un área interna.
     *
     * @param nombre         Nombre del área.
     * @param tipo           Tipo de área (Consultorio, Quirófano, etc.).
     * @param capacidad      Capacidad del área en personas.
     * @param disponibilidad Disponibilidad actual ("Disponible" u "Ocupado").
     * @param estado         Estado del área ("Activo" o "Inactivo").
     */
    public AreaFila(String nombre, String tipo, String capacidad,
                    String disponibilidad, String estado) {
        this.nombre         = new SimpleStringProperty(nombre);
        this.tipo           = new SimpleStringProperty(tipo);
        this.capacidad      = new SimpleStringProperty(capacidad);
        this.disponibilidad = new SimpleStringProperty(disponibilidad);
        this.estado         = new SimpleStringProperty(estado);
    }

    /** @return Nombre del área. */
    public String getNombre()         { return nombre.get(); }

    /** @return Tipo del área. */
    public String getTipo()           { return tipo.get(); }

    /** @return Capacidad del área. */
    public String getCapacidad()      { return capacidad.get(); }

    /** @return Disponibilidad actual del área. */
    public String getDisponibilidad() { return disponibilidad.get(); }

    /** @return Estado del área. */
    public String getEstado()         { return estado.get(); }

    /** @return Propiedad observable del nombre. */
    public StringProperty nombreProperty()         { return nombre; }

    /** @return Propiedad observable del tipo. */
    public StringProperty tipoProperty()           { return tipo; }

    /** @return Propiedad observable de la capacidad. */
    public StringProperty capacidadProperty()      { return capacidad; }

    /** @return Propiedad observable de la disponibilidad. */
    public StringProperty disponibilidadProperty() { return disponibilidad; }

    /** @return Propiedad observable del estado. */
    public StringProperty estadoProperty()         { return estado; }
}