
wkhomeControllers.controller('groupbyCloud', ['$translate', '$routeParams', '$scope', 'globalData', 'sparqlQuery', 'searchData', '$route', '$window',
    function ($translate, $routeParams, $scope, globalData, sparqlQuery, searchData, $route, $window) {
        $translate.use($routeParams.lang);
        var selected = null;
        $scope.ifClick = function (value)
        {
            //selected = value; //searchData.genericData = value;
            $scope.todos = [];
            var model = {};
            _.map(value, function (pub) {

                model["id"] = pub["@id"];
                model["title"] = pub["dct:title"];
                model["abstract"] = pub["bibo:abstract"] ? pub["bibo:abstract"] : "";
                model["uri"] = pub["bibo:uri"] ? pub["bibo:uri"]["@id"] : "";
                model["keywords"] = pub["bibo:Quote"] ? pub["bibo:Quote"] : "";

                $scope.todos.push({id: model["id"], title: model["title"], abstract: model["abstract"], uri: model["uri"], keywords: model["keywords"]});

            });
            $scope.data = {schema: {"context": globalData.CONTEXT, fields: ["rdfs:label", "uc:total"]}, data: value};
            $scope.loadData();
            if ($scope.todos[0]) {
                $scope.searchAuthor($scope.todos[0]);
            }
            //$window.location.hash = '/' + $routeParams.lang + "/w/publication";
            $('html,body').animate({
                scrollTop: $("#scrollToHere").offset().top
            }, "slow");
        };

        $('html,body').animate({
            scrollTop: $("#scrollToTop").offset().top
        }, "slow");
        $scope.$watch('searchData.areaSearch', function (newValue, oldValue, scope) {

            if (searchData.areaSearch) {
                var areaSearch = searchData.areaSearch["@graph"];
                if (areaSearch) {
                    //    if (authorSearch.length > 1) {
                    var candidates = _.map(areaSearch, function (area) {
                        var model = {};
                        //var keys = Object.keys(author);
                        model["id"] = area["@id"];
                        model["label"] = area["rdfs:label"];
                        return model;
                    });
                    $scope.candidates = candidates;
                    $scope.selectedAuthor = function ($event, label) {
                        $('#searchResults').modal('hide');
                        searchData.researchArea = label;
                        $scope.selectedItem = label;
                    };
                    waitingDialog.hide();
                    $('#searchResults').modal('show');
                }//End if(authorSearch)
                else
                {
                    alert("Information not found");
                    $window.location.hash = "/";
                    waitingDialog.hide();
                }

            }

        }, true);
        if (!searchData.allkeywords)
        {
            $scope.themes = [];
            waitingDialog.show();
            executeGroupTags();
            function executeGroupTags() {

                //only keywords that appear in more than 2 articles
                var queryKeywords = globalData.PREFIX
                        + ' CONSTRUCT { ?keywordp rdfs:label ?keyp } '
                        + '	FROM <' + globalData.centralGraph + '> '
                        + ' WHERE { '
                        + ' SELECT DISTINCT (count(?key) as ?k) (SAMPLE(?keyword) as ?keywordp) (SAMPLE(?key) as ?keyp) '
                        + ' WHERE { '
                        + '         ?subject foaf:publications ?pubs. '
                        + '         ?subject dct:subject ?key. '
                        + '         BIND(REPLACE(?key, " ", "_", "i") AS ?unickey). '
                        + '         BIND(IRI(?unickey) as ?keyword) '
                        + ' }'
                        //+ ' group by ?keyword  ?key '
                        + ' group by ?subject'
                        // + ' HAVING(?k > 1) '
                        + '}';
                sparqlQuery.querySrv({query: queryKeywords}, function (rdf) {
                    waitingDialog.show();
                    jsonld.compact(rdf, globalData.CONTEXT, function (err, compacted) {

                        _.map(compacted["@graph"], function (pub) {
                            var model = {};
                            model["id"] = pub["@id"];
                            model["tag"] = pub["rdfs:label"];
                            $scope.themes.push({tag: model["tag"]});
                        });
                        applyvalues();
                        waitingDialog.hide();
                    });
                });
            }
            function applyvalues() {
                $scope.$apply(function () {
                    $scope.relatedthemes = $scope.themes;
                    $scope.selectedItem = searchData.researchArea; // Selected Research Area Filter Default
                    searchData.allkeywords = $scope.themes;
                });
            }
            ;
        }
        else
        {
            $scope.relatedthemes = searchData.allkeywords;
            $scope.selectedItem = searchData.researchArea;
        } //  end  if (!searchData.allkeywords)



        $scope.$watch('gbselectedItem', function () {//Funcion para cuando selecciona el filtro para agrupar
            groupByResources($scope.dataaux, $scope.gbselectedItem);
        });
        $scope.$watch('selectedItem', function () {//Funcion para cuando se selecciona la Research Area
            $scope.selectedItem = $scope.selectedItem ? $scope.selectedItem : "SEMANTICWEB";
            waitingDialog.show("Consultando Autores Relacionados con:  \"" + $scope.selectedItem + "\"");
            $scope.todos = [];
            $scope.filteredTodos = [];
            loadResources($scope.selectedItem, $scope.gbselectedItem); //query and load resource related with selected theme
            var authorInfo = $('div.tree-node-author-info .authorsByClusters');
            authorInfo.html('');
            authorInfo = $('div.tree-node-author-info .authorsByPublications');
            authorInfo.html('');
            var title = $('div#scrollToHere.col-md-12 div.col-md-12.head-info');
            title.html('');
        });
        function groupByResources(values, groupby)//grouByResources resources by ...
        {
            // executeDraw(values,groupby);
            //this activity is cheking directly in cloudGroup.js 
        }//end grouByResources

        function loadResources(value, groupby)//load resources related with selected keyword
        {

            var queryRelatedPublications = globalData.PREFIX
                    + ' CONSTRUCT {      '
                    + ' ?subject foaf:name  ?nameauthor . '
                    + ' ?subject  dct:provenance ?sourcename . } '
                    + ' WHERE {'
                    + '   SELECT ?subject ?nameauthor ?sourcename'
                    + '       WHERE {'
                    + '            graph <' + globalData.centralGraph + '> {'

                    + '         ?subject foaf:publications ?pubs .'
                    + '         ?subject foaf:name  ?nameauthor .'
                    + '         ?subject dct:subject ?keyword.    '
                    + '         ?subject dct:provenance ?provenance .'

                    + '         ?subject2 dct:subject ?keyfilter.    '
                    + '         ?subject2 dct:subject ?keyword.    '
                    + '         FILTER (regex(UCASE(?keyfilter), "' + value + '"))'
                    + '         { '
                    + '              select * '
                    + '                 WHERE '
                    + '                 { '
                    + '                     graph <http://ucuenca.edu.ec/wkhuska/endpoints> '
                    + '                     { '
                    + '                         ?provenance uc:name ?sourcename  '
                    + '                     } '
                    + '                 } '
                    + '          } '
                    + '    }'
                    + '    } group by ?subject ?nameauthor ?sourcename LIMIT 200'
                    + ' } ';
            $scope.authorsByKeyword = [];
            sparqlQuery.querySrv({query: queryRelatedPublications}, function (rdf) {
                jsonld.compact(rdf, globalData.CONTEXT, function (err, compacted) {
                    if (compacted["@graph"])
                    {
                        _.map(compacted["@graph"], function (pub) {
                            var model = {};
                            //model["Publication"] = pub["foaf:publications"]["@id"];
                            model["Author"] = pub["@id"];
                            model["Name"] = pub["foaf:name"];
                            model["Organization"] = pub["dct:provenance"];
//                        model["Source"] = pub["uc:namesource"];
//                        model["Abstract"] = pub["uc:abstract"];
//                        model["Author"] = pub["uc:nameauthor"];
                            $scope.$apply(function () {
                                $scope.authorsByKeyword.push({id: model["Author"], name: model["Name"], organization: model["Organization"]});
                            });
                        });
                        executeDraw($scope.authorsByKeyword, groupby);
                        searchData.areaSearch = null;
                        waitingDialog.hide();
                    }
                    else//no retrieve data
                    {
                        alert("No se han recuperado datos");
                        waitingDialog.hide();
                    }
                }); //end jsonld.compact
            }); //end sparqlService
        }//end Load Resources

        function executeDraw(dataToDraw, groupby)
        {
            $scope.$apply(function () {
                $scope.data = [{value: dataToDraw, group: groupby}];
                $scope.dataaux = dataToDraw;
            });
        }

        //PUBLICATION INFORMATION
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

        //var value = searchData.genericData;

        //Remove to scroll automatically to the div "scrollToHere"
        /*$('html,body').animate({
         scrollTop: $("#scrollToHere").offset().top
         }, "slow");*/


        $scope.$watch(selected, function (newValue, oldValue, scope) {
            if (newValue) {
                $scope.data = {schema: {"context": globalData.CONTEXT, fields: ["rdfs:label", "uc:total"]}, data: searchData.genericData};
                $scope.loadData();
                if ($scope.todos[0]) {
                    $scope.searchAuthor($scope.todos[0]);
                }
            }
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
                    + '   GRAPH <' + globalData.clustersGraph + '> '
                    + '         { '
                    + ' ?cluster uc:hasPerson <{1}> .'
                    + ' ?cluster uc:hasPerson ?subject.'
                    + '           ?subject foaf:publications ?pub'
                    + '          {'
                    + ' SELECT ?name'
                    + ' {'
                    + '      graph <' + globalData.centralGraph + '>'
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
                    + '             GRAPH <' + globalData.centralGraph + '> { '
                    + '             <{1}> foaf:publications ?pub.  '
                    + '            ?subject foaf:publications ?pub. '
                    + '            ?subject foaf:name ?name.  } '
                    + '             } '
                    + '         GROUP BY ?subject ?name '
                    + '  } '
                    + ' }';

            clickonRelatedauthor = function (author)
            {
                var getAuthorDataQuery = globalData.PREFIX
                        + ' CONSTRUCT {   <' + author + '> foaf:name ?name; a foaf:Person  '
                        + ' }   '
                        + ' WHERE '
                        + ' {'
                        + '     <' + author + '> foaf:name ?name'
                        + ' } ';

                sparqlQuery.querySrv({query: getAuthorDataQuery}, function (rdf) {
                    jsonld.compact(rdf, globalData.CONTEXT, function (err, compacted) {
                        $scope.$apply(function () {
                            //$scope.data = compacted;
                            searchData.authorSearch = compacted;
                            $window.location.hash = "/" + $routeParams.lang + "/w/search?" + compacted['@graph'][0]['foaf:name'].replace(',', '-').replace(' ', '_');
                        });
                    });
                });

            };

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
            }
            ;

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
            }
            ;
            relatedAuthors(author["@graph"][0]);
        };


    }]); //end groupTagsController 
