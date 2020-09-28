/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.pubman.api;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.marmotta.platform.core.exception.MarmottaException;
import org.apache.marmotta.ucuenca.wk.pubman.utils.BucketType;
import org.apache.marmotta.ucuenca.wk.pubman.utils.MapSetWID;

public interface IdentificationManager {

    public void addBucket(BucketType typ, Set<String> set, Map<BucketType, Long> tc, MapSetWID mpp) throws MarmottaException, Exception;

    public void applyFix() throws MarmottaException, Exception;

    public List<String> getBuckets(BucketType typ) throws MarmottaException, Exception;

    public String getGraph() throws MarmottaException, Exception;

    public Map<BucketType, Long> getCounters() throws MarmottaException, Exception;

    public void saveCounters(Map<BucketType, Long> m) throws MarmottaException, Exception;

    public MapSetWID getBucketsContent(BucketType typ) throws MarmottaException, Exception;

    public void saveBucketsContent(MapSetWID typ, BucketType t) throws MarmottaException, Exception;

}
