<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fn="http://www.w3.org/2005/xpath-functions" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:m="http://www.lyncode.com/xoai" xmlns:oai="http://www.openarchives.org/OAI/2.0/" xmlns:oai_cerif="https://www.openaire.eu/cerif-profile/1.1/" xmlns:oai_datacite="http://datacite.org/schema/kernel-4" xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" version="1.0">
   <xsl:output method="xml" indent="yes" />
   <xsl:template match="/">
      <TriX xmlns='http://www.w3.org/2004/03/trix/trix-1/'>
         <graph>
            <xsl:for-each select="//oai_datacite:resource">
               <xsl:apply-templates select="." />
            </xsl:for-each>
         </graph>
      </TriX>
   </xsl:template>
   <xsl:template match="oai_datacite:resource">
      <xsl:variable name="dataset_doi"><xsl:value-of select="oai_datacite:identifier" /></xsl:variable>
      <xsl:variable name="dataset_uri">http://mock.com/dataset/<xsl:value-of select="fn:encode-for-uri(oai_datacite:identifier)" /></xsl:variable>
      <xsl:variable name="dataset_doi_codec"><xsl:value-of select="fn:encode-for-uri(oai_datacite:identifier)" /></xsl:variable>
      <triple>
         <uri><xsl:copy-of select="$dataset_uri" /></uri>
         <uri>http://mock.com/bar/doi</uri>
         <plainLiteral><xsl:copy-of select="$dataset_doi" /></plainLiteral>
      </triple>
      <xsl:for-each select="oai_datacite:titles/oai_datacite:title">
         <triple>
            <uri><xsl:copy-of select="$dataset_uri" /></uri>
            <uri>http://mock.com/bar/title</uri>
            <plainLiteral><xsl:value-of select="." /></plainLiteral>
         </triple>
      </xsl:for-each>
      <xsl:for-each select="oai_datacite:descriptions/oai_datacite:description">
         <triple>
            <uri><xsl:copy-of select="$dataset_uri" /></uri>
            <uri>http://mock.com/bar/abstract</uri>
            <plainLiteral>
               <xsl:value-of select="." />
            </plainLiteral>
         </triple>
      </xsl:for-each>
      <xsl:for-each select="oai_datacite:subjects/oai_datacite:subject[not(@subjectScheme)]">
         <triple>
            <uri><xsl:copy-of select="$dataset_uri" /></uri>
            <uri>http://mock.com/bar/subject</uri>
            <plainLiteral>
               <xsl:value-of select="." />
            </plainLiteral>
         </triple>
      </xsl:for-each>
      <triple>
            <uri><xsl:copy-of select="$dataset_uri" /></uri>
            <uri>http://mock.com/bar/publisher</uri>
            <plainLiteral>
               <xsl:value-of select="oai_datacite:publisher" />
            </plainLiteral>
      </triple>
      <triple>
            <uri><xsl:copy-of select="$dataset_uri" /></uri>
            <uri>http://mock.com/bar/publicationYear</uri>
            <plainLiteral>
               <xsl:value-of select="oai_datacite:publicationYear" />
            </plainLiteral>
      </triple>
      <triple>
            <uri><xsl:copy-of select="$dataset_uri" /></uri>
            <uri>http://mock.com/bar/submittedDate</uri>
            <plainLiteral>
               <xsl:value-of select="oai_datacite:dates/oai_datacite:date[@dateType='Submitted']" />
            </plainLiteral>
      </triple>
      <xsl:for-each select="oai_datacite:creators/oai_datacite:creator">
         <xsl:variable name="creator_uri">http://mock.com/author/<xsl:copy-of select="$dataset_doi_codec" />_<xsl:value-of select="fn:encode-for-uri(oai_datacite:creatorName)" /></xsl:variable>
         <triple>
            <uri><xsl:copy-of select="$dataset_uri" /></uri>
            <uri>http://mock.com/bar/creator</uri>
            <uri><xsl:copy-of select="$creator_uri" /></uri>
         </triple>
         <triple>
            <uri><xsl:copy-of select="$creator_uri" /></uri>
            <uri>http://mock.com/bar/name</uri>
            <plainLiteral><xsl:value-of select="oai_datacite:creatorName" /></plainLiteral>
         </triple>
         <triple>
            <uri><xsl:copy-of select="$creator_uri" /></uri>
            <uri>http://mock.com/bar/givenName</uri>
            <plainLiteral><xsl:value-of select="oai_datacite:givenName" /></plainLiteral>
         </triple>
         <triple>
            <uri><xsl:copy-of select="$creator_uri" /></uri>
            <uri>http://mock.com/bar/familyName</uri>
            <plainLiteral><xsl:value-of select="oai_datacite:familyName" /></plainLiteral>
         </triple>
         <triple>
            <uri><xsl:copy-of select="$creator_uri" /></uri>
            <uri>http://mock.com/bar/orcid</uri>
            <plainLiteral><xsl:value-of select="oai_datacite:nameIdentifier[@nameIdentifierScheme='ORCID']" /></plainLiteral>
         </triple>
         <triple>
            <uri><xsl:copy-of select="$creator_uri" /></uri>
            <uri>http://mock.com/bar/affiliation</uri>
            <plainLiteral><xsl:value-of select="oai_datacite:affiliation" /></plainLiteral>
         </triple>
      </xsl:for-each>
      <xsl:for-each select="oai_datacite:contributors/oai_datacite:contributor">
         <xsl:variable name="contributor_uri">http://mock.com/author/<xsl:copy-of select="$dataset_doi_codec" />_<xsl:value-of select="fn:encode-for-uri(oai_datacite:contributorName)" /></xsl:variable>
         <triple>
            <uri><xsl:copy-of select="$dataset_uri" /></uri>
            <uri>http://mock.com/bar/contributor</uri>
            <uri><xsl:copy-of select="$contributor_uri" /></uri>
         </triple>
         <triple>
            <uri><xsl:copy-of select="$contributor_uri" /></uri>
            <uri>http://mock.com/bar/name</uri>
            <plainLiteral><xsl:value-of select="oai_datacite:contributorName" /></plainLiteral>
         </triple>
         <triple>
            <uri><xsl:copy-of select="$contributor_uri" /></uri>
            <uri>http://mock.com/bar/givenName</uri>
            <plainLiteral><xsl:value-of select="oai_datacite:givenName" /></plainLiteral>
         </triple>
         <triple>
            <uri><xsl:copy-of select="$contributor_uri" /></uri>
            <uri>http://mock.com/bar/familyName</uri>
            <plainLiteral><xsl:value-of select="oai_datacite:familyName" /></plainLiteral>
         </triple>
         <triple>
            <uri><xsl:copy-of select="$contributor_uri" /></uri>
            <uri>http://mock.com/bar/orcid</uri>
            <plainLiteral><xsl:value-of select="oai_datacite:nameIdentifier[@nameIdentifierScheme='ORCID']" /></plainLiteral>
         </triple>
         <triple>
            <uri><xsl:copy-of select="$contributor_uri" /></uri>
            <uri>http://mock.com/bar/affiliation</uri>
            <plainLiteral><xsl:value-of select="oai_datacite:affiliation" /></plainLiteral>
         </triple>
      </xsl:for-each>
   </xsl:template>
</xsl:stylesheet>

