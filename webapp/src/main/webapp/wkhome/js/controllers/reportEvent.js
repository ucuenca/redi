wkhomeControllers.controller('reportEvent', ['$scope', '$routeParams', 'globalData', 'sparqlQuery', 'StatisticsbyInst', 'searchData', '$route', '$window', 'EventsService', 'getMetric',
  function ($scope, $routeParams, globalData, sparqlQuery, StatisticsbyInst, searchData, $route, $window, EventsService, getMetric) {
    var self = this;
    $scope.data = {};
    var colors = Highcharts.getOptions().colors;
    var colour = ['#00429d', '#3552a5', '#5062ad', '#6673b5', '#7985bb', '#8a98c1', '#58bc9b', '#91dcfc', '#4ebce8', '#219ac8', '#0077a6', '#005682', '#003760'];
    var colorred = ['#8f0000', '#b50000', '#dc0000', '#f83120', '#fe6843', '#fd9270', '#fab5a0', '#f7d6cb', '#ecd4f9', '#e3b2fc', '#d490ff', '#b476ff', '#925cff', '#6d3ffe', '#4126ef', '#0000d8'];
    var multi = ['#e5340f', '#e9520d', '#ec6a0b', '#ef8009', '#f19408', '#f4a707', '#f6b905', '#f8cb04', '#fbdc03', '#fdee01', '#a1f0e6', '#95e6cd', '#8adcb4', '#7fd19a', '#73c781', '#68bd68', '#5cb24e', '#50a834', '#459d1a', '#399200'];
    var palete = ["#001219", "#005F73", "#0A9396", "#94D2BD", "#E9D8A6", "#CA6702", "#BB3E03", "#AE2012", "#9B2226"];




    EventsService.get({
      search: "*"
    }, function (data) {
      $scope.data.neventos = data.response.numFound;
    });


    getMetric.query({
      uri: null,
      metric: "EventsYears"
    }, function (data) {
      var datos = {ax: [], ay: []};
      var datosTop = [];
      data.data.map(function (row) {
        datos.ax.push(row.k);
        datos.ay.push(Number(row.v));
        datosTop.push({name: row.k, cname: row.k, value: Number(row.v)});
        return 0;
      });
      datosTop.sort((a, b) => {
        return a.value < b.value;
      });
      $scope.disevents = {container: "containerevent", "datos": datos};
      $scope.topeventos = {container: "containerevent", datos: datosTop};
    });


    getMetric.query({
      uri: null,
      metric: "EventsLocation"
    }, function (data) {
      var datos = [];
      var i = 0;
      data.data.map(function (row) {
        datos.push({name: row.k, cname: row.k, value: Number(row.v), color: colors[i], position: i + 1});
        i++;
        return 0;
      });
      $scope.locations = {container: "containerareas", "datos": datos};
    });


    getMetric.query({
      uri: null,
      metric: "EventsKeywords"
    }, function (data) {
      var datos = [];
      var i = 0;
      data.data.map(function (row) {
        datos.push({name: row.k, cname: row.k, value: Number(row.v), color: colors[i], position: i + 1});
        i++;
        return 0;
      });
      $scope.fuentes = {container: "containerfuentes", "datos": datos};
    });


  }]);

 