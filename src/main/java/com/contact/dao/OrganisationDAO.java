package com.contact.dao;

import com.contact.config.Database;
import com.contact.exception.DaoException;
import com.contact.model.Organisation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrganisationDAO {


    public Organisation trouverParId(long id) {
        String sql = "SELECT * FROM organisation WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Organisation org = new Organisation();
                    org.setId(rs.getLong("id"));
                    org.setId(rs.getLong("id"));
                    org.setNom(rs.getString("nom"));
                    org.setCategorieId(rs.getLong("categorie_id"));
                    org.setTypeStructure(rs.getString("type_structure"));
                    org.setLocaliteId(rs.getLong("localite_id"));
                    org.setActif(rs.getBoolean("actif"));
                    return org;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * Trouve une organisation par son nom (insensible aux majuscules, espaces et accents via 'unaccent'),
     * ou la crée automatiquement si elle n'existe pas.
     * Retourne l'ID de l'organisation (ou null si le nom est vide).
     */
    public Long trouverOuCreerOrganisation(String nom, Long categorieId, Long localiteId) {
        if (nom == null || nom.trim().isEmpty()) {
            return null; // L'organisation est optionnelle
        }

        String nomNettoye = nom.trim();

        // 1. Recherche intelligente anti-doublon
        String sqlSelect = "SELECT id FROM organisation " +
                "WHERE unaccent(lower(trim(nom))) = unaccent(lower(trim(?)))";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlSelect)) {

            pstmt.setString(1, nomNettoye);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("id"); // L'organisation existe déjà, on récupère son ID !
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la recherche de l'organisation (" + nomNettoye + ") : ", e);
        }

        // 2. Si elle n'existe pas, on l'insère dynamiquement en base
        String sqlInsert = "INSERT INTO organisation (nom, categorie_id, type_structure, localite_id, actif) VALUES (?, ?, ?, ?, TRUE)";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, nomNettoye);
            pstmt.setLong(2, categorieId);
            pstmt.setString(3, "Structure locale"); // Type par défaut pour les saisies libres
            pstmt.setLong(4, localiteId);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Échec de la création de l'organisation.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1); // Retourne le nouvel ID généré
                } else {
                    throw new SQLException("Échec de la création, aucun ID généré.");
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la création automatique de l'organisation (" + nomNettoye + ") : ", e);
        }
    }


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