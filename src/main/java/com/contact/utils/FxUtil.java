package com.contact.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.util.Optional;

public class FxUtil {

    /*
    * Methode utilitaire interne pour genere les alert nasique
    * */
    private static void showAlert(Alert.AlertType type ,String title, String message){
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /*
    * Afficher une boite de dialogue d'erreur
    * */
    public static void showError(String title, String message){
        showAlert(Alert.AlertType.ERROR, title, message);
    }

    /*
    * Afficher une boite de dialogue de succés / information.
    * */
    public static void showSuccess(String title, String message){
        showAlert(Alert.AlertType.INFORMATION, title, message);
    }

    /*
    * Affiche une boite de dialogue d'avertissement.
    * */
    public static void showWarning(String title, String message){
        showAlert(Alert.AlertType.WARNING, title, message);
    }

    /**
     * Affiche une boîte de confirmation (ex: "Voulez-vous vraiment supprimer ?").
     * Retourne true si l'utilisateur clique sur OK, false sinon.
     */
    public static boolean showConfirmation(String title , String message){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}
