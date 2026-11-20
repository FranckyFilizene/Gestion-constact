package com.contact.model;

import com.contact.model.TypeLocalite;

public class Localite {
    // Nouveaux champs pour les communes
    private String typeCom;      // Ex: URBAINE / RURALE
    private String categorieCom; // Ex: CU 1, CR 1, CR 2, CR 2*
    private Long id;
    private String nom;
    private TypeLocalite typeLocalite;
    private Long parentID; // null si c'est le region en principale.
    private String codeSource;
    private boolean actif;
    private String codeDistrict;
    private String codeCommune;
    private String codeFokotany;

    //Contructeur vide
    public Localite(){};

    //constructeur de la classe
    public Localite(Long id ,String typeCom,String categorieCom ,String nom, TypeLocalite typeLocalite, Long parentID, String codeSource, boolean actif, String codeDistrict, String codeCommune, String codeFokotany){
        this.id = id;
        this.nom = nom;
        this.typeLocalite = typeLocalite;
        this.parentID = parentID;
        this.codeSource = codeSource;
        this.actif = actif;
        this.codeDistrict = codeDistrict;
        this.codeCommune = codeCommune;
        this.codeFokotany = codeFokotany;
        this.typeCom = typeCom;
        this.categorieCom = categorieCom;
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

    public String getCodeDistrict() { return codeDistrict; }
    public void setCodeDistrict(String codeDistrict) { this.codeDistrict = codeDistrict; }

    public String getCodeCommune() { return codeCommune; }
    public void setCodeCommune(String codeCommune) { this.codeCommune = codeCommune; }

    public String getCodeFokotany() { return codeFokotany; }
    public void setCodeFokotany(String codeFokotany) { this.codeFokotany = codeFokotany; }

    public String getTypeCom() { return typeCom; }
    public void setTypeCom(String typeCom) { this.typeCom = typeCom; }
    public String getCategorieCom() { return categorieCom; }
    public void setCategorieCom(String categorieCom) { this.categorieCom = categorieCom; }

    @Override
    public String toString() {
        return nom; // C'est le nom du district qui s'affichera proprement dans le ComboBox
    }
}
