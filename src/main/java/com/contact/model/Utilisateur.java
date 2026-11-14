package com.contact.model;

import java.time.LocalDateTime;

public class Utilisateur {
    private long id;
    private String login;
    private String nomComplet;
    private String motDePasseHash;
    private Role role; // "ADMIN" ou "LECTEUR"
    private boolean actif = true;
    private LocalDateTime dateCreation;

    // Constructeurs
    public Utilisateur() {}

    public Utilisateur(String login, String nomComplet, String motDePasseHash, Role role) {
        this.login = login;
        this.nomComplet = nomComplet;
        this.motDePasseHash = motDePasseHash;
        this.role = role;
        this.actif = true;
    }

    // Getters et Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getNomComplet() { return nomComplet; }
    public void setNomComplet(String nomComplet) { this.nomComplet = nomComplet; }

    public String getMotDePasseHash() { return motDePasseHash; }
    public void setMotDePasseHash(String motDePasseHash) { this.motDePasseHash = motDePasseHash; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
}