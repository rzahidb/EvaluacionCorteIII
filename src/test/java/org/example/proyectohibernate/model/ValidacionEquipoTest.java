package org.example.proyectohibernate.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

class ValidacionEquipoTest {
    private Equipo valido() {
        TipoEquipo tipo = new TipoEquipo(); tipo.setIdTipo(1);
        Equipo equipo = new Equipo();
        equipo.setCodigo("PRU-001"); equipo.setNombre("Equipo de prueba");
        equipo.setCantidad(0); equipo.setEstado("Disponible"); equipo.setTipoEquipo(tipo);
        return equipo;
    }

    @Test void aceptaEquipoValidoSinMarca() { assertDoesNotThrow(() -> ValidacionEquipo.validar(valido())); }

    @ParameterizedTest @NullAndEmptySource @ValueSource(strings = {" ", "abc", "1.5", "2147483648", "-1"})
    void rechazaCantidadesInvalidas(String texto) {
        assertThrows(IllegalArgumentException.class, () -> ValidacionEquipo.cantidad(texto));
    }

    @Test void aceptaLimitesDeCantidad() {
        assertEquals(0, ValidacionEquipo.cantidad("0"));
        assertEquals(Integer.MAX_VALUE, ValidacionEquipo.cantidad("2147483647"));
    }

    @Test void rechazaCamposObligatorios() {
        Equipo sinCodigo = valido(); sinCodigo.setCodigo(" ");
        assertThrows(IllegalArgumentException.class, () -> ValidacionEquipo.validar(sinCodigo));
        Equipo sinNombre = valido(); sinNombre.setNombre("");
        assertThrows(IllegalArgumentException.class, () -> ValidacionEquipo.validar(sinNombre));
    }

    @Test void rechazaTipoAusente() {
        Equipo equipo = valido(); equipo.setTipoEquipo(null);
        assertThrows(IllegalArgumentException.class, () -> ValidacionEquipo.validar(equipo));
    }

    @Test void rechazaEstadoNoPermitido() {
        Equipo equipo = valido(); equipo.setEstado("Prestado");
        assertThrows(IllegalArgumentException.class, () -> ValidacionEquipo.validar(equipo));
    }

    @Test void rechazaLongitudesExcesivas() {
        Equipo equipo = valido(); equipo.setCodigo("12345678901");
        assertThrows(IllegalArgumentException.class, () -> ValidacionEquipo.validar(equipo));
    }
}
