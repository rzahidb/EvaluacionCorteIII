-- ScriptEvaluacionZahid-III.sql
-- Ejecucion completa desde la carpeta del proyecto (psql, no Query Tool):
-- psql -h localhost -U postgres -d postgres -f Script/ScriptEvaluacionZahid-III.sql
-- Solicita la contrasena local de PostgreSQL.
-- En pgAdmin: crear bd_laboratorio_orm con el menu Create > Database;
-- abrir Query Tool sobre esa base y ejecutar solo INICIO_ESQUEMA a FIN_ESQUEMA.
-- Las lineas que empiezan con barra inversa son comandos exclusivos de psql.
\set ON_ERROR_STOP on
\connect postgres
SELECT 'CREATE DATABASE bd_laboratorio_orm'
WHERE NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'bd_laboratorio_orm');
\gexec
\connect bd_laboratorio_orm

-- INICIO_ESQUEMA
-- EJECUTAR CONECTADO A bd_laboratorio_orm, no a postgres.
-- No elimina ni sobrescribe registros existentes. Las tablas se crean manualmente.
BEGIN;

CREATE SCHEMA IF NOT EXISTS inventario;

CREATE TABLE IF NOT EXISTS inventario.tipo_equipo (
    id_tipo INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(80),
    CONSTRAINT uq_tipo_equipo_nombre UNIQUE (nombre),
    CONSTRAINT chk_tipo_nombre CHECK (btrim(nombre) <> '')
);

CREATE TABLE IF NOT EXISTS inventario.equipo (
    id_equipo INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    codigo VARCHAR(10) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    marca VARCHAR(100),
    estado VARCHAR(30) NOT NULL,
    cantidad INTEGER NOT NULL,
    id_tipo INTEGER NOT NULL,
    CONSTRAINT uq_equipo_codigo UNIQUE (codigo),
    CONSTRAINT chk_equipo_codigo CHECK (btrim(codigo) <> ''),
    CONSTRAINT chk_equipo_nombre CHECK (btrim(nombre) <> ''),
    CONSTRAINT chk_cantidad_equipos CHECK (cantidad >= 0),
    CONSTRAINT chk_estado_equipo CHECK (estado IN ('Disponible', 'En mantenimiento', 'Fuera de servicio')),
    CONSTRAINT fk_equipo_tipo FOREIGN KEY (id_tipo)
        REFERENCES inventario.tipo_equipo (id_tipo)
        ON UPDATE RESTRICT ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_equipo_id_tipo ON inventario.equipo (id_tipo);

INSERT INTO inventario.tipo_equipo (nombre, descripcion) VALUES
    ('Computadoras', 'Computadoras de escritorio y portatiles'),
    ('Proyectores', 'Equipos de proyeccion para clases'),
    ('Perifericos', 'Teclados, ratones, monitores y otros accesorios')
ON CONFLICT (nombre) DO NOTHING;

INSERT INTO inventario.equipo (codigo, nombre, marca, estado, cantidad, id_tipo)
SELECT datos.codigo, datos.nombre, datos.marca, datos.estado, datos.cantidad, tipo.id_tipo
FROM (VALUES
    ('PC-001', 'Computadora de escritorio OptiPlex', 'Dell', 'Disponible', 12, 'Computadoras'),
    ('LAP-002', 'Computadora portatil ThinkPad', 'Lenovo', 'En mantenimiento', 2, 'Computadoras'),
    ('PC-003', 'Computadora de escritorio ProDesk', 'HP', 'Fuera de servicio', 1, 'Computadoras'),
    ('PRO-004', 'Proyector PowerLite', 'Epson', 'Disponible', 4, 'Proyectores'),
    ('PRO-005', 'Proyector de aula PA503', 'ViewSonic', 'En mantenimiento', 1, 'Proyectores'),
    ('TEC-006', 'Teclado USB K120', 'Logitech', 'Disponible', 20, 'Perifericos'),
    ('MON-007', 'Monitor de laboratorio 24 pulgadas', 'Samsung', 'Disponible', 8, 'Perifericos'),
    ('MOU-008', 'Raton optico USB', 'Genius', 'Fuera de servicio', 0, 'Perifericos')
) AS datos(codigo, nombre, marca, estado, cantidad, tipo_nombre)
JOIN inventario.tipo_equipo tipo ON tipo.nombre = datos.tipo_nombre
ON CONFLICT (codigo) DO NOTHING;

COMMIT;

-- Verificacion inicial: 3 tipos, 8 equipos, tres estados diferentes.
SELECT e.id_equipo, e.codigo, e.nombre, e.marca, e.estado, e.cantidad, t.nombre AS tipo
FROM inventario.equipo e
JOIN inventario.tipo_equipo t ON t.id_tipo = e.id_tipo
ORDER BY e.id_equipo;

-- Consultas de solo lectura; ejecutar dentro de bd_laboratorio_orm.
SELECT current_database() AS base_actual;
SELECT COUNT(*) AS tipos FROM inventario.tipo_equipo;
SELECT COUNT(*) AS equipos FROM inventario.equipo;
SELECT estado, COUNT(*) AS registros FROM inventario.equipo GROUP BY estado ORDER BY estado;
SELECT e.codigo, e.nombre, e.estado, e.cantidad, t.nombre AS tipo
FROM inventario.equipo e JOIN inventario.tipo_equipo t ON t.id_tipo = e.id_tipo
ORDER BY e.id_equipo;
-- P5: busqueda parcial sin distinguir mayusculas.
SELECT codigo, nombre FROM inventario.equipo WHERE lower(nombre) LIKE '%computadora%';
-- P6: ambas condiciones simultaneas.
SELECT e.codigo, e.nombre, t.nombre AS tipo
FROM inventario.equipo e JOIN inventario.tipo_equipo t ON t.id_tipo = e.id_tipo
WHERE lower(e.nombre) LIKE '%computadora%' AND t.nombre = 'Computadoras';

-- FIN_ESQUEMA
