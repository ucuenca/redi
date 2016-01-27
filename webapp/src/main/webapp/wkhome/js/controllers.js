//'use strict';

/* Controllers */

var wkhomeControllers = angular.module('wkhomeControllers', ['mapView', 'cloudTag', 'pieChart', 'explorableTree', 'cloudGroup', 'cloudCluster', 'genericCloud', 'snapscroll', 'ui.bootstrap.pagination', 'keywClusters', 'clusterKeywCloud']);
wkhomeControllers.controller('indexInformation', ['$scope', '$window', 'Phone',
    function ($scope, $window, Phone) {
        $scope.welcome = "Hello World!";
        $scope.user = {};
    }]);
//  Main  application controller - TEST D3
wkhomeControllers.controller('MainCtrl', ['$scope', '$interval',
    function ($scope, $interval) {
        var time = new Date('2014-01-01  00:00:00  -0500');
        //  Random  data  point generator
        var randPoint = function () {
            var rand = Math.random;
            return  {time: time.toString(), visitors: rand() * 100};
        }
        //  We  store a list  of  logs
        $scope.data = [randPoint()];
        $interval(function () {
            time.setSeconds(time.getSeconds() + 1);
            $scope.data.push(randPoint());
        }, 1000);
    }]);
//  Main  application controller - TEST D3
wkhomeControllers.controller('worldPath', ['$scope',
    function ($scope) {
        $scope.data = {};
        var places = {
            HNL: [-157 - 55 / 60 - 21 / 3600, 21 + 19 / 60 + 07 / 3600],
            HKG: [113 + 54 / 60 + 53 / 3600, 22 + 18 / 60 + 32 / 3600],
            SVO: [37 + 24 / 60 + 53 / 3600, 55 + 58 / 60 + 22 / 3600],
            HAV: [-82 - 24 / 60 - 33 / 3600, 22 + 59 / 60 + 21 / 3600],
            CCS: [-66 - 59 / 60 - 26 / 3600, 10 + 36 / 60 + 11 / 3600],
            UIO: [-78 - 21 / 60 - 31 / 3600, 0 + 06 / 60 + 48 / 3600]
        };
        $scope.data.places = places;
        $scope.data.route = {
            type: "LineString",
            coordinates: [
                places.HNL,
                places.HKG,
                places.SVO,
                places.HAV,
                places.CCS,
                places.UIO
            ]
        };
    }]);


wkhomeControllers.controller('colorClusterCloud', ['$scope', '$window', 'globalData', 'sparqlQuery', 'searchData',
    function ($scope, $window, globalData, sparqlQuery, searchData) {
        var queryTotalAuthors = globalData.PREFIX
                + ' CONSTRUCT { ?cluster a uc:Cluster . '
                + ' ?cluster uc:hasPerson ?person .  '
                + '            ?person a foaf:Person }  '
                + ' WHERE { '
                + '     GRAPH <' + globalData.clustersGraph + '> '
                + '         { '
                + '         	?cluster uc:hasPerson ?person.   '
                + '         } '
                + ' } ORDER BY ?cluster LIMIT 100';

        sparqlQuery.querySrv({query: queryTotalAuthors}, function (rdf) {
            jsonld.compact(rdf, globalData.CONTEXT, function (err, compacted) {
                //$scope.data = compacted;
                var clusters = _.where(compacted['@graph'], {"@type": "uc:Cluster"});

                //var clusters = compacted['@graph'];
                var data = []
                clusters.forEach(function (cluster) {
                    data.push({label: cluster['@id'], members: cluster['uc:hasPerson'], color: get_random_color2()});
                });
                $scope.$apply(function () {
                    $scope.data = {'entityName': 'Authors', 'data': data};
                });
            });
        });// End sparqlQuery.querySrv ...

        function get_random_color2()
        {
            var r = function () {
                return Math.floor(Math.random() * 256)
            };
            return "rgb(" + r() + "," + r() + "," + r() + ")";
        }
    }]);

