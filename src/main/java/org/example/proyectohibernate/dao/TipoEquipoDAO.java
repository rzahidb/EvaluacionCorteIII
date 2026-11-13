package org.example.proyectohibernate.dao;

import jakarta.persistence.EntityManager;
import org.example.proyectohibernate.database.JPAutil;
import org.example.proyectohibernate.model.TipoEquipo;
import java.util.List;

public class TipoEquipoDAO {
    public List<TipoEquipo> listar() {
        EntityManager em = JPAutil.getEntityManager();
        try {
            return em.createQuery("SELECT t FROM TipoEquipo t ORDER BY t.nombre", TipoEquipo.class).getResultList();
        } finally {
            em.close();
        }
    }
}
