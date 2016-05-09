/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.commons.impl;

import java.util.ArrayList;
import java.util.List;
import org.apache.marmotta.ucuenca.wk.commons.service.ComparisonNames;

/**
 *
 * @author Satellite
 */
public class ComparisonNamesImpl implements ComparisonNames {

    private static String empty = "null";
    private static double threshold = 0.95;

    @Override
    public boolean syntacticComparison(String... args) {

        List<String> sourcenames = getSplitNames(args[0], args[1]);

        List<String> targetnames = getSplitNames(args[2], args[3]);

        return compareNames(sourcenames, targetnames);

    }

    public static boolean compareNames(List<String> sourcenames, List<String> targetnames) {
        List<Double> valueOfComparison = new ArrayList<Double>();
        valueOfComparison.add(getComparisonValue(sourcenames.get(0), targetnames.get(0)));
        valueOfComparison.add(getComparisonValue(sourcenames.get(1), targetnames.get(1)));
        valueOfComparison.add(getComparisonValue(sourcenames.get(2), targetnames.get(2)));
        valueOfComparison.add(getComparisonValue(sourcenames.get(3), targetnames.get(3)));
        if (valueOfComparison.get(0) > threshold && valueOfComparison.get(1) > threshold && valueOfComparison.get(2) > threshold && valueOfComparison.get(3) > threshold) {
            return true; //si tienen dos nombres y dos apellidos exactos 
        }
        if ((sourcenames.get(0).charAt(0) != targetnames.get(0).charAt(0)) || (sourcenames.get(2).charAt(0) != targetnames.get(2).charAt(0))) {
            return false; //si los caracteres iniciales de los primeros apellidos, y primeros nombres no coincide
        }
        //compara primeros apellidos, segundos apellidos, primeros nombres y segundos nombres
        for (int i = 0; i < 4; i++) {
            if (!compareStringNames(sourcenames.get(i), targetnames.get(i), valueOfComparison.get(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * return string distance between two values
     *
     * @param source
     * @param target
     * @return
     */
    public static double getComparisonValue(String source, String target) {
        return compareStrings(!source.contains(empty) ? source : "0", !target.contains(empty) ? target : "1");
    }

    /**
     * This function compare individual names
     *
     * @param source
     * @param target
     * @param valuecomparison //value-distance of compare strings ( 0.0 - 1.0 )
     * @return
     */
    public static boolean compareStringNames(String source, String target, double valuecomparison) {

        if (!((source.contains(empty)) || (target.contains(empty)))) {
            //si tienen la misma longitud deberian ser exactamente iguales
            if ((source.length() == target.length()) && (valuecomparison < threshold)) {
                return false;
            } else if (source.compareTo(target) == 0) {
                return true;
            } else if (!(((source.length() == 1) || (target.length() == 1)) && (source.charAt(0) == target.charAt(0)))) { //si no tienen la misma longitud uno de los nombres debe tener UNA SOLA letra
                return false; //si la letra inicial no coincide
            }
        }
        return true;
    }

    public static List getSplitNames(String source, String fullname) {
        ArrayList<String> names = new ArrayList<String>();
        //clean some characters
        fullname = fullname.replace("??", "?");
        fullname = fullname.replace("(Dir.)", "");
        fullname = eliminarAcentos(fullname);
        fullname = fullname.toUpperCase();

        //from DBLP
        if (source.toLowerCase().contains("dblp")) {
            String tildepart = "ACUTE";
            //Delete "acute" chars of accents
            while (fullname.indexOf(tildepart) > -1) {
                fullname = fullname.substring(0, fullname.indexOf(tildepart) - 2)
                        + fullname.substring(fullname.indexOf(tildepart) - 1, fullname.indexOf(tildepart))
                        + fullname.substring(fullname.indexOf(tildepart) + 6);
            }
            //Split FirstName and LastName
            String lastName = fullname.substring(0, fullname.indexOf(':'));
            String firstName = fullname.substring(fullname.indexOf(':') + 1);
            String partfirstName = "";
            //insert nombre 1
            String name1toAdd = getfirstgivenName(firstName);
            names.add(name1toAdd);
            partfirstName = getrestofName(firstName);
            if (partfirstName.indexOf('_') > -1) {
                names.add(partfirstName.substring(0, partfirstName.indexOf('_')));  //insert nombre 2
                names.add(partfirstName.substring(partfirstName.indexOf('_') + 1)); //insert apellido 1
            } else {
                names.add(partfirstName);//insert nombre 2
            }
            if (lastName.indexOf('=') > -1) {
                names.add(lastName.substring(0, lastName.indexOf('=')));
                names.add(lastName.substring(lastName.indexOf('=') + 1));
            } else {
                names.add(lastName);
                names.add(empty);
            }
        }//end if DBLP

        //from SCOPUS
        if (source.toLowerCase().contains("scopus") || source.toLowerCase().contains("local")) {
            //Split FirstName and LastName
            String lastName = fullname.substring(0, fullname.indexOf(':'));
            String firstName = fullname.substring(fullname.indexOf(':') + 1);
            lastName = lastName.replace("-", " ");
            firstName = firstName.replace(".","");
            if (firstName.indexOf(' ') > -1) {
                names.add(firstName.substring(0, firstName.indexOf(' ')));
                names.add(firstName.substring(firstName.indexOf(' ') + 1));
            } else {
                names.add(firstName);
                names.add(empty);
            }
            if (lastName.indexOf(' ') > -1) {
                names.add(lastName.substring(0, lastName.indexOf(' ')));
                names.add(lastName.substring(lastName.indexOf(' ') + 1));
            } else {
                names.add(lastName);
                names.add(empty);
            }
        }
        return names;
    }

    public static String getfirstgivenName(String firstName) {
        return firstName.substring(0, firstName.contains("_") ? firstName.indexOf('_') : firstName.length());
    }

    public static String getrestofName(String firstName) {
        return firstName.contains("_") ? firstName.substring(firstName.indexOf('_') + 1) : empty;
    }

    public static String eliminarAcentos(String input) {
        // Cadena de caracteres original a sustituir.
        String original = "áàäéèëíìïóòöúùuñÁÀÄÉÈËÍÌÏÓÒÖÚÙÜÑçÇ";
        // Cadena de caracteres ASCII que reemplazarán los originales.
        String ascii = "aaaeeeiiiooouuunAAAEEEIIIOOOUUUNcC";
        String output = input;
        for (int i = 0; i < original.length(); i++) {
            // Reemplazamos los caracteres especiales.
            output = output.replace(original.charAt(i), ascii.charAt(i));
        }//for i
        return output;
    }

    /**
     * PROVENANACE ALGORITHM:
     * http://www.catalysoft.com/articles/StrikeAMatch.html Catalysoft Limited,
     * 31 Flack End, Cambridge CB4 2WQ UK
     *
     * @param source
     * @param target
     * @return lexical similarity value in the range [0,1]
     */
    public static double compareStrings(String source, String target) {
        List pairs1 = new ArrayList();
        List pairs2 = new ArrayList();
        pairs1 = wordLetterPairs(source.toUpperCase());
        pairs2 = wordLetterPairs(target.toUpperCase());

        int intersection = 0;
        int union = pairs1.size() + pairs2.size();
        for (int i = 0; i < pairs1.size(); i++) {
            Object pair1 = pairs1.get(i);
            for (int j = 0; j < pairs2.size(); j++) {
                Object pair2 = pairs2.get(j);
                if (pair1.equals(pair2)) {
                    intersection++;
                    pairs2.remove(j);
                    break;
                }
            }
        }
        return (2.0 * intersection) / union;
    }

    /**
     * @return an ArrayList of 2-character Strings.
     */
    private static List wordLetterPairs(String str) {
        List allPairs = new ArrayList();
        // Tokenize the string and put the tokens/words into an array
        String[] words = str.split("\\s");
        // For each word
        for (int w = 0; w < words.length; w++) {
            // Find the pairs of characters
            String[] pairsInWord = letterPairs(words[w]);
            for (int p = 0; p < pairsInWord.length; p++) {
                allPairs.add(pairsInWord[p]);
            }
        }
        return allPairs;
    }

    /**
     * @return an array of adjacent letter pairs contained in the input string
     */
    private static String[] letterPairs(String str) {
        int numPairs = str.length() - 1;
        String[] pairs = new String[numPairs];
        for (int i = 0; i < numPairs; i++) {
            pairs[i] = str.substring(i, i + 2);
        }
        return pairs;
    }

}
