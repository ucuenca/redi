/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.pubman.harvesters;

import static org.apache.marmotta.ucuenca.wk.pubman.harvesters.SameAsVIVO.gRepository;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.ucuenca.wk.pubman.utils.MapSet;
import org.openrdf.model.Value;
import org.openrdf.query.Binding;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sparql.SPARQLRepository;

/**
 *
 * @author cedia
 */
public class Group {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws RepositoryException, MarmottaException {
        // TODO code application logic here

        final SPARQLRepository data_rp = gRepository("data");

        List<Map<String, Value>> query = query(data_rp, QueryLanguage.SPARQL, "PREFIX dct: <http://purl.org/dc/terms/>\n"
                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
                + "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
                + "select distinct ?c ?cx {\n"
                + "            graph <https://redi.cedia.edu.ec/context/redi> {\n"
                + "                ?p dct:isPartOf ?c .\n"
                + "                ?c a ?t .\n"
                + "        		optional {\n"
                + "                    ?p dct:isPartOf ?cx .\n"
                + "                    ?cx a ?t .\n"
                + "            		filter (str(?c) > str(?cx)) .\n"
                + "        		}\n"
                + "            }\n"
                + "}");

        MapSet ms;
        ms = new MapSet(new HashMap<String, Set<String>>());

        for (Map<String, Value> mp : query) {
            Value get1 = mp.get("cx");
            if (get1 == null) {
                ms.put(mp.get("c").stringValue());
            } else {
                ms.put(mp.get("c").stringValue(), get1.stringValue());
            }
        }
        
        System.out.println(ms.values().size());
        for (Set<String> cc: ms.values()){
            System.out.println(cc.size());
        }
        
        
    }

    public static SPARQLRepository gRepository(String dbs) throws RepositoryException {
        final SPARQLRepository data = new SPARQLRepository("http://201.159.222.25:8180/repositories/" + dbs, "http://201.159.222.25:8180/repositories/" + dbs + "/statements");
        ConcurrentHashMap<String, String> additionalHttpHeaders = new ConcurrentHashMap<>();
        additionalHttpHeaders.put("Accept", "application/sparql-results+json,*/*;q=0.9");
        data.setAdditionalHttpHeaders(additionalHttpHeaders);
        data.setUsernameAndPassword("rediclon", "5783b10a8f22$mznx");
        data.initialize();

        return data;
    }

    public static List<Map<String, Value>> query(SPARQLRepository rx, QueryLanguage ql, String string) throws MarmottaException {
        List<Map<String, Value>> r = new ArrayList<>();
        try {
            RepositoryConnection connection = rx.getConnection();
            TupleQueryResult evaluate = connection.prepareTupleQuery(ql, string).evaluate();
            while (evaluate.hasNext()) {
                Iterator<Binding> iterator = evaluate.next().iterator();
                ConcurrentHashMap<String, Value> mp = new ConcurrentHashMap<>();
                while (iterator.hasNext()) {
                    Binding next = iterator.next();
                    mp.put(next.getName(), next.getValue());
                }
                r.add(mp);
            }
            connection.close();
        } catch (Exception ex) {
            throw new MarmottaException(ex + "" + string);
        }
        return r;
    }

}
