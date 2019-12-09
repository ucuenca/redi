/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucuenca.storage.webservices;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.ucuenca.storage.api.ProfileValidation;
import edu.ucuenca.storage.exceptions.FailMongoConnectionException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.io.IOUtils;
import org.apache.marmotta.platform.core.api.config.ConfigurationService;
import org.json.simple.JSONObject;

/**
 *
 * @author joe
 */
@Path("/profileval")
@ApplicationScoped
public class ProfileValidationService {

  @Inject
  private org.slf4j.Logger log;
  @Inject
  private ConfigurationService configurationService;
  @Inject
  private ProfileValidation pv;

  @GET
  @Path("/profileData")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getProfile(@QueryParam("uri") String uri, @QueryParam("orcid") String orcid) throws FailMongoConnectionException {
    String response;
    try {
      response = pv.getProfile(uri, orcid);
    } catch (Exception e) {
      throw new FailMongoConnectionException(String.format("Cannot retrieve profile author %s", uri), e);
    }
    return Response.ok().entity(response).build();
  }

  @GET
  @Path("/tablesInfo")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getInfo(@QueryParam("uri") String uri, @QueryParam("orcid") String orcid) throws FailMongoConnectionException {
    String response;
    try {
      response = pv.totalProfileVal(uri, orcid);
    } catch (Exception e) {
      throw new FailMongoConnectionException(String.format("Cannot retrieve profile author %s", uri), e);
    }
    return Response.ok().entity(response).build();
  }

  @POST
  @Path("/saveData")
  @Produces(MediaType.APPLICATION_JSON)
  public Response extractAuthors(@Context HttpServletRequest request) {
    try {
      request.getParameterMap().get("profile");
      String[] get = request.getParameterMap().get("data");
      System.out.print(get[0]);
      String[] getid = request.getParameterMap().get("id");
      String[] getak = request.getParameterMap().get("atk");
      String[] geturi = request.getParameterMap().get("uri");
      String[] getprofile = request.getParameterMap().get("profile");
      /* Iterator p = request.getParameterMap().values().iterator();
             while (p.hasNext()){
             Object a =  p.next();
             System.out.print (a);
             }*/
      pv.saveProfileData(get[0], getid[0], geturi[0], getprofile[0]);
      String output = "{'resp':'ok'}";

      ObjectMapper mapper = new ObjectMapper();
      JsonNode actualObj = mapper.readTree(output);

      return Response.ok().entity(actualObj).build();
      //return  Response.status(Status.BAD_REQUEST).entity("Incorrect file format.").build();
    } catch (IOException ex) {
      Logger.getLogger(ProfileValidationService.class.getName()).log(Level.SEVERE, null, ex);
    }
    return null;
  }

  @GET
  @Path("/obtainAuthors")
  @Produces(MediaType.APPLICATION_JSON)
  public Response obtainAuthors(@QueryParam("org") String org) throws FailMongoConnectionException {
    String response;
    try {
      JSONObject obtainNewProfiles = pv.obtainNewProfiles(org);
      response = obtainNewProfiles.toJSONString();
    } catch (Exception e) {
      throw new FailMongoConnectionException(String.format("Cannot retrieve organization's authors %s", org), e);
    }
    return Response.ok().entity(response).build();
  }

  @POST
  @Path("/uploadPhoto")
  @SuppressWarnings({"PMD.ExcessiveMethodLength", "PMD.AvoidDuplicateLiterals", "PMD.NPathComplexity"})
  public Response uploadAuthorsPhoto(
          @HeaderParam(HttpHeaders.CONTENT_TYPE) String type,
          @Context HttpServletRequest request,
          @QueryParam("orcid") String orcid
  ) throws IOException {
    if (type == null || !("image/jpeg".equals(type.toLowerCase()))) {
      return Response.status(Response.Status.BAD_REQUEST).entity("Incorrect file format.").build();
    }
    if (orcid == null || orcid.trim().length() <= 0) {
      return Response.status(Response.Status.BAD_REQUEST).entity("There is not orcid.").build();
    }

    File f = new File(configurationService.getHome() + File.separator + "profile_photo", orcid.hashCode() + ".jpg");
    if (f.exists()) {
      f.delete();
    }
    if (!f.exists()) {
      f.getParentFile().mkdirs();
      f.createNewFile();
    }
    try (FileOutputStream fos = new FileOutputStream(f)) {
      IOUtils.copy(request.getInputStream(), fos);
    }

    return Response.ok().entity(orcid.hashCode() + ".jpg").build();
  }

  @GET
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  @Path("/getPhoto")
  public Response DownloadPhoto(@QueryParam("orcid") String orcid) {
    File file = new File(configurationService.getHome() + File.separator + "profile_photo", orcid.hashCode() + ".jpg");
    Response.ResponseBuilder response = Response.ok((Object) file);
    response.header("Content-Disposition", "attachment; filename=profile.jpg");
    return response.build();
  }

  @POST
  @Path("/uploadBanner")
  @SuppressWarnings({"PMD.ExcessiveMethodLength", "PMD.AvoidDuplicateLiterals", "PMD.NPathComplexity"})
  public Response uploadBanner(
          @HeaderParam(HttpHeaders.CONTENT_TYPE) String type,
          @Context HttpServletRequest request,
          @QueryParam("id") String id
  ) throws IOException {
    if (type == null || !("image/png".equals(type.toLowerCase()))) {
      return Response.status(Response.Status.BAD_REQUEST).entity("Incorrect file format.").build();
    }
    if (id == null || id.trim().length() <= 0) {
      return Response.status(Response.Status.BAD_REQUEST).entity("There is not orcid.").build();
    }

    File f = new File(configurationService.getHome() + File.separator + "banner_photo", id.hashCode() + ".png");
    if (f.exists()) {
      f.delete();
    }
    if (!f.exists()) {
      f.getParentFile().mkdirs();
      f.createNewFile();
    }
    try (FileOutputStream fos = new FileOutputStream(f)) {
      IOUtils.copy(request.getInputStream(), fos);
    }

    return Response.ok().entity(id.hashCode() + ".png").build();
  }

  @GET
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  @Path("/getBanner")
  public Response DownloadBanner(@QueryParam("id") String id) {
    File file = new File(configurationService.getHome() + File.separator + "banner_photo", id.hashCode() + ".png");
    Response.ResponseBuilder response = Response.ok((Object) file);
    response.header("Content-Disposition", "attachment; filename=banner.png");
    return response.build();
  }

  @GET
  @Path("/sendFeedback")
  @Produces(MediaType.APPLICATION_JSON)
  public Response sendFeedback(@QueryParam("name") String name, @QueryParam("email") String email, @QueryParam("topic") String topic,
          @QueryParam("content") String content, @QueryParam("url") String url
  ) throws FailMongoConnectionException {
    try {
      pv.sendFeedback(name, email, topic, content, url);
      return Response.ok().build();
    } catch (Exception e) {
      throw new FailMongoConnectionException(String.format("Cannot send feedback from  %s", name), e);
    }
  }
}
