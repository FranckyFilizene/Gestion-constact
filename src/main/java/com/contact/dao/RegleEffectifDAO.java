package com.contact.dao;

import com.contact.config.Database;
import com.contact.model.RegleEffectif;
import com.contact.model.TypeLocalite;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RegleEffectifDAO {

    public boolean inserer(RegleEffectif regle) {
        String sql = "INSERT INTO regle_effectif (fonction_id, type_localite, effectif_prevu) VALUES (?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, regle.getFonctionId());
            // On convertit l'Enum en String pour la base de données
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

                // On récupère le texte de la BDD et on le reconvertit en Enum TypeLocalite
                String typeStr = rs.getString("type_localite");
                if (typeStr != null) {
                    try {
                        r.setTypeLocalite(TypeLocalite.valueOf(typeStr));
                    } catch (IllegalArgumentException e) {
                        r.setTypeLocalite(null); // Sécurité si valeur inconnue en BDD)
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