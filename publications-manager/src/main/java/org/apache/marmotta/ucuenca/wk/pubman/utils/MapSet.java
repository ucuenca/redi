/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.pubman.utils;

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MapSet {

    Map<String, Set<String>> data;

    public MapSet(Map<String, Set<String>> data) {
        this.data = data;
    }

    public void clear() {
        data.clear();
    }

    public Collection<Set<String>> values() {
        return this.data.values();
    }

    public synchronized void put(Set<String> ks) {
        Set<String> new_set = Sets.newConcurrentHashSet();
        for (String k : ks) {
            new_set.add(k);
            Set<String> get = this.data.get(k);
            if (get != null) {
                new_set.addAll(get);
            }
        }
        for (String k : new_set) {
            this.data.put(k, new_set);
        }
    }

    public synchronized void put(String k1) {
        if (!data.containsKey(k1)) {
            Set<String> hsalone = new HashSet<>();
            hsalone.add(k1);
            data.put(k1, hsalone);
        }
    }

    public synchronized void put(String k1, String k2) {
        if (k2 == null) {
            put(k1);
        } else {
            Set<String> s1 = data.get(k1);
            Set<String> s2 = data.get(k2);
            if (s1 == null && s2 == null) {
                Set<String> as = new HashSet<>();
                as.add(k1);
                as.add(k2);
                data.put(k1, as);
                data.put(k2, as);
            } else if (s1 != null && s2 == null) {
                s1.add(k2);
                data.put(k2, s1);
                data.put(k1, s1);
            } else if (s1 == null && s2 != null) {
                s2.add(k1);
                data.put(k1, s2);
                data.put(k2, s2);
            } else if (s1 != null && s2 != null) {
                Set<String> as = new HashSet<>();
                as.addAll(s1);
                as.addAll(s2);
                for (String sx : as) {
                    data.put(sx, as);
                }
            }
        }
    }

}
