
wkhomeControllers.controller('pubInfoController', ['$routeParams', '$scope', '$window', 'sparqlQuery', 'searchData', 'globalData',
    function ($routeParams, $scope, $window, sparqlQuery, searchData, globalData) {
        $scope.todos = [];
        $scope.loadData = function () {
                $scope.filteredTodos = []
                        , $scope.currentPage = 1
                        , $scope.numPerPage = 10
                        , $scope.maxSize = 5;
                $scope.$watch('currentPage + numPerPage', function () {
                    var begin = (($scope.currentPage - 1) * $scope.numPerPage)
                            , end = begin + $scope.numPerPage;
                    $scope.filteredTodos = $scope.todos.slice(begin, end);
                });
        };
        
        var value = searchData.genericData;
        $scope.todos = [];
        var model = {};
        _.map(value, function (pub) {

            model["id"] = pub["@id"];
            model["title"] = pub["dct:title"];
            model["abstract"] = pub["bibo:abstract"] ? pub["bibo:abstract"] : "";
            model["uri"] = pub["bibo:uri"]["@id"];
            model["keywords"] = pub["bibo:Quote"];
            if (model["title"])
            {
                $scope.todos.push({id: model["id"], title: model["title"], abstract: model["abstract"], uri: model["uri"], keywords: model["keywords"]});
            }
        });
        //Remove to scroll automatically to the div "scrollToHere"
        /*$('html,body').animate({
            scrollTop: $("#scrollToHere").offset().top
        }, "slow");*/
        $scope.loadData();

        $scope.$watch('searchData.genericData', function (newValue, oldValue, scope) {
            $scope.data = {schema: {"context": globalData.CONTEXT, fields: ["rdfs:label", "uc:total"]}, data: searchData.genericData};
        });

        $scope.searchAuthor = function (pub) {
            var getAuthorDataQuery = globalData.PREFIX
                    
                    + ' CONSTRUCT {   ?subject foaf:name ?name; a foaf:Person  '
                    + ' }   '
                    + 'WHERE '
                    + '{'
                    + '     Graph <' + globalData.centralGraph + '>'
                    + '     {'
                    + '         ?subject a foaf:Person.'
                    + '         ?subject foaf:name ?name.'
                    + '         ?subject foaf:publications <' + pub.id + '>.'
                    + '     }'
                    + '} limit 1';

            sparqlQuery.querySrv({query: getAuthorDataQuery}, function (rdf) {
                jsonld.compact(rdf, globalData.CONTEXT, function (err, compacted) {
                    $scope.$apply(function () {
                        //searchData.authorSearch = compacted;
                        $scope.loadRelatedAuthors(compacted);
                    });
                });
            });
        };

        $scope.loadRelatedAuthors = function (author) {
            
            var getRelatedAuthorsByClustersQuery = globalData.PREFIX
                + ' CONSTRUCT {  <http://ucuenca.edu.ec/wkhuska/resultTitle> a uc:pagetitle. <http://ucuenca.edu.ec/wkhuska/resultTitle> uc:viewtitle "Authors Related With {0}"  .         ?subject rdfs:label ?name.         ?subject uc:total ?totalPub   }   WHERE {   { '
                + ' SELECT DISTINCT  ?subject ?name (count(?pub) as ?totalPub)'
                + ' WHERE { '
                + '   GRAPH <'+globalData.clustersGraph+'> '
                + '         { '
                + ' ?cluster <http://ucuenca.edu.ec/resource/hasPerson> <{1}> .'
                + ' ?cluster <http://ucuenca.edu.ec/resource/hasPerson> ?subject.'
                + '           ?subject foaf:publications ?pub'
                + '          {'
                + ' SELECT ?name'
                + ' {'
                + '      graph <'+globalData.centralGraph+'>'
                + '            {'
                + '        	?subject foaf:name ?name.'
                + '            }'
                + ' }'
                + '  }'
                + '              } '
                + '     } group by ?subject ?name '
                + '          }}    ';


        var getRelatedAuthorsByPublicationsQuery = globalData.PREFIX
                + '  CONSTRUCT { '
                + ' <http://ucuenca.edu.ec/wkhuska/resultTitle> a uc:pagetitle. <http://ucuenca.edu.ec/wkhuska/resultTitle> uc:viewtitle "Authors Related With {0}" . '
                + '        ?subject rdfs:label ?name. '
                + '        ?subject uc:total ?totalPub '
                + '  } '
                + '  WHERE { '
                + '  { '
                + '     SELECT ?subject (count(?pub) as ?totalPub) ?name '
                + '         WHERE { '
                + '             GRAPH <'+globalData.centralGraph+'> { '
                + '             <{1}> foaf:publications ?pub.  '
                + '            ?subject foaf:publications ?pub. '
                + '            ?subject foaf:name ?name.  } '
                + '             } '
                + '         GROUP BY ?subject ?name '
                + '  } '
                + ' }';
            
            function executeRelatedAuthors(querytoExecute, divtoload) {
                var sparqlquery = querytoExecute;
                sparqlQuery.querySrv({query: sparqlquery}, function (rdf) {
                    jsonld.compact(rdf, globalData.CONTEXT, function (err, compacted) {
                        if (compacted)
                        {
                            var entity = compacted["@graph"];
                            if (entity)
                            {
                                var authorInfo = $('div.tree-node-author-info .' + divtoload);
                                authorInfo.html('');
                                var values = entity.length ? entity : [entity];
                                var div = $('<div>');
                                authorInfo.append(div);
                                _.map(values, function (value) {
                                    var datastring = JSON.stringify(value);
                                    var anchor = $("<a class='relatedauthors' target='blank' onclick = 'return clickonRelatedauthor(\"" + value["@id"] + "\")'  >").text("");
                                    anchor.append('<img src="/wkhome/images/author-ec.png" class="img-rounded" alt="Logo Cedia" width="20" height="20"        >');

                                    anchor.append(value["rdfs:label"]);
                                    div.append(anchor);
                                    div.append("</br>");
                                    return anchor;
                                });
                            }
                        }
                    });
                }); // end  sparqlQuery.querySrv(...
            };
            
            function relatedAuthors(author) {
                var id = author["@id"];
                //var author = _.findWhere(root.author.jsonld["@graph"], {"@id": id, "@type": "foaf:Person"});
                if (author["foaf:name"])
                {
                    //********** AUTORES RELACIONADOS - POR CLUSTERING *********//
                    var query = String.format(getRelatedAuthorsByClustersQuery, author["foaf:name"], id);
                    executeRelatedAuthors(query, "authorsByClusters");
                    //********** AUTORES RELACIONADOS - POR PUBLICACION *********//
                    var query = String.format(getRelatedAuthorsByPublicationsQuery, author["foaf:name"], id);
                    executeRelatedAuthors(query, "authorsByPublications");
                }//end if author["foaf:name"]
            };
            relatedAuthors(author["@graph"][0]);
        };
        
        if ($scope.todos[0]) {
            $scope.searchAuthor($scope.todos[0]);
        }
        
    }]); //end Controller