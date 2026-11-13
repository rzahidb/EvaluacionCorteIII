package org.example.proyectohibernate.controller;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import jakarta.persistence.*;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.example.proyectohibernate.HelloApplication;
import org.example.proyectohibernate.dao.*;
import org.example.proyectohibernate.database.UnidadTrabajo;
import org.example.proyectohibernate.model.*;
import org.junit.jupiter.api.*;
import java.nio.file.*;
import java.sql.Connection;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

/** Pruebas reales con PostgreSQL temporal: no usan ni modifican la BD del usuario. */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LaboratorioIntegracionTest {
    private static EmbeddedPostgres postgres;
    private static EntityManagerFactory fabrica;
    private static Map<String, Object> propiedades;
    private static UnidadTrabajo trabajo;
    private static EquipoDAO equipoDAO;
    private static TipoEquipoDAO tipoDAO;
    private static Parent vista;
    private static Stage ventana;
    private static ProbeController controller;
    private static Integer idCreado;

    static class ProbeController extends EquipoController {
        Alert.AlertType ultimoTipo;
        String ultimoMensaje;
        ProbeController() { super(equipoDAO, tipoDAO); }
        @Override protected void mostrarAlerta(Alert.AlertType tipo, String mensaje) {
            ultimoTipo = tipo; ultimoMensaje = mensaje;
        }
    }

    @BeforeAll
    static void preparar() throws Exception {
        postgres = EmbeddedPostgres.builder().setServerConfig("unix_socket_directories", "").start();
        // JDBC se usa SOLO para preparar el servidor de pruebas y aplicar el DDL entregado.
        // Todas las operaciones probadas del inventario usan los DAO Hibernate de produccion.
        try (Connection c = postgres.getPostgresDatabase().getConnection()) {
            c.createStatement().execute("CREATE DATABASE bd_laboratorio_orm");
        }
        try (Connection c = postgres.getDatabase("postgres", "bd_laboratorio_orm").getConnection()) {
            String script = Files.readString(Path.of("Script/ScriptEvaluacionZahid-III.sql"));
            String ddl = script.split("-- INICIO_ESQUEMA", 2)[1].split("-- FIN_ESQUEMA", 2)[0];
            c.createStatement().execute(ddl);
            c.createStatement().execute(ddl); // Reejecucion sin duplicar ni borrar datos.
        }
        propiedades = new HashMap<>();
        propiedades.put("jakarta.persistence.jdbc.url", postgres.getJdbcUrl("postgres", "bd_laboratorio_orm"));
        propiedades.put("jakarta.persistence.jdbc.user", "postgres");
        propiedades.put("jakarta.persistence.jdbc.password", "solo_pruebas_temporales");
        propiedades.put("hibernate.show_sql", "false");
        fabrica = Persistence.createEntityManagerFactory("laboratorioPU", propiedades);
        trabajo = new UnidadTrabajo(() -> fabrica.createEntityManager());
        equipoDAO = new EquipoDAO(trabajo);
        tipoDAO = new TipoEquipoDAO(trabajo);
        CountDownLatch iniciado = new CountDownLatch(1);
        Platform.startup(iniciado::countDown);
        assertTrue(iniciado.await(15, TimeUnit.SECONDS));
        Platform.setImplicitExit(false);
        fx(() -> {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("equipos-view.fxml"));
            loader.setControllerFactory(clase -> new ProbeController());
            vista = loader.load(); controller = loader.getController();
            ventana = new Stage();
            Scene scene = new Scene(vista, 1120, 740);
            scene.getStylesheets().add(HelloApplication.class.getResource("estilos.css").toExternalForm());
            ventana.setScene(scene); ventana.show();
            vista.applyCss(); vista.layout(); controller.cargarDatos();
            return null;
        });
    }

    @AfterAll
    static void cerrar() throws Exception {
        if (ventana != null) fx(() -> { ventana.close(); return null; });
        if (fabrica != null && fabrica.isOpen()) fabrica.close();
        if (postgres != null) postgres.close();
        if (ventana != null) Platform.exit();
    }

    @Test @Order(0)
    void esquemaInicialYFXML() throws Exception {
        assertEquals(3, tipoDAO.listar().size());
        assertEquals(8, equipoDAO.listar().size());
        assertEquals(3, equipoDAO.listar().stream().map(Equipo::getEstado).distinct().count());
        fx(() -> {
            assertEquals(8, tabla().getItems().size());
            assertTrue(boton("btnActualizar").isDisabled());
            assertTrue(boton("btnEliminar").isDisabled());
            // Una instantanea de la vista real, sin datos personales.
            var instantanea = vista.snapshot(null, null);
            Files.createDirectories(Path.of("target/evidencias"));
            // PNG sin depender de javafx-swing: codificacion RGBA minima por ImageIO de AWT.
            int ancho = (int) instantanea.getWidth(), alto = (int) instantanea.getHeight();
            var png = new java.awt.image.BufferedImage(ancho, alto, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            for (int y = 0; y < alto; y++) for (int x = 0; x < ancho; x++)
                png.setRGB(x, y, instantanea.getPixelReader().getArgb(x, y));
            javax.imageio.ImageIO.write(png, "png", Path.of("target/evidencias/interfaz.png").toFile());
            return null;
        });
    }

    @Test @Order(1)
    void p1RegistrarEquipoValido() throws Exception {
        fx(() -> {
            formulario("PRU-009", "Equipo de prueba", "Disponible", "3", "Computadoras");
            boton("btnGuardar").fire();
            assertEquals(Alert.AlertType.INFORMATION, controller.ultimoTipo);
            assertEquals(9, tabla().getItems().size());
            return null;
        });
        Equipo guardado = equipoDAO.buscar("Equipo de prueba", null).getFirst();
        idCreado = guardado.getIdEquipo();
        assertNotNull(idCreado);
        assertEquals("Computadoras", guardado.getTipoEquipo().getNombre());
    }

    @Test @Order(2)
    void p2CodigoDuplicadoYRecuperacion() throws Exception {
        fx(() -> {
            formulario("PC-001", "Intento duplicado", "Disponible", "2", "Computadoras");
            boton("btnGuardar").fire();
            assertEquals(Alert.AlertType.ERROR, controller.ultimoTipo);
            assertTrue(controller.ultimoMensaje.contains("código"));
            assertEquals(9, tabla().getItems().size());
            boton("btnLimpiar").fire();
            return null;
        });
        assertEquals(9, equipoDAO.listar().size());
        assertTrue(equipoDAO.buscar("Intento duplicado", null).isEmpty());
    }

    @Test @Order(3)
    void p3SeleccionYActualizacion() throws Exception {
        fx(() -> {
            seleccionar(idCreado);
            assertEquals("PRU-009", texto("txtCodigo").getText());
            assertFalse(boton("btnActualizar").isDisabled());
            texto("txtNombre").setText("Equipo de prueba actualizado");
            estados().setValue("En mantenimiento");
            tipos("comboTipo").setValue(tipo("Perifericos"));
            boton("btnActualizar").fire();
            assertEquals(Alert.AlertType.INFORMATION, controller.ultimoTipo);
            return null;
        });
        Equipo actualizado = equipoDAO.buscar("Equipo de prueba actualizado", null).getFirst();
        assertEquals("En mantenimiento", actualizado.getEstado());
        assertEquals("Perifericos", actualizado.getTipoEquipo().getNombre());
    }

    @Test @Order(4)
    void p4EliminarConConfirmacionReal() throws Exception {
        fx(() -> {
            seleccionar(idCreado);
            responderConfirmacion(ButtonType.NO);
            boton("btnEliminar").fire();
            assertEquals(9, tabla().getItems().size());
            responderConfirmacion(ButtonType.YES);
            boton("btnEliminar").fire();
            assertEquals(Alert.AlertType.INFORMATION, controller.ultimoTipo);
            assertEquals(8, tabla().getItems().size());
            return null;
        });
        assertTrue(equipoDAO.buscar("Equipo de prueba", null).isEmpty());
    }

    @Test @Order(5)
    void p5BusquedaParcial() throws Exception {
        fx(() -> {
            texto("txtBuscar").setText("COMPUTADORA"); boton("btnBuscar").fire();
            assertEquals(3, tabla().getItems().size());
            assertTrue(tabla().getItems().stream().allMatch(e -> e.getNombre().toLowerCase(Locale.ROOT).contains("computadora")));
            return null;
        });
    }

    @Test @Order(6)
    void p6FiltrosCombinados() throws Exception {
        fx(() -> {
            tipos("comboFiltroTipo").setValue(tipo("Proyectores")); boton("btnBuscar").fire();
            assertTrue(tabla().getItems().isEmpty());
            texto("txtBuscar").setText("proyector"); boton("btnBuscar").fire();
            assertEquals(2, tabla().getItems().size());
            assertTrue(tabla().getItems().stream().allMatch(e -> e.getTipoEquipo().getNombre().equals("Proyectores")));
            boton("btnQuitarFiltros").fire();
            assertEquals(8, tabla().getItems().size());
            return null;
        });
        assertTrue(equipoDAO.buscar("%", null).isEmpty());
        assertTrue(equipoDAO.buscar("' OR 1=1 --", null).isEmpty());
    }

    @Test @Order(7)
    void p7CantidadNegativaYNoNumerica() throws Exception {
        fx(() -> {
            formulario("NEG-001", "Equipo invalido", "Disponible", "-1", "Computadoras");
            boton("btnGuardar").fire();
            assertEquals(Alert.AlertType.WARNING, controller.ultimoTipo);
            assertTrue(controller.ultimoMensaje.contains("negativa"));
            texto("txtCantidad").setText("abc"); boton("btnGuardar").fire();
            assertEquals(Alert.AlertType.WARNING, controller.ultimoTipo);
            boton("btnLimpiar").fire();
            return null;
        });
        assertEquals(8, equipoDAO.listar().size());
    }

    @Test @Order(8)
    void p8ReabrirPersistenciaYVista() throws Exception {
        Equipo nuevo = equipo("REI-001", "Persistencia al reiniciar", tipoDAO.listar().getFirst());
        equipoDAO.guardar(nuevo);
        fabrica.close();
        // Otro proceso Java arranca JPAutil desde cero y consulta el registro confirmado.
        String classpath = System.getProperty("surefire.test.class.path", System.getProperty("java.class.path"));
        String ejecutable = System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("win") ? "java.exe" : "java";
        ProcessBuilder proceso = new ProcessBuilder(Path.of(System.getProperty("java.home"), "bin", ejecutable).toString(),
                "-cp", classpath, "org.example.proyectohibernate.controller.VerificarReinicio", "REI-001");
        proceso.environment().put("DB_URL", propiedades.get("jakarta.persistence.jdbc.url").toString());
        proceso.environment().put("DB_USER", "postgres");
        proceso.environment().put("DB_PASSWORD", "solo_pruebas_temporales");
        proceso.redirectErrorStream(true);
        Process reinicio = proceso.start();
        boolean termino = reinicio.waitFor(30, TimeUnit.SECONDS);
        if (!termino) reinicio.destroyForcibly();
        assertTrue(termino, "El proceso de reinicio no termino");
        String salida = new String(reinicio.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        assertEquals(0, reinicio.exitValue(), salida);
        assertTrue(salida.contains("PERSISTENCIA_VERIFICADA"), salida);
        fabrica = Persistence.createEntityManagerFactory("laboratorioPU", propiedades);
        // Nueva fabrica, nuevos EntityManager y nuevo FXMLLoader/Controller.
        fx(() -> {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("equipos-view.fxml"));
            loader.setControllerFactory(clase -> new ProbeController());
            Parent otraVista = loader.load();
            ProbeController otro = loader.getController(); otro.cargarDatos();
            @SuppressWarnings("unchecked") TableView<Equipo> tablaNueva = (TableView<Equipo>) otraVista.lookup("#tablaEquipos");
            assertTrue(tablaNueva.getItems().stream().anyMatch(e -> "REI-001".equals(e.getCodigo())));
            return null;
        });
        equipoDAO.eliminar(nuevo.getIdEquipo());
    }

    @Test @Order(9)
    void rollbackSinCambiosParciales() {
        Integer id = equipoDAO.listar().getFirst().getIdEquipo();
        String nombreAntes = equipoDAO.listar().getFirst().getNombre();
        assertThrows(RuntimeException.class, () -> trabajo.escribir(em -> {
            Equipo existente = em.find(Equipo.class, id);
            existente.setNombre("CAMBIO QUE DEBE REVERTIRSE");
            em.flush(); // La primera escritura llega a PostgreSQL dentro de la transaccion.
            Equipo duplicado = equipo("PC-001", "Forzar UNIQUE", em.find(TipoEquipo.class, existente.getTipoEquipo().getIdTipo()));
            em.persist(duplicado); // Segunda escritura falla: se revierte tambien la primera.
            em.flush();
        }));
        assertEquals(nombreAntes, equipoDAO.listar().getFirst().getNombre());
        assertEquals(8, equipoDAO.listar().size());
        assertTrue(equipoDAO.buscar("Forzar UNIQUE", null).isEmpty());
    }

    @Test @Order(10)
    void restriccionesPostgresYPoliticaDeTipos() {
        TipoEquipo tipo = tipoDAO.listar().getFirst();
        for (String caso : List.of("cantidad", "estado", "tipo")) {
            assertThrows(RuntimeException.class, () -> trabajo.escribir(em -> {
                Equipo invalido = equipo("INV-001", "Forzar CHECK", em.find(TipoEquipo.class, tipo.getIdTipo()));
                if (caso.equals("cantidad")) invalido.setCantidad(-1);
                if (caso.equals("estado")) invalido.setEstado("Prestado");
                if (caso.equals("tipo")) invalido.setTipoEquipo(em.getReference(TipoEquipo.class, Integer.MAX_VALUE));
                em.persist(invalido); em.flush();
            }));
        }
        assertThrows(RuntimeException.class, () -> trabajo.escribir(em -> {
            em.remove(em.find(TipoEquipo.class, tipo.getIdTipo())); em.flush();
        }));
        assertEquals(8, equipoDAO.listar().size());
        assertEquals(3, tipoDAO.listar().size());
    }

    private static Equipo equipo(String codigo, String nombre, TipoEquipo tipo) {
        Equipo e = new Equipo(); e.setCodigo(codigo); e.setNombre(nombre);
        e.setEstado("Disponible"); e.setCantidad(1); e.setTipoEquipo(tipo); return e;
    }

    private static void responderConfirmacion(ButtonType respuesta) {
        Platform.runLater(() -> {
            for (Window w : List.copyOf(Window.getWindows())) {
                if (w.getScene() != null && w.getScene().getRoot() instanceof DialogPane dialogo) {
                    ((Button) dialogo.lookupButton(respuesta)).fire(); return;
                }
            }
            throw new AssertionError("No se abrio el dialogo de confirmacion");
        });
    }

    private static void formulario(String codigo, String nombre, String estado, String cantidad, String tipo) {
        boton("btnLimpiar").fire();
        texto("txtCodigo").setText(codigo); texto("txtNombre").setText(nombre); texto("txtMarca").setText("Pruebas");
        texto("txtCantidad").setText(cantidad); estados().setValue(estado); tipos("comboTipo").setValue(tipo(tipo));
    }
    private static TipoEquipo tipo(String nombre) { return tipoDAO.listar().stream().filter(t -> t.getNombre().equals(nombre)).findFirst().orElseThrow(); }
    private static void seleccionar(Integer id) { tabla().getSelectionModel().select(tabla().getItems().stream().filter(e -> id.equals(e.getIdEquipo())).findFirst().orElseThrow()); }
    private static Button boton(String id) { return (Button) vista.lookup("#" + id); }
    private static TextField texto(String id) { return (TextField) vista.lookup("#" + id); }
    @SuppressWarnings("unchecked") private static ComboBox<TipoEquipo> tipos(String id) { return (ComboBox<TipoEquipo>) vista.lookup("#" + id); }
    @SuppressWarnings("unchecked") private static ComboBox<String> estados() { return (ComboBox<String>) vista.lookup("#comboEstado"); }
    @SuppressWarnings("unchecked") private static TableView<Equipo> tabla() { return (TableView<Equipo>) vista.lookup("#tablaEquipos"); }
    private static <T> T fx(Callable<T> tarea) throws Exception {
        FutureTask<T> futuro = new FutureTask<>(tarea); Platform.runLater(futuro); return futuro.get(25, TimeUnit.SECONDS);
    }
}
