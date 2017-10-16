/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.commons.function;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.marmotta.ucuenca.wk.commons.impl.CommonsServicesImpl;
import static org.simmetrics.StringMetricBuilder.with;
import org.simmetrics.metrics.JaroWinkler;
import org.simmetrics.simplifiers.Simplifiers;

/**
 *
 * @author FernandoBac
 */
public class SyntacticDistance {

    private CommonsServicesImpl commonservices = new CommonsServicesImpl();
    private static String empty = "null";
    private static double threshold = 0.90;

    public boolean wOrder = false;

    public double dDistance(String nName1, String nName2) {

        List<String> tTks1 = tTokenizer(nName1.toLowerCase().replaceAll("(\\p{Lu})(\\p{Lu})", "$1 $2"));
        List<String> tTks2 = tTokenizer(nName2.toLowerCase().replaceAll("(\\p{Lu})(\\p{Lu})", "$1 $2"));

        Object[] prefc = cCountFullMatchs(tTks1, tTks2);

        Object[] prefc2 = cCountAbvMatchs(tTks1, tTks2);

        double c = (double) prefc[0];
        double c2 = (double) prefc2[0];

        double i = (int) prefc[1];
        double i2 = (int) prefc2[1];

        double mx = Math.min(tTks1.size(), tTks2.size());

        //System.out.println("a"+c);
        //System.out.println("b"+c2);
        //System.out.println("c"+c2);
        //mx = Tks1.size()+ Tks2.size();
        return ((c + c2) / (i + i2 + mx));
    }

    private Object[] cCountAbvMatchs(List<String> tk1, List<String> tk2) {
        double cCount = 0;
        int cCount2 = 0;
        List<Integer> uUsedT1 = new ArrayList<>();
        List<Integer> uUsedT2 = new ArrayList<>();
        for (int i = 0; i < tk1.size(); i++) {
            for (int j = 0; j < tk2.size(); j++) {
                if (!uUsedT1.contains(i) && !uUsedT2.contains(j)) {
                    String t1 = tk1.get(i);
                    String t2 = tk2.get(j);
                    boolean abv = (t1.length() < 3 && t2.length() >= 3) || (t2.length() < 3 && t1.length() >= 3);
                    boolean startsw = t1.startsWith(t2) || t2.startsWith(t1);
                    if (abv && startsw) {

                        double ix = tk1.size() - i;
                        double jx = tk2.size() - j;
                        ix = ix / (tk1.size() + 0.0);
                        jx = jx / (tk2.size() + 0.0);
                        double ij = Math.min(ix, jx) / Math.max(ix, jx);

                        cCount += wOrder ? ij * 0.90 : 0.90;
                        cCount2++;
                        uUsedT1.add(i);
                        uUsedT2.add(j);
                    }
                }
            }
        }
        Collections.sort(uUsedT1, Collections.reverseOrder());
        Collections.sort(uUsedT2, Collections.reverseOrder());

        for (int i : uUsedT1) {
            tk1.remove(i);
        }
        for (int i : uUsedT2) {
            tk2.remove(i);
        }
        return new Object[]{cCount, cCount2};
    }

    private Object[] cCountFullMatchs(List<String> tk1, List<String> tk2) {
        double cCount = 0;
        int cCount2 = 0;
        List<Integer> uUsedT1 = new ArrayList<>();
        List<Integer> uUsedT2 = new ArrayList<>();
        for (int i = 0; i < tk1.size(); i++) {
            for (int j = 0; j < tk2.size(); j++) {
                if (!uUsedT1.contains(i) && !uUsedT2.contains(j)) {
                    String t1 = tk1.get(i);
                    String t2 = tk2.get(j);
                    double sim = sSim(t1, t2);
                    if (sim > threshold) {
                        double ix = tk1.size() - i;
                        double jx = tk2.size() - j;
                        ix = ix / (tk1.size() + 0.0);
                        jx = jx / (tk2.size() + 0.0);
                        double ij = Math.min(ix, jx) / Math.max(ix, jx);
                        cCount += wOrder ? ij * sim : sim;
                        cCount2++;
                        uUsedT1.add(i);
                        uUsedT2.add(j);
                    }
                }
            }
        }
        Collections.sort(uUsedT1, Collections.reverseOrder());
        Collections.sort(uUsedT2, Collections.reverseOrder());

        for (int i : uUsedT1) {
            tk1.remove(i);
        }
        for (int i : uUsedT2) {
            tk2.remove(i);
        }
        return new Object[]{cCount, cCount2};
    }

