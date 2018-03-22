'use strict';

var rela = angular.module('relatedAuthor', []);
//	D3	Factory
rela.factory('d3', function () {
    return	d3;
});
rela.directive('relatedAuthor', ["d3", 'globalData','sparqlQuery', '$routeParams' , '$window' ,
    function (d3, globalData, sparqlQuery , $routeParams, $window ) {

       var draw = function draw(uri){

  
  var organization =  {};
  var norg = 0;

  var color = d3.scaleOrdinal(d3.schemeCategory20);
  var newhost =  $window.location.protocol + '//' + $window.location.hostname + ($window.location.port ? ':8080' : '') + '';
  console.log (newhost);

  var host = "http://localhost:8080";
  
  $.ajax({
        type: "GET",
        dataType: "JSON", //result data type
       // url: host + "/pubman/reports/collaboratorsData?URI=https://redi.cedia.edu.ec/resource/authors/UCUENCA/file/_SAQUICELA_GALARZA_____VICTOR_HUGO_" ,
      // url: host + "/pubman/reports/collaboratorsData?URI=https://redi.cedia.edu.ec/resource/authors/UCUENCA/file/_FEYEN_____JAN_" ,
         //url: newhost + "/pubman/reports/collaboratorsData?URI="+uri ,
         url: newhost + "/mongo/relatedauthors?uri="+uri ,
        success: function(Result) {
            //document.getElementById("imgloading").style.visibility = "hidden";
           // alert("Correcto: " + Result);
            if ( "Error" in Result ){
            $('#relatedArea').css("display", "block");
           } else {
            render (Result) ;
           etiquetas (); 
                }
    
        },
        error: function(data) {
            //document.getElementById("imgloading").style.visibility = "hidden";
            alert("Error" + data.responseText);
        }
    }); 



function etiquetas () {
  $( "#colores" ).append("<li class='list-group-item' style='font-weight: bold' >  LEYEND  </li>");
  $( "#colores" ).append("<li class='list-group-item'> <svg height='5' width='8'> <line x1='0' y1='0' x2='10' y2='0' style='stroke:#999;stroke-width:3'/> </svg>  Related Author  </li>");
  $( "#colores" ).append("<li class='list-group-item'> <svg height='5' width='8'> <line x1='0' y1='0' x2='10' y2='0' style='stroke:#999;stroke-width:10'/> </svg> Coauthor Relation  </li>");
  $( "#colores" ).append("<li class='list-group-item' style='font-weight: bold' >  ORGANIZATIONS  </li>");

  for (var org in organization){
    console.log (color (organization [org]));
 //  $( "#colores" ).append( "<span style='color:"+color (organization [org])+"'> &#9658 "+org+" </span> " );
    $( "#colores" ).append("<li class='list-group-item'> <span class='badge ' id='leyend' style='color:"+color(organization [org])+"' >&#9632 </span>"+ org +" </li>");
  }

  
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

  link.append("title")
  .text(function(d) { return  d.coauthor ?  "coauthor": ""; }); 

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
        return d.lastname.split(" ")[0].split(",")[0].toUpperCase();
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
                       relatedAuthorData: '='
                   },
                  compile: function (element, attrs, transclude) {
                

                return function (scope, element, attrs) { 

                     draw(  $routeParams.authorId );

   
               }
                }
            }
        }
    ]);