wkhomeControllers.controller('totalPersonReg', ['$scope', '$window', 'globalData', 'sparqlQuery', 'searchData',
    function ($scope, $window, globalData, sparqlQuery, searchData) {
        //if click in pie-chart (Authors)
        $scope.ifClick = function (value)
        {
            searchData.genericData = value;
            $window.location.hash = "w/cloud?" + "datacloud";
        };
        //sparql construct to get total authors of publications
        var queryTotalAuthors = globalData.PREFIX
                + 'CONSTRUCT { '
                + '         ?provenance a uc:Endpoint . '
                + '         ?provenance uc:name ?name . '
                + '         ?provenance uc:total ?total  } '
                + 'WHERE { '
                + '         SELECT ?provenance ?name (COUNT(DISTINCT(?s)) AS ?total) '
                + '             WHERE { '
                + '                 ?s a foaf:Person. '
                + '                 ?s foaf:publications ?pub . '
                + '                 ?s dct:provenance ?provenance . '
                + '                 ?provenance uc:name ?name . } '
                + ' GROUP BY ?provenance ?name '
                + ' } ';

        //for parliament triplestore test
//        var queryTotalAuthors = 'PREFIX dct: <http://purl.org/dc/terms/> PREFIX foaf: <http://xmlns.com/foaf/0.1/> '
//                + 'PREFIX uc: <http://ucuenca.edu.ec/wkhuska/resource/> '
//                + 'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> '
//                +       " construct { ?s ?p ?o } "  
//                +    "  WHERE {  ?s ?p ?o } limit 10 " ;

        sparqlQuery.querySrv({query: queryTotalAuthors}, function (rdf) {
            var context = {
                "uc": "http://ucuenca.edu.ec/resource/"
            };
            jsonld.compact(rdf, globalData.CONTEXT, function (err, compacted) {
                //$scope.data = compacted;
                var endpoints = compacted['@graph'];
                var data = []
                endpoints.forEach(function (endpoint) {
                    data.push({label: endpoint['uc:name'], value: endpoint['uc:total']['@value']});
                });
                $scope.$apply(function () {
                    $scope.data = {'entityName': 'Authors', 'data': data};
                });
            });
        });// End sparqlQuery.querySrv ...
        /*************************************************************/
        /*************************************************************/
        /*query to get the keywords in memory */
        /*************************************************************/
        loadAllKeyword();
        $scope.themes = [];
        function loadAllKeyword() {
            var queryKeywords = globalData.PREFIX
                    + ' CONSTRUCT { ?keyword rdfs:label ?key } '
                    + '	FROM <' + globalData.centralGraph + '> '
                    + ' WHERE { '
                    + '     SELECT  (count(?key) as ?k) ?key '
                    + '         WHERE { '
                    + '             ?subject bibo:Quote ?key. '
                    + '             BIND(REPLACE(?key, " ", "_", "i") AS ?unickey). '
                    + '             BIND(IRI(?unickey) as ?keyword) '
                    + '         } '
                    + '     GROUP BY ?keyword  ?key '
                    + '     HAVING(?k > 10) '
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
                        searchData.allkeywords = $scope.themes;
                    });
                    waitingDialog.hide();
                });
            });
        }
        /***********************************/
        /***********************************/




        /*********************************************/
        /* LOAD DATA TO KEYWORDS CLUD */
        /*********************************************/

        var queryKeywords = globalData.PREFIX
                + ' CONSTRUCT { '
                + '         ?keyword rdfs:label ?k; '
                + '               uc:total ?totalPub } '
                + ' FROM <' + globalData.centralGraph + '>  '
                + ' WHERE { '
                + '     SELECT ?keyword ?k (COUNT(DISTINCT(?subject)) AS ?totalPub) '
                + '     WHERE { '
                + '         ?person foaf:publications ?subject. '
                + '         ?subject bibo:Quote ?k . '
                + '         BIND(IRI(?k) AS ?keyword) . '
                + '     } '
                + '     GROUP BY ?keyword ?k '
                + '     HAVING(?totalPub > 25 && ?totalPub < 200) '
                + '     LIMIT 150'
                + '}';
        sparqlQuery.querySrv({query: queryKeywords}, function (rdf) {
            var context = {
                "rdfs": "http://www.w3.org/2000/01/rdf-schema#",
                "uc": "http://ucuenca.edu.ec/resource/"
            };
            jsonld.compact(rdf, globalData.CONTEXT, function (err, compacted) {
                $scope.$apply(function () {
                    //$scope.data = {schema: {"context": context, fields: ["rdfs:label", "uc:total"]}, data: compacted};
                    searchData.allkeywordsCloud = {schema: {"context": context, fields: ["rdfs:label", "uc:total"]}, data: compacted};
                    waitingDialog.hide();
                });
            });
        });
        //***************************************************//








    }]);
wkhomeControllers.controller('totalPublicationReg', ['$scope', 'globalData', 'sparqlQuery',
    function ($scope, globalData, sparqlQuery) {
        var queryTotalAuthors = globalData.PREFIX
                + ' CONSTRUCT { '
                + ' ?graph a uc:Provider. '
                + ' ?graph uc:name ?name; '
                + '         uc:total ?total }'
                + ' WHERE {  '
                + '     SELECT ?graph (substr(?graph,40) as ?name) ?total'
                + '         WHERE {'
                + '             SELECT DISTINCT ?graph (COUNT(?s) AS ?total)'
                + '             WHERE {'
                + '                 GRAPH ?graph {'
                + '                     ?s owl:sameAs ?same'
                + '                 }'
                + '             }'
                + '             GROUP BY ?graph'
                + '         }'
                + ' }';
        sparqlQuery.querySrv({query: queryTotalAuthors}, function (rdf) {
            var context = {
                "uc": "http://ucuenca.edu.ec/resource/"
            };
            jsonld.compact(rdf, globalData.CONTEXT, function (err, compacted) {
                //$scope.data = compacted;
                var endpoints = compacted['@graph'];
                var data = []
                endpoints.forEach(function (endpoint) {
                    data.push({label: endpoint['uc:name'], value: endpoint['uc:total']['@value']});
                });
                $scope.$apply(function () {
                    $scope.data = {'entityName': 'Articles', 'data': data};
                });
            });
        });
    }]);
