package org.example.proyectohibernate.database;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;

/** Una sola fabrica para la aplicacion y un EntityManager por unidad de trabajo. */
public final class JPAutil {
    private static EntityManagerFactory entityManagerFactory;
    private JPAutil() { }

    public static synchronized void iniciar() {
        if (entityManagerFactory == null || !entityManagerFactory.isOpen()) {
            String password = System.getenv("DB_PASSWORD");
            if (password == null || password.isBlank()) {
                throw new IllegalStateException("Configure DB_PASSWORD con su contraseña local de PostgreSQL y reinicie la aplicación.");
            }
            Map<String, Object> propiedades = new HashMap<>();
            propiedades.put("jakarta.persistence.jdbc.password", password);
            agregarEntorno(propiedades, "DB_URL", "jakarta.persistence.jdbc.url");
            agregarEntorno(propiedades, "DB_USER", "jakarta.persistence.jdbc.user");
            entityManagerFactory = Persistence.createEntityManagerFactory("laboratorioPU", propiedades);
        }
    }

    private static void agregarEntorno(Map<String, Object> propiedades, String variable, String propiedad) {
        String valor = System.getenv(variable);
        if (valor != null && !valor.isBlank()) propiedades.put(propiedad, valor);
    }

    public static synchronized EntityManagerFactory getEntityManagerFactory() {
        iniciar();
        return entityManagerFactory;
    }

    public static EntityManager getEntityManager() {
        return getEntityManagerFactory().createEntityManager();
    }

    public static synchronized void cerrar() {
        if (entityManagerFactory != null) {
            if (entityManagerFactory.isOpen()) entityManagerFactory.close();
            entityManagerFactory = null;
        }
    }
}
