package com.contact.model;

public class Organisation {
    private Long id;
    private String nom;
    private Long categorieId;
    private String typeStructure;
    private Long localiteId;
    private boolean actif = true;

    public Organisation() {}

    public Organisation(Long id, String nom, Long categorieId,
                        String typeStructure, Long localiteId, boolean actif) {
        this.id = id;
        this.nom = nom;
        this.categorieId = categorieId;
        this.typeStructure = typeStructure;
        this.localiteId = localiteId;
        this.actif = actif;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public Long getCategorieId() { return categorieId; }
    public void setCategorieId(Long categorieId) { this.categorieId = categorieId; }
    public String getTypeStructure() { return typeStructure; }
    public void setTypeStructure(String typeStructure) { this.typeStructure = typeStructure; }
    public Long getLocaliteId() { return localiteId; }
    public void setLocaliteId(Long localiteId) { this.localiteId = localiteId; }
    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }
}

