package org.ufsc.repository;

import org.ufsc.model.Block;
import org.ufsc.service.CryptoService;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BlockchainRepository {
    public String getLastBlockHash() {
        String sql = "SELECT encrypted_data FROM blocks ORDER BY id DESC LIMIT 1";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return CryptoService.calculateHash(Arrays.toString(rs.getBytes("encrypted_data")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "0";
    }

    public void saveBlock(Block block) {
        String sql = "INSERT INTO blocks(username, iv, encrypted_data, prev_hash, timestamp) VALUES(?,?,?,?,?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, block.getUsername());
            pstmt.setBytes(2, block.getIv());
            pstmt.setBytes(3, block.getEncryptedData());
            pstmt.setString(4, block.getPrevHash());
            pstmt.setLong(5, block.getTimestamp());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Block> getAllBlocks() {
        List<Block> blocks = new ArrayList<>();
        String sql = "SELECT * FROM blocks ORDER BY id ASC";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                blocks.add(new Block(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getBytes("iv"),
                        rs.getBytes("encrypted_data"),
                        rs.getString("prev_hash"),
                        rs.getLong("timestamp")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar blocos: " + e.getMessage());
        }
        return blocks;
    }

    public void corruptBlockData(int blockId) {
        String sql = "UPDATE blocks SET encrypted_data = X'00010203' WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, blockId);
            pstmt.executeUpdate();
            System.out.println("ALERTA: Bloco #" + blockId + " foi corrompido manualmente para teste!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}