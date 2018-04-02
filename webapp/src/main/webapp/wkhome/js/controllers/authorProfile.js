wkhomeControllers.controller('AuthorProfile', ['$scope', '$routeParams',
  function($scope, $routeParams) {
    // Define a new author object
    $scope.author = {}
    var author = $scope.author;
    // Set URIs for navigation
    author.uri = $routeParams.author;
    author.encodedUri = encodeURIComponent(author.uri);
    // Get author information
    author.name = "Juan Perez";
    author.areas = ["Linked Data", "Semantic Web"];
    author.email = "jperex@networth.il";

  }]);
