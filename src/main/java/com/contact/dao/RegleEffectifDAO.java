package com.contact.dao;

import com.contact.config.Database;
import com.contact.model.RegleEffectif;
import com.contact.model.EffectifStatut;
import com.contact.model.TypeLocalite;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RegleEffectifDAO {

    /**
     * Initialise automatiquement les règles UNIQUEMENT pour les 3 rôles clés :
     * 1. Maire (1 par COMMUNE)
     * 2. Vigile (3 par FOKOTANY)
     * 3. KMD / Komity olon-kendry (3 par FOKOTANY)
     */
    public void initialiserReglesParDefautSiVide() {
        String checkSql = "SELECT COUNT(*) FROM regle_effectif";

        // Insertion propre des règles pour les 3 rôles clés sans doublons superflus
        String insertSql =
                "INSERT INTO regle_effectif (fonction_id, type_localite, effectif_prevu) " +
                        "SELECT id, niveau_cible, " +
                        "       CASE " +
                        "           WHEN LOWER(libelle) LIKE '%vigile%' THEN 4 " +
                        "           WHEN LOWER(libelle) LIKE '%maire%' THEN 1 " +
                        "           ELSE 3 " + // 3 pour les KMD et Komity Olon-Kendry
                        "       END " +
                        "FROM fonction " +
                        "WHERE actif = TRUE AND (" +
                        "     LOWER(libelle) LIKE '%vigile%' OR " +
                        "     LOWER(libelle) LIKE '%maire%' OR " +
                        "     LOWER(libelle) LIKE '%president (kmd)%' OR " +
                        "     LOWER(libelle) LIKE '%olon-kendry%'" +
                        ") " +
                        "ON CONFLICT (fonction_id, type_localite) DO UPDATE SET effectif_prevu = EXCLUDED.effectif_prevu";

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(checkSql)) {

            if (rs.next() && rs.getInt(1) == 0) {
                stmt.executeUpdate(insertSql);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<EffectifStatut> calculerStatutsEffectifs() {
        List<EffectifStatut> statuts = new ArrayList<>();

        // Requête de synthèse par localité (on groupe par localité pour éviter les doublons)
        String sql =
                "SELECT l.id AS localite_id, l.nom AS nom_localite, l.type_localite, " +
                        "       COUNT(DISTINCT f.id) AS total_postes_prevu, " +
                        "       (SELECT COUNT(*) FROM contact c WHERE c.localite_id = l.id AND c.actif = TRUE) AS effectif_actuel " +
                        "FROM localite l " +
                        "LEFT JOIN fonction f ON f.niveau_cible = l.type_localite AND f.actif = TRUE " +
                        "WHERE l.actif = TRUE AND l.type_localite IN ('COMMUNE', 'FOKOTANY') " +
                        "GROUP BY l.id, l.nom, l.type_localite " +
                        "ORDER BY l.type_localite DESC, l.nom ASC";

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                EffectifStatut statut = new EffectifStatut(
                        rs.getLong("localite_id"),
                        rs.getString("nom_localite"),
                        TypeLocalite.valueOf(rs.getString("type_localite")),
                        0L,
                        "Synthèse globale",
                        rs.getInt("total_postes_prevu"),
                        rs.getInt("effectif_actuel")
                );
                statuts.add(statut);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return statuts;
    }

    public boolean inserer(RegleEffectif regle) {
        String sql = "INSERT INTO regle_effectif (fonction_id, type_localite, effectif_prevu) VALUES (?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, regle.getFonctionId());
            pstmt.setString(2, regle.getTypeLocalite() != null ? regle.getTypeLocalite().name() : null);
            pstmt.setInt(3, regle.getEffectifPrevu());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<RegleEffectif> listerToutes() {
        List<RegleEffectif> liste = new ArrayList<>();
        String sql = "SELECT * FROM regle_effectif";
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                RegleEffectif r = new RegleEffectif();
                r.setId(rs.getLong("id"));
                r.setFonctionId(rs.getLong("fonction_id"));
                String typeStr = rs.getString("type_localite");
                if (typeStr != null) {
                    try {
                        r.setTypeLocalite(TypeLocalite.valueOf(typeStr));
                    } catch (IllegalArgumentException e) {
                        r.setTypeLocalite(null);
                    }
                }
                r.setEffectifPrevu(rs.getInt("effectif_prevu"));
                liste.add(r);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return liste;
    }

    public boolean modifier(RegleEffectif regle) {
        String sql = "UPDATE regle_effectif SET fonction_id = ?, type_localite = ?, effectif_prevu = ? WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, regle.getFonctionId());
            pstmt.setString(2, regle.getTypeLocalite() != null ? regle.getTypeLocalite().name() : null);
            pstmt.setInt(3, regle.getEffectifPrevu());
            pstmt.setLong(4, regle.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean supprimer(long id) {
        String sql = "DELETE FROM regle_effectif WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}