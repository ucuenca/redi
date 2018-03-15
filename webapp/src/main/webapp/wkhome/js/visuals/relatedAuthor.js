'use strict';

var rela = angular.module('relatedAuthor', []);
//	D3	Factory
rela.factory('d3', function () {
    return	d3;
});
rela.directive('relatedAuthor', ["d3", 'globalData','sparqlQuery',
    function (d3, globalData, sparqlQuery) {

 var draw = function draw(data){

  
  var organization =  {};
  var norg = 0;

  var color = d3.scaleOrdinal(d3.schemeCategory20);

  render (data);
  etiquetas ();
  /*
  $.ajax({
        type: "GET",
        dataType: "JSON", //result data type
       // url: host + "/pubman/reports/collaboratorsData?URI=https://redi.cedia.edu.ec/resource/authors/UCUENCA/file/_SAQUICELA_GALARZA_____VICTOR_HUGO_" ,
      // url: host + "/pubman/reports/collaboratorsData?URI=https://redi.cedia.edu.ec/resource/authors/UCUENCA/file/_FEYEN_____JAN_" ,
         url: host + "/pubman/reports/collaboratorsData?URI=https://redi.cedia.edu.ec/resource/authors/UTPL/oai-pmh/PIEDRA__NELSON" ,
        success: function(Result) {
            //document.getElementById("imgloading").style.visibility = "hidden";
           // alert("Correcto: " + Result);
          
         
         etiquetas ();
    
        },
        error: function(data) {
            //document.getElementById("imgloading").style.visibility = "hidden";
            alert("Error" + data.responseText);
        }
    }); */

//numero color
var graph = {
  "nodes": [
    {"id": "Saquicela", "group": 1,},
    {"id": "Palacios", "group": 1 } ,
    {"id": "Espinoza", "group": 2 }
  ],
  "links": [
    {"source": "Saquicela", "target": "Palacios", "value": 1 , "distance":80},
    {"source": "Espinoza", "target": "Palacios", "value": 20 , "distance":100},
  ]
};

function etiquetas () {

 
  for (var org in organization){
    console.log (color (organization [org]));
 //  $( "#colores" ).append( "<span style='color:"+color (organization [org])+"'> &#9658 "+org+" </span> " );
    $( "#colores" ).append("<li class='list-group-item'> <span class='badge' style='color:"+color(organization [org])+"' >&#9632 </span>"+ org +" </li>");
  }

  $( "#colores" ).append("<li class='list-group-item'> <svg height='5' width='8'> <line x1='0' y1='0' x2='10' y2='0' style='stroke:#999;stroke-width:10'/> Coauthor Relationship  </li>");
  $( "#colores" ).append("<li class='list-group-item'> <svg height='5' width='8'> <line x1='0' y1='0' x2='10' y2='0' style='stroke:#999;stroke-width:5'/>Possible Relationship  </li>");
}

function orgcolor (org) {
  if (! (org in organization)) {
     organization [org] = norg;
     norg = norg+1;
  
  }
    return color (organization [org]);
  };

 function coauthorFactor (coauthor) {

  if (coauthor == "true"){
    return Math.sqrt(20);
  }else {
    return Math.sqrt(1);
  }
 };
 
 function distanceCalc (distance) {

    if (distance <  100 ){

     return parseInt(distance) + 100;
    }else {
     return parseInt(distance) ;  
    }
 }
//numero ancho enlace
function render (graph) {

var svg = d3.select("svg"),
    width = +svg.attr("width"),
    height = +svg.attr("height");




var simulation = d3.forceSimulation()
    .force("link", d3.forceLink().id(function(d) { return d.id; }).distance(function(d) { console.log (d); return distanceCalc(d.distance);}))
    //.force("link", d3.forceLink().distance(function(d) {return d.distance;}).strength(0.1))
    .force("charge", d3.forceManyBody())
    .force("center", d3.forceCenter(width / 2.5, height / 2))
    .force('collision', d3.forceCollide().radius(function(d) {
    return 30;
  }))



  var link = svg.append("g")
      .attr("class", "links")
    .selectAll("line")
    .data(graph.links)
    .enter().append("line")
      .attr("stroke-width", function(d) {  console.log (d.coauthor); return coauthorFactor (d.coauthor); });

  var node = svg.append("g")
      .attr("class", "nodes")
    .selectAll("g")
    .data(graph.nodes)
    .enter().append("g");
    
  var circles = node.append("circle")
      .attr("r", 30)
      .attr("fill", function(d) { console.log (orgcolor(d.group)+" "+d.group) ;return orgcolor(d.group); }) ;


      node.append("clipPath")
                        .attr("id", "clipCircle")
                        .append("circle")
                        .attr("r", 26);


     var imagen = node.append ("image")
       .attr("xlink:href", function (d) { if (d.img != ""){return d.img}else{return "wkhome/images/author-borderless.png"; }})
       .attr("class", "node")
       .attr("x", "-30px")
       .attr("y", "-30px")
       .attr("width", function (d) { return 60})
       .attr("height", function (d) { return 60})
       .attr("clip-path", "url(#clipCircle)")
       .call(d3.drag()
          .on("start", dragstarted)
          .on("drag", dragged)
          .on("end", dragended))
        .on("mouseover", function (d) {
                               // tip.html(d.subject);
                               // tip.show(d);
        //showPopover.call(this, d);
        showPopover.call(this, d);
                    }) 
        .on("mouseout", function (d) {
                        removePopovers();
                    });  

    /*   .on("mouseover", function (d) {
                              tip.html(d.subject);
                                tip.show(d);
                        });*/

  var lables = node.append("text")
      .text(function(d) {
        return d.lastname.split(" ")[0].split(",")[0];
      })
      .attr('x', -20)
      .attr('y',  40);

 /* node.append("title")
      .text(function(d) { return "hola"; }); */

  simulation
      .nodes(graph.nodes)
      .on("tick", ticked);

  simulation.force("link")
      .links(graph.links);

  function ticked() {
    link
        .attr("x1", function(d) { return d.source.x; })
        .attr("y1", function(d) { return d.source.y; })
        .attr("x2", function(d) { return d.target.x; })
        .attr("y2", function(d) { return d.target.y; });

    node
        .attr("transform", function(d) {
          return "translate(" + d.x + "," + d.y + ")";
        })
  }
//}
//);

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
 function showPopover(d) {
    console.log ("OVER");
                $(this).popover({
                    placement: 'top',
                    container: 'body',
                    trigger: 'manual',
                    html: true,
                    content: function () {
                        return  "<b>Author:</b> " + d.label + "<br />" +
                                "<b>Keywords:</b> " + d.subject + "<br />" ;
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

}


      }

        return {
            restrict: 'E',
            scope: {
                'ctrlFn': "&",
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
                console.log (element);
                console.log (attrs);
                console.log (transclude);
                var elementWidth = parseInt(element.css('width'));
                var elementHeight = parseInt(element.css('height'));
                var width = attrs.ctWidth ? attrs.ctWidth : elementWidth;
                var height = attrs.ctHeight ? attrs.ctHeight : elementHeight;



                var svg = d3.select(element[0]);

                //	Return	the	link	function
                return	function (scope, element, attrs) {
                 //   draw();
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
                    scope.$watch('relatedAuthorData', function (newVal, oldVal, scope) {
                        //	Update	the	chart
                           console.log (scope);
                           console.log(newVal);
                           console.log(oldVal);
                             var data = scope.relatedAuthorData;
                           draw(data);
                    /*
                        if (data) {
                            var jsonld = data.data;
                            var schema = data.schema;
                            var fields = schema.fields;
                            var mappedData = [];

                            _.each(jsonld['@graph'], function (keyword, idx) {
                                if (keyword["rdfs:label"])
                                {
                                  var pubsvalue =  keyword[fields[1]]["@value"] > 50 ?  "+50" : keyword[fields[1]]["@value"];
                                  var name = typeof keyword[fields[0]] === 'string' ? keyword[fields[0]] : _(keyword[fields[0]]).first();
                                  mappedData.push({id:keyword["@id"], label: name, value: pubsvalue});
                                }
                            });
                            var pageTitle = "";
                            pageTitle = _.findWhere(jsonld['@graph'],{"@type": "uc:pagetitle"})["uc:viewtitle"];
                            draw(svg, width, height, mappedData, scope, attrs, pageTitle);
                        }*/
                    }, true);

                };
            }
        };
    }]);
