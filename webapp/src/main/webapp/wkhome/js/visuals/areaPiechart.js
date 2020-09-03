'use strict';
//Author of this View using D3: http://bl.ocks.org/NPashaP/96447623ef4d342ee09b

//var dateLineChart = angular.module('statsInst', []);
//  D3  Factory
//dateLineChart.factory('d3', function () {
 //   return  d3;
//});
wkhomeApp.directive('areaPiechart', ["d3", "globalData", "sparqlQuery",
    function (d3, globalData, sparqlQuery) {


        function draw(id, fData, w, h) {


var pieColors = (function () {
    var colors = [],
        base = Highcharts.getOptions().colors[0],
        i;

    for (i = 0; i < 10; i += 1) {
        // Start out with a darkened base color (negative brighten), and end
        // up with a much brighter color
        colors.push(Highcharts.Color(base).brighten((i - 5) / 11).get());
    }
    return colors;
}());

// Build the chart
Highcharts.chart('containerpc', {
    chart: {
        plotBackgroundColor: null,
        plotBorderWidth: null,
        plotShadow: false,
        type: 'pie'
    },
    title: {
        text: 'Areas de conocimiento'
    },
    tooltip: {
        pointFormat: '{series.name}: <b>{point.percentage:.1f}%</b>'
    },
    plotOptions: {
        pie: {
            allowPointSelect: true,
            cursor: 'pointer',
            colors: pieColors,
            dataLabels: {
                enabled: true,
                format: '<b>{point.label}</b>',
                distance: -20,
                // style: {
                //     color: (Highcharts.theme && Highcharts.theme.contrastTextColor) || 'black'
                // },
              // connectorColor: 'silver' ,
                filter: {
                    property: 'percentage',
                    operator: '>',
                    value: 1
                }
            }
        }
    }, credits: {
        enabled: false
    },
    series: [{
        name: 'Share',
        data:  fData.array,
        point: {
            events : {
                click : function () {
                    console.log (this);
                    window.location.href = "/#/info/statisticsbyArea/"+this.name;
                }
            }
        }

    }]
});

 
        }
        return {
            restrict: 'E',
            scope: {
                datapc: '=',
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
                    scope.$watch('datapc', function (newVal, oldVal, scope) {
                        //  Update  the chart

                        var data = scope.datapc;
                            // draw(svg, data, w, h);
   
                        if (data) {
                            draw(svg, data, w, h);
                        }
                    }, true);

                };
            }
        };
    }]);

//
// Highcharts.chart('container', {
//     chart: {
//         plotBackgroundColor: null,
//         plotBorderWidth: null,
//         plotShadow: false,
//         type: 'pie'
//     },
//     title: {
//         text: 'Browser market shares in January, 2018'
//     },
//     tooltip: {
//         pointFormat: '{series.name}: <b>{point.percentage:.1f}%</b>'
//     },
//     plotOptions: {
//         pie: {
//             allowPointSelect: true,
//             cursor: 'pointer',
//             dataLabels: {
//                 enabled: true,
//                 format: '<b>{point.name}</b>: {point.percentage:.1f} %',
//                 style: {
//                     color: (Highcharts.theme && Highcharts.theme.contrastTextColor) || 'black'
//                 },
//                 connectorColor: 'silver'
//             }
//         }
//     },
//     series: [{
//         name: 'Share',
//         data: [
//             { name: 'Chrome', y: 61.41 },
//             { name: 'Internet Explorer', y: 11.84 },
//             { name: 'Firefox', y: 10.85 },
//             { name: 'Edge', y: 4.67 },
//             { name: 'Safari', y: 4.18 },
//             { name: 'Other', y: 7.05 }
//         ]
//     }]
// }); 