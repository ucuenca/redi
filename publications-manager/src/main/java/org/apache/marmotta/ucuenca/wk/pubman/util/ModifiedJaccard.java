/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.pubman.util;

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

    public boolean Order = false;
    public boolean CompleteMatchs = false;

    public double Threshold = 0.9;
    
    public double AbvPenalty = 0.9;
    
    
    public double DistanceJournalName(String Name1, String Name2) {
        
        Threshold = 0.99;
        List<String> Tks1 = Tokenizer(Name1.toLowerCase());
        List<String> Tks2 = Tokenizer(Name2.toLowerCase());
        Object[] c_ = CountFullMatchs(Tks1, Tks2);
        double c = (double) c_[0];
        double i = (int) c_[1];
        double mx = Math.min(Tks1.size(), Tks2.size());
        return i / (i + mx);
    }
    
    
    
    public double DistanceName(String Name1, String Name2) {

        List<String> Tks1 = Tokenizer(Name1.toLowerCase().replaceAll("(\\p{Lu})(\\p{Lu})", "$1 $2"));
        List<String> Tks2 = Tokenizer(Name2.toLowerCase().replaceAll("(\\p{Lu})(\\p{Lu})", "$1 $2"));

        Object[] c_ = CountFullMatchs(Tks1, Tks2);

        Object[] c2_ = CountAbvMatchs(Tks1, Tks2);

        double c = (double) c_[0];
        double c2 = (double) c2_[0];

        double i = (int) c_[1];
        double i2 = (int) c2_[1];

        double mx = Math.min(Tks1.size(), Tks2.size());

        return ((c + c2) / (i + i2 + mx));
    }

    private Object[] CountAbvMatchs(List<String> tk1, List<String> tk2) {
        double Count = 0;
        int Count2 = 0;
        List<Integer> UsedT1 = new ArrayList<>();
        List<Integer> UsedT2 = new ArrayList<>();
        for (int i = 0; i < tk1.size(); i++) {
            for (int j = 0; j < tk2.size(); j++) {
                if (!UsedT1.contains(i) && !UsedT2.contains(j)) {
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

                        Count += Order ? ij * AbvPenalty : AbvPenalty;
                        Count2++;
                        UsedT1.add(i);
                        UsedT2.add(j);
                    }
                }
            }
        }
        Collections.sort(UsedT1, Collections.reverseOrder());
        Collections.sort(UsedT2, Collections.reverseOrder());

        for (int i : UsedT1) {
            tk1.remove(i);
        }
        for (int i : UsedT2) {
            tk2.remove(i);
        }
        return new Object[]{Count, Count2};
    }

    private Object[] CountFullMatchs(List<String> tk1, List<String> tk2) {
        double Count = 0;
        int Count2 = 0;
        List<Integer> UsedT1 = new ArrayList<>();
        List<Integer> UsedT2 = new ArrayList<>();
        for (int i = 0; i < tk1.size(); i++) {
            for (int j = 0; j < tk2.size(); j++) {
                if (!UsedT1.contains(i) && !UsedT2.contains(j)) {
                    String t1 = tk1.get(i);
                    String t2 = tk2.get(j);
                    double sim = Sim(t1, t2);
                    if (sim > Threshold) {
                        double ix = tk1.size() - i;
                        double jx = tk2.size() - j;
                        ix = ix / (tk1.size() + 0.0);
                        jx = jx / (tk2.size() + 0.0);
                        double ij = Math.min(ix, jx) / Math.max(ix, jx);
                        Count += Order ? ij * sim : sim;
                        Count2++;
                        UsedT1.add(i);
                        UsedT2.add(j);
                    }
                }
            }
        }
        Collections.sort(UsedT1, Collections.reverseOrder());
        Collections.sort(UsedT2, Collections.reverseOrder());

        for (int i : UsedT1) {
            tk1.remove(i);
        }
        for (int i : UsedT2) {
            tk2.remove(i);
        }
        return new Object[]{Count, Count2};
    }

    public double Sim(String t1, String t2) {
        return with(new JaroWinkler()).simplify(Simplifiers.removeDiacritics()).build().compare(t2, t1);
    }

    public String Clean(String n) {
        return n.replace(",", " ").replace(".", " ").replace("\"", " ").replace("'", " ").replace("-", " ").replace("\n", " ") ;
    }

    public List<String> Tokenizer(String n) {
        n = Clean(n);
        String[] tokens = n.split(" ");
        List<String> list = new ArrayList<String>(Arrays.asList(tokens));
        list.removeAll(Arrays.asList("", null));
        return list;
    }
}
