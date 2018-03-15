wkhomeControllers.controller('searchTextResults', ['$scope', '$window', '$routeParams', 'searchTextResultsService', function ($scope, $window, $routeParams, searchTextResultsService) {
        $scope.candidates = searchTextResultsService.getData();

        $scope.$on('saveData', function () {
            $scope.candidates = searchTextResultsService.getData();
        });


        $scope.selectedOption = function ($event, path, param, paramsQuery) {
            $('#searchResults').modal('hide');
            $('#searchResults').on('hidden.bs.modal', function () {
                $window.location.hash =  path + param;
                $('#searchResults').off('hidden.bs.modal');
            });
        };

        $scope.showSubjects = function (values) {
            return _.some(values, 'desc');
        }
    }]);

