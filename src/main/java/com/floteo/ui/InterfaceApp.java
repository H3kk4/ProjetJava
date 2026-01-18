package com.floteo.ui;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import static javafx.application.Application.launch;

public class InterfaceApp extends Application {
    // Déclaration des variables composants de FXML
    // PANE ACCUEIL
    @FXML
    public Pane paneAccueil;
        @FXML
        public Button btnBack;
        @FXML
        public Button btnHome;
        @FXML
        public Button btnAffectationAgents;
        @FXML
        public Button btnGestionVehicules;

    // PANE GESTION VEHICULES
    @FXML
    public Pane paneGestionVehicules;
        // PANE DASHBOARD VEHICULES
        @FXML
        public Pane paneDashboardVehicules;
            @FXML
            public Label lblDate;
            @FXML
            public Label lblDisponibles;
            @FXML
            public Label lblIndisponibles;
            @FXML
            public Label lblReserves;
            @FXML
            public Button btnCalendrier;
            @FXML
            public Button btnVehicules;
        // PANE LISTE VEHICULES
        @FXML
        public Pane paneListeVehicules;
            @FXML
            public TableView tabVehicules;
            @FXML
            public TableColumn colVehicule;
            @FXML
            public TableColumn colImmat;
            @FXML
            public TableColumn colStatus;
            @FXML
            public TextField tfMarque;
            @FXML
            public TextField tfModele;
            @FXML
            public TextField tfImmat;
            @FXML
            public TextField tfKilometrage;
            @FXML
            public ComboBox comboType;
            @FXML
            public ComboBox comboStatus;
            @FXML
            public ComboBox comboEtat;
            @FXML
            public Button btnAffecterAgent;
            @FXML
            public Button btnRendreIndisponible;
            @FXML
            public Button btnRendreDisponible;
            @FXML
            public Button btnModifierVehicule;
            @FXML
            public Button btnSupprimerVehicule;
            @FXML
            public Button btnSauvegarderVehicule;
            @FXML
            public Button btnAjouterVehicule;
    // PANE GESTION AGENTS
    @FXML
    public Pane paneGestionAgents;
        // PANE MENU GESTION AGENTS
        @FXML
        public Pane paneMenuGestionAgents;
            @FXML
            public Button btnListeAgents;
            @FXML
            public Button btnVoirAffectations;
            @FXML
            public Button btnAffecterAgents;
            @FXML
            public Pane paneListeAgents;
            @FXML
            public TableView tabAgents;
            @FXML
            public TableColumn colPrenomAgent;
            @FXML
            public TableColumn colNomAgent;
            @FXML
            public TableColumn colMatAgent;
            @FXML
            public TextField tfNomAgent;
            @FXML
            public TextField tPrenomAgent;
            @FXML
            public TextField tfMatAgent;
            @FXML
            public ComboBox comboServiceAgent;
            @FXML
            public Button btnAffecterVehicule;
            @FXML
            public Button btnRetirerAffectation;
            @FXML
            public Button btnModifierAgent;
            @FXML
            public Button btnSupprimerAgent;
            @FXML
            public Button btnSauvegarderAgent;
            @FXML
            public Button btnAjouterAgent;
        // PANE LISTE AFFECTATIONS
        @FXML
        public Pane paneListeAffectations;
            @FXML
            public TableView tabListeAffectations;
            @FXML
            public TableColumn colLADateDemande;
            @FXML
            public TableColumn colLAAgent;
            @FXML
            public TableColumn colLAVehicule;
            @FXML
            public TableColumn colLAImmat;
            @FXML
            public TableColumn colLADateDepart;
            @FXML
            public TableColumn colLADateRetour;
            @FXML
            public TableColumn colLAStatus;
            @FXML
            public Button btnAccepter;
            @FXML
            public Button btnRefuser;
            @FXML
            public Button btnVoirAgent;
            @FXML
            public Button btnVoirVehicule;
            @FXML
            public TextField tfChercherVehicule;
            @FXML
            public Button btnGoVehicule;
        // PANE AFFECTATION AGENT
        @FXML
        public Pane paneAffectationAgent;
            @FXML
            public TableView tabAListeAgents;
            @FXML
            public TableColumn colANomAgent;
            @FXML
            public TableColumn colAPrenomAgent;
            @FXML
            public TableColumn colAMatAgent;
            @FXML
            public TableColumn colAServiceAgent;
            @FXML
            public TextField tfRechercherAgent;
            @FXML
            public Button btnGoAgents;
            @FXML
            public TableView tabAListeVehicules;
            @FXML
            public TableColumn colAMarque;
            @FXML
            public TableColumn colAModele;
            @FXML
            public TableColumn colAImmat;
            @FXML
            public TableColumn colAStatus;
            @FXML
            public TextField tfRechercherVehicule;
            @FXML
            public Button btnGoVehicules;
            @FXML
            public Button btnAffecter;
            @FXML
            public Button btnRetirer;

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
