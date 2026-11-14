package com.contact.model;

public class Categorie {
    private Long id;
    private String code;
    private String libelle;
    private boolean sensible;

    //Constructeur vide
    public Categorie(){}

    //Construteur non vide
    public Categorie(Long id ,String code ,String libelle ,boolean sensible){
        this.id = id;
        this.code = code;
        this.libelle = libelle;
        this.sensible = sensible;
    }

    //GETTERS
    public Long getId(){
        return id;
    }

    public String getCode(){
        return code;
    }

    public String getLibelle(){
        return libelle;
    }

    public boolean isSensible(){
        return sensible;
    }

    //SETTERS
    public  void setId ( Long id ){
        this.id = id;
    }

    public void setCode ( String code ) {
        this.code = code;
    }

    public void setLibelle ( String libelle ){
        this.libelle = libelle;
    }

    public void setSensible ( boolean sensible ){
        this.sensible = sensible;
    }

}