wkhomeControllers.controller('totalResearchAreas', ['$scope', 'globalData', 'sparqlQuery', 'searchData', '$window',
    function ($scope, globalData, sparqlQuery, searchData, $window) {
        //if click in pie-chart (Research Areas)
        $scope.ifClick = function (value)
        {
            searchData.researchArea = value;
            $window.location.hash = "cloud/group-by";
        };
        var queryTotalAreas = globalData.PREFIX
                + ' CONSTRUCT { '
                + '     ?uriArea a uc:ResearchArea. '
                + '     ?uriArea uc:name ?keyword; '
                + '                 uc:total ?total }'
                + ' WHERE {  '
                + '     SELECT ?keyword (IRI(REPLACE(?keyword, " ", "_", "i")) as ?uriArea) ?total '
                + '     WHERE { '
                + '         { '
                + '             SELECT DISTINCT ?keyword (COUNT(?keyword) AS ?total) '
                + '             WHERE { '
                + '                 GRAPH <' + globalData.centralGraph + '> { '
                + '                     ?s foaf:publications ?publications. '
                + '                     ?publications bibo:Quote ?keyword. '
                + '                 } '
                + '              } '
                + '              GROUP BY ?keyword '
                + '              ORDER BY DESC(?total) '
                + '              LIMIT 10 '
                + '         } '
                + '     }'
                + ' }';
        sparqlQuery.querySrv({query: queryTotalAreas}, function (rdf) {
            jsonld.compact(rdf, globalData.CONTEXT, function (err, compacted) {
                //$scope.data = compacted;
                var endpoints = compacted['@graph'];
                var data = []
                endpoints.forEach(function (endpoint) {
                    data.push({label: endpoint['uc:name'], value: endpoint['uc:total']['@value']});
                });
                $scope.$apply(function () {
                    $scope.data = {'entityName': 'Articles', 'data': data};
                });
            });
        });
    }]);
