package co.edu.uniquindio.poo.model.enums;

/**
 * Roles de usuario dentro del sistema.
 * RF-13: Autorización básica de operaciones según el rol del usuario.
 */
public enum Rol {

    ESTUDIANTE("Estudiante"),
    ADMINISTRATIVO("Administrativo"),
    RESPONSABLE("Responsable de atención");

    private final String descripcion;

    Rol(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
