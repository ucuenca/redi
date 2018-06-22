'use strict';

var pieChart = angular.module('cloudTag', []);
//	D3	Factory
pieChart.factory('d3', function () {
    return	d3;
});
pieChart.directive('cloudTag', ["$routeParams", "d3", 'globalData', 'sparqlQuery',
    function ($routeParams, d3, globalData, sparqlQuery) {

        var getRelatedAuthorsByClustersQuery = globalData.PREFIX
                + ' CONSTRUCT {  <http://ucuenca.edu.ec/wkhuska/resultTitle> a uc:pagetitle. <http://ucuenca.edu.ec/wkhuska/resultTitle> uc:viewtitle "Authors Related With {0}"  .         ?subject rdfs:label ?name.         ?subject uc:total ?totalPub   }   WHERE {   { '
                + ' SELECT DISTINCT  ?subject ?name (count(?pub) as ?totalPub)'
                + ' WHERE { '
                + '   GRAPH <' + globalData.clustersGraph + '> '
                + '         { '
                + '                 ?cluster <http://ucuenca.edu.ec/resource/hasPerson> <{1}> .'
                + '                 ?cluster <http://ucuenca.edu.ec/resource/hasPerson> ?subject.'
                + '                 ?subject foaf:publications ?pub'
                + '                 {'
                + '                     SELECT ?name'
                + '                     {'
                + '                         graph <' + globalData.centralGraph + '>'
                + '                         {'
                + '                             ?subject foaf:name ?name.'
                + '                         }'
                + '                     }'
                + '             }'
                + '         } '
                + '     } group by ?subject ?name '
                + '          }}    ';


        var getRelatedAuthorsByPublicationsQuery = globalData.PREFIX
                + '  CONSTRUCT { '
                + ' <http://ucuenca.edu.ec/wkhuska/resultTitle> a uc:pagetitle. <http://ucuenca.edu.ec/wkhuska/resultTitle> uc:viewtitle "Authors Related With {0}" . '
                + '        ?subject rdfs:label ?name. '
                + '        ?subject uc:total ?totalPub '
                + '  } '
                + '  WHERE { '
                + '  { '
                + '     SELECT ?subject (count(DISTINCT(?publicationUri)) as ?totalPub) ?name '
                + '         WHERE { '
                + '             GRAPH <' + globalData.centralGraph + '> { '
                + '             ?subject foaf:publications ?publicationUri. '
                + '             ?publicationUri dcterms:subject ?keySub .'
                + '             ?keySub rdfs:label ?quote. '
                + '             FILTER (mm:fulltext-search(?quote, "{1}" )) .'
                + '             ?subject foaf:name ?name.  } '
                + '             } '
                + '         GROUP BY ?subject ?name '
                + '  } '
                + ' } LIMIT 100';


        var chart, clear, click, collide, collisionPadding, connectEvents, data, force, gravity, hashchange, height, idValue, jitter, label, margin, maxRadius, minCollisionRadius, mouseout, mouseover, node, rScale, rValue, textValue, tick, transformData, update, updateActive, updateLabels, updateNodes, width;
        var scope;
        var attrs;

        width;// = 980;
        height;// = 510;
        data = [];
        node = null;
        label = null;
        margin = {
            top: 5,
            //top: 1350,
            right: 0,
            bottom: 0,
            left: 0
        };
        maxRadius = 55;
        rScale = d3.scale.sqrt().range([0, maxRadius]);
        rValue = function (d) {
            return parseInt(d.value);
        };
        idValue = function (d) {
            return d.label;
        };
        textValue = function (d) {
            return d.label;
        };
        collisionPadding = 10;
        minCollisionRadius = 3;
        jitter = 0.01;
        transformData = function (rawData) {
            rawData.forEach(function (d) {
                d.value = parseInt(d.value);
            });
            return rawData;
        };
        tick = function (e) {
            var dampenedAlpha;
            dampenedAlpha = e.alpha * 0.01;
            node.each(gravity(dampenedAlpha)).each(collide(jitter)).attr("transform", function (d) {
                return "translate(" + d.x + "," + d.y + ")";
            });
            return label.style("left", function (d) {
                /*if(d.label == "Amino Acid") {
                 d = d;
                 }*/      //POSITION OF LABEL
                return (17 + (margin.left + d.x) - d.dx / 2) + "px";
            }).style("top", function (d) {
                return (50 + (margin.top + d.y) - d.dy / 2) + "px";
            });
        };

        //main method to plot
        chart = function (selection) {
            return selection.each(function (rawData) {
                var maxDomainValue, svg, svgEnter;
                data = transformData(rawData);
                maxDomainValue = d3.max(data, function (d) {
                    return rValue(d);
                });
                rScale.domain([0, maxDomainValue]);
                svg = d3.select(this).selectAll("svg").data([data]);
                svgEnter = svg.enter().append("svg");
                svg.attr("width", width + margin.left + margin.right);
                svg.attr("height", height + margin.top + margin.bottom);
                node = svgEnter.append("g").attr("id", "bubble-nodes").attr("transform", "translate(" + margin.left + "," + margin.top + ")");
                node.append("rect").attr("id", "bubble-background").attr("width", width).attr("height", height).on("click", clear);
                label = d3.select(this).selectAll("#bubble-labels").data([data]).enter().append("div").attr("id", "bubble-labels");
                update();
                /*hashchange();
                 return d3.select(window).on("hashchange", hashchange);*/
                return d3.select(window);
            });
        };
        update = function () {
            data.forEach(function (d, i) {
                return d.forceR = Math.max(minCollisionRadius, rScale(rValue(d)));
            });
            force.nodes(data).start();
            updateNodes();
            return updateLabels();
        };
        updateNodes = function () {
            node = node.selectAll(".bubble-node").data(data, function (d) {
                return idValue(d);
            });
            node.exit().remove();
            return node.enter().append("a").attr("class", "bubble-node").attr("xlink:href", function (d) {
                return "#" + (encodeURIComponent(idValue(d)));
            }).call(force.drag).call(connectEvents).append("circle").attr("id", "kcircle").attr("r", function (d) {
                return rScale(rValue(d));
            });
        };
        updateLabels = function () {
            var labelEnter;
            label = label.selectAll(".bubble-label").data(data, function (d) {
                return idValue(d);
            });
            label.exit().remove();
            labelEnter = label.enter().append("a").attr("class", "bubble-label")
//            .attr("href", function (d) {
//                return "#" + (encodeURIComponent(idValue(d)));
//            })
//            .call(force.drag).call(connectEvents);
            labelEnter.append("div").attr("class", "bubble-label-name").text(function (d) {
                return textValue(d);
            }).call(connectEvents);
            labelEnter.append("div").attr("class", "bubble-label-value").text(function (d) {
                return rValue(d);
            });
            label.style("font-size", function (d) {
                return Math.max(9, rScale(rValue(d) / 5)) + "px";
            }).style("width", function (d) {
                return 0.1 * rScale(rValue(d)) + "px";
            });
            label.append("span").text(function (d) {
                return textValue(d);
            }).each(function (d) {
                return d.dx = Math.max(2.5 * rScale(rValue(d)), this.getBoundingClientRect().width);
            }).remove();
            label.style("width", function (d) {
                return d.dx + "px";
            });
            return label.each(function (d) {
                return d.dy = (Number(this.getBoundingClientRect().height) -110).toString();
            });
        };
        gravity = function (alpha) {
            var ax, ay, cx, cy;
            cx = width / 2;
            cy = height / 2;
            ax = alpha / 8;
            ay = alpha;
            return function (d) {
                d.x += (cx - d.x) * ax;
                return d.y += (cy - d.y) * ay;
            };
        };
        collide = function (jitter) {
            return function (d) {
                return data.forEach(function (d2) {
                    var distance, minDistance, moveX, moveY, x, y;
                    if (d !== d2) {
                        x = d.x - d2.x;
                        y = d.y - d2.y;
                        distance = Math.sqrt(x * x + y * y);
                        minDistance = d.forceR + d2.forceR + collisionPadding;
                        if (distance < minDistance) {
                            distance = (distance - minDistance) / distance * jitter;
                            moveX = x * distance;
                            moveY = y * distance;
                            d.x -= moveX;
                            d.y -= moveY;
                            d2.x += moveX;
                            return d2.y += moveY;
                        }
                    }
                });
            };
        };
        connectEvents = function (d) {
            d.on("click", click);
            d.on("mouseover", mouseover);
            return d.on("mouseout", mouseout);
        };
        clear = function () {
            return location.replace("#");
        };
        function executeRelatedAuthors(querytoExecute, divtoload) {
            var sparqlquery = querytoExecute;

            sparqlQuery.querySrv({query: sparqlquery}, function (rdf) {
                jsonld.compact(rdf, globalData.CONTEXT, function (err, compacted) {
                    if (compacted)
                    {
                        var entity = compacted["@graph"];
                        if (entity)
                        {
                            var authorInfo = $('div.tree-node-author-info .' + divtoload);
                            authorInfo.html('');
                            var values = entity.length ? entity : [entity];
                            authorInfo.append('<h4 style="padding: 20px" class="totalauthors text-success"> Autores: ' + ( values.length -1 ) + '</h4>');
                            var div = $('<div>');

                            authorInfo.append(div);
                            _.map(values, function (value) {
                                if (value["rdfs:label"] && value["uc:total"]["@value"])
                                {
                                    var anchor = $("<a class='relatedauthors' target='blank' onclick = 'clickonRelatedauthor(\"" + value["@id"] + "\")'  >").text("");
                                    //anchor.append('<img src="/wkhome/images/author-ec.png" class="img-rounded" alt="Logo Cedia" width="20" height="20"        >');4
                                    var name = typeof value["rdfs:label"] === 'string' ? value["rdfs:label"] : _(value["rdfs:label"] ).first();
                                    anchor.append(name + "(" + value["uc:total"]["@value"] + ")");
                                    div.append(anchor);
                                    div.append("</br>");
                                    return anchor;
                                }
                            });
                        }
                        waitingDialog.hide();
                    }
                    waitingDialog.hide();
                });
            }); // end  sparqlQuery.querySrv(...
        }
        ;
        function relatedAuthors(root) {

            var keyword = root.label;
            if (keyword)
            {
                //********** AUTORES RELACIONADOS - POR CLUSTERING *********//
                //var query = String.format(getRelatedAuthorsByClustersQuery, author["foaf:name"], keyword);
                //executeRelatedAuthors(query, "authorsByClusters");
                //********** AUTORES RELACIONADOS - POR PUBLICACION *********//
                var query = String.format(getRelatedAuthorsByPublicationsQuery, keyword, keyword);
                executeRelatedAuthors(query, "authorsByPublications");
            }//end if author["foaf:name"]
        }
        ;
        click = function (d) {
            relatedAuthors(d);
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
            if (infoBar) {
                var keyword = d.label;
                var headbar = $('div.head-info');
                headbar.find('title').text("ddddddtitletitle");
                headbar.html('');
                //Function to show the buttons for the reports
                scope.$parent.exportReport(keyword);
                var div = $('<div>');
                var label;
                if ($routeParams.lang === "es") {
                    label= $('<span class="label label-primary" style="font-size:35px; background-color: #003769; opacity: 0.8;">').text("PUBLICACIONES Y AUTORES RELACIONADOS CON: " + keyword);
                } else {
                    label= $('<span class="label label-primary" style="font-size:35px; background-color: #003769; opacity: 0.8;">').text("PUBLICATIONS AND AUTHORS RELATED WITH: " + keyword);
                }
                div.append(label);
                div.append("</br>");
                headbar.append(div);
                //var sparqlDescribe = "DESCRIBE <" + id + ">";
                var sparqlPublications = globalData.PREFIX
                        + ' CONSTRUCT { ?keyword uc:publication ?publicationUri. '
                        + ' ?publicationUri dct:contributors ?subject . '
                        + ' ?subject foaf:name ?name . '
                        + ' ?subject a foaf:Person . '
                        + ' ?publicationUri a bibo:Document. '
                        + ' ?publicationUri dct:title ?title. '
                        + ' ?publicationUri bibo:abstract ?abstract. '
                        + ' ?publicationUri bibo:uri ?uri. '
                        + ' } '
                        + ' WHERE {'
                        + ' GRAPH <' + globalData.centralGraph + '>'
                        + ' {'
                        + ' ?subject foaf:publications ?publicationUri .'
                        + ' ?subject foaf:name ?name .'
                        + ' ?publicationUri dct:title ?title . '
                        + ' OPTIONAL{ ?publicationUri bibo:abstract  ?abstract. } '
                        + ' OPTIONAL{ ?publicationUri bibo:uri  ?uri. } '
                        + ' ?publicationUri dcterms:subject ?keySub. '
                        + ' ?keySub rdfs:label ?quote. '
                        + ' FILTER (mm:fulltext-search(?quote, "' + keyword+ '" )) .'
                        + '  BIND(REPLACE( "' + keyword + '", " ", "_", "i") AS ?key) .'
                        + '  BIND(IRI(?key) as ?keyword)'
                        + ' }'
                        + '}';
                waitingDialog.show("Searching publications with the keyword: " + keyword);

                sparqlQuery.querySrv({query: sparqlPublications}, function (rdf) {

                    jsonld.compact(rdf, globalData.CONTEXT, function (err, compacted) {
                      waitingDialog.show("Searching publications with the keyword: " + keyword);

                        if (compacted){
                            var entity = compacted["@graph"];
                            //var final_entity = _.where(entity, {"@type": "bibo:Document"});
                            var final_entity = entity;
                            var values = final_entity.length ? final_entity : [final_entity];
                            //send data to getKeywordTag Controller
                            scope.ctrlFn({value: entity});
                            waitingDialog.hide();

                        } else
                        {
                            waitingDialog.hide();
                        }
                    });
                });
            }
            return d3.event.preventDefault();
        };
        hashchange = function () {
            var id;
            id = decodeURIComponent(location.hash.substring(1)).trim();
            return updateActive(id);
        };
        updateActive = function (id) {
            node.classed("bubble-selected", function (d) {
                return id === idValue(d);
            });
            if (id.length > 0) {
                return d3.select("#status").html("<h3>The word <span class=\"active\">" + id + "</span> is now active</h3>");
            } else {
                return d3.select("#status").html("<h3>No word is active</h3>");
            }
        };
        mouseover = function (d) {
            return node.classed("bubble-hover", function (p) {
                return p === d;
            });
        };
        mouseout = function (d) {
            return node.classed("bubble-hover", false);
        };
        chart.jitter = function (_) {
            if (!arguments.length) {
                return jitter;
            }
            jitter = _;
            force.start();
            return chart;
        };
        chart.height = function (_) {
            if (!arguments.length) {
                return height;
            }
            height = _;
            return chart;
        };
        chart.width = function (_) {
            if (!arguments.length) {
                return width;
            }
            width = _;
            return chart;
        };
        chart.r = function (_) {
            if (!arguments.length) {
                return rValue;
            }
            rValue = _;
            return chart;
        };

        var draw = function draw(element, widthEl, heightEl, data, scopeEl, attrsEl) {
            var pubInfo = element;
            pubInfo.html('');
            width = widthEl;
            height = heightEl;
            scope = scopeEl;
            attrs = attrsEl;

            force = d3.layout.force().gravity(0).charge(0).size([width, height]).on("tick", tick);
            element.datum(data).call(chart);

        }

        return {
            restrict: 'E',
            scope: {
                'ctrlFn': "&",
                data: '='
            },
            compile: function (element, attrs, transclude) {
                //	Define	the	dimensions	for	the	chart
                //var width = 960, height = 500;
                var elementWidth = parseInt(element.css('width'));
                var elementHeight = parseInt(element.css('height'));
                var width = attrs.ctWidth ? attrs.ctWidth : elementWidth;
                var height = attrs.ctHeight ? attrs.ctHeight : elementHeight;

                //	Create	a	SVG	root	element
                var svg = d3.select(element[0]);

                //	Return	the	link	function
                return	function (scope, element, attrs) {
                    //	Watch	the	data	attribute	of	the	scope
                    scope.$watch('data', function (newVal, oldVal, scope) {
                        //	Update	the	chart
                        var data = scope.data;
                        if (data) {
                            draw(svg, width, height, data, scope, attrs);
                        }
                    }, true);
                };
            }
        };
    }]);
