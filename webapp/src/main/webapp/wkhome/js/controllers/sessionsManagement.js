wkhomeControllers.controller('sessionMg', ["$scope", "cookies", '$routeParams', '$http', 'getKeyCloakToken', 'globalData', function ($scope, cookies, $routeParams, $http, getKeyCloakToken, globalData) {

    $scope.keycloak = Keycloak({
      "realm": "redi",
      "auth-server-url": "https://service.login.cedia.edu.ec/auth",
      "ssl-required": "external",
      "resource": "redi",
      "public-client": true,
      "confidential-port": 0,
      "url": 'https://service.login.cedia.edu.ec/auth',
      "clientId": 'rediclon',
      "enable-cors": true
    });

    $scope.authenticated = false;



    $scope.keycloak.init({onLoad: 'check-sso', checkLoginIframe: false}).then(function (authenticated) {
      $scope.authenticated = authenticated;
      if (!authenticated) {
        cookies.set(globalData.cookie_prefix, '');
      } else {
        $scope.name = $scope.keycloak.idTokenParsed.name;
        $scope.mail = $scope.keycloak.idTokenParsed.email;
        $scope.$apply();
        getKeyCloakToken.query({uri: 'mock', code: $scope.keycloak.refreshToken}, function (data) {
          if (data['token']) {
            $scope.keycloak.idTokenParsed['redi_token'] = data['token']
            cookies.set(globalData.cookie_prefix, JSON.stringify($scope.keycloak.idTokenParsed));
            window.location.hash = "/author/profileval/_";
          } else {
            $scope.logout();
          }
        });

      }
    }).catch(function (e) {
      alert('failed to initialize Keycloak' + e);
    });

    $scope.init = function () {
      $scope.keycloak.login();
    }

    $scope.loggedin = function () {
      return $scope.keycloak.authenticated;
    };

    $scope.logout = function () {
      cookies.set(globalData.cookie_prefix, '');
      window.location.hash = '/';
      $scope.keycloak.logout();

    };

  }]);