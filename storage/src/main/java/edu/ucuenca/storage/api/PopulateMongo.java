/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucuenca.storage.api;

import org.apache.marmotta.platform.core.exception.MarmottaException;

/**
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
public interface PopulateMongo {

  /**
   * Load information of authors in collection authors.
   */
  public void authors(String uri);

  /**
   * Load information of publications in collection publications.
   */
  public void publications();

  /**
   * Load aggregations for statistics of publications/keywords/authors and store
   * areas with a frequency greater than 4.F
   */
  public void statistics();

  /**
   * Pre-calculate information to network building.
   */
  public void networks();

  /**
   * Information of clusters and subclusters.
   */
  public void clusters();
  
  public void ProjectProfile();
  
  public void PatentProfile();

  /**
   * Information of authors by area.
   */
  public void authorsByArea();

  public void authorsByDiscipline();

  /**
   * Information countries for map
   */
  public void Countries();

  public void cleanSPARQLS();

  public void LoadStatisticsbyInst();

  public String getStatsbyAuthor();

  public void LoadStatisticsbyAuthor();

  public void populatePublicationTranslations();
  
  public void populatePublicationKeywords();
  
  public void populateProfileChanges();

  public void instbyProj();

  public void areasbydocument();
  
  public String getPublicationDatesbyAreas ();

  public String googlePublicationTranslation();
}
