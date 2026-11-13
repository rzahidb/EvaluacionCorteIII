package org.example.proyectohibernate.database;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/** Centraliza el cierre y rollback; los DAO trabajan unicamente con entidades JPA. */
public final class UnidadTrabajo {
    private final Supplier<EntityManager> proveedor;
    public UnidadTrabajo() { this(JPAutil::getEntityManager); }
    public UnidadTrabajo(Supplier<EntityManager> proveedor) { this.proveedor = proveedor; }

    public <T> T consultar(Function<EntityManager, T> consulta) {
        try (EntityManager em = proveedor.get()) { return consulta.apply(em); }
    }

    public void escribir(Consumer<EntityManager> operacion) {
        try (EntityManager em = proveedor.get()) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                operacion.accept(em);
                tx.commit();
            } catch (RuntimeException ex) {
                if (tx.isActive()) {
                    try { tx.rollback(); }
                    catch (RuntimeException rollbackError) { ex.addSuppressed(rollbackError); }
                }
                throw ex;
            }
        }
    }
}
