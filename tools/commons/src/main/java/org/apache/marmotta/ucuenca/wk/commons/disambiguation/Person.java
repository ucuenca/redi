/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.marmotta.ucuenca.wk.commons.disambiguation;

import edu.emory.mathcs.backport.java.util.Collections;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.marmotta.ucuenca.wk.commons.disambiguation.utils.AffiliationUtils;
import org.apache.marmotta.ucuenca.wk.commons.disambiguation.utils.NameUtils;
import org.apache.marmotta.ucuenca.wk.commons.disambiguation.utils.PublicationUtils;

/**
 *
 * @author Jose Ortiz
 */
@SuppressWarnings("PMD")
public class Person {

    public static final double thresholdName = 0.9;
    public static final double thresholdCAName = 0.9;
    public static final double thresholdTitle = 0.93;
    public static final double thresholdAff = 0.95;
    public static final int thresholdCoauthors = 2;
    public static final int thresholdPublications = 1;
    public static final int thresholdAffiliation = 1;

    public Provider Origin;
    public String URI;
    public Set<String> URIS;
    public List<List<String>> Name;
    public List<List<String>> Coauthors;
    public List<String> Publications;
    public List<String> Affiliations;
    public List<String> Topics;

    public Person() {
        URIS = new HashSet<>();
    }

    public boolean check(Person p, boolean priorFName) {
        boolean common = !Collections.disjoint(URIS, p.URIS);
        if (common) {
            return false;
        }
        if (URI == null) {
            return true;
        }
        Boolean checkName = checkName(p, priorFName);
        if (checkName != null && checkName == true) {
            Boolean checkAffiliations = checkAffiliations(p);
            Boolean checkCoauthors = checkCoauthors(p);
            Boolean checkPublications = checkPublications(p);
            Boolean checkTopics = checkTopics(p);
            if (checkCoauthors == null && checkPublications == null && checkTopics == null && checkAffiliations != null && checkAffiliations == true) {
                return true;
            }
            int count = 0;
            if (checkAffiliations != null && checkAffiliations == true) {
                count++;
            }
            if (checkPublications != null && checkPublications == true) {
                count++;
            }
            if (checkCoauthors != null && checkCoauthors == true) {
                count++;
            }
            if (checkTopics != null && checkTopics == true) {
                count++;
            }
            return count >= 2;

        }
        return false;
    }

    public Boolean checkName(Person p, boolean priorFName) {
        if (Name.isEmpty() || p.Name.isEmpty()) {
            return null;
        }
        List<String> name1 = NameUtils.bestName(Name);
        List<String> name2 = NameUtils.bestName(p.Name);
        double sim = NameUtils.compareName(name1, name2);
        if (priorFName) {
            int lenOrigin = NameUtils.bestNameLen(Name);
            int lenOther = NameUtils.bestNameLen(p.Name);
            if (lenOther > lenOrigin) {
                sim = 0;
            }
        }
        return sim >= thresholdName;
    }

    public int bestNameLen() {
        return NameUtils.bestNameLen(Name);
    }

    public Boolean checkCoauthors(Person p) {
        if (Coauthors.isEmpty() || p.Coauthors.isEmpty()) {
            return null;
        }
        List<List<String>> name1 = Coauthors;
        List<List<String>> name2 = p.Coauthors;
        List<List<String>> uname1 = new ArrayList<>();
        List<List<String>> uname2 = new ArrayList<>();
        int co = 0;
        for (List<String> n1 : name1) {
            for (List<String> n2 : name2) {
                if (!uname1.contains(n1) && !uname2.contains(n2)) {
                    double sim = NameUtils.compareName(n1, n2);
                    if (sim >= thresholdCAName) {
                        co++;
                        uname1.add(n1);
                        uname2.add(n2);
                    }
                }
            }
        }
        return co >= thresholdCoauthors;
    }

    public Boolean checkPublications(Person p) {
        if (Publications.isEmpty() || p.Publications.isEmpty()) {
            return null;
        }
        List<String> name1 = Publications;
        List<String> name2 = p.Publications;
        List<String> uname1 = new ArrayList<>();
        List<String> uname2 = new ArrayList<>();
        int co = 0;
        for (String n1 : name1) {
            for (String n2 : name2) {
                if (!uname1.contains(n1) && !uname2.contains(n2)) {
                    double sim = PublicationUtils.compareTitle(n1, n2);
                    if (sim >= thresholdTitle) {
                        co++;
                        uname1.add(n1);
                        uname2.add(n2);
                    }
                }
            }
        }
        return co >= thresholdPublications;
    }

