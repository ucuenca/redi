wkhomeControllers.controller('authorProfile', ['$scope', '$routeParams', '$window', 'globalData', 'sparqlQuery', 'queryProfile',
  function($scope, $routeParams, $window , globalData , sparqlQuery ,queryProfile) {
    // Define a new author object
    $scope.author = {}
    var author = $scope.author;
    author.uri = $routeParams.author;
    author.encodedUri = encodeURIComponent(author.uri);
    var newhost =  $window.location.protocol + '//' + $window.location.hostname + ($window.location.port ? ':8080' : '') + '';


    queryProfile.query({
      id: author.uri
    }, function(Result) {

            $scope.author= Result;
            console.log (Result);
  
    });



    

        $scope.tree = function () {  
          $window.location.hash = '/author/tree/' + author.uri;
        };

        $scope.network = function () {

          $window.location.hash = '/author/network/' + author.uri;
        };

        $scope.publication = function () {

          $window.location.hash = 'author/publications/q=author:"' + author.encodedUri + '"&fl=*&rows=10&wt=json/author/' + author.uri;
        };
     
         clickonRelatedauthor = function (uri) {
          $window.location.hash = '/author/profile/' + uri;
          }


        function executeRelatedAuthors1(querytoExecute, divtoload) {
                sparqlQuery.querySrv({query: querytoExecute}, function (rdf) {
                    jsonld.compact(rdf, globalData.CONTEXT, function (err, compacted) {
                        var authorInfo = $('div.coauthor-panel .' + divtoload);
                        authorInfo.html('');

                        if (compacted) {
                            var entity = compacted["@graph"];
                            if (entity) {
                                var values = entity.length ? entity : [entity];
                                var div = $('<div>');
                                authorInfo.append(div);

                                values = _.sortBy(values, function (value) {
                                    if (value.hasOwnProperty('uc:total')) {
                                        return parseInt(value["uc:total"]["@value"]);
                                    } else
                                        return -1;
                                }).reverse();

                                values = _.first(values, 20);

                                _.map(values, function (value) {
                                    if (value["rdfs:label"] && value["uc:total"]["@value"]) {
                                        var authorname = typeof value["rdfs:label"] == "string" ? value["rdfs:label"] : _.first(value["rdfs:label"], 1);
                                        var anchor = $("<a class='listCoauthor' target='blank' onclick='return clickonRelatedauthor(\""+value["@id"]+"\")'  >").text("");
                                        var img = value["foaf:img"] ? value["foaf:img"]["@id"] : "/wkhome/images/author-ec.png";
                                        anchor.append('<img src="' + img + '" class="img-rounded" alt="Logo Cedia" width="30" height="30"        >');
                                        anchor.append("  "+ authorname + "(" + value["uc:total"]["@value"] + ")");
                                        div.append(anchor);
                                        div.append("</br>");
                                        div.append("</br>");

                                        return anchor;
                                    }
                                });
                            }
                         //   waitingDialog.hide();
                        }
                      //  waitingDialog.hide();
                    });
                }); // end  sparqlQuery.querySrv(...
            };



            //cambiar por centralgraph
             var getRelatedAuthorsByPublicationsQuery = globalData.PREFIX 
                + 'CONSTRUCT {'
                + '  <http://ucuenca.edu.ec/wkhuska/resultTitle> a uc:pagetitle;'
                + '                                              uc:viewtitle "Authors Related With {0}".'
                + '  ?subject rdfs:label ?name.'
                + '  ?subject uc:total ?totalPub .'
                + '  ?subject foaf:img ?img .'
                + '} WHERE  {'
                + '  SELECT ?subject (MAX(str(?name_)) as ?name) (COUNT( DISTINCT ?pub) as ?totalPub) (MAX(str(?img_)) as ?imgm) (IRI (?imgm) as ?img)'
                + '  WHERE { GRAPH <https://redi.cedia.edu.ec/context/redi> {'
                + '    <'+author.uri+'> foaf:publications ?pub.'
                + '    ?subject foaf:publications ?pub;'
                + '             foaf:name ?name_.'
                + '     OPTIONAL{?subject  foaf:img ?img_.}'
                + '    FILTER(<'+author.uri+'> != ?subject)'
                + '  }} GROUP BY ?subject  order by desc(?totalPub) limit 6'
                + '}';

            function relatedAuthors (id) {
                
               
         
                   // var relatedAuthosPublicationsQuery = String.format(getRelatedAuthorsByPublicationsQuery, author["foaf:name"], id);
                    executeRelatedAuthors1(getRelatedAuthorsByPublicationsQuery, "coauthor-list");
                
            };
                  relatedAuthors (author.uri);



  }]);
