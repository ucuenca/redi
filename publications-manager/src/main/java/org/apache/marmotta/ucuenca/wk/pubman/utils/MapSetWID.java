/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.pubman.utils;

import com.google.common.collect.Sets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author cedia
 */
public class MapSetWID {

    Map<String, Set<String>> data; //bucket, <URIS>
    Map<String, String> index; // URI, bucket
    Set<String> delete; // URI, bucket

    public Map<String, Set<String>> getData() {
        return data;
    }

    public void setData(Map<String, Set<String>> data) {
        this.data = data;
    }

    public Map<String, String> getIndex() {
        return index;
    }

    public void setIndex(Map<String, String> index) {
        this.index = index;
    }

    public Set<String> getDelete() {
        return delete;
    }

    public void setDelete(Set<String> delete) {
        this.delete = delete;
    }

    
    
    
    
    public Set<String> findBuckets(Set<String> uris) {
        Set<String> r = new HashSet<>();
        for (String m : uris) {
            if (index.containsKey(m)) {
                r.add(index.get(m));
            }
        }
        return r;
    }

    public MapSetWID() {
        this.data = new HashMap<>();
        this.index = new HashMap<>();
        this.delete = new HashSet<>();
    }

    public void clear() {
        data.clear();
        index.clear();
    }

    public void deleteRest(List<String> ls) {
        String n = ls.get(0);
        Set<String> del = Sets.newHashSet();
        for (int i = 1; i < ls.size(); i++) {
            del.addAll(data.remove(ls.get(i)));
            delete.add(ls.get(i));
        }
        data.get(n).addAll(del);
        for (String c : del) {
            index.put(c, n);
        }
    }

    public void put(Map<String, Set<String>> mp) {
        for (Map.Entry<String, Set<String>> next : mp.entrySet()) {
            put(next.getKey(), next.getValue());
        }
    }

    public void put(String bucket, Set<String> uris) {
        if (!data.containsKey(bucket)) {
            data.put(bucket, new HashSet<String>());
        }
        data.get(bucket).addAll(uris);
        for (String c : uris) {
            index.put(c, bucket);
        }
    }

}
