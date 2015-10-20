//'use strict';

/* Controllers */

var wkhomeControllers = angular.module('wkhomeControllers', ['pieChart', 'explorableTree', 'cloudTag', 'snapscroll', 'ui.bootstrap.pagination']);
wkhomeControllers.controller('indexInformation', ['$scope', '$window', 'Phone',
    function ($scope, $window, Phone) {
        $scope.welcome = "Hello World!";
        $scope.user = {};
        /*$scope.phones = Phone.query();
         $scope.orderProp = 'age';*/

    }]);
wkhomeControllers.controller('showSection', ['$scope', '$routeParams', '$window', 'Phone',
    function ($scope, $routeParams, $window, Phone) {
        $scope.welcome = "Hello " + ($routeParams.section == 0 ? "World" : "Miami") + "!";
        $scope.user = {};
        /*$scope.phones = Phone.query();
         $scope.orderProp = 'age';*/

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
wkhomeControllers.controller('totalPersonReg', ['$scope', 'sparqlQuery',
    function ($scope, sparqlQuery) {
        //if(window.location.hash == '#/0') {
        var queryTotalAuthors = 'PREFIX dct: <http://purl.org/dc/terms/> PREFIX foaf: <http://xmlns.com/foaf/0.1/> '
                + 'PREFIX uc: <http://ucuenca.edu.ec/wkhuska/resource/> '
                + 'CONSTRUCT { ?provenance a uc:Endpoint . ?provenance uc:name ?name . ?provenance uc:total ?total } '
                + 'WHERE { SELECT ?provenance ?name (COUNT(DISTINCT(?s)) AS ?total) WHERE { '
                + '?s a foaf:Person . ?s dct:provenance ?provenance . ?provenance uc:name ?name . }GROUP BY ?provenance ?name '
                + '}';
        sparqlQuery.querySrv({query: queryTotalAuthors}, function (rdf) {
            var context = {
                "uc": "http://ucuenca.edu.ec/wkhuska/resource/"
            };
            jsonld.compact(rdf, context, function (err, compacted) {
                //$scope.data = compacted;
                var endpoints = compacted['@graph'];
                var data = []
                endpoints.forEach(function (endpoint) {
                    data.push({label: endpoint['uc:name'], value: endpoint['uc:total']['@value']});
                });
                $scope.data = {'entityName': 'authors', 'data': data};
                console.log(compacted);
            });
            //$rootScope.$emit('authorSearch', rdf);

            /*$scope.data = [*//*{label: "Lorem ipsum", value: 1, color: "#98abc5"}, 
             {label: "dolor sit", value: 1, color: "#8a89a6"},
             {label: "amet", value: 2, color: "#7b6888"},
             {label: "consectetur", value: 3, color: "#6b486b"},*/

            /*{label: "adipisicing", value: 5, color: "#a05d56"},
             {label: "elit", value: 8, color:"#d0743c"},
             {label: "sed", value: 13, color: "#ff8c00"},
             {label: "do", value: 21},
             {label: "eiusmod", value: 34},
             {label: "tempor", value: 55},
             {label: "incididunt", value: 89}];*/

            /*$scope.data = [1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89];*/
            /*$scope.data = [{label: "Lorem ipsum", value: 1}, 
             {label: "dolor sit", value: 1},
             {label: "amet", value: 2},
             {label: "consectetur", value: 3},
             {label: "adipisicing", value: 5},
             {label: "elit", value: 8},
             {label: "sed", value: 13},
             {label: "do", value: 21},
             {label: "eiusmod", value: 34},
             {label: "tempor", value: 55},
             {label: "incididunt", value: 89}];*/
            /*$scope.data.domain = ["Lorem ipsum", "dolor sit", "amet", "consectetur", "adipisicing", 
             "elit", "sed", "do", "eiusmod", "tempor", "incididunt"];
             $scope.data.range = [, , , , , , ];*/
        });
        //}
    }]);
wkhomeControllers.controller('totalPublicationReg', ['$scope', 'sparqlQuery',
    function ($scope, sparqlQuery) {
        //if(window.location.hash == '#/0') {
        var queryTotalAuthors = ''
                + ' PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>'
                + ' PREFIX uc: <http://ucuenca.edu.ec/wkhuska/resource/> '
                + ' CONSTRUCT { ?graph a uc:Provider. ?graph uc:name ?name; uc:total ?total }'
                + ' WHERE {  '
                + '     SELECT ?graph (substr(?graph,40) as ?name) ?total'
                + '     WHERE {'
                + '         SELECT DISTINCT ?graph (COUNT(?s) AS ?total)'
                + '         WHERE {'
                + '             GRAPH ?graph {'
                + '                 ?s owl:sameAs ?same'
                + '             }'
                + '         }'
                + '         GROUP BY ?graph'
                + '     }'
                + ' }';
        sparqlQuery.querySrv({query: queryTotalAuthors}, function (rdf) {
            var context = {
                "uc": "http://ucuenca.edu.ec/wkhuska/resource/"
            };
            jsonld.compact(rdf, context, function (err, compacted) {
                //$scope.data = compacted;
                var endpoints = compacted['@graph'];
                var data = []
                endpoints.forEach(function (endpoint) {
                    data.push({label: endpoint['uc:name'], value: endpoint['uc:total']['@value']});
                });
                $scope.data = {'entityName': 'articles', 'data': data};
            });
        });
    }]);
wkhomeControllers.controller('groupTagsController', ['$scope', 'sparqlQuery',
    function ($scope, sparqlQuery) {
        $scope.themes = [];
        $scope.relatedthemes = [];
        waitingDialog.show();
        var queryKeywords = 'PREFIX bibo: <http://purl.org/ontology/bibo/> '
                + 'CONSTRUCT { ?keyword rdfs:label ?k } '
                + '	FROM <http://ucuenca.edu.ec/wkhuska> '
                + '	WHERE { '
                + '		SELECT DISTINCT ?keyword ?k '
                + '             WHERE { ?subject bibo:Quote ?k . '
                + '                     BIND(REPLACE( ?k, " ", "_", "i") AS ?key) . '
                + '                     BIND(IRI(?key) as ?keyword)'
                + '                   } '
                + '             ORDER BY ?k'
                + '     } ';
        sparqlQuery.querySrv({query: queryKeywords}, function (rdf) {
            var context = {
                "rdfs": "http://www.w3.org/2000/01/rdf-schema#",
            };
            jsonld.compact(rdf, context, function (err, compacted) {
                _.map(compacted["@graph"], function (pub) {
                    var model = {};
                    model["id"] = pub["@id"];
                    model["tag"] = pub["rdfs:label"];
                    $scope.themes.push({tag: model["tag"]});
                });
            });
            waitingDialog.hide();
        });
        $scope.putSelectedItem = function (value) {
            loadResources(value); //query and load resource related with selected theme


            //no hace nada por ahora
            $scope.relatedthemes = [];
            waitingDialog.show();
            var queryRelatedKeywords = 'PREFIX bibo: <http://purl.org/ontology/bibo/> '
                    + 'CONSTRUCT { ?keyword rdfs:label ?k } '
                    + '	FROM <http://ucuenca.edu.ec/wkhuska> '
                    + '	WHERE { '
                    + '		SELECT DISTINCT ?keyword ?k '
                    + '             WHERE { ?subject bibo:Quote ?k . '
                    + '                     ?subject bibo:Quote "' + value + '"^^xsd:string . '
                    + '                     BIND(REPLACE( ?k , " ", "_", "i") AS ?key) . '
                    + '                     BIND(IRI(?key) as ?keyword)'
                    + '                   } '
                    + '             ORDER BY ?k'
                    + '     } ';
            sparqlQuery.querySrv({query: queryRelatedKeywords}, function (rdf) {
                var context = {
                    "rdfs": "http://www.w3.org/2000/01/rdf-schema#",
                };
                jsonld.compact(rdf, context, function (err, compacted) {
                    _.map(compacted["@graph"], function (pub) {
                        var model = {};
                        model["id"] = pub["@id"];
                        model["tag"] = pub["rdfs:label"];
                        $scope.$apply(function () {
                            $scope.relatedthemes.push({tag: model["tag"]});

                        });
                    });
                });
            });
            waitingDialog.hide();
        };//end PutSelectedItem



        function loadResources(value)//load resources related with selected keyword
        {
            var queryRelatedPublications = 'PREFIX dct: <http://purl.org/dc/terms/> '
                    + ' PREFIX foaf: <http://xmlns.com/foaf/0.1/> '
                    + '     PREFIX bibo: <http://purl.org/ontology/bibo/> '
                    + '     PREFIX uc: <http://ucuenca.edu.ec/wkhuska/resource/> '
                    + ' CONSTRUCT { ?resource bibo:Quote ?k. ?resource dct:title ?title'
                    + '              ; bibo:abstract ?abstract; foaf:publications ?publicationUri'
                    + '         } '
                    + ' FROM <http://ucuenca.edu.ec/wkhuska>'
                    + '   	WHERE {'
                    + '         {'
                    + '             SELECT DISTINCT   ?k ?title ?abstract ?resource ?publicationUri'
                    + '             WHERE { '
                    + '                 ?publicationUri bibo:Quote ?k .'
                    + '                 ?publicationUri  dct:title ?title .'
                    + '                 ?publicationUri bibo:abstract  ?abstract. '
                    + '                 { '
                    + '                     ?publicationUri bibo:Quote "' + value + '"^^xsd:string . '
                    + '                 } UNION '
                    + '                 { '
                    + '                     ?publicationUri bibo:Quote "' + value + '" . '
                    + '                 } '
                    + '                 BIND(REPLACE( ?k , " ", "_", "i") AS ?key) . '
                    + '                 BIND (IRI(CONCAT(?publicationUri,?key)) as ?resource )  '
                    + '            } '
                    + '         }  '
                    + '         }';

            $scope.publicationsByKeyword = [];
            sparqlQuery.querySrv({query: queryRelatedPublications}, function (rdf) {
                var context = {
                    "rdfs": "http://www.w3.org/2000/01/rdf-schema#",
                    "dcterms": "http://purl.org/dc/terms/",
                    "foaf": "http://xmlns.com/foaf/0.1/",
                    "bibo": "http://purl.org/ontology/bibo/"
                };

                jsonld.compact(rdf, context, function (err, compacted) {
                    _.map(compacted["@graph"], function (pub) {
                        var model = {};
                        model["Publication"] = pub["foaf:publications"]["@id"];
                        model["Title"] = pub["dcterms:title"];
                        model["Keyword"] = pub["bibo:Quote"];
                        model["Abstract"] = pub["bibo:abstract"];
                        $scope.$apply(function () {
                            $scope.publicationsByKeyword.push({title: model["Title"], publication: model["Publication"], keyword: model["Keyword"], abstract: model["Abstract"]});
                        });
                    });
                    //$scope.data =  $scope.publicationsByKeyword;
                    startCloud($scope.publicationsByKeyword);
                });  //end jsonld.compact

            }); //end sparqlService

        }//end Load Resources


//        $scope.putSelectedItem = function (value) {
//            drawByGroup(value);
//        };

    }]); //end groupTagsController 


wkhomeControllers.controller('getKeywordsTag', ['$scope', 'sparqlQuery',
    function ($scope, sparqlQuery) {
        $scope.todos = [];
        $scope.ctrlFn = function (value)
        {
            $scope.todos = [];
            var model = {};
            _.map(value, function (pub) {
                //var keys = Object.keys(author);
               
                    model["id"] = pub["@id"];
                    model["title"] = pub["dcterms:title"];
                    model["abstract"] = pub["bibo:abstract"];
                    model["uri"] = pub["bibo:uri"]["@id"];
//                if (model["abstract"].length > 0 && model["title"].length > 0 )
//                {
                    if (model["title"])
                    {
                        $scope.todos.push({id: model["id"], title: model["title"], abstract: model["abstract"], uri: model["uri"]});
                    }
                
                //  }
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
        $scope.filteredTodos = []
                , $scope.currentPage = 1
                , $scope.numPerPage = 10
                , $scope.maxSize = 5;
//        $scope.$watch('currentPage + numPerPage', function () {
//            var begin = (($scope.currentPage - 1) * $scope.numPerPage)
//                    , end = begin + $scope.numPerPage;
//            $scope.filteredTodos = $scope.todos.slice(begin, end);
//        });
        waitingDialog.show();
        //if(window.location.hash == '#/1') {
        var queryKeywords = 'PREFIX bibo: <http://purl.org/ontology/bibo/> '
                + 'PREFIX foaf: <http://xmlns.com/foaf/0.1/> '
                + 'PREFIX uc: <http://ucuenca.edu.ec/wkhuska/resource/> '
                + 'CONSTRUCT { ?keyword rdfs:label ?k; uc:total ?totalPub } FROM <http://ucuenca.edu.ec/wkhuska> WHERE { '
                + 'SELECT ?keyword ?k (COUNT(DISTINCT(?subject)) AS ?totalPub) WHERE { ?subject bibo:Quote ?k . '
                + 'BIND(IRI(?k) AS ?keyword) . } '
                + 'GROUP BY ?keyword ?k '
                + 'HAVING(?totalPub > 100) '
                //+'ORDER BY DESC(?totalPub) '
                + '}';
        sparqlQuery.querySrv({query: queryKeywords}, function (rdf) {
            var context = {
                "rdfs": "http://www.w3.org/2000/01/rdf-schema#",
                "uc": "http://ucuenca.edu.ec/wkhuska/resource/"
            };
            jsonld.compact(rdf, context, function (err, compacted) {
                $scope.data = {schema: {"context": context, fields: ["rdfs:label", "uc:total"]}, data: compacted};
                waitingDialog.hide();
            });
        });
        //}
    }]);
wkhomeControllers.controller('exploreAuthor', ['$scope', '$rootScope', 'searchData',
    function ($scope, $rootScope, searchData) {
        /*$rootScope.$on('authorSearch', function(event, model) {
         $scope.data = model;
         
         });*/
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
                        $scope.selectedAuthor = function ($event, uri) {
                            searchData.authorSearch["@graph"] = _.where(authorSearch, {"@id": uri});
                            //$scope.data = _.where(authorSearch, {"@id": uri});
                            $scope.data = searchData.authorSearch;
                            $('#searchResults').modal('hide');
                        }
                        waitingDialog.hide();
                        $('#searchResults').modal('show');
                    } else {
                        //$scope.data = authorSearch[0];
                        $scope.data = searchData.authorSearch;
                        waitingDialog.hide();
                    }
                }//End if(authorSearch)
                else
                {
                    alert("Author not found");
                    waitingDialog.hide();
                }
            }
        }, true);
        /*$scope.data = {"external_urls":{"spotify":"https://open.spotify.com/artist/43ZHCT0cAZBISjO8DG9PnE"},
         "followers":{"href":null,"total":833907},
         "genres":["rock-and-roll","rockabilly"],"href":"https://api.spotify.com/v1/artists/43ZHCT0cAZBISjO8DG9PnE",
         "id":"43ZHCT0cAZBISjO8DG9PnE",
         "images":[{"height":1296,"url":"https://i.scdn.co/image/c7b8708eab6d0f0902908c1b9f9ba1daaeed06af","width":1000},
         {"height":829,"url":"https://i.scdn.co/image/e25cb372ca9a5317c17d5f62b3556f76ce2edde8","width":640},
         {"height":259,"url":"https://i.scdn.co/image/7d0e683d6bb4cbb384586cd6d9007f5a40928251","width":200},
         {"height":83,"url":"https://i.scdn.co/image/16045a251c9e9f5772d4aeb3f6fa23fe4fdeb54a","width":64}],
         "name":"Elvis Presley",
         "popularity":82,
         "type":"artist",
         "uri":"spotify:artist:43ZHCT0cAZBISjO8DG9PnE"};*/
    }]);
wkhomeControllers.controller('SearchController', ['$scope', '$rootScope', '$window', 'sparqlQuery', 'searchData',
    function ($scope, $rootScope, $window, sparqlQuery, searchData) {
        $scope.sparqlQuery = sparqlQuery;
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
        }
        /*$.post( "/sparql/select", { query: 'construct {?s <http://xmlns.com/foaf/0.1/name> ?name} where {?s a <http://xmlns.com/foaf/0.1/Person>. ?s <http://xmlns.com/foaf/0.1/name> ?name} limit 10' }, 
         function( data ) {
         console.log( data ); // John
         }, "application/ld+json")
         .fail(function(e) {
         console.log(e.responseText);
         });*/
        $scope.submit = function () {
            if ($scope.searchText) {
                console.log($scope.searchText);
                waitingDialog.show();
                var queryAuthors = "PREFIX foaf: <http://xmlns.com/foaf/0.1/> CONSTRUCT{?s a foaf:Person . ?s foaf:name ?name} "
                        + "FROM <http://ucuenca.edu.ec/wkhuska> WHERE { ?s a foaf:Person . ?s foaf:name ?name . ?s foaf:publications ?pub . {0} } ";
                var filterPath = 'FILTER(CONTAINS(UCASE(?name), "{0}" )) . ';
                var searchText = $scope.searchText.trim();
                var keywords = searchText.split(" ");
                var filterContainer = "";
                keywords.forEach(function (val) {
                    if (val.length > 0) {
                        filterContainer += String.format(filterPath, val.toUpperCase());
                    }
                });
                queryAuthors = String.format(queryAuthors, filterContainer);
                $scope.sparqlQuery.querySrv({query: queryAuthors},
                function (rdf) {
                    var context = {
                        "foaf": "http://xmlns.com/foaf/0.1/"/*,
                         "foaf:name": {"@id": "http://xmlns.com/foaf/0.1/name"},
                         "foaf:Person": {"@id": "http://xmlns.com/foaf/0.1/Person"}*/
                    };
                    jsonld.compact(rdf, context, function (err, compacted) {
                        searchData.authorSearch = compacted;
                        $window.location.hash = "w/search?" + $scope.searchText;
                    });
                    //$rootScope.$emit('authorSearch', rdf);
                });

            }
        }

    }]);
