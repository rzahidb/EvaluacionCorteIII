package org.example.proyectohibernate.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.example.proyectohibernate.database.UnidadTrabajo;
import org.example.proyectohibernate.model.Equipo;
import org.example.proyectohibernate.model.TipoEquipo;
import org.example.proyectohibernate.model.ValidacionEquipo;
import java.util.List;
import java.util.Locale;

public class EquipoDAO {
    private final UnidadTrabajo trabajo;
    public EquipoDAO() { this(new UnidadTrabajo()); }
    public EquipoDAO(UnidadTrabajo trabajo) { this.trabajo = trabajo; }

    public void guardar(Equipo equipo) {
        ValidacionEquipo.validar(equipo);
        if (equipo.getIdEquipo() != null) throw new IllegalArgumentException("Para crear, use un equipo sin identificador.");
        trabajo.escribir(em -> {
            equipo.setTipoEquipo(tipoExistente(em, equipo));
            // UNIQUE en PostgreSQL resuelve incluso duplicados concurrentes.
            em.persist(equipo);
        });
    }

    public List<Equipo> listar() { return buscar("", null); }

    public List<Equipo> buscar(String nombre, Integer idTipo) {
        String fragmento = nombre == null ? "" : nombre.strip().toLowerCase(Locale.ROOT);
        return trabajo.consultar(em -> {
            // Se agregan solo fragmentos fijos de JPQL; los valores van en parametros.
            String jpql = "SELECT e FROM Equipo e JOIN FETCH e.tipoEquipo WHERE 1 = 1";
            if (!fragmento.isEmpty()) jpql += " AND LOWER(e.nombre) LIKE :nombre ESCAPE '!'";
            if (idTipo != null) jpql += " AND e.tipoEquipo.idTipo = :idTipo";
            jpql += " ORDER BY e.idEquipo";
            TypedQuery<Equipo> consulta = em.createQuery(jpql, Equipo.class);
            if (!fragmento.isEmpty()) consulta.setParameter("nombre", "%" + literalLike(fragmento) + "%");
            if (idTipo != null) consulta.setParameter("idTipo", idTipo);
            return consulta.getResultList();
        });
    }

    private static String literalLike(String valor) {
        return valor.replace("!", "!!").replace("%", "!%").replace("_", "!_");
    }

    public void actualizar(Equipo datos) {
        ValidacionEquipo.validar(datos);
        if (datos.getIdEquipo() == null) throw new IllegalArgumentException("Seleccione un equipo para actualizar.");
        trabajo.escribir(em -> {
            Equipo existente = em.find(Equipo.class, datos.getIdEquipo());
            if (existente == null) throw new IllegalArgumentException("El equipo ya no existe. Recargue el inventario.");
            existente.setCodigo(datos.getCodigo());
            existente.setNombre(datos.getNombre());
            existente.setMarca(datos.getMarca());
            existente.setEstado(datos.getEstado());
            existente.setCantidad(datos.getCantidad());
            existente.setTipoEquipo(tipoExistente(em, datos));
            // existente es managed: Hibernate detecta los cambios al confirmar.
        });
    }

    public void eliminar(Integer id) {
        if (id == null) throw new IllegalArgumentException("Seleccione un equipo para eliminar.");
        trabajo.escribir(em -> {
            Equipo existente = em.find(Equipo.class, id);
            if (existente == null) throw new IllegalArgumentException("El equipo ya no existe. Recargue el inventario.");
            em.remove(existente);
        });
    }

    private TipoEquipo tipoExistente(EntityManager em, Equipo equipo) {
        TipoEquipo tipo = em.find(TipoEquipo.class, equipo.getTipoEquipo().getIdTipo());
        if (tipo == null) throw new IllegalArgumentException("El tipo seleccionado ya no existe. Recargue los tipos.");
        return tipo;
    }
}
