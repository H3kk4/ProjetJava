package com.floteo.ui;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarView;
import com.floteo.dao.*;
import com.floteo.model.*;
import com.floteo.service.AssignmentService;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.*;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.layout.StackPane;

import java.time.LocalDate;
import java.sql.Connection;

/**
 * Contrôleur principal JavaFX (lié au fichier Interface.fxml).
 *
 * Rôles principaux :
 * - Navigation entre les différents "écrans" (Pane) de l'application.
 * - Gestion CRUD des Agents et des Véhicules.
 * - Gestion des Affectations (demandes, acceptation, clôture/refus).
 * - Affichage d'un calendrier (CalendarFX) des réservations.
 *
 * Remarque : ce contrôleur est "gros" (centralise beaucoup),
 * Nous n'avons pas eu le temps de séparer en plusieurs controlleurs... Désolé :/
 */
public class InterfaceController {

    // ----------------------------
    //     ICONES DE NAVIGATION
    // ----------------------------
    @FXML private javafx.scene.shape.SVGPath svgBack;
    @FXML private javafx.scene.shape.SVGPath svgHome;

    // ----------------------------
    //            PANES
    // (chaque Pane = un écran / sous-écran)
    // ----------------------------
    @FXML private Pane paneAccueil;
    @FXML private Pane paneGestionVehicules;
    @FXML private Pane paneDashboardVehicules;
    @FXML private Pane paneListeVehicules;
    @FXML private Pane paneGestionAgents;
    @FXML private Pane paneMenuGestionAgents;
    @FXML private Pane paneListeAgents;
    @FXML private Pane paneListeAffectations;
    @FXML private Pane paneAffectationAgent;

    // ----------------------------
    //           BOUTONS
    // ----------------------------
    @FXML private Button btnGestionVehicules;
    @FXML private Button btnAffectationAgents;
    @FXML private Button btnBack;
    @FXML private Button btnHome;
    @FXML private Button btnVehicules;

    // ----------------------------
    //    DB / DAO / SERVICES
    // ----------------------------
    /** Connexion JDBC injectée depuis InterfaceApp (une seule connexion partagée). */
    private Connection conn;

    /** DAO : accès aux tables en base. */
    private ServiceDao serviceDao;
    private AgentDao agentDao;
    private EtatDao etatDao;
    private VehicleDao vehicleDao;
    private AssignmentDao assignmentDao;

    /** Service métier : règles d'affectation (assign, end, etc.). */
    private AssignmentService assignmentService;

    // ----------------------------
    //              AGENTS
    // ----------------------------
    @FXML private TableView<Agent> tabAgents;
    @FXML private TableColumn<Agent, String> colNomAgent;
    @FXML private TableColumn<Agent, String> colPrenomAgent;
    @FXML private TableColumn<Agent, String> colMatAgent;

    // Formulaire agent
    @FXML private TextField tfNomAgent;
    @FXML private TextField tPrenomAgent;
    @FXML private TextField tfMatAgent;

    @FXML private ComboBox<Service> comboServiceAgent;

    // Boutons CRUD agent
    @FXML private Button btnModifierAgent;
    @FXML private Button btnSupprimerAgent;
    @FXML private Button btnSauvegarderAgent;
    @FXML private Button btnAjouterAgent;
    @FXML private Button btnListeAgents;
    @FXML private Button btnVoirAffectations;
    @FXML private Button btnAffecterAgents;
    @FXML private Button btnAffecterVehicule;
    @FXML private Button btnRetirerAffectation;

    // Ecran "Affectation Agent <-> Véhicule" (liste agents)
    @FXML private TableView<Agent> tabAListeAgents;
    @FXML private TableColumn<Agent, String> colANomAgent;
    @FXML private TableColumn<Agent, String> colAPrenomAgent;
    @FXML private TableColumn<Agent, String> colAMatAgent;
    @FXML private TableColumn<Agent, String> colAServiceAgent;

    @FXML private Button btnAffecter;
    @FXML private Button btnRetirer;

    // ----------------------------
    //             VEHICULES
    // ----------------------------
    @FXML private TextField tfMarque;
    @FXML private TextField tfModele;
    @FXML private TextField tfImmat;
    @FXML private TextField tfKilometrage;

    @FXML private ComboBox<String> comboType;
    @FXML private ComboBox<String> comboEtat;
    @FXML private ComboBox<VehicleStatus> comboStatus;

    // Boutons CRUD + actions rapides
    @FXML private Button btnModifierVehicule;
    @FXML private Button btnSupprimerVehicule;
    @FXML private Button btnSauvegarderVehicule;
    @FXML private Button btnAjouterVehicule;
    @FXML private Button btnAffecterAgent;
    @FXML private Button btnRendreIndisponible;
    @FXML private Button btnRendreDisponible;

    // Table véhicules (liste principale)
    @FXML private TableView<Vehicle> tabVehicules;
    @FXML private TableColumn<Vehicle, String> colVehicule;
    @FXML private TableColumn<Vehicle, String> colImmat;
    @FXML private TableColumn<Vehicle, VehicleStatus> colStatus;

    // Table véhicules (écran affectation)
    @FXML private TableView<Vehicle> tabAListeVehicules;
    @FXML private TableColumn<Vehicle, String> colAMarque;
    @FXML private TableColumn<Vehicle, String> colAModele;
    @FXML private TableColumn<Vehicle, String> colAImmat;
    @FXML private TableColumn<Vehicle, String> colAStatus;
    @FXML private TableColumn<Vehicle, String> colAEtat;

    // Dashboard véhicules
    @FXML private javafx.scene.control.Label lblDate;
    @FXML private javafx.scene.control.Label lblDisponibles;
    @FXML private javafx.scene.control.Label lblIndisponibles;
    @FXML private javafx.scene.control.Label lblReserves;

    // ----------------------------
    //      AFFECTATIONS (liste)
    // ----------------------------
    @FXML private TableView<AssignmentRow> tabListeAffectations;
    @FXML private TableColumn<AssignmentRow, String> colLADateDemande;
    @FXML private TableColumn<AssignmentRow, String> colLAAgent;
    @FXML private TableColumn<AssignmentRow, String> colLAVehicule;
    @FXML private TableColumn<AssignmentRow, String> colLAImmat;
    @FXML private TableColumn<AssignmentRow, String> colLADateDepart;
    @FXML private TableColumn<AssignmentRow, String> colLADateRetour;
    @FXML private TableColumn<AssignmentRow, String> colLAStatus;

