/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.commons.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.ucuenca.wk.commons.service.KeywordsService;
import org.apache.marmotta.ucuenca.wk.commons.service.QueriesService;
import org.apache.marmotta.platform.sparql.api.sparql.SparqlService;
import org.apache.marmotta.ucuenca.wk.commons.service.CommonsServices;
import org.apache.marmotta.ucuenca.wk.commons.service.DistanceService;
import org.openrdf.model.Value;
import org.openrdf.query.QueryLanguage;

/**
 *
 * @author FernandoBac
 */
public class KeywordsServiceImpl implements KeywordsService {

    //@Inject
    private QueriesService queriesService = new QueriesServiceImpl();

    private CommonsServices commonService = new CommonsServicesImpl();
    @Inject
    private SparqlService sparqlService;

    private final static int MAXKEYWORDS = 10; //maximo retorna 10 keywords desde el texto ( primeras )

    @Override
    public List<String> getKeywords(String abstracttext, String titletext) throws IOException, ClassNotFoundException {

        abstracttext = cleaningText(abstracttext);
        titletext = cleaningText(titletext);

        List<String> keyAbstract = new ArrayList<String>();
        List<String> keyTitle = new ArrayList<String>();
        List<String> keywords = new ArrayList<String>();

        keyAbstract = splitKeywords(abstracttext, 2);
        keyTitle = splitKeywords(titletext, 2);

        for (String keyabs : keyAbstract) {
            for (String keytit : keyTitle) {
                if (keytit.compareTo(keyabs) == 0) {
                    keywords.add(keytit);
                }
            }
        }
        return keywords;
    }

    public List<String> splitKeywords(String text, int minletters) {
        List<String> keywords = new ArrayList();
        int i = 0;

        for (String key : text.split(" ")) {
            if ((!isNumber(key))&&(!isConstant(commonService.removeAccents(key))) && (key.length() > minletters)) {
                keywords.add(key);
            }
            i++;
            if (i == MAXKEYWORDS) {
                return keywords;
            }
        }
        return keywords;
    }

    @Override
    public List<String> getKeywords(String text) throws IOException, ClassNotFoundException {
        text = cleaningText(text);
        List<String> keywords = new ArrayList<String>();
        keywords = splitKeywords(text, 3);
        return keywords;
    }

