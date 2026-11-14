package com.contact.service;

import com.contact.dao.ContactDAO;
import com.contact.dao.JournalDAO;
import com.contact.exception.DaoException;
import com.contact.exception.ServiceException;
import com.contact.model.ActionJournal;
import com.contact.model.Contact;
import com.contact.security.Session;

import java.util.List;

public class ContactService {
    private final ContactDAO contactDAO;
    private final JournalDAO journalDAO;

    public ContactService() {
        this.contactDAO = new ContactDAO();
        this.journalDAO = new JournalDAO();
    }

    /**
     * Récupère tous les contacts.
     * Règle de sécurité : Si l'utilisateur est un LECTEUR, on filtre
     * pour masquer les contacts marqués comme sensibles / suspects.
     */
    public List<Contact> listerContacts() {
        List<Contact> contacts;
        try {
            contacts = contactDAO.listerTous();
        } catch (DaoException e) {
            throw new ServiceException("Impossible de récupérer la liste des contacts : " + e.getMessage(), e);
        }

        // Si l'utilisateur connecté est un LECTEUR (pas ADMIN),
        // on s'assure qu'aucun détail sensible (motif) ne fuite par précaution.
        if (!Session.getInstance().estAdmin()) {
            for (Contact c : contacts) {
                if (c.getMotif() != null && !c.getMotif().isEmpty()) {
                    c.setMotif("[CONFIDENTIEL - Réservé aux Administrateurs]");
                }
            }
        }

        return contacts;
    }

    /**
     * Crée un nouveau contact.
     */
    public boolean creerContact(Contact contact) {
        // Vérification de la connexion
        if (!Session.getInstance().estConnecte()) {
            throw new ServiceException("Accès refusé : Vous devez être connecté pour ajouter un contact.");
        }

        if (contact == null || contact.getNom() == null || contact.getNom().trim().isEmpty()) {
            throw new ServiceException("Validation échouée : Le nom du contact est obligatoire.");
        }

        // Si le contact possède un motif (profil sensible), seul un ADMIN a le droit de l'enregistrer
        if (contact.getMotif() != null && !contact.getMotif().trim().isEmpty()) {
            if (!Session.getInstance().estAdmin()) {
                throw new ServiceException("Sécurité : Seul un ADMIN peut enregistrer un motif sur un profil sensible.");
            }
        }

        try {
            boolean succes = contactDAO.inserer(contact);

            if (succes) {
                var utilisateurActif = Session.getInstance().getUtilisateurConnecte();
                journalDAO.enregistrer(
                        utilisateurActif != null ? utilisateurActif.getId() : null,
                        ActionJournal.CREATION,
                        "contact",
                        0,
                        "Création du contact : " + contact.getNom() + " " + (contact.getPrenom() != null ? contact.getPrenom() : "")
                );
            }

            return succes;
        } catch (DaoException e) {
            throw new ServiceException("Erreur technique lors de l'enregistrement du contact : " + e.getMessage(), e);
        }
    }
}