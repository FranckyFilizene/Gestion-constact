package com.contact.service;

import com.contact.dao.JournalDAO;
import com.contact.dao.RegleEffectifDAO;
import com.contact.exception.DaoException;
import com.contact.exception.ServiceException;
import com.contact.model.ActionJournal;
import com.contact.model.RegleEffectif;
import com.contact.security.Session;

import java.util.List;

public class RegleEffectifService {
    private final RegleEffectifDAO regleEffectifDAO;
    private final JournalDAO journalDAO;

    public RegleEffectifService() {
        this.regleEffectifDAO = new RegleEffectifDAO();
        this.journalDAO = new JournalDAO();
    }

    /**
     * Récupère toutes les règles d'effectifs.
     */
    public List<RegleEffectif> listerToutes() {
        try {
            return regleEffectifDAO.listerToutes();
        } catch (DaoException e) {
            throw new ServiceException("Impossible de récupérer la liste des règles d'effectifs : " + e.getMessage(), e);
        }
    }

    /**
     * Crée une nouvelle règle d'effectif (Réservé à l'ADMIN).
     */
    public boolean creerRegle(RegleEffectif regle) {
        if (!Session.getInstance().estAdmin()) {
            throw new ServiceException("Accès refusé : Seul un ADMIN peut créer une règle d'effectif.");
        }

        if (regle == null || regle.getTypeLocalite() == null || regle.getEffectifPrevu() < 0) {
            throw new ServiceException("Validation échouée : Le type de localité est obligatoire et l'effectif prévu doit être positif.");
        }

        try {
            boolean succes = regleEffectifDAO.inserer(regle);
            if (succes) {
                var admin = Session.getInstance().getUtilisateurConnecte();
                journalDAO.enregistrer(
                        admin != null ? admin.getId() : null,
                        ActionJournal.CREATION,
                        "regle_effectif",
                        0,
                        "Création règle effectif (Fonction ID: " + regle.getFonctionId() + ", Type: " + regle.getTypeLocalite() + ", Prévu: " + regle.getEffectifPrevu() + ")"
                );
            }
            return succes;
        } catch (DaoException e) {
            throw new ServiceException("Erreur technique lors de la création de la règle d'effectif : " + e.getMessage(), e);
        }
    }

    /**
     * Modifie une règle d'effectif existante (Réservé à l'ADMIN).
     */
    public boolean modifierRegle(RegleEffectif regle) {
        if (!Session.getInstance().estAdmin()) {
            throw new ServiceException("Accès refusé : Seul un ADMIN peut modifier une règle d'effectif.");
        }

        if (regle == null || regle.getTypeLocalite() == null || regle.getEffectifPrevu() < 0) {
            throw new ServiceException("Validation échouée : Données invalides pour la modification.");
        }

        try {
            boolean succes = regleEffectifDAO.modifier(regle);
            if (succes) {
                var admin = Session.getInstance().getUtilisateurConnecte();
                journalDAO.enregistrer(
                        admin != null ? admin.getId() : null,
                        ActionJournal.MODIFICATION,
                        "regle_effectif",
                        regle.getId(),
                        "Modification règle effectif ID " + regle.getId() + " (Nouveau prévu: " + regle.getEffectifPrevu() + ")"
                );
            }
            return succes;
        } catch (DaoException e) {
            throw new ServiceException("Erreur technique lors de la modification de la règle d'effectif : " + e.getMessage(), e);
        }
    }

    /**
     * Supprime une règle d'effectif (Réservé à l'ADMIN).
     */
    public boolean supprimerRegle(long id) {
        if (!Session.getInstance().estAdmin()) {
            throw new ServiceException("Accès refusé : Seul un ADMIN peut supprimer une règle d'effectif.");
        }

        try {
            boolean succes = regleEffectifDAO.supprimer(id);
            if (succes) {
                var admin = Session.getInstance().getUtilisateurConnecte();
                journalDAO.enregistrer(
                        admin != null ? admin.getId() : null,
                        ActionJournal.SUPPRESSION,
                        "regle_effectif",
                        id,
                        "Suppression de la règle d'effectif ID " + id
                );
            }
            return succes;
        } catch (DaoException e) {
            throw new ServiceException("Erreur technique lors de la suppression de la règle d'effectif : " + e.getMessage(), e);
        }
    }
}