wkhomeControllers.controller('groupTagsController', ['$scope', 'globalData', 'sparqlQuery', 'searchData', '$route', '$window',
    function ($scope, globalData, sparqlQuery, searchData, $window) {
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
                        + ' CONSTRUCT { ?keyword rdfs:label ?key } '
                        + '	FROM <' + globalData.centralGraph + '> '
                        + ' WHERE { '
                        + ' SELECT  (count(?key) as ?k) ?key '
                        + ' WHERE { '
                        + '         ?subject bibo:Quote ?key. '
                        + '         BIND(REPLACE(?key, " ", "_", "i") AS ?unickey). '
                        + '         BIND(IRI(?unickey) as ?keyword) '
                        + ' } '
                        + ' group by ?keyword  ?key '
                        + ' HAVING(?k > 10) '
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



        $scope.$watch('gbselectedItem', function () {
            groupByResources($scope.dataaux, $scope.gbselectedItem);
        });
        $scope.$watch('selectedItem', function () {
            //alert($scope.selectedItem);
            loadResources($scope.selectedItem, $scope.gbselectedItem); //query and load resource related with selected theme
        });
        function groupByResources(values, groupby)//grouByResources resources by ...
        {
            // executeDraw(values,groupby);
            //this activity is cheking directly in cloudGroup.js 
        }//end grouByResources

        function loadResources(value, groupby)//load resources related with selected keyword
        {

            var queryRelatedPublications = globalData.PREFIX
                    + ' CONSTRUCT { '
                    + '     ?publicationUri uc:title ?title; '
                    + '     uc:nameauthor ?nameauthor; '
                    + '     uc:namesource ?namesource ; '
                    + '     uc:abstract ?abstract '
                    + ' }'
                    + ' WHERE'
                    + ' {'
                    + '     SELECT ?publicationUri ?title ?nameauthor ?namesource ?abstract '
                    + '         WHERE {   '
                    + '             graph <' + globalData.centralGraph + '> { '
                    + '                     ?subject foaf:publications ?publicationUri .      '
                    + '                     ?subject foaf:name ?nameauthor.           '
                    + '                     ?subject dct:provenance ?source.'
                    + '                     ?publicationUri  dct:title ?title .       '
                    + '                     OPTIONAL { ?publicationUri bibo:abstract  ?abstract. }     '
                    + '                     {         '
                    + '                         ?publicationUri bibo:Quote "' + value + '"^^xsd:string . '
                    + '                     } UNION     '
                    + '                     {             '
                    + '                         ?publicationUri bibo:Quote "' + value + '" .      '
                    + '                     }       '
                    + '                     { '
                    + '                         SELECT * WHERE{ '
                    + '                             GRAPH <' + globalData.endpointsGraph + '>  { '
                    + '                                  ?source  <http://ucuenca.edu.ec/resource/name> ?namesource. '
                    + '                             } '
                    + '                         }'
                    + '                     } '
                    + '             } '
                    + '         }  '
                    + ' } ';
            $scope.publicationsByKeyword = [];
            sparqlQuery.querySrv({query: queryRelatedPublications}, function (rdf) {
                jsonld.compact(rdf, globalData.CONTEXT, function (err, compacted) {
                    _.map(compacted["@graph"], function (pub) {
                        var model = {};
                        //model["Publication"] = pub["foaf:publications"]["@id"];
                        model["Publication"] = pub["id"];
                        model["Title"] = pub["uc:title"];
                        model["Source"] = pub["uc:namesource"];
                        model["Abstract"] = pub["uc:abstract"];
                        model["Author"] = pub["uc:nameauthor"];
                        $scope.$apply(function () {
                            $scope.publicationsByKeyword.push({title: model["Title"], publication: model["Publication"], source: model["Source"], abstract: model["Abstract"], author: model["Author"]});
                        });
                    });
                    executeDraw($scope.publicationsByKeyword, groupby);
                    searchData.areaSearch = null;
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

    }]); //end groupTagsController 


wkhomeControllers.controller('getKeywordsTag', ['$scope', 'globalData', 'sparqlQuery', 'searchData',
    function ($scope, globalData, sparqlQuery, searchData) {
        $('html,body').animate({
            scrollTop: $("#scrollToTop").offset().top
        }, "slow");
        $scope.todos = [];
        $scope.ctrlFn = function (value)
        {
            $scope.todos = [];
            var model = {};
            _.map(value, function (pub) {
                //var keys = Object.keys(author);

                model["id"] = pub["@id"];
                model["title"] = pub["dct:title"];
                model["abstract"] = pub["bibo:abstract"];
                model["uri"] = pub["bibo:uri"]["@id"];
                if (model["title"] && model["abstract"])
                {
                    $scope.todos.push({id: model["id"], title: model["title"], abstract: model["abstract"], uri: model["uri"]});
                }
            });
            $('html,body').animate({
                scrollTop: $("#scrollToHere").offset().top
            }, "slow");
            $scope.loadData();
        };
        $scope.loadData = function () {
            $scope.$apply(function () {
                $scope.filteredTodos = []
                        , $scope.currentPage = 1
                        , $scope.numPerPage = 10
                        , $scope.maxSize = 5;
                $scope.$watch('currentPage + numPerPage', function () {
                    var begin = (($scope.currentPage - 1) * $scope.numPerPage)
                            , end = begin + $scope.numPerPage;
                    $scope.filteredTodos = $scope.todos.slice(begin, end);
                });
            });
        };
        if (!searchData.allkeywordsCloud) // if no load data by default
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
                    + '         ?subject bibo:Quote ?k . '
                    + '         BIND(IRI(?k) AS ?keyword) . '
                    + '     } '
                    + '     GROUP BY ?keyword ?k '
                    + '     HAVING(?totalPub > 25 && ?totalPub < 200) '
                    + ' LIMIT 150'
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
        } // end if if (!searchData.allkeywordsCloud) 


    }]);
wkhomeControllers.controller('exploreAuthor', ['$scope', '$rootScope', 'globalData', 'searchData', '$window', 'sparqlQuery',
    function ($scope, $rootScope, globalData, searchData, $window, sparqlQuery) {

        $rootScope.$on("CallParentMethod", function(author){
           $scope.clickonRelatedauthor (author);
        });
        
        $scope.data = '';
        
        $('html,body').animate({
                scrollTop: $("#scrollToHere").offset().top
        }, "slow");

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

                        $scope.data = compacted;
                    });
                });
            });


        };
        $scope.ifrightClick = function (value)
        {
            searchData.genericData = value;
            $window.location.hash = "w/cloud?" + "datacloud";
        };
        
        if (searchData.authorSearch != null && searchData.authorSearch["@graph"].length == 1) {
            //$scope.data = searchData.authorSearch;
            clickonRelatedauthor(searchData.authorSearch["@graph"][0]["@id"]);
        } 
        
        $scope.$watch('searchData.authorSearch', function (newValue, oldValue, scope) {

            if (searchData.authorSearch) {
                var authorSearch = searchData.authorSearch["@graph"];
                if (authorSearch) {
                        if (authorSearch.length > 1) {
                    var candidates = _.map(authorSearch, function (author) {
                        var model = {};
                        //var keys = Object.keys(author);
                        model["id"] = author["@id"];
                        model["name"] = author["foaf:name"];
                        return model;
                    });
                    $scope.candidates = candidates;
                    
                    /*if (searchData.authorSearch["@graph"].length === 1)
                    {
                        $scope.data = searchData.authorSearch;
                    }*/
                    
                    $scope.selectedAuthor = function ($event, uri) {
                        searchData.authorSearch["@graph"] = _.where(authorSearch, {"@id": uri});
                        //$scope.data = _.where(authorSearch, {"@id": uri});
                        $scope.data = searchData.authorSearch;
                        $('#searchResults').modal('hide');
                    };
                    waitingDialog.hide();
                    $('#searchResults').modal('show');
                    } else {
                        searchData.authorSearch["@graph"] = authorSearch;
                        //$scope.data = searchData.authorSearch;         
                        waitingDialog.hide();
                    }
                }//End if(authorSearch)
                else
                {
                    alert("Information not found");
                    $window.location.hash = "/";
                    waitingDialog.hide();
                }
            }
        }, true);
    }]); // end exploreAuthor



wkhomeControllers.controller('exploreresearchArea', ['$scope', '$rootScope', 'searchData', '$window', '$route',
    function ($scope, $rootScope, searchData, $window) {
        $scope.ifrightClick = function (value)
        {
            searchData.genericData = value;
            $window.location.hash = "w/cloud?" + "datacloud";
        };
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
                        $scope.$destroy();
                        $scope.$apply();
                        searchData.researchArea = label;
                        $window.location.hash = "cloud/group-by";
                    };
                    waitingDialog.hide();
                    $('#searchResults').modal('show');
//                        } else {
//                        searchData.authorSearch["@graph"] = authorSearch;
//                        $scope.data = searchData.authorSearch;         
//                        waitingDialog.hide();
//                    }
                }//End if(authorSearch)
                else
                {
                    alert("Information not found");
                    $window.location.hash = "/";
                    waitingDialog.hide();
                }
            }

        }, true);
    }]); // end exploreresearchArea

