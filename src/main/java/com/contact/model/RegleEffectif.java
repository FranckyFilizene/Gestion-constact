package com.contact.model;

public class RegleEffectif {
    private Long id;
    private Long fonctionId;
    private TypeLocalite typeLocalite;
    private Integer effectifPrevu;

    public RegleEffectif() {}

    public RegleEffectif(Long id, Long fonctionId, TypeLocalite typeLocalite,
                         Integer effectifPrevu) {
        this.id = id;
        this.fonctionId = fonctionId;
        this.typeLocalite = typeLocalite;
        this.effectifPrevu = effectifPrevu;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getFonctionId() { return fonctionId; }
    public void setFonctionId(Long fonctionId) { this.fonctionId = fonctionId; }
    public TypeLocalite getTypeLocalite() { return typeLocalite; }
    public void setTypeLocalite(TypeLocalite typeLocalite) { this.typeLocalite = typeLocalite; }
    public Integer getEffectifPrevu() { return effectifPrevu; }
    public void setEffectifPrevu(Integer effectifPrevu) { this.effectifPrevu = effectifPrevu; }
}
