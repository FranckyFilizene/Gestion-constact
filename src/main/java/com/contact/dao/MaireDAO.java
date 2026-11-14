package com.contact.dao;

import com.contact.config.Database;
import com.contact.exception.DaoException;
import com.contact.model.Maire;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MaireDAO {

    /**
     * Récupère tous les maires enregistrés.
     */
    public List<Maire> listerTous() {
        List<Maire> maires = new ArrayList<>();
        String sql = "SELECT * FROM maire";

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                maires.add(extraireMaireDeResultSet(rs));
            }

        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la récupération des maires : ",e);
        }
        return maires;
    }

    /**
     * Associe ou insère les informations spécifiques d'un maire à un contact existant.
     */
    public boolean inserer(Maire maire) {
        String sql = "INSERT INTO maire (contact_id, code_commune, type_com, categorie_com, genre, statut_election, observation) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, maire.getContactId());
            pstmt.setString(2, maire.getCodeCommune());
            pstmt.setString(3, maire.getTypeCom());
            pstmt.setString(4, maire.getCategorieCom());
            pstmt.setString(5, maire.getGenre());
            pstmt.setString(6, maire.getStatutElection()); // Ex: Nouveau, Réélu\
            pstmt.setString(7, maire.getObservation());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DaoException("Erreur lors de l'insertion des détails du maire : " ,e);
        }
    }

    /**
     * Méthode utilitaire privée pour transformer une ligne de la base en objet Maire.
     */
    private Maire extraireMaireDeResultSet(ResultSet rs) throws SQLException {
        Maire m = new Maire();
        m.setId(rs.getLong("id"));
        m.setContactId(rs.getLong("contact_id"));
        m.setCodeCommune(rs.getString("code_commune"));
        m.setTypeCom(rs.getString("type_commune"));
        m.setCategorieCom(rs.getString("categorie_commune"));
        m.setGenre(rs.getString("genre"));
        m.setStatutElection(rs.getString("statut_election"));
        return m;
    }
}