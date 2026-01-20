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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.io.Console;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.*;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.layout.StackPane;


import java.time.LocalDate;


import java.sql.Connection;

public class InterfaceController {

    @FXML private javafx.scene.shape.SVGPath svgBack;
    @FXML private javafx.scene.shape.SVGPath svgHome;

    // --- PANES ---
    @FXML private Pane paneAccueil;
    @FXML private Pane paneGestionVehicules;
    @FXML private Pane paneDashboardVehicules;
    @FXML private Pane paneListeVehicules;
    @FXML private Pane paneGestionAgents;
    @FXML private Pane paneMenuGestionAgents;
    @FXML private Pane paneListeAgents;
    @FXML private Pane paneListeAffectations;
    @FXML private Pane paneAffectationAgent;

    // --- BUTTONS ---
    @FXML private Button btnGestionVehicules;
    @FXML private Button btnAffectationAgents;
    @FXML private Button btnBack;
    @FXML private Button btnHome;
    @FXML private Button btnVehicules;

    // --- DB / DAO / SERVICES ---
    private Connection conn;
    private ServiceDao serviceDao;
    private AgentDao agentDao;
    private EtatDao etatDao;
    private VehicleDao vehicleDao;
    private AssignmentDao assignmentDao;
    private AssignmentService assignmentService;

    // --- AGENTS ---
    @FXML private TableView<Agent> tabAgents;
    @FXML private TableColumn<Agent, String> colNomAgent;
    @FXML private TableColumn<Agent, String> colPrenomAgent;
    @FXML private TableColumn<Agent, String> colMatAgent;

    @FXML private TextField tfNomAgent;
    @FXML private TextField tPrenomAgent;
    @FXML private TextField tfMatAgent;

    @FXML private ComboBox<Service> comboServiceAgent;

    @FXML private Button btnModifierAgent;
    @FXML private Button btnSupprimerAgent;
    @FXML private Button btnSauvegarderAgent;
    @FXML private Button btnAjouterAgent;
    @FXML private Button btnListeAgents;
    @FXML private Button btnVoirAffectations;
    @FXML private Button btnAffecterAgents;
    @FXML private Button btnAffecterVehicule;
    @FXML private Button btnRetirerAffectation;


    @FXML private TableView<Agent> tabAListeAgents;
    @FXML private TableColumn<Agent, String> colANomAgent;
    @FXML private TableColumn<Agent, String> colAPrenomAgent;
    @FXML private TableColumn<Agent, String> colAMatAgent;
    @FXML private TableColumn<Agent, String> colAServiceAgent;

    @FXML private Button btnAffecter;
    @FXML private Button btnRetirer;


    // --- VEHICULE ---
    @FXML private TextField tfMarque;
    @FXML private TextField tfModele;
    @FXML private TextField tfImmat;
    @FXML private TextField tfKilometrage;

    @FXML private ComboBox<String> comboType;
    @FXML private ComboBox<String> comboEtat;
    @FXML private ComboBox<VehicleStatus> comboStatus;

    @FXML private Button btnModifierVehicule;
    @FXML private Button btnSupprimerVehicule;
    @FXML private Button btnSauvegarderVehicule;
    @FXML private Button btnAjouterVehicule;
    @FXML private Button btnAffecterAgent;
    @FXML private Button btnRendreIndisponible;
    @FXML private Button btnRendreDisponible;

    @FXML private TableView<Vehicle> tabVehicules;
    @FXML private TableColumn<Vehicle, String> colVehicule;
    @FXML private TableColumn<Vehicle, String> colImmat;
    @FXML private TableColumn<Vehicle, VehicleStatus> colStatus;

    @FXML private TableView<Vehicle> tabAListeVehicules;
    @FXML private TableColumn<Vehicle, String> colAMarque;
    @FXML private TableColumn<Vehicle, String> colAModele;
    @FXML private TableColumn<Vehicle, String> colAImmat;
    @FXML private TableColumn<Vehicle, String> colAStatus;

    @FXML private javafx.scene.control.Label lblDate;
    @FXML private javafx.scene.control.Label lblDisponibles;
    @FXML private javafx.scene.control.Label lblIndisponibles;
    @FXML private javafx.scene.control.Label lblReserves;

