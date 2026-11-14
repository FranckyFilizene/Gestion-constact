package com.contact.dao;

import com.contact.config.Database;
import com.contact.exception.DaoException;
import com.contact.model.Categorie;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.List;
import java.util.ArrayList;

public class CategorieDAO {

    /**
     * Liste toutes les catégories triées par libellé.
     */
    public List<Categorie> listerCategorie() {
        List<Categorie> categories = new ArrayList<>();
        // Correction : Utilisation de "categorie" au singulier (aligné avec la table d'insertion)
        String sql = "SELECT * FROM categorie ORDER BY libelle ASC";

        try (
                Connection conn = Database.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);
        ) {
            while (rs.next()) {
                categories.add(extraireCategorieDeResultSet(rs));
            }
        } catch (SQLException e) {
           throw new DaoException("Erreur lors de la recuperation des categories : ",e);
        }

        return categories;
    }

    /**
     * Ajoute une nouvelle catégorie.
     */
    public boolean inserer(Categorie c) {
        String sql = "INSERT INTO categorie (code, libelle, sensible) VALUES (?, ?, ?)";
        try (
                Connection conn = Database.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
        ) {
            pstmt.setString(1, c.getCode());
            pstmt.setString(2, c.getLibelle());
            pstmt.setBoolean(3, c.isSensible()); // Indiquer si la catégorie est sensible (ex: personne suspecte)

            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            throw new DaoException("Erreur lors de l'insertion des categories : ",e);
        }
    }

    /**
     * Méthode utilitaire pour transformer une ligne du ResultSet en objet Categorie.
     */
    public Categorie extraireCategorieDeResultSet(ResultSet rs) throws SQLException {
        Categorie c = new Categorie();
        c.setId(rs.getLong("id"));
        c.setCode(rs.getString("code"));
        c.setLibelle(rs.getString("libelle"));
        c.setSensible(rs.getBoolean("sensible"));

        return c;
    }
}