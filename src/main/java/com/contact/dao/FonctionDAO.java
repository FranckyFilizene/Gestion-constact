package com.contact.dao;

import com.contact.config.Database;
import com.contact.exception.DaoException;
import com.contact.model.Fonction;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FonctionDAO {

    /**
     * Récupère toutes les fonctions enregistrées, triées par libellé.
     */
    public List<Fonction> listerToutes() {
        List<Fonction> fonctions = new ArrayList<>();
        String sql = "SELECT * FROM fonction ORDER BY libelle ASC";

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                fonctions.add(extraireFonctionDeResultSet(rs));
            }

        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la récupération des fonctions : ",e);
        }
        return fonctions;
    }

    /**
     * Récupère les fonctions associées à une catégorie spécifique (ex: les fonctions liées aux maires ou aux KMD).
     */
    public List<Fonction> listerParCategorie(Long categorieId) {
        List<Fonction> fonctions = new ArrayList<>();
        String sql = "SELECT * FROM fonction WHERE categorie_id = ? ORDER BY libelle ASC";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, categorieId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    fonctions.add(extraireFonctionDeResultSet(rs));
                }
            }

        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la récupération des fonctions par catégorie : ",e);
        }
        return fonctions;
    }

    /**
     * Insère une nouvelle fonction dans la base de données.
     */
    public boolean inserer(Fonction f) {
        String sql = "INSERT INTO fonction (libelle, categorie_id, niveau_cible, actif) VALUES (?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, f.getLibelle());
            pstmt.setLong(2, f.getCategorieId());
            pstmt.setString(3, f.getNiveauCible()); // Ex: REGION, DISTRICT, COMMUNE, FOKOTANY
            pstmt.setBoolean(4, f.isActif());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DaoException("Erreur lors de l'insertion de la fonction : ",e);
        }
    }

    /**
     * Méthode utilitaire privée pour transformer une ligne de la base en objet Fonction.
     */
    private Fonction extraireFonctionDeResultSet(ResultSet rs) throws SQLException {
        Fonction f = new Fonction();
        f.setId(rs.getLong("id"));
        f.setLibelle(rs.getString("libelle"));
        f.setCategorieId(rs.getLong("categorie_id"));
        f.setNiveauCible(rs.getString("niveau_cible"));
        f.setActif(rs.getBoolean("actif"));
        return f;
    }
}