package com.contact;

import com.contact.dao.AnnexeLocaliteSeeder;
import com.contact.dao.LocaliteSeeder;
import com.contact.dao.CategorieSeeder;
import com.contact.dao.FonctionSeeder;
import com.contact.dao.OrganisationSeeder;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.InputStream;
import java.util.Objects;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // 1. Initialisations de la base de données (Seeders)
            LocaliteSeeder.seedDatabase();
            AnnexeLocaliteSeeder.seedAnnexes();
            CategorieSeeder.seedCategories();
            FonctionSeeder.seedFonctions();
            OrganisationSeeder.seedOrganisations();

            // 2. Chargement de la vue Login
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/view/login.fxml")));
            Scene scene = new Scene(root, 900, 540);

            // Rendre le fond de la scène transparent
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);

            // Icône de la fenêtre et de la barre des tâches (PNG en priorité, repli sur ICO)
            chargerIconeApplication(primaryStage);

            // Utiliser TRANSPARENT au lieu de UNDECORATED pour autoriser les coins arrondis
            primaryStage.initStyle(StageStyle.TRANSPARENT);
            primaryStage.setTitle("SYSTEME DE SECURITE DINA (SSD) - Atsimo Andrefana & Annexes");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();

        } catch (Exception e) {
            System.err.println("Erreur critique lors du lancement de l'application : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void chargerIconeApplication(Stage stage) {
        try (InputStream iconStream = getClass().getResourceAsStream("/images/logo.png")) {
            if (iconStream != null) {
                stage.getIcons().add(new Image(iconStream));
                return;
            }
        } catch (Exception e) {
            System.err.println("Impossible de charger /images/logo.png : " + e.getMessage());
        }

        try (InputStream icoStream = getClass().getResourceAsStream("/images/logo.ico")) {
            if (icoStream != null) {
                stage.getIcons().add(new Image(icoStream));
            }
        } catch (Exception e) {
            System.err.println("Impossible de charger /images/logo.ico : " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}