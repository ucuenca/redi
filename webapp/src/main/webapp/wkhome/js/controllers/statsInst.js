wkhomeControllers.controller('statsInst', ['$scope', 'globalData', 'sparqlQuery', 'Statistics', 'searchData', '$route', '$window',
  function($scope, globalData, sparqlQuery, Statistics, searchData, $window) {
    var self = this;
    //$scope.data = [];

      var querytoExecute =  "PREFIX dct: <http://purl.org/dc/terms/> " +
    "PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
    "PREFIX schema: <http://schema.org/> " +
    "PREFIX bibo: <http://purl.org/ontology/bibo/> " +
    "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> " +
    "SELECT  ?y  (COUNT( ?publication ) as ?num)    " +
    "WHERE { " +
    "  graph <https://redi.cedia.edu.ec/context/redi> { " +
    "  ?author schema:memberOf  <https://redi.cedia.edu.ec/resource/organization/UDA> . " +
    "  ?author foaf:publications ?publication.   " +
    "  ?publication bibo:created ?y2 . " +
    "   bind( strbefore( ?y2, '-' ) as ?y3 ).   " +
    "   bind( strafter( ?y2, ' ' ) as ?y4 ).  " +
    "   bind( if (str(?y3)='' && str(?y4)='',?y2, if(str(?y3)='',strafter( ?y2, ' ' ),strbefore( ?y2, '-' ))) as ?y )  " +
    "      " +
    "  } " +
    "}  GROUP BY ?y Order by ASC(?y)";

       sparqlQuery.querySrv({
        query: querytoExecute
      }, function(rdf) {

        jsonld.compact(rdf, globalData.CONTEXT, function(err, compacted) {
          console.log (compacted);
          $scope.data = compacted;
        });
      });
   
     /*    var IES = [];
         var Authors = [];
         var Publications = [];
         var countAuthors = 0;
         var countPub = 0;
        self.data = Statistics.query({
      id: 'barchar'
    }, function(data) {
      var totalPubAut = data["@graph"];
      if (totalPubAut) {
        _.map(totalPubAut, function(total) {
          var sourceid = total["@id"];
          var sourcename = total["uc:name"];
          var totalAuthors = total["uc:totalAuthors"]["@value"];
          var totalPublications = total["uc:totalPublications"]["@value"];
           IES.push ( sourcename);
           Authors.push ( parseInt(totalAuthors));
           Publications.push ( parseInt(totalPublications)); 
           countAuthors = countAuthors + parseInt(totalAuthors);
           countPub = countPub +  parseInt(totalPublications);

        });
        dataToSend = {
          categories : IES ,
          Data : [ { name : "Authors" , data : Authors } ,
                  { name : "Publications", data : Publications }
          ] 
          ,
          total : {
                "totalAuthors" : countAuthors ,
                "totalPub" : countPub
          }
        };

        $scope.data = dataToSend;
      }
    });*/


  }
]);
