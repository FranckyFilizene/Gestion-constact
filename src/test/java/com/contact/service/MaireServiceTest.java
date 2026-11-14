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
public class MaireServiceTest {

    private static MaireService maireService;
    private static AuthService authService;
    private static UtilisateurDAO utilisateurDAO;

    private static final String ADMIN_LOGIN = "admin_maire";
    private static final String ADMIN_PASS = "Admin123!";
    private static final String LECTEUR_LOGIN = "lecteur_maire";
    private static final String LECTEUR_PASS = "Lecteur123!";

    private static Long contactIdTest;

    @BeforeAll
    static void initialiser() {
        maireService = new MaireService();
        authService = new AuthService();
        utilisateurDAO = new UtilisateurDAO();

        // 1. Créer un ADMIN de test s'il n'existe pas
        if (utilisateurDAO.rechercherParLogin(ADMIN_LOGIN) == null) {
            Utilisateur admin = new Utilisateur();
            admin.setLogin(ADMIN_LOGIN);
            admin.setNomComplet("Admin Maire");
            admin.setMotDePasseHash(BCrypt.hashpw(ADMIN_PASS, BCrypt.gensalt()));
            admin.setRole(Role.ADMIN);
            admin.setActif(true);
            utilisateurDAO.inserer(admin);
        }

        // 2. Créer un LECTEUR de test s'il n'existe pas
        if (utilisateurDAO.rechercherParLogin(LECTEUR_LOGIN) == null) {
            Utilisateur lecteur = new Utilisateur();
            lecteur.setLogin(LECTEUR_LOGIN);
            lecteur.setNomComplet("Lecteur Maire");
            lecteur.setMotDePasseHash(BCrypt.hashpw(LECTEUR_PASS, BCrypt.gensalt()));
            lecteur.setRole(Role.LECTEUR);
            lecteur.setActif(true);
            utilisateurDAO.inserer(lecteur);
        }

        // 3. S'assurer qu'un contact de test existe pour y lier le maire
        ContactDAO contactDAO = new ContactDAO();
        List<Contact> contacts = contactDAO.listerTous();
        if (!contacts.isEmpty()) {
            contactIdTest = contacts.get(0).getId();
        } else {
            // Créer un contact minimal si aucun n'existe
            FonctionDAO fonctionDAO = new FonctionDAO();
            LocaliteDAO localiteDAO = new LocaliteDAO();
            var fonctions = fonctionDAO.listerToutes();
            var localites = localiteDAO.listerToutes();

            if (!fonctions.isEmpty() && !localites.isEmpty()) {
                Contact c = new Contact();
                c.setNom("TestMaire");
                c.setPrenom("Jean");
                c.setFonctionId(fonctions.get(0).getId());
                c.setLocaliteId(localites.get(0).getId());
                c.setTelephone1("0341122334");
                c.setActif(true);
                contactDAO.inserer(c);

                // Récupérer l'ID après insertion
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
    @DisplayName("1. Refus de création d'un maire par un LECTEUR")
    void testCreerMaireRefusePourLecteur() {
        // Connexion en tant que LECTEUR
        boolean connecte = authService.connecter(LECTEUR_LOGIN, LECTEUR_PASS);
        assertTrue(connecte);
        assertFalse(Session.getInstance().estAdmin());

        Maire maire = new Maire();
        maire.setContactId(contactIdTest);
        maire.setCodeCommune("COM_01");
        maire.setTypeCom("Urbaine");
        maire.setGenre("M");
        maire.setStatutElection("Élu");

        boolean resultat = maireService.creerMaire(maire);
        assertFalse(resultat, "Un LECTEUR ne doit pas pouvoir enregistrer un profil de maire");
    }

    @Test
    @Order(2)
    @DisplayName("2. Création réussie d'un maire par un ADMIN")
    void testCreerMaireReussiParAdmin() {
        // Connexion en tant qu'ADMIN
        boolean connecte = authService.connecter(ADMIN_LOGIN, ADMIN_PASS);
        assertTrue(connecte);
        assertTrue(Session.getInstance().estAdmin());

        Maire maire = new Maire();
        maire.setContactId(contactIdTest);
        maire.setCodeCommune("COM_01");
        maire.setTypeCom("Urbaine");
        maire.setCategorieCom("1ère Catégorie");
        maire.setGenre("M");
        maire.setStatutElection("Élu");

        boolean resultat = maireService.creerMaire(maire);
        assertTrue(resultat, "Un ADMIN doit pouvoir enregistrer un profil de maire");
    }
}