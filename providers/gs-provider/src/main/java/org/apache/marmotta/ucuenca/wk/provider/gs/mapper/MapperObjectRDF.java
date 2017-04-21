/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.provider.gs.mapper;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.marmotta.ucuenca.wk.provider.gs.GoogleScholarSearchProvider;
import org.apache.marmotta.ucuenca.wk.provider.gs.util.Author;
import org.apache.marmotta.ucuenca.wk.provider.gs.util.Publication;
import org.apache.marmotta.ucuenca.wk.wkhuska.vocabulary.BIBO;
import org.apache.marmotta.ucuenca.wk.wkhuska.vocabulary.REDI;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.TreeModel;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.FOAF;
import org.openrdf.model.vocabulary.RDF;

/**
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
@SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.StdCyclomaticComplexity", "PMD.ModifiedCyclomaticComplexity"})
public class MapperObjectRDF {

    private final ValueFactory factory = ValueFactoryImpl.getInstance();
    private final List fields = Arrays.asList("name", "affiliation", "profile", "img", "numCitations",
            "title", "description", "pages", "publisher", "conference", "journal", "volume", "issue", "date");
    private final URI authorURI;
    private final String publicationResource;
    private final String authorResource;
    private final String bookResource;

    /**
     * Map an object to RDF. Constraints author should the name searched or an
     * URI.
     *
     * @param author
     * @param baseResource
     */
    public MapperObjectRDF(String author, String baseResource) {
        publicationResource = String.format("%spublication/", baseResource);
        authorResource = String.format("%sauthor/", baseResource);
        bookResource = String.format("%sbook/", baseResource);
        if (author.matches("^https?:\\/\\/.*")) {
            authorURI = factory.createURI(author);
        } else {
            authorURI = generateURI(String.format("%sauthor/", baseResource), author);
        }
    }

    /**
     * Convert a publication object to RDF.
     *
     * @param publication
     * @return
     * @throws java.lang.IllegalAccessException
     */
    public Model map(Publication publication) throws IllegalArgumentException, IllegalAccessException {
        Model triples = new TreeModel();

        URI publicationURI = generateURI(publicationResource, StringEscapeUtils.unescapeJava(publication.getTitle().toUpperCase()));
        // Parse common attributes
        parseObject(triples, publication, publicationURI);

        // Store the 
        triples.add(new StatementImpl(publicationURI, REDI.GSCHOLAR_URl, factory.createLiteral(publication.getUrl())));

        // Add types/relation author-publications for
        triples.add(new StatementImpl(publicationURI, RDF.TYPE, BIBO.ACADEMIC_ARTICLE));
        triples.add(new StatementImpl(publicationURI, RDF.TYPE, BIBO.DOCUMENT));
        triples.add(new StatementImpl(authorURI, FOAF.PUBLICATIONS, publicationURI));

        // Add authors if exist
        if (publication.getAuthors().size() > 0) {
            String authorName = publication.getAuthors().get(0);
            URI creatorURI = generateURI(authorResource, authorName);
            triples.add(new StatementImpl(publicationURI, DCTERMS.CREATOR, creatorURI));

            if (!authorURI.equals(creatorURI)) {
                triples.add(new StatementImpl(creatorURI, FOAF.NAME, factory.createLiteral(authorName)));
                triples.add(new StatementImpl(creatorURI, RDF.TYPE, FOAF.PERSON));
            }
            for (int i = 1; i < publication.getAuthors().size(); i++) {
                authorName = publication.getAuthors().get(i);
                URI contributorURI = generateURI(authorResource, authorName);
                triples.add(new StatementImpl(publicationURI, DCTERMS.CONTRIBUTOR, contributorURI));
                if (!authorURI.equals(contributorURI)) {
                    triples.add(new StatementImpl(contributorURI, FOAF.NAME, factory.createLiteral(authorName)));
                    triples.add(new StatementImpl(contributorURI, RDF.TYPE, FOAF.PERSON));
                }
            }
        }

        // Add book resoruces/literals if exist
        if (publication.getBook() != null) {
            URI bookURI = generateURI(bookResource, publication.getBook());

            triples.add(new StatementImpl(publicationURI, DCTERMS.IS_PART_OF, bookURI));
            triples.add(new StatementImpl(bookURI, RDF.TYPE, BIBO.BOOK));
            triples.add(new StatementImpl(bookURI, DCTERMS.TITLE, factory.createLiteral(publication.getBook())));
        }

        // Add resources where you can find the publication
        for (String resource : publication.getResources()) {
            triples.add(new StatementImpl(publicationURI, BIBO.URI, factory.createLiteral(resource)));
        }

        return triples;
    }

    /**
     * Convert an author object to RDF.
     *
     * @param author
     * @return
     */
    public Model map(Author author) throws IllegalArgumentException, IllegalAccessException {
        Model triples = new TreeModel();
        //URI authorURI = generateURI(REDI.NAMESPACE_AUTHOR, author.getName());

        parseObject(triples, author, authorURI);
        for (String area : author.getAreas()) {
            triples.add(new StatementImpl(authorURI, GoogleScholarSearchProvider.MAPPING_SCHEMA.get("areas"), factory.createLiteral(area)));
        }
        triples.add(new StatementImpl(authorURI, RDF.TYPE, FOAF.PERSON));

        // Add URLS for each publication
        for (Publication publication : author.getPublications()) {
            triples.add(new StatementImpl(authorURI, REDI.GSCHOLAR_URl, factory.createLiteral(publication.getUrl())));
        }
        return triples;
    }

    private void parseObject(Model triples, Object o, URI resource) throws IllegalArgumentException, IllegalAccessException {
        for (Field f : o.getClass().getDeclaredFields()) {

            if (fields.contains(f.getName())) {
                f.setAccessible(true);
                if (f.get(o) != null) {
                    Value object = null;

                    if (f.getType() == String.class) {
                        object = String.valueOf(f.get(o)).startsWith(GoogleScholarSearchProvider.URI_START_WITH)
                                ? factory.createURI(String.valueOf(f.get(o)))
                                : factory.createLiteral(String.valueOf(f.get(o)).trim());
                    } else if (f.getType() == Integer.TYPE) {
                        object = factory.createLiteral(new Integer(String.valueOf(f.get(o))));
                    }

                    if ("numCitations".equals(f.getName())) { // Specific case to store just citations greater that 0
                        if (Integer.parseInt(object.stringValue()) > 0) {
                            triples.add(new StatementImpl(resource, GoogleScholarSearchProvider.MAPPING_SCHEMA.get(f.getName()), object));
                        }
                    } else if ("profile".equals(f.getName())) { // Specific case to store profile url from an Author as Literal using bibo:uri
                        triples.add(new StatementImpl(resource, GoogleScholarSearchProvider.MAPPING_SCHEMA.get(f.getName()),
                                factory.createLiteral(String.valueOf(f.get(o)))));
                    } else if ("img".equals(f.getName())) {
                        triples.add(new StatementImpl(resource, GoogleScholarSearchProvider.MAPPING_SCHEMA.get(f.getName()), object));
                        triples.add(new StatementImpl(factory.createURI(object.stringValue()), RDF.TYPE, FOAF.IMAGE));
                    } else {
                        triples.add(new StatementImpl(resource, GoogleScholarSearchProvider.MAPPING_SCHEMA.get(f.getName()), object));
                    }
                }

            }
        }
    }

    private URI generateURI(String namespace, String id) {
        return factory.createURI(namespace + id.trim().replace(" ", "_"));
    }
}
