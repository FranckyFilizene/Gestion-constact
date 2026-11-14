package com.contact.model;

/** Résultat calculé par le service des effectifs ; ce n'est pas une table SQL. */
public class EffectifStatut {
    private Long localiteId;
    private String nomLocalite;
    private TypeLocalite typeLocalite;
    private Long fonctionId;
    private String libelleFonction;
    private int effectifPrevu;
    private int effectifActuel;

    public EffectifStatut() {}

    public EffectifStatut(Long localiteId, String nomLocalite,
                          TypeLocalite typeLocalite, Long fonctionId,
                          String libelleFonction, int effectifPrevu,
                          int effectifActuel) {
        this.localiteId = localiteId;
        this.nomLocalite = nomLocalite;
        this.typeLocalite = typeLocalite;
        this.fonctionId = fonctionId;
        this.libelleFonction = libelleFonction;
        this.effectifPrevu = effectifPrevu;
        this.effectifActuel = effectifActuel;
    }

    public Long getLocaliteId() { return localiteId; }
    public void setLocaliteId(Long localiteId) { this.localiteId = localiteId; }
    public String getNomLocalite() { return nomLocalite; }
    public void setNomLocalite(String nomLocalite) { this.nomLocalite = nomLocalite; }
    public TypeLocalite getTypeLocalite() { return typeLocalite; }
    public void setTypeLocalite(TypeLocalite typeLocalite) { this.typeLocalite = typeLocalite; }
    public Long getFonctionId() { return fonctionId; }
    public void setFonctionId(Long fonctionId) { this.fonctionId = fonctionId; }
    public String getLibelleFonction() { return libelleFonction; }
    public void setLibelleFonction(String libelleFonction) { this.libelleFonction = libelleFonction; }
    public int getEffectifPrevu() { return effectifPrevu; }
    public void setEffectifPrevu(int effectifPrevu) { this.effectifPrevu = effectifPrevu; }
    public int getEffectifActuel() { return effectifActuel; }
    public void setEffectifActuel(int effectifActuel) { this.effectifActuel = effectifActuel; }
    public int getManquants() { return Math.max(0, effectifPrevu - effectifActuel); }
    public boolean isComplet() { return effectifActuel >= effectifPrevu; }
}
