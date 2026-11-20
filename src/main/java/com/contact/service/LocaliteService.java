package com.contact.service;

import com.contact.dao.JournalDAO;
import com.contact.dao.LocaliteDAO;
import com.contact.exception.DaoException;
import com.contact.exception.ServiceException;
import com.contact.model.ActionJournal;
import com.contact.model.Localite;
import com.contact.model.TypeLocalite;
import com.contact.security.Session;

import java.util.List;

public class LocaliteService {
    private final LocaliteDAO localiteDAO;
    private final JournalDAO journalDAO;

    public LocaliteService() {
        this.localiteDAO = new LocaliteDAO();
        this.journalDAO = new JournalDAO();
    }

    /**
     * Récupère toutes les localités de la base.
     */
    public List<Localite> listeToutes() {
        try {
            return localiteDAO.listerToutes();
        } catch (DaoException e) {
            throw new ServiceException("Impossible de récupérer la liste des localités : " + e.getMessage(), e);
        }
    }

    /**
     * Récupère les sous-localités d'un parent (ex: les communes d'un district).
     * Permet le chargement en cascade dans l'interface.
     */
    public List<Localite> listerParParent(Long parentId) {
        if (parentId == null) {
            throw new ServiceException("L'identifiant du parent est invalide.");
        }
        try {
            return localiteDAO.listerParParent(parentId);
        } catch (DaoException e) {
            throw new ServiceException("Impossible de récupérer les sous-localités : " + e.getMessage(), e);
        }
    }

    /**
     * Récupère toutes les localités d'un type précis (ex: toutes les COMMUNES).
     */
    public List<Localite> listerParType(TypeLocalite type) {
        if (type == null) {
            throw new ServiceException("Le type de localité spécifié est invalide.");
        }
        try {
            return localiteDAO.findByType(type);
        } catch (DaoException e) {
            throw new ServiceException("Impossible de récupérer les localités par type : " + e.getMessage(), e);
        }
    }

    /**
     * Crée une nouvelle localité (Réservé à l'admin).
     */
    public boolean creerLocalite(Localite localite) {
        // Vérification stricte des droits via la session (Seul l'admin peut modifier)
        if (!Session.getInstance().estAdmin()) {
            throw new ServiceException("Accès refusé : Seul un ADMIN peut créer ou modifier les localités.");
        }

        if (localite == null) {
            throw new ServiceException("Les données de la localité sont invalides.");
        }

        // Validation métier basique
        if (localite.getNom() == null || localite.getNom().trim().isEmpty()) {
            throw new ServiceException("Validation échouée : Le nom de la localité est obligatoire.");
        }

        if (localite.getTypeLocalite() == null) {
            throw new ServiceException("Validation échouée : Le type de localité est obligatoire.");
        }

        try {
            // Appel au DAO pour l'insertion
            boolean succes = localiteDAO.insertLocalite(localite);

            // Journalisation en cas de succès
            // Journalisation en cas de succès
            if (succes) {
                var adminActif = Session.getInstance().getUtilisateurConnecte();
                journalDAO.enregistrer(
                        adminActif != null ? adminActif.getId() : null,
                        ActionJournal.CREATION,
                        "localite",
                        localite.getId() != null ? localite.getId() : 0L,
                        "Création de la localité : " + localite.getNom() + " [Type: " + localite.getTypeLocalite() + "]"
                );
            }

            return succes;
        } catch (DaoException e) {
            throw new ServiceException("Erreur technique lors de l'enregistrement de la localité : " + e.getMessage(), e);
        }
    }
}