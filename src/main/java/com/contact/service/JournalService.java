package com.contact.service;

import com.contact.dao.JournalDAO;
import com.contact.exception.DaoException;
import com.contact.exception.ServiceException;
import com.contact.model.JournalEntry;
import com.contact.security.Session;

import java.util.List;

public class JournalService {
    private final JournalDAO journalDAO;

    public JournalService() {
        this.journalDAO = new JournalDAO();
    }

    /**
     * Récupère tout l'historique du journal.
     * Réservé exclusivement à l'ADMIN.
     */
    public List<JournalEntry> listerJournal() {
        // Vérification des droits via la Session
        if (!Session.getInstance().estAdmin()) {
            throw new ServiceException("Accès refusé : Seul un ADMIN peut consulter le journal d'activité.");
        }

        try {
            return journalDAO.listerTous();
        } catch (DaoException e) {
            throw new ServiceException("Erreur technique lors de la récupération du journal d'activité : " + e.getMessage(), e);
        }
    }
}