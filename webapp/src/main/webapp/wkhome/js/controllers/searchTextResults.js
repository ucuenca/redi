wkhomeControllers.controller('searchTextResults', ['$scope', '$window', '$routeParams', 'searchTextResultsService', function ($scope, $window, $routeParams, searchTextResultsService) {
        $scope.candidates = searchTextResultsService.getData();

        $scope.$on('saveData', function () {
            $scope.candidates = searchTextResultsService.getData();
        });


         $scope.getType  = function () {
            console.log (searchTextResultsService.getType());
           return  searchTextResultsService.getType();
         } 



        $scope.selectedOption = function ($event, path, param, paramsQuery) {
            $('#searchResults').modal('hide');
            $('#searchResults').on('hidden.bs.modal', function () {
            
                $window.location.hash = path + param;
                console.log (path);
                $('#searchResults').off('hidden.bs.modal');
            });
        };

          $scope.selectedOptionClaim = function ($event, path, param, paramsQuery) {
            $('#searchResults').modal('hide');
            $('#searchResults').on('hidden.bs.modal', function () {
                $window.location.hash = '/author/profileval/' + param;
                $('#searchResults').off('hidden.bs.modal');
            });
        };

             $scope.selectedOptionNewProfile = function ($event, path, param, paramsQuery) {
            $('#searchResults').modal('hide');
            $('#searchResults').on('hidden.bs.modal', function () {
                $window.location.hash = '/author/profileval/new_';
                $('#searchResults').off('hidden.bs.modal');
            });
        };

        $scope.showSubjects = function (values) {
            return _.some(values, 'desc');
        }
    }]);
