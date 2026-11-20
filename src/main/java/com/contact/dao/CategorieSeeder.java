package com.contact.dao;

import com.contact.config.Database;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class CategorieSeeder {

    public static void seedCategories() {
        String sqlCheck = "SELECT COUNT(*) FROM categorie";

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sqlCheck)) {

            if (rs.next() && rs.getInt(1) == 0) {
                System.out.println("=== INITIALISATION DES CATÉGORIES PAR DÉFAUT ===");

                String sqlInsert = "INSERT INTO categorie (code, libelle, sensible) VALUES (?, ?, ?)";

                try (PreparedStatement pstmt = conn.prepareStatement(sqlInsert)) {
                    // Les codes exacts utilisés dans ton code Java et tes scripts !
                    insererCategorie(pstmt, "ADMIN", "Administration", false);
                    insererCategorie(pstmt, "KMD", "KMD (Komity Dina)", false);
                    insererCategorie(pstmt, "MAIRE", "Maires", false);
                    insererCategorie(pstmt, "GEND", "Gendarmerie", false);
                    insererCategorie(pstmt, "POLICE", "Police", false);
                    insererCategorie(pstmt, "MIL", "Militaires", false);
                    insererCategorie(pstmt, "ANN", "Annexe (Limitrophe)", false);
                    insererCategorie(pstmt, "SUSP", "Personnes suspectes", true); // C'elle qui déclenche l'alerte rouge !

                    System.out.println("=== CATÉGORIES INSÉRÉES AVEC SUCCÈS ===");
                }
            }else {
                System.out.println("=== LES CODE CATEGORIE EXISTE DEJA ===");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void insererCategorie(PreparedStatement pstmt, String code, String libelle, boolean sensible) throws SQLException {
        pstmt.setString(1, code);
        pstmt.setString(2, libelle);
        pstmt.setBoolean(3, sensible);
        pstmt.executeUpdate();
    }
}