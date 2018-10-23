/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.edu.cedia.redi.ldclient.test.doaj;

import java.io.FileNotFoundException;
import org.apache.marmotta.ldclient.exception.DataRetrievalException;
import org.apache.marmotta.ldclient.model.ClientResponse;
import org.apache.marmotta.ldclient.test.provider.ProviderTestBase;
import org.junit.Assert;
import org.junit.Test;
import org.openrdf.model.Model;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFHandlerException;

/**
 *
 * @author cedia
 */
public class TestDOAJProvider extends ProviderTestBase {

    @Test
    public void testDOAJ() throws RepositoryException, DataRetrievalException, RDFHandlerException, FileNotFoundException {
        ClientResponse retrieveResource = ldclient.retrieveResource("https://doaj.org/search/pedro-martin_merino");
        Model data = retrieveResource.getData();
        Assert.assertEquals(data.size(), 110);
    }
}
