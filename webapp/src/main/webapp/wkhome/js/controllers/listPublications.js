
wkhomeControllers.controller('listPublications', ['$translate', '$routeParams', '$scope', 'globalData', 'sparqlQuery', 'searchData', '$window',
    function ($translate, $routeParams, $scope, globalData, sparqlQuery, searchData, $window) {
      
      $translate.use($routeParams.lang);
      
      var compacted = searchData.publicationsSearch;
        /*var sparqlPublications = globalData.PREFIX
                + ' CONSTRUCT { ?keyword uc:publication ?publicationUri. '
                + ' ?publicationUri dct:contributors ?subject . '
                + ' ?subject foaf:name ?name . '
                + ' ?subject a foaf:Person . '
                + ' ?publicationUri a bibo:Document. '
                + ' ?publicationUri dct:title ?title. '
                + ' ?publicationUri bibo:abstract ?abstract. '
                + ' ?publicationUri bibo:uri ?uri. '
                + ' } '
                + ' WHERE {'
                + ' GRAPH <' + globalData.centralGraph + '>'
                + ' {'
                + ' ?subject foaf:publications ?publicationUri .'
                + ' ?subject foaf:name ?name .'
                + ' ?publicationUri dct:title ?title . '
                + ' OPTIONAL{ ?publicationUri bibo:abstract  ?abstract. } '
                + ' OPTIONAL{ ?publicationUri bibo:uri  ?uri. } '
                + ' ?publicationUri dcterms:subject ?keySub. '
                + ' ?keySub rdfs:label ?quote. '
                + ' FILTER (mm:fulltext-search(?quote, "' + 'population' + '" )) .'
                + '  BIND(REPLACE( "' + 'population' + '", " ", "_", "i") AS ?key) .'
                + '  BIND(IRI(?key) as ?keyword)'
                + ' }'
                + '}';*/
                

            //sparqlQuery.querySrv({query: sparqlPublications}, function (rdf) {

            //jsonld.compact(rdf, globalData.CONTEXT, function (err, compacted) {
            var todos = [];
            $scope.autores = [];
            
                if (compacted)
                {
                    var entity = compacted["@graph"];
                    var final_entity = entity;
                    var values = final_entity.length ? final_entity : [final_entity];
                    //send data to getKeywordTag Controller
                    
                    var publicaciones = _.where(entity, {"@type": "bibo:Document"});
                    var autores = _.where(entity, {"@type": "foaf:Person"});

                    
                    
                    var model = {};
                    _.map(publicaciones, function (pub) {
                        //var keys = Object.keys(author);

                        model["id"] = pub["@id"];
                        var title = pub["dct:title"];
                        if (title.constructor === Array || title instanceof Array || Array.isArray(title)) {
                            model["title"] = title[0];
                        } else {
                            model["title"] = pub["dct:title"];
                        }
                        
                        

                        model["author"] = pub["dct:contributor"] ? pub["dct:contributor"] : [];
                        model["abstract"] = pub["bibo:abstract"] ? pub["bibo:abstract"] : "Sorry, still not found abstract for this publication.";
                        model["uri"] = pub["bibo:uri"] ? (pub["bibo:uri"]["@id"] ? pub["bibo:uri"]["@id"] : "") : "";

                        $scope.autores = [];
                        var cont = 0;
                        _.map(pub["dct:contributors"], function (authorid) {
                            cont = cont + 1;
                            var authorresource = authorid["@id"] ? (_.findWhere(autores, {"@id": authorid["@id"]})) : (_.findWhere(autores, {"@id": authorid}));
                            $scope.autores.push({id: authorresource["@id"], name: authorresource["foaf:name"]});
                        });

                        if (model["title"])
                        {
                            todos.push({id: model["id"], title: model["title"], abstract: model["abstract"], uri: model["uri"], author: $scope.autores});
                        }
                    });
                    
                    $scope.filteredTodos = []
                        , $scope.currentPage = 1
                        , $scope.numPerPage = 10
                        , $scope.maxSize = 5;
                    $scope.$watch('currentPage + numPerPage', function () {
                    var begin = (($scope.currentPage - 1) * $scope.numPerPage)
                            , end = begin + $scope.numPerPage;
                    $scope.filteredTodos = todos.slice(begin, end);
                    });

                }
            //});
        //});
      
      
        /*if (!searchData.allkeywordsList) {
            $scope.themes = [];
            var queryKeywords = globalData.PREFIX
                    + ' CONSTRUCT { ?keyword rdfs:label ?key } '
                    + '	FROM <' + globalData.centralGraph + '> '
                    + ' WHERE { '
                    + '     SELECT  (count(?key) as ?k) ?key '
                    + '         WHERE { '
                    + '             ?subject foaf:publications ?pub. '
                    + '             ?pub dcterms:subject ?keySub. '
                    + '             ?keySub rdfs:label ?key. '
                    + '             BIND(REPLACE(?key, " ", "_", "i") AS ?unickey). '
                    + '             BIND(IRI(?unickey) as ?keyword) '
                    + '         } '
                    + '     GROUP BY ?keyword  ?key '
                    + '     HAVING(?k > 4) '
                    + '}';
            sparqlQuery.querySrv({query: queryKeywords}, function (rdf) {
                var context = {
                    "rdfs": "http://www.w3.org/2000/01/rdf-schema#",
                };
                jsonld.compact(rdf, globalData.CONTEXT, function (err, compacted) {
                    _.map(compacted["@graph"], function (pub) {
                        var model = {};
                        model["id"] = pub["@id"];
                        model["tag"] = pub["rdfs:label"];
                        $scope.themes.push({tag: model["tag"]});
                    });
                    $scope.$apply(function () {
                        searchData.allkeywordsList = $scope.themes;
                        $scope.relatedthemes = searchData.allkeywordsList;
                        $scope.selectedItem = "";
                    });

                });
            });
        } else {
            $scope.relatedthemes = searchData.allkeywordsList;
            $scope.selectedItem = "";
        }*/

        /**
        * Search for areas...
        */
        $scope.querySearch   = function (query) {
              return query ? searchData.allkeywordsList.filter( createFilterFor(query) ) : searchData.allkeywordsList;
        };

        /**
        * Create filter function for a query string
        */
        function createFilterFor(query) {
          var lowercaseQuery = angular.lowercase(query);
          return function filterFn(area) {
            return (angular.lowercase(area.tag).indexOf(lowercaseQuery) !== -1);
          };
        }

        $scope.clickonAuthor = function (id_author)
        {
            clickonRelatedauthor(id_author);
        }; //end clickonAuthor

        clickonRelatedauthor = function (id_author)
        {
              var getAuthorDataQuery = globalData.PREFIX
                    + ' CONSTRUCT {   <' + id_author + '> foaf:name ?name; a foaf:Person  '
                    + ' }   '
                    + ' WHERE '
                    + ' {'
                    + 'Graph <' + globalData.centralGraph + '>'
                    + '{'
                    + '     <' + id_author + '> a foaf:Person.'
                    + '     <' + id_author + '> foaf:name ?name'
                    + ' } '
                    + '}';

            sparqlQuery.querySrv({query: getAuthorDataQuery}, function (rdf) {
                jsonld.compact(rdf, globalData.CONTEXT, function (err, compacted) {
                    $scope.$apply(function () {
                        searchData.authorSearch = compacted;
                        //alert(author);
                        //$window.location.hash = "/" + $routeParams.lang + "/w/search?" + id_author;
                    });
                });
            });
        }; //end clickonRelatedauthor

        $scope.todos = [];
        $scope.ctrlFn = function (value)
        {
            var publicaciones = _.where(value, {"@type": "bibo:Document"});
            var autores = _.where(value, {"@type": "foaf:Person"});

            $scope.todos = [];
            $scope.autores = [];
            var model = {};
            _.map(publicaciones, function (pub) {
                //var keys = Object.keys(author);

                model["id"] = pub["@id"];
                model["title"] = pub["dct:title"];

                model["author"] = pub["dct:contributor"] ? pub["dct:contributor"] : [];
                model["abstract"] = pub["bibo:abstract"] ? pub["bibo:abstract"] : "Sorry, still not found abstract for this publication.";
                model["uri"] = pub["bibo:uri"] ? (pub["bibo:uri"]["@id"] ? pub["bibo:uri"]["@id"] : "") : "";

                $scope.autores = [];
                var cont = 0;
                _.map(pub["dct:contributors"], function (authorid) {
                    cont = cont + 1;
                    var authorresource = authorid["@id"] ? (_.findWhere(autores, {"@id": authorid["@id"]})) : (_.findWhere(autores, {"@id": authorid}));
                    $scope.autores.push({id: authorresource["@id"], name: authorresource["foaf:name"]});
                });

                if (model["title"])
                {
                    $scope.todos.push({id: model["id"], title: model["title"], abstract: model["abstract"], uri: model["uri"], author: $scope.autores});
                }
            });
            
            $scope.loadData();
        };
        
        
        /*if (!searchData.allkeywordsCloud) // if no load data by default
        {
            waitingDialog.show();
            var queryKeywords = globalData.PREFIX
                    + ' CONSTRUCT { '
                    + ' ?keyword rdfs:label ?k; '
                    + ' uc:total ?totalPub } '
                    + ' FROM <' + globalData.centralGraph + '> '
                    + ' WHERE { '
                    + '     SELECT ?keyword ?k (COUNT(DISTINCT(?subject)) AS ?totalPub) '
                    + '     WHERE { '
                    + '         ?person foaf:publications ?subject. '
                    + '         ?subject dcterms:subject  ?keySub.'
                    + '         ?keySub rdfs:label ?k .'
                    + '         BIND(IRI(?k) AS ?keyword) . '
                    + '     } '
                    + '     GROUP BY ?keyword ?k '
                    + '     HAVING(?totalPub > 2 && ?totalPub < 180) '
                    + '     ORDER BY DESC(?totalPub) '
                    + ' LIMIT 145'
                    //+'ORDER BY DESC(?totalPub) '
                    + '}';
            sparqlQuery.querySrv({query: queryKeywords}, function (rdf) {
                jsonld.compact(rdf, globalData.CONTEXT, function (err, compacted) {
                    $scope.$apply(function () {
                        $scope.data = {schema: {"context": globalData.CONTEXT, fields: ["rdfs:label", "uc:total"]}, data: compacted};
                        searchData.allkeywordsCloud = {schema: {"context": globalData.CONTEXT, fields: ["rdfs:label", "uc:total"]}, data: compacted};
                        waitingDialog.hide();
                    });
                });
            });
        }
        else
        {
            $scope.data = searchData.allkeywordsCloud;
        }*/ // end if if (!searchData.allkeywordsCloud)


        
        /**
        $scope.$watch('selectedItem', function (newValue, oldValue, scope) {
            if (newValue && newValue != "") {
                waitingDialog.show();
                var queryKeywords = globalData.PREFIX
                        + ' CONSTRUCT { '
                        + ' ?keyword rdfs:label ?key1; '
                        + ' uc:total ?totalPub '
                        + ' } '
                        + ' WHERE '
                        + ' { '
                        + ' SELECT DISTINCT ?key1 ?keyword (COUNT(DISTINCT(?publications)) AS ?totalPub)'
                        + ' WHERE'
                        + ' {'
                        + '     graph <' + globalData.centralGraph + '>    '
                        + '     {'
                       // + '         ?author foaf:publications ?publications. '
                        + '         ?publications dcterms:subject ?key1.'
                        + '         ?publications dcterms:subject ?quote . '
                        + '         FILTER (mm:fulltext-search(?quote, "' + $scope.selectedItem + '")).'
                        + '         BIND(IRI(?key1) AS ?keyword) '
                        + '     } '
                        + ' } '
                        + ' GROUP BY ?key1 ?keyword   '
                        + ' }';
                sparqlQuery.querySrv({query: queryKeywords}, function (rdf) {
                    jsonld.compact(rdf, globalData.CONTEXT, function (err, compacted) {
                        $scope.$apply(function () {
                            $scope.data = {schema: {"context": globalData.CONTEXT, fields: ["rdfs:label", "uc:total"]}, data: compacted};
                            //searchData.allkeywordsCloud = {schema: {"context": globalData.CONTEXT, fields: ["rdfs:label", "uc:total"]}, data: compacted};
                            waitingDialog.hide();
                        });
                    });
                });
            } else {
                $scope.data = searchData.allkeywordsCloud;
            }
        });
        */
        //Function that displays the buttons to export the report
        $scope.exportReport = function (id) {
            $scope.keyw = id;
            $scope.showRepButtons = true;
        };

    }]);
