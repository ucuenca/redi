wkhomeControllers.controller('authorProfile', ['$scope', '$routeParams', '$window', 'globalData', 'sparqlQuery', 'Authors', '$translate' , '$rootScope' ,
  function($scope, $routeParams, $window, globalData, sparqlQuery, Authors , $translate   , $rootScope ) {
    // Define a new author object
    $scope.author = {};
    $scope.coauthors = {};

    /*  $scope.coauthors  = [];*/
    var author = $scope.author;
    author.uri = $routeParams.author;
    author.encodedUri = encodeURIComponent(author.uri);
    var newhost = $window.location.protocol + '//' + $window.location.hostname + ($window.location.port ? ':8080' : '') + '';
    var profilevalUri = '#/author/profileval/'+author.uri;

    var language = $translate.use();

    Authors.query({
      id: author.uri
    }, function(data) {
      language = $translate.use();
      $scope.author = data;


      for (var ki = 0; ki<data.orgs.length; ki++){
          var las = data.orgs[ki].split('/');
          data.orgs[ki] = {orgName:las[las.length-1], org:data.orgs[ki]};
      }
      
      if ( data.bio && data.bio.length > 300 )  {
        $scope.author.bio = data.bio.substring(0, 300)+" ...";
      
      }else if (data.bio == null || data.bio.length < 1){
        $scope.author.bio = "Autor registrado en REDI ";

      }

      $scope.author.cluster = language == "es" ? data.clustersEs : data.clustersEn;
      $scope.author.topics = language == "es" ? data.topicsEs : data.topicsEn;
    


      var img = data.img == null ? "wkhome/images/no_photo.jpg" : data.img;
      $scope.author.img = img;
      $scope.author.claimUri = profilevalUri;
    
    });

    $scope.tree = function() {
      $window.location.hash = '/author/tree/' + author.uri;
    };

    $scope.stat = function() {

      $window.location.hash = '/info/statisticsbyAuthor/' + author.uri; 
    };


    $scope.network = function() {

      $window.location.hash = '/author/network/' + author.uri;
    };

    $scope.publication = function() {

      $window.location.hash = '/author/publications/q=author:%22' + author.uri + '%22&fl=*&rows=10&wt=json/authorProfile/' + author.uri;
    };

    $scope.clickonRelatedauthor = function(uri) {

      $window.location.hash = '/author/profile/' + uri;
    }

    function executeRelatedAuthors1(querytoExecute, divtoload) {

      sparqlQuery.querySrv({
        query: querytoExecute
      }, function(rdf) {

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

                if (value["rdfs:label"] && value["uc:total"]["@value"]) {
                  var coauthor = {};
                  var authorname = typeof value["rdfs:label"] == "string" ? value["rdfs:label"] : _.first(value["rdfs:label"], 1);
                  var anchor = $("<a class='listCoauthor' target='blank' onclick='return clickonRelatedauthor(\"" + value["@id"] + "\")'  >").text("");
                  var img = value["foaf:img"] ? value["foaf:img"]["@id"] : "wkhome/images/author-ec.png";
                  coauthor.authorname = authorname;
                  coauthor.id = value["@id"];
                  coauthor.img = img;
                  coauthor.total = value["uc:total"]["@value"];
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


    //cambiar por centralgraph
    var getRelatedAuthorsByPublicationsQuery = globalData.PREFIX +
      'CONSTRUCT {' +
      '  <http://ucuenca.edu.ec/wkhuska/resultTitle> a uc:pagetitle;' +
      '                                              uc:viewtitle "Authors Related With {0}".' +
      '  ?subject rdfs:label ?name.' +
      '  ?subject uc:total ?totalPub .' +
      '  ?subject foaf:img ?img .' +
      '} WHERE  {' +
      '  SELECT ?subject (MAX(str(?name_)) as ?name) (COUNT( DISTINCT ?pub) as ?totalPub) (MAX(str(?img_)) as ?imgm) (IRI (?imgm) as ?img)' +
      '  WHERE { GRAPH <' + globalData.centralGraph + '> {' +
      '    <' + author.uri + '> foaf:publications ?pub.' +
      '    ?subject foaf:publications ?pub;' +
      '             foaf:name ?name_ ; schema:memberOf ?org .' +
      '     OPTIONAL{?subject  foaf:img ?img_.}' +
      '    FILTER(<' + author.uri + '> != ?subject)' +
      '  } GRAPH <'+globalData.organizationsGraph+'> { ?org ?prop ?val } } GROUP BY ?subject  order by desc(?totalPub) limit 6' +
      '}';

    function relatedAuthors(id) {
      // var relatedAuthosPublicationsQuery = String.format(getRelatedAuthorsByPublicationsQuery, author["foaf:name"], id);
      executeRelatedAuthors1(getRelatedAuthorsByPublicationsQuery, "coauthor-list");
    };
    relatedAuthors(author.uri);



  }
]);
