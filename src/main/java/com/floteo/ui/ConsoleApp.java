package com.floteo.ui;

import com.floteo.dao.*;
import com.floteo.model.Etat;
import com.floteo.model.VehicleStatus;
import com.floteo.service.AssignmentService;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

/**
 * Interface utilisateur en mode console.
 * Elle ne contient aucune logique métier : elle se contente
 * de lire les entrées de l'utilisateur et d'appeler les DAO
 * ou les services.
 */
public final class ConsoleApp {
    private final Scanner in = new Scanner(System.in);

    private final ServiceDao serviceDao;
    private final AgentDao agentDao;
    private final EtatDao etatDao;
    private final VehicleDao vehicleDao;
    private final AssignmentDao assignmentDao;
    private final AssignmentService assignmentService;

    /**
     * Le point d'entrée de l'UI.
     * On reçoit une Connection JDBC et on instancie
     * tous les DAO et le service métier.
     */
    public ConsoleApp(Connection conn) {
        this.serviceDao = new ServiceDao(conn);
        this.agentDao = new AgentDao(conn);
        this.etatDao = new EtatDao(conn);
        this.vehicleDao = new VehicleDao(conn);
        this.assignmentDao = new AssignmentDao(conn);
        this.assignmentService = new AssignmentService(conn, agentDao, vehicleDao, assignmentDao);
    }

