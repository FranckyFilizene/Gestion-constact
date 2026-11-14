package com.contact.model;

public class Fonction {
    private Long id;
    private String libelle;
    private Long categorieId;
    private String niveauCible;
    private boolean actif = true;

    public Fonction() {}

    public Fonction(Long id, String libelle, Long categorieId,
                    String niveauCible, boolean actif) {
        this.id = id;
        this.libelle = libelle;
        this.categorieId = categorieId;
        this.niveauCible = niveauCible;
        this.actif = actif;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }
    public Long getCategorieId() { return categorieId; }
    public void setCategorieId(Long categorieId) { this.categorieId = categorieId; }
    public String getNiveauCible() { return niveauCible; }
    public void setNiveauCible(String niveauCible) { this.niveauCible = niveauCible; }
    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }
}
