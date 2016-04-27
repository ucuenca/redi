'use strict';

var keywClusters = angular.module('keywClusters', []);
//	D3	Factory
keywClusters.factory('d3', function () {
    return	d3;
});
keywClusters.directive('keywClusters', ["d3", '$routeParams','globalData', 'sparqlQuery',
    function (d3, $routeParams, globalData, sparqlQuery) {

        var chart, clear, click, collide, collisionPadding, connectEvents, data, force, gravity, hashchange, height, idValue, jitter, label, margin, maxRadius, minCollisionRadius, mouseout, mouseover, node, rScale, rValue, textValue, tick, transformData, update, updateActive, updateLabels, updateNodes, width;
        var scope;
        var attrs;

        width;// = 980;
        height;// = 510;
        data = [];
        node = null;
        label = null;
        margin = {
            top: 5,
            //top: 1350,
            right: 0,
            bottom: 0,
            left: 0
        };
        maxRadius = 35;
        rScale = d3.scale.sqrt().range([0, maxRadius]);
        rValue = function (d) {
            return parseInt(d.value);
        };
        idValue = function (d) {
            return d.label;
        };
        textValue = function (d) {
            return d.label;
        };
        collisionPadding = 2;
        minCollisionRadius = 3;
        jitter = 0.08;
        transformData = function (rawData) {
            rawData.forEach(function (d) {
                d.value = parseInt(d.value);
            });
            return rawData;
        };
        tick = function (e) {
            var dampenedAlpha;
            dampenedAlpha = e.alpha * 0.01;
            node.each(gravity(dampenedAlpha)).each(collide(jitter)).attr("transform", function (d) {
                return "translate(" + d.x + "," + d.y + ")";
            });
            return label.style("left", function (d) {
                /*if(d.label == "Amino Acid") { 
                 d = d;
                 }*/      //POSITION OF LABEL
                return (17 + (margin.left + d.x) - d.dx / 2) + "px";
            }).style("top", function (d) {
                return (50 + (margin.top + d.y) - d.dy / 2) + "px";
            });
        };

        //main method to plot 
        chart = function (selection) {
            return selection.each(function (rawData) {
                var maxDomainValue, svg, svgEnter;
                data = transformData(rawData);
                maxDomainValue = d3.max(data, function (d) {
                    return rValue(d);
                });
                if (maxDomainValue>80) {		
                    maxDomainValue -= 80;		
                }
                rScale.domain([0, maxDomainValue]);
                svg = d3.select(this).selectAll("svg").data([data]);
                svgEnter = svg.enter().append("svg");
                svg.attr("width", width + margin.left + margin.right);
                svg.attr("height", height + margin.top + margin.bottom);
                node = svgEnter.append("g").attr("id", "bubble-nodes").attr("transform", "translate(" + margin.left + "," + margin.top + ")");
                node.append("rect").attr("id", "bubble-background").attr("width", width).attr("height", height);
                label = d3.select(this).selectAll("#bubble-labels").data([data]).enter().append("div").attr("id", "bubble-labels");
                update();
                /*hashchange();
                 return d3.select(window).on("hashchange", hashchange);*/
                return d3.select(window);
            });
        };
        update = function () {
            data.forEach(function (d, i) {
                return d.forceR = Math.max(minCollisionRadius, rScale(rValue(d)));
            });
            force.nodes(data).start();
            updateNodes();
            return updateLabels();
        };
        updateNodes = function () {
            node = node.selectAll(".bubble-node").data(data, function (d) {
                return idValue(d);
            });
            node.exit().remove();
            return node.enter().append("a").attr("class", "bubble-node")
                .attr("xlink:href", function (d) {
                     //   return "#/" + $routeParams.lang+ "/" +(encodeURIComponent(idValue(d)));
                    return "#/" + $routeParams.lang+ "/cloud/keywords";
             
                })
                .call(force.drag).call(connectEvents).append("circle").attr("id", "kcircle").attr("r", function (d) {
                return rScale(rValue(d));
            });
        };
        updateLabels = function () {
            var labelEnter;
            label = label.selectAll(".bubble-label").data(data, function (d) {
                return idValue(d);
            });
            label.exit().remove();
            labelEnter = label.enter().append("a").attr("class", "bubble-label")
//            .attr("href", function (d) {
//                return "#" + (encodeURIComponent(idValue(d)));
//            })
            .call(force.drag).call(connectEvents);
            labelEnter.append("div").attr("class", "bubble-label-name").text(function (d) {
                return textValue(d);
            });
            labelEnter.append("div").attr("class", "bubble-label-value").text(function (d) {
                return rValue(d);
            });
            label.style("font-size", function (d) {
                return Math.max(9, rScale(rValue(d) / 5)) + "px";
            }).style("width", function (d) {
                return 0.1 * rScale(rValue(d)) + "px";
            });
            label.append("span").text(function (d) {
                return textValue(d);
            }).each(function (d) {
                return d.dx = Math.max(2.5 * rScale(rValue(d)), this.getBoundingClientRect().width);
            }).remove();
            label.style("width", function (d) {
                return d.dx + "px";
            });
            return label.each(function (d) {
                return d.dy = (Number(this.getBoundingClientRect().height) -10).toString();
            });
        };
        gravity = function (alpha) {
            var ax, ay, cx, cy;
            cx = width / 2;
            cy = height / 2;
            ax = alpha / 8;
            ay = alpha;
            return function (d) {
                d.x += (cx - d.x) * ax;
                return d.y += (cy - d.y) * ay;
            };
        };
        collide = function (jitter) {
            return function (d) {
                return data.forEach(function (d2) {
                    var distance, minDistance, moveX, moveY, x, y;
                    if (d !== d2) {
                        x = d.x - d2.x;
                        y = d.y - d2.y;
                        distance = Math.sqrt(x * x + y * y);
                        minDistance = d.forceR + d2.forceR + collisionPadding;
                        if (distance < minDistance) {
                            distance = (distance - minDistance) / distance * jitter;
                            moveX = x * distance;
                            moveY = y * distance;
                            d.x -= moveX;
                            d.y -= moveY;
                            d2.x += moveX;
                            return d2.y += moveY;
                        }
                    }
                });
            };
        };
        connectEvents = function (d) {
            d.on("click", click);
            d.on("mouseover", mouseover);
            return d.on("mouseout", mouseout);
        };
      
        click = function (d) {

            var title = '';
            if ($routeParams.lang === "es") {
                title = '"Clusters que contienen \'' + d.label + '\' Keyword"';
            } else {
                title = '"Clusters that contain \'' + d.label + '\' Keyword"';
            }

            /**/
            
            var sparqlquery = globalData.PREFIX

            +' Construct {'

            +' uc:resultTitle a uc:pagetitle.'
            +' uc:resultTitle uc:viewtitle ' + title + '.'
            +'  ?cluster rdfs:label ?name. ?cluster uc:total ?totalperson.'
            +'} '
            +'WHERE'
            +'{'
            +'{'
            +'SELECT DISTINCT ?cluster ?name (COUNT(DISTINCT ?subject) as ?totalperson)'
            +'WHERE' 
            +'{'
            +'  graph <'+globalData.clustersGraph+'>'
            +'        {'
            +'          ?cluster uc:hasPerson ?subject. '
            + '          ?cluster rdfs:label ?name. '
            +'  		?subject foaf:publications ?pubb. '
            +'          	{'
            +'      			select DISTINCT ?pubb ?title '
            +'            		where'
            +'            		{'
            +'            			graph <'+globalData.centralGraph+'>'
            +'            			{'      
            +'            				?pubb bibo:Quote "' + d.label + '".'
            +'                          	?pubb dct:title ?title.'
            +'              			}'
            +'          			}'

            +'          	}'
            +'          }'
            +' }'
            +'  group by ?cluster ?name'
            + '         }'
            + ' Filter(?totalperson > 1)'
            + '}';

            
            waitingDialog.show("Loading Authors Related with " + d.label);
            sparqlQuery.querySrv({query: sparqlquery}, function (rdf) {
                jsonld.compact(rdf, globalData.CONTEXT, function (err, compacted) {
                    if (compacted["@graph"])
                    {
                        var entity = compacted["@graph"];
                        //var final_entity = _.where(entity, {"@type": "bibo:Document"});
                        var values = entity.length ? entity : [entity];
                        //Change data
//                        for (var i = 0, len = compacted["@graph"].length; i < len; i++) {
//                            //var labelCluster = compacted["@graph"][i]["@id"].toString().replace("http://ucuenca.edu.ec/resource/cluster","");
//                            var labelCluster = compacted["@graph"][i]["rdfs:name"];
//                            compacted["@graph"][i]["rdfs:label"] = labelCluster; //? labelCluster[0].toUpperCase() + labelCluster.slice(1) : compacted["@graph"][i]["@id"].toString();
//                        }
                        //send data to getKeywordTag Controller
                        scope.ifClick({value: compacted});
                        waitingDialog.hide();
                    } else
                    {
                        waitingDialog.hide();
                    }
                });
            });   // end  sparqlQuery.querySrv(...

            
        };
        hashchange = function () {
            var id;
            id = decodeURIComponent(location.hash.substring(1)).trim();
            return updateActive(id);
        };
        updateActive = function (id) {
            node.classed("bubble-selected", function (d) {
                return id === idValue(d);
            });
            if (id.length > 0) {
                return d3.select("#status").html("<h3>The word <span class=\"active\">" + id + "</span> is now active</h3>");
            } else {
                return d3.select("#status").html("<h3>No word is active</h3>");
            }
        };
        mouseover = function (d) {
            return node.classed("bubble-hover", function (p) {
                return p === d;
            });
        };
        mouseout = function (d) {
            return node.classed("bubble-hover", false);
        };
        chart.jitter = function (_) {
            if (!arguments.length) {
                return jitter;
            }
            jitter = _;
            force.start();
            return chart;
        };
        chart.height = function (_) {
            if (!arguments.length) {
                return height;
            }
            height = _;
            return chart;
        };
        chart.width = function (_) {
            if (!arguments.length) {
                return width;
            }
            width = _;
            return chart;
        };
        chart.r = function (_) {
            if (!arguments.length) {
                return rValue;
            }
            rValue = _;
            return chart;
        };

        var draw = function draw(element, widthEl, heightEl, data, scopeEl, attrsEl) {
            width = widthEl;
            height = heightEl;
            scope = scopeEl;
            attrs = attrsEl;

            force = d3.layout.force().gravity(0).charge(0).size([width, height]).on("tick", tick);
            element.datum(data).call(chart);

        }

        return {
            restrict: 'E',
            scope: {
                //'ctrlFn': "&",
                'ifClick': "&",
                data: '='
            },
            compile: function (element, attrs, transclude) {
                //	Define	the	dimensions	for	the	chart
                //var width = 960, height = 500;
                var elementWidth = parseInt(element.css('width'));
                var elementHeight = parseInt(element.css('height'));
                var width = attrs.ctWidth ? attrs.ctWidth : elementWidth;
                var height = attrs.ctHeight ? attrs.ctHeight : elementHeight;

                //	Create	a	SVG	root	element
                var svg = d3.select(element[0]);

                //	Return	the	link	function
                return	function (scope, element, attrs) {
                    //	Watch	the	data	attribute	of	the	scope
                    scope.$watch('data', function (newVal, oldVal, scope) {
                        //	Update	the	chart
                        var data = scope.data;
                        if (data) {
                            var jsonld = data.data;
                            var schema = data.schema;
                            var fields = schema.fields;
                            var mappedData = [];
                            _.each(jsonld['@graph'], function (keyword, idx) {
                                mappedData.push({label: keyword[fields[0]], value: keyword[fields[1]]["@value"]});
                            });
                            draw(svg, width, height, mappedData, scope, attrs);
                        }
                    }, true);
                };
            }
        };
    }]);