    /**
     * Boucle principale du programme.
     * Affiche le menu, lit le choix, exécute l'action correspondante.
     */
    public void run() {
        while (true) {
            try {
                printMenu();
                int choice = readInt("Choix: ");

                switch (choice) {
                    case 1 -> listServices();
                    case 2 -> createService();
                    case 3 -> listAgents();
                    case 4 -> createAgent();
                    case 5 -> listVehicles();
                    case 6 -> createVehicle();
                    case 7 -> assignVehicle();
                    case 8 -> endAssignment();
                    case 9 -> listAssignments();
                    case 0 -> { System.out.println("Au revoir."); return; }
                    default -> System.out.println("Choix invalide.");
                }
            } catch (Exception e) {
                System.out.println("ERREUR: " + e.getMessage());
            }
            System.out.println();
            try{
                List<Etat> etats = etatDao.findAll();
                System.out.println("qfiupghqeigmbh");
                System.out.println(etats.get(0) + " " +  etats.get(1) + " " + etats.get(2));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Affiche le menu principal.
     */
    private void printMenu() {
        System.out.println("===== Floteo (console) =====");
        System.out.println("1) Lister services");
        System.out.println("2) Créer service");
        System.out.println("3) Lister agents");
        System.out.println("4) Créer agent");
        System.out.println("5) Lister véhicules");
        System.out.println("6) Créer véhicule");
        System.out.println("7) Affecter véhicule à agent");
        System.out.println("8) Terminer affectation (par véhicule)");
        System.out.println("9) Lister affectations");
        System.out.println("0) Quitter");
    }

    // ---- Services ----

    private void listServices() throws Exception {
        var services = serviceDao.findAll();
        if (services.isEmpty()) {
            System.out.println("(Aucun service)");
            return;
        }
        services.forEach(s -> System.out.printf("- #%d %s%n", s.id(), s.name()));
    }

    private void createService() throws Exception {
        String name = readNonEmpty("Nom du service: ");
        var s = serviceDao.create(name);
        System.out.printf("Service créé: #%d %s%n", s.id(), s.name());
    }

    // ---- Agents ----

    private void listAgents() throws Exception {
        var agents = agentDao.findAll();
        if (agents.isEmpty()) {
            System.out.println("(Aucun agent)");
            return;
        }

        agents.forEach(a ->
                System.out.printf(
                        "- #%d [%s] %s %s (serviceId=%d)%n",
                        a.id(),
                        a.matricule(),
                        a.firstName(),
                        a.lastName(),
                        a.serviceId()
                )
        );
    }

    private void createAgent() throws Exception {
        String matricule = readNonEmpty("Matricule: ");
        String first = readNonEmpty("Prénom: ");
        String last = readNonEmpty("Nom: ");
        long serviceId = readLong("Service ID: ");

        var a = agentDao.create(matricule, first, last, serviceId);

        System.out.printf(
                "Agent créé: #%d [%s] %s %s%n",
                a.id(),
                a.matricule(),
                a.firstName(),
                a.lastName()
        );
    }

    // ---- Vehicles ----

    private void listVehicles() throws Exception {
        var vehicles = vehicleDao.findAll();
        if (vehicles.isEmpty()) {
            System.out.println("(Aucun véhicule)");
            return;
        }

        vehicles.forEach(v ->
                System.out.printf(
                        "- #%d %s | %s | %s %s | %d km | acquis le %s | [%s]%n",
                        v.id(),
                        v.plate(),
                        v.type(),
                        v.brand(),
                        v.model(),
                        v.mileage(),
                        v.acquisitionDate(),
                        v.status()
                )
        );
    }


    private void createVehicle() throws Exception {
        String plate = readNonEmpty("Plaque d'immatriculation: ");
        String type = readNonEmpty("Type de véhicule (ex: Voiture, Camion, Moto): ");
        String brand = readNonEmpty("Marque: ");
        String model = readNonEmpty("Modèle: ");
        long etat = Long.parseLong(readNonEmpty("Etat (1, 2, 3, 4) : "));
        int mileage = readInt("Kilométrage: ");
        LocalDate acquisitionDate =
                readDateOrToday("Date d'acquisition (yyyy-MM-dd) [aujourd'hui]: ");

        VehicleStatus status = readStatus(
                "Statut (DISPONIBLE/AFFECTE/ENTRETIEN) [DISPONIBLE]: ",
                VehicleStatus.DISPONIBLE
        );

        var v = vehicleDao.create(
                plate,
                type,
                brand,
                model,
                mileage,
                acquisitionDate,
                status,
                etat
        );

        System.out.printf(
                "Véhicule créé: #%d %s (%s)%n",
                v.id(),
                v.plate(),
                v.type()
        );
    }

    // ---- Assignments ----

    private void assignVehicle() throws Exception {
        long vehicleId = readLong("Vehicle ID: ");
        long agentId = readLong("Agent ID: ");
        LocalDate start = readDateOrToday("Date début (yyyy-MM-dd) [aujourd'hui]: ");
        assignmentService.assignVehicle(vehicleId, agentId, start);
        System.out.println("Affectation créée + véhicule passé en AFFECTE.");
    }

    private void endAssignment() throws Exception {
        long vehicleId = readLong("Vehicle ID: ");
        LocalDate end = readDateOrToday("Date fin (yyyy-MM-dd) [aujourd'hui]: ");
        assignmentService.endActiveAssignmentForVehicle(vehicleId, end);
        System.out.println("Affectation terminée + véhicule passé en DISPONIBLE.");
    }

    private void listAssignments() throws Exception {
        var list = assignmentDao.findAll();
        if (list.isEmpty()) {
            System.out.println("(Aucune affectation)");
            return;
        }
        list.forEach(a -> System.out.printf("- #%d vehicle=%d agent=%d %s -> %s%n",
                a.id(), a.vehicleId(), a.agentId(), a.startDate(), (a.endDate() == null ? "EN COURS" : a.endDate())));
    }

    // ---- Méthodes utilitaires de lecture ----

    /**
     * Lit une chaîne non vide.
     */
    private String readNonEmpty(String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = in.nextLine().trim();
            if (!s.isEmpty()) return s;
            System.out.println("Valeur obligatoire.");
        }
    }

    /**
     * Lit un entier.
     */
    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = in.nextLine().trim();
            try { return Integer.parseInt(s); }
            catch (NumberFormatException e) { System.out.println("Nombre invalide."); }
        }
    }

    /**
     * Lit un long.
     */
    private long readLong(String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = in.nextLine().trim();
            try { return Long.parseLong(s); }
            catch (NumberFormatException e) { System.out.println("Nombre invalide."); }
        }
    }

    /**
     * Lit une date ou retourne aujourd'hui si vide.
     */
    private LocalDate readDateOrToday(String prompt) {
        System.out.print(prompt);
        String s = in.nextLine().trim();
        if (s.isEmpty()) return LocalDate.now();
        return LocalDate.parse(s); // yyyy-MM-dd
    }

    /**
     * Lit un statut de véhicule, avec valeur par défaut.
     */
    private VehicleStatus readStatus(String prompt, VehicleStatus def) {
        while (true) {
            System.out.print(prompt);
            String s = in.nextLine().trim();
            if (s.isEmpty()) return def;
            try { return VehicleStatus.valueOf(s); }
            catch (IllegalArgumentException e) { System.out.println("Statut invalide."); }
        }
    }
}
