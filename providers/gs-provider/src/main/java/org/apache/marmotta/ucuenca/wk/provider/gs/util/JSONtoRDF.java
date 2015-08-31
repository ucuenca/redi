package org.apache.marmotta.ucuenca.wk.provider.gs.util;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openrdf.model.Model;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class JSONtoRDF {

    private String resource;
    private JsonArray data;
    private Model model;
    private Map<String, String> schema;
    private final ValueFactory factory = ValueFactoryImpl.getInstance();

    public JSONtoRDF(String resource, Map<String, String> schema, JsonArray data, Model model) {
        this.resource = resource;
        this.schema = schema;
        this.data = data;
        this.model = model;
    }

    public void parse() throws Exception {
        Integer i = 0;
        while (i < data.size()) {
            JsonObject json = data.get(i).getAsJsonObject();
            if (json.has("link")) {
                String value = json.get("link").getAsString();
                model.add(factory.createStatement(factory.createURI(resource),
                        factory.createURI(schema.get("entity::property:link")), factory.createURI(value)));
                this.mappingProcess(value, json);
            }
            
            i++;
        }
        /*
         PrintWriter out = new PrintWriter("/home/santteegt/JSON" + this.hashCode() + ".ttl");
         out.print(data.toString());
         out.close();*/
    }

    private void mappingProcess(String resource, JsonObject json) {
        for (String key : schema.keySet()) {
            if (key.matches("^entity::type$")) {
                model.add(factory.createStatement(factory.createURI(resource),
                        RDF.TYPE, factory.createURI(schema.get("entity::type"))));
                continue;
            }
            Matcher m = Pattern.compile("^(entity::property:)(.*)$").matcher(key);
            if (m.find()) {
                if (json.has(m.group(2))) {
                    String value = json.get(m.group(2)).getAsString();
                    model.add(factory.createStatement(factory.createURI(resource),
                            factory.createURI(schema.get(key)), factory.createLiteral(value)));
                }
            }

        }

    }

}
