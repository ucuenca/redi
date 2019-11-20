wkhomeControllers.controller('feedbackController', ['$scope', '$window', '$routeParams', 'sendFeedback',  function ($scope, $window, $routeParams, sendFeedback) {

        $scope.url = window.location.href;
        $scope.topic = 'Otros';


        sendReport = function () {
                  sendFeedback.query(
                        {'name': $scope.name, 
                         'email': $scope.email,
                         'topic': $scope.topic,
                         'content': $scope.content,
                         'url': $scope.url
                        }
                    , function (data) {
                        alert ('Se reporte fue enviado exitosamente, gracias por su ayuda.') ;
                        $('#feedbackmodal').modal('hide');
                      }, function (some) {
                        alert ('Hubo un error al enviar su reporte, por favor inténtelo más tarde.') ;
                          
                      });

         };




    }]);
