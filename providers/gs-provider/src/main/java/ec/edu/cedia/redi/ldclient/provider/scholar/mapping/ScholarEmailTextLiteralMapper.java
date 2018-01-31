/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.edu.cedia.redi.ldclient.provider.scholar.mapping;

import org.apache.marmotta.ldclient.provider.html.mapping.CssTextLiteralMapper;

/**
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
public class ScholarEmailTextLiteralMapper extends CssTextLiteralMapper {

    public ScholarEmailTextLiteralMapper(String cssSelector) {
        super(cssSelector);
    }

    @Override
    protected String cleanValue(String value) {
        value = value.replace("Verified email at", "");
        return super.cleanValue(value);
    }

}
