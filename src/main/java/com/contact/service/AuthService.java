package com.contact.service;

import com.contact.dao.JournalDAO;
import com.contact.dao.UtilisateurDAO;
import com.contact.exception.DaoException;
import com.contact.exception.ServiceException;
import com.contact.model.Utilisateur;
import com.contact.model.ActionJournal;
import com.contact.security.Session;
import org.mindrot.jbcrypt.BCrypt;

public class AuthService {
    private final UtilisateurDAO utilisateurDAO;
    private final JournalDAO journalDAO;

    public AuthService() {
        this.utilisateurDAO = new UtilisateurDAO();
        this.journalDAO = new JournalDAO();
    }

    public boolean connecter(String login, String motDePasseClaire) {
        if (login == null || login.trim().isEmpty() || motDePasseClaire == null || motDePasseClaire.trim().isEmpty()) {
            throw new ServiceException("Veuillez saisir un identifiant et un mot de passe.");
        }

        try {
            Utilisateur utilisateur = utilisateurDAO.rechercherParLogin(login);

            if (utilisateur == null) {
                throw new ServiceException("Echec de connexion : Login inconnu.");
            }

            if (!utilisateur.isActif()) {
                throw new ServiceException("Echec de connexion : Compte Désactivé");
            }

            if (BCrypt.checkpw(motDePasseClaire, utilisateur.getMotDePasseHash())) {
                Session.getInstance().ouvrirSession(utilisateur);

                journalDAO.enregistrer(
                        utilisateur.getId(),
                        ActionJournal.CONNEXION,
                        "Utilisateur",
                        utilisateur.getId(),
                        "Connexion réussie de l'utilisateur : " + login
                );

                System.out.println("Connection réussi : Bienvenue " + utilisateur.getNomComplet() + "( " + utilisateur.getRole() + " )");
                return true;
            } else {
                throw new ServiceException("Échec de connexion : Mot de passe incorrect.");
            }
        } catch (DaoException e) {
            throw new ServiceException("Erreur technique lors de l'authentification : " + e.getMessage(), e);
        }
    }

    public void deconnecter() {
        try {
            if (Session.getInstance().estConnecte()) {
                Utilisateur u = Session.getInstance().getUtilisateurConnecte();

                journalDAO.enregistrer(
                        u.getId(),
                        ActionJournal.DECONNEXION,
                        "utilisateur",
                        u.getId(),
                        "Déconnexion de l'utilisateur : " + u.getLogin()
                );
            }
        } catch (DaoException e) {
            System.err.println("Avertissement : Impossible de journaliser la déconnexion : " + e.getMessage());
        } finally {
            Session.getInstance().fermerSession();
            System.out.println("Déconnexion effectuée avec succès.");
        }
    }
}