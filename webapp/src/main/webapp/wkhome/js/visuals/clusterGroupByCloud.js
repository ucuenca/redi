'use strict';

var cloudCluster = angular.module('cloudCluster', []);
//	D3	Factory
cloudCluster.factory('d3', function () {
    return	d3;
});
cloudCluster.directive('cloudCluster', ["d3", 'sparqlQuery',
    function (d3, sparqlQuery) {
        var group = '';
        var size = '';
        var color = '';
        var arrayColors = new Array();
        arrayColors['default'] = '#5882FA';
        function create(svgElement, dataToDraw, groupByOption) {
            
            var radius = 400;
            var width = Math.max(document.documentElement.clientWidth * 1, window.innerWidth * 1) ;
            var height = Math.max(document.documentElement.clientHeight * 1, window.innerHeight * 1);
            
            
            var svg = svgElement.append("svg")
                    .attr("width", width )
                    .attr("height", height);


            var dataMapping = getDataMapping(dataToDraw, size);

            var padding = 8;
            var maxRadius = d3.max(_.pluck(dataMapping, 'radius'));

            
            var getCenters = function (vname, size) {
                var centers, map;
                centers = _.uniq(_.pluck(dataMapping, vname)).map(function (d) {
                    var keyw = '';
                    if (vname == 'cluster') {
                        var cont = 0;
                        var i = 0;
                        //Finding the keywords of the first node with that cluster name
                        while (d != null && cont == 0 && dataMapping[i] != null) {
                            if (dataMapping[i].cluster == d){
                                keyw = dataMapping[i].keyword;
                                arrayColors[d] = '#' + Math.floor(Math.random() * 16777215).toString(16);
                                cont++;
                            }
                            i++;
                        }
                    } 
                    return {name: d, value: 1, keyword: keyw};
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
                        return arrayColors['default'];
                    })
                    .on("mouseover", function (d) {
                        showPopover.call(this, d);
                    })
                    .on("mouseout", function (d) {
                        removePopovers();
                    });

            function getDataMapping(dataM, vname) {
                var max = d3.max(_.pluck(dataM, vname));
                var newData = dataM;
                for (var j = 0; j < dataM.length; j++) {
                    newData[j].radius = (vname != '') ? radius * (dataM[j][vname] / max) : 5;
                    newData[j].x = dataM[j].x ? dataM[j].x : Math.random() * width;
                    newData[j].y = dataM[j].y ? dataM[j].y : Math.random() * height;
                }

                return newData;
            }

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

            /*$('#color').change(function () {
                color = this.value;
                changeColor(this.value);
            });*/


            function changeColor(groupOption) {
                d3.selectAll("circle")
                        .transition()
                        .style('fill', function (d) {
                            if (groupOption == 'cluster') {
                                return d ? arrayColors[d.cluster] : arrayColors['default'];
                            } else {
                                return arrayColors['default'];
                            }
                        })
                        .duration(100);
            }


            var force = d3.layout.force();

            //changeColor(color);
            draw(groupByOption);

            function draw(varname) {
                
                if (varname == 'cluster') {
                    width = Math.max(document.documentElement.clientWidth * (0.95), window.innerWidth * (0.95) || 0);
                    height = Math.max(document.documentElement.clientHeight + dataToDraw.length * 1 - 350, window.innerHeight + dataToDraw.length * 1 - 350 || 0);
                    svg.attr("width", width).attr("height", height);
                } else {
                    width = Math.max(document.documentElement.clientWidth * 0.95);
                    height = Math.max(document.documentElement.clientHeight * 0.8);
                    svg.attr("width", width).attr("height", height);
                }
                
                var centers = getCenters(varname, [width, height]);
                force.on("tick", tick(centers, varname));
                labels(centers)
                force.start();
                changeColor(varname);
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
                            //if(d.keyword != null && (d.keyword.constructor === Array || d.keyword instanceof Array))
                            //    d.keyword = d.keyword[0];
                            if (d.keyword != null && (typeof d.keyword === 'string' || d.keyword instanceof String)) {
                                var keyArray = d.keyword.toString().split(",");
                                var keyword = keyArray.length > 0 ? (keyArray[0].length > 1 ? keyArray[0].trim().replace("\"", "").substring(0, 22):'') : '';
                                return keyword;
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
                        return  "<b>Title:</b> " + d.title + "<br />" +
                                "<b>Keywords:</b> " + d.keyword + "<br />" +
                                "<b>Author:</b> " + d.author + "<br />" +
                                "<b>Cluster:</b> " + d.cluster + "<br />";
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

        }

        return {
            restrict: 'E',
            scope: {
                data: '='
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
                            create(svg, dataToDraw, groupByOption);
                        }
                        else

                        if (scope.data && scope.data[0] && scope.data[0]["value"] && scope.data[0]["value"][0] &&
                                (JSON.stringify(newVal[0]["value"][0]["title"] ? newVal[0]["value"][0]["title"] : newVal) != JSON.stringify(oldVal[0]["value"][0] ? oldVal[0]["value"][0]["title"] : oldVal))) {
                            //var jsonld = data.data;
                            //var schema = data.schema;
                            //var fields = schema.fields;
                            //var mappedData = [];
//                            _.each(jsonld['@graph'], function (keyword, idx) {
//                                mappedData.push({label: keyword[fields[0]], value: keyword[fields[1]]["@value"]});
//                            });
                            var dataToDraw = scope.data[0]["value"];
                            var groupByOption = scope.data[0]["group"];
                            create(svg, dataToDraw, groupByOption);
                        }

                    }, true);

                };
            }
        };
    }]);

