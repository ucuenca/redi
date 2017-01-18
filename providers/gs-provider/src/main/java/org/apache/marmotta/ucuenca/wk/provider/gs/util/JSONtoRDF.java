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
            Matcher m = Pattern.compile("^(entity::property:creator)(.*)$").matcher(key);
            if (m.find()) {
                getAllAttributesAuthor(key, json, resource);
            } else {
                m = Pattern.compile("^(entity::property:)(.*)$").matcher(key);
                if (m.find()) {
                    getAllAttributes(m, key, json, resource);
                }
            }

        }

    }

    public void getAllAttributes(Matcher m, String key, JsonObject json, String resource) {
        if (json.has(m.group(2))) {
            String value = json.get(m.group(2)).getAsString();
            model.add(factory.createStatement(factory.createURI(resource),
                    factory.createURI(schema.get(key)), factory.createLiteral(value)));
        }

    }

    private void getAllAttributesAuthor(String key, JsonObject json, String resource) {
        if (json.has("author")) {
            String aux = json.get("author").getAsString();
            if (aux.length() > 0) {
                String guion = "… -";
                try {
                    aux = aux.substring(0, aux.indexOf(guion));
                } catch (Exception e) {
                    String guion2 = " - ";
                    aux = aux.substring(0, aux.indexOf(guion2));
                }
                String[] authors = aux.split(",");
                for (String author : authors) {
                    model.add(factory.createStatement(factory.createURI(resource),
                            factory.createURI(schema.get(key)), factory.createURI("https://scholar.google.com/scholar?start=0&q=author:%22" + author.replace(" ", "+") + "%22&hl=en&as_sdt=1%2C15&as_vis=1")));

                }
            }
        }

    }

}
