package org.apache.marmotta.ucuenca.wk.provider.scopus;

import com.google.common.collect.ImmutableList;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.enterprise.context.ApplicationScoped;
import org.apache.marmotta.ldclient.api.endpoint.Endpoint;
import org.apache.marmotta.ldclient.api.provider.DataProvider;
import org.apache.marmotta.ldclient.exception.DataRetrievalException;
import org.apache.marmotta.ldclient.model.ClientConfiguration;
import org.apache.marmotta.ldclient.model.ClientResponse;
import org.apache.marmotta.ldclient.provider.xml.AbstractXMLDataProvider;
import org.apache.marmotta.ldclient.provider.xml.mapping.XPathLiteralMapper;
import org.apache.marmotta.ldclient.provider.xml.mapping.XPathValueMapper;
import org.apache.marmotta.ldclient.services.ldclient.LDClient;
import org.apache.marmotta.ucuenca.wk.endpoint.scopus.ScopusPublicationSearchEndpoint;
import org.jdom2.Namespace;
import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.OWL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
@Deprecated
public class ScopusAuthorProvider
        extends AbstractXMLDataProvider
        implements DataProvider {
    
    /*@Inject
    private DistanceService distanceService;
    
    @Inject
    private KeywordsService kservice;*/

    public static final String NAME = "Scopus Author Provider";
    public static final String API = "http://api.elsevier.com/content/author/author_id/?apiKey=&view=ENHANCED&httpAccept=application/rdf%2Bxml";
    public static final String PATTERN = "http://api\\.elsevier\\.com/content/author/author\\_id/(.*)\\?apiKey\\=(.*)\\&view\\=(.*)\\&httpAccept\\=application/rdf%2Bxml(.*)";
    public static final String URLRESOURCEAUTHOR = "http://api.elsevier.com/content/author/author_id/AuthorIdParam?apiKey=apiKeyParam&view=ENHANCED&httpAccept=application/rdf%2Bxml";
    private static Logger log = LoggerFactory.getLogger((Class) ScopusAuthorProvider.class);
    private static String apiKeyParam = "";
    private static ConcurrentMap<String, String> scopusNamespaces;
    private static ConcurrentMap<String, XPathValueMapper> mediaOntMappings;
    
    public static Namespace namespaceRDF;
    public static final Double LIMIT_SEMANTIC = 0.85;

    public String getName() {
        return "Scopus Author Provider";
    }

    public String[] listMimeTypes() {
        return new String[]{"application/rdf+xml"};
    }

    public List<String> buildRequestUrl(String resource, Endpoint endpoint) {
        init();
        String url = null;
        Matcher m = Pattern.compile(PATTERN).matcher(resource);
        if (m.find()) {
            url = resource;
            apiKeyParam = m.group(2);
        }
        return Collections.singletonList(url);
    }

    public List<String> parseResponse(String resource, String requestUrl, Model triples, InputStream input, String contentType) throws DataRetrievalException {
        //Set<String> keywords = new HashSet<String>();
        //List<String> keywordsAuthorScopus = new ArrayList();
        
        super.parseResponse(resource, requestUrl, triples, input, contentType);
        
        /*try {
            
            super.parseResponse(resource, requestUrl, triples, input, contentType);
            
            InputStream input2 = IOUtils.toBufferedInputStream(input);
            
            CloseShieldInputStream csis = new CloseShieldInputStream(input2);
            final Document doc = new SAXBuilder(XMLReaders.NONVALIDATING).build(csis);
            Element aux = doc.getRootElement();
            List<Element> elements = aux.getChildren();
            for (Element element : elements) {
                List<Element> nextElements = element.getChildren();
                if (element.getAttributeValue("about", namespaceRDF).contains("http://data.elsevier.com/vocabulary/")) {
                    for (Element nextElement : nextElements) {
                        if (nextElement.getNamespace().getPrefix().equals("skos") 
                                && (nextElement.getName().equals("prefLabel") || nextElement.getName().equals("altLabel"))) {
                            keywords.add(nextElement.getValue().contains("(") ? nextElement.getValue().split("\\(")[0].trim() : nextElement.getValue().trim());
                        }
                    }
                }
            }
            //keywordsAuthorScopus = new ArrayList(keywords);

        } catch (IOException e) {
            throw new DataRetrievalException("I/O error while parsing HTML response", e);
        } catch (JDOMException e) {
            throw new DataRetrievalException("could not parse XML response. It is not in proper XML format", e);
        }*/
        
        //Uncomment for use of comparison
        //String authorID = requestUrl.split("&authorURI=")[1].replace("%20", " ");
        
        //List<String> listKeywordsDspace = kservice.getKeywordsOfAuthor(authorID);//dspace
        Double semanticComparisonValue = LIMIT_SEMANTIC; //distanceService.semanticComparisonValue(keywordsAuthorScopus, listKeywordsDspace);
        
        if (semanticComparisonValue <= LIMIT_SEMANTIC) {
            
            
            try {
                //log.debug("Request Successful to {0}", (Object) requestUrl);
                ValueFactoryImpl factory = ValueFactoryImpl.getInstance();
                ClientConfiguration conf = new ClientConfiguration();
                conf.addEndpoint((Endpoint) new ScopusPublicationSearchEndpoint());
                LDClient ldClient = new LDClient(conf);
                Set<Value> resources = triples.filter((Resource) factory.createURI(resource), factory.createURI("http://www.elsevier.com/xml/svapi/rdf/dtd/searchResults"), null, new Resource[0]).objects();
                if (!resources.isEmpty()) {
                    Model resourceModel = null;
                    for (Value scopusResource : resources) {
                        if (scopusResource.stringValue().isEmpty()) {
                            continue;
                        }
                        String resourceDoc = ((Literal) scopusResource).stringValue();
                        ClientResponse response = ldClient.retrieveResource(resourceDoc + "&apiKey=" + apiKeyParam + "&httpAccept=application/xml&view=COMPLETE");
                        Model rsModel = response.getData();
                        if (resourceModel == null) {
                            resourceModel = rsModel;
                            continue;
                        }
                        resourceModel.addAll((Collection) rsModel);
                    }
                    triples.addAll((Collection) resourceModel);
                }
                if (!resource.matches(PATTERN)) {
                    triples.add((Resource) factory.createURI(resource), OWL.SAMEAS, (Value) factory.createURI(requestUrl), new Resource[0]);
                }
            } catch (DataRetrievalException e) {
                log.error("Data Retrieval Exception: " + e);
            } catch (Exception e) {
                    
            }
            
        }
        return Collections.emptyList();
    }

    protected Map<String, XPathValueMapper> getXPathMappings(String requestUrl) {
        return mediaOntMappings;
    }

    protected List<String> getTypes(URI resource) {
        return ImmutableList.of();
    }

    protected Map<String, String> getNamespaceMappings() {
        return scopusNamespaces;
    }

    public void init() {
        namespaceRDF = Namespace.getNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        scopusNamespaces = new ConcurrentHashMap<String, String>();
        scopusNamespaces.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        scopusNamespaces.put("owl", "http://www.w3.org/2002/07/owl#");
        scopusNamespaces.put("dcterms", "http://purl.org/dc/terms/");
        scopusNamespaces.put("foaf", "http://xmlns.com/foaf/0.1");
        scopusNamespaces.put("bibtex", "http://data.bibbase.org/ontology/#");
        scopusNamespaces.put("api", "http://www.elsevier.com/xml/svapi/rdf/dtd/");
        scopusNamespaces.put("prism", "http://prismstandard.org/namespaces/basic/2.0/");
        scopusNamespaces.put("skos", "http://www.w3.org/2004/02/skos/core#");
        mediaOntMappings = new ConcurrentHashMap<String, XPathValueMapper>();
        mediaOntMappings.put("http://www.elsevier.com/xml/svapi/rdf/dtd/searchResults", (XPathValueMapper) new XPathLiteralMapper("/rdf:RDF/rdf:Description/api:searchResults/@rdf:resource", scopusNamespaces));
        mediaOntMappings.put("http://www.elsevier.com/xml/svapi/rdf/dtd/orcid", (XPathValueMapper) new XPathLiteralMapper("/rdf:RDF/rdf:Description/api:orcid", scopusNamespaces));
    }
}
