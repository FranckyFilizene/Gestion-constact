package com.contact.model;

import java.time.LocalDateTime;

public class Contact {
    private Long id;
    private String nom;
    private String prenom;
    private Long fonctionId;
    private Long organisationId;
    private Long localiteId;
    private String telephone1;
    private String telephone2;
    private String email;
    private String observation;

    /** Champ réservé à l'ADMIN pour les personnes de la catégorie sensible. */
    private String motif;
    private boolean actif;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;

    //Constructeur vide
    public Contact(){}

    //constructeur non vide
    public Contact(Long id, String nom, String prenom, Long fonctionId,
                   Long organisationId, Long localiteId, String telephone1,
                   String telephone2, String email, String observation,
                   String motif, boolean actif, LocalDateTime dateCreation,
                   LocalDateTime dateModification) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.fonctionId = fonctionId;
        this.organisationId = organisationId;
        this.localiteId = localiteId;
        this.telephone1 = telephone1;
        this.telephone2 = telephone2;
        this.email = email;
        this.observation = observation;
        this.motif = motif;
        this.actif = actif;
        this.dateCreation = dateCreation;
        this.dateModification = dateModification;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public Long getFonctionId() { return fonctionId; }
    public void setFonctionId(Long fonctionId) { this.fonctionId = fonctionId; }
    public Long getOrganisationId() { return organisationId; }
    public void setOrganisationId(Long organisationId) { this.organisationId = organisationId; }
    public Long getLocaliteId() { return localiteId; }
    public void setLocaliteId(Long localiteId) { this.localiteId = localiteId; }
    public String getTelephone1() { return telephone1; }
    public void setTelephone1(String telephone1) { this.telephone1 = telephone1; }
    public String getTelephone2() { return telephone2; }
    public void setTelephone2(String telephone2) { this.telephone2 = telephone2; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getObservation() { return observation; }
    public void setObservation(String observation) { this.observation = observation; }
    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }
    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }
    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
    public LocalDateTime getDateModification() { return dateModification; }
    public void setDateModification(LocalDateTime dateModification) { this.dateModification = dateModification; }

}
