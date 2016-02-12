/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.ma;

import org.apache.marmotta.ldclient.api.ldclient.LDClientService;
import org.apache.marmotta.ldclient.exception.DataRetrievalException;
import org.apache.marmotta.ldclient.model.ClientConfiguration;
import org.apache.marmotta.ldclient.model.ClientResponse;
import org.apache.marmotta.ldclient.services.ldclient.LDClient;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.rdfxml.util.RDFXMLPrettyWriter;

/**
 *
 * @author Freddy Sumba
 */
public class TestMA {

    public TestMA() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Test
    public void testMicrosoftAcademics() {
        ClientConfiguration config = new ClientConfiguration();

        LDClientService ldclient = new LDClient(config);
        try {
            ClientResponse res;
            
            //Test seach by author  ID for query author provier
//            res = ldclient.retrieveResource("http://academic.research.microsoft.com/Author/53756505/");
            
            //Test seach by author  ID
            //res = ldclient.retrieveResource("http://academic.research.microsoft.com/json.svc/search?AppId=d4d1924a-5da9-4e8b-a515-093e8a2d1748&AuthorID=34038376&ResultObjects=Publication&PublicationContent=AllInfo&StartIdx=1&EndIdx=100");
            
            //Test seach by author QUERY
            res = ldclient.retrieveResource("http://academic.research.microsoft.com/json.svc/search?AppId=d4d1924a-5da9-4e8b-a515-093e8a2d1748&AuthorQuery=saquicela&ResultObjects=Publication&PublicationContent=AllInfo&StartIdx=1&EndIdx=100");
            RDFHandler handler = new RDFXMLPrettyWriter(System.out);
            try {
                res.getTriples().getConnection().export(handler);
            } catch (RepositoryException e) {
                //e.printStackTrace();
            } catch (RDFHandlerException e) {
                //e.printStackTrace();
            }
        } catch (DataRetrievalException e) {
            // TODO Auto-generated catch block
            //e.printStackTrace();
        }
    }
}
