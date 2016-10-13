
wkhomeControllers.controller('dataCube', ['$scope', '$window',
    function ($scope, $window) {
      $scope.load1 = function(){
        $("#idDataCube").load(function() {
          console.log($("#idDataCube").height());
            //$(this).height( $(this).contents().find("body").height() );
        });
      };

    }]);
