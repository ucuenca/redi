wkhomeControllers.controller('relatedAuthor', ['$scope', '$window', 'globalData', 'sparqlQuery', 'searchData', '$routeParams',
    function ($scope, $window, globalData,   sparqlQuery, searchData , $routeParams) {
        //$scope.todos = [];

         var host = "http://localhost:8080";
         var authorURI = $routeParams.authorId;

          $.ajax({
        type: "GET",
        dataType: "JSON", //result data type
       // url: host + "/pubman/reports/collaboratorsData?URI=https://redi.cedia.edu.ec/resource/authors/UCUENCA/file/_SAQUICELA_GALARZA_____VICTOR_HUGO_" ,
      // url: host + "/pubman/reports/collaboratorsData?URI=https://redi.cedia.edu.ec/resource/authors/UCUENCA/file/_FEYEN_____JAN_" ,
         url: host + "/pubman/reports/collaboratorsData?URI="+authorURI ,
        success: function(Result) {
         $scope.relatedAuthorData = Result;
       //  render (Result);
       //  etiquetas ();
    
        },
        error: function(data) {
            //document.getElementById("imgloading").style.visibility = "hidden";
            alert("Error" + data.responseText);
        }
        });

        console.log ("NADA q hacer");

       /* $scope.ctrlFn = function (value)
        {
            $scope.todos = [];
            var model = {};
            _.map(value, function (pub) {
                //var keys = Object.keys(author);
                model["id"] = pub["@id"];
                model["title"] = _.isArray(pub["dct:title"]) ? _.first(pub["dct:title"]) : pub["dct:title"];
                model["abstract"] = pub["bibo:abstract"] ? pub["bibo:abstract"] : "";
                model["uri"] = pub["bibo:uri"] ? pub["bibo:uri"]["@id"] : "";
                
                if (model["title"])
                {
                    $scope.todos.push({id: model["id"], title: model["title"], abstract: model["abstract"], uri: model["uri"]});
                }
            });
            $('html,body').animate({
                scrollTop: $("#scrollToHere").offset().top
            }, "slow");
           // $scope.loadData();
        };*/
        /*
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
        };*/
       /* 
        $scope.$watch('searchData.genericData', function (newValue, oldValue, scope) {
            $scope.data = {schema: {"context": globalData.CONTEXT, fields: ["rdfs:label", "uc:total"]}, data: searchData.genericData};
        });*/

        //Function that displays the buttons to export the report
       /* $scope.exportReport = function (id) {
            $scope.author = id;
            $scope.showRepButtons = true;
        };*/

    }]); //end genericcloudController
