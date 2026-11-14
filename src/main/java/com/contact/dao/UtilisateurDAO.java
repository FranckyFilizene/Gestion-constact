package com.contact.dao;

import com.contact.config.Database;
import com.contact.exception.DaoException;
import com.contact.model.Role;
import com.contact.model.Utilisateur;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UtilisateurDAO {

    /**
     * Insère un utilisateur en base (le mot de passe doit déjà être haché).
     */
    public boolean inserer(Utilisateur u) {
        String sql = "INSERT INTO utilisateur (login, nom_complet, mot_de_passe_hash, role, actif) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, u.getLogin());
            pstmt.setString(2, u.getNomComplet());
            pstmt.setString(3, u.getMotDePasseHash()); // Le hash arrive déjà prêt du service
            pstmt.setString(4, u.getRole().name());
            pstmt.setBoolean(5, u.isActif());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Erreur lors de l'insertion de l'utilisateur : ",e);
        }
    }

    /**
     * Recherche un utilisateur par son login.
     */
    public Utilisateur rechercherParLogin(String login) {
        String sql = "SELECT * FROM utilisateur WHERE login = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, login);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extraireUtilisateurDeResultSet(rs);
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la recherche par login : ",e);
        }
        return null;
    }

    /**
     * Récupère tous les utilisateurs enregistrés.
     */
    public List<Utilisateur> listerTous() {
        List<Utilisateur> utilisateurs = new ArrayList<>();
        String sql = "SELECT * FROM utilisateur ORDER BY nom_complet ASC";

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                utilisateurs.add(extraireUtilisateurDeResultSet(rs));
            }

        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la récupération des utilisateurs : ",e);
        }
        return utilisateurs;
    }

    /**
     * Méthode utilitaire privée pour transformer une ligne de la base en objet Utilisateur.
     */
    private Utilisateur extraireUtilisateurDeResultSet(ResultSet rs) throws SQLException {
        Utilisateur u = new Utilisateur();
        u.setId(rs.getLong("id"));
        u.setLogin(rs.getString("login"));
        u.setNomComplet(rs.getString("nom_complet"));
        u.setMotDePasseHash(rs.getString("mot_de_passe_hash"));
        u.setRole(Role.valueOf(rs.getString("role")));
        u.setActif(rs.getBoolean("actif"));

        Timestamp dateCr = rs.getTimestamp("date_creation");
        if (dateCr != null) {
            u.setDateCreation(dateCr.toLocalDateTime());
        }

        return u;
    }
}