wkhomeControllers.controller('SearchController', ['$scope', '$window', 'globalData', 'sparqlQuery', 'searchData',
    function ($scope, $window, globalData, sparqlQuery, searchData) {
        //$scope.sparqlQuery = sparqlQuery;
        String.format = function () {
            // The string containing the format items (e.g. "{0}")
            // will and always has to be the first argument.
            var theString = arguments[0];
            // start with the second argument (i = 1)
            for (var i = 1; i < arguments.length; i++) {
                // "gm" = RegEx options for Global search (more than one instance)
                // and for Multiline search
                var regEx = new RegExp("\\{" + (i - 1) + "\\}", "gm");
                theString = theString.replace(regEx, arguments[i]);
            }
            return theString;
        };
        $scope.submit = function () {
            if ($scope.searchText) {
                console.log($scope.searchText);
                waitingDialog.show();
                var queryAuthors = globalData.PREFIX
                        + " CONSTRUCT { "
                        + " ?subject a foaf:Person. "
                        + " ?subject foaf:name ?name } "
                        + " WHERE { "
                        + " { "
                        + "     SELECT DISTINCT (sample(?s) AS ?subject) ?name "
                        + "     WHERE { "
                        + '         GRAPH <' + globalData.centralGraph + '> {'
                        + "             ?s a foaf:Person. "
                        + "             ?s foaf:name ?name."
                        + "             ?s foaf:publications ?pub. "
                        + '             FILTER(mm:fulltext-search(str(?name), "' + $scope.searchText + '")).'
                        + "     } } "
                        + "     GROUP BY ?name "
                        + "  } "
                        + " }";
//                var filterPath = 'FILTER(CONTAINS(UCASE(?name), "{0}" )) . ';
//                var searchTextt = $scope.searchText.trim();
//                var keywords = searchTextt.split(" ");
//                var filterContainer = "";
//                keywords.forEach(function (val) {
//                    if (val.length > 0) {
//                        filterContainer += String.format(filterPath, val.toUpperCase());
//                    }
//                });
//                queryAuthors = String.format(queryAuthors, filterContainer);
                sparqlQuery.querySrv({query: queryAuthors},
                function (rdf) {
                    var context = {
                        "foaf": "http://xmlns.com/foaf/0.1/"/*,
                         "foaf:name": {"@id": "http://xmlns.com/foaf/0.1/name"},
                         "foaf:Person": {"@id": "http://xmlns.com/foaf/0.1/Person"}*/
                    };
                    jsonld.compact(rdf, globalData.CONTEXT, function (err, compacted) {
                        if (compacted["@graph"])
                        {
                            searchData.authorSearch = compacted;
                            $window.location.hash = "w/search?" + $scope.searchText;
                        }
                        else
                        {
                            waitingDialog.show();
                            var queryAuthors = globalData.PREFIX
                                    + " CONSTRUCT { ?keywordduri rdfs:label ?k } "
                                    + " WHERE { "
                                    + " { "
                                    + "     SELECT DISTINCT (sample(?keyword) AS ?keywordduri) ?k "
                                    + "     WHERE { "
                                    + '         GRAPH <' + globalData.centralGraph + '> {'
                                    + "         ?s foaf:publications ?pub. "
                                    + "         ?pub bibo:Quote ?k."
                                    + "         BIND(IRI(?k) AS ?keyword) . "
                                    // + "         {0}"
                                    + '         FILTER(mm:fulltext-search(str(?k), "' + $scope.searchText + '")).'
                                    + "     } } "
                                    + "     GROUP BY ?k "
                                    + "  } "
                                    + " }";
//                            var filterPath = 'FILTER(CONTAINS(UCASE(?k), "{0}" )) . ';
//                            var searchTextt = $scope.searchText.trim();
//                            var keywords = searchTextt.split(" ");
//                            var filterContainer = "";
//                            keywords.forEach(function (val) {
//                                if (val.length > 0) {
//                                    filterContainer += String.format(filterPath, val.toUpperCase());
//                                }
//                            });
                            //queryAuthors = String.format(queryAuthors, filterContainer);
                            sparqlQuery.querySrv({query: queryAuthors},
                            function (rdf) {
                                jsonld.compact(rdf, globalData.CONTEXT, function (err, compacted) {
                                    if (compacted["@graph"])
                                    {
                                        searchData.areaSearch = compacted;
                                        waitingDialog.hide();
                                        //   $window.location.hash = "w/research-area?" + $scope.searchText;
                                        $window.location.hash = "cloud/group-by";
                                    }
                                    else
                                    {
                                        alert("Information not found");
                                        waitingDialog.hide();
                                    }
                                });
                            }); // end of  sparqlQuery.querySrv({...
                        }
                    });
                }); // end of  sparqlQuery.querySrv({...
            }
        };
    }]);
wkhomeControllers.controller('ExploreController', ['$scope', '$window',
    function ($scope, $window) {
        console.log($scope.text);
    }]);
