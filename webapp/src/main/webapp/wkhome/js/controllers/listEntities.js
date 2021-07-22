wkhomeControllers.controller('listEntitiesController', ['$scope', '$window', 'globalData', 'sparqlQuery', 'searchData', '$routeParams', '$translate', '$uibModal',
    function ($scope, $window, globalData, sparqlQuery, searchData, $routeParams, $translate, $uibModal) {

        if ($routeParams.authorId) {
            $scope.authorURI = $routeParams.authorId;
        }


        var typent = $window.location.hash.split('/')[2]; 


        
        console.log ("Consultando");
        console.log ($routeParams.query);
        console.log (typent);
        var defaulturl = '';
        var facets = [];
        switch(typent) {
            case 'publications':
            $scope.core = globalData.publicationsCore;
            defaulturl = 'total/publications/q=title:LET*&fl=*&rows=10&wt=json&sort=title+asc';
            queryurl = 'total/publications/q=(LET)&fl=*&rows=10&wt=json';
            facets = [{'ent': 'org' , 'label' : 'organization'},{'ent' : 'subjects' , 'label' : 'palabras clave'}]

            break;
            case 'authors':
            $scope.core = globalData.authorsCore;
            defaulturl = 'total/authors/q=familyname:LET*&fl=*&rows=10&wt=json&sort=familyname+asc';
            queryurl = 'total/authors/q=name:(LET)&fl=*&rows=10&wt=json';
            facets = [{'ent': 'org' , 'label' : 'organization'},{'ent' : 'subjects' , 'label' : 'palabras clave'}]
            break;

            case 'projects':
            $scope.core = globalData.projectsCore;
            defaulturl = 'total/projects/q=title:LET*&fl=*&rows=10&wt=json&sort=title+asc';
            queryurl = 'total/projects/q=(LET)&fl=*&rows=10&wt=json';
            facets = [{'ent': 'org' , 'label' : 'organization'},{'ent' : 'subjects' , 'label' : 'palabras clave'}]
            break;

            case 'patents':
            $scope.core = globalData.patentsCore;
            defaulturl = 'total/patents/q=title:LET*&fl=*&rows=10&wt=json&sort=title+asc';
            queryurl = 'total/patents/q=(LET)&fl=*&rows=10&wt=json';
            var inil = 'R';
            break;
            
            case 'datasets':
            $scope.core = globalData.datasetsCore;
            defaulturl = 'total/datasets/q=title:LET*&fl=*&rows=10&wt=json&sort=title+asc';
            queryurl = 'total/datasets/q=(LET)&fl=*&rows=10&wt=json';
            break;

            case 'events':
            $scope.core = globalData.eventsCore;
            defaulturl = 'total/events/q=title:LET*&fl=*&rows=10&wt=json&sort=title+asc';
            queryurl = 'total/events/q=(LET)&fl=*&rows=10&wt=json';
            break;

            case 'services':
            $scope.core = globalData.servicesCore;
            defaulturl = 'total/services/q=title:LET*&fl=*&rows=10&wt=json&sort=title+asc';
            queryurl = 'total/services/q=(LET)&fl=*&rows=10&wt=json';
            var inil = '*';
            break;


            case 'organizations':
            $scope.core = globalData.organizationsCore;
            defaulturl = 'total/organizations/q=name_abbr:LET*&fl=*&rows=10&wt=json&sort=name_abbr+asc';
            queryurl = 'total/organizations/q=name_es:(LET)+name_abbr:(LET)&fl=*&rows=10&wt=json';
            facets = [{'ent': 'org' , 'label' : 'organization'},{'ent' : 'subjects' , 'label' : 'palabras clave'}]
            var inil = 'U';
            break;

            default:
    
        }

        //$scope.core = globalData.authorsCore;
        $scope.puburi = "";
        $scope.showInfo = function (uri, type) {
            $uibModal.open({
                templateUrl: 'wkhome/partials/collectionTemplate.html',
                controller: JournalController,
                resolve: {
                    collections: function () {
                        return {
                            collectionType: type,
                            collectionUri: uri
                        };
                    }
                }
            });
        };

        $scope.mySplit = function(string, nb) {
        var array = string.split('-');
        return array[nb];
        }



       $scope.letters = ['*','A','B','C','D', 'E', 'F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','W','X','Y','Z'];
       // $scope.selectLetter = 'A';
       //console.log ("Seleccionado");
       //console.log ($scope.selectLetter);

       $scope.selectLetter = localStorage.getItem("lett");
       console.log ('Si letra'+$scope.selectLetter); 
       $scope.facets = facets;

       if ($scope.selectLetter === undefined || $routeParams.query ===  undefined) {
        
        console.log ('Letra'+$scope.selectLetter);
        if (inil === undefined) {
          inil = 'A'  
        } 
        
        localStorage.setItem("lett", inil);
        $window.location.hash = defaulturl.replace ('LET',inil);
       
       }

        


        $scope.isSelected = function ( leter ) {
            return  $scope.selectLetter == leter;

        }

        $scope.letterClick = function ( lett ) {

            localStorage.setItem("lett", lett);

            //this.classList.add("active");
            $window.location.hash = defaulturl.replace('LET',lett);

        }


        $scope.searchEntK = function (keyEvent) {
         
            if (keyEvent.which === 13)
               
                $scope.searchEnt ();
            }

        



        $scope.searchEnt = function () { 
            var texts = $('#txtSearch').val();
            
            $window.location.hash = queryurl.replaceAll('LET',texts);
            localStorage.setItem("lett", '');

        }




        $scope.showmodal = function () {
            $("#myModal").modal();
        }

        $scope.openmodal = function (puburi, stype) {
          if (stype==='authorMetrics') {
                $window.location.hash = "/author/report/"+puburi;
        	} else if (typent === 'authors') {
                $window.location.hash = "/author/profile/"+puburi;
            }else if (typent === 'projects') {

                $window.location.hash = "/project/profile/"+puburi;
            }else if (typent === 'patents') {

                $window.location.hash = "/patent/profile/"+puburi;

            }else if (typent === 'datasets') {

                $window.location.hash = "/dataset/profile/"+puburi;

            }else if (typent === 'events') {

                $window.location.hash = "/event/profile/"+puburi;

            }else if (typent === 'services') {

                $window.location.hash = "/service/profile/"+puburi;

            }else if (typent === 'organizations') {

                $window.location.hash = "/organization/profile/"+puburi;
            }
            else{

            //  $scope.puburi = "https://redi.cedia.edu.ec/resource/publication/080790388cd3adbc37099a6e41b3e163" ;
            $scope.puburi = puburi;
            console.log("Documento");
            console.log(puburi);
            $scope.$broadcast('hijo');

            }
        }

        $scope.openmodal2 = function () {
            $scope.puburi = "https://redi.cedia.edu.ec/resource/publication/a211ec9c3c5da269fbadb4a8bf48b55c";
            $("#myModal").modal();
            $scope.$broadcast('hijo');

        }

         function unique( a) {
            return Array.isArray(a) ? a[0] : a;
        }



        var JournalController = function ($scope, $uibModalInstance, collections, Journal) {
            $scope.journals = [];
            _.forEach(collections.collectionUri, function (uri) {
                Journal.getJournal().get({uri: uri}, function (data) {
                    $scope.journals.push(data.response.docs);
                    $scope.journals = _.flatten($scope.journals);
                });
            });
            $scope.type = collections.collectionType;
            $scope.dialogTitle = "";
        };

        if ($routeParams.authorId) {
            var sparqlAuthor = globalData.PREFIX +
                    "CONSTRUCT {" +
                    "  ?author a foaf:Person ;" +
                    "                    foaf:name ?name ;" +
                    "                    schema:memberOf ?org;" +
                    "                    foaf:img ?img." +
                    "  ?org uc:city ?city ;" +
                    "       uc:province ?province ;" +
                    "       uc:fullName ?fullname ." +
                    "} " +
                    "WHERE{" +
                    "  {" +
                    "   SELECT DISTINCT *" +
                    "    WHERE { " +
                    "      VALUES ?author { <" + $scope.authorURI + "> }" +
                    "      GRAPH <" + globalData.centralGraph + "> {" +
                    "        ?author a foaf:Person ;" +
                    "                           foaf:name ?name ;" +
                    "                           schema:memberOf ?org ." +
                    "        OPTIONAL { ?author foaf:img ?img. }" +
                    "      }" +
                    "      GRAPH <" + globalData.organizationsGraph + "> {" +
                    "        ?org uc:city ?city ;" +
                    "        uc:province ?province ;" +
                    "        uc:fullName ?fullname ." +
                    "        FILTER (lang(?fullname) = '" + $translate.use() + "')" +
                    "      }" +
                    "    }" +
                    "    LIMIT 1" +
                    "  }" +
                    "}";
            // waitingDialog.show("Searching: ");
            sparqlQuery.querySrv({
                query: sparqlAuthor
            }, function (rdf) {
                jsonld.compact(rdf, globalData.CONTEXT, function (error, compacted) {
                    if (!error) {
                        var entity = _.findWhere(compacted["@graph"], {
                            "@type": "foaf:Person"
                        });
                        author = {};
                        author.name = typeof (entity["foaf:name"]) === 'string' ? entity["foaf:name"] : _.first(entity["foaf:name"]);
                        author.photo = entity["foaf:img"] ? unique(entity["foaf:img"]["@id"]) : 'wkhome/images/no_photo.png';
                        author.institutions = [];
                        _.each(entity["schema:memberOf"], function (v) {
                            var provenance = typeof (v) === 'object' ?
                                    _.findWhere(compacted["@graph"], v) :
                                    _.findWhere(compacted["@graph"], {
                                        "@id": v
                                    })
                            author.institutions.push({
                                name: provenance['uc:fullName']['@value'],
                                province: provenance["uc:city"],
                                city: provenance["uc:province"]
                            })
                        });
                        $scope.$apply(function () {
                            $scope.author = author;
                            $scope.puburi = "https://redi.cedia.edu.ec/resource/publication/080790388cd3adbc37099a6e41b3e163";
                        });
                    }
                });
            });
        }

    }
]);
