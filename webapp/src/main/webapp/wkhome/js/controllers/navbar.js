wkhomeControllers.controller('navbarController', ["$scope", "$location", function ($scope, $location) {
    $scope.isActive = function (viewLocation) {
          return viewLocation === $location.path();
    };
    $scope.isHome = function() {
      console.log($location);
      return $location.path() === '/';
    }
}]);
