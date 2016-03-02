wkhomeControllers.controller('exportController', ['$scope', function($scope) {
    $scope.exportData = function (type, data) {
        
        var appType;
        if (data) {
            switch (type) {
                case 'pdf':
                    appType = "application/x-pdf";
                    break;
                case 'xls':
                    appType = "application/vnd.ms-excel";
                    break;
            }

            var blob = new Blob([document.getElementById('exportable').innerHTML], {
                type: appType
            });
            saveAs(blob, "Report." + type);
        }
    };

    /*$scope.items = [{
        name: "John Smith",
        email: "j.smith@example.com",
        dob: "1985-10-10"
    }, {
        name: "Jane Smith",
        email: "jane.smith@example.com",
        dob: "1988-12-22"
    }, {
        name: "Jan Smith",
        email: "jan.smith@example.com",
        dob: "2010-01-02"
    }, {
        name: "Jake Smith",
        email: "jake.smith@exmaple.com",
        dob: "2009-03-21"
    }, {
        name: "Josh Smith",
        email: "josh@example.com",
        dob: "2011-12-12"
    }, {
        name: "Jessie Smith",
        email: "jess@example.com",
        dob: "2004-10-12"
    }];*/
}]);


