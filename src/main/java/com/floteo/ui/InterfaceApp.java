package com.floteo.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import static javafx.application.Application.launch;

public class InterfaceApp extends Application {
    public void start(Stage stage) {
        Pane root = new Pane();
        try {
            root = FXMLLoader.load(getClass().getResource("/com/floteo/ui/Interface.fxml"));

        } catch(Exception e) {
            System.out.println(e.getMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Erreur chargement du document FXML");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
            System.exit(1);
        }

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Interface Application");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}
