/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.commons.impl;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.apache.marmotta.ucuenca.wk.commons.function.SemanticDistance;
import org.apache.marmotta.ucuenca.wk.commons.function.SyntacticDistance;
import org.apache.marmotta.ucuenca.wk.commons.service.CommonsServices;
import org.apache.marmotta.ucuenca.wk.commons.service.DistanceService;
import org.simmetrics.StringMetric;
import static org.simmetrics.StringMetricBuilder.with;
import org.simmetrics.metrics.CosineSimilarity;
import org.simmetrics.metrics.JaccardSimilarity;
import org.simmetrics.metrics.Levenshtein;
import org.simmetrics.simplifiers.Simplifiers;
import org.simmetrics.tokenizers.Tokenizers;

/**
 *
 * @author Jose Luis Cullcay
 */
@SuppressWarnings("PMD")
@ApplicationScoped
public class DistanceServiceImpl implements DistanceService {

    @Inject
    private org.slf4j.Logger log;

    @Inject
    private CommonsServices commonService;

    //private SemanticDistance dist;
//    private static int one = 1;
    @Override
    public boolean semanticComparison(List<String> listA, List<String> listB) {
        try {
            SemanticDistance dist = new SemanticDistance();
            double value = dist.semanticKeywordsDistance(listA, listB);

            double semthreshold = Double.parseDouble(commonService.readPropertyFromFile("parameters.properties", "semanticDistanceListAListB"));
            if (value < semthreshold) {
                return true;
            }
        } catch (IOException | ClassNotFoundException | SQLException ex) {
            log.error("ERROR IN SemanticDistance:" + ex);
            //         Logger.getLogger(DistanceServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    @Override
    public Double semanticComparisonValue(List<String> listA, List<String> listB) {
        try {
            SemanticDistance dist = new SemanticDistance();
            return dist.nwdDistance(listA, listB); //double value = dist.semanticKeywordsDistance(listA, listB);

        } catch (IOException | ClassNotFoundException | SQLException ex) {
            log.error("ERROR IN SemanticDistance:" + ex);
        }
        return 1.11;
    }

    @Override
    public boolean semanticComparison(String word, List<String> listB) {
        try {
            List<String> listA = new ArrayList<>();
            listA.add(word);
            SemanticDistance dist = new SemanticDistance();

            double value = dist.semanticKeywordsDistance(listA, listB);
            double semthreshold = Double.parseDouble(commonService.readPropertyFromFile("parameters.properties", "semanticDistanceWordListB"));
            if (value < semthreshold) {
                return true;
            }
        } catch (IOException | ClassNotFoundException | SQLException ex) {
            log.error("ERROR IN SemanticDistance:" + ex);
            //         Logger.getLogger(DistanceServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    @Override
    public boolean syntacticComparisonNames(String... args) {

        try {
            SyntacticDistance syntacticdistance = new SyntacticDistance();
            if (args != null && args.length > 0) {
                for (int i = 0; i < args.length; i++) {
                    if (args[i] == null) {
                        return false;
                    }
                }
            } else {
                return false;
            }
            return syntacticdistance.compareNames(args);
        } catch (Exception ex) {
            Logger.getLogger(DistanceServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public double cosineSimilarityAndLevenshteinDistance(String param1, String param2) {

        String a = param1;
        String b = param2;

        StringMetric metric
                = with(new CosineSimilarity<String>())
                .simplify(Simplifiers.toLowerCase())
                .simplify(Simplifiers.removeNonWord()).simplifierCache()
                .tokenize(Tokenizers.qGram(3)).tokenizerCache().build();
        float compare = metric.compare(a, b);

        StringMetric metric2
                = with(new Levenshtein())
                .simplify(Simplifiers.removeDiacritics())
                .simplify(Simplifiers.toLowerCase()).build();

        float compare2 = metric2.compare(a, b);

        float similarity = (float) ((compare + compare2) / 2.0);

        return similarity;
    }

    @Override
    public float jaccardDistance(String param1, String param2) {
        StringMetric metric2
                = with(new JaccardSimilarity<String>())
                .simplify(Simplifiers.removeDiacritics())
                .simplify(Simplifiers.toLowerCase())
                .tokenize(Tokenizers.qGram(2)).tokenizerCache().build();

        return metric2.compare(param1, param2);
    }

    @Override
    public Boolean getEqualNames(String nombresOrig, String apellidosOrig, String otherName) {
        String otherGivenName;
        String otherLastName;
        Boolean equalNames = false;
        String[] split = otherName.split(" ");
        for (int i = 0; i < split.length - 1; i++) {
            String string = split[0];
            for (int j = 1; j <= i; j++) {
                string = string + " " + split[j];
            }
            otherGivenName = string;
            string = "";
            for (int j = i + 1; j < split.length; j++) {
                string = string + " " + split[j];
            }
            otherLastName = string;
            equalNames = getEqualNames(nombresOrig, apellidosOrig, otherGivenName, otherLastName);
            if (equalNames) {
                break;
            }
        }

        return equalNames;
    }

    @Override
    public Boolean getEqualNames(String nombresOrig, String apellidosOrig, String otherGivenName, String otherLastName) {
        int one = 1;

        nombresOrig = commonService.cleanNameArticles(nombresOrig);
        apellidosOrig = commonService.cleanNameArticles(apellidosOrig);
        otherGivenName = commonService.cleanNameArticles(otherGivenName);
        otherLastName = commonService.cleanNameArticles(otherLastName);

        boolean equal = false;
        //Getting the original names
        String givenName1 = commonService.removeAccents(nombresOrig.split(" ")[0]).toLowerCase().trim();
        String givenName2 = null;
        int numberGivenNames = nombresOrig.split(" ").length;
        if (numberGivenNames > one) {
            givenName2 = commonService.removeAccents(nombresOrig.split(" ")[1]).toLowerCase().trim();
        }

        String lastName1 = commonService.removeAccents(apellidosOrig.split(" ")[0]).toLowerCase().trim();
        String lastName2 = null;
        int numberLastNames = apellidosOrig.split(" ").length;
        if (numberLastNames > one) {
            lastName2 = commonService.removeAccents(apellidosOrig.split(" ")[1]).toLowerCase().trim();
        }

        //Getting the other names
        String otherGivenName1 = commonService.removeAccents(otherGivenName.split(" ")[0]).toLowerCase().trim();
        String otherGivenName2 = null;
        if (otherGivenName.split(" ").length > one) {
            otherGivenName2 = commonService.removeAccents(otherGivenName.split(" ")[1]).toLowerCase().trim();
        }

        String otherLastName1 = commonService.removeAccents(otherLastName.split(" ")[0]).toLowerCase().trim();
        String otherLastName2 = null;
        if (otherLastName.split(" ").length > one) {
            otherLastName2 = commonService.removeAccents(otherLastName.split(" ")[1]).toLowerCase().trim();
        }

        if (lastName2 != null && lastName2.length() == one && otherLastName2 != null && otherLastName2.trim().length() >= one) {
            otherLastName2 = otherLastName2.trim().substring(0, 1);
        }

        //Compare given names and surnames
        equal = compareNames(givenName1, givenName2, lastName1, lastName2,
                otherGivenName1, otherGivenName2, otherLastName1, otherLastName2);

        // 1. Busca 4 nombres sin acentos
        // 2. primer nombre y apellidos
        // 3. segundo nombre y apellidos
        // 5. segundo nombre y primer apellido (si hay mas de un nombre)
        // 4. primer nombre y primer apellido (si hay más de un nombre y un apellido)
        // 6. primera inicial y apellidos (si hay mas de un nombre y el primer nombre no es inicial solamente)
        // 7. primera inicial y primer apellido (si hay más de un apellido y el nombre no era solo inicial)
        // 8. segunda inicial y apellidos (si hay mas de un nombre)
        // 9. segunda inicial y primer apellido (si hay mas de un apellido)
        return equal;

    }

    /**
     * Delete along with {@link getEqualNamesWithoutInjects}.
     *
     * @param nombresOrig
     * @param apellidosOrig
     * @param otherName
     * @return
     */
    public Boolean getEqualNamesWithoutInjects(String nombresOrig, String apellidosOrig, String otherName) {
        String otherGivenName;
        String otherLastName;
        Boolean equalNames = false;
        String[] split = otherName.split(" ");
        for (int i = 0; i < split.length - 1; i++) {
            String string = split[0];
            for (int j = 1; j <= i; j++) {
                string = string + " " + split[j];
            }
            otherGivenName = string;
            string = "";
            for (int j = i + 1; j < split.length; j++) {
                string = string + " " + split[j];
            }
            otherLastName = string;
            equalNames = getEqualNamesWithoutInjects(nombresOrig, apellidosOrig, otherGivenName, otherLastName);
            if (equalNames) {
                break;
            }
        }

        return equalNames;
    }

    /**
     * Do not use in production. It's an auxiliary method to avoid injects
     * problems.
     *
     * @param nombresOrig
     * @param apellidosOrig
     * @param otherGivenName
     * @param otherLastName
     * @return
     */
    public Boolean getEqualNamesWithoutInjects(String nombresOrig, String apellidosOrig, String otherGivenName, String otherLastName) {
        int one = 1;

        nombresOrig = cleanNameArticles(nombresOrig);
        apellidosOrig = cleanNameArticles(apellidosOrig);
        otherGivenName = cleanNameArticles(otherGivenName);
        otherLastName = cleanNameArticles(otherLastName);

        boolean equal = false;
        //Getting the original names
        String givenName1 = StringUtils.stripAccents(nombresOrig.split(" ")[0]).toLowerCase().trim();
        String givenName2 = null;
        int numberGivenNames = nombresOrig.split(" ").length;
        if (numberGivenNames > one) {
            givenName2 = StringUtils.stripAccents(nombresOrig.split(" ")[1]).toLowerCase().trim();
        }

        String lastName1 = StringUtils.stripAccents(apellidosOrig.split(" ")[0]).toLowerCase().trim();
        String lastName2 = null;
        int numberLastNames = apellidosOrig.split(" ").length;
        if (numberLastNames > one) {
            lastName2 = StringUtils.stripAccents(apellidosOrig.split(" ")[1]).toLowerCase().trim();
        }

        //Getting the other names
        String otherGivenName1 = StringUtils.stripAccents(otherGivenName.split(" ")[0]).toLowerCase().trim();
        String otherGivenName2 = null;
        if (otherGivenName.split(" ").length > one) {
            otherGivenName2 = StringUtils.stripAccents(otherGivenName.split(" ")[1]).toLowerCase().trim();
        }

        String otherLastName1 = StringUtils.stripAccents(otherLastName.split(" ")[0]).toLowerCase().trim();
        String otherLastName2 = null;
        if (otherLastName.split(" ").length > one) {
            otherLastName2 = StringUtils.stripAccents(otherLastName.split(" ")[1]).toLowerCase().trim();
        }

        if (lastName2 != null && lastName2.length() == one && otherLastName2 != null && otherLastName2.trim().length() >= one) {
            otherLastName2 = otherLastName2.trim().substring(0, 1);
        }

        //Compare given names and surnames
        equal = compareNames(givenName1, givenName2, lastName1, lastName2,
                otherGivenName1, otherGivenName2, otherLastName1, otherLastName2);

        // 1. Busca 4 nombres sin acentos
        // 2. primer nombre y apellidos
        // 3. segundo nombre y apellidos
        // 5. segundo nombre y primer apellido (si hay mas de un nombre)
        // 4. primer nombre y primer apellido (si hay más de un nombre y un apellido)
        // 6. primera inicial y apellidos (si hay mas de un nombre y el primer nombre no es inicial solamente)
        // 7. primera inicial y primer apellido (si hay más de un apellido y el nombre no era solo inicial)
        // 8. segunda inicial y apellidos (si hay mas de un nombre)
        // 9. segunda inicial y primer apellido (si hay mas de un apellido)
        return equal;

    }

    /**
     * Delete along with {@link getEqualNamesWithoutInjects}.
     *
     * @param value to clean.
     * @return
     */
    private String cleanNameArticles(String value) {
        value = value.replace(".", "").trim()
                .replace("??", ".*")
                .replace("?", ".*").toLowerCase()
                .replaceAll(" de ", " ")
                .replaceAll("^del ", " ")
                .replaceAll(" del ", " ")
                .replaceAll(" los ", " ")
                .replaceAll(" y ", " ")
                .replaceAll(" las ", " ")
                .replaceAll(" la ", " ")
                .replaceAll("^de ", " ")
                .replaceAll("^los ", " ")
                .replaceAll("^las ", " ")
                .replaceAll("^la ", " ");

        return value;
    }

    public boolean compareNames(String givenName1, String givenName2, String lastName1, String lastName2,
            String otherGivenName1, String otherGivenName2, String otherLastName1, String otherLastName2) {
        boolean result = false;

        if (givenName2 != null && lastName2 != null) {

            if (otherGivenName2 != null && otherLastName2 != null) {
                if (compareExactStrings(givenName1, otherGivenName1) && compareExactStrings(givenName2, otherGivenName2)
                        && compareExactStrings(lastName1, otherLastName1) && compareExactStrings(lastName2, otherLastName2)) {
                    return true;
                }
            } else if (otherGivenName2 != null && otherLastName2 == null) {
                if (compareExactStrings(givenName1, otherGivenName1) && compareExactStrings(givenName2, otherGivenName2)
                        && compareExactStrings(lastName1, otherLastName1)) {
                    return true;
                }
            } else if (otherGivenName2 == null && otherLastName2 != null) {
                if ((compareExactStrings(otherGivenName1, givenName1) || compareExactStrings(otherGivenName1, givenName2))
                        && compareExactStrings(lastName1, otherLastName1) && compareExactStrings(lastName2, otherLastName2)) {
                    return true;
                }
            } else if (otherGivenName2 == null && otherLastName2 == null
                    && (compareExactStrings(otherGivenName1, givenName1) || compareExactStrings(otherGivenName1, givenName2))
                    && compareExactStrings(lastName1, otherLastName1)) {
                return true;

            }

        } else if (givenName2 == null && lastName2 != null) {
            if (otherGivenName2 != null && otherLastName2 != null) {
                if ((compareExactStrings(givenName1, otherGivenName1) || compareExactStrings(givenName1, otherGivenName2))
                        && compareExactStrings(lastName1, otherLastName1) && compareExactStrings(lastName2, otherLastName2)) {
                    return true;
                }
            } else if (otherGivenName2 != null && otherLastName2 == null) {
                if ((compareExactStrings(givenName1, otherGivenName1) || compareExactStrings(givenName1, otherGivenName2))
                        && compareExactStrings(lastName1, otherLastName1)) {
                    return true;
                }
            } else if (otherGivenName2 == null && otherLastName2 != null) {
                if (compareExactStrings(otherGivenName1, givenName1)
                        && compareExactStrings(lastName1, otherLastName1) && compareExactStrings(lastName2, otherLastName2)) {
                    return true;

                }
            } else if (otherGivenName2 == null && otherLastName2 == null
                    && compareExactStrings(otherGivenName1, givenName1) && compareExactStrings(lastName1, otherLastName1)) {
                return true;

            }

        } else if (givenName2 != null && lastName2 == null) {
            if (otherGivenName2 != null) {
                if (compareExactStrings(givenName1, otherGivenName1) && compareExactStrings(givenName2, otherGivenName2)
                        && compareExactStrings(lastName1, otherLastName1)) {
                    return true;
                }
            } else if (otherGivenName2 == null
                    && (compareExactStrings(otherGivenName1, givenName1) || compareExactStrings(otherGivenName1, givenName2))
                    && compareExactStrings(lastName1, otherLastName1)) {
                return true;

            }

        } else if (givenName2 == null && lastName2 == null) {
            if (otherGivenName2 != null) {
                if ((compareExactStrings(givenName1, otherGivenName1) || compareExactStrings(givenName1, otherGivenName2))
                        && compareExactStrings(lastName1, otherLastName1)) {
                    return true;
                }
            } else if (otherGivenName2 == null && compareExactStrings(otherGivenName1, givenName1)
                    && compareExactStrings(lastName1, otherLastName1)) {
                return true;

            }

        }
        return result;
    }

    public boolean compareExactStrings(String string1, String string2) {
        if (string1.length() == 1 || string2.length() == 1) {
            return string1.contains(string2) || string2.contains(string1);
        }
        return (string1.matches("^" + string2 + "$") || string2.matches("^" + string1 + "$") || jaccardDistance(string1, string2) > 0.85);
    }
}
