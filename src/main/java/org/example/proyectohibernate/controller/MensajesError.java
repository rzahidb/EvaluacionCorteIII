package org.example.proyectohibernate.controller;

import java.sql.SQLException;

/** Traduce errores sin mostrar sentencias SQL ni datos de conexion en la ventana. */
public final class MensajesError {
    private MensajesError() { }
    public static String describir(Throwable error) {
        for (Throwable causa = error; causa != null; causa = causa.getCause()) {
            if (causa instanceof SQLException sql) {
                String estado = sql.getSQLState();
                if ("23505".equals(estado)) return "Ya existe un equipo con ese código. Ingrese uno diferente.";
                if ("23503".equals(estado)) return "El tipo está relacionado con registros o ya no existe. Recargue los datos.";
                if ("23514".equals(estado)) return "PostgreSQL rechazó el estado, la cantidad o un dato vacío. Revise el formulario.";
                if ("28P01".equals(estado)) return "PostgreSQL rechazó las credenciales. Revise DB_USER y DB_PASSWORD.";
                if ("3D000".equals(estado)) return "No existe bd_laboratorio_orm. Ejecute primero los scripts SQL.";
                if (estado != null && estado.startsWith("08")) return "No se pudo conectar a PostgreSQL. Revise el servicio, DB_URL y el puerto.";
            }
            if (causa instanceof IllegalArgumentException || causa instanceof IllegalStateException)
                return causa.getMessage();
        }
        return "No se pudo completar la operación. Revise la conexión y ejecute el script del esquema; consulte la consola para más detalles.";
    }
}
