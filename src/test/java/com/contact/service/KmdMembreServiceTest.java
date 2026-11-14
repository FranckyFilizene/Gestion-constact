package com.contact.service;

import com.contact.dao.ContactDAO;
import com.contact.dao.FonctionDAO;
import com.contact.dao.LocaliteDAO;
import com.contact.dao.UtilisateurDAO;
import com.contact.model.*;
import com.contact.security.Session;
import org.junit.jupiter.api.*;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class KmdMembreServiceTest {

    private static KmdMembreService kmdMembreService;
    private static AuthService authService;
    private static UtilisateurDAO utilisateurDAO;

    private static final String ADMIN_LOGIN = "admin_kmd";
    private static final String ADMIN_PASS = "Admin123!";
    private static final String LECTEUR_LOGIN = "lecteur_kmd";
    private static final String LECTEUR_PASS = "Lecteur123!";

    private static Long contactIdTest;

    @BeforeAll
    static void initialiser() {
        kmdMembreService = new KmdMembreService();
        authService = new AuthService();
        utilisateurDAO = new UtilisateurDAO();

        // 1. Créer un ADMIN de test s'il n'existe pas
        if (utilisateurDAO.rechercherParLogin(ADMIN_LOGIN) == null) {
            Utilisateur admin = new Utilisateur();
            admin.setLogin(ADMIN_LOGIN);
            admin.setNomComplet("Admin KMD");
            admin.setMotDePasseHash(BCrypt.hashpw(ADMIN_PASS, BCrypt.gensalt()));
            admin.setRole(Role.ADMIN);
            admin.setActif(true);
            utilisateurDAO.inserer(admin);
        }

        // 2. Créer un LECTEUR de test s'il n'existe pas
        if (utilisateurDAO.rechercherParLogin(LECTEUR_LOGIN) == null) {
            Utilisateur lecteur = new Utilisateur();
            lecteur.setLogin(LECTEUR_LOGIN);
            lecteur.setNomComplet("Lecteur KMD");
            lecteur.setMotDePasseHash(BCrypt.hashpw(LECTEUR_PASS, BCrypt.gensalt()));
            lecteur.setRole(Role.LECTEUR);
            lecteur.setActif(true);
            utilisateurDAO.inserer(lecteur);
        }

        // 3. S'assurer qu'un contact de test existe
        ContactDAO contactDAO = new ContactDAO();
        List<Contact> contacts = contactDAO.listerTous();
        if (!contacts.isEmpty()) {
            contactIdTest = contacts.get(0).getId();
        } else {
            FonctionDAO fonctionDAO = new FonctionDAO();
            LocaliteDAO localiteDAO = new LocaliteDAO();
            var fonctions = fonctionDAO.listerToutes();
            var localites = localiteDAO.listerToutes();

            if (!fonctions.isEmpty() && !localites.isEmpty()) {
                Contact c = new Contact();
                c.setNom("TestKMD");
                c.setPrenom("Paul");
                c.setFonctionId(fonctions.get(0).getId());
                c.setLocaliteId(localites.get(0).getId());
                c.setTelephone1("0321122334");
                c.setActif(true);
                contactDAO.inserer(c);

                contacts = contactDAO.listerTous();
                if (!contacts.isEmpty()) {
                    contactIdTest = contacts.get(contacts.size() - 1).getId();
                }
            }
        }
    }

    @AfterEach
    void nettoyerSession() {
        Session.getInstance().fermerSession();
    }

    @Test
    @Order(1)
    @DisplayName("1. Refus d'enregistrement d'un membre KMD par un LECTEUR")
    void testCreerMembreKmdRefusePourLecteur() {
        boolean connecte = authService.connecter(LECTEUR_LOGIN, LECTEUR_PASS);
        assertTrue(connecte);
        assertFalse(Session.getInstance().estAdmin());

        KmdMembre membre = new KmdMembre();
        membre.setContactId(contactIdTest);
        membre.setNiveauKmd("District");
        membre.setRoleKmd("Président");
        membre.setNumeroOrdre(1);

        boolean resultat = kmdMembreService.creerMembre(membre);
        assertFalse(resultat, "Un LECTEUR ne doit pas pouvoir enregistrer un membre KMD");
    }

    @Test
    @Order(2)
    @DisplayName("2. Enregistrement réussi d'un membre KMD par un ADMIN")
    void testCreerMembreKmdReussiParAdmin() {
        boolean connecte = authService.connecter(ADMIN_LOGIN, ADMIN_PASS);
        assertTrue(connecte);
        assertTrue(Session.getInstance().estAdmin());

        KmdMembre membre = new KmdMembre();
        membre.setContactId(contactIdTest);
        membre.setNiveauKmd("District");
        membre.setRoleKmd("Président");
        membre.setNumeroOrdre(1);

        boolean resultat = kmdMembreService.creerMembre(membre);
        assertTrue(resultat, "Un ADMIN doit pouvoir enregistrer un membre KMD");
    }
}