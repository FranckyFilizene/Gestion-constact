package com.contact.dao;

import com.contact.config.Database;
import com.contact.exception.DaoException;
import com.contact.model.Organisation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrganisationDAO {

    /**
     * Récupère toutes les organisations enregistrées, triées par nom.
     */
    public List<Organisation> listerToutes() {
        List<Organisation> organisations = new ArrayList<>();
        String sql = "SELECT * FROM organisation ORDER BY nom ASC";

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                organisations.add(extraireOrganisationDeResultSet(rs));
            }

        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la récupération des organisations : ",e);
        }
        return organisations;
    }

    /**
     * Récupère les organisations situées dans une localité précise.
     */
    public List<Organisation> listerParLocalite(Long localiteId) {
        List<Organisation> organisations = new ArrayList<>();
        String sql = "SELECT * FROM organisation WHERE localite_id = ? ORDER BY nom ASC";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, localiteId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    organisations.add(extraireOrganisationDeResultSet(rs));
                }
            }

        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la récupération des organisations par localité : ",e);
        }
        return organisations;
    }

    /**
     * Insère une nouvelle organisation dans la base de données.
     */
    public boolean inserer(Organisation org) {
        String sql = "INSERT INTO organisation (nom, categorie_id, type_structure, localite_id, actif) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, org.getNom());
            pstmt.setLong(2, org.getCategorieId());
            pstmt.setString(3, org.getTypeStructure());
            pstmt.setLong(4, org.getLocaliteId());
            pstmt.setBoolean(5, org.isActif());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DaoException("Erreur lors de l'insertion de l'organisation : ",e);
        }
    }

    /**
     * Méthode utilitaire privée pour transformer une ligne de la base en objet Organisation.
     */
    private Organisation extraireOrganisationDeResultSet(ResultSet rs) throws SQLException {
        Organisation org = new Organisation();
        org.setId(rs.getLong("id"));
        org.setNom(rs.getString("nom"));
        org.setCategorieId(rs.getLong("categorie_id"));
        org.setTypeStructure(rs.getString("type_structure"));
        org.setLocaliteId(rs.getLong("localite_id"));
        org.setActif(rs.getBoolean("actif"));
        return org;
    }
}