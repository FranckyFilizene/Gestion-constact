package com.contact.service;

import com.contact.dao.CentreVoteDAO;
import com.contact.dao.JournalDAO;
import com.contact.exception.DaoException;
import com.contact.exception.ServiceException;
import com.contact.model.ActionJournal;
import com.contact.model.CentreVote;
import com.contact.security.Session;

import java.util.List;

public class CentreVoteService {
    private final CentreVoteDAO centreVoteDAO;
    private final JournalDAO journalDAO;

    public CentreVoteService() {
        this.centreVoteDAO = new CentreVoteDAO();
        this.journalDAO = new JournalDAO();
    }

    /**
     * Récupère tous les centres de vote actifs.
     */
    public List<CentreVote> listerTous() {
        try {
            return centreVoteDAO.listerTous();
        } catch (DaoException e) {
            throw new ServiceException("Impossible de récupérer la liste des centres de vote : " + e.getMessage(), e);
        }
    }

    /**
     * Récupère les centres de vote d'une commune spécifique.
     */
    public List<CentreVote> listerParCommune(Long communeId) {
        if (communeId == null) {
            throw new ServiceException("L'identifiant de la commune est invalide.");
        }
        try {
            return centreVoteDAO.listerParCommune(communeId);
        } catch (DaoException e) {
            throw new ServiceException("Impossible de récupérer les centres de vote pour cette commune : " + e.getMessage(), e);
        }
    }

    /**
     * Crée un nouveau centre de vote (Réservé à l'ADMIN).
     */
    public boolean creerCentreVote(CentreVote cv) {
        if (!Session.getInstance().estAdmin()) {
            throw new ServiceException("Accès refusé : Seul un ADMIN peut créer un centre de vote.");
        }

        if (cv == null || cv.getNom() == null || cv.getNom().trim().isEmpty() || cv.getCommuneId() == null) {
            throw new ServiceException("Validation échouée : Le nom et la commune associée sont obligatoires.");
        }

        // S'assure qu'il soit actif par défaut à la création
        cv.setActif(true);

        try {
            boolean succes = centreVoteDAO.inserer(cv);
            if (succes) {
                var admin = Session.getInstance().getUtilisateurConnecte();
                journalDAO.enregistrer(
                        admin != null ? admin.getId() : null,
                        ActionJournal.CREATION,
                        "centre_vote",
                        cv.getId() != null ? cv.getId() : 0L,
                        "Création du centre de vote : " + cv.getNom() + " (Commune ID: " + cv.getCommuneId() + ")"
                );
            }
            return succes;
        } catch (DaoException e) {
            throw new ServiceException("Erreur technique lors de l'enregistrement du centre de vote : " + e.getMessage(), e);
        }
    }
}