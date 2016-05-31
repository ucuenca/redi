'use strict';

var commonDirectives = angular.module('commonDirectives', []);

commonDirectives.directive('keyboardKeys', ['$document', function ($document) {
        return {
          restrict: 'A',
          link: function (scope) {
            var keydown = function (e) {
              if (e.keyCode === 38) {
                e.preventDefault();
                scope.$emit('arrow-up');
              }
              if (e.keyCode === 40) {
                e.preventDefault();
                scope.$emit('arrow-down');
              }
            };
            $document.on('keydown', keydown);
            scope.$on('$destroy', function () {
              $document.off('keydown', keydown);
            });
          }
        }
      }]);