    // --- AFFECTATIONS (liste) ---
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

    @FXML private TextField tfChercherVehicule;
    @FXML private Button btnGoVehicule;
    @FXML private TextField tfRechercherAgent;
    @FXML private Button btnGoAgents;
    @FXML private TextField tfRechercherVehicule;
    @FXML private Button btnGoVehicules;

    // --- CALENDRIER ---
    @FXML private StackPane calendarContainer;
    @FXML private Button btnCalendrier;
    private Parent calendarRoot;

    private final ObservableList<AssignmentRow> affectations = FXCollections.observableArrayList();
    private final DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private ObservableList<Vehicle> vehicles = FXCollections.observableArrayList();

    private boolean editMode = false;
    private boolean createMode = false;
    private boolean agentEditMode = false;
    private boolean agentCreateMode = false;

    private Map<Long, String> etatIdToName = new HashMap<>();

    /** appelée depuis InterfaceApp après le load() */
    public void init(Connection conn) {
        this.conn = conn;

        this.serviceDao = new ServiceDao(conn);
        this.agentDao = new AgentDao(conn);
        this.etatDao = new EtatDao(conn);
        this.vehicleDao = new VehicleDao(conn);
        this.assignmentDao = new AssignmentDao(conn);
        this.assignmentService = new AssignmentService(conn, agentDao, vehicleDao, assignmentDao);

        // initialiser l'écran
        showAccueil();

        // brancher les events
        bindActions();
    }

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
//        if (btnCalendrier != null) {
//            btnCalendrier.setOnAction(e -> showError("Calendrier non implémenté (à faire)."));
//        }
    }

    // --- NAVIGATION ---
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
        svgBack.setVisible(true);
        svgHome.setVisible(true);
    }

    private void showAccueil() {
        hideAll();
        paneAccueil.setVisible(true);
        btnBack.setVisible(false);
        btnHome.setVisible(false);
        svgBack.setVisible(true);
        svgHome.setVisible(true);
    }

    private void showGestionVehiculesDashboard() {
        hideAll();
        paneGestionVehicules.setVisible(true);
        paneDashboardVehicules.setVisible(true);
        btnBack.setVisible(true);
        btnHome.setVisible(true);

        loadDashboardVehicules();
    }

    private void loadDashboardVehicules() {
        try {
            lblDate.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

            Map<VehicleStatus, Integer> counts = new EnumMap<>(VehicleStatus.class);
            for (VehicleStatus s : VehicleStatus.values()) counts.put(s, 0);

            for (Vehicle v : vehicleDao.findAll()) {
                counts.put(v.status(), counts.get(v.status()) + 1);
            }

            lblDisponibles.setText(String.valueOf(counts.getOrDefault(VehicleStatus.DISPONIBLE, 0)));
            lblIndisponibles.setText(String.valueOf(counts.getOrDefault(VehicleStatus.ENTRETIEN, 0)));
            // "Réservés" = AFFECTE
            lblReserves.setText(String.valueOf(counts.getOrDefault(VehicleStatus.AFFECTE, 0)));

            initVehiculeEtat();

        } catch (Exception e) {
            showError("Impossible de charger le dashboard: " + e.getMessage());
        }
    }

    private boolean vehiclesUiInitialized = false;

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

    private void showGestionAgentsMenu() {
        hideAll();
        paneGestionAgents.setVisible(true);
        paneMenuGestionAgents.setVisible(true);
        btnBack.setVisible(true);
        btnHome.setVisible(true);
    }

    private final java.util.ArrayDeque<Runnable> navStack = new java.util.ArrayDeque<>();
    private Runnable currentView = null;

    // --- NAV HISTORY ---
    private void navigateTo(Runnable view) {
        if (currentView != null) navStack.push(currentView);
        currentView = view;
        view.run();
        btnBack.setVisible(!navStack.isEmpty());
        btnHome.setVisible(true);
    }

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

    private void goHome() {
        navStack.clear();
        currentView = null;
        showAccueil();
    }

    //--- AGENT ---
    private boolean agentsUiInitialized = false;
    private boolean affectationsUiInitialized = false;
    private boolean affectationAgentsUiInitialized = false;

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

    private final ObservableList<Agent> agents = FXCollections.observableArrayList();
    private final ObservableList<Service> services = FXCollections.observableArrayList();

    private void initAgentsUi() {
        // colonnes (adapte selon ton record Agent)
        colNomAgent.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().lastName()));
        colPrenomAgent.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().firstName()));
        colMatAgent.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().matricule()));

        tabAgents.setItems(agents);

        tabAgents.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) fillAgentForm(n);
        });

        // services dans combo
        reloadServicesForCombo();
        comboServiceAgent.setItems(services);

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

        setAgentFormEditable(false);

        if (btnAffecterVehicule != null) {
            btnAffecterVehicule.setOnAction(e -> {
                Agent a = tabAgents.getSelectionModel().getSelectedItem();
                if (a == null) { showError("Sélectionne un agent."); return; }
                showAffectationAgents();
                reloadAffectationAgentsData();
                selectAgentInAffectationById(a.id());
            });
        }

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

    private void selectAgentInAffectationById(long id) {
        for (Agent a : agentsA) {
            if (a.id() == id) {
                tabAListeAgents.getSelectionModel().select(a);
                tabAListeAgents.scrollTo(a);
                return;
            }
        }
    }

    private void setAgentFormEditable(boolean editable) {
        tfNomAgent.setEditable(editable);
        tPrenomAgent.setEditable(editable);
        tfMatAgent.setEditable(editable);
        comboServiceAgent.setDisable(!editable);
    }

    private void clearAgentForm() {
        tfNomAgent.clear();
        tPrenomAgent.clear();
        tfMatAgent.clear();
        comboServiceAgent.setValue(null);
    }

    private void onAddAgent() {
        clearAgentForm();
        setAgentFormEditable(true);
        agentCreateMode = true;
        agentEditMode = false;
    }

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

    private void onSaveAgent() throws Exception {
        String lastName = tfNomAgent.getText().trim();
        String firstName = tPrenomAgent.getText().trim();
        String matricule = tfMatAgent.getText().trim();
        Service service = comboServiceAgent.getValue();

        if (lastName.isEmpty() || firstName.isEmpty() || matricule.isEmpty() || service == null) {
            throw new IllegalArgumentException("Tous les champs agent sont obligatoires.");
        }

        if (agentCreateMode) {
            Agent created = agentDao.create(matricule, firstName, lastName, service.id());
            reloadAgents();
            tabAgents.getSelectionModel().select(created);
            tabAgents.scrollTo(created);

            agentCreateMode = false;
            setAgentFormEditable(false);
            return;
        }

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

    private void fillAgentForm(Agent a) {
        tfNomAgent.setText(a.lastName());
        tPrenomAgent.setText(a.firstName());
        tfMatAgent.setText(a.matricule());

        // sélectionner le service correspondant
        for (Service s : services) {
            if (s.id() == a.serviceId()) {
                comboServiceAgent.setValue(s);
                break;
            }
        }

        setAgentFormEditable(false);
        agentCreateMode = false;
        agentEditMode = false;
    }


    private void reloadAgents() {
        try {
            agents.setAll(agentDao.findAll());
        } catch (Exception e) {
            showError("Impossible de charger les agents: " + e.getMessage());
        }
    }

    private void reloadServicesForCombo() {
        try {
            services.setAll(serviceDao.findAll());
        } catch (Exception e) {
            showError("Impossible de charger les services: " + e.getMessage());
        }
    }

    private final ObservableList<com.floteo.model.Agent> agentsA = FXCollections.observableArrayList();
    private final ObservableList<Vehicle> vehiclesA = FXCollections.observableArrayList();

    private void initAffectationAgentsUi() {
        // colonnes agents
        colANomAgent.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().lastName()));
        colAPrenomAgent.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().firstName()));
        colAMatAgent.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().matricule()));
        colAServiceAgent.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().serviceId())));

        tabAListeAgents.setItems(agentsA);

        // colonnes véhicules
        colAMarque.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().brand()));
        colAModele.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().model()));
        colAImmat.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().plate()));
        colAStatus.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().status())));

        tabAListeVehicules.setItems(vehiclesA);

        // actions
        btnAffecter.setOnAction(e -> {
            try { onAffecterAgentVehicule(); }
            catch (Exception ex) { showError(ex.getMessage()); }
        });

        btnCloturer.setOnAction(e -> {
            try { onRetirerAffectationVehicule(); }
            catch (Exception ex) { showError(ex.getMessage()); }
        });

        if (btnGoAgents != null) btnGoAgents.setOnAction(e -> reloadAffectationAgentsData());
        if (tfRechercherAgent != null) tfRechercherAgent.setOnAction(e -> reloadAffectationAgentsData());

        if (btnGoVehicules != null) btnGoVehicules.setOnAction(e -> reloadAffectationAgentsData());
        if (tfRechercherVehicule != null) tfRechercherVehicule.setOnAction(e -> reloadAffectationAgentsData());

        // Combo état
        if (comboEtat != null) {
            comboEtat.setItems(FXCollections.observableArrayList("OK", "ABIME", "A CONTROLER"));
        }

        // Actions rapides véhicule
        if (btnRendreDisponible != null) {
            btnRendreDisponible.setOnAction(e -> {
                try {
                    Vehicle v = tabVehicules.getSelectionModel().getSelectedItem();
                    if (v == null) throw new IllegalArgumentException("Sélectionne un véhicule.");
                    vehicleDao.updateStatus(v.id(), VehicleStatus.DISPONIBLE);
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
                    reloadVehicles();
                } catch (Exception ex) {
                    showError(ex.getMessage());
                }
            });
        }

        if (btnAffecterAgent != null) {
            btnAffecterAgent.setOnAction(e -> {
                Vehicle v = tabVehicules.getSelectionModel().getSelectedItem();
                if (v == null) { showError("Sélectionne un véhicule."); return; }
                // on bascule sur l’écran d’affectation et on pré-sélectionne le véhicule
                showAffectationAgents();
                reloadAffectationAgentsData();
                selectVehicleInAffectationById(v.id());
            });
        }


        reloadAffectationAgentsData();
    }

    private void selectVehicleInAffectationById(long id) {
        for (Vehicle v : vehiclesA) {
            if (v.id() == id) {
                tabAListeVehicules.getSelectionModel().select(v);
                tabAListeVehicules.scrollTo(v);
                return;
            }
        }
    }

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

    private void onAffecterAgentVehicule() throws Exception {
        var agent = (com.floteo.model.Agent) tabAListeAgents.getSelectionModel().getSelectedItem();
        var veh   = (Vehicle) tabAListeVehicules.getSelectionModel().getSelectedItem();

        if (agent == null || veh == null) throw new IllegalArgumentException("Sélectionne un agent et un véhicule.");
        assignmentService.assignVehicle(veh.id(), agent.id(), LocalDate.now());

        reloadAffectationAgentsData();
        reloadVehicles(); // si tu veux rafraîchir aussi l’autre page
    }

    private void onRetirerAffectationVehicule() throws Exception {
        var veh = (Vehicle) tabAListeVehicules.getSelectionModel().getSelectedItem();
        if (veh == null) throw new IllegalArgumentException("Sélectionne un véhicule.");
        assignmentService.endActiveAssignmentForVehicle(veh.id(), LocalDate.now());

        reloadAffectationAgentsData();
        reloadVehicles();
    }

    // --- VEHICULE ---
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

        // sélection -> remplir le panneau détail
        tabVehicules.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) fillVehicleForm(newV);
            setVehicleFormEditable(false);
            editMode = false;
            createMode = false;
        });

        // boutons
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

        // état initial
        setVehicleFormEditable(false);
        reloadVehicles();
    }

    private void reloadVehicleTypes() {
        try {
            comboType.setItems(FXCollections.observableArrayList(vehicleDao.findAllTypeLabels()));
        } catch (Exception e) {
            showError("Impossible de charger les types: " + e.getMessage());
            comboType.setItems(FXCollections.observableArrayList()); // fallback vide
        }
    }


    private void reloadVehicles() {
        try {
            vehicles.setAll(vehicleDao.findAll());
        } catch (Exception e) {
            showError("Impossible de charger les véhicules: " + e.getMessage());
        }
    }

    private void fillVehicleForm(Vehicle v) {
        tfMarque.setText(v.brand());
        tfModele.setText(v.model());
        tfImmat.setText(v.plate());
        tfKilometrage.setText(String.valueOf(v.mileage()));
        comboType.setValue(v.type());
        comboStatus.setValue(v.status());

        try {
            if (v.etat() != 0 && comboEtat.getItems() != null) {
                for (String etatName : comboEtat.getItems()) {
                    // rechercher le nom correspondant à l'id du véhicule
                    Optional<Etat> etat = etatDao.findByName(etatName);
                    if (etat != null && etat.get().id() == v.etat()) {
                        comboEtat.setValue(etatName);
                        break;
                    }
                }
            }
        }
        catch(SQLException ex){
            throw new RuntimeException(ex);
        }
    }

    private void clearVehicleForm() {
        tfMarque.clear();
        tfModele.clear();
        tfImmat.clear();
        tfKilometrage.clear();
        comboType.setValue(null);
        comboStatus.setValue(null);
    }

    private void setVehicleFormEditable(boolean editable) {
        tfMarque.setEditable(editable);
        tfModele.setEditable(editable);
        tfImmat.setEditable(editable);
        tfKilometrage.setEditable(editable);

        comboType.setDisable(!editable);
        comboStatus.setDisable(!editable);
        comboEtat.setDisable(!editable);
    }

    private void onAddVehicle() {
        clearVehicleForm();
        setVehicleFormEditable(true);

        // valeurs par défaut
        comboStatus.setValue(VehicleStatus.DISPONIBLE);

        createMode = true;
        editMode = false;
    }

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

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Erreur");
        a.setHeaderText("Erreur");
        a.setContentText(msg);
        a.showAndWait();
    }

    // --- AFFECTATION ---
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

    private void initAffectationsUi() {
        colLAAgent.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().agent()));
        colLAVehicule.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().vehicule()));
        colLAImmat.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().immat()));
        colLAStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().status()));
        colLADateDemande.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().dateDemande() == null ? "" : c.getValue().dateDemande().format(df)));
        colLADateDepart.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().dateDepart() == null ? "" : c.getValue().dateDepart().format(df)));
        colLADateRetour.setCellValueFactory(c -> {
            var r = c.getValue();
            if (r.dateRetour() != null) return new SimpleStringProperty(r.dateRetour().format(df));
            if ("EN_COURS".equalsIgnoreCase(r.status())) return new SimpleStringProperty("EN COURS");
            return new SimpleStringProperty(""); // DEMANDEE / REFUSEE sans date retour
        });

        tabListeAffectations.setItems(affectations);

        tabListeAffectations.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            boolean isDemande = n != null && "DEMANDEE".equalsIgnoreCase(n.status());
            boolean isEnCours = n != null && "EN_COURS".equalsIgnoreCase(n.status());

            btnAccepter.setDisable(!isDemande);
            btnCloturer.setDisable(!(isDemande || isEnCours));
        });
        btnAccepter.setDisable(true);
        btnCloturer.setDisable(true);


        // boutons
        btnGoVehicule.setOnAction(e -> reloadAffectations());
        tfChercherVehicule.setOnAction(e -> reloadAffectations()); // Enter dans le champ

        btnVoirAgent.setOnAction(e -> {
            AssignmentRow row = tabListeAffectations.getSelectionModel().getSelectedItem();
            if (row == null) { showError("Sélectionne une affectation."); return; }

            showListeAgents();   // affiche l'écran
            reloadAgents();      // garantit que la liste est remplie
            selectAgentById(row.agentId());
        });

        btnVoirVehicule.setOnAction(e -> {
            AssignmentRow row = tabListeAffectations.getSelectionModel().getSelectedItem();
            if (row == null) { showError("Sélectionne une affectation."); return; }

            showListeVehicules();
            reloadVehicles();    // garantit la liste
            selectVehicleById(row.vehicleId());
        });

        btnAccepter.setOnAction(e -> {
            try { onAccepterAffectation(); }
            catch (Exception ex) { showError(ex.getMessage()); }
        });

        btnCloturer.setOnAction(e -> {
            try { onRefuserAffectation(); }
            catch (Exception ex) { showError(ex.getMessage()); }
        });

        reloadAffectations();
    }

    private void reloadAffectations() {
        try {
            String q = tfChercherVehicule.getText() == null ? "" : tfChercherVehicule.getText().trim();
            affectations.setAll(assignmentDao.findRowsForUi(q));
        } catch (Exception e) {
            showError("Impossible de charger les affectations: " + e.getMessage());
        }
    }

    private void selectAgentById(long id) {
        for (Agent a : agents) {
            if (a.id() == id) {
                tabAgents.getSelectionModel().select(a);
                tabAgents.scrollTo(a);
                return;
            }
        }
    }

    private void selectVehicleById(long id) {
        for (Vehicle v : vehicles) {
            if (v.id() == id) {
                tabVehicules.getSelectionModel().select(v);
                tabVehicules.scrollTo(v);
                return;
            }
        }
    }

    private void onAccepterAffectation() throws Exception {
        AssignmentRow row = tabListeAffectations.getSelectionModel().getSelectedItem();
        if (row == null) { showError("Sélectionne une affectation."); return; }

        boolean ok = assignmentDao.accept(row.assignmentId());
        if (!ok) { showError("Affectation déjà traitée (ou pas en attente)."); return; }

        vehicleDao.updateStatus(row.vehicleId(), VehicleStatus.AFFECTE);

        reloadAffectations();
        reloadVehicles();
    }

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



    // --- CALENDAR ---
    @FXML
    private void ouvrirCalendrier() {

        if (calendarRoot == null) {
            calendarRoot = creerCalendarView();
            calendarContainer.getChildren().add(calendarRoot);
        }

        paneDashboardVehicules.setVisible(false);
        calendarContainer.setVisible(true);

        btnBack.setVisible(false);
        btnHome.setVisible(false);
        svgBack.setVisible(false);
        svgHome.setVisible(false);
    }

    private Parent creerCalendarView() {

        Calendar calendar = new Calendar("Réservations");
        calendar.setStyle(Calendar.Style.STYLE1);
        chargerReservations(calendar);

        CalendarSource source = new CalendarSource("BDD");
        source.getCalendars().add(calendar);

        CalendarView calendarView = new CalendarView();
        calendarView.getCalendarSources().add(source);

        calendarView.setShowAddCalendarButton(false);
        calendarView.setShowPrintButton(false);

        Button btnRetour = new Button("Retour");
        btnRetour.setOnAction(e -> {
            calendarContainer.setVisible(false);
            paneDashboardVehicules.setVisible(true);
            btnBack.setVisible(true);
            btnHome.setVisible(true);
            svgBack.setVisible(true);
            svgHome.setVisible(true);
        });

        BorderPane root = new BorderPane();
        root.setTop(btnRetour);
        root.setCenter(calendarView);

        return root;
    }

    private void chargerReservations(Calendar calendar) {

            Task<List<Assignment>> task = new Task<>() {
                @Override
                protected List<Assignment> call() {
                    try {
                        return new AssignmentDao(conn).findAll();
                    }catch(SQLException ex){
                        System.out.println(ex.getMessage());
                        List<Assignment> lst = new ArrayList<>();
                        return lst;
                    }
                }
            };

            task.setOnSucceeded(e -> {
                for (Assignment a : task.getValue()) {
                    Entry<?> entry = convertirEnEntry(a);
                    calendar.addEntry(entry);
                }
            });

            task.setOnFailed(e -> task.getException().printStackTrace());

            new Thread(task).start();
    }

    private Entry<?> convertirEnEntry(Assignment a) {

        Entry<?> entry = new Entry<>("Réservation #" + a.vehicleId());

        entry.setInterval(
                a.startDate(),
                a.endDate()
        );

//        switch (a.getStatut()) {
//            case "RESERVE" -> entry.setStyle("entry-reserve");
//            case "INDISPONIBLE" -> entry.setStyle("entry-indispo");
//        }

        return entry;
    }

    // --- ETAT VEHICULE ---
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
