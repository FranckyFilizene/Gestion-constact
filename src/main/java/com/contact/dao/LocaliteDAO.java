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
     * Récupère uniquement la liste des 9 districts de la région Atsimo Andrefana (exclut les limitrophes).
     */
    public List<Localite> listerDistrictsRaa() {
        List<Localite> districts = new ArrayList<>();
        // On cible les districts dont le parent (la région) s'appelle "Atsimo Andrefana"
        String sql = "SELECT d.* FROM localite d " +
                "JOIN localite r ON d.parent_id = r.id " +
                "WHERE d.type_localite = 'DISTRICT' AND d.actif = TRUE " +
                "AND unaccent(lower(r.nom)) LIKE '%atsimo%andrefana%' " +
                "ORDER BY d.nom ASC";

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                districts.add(mapResultSetToLocalite(rs));
            }
        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la récupération des districts de l'Atsimo-Andrefana : ", e);
        }

        return districts;
    }

    /**
     * Récupère uniquement la liste de tous les districts (général).
     */
    public List<Localite> listerDistricts() {
        List<Localite> districts = new ArrayList<>();
        String sql = "SELECT * FROM localite WHERE type_localite = 'DISTRICT' AND actif = TRUE ORDER BY nom ASC";

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                districts.add(mapResultSetToLocalite(rs));
            }
        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la récupération des districts : ", e);
        }

        return districts;
    }

    /**
     * Trouve une localité par son nom sous un parent donné, ou la crée automatiquement.
     */
    public long trouverOuCreerLocalite(String nom, String typeLocalite, Long parentId) {
        if (nom == null || nom.trim().isEmpty()) {
            return parentId;
        }

        String nomNettoye = nom.trim();
        String sqlSelect = "SELECT id FROM localite " +
                "WHERE parent_id = ? AND type_localite = ? " +
                "AND unaccent(lower(trim(nom))) = unaccent(lower(trim(?)))";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlSelect)) {

            pstmt.setLong(1, parentId);
            pstmt.setString(2, typeLocalite);
            pstmt.setString(3, nomNettoye);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("id");
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la recherche de la localité (" + nomNettoye + ") : ", e);
        }

        String sqlInsert = "INSERT INTO localite (nom, type_localite, parent_id, actif) VALUES (?, ?, ?, TRUE)";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, nomNettoye);
            pstmt.setString(2, typeLocalite);
            pstmt.setLong(3, parentId);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Échec de la création de la localité.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                } else {
                    throw new SQLException("Échec de la création, aucun ID généré.");
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la création automatique de la localité (" + nomNettoye + ") : ", e);
        }
    }

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
            throw new DaoException("Erreur lors de la récupération des localités : ", e);
        }

        return localites;
    }

    public List<Localite> listerParParent(Long parentId) {
        List<Localite> localites = new ArrayList<>();
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
            throw new DaoException("Erreur lors de la recherche des sous-localités : ", e);
        }

        return localites;
    }

    public boolean insertLocalite(Localite l) {
        String sql = "INSERT INTO localite (nom, type_localite, parent_id, code_source, actif) VALUES (?, ?, ?, ?, TRUE)";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, l.getNom());
            pstmt.setString(2, l.getTypeLocalite().name());

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
            throw new DaoException("Erreur lors de l'insertion de la localité (" + l.getNom() + ") : ", e);
        }
    }

    public List<Localite> findByType(TypeLocalite type) {
        List<Localite> localites = new ArrayList<>();
        String sql = "SELECT * FROM localite WHERE type_localite = ? AND actif = TRUE ORDER BY nom";

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

        localite.setCodeDistrict(rs.getString("code_district"));
        localite.setCodeCommune(rs.getString("code_commune"));
        localite.setCodeFokotany(rs.getString("code_fokotany"));
        localite.setTypeCom(rs.getString("type_com"));
        localite.setCategorieCom(rs.getString("categorie_com"));

        return localite;
    }

    public Localite trouverParId(long id) {
        String sql = "SELECT * FROM localite WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToLocalite(rs);
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la recherche de la localité par ID (" + id + ") : ", e);
        }
        return null;
    }
}