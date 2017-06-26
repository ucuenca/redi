/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.provider.gs.handler;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
public abstract class IHandler extends DefaultHandler {

    private Logger log = LoggerFactory.getLogger(IHandler.class);

    @SuppressWarnings("PMD.UnusedModifier")
    public abstract List getResults();

    public <T extends IHandler> void extract(InputStream input) throws MalformedURLException, SAXException, InterruptedException {
        int tries = 0;
        while (true) {
            try {
                XMLReader xr = XMLReaderFactory.createXMLReader("org.ccil.cowan.tagsoup.Parser");

                xr.setContentHandler(this);
                InputSource is = new InputSource(input);
                is.setEncoding("iso-8859-1");
                xr.parse(is);
                sleep(5000);
                break;
            } catch (IOException e) {
                tries++;
                log.error(String.format("TRIES: %s \n", tries), e);
                final int two_hour = 2 * 60 * 60 * 1000;
                log.info("WAITING TWO HOURS....");
                sleep(two_hour);
            }
        }
    }

    private void sleep(int ms) throws InterruptedException {
        Thread.sleep(ms);
    }
}
