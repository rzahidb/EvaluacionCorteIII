package org.example.proyectohibernate;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.proyectohibernate.controller.EquipoController;
import org.example.proyectohibernate.database.JPAutil;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("equipos-view.fxml"));
        Scene scene = new Scene(loader.load(), 1120, 740);
        scene.getStylesheets().add(HelloApplication.class.getResource("estilos.css").toExternalForm());
        stage.setTitle("Laboratorio ORM | Control de equipos");
        stage.setMinWidth(960);
        stage.setMinHeight(680);
        stage.setScene(scene);
        stage.show();
        // El Controller captura los fallos y mantiene la ventana abierta para reintentar.
        EquipoController controller = loader.getController();
        controller.cargarDatos();
    }

    @Override
    public void stop() {
        JPAutil.cerrar();
    }

    public static void main(String[] args) { launch(args); }
}
