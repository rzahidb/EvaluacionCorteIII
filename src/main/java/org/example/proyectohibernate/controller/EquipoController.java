package org.example.proyectohibernate.controller;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.proyectohibernate.dao.EquipoDAO;
import org.example.proyectohibernate.dao.TipoEquipoDAO;
import org.example.proyectohibernate.model.Equipo;
import org.example.proyectohibernate.model.TipoEquipo;
import org.example.proyectohibernate.model.ValidacionEquipo;
import java.util.List;

public class EquipoController {
    @FXML private TextField txtCodigo, txtNombre, txtMarca, txtCantidad, txtBuscar;
    @FXML private ComboBox<TipoEquipo> comboTipo, comboFiltroTipo;
    @FXML private ComboBox<String> comboEstado;
    @FXML private TableView<Equipo> tablaEquipos;
    @FXML private TableColumn<Equipo, Integer> colId, colCantidad;
    @FXML private TableColumn<Equipo, String> colCodigo, colNombre, colMarca, colEstado, colTipo;
    @FXML private Button btnGuardar, btnActualizar, btnEliminar;
    @FXML private Label lblSeleccion, lblTotal;

    private final EquipoDAO equipoDAO = new EquipoDAO();
    private final TipoEquipoDAO tipoDAO = new TipoEquipoDAO();
    private Integer idSeleccionado;

