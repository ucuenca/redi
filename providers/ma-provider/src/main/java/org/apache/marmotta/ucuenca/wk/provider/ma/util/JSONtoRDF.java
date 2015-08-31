package org.apache.marmotta.ucuenca.wk.provider.ma.util;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openrdf.model.Model;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
//import static org.apache.marmotta.ucuenca.wk.provider.ma.MicrosoftAcademicsProvider.PATTERN;
//import org.openrdf.model.impl.EmptyModel;

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

    public void buldHead() {
        model.add(factory.createStatement(factory.createURI(resource),
                RDF.TYPE, factory.createURI(schema.get("entity::type")))
        );

        int i = 0;

        while (i < data.size()) {
            JsonObject json = data.get(i).getAsJsonObject();

            model.add(factory.createStatement(factory.createURI(resource),
                    factory.createURI("http://xmlns.com/foaf/0.1/publications"), factory.createURI("http://academic.research.microsoft.com/Publication/" + json.get("id").getAsString() + "/")));
            i++;
        }

    }

    public void parse() throws Exception {
        Integer i = 0;
        buldHead();
        while (i < data.size()) {
            JsonObject json = data.get(i).getAsJsonObject();
            this.mappingProcess("http://academic.research.microsoft.com/Publication/" + json.get("id").getAsString() + "/", json);
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
            m = Pattern.compile("^(entity::property:abstract)(.*)$").matcher(key);
            if (m.find()) {

                String value = json.get("abstractt").getAsString();
                model.add(factory.createStatement(factory.createURI(resource),
                        factory.createURI(schema.get(key)), factory.createLiteral(value)));
            }
            m = Pattern.compile("^(entity::property:fullversionurl)(.*)$").matcher(key);
            if (m.find()) {
                //if(key.matches("^rdf::property:")) {
                JsonArray aux = json.get("fullVersionURL").getAsJsonArray();
                for (JsonElement version : aux) {
                    model.add(factory.createStatement(factory.createURI(resource),
                            factory.createURI(schema.get(key)), factory.createURI(version.getAsString())));

                }

            }
            m = Pattern.compile("^(entity::property:date)(.*)$").matcher(key);
            if (m.find()) {
                //if(key.matches("^rdf::property:")) {
                String value = json.get("year").getAsString();
                model.add(factory.createStatement(factory.createURI(resource),
                        factory.createURI(schema.get(key)), factory.createLiteral(value)));
            }
            m = Pattern.compile("^(entity::property:uri)(.*)$").matcher(key);
            if (m.find()) {

                model.add(factory.createStatement(factory.createURI(resource),
                        factory.createURI(schema.get(key)), factory.createLiteral("http://academic.research.microsoft.com/Publication/" + json.get("id").getAsString() + "/")));
            }
            m = Pattern.compile("^(entity::property:quote)(.*)$").matcher(key);
            if (m.find()) {
                JsonArray aux = json.get("keyWord").getAsJsonArray();
                for (JsonElement version : aux) {
                    model.add(factory.createStatement(factory.createURI(resource),
                            factory.createURI(schema.get(key)), factory.createLiteral(version.getAsString())));

                }

            }
            m = Pattern.compile("^(entity::property:authorlist)(.*)$").matcher(key);
            if (m.find()) {
                //if(key.matches("^rdf::property:")) {
                JsonArray aux = json.get("authors").getAsJsonArray();
                String authors = "";
                for (JsonElement version : aux) {
                    authors += "<" + (("http://academic.research.microsoft.com/Author/" + version.getAsJsonObject().get("id").getAsString() + "/").trim()) + "> ";

                }

                model.add(factory.createStatement(factory.createURI(resource),
                        factory.createURI(schema.get(key)), factory.createLiteral("(" + authors + ")")));

            }
            m = Pattern.compile("^(entity::property:creator)(.*)$").matcher(key);
            if (m.find()) {
                //if(key.matches("^rdf::property:")) {
                JsonArray aux = json.get("authors").getAsJsonArray();
                for (JsonElement version : aux) {
                    model.add(factory.createStatement(factory.createURI(resource),
                            factory.createURI(schema.get(key)), factory.createURI("http://academic.research.microsoft.com/Author/" + version.getAsJsonObject().get("id").getAsString() + "/")));

                }

            }

        }

    }

}
