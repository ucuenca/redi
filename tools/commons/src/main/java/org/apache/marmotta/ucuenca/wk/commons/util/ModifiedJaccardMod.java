/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.commons.util;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import static org.simmetrics.StringMetricBuilder.with;
import org.simmetrics.metrics.JaroWinkler;
import org.simmetrics.simplifiers.Simplifiers;

/**
 *
 * @author cedia
 */
public class ModifiedJaccardMod {

    public boolean prioritizeWordOrder = false;
    public boolean onlyCompleteMatchs = false;
    public double syntacticThreshold = 0.89;
    public double abvPenalty = 0.95;

    public double distanceName(String name1, String name2) {
        List<String> tks1 = tokenizer(name1.toLowerCase());
        List<String> tks2 = tokenizer(name2.toLowerCase());
        onlyCompleteMatchs = true;
        Map.Entry<Integer, Double> c = countMatchs(tks1, tks2);
        onlyCompleteMatchs = false;
        Map.Entry<Integer, Double> c1 = countMatchs(tks1, tks2);
        double mx = Math.min(tks1.size(), tks2.size());

        return (c.getValue() + c1.getValue()) / (c.getKey() + c1.getKey() + mx);
    }

    private Map.Entry<Integer, Double> countMatchs(List<String> tokens1, List<String> tokens2) {
        double sumSimilarity = 0;
        int countMatchs = 0;
        List<Integer> usedTokens1 = new ArrayList<>();
        List<Integer> usedTokens2 = new ArrayList<>();
        for (int i = 0; i < tokens1.size(); i++) {
            for (int j = 0; j < tokens2.size(); j++) {
                if (!usedTokens1.contains(i) && !usedTokens2.contains(j)) {
                    String token1 = tokens1.get(i);
                    String token2 = tokens2.get(j);
                    double sim = syntacticSim(token1, token2);
                    boolean abv = (token1.length() < 3 && token2.length() >= 3) || (token2.length() < 3 && token1.length() >= 3);
                    boolean startsw = token1.startsWith(token2) || token2.startsWith(token1);
                    boolean condFullMatch = sim >= syntacticThreshold;
                    boolean condAbvMatch = abv && startsw;
                    if (condAbvMatch) {
                        sim = abvPenalty;
                    }
                    if (onlyCompleteMatchs && condFullMatch || !onlyCompleteMatchs && condAbvMatch) {
                        double ix = tokens1.size() - i;
                        double jx = tokens2.size() - j;
                        ix = ix / (tokens1.size() + 0.0);
                        jx = jx / (tokens2.size() + 0.0);
                        double ij = Math.min(ix, jx) / Math.max(ix, jx);
                        sumSimilarity += prioritizeWordOrder ? ij * sim : sim;
                        countMatchs++;
                        usedTokens1.add(i);
                        usedTokens2.add(j);
                    }
                }
            }
        }
        Collections.sort(usedTokens1, Collections.reverseOrder());
        Collections.sort(usedTokens2, Collections.reverseOrder());

        for (int i : usedTokens1) {
            tokens1.remove(i);
        }
        for (int i : usedTokens2) {
            tokens2.remove(i);
        }

        return new AbstractMap.SimpleEntry<>(countMatchs, sumSimilarity);
    }

    public double syntacticSim(String t1, String t2) {
        return with(new JaroWinkler()).simplify(Simplifiers.removeDiacritics()).build().compare(t2, t1);
    }

    public String specialCharactersClean(String n) {
        return n.replaceAll("\\.|,|;|:|-|\n|\\\\|\\||\"|\'|_|/", " ");
    }

    public List<String> tokenizer(String n) {
        n = specialCharactersClean(n);
        String[] tokens = n.split(" ");
        List<String> list = new ArrayList<>(Arrays.asList(tokens));
        list.removeAll(Arrays.asList("", null));
        return list;
    }
}
