package com.contact.service;

import org.junit.jupiter.api.*;
import org.mindrot.jbcrypt.BCrypt;

import com.contact.dao.UtilisateurDAO;
import com.contact.model.Utilisateur;
import com.contact.model.Role;
import com.contact.security.Session;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UtilisateurServiceTest {

    private static AuthService authService;
    private static UtilisateurService utilisateurService;
    private static UtilisateurDAO utilisateurDAO;

    private static final String ADMIN_LOGIN = "admin_test";
    private static final String ADMIN_PASS = "Admin123!";
    private static final String LECTEUR_LOGIN = "jean_test";

    @BeforeAll
    static void initialiser(){
        authService = new AuthService();
        utilisateurDAO = new UtilisateurDAO();
        utilisateurService = new UtilisateurService();

        // S'assurer qu'un compte ADMIN de test existe en base avant de démarrer
        if( utilisateurDAO.rechercherParLogin(ADMIN_LOGIN) == null ){
            Utilisateur admin = new Utilisateur();
            admin.setLogin(ADMIN_LOGIN);
            admin.setNomComplet("Administrateur Test");
            admin.setMotDePasseHash(BCrypt.hashpw(ADMIN_PASS, BCrypt.gensalt()));
            admin.setRole(Role.ADMIN);
            admin.setActif(true);
            utilisateurDAO.inserer(admin);
        }
    }

    @AfterEach
    void nettoyerSession(){
        //Fermer la session apres chaque teste pour garantir l'isolation
        Session.getInstance().fermerSession();
    }

    @Test
    @Order(1)
    @DisplayName("1.ECHEC de connexion avec un mauvais mot de passe")
    void testConnexionEchoue(){
        boolean resultat = authService.connecter(ADMIN_LOGIN, "MauvaisMotDePasse");
        assertFalse(resultat, "La connexion doit echoue avec un mauvais mot de passe");
        assertFalse(Session.getInstance().estConnecte(), "Aucune session ne doit être ouverte");
    }

    @Test
    @Order(2)
    @DisplayName("2. Connexion réussie d'un ADMIN et ouverture de session")
    void testConnexionAdminReussie(){
        boolean resultat = authService.connecter(ADMIN_LOGIN, ADMIN_PASS);
        assertTrue(resultat, "La connexion de l'admin doit reussir");
        assertTrue(Session.getInstance().estConnecte(), "La session doit être active");
        assertTrue(Session.getInstance().estAdmin(), "L'utilisateur connecté doit être ADMIN");
    }

    @Test
    @Order(3)
    @DisplayName("3.Refus de la creation d'un new user si c'est pas admin")
    void testCreationRefusSansDroits(){
        //Aucune session ouvert ici grace a @AfterEach
        boolean cree = utilisateurService.creerUtilisateur("pirate", "Pirate", "1234", Role.ADMIN);
        assertFalse(cree, "La création doit être refusée si l'utilisateur n'est pas ADMIN");
    }

    @Test
    @Order(4)
    @DisplayName("4. Création réussie d'un compte LECTEUR par un ADMIN connecté")
    void testCreationLecteurParAdmin() {
        // 1. Connexion en tant qu'ADMIN
        authService.connecter(ADMIN_LOGIN, ADMIN_PASS);

        // 2. Création du compte LECTEUR (s'il n'existe pas encore)
        if (utilisateurDAO.rechercherParLogin(LECTEUR_LOGIN) == null) {
            boolean cree = utilisateurService.creerUtilisateur(
                    LECTEUR_LOGIN,
                    "Jean Rakoto",
                    "Lecteur123!",
                    Role.LECTEUR
            );
            assertTrue(cree, "L'ADMIN doit pouvoir créer un compte LECTEUR");
        }

        // 3. Vérification en base
        Utilisateur jean = utilisateurDAO.rechercherParLogin(LECTEUR_LOGIN);
        assertNotNull(jean, "L'utilisateur créé doit exister en base");
        assertEquals(Role.LECTEUR, jean.getRole());
    }

    @Test
    @Order(5)
    @DisplayName("5. Un compte LECTEUR connecté ne peut pas créer d'utilisateur")
    void testLecteurNePeutPasCreer() {
        // Connexion avec le compte LECTEUR créé au test précédent
        boolean connecte = authService.connecter(LECTEUR_LOGIN, "Lecteur123!");
        assertTrue(connecte);
        assertFalse(Session.getInstance().estAdmin());

        // Tentative de création d'un autre compte
        boolean cree = utilisateurService.creerUtilisateur("autre_compte", "Autre", "1234", Role.LECTEUR);
        assertFalse(cree, "Un compte LECTEUR ne doit jamais pouvoir créer un utilisateur");
    }
}
