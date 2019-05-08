'use strict';
//Author of this View using D3: http://bl.ocks.org/NPashaP/96447623ef4d342ee09b

//var dateLineChart = angular.module('statsInst', []);
//  D3  Factory
//dateLineChart.factory('d3', function () {
 //   return  d3;
//});
wkhomeApp.directive('tagCanvas', ["d3", "globalData", "sparqlQuery",
    function (d3, globalData, sparqlQuery) {


        function draw(id, fData, w, h) {

             //var countries = ['United States', 'Canada', 'Argentina', 'Armenia'];
             var listk =  fData;
             var cList = $('#tags ul');
             for ( var i = 0 ; i< listk.length ;i++)
             {  var  newelement = $("<li><a href='blanck'>"+listk[i]+"</a></li>");

               cList.append(newelement);
               //console.log (li);
           // console.log (aaa);
            }



          TagCanvas.Start('myCanvas', 'tags', {
                    textColour: '#000000',
                    outlineColour: '#ff00ff',
                    reverse: true,
                    depth: 1.0,
                    weight: true,
                    weightFrom: 'data-weight',
                    initial: [0.0, -0.05],
                    maxSpeed: 0.05,
                    shadow: '#ccf',
                    shadowBlur: 3
                });


 
        }
        return {
            restrict: 'E',
            scope: {
                datacanvas: '=',
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
                    scope.$watch('datacanvas', function (newVal, oldVal, scope) {
                        //  Update  the chart

                        var data = scope.datacanvas;
                            // draw(svg, data, w, h);
                         console.log ("Dibujar"+data);
                        if (data) {
                            draw(svg, data, w, h);
                        }
                    }, true);

                };
            }
        };
    }]);
