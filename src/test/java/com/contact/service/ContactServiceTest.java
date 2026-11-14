package com.contact.service;

import com.contact.dao.CategorieDAO;
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
public class ContactServiceTest {

    private static ContactService contactService;
    private static AuthService authService;
    private static UtilisateurDAO utilisateurDAO;

    private static final String ADMIN_LOGIN = "admin_contact";
    private static final String ADMIN_PASS = "Admin123!";
    private static final String LECTEUR_LOGIN = "lecteur_contact";
    private static final String LECTEUR_PASS = "Lecteur123!";

    private static Long fonctionIdTest;
    private static Long localiteIdTest;

    @BeforeAll
    static void initialiser() {
        contactService = new ContactService();
        authService = new AuthService();
        utilisateurDAO = new UtilisateurDAO();

        // 1. Créer un compte ADMIN de test s'il n'existe pas
        if (utilisateurDAO.rechercherParLogin(ADMIN_LOGIN) == null) {
            Utilisateur admin = new Utilisateur();
            admin.setLogin(ADMIN_LOGIN);
            admin.setNomComplet("Admin Contact");
            admin.setMotDePasseHash(BCrypt.hashpw(ADMIN_PASS, BCrypt.gensalt()));
            admin.setRole(Role.ADMIN);
            admin.setActif(true);
            utilisateurDAO.inserer(admin);
        }

        // 2. Créer un compte LECTEUR de test s'il n'existe pas
        if (utilisateurDAO.rechercherParLogin(LECTEUR_LOGIN) == null) {
            Utilisateur lecteur = new Utilisateur();
            lecteur.setLogin(LECTEUR_LOGIN);
            lecteur.setNomComplet("Lecteur Contact");
            lecteur.setMotDePasseHash(BCrypt.hashpw(LECTEUR_PASS, BCrypt.gensalt()));
            lecteur.setRole(Role.LECTEUR);
            lecteur.setActif(true);
            utilisateurDAO.inserer(lecteur);
        }

        // 3. Récupérer une fonction et une localité valides pour insérer un contact
        FonctionDAO fonctionDAO = new FonctionDAO();
        List<Fonction> fonctions = fonctionDAO.listerToutes();
        if (!fonctions.isEmpty()) {
            fonctionIdTest = fonctions.get(0).getId();
        }

        LocaliteDAO localiteDAO = new LocaliteDAO();
        List<Localite> localites = localiteDAO.listerToutes();
        if (!localites.isEmpty()) {
            localiteIdTest = localites.get(0).getId();
        }
    }

    @AfterEach
    void nettoyerSession() {
        Session.getInstance().fermerSession();
    }

    @Test
    @Order(1)
    @DisplayName("1. Un LECTEUR ne peut pas créer un contact sensible avec un motif")
    void testLecteurInterditDeCreerProfilSensible() {
        // Connexion en tant que LECTEUR
        boolean connecte = authService.connecter(LECTEUR_LOGIN, LECTEUR_PASS);
        assertTrue(connecte, "Le lecteur doit pouvoir se connecter");
        assertFalse(Session.getInstance().estAdmin());

        // Tentative de création d'un contact avec un motif (profil sensible)
        Contact contactSensible = new Contact();
        contactSensible.setNom("Suspect");
        contactSensible.setPrenom("Jean");
        contactSensible.setFonctionId(fonctionIdTest);
        contactSensible.setLocaliteId(localiteIdTest);
        contactSensible.setTelephone1("0340000000");
        contactSensible.setMotif("Profil suspecté de non-conformité"); // Motif sensible
        contactSensible.setActif(true);

        boolean resultat = contactService.creerContact(contactSensible);
        assertFalse(resultat, "Un LECTEUR ne doit JAMAIS pouvoir enregistrer un profil sensible avec un motif");
    }

    @Test
    @Order(2)
    @DisplayName("2. Un ADMIN peut créer un contact sensible avec un motif")
    void testAdminAutoriseCreerProfilSensible() {
        // Connexion en tant qu'ADMIN
        boolean connecte = authService.connecter(ADMIN_LOGIN, ADMIN_PASS);
        assertTrue(connecte, "L'admin doit pouvoir se connecter");
        assertTrue(Session.getInstance().estAdmin());

        // Création d'un contact avec un motif par l'admin
        Contact contactSensible = new Contact();
        contactSensible.setNom("SuspectAdmin");
        contactSensible.setPrenom("Pierre");
        contactSensible.setFonctionId(fonctionIdTest);
        contactSensible.setLocaliteId(localiteIdTest);
        contactSensible.setTelephone1("0320000000");
        contactSensible.setMotif("Vérification demandée par la hiérarchie");
        contactSensible.setActif(true);

        boolean resultat = contactService.creerContact(contactSensible);
        assertTrue(resultat, "Un ADMIN doit pouvoir créer un profil sensible avec un motif");
    }

    @Test
    @Order(3)
    @DisplayName("3. Un LECTEUR ne voit pas les motifs des profils sensibles (masquage)")
    void testMasquageMotifPourLecteur() {
        // Connexion en tant que LECTEUR
        authService.connecter(LECTEUR_LOGIN, LECTEUR_PASS);
        assertFalse(Session.getInstance().estAdmin());

        List<Contact> contacts = contactService.listerContacts();
        assertNotNull(contacts);

        // Vérifier si un contact possède un motif, il doit être masqué pour le lecteur
        for (Contact c : contacts) {
            if (c.getNom().equals("SuspectAdmin")) {
                assertEquals("[CONFIDENTIEL - Réservé aux Administrateurs]", c.getMotif(),
                        "Le motif sensible doit être masqué pour un utilisateur LECTEUR");
            }
        }
    }
}