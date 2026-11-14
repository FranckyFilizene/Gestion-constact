package com.contact.security;

import com.contact.model.Role;
import com.contact.model.Utilisateur;

public class Session {
    private static Session instance;
    private Utilisateur utilisateurConnecte;

    private Session() {}

    public static Session getInstance() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }

    public void ouvrirSession(Utilisateur utilisateur) {
        this.utilisateurConnecte = utilisateur;
    }

    public void fermerSession() {
        this.utilisateurConnecte = null;
    }

    public boolean estConnecte() {
        return utilisateurConnecte != null;
    }

    public boolean estAdmin() {
        return estConnecte() && utilisateurConnecte.getRole() == Role.ADMIN;
    }

    public Utilisateur getUtilisateurConnecte() {
        return utilisateurConnecte;
    }
}