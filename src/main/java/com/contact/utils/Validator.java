package com.contact.utils;

import javafx.scene.control.TextInputControl;
import java.util.regex.Pattern;

public class Validator {

    //Expression reguliere basique pour valider les emails
    private static final String EMAIL_REGEX = "[A-Za-z0-9+_.-]+@[A-Z]a-z0-9.-]+$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    /*
    * Verifie si une chaine de caractere est nulle ou vide (apres suppression des espaces).
    * */
    public static boolean isNullOrEmpty(String value){
        return value == null || value.trim().isEmpty();
    }

    /*
    * Verifie si une composant de saisir javaFX (TextField, textarea, passwordField) est vide
    * */
    public static boolean isFieldEmpty (TextInputControl field){
        return field.getText() == null || field.getText().trim().isEmpty();
    }

    /*
    * Valide le format d'une email
    * */
    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Vérifie si une chaîne de caractères représente un nombre valide.
     */
    public static boolean isNumeric(String str) {
        if (str == null || str.trim().isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(str.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

}
