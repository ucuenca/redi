wkhomeControllers.controller('searchText', ['$routeParams', '$scope', '$window', 'globalData', 'sparqlQuery', 'searchData', 'searchQueryService', '$location', 'AuthorsService', 'KeywordsService', 'PublicationsService', 'ProjectsService', 'searchTextResultsService',
    function ($routeParams, $scope, $window, globalData, sparqlQuery, searchData, searchQueryService, $location, AuthorsService, KeywordsService, PublicationsService, ProjectsService, searchTextResultsService) {
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

        $('#searchType').on('change', function () {
            if (this.value == 'all') {
                $('#txtSearch').attr("placeholder", "Buscar ...");
            } else if (this.value == 'authors') {
                $('#txtSearch').attr("placeholder", "Researcher's name ...");
            } else if (this.value == 'organizations') {
                $('#txtSearch').attr("placeholder", "Organization's name ...");
            } else if (this.value == 'publications') {
                $('#txtSearch').attr("placeholder", "Publication's keywords ...");
            } else if (this.value == 'projects') {
                $('#txtSearch').attr("placeholder", "Publication's keywords ...");
            }
        });


        $('#searchType').selectpicker();



        function Candidate(id, val, desc, path) {
            this.id = id;
            this.val = val;
            this.desc = desc;
            this.path = path;
        }
        ;

        var queryPublications = globalData.PREFIX +
                "  " +
                " construct { " +
                " ?a ?a ?a . " +
                " }where { " +
                "  { " +
                "     select ?a (group_concat (?v ; separator=' ') as ?qq) { " +
                "        graph <https://redi.cedia.edu.ec/context/organization> { " +
                "            ?a a foaf:Organization . " +
                "            ?a ?p ?v . " +
                "            filter isLiteral (?v) . " +
                "        } " +
                "    } group by ?a " +
                "  } .  " +
                "  filter regex (?qq, '{0}', 'i') . " +
                "} ";


        function searchAuthor(searchTextVar, chain) {
            if (!chain) {
                waitingDialog.show();
            }
            AuthorsService.get({
                search: searchTextVar
            }, function (result) {
                if (!chain) {
                    waitingDialog.hide();
                }
                var authors = result.response.docs;
                if (authors.length > 1) {
                    if (chain) {
                        waitingDialog.hide();
                    }
                    var path = "/author/profile/";
                    var candidates = _.map(authors, function (author) {
                        var id = author["lmf.uri"];
                        var name = _.max(author.name, function (name) {
                            return name.length;
                        });
                        var topics = _.chain(author.topics)
                                .uniq()
                                .first(10)
                                .value()
                                .join(", ");
                        var candidate = new Candidate(id, name, topics, path);

                        return candidate;
                    });
                    $scope.candidates = candidates;
                    searchTextResultsService.saveData(candidates);
                    $('#searchResults').modal('show');
                } else if (authors.length === 1) {
                    if (chain) {
                        waitingDialog.hide();
                    }
                    var authorId = authors[0]["lmf.uri"];
                    $window.location.hash = "/author/profile/" + authorId;
                } else if (authors.length === 0) {
                    if (chain) {
                        searchOrganization(searchTextVar, true);
                    } else {
                        if (chain) {
                            waitingDialog.hide();
                        }
                        alert('No hay resultados ...');
                    }
                }
            } , function (error){
                if (!chain) {
                waitingDialog.hide();
                alert("Problemas con los datos");
                }
            });
        }

        function searchProjects(searchTextVar, chain) {
            if (!chain) {
                waitingDialog.show();
            }
            ProjectsService.get({
                search: searchTextVar
            }, function (result) {
                if (!chain) {
                    waitingDialog.hide();
                }
                var projects = result.response.docs;
                if (projects.length > 1) {
                    if (chain) {
                        waitingDialog.hide();
                    }
                    var path = "/project/profile/";
                    var candidates = _.map(projects, function (author) {
                        var id = author["lmf.uri"];
                        var title = _.max(author.title, function (title) {
                            return title.length;
                        });
                        var topics = _.chain(author['member-organization-name'])
                                .uniq()
                                .first(10)
                                .value()
                                .join(", ");
                        var candidate = new Candidate(id, title, topics, path);

                        return candidate;
                    });
                    $scope.candidates = candidates;
                    searchTextResultsService.saveData(candidates);
                    $('#searchResults').modal('show');
                } else if (projects.length === 1) {
                    if (chain) {
                        waitingDialog.hide();
                    }
                    var authorId = projects[0]["lmf.uri"];
                    $window.location.hash = "/project/profile/" + authorId;
                } else if (projects.length === 0) {
                    if (chain) {
                        searchPublications(searchTextVar, true);
                    } else {
                        if (chain) {
                            waitingDialog.hide();
                        }
                        alert('No hay resultados ...');
                    }
                }
            } , function (error){
                if (!chain) {
                waitingDialog.hide();
                alert("Problemas con los datos");
                }
            });
        }



        function searchOrganization(searchTextVar, chain) {
            if (!chain) {
                waitingDialog.show();
            }
            var res = String.format(queryPublications, searchTextVar);
            sparqlQuery.querySrv({
                query: res
            },
                    function (rdf) {
                        if (!chain) {
                            waitingDialog.hide();
                        }
                        if (rdf && rdf.length >= 1) {
                            if (chain) {
                                waitingDialog.hide();
                            }
                            var uri = rdf[0]['@id'];
                            $window.location.hash = '/info/statisticsbyInst/' + uri;
                        } else {
                            if (chain) {
                                waitingDialog.hide();
                                searchProjects(searchTextVar, true);
                            } else {
                                alert('No hay resultados ...');
                            }
                        }
                    } , function (error){
                if (!chain) {
                waitingDialog.hide();
                alert("Problemas con los datos");
                }
            });
        }


        function searchPublications(searchTextVar, chain) {
            var tx = '/search/publications/q=' + encodeURIComponent(searchTextVar) + '&fl=*&rows=10&wt=json';
            $window.location.hash = tx;
        }

        $scope.submit = function () {
            var typeSearch = $('#searchType :selected').text();
            if ($scope.searchText) {
                var searchTextVar = $scope.searchText;
                if (typeSearch && typeSearch != 'all') {
                    if (typeSearch == 'authors') {
                        searchAuthor(searchTextVar, false);
                    } else if (typeSearch == 'organizations') {
                        searchOrganization(searchTextVar, false);
                    } else if (typeSearch == 'projects') {
                        searchProjects(searchTextVar, false);
                    } else if (typeSearch == 'publications') {
                        searchPublications(searchTextVar, false);
                    }
                } else {
                    waitingDialog.show();
                    searchAuthor(searchTextVar, true);
                }
                $scope.searchText = '';
            }
        }
    }
]);