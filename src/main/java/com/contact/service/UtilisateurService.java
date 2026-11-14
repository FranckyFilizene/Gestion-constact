package com.contact.service;

import com.contact.dao.JournalDAO;
import com.contact.dao.UtilisateurDAO;
import com.contact.exception.DaoException;
import com.contact.exception.ServiceException;
import com.contact.model.ActionJournal;
import com.contact.model.Role;
import com.contact.model.Utilisateur;
import com.contact.utils.Validator;
import com.contact.security.Session;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;

public class UtilisateurService {
    private final UtilisateurDAO utilisateurDAO;
    private final JournalDAO journalDAO;

    public UtilisateurService() {
        this.utilisateurDAO = new UtilisateurDAO();
        this.journalDAO = new JournalDAO();
    }

    /**
     * Crée un nouvel utilisateur (Réservé à l'ADMIN)
     */
    public boolean creerUtilisateur(String login, String nomComplet, String motDePasseClair, Role role) {
        // Vérification des droits via la session
        if (!Session.getInstance().estAdmin()) {
            throw new ServiceException("Accès refusé : Seul un ADMIN peut créer un utilisateur.");
        }

        if (login == null || login.trim().isEmpty() || motDePasseClair == null || motDePasseClair.isEmpty()) {
            throw new ServiceException("Validation échouée : Le login et le mot de passe sont obligatoires.");
        }

        if (role == null) {
            throw new ServiceException("Validation échouée : Le rôle de l'utilisateur est obligatoire.");
        }

        try {
            // Vérification si le login existe déjà
            if (utilisateurDAO.rechercherParLogin(login.trim()) != null) {
                throw new ServiceException("Erreur : Ce login existe déjà.");
            }

            // Hachage BCrypt du mot de passe dans le SERVICE
            String hash = BCrypt.hashpw(motDePasseClair, BCrypt.gensalt());

            Utilisateur nouvelUtilisateur = new Utilisateur();
            nouvelUtilisateur.setLogin(login.trim());
            nouvelUtilisateur.setNomComplet(nomComplet);
            nouvelUtilisateur.setMotDePasseHash(hash);
            nouvelUtilisateur.setRole(role);
            nouvelUtilisateur.setActif(true);

            boolean succes = utilisateurDAO.inserer(nouvelUtilisateur);

            if (succes) {
                Utilisateur adminActif = Session.getInstance().getUtilisateurConnecte();
                journalDAO.enregistrer(
                        adminActif != null ? adminActif.getId() : null,
                        ActionJournal.CREATION,
                        "utilisateur",
                        0,
                        "Création du compte utilisateur : " + login
                );
            }

            return succes;
        } catch (DaoException e) {
            throw new ServiceException("Erreur technique lors de la création de l'utilisateur : " + e.getMessage(), e);
        }
    }

    public boolean inscrireLecteur(String login, String nomComplet, String motDePasseClair) {
        if (Validator.isNullOrEmpty(login) || Validator.isNullOrEmpty(nomComplet) || Validator.isNullOrEmpty(motDePasseClair)) {
            throw new ServiceException("Tous les champs sont obligatoires pour l'inscription.");
        }

        try {
            if (utilisateurDAO.rechercherParLogin(login.trim()) != null) {
                throw new ServiceException("Cet identifiant est déjà utilisé.");
            }

            String hash = BCrypt.hashpw(motDePasseClair, BCrypt.gensalt());

            Utilisateur nouvelUtilisateur = new Utilisateur();
            nouvelUtilisateur.setLogin(login.trim());
            nouvelUtilisateur.setNomComplet(nomComplet.trim());
            nouvelUtilisateur.setMotDePasseHash(hash);
            nouvelUtilisateur.setRole(Role.LECTEUR);
            nouvelUtilisateur.setActif(true);

            boolean succes = utilisateurDAO.inserer(nouvelUtilisateur);
//            if (succes) {
//                journalDAO.enregistrer(null, ActionJournal.CREATION, "utilisateur", 0, "Auto-inscription lecteur : " + login);
//            }
            return succes;
        } catch (DaoException e) {
            throw new ServiceException("Erreur technique lors de l'inscription : " + e.getMessage(), e);
        }
    }

    public List<Utilisateur> listeTous() {
        try {
            return utilisateurDAO.listerTous();
        } catch (DaoException e) {
            throw new ServiceException("Impossible de récupérer la liste des utilisateurs : " + e.getMessage(), e);
        }
    }
}