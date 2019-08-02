'use strict';

var rela = angular.module('relatedAuthor', []);
//  d3  Factory
rela.factory('d3', function () {
  return  d3;
});
rela.directive('relatedAuthor', ["d3", 'globalData', 'sparqlQuery', '$routeParams', '$window',
  function (d3, globalData, sparqlQuery, $routeParams, $window) {

    var draw = function draw(uri) {


      var organization = {};
      var norg = 0;

      //var color = d3.scaleOrdinal(d3.schemeCategory20);
      var newhost = $window.location.protocol + '//' + $window.location.hostname + ($window.location.port ? ':8080' : '') + '';
      console.log(newhost);

      var host = "http://localhost:8080";

      var svg_ = null;

      refresh(true);


      function refresh(labels) {
        $.ajax({
          type: "GET",
          dataType: "JSON", //result data type
          // url: host + "/pubman/reports/collaboratorsData?URI=https://redi.cedia.edu.ec/resource/authors/UCUENCA/file/_SAQUICELA_GALARZA_____VICTOR_HUGO_" ,
          // url: host + "/pubman/reports/collaboratorsData?URI=https://redi.cedia.edu.ec/resource/authors/UCUENCA/file/_FEYEN_____JAN_" ,
          //    url: newhost + "/pubman/reports/collaboratorsData?URI="+uri ,
          url: globalData.serverInstance + "mongo/relatedauthors?uri=" + uri,
          success: function (Result) {

            if ("Error" in Result) {
              $('#relatedArea').css("display", "block");
            } else {
              if (!labels) {

                var cmbRel = $("#cmbRel").prop('checked');
                var cmbCoa = $("#cmbCoa").prop('checked');
                var limit = $("#rangeNum").prop('value');

                var orgs = [];
                for (var org in organization) {
                  if ($("#chkOrg_" + organization [org]).prop('checked')) {
                    orgs.push(org);
                  }
                }

                var blck_node = [];
                for (var nod in Result.nodes) {
                  if (nod > 0) {
                    var nod_ = Result.nodes[nod];
                    if (orgs.indexOf(nod_.group) > -1) {
                    } else {
                      blck_node.push(nod_.id);
                    }

                  }
                }
                var blck_new_nod = [];
                var new_nod = [];
                for (var nod in Result.links) {
                  var nod_ = Result.links[nod];
                  var bbl = false;
                  if ((cmbRel && nod_.coauthor == 'false') || (cmbCoa && nod_.coauthor == 'true')) {
                    if (blck_node.indexOf(nod_.target) > -1) {
                    } else {
                      if (new_nod.length < limit) {
                        new_nod.push(nod_);
                        bbl = true;
                      }
                    }
                  }
                  if (!bbl) {
                    blck_new_nod.push(nod_.target);
                  }
                }
                console.log(blck_new_nod);
                Result.links = new_nod;
                var sssw = [];
                for (var nod = 0; nod < Result.nodes.length; nod++) {
                  var nod_ = Result.nodes[nod];
                  if (blck_new_nod.indexOf(nod_.id) > -1) {
                  } else {
                    sssw.push(nod_);
                  }
                }
                Result.nodes = sssw;

              }
              console.log(Result);
              render(Result);
              if (labels) {
                etiquetas();
                events();
              }
            }

          },
          error: function (data) {
            //document.getElementById("imgloading").style.visibility = "hidden";
            $('#relatedArea').css("display", "block");
            // alert("Error" + data.responseText);
          }
        });
      }



      function events() {
        $("#cmbRel").change(function () {
          refresh(false);
        });

        $("#cmbCoa").change(function () {
          refresh(false);
        });

        for (var org in organization) {
          $("#chkOrg_" + organization [org]).change(function () {
            refresh(false);
          });
        }

        $("#rangeNum").change(function (a) {
          refresh(false);
          console.log(a);
          $("#uptoauth").text("Limite: " + $("#rangeNum").prop('value') + " autores");
        });


      }





      function etiquetas() {
        $("#colores").append("<li class='list-group-item' style='font-weight: bold' >  LEYEND  </li>");
        $("#colores").append("<li class='list-group-item'> <input type='checkbox' id='cmbRel' value='' checked> <svg height='5' width='8'> <line x1='0' y1='0' x2='10' y2='0' style='stroke:#999;stroke-width:3'/> </svg>  Related Author  </li>");
        $("#colores").append("<li class='list-group-item'> <input type='checkbox' id='cmbCoa' value='' checked> <svg height='5' width='8'> <line x1='0' y1='0' x2='10' y2='0' style='stroke:#999;stroke-width:10'/> </svg> Coauthor Relation  </li>");
        $("#colores").append("<li class='list-group-item' style='font-weight: bold' >  ORGANIZATIONS  </li>");

        for (var org in organization) {
          console.log(color(organization [org]));
          //  $( "#colores" ).append( "<span style='color:"+color (organization [org])+"'> &#9658 "+org+" </span> " );
          $("#colores").append("<li class='list-group-item organization'> <input type='checkbox' id='chkOrg_" + organization [org] + "' value='" + organization [org] + "' checked> <span class='badge ' id='leyend' style='color:" + color(organization [org]) + "' >&#9632 </span>" + org + " </li>");
        }

        $("#colores").append("<li class='list-group-item' style='font-weight: bold' >  FILTROS  </li>");

        $("#colores").append("<li class='list-group-item'> <input type='range' min='1' max='100' value='20' class='slider' id='rangeNum'> </li>");
        $("#colores").append("<li class='list-group-item'> <span id ='uptoauth' > Limite: 20 autores</span> </li>");


      }

      var color = d3.scale.category10();

      function orgcolor(org) {
        if (!(org in organization)) {
          organization [org] = norg;
          norg = norg + 1;

        }
        return color(organization [org]);
      }
      ;

      function coauthorFactor(coauthor) {

        if (coauthor == "true") {
          return Math.sqrt(20);
        } else {
          return Math.sqrt(1);
        }
      }
      ;

      function distanceCalc(distance) {

        if (distance < 100) {

          return parseInt(distance) + 100;
        } else {
          return parseInt(distance);
        }
      }
//numero ancho enlace
      function render(graph) {
        var svg = d3.select("svg");
        svg_ = svg_ == null ? svg : svg_;
        svg = svg_;
        svg.selectAll("*").remove();
        var width = +svg.attr("width");
        var height = +svg.attr("height");
        var simulation = d3.layout.force()
                .gravity(0.05).
                linkDistance(function (d) {
                  console.log(d);
                  return distanceCalc(d.distance);
                })
                .charge(-700)
                .size([width, height]);

        function coauthorFactor(coauthor) {

          if (coauthor == "true") {
            return Math.sqrt(20);
          } else {
            return Math.sqrt(1);
          }
        }
        ;


        function distanceCalc(distance) {

          if (distance < 100) {

            return parseInt(distance) + 100;
          } else if (distance > 500) {
            return parseInt(400);
          } else {
            return parseInt(distance);
          }
        }


        function showPopover(d) {
          console.log("OVER");
          $(this).popover({
            placement: 'top',
            container: 'body',
            trigger: 'manual',
            html: true,
            content: function () {
              return  "<b>Author:</b> " + d.label + "<br />" +
                      "<b>Keywords:</b> " + d.subject + "<br />";
              //"<b>Title:</b> " + d.title + "<br />" +
              //  "<b>Cluster:</b> " + d.clusterName + "<br />";
            }
          });
          $(this).popover('show')
        }

        function removePopovers() {
          $('.popover').each(function () {
            $(this).remove();
          });
        }



        for (var g in graph.links) {
          graph.links[g].source = 0;
          graph.links[g].target = parseInt(g) + 1;
          //  console.log(graph.links[g].source );
        }

        simulation
                .nodes(graph.nodes)
                .links(graph.links)
                .start();

        var link = svg.append("g")
                .attr("class", "links")
                .selectAll("line")
                .data(graph.links)
                .enter().append("line")
                .attr("stroke-width", function (d) {
                  return coauthorFactor(d.coauthor);
                });

        link.append("title")
                .text(function (d) {
                  return  d.coauthor ? "coauthor" : "";
                });

        var node = svg.append("g")
                .attr("class", "nodes")
                .selectAll("g")
                .data(graph.nodes)
                .enter().append("g")
                .call(simulation.drag);


        var circles = node.append("circle")
                .attr("r", 26)
                .attr("fill", function (d) {
                  return orgcolor(d.group);
                });

        node.append("clipPath")
                .attr("id", "clipCircle")
                .append("circle")
                .attr("r", 22);


        var imagen = node.append("image")
                .attr("xlink:href", function (d) {
                  if (d.img != "") {
                    return d.img
                  } else {
                    return "wkhome/images/author-borderless.png";
                  }
                })
                .attr("class", "node")
                .attr("x", "-30px")
                .attr("y", "-30px")
                .attr("width", function (d) {
                  return 60
                })
                .attr("height", function (d) {
                  return 60
                })
                .attr("clip-path", "url(#clipCircle)")
                .on("mouseover", function (d) {
                  showPopover.call(this, d);
                })
                .on("mouseout", function (d) {
                  removePopovers();
                }).on("dblclick", function (d) {

          removePopovers(d);
          $window.location.hash = '/author/profile/' + d.id;
        });


        var lables = node.append("text")
                .text(function (d) {
                  return d.lastname.split(" ")[0].split(",")[0].toUpperCase();
                })
                .attr('x', -20)
                .attr('y', 40);


        simulation.on("tick", function () {
          link.attr("x1", function (d) {
            return d.source.x;
          })
                  .attr("y1", function (d) {
                    return d.source.y;
                  })
                  .attr("x2", function (d) {
                    return d.target.x;
                  })
                  .attr("y2", function (d) {
                    return d.target.y;
                  });
          node.attr("transform", function (d) {
            return "translate(" + d.x + "," + d.y + ")";
          });
        });
        //  });

        function dragstarted(d) {
          if (!d3.event.active)
            simulation.alphaTarget(0.3).restart();
          d.fx = d.x;
          d.fy = d.y;
        }

        function dragged(d) {
          d.fx = d3.event.x;
          d.fy = d3.event.y;
        }

        function dragended(d) {
          if (!d3.event.active)
            simulation.alphaTarget(0);
          d.fx = null;
          d.fy = null;
        }


      }
    }


    return {
      restrict: 'E',
      scope: {
        relatedAuthorData: '='
      },
      compile: function (element, attrs, transclude) {


        return function (scope, element, attrs) {

          draw($routeParams.authorId);


        }
      }
    }
  }
]);