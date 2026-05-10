package models;

/*
 * Clase que representa una ciudad dentro del sistema
 * Aquí almacenamos:
 * - código
 * - nombre
 * - departamento
 * - país
 * - cantidad de hospitales
 */

public class Ciudad {

    // =========================
    // ATRIBUTOS
    // =========================

    private String codigo;
    private String nombre;
    private String departamento;
    private String pais;
    private int hospitales;

    /*
     * Constructor principal, se utiliza para crear una ciudad con toda su información
     */

    public Ciudad(
            String codigo,
            String nombre,
            String departamento,
            String pais,
            int hospitales
    ) {

        this.codigo = codigo;
        this.nombre = nombre;
        this.departamento = departamento;
        this.pais = pais;
        this.hospitales = hospitales;
    }

    // =========================
    // GETTERS
    // =========================

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDepartamento() {
        return departamento;
    }

    public String getPais() {
        return pais;
    }

    public int getHospitales() {
        return hospitales;
    }

    // =========================
    // SETTERS
    // =========================

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public void setHospitales(int hospitales) {
        this.hospitales = hospitales;
    }

    /*
     * Método utilizado para mostrar la información de la ciudad
     */

    @Override
    public String toString() {

        return nombre + " - " + departamento;
    }
}
