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
        if (max!=-1 && max<=list.size()){
            list = list.subList(0, max);
        }
        for (int i = 0; i < list.size(); i++) {
            s += list.get(i) + (i == list.size() - 1 ? "" : "-");
        }
        return s;
    }
    
}
