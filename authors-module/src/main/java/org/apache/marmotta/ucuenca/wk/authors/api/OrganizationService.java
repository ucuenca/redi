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
      String addOrganization (String acro , String namEn , String namEs , String country , String prov , String city, String lat , String lon , String type );
      String loadOrgbyURI(String uri);
      String editOrg (String acro, String namEn, String namEs, String country, String prov, String city, String lat, String lon, String type );
      String removeOrgbyURI(String resourceid);
      boolean  askOrganization (String uri );
      String listOrganization ();
}
