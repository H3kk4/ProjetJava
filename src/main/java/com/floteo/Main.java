package com.floteo;

import com.floteo.db.Db;
import com.floteo.db.SchemaRunner;
import com.floteo.ui.ConsoleApp_non_fonctionnel;

/**
 * Point d'entrée alternatif de l'application (mode console).
 * Initialise la base de données puis lance l'interface en mode texte.
 */
public class Main {

    public static void main(String[] args) throws Exception {
        // Ouverture de la connexion à la base dans un try-with-resources
        // pour garantir sa fermeture automatique à la fin.
        try (var conn = Db.connect()) {

            // Vérifie que le schéma de la base existe (tables, contraintes, etc.)
            // et le crée si nécessaire.
            SchemaRunner.ensureSchema(conn);

            // Lancement de l'application en mode console (version non fonctionnelle)
            new ConsoleApp_non_fonctionnel(conn).run();
        }
    }
}