wkhomeControllers.controller('genericcloudController', ['$scope', '$window', 'globalData', 'sparqlQuery', 'searchData',
    function ($scope, $window, globalData, sparqlQuery, searchData) {
        $scope.todos = [];
        $scope.ctrlFn = function (value)
        {
            $scope.todos = [];
            var model = {};
            _.map(value, function (pub) {
                //var keys = Object.keys(author);
                model["id"] = pub["@id"];
                model["title"] = pub["dct:title"];
                model["abstract"] = pub["bibo:abstract"];
                if (pub["bibo:uri"]) {
                    model["uri"] = pub["bibo:uri"]["@id"];
                }
                if (model["title"] && model["abstract"])
                {
                    $scope.todos.push({id: model["id"], title: model["title"], abstract: model["abstract"], uri: model["uri"]});
                }
            });
            $('html,body').animate({
                scrollTop: $("#scrollToHere").offset().top
            }, "slow");
            $scope.loadData();
        };
        $scope.loadData = function () {
            $scope.$apply(function () {
                $scope.filteredTodos = []
                        , $scope.currentPage = 1
                        , $scope.numPerPage = 10
                        , $scope.maxSize = 5;
                $scope.$watch('currentPage + numPerPage', function () {
                    var begin = (($scope.currentPage - 1) * $scope.numPerPage)
                            , end = begin + $scope.numPerPage;
                    $scope.filteredTodos = $scope.todos.slice(begin, end);
                });
            });
        };
        $scope.$watch('searchData.genericData', function (newValue, oldValue, scope) {
            $scope.data = {schema: {"context": globalData.CONTEXT, fields: ["rdfs:label", "uc:total"]}, data: searchData.genericData};
        });
    }]); //end genericcloudController 

wkhomeControllers.controller('resourcesMap', ['$scope', '$window', 'globalData', 'sparqlQuery', 'searchData',
    function ($scope, $window, globalData, sparqlQuery, searchData) {

        //if click in pie-chart
        $scope.ifClick = function (value)
        {
            searchData.genericData = value;
            $window.location.hash = "w/cloud?" + "datacloud";
        };
        $scope.themes = [];
        if (!searchData.allkeywords)
        {
            waitingDialog.show("Loading Research Areas");
            var queryKeywords = globalData.PREFIX
                    + ' CONSTRUCT { ?keyword rdfs:label ?key } '
                    + '	FROM <' + globalData.centralGraph + '> '
                    + ' WHERE { '
                    + '     SELECT  (count(?key) as ?k) ?key '
                    + '     WHERE { '
                    + '         ?subject bibo:Quote ?key. '
                    + '         BIND(REPLACE(?key, " ", "_", "i") AS ?unickey). '
                    + '         BIND(IRI(?unickey) as ?keyword) '
                    + '     } '
                    + '     GROUP BY ?keyword  ?key '
                    + '     HAVING(?k > 10) '
                    + '}';
            sparqlQuery.querySrv({query: queryKeywords}, function (rdf) {
               jsonld.compact(rdf, globalData.CONTEXT, function (err, compacted) {
                    _.map(compacted["@graph"], function (pub) {
                        var model = {};
                        model["id"] = pub["@id"];
                        model["tag"] = pub["rdfs:label"];
                        $scope.themes.push({tag: model["tag"]});
                    });
                    $scope.$apply(function () {
                        $scope.relatedtags = $scope.themes;
                        $scope.selectedTagItem = 'Semantic Web';
                        searchData.allkeywords = $scope.themes;
                    });
                    waitingDialog.hide();
                });
            });
        }
        else
        {
            $scope.relatedtags = searchData.allkeywords;
            $scope.selectedTagItem = 'Semantic Web';
        }



        //default selectedTagItem =  Semantic Web  - > see in app.js
        $scope.$watch('selectedTagItem', function () {
            //alert($scope.selectedItem);
            var queryBySource = globalData.PREFIX
                    + ' CONSTRUCT { '
                    + '         ?urikeyword bibo:Quote "' + $scope.selectedTagItem + '". '
                    + '         ?urikeyword uc:totalpublications ?cont. '
                    + '         ?urikeyword uc:name ?sourcename.  '
                    + '         ?urikeyword uc:lat ?lat. '
                    + '         ?urikeyword uc:long ?long. '
                    + '         ?urikeyword uc:province ?province. '
                    + '         ?urikeyword uc:city ?city. '
                    + '         ?urikeyword uc:fullname ?fullname. '
                    + ' } '
                    + 'WHERE {'
                    + '     SELECT (count(?object) as ?cont) ?provenance  ?urikeyword ?provenance ?sourcename ?lat ?long ?province ?city ?fullname '
                    + '     WHERE {'
                    + '         GRAPH <' + globalData.centralGraph + '>  {'
                    + '             ?subject foaf:publications ?object.'
                    + '             ?object bibo:Quote "' + $scope.selectedTagItem + '".'
                    + '             ?subject dct:provenance ?provenance.'
                    + '             { '
                    + '                 SELECT DISTINCT ?sourcename ?lat ?long ?province ?city ?fullname '
                    + '                 WHERE { '
                    + '                     GRAPH <' + globalData.endpointsGraph + '>  { '
                    + '                         ?provenance uc:name ?sourcename. '
                    + '                         ?provenance  uc:latitude ?lat. '
                    + '                         ?provenance uc:longitude ?long. '
                    + '                         ?provenance uc:province ?province. '
                    + '                         ?provenance uc:city ?city. '
                    + '                         ?provenance uc:fullName ?fullname.'
                    + '                     } '
                    + '                 } '
                    + '             } '
                    + '             BIND(REPLACE("' + $scope.selectedTagItem + '"," ","_","i") + "_" + ?sourcename  as ?iduri). '
                    + '             BIND(IRI(?iduri) as ?urikeyword) '
                    + '         } '
                    + '     } '
                    + '     GROUP BY ?sourcename ?provenance ?lat ?long ?province ?city ?fullname  ?urikeyword '
                    + ' } ';
            $scope.publicationsBySource = [];
            sparqlQuery.querySrv({query: queryBySource},
            function (rdf) {
                var context = {
                    "bibo": "http://purl.org/ontology/bibo/",
                    "uc": "http://ucuenca.edu.ec/resource/"
                };
                jsonld.compact(rdf, globalData.CONTEXT, function (err, compacted) {
                    var sd;
                    var model = [];
                    _.map(compacted["@graph"], function (resource) {
                        //var keys = Object.keys(author);
                        var model = {};
                        model["id"] = resource["@id"];
                        model["name"] = resource["uc:name"];
                        model["fullname"] = resource["uc:fullname"];
                        model["total"] = resource["uc:totalpublications"]["@value"];
                        model["lat"] = resource["uc:lat"];
                        model["long"] = resource["uc:long"];
                        model["keyword"] = resource["bibo:Quote"];
                        model["city"] = resource["uc:city"];
                        model["province"] = resource["uc:province"];
                        if (model["id"])
                        {
                            $scope.publicationsBySource.push({id: model["id"], name: model["name"], fullname: model["fullname"], total: model["total"], latitude: model["lat"]
                                , longitude: model["long"], city: model["city"], province: model["province"], keyword: model["keyword"]});
                        }
                    });
                    $scope.$apply(function () {
                        $scope.data = $scope.publicationsBySource;
                    });
                });
            });
        });
    }]);
