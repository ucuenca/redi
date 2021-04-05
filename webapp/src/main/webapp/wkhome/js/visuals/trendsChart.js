'use strict';
//Author of this View using D3: http://bl.ocks.org/NPashaP/96447623ef4d342ee09b

//var dateLineChart = angular.module('statsInst', []);
//  D3  Factory
//dateLineChart.factory('d3', function () {
 //   return  d3;
//});
wkhomeApp.directive('trendsChart', ["d3", "globalData", "sparqlQuery",  
    function (d3, globalData, sparqlQuery  ) {

        var seleccionado = "";



        function draw(id, fData, w, h) {

        var colors = Highcharts.getOptions().colors;
        console.log ("Dibujar" , fData);
        var dy = fData.datay;
         console.log ("Dibujar" , dy );
        var series = [];
         console.log ("Dibujar" , fData.datax ); 
         var da;
         var num = 0; 
        for ( da in fData.datax  ) {
            console.log (da);
             console.log (fData.datax[da]);
         var object = {
            'name': da,
            'data': fData.datax[da] ,
            'website' : "http://dbpedia.org/resource/Artificial_intelligence",
            'color' : colors[num]
        }       
        series.push (object); 
        num = num +1;

        }
        console.log ("series");
        console.log (series);

//Highcharts.theme = {};

//Highcharts.setOptions(Highcharts.theme);

//console.log (Highcharts.theme);


Highcharts.chart('container', {
    chart: {
        type: 'spline'
    },

    legend: {
        symbolWidth: 40
    },

    title: {
        text: 'Tendencias por áreas'
    },

    subtitle: {
        text: 'Número de publicaciones en los ultimos 10 años'
    },

    yAxis: {
        title: {
            text: 'Numero de publicaciones'
        },
        accessibility: {
            description: 'Numero de publicaciones'
        }
    },

    xAxis: {
        title: {
            text: 'Años'
        },
        accessibility: {
            description: 'Time from December 2010 to September 2019'
        },
       // categories: ['2010', '2011', '2012', '2013', '2014', '2015']
        categories: dy
    },

    tooltip: {
        valueSuffix: ''
    },

    plotOptions: {
        series: {
            point: {
                events: {
                    click: function () {
                        //window.location.href = this.series.options.website;
                        //$scope.areaCombo.selected.id = this.series.options.website;
                        //console.log (this);
                        //seleccionado = this.series.options.website;
                    }
                }
            },
            cursor: 'pointer'
        }
    },
        series : series
    /*series: [
        {
            name: 'Matematicas',
            data: [34.8, 43.0, 51.2, 41.4, 64.9, 72.4],
            website: 'https://www.nvaccess.org',
            color: colors[2],
            accessibility: {
                description: 'This is the most used screen reader in 2019'
            }
        }, {
            name: 'Biologia',
            data: [69.6, 63.7, 63.9, 43.7, 66.0, 61.7],
            website: 'https://www.freedomscientific.com/Products/Blindness/JAWS',
            dashStyle: 'ShortDashDot',
            color: colors[0]
        }, {
            name: 'Geografia',
            data: [20.2, 30.7, 36.8, 30.9, 39.6, 47.1],
            website: 'http://www.apple.com/accessibility/osx/voiceover',
            dashStyle: 'ShortDot',
            color: colors[1]
        }, {
            name: 'Economia',
            data: [null, null, null, null, 21.4, 30.3],
            website: 'https://support.microsoft.com/en-us/help/22798/windows-10-complete-guide-to-narrator',
            dashStyle: 'Dash',
            color: colors[9]
        }, {
            name: 'Ciencia de las computación',
            data: [6.1, 6.8, 5.3, 27.5, 6.0, 5.5],
            website: 'http://www.zoomtext.com/products/zoomtext-magnifierreader',
            dashStyle: 'ShortDot',
            color: colors[5]
        }, {
            name: 'Historia',
            data: [42.6, 51.5, 54.2, 45.8, 20.2, 15.4],
            website: 'http://www.disabled-world.com/assistivedevices/computer/screen-readers.php',
            dashStyle: 'ShortDash',
            color: colors[3]
        }
    ]*/,

    responsive: {
        rules: [{
            condition: {
                maxWidth: 550
            },
            chartOptions: {
                chart: {
                    spacingLeft: 3,
                    spacingRight: 3
                },
                legend: {
                    itemWidth: 150
                },
                xAxis: {
                    categories: ['Dec. 2010', 'May 2012', 'Jan. 2014', 'July 2015', 'Oct. 2017', 'Sep. 2019'],
                    title: ''
                },
                yAxis: {
                    visible: false
                }
            }
        }]
    }
});


 
        }
        return {
            restrict: 'E',
            scope: {
                datacl: '='
            },
            compile: function (element, attrs, transclude) {
                //  Create  a   SVG root    element

                var svg = d3.select(element[0]);
                //var svg = d3.select("svg");
                //var width = 960, height = 500;
                var elementWidth = parseInt(element.css('width'));
                var width = attrs.pcWidth ? attrs.pcWidth : elementWidth,
                        height = attrs.pcHeight;
                var w = element.parent()[0].offsetWidth - 0.01*(element.parent()[0].offsetWidth),
                    h = (2 * w) / 7;
                //  Return  the link    function
                return  function (scope, element, attrs) {

                    //  Watch   the data    attribute   of  the scope
                    scope.$watch('datacl', function (newVal, oldVal, scope) {
                        //  Update  the chart
                        console.log ("DATACL");
                        console.log (scope.datacl);
                        var data = scope.datacl;

                        //var data = 1;
                            // draw(svg, data, w, h);
                        

                        if (data) {
                            draw(svg, data.data , w, h);
                        }
                    }, true);

                };
            }
        };
    }]);
