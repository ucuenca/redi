package at.newmedialab.lmf.util.geonames.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeNotNull;

import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import at.newmedialab.lmf.util.geonames.builder.GeoLookupBuilder;
import at.newmedialab.lmf.util.geonames.builder.GeoLookupImpl;

public class GeoLookupImplTest {

    private GeoLookupImpl lookup;
    private DefaultHttpClient http;

    @Before
    public void setUp() throws Exception {
        http = new DefaultHttpClient();
        lookup = new GeoLookupImpl(http, GeoLookupBuilder.GEONAMES_LEGACY, null, null, 0, 0);
    }

    @After
    public void tearDown() throws Exception {
        if (http != null)
            http.getConnectionManager().shutdown();
    }

    @Test
    public void testResolveBerlin() {
        final String uri = lookup.resolvePlace("Berlin");
        assumeNotNull(uri);
        assertEquals("http://sws.geonames.org/2950159/", uri);
    }

    @Test
    public void testResolveParisWithCountryBias() {
        lookup.setCountryBias("us");
        final String uri = lookup.resolvePlace("Paris");
        assumeNotNull(uri);
        assertEquals("http://sws.geonames.org/4717560/", uri);
    }

    @Test
    public void testResolveParisWithCountry() {
        lookup.setCountry("us");
        final String uri = lookup.resolvePlace("Paris");
        assumeNotNull(uri);
        assertEquals("http://sws.geonames.org/4717560/", uri);
    }

    @Test
    public void testContinent() {
        lookup.setContinentCode("NA");
        final String uri = lookup.resolvePlace("Sydney");
        assumeNotNull(uri);
        assertEquals("http://sws.geonames.org/6354908/", uri);
    }
    
    @Test
    public void testResolveParisWithoutCountryBias() {
        final String uri = lookup.resolvePlace("Paris");
        assumeNotNull(uri);
        assertEquals("http://sws.geonames.org/2988507/", uri);
    }
}
