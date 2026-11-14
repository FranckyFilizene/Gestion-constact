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

    public AuthService(){
        this.utilisateurDAO = new UtilisateurDAO();
        this.journalDAO = new JournalDAO();
    }

    /**
     * Tente de connecter un utilisateur en vérifiant son login, son statut actif
     * et en comparant le mot de passe en clair avec le hachage BCrypt en base.
     */
    public boolean  connecter(String login, String motDePasseClaire){

        if(login == null || login.trim().isEmpty() || motDePasseClaire == null || motDePasseClaire.trim().isEmpty()){
            throw new ServiceException("Veuillez saisir un identifiant et un mot de passe.");
        }

        try{
            //1.recuperer l'user via le DAO par son login
            Utilisateur utilisateur = utilisateurDAO.rechercherParLogin(login);

            if(utilisateur == null){
                throw new ServiceException("Echec de connexion : Login inconnu.");
            }

            //2.Verifier si le compte est actif
            if(!utilisateur.isActif()){
                throw new ServiceException("Echec de connexion : Compte Désactivé");
            }

            //3.Verification du mot de passe avec BCrypt
            if(BCrypt.checkpw(motDePasseClaire, utilisateur.getMotDePasseHash())){
                //Ouvrir la session
                Session.getInstance().ouvrirSession(utilisateur);

                // Enregistrer l'action de connexion dans le journal d'audit
                journalDAO.enregistrer(
                        utilisateur.getId(),
                        ActionJournal.CONNEXION,
                        "Utilisateur",
                        utilisateur.getId(),
                        "Connexion réussie de l'utilisateur : " + login
                );

                System.out.println("Connection réussi : Bienvenue " + utilisateur.getNomComplet() + "( " + utilisateur.getRole() + " )" );
                return true;
            }else {
                throw new ServiceException("Échec de connexion : Mot de passe incorrect.");
            }
        } catch (DaoException e) {
            throw new ServiceException("Erreur technique lors de l'authentification : " + e.getMessage(), e);
        }

    }

    public void deconnecter(){
        try{
            if(Session.getInstance().estConnecte()){
                Utilisateur u = Session.getInstance().getUtilisateurConnecte();

                // Tracer la déconnexion dans le journal
                journalDAO.enregistrer(
                        u.getId(),
                        ActionJournal.DECONNEXION, // ou une action spécifique si tu en as une, sinon CONNEXION/MODIFICATION
                        "utilisateur",
                        u.getId(),
                        "Déconnexion de l'utilisateur : " + u.getLogin()
                );
            }
        } catch (DaoException e) {
            System.err.println("Avertissement : Impossible de journaliser la déconnexion : " + e.getMessage());
        }finally {
            // Fermer la session en mémoire
            Session.getInstance().fermerSession();
            System.out.println("Déconnexion effectuée avec succès.");
        }

    }

}