    @FXML private Button btnAccepter;
    @FXML private Button btnCloturer;
    @FXML private Button btnVoirAgent;
    @FXML private Button btnVoirVehicule;

    // Recherche
    @FXML private TextField tfChercherVehicule;
    @FXML private Button btnGoVehicule;
    @FXML private TextField tfRechercherAgent;
    @FXML private Button btnGoAgents;
    @FXML private TextField tfRechercherVehicule;
    @FXML private Button btnGoVehicules;

    // ----------------------------
    //            CALENDRIER
    // ----------------------------
    @FXML private StackPane calendarContainer;
    @FXML private Button btnCalendrier;
    private Parent calendarRoot;

    /** Liste observable pour la TableView d'affectations (UI). */
    private final ObservableList<AssignmentRow> affectations = FXCollections.observableArrayList();

    /** Format d'affichage des dates (UI). */
    private final DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /** Liste observable des véhicules (UI). */
    private ObservableList<Vehicle> vehicles = FXCollections.observableArrayList();

    /**
     * Flags de mode : on distingue "Ajouter" et "Modifier" pour empêcher une sauvegarde ambiguë.
     */
    private boolean editMode = false;
    private boolean createMode = false;
    private boolean agentEditMode = false;
    private boolean agentCreateMode = false;

    /**
     * Cache idEtat -> nomEtat.
     * Utile pour retrouver rapidement l'id correspondant au libellé choisi en ComboBox.
     */
    private Map<Long, String> etatIdToName = new HashMap<>();

    /**
     * Appelée depuis InterfaceApp après le load().
     * Initialise les DAO / services puis configure l'écran et les événements.
     */
    public void init(Connection conn) {
        this.conn = conn;

        // DAO (accès DB)
        this.serviceDao = new ServiceDao(conn);
        this.agentDao = new AgentDao(conn);
        this.etatDao = new EtatDao(conn);
        this.vehicleDao = new VehicleDao(conn);
        this.assignmentDao = new AssignmentDao(conn);

        // Service métier (règles d'affectation)
        this.assignmentService = new AssignmentService(conn, agentDao, vehicleDao, assignmentDao);

        // écran par défaut
        showAccueil();

        // branchement des actions (boutons)
        bindActions();
    }

    /**
     * Branche les boutons vers les méthodes de navigation.
     * La navigation utilise navigateTo(...) pour gérer l'historique (retour).
     */
    private void bindActions() {
        btnGestionVehicules.setOnAction(e -> navigateTo(this::showGestionVehiculesDashboard));
        btnAffectationAgents.setOnAction(e -> navigateTo(this::showGestionAgentsMenu));
        btnHome.setOnAction(e -> goHome());
        btnBack.setOnAction(e -> goBack());
        btnListeAgents.setOnAction(e -> navigateTo(this::showListeAgents));
        btnVoirAffectations.setOnAction(e -> navigateTo(this::showListeAffectations));
        btnAffecterAgents.setOnAction(e -> navigateTo(this::showAffectationAgents));

        if (btnVehicules != null) {
            btnVehicules.setOnAction(e -> navigateTo(this::showListeVehicules));
        }
    }

    // ----------------------------
    //           NAVIGATION
    // ----------------------------

    /** Cache tous les panes avant d'afficher celui qui nous intéresse. */
    private void hideAll() {
        paneAccueil.setVisible(false);
        paneGestionVehicules.setVisible(false);
        paneDashboardVehicules.setVisible(false);
        paneListeVehicules.setVisible(false);

        paneGestionAgents.setVisible(false);
        paneMenuGestionAgents.setVisible(false);
        paneListeAgents.setVisible(false);
        paneListeAffectations.setVisible(false);
        paneAffectationAgent.setVisible(false);

        // icônes visibles par défaut
        svgBack.setVisible(true);
        svgHome.setVisible(true);
    }

    /** Affiche la page d'accueil (pas de back/home). */
    private void showAccueil() {
        hideAll();
        paneAccueil.setVisible(true);
        btnBack.setVisible(false);
        btnHome.setVisible(false);
        svgBack.setVisible(true);
        svgHome.setVisible(true);
    }

    /** Affiche le dashboard des véhicules (compteurs par statut). */
    private void showGestionVehiculesDashboard() {
        hideAll();
        paneGestionVehicules.setVisible(true);
        paneDashboardVehicules.setVisible(true);
        btnBack.setVisible(true);
        btnHome.setVisible(true);

        loadDashboardVehicules();
    }

    /** Charge et affiche les statistiques véhicules du dashboard. */
    private void loadDashboardVehicules() {
        try {
            lblDate.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

            // Comptage des véhicules par statut
            Map<VehicleStatus, Integer> counts = new EnumMap<>(VehicleStatus.class);
            for (VehicleStatus s : VehicleStatus.values()) counts.put(s, 0);

            for (Vehicle v : vehicleDao.findAll()) {
                counts.put(v.status(), counts.get(v.status()) + 1);
            }

            // Mapping UI (réservés = AFFECTE)
            lblDisponibles.setText(String.valueOf(counts.getOrDefault(VehicleStatus.DISPONIBLE, 0)));
            lblIndisponibles.setText(String.valueOf(counts.getOrDefault(VehicleStatus.ENTRETIEN, 0)));
            lblReserves.setText(String.valueOf(counts.getOrDefault(VehicleStatus.AFFECTE, 0)));

            // Initialise la liste des états (comboEtat)
            initVehiculeEtat();

        } catch (Exception e) {
            showError("Impossible de charger le dashboard: " + e.getMessage());
        }
    }

    /** Flag : on initialise l'UI véhicules une seule fois. */
    private boolean vehiclesUiInitialized = false;

