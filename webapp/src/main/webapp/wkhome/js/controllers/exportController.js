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
                        $scope.loading = true;
                        var params = {hostname: '', report: reportName, type: type1, param1: data};
                        reportService.querySrv(params, function (response) {
                            var res = '';
                            for (var i = 0; i < Object.keys(response).length - 2; i++) {
                                res += response[i];
                            }
                            if (res && res!=='' && res!=='undefinedundefinedundefinedundefined') {
                                $window.open($sce.trustAsResourceUrl($window.location.origin + res));
                            } else {
                                alert("Error al procesar el reporte. Por favor, espere un momento y vuelva a intentarlo. Si el error persiste, consulte al administrador del sistema.");
                            }
                            $scope.loading = false;
                        });
                        break;
                }
            }
        };
        
        
    }]);


