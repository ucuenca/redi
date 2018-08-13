'use strict';
var cloudGroup = angular.module('mapView', []);
//  D3  Factory
cloudGroup.factory('d3', function () {
    return  d3;
});
cloudGroup.directive('mapView', ["d3", 'globalData', 'sparqlQuery',
    function (d3, globalData, sparqlQuery) {
         // drawResourcesOnMap(null , null , null);

        function drawResourcesOnMap(data, element, scope, map)
        {  
           var urlmap = "countries/"+map+"/"+map+"-all";
          // var maps = { "ec": "countries/ec/ec-all",  "cr": "countries/cr/cr-all"};

 //console.log ("Entra MAPA");
 // console.log (map);
 //console.log (maps[map]);
 //             console.log (data);
             
/*var datos = [{
            name: "Quito",
            fullName : "EPN" ,
            lat: -0.21,
            lon: -78 ,
            z : 20
        }, {
            name: 'Cuenca',
            fullName : "UPS" ,
            lat: -2.9,
            lon: -79 ,
            z : 100
        }, {
            name: 'Cuenca',
            fullName : "UCUENCA" ,
            lat: -2.98,
            lon: -79.2 ,
            z : 75
        }];*/
 console.log (urlmap);
 var description = "";
$( "select  option:selected" ).each( function( i, el ) {
    var elem = $( el );
     if ((elem).val().length > 1) {
    description =  elem.text()+ " - " + description ;
    }
});

         var cities = data;
// Initiate the chart
Highcharts.mapChart('containermap', {

    chart: {
        map: urlmap ,
        height: 500
    },

    title: {
        text: description
    },

    mapNavigation: {
        enabled: true
    },

    tooltip: {
        headerFormat: '',
        pointFormat: '<b>{point.name}</b><br>Lat: {point.lat}, Lon: {point.lon}'
    }, plotOptions: {
        series: {  cursor: 'pointer',
            point: {
                events: {
                    mouseOver: function (e) {
                        var d = this;
                        console.log (d);
                        console.log (this);
                            d3.select("h3.tag").text(d.keyword);
                            d3.select("h3.name").text(d.name);
                            d3.select("h3.fullname").text(d.fullname);
                            d3.select("h3.city").text(d.city);
                            d3.select("h3.province").text(d.province);
                            d3.select("h3.total").text(d.z);
                            d3.select("h3.latitude").text(d.lat);
                            d3.select("h3.longitude").text(d.lon);

                    }
                }
            },
            events: {
                mouseOut: function (d, e) {
                }
            }
        }
    } ,

    series: [ {
        // Use the gb-all map with no data as a basemap
        name: 'Basemap',
        borderColor: '#A0A0A0',
        nullColor: 'rgba(200, 200, 200, 0.3)',
        showInLegend: false
    }, {
        name: 'Separators',
        type: 'mapline',
        nullColor: '#707070',
        showInLegend: false,
        enableMouseTracking: false
    }, {
            name: 'Province',
            color: '#E0E0E0',
            enableMouseTracking: false
        }, {
        // Specify points using lat/lon
        type: 'mapbubble',
        name: 'IES',
        color: Highcharts.getOptions().colors[2],
        data: cities , minSize: 10,
            maxSize: '8%',
            tooltip: {
                pointFormat: ' <strong>{point.name}</strong>: {point.z} authors' ,
                fontSize: '25px'
            }
    }]
});

                
            d3.selectAll(".highcharts-negative")
            .on("mouseover", function (d, i) {
            var dat = d3.select(this).attr("d").split(" ");
               var newatr = "";
               for (var  i=0 ; i < dat.length;i++){
                if (i == 4 || i == 5){
                dat[i] = parseInt(dat[i])+10; 
                }
                  newatr =newatr +" " + dat[i] ;
               }
               d3.select(this)
                        .attr("a", d3.select(this).attr("d") );
                        
                d3.select(this).transition()
                        .duration(250)
                        .attr("d", newatr.trim() );
            })
            .on("mouseout", function (d, i) { console.log (this);
               
                d3.select(this).transition()
                       // .duration(50)
                        .attr("d", d3.select(this).attr("a") );
            });

        }
        return {
            restrict: 'E',
             scope : true ,
            compile: function (element, attrs, transclude) {
                //  Create  a   SVG root    element
                var svg = null;
                var data = [];

                //  Return  the link    function
                return  function (scope, element, attrs) {
                    console.log ("Scope");
                     console.log (scope);
                     var map = scope.datamap;
                      console.log (map);
                    scope.$watch( 'datamap', function (newVal, oldVal, scope) {
                      map = scope.datamap;
                      console.log (map);
                       if (scope.data != undefined) {
                        data = scope.data;  
                      } 
                     drawResourcesOnMap(data, svg, scope , map);
                     },true);
                    scope.$watch('data', function (newVal, oldVal, scope) {
                          map = scope.datamap;
                        if (scope.data && scope.data[0])
                        {  
                            drawResourcesOnMap(scope.data, svg, scope , map);
                        }
                    }, true);
                };
            }
        };
    }]);

