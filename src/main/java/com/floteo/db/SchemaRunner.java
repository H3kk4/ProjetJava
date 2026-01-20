package com.floteo.db;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.util.stream.Collectors;

public final class SchemaRunner {
    private SchemaRunner() {}

    public static void ensureSchema(Connection c) throws Exception {
        String sql;
        try (var in = SchemaRunner.class.getClassLoader().getResourceAsStream("sql/001_init.sql")) {
            if (in == null) throw new IllegalStateException("sql/001_init.sql introuvable");
            sql = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))
                    .lines().collect(Collectors.joining("\n"));
        }
        try (Statement st = c.createStatement()) {
            st.execute(sql);
        }
    }
}
