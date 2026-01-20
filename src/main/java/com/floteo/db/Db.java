package com.floteo.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class Db {
    private Db() {}

    public static Connection connect() throws SQLException {
        String host = "ep-rough-paper-absi3kvo-pooler.eu-west-2.aws.neon.tech";
        String db   = "neondb";
        String user = "neondb_owner";
        String pass = "npg_rfCc4atdgUJ0";

        String url = "jdbc:postgresql://" + host + ":5432/" + db
                + "?sslmode=require&channel_binding=require";

        Properties props = new Properties();
        props.setProperty("user", user);
        props.setProperty("password", pass);

        return DriverManager.getConnection(url, props);
    }

}