package com.contact.dao;

import com.contact.config.Database;
import com.contact.exception.DaoException;
import com.contact.model.Localite;
import com.contact.model.TypeLocalite;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LocaliteDAO {

    /**
     * Récupère toutes les localités de la base de données.
     */
    public List<Localite> listerToutes() {
        List<Localite> localites = new ArrayList<>();
        String sql = "SELECT * FROM localite ORDER BY nom ASC";

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                localites.add(mapResultSetToLocalite(rs));
            }
        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la récupération des localités : ",e);
        }

        return localites;
    }

    /**
     * Récupère les enfants d'une localité (ou les racines si parentId est null).
     */
    public List<Localite> listerParParent(Long parentId) {
        List<Localite> localites = new ArrayList<>();
        // Correction SQL : Gestion propre du IS NULL si parentId est null
        String sql = (parentId == null)
                ? "SELECT * FROM localite WHERE parent_id IS NULL ORDER BY nom ASC"
                : "SELECT * FROM localite WHERE parent_id = ? ORDER BY nom ASC";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (parentId != null) {
                pstmt.setLong(1, parentId);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    localites.add(mapResultSetToLocalite(rs));
                }
            }

        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la recherche des sous-localités : ",e);
        }

        return localites;
    }

    /**
     * Insère une nouvelle localité rattachée à un parent.
     */
    public boolean insertLocalite(Localite l) {
        String sql = "INSERT INTO localite (nom, type_localite, parent_id, code_source, actif) VALUES (?, ?, ?, ?, TRUE)";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, l.getNom());
            pstmt.setString(2, l.getTypeLocalite().name());

            // Correction : Utilisation de getParentId() (avec un 'd' minuscule selon les conventions Java)
            if (l.getParentId() == null) {
                pstmt.setNull(3, Types.BIGINT);
            } else {
                pstmt.setLong(3, l.getParentId());
            }

            if (l.getCodeSource() != null) {
                pstmt.setString(4, l.getCodeSource());
            } else {
                pstmt.setNull(4, Types.VARCHAR);
            }

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DaoException("Erreur lors de l'insertion de la localité (" + l.getNom() + ") : ",e);
        }
    }

    /**
     * Récupère toutes les localités d'un type précis et actives.
     */
    public List<Localite> findByType(TypeLocalite type) {
        List<Localite> localites = new ArrayList<>();
        String sql = "SELECT id, nom, type_localite, parent_id, code_source, actif FROM localite WHERE type_localite = ? AND actif = TRUE ORDER BY nom";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, type.name());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    localites.add(mapResultSetToLocalite(rs));
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la recherche par type : " + e.getMessage());
        }

        return localites;
    }

    /**
     * Méthode utilitaire privée pour transformer une ligne ResultSet en objet Localite.
     */
    private Localite mapResultSetToLocalite(ResultSet rs) throws SQLException {
        Localite localite = new Localite();
        localite.setId(rs.getLong("id"));
        localite.setNom(rs.getString("nom"));
        localite.setTypeLocalite(TypeLocalite.valueOf(rs.getString("type_localite")));

        long parentIdVal = rs.getLong("parent_id");
        if (rs.wasNull()) {
            localite.setParentId(null);
        } else {
            localite.setParentId(parentIdVal);
        }

        localite.setCodeSource(rs.getString("code_source"));
        localite.setActif(rs.getBoolean("actif"));

        return localite;
    }
}