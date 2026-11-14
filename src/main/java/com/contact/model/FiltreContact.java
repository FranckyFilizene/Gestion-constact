package com.contact.model;

/** Objet de critères de recherche ; ne correspond pas à une table PostgreSQL. */
public class FiltreContact {
    private String texte;
    private Long categorieId;
    private Long fonctionId;
    private Long districtId;
    private Long communeId;
    private Long fokotanyId;
    private Boolean actif;
    private boolean inclurePersonnesSuspectes;

    public FiltreContact() {}

    public String getTexte() { return texte; }
    public void setTexte(String texte) { this.texte = texte; }
    public Long getCategorieId() { return categorieId; }
    public void setCategorieId(Long categorieId) { this.categorieId = categorieId; }
    public Long getFonctionId() { return fonctionId; }
    public void setFonctionId(Long fonctionId) { this.fonctionId = fonctionId; }
    public Long getDistrictId() { return districtId; }
    public void setDistrictId(Long districtId) { this.districtId = districtId; }
    public Long getCommuneId() { return communeId; }
    public void setCommuneId(Long communeId) { this.communeId = communeId; }
    public Long getFokotanyId() { return fokotanyId; }
    public void setFokotanyId(Long fokotanyId) { this.fokotanyId = fokotanyId; }
    public Boolean getActif() { return actif; }
    public void setActif(Boolean actif) { this.actif = actif; }
    public boolean isInclurePersonnesSuspectes() { return inclurePersonnesSuspectes; }
    public void setInclurePersonnesSuspectes(boolean inclurePersonnesSuspectes) {
        this.inclurePersonnesSuspectes = inclurePersonnesSuspectes;
    }
}
