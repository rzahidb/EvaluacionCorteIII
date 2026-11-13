# Pruebas manuales

Estas pruebas deben ejecutarse en la base local. Resultado obtenido y evidencia se completan al realizarlas.

| ID | Entrada y accion | Resultado esperado | Resultado obtenido / evidencia |
| --- | --- | --- | --- |
| P1 | Guardar PRU-009 con nombre, tipo, estado y cantidad validos. | Registro visible en tabla y PostgreSQL. | Pendiente de ejecucion local. |
| P2 | Intentar guardar otro PC-001. | Error de codigo duplicado; no se crea otro registro. | Pendiente de ejecucion local. |
| P3 | Seleccionar PRU-009 y actualizar nombre, estado y tipo. | Cambios persistidos y visibles. | Pendiente de ejecucion local. |
| P4 | Eliminar PRU-009; primero cancelar y luego confirmar. | Cancelar conserva; confirmar elimina. | Pendiente de ejecucion local. |
| P5 | Buscar COMPUTADORA. | Tres coincidencias iniciales sin distinguir mayusculas. | Pendiente de ejecucion local. |
| P6 | Buscar proyector y elegir tipo Proyectores. | Solo coincidencias del tipo seleccionado. | Pendiente de ejecucion local. |
| P7 | Intentar guardar cantidad -1 o abc. | Alert de validacion sin escritura. | Pendiente de ejecucion local. |
| P8 | Guardar un equipo, cerrar la aplicacion y abrirla otra vez. | El equipo sigue almacenado. | Pendiente de ejecucion local. |
