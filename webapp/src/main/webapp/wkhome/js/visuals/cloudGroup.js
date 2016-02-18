'use strict';

var cloudGroup = angular.module('cloudGroup', []);
//	D3	Factory
cloudGroup.factory('d3', function () {
    return	d3;
});
cloudGroup.directive('cloudGroup', ["$routeParams", "d3", 'sparqlQuery', 'globalData',
    function ($routeParams, d3, sparqlQuery, globalData) {
        var group = '';
        var size = '';
        var color = '';
        function create(svgElement, dataToDraw, groupByOption, scope) {


            var colors = {
                exchange: {
                    NYSE: 'red',
                    NASDAQ: 'orange',
                    TSX: 'blue',
                    'TSX-V': 'green'
                },
                volumeCategory: {
                    Top: 'mediumorchid',
                    Middle: 'cornflowerblue',
                    Bottom: 'gold'
                },
                lastPriceCategory: {
                    Top: 'aqua',
                    Middle: 'chartreuse',
                    Bottom: 'crimson'
                },
                standardDeviationCategory: {
                    Top: 'slateblue',
                    Middle: 'darkolivegreen',
                    Bottom: 'orangered'
                },
                default: '#5882FA'
            };

            var radius = 350;
            var width = Math.max(document.documentElement.clientWidth, window.innerWidth || 0);
            var height = Math.max(document.documentElement.clientHeight, window.innerHeight || 0);
            var fill = d3.scale.ordinal().range(['#FF00CC', '#FF00CC', '#00FF00', '#00FF00', '#FFFF00', '#FF0000', '#FF0000', '#FF0000', '#FF0000', '#7F0000']);
            //var svg = d3.select("#chart").append("svg")
            var pubInfo = svgElement;
            pubInfo.html('');
            var svg = svgElement.append("svg")
                    .attr("width", width)
                    .attr("height", height);


            var dataMapping = getDataMapping(dataToDraw, size);

            var padding = 8;
            var maxRadius = d3.max(_.pluck(dataMapping, 'radius'));

            /*var maximums = {
                volume: d3.max(_.pluck(dataMapping, 'volume')),
                lasPrice: d3.max(_.pluck(dataMapping, 'lastPrice')),
                standardDeviation: d3.max(_.pluck(dataMapping, 'standardDeviation'))
            };*/

            var getCenters = function (vname, size) {
                var centers, map;
                centers = _.uniq(_.pluck(dataMapping, vname)).map(function (d) {
                    return {name: d, value: 1};
                });

                map = d3.layout.treemap().size(size).ratio(1 / 1);
                map.nodes({children: centers});

                return centers;
            };

            var nodes = svg.selectAll("circle")
                    .data(dataMapping);

            nodes.enter().append("circle")
                    .attr("class", "node")
                    .attr("cx", function (d) {
                        return d.x;
                    })
                    .attr("cy", function (d) {
                        return d.x;
                    })
                    .attr("r", function (d) {
                        return d.radius;
                    })
                    .style("fill", function (d, i) {
                        return colors['default'];
                    })
                    .on("mouseover", function (d) {
                        showPopover.call(this, d);
                    })
                    .on("mouseout", function (d) {
                        removePopovers();
                    })
                    .on("click", click);
                    ;

            function getDataMapping(dataM, vname) {
                var max = d3.max(_.pluck(dataM, vname));
                var newData = dataM;
                for (var j = 0; j < dataM.length; j++) {
                    newData[j].radius = (vname != '') ? radius * (dataM[j][vname] / max) : 8;
                    newData[j].x = dataM[j].x ? dataM[j].x : Math.random() * width;
                    newData[j].y = dataM[j].y ? dataM[j].y : Math.random() * height;
                    /*newData[j].volumeCategory = getCategory('volume', dataM[j]);
                    newData[j].lastPriceCategory = getCategory('lastPrice', dataM[j]);
                    newData[j].standardDeviationCategory = getCategory('standardDeviation', dataM[j]);*/
                }

                return newData;
            }

            /*function getCategory(type, d) {
                var max = d3.max(_.pluck(dataMapping, type));
                var val = d[type] / max;

                if (val > 0.4)
                    return 'Top';
                else if (val > 0.1)
                    return 'Middle';
                else
                    return 'Bottom';
            }*/

//        $('#board').change(function() {
//          $('#chart').empty();
//
//          start(this.value);
//        });

            $('#group').change(function () {
                group = this.value;
                draw(group);
            });

            $('#size').change(function () {
                var val = this.value;
                var max = d3.max(_.pluck(dataMapping, val));

                d3.selectAll("circle")
                        .data(getDataMapping(dataMapping, this.value))
                        .transition()
                        .attr('r', function (d, i) {
                            return val ? (radius * (dataMapping[i][val] / max)) : 15
                        })
                        .attr('cx', function (d) {
                            return d.x
                        })
                        .attr('cy', function (d) {
                            return d.y
                        })
                        .duration(2000);

                size = this.value;

                force.start();
            });

            $('#color').change(function () {
                color = this.value;
                changeColor(this.value);
            });


            function changeColor(val) {
                console.log(val);
                d3.selectAll("circle")
                        .transition()
                        .style('fill', function (d) {
                            return val ? colors[val][d[val]] : colors['default']
                        })
                        .duration(1000);

                $('.colors').empty();
                if (val) {
                    for (var label in colors[val]) {
                        $('.colors').append('<div class="col-xs-1 color-legend" style="background:' + colors[val][label] + ';">' + label + '</div>')
                    }
                }
            }


            var force = d3.layout.force();

            changeColor(color);
            draw(groupByOption);

            function draw(varname) {

                var centers = getCenters(varname, [width, height]);
                force.on("tick", tick(centers, varname));
                labels(centers)
                force.start();

            }

            function tick(centers, varname) {
                var foci = {};
                for (var i = 0; i < centers.length; i++) {
                    foci[centers[i].name] = centers[i];
                }
                return function (e) {
                    for (var i = 0; i < dataMapping.length; i++) {
                        var o = dataMapping[i];
                        var f = foci[o[varname]];
                        o.y += ((f.y + (f.dy / 2)) - o.y) * e.alpha;
                        o.x += ((f.x + (f.dx / 2)) - o.x) * e.alpha;
                    }
                    nodes.each(collide(.11))
                            .attr("cx", function (d) {
                                return d.x;
                            })
                            .attr("cy", function (d) {
                                return d.y;
                            });
                }
            }

            function labels(centers) {
                svg.selectAll(".label").remove();

                svg.selectAll(".label")
                        .data(centers).enter().append("text")
                        .attr("class", "label")
                        .attr("fill", "red")
                        .text(function (d) {
                            if(d.name != null && (d.name.constructor === Array || d.name instanceof Array))
                                d.name = d.name[0];
                            if (d.name != null && (typeof d.name === 'string' || d.name instanceof String)) {
                                var names = d.name.split(",");
                                var surname = [""];
                                var firstName = [""];
                                if (names[0] != null)
                                    surname = names[0].trim().split(" ");
                                if (names[1] != null)
                                    firstName = names[1].trim().split(" ");
                                return surname[0] + ", " + firstName[0];
                            }
                            return d.name;
                        })
                        .attr("transform", function (d) {
                            return "translate(" + (d.x + (d.dx / 2) - 40) + ", " + (d.y + 20) + ")";
                        });
            }

            function removePopovers() {
                $('.popover').each(function () {
                    $(this).remove();
                });
            }

            function showPopover(d) {
                $(this).popover({
                    placement: 'auto top',
                    container: 'body',
                    trigger: 'manual',
                    html: true,
                    content: function () {
                        if(d.abstract != null && (d.abstract.constructor === Array || d.abstract instanceof Array))
                                d.abstract = d.abstract[0];
                        return "Title: " + d.title + "<br />" +
                                "Abstract: " + d.abstract.substring(0, 50) + "<br />" +
                                "Author: " + d.author + "<br />"
                        "Author Source: " + d.source + "<br />"

//                        "Country: " + d.country + "<br />" +
//                        "SIC Sector: " + d.sicSector + "<br />" +
//                        "Last: " + d.lastPrice + " (" + d.pricePercentChange + "%)<br />" +
//                        "Volume: " + d.volume + "<br />" +
//                        "Standard Deviation: " + d.standardDeviation
                                ;
                    }
                });
                $(this).popover('show')
            }

            function collide(alpha) {
                var quadtree = d3.geom.quadtree(dataMapping);
                return function (d) {
                    var r = d.radius + maxRadius + padding,
                            nx1 = d.x - r,
                            nx2 = d.x + r,
                            ny1 = d.y - r,
                            ny2 = d.y + r;
                    quadtree.visit(function (quad, x1, y1, x2, y2) {
                        if (quad.point && (quad.point !== d)) {
                            var x = d.x - quad.point.x,
                                    y = d.y - quad.point.y,
                                    l = Math.sqrt(x * x + y * y),
                                    r = d.radius + quad.point.radius + padding;
                            if (l < r) {
                                l = (l - r) / l * alpha;
                                d.x -= x *= l;
                                d.y -= y *= l;
                                quad.point.x += x;
                                quad.point.y += y;
                            }
                        }
                        return x1 > nx2 || x2 < nx1 || y1 > ny2 || y2 < ny1;
                    });
                };
            }
            
            function click(d) {
                //adding information about publications of THIS keyword into "tree-node-info"   DIV
                var infoBar = $('div.tree-node-info');
                /*var model = {"dct:title": {label: "Title", containerType: "div"},
                    "bibo:uri": {label: "URL", containerType: "a"},
                    "dct:contributor": {label: "Contributor", containerType: "a"},
                    "dct:isPartOf": {label: "Is Part Of", containerType: "a"},
                    "dct:license": {label: "License", containerType: "a"},
                    "dct:provenance": {label: "Source", containerType: "div"},
                    "dct:publisher": {label: "Publisher", containerType: "div"},
                    "bibo:numPages": {label: "Pages", containerType: "div"}
                };*/
                if (infoBar) {
                    var key = d.publication;
                    var headbar = $('div.head-info');
                    headbar.find('title').text("ddddddtitletitle");
                    headbar.html('');
                    var div = $('<div>');
                    var label;
                    if ($routeParams.lang === "es") {
                        label= $('<span class="label label-primary" style="font-size:35px">').text("PUBLICACION: " + d.title);
                    } else {
                        label= $('<span class="label label-primary" style="font-size:35px">').text("PUBLICATION: " + d.title);
                    }
                    div.append(label);
                    div.append("</br>");
                    headbar.append(div);

                    var sparqlPublications = globalData.PREFIX
                            + " CONSTRUCT { "
                            + "<" + key + "> a bibo:Document. "
                            + "<" + key + "> dct:title ?title. "
                            + "<" + key + "> bibo:abstract ?abstract. "
                            + "<" + key + "> bibo:uri ?uri. "
                            + "<" + key + "> bibo:Quote ?keywords "
                            + " } "
                            + " WHERE { "
                            + "  SELECT  ?title ?uri (group_concat(distinct ?abst;separator=\" \") as ?abstract) (group_concat(distinct ?key;separator=\", \") as ?keywords)"
                            + "  WHERE"
                            + "  {"
                            + "     GRAPH <" + globalData.centralGraph + "> "
                            + "     { "
                            + "     <" + key + "> dct:title ?title . "
                            + "     <" + key + "> bibo:uri  ?uri. "
                            + "     OPTIONAL { <" + key + "> bibo:Quote ?key.}"
                            + "     OPTIONAL { <" + key + "> bibo:abstract  ?abst  }"
                            + "     }"
                            + "  } GROUP BY ?title ?uri "
                            + "}";

                    waitingDialog.show("Searching the publication \"" + d.title + "\"");

                    sparqlQuery.querySrv({query: sparqlPublications}, function (rdf) {
                
                        jsonld.compact(rdf, globalData.CONTEXT, function (err, compacted) {
                                if (compacted)
                                {
                                    var entity = compacted["@graph"];
                                    var final_entity = _.where(entity, {"@type": "bibo:Document"});
                                    var values = final_entity.length ? final_entity : [final_entity];
                                    //send data to the Controller
                                    scope.ifClick({value: values});
                                    waitingDialog.hide();

                                } else
                                {
                                    waitingDialog.hide();
                                }
                                removePopovers();
                            });
                        });
                }
                return d3.event.preventDefault();
            };

        }

        return {
            restrict: 'E',
            scope: {
                data: '=',
                'ifClick': "&"
            },
            compile: function (element, attrs, transclude) {
                //	Create	a	SVG	root	element
                /*var	svg	=	d3.select(element[0]).append('svg');
                 svg.append('g').attr('class', 'data');
                 svg.append('g').attr('class', 'x-axis axis');
                 svg.append('g').attr('class', 'y-axis axis');*/
                //	Define	the	dimensions	for	the	chart
                //var width = 960, height = 500;
                var elementWidth = parseInt(element.css('width'));
                var elementHeight = parseInt(element.css('height'));
                var width = attrs.ctWidth ? attrs.ctWidth : elementWidth;
                var height = attrs.ctHeight ? attrs.ctHeight : elementHeight;


                var svg = d3.select(element[0]);

                //	Return	the	link	function
                return	function (scope, element, attrs) {
                    //	Watch	the	data	attribute	of	the	scope
                    /*scope.$watch('$parent.logs', function(newVal, oldVal, scope) {
                     //	Update	the	chart
                     var data = scope.$parent.logs.map(function(d) {
                     return {
                     x: d.time,
                     y: d.visitors
                     }
                     
                     });
                     
                     draw(svg, width, height, data);
                     },	true);*/
                    scope.$watch('data', function (newVal, oldVal, scope) {
                        //	Update	the	chart


                        if (scope.data && scope.data[0] && scope.data[0]["value"] && scope.data[0]["value"][0]
                                && !oldVal) {
                            var dataToDraw = scope.data[0]["value"];
                            var groupByOption = scope.data[0]["group"];
                            create(svg, dataToDraw, groupByOption, scope);
                        }
                        else

                        if (scope.data && scope.data[0] && scope.data[0]["value"] && scope.data[0]["value"][0] &&
                                (JSON.stringify(newVal[0]["value"][0]["title"] ? newVal[0]["value"][0]["title"] : newVal) != (oldVal[0] && oldVal[0]["value"] ? (JSON.stringify(oldVal[0]["value"][0] ? oldVal[0]["value"][0]["title"] : oldVal)) : null) )) {
                            //var jsonld = data.data;
                            //var schema = data.schema;
                            //var fields = schema.fields;
                            //var mappedData = [];
//                            _.each(jsonld['@graph'], function (keyword, idx) {
//                                mappedData.push({label: keyword[fields[0]], value: keyword[fields[1]]["@value"]});
//                            });
                            var dataToDraw = scope.data[0]["value"];
                            var groupByOption = scope.data[0]["group"];
                            create(svg, dataToDraw, groupByOption, scope);
                        }

                    }, true);

                };
            }
        };
    }]);

