/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.apache.marmotta.ucuenca.wk.authors.api;

/**
 *
 * @author joe
 */
public interface OrganizationService {
      String addOrganization (String acro , String namEn , String namEs , String alias, String scopusId , String country , String prov , String city, String lat , String lon , String type , String link , String description);
      String loadOrgbyURI(String uri);

    /**
     *
     * @param acro
     * @param namEn
     * @param namEs
     * @param alias
     * @param scopusId
     * @param country
     * @param prov
     * @param city
     * @param lat
     * @param lon
     * @param type
     * @param link
     * @param description
     * @return
     */
    String editOrg (String acro, String namEn, String namEs, String alias , String scopusId , String country, String prov, String city, String lat, String lon, String type , String link , String description);
      String removeOrgbyURI(String resourceid);
      boolean  askOrganization (String uri );
      String listOrganization ();
}
