'use strict';
//Author of this View using D3: http://bl.ocks.org/NPashaP/96447623ef4d342ee09b

var barChart = angular.module('barChart', []);
//  D3  Factory
barChart.factory('d3', function () {
    return  d3;
});
barChart.directive('barChart', ["d3", "globalData", "sparqlQuery",
    function (d3, globalData, sparqlQuery) {


        function draw(id, fData, w, h) {


    //dynamic heigth

    if (fData.categories.length > 34) {
    var chartHeight = 500+7*fData.categories.length;
    var typegraph = "bar" ;
    }else {
    var chartHeight = 500;
    var typegraph = "column";
    }
     console.log (fData);

     function segColor(c) {
                return {Autores: "#807dba", Publicaciones: "#e08214", Salud: "#41ab5d"}[c];
            }

    function legend(lD) {
                var leg = {};
                var  element = d3.select("#tablaTotales") ;

                // create table for legend.
                var legend = element.append("table").attr('class', 'legend');

                // create one row per segment.
                var tr = legend.append("tbody").selectAll("tr").data(lD).enter().append("tr");

                // create the first column for each segment.
                tr.append("td").append("svg").attr("width", '16').attr("height", '16').append("rect")
                        .attr("width", '16').attr("height", '16')
                        .attr("fill", function (d) {
                            return segColor(d.type);
                        });

                // create the second column for each segment.
                tr.append("td").text(function (d) {
                    return d.type;
                });

                // create the third column for each segment.
                tr.append("td").attr("class", 'legendFreq')
                        .text(function (d) {
                            return d3.format(",")(d.freq);
                        });
               }
             /**
 * (c) 2010-2017 Torstein Honsi
 *
 * License: www.highcharts.com/license
 *
 * Grid-light theme for Highcharts JS
 * @author Torstein Honsi
 */
 /*
  */
        var lD = [{type: "Autores", freq:fData.total.totalAuthors } , { type: "Publicaciones", freq:fData.total.totalPub }];
/* global document */
// Load the fonts
Highcharts.createElement('link', {
    href: 'https://fonts.googleapis.com/css?family=Dosis:400,600',
    rel: 'stylesheet',
    type: 'text/css'
}, null, document.getElementsByTagName('head')[0]);

Highcharts.theme = {
    colors: ['#e08214', '#807dba', '#7cb5ec', '#7798BF', '#aaeeee', '#ff0066',
        '#eeaaee', '#55BF3B', '#DF5353', '#7798BF', '#aaeeee'],
    chart: {
        backgroundColor: null,
        style: {
           // fontFamily: 'Dosis, sans-serif'
        }
    },
    title: {
        style: {
            fontSize: '18px',
          //  fontWeight: 'bold',
            color : '#678C03' ,
           // textTransform: 'uppercase'
        }
    },
    tooltip: {
        borderWidth: 0,
        backgroundColor: 'rgba(219,219,216,0.8)',
        shadow: false
    },
    legend: {
        itemStyle: {
            fontWeight: 'bold',
            fontSize: '13px'
        }
    },
    xAxis: {
        gridLineWidth: 0,
        labels: {
            style: {
                fontSize: '12px',
             //   fontWeight: 'bold',
                color : '#1a1a1a'
            }
        }
    },
    yAxis: {
     //   minorTickInterval: 'auto',
       //  type: 'logarithmic',
     //   minorTickInterval: 0.1 ,

        title: {
            style: {
                textTransform: 'uppercase'
            }
        },
        labels: {
            style: {
                fontSize: '12px'
            }
        }
    },
    plotOptions: {
        candlestick: {
            lineColor: '#FFFFFF'
        },
        series: {
   // stacking: 'normal',
    borderWidth: 0
  }
    },


    // General
    //background2: '#F0F0EA'#F0F0EA
background2: '#F0F0EA'
};

// Apply the theme
Highcharts.setOptions(Highcharts.theme);

            Highcharts.chart('containerbar', {
    chart: {
        type: typegraph ,
        height: chartHeight
       // type: 'column'
    },
    title: {
        text: 'Total de Investigadores y Publicaciones en el Repositorio'
    },
    subtitle: {
        text: 'Todas las universidades'
    },
    xAxis: {
        categories: fData.categories ,
     //   categories : cat ,
        title: {
            text: null
        }
    },
    yAxis: {
         min: 1,
         type: 'logarithmic',
         stackLabels: {
            enabled: true,
            align: 'center'
        } ,
        title: {
           // text: '',
           // align: 'high'
        },
        labels: {
            overflow: 'justify'
        }
    },
    tooltip: {
        valueSuffix: ' '
    },
    plotOptions: {
        bar: {
            dataLabels: {
                enabled: true
            }
        }
    },
    legend: {
        layout: 'vertical',
        align: 'right',
        verticalAlign: 'top',
        x: -40,
        y: 80,
        floating: true,
        borderWidth: 1,
        //backgroundColor: ((Highcharts.theme && Highcharts.theme.legendBackgroundColor) || '#FFFFFF'),
        backgroundColor: ((Highcharts.theme && Highcharts.theme.legendBackgroundColor) || '#CCCCCC'),
        shadow: true
    },
    credits: {
        enabled: false
    },
    series: fData.Data 
});
      if ($("table").length < 2){
      legend(lD);
             }
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
