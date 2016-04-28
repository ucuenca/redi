wkhomeControllers.controller('exportController', ['$scope', 'reportService', '$window', '$sce',
    function ($scope, reportService, $window, $sce) {
        
        //--- Jose Luis ---
        //Generic function used to generate the reports in the server side, and then show them in the web browser
        $scope.exportData = function (type1, reportName, data) {
            //If the data is not null
            if (data) {
                switch (type1) {
                    case 'xls':
                    case 'pdf':
                        var params = {hostname: $window.location.origin, report: reportName, type: type1, param1: data};
                        reportService.querySrv(params, function (response) {
                            var res = '';
                            for (var i = 0; i < Object.keys(response).length - 2; i++) {
                                res += response[i];
                            }
                            if (res && res!=='') {
                                $window.open($sce.trustAsResourceUrl(res));
                            } else {
                                alert("Error. Por favor, espere un momento y vuelva a intentarlo.");
                            }
                        });
                        break;
                }
            }
        };
        
        
    }]);


