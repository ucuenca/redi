/* 
 * Controller to load/list Authors Sources
 */

wkhomeControllers.controller('helpC', ['sparqlQuery', '$scope', 'globalData', 'Statistics',  '$window' , '$location', '$anchorScroll',
    function (sparqlQuery, $scope, globalData, Statistics , window ,  $location, $anchorScroll) {
        console.log ("CARGO HELP");

        movetoMap = function ( sec ) {
            console.log ("Move " + sec);

            $location.hash( sec);
             $anchorScroll();

        }

    }]); //end controller

