# Matriz de pruebas P1-P8

Estudiante: Zahid Benjamín Reyes López — CIF 24013713.

Las pruebas se ejecutan con `mvnw.cmd -Pintegracion verify` usando PostgreSQL 14.15 temporal, Java 21 y la vista FXML real de JavaFX con Monocle. Las acciones del CRUD y de filtros se disparan sobre los botones del Controller de producción. Las alertas informativas/de error se capturan para comprobar su tipo y contenido; el diálogo de confirmación de eliminación sí se abre y responde durante la prueba.

| ID | Entrada y acción | Resultado esperado | Resultado obtenido | Evidencia verificable |
| --- | --- | --- | --- | --- |
| P1 | PRU-009, Equipo de prueba, cantidad 3, Computadoras, Disponible; pulsar Guardar. | Se confirma y aparece en tabla y PostgreSQL. | Correcto: 9 registros, ID generado y nombre del tipo recuperable con un nuevo EntityManager. | `p1RegistrarEquipoValido`; resumen en `evidencias/resultados_pruebas.txt`. |
| P2 | PC-001, Intento duplicado, cantidad 2; Guardar. | Rechazo por UNIQUE, sin segundo registro ni éxito. | Correcto: alerta ERROR con mensaje de código duplicado, 9 registros y ningún Intento duplicado. | `p2CodigoDuplicadoYRecuperacion`; restricción real `uq_equipo_codigo`. |
| P3 | Seleccionar PRU-009; cambiar nombre a Equipo de prueba actualizado, estado En mantenimiento y tipo Perifericos; Actualizar. | Los tres cambios persisten. | Correcto: selección carga el formulario; consulta nueva confirma nombre, estado y tipo. | `p3SeleccionYActualizacion`. |
| P4 | Seleccionar PRU-009; Eliminar, responder No y luego Sí. | Al responder No se conserva el registro; al responder Sí se elimina de tabla y base. | Correcto: tras No hay 9 registros; tras Sí hay 8 y no existe Equipo de prueba. | `p4EliminarConConfirmacionReal`, con diálogo Alert de producción. |
| P5 | Buscar COMPUTADORA, sin tipo; Aplicar filtros. | Solo coincidencias parciales, sin distinguir mayúsculas. | Correcto: 3 equipos, todos con computadora en el nombre. | `p5BusquedaParcial`. |
| P6 | Mantener COMPUTADORA y elegir Proyectores; después buscar proyector sin quitar el tipo. | Primer caso: vacío; segundo: los dos proyectores. | Correcto: 0 y luego 2 registros; todos pertenecen a Proyectores. Quitar filtros restaura 8. | `p6FiltrosCombinados`; también verifica búsqueda literal de % y entrada con comillas. |
| P7 | NEG-001, Equipo invalido, cantidad -1 y después abc; Guardar. | Validación rechaza ambos valores sin escribir. | Correcto: alerta WARNING; PostgreSQL conserva 8 equipos. | `p7CantidadNegativaYNoNumerica`, más pruebas unitarias de límites. |
| P8 | Guardar REI-001; cerrar fábrica; iniciar otro proceso Java con JPAutil; recrear fábrica, FXML y Controller. | Se recupera el equipo confirmado sin reutilizar caché/objetos previos. | Correcto: proceso separado verifica PERSISTENCIA_VERIFICADA y la nueva tabla contiene REI-001. Se retira luego el registro temporal de prueba. | `p8ReabrirPersistenciaYVista` y `VerificarReinicio.java`. |

## Integridad y transacciones adicionales

| Prueba | Resultado comprobado |
| --- | --- |
| `esquemaInicialYFXML` | El SQL de esquema/semillas se ejecuta dos veces sin duplicados; 3 tipos, 8 equipos y 3 estados. FXML y CSS cargan; el tipo se muestra por nombre. |
| `rollbackSinCambiosParciales` | Un UPDATE se envía con flush, después un INSERT falla por código repetido. El rollback conserva el nombre previo, no crea duplicado y mantiene 8 equipos. |
| `restriccionesPostgresYPoliticaDeTipos` | PostgreSQL rechaza cantidad negativa, estado no permitido, tipo inexistente y borrado de tipo referenciado. Tras cada fallo se conserva el inventario. |
| `ValidacionEquipoTest` | 13 casos de validación: cantidad vacía, no numérica, decimal, negativa y fuera del rango INTEGER; campos vacíos, tipo ausente, estado inválido, longitud excesiva y valores válidos. |

## Archivos de evidencia

- `evidencias/interfaz.png`: instantánea de la vista FXML real con los datos iniciales.
- `evidencias/resultados_pruebas.txt`: resumen de ejecución y nombres de los casos, sin credenciales.
- Código de las comprobaciones: `src/integration-test/java/.../LaboratorioIntegracionTest.java` y `src/test/java/.../ValidacionEquipoTest.java`.

Las pruebas fueron ejecutadas en Linux con PostgreSQL temporal. La ejecución interactiva en la computadora Windows del estudiante requiere crear la base local y configurar sus credenciales siguiendo el README. Para la defensa individual, repetir P1-P8 en esa computadora y mostrar los cambios en pgAdmin.
