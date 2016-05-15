'use strict';
var cloudGroup = angular.module('mapView', []);
//	D3	Factory
cloudGroup.factory('d3', function () {
    return	d3;
});
cloudGroup.directive('mapView', ["d3", 'globalData', 'sparqlQuery',
    function (d3, globalData, sparqlQuery) {

        function drawResourcesOnMap(data, element, scope)
        {
            var pubInfo = element;
            pubInfo.html('');
            var cities = data;

            var w = 500;
            var h = 600;
            var proj = d3.geo.mercator().scale(28500).translate([6400, 110]);
            var path = d3.geo.path().projection(proj);
            var t = proj.translate(); // the projection's default translation
            var s = proj.scale(); // the projection's default scale
//                    .origin([-84, -1])//lat long origin
//                    .scale(4500)
//                    .translate([-250, 230]);

            var positions = [];
            cities = cities.filter(function (city) {
                //             if (countByAirport[city.id]) {
                var location = [+city.longitude, +city.latitude];
                //          locationByAirport[city.id] = location;
                positions.push(proj(location));
                return true;
                //            }
            });

            var tip = d3.tip()
                    .attr('class', 'tree-d3-tip')
                    .html(function (d) {
                        return ' ';
                    });

            var map = element.append("svg:svg")
                    .attr("width", w)
                    .attr("height", h)
                    .call(d3.behavior.zoom().on("zoom", redraw));

            var axes = map.append("svg:g").attr("id", "axes");

            var xAxis = axes.append("svg:line")
                    .attr("x1", t[0])
                    .attr("y1", 0)
                    .attr("x2", t[0])
                    .attr("y2", h);

            var yAxis = axes.append("svg:line")
                    .attr("x1", 0)
                    .attr("y1", t[1])
                    .attr("x2", w)
                    .attr("y2", t[1]);

            var states = map.append("svg:g").attr("id", "states");

            var circles = map.append("svg:g")
                    .attr("id", "circles");
            circles.selectAll("circle")
                    .data(cities)
                    .enter().append("svg:circle")
                    .call(cities ? tip : function () {
                    })
                    .attr("cx", function (d, i) {
                        return positions[i][0];
                    })
                    .attr("cy", function (d, i) {
                        return positions[i][1];
                    })
                    .attr("r", function (d, i) {
                        return Math.sqrt(d.total * 10);
                    })


            var cells = map.append("svg:g")
                    .attr("id", "cells");

            d3.select("input[type=checkbox]").on("change", function () {
                cells.classed("voronoi", this.checked);
            });


            d3.json("../resources/ec-states.geojson", function (json) {
                states.selectAll("path")
                        .data(json.features)
                        .enter().append("svg:path")
                        .attr("d", path);
            });

            function redraw() {
                // d3.event.translate (an array) stores the current translation from the parent SVG element
                // t (an array) stores the projection's default translation
                // we add the x and y vales in each array to determine the projection's new translation
                var tx = t[0] * d3.event.scale + d3.event.translate[0];
                var ty = t[1] * d3.event.scale + d3.event.translate[1];

                proj.translate([tx, ty]);

                // now we determine the projection's new scale, but there's a problem:
                // the map doesn't 'zoom onto the mouse point'
                proj.scale(s * d3.event.scale);

                // redraw the map
                states.selectAll("path").attr("d", path);

                // redraw the points 
                circles.selectAll("circle")
                        .attr("cx", function (d, i) {
                            return t[0] + d3.event.translate[0] + (positions[i][0] * d3.event.scale) - 6400;
                        })
                        .attr("cy", function (d, i) {
                            return t[1] + d3.event.translate[1] + (positions[i][1] * d3.event.scale) - 110;
                        });

                // redraw the x axis
                xAxis.attr("x1", tx).attr("x2", tx);

                // redraw the y axis
                yAxis.attr("y1", ty).attr("y2", ty);
            }




            draw(data);
            function draw(cities) {

                var g = cells.selectAll("g")
                        .data(cities)
                        .enter().append("svg:g");
                circles.selectAll("circle")
                        .on("mouseover", function (d, i) {
                            d3.select("h3.tag").text(d.keyword);
                            d3.select("h3.name").text(d.name);
                            d3.select("h3.fullname").text(d.fullname);
                            d3.select("h3.city").text(d.city);
                            d3.select("h3.province").text(d.province);
                            d3.select("h3.total").text(d.total);
                            d3.select("h3.latitude").text(d.latitude);
                            d3.select("h3.longitude").text(d.longitude);
                            d3.select(this).transition()
                                    .duration(750)
                                    .attr("r", Math.sqrt(d.total * 2) + 16);

                            tip.html("<h2>" + d.keyword + "</h2> <br>  <h3>" + d.name + "</h3> <br>Click to See Authors Within This Research Area");
                            tip.show(d);
                        })
                        .on("mouseout", function (d, i) {
                            d3.select(this).transition()
                                    .duration(750)
                                    .attr("r", Math.sqrt(d.total * 10));
                            tip.hide(d);
                        })
                        .on("click", function (d, i) {
                            var sparqlquery = globalData.PREFIX
                                    + ' CONSTRUCT { <http://ucuenca.edu.ec/wkhuska/resultTitle> a uc:pagetitle. <http://ucuenca.edu.ec/wkhuska/resultTitle> uc:viewtitle "Authors from ' + d.name + ' , taking place in ' + d.keyword + ' field" . ?subject rdfs:label ?name. ?subject uc:total ?totalPub }   '
                                    + ' WHERE {  '
                                    + '     SELECT ?subject ?totalPub ?name '
                                    + '     WHERE { '
                                    + '         ?subject foaf:publications ?pubb. '
                                    //+ '         ?subject bibo:Quote "' + d.keyword + '". '
                                    + '         ?subject dct:subject ?key . '
                                    + '         FILTER (mm:fulltext-search(?key,"' + d.keyword + '")). '
                                    + '         { '
                                    + '         SELECT ?subject ?name (COUNT( DISTINCT ?pub) AS ?totalPub)  '
                                    + '             WHERE { '
                                    + '                 GRAPH <' + globalData.centralGraph + '>  { '
                                    + '                     ?subject foaf:publications  ?pub . '
                                    + '                     ?subject foaf:name       ?name.        '
                                    + '                     ?subject dct:provenance ?provenance. '
                                    + '                     {  '
                                    + '                         SELECT * WHERE { '
                                    + '                             GRAPH <' + globalData.endpointsGraph + '>  { '
                                    + '                                 ?provenance uc:name ?sourcename. '
                                    + '                                 FILTER(mm:fulltext-search(?sourcename,"'+d.name+'"))' 
                                    + '                             } '
                                    + '                         } '
                                    + '                     } '
                                    + '                 } '
                                    + '             }  '
                                    + '         GROUP BY ?subject ?name '
                                    + '         } '
                                    + '     }'
                                    + ' }  ';
                            waitingDialog.show("Loading Authors Related with " + d.keyword);
                            sparqlQuery.querySrv({query: sparqlquery}, function (rdf) {
                                jsonld.compact(rdf, globalData.CONTEXT, function (err, compacted) {
                                    if (compacted)
                                    {
                                        var entity = compacted["@graph"];
                                        //var final_entity = _.where(entity, {"@type": "bibo:Document"});
                                        var values = entity.length ? entity : [entity];
                                        //send data to getKeywordTag Controller
                                        scope.ifClick({value: compacted});
                                        waitingDialog.hide();
                                    }
                                    else
                                    {
                                        waitingDialog.hide();
                                    }
                                });
                            });   // end  sparqlQuery.querySrv(...
                        });
                g.append("svg:path")
                        .attr("class", "cell")
                        .attr("d", function (d, i) {
                            //                  return "M" + polygons[i].join("L") + "Z";
                        })
                        .on("mouseover", function (d, i) {
                            d3.select("h3.name").text(d.name);
                        });
            }
        }
        return {
            restrict: 'E',
            scope: {
                data: '=',
                'ifClick': "&"
            },
            compile: function (element, attrs, transclude) {
                //	Create	a	SVG	root	element
                var svg = d3.select(element[0]);
                //var width = 960, height = 500;
                var elementWidth = parseInt(element.css('width'));
                var elementHeight = parseInt(element.css('height'));
                var width = attrs.ctWidth ? attrs.ctWidth : elementWidth;
                var height = attrs.ctHeight ? attrs.ctHeight : elementHeight;
                //	Return	the	link	function
                return	function (scope, element, attrs) {
                    //	Watch	the	data	attribute	of	the	scope
                    scope.$watch('data', function (newVal, oldVal, scope) {
                        if (scope.data && scope.data[0])
                        {
                            drawResourcesOnMap(scope.data, svg, scope);
                        }
                    }, true);
                };
            }
        };
    }]);

