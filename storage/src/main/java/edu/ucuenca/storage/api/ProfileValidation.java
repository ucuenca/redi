/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucuenca.storage.api;

import java.util.HashMap;
import org.json.simple.JSONObject;

/**
 *
 * @author joe
 */
public interface ProfileValidation {

  public JSONObject getProfileCandidates(String uri, HashMap<String, Boolean> table);

  public JSONObject getProfileNames(String uri, HashMap<String, Boolean> table);

  public JSONObject getProfileEmail(String uri, HashMap<String, Boolean> table);

  public JSONObject getPublicationsCandidates(String uri, HashMap<String, Boolean> table);

  public JSONObject getProfileInst(String uri);

  public String totalProfileVal(String uri, String orcid);

  public String getProfile(String uri, String orcid);

  public String saveProfileData(String data, String id, String uri, String profile);

  public JSONObject obtainNewProfiles(String org) throws Exception;

}