    /** Affiche l'écran liste des véhicules (init 1 fois, sinon reload). */
    private void showListeVehicules() {
        hideAll();
        paneGestionVehicules.setVisible(true);
        paneListeVehicules.setVisible(true);
        btnBack.setVisible(true);
        btnHome.setVisible(true);

        if (!vehiclesUiInitialized) {
            initVehiclesUi();
            vehiclesUiInitialized = true;
        } else {
            reloadVehicles();
        }
    }

    /** Affiche l'écran "menu gestion agents". */
    private void showGestionAgentsMenu() {
        hideAll();
        paneGestionAgents.setVisible(true);
        paneMenuGestionAgents.setVisible(true);
        btnBack.setVisible(true);
        btnHome.setVisible(true);
    }

    // Historique de navigation : on stocke les vues précédentes.
    private final java.util.ArrayDeque<Runnable> navStack = new java.util.ArrayDeque<>();
    private Runnable currentView = null;

    /** Va vers une vue en enregistrant l'historique (pour revenir en arrière). */
    private void navigateTo(Runnable view) {
        if (currentView != null) navStack.push(currentView);
        currentView = view;
        view.run();
        btnBack.setVisible(!navStack.isEmpty());
        btnHome.setVisible(true);
    }

    /** Retour à la vue précédente. */
    private void goBack() {
        if (navStack.isEmpty()) {
            showAccueil();
            return;
        }
        currentView = navStack.pop();
        currentView.run();
        btnBack.setVisible(!navStack.isEmpty());
        btnHome.setVisible(true);
    }

    /** Retour accueil : reset complet de l'historique. */
    private void goHome() {
        navStack.clear();
        currentView = null;
        showAccueil();
    }

    // ----------------------------
    //             AGENTS
    // ----------------------------

    // Flags : init UI une seule fois
    private boolean agentsUiInitialized = false;
    private boolean affectationsUiInitialized = false;
    private boolean affectationAgentsUiInitialized = false;

    /** Affiche la liste agents + init au premier affichage. */
    private void showListeAgents() {
        hideAll();
        paneGestionAgents.setVisible(true);
        paneListeAgents.setVisible(true);
        btnBack.setVisible(true);
        btnHome.setVisible(true);

        if (!agentsUiInitialized) {
            initAgentsUi();
            agentsUiInitialized = true;
        } else {
            reloadAgents();
        }
    }

    /** Affiche la liste des affectations + init au premier affichage. */
    private void showListeAffectations() {
        hideAll();
        paneGestionAgents.setVisible(true);
        paneListeAffectations.setVisible(true);
        btnBack.setVisible(true);
        btnHome.setVisible(true);

        if (!affectationsUiInitialized) {
            initAffectationsUi();
            affectationsUiInitialized = true;
        } else {
            reloadAffectations();
        }
    }

    /** Affiche l'écran d'affectation Agent/Véhicule + init au premier affichage. */
    private void showAffectationAgents() {
        hideAll();
        paneGestionAgents.setVisible(true);
        paneAffectationAgent.setVisible(true);
        btnBack.setVisible(true);
        btnHome.setVisible(true);

        if (!affectationAgentsUiInitialized) {
            initAffectationAgentsUi();
            affectationAgentsUiInitialized = true;
        } else {
            reloadAffectationAgentsData();
        }
    }

    /** Données UI : listes observables. */
    private final ObservableList<Agent> agents = FXCollections.observableArrayList();
    private final ObservableList<Service> services = FXCollections.observableArrayList();

