package com.contact.dao;

import com.contact.config.Database;
import com.contact.exception.DaoException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class AnnexeLocaliteSeeder {

    public static void seedAnnexes() {
        if (isTableAnnexesEmpty()) {
            System.out.println("=== DÉBUT DU SEEDING DES RÉGIONS LIMITROPHES (ANNEXES) ===");
            try (Connection conn = Database.getConnection()) {
                conn.setAutoCommit(false);

                // Liste des feuilles Excel correspondant aux 4 régions limitrophes
                String[] feuillesAnnexes = {
                        "Region MENABE",
                        "Région IHOROMBE",
                        "Région ANDROY",
                        "Région ANOSY"
                };

                InputStream is = AnnexeLocaliteSeeder.class.getResourceAsStream("/data/regionLimitrophe.xlsx");
                if (is == null) {
                    System.err.println("Fichier /data/regionLimitrophe.xlsx introuvable dans les ressources !");
                    return;
                }

                try (Workbook workbook = new XSSFWorkbook(is)) {
                    for (String nomFeuille : feuillesAnnexes) {
                        Sheet sheet = workbook.getSheet(nomFeuille);
                        if (sheet == null) {
                            System.err.println("Feuille '" + nomFeuille + "' introuvable dans regionLimitrophe.xlsx. Ignorée.");
                            continue;
                        }

                        System.out.println("-> Importation de la région limitrophe : " + nomFeuille);

                        // 1. Insérer la Région Limitrophe principale
                        // On extrait un nom propre (ex: "MENABE" à partir de "Region MENABE")
                        String nomRegion = nomFeuille.replace("Région", "").replace("Region", "").trim().toUpperCase();
                        long regionId = insererLocaliteLimitrophe(conn, nomRegion, "LIMITROPHE", null, null, null, null);

                        Map<String, Long> districtMap = new HashMap<>();
                        Map<String, Long> communeMap = new HashMap<>();
                        Map<String, Boolean> fokotanyMap = new HashMap<>();

                        int countDistricts = 0;
                        int countCommunes = 0;
                        int countFokotany = 0;

                        // Parcours des lignes du fichier Excel (généralement à partir de la ligne 5 ou de la première ligne valide)
                        for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                            Row row = sheet.getRow(i);
                            if (row == null) continue;

                            // D'après la structure de tes feuilles Excel (colonnes similaires à RAA) :
                            // Col 1 ou 2 : Région, Col 2 : District, Col 3 : Commune, Col 4 : Fokotany
                            String districtNom = getCellValue(row.getCell(2)).trim();
                            String communeNom  = getCellValue(row.getCell(3)).trim();
                            String fokotanyNom = getCellValue(row.getCell(4)).trim();

                            if (districtNom.isEmpty() || districtNom.equalsIgnoreCase("DISTRICT")) continue;

                            // --- Gestion District ---
                            Long districtId = districtMap.get(districtNom.toUpperCase());
                            if (districtId == null) {
                                districtId = insererLocaliteLimitrophe(conn, districtNom, "DISTRICT", regionId, null, null, null);
                                districtMap.put(districtNom.toUpperCase(), districtId);
                                countDistricts++;
                            }

                            if (communeNom.isEmpty()) continue;

                            // --- Gestion Commune ---
                            String cleCommune = districtNom.toUpperCase() + "_" + communeNom.toUpperCase();
                            Long communeId = communeMap.get(cleCommune);
                            if (communeId == null) {
                                communeId = insererLocaliteLimitrophe(conn, communeNom, "COMMUNE", districtId, null, null, null);
                                communeMap.put(cleCommune, communeId);
                                countCommunes++;
                            }

                            if (fokotanyNom.isEmpty()) continue;

                            // --- Gestion Fokotany ---
                            String cleFokotany = cleCommune + "_" + fokotanyNom.toUpperCase();
                            if (!fokotanyMap.containsKey(cleFokotany)) {
                                insererLocaliteLimitrophe(conn, fokotanyNom, "FOKOTANY", communeId, null, null, null);
                                fokotanyMap.put(cleFokotany, true);
                                countFokotany++;
                            }
                        }
                        System.out.println("   -> " + nomRegion + " : " + countDistricts + " districts, " + countCommunes + " communes, " + countFokotany + " fokotany importés.");
                    }
                }

                conn.commit();
                System.out.println("=== FIN DU SEEDING DES RÉGIONS LIMITROPHES AVEC SUCCÈS ===");

            } catch (Exception e) {
                throw new DaoException("Erreur critique lors du seeding des annexes limitrophes", e);
            }
        } else {
            System.out.println("Les localités limitrophes existent déjà en base. Seeding ignoré.");
        }
    }

    private static long insererLocaliteLimitrophe(Connection conn, String nom, String type, Long parentId,
                                                  String codeDist, String codeCom, String codeFok) throws SQLException {
        // Vérifier si elle existe déjà pour éviter les doublons
        String sqlCheck = "SELECT id FROM localite WHERE type_localite = ? AND unaccent(lower(trim(nom))) = unaccent(lower(trim(?))) AND " +
                (parentId == null ? "parent_id IS NULL" : "parent_id = ?") + " LIMIT 1";

        try (PreparedStatement pstmtCheck = conn.prepareStatement(sqlCheck)) {
            pstmtCheck.setString(1, type);
            pstmtCheck.setString(2, nom);
            if (parentId != null) pstmtCheck.setLong(3, parentId);

            try (ResultSet rs = pstmtCheck.executeQuery()) {
                if (rs.next()) return rs.getLong("id");
            }
        }

        String sql = "INSERT INTO localite (nom, type_localite, parent_id, code_district, code_commune, code_fokotany, actif) " +
                "VALUES (?, ?, ?, ?, ?, ?, TRUE)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, nom);
            pstmt.setString(2, type);
            if (parentId != null) pstmt.setLong(3, parentId); else pstmt.setNull(3, Types.BIGINT);
            pstmt.setString(4, codeDist);
            pstmt.setString(5, codeCom);
            pstmt.setString(6, codeFok);
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
                throw new SQLException("Échec de récupération de l'ID généré pour l'annexe : " + nom);
            }
        }
    }

    private static String getCellValue(Cell cell) {
        if (cell == null) return "";
        if (cell.getCellType() == CellType.STRING) return cell.getStringCellValue().trim();
        if (cell.getCellType() == CellType.NUMERIC) return String.valueOf((long) cell.getNumericCellValue());
        return "";
    }

    private static boolean isTableAnnexesEmpty() {
        // On vérifie s'il existe déjà des localités de type 'LIMITROPHE'
        String sql = "SELECT COUNT(*) FROM localite WHERE type_localite = 'LIMITROPHE'";
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1) == 0;
            }
        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la vérification des localités limitrophes", e);
        }
        return false;
    }
}