package org.example.proyectohibernate.controller;

import org.example.proyectohibernate.dao.EquipoDAO;
import org.example.proyectohibernate.database.JPAutil;

/** Proceso separado para comprobar P8 sin reutilizar objetos o caches de la primera JVM. */
public final class VerificarReinicio {
    public static void main(String[] args) {
        try {
            boolean existe = new EquipoDAO().listar().stream().anyMatch(e -> args[0].equals(e.getCodigo()));
            if (!existe) throw new IllegalStateException("El registro confirmado no existe al reiniciar.");
            System.out.println("PERSISTENCIA_VERIFICADA");
        } finally { JPAutil.cerrar(); }
    }
}
