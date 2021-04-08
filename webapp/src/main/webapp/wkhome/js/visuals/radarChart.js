'use strict';
//Author of this View using D3: http://bl.ocks.org/NPashaP/96447623ef4d342ee09b

//var dateLineChart = angular.module('statsInst', []);
//  D3  Factory
//dateLineChart.factory('d3', function () {
 //   return  d3;
//});
wkhomeApp.directive('radarChart', ["d3", "globalData", "sparqlQuery",
    function (d3, globalData, sparqlQuery) {


        function draw(id, fData, w, h) {
            console.log ("Circle Data");
            console.log (fData);

console.log ( fData.array.labels );
console.log ( fData.array.values );
/*var pieColors = (function () {
    var colors = [],
        base = Highcharts.getOptions().colors[0],
        i;

    for (i = 0; i < 10; i += 1) {
        // Start out with a darkened base color (negative brighten), and end
        // up with a much brighter color
        colors.push(Highcharts.Color(base).brighten((i - 5) / 11).get());
    }
    return colors;
}());*/

// Build the chart
Highcharts.chart('containerpc', {

    chart: {
        polar: true,
        type: 'line'
    },

    accessibility: {
        description: 'Grafico'
    },

    title: {
        text: 'Areas generales de investigación',
        x: -80
    },

    pane: {
        size: '90%'
    },

    xAxis: {
        categories: fData.array.labels ,
        tickmarkPlacement: 'off',
        lineWidth: 0
    },

    yAxis: {
         labels: {
            enabled: false
        },
        gridLineInterpolation: 'polygon',
        lineWidth: 0,
        min: 0 
    },

    tooltip: {
        shared: true,
        pointFormat: '<span style="color:{series.color}">{series.name}: <br/>'
    },

    legend: {
        align: 'right',
        verticalAlign: 'middle',
        layout: 'vertical'
    },

    series: [{
        name: 'Áreas generales',
        data: fData.array.values 
        //pointPlacement: 'off'
    }],

    responsive: {
        rules: [{
            condition: {
                maxWidth: 500
            },
            chartOptions: {
                legend: {
                    align: 'center',
                    verticalAlign: 'bottom',
                    layout: 'horizontal'
                },
                pane: {
                    size: '70%'
                }
            }
        }]
    }

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

