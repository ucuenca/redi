wkhomeControllers.controller('serviceProfile', ['$scope', '$routeParams', '$window', 'globalData', 'sparqlQuery', 'Services',
  function($scope, $routeParams, $window, globalData, sparqlQuery, Services) {
    // Define a new author object
    $scope.service = {};
 
    /*  $scope.coauthors  = [];*/
    var service = $scope.service;
    service.uri = $routeParams.service;

    service.encodedUri = encodeURIComponent(service.uri);
    var profilevalUri = '/service/profileval/'+service.uri;
    console.log ("URL service");
    console.log(service.uri);


    Services.get({
      search: service.uri
    }, function(data) {
       data = data.response.docs[0];
       var patentdata = {}
       patentdata.title = data.title
       patentdata.abstract = data.abstract
       patentdata.subject = data.subjects;
       patentdata.subject = patentdata.subject.concat(data.areas);
       patentdata.funders = data.organization && data.organization.length!=0? data.organization[0] :"" ;
       patentdata.url = data.url
       service.url = data.url
       patentdata.date = data.date[0]
       patentdata.stype = data.stype[0]
       patentdata.img = '/wkhome/images/projectimg.png';
       $scope.service = patentdata;
    } , function (error){
      console.log (error);
    });

    function acro ( uri ) {
      console.log (uri);

      return uri.slice(uri.lastIndexOf("/")+1).replace('university_university','').replace('_',' ');

    }

    function acro ( uri ) {
      console.log (uri);

      return uri.slice(uri.lastIndexOf("/")+1).replace('university_university','').replace('_',' ');

    }
    $scope.tree = function() {
      $window.location.hash = '/author/tree/' + patent.uri;
    };

    $scope.stat = function() {

      $window.location.hash = '/info/statisticsbyAuthor/' + patent.uri; 
    };


    $scope.network = function() {

      $window.location.hash = '/author/network/' + patent.uri;
    };

    $scope.publication = function() {
      $window.open(service.url, '_blank');
    };

    $scope.clickonRelatedauthor = function(uri) {

      $window.location.hash = '/author/profile/' + uri;
    }

    function executeRelatedAuthors1(querytoExecute, divtoload) {
       console.log (querytoExecute);
      sparqlQuery.querySrv({
        query: querytoExecute
      }, function(rdf) {
        console.log (rdf);

        jsonld.compact(rdf, globalData.CONTEXT, function(err, compacted) {
          var authorInfo = $('div.coauthor-panel .' + divtoload);
          /*    authorInfo.html('');*/

          if (compacted) {
            var entity = compacted["@graph"];
            if (entity) {
              var values = entity.length ? entity : [entity];
              var div = $('<div>');
              authorInfo.append(div);

              values = _.sortBy(values, function(value) {
                if (value.hasOwnProperty('uc:total')) {
                  return parseInt(value["uc:total"]["@value"]);
                } else
                  return -1;
              }).reverse();

              values = _.first(values, 20);
              var coauthors = [];
              _.map(values, function(value) {

                if (value["rdfs:label"] ) {
                  var coauthor = {};
                  var authorname = typeof value["rdfs:label"] == "string" ? value["rdfs:label"] : _.first(value["rdfs:label"], 1);
                  var anchor = $("<a class='listCoauthor' target='blank' onclick='return clickonRelatedauthor(\"" + value["@id"] + "\")'  >").text("");
                  var img = value["foaf:img"] ? value["foaf:img"]["@id"] : "/wkhome/images/author-ec.png";
                  coauthor.authorname = authorname;
                  coauthor.id = value["@id"];
                  coauthor.img = img;
                  coauthors.push(coauthor);

                  return "";
                }
              });

              $scope.$apply(function() {
                $scope.coauthors.data = coauthors;
              });
            }
            //   waitingDialog.hide();
          }
          //  waitingDialog.hide();
        });
      }); // end  sparqlQuery.querySrv(...
    };




  }
]);
