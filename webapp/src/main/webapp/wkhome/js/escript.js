var group = size = color = '';
startCloud('active');

//function drawByGroup(groupValue)
//{
//    draw(groupValue);
//}


function startCloud(type) {

    create(type);
//        $.ajax({
//            'async': false,
//            'global': false,
//            'url': 'http://tomsisk.com/ondemand/data.php?exchanges=NYSE,NASDAQ,TSX,TSX-V&assetType=STK&type='+type+'&maxRecords=200&callback=data',
//            'dataType': "jsonp",
//            "jsonpCallback": "data",
//		
//		
//					'success': function (data) {
//				create(data.results);
//            },
//			'error': function(data){
//				alert(data);
//			}
//        });
}

function create(data) {
   


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
        default: '#4CC1E9'
    };

    var radius = 50;
    var width = Math.max(document.documentElement.clientWidth, window.innerWidth || 0);
    var height = Math.max(document.documentElement.clientHeight, window.innerHeight || 0);
    var fill = d3.scale.ordinal().range(['#FF00CC', '#FF00CC', '#00FF00', '#00FF00', '#FFFF00', '#FF0000', '#FF0000', '#FF0000', '#FF0000', '#7F0000']);
    var svg = d3.select("#chart").append("svg")
            .attr("width", width)
            .attr("height", height);

    data = getDataMapping(data, size);

    var padding = 5;
    var maxRadius = d3.max(_.pluck(data, 'radius'));

    var maximums = {
        volume: d3.max(_.pluck(data, 'volume')),
        lasPrice: d3.max(_.pluck(data, 'lastPrice')),
        standardDeviation: d3.max(_.pluck(data, 'standardDeviation'))
    };

    var getCenters = function (vname, size) {
        var centers, map;
        centers = _.uniq(_.pluck(data, vname)).map(function (d) {
            return {name: d, value: 1};
        });

        map = d3.layout.treemap().size(size).ratio(1 / 1);
        map.nodes({children: centers});

        return centers;
    };

    var nodes = svg.selectAll("circle")
            .data(data);

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
            });

    function getDataMapping(data, vname) {
        var max = d3.max(_.pluck(data, vname));

        for (var j = 0; j < data.length; j++) {
            data[j].radius = (vname != '') ? radius * (data[j][vname] / max) : 15;
            data[j].x = data[j].x ? data[j].x : Math.random() * width;
            data[j].y = data[j].y ? data[j].y : Math.random() * height;
            data[j].volumeCategory = getCategory('volume', data[j]);
            data[j].lastPriceCategory = getCategory('lastPrice', data[j]);
            data[j].standardDeviationCategory = getCategory('standardDeviation', data[j]);
        }

        return data;
    }

    function getCategory(type, d) {
        var max = d3.max(_.pluck(data, type));
        var val = d[type] / max;

        if (val > 0.4)
            return 'Top';
        else if (val > 0.1)
            return 'Middle';
        else
            return 'Bottom';
    }

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
        var max = d3.max(_.pluck(data, val));

        d3.selectAll("circle")
                .data(getDataMapping(data, this.value))
                .transition()
                .attr('r', function (d, i) {
                    return val ? (radius * (data[i][val] / max)) : 15
                })
                .attr('cx', function (d) {
                    return d.x
                })
                .attr('cy', function (d) {
                    return d.y
                })
                .duration(1000);

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
    draw(group);

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
            for (var i = 0; i < data.length; i++) {
                var o = data[i];
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
                    return d.name
                })
                .attr("transform", function (d) {
                    return "translate(" + (d.x + (d.dx / 2)) + ", " + (d.y + 20) + ")";
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
                return "Title: " + d.title + "<br />" +
                        //"Uri: " + d.title + "<br />" +
                        "Keyword: " + d.keyword + "<br />" +
                        "Abstract: " + d.abstract.substring(0,50) + "<br />" 
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
        var quadtree = d3.geom.quadtree(data);
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