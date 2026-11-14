package com.contact.service;

import com.contact.dao.JournalDAO;
import com.contact.dao.MaireDAO;
import com.contact.exception.DaoException;
import com.contact.exception.ServiceException;
import com.contact.model.ActionJournal;
import com.contact.model.Maire;
import com.contact.security.Session;

import java.util.List;

public class MaireService {
    private final MaireDAO maireDAO;
    private final JournalDAO journalDAO;

    public MaireService() {
        this.maireDAO = new MaireDAO();
        this.journalDAO = new JournalDAO();
    }

    /**
     * Récupère la liste de tous les maires enregistrés.
     */
    public List<Maire> listerTous() {
        try {
            return maireDAO.listerTous();
        } catch (DaoException e) {
            throw new ServiceException("Impossible de récupérer la liste des maires : " + e.getMessage(), e);
        }
    }

    /**
     * Enregistre les informations d'un maire (Réservé à l'ADMIN).
     */
    public boolean creerMaire(Maire maire) {
        if (!Session.getInstance().estAdmin()) {
            throw new ServiceException("Accès refusé : Seul un ADMIN peut enregistrer un profil de maire.");
        }

        if (maire == null || maire.getContactId() == null || maire.getCodeCommune() == null || maire.getCodeCommune().trim().isEmpty()) {
            throw new ServiceException("Validation échouée : Le contact associé et le code de la commune sont obligatoires.");
        }

        try {
            boolean succes = maireDAO.inserer(maire);
            if (succes) {
                var admin = Session.getInstance().getUtilisateurConnecte();
                journalDAO.enregistrer(
                        admin != null ? admin.getId() : null,
                        ActionJournal.CREATION,
                        "maire",
                        0,
                        "Enregistrement du maire pour la commune : " + maire.getCodeCommune()
                );
            }
            return succes;
        } catch (DaoException e) {
            throw new ServiceException("Erreur technique lors de l'enregistrement du maire : " + e.getMessage(), e);
        }
    }
}