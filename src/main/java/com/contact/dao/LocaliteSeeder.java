package com.contact.dao;

import com.contact.config.Database;
import com.contact.exception.DaoException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.sql.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class LocaliteSeeder {

    public static void seedDatabase() {
        if (isTableEmpty()) {
            System.out.println("=== DÉBUT DU SEEDING COMPLET DES LOCALITÉS (AVEC CODES, TYPE, CATÉGORIE & VILLAGES) ===");
            try (Connection conn = Database.getConnection()) {
                conn.setAutoCommit(false); // Mode transactionnel

                // 1. Insertion de la Région principale
                long regionId = insererLocalite(conn, "Atsimo Andrefana", "REGION", null, null, null, null, null, null);
                System.out.println("-> Région insérée : Atsimo Andrefana (ID: " + regionId + ")");

                // 2. Insertion des 9 Districts officiels
                Map<String, String> districtsOfficiels = new LinkedHashMap<>();
                districtsOfficiels.put("Toliara I", "601");
                districtsOfficiels.put("Toliara II", "602");
                districtsOfficiels.put("Betioky Sud", "603");
                districtsOfficiels.put("Benenitra", "604");
                districtsOfficiels.put("Ampanihy Ouest", "605");
                districtsOfficiels.put("Beroroha", "606");
                districtsOfficiels.put("Morombe", "607");
                districtsOfficiels.put("Ankazoabo sud", "608");
                districtsOfficiels.put("Sakaraha", "609");

                Map<String, Long> districtNormToId = new HashMap<>();
                Map<String, String> districtNormToCode = new HashMap<>();

                for (Map.Entry<String, String> entry : districtsOfficiels.entrySet()) {
                    String nomExact = entry.getKey();
                    String codeDist = entry.getValue();
                    long distId = insererLocalite(conn, nomExact, "DISTRICT", regionId, codeDist, null, null, null, null);

                    districtNormToId.put(nomExact.toUpperCase(), distId);
                    districtNormToCode.put(nomExact.toUpperCase(), codeDist);
                }
                System.out.println("-> Les 9 districts officiels et leurs codes ont été insérés.");

                // 3. Importation des Communes et Fokotany depuis l'Excel des Fokontany ET enrichissement Type/Catégorie via Excel des Maires
                importerCommunesEtFokotanyDepuisExcel(conn, districtNormToId, districtNormToCode);

                // 4. Importation des Villages rattachés aux Fokotany depuis NomDesVillages.xlsx
                importerVillagesDepuisExcel(conn);

                conn.commit(); // Validation finale
                System.out.println("=== FIN DU SEEDING COMPLET AVEC SUCCÈS ===");

            } catch (Exception e) {
                throw new DaoException("Erreur critique lors du seeding complet des localités", e);
            }
        } else {
            System.out.println("La table localite contient déjà des données. Seeding ignoré.");
        }
    }

    private static void importerCommunesEtFokotanyDepuisExcel(Connection conn,
                                                              Map<String, Long> districtNormToId,
                                                              Map<String, String> districtNormToCode) throws Exception {

        // A. Charger d'abord les métadonnées TypeCom et CategorieCom depuis le fichier des Maires
        Map<String, Map<String, String[]>> communeInfosMaires = chargerInfosMairesDepuisExcel();

        // B. Charger le fichier principal des Fokotany
        InputStream is = LocaliteSeeder.class.getResourceAsStream("/data/Liste Fokontany RAA.xlsx");
        if (is == null) {
            System.err.println("Fichier /data/Liste Fokontany RAA.xlsx introuvable !");
            return;
        }

        try (Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheet("Région ATSIMO ANDREFANA");
            if (sheet == null) {
                System.err.println("Onglet 'Région ATSIMO ANDREFANA' introuvable dans le fichier Excel.");
                return;
            }

            Map<String, Integer> compteurCommunesParDistrict = new HashMap<>();
            Map<String, Long> communeIds = new HashMap<>();
            Map<String, String> communeCodes = new HashMap<>();
            Map<String, Integer> compteurFokotanyParCommune = new HashMap<>();
            Map<String, Boolean> fokotanyDejaInseres = new HashMap<>();

            for (int i = 5; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String distExcel = getCellValue(row.getCell(2)).trim();
                String commExcel = getCellValue(row.getCell(3)).trim();
                String fokExcel  = getCellValue(row.getCell(4)).trim();

                if (distExcel.isEmpty() || commExcel.isEmpty() || fokExcel.isEmpty()) continue;

                String distKeyUpper = distExcel.toUpperCase();
                Long districtId = districtNormToId.get(distKeyUpper);
                String codeDist = districtNormToCode.get(distKeyUpper);

                if (districtId == null) continue;

                // --- Gestion Commune ---
                String cleCommune = distKeyUpper + "_" + commExcel.toUpperCase();
                if (!communeIds.containsKey(cleCommune)) {
                    int numCom = compteurCommunesParDistrict.getOrDefault(distKeyUpper, 0) + 1;
                    compteurCommunesParDistrict.put(distKeyUpper, numCom);
                    String codeCom = codeDist + String.format("%02d", numCom);

                    String typeCom = "RURALE";
                    String catCom = "CR 2";
                    if (communeInfosMaires.containsKey(distKeyUpper)) {
                        Map<String, String[]> comMap = communeInfosMaires.get(distKeyUpper);
                        for (Map.Entry<String, String[]> entry : comMap.entrySet()) {
                            if (entry.getKey().equalsIgnoreCase(commExcel)) {
                                typeCom = entry.getValue()[0];
                                catCom = entry.getValue()[1];
                                break;
                            }
                        }
                    }

                    long cId = insererLocalite(conn, commExcel, "COMMUNE", districtId, codeDist, codeCom, null, typeCom, catCom);
                    communeIds.put(cleCommune, cId);
                    communeCodes.put(cleCommune, codeCom);
                }

                long currentCommuneId = communeIds.get(cleCommune);
                String currentCodeCom = communeCodes.get(cleCommune);

                // --- Gestion Fokotany ---
                String cleFokotany = cleCommune + "_" + fokExcel.toUpperCase();
                if (!fokotanyDejaInseres.containsKey(cleFokotany)) {
                    int numFok = compteurFokotanyParCommune.getOrDefault(cleCommune, 0) + 1;
                    compteurFokotanyParCommune.put(cleCommune, numFok);
                    String codeFok = currentCodeCom + String.format("%02d", numFok);

                    insererLocalite(conn, fokExcel, "FOKOTANY", currentCommuneId, codeDist, currentCodeCom, codeFok, null, null);
                    fokotanyDejaInseres.put(cleFokotany, true);
                }
            }
            System.out.println("-> Importation Communes & Fokotany terminée avec succès !");
        }
    }

    /**
     * Importe automatiquement les villages rattachés aux Fokotany depuis /data/NomDesVillages.xlsx
     */
    private static void importerVillagesDepuisExcel(Connection conn) throws Exception {
        InputStream is = LocaliteSeeder.class.getResourceAsStream("/data/NomDesVillages.xlsx");
        if (is == null) {
            System.err.println("Fichier /data/NomDesVillages.xlsx introuvable dans les ressources !");
            return;
        }

        try (Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                System.err.println("Aucune feuille trouvée dans NomDesVillages.xlsx");
                return;
            }

            System.out.println("-> Début de l'importation des villages...");
            int countVillages = 0;

            String sqlInsert = "INSERT INTO localite (nom, type_localite, parent_id, code_source, actif) " +
                    "VALUES (?, 'VILLAGE', ?, ?, TRUE)";

            try (PreparedStatement pstmtInsert = conn.prepareStatement(sqlInsert)) {
                for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) continue;

                    String districtNom = getCellValue(row.getCell(2)).trim();
                    String communeNom  = getCellValue(row.getCell(3)).trim();
                    String fokotanyNom = getCellValue(row.getCell(4)).trim();
                    String villageNom  = getCellValue(row.getCell(5)).trim();
                    String codeSource  = getCellValue(row.getCell(6)).trim();

                    if (districtNom.isEmpty() || communeNom.isEmpty() || fokotanyNom.isEmpty() || villageNom.isEmpty()) {
                        continue;
                    }

                    // 1. Retrouver l'ID du District parent
                    Long districtId = trouverIdLocaliteParNomEtType(conn, districtNom, "DISTRICT");
                    if (districtId == null) continue;

                    // 2. Retrouver l'ID de la Commune parente
                    Long communeId = trouverIdLocaliteParNomEtTypeEtParent(conn, communeNom, "COMMUNE", districtId);
                    if (communeId == null) continue;

                    // 3. Retrouver l'ID du Fokotany parent
                    Long fokotanyId = trouverIdLocaliteParNomEtTypeEtParent(conn, fokotanyNom, "FOKOTANY", communeId);
                    if (fokotanyId == null) continue;

                    // 4. Éviter les doublons sous le même Fokotany
                    if (!villageExisteDeja(conn, villageNom, fokotanyId)) {
                        pstmtInsert.setString(1, villageNom);
                        pstmtInsert.setLong(2, fokotanyId);
                        pstmtInsert.setString(3, codeSource.isEmpty() ? null : codeSource);
                        pstmtInsert.executeUpdate();
                        countVillages++;
                    }
                }
            }
            System.out.println("-> Importation des villages terminée : " + countVillages + " villages insérés !");
        }
    }

    private static Long trouverIdLocaliteParNomEtType(Connection conn, String nom, String type) throws SQLException {
        String sql = "SELECT id FROM localite WHERE type_localite = ? AND unaccent(lower(trim(nom))) = unaccent(lower(trim(?))) LIMIT 1";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, type);
            pstmt.setString(2, nom);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getLong("id");
            }
        }
        return null;
    }

    private static Long trouverIdLocaliteParNomEtTypeEtParent(Connection conn, String nom, String type, Long parentId) throws SQLException {
        String sql = "SELECT id FROM localite WHERE type_localite = ? AND parent_id = ? AND unaccent(lower(trim(nom))) = unaccent(lower(trim(?))) LIMIT 1";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, type);
            pstmt.setLong(2, parentId);
            pstmt.setString(3, nom);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getLong("id");
            }
        }
        return null;
    }

    private static boolean villageExisteDeja(Connection conn, String nom, Long parentId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM localite WHERE type_localite = 'VILLAGE' AND parent_id = ? AND unaccent(lower(trim(nom))) = unaccent(lower(trim(?)))";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, parentId);
            pstmt.setString(2, nom);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    private static Map<String, Map<String, String[]>> chargerInfosMairesDepuisExcel() {
        Map<String, Map<String, String[]>> result = new HashMap<>();
        try (InputStream is = LocaliteSeeder.class.getResourceAsStream("/data/MAIRES ATSIMO ANDREFANA 2025.xlsx")) {
            if (is == null) return result;

            try (Workbook workbook = new XSSFWorkbook(is)) {
                for (int s = 0; s < workbook.getNumberOfSheets(); s++) {
                    Sheet sheet = workbook.getSheetAt(s);
                    if (sheet.getSheetName().equalsIgnoreCase("Maires communes_RAA")) continue;

                    for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                        Row row = sheet.getRow(i);
                        if (row == null) continue;

                        String districtNom = getCellValue(row.getCell(0)).trim().toUpperCase();
                        String communeNom = getCellValue(row.getCell(2)).trim();
                        String typeCom = getCellValue(row.getCell(3)).trim();
                        String catCom = getCellValue(row.getCell(4)).trim();

                        if (districtNom.isEmpty() || communeNom.isEmpty()) continue;

                        if (districtNom.equals("TOLIARY-I") || districtNom.equals("TOLIARA I")) {
                            districtNom = "TOLIARA I";
                        } else if (districtNom.contains("TOLIARY")) {
                            districtNom = "TOLIARA II";
                        }

                        result.computeIfAbsent(districtNom, k -> new HashMap<>())
                                .put(communeNom, new String[]{typeCom.isEmpty() ? "RURALE" : typeCom, catCom.isEmpty() ? "CR 2" : catCom});
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Avertissement: Impossible de charger le fichier des Maires pour les types/catégories : " + e.getMessage());
        }
        return result;
    }

    private static long insererLocalite(Connection conn, String nom, String type, Long parentId,
                                        String codeDist, String codeCom, String codeFok,
                                        String typeCom, String categorieCom) throws SQLException {
        String sql = "INSERT INTO localite (nom, type_localite, parent_id, code_district, code_commune, code_fokotany, type_com, categorie_com, actif) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, TRUE)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, nom);
            pstmt.setString(2, type);
            if (parentId != null) pstmt.setLong(3, parentId); else pstmt.setNull(3, Types.BIGINT);
            pstmt.setString(4, codeDist);
            pstmt.setString(5, codeCom);
            pstmt.setString(6, codeFok);
            pstmt.setString(7, typeCom);
            pstmt.setString(8, categorieCom);
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
                throw new SQLException("Échec de récupération de l'ID généré pour : " + nom);
            }
        }
    }

    private static String getCellValue(Cell cell) {
        if (cell == null) return "";
        if (cell.getCellType() == CellType.STRING) return cell.getStringCellValue().trim();
        if (cell.getCellType() == CellType.NUMERIC) return String.valueOf((long) cell.getNumericCellValue());
        return "";
    }

    private static boolean isTableEmpty() {
        String sql = "SELECT COUNT(*) FROM localite";
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1) == 0;
            }
        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la vérification de la table localite", e);
        }
        return false;
    }
}