package org.example.proyectohibernate.model;

import jakarta.persistence.*;

@Entity
@Table(name = "tipo_equipo", schema = "inventario")
public class TipoEquipo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo")
    private Integer idTipo;

    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(length = 80)
    private String descripcion;

    public TipoEquipo() { }
    public Integer getIdTipo() { return idTipo; }
    public void setIdTipo(Integer idTipo) { this.idTipo = idTipo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    @Override public String toString() { return nombre; }
}
