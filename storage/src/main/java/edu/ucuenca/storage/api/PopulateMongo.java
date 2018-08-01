/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucuenca.storage.api;

/**
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
public interface PopulateMongo {

    /**
     * Load information of authors in collection authors.
     */
    public void authors();

    /**
     * Load information of publications in collection publications.
     */
    public void publications();

    /**
     * Load aggregations for statistics of publications/keywords/authors and
     * store areas with a frequency greater than 4.F
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

    /**
     * Information of authors by area.
     */
    public void authorsByArea();
    
    /**
     * Information countries for map
     */
    public void Countries ();
}
