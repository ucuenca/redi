'use strict';
wkhomeApp.directive('colProject', ["d3", 'globalData', 'sparqlQuery', '$routeParams', '$window',
  function (d3, globalData, sparqlQuery, $routeParams, $window) {


    var draw = function draw(cluster, subcluster) {
      var organization = {};
      var norg = 0;
      console.log ("Graficando");
      //var newhost = $window.location.protocol + '//' + $window.location.hostname + ($window.location.port ? ':8080' : '') + '';
      var newsub = subcluster.replace("#", "%23");
      var Result = { "_id" : { "cluster" : "http://skos.um.es/unesco6/1201", "subcluster" : "http://dbpedia.org/resource/Operator_theory" }, "nodes" : [{ "mails" : "hleiva@ula.ve", "img" : "", "subject" : " strongly continuous semigroup,  mathematics,  approximate controllability, approximate controllability,  controllability, ", "id" : "https://redi.cedia.edu.ec/resource/authors/YACHAY/file/LEIVA___HUGO", "label" : "Leiva , Hugo", "orgs" : "YACHAY", "group" : "YACHAY", "lastname" : "Leiva" }, { "mails" : "", "img" : "", "subject" : " tercias,  suma,  teorema de pitagoras, cuadrados, ", "id" : "https://redi.cedia.edu.ec/resource/authors/UCUENCA/oai-pmh/VASQUEZ_BERNAL__MARCO_VINICIO", "label" : "Vásquez Bernal, Marco Vinicio", "orgs" : "UCUENCA", "group" : "UCUENCA", "lastname" : "Vásquez Bernal" }, { "mails" : "", "img" : "", "subject" : " tercias,  suma,  teorema de pitagoras, cuadrados, ", "id" : "https://redi.cedia.edu.ec/resource/authors/UCUENCA/oai-pmh/VASQUEZ_CHIQUITO__MERCEDES_ELIZABETH", "label" : "Vásquez Chiquito, Mercedes Elizabeth", "orgs" : "UCUENCA", "group" : "UCUENCA", "lastname" : "Vásquez Chiquito" }, { "mails" : "", "img" : "", "subject" : ", ", "id" : "https://redi.cedia.edu.ec/resource/scopus_author/23099667100", "label" : "Di Teodoro , Antonio", "orgs" : "YACHAY;USFQ", "group" : "YACHAY;USFQ", "lastname" : "Di Teodoro" }, { "mails" : "", "img" : "", "subject" : " meta-monogenic functions,  clifford algebras,  clifford type algebras,  fundamental solutions, clifford algebras, ", "id" : "https://redi.cedia.edu.ec/resource/scopus_author/55743709800", "label" : "Antonio Nicola, Di Teodoro", "orgs" : "YACHAY;USFQ", "group" : "YACHAY;USFQ", "lastname" : "Di Teodoro" }, { "mails" : "", "img" : "", "subject" : " auslander, hochschild cohomology,  m-cluster tilted algebras,  fundamental group,  simple connectedness, ", "id" : "https://redi.cedia.edu.ec/resource/scopus_author/7006455618", "label" : "Bustamante , Juan Carlos", "orgs" : "USFQ", "group" : "USFQ", "lastname" : "BUSTAMANTE" }, { "mails" : "", "img" : "", "subject" : " scilab,  linear algebra,  subspaces vector,  linear dependence and independence, linear algebra, ", "id" : "https://redi.cedia.edu.ec/resource/scopus_author/57201313300", "label" : "Dávila , Paúl S.", "orgs" : "ESPE", "group" : "ESPE", "lastname" : "Dávila" }, { "mails" : "", "img" : "", "subject" : " geometric morphometry,  morphotypes,  immature,  south american fruit fly,  linear morphometrics, ", "id" : "https://redi.cedia.edu.ec/resource/scopus_author/56976403400", "label" : "Tigrero Salas , Juan O.", "orgs" : "ESPE", "group" : "ESPE", "lastname" : "Tigrero Salas" }, { "mails" : "", "img" : "", "subject" : " meta-n-weighted-monogenic functions, clifford type algebras,  meta-monogenic functions,  multi-meta-weighted- monogenic functions,  monogenic functions, ", "id" : "https://redi.cedia.edu.ec/resource/scopus_author/57210446490", "label" : "García , Eusebio Ariza", "orgs" : "YACHAY", "group" : "YACHAY", "lastname" : "García" }, { "mails" : "", "img" : "", "subject" : " finsler geometry,  extended relativity in clifford spaces, clifford algebras,  grand unification,  gravity, ", "id" : "https://redi.cedia.edu.ec/resource/scopus_author/7202237774", "label" : "Castro , Carlos", "orgs" : "UTPL", "group" : "UTPL", "lastname" : "CASTRO" }], "links" : [{ "coauthor" : "true", "distance" : "10", "source" : "https://redi.cedia.edu.ec/resource/authors/UCUENCA/oai-pmh/VASQUEZ_BERNAL__MARCO_VINICIO", "target" : "https://redi.cedia.edu.ec/resource/authors/UCUENCA/oai-pmh/VASQUEZ_CHIQUITO__MERCEDES_ELIZABETH" }, { "coauthor" : "true", "distance" : "10", "source" : "https://redi.cedia.edu.ec/resource/authors/UCUENCA/oai-pmh/VASQUEZ_CHIQUITO__MERCEDES_ELIZABETH", "target" : "https://redi.cedia.edu.ec/resource/authors/UCUENCA/oai-pmh/VASQUEZ_BERNAL__MARCO_VINICIO" }, { "coauthor" : "true", "distance" : "10", "source" : "https://redi.cedia.edu.ec/resource/scopus_author/55743709800", "target" : "https://redi.cedia.edu.ec/resource/scopus_author/57210446490" }, { "coauthor" : "true", "distance" : "10", "source" : "https://redi.cedia.edu.ec/resource/scopus_author/57210446490", "target" : "https://redi.cedia.edu.ec/resource/scopus_author/55743709800" }] };
   

       $.ajax({
        type: "GET",
        dataType: "JSON", //result data type
        url: globalData.serverInstance + "mongo/instbyproject",
        success: function (Result) {
        //var Result = { "nodes" : [{"id" : "http://test1" , "name" : "UDA" , "nproy" : "10" , "img" : ""}, {"id" : "http://test2" , "name" : "UCE" , "nproy" : "50" , "img" : ""} , {"id" : "http://test3" , "name" : "UCUENCA" , "nproy" : "1" , "img" : ""}] , "links" : [{"source" : "http://test1" , "target" : "http://test2" , "nproy" : "30" } , {"source" : "http://test2" , "target" : "http://test1" , "nproy" : "30" } , {"source" : "http://test2" , "target" : "http://test3" , "nproy" : "2" }] };

        render(Result);

          if ("Error" in Result) {
            console.log ("NO DATA");
            $('#relatedArea').css("display", "block");
          } else {
            console.log ("Llamando RENDER");
            render(Result);
           // etiquetas();
          }

        },
        error: function (data) {
        var Result = { "nodes" : [{"id" : "http://test1" , "name" : "UDA" , "nproy" : "10" , "img" : ""}, {"id" : "http://test2" , "name" : "UCE" , "nproy" : "50" , "img" : ""} , {"id" : "http://test3" , "name" : "UCUENCA" , "nproy" : "1" , "img" : ""}] , "links" : [{"source" : "http://test1" , "target" : "http://test2" , "nproy" : "30" } , {"source" : "http://test2" , "target" : "http://test1" , "nproy" : "30" } , {"source" : "http://test2" , "target" : "http://test3" , "nproy" : "2" }] };
          render(Result);
        }
      });

      function orgcolor(org) {
        if (!(org in organization)) {
          organization[org] = norg;
          norg = norg + 1;

        }
        return "white";
      }
      ;

      function etiquetas() {
        $("#colores").append("<li class='list-group-item' style='font-weight: bold' >  LEGEND  </li>");
        $("#colores").append("<li class='list-group-item'> <svg height='5' width='8'> <line x1='0' y1='0' x2='10' y2='0' style='stroke:#999;stroke-width:10'/> </svg> Coauthor Relation  </li>");
        $("#colores").append("<li class='list-group-item' style='font-weight: bold' >  ORGANIZATIONS  </li>");

        for (var org in organization) {
          $("#colores").append("<li class='list-group-item'> <span class='badge ' id='leyend' style='color:" + "blue" + "' >&#9632 </span>" + org + " </li>");
        }


      }






      function coauthorFactor(coauthor) {
        if (coauthor == "true") {
          return Math.sqrt(10);
        } else {
          return Math.sqrt(1);
        }
      };

      function distanceCalc(distance) {
        if (distance > 5) {
          console.log(200 - parseInt(distance) * 10);
          //return 250 - parseInt(distance) * 10;
          var expand =  Math.floor(Math.random() * 200); 
          //return 200  + expand ;
          return 50  + expand;
        } else if (distance > 2) {
          console.log(200 - parseInt(distance) * 10);
          //return 250 - parseInt(distance) * 10;
          return 200;
        } else {
          return 100;
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
                .gravity(0.09)
                .linkDistance(function (d) {
                  console.log(d);
                  //return d.distance;
                  return 200;//distanceCalc(d.source.coautor);
                })
                .linkStrength(0.2)
                .charge(-700)
                .size([width, height]);

        function colaborationFactor(d) {
            
            
            return Math.sqrt(12+d.nproy*0.4) ;
          
        };

         function tipy () {
          return -10;
        }

        var tool_tip = d3.tip()
        .attr("class", "d3-tip")
        .offset( function (d) { return  [Math.abs(d.source.y - d.target.y)/2, 0]; } )
        .html(function(d) { console.log (d.source.y - d.target.y);  return "Proy. Compartidos: " + d.nproy; });
         svg.call(tool_tip);

        function showPopover(d) {
          console.log("OVER");
          console.log(d);

          $("line[id*='" + d.id + "']").css("stroke", "#777171");

          $(this).popover({
            placement: 'top',
            container: 'body',
            trigger: 'manual',
            html: true,
            content: function () {
              return "<b> Institución :</b> " + d.name + "<br />" +
                      "<b> Num. Proyectos :</b> " + d.nproy + "<br />";
            }
          });
          $(this).popover('show')
        }

        function removePopovers(d) {
          $("line[id*='" + d.id + "']").css("stroke", "#c9c3c3");
          $('.popover').each(function () {
            $(this).remove();
          });
        }

        var table = [];
        for (var n in graph.nodes) {
          console.log (n , graph.nodes[n]);
          if (!table.hasOwnProperty(graph.nodes[n].id)) {
            table[graph.nodes[n].id] = n;
            //graph.nodes[n].coautor = 0;
          }
        }

        for (var g in graph.links) {
         // graph.nodes[table[graph.links[g].source]].coautor = graph.nodes[table[graph.links[g].source]].coautor + 1;
          graph.links[g].source = parseInt(table[graph.links[g].source]);
          graph.links[g].target = parseInt(table[graph.links[g].target]);
        }
        console.log (graph.nodes);
        console.log (graph.links);

        simulation
                .nodes(graph.nodes)
                .links(graph.links)
                .start();

        var link = container
                .attr("class", "links_scl")
                .selectAll("line")
                .data(graph.links)
                .enter().append("line")
                .attr("stroke-width", function (d) {
                  return colaborationFactor(d);
                })
                .attr("stroke", function (d) {
                  return "#c9c3c3";
                })
                .attr("id", function (d) {
                  return d.source.id + " " + d.target.id;
                }).on('mouseover', tool_tip.show)
                .on('mouseout', tool_tip.hide);

        link.append("title")
                .text(function (d) {
                  return d.coauthor ? "coauthor" : "";
                });

        var node = container
                .attr("class", "nodes")
                .attr("class", "org")
                .selectAll("g")
                .data(graph.nodes)
                .enter().append("g")
                .call(simulation.drag);

        var circles = node.append("circle")
                .attr("r", function (d) {
                  return  25+parseInt(d.nproy)*0.2;
                })
                .attr("fill", function (d) {
                  return orgcolor(d.group);
                });

        node.append("clipPath")
                .attr("id", "clipCircle")
                .append("circle")
                .attr("r", 26);

        var imagen = node.append("image")
                .attr("xlink:href", function (d) {
                  if (d.img != "") {
                    return d.img;
                  } else {
                    return "wkhome/images/orgs/"+d.name+".png";
                  }
                })
                .attr("class", "node")
                .attr("x", "-30px")
                .attr("y", "-30px")
                .attr("width", function (d) {
                  return 60;
                })
                .attr("height", function (d) {
                  return 60
                })
                .attr("clip-path", "url(#clipCircle)")
                .on("mouseover", function (d) {
                  showPopover.call(this, d);
                })
                .on("mouseout", function (d) {
                  removePopovers(d);
                })
                .on("dblclick", function (d) {
                  removePopovers(d);
                  $window.location.hash = '/author/profile/' + d.id;
                });


        var lables = node.append("text")
                .text(function (d) {
                  return d.name.toUpperCase();
                })
                .attr('x', -10)
                .attr('y', function (d) { return 40+ d.nproy*0.1 });

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
        datacl: '=',
      },
      compile: function (element, attrs, transclude) {

        return function (scope, element, attrs) {
          draw ("1","2");

        }
      }
    }
  }
]);
