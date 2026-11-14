package com.contact.dao;

import com.contact.config.Database;
import com.contact.exception.DaoException;
import com.contact.model.CentreVote;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CentreVoteDAO {

    /**
     * Récupère tous les centres de vote enregistrés, triés par nom.
     */
    public List<CentreVote> listerTous() {
        List<CentreVote> centres = new ArrayList<>();
        String sql = "SELECT * FROM centre_vote WHERE actif = TRUE ORDER BY nom ASC";

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                centres.add(extraireCentreVoteDeResultSet(rs));
            }

        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la récupération des centres de vote : ",e);
        }
        return centres;
    }

    /**
     * Récupère les centres de vote rattachés à une commune spécifique.
     */
    public List<CentreVote> listerParCommune(Long communeId) {
        List<CentreVote> centres = new ArrayList<>();
        String sql = "SELECT * FROM centre_vote WHERE commune_id = ? AND actif = TRUE ORDER BY nom ASC";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, communeId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    centres.add(extraireCentreVoteDeResultSet(rs));
                }
            }

        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la récupération des centres de vote par commune : ",e);
        }
        return centres;
    }

    /**
     * Insère un nouveau centre de vote dans la base de données.
     */
    public boolean inserer(CentreVote cv) {
        String sql = "INSERT INTO centre_vote (nom, commune_id, fokotany_id, nombre_bureaux, nombre_electeurs, observation, actif) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, cv.getNom());

            // commune_id (obligatoire)
            pstmt.setLong(2, cv.getCommuneId());

            // fokotany_id (peut être null)
            if (cv.getFokotanyId() == null) {
                pstmt.setNull(3, Types.BIGINT);
            } else {
                pstmt.setLong(3, cv.getFokotanyId());
            }

            if (cv.getNombreBureaux() == null) {
                pstmt.setNull(4, Types.SMALLINT);
            } else {
                pstmt.setInt(4, cv.getNombreBureaux());
            }

            if (cv.getNombreElecteur() == null) {
                pstmt.setNull(5, Types.INTEGER);
            } else {
                pstmt.setInt(5, cv.getNombreElecteur());
            }

            pstmt.setString(6, cv.getObservation());
            pstmt.setBoolean(7, cv.getActif());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DaoException("Erreur lors de l'insertion du centre de vote : " ,e);
        }
    }

    /**
     * Méthode utilitaire privée pour transformer une ligne de la base en objet CentreVote.
     */
    private CentreVote extraireCentreVoteDeResultSet(ResultSet rs) throws SQLException {
        CentreVote cv = new CentreVote();
        cv.setId(rs.getLong("id"));
        cv.setNom(rs.getString("nom"));

        cv.setCommuneId(rs.getLong("commune_id"));

        long fokId = rs.getLong("fokotany_id");
        if (rs.wasNull()) {
            cv.setFoktanyId(null);
        } else {
            cv.setFoktanyId(fokId);
        }

        int nbBureaux = rs.getInt("nombre_bureaux");
        if (rs.wasNull()) {
            cv.setNombreBureaux(null);
        } else {
            cv.setNombreBureaux(nbBureaux);
        }

        int nbElecteurs = rs.getInt("nombre_electeurs");
        if (rs.wasNull()) {
            cv.setNombreElecteur(null);
        } else {
            cv.setNombreElecteur(nbElecteurs);
        }

        cv.setObservation(rs.getString("observation"));
        cv.setActif(rs.getBoolean("actif"));

        Timestamp dateCr = rs.getTimestamp("date_creation");
        if (dateCr != null) {
            cv.setDateCreation(dateCr.toLocalDateTime());
        }

        return cv;
    }
}