wkhomeControllers.controller('feedbackController', ['$scope', '$window', '$routeParams', 'sendFeedback', 'searchTextResultsService', function ($scope, $window, $routeParams, sendFeedback, searchTextResultsService) {

    $scope.$on('updateStatus', function () {
      $scope.url = window.location.href;
      $scope.topic = 'Otros';
      $scope.name = '';
      $scope.email = '';
      $scope.content = '';
      $('#feedbackmodal').modal('show');
    });

    sendReport = function () {
      sendFeedback.query(
              {'name': $scope.name,
                'email': $scope.email,
                'topic': $scope.topic,
                'content': $scope.content,
                'url': $scope.url
              }
      , function (data) {
        alert('Se reporte fue enviado exitosamente, gracias por su ayuda.');
        $('#feedbackmodal').modal('hide');
      }, function (some) {
        alert('Hubo un error al enviar su reporte.');

      });

    };




  }]);
