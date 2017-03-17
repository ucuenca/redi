package org.apache.marmotta.ucuenca.wk.provider.gs.handler;

import java.util.ArrayList;
import java.util.List;
import org.apache.marmotta.ucuenca.wk.provider.gs.GoogleScholarSearchProvider;
import org.apache.marmotta.ucuenca.wk.provider.gs.util.Author;
import org.apache.marmotta.ucuenca.wk.provider.gs.util.ProfileAttributes;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Extract profile(s) of author(s) matching the search in Google Scholar (GS),
 * and each profile has a name and a URL (link to the profile in GS). In
 * addition, a profile could have an affiliation, area(s), number of citations,
 * a domain from an institution, and a photo.
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
@SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.StdCyclomaticComplexity"})
public final class SearchHandler extends IHandler {

    private static final String ANCHOR = "a";
    private static final String DIV = "div";
    private static final String IMG = "img";

    private final List<Author> authors = new ArrayList<>();
    private Author author;

    private ProfileAttributes field;

    private boolean isProfile = false;
    private boolean isPhotoDiv = false;
    private boolean extract = false;
    private boolean isLastAttr = false;

    @Override
    public List<Author> getResults() {
        return authors;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

        if (localName.equals("div") && attributes.getValue("class") != null && attributes.getValue("class").equals("gsc_1usr gs_scl")) {
            isProfile = true;
            author = new Author();
            authors.add(author);
            return;
        }
        if (isProfile && DIV.equals(localName)) {
            switch (attributes.getValue("class")) {
                case "gsc_1usr_photo":
                    isPhotoDiv = true;
                    break;
                case "gsc_1usr_aff":
                    extract = true;
                    field = ProfileAttributes.AFFILIATION;
                    break;
                case "gsc_1usr_emlb":
                    extract = true;
                    field = ProfileAttributes.DOMAIN;
                    break;
                case "gsc_1usr_cby":
                    extract = true;
                    field = ProfileAttributes.CITE;
                    break;
                case "gsc_1usr_int":
                    extract = true;
                    field = ProfileAttributes.AREA;
                    isLastAttr = true;
                    break;
                default:
                    break;
            }
        } else if (isProfile) {
            if (isPhotoDiv && ANCHOR.equals(localName)) {
                author.updateAuthor(ProfileAttributes.URL, GoogleScholarSearchProvider.SCHOLAR_GOOGLE + attributes.getValue("href"));
            }
            if (isPhotoDiv && IMG.equals(localName)) {
                String img = !attributes.getValue("src").contains("avatar") ? GoogleScholarSearchProvider.SCHOLAR_GOOGLE + attributes.getValue("src") : null;
                author.updateAuthor(ProfileAttributes.PHOTO, img);
                author.updateAuthor(ProfileAttributes.NAME, attributes.getValue("alt"));
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (isPhotoDiv && ANCHOR.equals(localName)) {
            isPhotoDiv = false;
        }
        if (DIV.equals(localName)) {
            if (extract) {
                extract = false;
            } else if (!extract && isProfile && isLastAttr) {
                isProfile = false;
                isLastAttr = false;
            }
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (isProfile && extract) {
            author.updateAuthor(field, new String(ch, start, length));
        }
    }
}
