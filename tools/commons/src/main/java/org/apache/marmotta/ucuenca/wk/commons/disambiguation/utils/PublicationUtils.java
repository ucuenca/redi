/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.commons.disambiguation.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.marmotta.ucuenca.wk.commons.util.ModifiedJaccardMod;
import org.apache.marmotta.ucuenca.wk.commons.disambiguation.Person;

/**
 *
 * @author Jose Ortiz
 */
@SuppressWarnings("PMD")
public class PublicationUtils {

    public static double compareTitle(String name1, String name2) {
        ModifiedJaccardMod metric = new ModifiedJaccardMod();
        metric.prioritizeWordOrder = false;
        double sim = metric.distanceName(name1, name2).getValue();
        return sim;
    }

    public static List<String> uniqueTitle(List<String> options) {
        final double aggThreshold = Person.thresholdTitle;
        Set<Set<Integer>> ls = new HashSet<>();
        for (int i = 0; i < options.size(); i++) {
            for (int j = i + 1; j < options.size(); j++) {
                double sim = compareTitle(options.get(i), options.get(j));
                if (sim >= aggThreshold) {
                    Set<Integer> lss = new HashSet<>();
                    lss.add(i);
                    lss.add(j);
                    Set<Integer> lsn = null;
                    for (Set<Integer> p : ls) {
                        if (p.contains(i) || p.contains(j)) {
                            lsn = p;
                            break;
                        }
                    }
                    if (lsn == null) {
                        ls.add(lss);
                    } else {
                        lsn.addAll(lss);
                    }
                }
            }
        }
        Set<Set<Integer>> ls_alone = new HashSet<>();
        for (int i = 0; i < options.size(); i++) {
            boolean alone = true;
            for (Set<Integer> ung : ls) {
                if (ung.contains(i)) {
                    alone = false;
                    break;
                }
            }
            if (alone){
                Set<Integer> hsalone = new HashSet<>();
                hsalone.add(i);
                ls_alone.add(hsalone);
            }
        }
        ls.addAll(ls_alone);
        
        List<String> optsal = new ArrayList<>();
        for (Set<Integer> grp : ls) {
            List<String> opt = new ArrayList<>();
            for (Integer i : grp) {
                opt.add(options.get(i));
            }
            String bestName = bestTitle(opt);
            optsal.add(bestName);
        }
        return optsal;
    }

    public static String bestTitle(List<String> options) {
        String longestWord = null;
        for (String word : options) {
            if (longestWord == null || word.length() > longestWord.length()) {
                longestWord = word;
            }
        }
        return longestWord;
    }

}