wkhomeControllers.controller('ExploreController', ['$scope', '$window',
    function ($scope, $window) {
        console.log($scope.text);
    }]);
/* Sample of a oontroller that manages requests with URL params */
wkhomeControllers.controller('PhoneDetailCtrl', ['$scope', '$routeParams', 'Phone',
    function ($scope, $routeParams, Phone) {

        $scope.phone = Phone.get({phoneId: $routeParams.phoneId}, function (phone) {
            $scope.mainImageUrl = phone.images[0];
        });
        $scope.setImage = function (imageUrl) {
            $scope.mainImageUrl = imageUrl;
        }
    }]);
/*
 wkhomeControllers.controller('CallbacksController', ['$scope', function ($scope) {
 $scope.beforeSnapMessages = [];
 $scope.afterSnapMessages = [];
 $scope.beforeCallback = function (snapIndex) {
 $scope.beforeSnapMessages.push('Snapping to index ' + snapIndex);
 if (snapIndex === 4) {
 $scope.beforeSnapMessages.push('Snapping to index 4 disabled');
 return false;
 }
 };
 $scope.afterCallback = function (snapIndex) {
 $scope.afterSnapMessages.push('Just snapped to index ' + snapIndex);
 };
 }]);
 */

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
wkhomeControllers.controller('viewController', ['$scope', 'publicationsLinksData',
    function ($scope, publicationsLinksData) {
        // variable passed to app controller




        $scope.persona = {
            nombre: "Lorenzo",
            ape1: "Garcia"
        }

        $scope.$watch("values", function (newValue, oldValue) {
            if (newValue === oldValue) {
                return;
            }

            alert("El valor ha cambiado");
        });
        $scope.change = function () {
            $scope.persona = {
                nombre: "Lorenzo",
                ape1: "Garcia"
            }
        }




        $scope.filteredTodos = []
                , $scope.currentPage = 1
                , $scope.numPerPage = 10
                , $scope.maxSize = 5
                , $scope.data = publicationsLinksData.pubData
                , $scope.todos = [];
        $scope.makeTodos = function () {
            $scope.todos.push = ({text: 'Dato Origen ', done: false}); //$scope.data;
//    for (i=1;i<=1000;i++) {
//      $scope.todos.push({ text:'todo '+i, done:false});
//    }
        };
        $scope.makeTodos();
        $scope.$watch('currentPage + numPerPage', function () {
            var begin = (($scope.currentPage - 1) * $scope.numPerPage)
                    , end = begin + $scope.numPerPage;
            // $scope.filteredTodos = $scope.todos.slice(begin, end);
        });
    }]);
/*
 wkhomeControllers.directive('keyboardKeys', ['$document', function ($document) {
 return {
 restrict: 'A',
 link: function (scope) {
 var keydown = function (e) {
 if (e.keyCode === 38) {
 e.preventDefault();
 scope.$emit('arrow-up');
 }
 if (e.keyCode === 40) {
 e.preventDefault();
 scope.$emit('arrow-down');
 }
 };
 $document.on('keydown', keydown);
 scope.$on('$destroy', function () {
 $document.off('keydown', keydown);
 });
 }
 }
 }]);
 */
