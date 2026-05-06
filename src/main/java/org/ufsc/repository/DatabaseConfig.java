package org.ufsc.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConfig {
    private static final String URL = "jdbc:sqlite:blockchain_data.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void initialize() {
        String userTable = "CREATE TABLE IF NOT EXISTS users (" +
                "username TEXT PRIMARY KEY, " +
                "password_hash BLOB NOT NULL, " +
                "salt BLOB NOT NULL, " +
                "totp_secret TEXT NOT NULL" +
                ");";

        String blockTable = "CREATE TABLE IF NOT EXISTS blocks (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT NOT NULL, " +
                "iv BLOB NOT NULL, " +
                "encrypted_data BLOB NOT NULL, " +
                "prev_hash TEXT, " +
                "timestamp INTEGER NOT NULL, " +
                "FOREIGN KEY(username) REFERENCES users(username)" +
                ");";

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(userTable);
            stmt.execute(blockTable);
            System.out.println("Banco de dados inicializado com sucesso.");
        } catch (SQLException e) {
            System.err.println("Erro ao inicializar banco: " + e.getMessage());
        }
    }
}