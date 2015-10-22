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

                    //var xxxx=1;
                    /*
                     var g = svg
                     .append("g")
                     .attr("transform", "translate(" + width / 2 + "," + height / 2 + ")");
                     
                     var x = g.selectAll("path")
                     .data(pie(_.pluck(data,"value")))
                     .enter().append("path");
                     
                     x
                     .each(function(d) { d.outerRadius = outerRadius - 20; })
                     .attr("d", arc)*/

                    //.on("mouseover", arcTween(outerRadius, 0))
                    .on("mouseover", function (d) {
                        d3.select('#tooltip p strong')
                                .text(d.data.label);
                        d3.select("#tooltip")
                                .style("left", d3.event.pageX + "px")
                                .style("top", d3.event.pageY + "px")
                                .style("opacity", 1)
                                .select("#value")
                                .text(d.value + ' ' + entityName);


                    })
                    //.on("mouseout", arcTween(outerRadius - 20, 150));
                    .on("mouseout", function () {
                        // Hide the tooltip
                        d3.select("#tooltip")
                                .style("opacity", 0);

                    })

                    .on("click", function (d) {
                        //   location.replace("#" + encodeURIComponent(idValue(d)));
                        //adding information about publications of THIS keyword into "tree-node-info"   DIV
                        var infoBar = $('div.tree-node-info');
                        var model = {"dcterms:title": {label: "Title", containerType: "div"},
                            "bibo:uri": {label: "URL", containerType: "a"},
                            "dcterms:contributor": {label: "Contributor", containerType: "a"},
                            "dcterms:isPartOf": {label: "Is Part Of", containerType: "a"},
                            "dcterms:license": {label: "License", containerType: "a"},
                            "dcterms:provenance": {label: "Source", containerType: "div"},
                            "dcterms:publisher": {label: "Publisher", containerType: "div"},
                            "bibo:numPages": {label: "Pages", containerType: "div"}
                        };
                        //var sparqlDescribe = "DESCRIBE <" + id + ">";
                        var sparqlquery = 'PREFIX dct: <http://purl.org/dc/terms/> '
                                + ' PREFIX bibo: <http://purl.org/ontology/bibo/>  '
                                + ' PREFIX foaf: <http://xmlns.com/foaf/0.1/>  '
                                + ' PREFIX uc: <http://ucuenca.edu.ec/wkhuska/resource/>  '
                                + ' CONSTRUCT { ?subject rdfs:label ?name. ?subject uc:total ?totalPub }   '
                                + ' WHERE {  '
                                + ' {       '
                                + ' SELECT ?subject ?name (COUNT(?pub) AS ?totalPub)  '
                                + ' WHERE { '
                                + ' 	?subject foaf:publications  ?pub . '
                                + ' 	?subject foaf:name       ?name.        '
                                + ' 	?subject dct:provenance ?provenance. '
                                + '     ?provenance <http://ucuenca.edu.ec/wkhuska/resource/name> "UCUENCA" '
                                + '    }  '
                                + ' GROUP BY ?subject ?name '
                                + ' HAVING( ?totalPub > 100) '
                                + '} '
                                + '}';
                        waitingDialog.show("Searching data of: " + d);

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
                        return d3.event.preventDefault();
                    });
            /*
             
             var text = g.select(".labels").selectAll("text")
             .data(pie(data), key);
             
             text.enter()
             .append("text")
             .attr("dy", ".35em")
             .text(function(d) {
             return d.data.label;
             });
             
             function midAngle(d){
             return d.startAngle + (d.endAngle - d.startAngle)/2;
             }
             
             text.transition().duration(1000)
             .attrTween("transform", function(d) {
             this._current = this._current || d;
             var interpolate = d3.interpolate(this._current, d);
             this._current = interpolate(0);
             return function(t) {
             var d2 = interpolate(t);
             var pos = outerArc.centroid(d2);
             pos[0] = radius * (midAngle(d2) < Math.PI ? 1 : -1);
             return "translate("+ pos +")";
             };
             })
             .styleTween("text-anchor", function(d){
             this._current = this._current || d;
             var interpolate = d3.interpolate(this._current, d);
             this._current = interpolate(0);
             return function(t) {
             var d2 = interpolate(t);
             return midAngle(d2) < Math.PI ? "start":"end";
             };
             });
             
             text.exit()
             .remove();*/


            function arcTween(outerRadius, delay) {
                return function () {
                    var i = d3.interpolate(d.outerRadius, outerRadius);
                    return function (t) {
                        d.outerRadius = i(t);
                        return arc(d);
                    };
                };
            }

            //var key = function(d){ return d.data.label; };
            //var labels = data.domain;
            /*var dataS = labels.map(function(label, i){
             console.log(i);
             return { label: label, value: Math.random() }
             });*/
            /*dataS.label = data.domain;
             dataS.value = data.values;*/
            /*
             var slice = svg.select(".slices").selectAll("path.slice")
             .data(pie(data), key);
             
             slice.enter()
             .insert("path")
             .style("fill", function(d) { 
             return d.data.color; })
             .attr("class", "slice");*/

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
                var width = attrs.pcWidth ? attrs.pcWidth : elementWidth,
                        height = attrs.pcHeight;


                var svg = d3.select(element[0]).append("svg");

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

