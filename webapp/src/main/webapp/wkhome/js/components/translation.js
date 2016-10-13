/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


(function(){
var myApp = angular.module("wkhomeApp");
myApp.config(["$translateProvider", function ($translateProvider) {
  $translateProvider.translations("en", myApp.labels_en);
  $translateProvider.translations("es", myApp.labels_es);
  $translateProvider.preferredLanguage("en");
  // To get warnings in the developer console, regarding forgotten IDs in translations
  $translateProvider.useMissingTranslationHandlerLog ();
  // Enable escaping of HTML
  $translateProvider.useSanitizeValueStrategy('escape');
}]);
}());
