'use strict';
wkhomeApp.directive('subCluster', ["d3", 'globalData', 'sparqlQuery', '$routeParams', '$window',
  function(d3, globalData, sparqlQuery, $routeParams, $window) {

    var draw = function draw(cluster, subcluster) {
      var organization = {};
      var norg = 0;

      //var newhost = $window.location.protocol + '//' + $window.location.hostname + ($window.location.port ? ':8080' : '') + '';
      var newsub = subcluster.replace("#", "%23");

      $.ajax({
        type: "GET",
        dataType: "JSON", //result data type
        url: globalData.serverInstance + "mongo/authorByArea?cluster=" + cluster + "&subcluster=" + newsub,
        success: function(Result) {

          if ("Error" in Result) {
            $('#relatedArea').css("display", "block");
          } else {
            render(Result);
            etiquetas();
          }

        },
        error: function(data) {
          $('#relatedArea').css("display", "block");
        }
      });



      function etiquetas() {
        $("#colores").append("<li class='list-group-item' style='font-weight: bold' >  LEYEND  </li>");
        $("#colores").append("<li class='list-group-item'> <svg height='5' width='8'> <line x1='0' y1='0' x2='10' y2='0' style='stroke:#999;stroke-width:10'/> </svg> Coauthor Relation  </li>");
        $("#colores").append("<li class='list-group-item' style='font-weight: bold' >  ORGANIZATIONS  </li>");

        for (var org in organization) {
          $("#colores").append("<li class='list-group-item'> <span class='badge ' id='leyend' style='color:" + color(organization[org]) + "' >&#9632 </span>" + org + " </li>");
        }


      }

      var color = d3.scale.category10();

      function orgcolor(org) {
        if (!(org in organization)) {
          organization[org] = norg;
          norg = norg + 1;

        }
        return color(organization[org]);
      };

      function coauthorFactor(coauthor) {
        if (coauthor == "true") {
          return Math.sqrt(10);
        } else {
          return Math.sqrt(1);
        }
      };

      function distanceCalc(distance) {
        if (distance > 3) {
          console.log(200 - parseInt(distance) * 10);
          return 250 - parseInt(distance) * 10;
        } else {
          return 150;
        }
      }

      //numero ancho enlace
      function render(graph) {
        var margin = {
            top: -5,
            right: -5,
            bottom: -5,
            left: -5
          },
          width = 960 - margin.left - margin.right,
          height = 600 - margin.top - margin.bottom;

        var zoom = d3.behavior.zoom()
          .center([width / 2, height / 2])
          .scale(1)
          .scaleExtent([0.6, 1])
          .on("zoom", zoomed);

        var svg = d3.select("svg")
          .attr("width", width + margin.left + margin.right)
          .attr("height", height + margin.top + margin.bottom)
          .append("g")
          .attr("width", width + margin.left + margin.right)
          .attr("height", height + margin.top + margin.bottom)
          .attr("transform", "translate(" + margin.left + "," + margin.right + ")").call(zoom),
          width = +svg.attr("width"),
          height = +svg.attr("height");

        var rect = svg.append("rect")
          .attr("width", width)
          .attr("height", height)
          .style("fill", "white")
          .style("pointer-events", "all");

        var container = svg.append("g");

        function zoomed() {
          container.attr("transform", "translate(" + d3.event.translate + ")scale(" + d3.event.scale + ")");
        }

        var simulation = d3.layout.force()
          .gravity(0.09).
        linkDistance(function(d) {
            console.log(d);
            return distanceCalc(d.source.coautor);
          })
          .charge(-600)
          .size([width, height]);

        function coauthorFactor(coauthor) {

          if (coauthor == "true") {
            return Math.sqrt(20);
          } else {
            return Math.sqrt(1);
          }
        };

        function showPopover(d) {
          console.log("OVER");
          console.log(d);

          $("line[id*='" + d.id + "']").css("stroke", "#777171");

          $(this).popover({
            placement: 'top',
            container: 'body',
            trigger: 'manual',
            html: true,
            content: function() {
              return "<b>Author:</b> " + d.label + "<br />" +
                "<b>Keywords:</b> " + d.subject + "<br />";
            }
          });
          $(this).popover('show')
        }

        function removePopovers(d) {
          $("line[id*='" + d.id + "']").css("stroke", "#c9c3c3");
          $('.popover').each(function() {
            $(this).remove();
          });
        }

        var table = [];
        for (var n in graph.nodes) {
          if (!table.hasOwnProperty(graph.nodes[n].id)) {
            table[graph.nodes[n].id] = n;
            graph.nodes[n].coautor = 0;
          }
        }

        for (var g in graph.links) {
          graph.nodes[table[graph.links[g].source]].coautor = graph.nodes[table[graph.links[g].source]].coautor + 1;
          graph.links[g].source = parseInt(table[graph.links[g].source]);
          graph.links[g].target = parseInt(table[graph.links[g].target]);
        }

        simulation
          .nodes(graph.nodes)
          .links(graph.links)
          .start();

        var link = container
          .attr("class", "links_scl")
          .selectAll("line")
          .data(graph.links)
          .enter().append("line")
          .attr("stroke-width", function(d) {
            return coauthorFactor(d.coauthor);
          })
          .attr("stroke", function(d) {
            return "#c9c3c3";
          })
          .attr("id", function(d) {
            return d.source.id + " " + d.target.id;
          });

        link.append("title")
          .text(function(d) {
            return d.coauthor ? "coauthor" : "";
          });

        var node = container
          .attr("class", "nodes")
          .selectAll("g")
          .data(graph.nodes)
          .enter().append("g")
          .call(simulation.drag);

        var circles = node.append("circle")
          .attr("r", 26)
          .attr("fill", function(d) {
            return orgcolor(d.group);
          });

        node.append("clipPath")
          .attr("id", "clipCircle")
          .append("circle")
          .attr("r", 22);

        var imagen = node.append("image")
          .attr("xlink:href", function(d) {
            if (d.img != "") {
              return d.img
            } else {
              return "wkhome/images/author-borderless.png";
            }
          })
          .attr("class", "node")
          .attr("x", "-30px")
          .attr("y", "-30px")
          .attr("width", function(d) {
            return 60
          })
          .attr("height", function(d) {
            return 60
          })
          .attr("clip-path", "url(#clipCircle)")
          .on("mouseover", function(d) {
            showPopover.call(this, d);
          })
          .on("mouseout", function(d) {
            removePopovers(d);
          })
          .on("dblclick", function(d) {
            removePopovers(d);
            $window.location.hash = '/author/profile/' + d.id;
          });


        var lables = node.append("text")
          .text(function(d) {
            return d.lastname.split(" ")[0].split(",")[0].toUpperCase();
          })
          .attr('x', -20)
          .attr('y', 40);

        simulation.on("tick", function() {
          link.attr("x1", function(d) {
              return d.source.x;
            })
            .attr("y1", function(d) {
              return d.source.y;
            })
            .attr("x2", function(d) {
              return d.target.x;
            })
            .attr("y2", function(d) {
              return d.target.y;
            });
          node.attr("transform", function(d) {
            return "translate(" + d.x + "," + d.y + ")";
          });
        });

        function dragstarted(d) {
          if (!d3.event.active) simulation.alphaTarget(0.3).restart();
          d.fx = d.x;
          d.fy = d.y;
        }

        function dragged(d) {
          d.fx = d3.event.x;
          d.fy = d3.event.y;
        }

        function dragended(d) {
          if (!d3.event.active) simulation.alphaTarget(0);
          d.fx = null;
          d.fy = null;
        }
      }
    }

    return {
      restrict: 'E',
      scope: {
        datacl: '=',
      },
      compile: function(element, attrs, transclude) {

        return function(scope, element, attrs) {
          scope.$watch('datacl', function(newVal, oldVal, scope) {
            $("#colores").html("");
            $("svg").html("");
            if (scope.datacl) {
              draw(scope.datacl.cluster, scope.datacl.subcluster);
            }
          }, true);
        }
      }
    }
  }
]);
