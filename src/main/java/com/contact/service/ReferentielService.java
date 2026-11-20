package com.contact.service;

import com.contact.dao.CategorieDAO;
import com.contact.dao.FonctionDAO;
import com.contact.dao.JournalDAO;
import com.contact.dao.OrganisationDAO;
import com.contact.exception.DaoException;
import com.contact.exception.ServiceException;
import com.contact.model.ActionJournal;
import com.contact.model.Categorie;
import com.contact.model.Fonction;
import com.contact.model.Organisation;
import com.contact.security.Session;

import java.util.List;

public class ReferentielService {
    private final FonctionDAO fonctionDAO;
    private final OrganisationDAO organisationDAO;
    private final CategorieDAO categorieDAO;
    private final JournalDAO journalDAO;

    public ReferentielService() {
        this.fonctionDAO = new FonctionDAO();
        this.organisationDAO = new OrganisationDAO();
        this.categorieDAO = new CategorieDAO();
        this.journalDAO = new JournalDAO();
    }

    // --- GESTION DES FONCTIONS ---

    public List<Fonction> listerToutesFonctions() {
        try {
            return fonctionDAO.listerToutes();
        } catch (DaoException e) {
            throw new ServiceException("Impossible de récupérer la liste des fonctions : " + e.getMessage(), e);
        }
    }

    public List<Fonction> listerFonctionsParCategorie(Long categorieId) {
        if (categorieId == null) {
            throw new ServiceException("L'identifiant de la catégorie est invalide.");
        }
        try {
            return fonctionDAO.listerParCategorie(categorieId);
        } catch (DaoException e) {
            throw new ServiceException("Impossible de récupérer les fonctions pour cette catégorie : " + e.getMessage(), e);
        }
    }

    public boolean creerFonction(Fonction fonction) {
        if (!Session.getInstance().estAdmin()) {
            throw new ServiceException("Accès refusé : Seul un ADMIN peut créer une fonction.");
        }

        if (fonction == null || fonction.getLibelle() == null || fonction.getLibelle().trim().isEmpty()) {
            throw new ServiceException("Validation échouée : Le libellé de la fonction est obligatoire.");
        }

        try {
            boolean succes = fonctionDAO.inserer(fonction);
            if (succes) {
                var admin = Session.getInstance().getUtilisateurConnecte();
                journalDAO.enregistrer(
                        admin != null ? admin.getId() : null,
                        ActionJournal.CREATION,
                        "fonction",
                        0L,
                        "Création de la fonction : " + fonction.getLibelle()
                );
            }
            return succes;
        } catch (DaoException e) {
            throw new ServiceException("Erreur technique lors de la création de la fonction : " + e.getMessage(), e);
        }
    }

    // --- GESTION DES ORGANISATIONS ---

    public List<Organisation> listerToutesOrganisations() {
        try {
            return organisationDAO.listerToutes();
        } catch (DaoException e) {
            throw new ServiceException("Impossible de récupérer la liste des organisations : " + e.getMessage(), e);
        }
    }

    public List<Organisation> listerOrganisationsParLocalite(Long localiteId) {
        if (localiteId == null) {
            throw new ServiceException("L'identifiant de la localité est invalide.");
        }
        try {
            return organisationDAO.listerParLocalite(localiteId);
        } catch (DaoException e) {
            throw new ServiceException("Impossible de récupérer les organisations pour cette localité : " + e.getMessage(), e);
        }
    }

    public boolean creerOrganisation(Organisation org) {
        if (!Session.getInstance().estAdmin()) {
            throw new ServiceException("Accès refusé : Seul un ADMIN peut créer une organisation.");
        }

        if (org == null || org.getNom() == null || org.getNom().trim().isEmpty()) {
            throw new ServiceException("Validation échouée : Le nom de l'organisation est obligatoire.");
        }

        try {
            boolean succes = organisationDAO.inserer(org);
            if (succes) {
                var admin = Session.getInstance().getUtilisateurConnecte();
                journalDAO.enregistrer(
                        admin != null ? admin.getId() : null,
                        ActionJournal.CREATION,
                        "organisation",
                        0L,
                        "Création de l'organisation : " + org.getNom()
                );
            }
            return succes;
        } catch (DaoException e) {
            throw new ServiceException("Erreur technique lors de la création de l'organisation : " + e.getMessage(), e);
        }
    }

    // --- GESTION DES CATEGORIES ---

    public List<Categorie> listerToutesCategories() {
        try {
            return categorieDAO.listerCategorie();
        } catch (DaoException e) {
            throw new ServiceException("Impossible de récupérer la liste des catégories : " + e.getMessage(), e);
        }
    }
}