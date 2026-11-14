package com.contact.model;

public class KmdMembre {
    private Long id;
    private Long contactId;
    private String niveauKmd;
    private String roleKmd;
    private Integer numeroOrdre;

    public KmdMembre() {}

    public KmdMembre(Long id, Long contactId, String niveauKmd,
                     String roleKmd, Integer numeroOrdre) {
        this.id = id;
        this.contactId = contactId;
        this.niveauKmd = niveauKmd;
        this.roleKmd = roleKmd;
        this.numeroOrdre = numeroOrdre;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getContactId() { return contactId; }
    public void setContactId(Long contactId) { this.contactId = contactId; }
    public String getNiveauKmd() { return niveauKmd; }
    public void setNiveauKmd(String niveauKmd) { this.niveauKmd = niveauKmd; }
    public String getRoleKmd() { return roleKmd; }
    public void setRoleKmd(String roleKmd) { this.roleKmd = roleKmd; }
    public Integer getNumeroOrdre() { return numeroOrdre; }
    public void setNumeroOrdre(Integer numeroOrdre) { this.numeroOrdre = numeroOrdre; }
}
