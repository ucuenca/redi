'use strict';
//Author of this View using D3: http://bl.ocks.org/NPashaP/96447623ef4d342ee09b

var barChart = angular.module('dateLineChart', []);
//  D3  Factory
barChart.factory('d3', function () {
    return  d3;
});
barChart.directive('barChart', ["d3", "globalData", "sparqlQuery",
    function (d3, globalData, sparqlQuery) {


        function draw(id, fData, w, h) {

Highcharts.chart('container', {
    chart: {
        type: 'line'
    },
    title: {
        text: 'Monthly Average Temperature'
    },
    subtitle: {
        text: 'Source: WorldClimate.com'
    },
    xAxis: {
        categories: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']
    },
    yAxis: {
        title: {
            text: 'Temperature (°C)'
        }
    },
    plotOptions: {
        line: {
            dataLabels: {
                enabled: true
            },
            enableMouseTracking: false
        }
    },
    series: [{
        name: 'Tokyo',
        data: [7.0, 6.9, 9.5, 14.5, 18.4, 21.5, 25.2, 26.5, 23.3, 18.3, 13.9, 9.6]
    }, {
        name: 'London',
        data: [3.9, 4.2, 5.7, 8.5, 11.9, 15.2, 17.0, 16.6, 14.2, 10.3, 6.6, 4.8]
    }]
});

 
        }
        return {
            restrict: 'E',
            scope: {
                data: '=',
            },
            compile: function (element, attrs, transclude) {
                //  Create  a   SVG root    element
                var svg = d3.select(element[0]);
                //var width = 960, height = 500;
                var elementWidth = parseInt(element.css('width'));
                var width = attrs.pcWidth ? attrs.pcWidth : elementWidth,
                        height = attrs.pcHeight;
                var w = element.parent()[0].offsetWidth - 0.01*(element.parent()[0].offsetWidth),
                    h = (2 * w) / 7;
                //  Return  the link    function
                return  function (scope, element, attrs) {
                    //  Watch   the data    attribute   of  the scope
                    scope.$watch('data', function (newVal, oldVal, scope) {
                        //  Update  the chart
                        var data = scope.data;

                        if (data) {
                            draw(svg, data, w, h);
                        }
                    }, true);

                };
            }
        };
    }]);
