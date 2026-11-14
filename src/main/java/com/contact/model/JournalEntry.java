package com.contact.model;

import java.time.LocalDateTime;

public class JournalEntry {
    private Long id;
    private Long utilisateurId;
    private ActionJournal action;
    private String tableCible;
    private Long idCible;
    private String details;
    private LocalDateTime dateAction;

    public JournalEntry() {}

    public JournalEntry(Long id, Long utilisateurId, ActionJournal action,
                        String tableCible, Long idCible, String details,
                        LocalDateTime dateAction) {
        this.id = id;
        this.utilisateurId = utilisateurId;
        this.action = action;
        this.tableCible = tableCible;
        this.idCible = idCible;
        this.details = details;
        this.dateAction = dateAction;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(Long utilisateurId) { this.utilisateurId = utilisateurId; }
    public ActionJournal getAction() { return action; }
    public void setAction(ActionJournal action) { this.action = action; }
    public String getTableCible() { return tableCible; }
    public void setTableCible(String tableCible) { this.tableCible = tableCible; }
    public Long getIdCible() { return idCible; }
    public void setIdCible(Long idCible) { this.idCible = idCible; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public LocalDateTime getDateAction() { return dateAction; }
    public void setDateAction(LocalDateTime dateAction) { this.dateAction = dateAction; }
}

