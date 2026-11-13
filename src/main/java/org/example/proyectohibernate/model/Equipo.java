package org.example.proyectohibernate.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "equipo", schema = "inventario")
public class Equipo {
    public static final List<String> ESTADOS = List.of("Disponible", "En mantenimiento", "Fuera de servicio");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_equipo")
    private Integer idEquipo;

    @Column(nullable = false, unique = true, length = 10)
    private String codigo;
    @Column(nullable = false, length = 100)
    private String nombre;
    @Column(length = 100)
    private String marca;
    @Column(nullable = false, length = 30)
    private String estado;
    @Column(nullable = false)
    private Integer cantidad;

    // El equipo es propietario de la FK. No hay cascade REMOVE hacia el tipo.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_tipo", nullable = false)
    private TipoEquipo tipoEquipo;

    public Equipo() { }
    public Integer getIdEquipo() { return idEquipo; }
    public void setIdEquipo(Integer idEquipo) { this.idEquipo = idEquipo; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    public TipoEquipo getTipoEquipo() { return tipoEquipo; }
    public void setTipoEquipo(TipoEquipo tipoEquipo) { this.tipoEquipo = tipoEquipo; }
}
