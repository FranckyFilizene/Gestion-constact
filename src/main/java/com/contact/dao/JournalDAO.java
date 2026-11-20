package com.contact.dao;

import com.contact.config.Database;
import com.contact.exception.DaoException;
import com.contact.model.ActionJournal;
import com.contact.model.JournalEntry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class JournalDAO {

    /**
     * Enregistre une action sans identifiant cible précis.
     */
    public void enregistrer(Long utilisateurId, ActionJournal action, String tableCible, String details) {
        enregistrer(utilisateurId, action, tableCible, (Long) null, details);
    }

    /**
     * Enregistre une action dans le journal d'audit.
     * Si idCible vaut null ou 0, il est enregistré comme NULL en base (ou 0L si la colonne est NOT NULL).
     */
    public void enregistrer(Long utilisateurId, ActionJournal action, String tableCible, Long idCible, String details) {
        String sql = "INSERT INTO journal (utilisateur_id, action, table_cible, id_cible, details, date_action) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (utilisateurId != null) {
                pstmt.setLong(1, utilisateurId);
            } else {
                pstmt.setNull(1, Types.BIGINT);
            }

            pstmt.setString(2, action != null ? action.name() : null);
            pstmt.setString(3, tableCible);

            if (idCible != null && idCible != 0L) {
                pstmt.setLong(4, idCible);
            } else {
                // Si ta colonne id_cible en PostgreSQL est NOT NULL, remplace par : pstmt.setLong(4, 0L);
                pstmt.setNull(4, Types.BIGINT);
            }

            pstmt.setString(5, details);
            pstmt.setObject(6, LocalDateTime.now());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new DaoException("Erreur lors de l'enregistrement dans le journal : " + e.getMessage(), e);
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
                j.setUtilisateurId(rs.wasNull() ? null : utilId);

                String actionStr = rs.getString("action");
                if (actionStr != null) {
                    try {
                        j.setAction(ActionJournal.valueOf(actionStr));
                    } catch (IllegalArgumentException e) {
                        j.setAction(null);
                    }
                }

                j.setTableCible(rs.getString("table_cible"));

                long cibleId = rs.getLong("id_cible");
                j.setIdCible(rs.wasNull() ? null : cibleId);

                j.setDetails(rs.getString("details"));

                Timestamp timestamp = rs.getTimestamp("date_action");
                if (timestamp != null) {
                    j.setDateAction(timestamp.toLocalDateTime());
                }

                liste.add(j);
            }

        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la récupération du journal : " + e.getMessage(), e);
        }

        return liste;
    }
}