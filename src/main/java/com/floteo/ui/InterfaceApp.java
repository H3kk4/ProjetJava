package com.floteo.ui;

import com.floteo.db.Db;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.sql.Connection;

public class InterfaceApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // 1) Connexion DB
        Connection conn = Db.connect();

        // 1 bis chargement de la police d'écriture
        Font.loadFont(
                getClass().getResourceAsStream("/fonts/gloucester-mt-extra-condensed.ttf"),
                12
        );
        Font.loadFont(
                getClass().getResourceAsStream("/fonts/Britannic Bold Regular.ttf"),
                12
        );
        Font.loadFont(
                getClass().getResourceAsStream("/fonts/Gill Sans Ultra Bold Condensed Regular.ttf"),
                12
        );


        // 2) Charger le FXML controller
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/floteo/ui/Interface.fxml"));
        Pane root = loader.load();

        // 3) Injecter la connexion au controller
        InterfaceController controller = loader.getController();
        controller.init(conn);

        // 4) Lancer la fenêtre
        Scene scene = new Scene(root, 978, 716);  // taille de ton FXML
        stage.setScene(scene);
        stage.setTitle("Floteo");

        // empêche que l'utilisateur réduise trop (sinon tout se chevauche avec Pane)
                stage.setMinWidth(978);
                stage.setMinHeight(716);

        // optionnel : si tu veux être tranquille tant que le layout n'est pas responsive
        // stage.setResizable(false);

        stage.show();
    }

    @Override
    public void stop() throws Exception {
        // fermer la DB si en variable
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
