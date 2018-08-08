wkhomeControllers.controller('barChart', ['$scope', 'globalData', 'sparqlQuery', 'Statistics', 'searchData', '$route', '$window',
  function($scope, globalData, sparqlQuery, Statistics, searchData, $window) {
    var self = this;

         var IES = [];
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
    });


  }
]);
