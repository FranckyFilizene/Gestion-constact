package com.contact.dao;

import com.contact.config.Database;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class FonctionSeeder {

    public static void seedFonctions() {
        String sqlCheck = "SELECT COUNT(*) FROM fonction";

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sqlCheck)) {

            if (rs.next() && rs.getInt(1) == 0) {
                System.out.println("=== INITIALISATION DES FONCTIONS OFFICIELLES (SSD - ATSIMO ANDREFANA) ===");

                String sqlInsert = "INSERT INTO fonction (libelle, categorie_id, niveau_cible, actif) " +
                        "VALUES (?, (SELECT id FROM categorie WHERE code = ?), ?, TRUE)";

                try (PreparedStatement pstmt = conn.prepareStatement(sqlInsert)) {

                    // --- 1. PERSONNES SUSPECTES ---
                    inserer(pstmt, "Personne suspecte (Doutant)", "SUSP", "REGION");

                    // --- 2. ADMINISTRATION ---
                    inserer(pstmt, "Chef de District (ADMIN)", "ADMIN", "DISTRICT");
                    inserer(pstmt, "Adjoint au Chef de District (ADMIN)", "ADMIN", "DISTRICT");
                    inserer(pstmt, "Chef d'arrondissement (ADMIN)", "ADMIN", "DISTRICT");
                    inserer(pstmt, "Chef de Fokotany (ADMIN)", "ADMIN", "FOKOTANY");
                    inserer(pstmt, "Adjoint Chef de Fokotany (ADMIN)", "ADMIN", "FOKOTANY");
                    inserer(pstmt, "Vigile (Andrimasom-pokonolona)(ADMIN)", "ADMIN", "FOKOTANY");

                    // --- 3. MAIRES ---
                    inserer(pstmt, "Maire (Maire)", "MAIRE", "COMMUNE");
                    inserer(pstmt, "Adjoint au Maire (Maire)", "MAIRE", "COMMUNE");

                    // --- 4. KOMITY DINA (KMD) ---
                    // Comité Olon-kendry
                    inserer(pstmt, "Hazomanga (comite olon-kendry)", "KMD", "FOKOTANY");
                    inserer(pstmt, "Mpiasa fanjakana (comite olon-kendry)", "KMD", "FOKOTANY");
                    inserer(pstmt, "Mpitondra fivavahana (comite olon-kendry)", "KMD", "FOKOTANY");
                    // Bureau KMD
                    inserer(pstmt, "President (KMD)", "KMD", "FOKOTANY");
                    inserer(pstmt, "Filoha (KMD)", "KMD", "FOKOTANY");
                    inserer(pstmt, "Filoha Lefitra (KMD)", "KMD", "FOKOTANY");
                    inserer(pstmt, "Mpitantsoratra (KMD)", "KMD", "FOKOTANY");
                    inserer(pstmt, "Vaomiera (KMD)", "KMD", "FOKOTANY");
                    inserer(pstmt, "Tresorier (KMD)", "KMD", "FOKOTANY");
                    inserer(pstmt, "Mpanaramaso (KMD)", "KMD", "FOKOTANY");
                    inserer(pstmt, "Mpanolontsaina (KMD)", "KMD", "FOKOTANY");
                    // KMD miandrakitra ny fifidianana
                    inserer(pstmt, "Filoha (KMD miandrakitra ny fifidianana)", "KMD", "FOKOTANY");
                    inserer(pstmt, "Filoha Lefitra (KMD miandrakitra ny fifidianana)", "KMD", "FOKOTANY");
                    inserer(pstmt, "Mpitantsoratra (KMD miandrakitra ny fifidianana)", "KMD", "FOKOTANY");
                    inserer(pstmt, "Vaomiera (KMD miandrakitra ny fifidianana)", "KMD", "FOKOTANY");
                    inserer(pstmt, "Mpanaramaso (KMD miandrakitra ny fifidianana)", "KMD", "FOKOTANY");

                    // --- 5. FORCES DE L'ORDRE & SÉCURITÉ ---
                    inserer(pstmt, "Commandant de Groupement (Gendarmerie)", "GEND", "REGION");
                    inserer(pstmt, "Commandant de Compagnie (Gendarmerie)", "GEND", "DISTRICT");
                    inserer(pstmt, "Commandant de Brigade (Gendarmerie)", "GEND", "COMMUNE");
                    inserer(pstmt, "Chef de Poste Avancé (Gendarmerie)", "GEND", "FOKOTANY");

                    inserer(pstmt, "Commissaire de Police (Police)", "POLICE", "DISTRICT");
                    inserer(pstmt, "Officier de Police (Police)", "POLICE", "DISTRICT");
                    inserer(pstmt, "Chef de Poste de Police (Police)", "POLICE", "COMMUNE");

                    inserer(pstmt, "Commandant de Détachement Militaire (Militaires)", "MIL", "REGION");
                    inserer(pstmt, "Capitaine de Compagnie Militaire (Militaires)", "MIL", "DISTRICT");

                    // --- 6. ANNEXES & LIMITROPHES ---
                    inserer(pstmt, "Directeur Régional (Annexe Limitrophe)", "ANN", "REGION");
                    inserer(pstmt, "Responsable ONC (Annexe Limitrophe)", "ANN", "REGION");
                    inserer(pstmt, "Délégué Zon'olombelona (Droits de l'Homme)", "ANN", "REGION");
                    inserer(pstmt, "Président Association Partenaire (Annexe Limitrophe)", "ANN", "REGION");
                    inserer(pstmt, "Coordinateur Technique (Annexe Limitrophe)", "ANN", "REGION");
                    inserer(pstmt, "Responsable Financier (Annexe Limitrophe)", "ANN", "REGION");

                    System.out.println("=== TOUTES LES FONCTIONS OFFICIELLES ONT ÉTÉ INSÉRÉES AVEC SUCCÈS ===");
                }
            } else {
                System.out.println("Les fonctions existent déjà, seeder ignoré.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void inserer(PreparedStatement pstmt, String libelle, String codeCategorie, String niveau) throws Exception {
        pstmt.setString(1, libelle);
        pstmt.setString(2, codeCategorie);
        pstmt.setString(3, niveau);
        pstmt.executeUpdate();
    }
}