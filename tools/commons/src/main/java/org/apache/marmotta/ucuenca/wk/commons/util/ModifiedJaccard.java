/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.commons.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static org.simmetrics.StringMetricBuilder.with;
import org.simmetrics.metrics.JaroWinkler;
import org.simmetrics.simplifiers.Simplifiers;

/**
 *
 * @author cedia
 */
public class ModifiedJaccard {

    public boolean prioritizeWordOrder = false;
    public boolean onlyCompleteMatchs = false;

    public double syntacticThreshold = 0.9;
    
    public double abvPenalty = 0.9;
    
    
    public double distanceJournalName(String name1, String name2) {
        
        syntacticThreshold = 0.99;
        List<String> tks1 = tokenizer(name1.toLowerCase());
        List<String> tks2 = tokenizer(name2.toLowerCase());
        Object[] countFM = countFullMatchs(tks1, tks2);
        //double c = (double) countFM[0];
        double i = (int) countFM[1];
        double mx = Math.min(tks1.size(), tks2.size());
        return i / (i + mx);
    }
    
    
    
    public double distanceName(String name1, String name2) {

        List<String> tks1 = tokenizer(name1.toLowerCase().replaceAll("(\\p{Lu})(\\p{Lu})", "$1 $2"));
        List<String> tks2 = tokenizer(name2.toLowerCase().replaceAll("(\\p{Lu})(\\p{Lu})", "$1 $2"));

        Object[] countFM = countFullMatchs(tks1, tks2);

        Object[] countAM = countAbvMatchs(tks1, tks2);

        double c = (double) countFM[0];
        double c2 = (double) countAM[0];

        double i = (int) countFM[1];
        double i2 = (int) countAM[1];

        double mx = Math.min(tks1.size(), tks2.size());

        return ((c + c2) / (i + i2 + mx));
    }

    private Object[] countAbvMatchs(List<String> tk1, List<String> tk2) {
        double count = 0;
        int count2 = 0;
        List<Integer> usedT1 = new ArrayList<>();
        List<Integer> usedT2 = new ArrayList<>();
        for (int i = 0; i < tk1.size(); i++) {
            for (int j = 0; j < tk2.size(); j++) {
                if (!usedT1.contains(i) && !usedT2.contains(j)) {
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

                        count += prioritizeWordOrder ? ij * abvPenalty : abvPenalty;
                        count2++;
                        usedT1.add(i);
                        usedT2.add(j);
                    }
                }
            }
        }
        Collections.sort(usedT1, Collections.reverseOrder());
        Collections.sort(usedT2, Collections.reverseOrder());

        for (int i : usedT1) {
            tk1.remove(i);
        }
        for (int i : usedT2) {
            tk2.remove(i);
        }
        return new Object[]{count, count2};
    }

    private Object[] countFullMatchs(List<String> tk1, List<String> tk2) {
        double count = 0;
        int count2 = 0;
        List<Integer> usedT1 = new ArrayList<>();
        List<Integer> usedT2 = new ArrayList<>();
        for (int i = 0; i < tk1.size(); i++) {
            for (int j = 0; j < tk2.size(); j++) {
                if (!usedT1.contains(i) && !usedT2.contains(j)) {
                    String t1 = tk1.get(i);
                    String t2 = tk2.get(j);
                    double sim = syntacticSim(t1, t2);
                    if (sim > syntacticThreshold) {
                        double ix = tk1.size() - i;
                        double jx = tk2.size() - j;
                        ix = ix / (tk1.size() + 0.0);
                        jx = jx / (tk2.size() + 0.0);
                        double ij = Math.min(ix, jx) / Math.max(ix, jx);
                        count += prioritizeWordOrder ? ij * sim : sim;
                        count2++;
                        usedT1.add(i);
                        usedT2.add(j);
                    }
                }
            }
        }
        Collections.sort(usedT1, Collections.reverseOrder());
        Collections.sort(usedT2, Collections.reverseOrder());

        for (int i : usedT1) {
            tk1.remove(i);
        }
        for (int i : usedT2) {
            tk2.remove(i);
        }
        return new Object[]{count, count2};
    }

    public double syntacticSim(String t1, String t2) {
        return with(new JaroWinkler()).simplify(Simplifiers.removeDiacritics()).build().compare(t2, t1);
    }

    public String specialCharactersClean(String n) {
        return n.replace(",", " ").replace(".", " ").replace("\"", " ").replace("'", " ").replace("-", " ").replace("\n", " ") ;
    }

    public List<String> tokenizer(String n) {
        n = specialCharactersClean(n);
        String[] tokens = n.split(" ");
        List<String> list = new ArrayList<String>(Arrays.asList(tokens));
        list.removeAll(Arrays.asList("", null));
        return list;
    }
}
