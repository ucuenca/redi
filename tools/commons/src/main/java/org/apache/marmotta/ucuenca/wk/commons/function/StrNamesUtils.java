/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.commons.function;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Jose Ortiz
 */
public final class StrNamesUtils {

    private StrNamesUtils() {
    }

    public static String or(String name) {
        return or(name, -1);
    }

    public static String or(String name, int max) {
        name = StringUtils.stripAccents(name).trim().toLowerCase().replaceAll("\\.|,|;|:|-|\n|\\\\|\\||\"|\'|_|/", " ").trim();
        String s = "";
        String[] tokens = name.split(" ");
        List<String> list = new ArrayList<String>(Arrays.asList(tokens));
        list.removeAll(Arrays.asList("", null));
        List<String> list2 = new ArrayList<String>();
        for (int i = 0; i < list.size(); i++) {
            if (max == -1 || max != -1 && list2.size() < max) {
                if (list.get(i).length() > 2) {
                    list2.add(list.get(i));
                }
            }
        }
        if (list2.isEmpty()) {
            list2.addAll(list);
        }
        if (max != -1 && max <= list2.size()) {
            list2 = list2.subList(0, max);
        }
        for (int i = 0; i < list2.size(); i++) {
            s += list2.get(i) + (i == list2.size() - 1 ? "" : "-");
        }
        return s;
    }

}
