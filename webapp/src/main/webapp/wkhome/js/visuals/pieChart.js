'use strict';

var pieChart = angular.module('pieChart', []);
//  D3  Factory
pieChart.factory('d3', function () {
    return  d3;
});
pieChart.directive('pieChart', ["d3", "globalData", "sparqlQuery",
    function (d3, globalData, sparqlQuery) {

        //  we  will    soon    implement   this    function
        var draw = function draw(svg, width, height, entityName, data, scope) {
            var outerRadius = height / 1.7,
                    innerRadius = outerRadius / 10,
                    cornerRadius = 10;

            var pie = d3.layout.pie()
                    .padAngle(.05)
                    //.sort(null)
                    .value(function (d) {
                        return d.value;
                    });


            var radius = Math.min(width, height) / 4.0;
            var arcWidget = d3.svg.arc()
                    .padRadius(outerRadius)
                    .innerRadius(innerRadius);

            var arc = d3.svg.arc()
                    .outerRadius(radius * 0.8)
                    .innerRadius(radius * 0.4);
            var outerArc = d3.svg.arc()
                    .innerRadius(radius * 0.9)
                    .outerRadius(radius * 0.9);
            //textos externos
            var outerLabelArc = d3.svg.arc()
                    .innerRadius(radius * 1.5)
                    .outerRadius(radius * 1.5);

            //para textos directos sobre cada porcion de pie
            var labelArc = d3.svg.arc()
                    .outerRadius(radius + 20)
                    .innerRadius(radius + 20);


            var color = d3.scale.ordinal()
                    //        .range(["#F7FC63","#F7B9CB","#609FF7", "#8a89a6", "#7b6888", "#6b486b", "#a05d56", "#d0743c", "#ff8c00"]);
                    .range(["#B4BFE1", "#94A2CF", "#7888BF", "#5E71AF", "#465BA1", "#324997", "#203A90", "#112E8D", "#022189"]);
            function midAngle(d) {
                return d.startAngle + (d.endAngle - d.startAngle) / 2;
            }

            svg
                    .attr("width", width)
                    .attr("height", height);


            var g = svg.append("g");
            g.append("g")
                    .attr("class", "slices");

            g.append("g")
                    .attr("class", "labels");
            g.append("g")
                    .attr("class", "lines");
            g.attr("transform", "translate(" + (width) / 2 + "," + (height) / 2 + ")");


            var key = function (d) {
                return d.data.label;
            };

            var slice = g.select(".slices").selectAll("path.slice").data(pie(data), key);


            slice
                    .enter().append("path")

                    .style("fill", function (d) {
                        return color(d.data.label);
                    })

                    .attr("class", "slice")

                    .each(function (d) {
                        d.outerRadius = outerRadius - 40;
                    })
                    .attr("d", arcWidget)
                    .on("mouseover", function (d) {
                        d3.select('#tooltip p strong')
                                .text(d.data.label);
                        d3.select("#tooltip")
                                .style("left", d3.event.pageX + "px")
                                .style("top", d3.event.pageY + "px")
                                .style("opacity", 1)
                                .select("#value")
                                .text(d.value + ' ' + entityName + " -   Click to see " + entityName)
                    })
                    //.on("mouseout", arcTween(outerRadius - 20, 150));
                    .on("mouseout", function () {
                        // Hide the tooltip
                        d3.select("#tooltip")
                                .style("opacity", 0);
                    })

                    .on("click", function (d) {
                        var key = d.data.uri;
                         console.log ("key");
                        console.log (key);
                         console.log (d);
                          if (entityName === 'Articles' || entityName === 'Researchers')
                       {
                             window.location.href = "/#/info/statisticsbyInst/"+key;
                       }else {
                            window.location.href = "/#/info/statisticsbyInst/"+key;

                       }

                        

//                        if (entityName === 'Articles')
//                        {
//
//                        }
//                        else
                       /* if (entityName === 'Researchers')
                        {
                            scope.ifClick({value: key});
                        }
                        else  if (entityName === 'Articles' || entityName === 'Authors')
                        {
                            var sparqlquery = globalData.PREFIX
                                    + ' CONSTRUCT { '
                                    + ' uc:resultTitle a uc:pagetitle. '
                                    + ' uc:resultTitle uc:viewtitle "Authors from ' + key + ' "  .  '
                                    + ' ?subject rdfs:label ?name. '
                                    + ' ?subject uc:total ?totalPub }   '
                                    + ' WHERE {  '
                                    + ' {       '
                                    + ' SELECT ?subject ?name (COUNT(DISTINCT ?pub) AS ?totalPub)  '
                                    + ' WHERE { '
                                    + ' GRAPH <' + globalData.centralGraph + '> {'
                                    + '     ?subject foaf:publications  ?pub . '
                                    + '     ?pub dct:title ?title . '
                                    + '     ?subject foaf:name       ?name.        '
                                    + '     ?subject schema:memberOf ?provenance. '
                                    + '     { '
                                    + '         SELECT * '
                                    + '         WHERE { '
                                    + '         GRAPH <' + globalData.organizationsGraph + '> { '
                                    + '           ?provenance uc:name ?sourcename. '
                                    + '           FILTER(mm:fulltext-search(?sourcename,"'+key+'"))'
                                    + '             }'
                                    + '         } '
                                    + '     } '
                                    + ' } } '
                                    + ' GROUP BY ?subject ?name '
                                    + ' HAVING( ?totalPub > 2 && ?totalPub < 500) '
                                    + '} '
                                    + '} limit 150';
                            waitingDialog.show("Loading All Authors of Selected Source");

                            sparqlQuery.querySrv({query: sparqlquery}, function (rdf) {

                                jsonld.compact(rdf, globalData.CONTEXT, function (err, compacted) {
                                    if (compacted)
                                    {
                                        var entity = compacted["@graph"];
                                        //send data to getKeywordTag Controller
                                        scope.ifClick({value: compacted});
                                        waitingDialog.hide();
                                    }
                                    else
                                    {
                                        waitingDialog.hide();
                                    }
                                });
                            });
                        }*///end if entityName === Articles
                        return d3.event.preventDefault();
                    });
            slice.enter().append("text")
                    .attr("transform", function (d) {
                        return "translate(" + labelArc.centroid(d) + ")";
                    })
                    .attr("class", "piechart-text")
                    .attr("dy", ".35em")
                    .text(function (d) {
                        return d.data.value;
                    });
            /*
             var polyline = svg.select(".lines").selectAll("polyline")
             .data(pie(data), key);

             polyline.enter()
             .append("polyline");

             polyline.transition().duration(1000)
             .attrTween("points", function (d) {
             this._current = this._current || d;
             var interpolate = d3.interpolate(this._current, d);
             this._current = interpolate(0);
             return function (t) {
             var d2 = interpolate(t);
             var pos = outerLabelArc.centroid(d2) ;
             pos[0] = radius * 0.95 * (midAngle(d2) < Math.PI ? 1 : -1)   ;
             return [arc.centroid(d2) + 80, outerArc.centroid(d2) , pos];
             };
             });

             polyline.exit()
             .remove();

             /* ------- TEXT LABELS -------*/
            /*
             var text = svg.select(".labels").selectAll("text")
             .data(pie(data), key);

             text.enter()
             .append("text")
             .attr("dy", ".35em")
             .text(function (d) {
             return d.data.value > 150 ? d.data.label + "(" + d.data.value +")" : "";
             });

             function midAngle(d) {
             return d.startAngle + (d.endAngle - d.startAngle) / 3;
             }

             text.transition().duration(1000)
             .attrTween("transform", function (d) {
             this._current = this._current || d;
             var interpolate = d3.interpolate(this._current, d);
             this._current = interpolate(0);
             return function (t) {
             var d2 = interpolate(t);
             var pos = outerLabelArc.centroid(d2);
             pos[0] = radius * (midAngle(d2) < Math.PI ? 1 : -1);
             return "translate(" + pos + ")";
             };
             })
             .styleTween("text-anchor", function (d) {
             this._current = this._current || d;
             var interpolate = d3.interpolate(this._current, d);
             this._current = interpolate(0);
             return function (t) {
             var d2 = interpolate(t);
             return midAngle(d2) < Math.PI ? "start" : "end";
             };
             });

             text.exit()
             .remove();
             */

            function arcTween(outerRadius, delay) {
                return function () {
                    var i = d3.interpolate(d.outerRadius, outerRadius);
                    return function (t) {
                        d.outerRadius = i(t);
                        return arc(d);
                    };
                };
            }
        };

        return {
            restrict: 'E',
            scope: {
                data: '=',
                'ifClick': "&"
            },
            compile: function (element, attrs, transclude) {
                //  Create  a   SVG root    element
                var svg = d3.select(element[0]).append("svg");
                //var width = 960, height = 500;
                var elementWidth = parseInt(element.css('width'));
                var width = attrs.pcWidth ? attrs.pcWidth : elementWidth,
                        height = attrs.pcHeight;
                //  Return  the link    function
                return  function (scope, element, attrs) {
                    //  Watch   the data    attribute   of  the scope
                    scope.$watch('data', function (newVal, oldVal, scope) {
                        //  Update  the chart
                        var data = scope.data;
                        if (data) {
                            var entityName = data.entityName;
                            data = data.data;
                            draw(svg, width, height, entityName, data, scope);
                        }
                    }, true);

                };
            }
        };
    }]);
