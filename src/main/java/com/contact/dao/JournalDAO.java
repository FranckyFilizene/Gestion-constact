package com.contact.dao;

import com.contact.config.Database;
import com.contact.exception.DaoException;
import com.contact.model.ActionJournal;
import com.contact.model.JournalEntry;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class JournalDAO {

    /**
     * Enregistre une action dans le journal d'audit.
     */
    public void enregistrer(Long utilisateurId, ActionJournal action, String tableCible, long idCible, String details) {
        String sql = "INSERT INTO journal (utilisateur_id, action, table_cible, id_cible, details, date_action) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (utilisateurId != null) {
                pstmt.setLong(1, utilisateurId);
            } else {
                pstmt.setNull(1, Types.BIGINT);
            }

            // Conversion de l'Enum en String pour PostgreSQL
            pstmt.setString(2, action != null ? action.name() : null);
            pstmt.setString(3, tableCible);
            pstmt.setLong(4, idCible);
            pstmt.setString(5, details);
            pstmt.setObject(6, LocalDateTime.now());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new DaoException("Erreur lors de l'enregistrement dans le journal : " ,e);
        }
    }

    /**
     * Récupère la liste de tous les journaux d'audit, du plus récent au plus ancien.
     */
    public List<JournalEntry> listerTous() {
        List<JournalEntry> liste = new ArrayList<>();
        String sql = "SELECT * FROM journal ORDER BY date_action DESC";

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                JournalEntry j = new JournalEntry();
                j.setId(rs.getLong("id"));

                long utilId = rs.getLong("utilisateur_id");
                if (rs.wasNull()) {
                    j.setUtilisateurId(null);
                } else {
                    j.setUtilisateurId(utilId);
                }

                // Conversion du String de la base en Enum ActionJournal
                String actionStr = rs.getString("action");
                if (actionStr != null) {
                    try {
                        j.setAction(ActionJournal.valueOf(actionStr));
                    } catch (IllegalArgumentException e) {
                        j.setAction(null);
                    }
                }

                j.setTableCible(rs.getString("table_cible"));
                j.setIdCible(rs.getLong("id_cible"));
                j.setDetails(rs.getString("details"));

                Timestamp timestamp = rs.getTimestamp("date_action");
                if (timestamp != null) {
                    j.setDateAction(timestamp.toLocalDateTime());
                }

                liste.add(j);
            }

        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la récupération du journal : ",e);
        }

        return liste;
    }
}