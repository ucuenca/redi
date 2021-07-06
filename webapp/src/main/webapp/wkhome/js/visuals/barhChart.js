'use strict';
//Author of this View using D3: http://bl.ocks.org/NPashaP/96447623ef4d342ee09b

//var dateLineChart = angular.module('statsInst', []);
//  D3  Factory
//dateLineChart.factory('d3', function () {
 //   return  d3;
//});
wkhomeApp.directive('barhChart', ["d3", "globalData", "sparqlQuery",
    function (d3, globalData, sparqlQuery) {


        function draw(id, fData  ,labely ) {



//Highcharts.theme = {};

//Highcharts.setOptions(Highcharts.theme);

//console.log (Highcharts.theme);
Highcharts.chart( id , {
    chart: {
        type: 'column'
    },
    title: {
        text: ''
    },
    subtitle: {
        text: ''
    },
    xAxis: {
        categories: fData.ax 

    },
    yAxis: {
        title: {
            text: labely
        }
    },
    plotOptions: {
        line: {
            dataLabels: {
                enabled: true,
               style: {
                fontSize: '10px',
                fontWeight: 'bold',
                color : '#1a1a1a'
            }
            },
            enableMouseTracking: true
        }
    }, credits: {
        enabled: false
    },
    series: [{
        name: labely,
        data: fData.ay
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
                         //  Watch   the data    attribute   of  the scope
                    scope.$watch('data', function (newVal, oldVal, scope) {
                        //  Update  the chart
                        console.log (scope);
                        console.log (element);
                         console.log (attrs);
                        var data = scope.data.datos;
                        console.log ("data");
                        console.log (data);
                        console.log (scope.data.container);
                            // draw(svg, data, w, h);

                        if (data) {
                            draw(scope.data.container , data , scope.data.axisy );
                        }
                    }, true);

                };
            }
        };
    }]);
