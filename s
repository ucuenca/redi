[INFO] Scanning for projects...
[WARNING] 
[WARNING] Some problems were encountered while building the effective model for org.apache.marmotta.ucuenca.wk.tools:commons:jar:0.0.1-SNAPSHOT
[WARNING] 'dependencies.dependency.(groupId:artifactId:type:classifier)' must be unique: com.github.mpkorstanje:simmetrics-core:jar -> duplicate declaration of version 3.2.1 @ org.apache.marmotta.ucuenca.wk.tools:commons:[unknown-version], /home/joe/REDI/redi/tools/commons/pom.xml, line 194, column 21
[WARNING] 
[WARNING] Some problems were encountered while building the effective model for org.apache.marmotta:marmotta-ldcache-file-mod:jar:0.0.1-SNAPSHOT
[WARNING] 'build.plugins.plugin.version' for org.apache.marmotta:buildinfo-maven-plugin is missing. @ org.apache.marmotta:marmotta-ldcache-file-mod:[unknown-version], /home/joe/REDI/redi/marmotta-ldcache-file-mod/pom.xml, line 67, column 21
[WARNING] 
[WARNING] Some problems were encountered while building the effective model for org.apache.marmotta.ucuenca.wk:webapp:war:0.0.1-SNAPSHOT
[WARNING] 'build.plugins.plugin.version' for org.apache.maven.plugins:maven-install-plugin is missing. @ org.apache.marmotta.ucuenca.wk:webapp:[unknown-version], /home/joe/REDI/redi/webapp/pom.xml, line 123, column 21
[WARNING] 
[WARNING] It is highly recommended to fix these problems because they threaten the stability of your build.
[WARNING] 
[WARNING] For this reason, future Maven versions might no longer support building such malformed projects.
[WARNING] 
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Build Order:
[INFO] 
[INFO] REDI LOD Platform
[INFO] REDI LOD Platform - Tools
[INFO] Wkhuska Tools: Common Vocabularies
[INFO] REDI - commons
[INFO] REDI - LDClient Providers
[INFO] LDClient Provider: DBLP - RDF Access
[INFO] LDClient Provider: Google Scholar - HTML Access
[INFO] LDClient Provider: Scopus - RDF Access
[INFO] LDClient Provider: Academics Knowledge - JSON Access
[INFO] LDClient Provider: Scielo - XML Access
[INFO] LDClient Provider: DOAJ - JSON Access
[INFO] LDClient Provider: ORCID - XML Access
[INFO] LDClient Provider: Springer - JSON Access
[INFO] REDI Module: Authors Module
[INFO] Marmotta Module: Workers
[INFO] Marmotta Util Collection
[INFO] SolrPlugins
[INFO] LDCache Backend: File (Mod)
[INFO] Apache Marmotta Platform: Linked Data Caching (File Backend Mod)
[INFO] Marmotta Search
[INFO] REDI Module: Publications Manager
[INFO] REDI Module: Mongo Storage
[INFO] REDI WebAPP
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Summary:
[INFO] 
[INFO] REDI LOD Platform .................................. SKIPPED
[INFO] REDI LOD Platform - Tools .......................... SKIPPED
[INFO] Wkhuska Tools: Common Vocabularies ................. SKIPPED
[INFO] REDI - commons ..................................... SKIPPED
[INFO] REDI - LDClient Providers .......................... SKIPPED
[INFO] LDClient Provider: DBLP - RDF Access ............... SKIPPED
[INFO] LDClient Provider: Google Scholar - HTML Access .... SKIPPED
[INFO] LDClient Provider: Scopus - RDF Access ............. SKIPPED
[INFO] LDClient Provider: Academics Knowledge - JSON Access SKIPPED
[INFO] LDClient Provider: Scielo - XML Access ............. SKIPPED
[INFO] LDClient Provider: DOAJ - JSON Access .............. SKIPPED
[INFO] LDClient Provider: ORCID - XML Access .............. SKIPPED
[INFO] LDClient Provider: Springer - JSON Access .......... SKIPPED
[INFO] REDI Module: Authors Module ........................ SKIPPED
[INFO] Marmotta Module: Workers ........................... SKIPPED
[INFO] Marmotta Util Collection ........................... SKIPPED
[INFO] SolrPlugins ........................................ SKIPPED
[INFO] LDCache Backend: File (Mod) ........................ SKIPPED
[INFO] Apache Marmotta Platform: Linked Data Caching (File Backend Mod) SKIPPED
[INFO] Marmotta Search .................................... SKIPPED
[INFO] REDI Module: Publications Manager .................. SKIPPED
[INFO] REDI Module: Mongo Storage ......................... SKIPPED
[INFO] REDI WebAPP ........................................ SKIPPED
[INFO] ------------------------------------------------------------------------
[INFO] BUILD FAILURE
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 1.323 s
[INFO] Finished at: 2019-03-06T17:28:27-05:00
[INFO] Final Memory: 7M/114M
[INFO] ------------------------------------------------------------------------
[ERROR] No goals have been specified for this build. You must specify a valid lifecycle phase or a goal in the format <plugin-prefix>:<goal> or <plugin-group-id>:<plugin-artifact-id>[:<plugin-version>]:<goal>. Available lifecycle phases are: validate, initialize, generate-sources, process-sources, generate-resources, process-resources, compile, process-classes, generate-test-sources, process-test-sources, generate-test-resources, process-test-resources, test-compile, process-test-classes, test, prepare-package, package, pre-integration-test, integration-test, post-integration-test, verify, install, deploy, pre-clean, clean, post-clean, pre-site, site, post-site, site-deploy. -> [Help 1]
[ERROR] 
[ERROR] To see the full stack trace of the errors, re-run Maven with the -e switch.
[ERROR] Re-run Maven using the -X switch to enable full debug logging.
[ERROR] 
[ERROR] For more information about the errors and possible solutions, please read the following articles:
[ERROR] [Help 1] http://cwiki.apache.org/confluence/display/MAVEN/NoGoalSpecifiedException
