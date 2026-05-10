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
    private final StringProperty capacidad;
    private final StringProperty disponibilidad;
    private final StringProperty estado;
    private final StringProperty codigoAreaInterna;

    /**
     * Crea una fila con la información de un área interna.
     *
     * @param codigo             Código único del área en este hospital (HospitalAreaInterna.codigo).
     * @param nombre             Nombre del área en este hospital.
     * @param tipo               Tipo genérico de área (nombreAreaInterna del backend).
     * @param capacidad          Capacidad del área en personas.
     * @param disponibilidad     Disponibilidad actual ("Disponible" u "Ocupado").
     * @param estado             Estado del área ("Activo" o "Inactivo").
     * @param codigoAreaInterna  Código del área interna genérica (para el PUT).
     */
    public AreaFila(String codigo, String nombre, String tipo, String capacidad,
                    String disponibilidad, String estado, String codigoAreaInterna) {
        this.codigo            = new SimpleStringProperty(codigo);
        this.nombre            = new SimpleStringProperty(nombre);
        this.tipo              = new SimpleStringProperty(tipo);
        this.capacidad         = new SimpleStringProperty(capacidad);
        this.disponibilidad    = new SimpleStringProperty(disponibilidad);
        this.estado            = new SimpleStringProperty(estado);
        this.codigoAreaInterna = new SimpleStringProperty(codigoAreaInterna);
    }

    /** @return Código del área en este hospital. */
    public String getCodigo()            { return codigo.get(); }

    /** @return Nombre del área. */
    public String getNombre()            { return nombre.get(); }

    /** @return Tipo del área. */
    public String getTipo()              { return tipo.get(); }

    /** @return Capacidad del área. */
    public String getCapacidad()         { return capacidad.get(); }

    /** @return Disponibilidad actual del área. */
    public String getDisponibilidad()    { return disponibilidad.get(); }

    /** @return Estado del área. */
    public String getEstado()            { return estado.get(); }

    /** @return Código del área interna genérica. */
    public String getCodigoAreaInterna() { return codigoAreaInterna.get(); }

    public StringProperty codigoProperty()            { return codigo; }
    public StringProperty nombreProperty()            { return nombre; }
    public StringProperty tipoProperty()              { return tipo; }
    public StringProperty capacidadProperty()         { return capacidad; }
    public StringProperty disponibilidadProperty()    { return disponibilidad; }
    public StringProperty estadoProperty()            { return estado; }
    public StringProperty codigoAreaInternaProperty() { return codigoAreaInterna; }
}
