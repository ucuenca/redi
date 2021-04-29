wkhomeControllers.controller('localeController', ["$localeSelectorDialog", "avilableLocales", "$translate", function ($localeSelectorDialog, avilableLocales, $translate) {
    var vm = this;

    // default locale values
    vm.selectedLocale = "es-EC";
    vm.selectedLocaleVals = _.chain(avilableLocales).pick(vm.selectedLocale).compact().first().value();
    $translate.use(vm.selectedLocaleVals.language);
    $translate.preferredLanguage(vm.selectedLocaleVals.language);


    vm.changeLocale = function () {
        $localeSelectorDialog.open({
            locales: avilableLocales,
            showFlags: true,
            showSearch: false,
            contributeUrl: 'https://github.com/ucuenca/redi'
        }).result.then(function (selectedLocale, b, c,d,e) {
            vm.selectedLocale = selectedLocale;
            vm.selectedLocaleVals = _.chain(avilableLocales).pick(selectedLocale).compact().first().value();
            $translate.use(vm.selectedLocaleVals.language);
            $translate.preferredLanguage(vm.selectedLocaleVals.language);
        });
    };

    // $scope.isActive = function (viewLocation) {
    //     return viewLocation === $location.path();
    // };
}]);
