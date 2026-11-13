package org.example.proyectohibernate.dao;

import org.example.proyectohibernate.database.UnidadTrabajo;
import org.example.proyectohibernate.model.TipoEquipo;
import java.util.List;

public class TipoEquipoDAO {
    private final UnidadTrabajo trabajo;
    public TipoEquipoDAO() { this(new UnidadTrabajo()); }
    public TipoEquipoDAO(UnidadTrabajo trabajo) { this.trabajo = trabajo; }

    public List<TipoEquipo> listar() {
        return trabajo.consultar(em -> em.createQuery(
                "SELECT t FROM TipoEquipo t ORDER BY t.nombre", TipoEquipo.class).getResultList());
    }
}
