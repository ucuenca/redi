wkhomeControllers.controller('exportController', ['$scope', 'reportService', '$window', '$sce',
    function ($scope, reportService, $window, $sce) {
        
        //--- Jose Luis ---
        //Author's report
        $scope.exportData = function (type1, data) {
            //If the data is not null
            if (data) {
                switch (type1) {
                    case 'xls':
                    case 'pdf':
                        var params = {hostname: $window.location.origin, report: 'ReportAuthor', type: type1, param1: data[0]["@id"]};
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


