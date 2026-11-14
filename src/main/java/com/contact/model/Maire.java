package com.contact.model;

public class Maire {
    private Long id;
    private Long contactId;
    private String codeCommune;
    private String typeCom;
    private String categorieCom;
    private String genre;
    private String statutElection;
    private String observation;

    public Maire() {}

    public Maire(Long id, Long contactId, String codeCommune, String typeCom,
                 String categorieCom, String genre, String statutElection,
                 String observation) {
        this.id = id;
        this.contactId = contactId;
        this.codeCommune = codeCommune;
        this.typeCom = typeCom;
        this.categorieCom = categorieCom;
        this.genre = genre;
        this.statutElection = statutElection;
        this.observation = observation;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getContactId() { return contactId; }
    public void setContactId(Long contactId) { this.contactId = contactId; }
    public String getCodeCommune() { return codeCommune; }
    public void setCodeCommune(String codeCommune) { this.codeCommune = codeCommune; }
    public String getTypeCom() { return typeCom; }
    public void setTypeCom(String typeCom) { this.typeCom = typeCom; }
    public String getCategorieCom() { return categorieCom; }
    public void setCategorieCom(String categorieCom) { this.categorieCom = categorieCom; }
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    public String getStatutElection() { return statutElection; }
    public void setStatutElection(String statutElection) { this.statutElection = statutElection; }
    public String getObservation() { return observation; }
    public void setObservation(String observation) { this.observation = observation; }
}
