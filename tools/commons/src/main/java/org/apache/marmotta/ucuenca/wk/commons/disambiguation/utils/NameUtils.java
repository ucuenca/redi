/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.commons.disambiguation.utils;

import edu.emory.mathcs.backport.java.util.Collections;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.marmotta.ucuenca.wk.commons.disambiguation.Person;
import org.apache.marmotta.ucuenca.wk.commons.util.ModifiedJaccardMod;

/**
 *
 * @author Jose Ortiz
 */
@SuppressWarnings("PMD")
public class NameUtils {
 
    public static double compareName(String name1, String name2) {
        return compareName(Collections.singletonList(name1), Collections.singletonList(name2));
    }

    public static double compareName(List<String> name1, List<String> name2) {
        double sim = -1;
        int tipo = 0;
        String nf1 = "";
        String nf2 = "";
        String nl1 = "";
        String nl2 = "";
        String nc1 = "";
        String nc2 = "";
        if (name1.size() == 1) {
            if (name2.size() == 1) {
                tipo = 1;
                nc1 = name1.get(0);
                nc2 = name2.get(0);
            } else {
                tipo = 2;
                nc2 = name1.get(0);
                nf1 = name2.get(0);
                nl1 = name2.get(1);
            }
        } else if (name2.size() == 1) {
            tipo = 2;
            nc2 = name2.get(0);
            nf1 = name1.get(0);
            nl1 = name1.get(1);
        } else {
            tipo = 3;
            nf1 = name1.get(0);
            nl1 = name1.get(1);
            nf2 = name2.get(0);
            nl2 = name2.get(1);
        }
        ModifiedJaccardMod metric = new ModifiedJaccardMod();
        switch (tipo) {
            case 1:
                metric.prioritizeWordOrder = false;
                sim = metric.distanceName(nc1, nc2);
                break;
            case 2:
                metric.prioritizeWordOrder = false;
                sim = metric.distanceName(nf1 + " " + nl1, nc2);
                break;
            case 3:
                metric.prioritizeWordOrder = true;
                double sim1 = metric.distanceName(nl1, nl2);
                metric.prioritizeWordOrder = false;
                double sim2 = metric.distanceName(nf1, nf2);
                sim = (sim1 + sim2) / 2;
                break;
        }
        return sim;
    }

    public static List<List<String>> uniqueName(List<List<String>> options) {
        final double aggThreshold = Person.thresholdName;
        Set<Set<Integer>> ls = new HashSet<>();
        for (int i = 0; i < options.size(); i++) {
            for (int j = i + 1; j < options.size(); j++) {
                double sim = compareName(options.get(i), options.get(j));
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
            if (alone) {
                Set<Integer> hsalone = new HashSet<>();
                hsalone.add(i);
                ls_alone.add(hsalone);
            }
        }
        ls.addAll(ls_alone);

        List<List<String>> optsal = new ArrayList<>();
        for (Set<Integer> grp : ls) {
            List<List<String>> opt = new ArrayList<>();
            for (Integer i : grp) {
                opt.add(options.get(i));
            }
            List<String> bestName = bestName(opt);
            optsal.add(bestName);
        }
        return optsal;
    }

    public static List<String> bestName(List<List<String>> options) {

        int selection = -1;
        int selectionScore = -1;
        for (int i = 0; i < options.size(); i++) {
            List<String> get = options.get(i);

            int v1 = get.size();
            int v2 = 0;
            for (String n : get) {
                v2 += n.length();
            }

            int score = v1 * v2;

            if (score > selectionScore) {
                selectionScore = score;
                selection = i;
            }
        }
        return options.get(selection);
    }

}
