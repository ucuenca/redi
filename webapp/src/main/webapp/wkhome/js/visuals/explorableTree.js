'use strict';
var explorableTree = angular.module('explorableTree', []);
//	D3	Factory
explorableTree.factory('d3', function () {
    return	d3;
});
explorableTree.directive('explorableTree', ['d3', 'globalData', 'sparqlQuery', 'authorRestQuery', '$window',
    function (d3, globalData, sparqlQuery, authorRestQuery, $window) {
        function numero(value) {
            if (!/^([0-9])*$/.test(value)) {
                return false;
            } else {
                return true;
            }
        }
        var getRelatedAuthorsByClustersQuery = globalData.PREFIX
                + 'CONSTRUCT {  <http://ucuenca.edu.ec/wkhuska/resultTitle> a uc:pagetitle.'
                + ' <http://ucuenca.edu.ec/wkhuska/resultTitle> uc:viewtitle "Authors Related With {0}".'
                + ' ?subject rdfs:label ?name.'
                + '?subject uc:total ?totalPub } '
                + 'WHERE {'
                + '  {'
                + '    SELECT ?subject ?name (COUNT(DISTINCT ?relpublication) as ?totalPub)'
                + '    WHERE {'
                + '        GRAPH <' + globalData.clustersGraph + '> {'
                + '          ?cluster foaf:publications ?publication .'
                + '          ?publication uc:hasPerson <{1}> .'
                + '          ?cluster foaf:publications ?relpublication .'
                + '          ?relpublication uc:hasPerson ?subject .'
                + '          {'
                + '            SELECT ?name {'
                + '              GRAPH <' + globalData.centralGraph + '> { '
                + '                ?subject foaf:name ?name .'
                + '              }'
                + '            }'
                + '          }'
                + '          FILTER (?subject != <{1}>)'
                + '        }'
                + '    }'
                + '    GROUP BY ?subject ?name'
                + '  }'
                + '}';

        var getRelatedAuthorsByPublicationsQuery = globalData.PREFIX
                + 'CONSTRUCT {'
                + '  <http://ucuenca.edu.ec/wkhuska/resultTitle> a uc:pagetitle;'
                + '                                              uc:viewtitle "Authors Related With {0}".'
                + '  ?subject rdfs:label ?name.'
                + '  ?subject uc:total ?totalPub .'
                + '  ?subject foaf:img ?img .'
                + '} WHERE  {'
                + '  SELECT ?subject ?name (COUNT(?pub) as ?totalPub) ?img'
                + '  WHERE { GRAPH <' + globalData.centralGraph + '> {'
                + '    <{1}> foaf:publications ?pub.'
                + '    ?subject foaf:publications ?pub;'
                + '             foaf:name ?name.'
                + '     OPTIONAL{?subject  foaf:img ?img.}'
                + '    FILTER(<{1}> != ?subject)'
                + '  }} GROUP BY ?subject ?name ?img'
                + '}';

        var draw = function draw(svg, width, height, data, scope) {

            var pubInfo = svg;
            pubInfo.html('');
            // Misc. variables
            var i = 0;
            var duration = 750;
            var rootAuthor;
            var root;
            var rightPaneWidth = 350;
            var exploredArtistIds = [];
            var exploredPublicationsIds = [];
            // avoid clippath issue by assigning each image its own clippath
            var clipPathId = 0;
            var lastExpandedNode;
            // size of the diagram
            var viewerWidth = width;
            var viewerHeight = height;
            var tree = d3.layout.tree()
                    .size([height, width]);
            var diagonal = d3.svg.diagonal()
                    .projection(function (d) {
                        return [d.y, d.x];
                    });
            // Define the zoom function for the zoomable tree

            function zoom() {
                svgGroup.attr("transform", "translate(" + d3.event.translate + ")scale(" + d3.event.scale + ")");
            }

            var zoomListener = d3.behavior.zoom().scaleExtent([0.1, 3]).on("zoom", zoom);
            svg
                    .attr("width", viewerWidth)
                    .attr("height", viewerHeight)
                    .attr("class", "tree-overlay")
                    .call(zoomListener);
            function updateWindow() {
                viewerWidth = $(window).width() - rightPaneWidth;
                viewerHeight = $(window).height();
                svg.attr("width", viewerWidth).attr("height", viewerHeight);
                if (lastExpandedNode) {
                    centerNode(lastExpandedNode);
                }
            }
            function executeRelatedAuthors(querytoExecute, divtoload) {
                sparqlQuery.querySrv({query: querytoExecute}, function (rdf) {
                    jsonld.compact(rdf, globalData.CONTEXT, function (err, compacted) {
                        var authorInfo = $('div.tree-node-author-info .' + divtoload);
                        authorInfo.html('');

                        if (compacted) {
                            var entity = compacted["@graph"];
                            if (entity) {
                                var values = entity.length ? entity : [entity];
                                var div = $('<div>');
                                authorInfo.append(div);

                                values = _.sortBy(values, function (value) {
                                    if (value.hasOwnProperty('uc:total')) {
                                        return parseInt(value["uc:total"]["@value"]);
                                    } else
                                        return -1;
                                }).reverse();

                                values = _.first(values, 20);

                                _.map(values, function (value) {
                                    if (value["rdfs:label"] && value["uc:total"]["@value"]) {
                                        var authorname = typeof value["rdfs:label"] == "string" ? value["rdfs:label"] : _.first(value["rdfs:label"], 1);
                                        var anchor = $("<a class='relatedauthors' target='blank' onclick = 'return clickonRelatedauthor(\"" + value["@id"] + "\")'  >").text("");
                                        var img = value["foaf:img"] ? value["foaf:img"]["@id"] : "/wkhome/images/author-ec.png";
                                        anchor.append('<img src="' + img + '" class="img-rounded" alt="Logo Cedia" width="20" height="20"        >');
                                        anchor.append(authorname + "(" + value["uc:total"]["@value"] + ")");
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
                var id = root.author["@id"];
                var author = _.findWhere(root.author.jsonld["@graph"], {"@id": id, "@type": "foaf:Person"});
                if (author["foaf:name"]) {
                    //********** AUTORES RELACIONADOS - POR CLUSTERING *********//
                    var relatedAuthorsClustersQuery = String.format(getRelatedAuthorsByClustersQuery, author["foaf:name"], id);
                    executeRelatedAuthors(relatedAuthorsClustersQuery, "authorsByClusters");
                    //********** AUTORES RELACIONADOS - POR PUBLICACION *********//
                    var relatedAuthosPublicationsQuery = String.format(getRelatedAuthorsByPublicationsQuery, author["foaf:name"], id);
                    executeRelatedAuthors(relatedAuthosPublicationsQuery, "authorsByPublications");
                }
            }
            ;
            // Function to center node when clicked/dropped so node doesn't get lost when collapsing/moving with large amount of children.
            function centerNode(source) {
                lastExpandedNode = source;
                var scale = zoomListener.scale();
                var x = -source.y0;
                var y = -source.x0;
                x = x * scale + viewerWidth / 2;
                y = y * scale + viewerHeight / 2;
                // d3.select('#tree-container g').transition()
                svg.select('g').transition()
                        .duration(duration)
                        .attr("transform", "translate(" + x + "," + y + ")scale(" + scale + ")");
                zoomListener.scale(scale);
                zoomListener.translate([x, y]);
            }
            //second parameter "if click right event"
            function setChildrenAndUpdateForAuthor(node, right) {
                var id = node.author["@id"];
                var modelAuthor = {"foaf:name": {label: "Nombre:", containerType: "div"},
                    "uc:fullName": {label: "Institución: ", containerType: "div"},
                    "uc:city": {label: "Ciudad: ", containerType: "div"},
                    "uc:province": {label: "Provincia: ", containerType: "div"},
                    "dct:subject": {label: "Subjects: ", containerType: "div"}
                };
                var authorDataSparqlQuery = globalData.PREFIX
                        + ' CONSTRUCT '
                        + '  { '
                        + '     <' + id + '> foaf:name ?name; '
                        + '     uc:fullName ?provname; '
                        + '     uc:city ?city; '
                        + '     uc:province ?province; '
                        + '     dct:subject ?subjects '
                        + ' } '
                        + ' WHERE { '
                        + '     GRAPH <' + globalData.centralGraph + '> { '
                        + '         <' + id + '> foaf:name ?name; '
                        + '         dct:provenance ?provenance; '
                        + '         OPTIONAL {  <' + id + '> foaf:topic_interest [rdfs:label ?subjects]. }'
                        + '         { '
                        + '             SELECT DISTINCT ?provenance ?city ?provname ?province '
                        + '             WHERE'
                        + '             {'
                        + '                 graph <' + globalData.endpointsGraph + '> '
                        + '                 { '
                        + '                     ?provenance uc:fullName ?provname.'
                        + '                     ?provenance uc:city ?city.'
                        + '                     ?provenance uc:province ?province.'
                        + '                 }'
                        + '             }'
                        + '         }'
                        + '       } '
                        + '  }  ';
                /*
                 * execute sparql to get name, city, province, and institution of the author
                 */
                var autInfo = $('div.authorinfo');
                autInfo.html('');
                sparqlQuery.querySrv({query: authorDataSparqlQuery}, function (rdf) {
                    jsonld.compact(rdf, globalData.CONTEXT, function (err, compacted) {
                        if (compacted["@graph"]) {
                            var entity = compacted["@graph"][0];
                            _.each(_.keys(modelAuthor), function (key, idx) {
                                if (entity[key]) {
                                    //append into a div container
                                    var div = $('<div class="explorableTree pubinfo"  style="background-color: #F7F8E0">');
                                    var label = $('<span class="label-author">').text(modelAuthor[key].label)
                                    div.append(label);
                                    autInfo.append(div);
                                    var values = entity[key].length ? entity[key] : [entity[key]];
                                    if(key==='foaf:name')
                                      values = typeof(values) === 'string' ? values : _.first(values);
                                    if(location.href.indexOf('/es/') !== -1 && key === 'uc:fullName')
                                      values = _.findWhere(entity["uc:fullName"], {"@language": "es"})["@value"];
                                    else if (location.href.indexOf('/en/') !== -1 && key === 'uc:fullName')
                                      values = _.findWhere(entity["uc:fullName"], {"@language": "en"})["@value"];
                                    if (typeof (values) === 'string') {
                                        var span = $('<span class="field-value">').text(values);
                                        div.append(span);
                                    } else {
                                        _.map(values, function (value, idx) {
                                            if (idx <= 5) {
                                                var span = $('<span class="field-value" style="font-size:70%">').text(value);
                                                div.append(span);
                                                div.append(", ");
                                            }
                                        });
                                    }
                                }
                            });
                            autInfo.append("<hr>");
                        }
                    });
                }); // end  sparqlQuery.querySrv(...


                if (right) {
                    var id = node.author["@id"];
                    var author = _.findWhere(node.author.jsonld["@graph"], {"@id": id, "@type": "foaf:Person"});
                    if (author["foaf:name"]) {
                        var sparqlquery = String.format(getRelatedAuthorsByClustersQuery, author["foaf:name"], id);
                        waitingDialog.show("Loading Authors Related with " + author["foaf:name"]);
                        sparqlQuery.querySrv({query: sparqlquery}, function (rdf) {

                            jsonld.compact(rdf, globalData.CONTEXT, function (err, compacted) {
                                if (compacted)
                                {
                                    var entity = compacted["@graph"];
                                    //var final_entity = _.where(entity, {"@type": "bibo:Document"});
                                    if (entity)
                                    {
                                        var values = entity.length ? entity : [entity];
                                        //send data to  Controller
                                        scope.ifrightClick({value: compacted});
                                    }
                                    waitingDialog.hide();
                                } else
                                {
                                    waitingDialog.hide();
                                }
                            });
                        }); // end  sparqlQuery.querySrv(...
                    }//end if authoir["foaf:name"]

                } else {
                    var infoBar = $('div.tree-node-info');
                    ///if (infoBar) {
                    var id = node.author["@id"];
                    var author = _.findWhere(node.author.jsonld["@graph"], {"@id": id, "@type": "foaf:Person"});
                    if (!(author['@id'].indexOf($window.location.hostname) > 0) && (node.author.jsonld["@graph"].length <= 1)) {
                        //       infoBar.find('h4').text("Informacion del CoAutor");
                        //      infoBar = $('div.tree-node-info .entityInfo');
                        //infoBar.find('div#title').text('');
                        //infoBar.find('div#title').text("Author: " + publication["dcterms:title"]);
//                        var anchor = $("<a target='blank'>").attr('href', id.replace('/xr/', '/')) //SOLO DBLP & MICROSOFT ACADEMICS
//                                .text("Click here for more info...");
//                        //

                        var authorToFind = author["@id"];
                        waitingDialog.show("Searching publications of the external author");
                        var getExternalAuthorFromLocal = globalData.PREFIX
                                + 'CONSTRUCT  { <' + authorToFind + '> ?propaut ?obbaut. ?obbaut ?propub ?objpub.'
                                + '    } '
                                + 'WHERE'
                                + '{'
                                + 'graph <' + globalData.externalAuthorsGraph + '>'
                                + '        {'
                                + '<' + authorToFind + '> ?propaut ?obbaut.'
                                + '?obbaut ?propub ?objpub.'
                                + '}}';
                        sparqlQuery.querySrv({query: getExternalAuthorFromLocal}, function (rdf) {
                            jsonld.compact(rdf, globalData.CONTEXT, function (err, compacted) {
                                if (!compacted["@graph"]) {
                                    authorRestQuery.query({resource: authorToFind}, function (rdf) {

                                        jsonld.compact(rdf, globalData.CONTEXT, function (err, compacted) {
                                            if (!compacted["@graph"]) {
                                                waitingDialog.hide();
                                                alert("Papers not obtained for external author, you can try again");
                                            } else {
                                                var rs = compacted;
                                                node.author.jsonld["@context"] = _.extend(node.author.jsonld["@context"], globalData.CONTEXT);
                                                node.author.jsonld["@graph"] = _.flatten([node.author.jsonld["@graph"], compacted["@graph"]]);
                                                setChildrenAndUpdate('publication', node, node.author.jsonld, {"@type": "bibo:AcademicArticle"}, globalData.CONTEXT, exploredPublicationsIds);
                                                waitingDialog.hide();
                                            }
                                        });
                                    });
                                } else {
                                    node.author.jsonld["@context"] = _.extend(node.author.jsonld["@context"], globalData.CONTEXT);
                                    node.author.jsonld["@graph"] = _.flatten([node.author.jsonld["@graph"], compacted["@graph"]]);
                                    setChildrenAndUpdate('publication', node, node.author.jsonld, {"@type": "bibo:AcademicArticle"}, globalData.CONTEXT, exploredPublicationsIds);
                                    waitingDialog.hide();
                                }
                            });
                        });

                        infoBar.append(anchor);
                    } else {
                        //AE.getRelated(node.author.id, exploredArtistIds).then(function(authors) {
                        var model;

                        if (node.author.jsonld["@graph"].length > 1) {
                            model = node.author.jsonld;
                            setChildrenAndUpdate('publication', node, model, {"@type": "bibo:AcademicArticle"}, globalData.CONTEXT, exploredPublicationsIds);
                        } else {
                            var nodeId = node.author['@id'];
                            var queryPublications = globalData.PREFIX
                                    + 'CONSTRUCT {'
                                    + '  <' + nodeId + '> foaf:publications ?pub .'
                                    + '  ?pub a bibo:AcademicArticle ;  '
                                    + '         dct:title ?title;'
                                    + '}'
                                    + 'WHERE { GRAPH <' + globalData.centralGraph + '> { '
                                    + '  <' + nodeId + '> foaf:publications ?pub .'
                                    + '  ?pub dct:title ?title.'
                                    + '}}';
                            sparqlQuery.querySrv({query: queryPublications}, function (rdf) {

                                jsonld.compact(rdf, globalData.CONTEXT, function (err, data) {
                                    var rs = data;
                                    node.author.jsonld["@context"] = _.extend(node.author.jsonld["@context"], globalData.CONTEXT);
                                    node.author.jsonld["@graph"] = _.flatten([node.author.jsonld["@graph"], data["@graph"]]);
                                    setChildrenAndUpdate('publication', node, node.author.jsonld, {"@type": "bibo:AcademicArticle"}, globalData.CONTEXT, exploredPublicationsIds);
                                });
                            });
                        }
                    }//End else if(!author["foaf:name"] &&  ...
                }//end IF clickright
            }

            function setChildrenAndUpdate(entityName, node, jsonld, filter, context, stack) {
                if (!node.children) {
                    node.children = []
                }
                var entities;
                if (filter === "null") {
                    entities = jsonld;
                } else {
                    entities = _.where(jsonld["@graph"], filter);
                }
                entities.forEach(function (entity) {
                    var child = {};
                    child[entityName] = {'@id': entity["@id"],
                        jsonld: {'@context': context, '@graph': [entity], '@id': jsonld['@id']}};
                    child['children'] = null;
                    node.children.push(child);
                    stack.push(entity["@id"]);
                });
                update(node, !_.isEmpty(entities));
                centerNode(node);
            }

            function setChildrenAndUpdateForPub(node) {
                var id = node.publication["@id"];
                if (id) {
                    var query = globalData.PREFIX
                      + "CONSTRUCT {"
                      + "  <" + id + "> a bibo:AcademicArticle ;"
                      + "								dct:title ?title; "
                      + "								bibo:abstract ?abstract; "
                      + "								bibo:uri ?uri; "
                      + "								bibo:doi ?doi; "
                      + "								bibo:pages ?pages; "
                      + "								bibo:created ?created; "
                      + "								bibo:issue ?issue; "
                      + "								bibo:volume ?volumne; "
                      + "								dct:contributor ?contributorURI; "
                      + "								dct:creator ?creatorURI; "
                      + "								dct:publisher ?publisher; "
                      + "								dct:subject ?keyword; "
                      + "								dct:isPartOf ?isPartOfURI. "
                      + " ?contributorURI foaf:name ?contributor."
                      + " ?contributorURI foaf:img ?imgContributor."
                      + " ?creatorURI foaf:name ?creator."
                      + " ?creatorURI foaf:img ?imgCreator."
                      + " ?isPartOfURI a bibo:Journal."
                      + " ?isPartOfURI bibo:uri ?eqJournalUri."
                      + " ?isPartOfURI rdfs:label ?isPartOf. } "
                      + "WHERE { GRAPH <" + globalData.centralGraph + "> { "
                      + "  <" + id + "> dct:title ?title."
                      + "  OPTIONAL {<" + id + "> bibo:abstract ?abstract. }"
                      + "  OPTIONAL {<" + id + "> bibo:uri ?uri. }"
                      + "  OPTIONAL {<" + id + "> bibo:doi ?doi. }"
                      + "  OPTIONAL {<" + id + "> bibo:pages ?pages. }"
                      + "  OPTIONAL {<" + id + "> bibo:created ?created. }"
                      + "  OPTIONAL {<" + id + "> bibo:issue ?issue. }"
                      + "  OPTIONAL {<" + id + "> bibo:volume ?volumne. }"
                      + "  OPTIONAL {<" + id + "> dct:contributor ?contributorURI. }"
                      + "  OPTIONAL {?contributorURI foaf:name ?contributor. }"
                      + "  OPTIONAL {?contributorURI foaf:img ?imgContributor. }"
                      + "  OPTIONAL {<" + id + "> dct:creator ?creatorURI. }"
                      + "  OPTIONAL {?creatorURI foaf:name ?creator.}"
                      + "  OPTIONAL {?creatorURI foaf:img ?imgCreator.}"
                      + "  OPTIONAL {<" + id + "> dct:publisher ?publisherURI.}"
                      + "  OPTIONAL {?publisherURI rdfs:label ?publisher. }"
                      + "  OPTIONAL {<" + id + "> dct:subject ?keywordURI. }"
                      + "  OPTIONAL {?keywordURI rdfs:label ?keyword}"
                      + "  OPTIONAL {<" + id + "> dct:isPartOf ?isPartOfURI. }"
                      + "  OPTIONAL { ?isPartOfURI <http://www.w3.org/2002/07/owl#sameAs> ?eqJournal. }"
                      + "  OPTIONAL {graph <"+globalData.latindexGraph+"> { ?eqJournal bibo:uri ?eqJournalUri. }  }"
                      + "  OPTIONAL {?isPartOfURI rdfs:label ?isPartOf}}}";

                    sparqlQuery.querySrv({query: query}, function (rdf) {
                        jsonld.compact(rdf, globalData.CONTEXT, function (err, compacted) {
                          var authors = [];
                          var pubclean = {};
                          var jouclean = {};
                          var publication = _.findWhere(compacted["@graph"], {"@type": "bibo:AcademicArticle"});
                          var journal = _.findWhere(compacted["@graph"], {"@type": "bibo:Journal"});
                          var exportpath = location.protocol+ '//' + location.host + location.pathname + "resource?uri=" + publication["@id"] + "&format={0}";
                          var formats = [{url: String.format(exportpath,"application/rdf%2Bjson"), label: "RDF/JSON", img: "/wkhome/images/rdf-json.png"} ,
                          {url: String.format(exportpath, "application/rdf%2Bxml"), label: "RDF/XML", img: "/wkhome/images/rdf-xml.png"},
                          {url: String.format(exportpath, "text/turtle"), label: "Turtle", img: "/wkhome/images/turtle.png"},
                          {url: String.format(exportpath, "text/rdf%2Bn3"), label: "N3", img: "/wkhome/images/n3.png"},
                          {url: String.format(exportpath, "application/ld%2Bjson"), label: "JSON-LD", img: "/wkhome/images/json-ld.png"}];

                          /**
                          * Get only a element in string format when there's a list,
                          * the first element is returned.
                          */
                          var getStrVal = function(element) {
                            if(typeof(element) === 'string')
                              return element;
                            return _.first(element);
                          };

                          /**
                          * Build an author object and selects only a name.
                          */
                          var buildAuthor = function(v) {
                            var aux = typeof(v) === 'string' ? _.findWhere(compacted["@graph"], {"@id": v}) : _.findWhere(compacted["@graph"], v);
                            var author = {};
                            author["@id"] = aux['@id'];
                            author["foaf:name"] = getStrVal(aux['foaf:name']);
                            author["@type"] = "foaf:Person"
                            author['foaf:img'] = aux["foaf:img"] ? aux["foaf:img"] : undefined;
                            authors.push(author);
                          };

                          /**
                          * Classify URLS from publications based on URL.
                          * If a domain/pdf is found, there's an image. Otherwise the general one is web.
                          */
                          var classifyURLS = function(val, key) {
                            var url;

                            if(typeof(val) === 'string')  {
                              url = val;
                              val = {"@id": url};
                            } else if(typeof(val) === 'object') url = val["@id"];
                            else return;

                            if (url.indexOf("scholar.google.com") !== -1) {
                              val["source"] = "scholar";
                              val["label"] = "Google Scholar";
                              val["img"] = "/wkhome/images/scholar.png";
                            } else if (url.indexOf("academic.microsoft.com") !== -1) {
                              val["source"] = "academics";
                              val["label"] = "Academics Knowledge";
                              val["img"] = "/wkhome/images/academics.png";
                            } else if (url.indexOf("dblp.com") !== -1) {
                              val["source"] = "dblp";
                              val["label"] = "DBLP";
                              val["img"] = "/wkhome/images/dblp.png";
                            } else if (url.indexOf("scopus.com") !== -1) {
                              val["source"] = "scopus";
                              val["label"] = "Scopus";
                              val["img"] = "/wkhome/images/scopus.png";
                            } else if (url.indexOf("researchgate.net") !== -1) {
                              val["source"] = "research-gate";
                              val["label"] = "Research Gate";
                              val["img"] = "/wkhome/images/researchgate.png";
                            }else if (url.indexOf("latindex.org") !== -1) {
                              val["source"] = "latindex";
                              val["label"] = "Latindex";
                              val["img"] = "/wkhome/images/latindex.jpg";
                            } else if (url.indexOf("springer.com") !== -1) {
                              val["source"] = "springer";
                              val["label"] = "Springer";
                              val["img"] = "/wkhome/images/springer.png";
                            } else if (url.indexOf("pdf") !== -1) {
                              val["source"] = "pdf";
                              val["label"] = "PDF source";
                              val["img"] = "/wkhome/images/pdf.png";
                            } else {
                              val["source"] = "web";
                              val["label"] = "Web source";
                              val["img"] = "/wkhome/images/world.png";
                            }
                            return val;
                          };



                          pubclean.id = publication['@id'];
                          pubclean.title = getStrVal(publication['dct:title']);
                          pubclean.abstract = getStrVal(publication['bibo:abstract']);
                          pubclean.doi = getStrVal(publication['bibo:doi']);
                          pubclean.pages = getStrVal(publication['bibo:pages']);
                          pubclean.created = getStrVal(publication['bibo:created']);
                          pubclean.issue = getStrVal(publication['bibo:issue']);//
                          pubclean.volume = getStrVal(publication['bibo:volume']);
                          pubclean.publisher = getStrVal(publication['dct:publisher']);
                          pubclean.uri = _.mapObject(publication["bibo:uri"], classifyURLS);
                          pubclean.subjects = publication['dct:subject'];

                          if (journal && journal['@id']){
                            jouclean.id = journal['@id'];
                            jouclean.journalName = getStrVal(journal['rdfs:label']);
                            if (journal['bibo:uri']){
                                    jouclean.journalURL = getStrVal(journal['bibo:uri']['@id']);
                                    jouclean.journalProvenanceIcon = _.mapObject(journal["bibo:uri"], classifyURLS);
                                    if (jouclean.journalProvenanceIcon['@id']){
                                        jouclean.journalProvenanceIcon=jouclean.journalProvenanceIcon['@id'];
                                    }
                            }else{
                                jouclean.journalURL = jouclean.id;
                            }
                          }


                          _.each(publication["dct:contributor"],buildAuthor);
                          _.each(publication["dct:creator"], buildAuthor);
                          pubclean.authors = _.uniq(authors, function(author){
                            return author['@id'];
                          });

                          pubclean.formats = formats;

                          scope.publication = pubclean;
                          scope.publication.journal = jouclean;
                          scope.$apply();
                          /** Used for validations and test purposes.
                          authors.push({"@id": node.parent.author["@id"], "foaf:name": "SAQUI", "@type": "foaf:Person"});
                          authors.push({"@id": "http://localhost:8080/resource/authors/jorge-mauricio-espinoza-mejia", "foaf:name": "Mauricio", "@type": "foaf:Person"});
                          setChildrenAndUpdate('author', node, {"@graph": authors}, 'foaf:Person', globalData.CONTEXT, exploredArtistIds);
                          */
                          setChildrenAndUpdate('author', node, {"@graph": _.without(pubclean.authors, _.findWhere(authors, {"@id": node.parent.author["@id"]}))}, 'foaf:Person', globalData.CONTEXT, exploredArtistIds);
                        });
                      });
                }
            }

            function initWithArtist(author) {
                var id = author["@graph"][0]["@id"];
                exploredArtistIds.push(id);
                return {
                    'author': {"@id": id, jsonld: author},
                    'children': null
                }
            }
            ;
            function initWithGenre(genreName) {
                return {
                    'genre': {
                        'name': genreName
                    },
                    'children': null,
                }
            }
            ;
            function isAuthor(d) {
                return 'author' in d;
            }

            function isPublication(d) {
                return 'publication' in d;
            }


            function removeExpandedId(d) {
                if (d.children) {
                    d.children.forEach(function (node) {
                        removeExpandedId(node);
                    });
                }
                if (isAuthor(d)) {
                    var indexToRem = exploredArtistIds.indexOf(d.author.id);
                    exploredArtistIds.splice(indexToRem, 1);
                } else {

                }
            }

            function removeChildrenFromExplored(d) {
                d.children.forEach(function (node) {
                    removeExpandedId(node);
                });
            }

            // Toggle children function
            function toggleChildrenRightClick(d) {
                if (isAuthor(d)) {
                    setChildrenAndUpdateForAuthor(d, true);
                }
                return d;
            }
            // Toggle children function
            function toggleChildren(d) {
                if (d.children) {
                    var pubInfo = $('div.tree-node-info .entityInfo');
                    pubInfo.html('');
                    var linkExternalGoogle = $('div.try-external-search .external-search-google');
                    linkExternalGoogle.html('');

                    var linkExternalDblp = $('div.try-external-search .external-search-dblp');
                    linkExternalDblp.html('');

                    var linkExternalScopus = $('div.try-external-search .external-search-scopus');
                    linkExternalScopus.html('');

                    removeChildrenFromExplored(d);
                    d.children = null;
                    update(d, false);
                    centerNode(d);
                } else {
                    if (isAuthor(d)) {
                        setChildrenAndUpdateForAuthor(d, false);
                    } else if (isPublication(d)) {
                        setChildrenAndUpdateForPub(d);
                    }
                }
                return d;
            }

            function click(d) {
                //          $('div.tree-node-info .entityInfo').html('');
                d = toggleChildren(d);
            }


            function contextMenu() {
                var height,
                        width,
                        margin = 0.1, // fraction of width
                        items = ["ad", "addd"],
                        rescale = false,
                        style = {
                            'rect': {
                                'mouseout': {
                                    'fill': 'rgb(244,244,244)',
                                    'stroke': 'white',
                                    'stroke-width': '1px'
                                },
                                'mouseover': {
                                    'fill': 'rgb(200,200,200)'
                                }
                            },
                            'text': {
                                'fill': 'steelblue',
                                'font-size': '13'
                            }
                        };
                function menu(x, y) {
                    d3.select('.context-menu').remove();
                    scaleItems();
                    // Draw the menu
                    d3.select('svg')
                            .append('g').attr('class', 'context-menu')
                            .selectAll('tmp')
                            .data(items).enter()
                            .append('g').attr('class', 'menu-entry')
                            .style({'cursor': 'pointer'})
                            .on('mouseover', function () {
                                d3.select(this).select('rect').style(style.rect.mouseover)
                            })
                            .on('mouseout', function () {
                                d3.select(this).select('rect').style(style.rect.mouseout)
                            });
                    d3.selectAll('.menu-entry')
                            .append('rect')
                            .attr('x', x)
                            .attr('y', function (d, i) {
                                return y + (i * height);
                            })
                            .attr('width', width)
                            .attr('height', height)
                            .style(style.rect.mouseout);
                    d3.selectAll('.menu-entry')
                            .append('text')
                            .text(function (d) {
                                return d;
                            })
                            .attr('x', x)
                            .attr('y', function (d, i) {
                                return y + (i * height);
                            })
                            .attr('dy', height - margin / 2)
                            .attr('dx', margin)
                            .style(style.text);
                    // Other interactions
                    d3.select('body')
                            .on('click', function () {
                                d3.select('.context-menu').remove();
                            });
                }

                menu.items = function (e) {
                    if (!arguments.length)
                        return items;
                    for (i in arguments)
                        items.push(arguments[i]);
                    rescale = true;
                    return menu;
                };

                // Automatically set width, height, and margin;
                function scaleItems() {
                    if (rescale) {
                        d3.select('g.tree-node').selectAll('tmp')
                                .data(items).enter()
                                .append('text')
                                .text(function (d) {
                                    return d;
                                })
                                .style(style.text)
                                .attr('x', -1000)
                                .attr('y', -1000)
                                .attr('class', 'tmp');
                        var z = d3.selectAll('.tmp')[0]
                                .map(function (x) {
                                    return x.getBBox();
                                });
                        width = d3.max(z.map(function (x) {
                            return x.width;
                        }));
                        margin = margin * width;
                        width = width + 2 * margin;
                        height = d3.max(z.map(function (x) {
                            return x.height + margin / 2;
                        }));
                        // cleanup
                        d3.selectAll('.tmp').remove();
                        rescale = false;
                    }
                }

                return menu;
            }

            var menu = [
                {
                    title: 'Autores Relacionados',
                    action: function (elm, d, i) {
                        toggleChildrenRightClick(d);
                    }
                }];
            function update(source, expand) {
                var levelWidth = [1];
                var childCount = function (level, n) {
                    if (n.children && n.children.length > 0) {
                        if (levelWidth.length <= level + 1)
                            levelWidth.push(0);
                        levelWidth[level + 1] += n.children.length;
                        n.children.forEach(function (d) {
                            childCount(level + 1, d);
                        });
                    }
                };
                childCount(0, root);
                var newHeight = d3.max(levelWidth) * 110;
                tree = tree.size([newHeight, viewerWidth]);
                // Compute the new tree layout.
                var nodes = tree.nodes(root).reverse();
                var links = tree.links(nodes);
                // Set widths between levels
                nodes.forEach(function (d) {
                    d.y = (d.depth * 220);
                });
                // Update the nodes…
                var node = svgGroup.selectAll("g.tree-node")
                        .data(nodes, function (d) {
                            return d.id || (d.id = ++i);
                        });
                // Tip Creation for title

                var tip = d3.tip()
                        .attr('class', 'tree-d3-tip')
                        .html(function (d) {
                            return ' ';
                        });
//                        .direction('se')
//                        .offset([0, 3]);

                // Enter any new nodes at the parent's previous position.

                var nodeEnter = node.enter().append("g")
                        // .call(dragListener)
                        .call(expand ? tip : function () {
                        })
                        .attr("class", "tree-node")
                        .attr("transform", function (d) {
                            return "translate(" + source.y0 + "," + source.x0 + ")";
                        })
                        .on("mouseover", function (d) {
                            var node = d;
                            if ('publication' in d) {
                                var id = d.publication["@id"];
                                var title = _.findWhere(node.publication.jsonld["@graph"], {"@id": id, "@type": "bibo:AcademicArticle"})["dct:title"];
                                if(typeof(title) !== 'string')
                                  title = _.first(title);
                                tip.html(title);
                                tip.show(d);

                                //AE.getInfo(d.author);
                            } else if ('author' in d)
                            {
//                                var id = d.author["@id"];
//                                tip.html(id);
//                                tip.show(d);
                            }
                        })
                        .on("mouseout", function (d) {
                            if ('publication' in d) {
                                tip.hide(d);
                                //AE.getInfoCancel();
                            }
                            if ('author' in d) {
                                tip.hide(d);
                                //AE.getInfoCancel();
                            }
                        })
                        .on('contextmenu', d3.contextMenu(menu, function (d) {
                            console.log('Quick! Before the menu appears!');//function (d) {
                            if (!('author' in d))
                            {
                                window.stop();
                            }
                        }

                        ))
                        .on('click', click);
                nodeEnter.append("circle")
                        .attr("r", 32)
                        .style("fill", function (d) {
                            return d._children ? "black" : "#fff";
                        });
                clipPathId++;
                nodeEnter.append("clipPath")
                        .attr("id", "clipCircle" + clipPathId)
                        .append("circle")
                        .attr("r", 32);
                /*
                 nodeEnter.append("svg:a")
                 .attr("xlink:href", function(d) {
                 if(isAuthor(d)) {
                 var id = d.author["@id"];
                 var author = _.findWhere( d.author.jsonld["@graph"], {"@id": id, "@type": "foaf:Person"} );
                 return author && author['foaf:name'] ? '':id;
                 }
                 }).attr('target','_blank');
                 */


                nodeEnter.append("image")
                        .attr("xlink:href", function (d) {
                            if (isAuthor(d)) {
                              if (d.author.jsonld["@graph"][0]["foaf:name"] && d.author.jsonld["@graph"][0]["foaf:img"]) {
                                  return d.author.jsonld["@graph"][0]["foaf:img"]["@id"];
                              } else if (d.author.jsonld["@graph"][0]["foaf:name"] && (d.author.jsonld["@graph"][0]["@id"].indexOf('localhost') > 0)) {
                                  return 'wkhome/images/author-ec.png';
                              } else {
                                  return 'wkhome/images/author-default.png';
                              }
                            } else {
                                return 'wkhome/images/document-default.png'
                            }
                        })
                        .attr("x", "-32px")
                        .attr("y", "-32px")
                        .attr("clip-path", "url(#clipCircle" + clipPathId + ")")
                        .attr("width",
                                function (d) {
                                    return 64;
                                    /*if (isAuthor(d)) {
                                     var image = d.author.images[1];
                                     if (!image) {
                                     return 64;
                                     }
                                     if (image.width > image.height) {
                                     return 64 * (image.width / image.height)
                                     } else {
                                     return 64;
                                     }
                                     } else {
                                     return 64;
                                     }*/
                                })
                        .attr("height",
                                function (d) {
                                    return 64;
                                    /*if (isAuthor(d)) {

                                     var image = d.author.images[1];
                                     if (!image) {
                                     return 64;
                                     }
                                     if (image.height > image.width) {
                                     return 64 * (image.height/image.width)
                                     } else {
                                     return 64;
                                     }
                                     } else {
                                     return 64;
                                     }*/
                                })

                nodeEnter.append("text")
                        .attr("x", function (d) {
                            return -125;
                        })
                        .attr("dy", "40")
                        .attr('class', 'tree-nodeText')
                        .attr("text-anchor", function (d) {
                            return "start";
                        })
                        .text(function (d) {
                            if (isAuthor(d)) {
                                //return d.author.name;
                                var id = d.author["@id"];
                                var author = _.findWhere(d.author.jsonld["@graph"], {"@id": id, "@type": "foaf:Person"});
                                var name = typeof(author["foaf:name"]) === 'string' ? author["foaf:name"] : _.first(author["foaf:name"]);
                                return name;
                            }/* else if (isPublication(d)) {
                             var id = d.publication["@id"];
                             var publication = _.findWhere( d.publication.jsonld["@graph"], {"@id": id, "@type": "bibo:Document"} );
                             return publication["dcterms:title"];
                             }*/

                        })
                        .style("fill-opacity", 0)


                // Transition nodes to their new position.
                var nodeUpdate = node.transition()
                        .duration(duration)
                        .attr("transform", function (d) {
                            return "translate(" + d.y + "," + d.x + ")";
                        });
                // Fade the text in
                nodeUpdate.select("text")
                        .style("fill-opacity", 1);
                // Transition exiting nodes to the parent's new position.
                var nodeExit = node.exit().transition()
                        .duration(duration)
                        .attr("transform", function (d) {
                            return "translate(" + (source.y) + "," + source.x + ")";
                        })
                        .remove();
                nodeExit.select("circle")
                        .attr("r", 0);
                nodeExit.select("text")
                        .style("fill-opacity", 0);
                // Update the links…
                var link = svgGroup.selectAll("path.tree-link")
                        .data(links, function (d) {
                            return d.target.id;
                        });
                // Enter any new links at the parent's previous position.
                link.enter().insert("path", "g")
                        .attr("class", "tree-link")
                        .attr("d", function (d) {
                            var o = {
                                x: source.x0,
                                y: source.y0
                            };
                            return diagonal({
                                source: o,
                                target: o
                            });
                        });
                // Transition links to their new position.
                link.transition()
                        .duration(duration)
                        .attr("d", diagonal);
                // Transition exiting nodes to the parent's new position.
                link.exit().transition()
                        .duration(duration)
                        .attr("d", function (d) {
                            var o = {
                                x: source.x,
                                y: source.y
                            };
                            return diagonal({
                                source: o,
                                target: o
                            });
                        })
                        .remove();
                // Stash the old positions for transition.
                nodes.forEach(function (d) {
                    d.x0 = d.x;
                    d.y0 = d.y;
                });
            }

            // Append a group which holds all nodes and which the zoom Listener can act upon.
            var svgGroup = svg.append("g");
            function copyTree(from, to) {
                if (from.author) {
                    to.author = from.author
                }

                if (from.genre) {
                    to.genre = from.genre
                }

                if (!from.children) {
                    return;
                }
                to.children = []
                from.children.forEach(function (node) {
                    var child = {}
                    copyTree(node, child)
                    to.children.push(child);
                })
            }

            function serializeTree() {
                var obj = {};
                copyTree(root, obj)
                return obj;
            }

            function initWithData(from, to) {
                if (from.author) {
                    to.author = from.author;
                    exploredArtistIds.push(to.author.id);
                }
                if (from.genre) {
                    to.genre = from.genre;
                }

                if (from.children) {
                    to.children = []
                    from.children.forEach(function (child) {
                        var obj = {}
                        initWithData(child, obj);
                        to.children.push(obj);
                    })
                }

                if (to.children && to.children.length > 0) {
                    //console.log(to.author.name);
                    //update(root);
                }

            }

            function getAllArtists(node, authorIds) {
                if (isAuthor(node)) {
                    authorIds.push(node.author.id);
                }
                if (!node.children) {
                    return;
                }
                node.children.forEach(function (child) {
                    getAllArtists(child, authorIds);
                })
            }

            exploredArtistIds = []
            root = initWithArtist(data);
            root.x0 = viewerHeight / 2;
            root.y0 = 0;
            rootAuthor = root.author["@id"];
            relatedAuthors(root);
            update(root, true);
            centerNode(root);
            click(root);
        };

        return {
            restrict: 'E',
            scope: {
                data: '=',
                publication: '=',
                ifrightClick: '&',
                clickonRelatedAuthor: '&'
            },
            compile: function (element, attrs, transclude) {

                //	Define	the	dimensions	for	the	chart
                //var width = 960, height = 500;
                var width = $(element).width(),
                        height = $(element).height();
                //	Create	a	SVG	root	element
                var svg = d3.select(element[0]).append("svg");

                //	Return	the	link	function
                return	function (scope, element, attrs) {
                    //	Watch	the	data	attribute	of	the	scope
                    scope.$watch('data', function (newVal, oldVal, scope) {
                        //	Update	the	chart
                        if (scope.data && scope.data["@graph"]){
                          //&&
                            //    (JSON.stringify(newVal["@graph"]) != JSON.stringify(oldVal ? oldVal["@graph"] : oldVal))) {
                            var data = jQuery.extend({}, scope.data);
                            draw(svg, width, height, data, scope);
                        }
                    }, true);
                };
            }
        };
    }]);
