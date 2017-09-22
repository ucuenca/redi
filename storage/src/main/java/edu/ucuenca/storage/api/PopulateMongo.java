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
    public void statistics();
}
