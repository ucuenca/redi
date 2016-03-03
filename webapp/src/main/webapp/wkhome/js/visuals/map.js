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
            var w = 450,
                    h = 600;
            var projection = d3.geo.azimuthal()
                    //  .mode("equidistant")
                    .origin([-84, -1])//lat long origin
                    .scale(4500)
                    .translate([-250, 230]);
            var path = d3.geo.path()
                    .projection(projection);
            //var svg = d3.select("#map").insert("svg:svg", "h2")

            // Define the zoom function for the zoomable map

            function clicked(d) {
                var x, y, k;

                if (d && centered !== d) {
                    var centroid = path.centroid(d);
                    x = centroid[0];
                    y = centroid[1];
                    k = 4;
                    centered = d;
                } else {
                    x = width / 2;
                    y = height / 2;
                    k = 1;
                    centered = null;
                }

                g.selectAll("path")
                        .classed("active", centered && function (d) {
                            return d === centered;
                        });

                g.transition()
                        .duration(750)
                        .attr("transform", "translate(" + w / 2 + "," + h / 2 + ")scale(" + k + ")translate(" + -x + "," + -y + ")")
                        .style("stroke-width", 1.5 / k + "px");
            }


            var svg = element.insert("svg:svg", "h2")
                    .attr("width", w)
                    .attr("height", h)
                    .on("click", clicked);
            var states = svg.append("svg:g")
                    .attr("id", "states");
            var circles = svg.append("svg:g")
                    .attr("id", "circles");
            var cells = svg.append("svg:g")
                    .attr("id", "cells");
            d3.select("input[type=checkbox]").on("change", function () {
                cells.classed("voronoi", this.checked);
            });
            d3.json("../resources/ec-states.geojson", function (collection) {
                states.selectAll("path")
                        .data(collection.features)
                        .enter().append("svg:path")
                        .attr("d", path);
            });

            draw(data);
            function draw(cities) {
                var positions = [];
                cities = cities.filter(function (city) {
                    //             if (countByAirport[city.id]) {
                    var location = [+city.longitude, +city.latitude];
                    //          locationByAirport[city.id] = location;
                    positions.push(projection(location));
                    return true;
                    //            }
                });
                // Compute the Voronoi diagram of airports' projected positions.


                var polygons = d3.geom.voronoi(positions);
                var g = cells.selectAll("g")
                        .data(cities)
                        .enter().append("svg:g");

                var tip = d3.tip()
                        .attr('class', 'tree-d3-tip')
                        .html(function (d) {
                            return ' ';
                        });
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
                                    .attr("r", Math.sqrt(d.total * 3));
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
                                    + '         FILTER (regex(?key,"' + d.keyword + '")). '
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
                                    + '                                 ?provenance uc:name "' + d.name + '"'
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

