/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.pubman.utils;

import com.hp.hpl.jena.rdf.model.*;
import org.openrdf.model.*;
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.model.impl.StatementImpl;

/**
 * Provides functionality to convert Sesame objects to Jena objects.
 * <p>
 * https://github.com/anno4j/anno4j/blob/master/anno4j-core/src/main/java/com/github/anno4j/util/JenaSesameUtils.java
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
public class JenaSesameUtils {

    /**
     * Internal model used to create instances of Jena API objects
     */
    private static final com.hp.hpl.jena.rdf.model.Model mInternalModel = com.hp.hpl.jena.rdf.model.ModelFactory.createDefaultModel();

    /**
     * Sesame value factory for creating instances of Sesame API objects
     */
    private static final org.openrdf.model.ValueFactory FACTORY = new org.openrdf.model.impl.ValueFactoryImpl();

    /**
     * Convert the given Jena Resource into a Sesame Resource
     *
     * @param theRes the jena resource to convert
     * @return the jena resource as a sesame resource
     */
    public static org.openrdf.model.Resource asSesameResource(com.hp.hpl.jena.rdf.model.Resource theRes) {
        if (theRes == null) {
            return null;
        } else if (theRes.canAs(Property.class)) {
            return asSesameURI(theRes.as(Property.class));
        } else {
            return FACTORY.createBNode(theRes.getId().getLabelString());
        }
    }

    /**
     * Convert the given Jena Property instance to a Sesame URI instance
     *
     * @param theProperty the Jena Property to convert
     * @return the Jena property as a Sesame Instance
     */
    public static org.openrdf.model.URI asSesameURI(Property theProperty) {
        if (theProperty == null) {
            return null;
        } else {
            // Sesame accepts absolute URIs. Add protocol in case there isn't.
            if (!theProperty.toString().startsWith("http")) {
                String uri = "http://" + theProperty.toString();
                theProperty = mInternalModel.createProperty(uri);
            }
            return FACTORY.createURI(theProperty.getURI());
        }
    }

    /**
     * Convert the given Jena Literal to a Sesame Literal
     *
     * @param theLiteral the Jena Literal to convert
     * @return the Jena Literal as a Sesame Literal
     */
    public static org.openrdf.model.Literal asSesameLiteral(com.hp.hpl.jena.rdf.model.Literal theLiteral) {
        if (theLiteral == null) {
            return null;
        } else if (theLiteral.getLanguage() != null && !theLiteral.getLanguage().equals("")) {
            return FACTORY.createLiteral(theLiteral.getLexicalForm(),
                    theLiteral.getLanguage());
        } else if (theLiteral.getDatatypeURI() != null) {
            return FACTORY.createLiteral(theLiteral.getLexicalForm(),
                    FACTORY.createURI(theLiteral.getDatatypeURI()));
        } else {
            return FACTORY.createLiteral(theLiteral.getLexicalForm());
        }
    }

    /**
     * Convert the given Jena node as a Sesame Value
     *
     * @param theNode the Jena node to convert
     * @return the jena node as a Sesame Value
     */
    public static Value asSesameValue(com.hp.hpl.jena.rdf.model.RDFNode theNode) {
        if (theNode == null) {
            return null;
        } else if (theNode.canAs(com.hp.hpl.jena.rdf.model.Literal.class)) {
            return asSesameLiteral(theNode.as(com.hp.hpl.jena.rdf.model.Literal.class));
        } else {
            return asSesameResource(theNode.as(com.hp.hpl.jena.rdf.model.Resource.class));
        }
    }

    /**
     * Convert the given Sesame Resource to a Jena Resource
     *
     * @param theRes the sesame resource to convert
     * @return the sesame resource as a jena resource
     */
    public static com.hp.hpl.jena.rdf.model.Resource asJenaResource(org.openrdf.model.Resource theRes) {
        if (theRes == null) {
            return null;
        } else if (theRes instanceof URI) {
            return asJenaURI((URI) theRes);
        } else {
            return mInternalModel.createResource(new AnonId(((BNode) theRes).getID()));
        }
    }

    /**
     * Convert the sesame value to a Jena Node
     *
     * @param theValue the Sesame value
     * @return the sesame value as a Jena node
     */
    public static RDFNode asJenaNode(Value theValue) {
        if (theValue instanceof org.openrdf.model.Literal) {
            return asJenaLiteral((org.openrdf.model.Literal) theValue);
        } else {
            return asJenaResource((org.openrdf.model.Resource) theValue);
        }
    }

    /**
     * Convert the Sesame URI to a Jena Property
     *
     * @param theURI the sesame URI
     * @return the URI as a Jena property
     */
    public static Property asJenaURI(URI theURI) {
        if (theURI == null) {
            return null;
        } else {
            return mInternalModel.getProperty(theURI.toString());
        }
    }

    /**
     * Convert a Sesame Literal to a Jena Literal
     *
     * @param theLiteral the Sesame literal
     * @return the sesame literal converted to Jena
     */
    public static com.hp.hpl.jena.rdf.model.Literal asJenaLiteral(org.openrdf.model.Literal theLiteral) {
        if (theLiteral == null) {
            return null;
        } else if (theLiteral.getLanguage() != null) {
            return mInternalModel.createLiteral(theLiteral.getLabel(),
                    theLiteral.getLanguage());
        } else if (theLiteral.getDatatype() != null) {
            return mInternalModel.createTypedLiteral(theLiteral.getLabel(),
                    theLiteral.getDatatype().toString());
        } else {
            return mInternalModel.createLiteral(theLiteral.getLabel());
        }
    }

    /**
     * Convert the Sesame Graph to a Jena Model
     *
     * @param theGraph the Graph to convert
     * @return the set of statements in the Sesame Graph converted and saved in
     * a Jena Model
     */
    public static com.hp.hpl.jena.rdf.model.Model asJenaModel(Graph theGraph) {
        com.hp.hpl.jena.rdf.model.Model aModel = ModelFactory.createDefaultModel();

        for (final org.openrdf.model.Statement aStmt : theGraph) {
            aModel.add(asJenaStatement(aStmt));
        }

        return aModel;
    }

    /**
     * Convert the Jena Model to a Sesame Graph
     *
     * @param theModel the model to convert
     * @return the set of statements in the Jena model saved in a sesame Graph
     */
    public static Graph asSesameGraph(com.hp.hpl.jena.rdf.model.Model theModel) {
        Graph aGraph = new GraphImpl();

        StmtIterator sIter = theModel.listStatements();
        while (sIter.hasNext()) {
            aGraph.add(asSesameStatement(sIter.nextStatement()));
        }

        sIter.close();

        return aGraph;
    }

    /**
     * Convert a Jena Statement to a Sesame statement
     *
     * @param theStatement the statement to convert
     * @return the equivalent Sesame statement
     */
    public static org.openrdf.model.Statement asSesameStatement(com.hp.hpl.jena.rdf.model.Statement theStatement) {
        return new StatementImpl(asSesameResource(theStatement.getSubject()),
                asSesameURI(theStatement.getPredicate()),
                asSesameValue(theStatement.getObject()));
    }

    /**
     * Convert a Sesame statement to a Jena statement
     *
     * @param theStatement the statemnet to convert
     * @return the equivalent Jena statement
     */
    public static com.hp.hpl.jena.rdf.model.Statement asJenaStatement(org.openrdf.model.Statement theStatement) {
        return mInternalModel.createStatement(asJenaResource(theStatement.getSubject()),
                asJenaURI(theStatement.getPredicate()),
                asJenaNode(theStatement.getObject()));
    }

}
