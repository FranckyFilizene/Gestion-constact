package com.contact.model;

import com.contact.model.TypeLocalite;

public class Localite {
    private Long id;
    private String nom;
    private TypeLocalite typeLocalite;
    private Long parentID; // null si c'est le region en principale.
    private String codeSource;
    private boolean actif;

    //Contructeur vide
    public Localite(){};

    //constructeur de la classe
    public Localite(Long id ,String nom, TypeLocalite typeLocalite, Long parentID, String codeSource, boolean actif){
        this.id = id;
        this.nom = nom;
        this.typeLocalite = typeLocalite;
        this.parentID = parentID;
        this.codeSource = codeSource;
        this.actif = actif;
    }

    //les Getters
    public Long getId(){
        return id;
    }

    public String getNom(){
        return nom;
    }

    public TypeLocalite getTypeLocalite(){
        return typeLocalite;
    }

    public Long getParentId(){
        return parentID;
    }

    public String getCodeSource(){
        return codeSource;
    }

    public boolean getActif(){
        return actif;
    }

    //Les Setters
    public void setId( Long id ){
        this.id = id;
    }

    public void setNom ( String nom ){
        this.nom = nom;
    }

    public void setTypeLocalite ( TypeLocalite typelocalite){
        this.typeLocalite = typelocalite;
    }

    public void setParentId ( Long parendID ){
        this.parentID = parendID;
    }

    public void setCodeSource( String codeSource ){
        this.codeSource = codeSource;
    }

    public void setActif ( boolean actif){
        this.actif = actif;
    }
}
