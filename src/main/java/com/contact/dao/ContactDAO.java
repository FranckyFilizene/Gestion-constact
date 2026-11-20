package com.contact.dao;

import com.contact.config.Database;
import com.contact.exception.DaoException;
import com.contact.model.Contact;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ContactDAO {

    /**
     * Récupère tous les contacts enregistrés dans le système.
     */
    public List<Contact> listerTous() {
        List<Contact> contacts = new ArrayList<>();
        String sql = "SELECT * FROM contact WHERE actif = TRUE ORDER BY nom ASC";

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                contacts.add(extraireContactDeResultSet(rs));
            }

        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la récupération des contacts : ",e);
        }
        return contacts;
    }

    /**
     * Insère un nouveau contact dans la base de données.
     */
    public boolean inserer(Contact contact) {
        String sqlContact = "INSERT INTO contact (nom, prenom, fonction_id, organisation_id, localite_id, telephone_1, telephone_2, email, observation, motif, actif) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, TRUE)";

        String sqlJournal = "INSERT INTO journal (utilisateur_id, action, tabel_cible, id_cible, details) VALUES (?, 'CREATION', 'contact', ?, ?)";

        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);

            long contactId;

            //1.Insertion du contact
            try(PreparedStatement pstmt = conn.prepareStatement(sqlContact, Statement.RETURN_GENERATED_KEYS)){
                pstmt.setString(1,contact.getNom().toLowerCase());
                pstmt.setString(2,contact.getPrenom());
                pstmt.setLong(3, contact.getFonctionId());

                //Gestion de l'organisation optionnelle (peut etre null)
                if(contact.getOrganisationId() != null){
                    pstmt.setLong(4,contact.getOrganisationId());
                }else{
                    pstmt.setNull(4, Types.BIGINT);
                }

                pstmt.setLong(5, contact.getLocaliteId());
                pstmt.setString(6, contact.getTelephone1());
                pstmt.setString(7, contact.getTelephone2());
                pstmt.setString(8, contact.getEmail());
                pstmt.setString(9, contact.getObservation());
                pstmt.setString(10, contact.getMotif());

                int affectedRows = pstmt.executeUpdate();
                if(affectedRows == 0){
                    throw new SQLException("Échec de la création du contact, aucune ligne insérée.");
                }

                //Recupration de l'id generer pour le contact
                try(ResultSet generatedKeys = pstmt.getGeneratedKeys()){
                    if(generatedKeys.next()){
                        contactId = generatedKeys.getLong(1);
                    }else {
                        throw new SQLException("Échec de la création, aucun ID généré.");
                    }
                }

                conn.commit();
                return true;
            }
        } catch (SQLException e) {
            throw new DaoException("Erreur lors de l'insertion du contact : ",e);
        }
    }

    /**
     * Met à jour un contact existant dans la base de données.
     */
    public boolean modifier(Contact contact) {
        String sql = "UPDATE contact SET nom = ?, prenom = ?, fonction_id = ?, organisation_id = ?, " +
                "localite_id = ?, telephone_1 = ?, telephone_2 = ?, email = ?, observation = ?, motif = ?, " +
                "date_modification = CURRENT_TIMESTAMP WHERE id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, contact.getNom());
            pstmt.setString(2, contact.getPrenom());
            pstmt.setLong(3, contact.getFonctionId());

            if (contact.getOrganisationId() != null) {
                pstmt.setLong(4, contact.getOrganisationId());
            } else {
                pstmt.setNull(4, Types.BIGINT);
            }

            pstmt.setLong(5, contact.getLocaliteId());
            pstmt.setString(6, contact.getTelephone1());
            pstmt.setString(7, contact.getTelephone2());
            pstmt.setString(8, contact.getEmail());
            pstmt.setString(9, contact.getObservation());
            pstmt.setString(10, contact.getMotif());
            pstmt.setLong(11, contact.getId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la modification du contact : ", e);
        }
    }

    /**
     * Supprime (ou désactive logiquement) un contact par son ID.
     */
    public boolean supprimer(long id) {
        String sql = "UPDATE contact SET actif = FALSE WHERE id = ?";
        // Ou si tu préfères une suppression physique directe : "DELETE FROM contact WHERE id = ?"

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la suppression du contact : ", e);
        }
    }

    /**
     * Méthode utilitaire privée pour transformer une ligne de la base en objet Contact.
     */
    private Contact extraireContactDeResultSet(ResultSet rs) throws SQLException {
        Contact c = new Contact();
        c.setId(rs.getLong("id"));
        c.setNom(rs.getString("nom"));
        c.setPrenom(rs.getString("prenom"));
        c.setFonctionId(rs.getLong("fonction_id"));

        long orgId = rs.getLong("organisation_id");
        if (rs.wasNull()) {
            c.setOrganisationId(null);
        } else {
            c.setOrganisationId(orgId);
        }

        c.setLocaliteId(rs.getLong("localite_id"));
        c.setTelephone1(rs.getString("telephone_1"));
        c.setTelephone2(rs.getString("telephone_2"));
        c.setEmail(rs.getString("email"));
        c.setObservation(rs.getString("observation"));
        c.setMotif(rs.getString("motif"));
        c.setActif(rs.getBoolean("actif"));

        // Gestion des dates de création / modification si présentes dans le modèle
        Timestamp dateCr = rs.getTimestamp("date_creation");
        if (dateCr != null) {
            c.setDateCreation(dateCr.toLocalDateTime());
        }
        Timestamp dateMod = rs.getTimestamp("date_modification");
        if (dateMod != null) {
            c.setDateModification(dateMod.toLocalDateTime());
        }

        return c;
    }
}