# Rollback

En EquipoDAO, cada escritura abre un EntityManager y obtiene su EntityTransaction.
Se llama a begin(), se ejecuta persist(), la modificacion managed o remove(), y luego commit().
Si ocurre una excepcion, se llama a rollback() si la transaccion sigue activa y se propaga el error al Controller.
El bloque finally cierra el EntityManager.

Para comprobarlo: intentar guardar un equipo con codigo PC-001, ya existente.
PostgreSQL rechaza el INSERT por UNIQUE, se ejecuta rollback() y el Controller muestra un Alert de error.
Al consultar PostgreSQL debe seguir existiendo un solo PC-001.
Los saltos en los identificadores despues de un error son normales: las secuencias no se revierten.
