wkhomeControllers.controller('keywordsCloud', ['$translate', '$routeParams', '$scope', 'globalData', 'sparqlQuery', 'searchData', '$window', 'Statistics', 'clustersTotals', 'subclustersTotals',
    function ($translate, $routeParams, $scope, globalData, sparqlQuery, searchData, $window, Statistics, clustersTotals, subclustersTotals) {
       /* $("html, body").animate({
            scrollTop: 0
        }, 'slow', 'swing');*/

        $scope.selectedItem = undefined;
         $scope.data = [];
        Statistics.query({
            id: 'keywords_frequencypub_gt4'
        }, function (data) {
            $scope.areas = [];
            _.map(data["@graph"], function (keyword) {
                $scope.areas.push({
                    label: keyword["rdfs:label"]["@value"],
                    id: keyword["@id"]
                });
            });
        });

        renderAll ();

        /**
         * Search for areas...
         */
       /* $scope.querySearch = function (query) {
            return query ? searchData.allkeywordsList.filter(createFilterFor(query)) : searchData.allkeywordsList;
        };*/

        /**
         * Create filter function for a query string
         */
       /* function createFilterFor(query) {
            var lowercaseQuery = angular.lowercase(query);
            return function filterFn(area) {
                return (angular.lowercase(area.tag).indexOf(lowercaseQuery) !== -1);
            };
        }*/

      /*  $scope.clickonAuthor = function (id_author) {
            clickonRelatedauthor(id_author);
        }; //end clickonAuthor

        clickonRelatedauthor = function (id_author) {
            $window.location.hash = "/" + $routeParams.lang + "/w/author/" + id_author;
        }; //end clickonRelatedauthor

        $scope.todos = [];
        $scope.ctrlFn = function (value) {

            var publicaciones = _.where(value, {
                "@type": "bibo:Document"
            });
            var autores = _.where(value, {
                "@type": "foaf:Person"
            });

            $scope.todos = [];
            $scope.autores = [];
            var model = {};
            _.map(publicaciones, function (pub) {
                //var keys = Object.keys(author);

                model["id"] = pub["@id"];
                model["title"] = typeof pub["dct:title"] === 'string' ? pub["dct:title"] : _(pub["dct:title"]).first();

                model["author"] = pub["dct:contributor"] ? pub["dct:contributor"] : [];
                model["abstract"] = pub["bibo:abstract"] ? (typeof pub["bibo:abstract"] === 'string' ? pub["bibo:abstract"] : _(pub["bibo:abstract"]).first()) : "";
                model["uri"] = typeof pub["bibo:uri"] === 'string' ? pub["bibo:uri"] : (_.some(pub["bibo:uri"], function (value) {
                    return _(value).has('@id');
                }) ? _.first(pub["bibo:uri"]) : "");

                $scope.autores = [];
                var cont = 0;
                _.map(pub["dct:contributors"], function (authorid) {
                    cont = cont + 1;
                    var authorresource = authorid["@id"] ? (_.findWhere(autores, {
                        "@id": authorid["@id"]
                    })) : (_.findWhere(autores, {
                        "@id": authorid
                    }));
                    var name = typeof authorresource["foaf:name"] === 'string' ? authorresource["foaf:name"] : _(authorresource["foaf:name"]).first();
                    $scope.autores.push({
                        id: authorresource["@id"],
                        name: name
                    });
                });

                if (model["title"]) {
                    $scope.todos.push({
                        id: model["id"],
                        title: model["title"],
                        abstract: model["abstract"],
                        uri: model["uri"],
                        author: $scope.autores
                    });
                }
            });
            $('html,body').animate({
                scrollTop: $("#scrollToHere").offset().top
            }, "slow");
            $scope.loadData();
        };*/
      
      /*  $scope.loadData = function () {
            $scope.$apply(function () {
                $scope.filteredTodos = [], $scope.currentPage = 1, $scope.numPerPage = 10, $scope.maxSize = 5;
                $scope.$watch('currentPage + numPerPage', function () {
                    var begin = (($scope.currentPage - 1) * $scope.numPerPage),
                            end = begin + $scope.numPerPage;
                    $scope.filteredTodos = $scope.todos.slice(begin, end);
                });
            });
        };*/


           function renderAll () {
                   waitingDialog.show();
            clustersTotals.query({}, function (res) {
         
                        _.map(res, function (area) {
                            $scope.data.push({
                                id: area["area"],
                                label: area["k"],
                                value: area["totalAuthors"]
                            });
                        });
                        waitingDialog.hide();
            });
              }
         
          
      /*  if (!searchData.allkeywordsCloud) {
            waitingDialog.show();
            clustersTotals.query({}, function (res) {
                setTimeout(function () {
                    $scope.$apply(function () {
                        $scope.data = [];
                        _.map(res, function (area) {
                            $scope.data.push({
                                id: area["area"],
                                label: area["k"],
                                value: area["totalAuthors"]
                            });
                        });
                        waitingDialog.hide();
                    });
                }, 3000);
            });

        } else {
            $scope.data = searchData.allkeywordsCloud;
        } */
    

       $scope.changeComboSelected  = function() {
                if ($scope.selectedItem != undefined && $scope.selectedItem.length > 0) {
               
               renderSub ();

            } 
            else {
                renderAll ();
            }

       }

       function renderSub () {
       /* waitingDialog.show();*/
             subclustersTotals.query({id: $scope.selectedItem}, function (res) {
               
                            $scope.data = [];
                            _.map(res, function (area) {
                                $scope.data.push({
                                    id: area["sc"],
                                    label: area["k"],
                                    value: area["totalAuthors"]
                                });
                            });
                         /*   waitingDialog.hide();*/
                        });

       }

         $scope.$watch('selectedItem', function () { 
           renderSub ();
         });

     

        //Function that displays the buttons to export the report
      /*  $scope.exportReport = function (id) {
            $scope.keyw = id;
            $scope.showRepButtons = true;
        };*/

    }
]);
