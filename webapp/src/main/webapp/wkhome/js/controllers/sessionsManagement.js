wkhomeControllers.controller('sessionMg', ["$scope", "cookies", '$routeParams', '$http', 'getORCIDToken', function ($scope, cookies, $routeParams, $http, getORCIDToken) {

        var oapp = '';
        var oappn = '';
        if (window.location.origin.includes('clon')) {
            oapp = 'APP-R08L1P7JVVGRW8YN';
            oappn = 'REDICLON';
        } else {
            oapp = 'APP-7Y3IFBAL9DB2NVJC';
            oappn = 'REDI';
        }
        $scope.oapp = oapp;
        $scope.ouri = window.location.origin + '/wkhome/partials/callback.html';

        $scope.loggedin = function () {
            var logg = cookies.get(oappn + '_ORCID');
            var lo = logg !== undefined && logg !== null && logg !== '';
            if (lo) {
                $scope.name = JSON.parse(logg).name;
                $scope.orcid = JSON.parse(logg).orcid;
            }
            return lo;
        };

        $scope.logout = function () {
            cookies.set(oappn + '_ORCID', '');
            window.location.hash = '/';
        };


        if ($routeParams.code) {
            var uri = window.location.origin + '/wkhome/partials/callback.html';
            getORCIDToken.query({uri: uri, code: $routeParams.code}, function (data) {
                cookies.set(oappn + '_ORCID', JSON.stringify(data));
                window.location.hash = '/';
            });
        }







    }]);
