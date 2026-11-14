package com.contact.dao;

import com.contact.config.Database;
import com.contact.exception.DaoException;

import java.sql.*;

public class LocaliteSeeder {

    public static void seedDatabase() {
        if (isTableEmpty()) {
            System.out.println("=== DÉBUT DU SEEDING COMPLET DES LOCALITÉS ===");
            try (Connection conn = Database.getConnection()) {
                conn.setAutoCommit(false); // Mode transactionnel

                // 1. Insertion de la Région principale
                String sqlRegion = "INSERT INTO localite (nom, type_localite, parent_id, actif) VALUES (?, 'REGION', NULL, TRUE)";
                long regionId;

                try (PreparedStatement pstmt = conn.prepareStatement(sqlRegion, Statement.RETURN_GENERATED_KEYS)) {
                    pstmt.setString(1, "Atsimo Andrefana");
                    pstmt.executeUpdate();
                    try (ResultSet rs = pstmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            regionId = rs.getLong(1);
                            System.out.println("-> Région insérée : Atsimo Andrefana (ID: " + regionId + ")");
                        } else {
                            throw new SQLException("Échec de l'insertion de la région.");
                        }
                    }
                }

                // 2. Insertion des Districts
                String[] districts = {
                        "Toliara I", "Toliara II", "Betioky Sud",
                        "Ampanihy Ouest", "Morombe", "Ankazoabo",
                        "Sakaraha", "Beroroha", "Benenitra"
                };

                String sqlDistrict = "INSERT INTO localite (nom, type_localite, parent_id, actif) VALUES (?, 'DISTRICT', ?, TRUE)";
                try (PreparedStatement pstmt = conn.prepareStatement(sqlDistrict)) {
                    for (String district : districts) {
                        pstmt.setString(1, district);
                        pstmt.setLong(2, regionId);
                        pstmt.addBatch();
                    }
                    pstmt.executeBatch();
                    System.out.println("-> Les 9 districts ont été insérés.");
                }

                conn.commit(); // Validation finale de la transaction
                System.out.println("=== FIN DU SEEDING AVEC SUCCÈS ===");

            } catch (SQLException e) {
                throw new DaoException("Erreur critique lors du seeding",e);
            }
        } else {
            System.out.println("La table localite contient déjà des données. Seeding ignoré.");
        }
    }

    private static boolean isTableEmpty() {
        String sql = "SELECT COUNT(*) FROM localite";
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1) == 0;
            }
        } catch (SQLException e) {
            throw new DaoException("Erreur : " ,e);
        }
        return false;
    }
}