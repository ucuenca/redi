/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.edu.cedia.redi.ldclient.provider.ak.mapping;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import ec.edu.cedia.redi.ldclient.provider.json.AbstractJSONDataProvider;
import ec.edu.cedia.redi.ldclient.provider.json.mappers.JsonPathValueMapper;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minidev.json.JSONObject;
import org.apache.marmotta.ldclient.exception.DataRetrievalException;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;

/**
 *
 * @author cedia
 */
public abstract class AbstractJSONDataProviderMod extends AbstractJSONDataProvider {

  @Override
  protected List<String> parseResponse(String resource, String requestUrl, Model triples, InputStream inp, String contentType) throws DataRetrievalException {
    ReadContext ctx = JsonPath.parse(inp, getConfiguration());
    ValueFactory vf = ValueFactoryImpl.getInstance();

    URI subject = vf.createURI(resource);
    for (Map.Entry<String, JsonPathValueMapper> mapping : getMappings(resource, requestUrl).entrySet()) {
      URI predicate = vf.createURI(mapping.getKey());

      List a = ctx.read(mapping.getValue().getPath());
      for (Object o : a) {
        String value;
        if (o == null) {
          continue;
        } else {
          value = String.valueOf(o);
          if (o instanceof LinkedHashMap) {
            JSONObject json = new JSONObject();
            json.putAll((LinkedHashMap) o);
            value = json.toJSONString();
          }
        }

        List<Value> objects = mapping.getValue().map(resource, value, vf);
        for (Value object : objects) {
          triples.add(subject, predicate, object);
        }
      }
    }

    URI ptype = vf.createURI(RDF.NAMESPACE + "type");
    for (String typeUri : getTypes(subject)) {
      Resource typeresource = vf.createURI(typeUri);
      triples.add(vf.createStatement(subject, ptype, typeresource));
    }
    return Collections.emptyList();
  }
}
