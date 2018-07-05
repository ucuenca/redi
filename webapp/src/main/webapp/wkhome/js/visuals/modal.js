wkhomeApp.directive('modalComponent', function() {
   return {
     restrict: 'E',
     replace: true,
     transclude: true,
     link: function(scope) {
       scope.cancel = function() {
         scope.$dismiss('cancel');
       };
     },
     template:
       "<div>" +
         "<div class='modal-header'>" +
           "<h3 ng-bind='dialogTitle'></h3>" +
           "<div ng-click='cancel()'>X</div>" +
         "</div>" +
         "<div class='modal-body' ng-transclude></div>" +
       "</div>"
   };
 });
