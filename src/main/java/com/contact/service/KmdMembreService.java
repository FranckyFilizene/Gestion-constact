package com.contact.service;

import com.contact.dao.JournalDAO;
import com.contact.dao.KmdMembreDAO;
import com.contact.exception.DaoException;
import com.contact.exception.ServiceException;
import com.contact.model.ActionJournal;
import com.contact.model.KmdMembre;
import com.contact.security.Session;

import java.util.List;

public class KmdMembreService {
    private final KmdMembreDAO kmdMembreDAO;
    private final JournalDAO journalDAO;

    public KmdMembreService() {
        this.kmdMembreDAO = new KmdMembreDAO();
        this.journalDAO = new JournalDAO();
    }

    /**
     * Récupère la liste de tous les membres KMD enregistrés.
     */
    public List<KmdMembre> listerTous() {
        try {
            return kmdMembreDAO.listerTous();
        } catch (DaoException e) {
            throw new ServiceException("Impossible de récupérer la liste des membres KMD : " + e.getMessage(), e);
        }
    }

    /**
     * Enregistre un nouveau membre KMD (Réservé à l'ADMIN).
     */
    public boolean creerMembre(KmdMembre membre) {
        if (!Session.getInstance().estAdmin()) {
            throw new ServiceException("Accès refusé : Seul un ADMIN peut enregistrer un membre KMD.");
        }

        if (membre == null || membre.getContactId() == null || membre.getNiveauKmd() == null || membre.getNiveauKmd().trim().isEmpty()) {
            throw new ServiceException("Validation échouée : Le contact associé et le niveau KMD sont obligatoires.");
        }

        try {
            boolean succes = kmdMembreDAO.inserer(membre);
            if (succes) {
                var admin = Session.getInstance().getUtilisateurConnecte();
                journalDAO.enregistrer(
                        admin != null ? admin.getId() : null,
                        ActionJournal.CREATION,
                        "kmd_membre",
                        membre.getContactId(),
                        "Enregistrement du membre KMD (Niveau: " + membre.getNiveauKmd() + ", Rôle: " + membre.getRoleKmd() + ")"
                );
            }
            return succes;
        } catch (DaoException e) {
            throw new ServiceException("Erreur technique lors de l'enregistrement du membre KMD : " + e.getMessage(), e);
        }
    }
}