wkhomeControllers.controller('SnapController', ['$scope', '$window',
    function ($scope, $window) {
        var index = parseInt($window.location.hash.slice(2), 10);
        $scope.snapAnimation = false; // turn animation off for the initial snap on page load
        if (index && angular.isNumber(index)) {
            $scope.snapIndex = index;
        } else {
            $scope.snapIndex = 0;
        }
        $scope.afterSnap = function (snapIndex) {
            $scope.snapAnimation = true; // turn animations on after the initial snap
            $window.location.hash = '#/' + snapIndex;
            console.log("afterCallback");
        };
        $scope.beforeCallback = function (snapIndex) {
            console.log("beforeCallback");
        };
        $scope.$on('arrow-up', function () {
            $scope.$apply(function () {
                $scope.snapIndex--;
            });
        });
        $scope.$on('arrow-down', function () {
            $scope.$apply(function () {
                $scope.snapIndex++;
            });
        });
        $scope.swipeUp = function () {
            $scope.snapIndex++;
        };
        $scope.swipeDown = function () {
            $scope.snapIndex--;
        };
        $scope.afterSnap = function (snapIndex) {
            $scope.snapAnimation = true; // turn animations on after the initial snap
            $window.location.hash = '#/' + snapIndex;
            console.log("afterCallback");
        };
    }]);
