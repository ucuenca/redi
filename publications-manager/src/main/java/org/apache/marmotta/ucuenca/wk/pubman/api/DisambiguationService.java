/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.pubman.api;

/**
 *
 * @author cedia
 */
public interface DisambiguationService {

    public String startDisambiguation();

    public String startMerge();

    public String startDisambiguation(String [] orgs);

    public void Proccess();

    public void Proccess(String [] orgs);

    public void Merge();

}
