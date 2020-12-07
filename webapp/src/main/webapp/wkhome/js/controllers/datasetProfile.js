wkhomeControllers.controller('datasetProfile', ['$scope', '$routeParams', '$window', 'globalData', 'sparqlQuery', 'Datasets',
  function($scope, $routeParams, $window, globalData, sparqlQuery, Datasets) {
    // Define a new author object
    $scope.dataset = {};
    $scope.coauthors = {};

    /*  $scope.coauthors  = [];*/
    var dataset = $scope.dataset;
    dataset.uri = $routeParams.dataset;

    dataset.encodedUri = encodeURIComponent(dataset.uri);
    var profilevalUri = '/dataset/profileval/'+dataset.uri;
    console.log ("URL dataset");
    console.log(dataset.uri);


    Datasets.get({
      search: dataset.uri
    }, function(data) {
       data = data.response.docs[0];
       var patentdata = {}
       patentdata.title = data.title
       patentdata.abstract = data.abstract
       patentdata.subject = data.subjects
       patentdata.doi = data.doi[0]
       patentdata.date = data.date[0]
       patentdata.doi = data.doi[0]
       patentdata.url = data.url[0]
       dataset.url = data.url[0]
       patentdata.img = '/wkhome/images/projectimg.png';
//       patentdata.subject = ['Linked data', 'Semantic Web', 'Repositorio'];
       patentdata.funders  = ['CEDIA'] ;
       $scope.dataset = patentdata;
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

      $window.open(dataset.url, '_blank');
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

    globalData.centralGraph = 'https://redi.cedia.edu.ec/context/redi';
    //cambiar por centralgraph
    var getRelatedAuthorsByPublicationsQuery = globalData.PREFIX +
      'CONSTRUCT {\n' +
      '        <http://ucuenca.edu.ec/wkhuska/resultTitle> a uc:pagetitle;\n' +
      '                                                    uc:viewtitle \"Authors Related With {0}\".\n' +
      '        ?person rdfs:label ?name.\n' +
      '        ?person foaf:img ?img .\n' +
      '      } WHERE  {\n' +
      '        SELECT ?person (MAX(str(?name_)) as ?name) (MAX(str(?img_)) as ?imgm) (IRI (?imgm) as ?img)\n' +
      '        WHERE { GRAPH <'+globalData.centralGraph +'> {\n' +
      '         ?person  foaf:publications <'+dataset.uri+'> .\n' +
      '          ?person foaf:name ?name_  .\n' +
      '           OPTIONAL{?person  foaf:img ?img_.}\n' +
      '         \n' +
      '        } \n' +
      '    } group by ?person }  ';

    function relatedAuthors(id) {

      // var relatedAuthosPublicationsQuery = String.format(getRelatedAuthorsByPublicationsQuery, author["foaf:name"], id);
      executeRelatedAuthors1(getRelatedAuthorsByPublicationsQuery, "coauthor-list");
    };
    relatedAuthors(dataset.uri);



  }
]);
