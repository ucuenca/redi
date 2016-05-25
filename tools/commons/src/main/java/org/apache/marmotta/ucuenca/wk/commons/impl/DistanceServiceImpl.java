/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.commons.impl;

import org.apache.marmotta.ucuenca.wk.commons.function.SyntacticDistance;
import org.apache.marmotta.ucuenca.wk.commons.function.SemanticDistance;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import org.apache.marmotta.ucuenca.wk.commons.service.DistanceService;

/**
 *
 * @author Satellite
 */
public class DistanceServiceImpl implements DistanceService {

    @Inject
    private org.slf4j.Logger log;

    @Override
    public boolean semanticComparison(List<String> listA, List<String> listB) {
        try {
            SemanticDistance dist = new SemanticDistance();
            double value = dist.semanticKeywordsDistance(listA, listB);
            double semthreshold = 1;
            if (value < semthreshold) {
                return true;
            }
        } catch (IOException | ClassNotFoundException | SQLException ex) {
            log.error("ERROR IN SemanticDistance:" + ex);
   //         Logger.getLogger(DistanceServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    @Override
    public boolean syntacticComparisonNames(String... args) {

        try {
            SyntacticDistance syntacticdistance = new SyntacticDistance();
            return syntacticdistance.compareNames(args);
        } catch (Exception ex) {
            Logger.getLogger(DistanceServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

}