    public double sSim(String t1, String t2) {
        return with(new JaroWinkler()).simplify(Simplifiers.removeDiacritics()).build().compare(t2, t1);
    }

    private String cClean(String n) {
        return n.replace(",", " ").replace(".", " ").replace("\"", " ").replace("'", " ").replace("-", " ");
    }

    private List<String> tTokenizer(String n) {
        n = cClean(n);
        String[] tokens = n.split(" ");
        List<String> list = new ArrayList<String>(Arrays.asList(tokens));
        list.removeAll(Arrays.asList("", null));
        return list;
    }

    public boolean compareNames(String... args) {
        List<String> sourcenames = getSplitNames(args[0], args[1]);

        List<String> targetnames = getSplitNames(args[2], args[3]);

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
    public double getComparisonValue(String source, String target) {
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
    public boolean compareStringNames(String source, String target, double valuecomparison) {

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

    /**
     * for local | scopus : syntax = LastName:FirstName
     *
     * @param source
     * @param fullname
     * @return
     */
    public List getSplitNames(String source, String fullname) {

        ArrayList<String> names = new ArrayList<String>();

        //clean some characters
        fullname = fullname.replace("??", "?");
        fullname = fullname.replace("(Dir.)", "");
        fullname = commonservices.removeAccents(fullname);
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
            //Insert nombre 1
            firstName = firstName.replace("=", "");
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

        //from LOCAL, SCOPUS AND GOOGLESCHOLAR
        if (source.toLowerCase().contains("scopus") || source.toLowerCase().contains("local")) {
            //Split FirstName and LastName
            String lastName = fullname.substring(0, fullname.indexOf(':'));
            String firstName = fullname.substring(fullname.indexOf(':') + 1);
            lastName = lastName.replace("-", " ");
            firstName = firstName.replace(":", " ");//when google scholar, in this case -> Mauricio:Espinoza:Mejia,primero viene el nombre luego apellidos
            firstName = firstName.replace(".", "");
            lastName = lastName.replace(".", "");
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

    public String getfirstgivenName(String firstName) {
        return firstName.substring(0, firstName.contains("_") ? firstName.indexOf('_') : firstName.length());
    }

    public String getrestofName(String firstName) {
        return firstName.contains("_") ? firstName.substring(firstName.indexOf('_') + 1) : empty;
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
    public double compareStrings(String source, String target) {
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
    private List wordLetterPairs(String str) {
        List allPairs = new ArrayList();
        // Tokenize the string and put the tokens/words into an array
        String[] words = str.split("\\s");
        // For each word
        for (int w = 0; w < words.length; w++) {
            // Find the pairs of characters
            String[] pairsInWord = letterPairs(words[w]);
            if (pairsInWord != null) {
                for (int p = 0; p < pairsInWord.length; p++) {
                    allPairs.add(pairsInWord[p]);
                }
            }
        }
        return allPairs;
    }

    /**
     * @return an array of adjacent letter pairs contained in the input string
     */
    private String[] letterPairs(String str) {
        try {
            int numPairs = str.length() - 1;
            String[] pairs = new String[numPairs];
            for (int i = 0; i < numPairs; i++) {
                pairs[i] = str.substring(i, i + 2);
            }
            return pairs;
        } catch (Exception e) {
            return null;
        }

    }

}
