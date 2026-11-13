# Demostración de rollback

La restricción `uq_equipo_codigo` de PostgreSQL impide repetir códigos. El DAO no sustituye esta garantía por una comprobación previa susceptible a condiciones de carrera.

## Duplicado desde la interfaz

1. Registrar un equipo con código `PC-001`, nombre `Intento duplicado`, cantidad 2, tipo Computadoras y estado Disponible.
2. PostgreSQL rechaza el INSERT con SQLSTATE `23505`.
3. `UnidadTrabajo.escribir()` captura la excepción, llama a `rollback()` si corresponde y vuelve a lanzarla.
4. El Controller muestra un error comprensible; no hay mensaje de éxito y la ventana sigue abierta.
5. Consultar el inventario: permanece un único `PC-001`, sin el registro `Intento duplicado`.

## Comprobar que no quedan cambios parciales

La prueba `rollbackSinCambiosParciales` realiza dos escrituras en la misma transacción de Hibernate:

```java
trabajo.escribir(em -> {
    Equipo existente = em.find(Equipo.class, id);
    existente.setNombre("CAMBIO QUE DEBE REVERTIRSE");
    em.flush(); // El UPDATE llega al servidor, pero todavia no hay commit.

    Equipo duplicado = new Equipo();
    duplicado.setCodigo("PC-001"); // Ya existe en los datos iniciales.
    duplicado.setNombre("Forzar UNIQUE");
    duplicado.setCantidad(1);
    duplicado.setEstado("Disponible");
    duplicado.setTipoEquipo(existente.getTipoEquipo());
    em.persist(duplicado); // Falla por UNIQUE; UnidadTrabajo ejecuta rollback.
});
```

Después del error se abre otro `EntityManager` y se comprueba que el primer nombre conserva su valor anterior, no existe el duplicado y siguen presentes los 8 equipos iniciales. Esto demuestra que ni siquiera el UPDATE enviado mediante `flush()` queda confirmado cuando falla el INSERT posterior.

Ejecutar con `mvnw.cmd -Pintegracion test`. El perfil usa PostgreSQL temporal y los mismos DAO y helper transaccional que utiliza la aplicación.

Un identificador de identidad puede saltarse después de una operación fallida: las secuencias no se revierten y esto no implica que hayan quedado equipos parciales.

También se verifican directamente las restricciones CHECK de cantidad/estado, la FK de tipos y el rechazo al eliminar un tipo referenciado. El JDBC de la prueba solo crea la base y aplica el DDL; las escrituras de inventario probadas utilizan entidades Hibernate.
