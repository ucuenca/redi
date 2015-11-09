'use strict';

var pieChart = angular.module('pieChart', []);
//	D3	Factory
pieChart.factory('d3', function () {
    return	d3;
});
pieChart.directive('pieChart', ["d3", "sparqlQuery",
    function (d3, sparqlQuery) {

        //	we	will	soon	implement	this	function
        var draw = function draw(svg, width, height, entityName, data, scope) {
            var outerRadius = height / 2 - 20,
                    innerRadius = outerRadius / 3,
                    cornerRadius = 10;

            var pie = d3.layout.pie()
                    .padAngle(.02)
                    //.sort(null)
                    .value(function (d) {
                        return d.value;
                    });

            var arc = d3.svg.arc()
                    .padRadius(outerRadius)
                    .innerRadius(innerRadius);

            var radius = Math.min(width, height) / 2;
            var outerArc = d3.svg.arc()
                    .innerRadius(radius * 0.9)
                    .outerRadius(radius * 0.9);


            var color = d3.scale.category10();
            /*
             svg
             .attr("width", width)
             .attr("height", height);
             
             var g = svg
             .append("g")
             
             .attr("transform", "translate(" + width / 2 + "," + height / 2 + ")");
             g.selectAll("path")
             
             .data(pie(data))
             .enter().append("path")
             
             
             .each(function(d) { d.outerRadius = outerRadius - 20; })
             
             attr("d", arc)
             //.on("mouseover", arcTween(outerRadius, 0))
             on("mouseover", function (d) {
             d3.select('#tooltip p strong')
             .text(Math.random());
             d3.select("#tooltip")
             .style("left", d3.event.pageX + "px")
             .style("top", d3.event.pageY + "px")
             .style("opacity", 1)
             .select("#value")
             .text(d.value);
             
             
             })
             //.on("mouseout", arcTween(outerRadius - 20, 150));
             .on("mouseout", function () {
             // Hide the tooltip
             d3.select("#tooltip")
             .style("opacity", 0);
             
             });*/
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
            g.attr("transform", "translate(" + width / 2 + "," + height / 2 + ")");

            var key = function (d) {
                return d.data.label;
            };

            var slice = g.select(".slices").selectAll("path.slice")
                    //g.selectAll("path")
                    .data(pie(data), key);
            slice
                    .enter().append("path")
                    //.enter().insert("path")
                    .style("fill", function (d) {
                        return d.data.color;
                    })
                    .attr("class", "slice")

                    .each(function (d) {
                        d.outerRadius = outerRadius - 20;
                    })
                    .attr("d", arc)
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
                        var key = d.data.label;
                        if (entityName === 'Articles')
                        {
                            scope.ifClick({value: key});
                        }
                        else
                        {
                            var sparqlquery = 'PREFIX dct: <http://purl.org/dc/terms/> '
                                    + ' PREFIX bibo: <http://purl.org/ontology/bibo/>  '
                                    + ' PREFIX foaf: <http://xmlns.com/foaf/0.1/>  '
                                    + ' PREFIX uc: <http://ucuenca.edu.ec/wkhuska/resource/>  '
                                    + ' CONSTRUCT { <http://ucuenca.edu.ec/wkhuska/resultTitle> a uc:pagetitle. <http://ucuenca.edu.ec/wkhuska/resultTitle> uc:viewtitle "Authors from ' + key + ' "  .  ?subject rdfs:label ?name. ?subject uc:total ?totalPub }   '
                                    + ' WHERE {  '
                                    + ' {       '
                                    + ' SELECT ?subject ?name (COUNT(?pub) AS ?totalPub)  '
                                    + ' WHERE { '
                                    + ' GRAPH <http://ucuenca.edu.ec/wkhuska> {'
                                    + ' 	?subject foaf:publications  ?pub . '
                                    + ' 	?subject foaf:name       ?name.        '
                                    + ' 	?subject dct:provenance ?provenance. '
                                    + '     { '
                                    + '         SELECT * '
                                    + '         WHERE { '
                                    + '         GRAPH <http://ucuenca.edu.ec/wkhuska/endpoints> { '
                                    + '             ?provenance <http://ucuenca.edu.ec/wkhuska/resource/name> "' + key + '" } '
                                    + '         } '
                                    + '     } '
                                    + ' } } '
                                    + ' GROUP BY ?subject ?name '
                                    + ' HAVING( ?totalPub > 50 && ?totalPub < 100) '
                                    + '} '
                                    + '}';
                            waitingDialog.show("Loading All Authors of Selected Source");

                            sparqlQuery.querySrv({query: sparqlquery}, function (rdf) {
                                var context = {
                                    "rdfs": "http://www.w3.org/2000/01/rdf-schema#",
                                    "foaf": "http://xmlns.com/foaf/0.1/",
                                    "dc": "http://purl.org/dc/elements/1.1/",
                                    "dcterms": "http://purl.org/dc/terms/",
                                    "bibo": "http://purl.org/ontology/bibo/",
                                    "uc": "http://ucuenca.edu.ec/wkhuska/resource/"
                                };

                                jsonld.compact(rdf, context, function (err, compacted) {
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
                            });
                        }//end if entityName === Articles
                        return d3.event.preventDefault();
                    });

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
                //	Create	a	SVG	root	element
                var svg = d3.select(element[0]).append("svg");
                //var width = 960, height = 500;
                var elementWidth = parseInt(element.css('width'));
                var width = attrs.pcWidth ? attrs.pcWidth : elementWidth,
                        height = attrs.pcHeight;
                //	Return	the	link	function
                return	function (scope, element, attrs) {
                    //	Watch	the	data	attribute	of	the	scope
                    scope.$watch('data', function (newVal, oldVal, scope) {
                        //	Update	the	chart
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

