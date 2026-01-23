package com.floteo.ui;

import com.floteo.db.Db;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.sql.Connection;

/**
 * Point d'entrée de l'application JavaFX.
 * Initialise la base de données, charge l'interface et lance la fenêtre principale.
 */
public class InterfaceApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // 1) Connexion à la base de données
        Connection conn = Db.connect();

        // 1 bis) Chargement des polices personnalisées utilisées par l'interface
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

        // 2) Chargement du fichier FXML et création de la vue
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/floteo/ui/Interface.fxml"));
        Pane root = loader.load();

        // 3) Récupération du contrôleur et injection de la connexion DB
        InterfaceController controller = loader.getController();
        controller.init(conn);

        // 4) Création et affichage de la fenêtre principale
        Scene scene = new Scene(root, 978, 716);
        stage.setScene(scene);
        stage.setTitle("Floteo");
        stage.setMinWidth(978);
        stage.setMinHeight(716);

        stage.show();
    }

    @Override
    public void stop() throws Exception {
        // Méthode appelée à la fermeture de l'application
        super.stop();
    }

    /**
     * Méthode principale : lance l'application JavaFX.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
