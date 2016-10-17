package org.apache.marmotta.ucuenca.wk.provider.ak.util;

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

public class JSONtoRDF {

    private JsonArray data;
    private Model model;
    private Map<String, String> schema;
    private final ValueFactory factory = ValueFactoryImpl.getInstance();
    private String academicsUrl = "https://academic.microsoft.com/#/detail/";

    public JSONtoRDF(Map<String, String> schema, JsonArray data, Model model) {
        this.schema = schema;
        this.data = data;
        this.model = model;
    }

    public void parse() throws Exception {
        Integer i = 0;
        while (i < data.size()) {
            JsonObject json = data.get(i).getAsJsonObject();
            this.mappingProcess(academicsUrl + Long.toString(json.get("id").getAsLong()) + "/", json);
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

                getGenericAttributes(m, key, json, resource);
            }
            getAttributesPartOne(key, json, resource);
            getAttributesPartTwo(key, json, resource);
            getAttributesPartThree(key, json, resource);
            getAttributesPartFour(key, json, resource);

        }

    }

    public void getAttributesPartOne(String key, JsonObject json, String resource) {
        String value = "";
        JsonArray aux = new JsonArray();

        switch (key) {
            case "entity::property:abstract":
                value = json.get("abstractt").getAsString();
                model.add(factory.createStatement(factory.createURI(resource),
                        factory.createURI(schema.get(key)), factory.createLiteral(value)));
                break;
            case "entity::property:Conference":
                value = json.get("conference").getAsString();
                model.add(factory.createStatement(factory.createURI(resource),
                        factory.createURI(schema.get(key)), factory.createLiteral(value)));
                break;
            case "entity::property:fullversionurl":
                aux = json.get("sources").getAsJsonArray();
                for (JsonElement version : aux) {
                    model.add(factory.createStatement(factory.createURI(resource),
                            factory.createURI(schema.get(key)), factory.createURI(version.getAsString())));

                }
                break;
            case "entity::property:date":
                value = json.get("year").getAsString();
                model.add(factory.createStatement(factory.createURI(resource),
                        factory.createURI(schema.get(key)), factory.createLiteral(value)));
                break;
            default:
                break;

        }
    }

    public void getAttributesPartTwo(String key, JsonObject json, String resource) {
        JsonArray aux = new JsonArray();

        switch (key) {
            case "entity::property:uri":
                model.add(factory.createStatement(factory.createURI(resource),
                        factory.createURI(schema.get(key)), factory.createLiteral(academicsUrl + json.get("id").getAsLong() + "/")));
                break;
            case "entity::property:quote":
                aux = json.get("keyWord").getAsJsonArray();
                for (JsonElement version : aux) {
                    model.add(factory.createStatement(factory.createURI(resource),
                            factory.createURI(schema.get(key)), factory.createLiteral(version.getAsString())));

                }
                break;

            case "entity::property:creator":
                aux = json.get("authors").getAsJsonArray();
                model.add(factory.createStatement(factory.createURI(resource),
                        factory.createURI(schema.get(key)), factory.createURI(academicsUrl + aux.get(0).getAsJsonObject().get("id").getAsString())));

                break;
            case "entity::property:contributor":
                aux = json.get("authors").getAsJsonArray();
                if (aux != null) {
                    aux.remove(0);
                }
                for (int iterator = 1; iterator < aux.size(); iterator++) {
                    JsonElement element = aux.get(iterator);
                    model.add(factory.createStatement(factory.createURI(resource),
                            factory.createURI(schema.get(key)), factory.createURI(academicsUrl + element.getAsJsonObject().get("id").getAsString())));

                }
                break;
            default:
                break;

        }

    }

    public void getAttributesPartThree(String key, JsonObject json, String resource) {
        JsonArray aux = new JsonArray();
        switch (key) {

            case "entity::property:references":
                aux = json.get("referencesId").getAsJsonArray();
                for (JsonElement references : aux) {
                    model.add(factory.createStatement(factory.createURI(resource),
                            factory.createURI(schema.get(key)), factory.createURI(academicsUrl + references.getAsString() + "/")));

                }
                break;
            case "entity::property:journal":
                aux = json.get("journals").getAsJsonArray();
                for (JsonElement journals : aux) {
                    model.add(factory.createStatement(factory.createURI(resource),
                            factory.createURI(schema.get(key)), factory.createLiteral(journals.getAsString())));

                }
                break;
            case "entity::property:subject":
                aux = json.get("fields").getAsJsonArray();
                for (JsonElement journals : aux) {
                    model.add(factory.createStatement(factory.createURI(resource),
                            factory.createURI(schema.get(key)), factory.createLiteral(journals.getAsString())));

                }
                break;
            default:
                break;

        }
    }

    public void getAttributesPartFour(String key, JsonObject json, String resource) {
        JsonArray aux = new JsonArray();
        switch (key) {

            case "entity::property:text":
                aux = json.get("authors").getAsJsonArray();
                String text = "";
                for (int iterator = 0; iterator < aux.size(); iterator++) {
                    JsonElement element = aux.get(iterator);

                    text = text + (element.getAsJsonObject().get("afiliation") != null ? element.getAsJsonObject().get("afiliation").getAsString() : "") + " -";
                }
                model.add(factory.createStatement(factory.createURI(resource),
                        factory.createURI(schema.get(key)), factory.createLiteral(text)));
                break;

            case "entity::property:empty":
                break;
            default:
                break;
        }
    }

    public void getGenericAttributes(Matcher m, String key, JsonObject json, String resource) {
        if (json.has(m.group(2))) {
            String value = json.get(m.group(2)).getAsString();

            model.add(factory.createStatement(factory.createURI(resource),
                    factory.createURI(schema.get(key)), factory.createLiteral(value)));
        }
    }
}
