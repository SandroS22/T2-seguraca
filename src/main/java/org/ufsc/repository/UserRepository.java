package org.ufsc.repository;

import org.ufsc.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UserRepository {

    public void saveUser(String username, byte[] passwordHash, byte[] salt, String totpSecret) {
        String sql = "INSERT INTO users(username, password_hash, salt, totp_secret) VALUES(?,?,?,?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setBytes(2, passwordHash);
            pstmt.setBytes(3, salt);
            pstmt.setString(4, totpSecret);
            pstmt.executeUpdate();
            System.out.println("Usuário cadastrado com sucesso!");
        } catch (SQLException e) {
            System.err.println("Erro ao salvar usuário: " + e.getMessage());
        }
    }
}