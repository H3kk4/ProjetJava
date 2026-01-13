package com.floteo;

import com.floteo.db.Db;
import com.floteo.db.SchemaRunner;
import com.floteo.ui.ConsoleApp;

public class Main {
    public static void main(String[] args) throws Exception {
        try (var conn = Db.connect()) {
            SchemaRunner.ensureSchema(conn);
            new ConsoleApp(conn).run();
        }
    }
}
