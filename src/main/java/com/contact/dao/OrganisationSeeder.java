package com.contact.dao;

import com.contact.config.Database;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class OrganisationSeeder {

    public static void seedOrganisations() {
        String sqlCheck = "SELECT COUNT(*) FROM organisation";

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sqlCheck)) {

            if (rs.next() && rs.getInt(1) == 0) {
                System.out.println("=== INITIALISATION DES ORGANISATIONS OFFICIELLES ===");

                // On récupère l'ID de la région Atsimo Andrefana par défaut pour rattacher les structures
                long regionId = 1;
                try (Statement stReg = conn.createStatement();
                     ResultSet rsReg = stReg.executeQuery("SELECT id FROM localite WHERE type_localite = 'REGION' LIMIT 1")) {
                    if (rsReg.next()) {
                        regionId = rsReg.getLong(1);
                    }
                }

                String sqlInsert = "INSERT INTO organisation (nom, categorie_id, type_structure, localite_id, actif) " +
                        "VALUES (?, (SELECT id FROM categorie WHERE code = ?), ?, ?, TRUE)";

                try (PreparedStatement pstmt = conn.prepareStatement(sqlInsert)) {
                    // --- GENDARMERIE (GEND) ---
                    inserer(pstmt, "Groupement de Gendarmerie Atsimo Andrefana", "GEND", "Groupement", regionId);
                    inserer(pstmt, "Compagnie de Gendarmerie Toliara", "GEND", "Compagnie", regionId);
                    inserer(pstmt, "Compagnie de Gendarmerie Sakaraha", "GEND", "Compagnie", regionId);
                    inserer(pstmt, "Compagnie de Gendarmerie Betioky", "GEND", "Compagnie", regionId);
                    inserer(pstmt, "Compagnie de Gendarmerie Ampanihy", "GEND", "Compagnie", regionId);
                    inserer(pstmt, "Compagnie de Gendarmerie Morombe", "GEND", "Compagnie", regionId);
                    inserer(pstmt, "Brigade de Gendarmerie Andranovory", "GEND", "Brigade", regionId);
                    inserer(pstmt, "Brigade de Gendarmerie Andranolava", "GEND", "Brigade", regionId);
                    inserer(pstmt, "Poste Avancé de Gendarmerie Mikoboka", "GEND", "Poste Avancé", regionId);

                    // --- POLICE (POLICE) ---
                    inserer(pstmt, "Commissariat Central de Police Toliara", "POLICE", "Commissariat Central", regionId);
                    inserer(pstmt, "Commissariat de Police Sakaraha", "POLICE", "Commissariat", regionId);
                    inserer(pstmt, "Commissariat de Police Ampanihy", "POLICE", "Commissariat", regionId);
                    inserer(pstmt, "Commissariat de Police Morombe", "POLICE", "Commissariat", regionId);
                    inserer(pstmt, "Poste de Police Beroroha", "POLICE", "Poste de Police", regionId);

                    // --- MILITAIRES (MIL) ---
                    inserer(pstmt, "1er Régiment Militaire RM5 Toliara", "MIL", "Régiment", regionId);
                    inserer(pstmt, "Compagnie Militaire Détachée Sakaraha", "MIL", "Compagnie", regionId);
                    inserer(pstmt, "Compagnie Militaire Détachée Betioky", "MIL", "Compagnie", regionId);

                    // --- ANNEXES & PARTENAIRES (ANN) ---
                    inserer(pstmt, "Direction Régionale de Développement (DRD)", "ANN", "Direction Régionale", regionId);
                    inserer(pstmt, "Office National de Concertation (ONC)", "ANN", "Organisme", regionId);
                    inserer(pstmt, "Commission Régionale Zon'olombelona", "ANN", "Commission", regionId);
                    inserer(pstmt, "Association des Pasteurs et Éleveurs du Sud", "ANN", "Association", regionId);
                    inserer(pstmt, "Cellule Technique Eau & Assainissement", "ANN", "Cellule Technique", regionId);

                    System.out.println("=== ORGANISATIONS INSÉRÉES AVEC SUCCÈS ===");
                }
            } else {
                System.out.println("Les organisations existent déjà, seeder ignoré.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void inserer(PreparedStatement pstmt, String nom, String codeCat, String typeStructure, long localiteId) throws Exception {
        pstmt.setString(1, nom);
        pstmt.setString(2, codeCat);
        pstmt.setString(3, typeStructure);
        pstmt.setLong(4, localiteId);
        pstmt.executeUpdate();
    }
}