    @FXML
    private void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idEquipo"));
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colMarca.setCellValueFactory(new PropertyValueFactory<>("marca"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        // JOIN FETCH en el DAO permite leer el nombre tras cerrar el EntityManager.
        colTipo.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getTipoEquipo().getNombre()));
        tablaEquipos.setPlaceholder(new Label("No hay equipos para los filtros seleccionados."));
        comboEstado.setItems(FXCollections.observableArrayList(Equipo.ESTADOS));
        tablaEquipos.getSelectionModel().selectedItemProperty().addListener((obs, anterior, actual) -> {
            if (actual == null) {
                idSeleccionado = null;
                actualizarBotones();
            } else seleccionarEquipo(actual);
        });
        limpiarCampos();
    }

    /** Tambien sirve para reintentar despues de corregir la conexion local. */
    @FXML
    public void cargarDatos() {
        try {
            Integer idTipo = comboTipo.getValue() == null ? null : comboTipo.getValue().getIdTipo();
            Integer idFiltro = comboFiltroTipo.getValue() == null ? null : comboFiltroTipo.getValue().getIdTipo();
            List<TipoEquipo> tipos = tipoDAO.listar();
            comboTipo.setItems(FXCollections.observableArrayList(tipos));
            comboTipo.setValue(encontrarTipo(tipos, idTipo));
            comboFiltroTipo.setItems(FXCollections.observableArrayList(tipos));
            TipoEquipo todos = new TipoEquipo();
            todos.setNombre("Todos los tipos");
            comboFiltroTipo.getItems().add(0, todos);
            comboFiltroTipo.setValue(idFiltro == null ? todos : encontrarTipo(tipos, idFiltro));
            refrescarTabla();
        } catch (RuntimeException ex) { fallo("No se pudieron cargar los datos", ex); }
    }

    @FXML
    private void aplicarFiltros() {
        try { refrescarTabla(); }
        catch (RuntimeException ex) { fallo("No se pudo consultar el inventario", ex); }
    }

    private void refrescarTabla() {
        TipoEquipo tipo = comboFiltroTipo.getValue();
        List<Equipo> equipos = equipoDAO.buscar(txtBuscar.getText(), tipo == null ? null : tipo.getIdTipo());
        // Una consulta fallida no sustituye la tabla por datos inventados.
        tablaEquipos.setItems(FXCollections.observableArrayList(equipos));
        limpiarCampos();
        lblTotal.setText(equipos.size() + (equipos.size() == 1 ? " equipo encontrado" : " equipos encontrados"));
    }

    @FXML
    private void quitarFiltros() {
        txtBuscar.clear();
        comboFiltroTipo.setValue(null);
        aplicarFiltros();
    }

    @FXML
    private void guardarEquipo() {
        try {
            Equipo equipo = obtenerFormulario();
            equipoDAO.guardar(equipo);
        } catch (RuntimeException ex) { fallo("No se pudo guardar", ex); return; }
        despuesDeEscribir("Equipo guardado correctamente.");
    }

    @FXML
    private void actualizarEquipo() {
        if (idSeleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Seleccione un equipo de la tabla para actualizar.");
            return;
        }
        try {
            Equipo equipo = obtenerFormulario();
            equipo.setIdEquipo(idSeleccionado);
            equipoDAO.actualizar(equipo);
        } catch (RuntimeException ex) { fallo("No se pudo actualizar", ex); return; }
        despuesDeEscribir("Equipo actualizado correctamente.");
    }

    @FXML
    private void eliminarEquipo() {
        if (idSeleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Seleccione un equipo de la tabla para eliminar.");
            return;
        }
        if (!confirmarEliminacion(txtCodigo.getText())) return;
        try { equipoDAO.eliminar(idSeleccionado); }
        catch (RuntimeException ex) { fallo("No se pudo eliminar", ex); return; }
        despuesDeEscribir("Equipo eliminado correctamente.");
    }

    private void despuesDeEscribir(String mensaje) {
        limpiarCampos();
        try {
            refrescarTabla();
            mostrarAlerta(Alert.AlertType.INFORMATION, mensaje);
        } catch (RuntimeException ex) {
            // El commit ya sucedio; distinguimos una falla posterior de lectura.
            tablaEquipos.getItems().clear();
            lblTotal.setText("Recargue el inventario");
            mostrarAlerta(Alert.AlertType.WARNING, mensaje + " No fue posible recargar la tabla: " + MensajesError.describir(ex));
        }
    }

    private Equipo obtenerFormulario() {
        Equipo equipo = new Equipo();
        equipo.setCodigo(txtCodigo.getText().strip());
        equipo.setNombre(txtNombre.getText().strip());
        String marca = txtMarca.getText().strip();
        equipo.setMarca(marca.isEmpty() ? null : marca);
        equipo.setCantidad(ValidacionEquipo.cantidad(txtCantidad.getText()));
        equipo.setEstado(comboEstado.getValue());
        equipo.setTipoEquipo(comboTipo.getValue());
        ValidacionEquipo.validar(equipo);
        return equipo;
    }

    private void seleccionarEquipo(Equipo equipo) {
        idSeleccionado = equipo.getIdEquipo();
        txtCodigo.setText(equipo.getCodigo());
        txtNombre.setText(equipo.getNombre());
        txtMarca.setText(equipo.getMarca() == null ? "" : equipo.getMarca());
        txtCantidad.setText(equipo.getCantidad().toString());
        comboEstado.setValue(equipo.getEstado());
        comboTipo.setValue(encontrarTipo(comboTipo.getItems(), equipo.getTipoEquipo().getIdTipo()));
        actualizarBotones();
    }

    private TipoEquipo encontrarTipo(List<TipoEquipo> tipos, Integer id) {
        if (id == null) return null;
        return tipos.stream().filter(t -> id.equals(t.getIdTipo())).findFirst().orElse(null);
    }

    @FXML
    private void limpiarCampos() {
        tablaEquipos.getSelectionModel().clearSelection();
        idSeleccionado = null;
        txtCodigo.clear(); txtNombre.clear(); txtMarca.clear(); txtCantidad.clear();
        comboTipo.setValue(null);
        comboEstado.setValue("Disponible");
        actualizarBotones();
    }

    private void actualizarBotones() {
        boolean sinSeleccion = idSeleccionado == null;
        btnGuardar.setDisable(!sinSeleccion);
        btnActualizar.setDisable(sinSeleccion);
        btnEliminar.setDisable(sinSeleccion);
        lblSeleccion.setText(sinSeleccion ? "Nuevo equipo" : "Editando equipo #" + idSeleccionado);
    }

    private void fallo(String contexto, RuntimeException error) {
        Alert.AlertType tipo = error instanceof IllegalArgumentException ? Alert.AlertType.WARNING : Alert.AlertType.ERROR;
        mostrarAlerta(tipo, contexto + ": " + MensajesError.describir(error));
    }

    protected boolean confirmarEliminacion(String codigo) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Desea eliminar el equipo " + codigo + "? Esta acción retirará el registro del inventario.",
                ButtonType.YES, ButtonType.NO);
        alerta.setTitle("Confirmar eliminación");
        alerta.setHeaderText(null);
        if (tablaEquipos.getScene() != null) alerta.initOwner(tablaEquipos.getScene().getWindow());
        return alerta.showAndWait().orElse(ButtonType.NO) == ButtonType.YES;
    }

    protected void mostrarAlerta(Alert.AlertType tipo, String mensaje) {
        Alert alerta = new Alert(tipo, mensaje, ButtonType.OK);
        alerta.setTitle("Control de equipos");
        alerta.setHeaderText(null);
        if (tablaEquipos.getScene() != null) alerta.initOwner(tablaEquipos.getScene().getWindow());
        alerta.showAndWait();
    }
}