    /**
     * Initialise l'UI "agents" :
     * - colonnes
     * - listeners de sélection
     * - boutons CRUD
     * - chargement initial des données
     */
    private void initAgentsUi() {
        // Colonnes TableView
        colNomAgent.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().lastName()));
        colPrenomAgent.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().firstName()));
        colMatAgent.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().matricule()));

        tabAgents.setItems(agents);

        // Quand on sélectionne un agent, on remplit le formulaire
        tabAgents.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) fillAgentForm(n);
        });

        // Chargement services pour la ComboBox
        reloadServicesForCombo();
        comboServiceAgent.setItems(services);

        // Actions CRUD
        btnAjouterAgent.setOnAction(e -> onAddAgent());
        btnModifierAgent.setOnAction(e -> onEditAgent());
        btnSauvegarderAgent.setOnAction(e -> {
            try { onSaveAgent(); }
            catch (Exception ex) { showError(ex.getMessage()); }
        });
        btnSupprimerAgent.setOnAction(e -> {
            try { onDeleteAgent(); }
            catch (Exception ex) { showError(ex.getMessage()); }
        });

        // Form en lecture seule au départ
        setAgentFormEditable(false);

        // affecter un véhicule à l'agent sélectionné
        if (btnAffecterVehicule != null) {
            btnAffecterVehicule.setOnAction(e -> {
                Agent a = tabAgents.getSelectionModel().getSelectedItem();
                if (a == null) { showError("Sélectionne un agent."); return; }
                showAffectationAgents();
                reloadAffectationAgentsData();
                selectAgentInAffectationById(a.id());
            });
        }

        // voir la liste des affectations de l'agent sélectionné
        if (btnRetirerAffectation != null) {
            btnRetirerAffectation.setOnAction(e -> {
                Agent a = tabAgents.getSelectionModel().getSelectedItem();
                if (a == null) { showError("Sélectionne un agent."); return; }
                showListeAffectations();
                reloadAffectations();
                selectAgentInAffectationById(a.id());
            });
        }

        reloadAgents();
    }

    /** Pré-sélectionne un agent dans l'écran "Affectation" à partir de son id. */
    private void selectAgentInAffectationById(long id) {
        for (Agent a : agentsA) {
            if (a.id() == id) {
                tabAListeAgents.getSelectionModel().select(a);
                tabAListeAgents.scrollTo(a);
                return;
            }
        }
    }

    /** Active/Désactive l'édition du formulaire agent. */
    private void setAgentFormEditable(boolean editable) {
        tfNomAgent.setEditable(editable);
        tPrenomAgent.setEditable(editable);
        tfMatAgent.setEditable(editable);
        comboServiceAgent.setDisable(!editable);
    }

    /** Vide le formulaire agent. */
    private void clearAgentForm() {
        tfNomAgent.clear();
        tPrenomAgent.clear();
        tfMatAgent.clear();
        comboServiceAgent.setValue(null);
    }

    /** Passage en mode création d'agent. */
    private void onAddAgent() {
        clearAgentForm();
        setAgentFormEditable(true);
        agentCreateMode = true;
        agentEditMode = false;
    }

    /** Passage en mode édition d'agent (sur agent sélectionné). */
    private void onEditAgent() {
        Agent selected = tabAgents.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Sélectionne un agent à modifier.");
            return;
        }
        setAgentFormEditable(true);
        agentEditMode = true;
        agentCreateMode = false;
    }

    /**
     * Sauvegarde agent :
     * - si agentCreateMode : INSERT
     * - si agentEditMode   : UPDATE
     */
    private void onSaveAgent() throws Exception {
        String lastName = tfNomAgent.getText().trim();
        String firstName = tPrenomAgent.getText().trim();
        String matricule = tfMatAgent.getText().trim();
        Service service = comboServiceAgent.getValue();

        if (lastName.isEmpty() || firstName.isEmpty() || matricule.isEmpty() || service == null) {
            throw new IllegalArgumentException("Tous les champs agent sont obligatoires.");
        }

        // Création
        if (agentCreateMode) {
            Agent created = agentDao.create(matricule, firstName, lastName, service.id());
            reloadAgents();
            tabAgents.getSelectionModel().select(created);
            tabAgents.scrollTo(created);

            agentCreateMode = false;
            setAgentFormEditable(false);
            return;
        }

        // Edition
        if (agentEditMode) {
            Agent selected = tabAgents.getSelectionModel().getSelectedItem();
            if (selected == null) throw new IllegalStateException("Aucun agent sélectionné.");

            boolean ok = agentDao.update(selected.id(), matricule, firstName, lastName, service.id());
            if (!ok) throw new IllegalStateException("Mise à jour échouée.");

            reloadAgents();
            agentEditMode = false;
            setAgentFormEditable(false);
            return;
        }

        showError("Clique sur Ajouter ou Modifier avant de sauvegarder.");
    }

    /** Supprime l'agent sélectionné (avec confirmation). */
    private void onDeleteAgent() throws Exception {
        Agent selected = tabAgents.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Sélectionne un agent à supprimer.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Suppression");
        confirm.setHeaderText("Supprimer l'agent " + selected.lastName() + " " + selected.firstName() + " ?");
        confirm.setContentText("Cette action est irréversible.");
        var res = confirm.showAndWait();
        if (res.isEmpty() || res.get() != javafx.scene.control.ButtonType.OK) return;

        boolean ok = agentDao.delete(selected.id());
        if (!ok) throw new IllegalStateException("Suppression échouée (id=" + selected.id() + ")");

        reloadAgents();
        clearAgentForm();
        setAgentFormEditable(false);
    }

    /** Remplit le formulaire avec l'agent sélectionné. */
    private void fillAgentForm(Agent a) {
        tfNomAgent.setText(a.lastName());
        tPrenomAgent.setText(a.firstName());
        tfMatAgent.setText(a.matricule());

        // Sélectionner le service correspondant
        for (Service s : services) {
            if (s.id() == a.serviceId()) {
                comboServiceAgent.setValue(s);
                break;
            }
        }

        // En sélection, on repasse en lecture seule et on sort des modes
        setAgentFormEditable(false);
        agentCreateMode = false;
        agentEditMode = false;
    }

    /** Recharge la liste agents depuis la base. */
    private void reloadAgents() {
        try {
            agents.setAll(agentDao.findAll());
        } catch (Exception e) {
            showError("Impossible de charger les agents: " + e.getMessage());
        }
    }

    /** Recharge la liste des services (ComboBox). */
    private void reloadServicesForCombo() {
        try {
            services.setAll(serviceDao.findAll());
        } catch (Exception e) {
            showError("Impossible de charger les services: " + e.getMessage());
        }
    }

    // ----------------------------
    //  AFFECTATION AGENT <-> VEH
    // ----------------------------

    /** Listes UI pour l'écran d'affectation. */
    private final ObservableList<com.floteo.model.Agent> agentsA = FXCollections.observableArrayList();
    private final ObservableList<Vehicle> vehiclesA = FXCollections.observableArrayList();

    /**
     * Initialise l'écran "affectation" :
     * - colonnes table agents + véhicules
     * - boutons Affecter / Retirer
     * - champs de recherche
     * - chargement initial des données
     */
    private void initAffectationAgentsUi() {
        // colonnes agents
        colANomAgent.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().lastName()));
        colAPrenomAgent.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().firstName()));
        colAMatAgent.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().matricule()));

        // aller chercher le nom du service pour lier agent.serviceId -> service.name
        reloadServicesForCombo();

        // Map locale idService -> nomService
        final Map<Long, String> idToName = new HashMap<>();
        for (Service s : services) {
            idToName.put(s.id(), s.name());
        }

        colAServiceAgent.setCellValueFactory(c ->
                new SimpleStringProperty(idToName.getOrDefault(c.getValue().serviceId(), "—"))
        );



        tabAListeAgents.setItems(agentsA);

        // colonnes véhicules
        colAMarque.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().brand()));
        colAModele.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().model()));
        colAImmat.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().plate()));
        colAStatus.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().status())));
        colAEtat.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().etat())));

        tabAListeVehicules.setItems(vehiclesA);

        // actions
        btnAffecter.setOnAction(e -> {
            try { onAffecterAgentVehicule(); }
            catch (Exception ex) { showError(ex.getMessage()); }
        });

        // Ici btnCloturer sert à "cloturer" une affectation (fin d'affectation active)
        btnCloturer.setOnAction(e -> {
            try { onRetirerAffectationVehicule(); }
            catch (Exception ex) { showError(ex.getMessage()); }
        });

        // Recherche agent / véhicule
        if (btnGoAgents != null) btnGoAgents.setOnAction(e -> reloadAffectationAgentsData());
        if (tfRechercherAgent != null) tfRechercherAgent.setOnAction(e -> reloadAffectationAgentsData());

        if (btnGoVehicules != null) btnGoVehicules.setOnAction(e -> reloadAffectationAgentsData());
        if (tfRechercherVehicule != null) tfRechercherVehicule.setOnAction(e -> reloadAffectationAgentsData());

        // Combo état (valeurs "UI") libellé fixes
        if (comboEtat != null) {
            comboEtat.setItems(FXCollections.observableArrayList("Neuf", "Très bon état", "Bon état", "Mauvais état", "Très mauvais état"));
        }

        reloadAffectationAgentsData();
    }

    /** Pré-sélectionne un véhicule dans l'écran affectation à partir de son id. */
    private void selectVehicleInAffectationById(long id) {
        for (Vehicle v : vehiclesA) {
            if (v.id() == id) {
                tabAListeVehicules.getSelectionModel().select(v);
                tabAListeVehicules.scrollTo(v);
                return;
            }
        }
    }

    /**
     * Recharge les données de l'écran affectation :
     * - agents filtrés par texte
     * - véhicules DISPONIBLES filtrés par texte
     */
    private void reloadAffectationAgentsData() {
        try {
            String qa = (tfRechercherAgent == null) ? "" : tfRechercherAgent.getText();
            String qv = (tfRechercherVehicule == null) ? "" : tfRechercherVehicule.getText();

            agentsA.setAll(agentDao.findByText(qa));
            vehiclesA.setAll(vehicleDao.findAvailableByText(qv));
        } catch (Exception e) {
            showError("Chargement affectation impossible: " + e.getMessage());
        }
    }

    /** Affecte le véhicule sélectionné à l'agent sélectionné (date = aujourd'hui). */
    private void onAffecterAgentVehicule() throws Exception {
        var agent = (com.floteo.model.Agent) tabAListeAgents.getSelectionModel().getSelectedItem();
        var veh   = (Vehicle) tabAListeVehicules.getSelectionModel().getSelectedItem();

        if (agent == null || veh == null) throw new IllegalArgumentException("Sélectionne un agent et un véhicule.");
        assignmentService.assignVehicle(veh.id(), agent.id(), LocalDate.now());

        reloadAffectationAgentsData();
        reloadVehicles();
    }

    /** Termine l'affectation active d'un véhicule (date de fin = aujourd'hui). */
    private void onRetirerAffectationVehicule() throws Exception {
        var veh = (Vehicle) tabAListeVehicules.getSelectionModel().getSelectedItem();
        if (veh == null) throw new IllegalArgumentException("Sélectionne un véhicule.");
        assignmentService.endActiveAssignmentForVehicle(veh.id(), LocalDate.now());

        reloadAffectationAgentsData();
        reloadVehicles();
    }

    // ----------------------------
    //            VEHICULES
    // ----------------------------

    /**
     * Initialise l'UI véhicules :
     * - colonnes table
     * - listeners de sélection
     * - combobox (type/status)
     * - handlers CRUD et actions rapides
     * - chargement initial
     */
    private void initVehiclesUi() {
        // colonnes
        colVehicule.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty(
                        cell.getValue().brand() + " " + cell.getValue().model()
                )
        );
        colImmat.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().plate()));
        colStatus.setCellValueFactory(cell -> new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().status()));

        // combos
        comboStatus.setItems(FXCollections.observableArrayList(VehicleStatus.values()));
        reloadVehicleTypes();

        // table data
        tabVehicules.setItems(vehicles);

        // remplir le formulaire + activer/désactiver boutons selon status
        tabVehicules.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                fillVehicleForm(newV);

                // Gestion UI selon statut du véhicule
                if (newV.status() == VehicleStatus.ENTRETIEN) {
                    btnAffecterAgent.setDisable(true);
                    btnRendreDisponible.setDisable(false);
                    btnRendreIndisponible.setDisable(true);
                }
                if (newV.status() == VehicleStatus.DISPONIBLE) {
                    btnAffecterAgent.setDisable(false);
                    btnRendreDisponible.setDisable(true);
                    btnRendreIndisponible.setDisable(false);
                }

                setVehicleFormEditable(false);
                editMode = false;
                createMode = false;
            }
        });

        // boutons CRUD
        btnAjouterVehicule.setOnAction(e -> onAddVehicle());
        btnModifierVehicule.setOnAction(e -> onEditVehicle());
        btnSauvegarderVehicule.setOnAction(e -> {
            try { onSaveVehicle(); }
            catch (Exception ex) { showError(ex.getMessage()); }
        });
        btnSupprimerVehicule.setOnAction(e -> {
            try { onDeleteVehicle(); }
            catch (Exception ex) { showError(ex.getMessage()); }
        });

        // Actions rapides véhicule : rendre disponible / indisponible
        if (btnRendreDisponible != null) {
            btnRendreDisponible.setOnAction(e -> {
                try {
                    System.out.println("btnRendreDisponible clicked");
                    Vehicle v = tabVehicules.getSelectionModel().getSelectedItem();
                    if (v == null) throw new IllegalArgumentException("Sélectionne un véhicule.");
                    vehicleDao.updateStatus(v.id(), VehicleStatus.DISPONIBLE);

                    // Mise à jour UI immédiate + reload
                    btnAffecterAgent.setDisable(false);
                    btnRendreDisponible.setDisable(true);
                    btnRendreIndisponible.setDisable(false);
                    reloadVehicles();
                } catch (Exception ex) {
                    showError(ex.getMessage());
                }
            });
        }

        if (btnRendreIndisponible != null) {
            btnRendreIndisponible.setOnAction(e -> {
                try {
                    Vehicle v = tabVehicules.getSelectionModel().getSelectedItem();
                    if (v == null) throw new IllegalArgumentException("Sélectionne un véhicule.");
                    vehicleDao.updateStatus(v.id(), VehicleStatus.ENTRETIEN);

                    // Mise à jour UI immédiate + reload
                    btnAffecterAgent.setDisable(true);
                    btnRendreDisponible.setDisable(false);
                    btnRendreIndisponible.setDisable(true);
                    reloadVehicles();
                } catch (Exception ex) {
                    showError(ex.getMessage());
                }
            });
        }

        // affecter un véhicule depuis la liste véhicules
        if (btnAffecterAgent != null) {
            btnAffecterAgent.setOnAction(e -> {
                Vehicle v = tabVehicules.getSelectionModel().getSelectedItem();
                if (v == null) { showError("Sélectionne un véhicule."); return; }

                // Bascule sur l’écran d’affectation et pré-sélectionne le véhicule
                showAffectationAgents();
                reloadAffectationAgentsData();
                selectVehicleInAffectationById(v.id());
            });
        }

        // état initial
        setVehicleFormEditable(false);
        reloadVehicles();
    }

    /** Recharge les types de véhicules (table vehicle_type) dans comboType. */
    private void reloadVehicleTypes() {
        try {
            comboType.setItems(FXCollections.observableArrayList(vehicleDao.findAllTypeLabels()));
        } catch (Exception e) {
            showError("Impossible de charger les types: " + e.getMessage());
            comboType.setItems(FXCollections.observableArrayList()); // fallback vide
        }
    }

    /** Recharge la liste des véhicules depuis la base. */
    private void reloadVehicles() {
        try {
            vehicles.setAll(vehicleDao.findAll());
        } catch (Exception e) {
            showError("Impossible de charger les véhicules: " + e.getMessage());
        }
    }

    /** Remplit le formulaire véhicule avec les données du véhicule sélectionné. */
    private void fillVehicleForm(Vehicle v) {
        tfMarque.setText(v.brand());
        tfModele.setText(v.model());
        tfImmat.setText(v.plate());
        tfKilometrage.setText(String.valueOf(v.mileage()));
        comboType.setValue(v.type());
        comboStatus.setValue(v.status());

        // Sélection de l'état (comboEtat) en comparant l'id "etat" du véhicule
        try {
            if (v.etat() != 0 && comboEtat.getItems() != null) {
                for (String etatName : comboEtat.getItems()) {
                    // id correspondant à un nom d'état (DAO)
                    Optional<Etat> etat = etatDao.findByName(etatName);
                    if (etat != null && etat.get().id() == v.etat()) {
                        comboEtat.setValue(etatName);
                        break;
                    }
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    /** Vide le formulaire véhicule. */
    private void clearVehicleForm() {
        tfMarque.clear();
        tfModele.clear();
        tfImmat.clear();
        tfKilometrage.clear();
        comboType.setValue(null);
        comboStatus.setValue(null);
    }

    /** Active/Désactive l'édition du formulaire véhicule. */
    private void setVehicleFormEditable(boolean editable) {
        tfMarque.setEditable(editable);
        tfModele.setEditable(editable);
        tfImmat.setEditable(editable);
        tfKilometrage.setEditable(editable);

        comboType.setDisable(!editable);
        comboStatus.setDisable(!editable);
        comboEtat.setDisable(!editable);
    }

    /** Mode création véhicule. */
    private void onAddVehicle() {
        clearVehicleForm();
        setVehicleFormEditable(true);

        // Valeur par défaut
        comboStatus.setValue(VehicleStatus.DISPONIBLE);

        createMode = true;
        editMode = false;
    }

    /** Mode édition véhicule (sur véhicule sélectionné). */
    private void onEditVehicle() {
        Vehicle selected = tabVehicules.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Sélectionne un véhicule à modifier.");
            return;
        }
        setVehicleFormEditable(true);
        editMode = true;
        createMode = false;
    }

    /**
     * Sauvegarde véhicule :
     * - createMode : INSERT
     * - editMode   : UPDATE
     */
    private void onSaveVehicle() throws Exception {
        String plate = tfImmat.getText().trim();
        String brand = tfMarque.getText().trim();
        String model = tfModele.getText().trim();
        String type = comboType.getValue();
        VehicleStatus status = comboStatus.getValue();

        int mileage;
        try {
            mileage = Integer.parseInt(tfKilometrage.getText().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Kilométrage invalide.");
        }

        // L'état est choisi via un libellé (ComboBox), puis converti en id
        String etatName = comboEtat.getValue();
        if (etatName == null || etatName.isEmpty()) {
            throw new IllegalArgumentException("L'état du véhicule doit être sélectionné.");
        }

        long etatId = etatIdToName.entrySet().stream()
                .filter(entry -> entry.getValue().equals(etatName))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("État invalide sélectionné"));

        if (plate.isEmpty() || brand.isEmpty() || model.isEmpty() || type == null || status == null) {
            throw new IllegalArgumentException("Tous les champs véhicule sont obligatoires.");
        }

        // Création
        if (createMode) {
            Vehicle created = vehicleDao.create(
                    plate,
                    type,
                    brand,
                    model,
                    mileage,
                    LocalDate.now(),
                    status,
                    etatId
            );
            reloadVehicles();
            tabVehicules.getSelectionModel().select(created);
            createMode = false;
            setVehicleFormEditable(false);
            return;
        }

        // Edition
        if (editMode) {
            Vehicle selected = tabVehicules.getSelectionModel().getSelectedItem();
            if (selected == null) throw new IllegalStateException("Aucun véhicule sélectionné.");

            boolean ok = vehicleDao.update(
                    selected.id(),
                    plate,
                    type,
                    brand,
                    model,
                    mileage,
                    status,
                    etatId
            );

            if (!ok) throw new IllegalStateException("Mise à jour échouée.");

            reloadVehicles();

            // Re-sélectionner l'élément mis à jour dans la table
            for (Vehicle v : vehicles) {
                if (v.id() == selected.id()) {
                    tabVehicules.getSelectionModel().select(v);
                    tabVehicules.scrollTo(v);
                    break;
                }
            }

            editMode = false;
            setVehicleFormEditable(false);
            return;
        }
    }

    /** Supprime le véhicule sélectionné (avec confirmation). */
    private void onDeleteVehicle() throws Exception {
        Vehicle selected = tabVehicules.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Sélectionne un véhicule à supprimer.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Suppression");
        confirm.setHeaderText("Supprimer le véhicule " + selected.plate() + " ?");
        confirm.setContentText("Cette action est irréversible.");
        var res = confirm.showAndWait();
        if (res.isEmpty() || res.get() != javafx.scene.control.ButtonType.OK) return;

        boolean ok = vehicleDao.delete(selected.id());
        if (!ok) throw new IllegalStateException("Suppression échouée (id=" + selected.id() + ")");

        reloadVehicles();
        clearVehicleForm();
    }

    // ----------------------------
    //     UTILITAIRE D'ERREUR
    // ----------------------------

    /** Affiche un message d'erreur à l'utilisateur. */
    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Erreur");
        a.setHeaderText("Erreur");
        a.setContentText(msg);
        a.showAndWait();
    }

    // ----------------------------
    //         AFFECTATIONS
    // ----------------------------

    /**
     * Ligne "UI" pour la table des affectations.
     * structure plus riche que l'entité Assignment (qui contient juste des ids).
     */
    public record AssignmentRow(
            long assignmentId,
            long agentId,
            long vehicleId,
            LocalDate dateDemande,
            String agent,
            String vehicule,
            String immat,
            LocalDate dateDepart,
            LocalDate dateRetour,
            String status
    ) {}

    /**
     * Initialise la liste des affectations :
     * - colonnes
     * - activation/désactivation des boutons selon le statut
     * - recherche et navigation vers agent/véhicule
     */
    private void initAffectationsUi() {
        colLAAgent.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().agent()));
        colLAVehicule.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().vehicule()));
        colLAImmat.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().immat()));
        colLAStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().status()));

        colLADateDemande.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().dateDemande() == null ? "" : c.getValue().dateDemande().format(df)));
        colLADateDepart.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().dateDepart() == null ? "" : c.getValue().dateDepart().format(df)));

        // Date retour
        colLADateRetour.setCellValueFactory(c -> {
            var r = c.getValue();
            if (r.dateRetour() != null) return new SimpleStringProperty(r.dateRetour().format(df));
            return new SimpleStringProperty(""); // DEMANDEE / REFUSEE sans date retour
        });

        tabListeAffectations.setItems(affectations);

        // Selon la ligne sélectionnée, activer les boutons accept/cloturer
        tabListeAffectations.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            boolean isDemande = n != null && "DEMANDEE".equalsIgnoreCase(n.status());
            boolean isEnCours = n != null && "EN_COURS".equalsIgnoreCase(n.status());

            btnAccepter.setDisable(!isDemande);
            btnCloturer.setDisable(!(isDemande || isEnCours));
        });

        btnAccepter.setDisable(true);
        btnCloturer.setDisable(true);

        // recherche (bouton + Enter)
        btnGoVehicule.setOnAction(e -> reloadAffectations());
        tfChercherVehicule.setOnAction(e -> reloadAffectations());

        // navigation depuis une affectation vers l'agent correspondant
        btnVoirAgent.setOnAction(e -> {
            AssignmentRow row = tabListeAffectations.getSelectionModel().getSelectedItem();
            if (row == null) { showError("Sélectionne une affectation."); return; }

            showListeAgents();
            reloadAgents();
            selectAgentById(row.agentId());
        });

        // navigation depuis une affectation vers le véhicule correspondant
        btnVoirVehicule.setOnAction(e -> {
            AssignmentRow row = tabListeAffectations.getSelectionModel().getSelectedItem();
            if (row == null) { showError("Sélectionne une affectation."); return; }

            showListeVehicules();
            reloadVehicles();
            selectVehicleById(row.vehicleId());
        });

        // actions métier sur affectation
        btnAccepter.setOnAction(e -> {
            try { onAccepterAffectation(); }
            catch (Exception ex) { showError(ex.getMessage()); }
        });

        // Ici "cloturer" sert à refuser une demande OU clôturer une affectation en cours
        btnCloturer.setOnAction(e -> {
            try { onRefuserAffectation(); }
            catch (Exception ex) { showError(ex.getMessage()); }
        });

        reloadAffectations();
    }

    /** Recharge la table des affectations en appliquant le filtre texte véhicule. */
    private void reloadAffectations() {
        try {
            String q = tfChercherVehicule.getText() == null ? "" : tfChercherVehicule.getText().trim();
            affectations.setAll(assignmentDao.findRowsForUi(q));
        } catch (Exception e) {
            showError("Impossible de charger les affectations: " + e.getMessage());
        }
    }

    /** Sélectionne un agent dans la table principale Agents à partir de son id. */
    private void selectAgentById(long id) {
        for (Agent a : agents) {
            if (a.id() == id) {
                tabAgents.getSelectionModel().select(a);
                tabAgents.scrollTo(a);
                return;
            }
        }
    }

    /** Sélectionne un véhicule dans la table principale Véhicules à partir de son id. */
    private void selectVehicleById(long id) {
        for (Vehicle v : vehicles) {
            if (v.id() == id) {
                tabVehicules.getSelectionModel().select(v);
                tabVehicules.scrollTo(v);
                return;
            }
        }
    }

    /**
     * Accepte une demande d'affectation :
     * - assignment : DEMANDEE -> EN_COURS
     * - vehicle    : DISPONIBLE -> AFFECTE
     */
    private void onAccepterAffectation() throws Exception {
        AssignmentRow row = tabListeAffectations.getSelectionModel().getSelectedItem();
        if (row == null) { showError("Sélectionne une affectation."); return; }

        boolean ok = assignmentDao.accept(row.assignmentId());
        if (!ok) { showError("Affectation déjà traitée (ou pas en attente)."); return; }

        vehicleDao.updateStatus(row.vehicleId(), VehicleStatus.AFFECTE);

        reloadAffectations();
        reloadVehicles();
    }

    /**
     * Refuse une demande OU clôture une affectation en cours :
     * - Si DEMANDEE : status -> REFUSEE + end_date
     * - Si EN_COURS : status -> CLOTUREE + end_date
     * Dans les deux cas, le véhicule redevient DISPONIBLE.
     */
    private void onRefuserAffectation() throws Exception {
        AssignmentRow row = tabListeAffectations.getSelectionModel().getSelectedItem();
        LocalDate end = LocalDate.now();

        if (row == null) {
            showError("Sélectionne une affectation.");
            return;
        }

        String status = row.status();

        if ("DEMANDEE".equalsIgnoreCase(status)) {
            // Cas normal : on refuse une demande
            boolean ok = assignmentDao.refuseDemande(row.assignmentId(), end);
            if (!ok) {
                showError("Affectation déjà traitée.");
                return;
            }

            assignmentDao.endAssignment(row.assignmentId(), end);
            vehicleDao.updateStatus(row.vehicleId(), VehicleStatus.DISPONIBLE);

        } else if ("EN_COURS".equalsIgnoreCase(status)) {
            // Cas spécial : on clôture une affectation en cours
            boolean ok = assignmentDao.cloturerEnCours(row.assignmentId(), end);
            if (!ok) {
                showError("Affectation déjà traitée.");
                return;
            }
            assignmentDao.endAssignment(row.assignmentId(), end);
            vehicleDao.updateStatus(row.vehicleId(), VehicleStatus.DISPONIBLE);

        } else {
            showError("Cette affectation ne peut plus être traitée.");
            return;
        }

        reloadAffectations();
        reloadVehicles();
    }

    // ----------------------------
    //           CALENDRIER
    // ----------------------------

    /**
     * Ouvre le calendrier (CalendarFX).
     * On ne crée la vue qu'une seule fois (lazy loading).
     */
    @FXML
    private void ouvrirCalendrier() {
        if (calendarRoot == null) {
            calendarRoot = creerCalendarView();
            calendarContainer.getChildren().add(calendarRoot);
        }

        // On masque le dashboard et on affiche le calendrier
        paneDashboardVehicules.setVisible(false);
        calendarContainer.setVisible(true);

        // Ici on masque les boutons de navigation pour un mode "plein écran"
        btnBack.setVisible(false);
        btnHome.setVisible(false);
        svgBack.setVisible(false);
        svgHome.setVisible(false);
    }

    /**
     * Construit la vue du calendrier (CalendarFX) et ajoute un bouton "Retour".
     *
     * - Crée un calendrier logique ("Réservations")
     * - Lance le chargement des affectations depuis la base
     * - Crée la vue graphique CalendarView
     * - Désactive certaines fonctionnalités inutiles (ajout / impression)
     * - Encapsule le tout dans un BorderPane avec un bouton Retour
     */
    private Parent creerCalendarView() {
        // Calendrier logique contenant les réservations
        Calendar calendar = new Calendar("Réservations");
        calendar.setStyle(Calendar.Style.STYLE1);

        // Chargement asynchrone des réservations depuis la base
        chargerReservations(calendar);

        // Source de calendriers (obligatoire pour CalendarView)
        CalendarSource source = new CalendarSource("BDD");
        source.getCalendars().add(calendar);

        // Vue graphique du calendrier
        CalendarView calendarView = new CalendarView();
        calendarView.getCalendarSources().add(source);

        // On enlève certaines options de l'UI CalendarFX
        calendarView.setShowAddCalendarButton(false);
        calendarView.setShowPrintButton(false);

        // Bouton retour vers le dashboard véhicules
        Button btnRetour = new Button("Retour");
        btnRetour.setOnAction(e -> {
            calendarContainer.setVisible(false);
            paneDashboardVehicules.setVisible(true);
            btnBack.setVisible(true);
            btnHome.setVisible(true);
            svgBack.setVisible(true);
            svgHome.setVisible(true);
        });

        // Assemblage final
        BorderPane root = new BorderPane();
        root.setTop(btnRetour);
        root.setCenter(calendarView);

        return root;
    }

    /**
     * Charge les affectations depuis la base en tâche asynchrone,
     * puis les convertit en "Entry" CalendarFX.
     *
     * On utilise AssignmentRow (jointure agent + véhicule) afin d'avoir
     * directement les libellés à afficher dans le calendrier
     * (nom de l'agent, véhicule, immatriculation).
     *
     * Le chargement se fait dans un Task pour ne pas bloquer le thread JavaFX.
     */
    private void chargerReservations(Calendar calendar) {

        Task<List<AssignmentRow>> task = new Task<>() {
            @Override
            protected List<AssignmentRow> call() {
                try {
                    // "" = pas de filtre : on charge toutes les affectations
                    return assignmentDao.findRowsForUi("");
                } catch (SQLException ex) {
                    System.out.println(ex.getMessage());
                    return new ArrayList<>();
                }
            }
        };

        task.setOnSucceeded(e -> {
            // Nettoyage pour éviter les doublons si le calendrier est rouvert
            calendar.clear();

            // Conversion de chaque affectation en Entry CalendarFX
            for (AssignmentRow row : task.getValue()) {
                calendar.addEntry(convertirEnEntry(row));
            }
        });

        task.setOnFailed(e -> task.getException().printStackTrace());

        new Thread(task).start();
    }

    /**
     * Convertit une ligne d'affectation (AssignmentRow) en Entry CalendarFX.
     *
     * - Le titre affiche : "Agent - Véhicule (Immatriculation)"
     * - La période est basée sur dateDepart / dateRetour
     * - Si la date de retour est absente, l'événement est limité à un seul jour
     * - L'id de l'affectation est stocké dans l'Entry (userObject)
     */
    private Entry<?> convertirEnEntry(AssignmentRow r) {
        // Titre affiché dans le calendrier
        String title = r.agent() + " - " + r.vehicule() + " (" + r.immat() + ")";

        Entry<Long> entry = new Entry<>(title);

        // Détermination de la date de début
        LocalDate start = r.dateDepart();           // prioritaire
        if (start == null) start = r.dateDemande(); // fallback
        if (start == null) start = LocalDate.now(); // sécurité

        // Détermination de la date de fin
        LocalDate end = (r.dateRetour() == null) ? start : r.dateRetour();
        if (end.isBefore(start)) end = start;

        entry.setInterval(start, end);

        // Stockage de l'id métier
        entry.setUserObject(r.assignmentId());

        return entry;
    }


    // ----------------------------
    //         ETAT VEHICULE
    // ----------------------------

    /**
     * Charge les états depuis la base et remplit comboEtat.
     * Met aussi à jour le cache etatIdToName.
     */
    private void initVehiculeEtat() {
        try {
            List<Etat> etats = etatDao.findAll();
            ObservableList<String> items = FXCollections.observableArrayList();
            etatIdToName.clear();

            for (Etat e : etats) {
                items.add(e.name());
                etatIdToName.put(e.id(), e.name());
            }

            comboEtat.setItems(items);

            if (!items.isEmpty()) {
                comboEtat.getSelectionModel().selectFirst();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Impossible de charger les états véhicule");
            throw new RuntimeException(ex);
        }
    }
}