    public Boolean checkAffiliations(Person p) {
        if (Affiliations.isEmpty() || p.Affiliations.isEmpty()) {
            return null;
        }
        List<String> name1 = Affiliations;
        List<String> name2 = p.Affiliations;
        List<String> uname1 = new ArrayList<>();
        List<String> uname2 = new ArrayList<>();
        int co = 0;
        for (String n1 : name1) {
            for (String n2 : name2) {
                if (!uname1.contains(n1) && !uname2.contains(n2)) {
                    double sim = AffiliationUtils.compareTitle(n1, n2);
                    if (sim >= thresholdAff) {
                        co++;
                        uname1.add(n1);
                        uname2.add(n2);
                    }
                }
            }
        }
        return co >= thresholdAffiliation;
    }

    public Boolean checkTopics(Person p) {
        if (Affiliations.isEmpty() || p.Affiliations.isEmpty()) {
            return null;
        }
        return null;
    }

    public Person enrich(Person p, boolean ignoreName) {
        Person newPersonClon = new Person();
        newPersonClon.URIS.addAll(URIS);
        newPersonClon.URIS.addAll(p.URIS);
        if (URI == null) {
            newPersonClon.Origin = p.Origin;
            newPersonClon.URI = p.URI + "";
        } else {
            newPersonClon.Origin = Origin;
            newPersonClon.URI = URI + "";
        }
        newPersonClon.Name = new ArrayList<>();
        if (Name != null) {
            for (List<String> i : Name) {
                newPersonClon.Name.add(new ArrayList<>(i));
            }
        }
        if (!ignoreName || newPersonClon.Name.isEmpty()) {
            for (List<String> i : p.Name) {
                newPersonClon.Name.add(new ArrayList<>(i));
            }
        }
        newPersonClon.Coauthors = new ArrayList<>();
        if (Coauthors != null) {
            for (List<String> i : Coauthors) {
                newPersonClon.Coauthors.add(new ArrayList<>(i));
            }
        }
        for (List<String> i : p.Coauthors) {
            newPersonClon.Coauthors.add(new ArrayList<>(i));
        }
        if (Publications != null) {
            newPersonClon.Publications = new ArrayList<>(Publications);
        } else {
            newPersonClon.Publications = new ArrayList<>();
        }
        newPersonClon.Publications.addAll(new ArrayList<>(p.Publications));
        if (Affiliations != null) {
            newPersonClon.Affiliations = new ArrayList<>(Affiliations);
        } else {
            newPersonClon.Affiliations = new ArrayList<>();
        }
        newPersonClon.Affiliations.addAll(new ArrayList<>(p.Affiliations));
        if (Topics != null) {
            newPersonClon.Topics = new ArrayList<>(Topics);
        } else {
            newPersonClon.Topics = new ArrayList<>();
        }
        newPersonClon.Topics.addAll(new ArrayList<>(p.Topics));
        RemoveDuplicatePerson(newPersonClon);
        return newPersonClon;
    }

    private void RemoveDuplicatePerson(Person p) {
        RemoveDuplicateList(p.Name);
        RemoveDuplicateList(p.Coauthors);
        RemoveDuplicateString(p.Affiliations);
        RemoveDuplicateString(p.Publications);
        RemoveDuplicateString(p.Topics);
        p.Coauthors = NameUtils.uniqueName(p.Coauthors);
        p.Publications = PublicationUtils.uniqueTitle(p.Publications);
    }

    private void RemoveDuplicateString(List<String> in) {
        Set<String> hs = new HashSet<>();
        hs.addAll(in);
        in.clear();
        in.addAll(hs);
    }

    private void RemoveDuplicateList(List<List<String>> in) {
        Map<String, List<String>> hm = new HashMap<>();
        for (List<String> n : in) {
            hm.put(n.toString(), n);
        }
        in.clear();
        in.addAll(hm.values());
    }

}
