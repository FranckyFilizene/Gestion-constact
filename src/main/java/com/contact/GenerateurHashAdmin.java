package com.contact;

import org.mindrot.jbcrypt.BCrypt;

public class GenerateurHashAdmin {
    public static void main(String[] args) {
        String[][] comptes = {
                {"admin1", "Admin#2026!1"},
                {"admin2", "Admin#2026!2"},
                {"admin3", "Admin#2026!3"},
                {"admin4", "Admin#2026!4"}
        };

        for (String[] compte : comptes) {
            String hash = BCrypt.hashpw(compte[1], BCrypt.gensalt(10));
            System.out.println("Login: " + compte[0] + " | MDP: " + compte[1] + " -> " + hash);
        }
    }
}