    @Override
    public List<String> getKeywordsOfAuthor(String authorUri) {
        List<String> keywords = new ArrayList<>();
        try {
            String getAllKeywordsQuery = queriesService.getAuthorsKeywordsQuery(authorUri);
            List<Map<String, Value>> resultKeywords = sparqlService.query(QueryLanguage.SPARQL, getAllKeywordsQuery);
            int i = 0;
            for (Map<String, Value> key : resultKeywords) {
                String keyword = key.get("keyword").stringValue();
                keyword = cleaningText(keyword);
                if (!isConstant(commonService.removeAccents(keyword)) && ! isNumber(keyword)) {
                    keywords.add(keyword);
                }
            }
            DistanceService distance = new DistanceServiceImpl();
            String analizedKeyword = keywords.get(0);
            List<String> keywordsG1 = new ArrayList<>();
            List<String> keywordsG2 = new ArrayList<>();
            int sumG1 = 0;
        //    int sumG2 = 0;
            for (String key : keywords) {

                List<String> onlyKeyword2 = new ArrayList<>();
                onlyKeyword2.add(key);
                if (distance.semanticComparison(analizedKeyword, onlyKeyword2)) {
                    keywordsG1.add(keywords.get(i));
                    sumG1++;
                } else {
                    keywordsG2.add(keywords.get(i));
            //        sumG2++;

                }
                i++;
                if (i == MAXKEYWORDS) {
                    if (sumG1 == 0) {
                        return keywordsG2;
                    }
//                    if (sumG2 == 0) {
//                        return keywordsG1;
//                    }
                    return keywordsG1;
                }
            }
            return keywordsG1;
        } catch (MarmottaException ex) {
            Logger.getLogger(KeywordsServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return keywords;
    }

    @Override
    public boolean isValidKeyword(String keyword){
        if (isNumber(keyword))
        {
            return false;
        }
        if (isConstant(commonService.removeAccents(keyword)))
        {
            return false;
        }
        return true;
    }
    
    @Override
    public boolean isNumber(String text) {
        try {
            Integer.parseInt(text);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public boolean isConstant(String text) {
        /**
         * estas palabras deben ir en recursos en el archivo de configuracion de
         * este modulo . Estas palabras no aportan significado para un area de
         * conocimiento de investigadores. todas las palabras aqui registradas
         * no tienen acentos. Antes de su comparacion se elimina el acento de la
         * palabra que va a ser eliminada ( Solo para comparar )
         */
        //si la palabra contiene numeros
        if (text.matches("^.*\\d.*$")) {
            return true;
        }

        String[] articlesEs = {
            //ciudades / pais/ lugares
            "oeste", "este", "sur", "norte", "extranjero", "extranjera", "nacional", "internacional", "ecuador", "cuenca", "guayaquil", "quito", "ambato", "cajas", "guayaquil", "paute", "promas", "quito",
            //numeros
            "uno", "dos", "tres", "cuatro", "cinco", "seis", "siete", "ocho", "nueve", "diez",
            //colores
            "amarillo", "azul", "rojo", "verde", "negro", "blanco", "gris",
            //a
            "adecuada", "adecuacion", "adaptativo", "adaptacion", "alto", "articulo", "anomalias", "abierta", "aplicación", "aplicados", "aplicadas", "adaptabilidad", "aptos", "aptas", "apto", "acción", "anticipo", "afecta", "aumento", "afectan", "apertura", "alcance", "análisis", "analisis", "aplicando", "aplicar", "aquellos", "al", "a",
            //b
            "basado","basada","bajo", "brecha", "buenas", "buenos", "bueno", "base",
            //c
            "correcion", "criterios", "convertir", "calidad", "clasificacion", "crear", "cambio", "competitividad", "contenido", "campana", "campaña", "comportamiento", "comparación", "comparativo", "comparar", "como", "control", "conceptos", "creación", "casos", "cabo", "contra", "cuanto", "cuales", "cuando", "con",
            //d
            "descriptivo", "desestimacion", "disminucion", "dosis", "demanda", "difusion", "diagnostico", "determinacion", "diseño", "distribución", "diferentes", "diferente", "directa", "directo", "debe", "donde", "del", "de", "diseno",
            //e
            "estrategia","ensayo", "extraccion", "estandar", "evaluacion", "efecto", "estatal", "externo", "externa", "empresas", "empresa", "estado", "estados", "experimental", "evaluación", "eficiencia", "emitida", "estilo", "estudio", "entre", "está", "esta", "etapa", "ellos", "el", "en",
            //f
            "formula", "fijo", "fija", "forma", "flexible", "factores", "fuerza", "futuros", "futuro",
            //g
            "generacion", "gestion", "ganar", "guia", "grado",
            //i
            "incluidos", "incluido", "intervencion", "interpretacion", "indole", "inglés", "ingles", "interno", "interna", "investigaciones", "investigacion", "investigación", "impactos", "inventarios", "inventario", "identificación", "ingreso", "inciden", "incidencias", "inciso", "implementacion",
            //l
            "llenar", "lo", "la", "las", "los",
            //m
            "mundo", "malas", "modelo", "mejoramiento", "medio", "mejora", "mediación", "metodo", "manejo", "manual", "mediante", "modelo", "mayor", "menor", "mas",
            //n
            "nivel", "nueva", "nuevo",
            //o
            "objetivo", "oferta", "organo", "optimización", "otros", "otras", "objeto",
            //p
            "pruebas", "procedimiento", "proyectos", "presencia", "propuesta", "post", "previo", "procesos", "proteccion", "proyecto", "portafolio", "pago", "por", "para", "porque", "por", "problema", "plan", "para", "pequeño", "pequeños", "posible", "primero", "primeros", "perfil", "preliminar", "proceso", "principio", "peso",
            //q
            "que",
            //r
            "realizar", "rendimiento", "registradas", "registrados", "registro", "realidad", "retiro", "realizado", "respuesta", "rapido", "rapida", "retratos", "riesgo", "relacion", "reforma", "reparto", "resumen",
            //s
            "seleccion", "sistema", "sometidos", "sesion", "severidad", "situacional", "sector", "seguro", "sector", "sobre", "sin", "su", "se",
            //t
            "test", "titulacion", "trata", "tratados", "tratado", "tesis", "todo", "tanto", "tecnicas",
            //u
            "utilidad", "unico", "usuario", "usos", "utilización", "utilizadas", "utilizar",
            //v
            "ve", "vez", "validar", "validacion",
            //z
            "zonas", "zona"};

        for (String word : articlesEs) {
            if (word.contains("-") || word.contains("_")) {
                return true;
            }
            if (text.toLowerCase().compareTo(word) == 0) {
                return true;
            }
        }
        String[] articlesEn = {"a", "are", "an", "abstract", "been", "by", "change", "chapter", "challenging", "do", "dont", "don't", "for", "has", "into", "model", "moreover", "lower", "hight", "of", "object", "problems", "related", "they", "that", "the", "we", "what", "when", "where", "with", "for", "the", "in", "a", "an", "with", "is", "to", "and", "of", "high", "to", "any", "on", "cuenca", "ecuador"};
        for (String article : articlesEn) {
            if (text.toLowerCase().compareTo(article) == 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String cleaningText(String text) {
        CommonsServicesImpl commonsservices = new CommonsServicesImpl();
        text = text.replace(".", "");
        text = text.replace("-", "");
        text = text.replace("_", "");
        text = text.replace("?", "");
        text = text.replace(":", "");
        text = text.replace(";", "");
        text = text.replace(",", "");
        text = text.replace("'", "");
        text = text.replace("¿", "");
        text = text.replace("\"", "");
        text = text.replace("^", "");
        text = text.replace("(", "");
        text = text.replace(")", "");
        text = text.replace("%", "");
        text = text.replace("#", "");
        text = text.replace("[", "");
        text = text.replace("]", "");
        text = text.replace("’’", "");
        text = text.replace("!", "");
        text = text.replace("¡", "");
        text = text.replace("TESIS DE MAESTRIA EN", "");
        text = text.replace("TESIS DE", "");
        text = text.replace("TESIS EN", "");
        text = text.replace("TESIS", "");
        text = text.replace("MAESTRIA", "");
        text = text.replace("FACULTAD", "");
        text = text.replace("PUBLICA", "");

        text = commonsservices.removeAccents(text);
        return text;
    }
}
