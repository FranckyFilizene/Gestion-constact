package com.contact.dao;

import com.contact.config.Database;
import com.contact.exception.DaoException;
import com.contact.model.KmdMembre;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class KmdMembreDAO {

    /**
     * Récupère tous les membres KMD enregistrés.
     */
    public List<KmdMembre> listerTous() {
        List<KmdMembre> membres = new ArrayList<>();
        String sql = "SELECT * FROM kmd_membre";

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                membres.add(extraireKmdMembreDeResultSet(rs));
            }

        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la récupération des membres KMD : ",e);
        }
        return membres;
    }

    /**
     * Associe ou insère les informations spécifiques d'un membre KMD à un contact existant.
     */
    public boolean inserer(KmdMembre membre) {
        String sql = "INSERT INTO kmd_membre (contact_id, niveau_kmd, role_kmd, numero_ordre) VALUES (?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, membre.getContactId());
            pstmt.setString(2, membre.getNiveauKmd());
            pstmt.setString(3, membre.getRoleKmd());

            if (membre.getNumeroOrdre() == null) {
                pstmt.setNull(4, Types.INTEGER);
            } else {
                pstmt.setInt(4, membre.getNumeroOrdre());
            }

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DaoException("Erreur lors de l'insertion du membre KMD : ",e);
        }
    }

    /**
     * Méthode utilitaire privée pour transformer une ligne de la base en objet KmdMembre.
     */
    private KmdMembre extraireKmdMembreDeResultSet(ResultSet rs) throws SQLException {
        KmdMembre m = new KmdMembre();
        m.setId(rs.getLong("id"));
        m.setContactId(rs.getLong("contact_id"));
        m.setNiveauKmd(rs.getString("niveau_kmd"));
        m.setRoleKmd(rs.getString("role_kmd"));

        int ordre = rs.getInt("numero_ordre");
        if (rs.wasNull()) {
            m.setNumeroOrdre(null);
        } else {
            m.setNumeroOrdre(ordre);
        }

        return cacherOrdreSiBesoin(m);
    }

    private KmdMembre cacherOrdreSiBesoin(KmdMembre m) {
        return m;
    }
}