wkhomeControllers.controller('clusterTagsController', ['$scope', 'globalData', 'sparqlQuery', 'clustersQuery', 'searchData', '$route', '$window',
    function ($scope, globalData, sparqlQuery, clustersQuery, searchData, $window) {

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
                        + ' CONSTRUCT { ?keyword rdfs:label ?key } '
                        + '	FROM <' + globalData.centralGraph + '> '
                        + ' WHERE { '
                        + '     SELECT  (count(?key) as ?k) ?key '
                        + '     WHERE { '
                        + '         ?subject bibo:Quote ?key. '
                        + '         BIND(REPLACE(?key, " ", "_", "i") AS ?unickey). '
                        + '         BIND(IRI(?unickey) as ?keyword) '
                        + '     } '
                        + '     GROUP BY ?keyword  ?key '
                        + '     HAVING(?k > 10) '
                        + '}';
                sparqlQuery.querySrv({query: queryKeywords}, function (rdf) {
                    waitingDialog.show();
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



        $scope.$watch('gbselectedItem', function () {
            groupByResources($scope.dataaux, $scope.gbselectedItem);
        });
        $scope.$watch('selectedItem', function () {
            //alert($scope.selectedItem);
            loadResources($scope.selectedItem, $scope.gbselectedItem); //query and load resource related with selected theme
        });
        function groupByResources(values, groupby)//grouByResources resources by ...
        {
            // executeDraw(values,groupby);
            //this activity is cheking directly in cloudGroup.js 
        }//end grouByResources

        function loadResources(value, groupby)//load resources related with selected keyword
        {
            $scope.publicationsByKeyword = [];
            clustersQuery.success(function (data) {
                $scope.clusters = data;
                var myArray = new Array();
                for (i = 0, len = data.length; i < len; i++) {
                    myArray[data[i].cluster.toString()] = myArray[data[i].cluster.toString()] == null ? 1 : myArray[data[i].cluster.toString()] + 1;
                }
                for (i = 0, len = data.length; i < len; i++) {
                    var numCluster = Number(data[i].cluster.toString().trim());
                    if (numCluster < 500 && myArray[data[i].cluster.toString()] > 4) {
                        var model = {};
                        model["Cluster"] = data[i].cluster;
                        model["Author"] = data[i].author;
                        model["Keyword"] = data[i].kw;
                        model["Title"] = data[i].title.toString();
                        model["URI"] = data[i].uri;
                        $scope.$apply(function () {
                            $scope.publicationsByKeyword.push({cluster: model["Cluster"], author: model["Author"], keyword: model["Keyword"], title: model["Title"], uri: model["URI"]});
                        });
                    }
                }

                executeDraw($scope.publicationsByKeyword, groupby);
                searchData.areaSearch = null;

            });
            
        }//end Load Resources

        function executeDraw(dataToDraw, groupby)
        {
            $scope.$apply(function () {
                $scope.data = [{value: dataToDraw, group: groupby}];
                $scope.dataaux = dataToDraw;
            });
        }


    }]); //end clusterTagsController 

wkhomeControllers.controller('kwCloudClusterController', ['$scope', '$window', 'sparqlQuery', 'searchData', 'globalData',
    function ($scope, $window, sparqlQuery, searchData, globalData) {

        $scope.ifClick = function (value)
        {
            searchData.genericData = value;
            $window.location.hash = "w/clusters?" + "datacloud";
        };
        
        $scope.todos = [];
        
        $scope.loadData = function () {
            $scope.$apply(function () {
                $scope.filteredTodos = []
                        , $scope.currentPage = 1
                        , $scope.numPerPage = 10
                        , $scope.maxSize = 5;
                $scope.$watch('currentPage + numPerPage', function () {
                    var begin = (($scope.currentPage - 1) * $scope.numPerPage)
                            , end = begin + $scope.numPerPage;
                    $scope.filteredTodos = $scope.todos.slice(begin, end);
                });
            });
        };
        
        
        
        if (!searchData.allkeywordsCloud) // if no load data by default
        {
            waitingDialog.show();
            var queryKeywords = globalData.PREFIX
                    + ' CONSTRUCT { ?keyword rdfs:label ?k; uc:total ?totalPub } FROM <' + globalData.centralGraph + '> WHERE { '
                    + ' SELECT ?keyword ?k (COUNT(DISTINCT(?subject)) AS ?totalPub) '
                    + ' WHERE { '
                    + ' ?person foaf:publications ?subject. '
                    + ' ?subject bibo:Quote ?k . '
                    + ' BIND(IRI(?k) AS ?keyword) . } '
                    + ' GROUP BY ?keyword ?k '
                    + ' HAVING(?totalPub > 25 && ?totalPub < 200) '
                    + ' LIMIT 150'
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
        } // end if if (!searchData.allkeywordsCloud)     
    }]);

wkhomeControllers.controller('clustersWithKeywordCloudController', ['$scope', '$window', 'sparqlQuery', 'searchData', 'globalData',
    function ($scope, $window, sparqlQuery, searchData, globalData) {
        $scope.todos = [];
        $scope.ctrlFn = function (value)
        {
            $scope.todos = [];
            var model = {};
            _.map(value, function (obj) {
                //var keys = Object.keys(author);
                model["id"] = obj["@id"];
                model["name"] = obj["foaf:name"];
                model["keywords"] = obj["bibo:Quote"];
                /*if (obj["bibo:uri"]) {
                    model["uri"] = obj["bibo:uri"]["@id"];
                }*/
                if (model["name"] && model["keywords"])
                {
                    $scope.todos.push({id: model["id"], name: model["name"], keywords: model["keywords"], uri: model["id"]});
                }
            });
            $('html,body').animate({
                scrollTop: $("#scrollToHere").offset().top
            }, "slow");
            $scope.loadData();
        };
        $scope.loadData = function () {
            $scope.$apply(function () {
                $scope.filteredTodos = []
                        , $scope.currentPage = 1
                        , $scope.numPerPage = 10
                        , $scope.maxSize = 5;
                $scope.$watch('currentPage + numPerPage', function () {
                    var begin = (($scope.currentPage - 1) * $scope.numPerPage)
                            , end = begin + $scope.numPerPage;
                    $scope.filteredTodos = $scope.todos.slice(begin, end);
                });
            });
        };
        $scope.$watch('searchData.genericData', function (newValue, oldValue, scope) {
            $scope.data = {schema: {"context": globalData.CONTEXT, fields: ["rdfs:label", "uc:total"]}, data: searchData.genericData};
        });
        
        $scope.searchAuthor = function (author)
        {
             var getAuthorDataQuery = globalData.PREFIX
                    + ' CONSTRUCT {   <' + author + '> foaf:name ?name; a foaf:Person  '
                    + ' }   '
                    + ' WHERE '
                    + ' {'
                    + 'Graph <'+globalData.centralGraph+'>'
                    +'{'
                    + '     <' + author + '> a foaf:Person.'
                    + '     <' + author + '> foaf:name ?name'
                    
                    + ' } '
                    +'}';

            sparqlQuery.querySrv({query: getAuthorDataQuery}, function (rdf) {
                jsonld.compact(rdf, globalData.CONTEXT, function (err, compacted) {
                    $scope.$apply(function () {
                        searchData.authorSearch = compacted;
                        //alert(author);
                        $window.location.hash = "w/search?" + author;
                        
                    });
                });
            });
            
                    
        };
    }]); //end clusterscloudController
