package org.example.proyectohibernate.model;

/** Validaciones reutilizadas por el formulario y las operaciones del DAO. */
public final class ValidacionEquipo {
    private ValidacionEquipo() { }

    public static int cantidad(String texto) {
        try {
            int valor = Integer.parseInt(texto == null ? "" : texto.strip());
            if (valor < 0) throw new IllegalArgumentException("La cantidad no puede ser negativa.");
            return valor;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Ingrese una cantidad entera entre 0 y 2147483647.");
        }
    }

    public static void validar(Equipo equipo) {
        if (equipo == null) throw new IllegalArgumentException("Ingrese los datos del equipo.");
        obligatorio(equipo.getCodigo(), "El código", 10);
        obligatorio(equipo.getNombre(), "El nombre", 100);
        if (equipo.getMarca() != null && equipo.getMarca().length() > 100)
            throw new IllegalArgumentException("La marca admite máximo 100 caracteres.");
        if (equipo.getCantidad() == null || equipo.getCantidad() < 0)
            throw new IllegalArgumentException("La cantidad debe ser un entero no negativo.");
        if (equipo.getTipoEquipo() == null || equipo.getTipoEquipo().getIdTipo() == null)
            throw new IllegalArgumentException("Seleccione un tipo de equipo.");
        if (!Equipo.ESTADOS.contains(equipo.getEstado()))
            throw new IllegalArgumentException("Seleccione un estado permitido.");
    }

    private static void obligatorio(String texto, String campo, int maximo) {
        if (texto == null || texto.isBlank()) throw new IllegalArgumentException(campo + " es obligatorio.");
        if (texto.length() > maximo) throw new IllegalArgumentException(campo + " admite máximo " + maximo + " caracteres.");
    }
}
