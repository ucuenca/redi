'use strict';
//Author of this View using D3: http://bl.ocks.org/NPashaP/96447623ef4d342ee09b

var barChart = angular.module('barChart', []);
//	D3	Factory
barChart.factory('d3', function () {
    return	d3;
});
barChart.directive('barChart', ["d3", "globalData", "sparqlQuery",
    function (d3, globalData, sparqlQuery) {

        function draw(id, fData, w, h) {
            var allhtml = id;
            allhtml.html('');
            var barColor = 'steelblue';
            function segColor(c) {
                return {Autores: "#807dba", Publicaciones: "#e08214", Salud: "#41ab5d"}[c];
            }

            var color = d3.scale.ordinal()
                .range(["#807dba", "#41ab5d"]);

            // compute total for each source.
            fData.forEach(function (d) {
                d.total = 0;
                //d.total = parseInt(d.freq.Autores) + parseInt(d.freq.Publicaciones) + parseInt(d.freq.Salud);
                d.total = parseInt(d.freq.Publicaciones);
            });

            // function to handle histogram.
            function histoGram(fD) {
                var hG = {}, hGDim = {t: 60, r: 0, b: 30, l: 0};
                hGDim.w = w - hGDim.l - hGDim.r,
                        hGDim.h = h - hGDim.t - hGDim.b;

                //create svg for histogram.
                var hGsvg = id.append("svg")
                        .attr("width", hGDim.w + hGDim.l + hGDim.r)
                        .attr("height", hGDim.h + hGDim.t + hGDim.b).append("g")
                        .attr("transform", "translate(" + hGDim.l + "," + (hGDim.t) + ")");

                // create function for x-axis mapping.
                var x = d3.scale.ordinal().rangeRoundBands([0, hGDim.w], 0.1)
                        .domain(fD.map(function (d) {
                            return d.ies;
                        }));
                var x1 = d3.scale.ordinal().domain(["publications","authors"]).rangeRoundBands([0, x.rangeBand()]);

                // Add x-axis to the histogram svg.
                hGsvg.append("g").attr("class", "x axis")
                        .attr("transform", "translate(0," + hGDim.h + ")")
                        .call(d3.svg.axis().scale(x).orient("bottom"));

                // Create function for y-axis map.
                var y = d3.scale.linear().range([hGDim.h, 0])
                        .domain([0, d3.max(fD, function (d) {
                                return d3.max(d.values, function (d){
                                  return parseInt(d.value);
                                });
                            })]);

                // Create bars for histogram to contain rectangles and freq labels.
                var bars = hGsvg.selectAll(".bar").data(fD).enter()
                        .append("g").attr("class", "bar")
                        .attr("transform", function(d) {
                          return "translate(" + x(d.ies) + ",0)";
                        });

                //create the rectangles.
                bars.selectAll("rect")
                      .data(function(d) {
                        return d.values;
                      })
                    .enter().append("rect")
                        .attr("x", function (d) {
                            return x1(d.name);
                        })
                        .attr("y", function (d) {
                            return y(d.value);
                        })
                        .attr("width", x1.rangeBand())
                        .attr("height", function (d) {
                            return hGDim.h - y(d.value);
                        })
                        .attr("fill", function(d) {
                          return d.name === "authors" ? segColor("Autores") : segColor("Publicaciones");
                        })
                        .on("mouseover", mouseover)// mouseover is defined below.
                        .on("mouseout", mouseout);// mouseout is defined below.

                //Create the frequency labels above the rectangles.
                bars.selectAll("div")
                      .data(function(d) {
                        return d.values;
                      })
                    .enter().append("text").text(function (d) {
                          return d3.format(",")(d.value)
                        })
                        .attr("x", function (d) {
                            return x1(d.name) + x1.rangeBand() / 2;
                        })
                        .attr("y", function (d) {
                            return y(d.value) - 5;
                        })
                        .attr("text-anchor", "middle")
                        .style("font-size","70%");

                function mouseover(d) {  // utility function to be called on mouseover.
                    $("h2.selected").text("");
                    $("h2.selected").text(d.ies);
                    // filter for selected source.
                    var st = fData.filter(function (s) {
                        return d.hasOwnProperty("ies") ? s.Source === d.ies : s.Source === d[0];
                    })[0],
                            nD = d3.keys(st.freq).map(function (s) {
                        return {type: s, freq: st.freq[s]};
                    });

                    // call update functions of pie-chart and legend.
                    //pC.update(nD);
                    leg.update(nD);
                }

                function mouseout(d) {    // utility function to be called on mouseout.
                    // reset the pie-chart and legend.
                    $("h2.selected").text("");
                    $("h2.selected").text("Todas las Universidades");
                    //pC.update(tF);
                    leg.update(tF);
                }

                // create function to update the bars. This will be used by pie-chart.
                hG.update = function (nD, color) {
                    if (typeof nD[0] === 'undefined') return;
                    // update the domain of the y-axis map to reflect change in frequencies.
                    y.domain([0, d3.max(nD, function (d) {
                            return d[1];
                        })]);

                    // Attach the new data to the bars.
                    var bars = hGsvg.selectAll(".bar").data(nD);

                    // transition the height and color of rectangles.
                    bars.select("rect").transition().duration(500)
                            .attr("y", function (d) {
                                return y(d[1]);
                            })
                            .attr("height", function (d) {
                                return hGDim.h - y(d[1]);
                            })
                            .attr("fill", color);

                    // transition the frequency labels location and change value.
                    bars.select("text").transition().duration(500)
                            .text(function (d) {
                                return d3.format(",")(d[1])
                            })
                            .attr("y", function (d) {
                                return y(d[1]) - 5;
                            });
                }
                return hG;
            }

            // function to handle pieChart.
            function pieChart(pD) {
                var pC = {}, pieDim = {w: 250, h: 250};
                pieDim.r = Math.min(pieDim.w, pieDim.h) / 2;

                // create svg for pie chart.
                var piesvg = id.append("svg")
                        .attr("width", pieDim.w).attr("height", pieDim.h).append("g")
                        .attr("transform", "translate(" + pieDim.w / 2 + "," + pieDim.h / 2 + ")");

                // create function to draw the arcs of the pie slices.
                var arc = d3.svg.arc().outerRadius(pieDim.r - 10).innerRadius(0);

                // create a function to compute the pie slice angles.
                var pie = d3.layout.pie().sort(null).value(function (d) {
                    return d.freq;
                });

                // Draw the pie slices.
                piesvg.selectAll("path").data(pie(pD)).enter().append("path").attr("d", arc).attr("class", "barchart")
                        .each(function (d) {
                            this._current = d;
                        })
                        .style("fill", function (d) {
                            return segColor(d.data.type);
                        })
                        .on("mouseover", mouseover).on("mouseout", mouseout);

                // create function to update pie-chart. This will be used by histogram.
                pC.update = function (nD) {
                    piesvg.selectAll("path").data(pie(nD)).transition().duration(500)
                            .attrTween("d", arcTween);
                }
                // Utility function to be called on mouseover a pie slice.
                function mouseover(d) {
                    // call the update function of histogram with new data.
                    hG.update(fData.map(function (v) {
                        //Esta funcion permita que las barras sean dinamicas segun lo que se seleccione en el pie
                        //documentado por FB            return [v.Source, v.freq[d.data.type]];
                    }), segColor(d.data.type));
                }
                //Utility function to be called on mouseout a pie slice.
                function mouseout(d) {
                    // call the update function of histogram with all data.
                    hG.update(fData.map(function (v) {
                        return [v.Source, v.total];
                    }), segColor(d.data.type));
                }
                // Animating the pie-slice requiring a custom function which specifies
                // how the intermediate paths should be drawn.
                function arcTween(a) {
                    var i = d3.interpolate(this._current, a);
                    this._current = i(0);
                    return function (t) {
                        return arc(i(t));
                    };
                }
                return pC;
            }

            // function to handle legend.
            function legend(lD) {
                var leg = {};

                // create table for legend.
                var legend = id.append("table").attr('class', 'legend');

                // create one row per segment.
                var tr = legend.append("tbody").selectAll("tr").data(lD).enter().append("tr");

                // create the first column for each segment.
                tr.append("td").append("svg").attr("width", '16').attr("height", '16').append("rect")
                        .attr("width", '16').attr("height", '16')
                        .attr("fill", function (d) {
                            return segColor(d.type);
                        });

                // create the second column for each segment.
                tr.append("td").text(function (d) {
                    return d.type;
                });

                // create the third column for each segment.
                tr.append("td").attr("class", 'legendFreq')
                        .text(function (d) {
                            return d3.format(",")(d.freq);
                        });

                // create the fourth column for each segment.
                // tr.append("td").attr("class", 'legendPerc')
                //         .text(function (d) {
                //             return getLegend(d, lD);
                //         });

                // Utility function to be used to update the legend.
                leg.update = function (nD) {
                    // update the data attached to the row elements.
                    var l = legend.select("tbody").selectAll("tr").data(nD);

                    // update the frequencies.
                    l.select(".legendFreq").text(function (d) {
                        return d3.format(",")(d.freq);
                    });

                    // update the percentage column.
                    l.select(".legendPerc").text(function (d) {
                        return getLegend(d, nD);
                    });
                }

                function getLegend(d, aD) { // Utility function to compute percentage.
                    return d3.format("%")(d.freq / d3.sum(aD.map(function (v) {
                        return v.freq;
                    })));
                }

                return leg;
            }

            // calculate total frequency by segment for all source.
            var tF = ['Autores', 'Publicaciones'].map(function (d) {
                return {type: d, freq: d3.sum(fData.map(function (t) {
                        return t.freq[d];
                    }))};
            });

            // calculate total frequency by source for all segment.
            var sF = fData.map(function (d) {
                return {ies:d.Source, values: [
                  {name:"publications", value:d.freq.Publicaciones, ies:d.Source},
                  {name:"authors", value:d.freq.Autores, ies:d.Source}]};
            });

            var hG = histoGram(sF), // create the histogram.
                    //pC = pieChart(tF), // create the pie-chart.
                    leg = legend(tF);  // create the legend.
        }
        return {
            restrict: 'E',
            scope: {
                data: '=',
            },
            compile: function (element, attrs, transclude) {
                //	Create	a	SVG	root	element
                var svg = d3.select(element[0]);
                //var width = 960, height = 500;
                var elementWidth = parseInt(element.css('width'));
                var width = attrs.pcWidth ? attrs.pcWidth : elementWidth,
                        height = attrs.pcHeight;
                var w = element.parent()[0].offsetWidth - 0.01*(element.parent()[0].offsetWidth),
                    h = (2 * w) / 7;
                //	Return	the	link	function
                return	function (scope, element, attrs) {
                    //	Watch	the	data	attribute	of	the	scope
                    scope.$watch('data', function (newVal, oldVal, scope) {
                        //	Update	the	chart
                        var data = scope.data;

                        if (data) {
                            draw(svg, data, w, h);
                        }
                    }, true);

                };
            }
        };
    }]);
