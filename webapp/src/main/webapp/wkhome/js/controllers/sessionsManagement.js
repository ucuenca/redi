wkhomeControllers.controller('sessionMg', ["$scope", "cookies", '$routeParams', '$http', 'getORCIDToken', 'globalData', function ($scope, cookies, $routeParams, $http, getORCIDToken, globalData) {

        $scope.clientId = globalData.client_id;
        $scope.callback = globalData.callback;

        $scope.loggedin = function () {
            var logg = globalData.getSession();
            var lo = logg !== undefined && logg !== null && logg !== '';
            if (lo) {
                $scope.name = JSON.parse(logg).name;
                $scope.orcid = JSON.parse(logg).orcid;
            }
            return lo;
        };

        $scope.logout = function () {
            cookies.set(globalData.cookie_prefix + '_ORCID', '');
            window.location.hash = '/';
        };
        
        $scope.getState = function () {
            return window.location.hash.substring(1);
        };


        if ($routeParams.code && $routeParams.state) {
            getORCIDToken.query({uri: globalData.callback, code: $routeParams.code}, function (data) {
                cookies.set(globalData.cookie_prefix + '_ORCID', JSON.stringify(data));
                window.location.hash = $routeParams.state;
            });
        }

    }]);
