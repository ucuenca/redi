wkhomeControllers.controller('translate', ['sparqlQuery','searchData','$translate','$routeParams', '$scope', '$window', 'translateService', 'globalData',
    function (sparqlQuery, searchData, $translate, $routeParams, $scope, $window, translateService, globalData) {
        $translate.use($routeParams.lang);

        $scope.lang = globalData.language;

        $scope.setLanguage = function (value) {
            globalData.language = value;
            $scope.lang = globalData.language;
            if($routeParams.lang === 'es' && value == 'en'){
                $window.location.hash = $window.location.hash.replace('/es/', '/en/');
            } 
            if($routeParams.lang === 'en' && value == 'es'){
                $window.location.hash = $window.location.hash.replace('/en/', '/es/');
            }
        };
        
        $scope.refreshLang = function () {
            $scope.lang = $routeParams.lang;
        };
        
        
        
        
    
             
        /**
         * Loading DATA in Memory
         */
           /*************************************************************/
        /*query to get the keywords in memory */
        /*************************************************************/
        loadAllKeyword();
        $scope.themes = [];
        function loadAllKeyword() {
            var queryKeywords = globalData.PREFIX
                    + ' CONSTRUCT { ?keywordp rdfs:label ?keyp } '
                    + '	FROM <' + globalData.centralGraph + '> '
                    + ' WHERE { '
                    + '     SELECT  (count(?key) as ?k) (SAMPLE(?keyword) as ?keywordp) (SAMPLE(?key) as ?keyp) '
                    + '         WHERE { '
                    + '              ?subject foaf:publications ?pubs. '
                    //+ '              ?subject dct:subject ?key. '
                    + '             ?pubs bibo:Quote ?key. '
                    + '             BIND(REPLACE(?key, " ", "_", "i") AS ?unickey). '
                    + '             BIND(IRI(?unickey) as ?keyword) '
                    + '         } '
                    + '     GROUP BY ?subject '
                    //            + '     HAVING(?k > 1) '
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
                        searchData.allkeywords = $scope.themes;
                    });
                    waitingDialog.hide();
                });
            });
        }
        /***********************************/
        /***********************************/




        /*********************************************/
        /* LOAD DATA TO KEYWORDS CLOUD */
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
                + '     HAVING(?totalPub > 2 && ?totalPub < 180) '
                + '     ORDER BY DESC(?totalPub) '
                + '     LIMIT 145'
                + ' } ';
        sparqlQuery.querySrv({query: queryKeywords}, function (rdf) {

            jsonld.compact(rdf, globalData.CONTEXT, function (err, compacted) {
                $scope.$apply(function () {
                    //$scope.data = {schema: {"context": context, fields: ["rdfs:label", "uc:total"]}, data: compacted};
                    searchData.allkeywordsCloud = {schema: {"context": globalData.CONTEXT, fields: ["rdfs:label", "uc:total"]}, data: compacted};
                    waitingDialog.hide();
                });
            });
        });
        //***************************************************//
        
        
   
    }]); //end translate controller