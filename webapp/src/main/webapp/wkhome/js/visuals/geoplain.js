'use strict';

var geoPlain = angular.module('geoPlain', []);
//	D3	Factory
myChart.factory('d3',	function()	{
	return	d3;
});
myChart.directive('geoPlain', ["d3", 'd3JSON',
	function(d3, d3JSON){
		
			//	we	will	soon	implement	this	function
			var draw = function draw(svg,	width,	height,	data) {

				var projection = d3.geo.kavrayskiy7()
				    .scale(170)
				    .rotate([-40, 0])
				    .translate([width / 2, height / 2])
				    .precision(.1);

				var path = d3.geo.path()
				    .projection(projection);

				var graticule = d3.geo.graticule();

				svg
				    .attr("width", width)
				    .attr("height", height);

				svg.append("defs").append("path")
				    .datum({type: "Sphere"})
				    .attr("id", "sphere")
				    .attr("d", path);

				svg.append("use")
				    .attr("class", "stroke")
				    .attr("xlink:href", "#sphere");

				svg.append("use")
				    .attr("class", "fill")
				    .attr("xlink:href", "#sphere");

				svg.append("path")
				    .datum(graticule)
				    .attr("class", "graticule")
				    .attr("d", path);

				svg.append("path")
				    .datum(data.route)
				    .attr("class", "route")
				    .attr("d", path);

				var point = svg.append("g")
				    .attr("class", "points")
				  .selectAll("g")
				    .data(d3.entries(data.places))
				  .enter().append("g")
				    .attr("transform", function(d) { return "translate(" + projection(d.value) + ")"; });

				point.append("circle")
				    .attr("r", 4.5);

				point.append("text")
				    .attr("y", 10)
				    .attr("dy", ".71em")
				    .text(function(d) { return d.key; });
				//var test = d3JSON.query();
				d3.json("wkhome/d3/world-50m.json", function(error, world) {
				//d3.json("http://bl.ocks.org/mbostock/raw/4090846/world-50m.json", function(error, world) {
				  if (error) throw error;

				  svg.insert("path", ".graticule")
				      .datum(topojson.feature(world, world.objects.land))
				      .attr("class", "land")
				      .attr("d", path);

				  svg.insert("path", ".graticule")
				      .datum(topojson.mesh(world, world.objects.countries, function(a, b) { return a !== b; }))
				      .attr("class", "boundary")
				      .attr("d", path);
				});

				d3.select(self.frameElement).style("height", height + "px");

 
			}

			return {

				restrict: 'E',
				scope:	{
					data: '='
				},
				compile: function(	element,	attrs,	transclude	)	{
					//	Create	a	SVG	root	element

					var	svg	=	d3.select(element[0]).append('svg');
					/*
					svg.append('g').attr('class', 'data');
					svg.append('g').attr('class', 'x-axis axis');
					svg.append('g').attr('class', 'y-axis axis');
					*/
					//	Define	the	dimensions	for	the	chart
					var	width = 960, height = 570;

					//	Return	the	link	function
					return	function(scope,	element,	attrs) {
						scope.$watch('data', function(newVal, oldVal, scope) {
								//	Update	the	chart
								/*var data = scope.data.map(function(d) {
									return {
										x: d.time,
										y: d.visitors
									}

								});*/
								
								draw(svg, width, height, scope.data);
						},	true);

					};
				}
			};
}]);

