package com.contact.model;

import java.time.LocalDateTime;

public class CentreVote {
    private Long id;
    private String nom;
    private Long communeId;
    private Long fokotanyId; //facultif
    private Integer nombreBureaux;
    private Integer nombreElecteur;
    private String observation;
    private boolean actif;
    private LocalDateTime dateCreation;

    //constructeur vide
    public CentreVote(){}

    //constructeur non vide
    public CentreVote(
            Long id,
            String nom,
            Long communeId,
            Long fokotanyId,
            Integer nombreBureaux,
            Integer nombreElecteur,
            String observation,
            boolean actif,
            LocalDateTime dateCreation
    ){
        this.id = id;
        this.nom = nom;
        this.communeId = communeId;
        this.fokotanyId = fokotanyId;
        this.nombreBureaux = nombreBureaux;
        this.nombreElecteur = nombreElecteur;
        this.observation = observation;
        this.actif = actif;
        this.dateCreation = dateCreation;
    }

    //GETTERS
    public Long getId(){
        return id;
    }

    public String getNom(){
        return nom;
    }

    public Long getCommuneId(){
        return communeId;
    }

    public Long getFokotanyId(){
        return fokotanyId;
    }

    public String getObservation(){
        return observation;
    }

    public Integer getNombreBureaux(){
        return nombreBureaux;
    }

    public Integer getNombreElecteur(){
        return nombreElecteur;
    }

    public boolean getActif(){
        return actif;
    }

    public LocalDateTime getDateCreation(){
        return dateCreation;
    }

    //SETTERS
    public void setId( Long id ){
        this.id = id;
    }

    public void setNom ( String nom ){
        this.nom = nom;
    }

    public void setCommuneId( Long communeId ){
        this.communeId = communeId;
    }

    public void setFoktanyId( Long fokotanyId ){
        this.fokotanyId = fokotanyId;
    }

    public void setNombreBureaux( Integer nombreBureaux){
        this.nombreBureaux = nombreBureaux;
    }

    public void setNombreElecteur( Integer nombreElecteur ){
        this.nombreElecteur = nombreElecteur;
    }

    public void setObservation ( String observation ){
        this.observation = observation;
    }

    public void setActif ( boolean actif ){
        this.actif = actif;
    }

    public void setDateCreation( LocalDateTime dateCreation ){
        this.dateCreation = dateCreation;
    }

}
