'use strict';
var explorableTree = angular.module('explorableTree', []);
//	D3	Factory
explorableTree.factory('d3', function () {
    return	d3;
});
explorableTree.directive('explorableTree', ['d3', 'sparqlQuery', 'authorRestQuery', '$window',
    function (d3, sparqlQuery, authorRestQuery, $window) {
        var getRelatedAuthorsByClustersQuery = 'PREFIX foaf: <http://xmlns.com/foaf/0.1/> '
                + ' PREFIX http: <http://www.w3.org/2011/http#> '
                + ' PREFIX dct: <http://purl.org/dc/terms/>  '
                + ' PREFIX bibo: <http://purl.org/ontology/bibo/>  '
                + ' PREFIX uc: <http://ucuenca.edu.ec/wkhuska/resource/>  '
                + ' CONSTRUCT {  <http://ucuenca.edu.ec/wkhuska/resultTitle> a uc:pagetitle. <http://ucuenca.edu.ec/wkhuska/resultTitle> uc:viewtitle "Authors Related With {0}"  .         ?subject rdfs:label ?name.         ?subject uc:total ?totalPub   }   WHERE {   { '
                + ' SELECT DISTINCT  ?subject ?name (count(?pub) as ?totalPub)'
                + ' WHERE { '
                + '   GRAPH <http://ucuenca.edu.ec/wkhuska/clusters> '
                + '         { '
                + ' ?cluster <http://ucuenca.edu.ec/resource/hasPerson> <{1}> .'
                + ' ?cluster <http://ucuenca.edu.ec/resource/hasPerson> ?subject.'
                + '           ?subject foaf:publications ?pub'
                + '          {'
                + ' SELECT ?name'
                + ' {'
                + '      graph <http://ucuenca.edu.ec/wkhuska>'
                + '            {'
                + '        	?subject foaf:name ?name.'
                + '            }'
                + ' }'
                + '  }'
                + '              } '
                + '     } group by ?subject ?name '
                + '          }}    ';


        var getRelatedAuthorsByPublicationsQuery = 'PREFIX http: <http://www.w3.org/2011/http#> '
                + ' PREFIX dct: <http://purl.org/dc/terms/> '
                + ' PREFIX bibo: <http://purl.org/ontology/bibo/> '
                + '  PREFIX foaf: <http://xmlns.com/foaf/0.1/>  '
                + ' PREFIX uc: <http://ucuenca.edu.ec/wkhuska/resource/>  '
                + '  CONSTRUCT { '
                + ' <http://ucuenca.edu.ec/wkhuska/resultTitle> a uc:pagetitle. <http://ucuenca.edu.ec/wkhuska/resultTitle> uc:viewtitle "Authors Related With {0}" . '
                + '        ?subject rdfs:label ?name. '
                + '        ?subject uc:total ?totalPub '
                + '  } '
                + '  WHERE { '
                + '  { '
                + '     SELECT ?subject (count(?pub) as ?totalPub) ?name '
                + '         WHERE { '
                + '             GRAPH <http://ucuenca.edu.ec/wkhuska> { '
                + '             <{1}> foaf:publications ?pub.  '
                + '            ?subject foaf:publications ?pub. '
                + '            ?subject foaf:name ?name.  } '
                + '             } '
                + '         GROUP BY ?subject ?name '
                + '  } '
                + ' }';




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
            var viewerHeight = height + 200;
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
                var sparqlquery = querytoExecute;
                sparqlQuery.querySrv({query: sparqlquery}, function (rdf) {
                    var context = {
                        "rdfs": "http://www.w3.org/2000/01/rdf-schema#",
                        "foaf": "http://xmlns.com/foaf/0.1/",
                        "dc": "http://purl.org/dc/elements/1.1/",
                        "dcterms": "http://purl.org/dc/terms/",
                        "uc": "http://ucuenca.edu.ec/wkhuska/resource/"
                    };
                    jsonld.compact(rdf, context, function (err, compacted) {
                        if (compacted)
                        {
                            var entity = compacted["@graph"];
                            if (entity)
                            {
                                var authorInfo = $('div.tree-node-author-info .' + divtoload);
                                authorInfo.html('');
                                var values = entity.length ? entity : [entity];
                                var div = $('<div>');
                                authorInfo.append(div);
                                _.map(values, function (value) {
                                    var datastring = JSON.stringify(value);
                                    //var anchor = $("<a class='relatedauthors' target='blank' >").attr('href', value).text("");
                                    var anchor = $("<a class='relatedauthors' target='blank' onclick = 'return clickonRelatedauthor(\"" + value["@id"] + "\")'  >").text("");
                                    anchor.append('<img src="/wkhome/images/author-ec.png" class="img-rounded" alt="Logo Cedia" width="20" height="20"        >');

                                    anchor.append(value["rdfs:label"]);
                                    div.append(anchor);
                                    div.append("</br>");
                                    return anchor;
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
                if (author["foaf:name"])
                {
                    //********** AUTORES RELACIONADOS - POR CLUSTERING *********//
                    var query = String.format(getRelatedAuthorsByClustersQuery, author["foaf:name"], id);
                    executeRelatedAuthors(query, "authorsByClusters");
                    //********** AUTORES RELACIONADOS - POR PUBLICACION *********//
                    var query = String.format(getRelatedAuthorsByPublicationsQuery, author["foaf:name"], id);
                    executeRelatedAuthors(query, "authorsByPublications");
                }//end if author["foaf:name"]
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


                if (right)
                {
                    var id = node.author["@id"];
                    var author = _.findWhere(node.author.jsonld["@graph"], {"@id": id, "@type": "foaf:Person"});
                    if (author["foaf:name"])
                    {
                        var sparqlquery = String.format(getRelatedAuthorsByClustersQuery, author["foaf:name"], id);
                        waitingDialog.show("Loading Authors Related with " + author["foaf:name"]);
                        sparqlQuery.querySrv({query: sparqlquery}, function (rdf) {
                            var context = {
                                "rdfs": "http://www.w3.org/2000/01/rdf-schema#",
                                "foaf": "http://xmlns.com/foaf/0.1/",
                                "dc": "http://purl.org/dc/elements/1.1/",
                                "dcterms": "http://purl.org/dc/terms/",
                                "bibo": "http://purl.org/ontology/bibo/",
                                "uc": "http://ucuenca.edu.ec/wkhuska/resource/"
                            };
                            jsonld.compact(rdf, context, function (err, compacted) {
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
                                }
                                else
                                {
                                    waitingDialog.hide();
                                }
                            });
                        }); // end  sparqlQuery.querySrv(...
                    }//end if authoir["foaf:name"]

                }
                else
                {
                    var infoBar = $('div.tree-node-info');
                    ///if (infoBar) {
                    var id = node.author["@id"];
                    var author = _.findWhere(node.author.jsonld["@graph"], {"@id": id, "@type": "foaf:Person"});
                    if (!author['foaf:name'] && node.author.jsonld["@graph"].length <= 1) {
                        infoBar.find('h4').text("Author External Info");
                        infoBar = $('div.tree-node-info .entityInfo');
                        //infoBar.find('div#title').text('');
                        //infoBar.find('div#title').text("Author: " + publication["dcterms:title"]);
                        var anchor = $("<a target='blank'>").attr('href', id.replace('/xr/', '/')) //SOLO DBLP & MICROSOFT ACADEMICS
                                .text("Click here for more info...");
                        // 
                        var context = {
                            "foaf": "http://xmlns.com/foaf/0.1/",
                            "dcterms": "http://purl.org/dc/terms/",
                            "bibo": "http://purl.org/ontology/bibo/",
                            "cedia": "https://www.cedia.org.ec/"/*,
                             "publications": {"@id": "http://xmlns.com/foaf/0.1/publications", "@type": "@id"},
                             "provenance": {"@id": "http://purl.org/dc/terms/provenance"},
                             "title": {"@id": "http://purl.org/dc/terms/title"}*/
                        };
                        var authorToFind = author["@id"];
                        waitingDialog.show("Searching publications of the external author");



                        var getExternalAuthorFromLocal = 'PREFIX dc: <http://purl.org/dc/elements/1.1/>  '
                                + 'PREFIX dct: <http://purl.org/dc/terms/> '
                                + 'PREFIX bibo: <http://purl.org/ontology/bibo/> '
                                + 'PREFIX foaf: <http://xmlns.com/foaf/0.1/> '
                                + 'CONSTRUCT  { <' + authorToFind + '> ?propaut ?obbaut. ?obbaut ?propub ?objpub.'
                                + '    } '
                                + 'WHERE'
                                + '{'
                                + 'graph <http://ucuenca.edu.ec/wkhuska/externalauthors>'
                                + '        {'
                                + '<' + authorToFind + '> ?propaut ?obbaut.'
                                + '?obbaut ?propub ?objpub.'
                                + '}'
                                + '      }';
                        sparqlQuery.querySrv({query: getExternalAuthorFromLocal}, function (rdf) {
                            jsonld.compact(rdf, context, function (err, compacted) {
                                if (!compacted["@graph"])
                                {
                                    authorRestQuery.query({resource: authorToFind}, function (rdf) {

                                        jsonld.compact(rdf, context, function (err, compacted) {
                                            if (!compacted["@graph"])
                                            {
                                                waitingDialog.hide();
                                                alert("Papers not obtained for external author, you can try again");
                                            }
                                            else
                                            {
                                                var rs = compacted;
                                                //if(compacted['@graph']) {
                                                node.author.jsonld["@context"] = _.extend(node.author.jsonld["@context"], context);
                                                node.author.jsonld["@graph"] = _.flatten([node.author.jsonld["@graph"], compacted["@graph"]]);
                                                setChildrenAndUpdate('publication', node, node.author.jsonld, {"@type": "bibo:Document"}, context, exploredPublicationsIds);
                                                //} else { //no results
                                                waitingDialog.hide();
                                            }
                                        });
                                    });
                                }
                                else
                                {
                                    //if(compacted['@graph']) {
                                    node.author.jsonld["@context"] = _.extend(node.author.jsonld["@context"], context);
                                    node.author.jsonld["@graph"] = _.flatten([node.author.jsonld["@graph"], compacted["@graph"]]);
                                    setChildrenAndUpdate('publication', node, node.author.jsonld, {"@type": "bibo:Document"}, context, exploredPublicationsIds);
                                    //} else { //no results
                                    waitingDialog.hide();
                                }
                            });
                        });

                        infoBar.append(anchor);
                    }
                    else {
                        //         }// End if (infoBar)
                        /*var b = jQuery.extend({}, exampleNode);
                         b.name = b.name + "" + Math.random();
                         consumedNodes.push(b);
                         var artists = consumedNodes;*/ //****
                        //AE.getRelated(node.author.id, exploredArtistIds).then(function(authors) {
                        var model;
                        var context = {
                            "foaf": "http://xmlns.com/foaf/0.1/",
                            "dcterms": "http://purl.org/dc/terms/",
                            "bibo": "http://purl.org/ontology/bibo/"/*,
                             "publications": {"@id": "http://xmlns.com/foaf/0.1/publications", "@type": "@id"},
                             "provenance": {"@id": "http://purl.org/dc/terms/provenance"},
                             "title": {"@id": "http://purl.org/dc/terms/title"}*/
                        };
                        if (node.author.jsonld["@graph"].length > 1) {
                            model = node.author.jsonld;
                            setChildrenAndUpdate('publication', node, model, {"@type": "bibo:Document"}, context, exploredPublicationsIds);
                        } else {
                            var nodeId = node.author['@id'];
                            var queryPublications = ' PREFIX dct: <http://purl.org/dc/terms/> PREFIX foaf: <http://xmlns.com/foaf/0.1/> '
                                    + ' PREFIX bibo: <http://purl.org/ontology/bibo/> '
                                    + ' CONSTRUCT { '
                                    + ' <' + nodeId + '> foaf:publications ?pub . '
                                    + ' ?pub a bibo:Document . '
                                    + ' ?pub dct:title ?title .   '
                                    + ' ?pub bibo:abstract ?abstract. '
                                    + ' ?pub bibo:uri ?uri. '
                                    + ' ?pub dct:contributor ?contributor. '
                                    + ' ?pub dct:publisher ?publisher. '
                                    + ' ?pub bibo:Quote ?keyword. '
                                    + ' ?pub dct:isPartOf ?isPartOf. '
                                    + ' ?pub bibo:numPages ?numPages. '
                                    + ' } '
                                    + ' WHERE { graph <http://ucuenca.edu.ec/wkhuska> {  '
                                    + ' <' + nodeId + '> foaf:publications ?pub . '
                                    + '?pub dct:title ?title '
                                    + ' OPTIONAL {?pub bibo:abstract ?abstract. } '
                                    + ' OPTIONAL {?pub bibo:uri ?uri. } '
                                    + ' OPTIONAL {?pub dct:contributor ?contributor. } '
                                    + ' OPTIONAL {?pub dct:publisher ?publisher. } '
                                    + ' OPTIONAL {?pub bibo:Quote ?keyword. } '
                                    + ' OPTIONAL {?pub dct:isPartOf ?isPartOf. } '
                                    + ' OPTIONAL {?pub bibo:numPages ?numPages. } '
                                    + ' } }';
                            sparqlQuery.querySrv({query: queryPublications}, function (rdf) {

                                jsonld.compact(rdf, context, function (err, compacted) {
                                    var rs = compacted;
                                    node.author.jsonld["@context"] = _.extend(node.author.jsonld["@context"], context);
                                    node.author.jsonld["@graph"] = _.flatten([node.author.jsonld["@graph"], compacted["@graph"]]);
                                    setChildrenAndUpdate('publication', node, node.author.jsonld, {"@type": "bibo:Document"}, context, exploredPublicationsIds);
                                });
                            });
                        }

                    }//End else if(!author["foaf:name"] &&  ...
                    //});

                }//end IF clickright

            }

            function setChildrenAndUpdate(entityName, node, jsonld, filter, context, stack) {
                if (!node.children) {
                    node.children = []
                }
                var entities;
                if (filter === "null")
                {
                    entities = jsonld;
                }
                else
                {
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
                update(node, true);
                centerNode(node);
            }

            function setChildrenAndUpdateForPub(node) {

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
                    var id = node.publication["@id"];
                    //var sparqlDescribe = "DESCRIBE <" + id + ">";

                    //view data in infoBar
                    var entity = _.findWhere(node.publication.jsonld["@graph"], {"@id": id, "@type": "bibo:Document"});
                    var model = {"dcterms:title": {label: "Title", containerType: "div"},
                        "bibo:uri": {label: "URL", containerType: "a"},
                        "dcterms:contributor": {label: "Contributor", containerType: "a"},
                        "dcterms:isPartOf": {label: "Is Part Of", containerType: "a"},
                        "dcterms:license": {label: "License", containerType: "a"},
                        "dcterms:provenance": {label: "Source", containerType: "div"},
                        "dcterms:publisher": {label: "Publisher", containerType: "div"},
                        "bibo:numPages": {label: "Pages", containerType: "div"},
                        "bibo:abstract": {label: "Abstract", containerType: "div"},
                        "bibo:Quote": {label: "Keywords", containerType: "div"}
                    };
                    infoBar.find('h4').text("Publication Info");
                    infoBar.find('div#title').text("Title: " + entity["dcterms:title"]);
                    infoBar.find('a').attr('href', "http://190.15.141.85:8080/marmottatest/meta/text/html?uri=" + entity["@id"])
                            .text("More Info...");
                    var pubInfo = $('div.tree-node-info .entityInfo');
                    pubInfo.html('');
                    _.each(_.keys(model), function (key, idx) {

                        if (entity[key]) {
                            if (model[key].containerType == 'a') {
                                var values = entity[key].length ?
                                        _.pluck(entity[key], '@id') : [entity[key]["@id"]];
                                var div = $('<div>');
                                var label = $('<span class="label label-primary">').text(model[key].label);
                                div.append(label);
                                div.append("</br>");
                                pubInfo.append(div);
                                _.map(values, function (value) {
                                    var anchor = $("<a target='blank'>").attr('href', value).text(value);
                                    div.append(anchor);
                                    div.append("</br>");
                                    return anchor;
                                });
                            } else { //append into a div container
                                var div = $('<div>');
                                var label = $('<span class="label label-primary">').text(model[key].label)
                                div.append(label);
                                div.append("</br>");
                                pubInfo.append(div);
                                var values = entity[key].length ? entity[key] : [entity[key]];
                                if (typeof (values) === 'string') {
                                    var span = $('<span class="field-value">').text(values);
                                    div.append(span);
                                } else {
                                    _.map(values, function (value, idx) {
                                        var span = $('<span class="field-value">').text(value);
                                        div.append(span);
                                        div.append("</br>");
                                    });
                                }
                            }
                        }

                    });
                }

                var model;
                var context = {
                    "foaf": "http://xmlns.com/foaf/0.1/",
                    "dcterms": "http://purl.org/dc/terms/",
                };
                if (node.publication.jsonld["@graph"].length > 1) {
                    model = node.publication.jsonld;
                    setChildrenAndUpdate('author', node, model, {"@type": "foaf:Person"}, context, exploredArtistIds);
                } else {
                    var nodeId = node.publication['@id'];
                    var coAuthors = [];
                    //**** GETTING EXTERNAL CONTRIBUTORS OF PUBLICATION ***/
                    var contributors = node.publication.jsonld["@graph"][0]["dcterms:contributor"];
                    _.map(contributors, function (val) {
                        coAuthors.push({'@id': val["@id"], '@type': 'foaf:Person'});
                    });

                    //****  GETTING LOCAL CONTRIBUTOR OF PUBLICATION ***** //    
                    var getLocalcoAuthorsSparqlQuery = 'PREFIX dct: <http://purl.org/dc/terms/> '
                            + ' PREFIX foaf: <http://xmlns.com/foaf/0.1/> '
                            + ' CONSTRUCT { '
                            + ' <' + node.publication['@id'] + '> dct:contributors ?subject. '
                            + ' ?subject foaf:name ?name. '
                            + ' ?subject a foaf:Person. '
                            + ' } '
                            + ' WHERE { '
                            + ' GRAPH <http://ucuenca.edu.ec/wkhuska> '
                            + ' { '
                            + ' ?subject foaf:publications  <' + node.publication['@id'] + '>. '
                            + ' ?subject foaf:name ?name. '
                            + ' }}';

//                    sparqlQuery.querySrv({query: getLocalcoAuthorsSparqlQuery}, function (rdf) {
//
//                        jsonld.compact(rdf, context, function (err, compacted) {
//                            if (compacted)
//                            {
//                                var localcontributors = _.where(compacted["@graph"], {"@type": "foaf:Person"});
//                                _.map(localcontributors, function (val) {
//                                    if (val["@id"] !== rootAuthor)
//                                    {
//                                        coAuthors.push({'@id': val["@id"], '@type': 'foaf:Person', 'foaf:name': val["foaf:name"]});
//                                    }
//                                });
//                            }
//                            var contributorsjsonld = {"@graph": coAuthors};
//                            setChildrenAndUpdate('author', node, contributorsjsonld, 'foaf:Person', context, exploredArtistIds);
//                        });
//                    });
                    //**** END GETTING EXTERNAL CONTRIBUTORS OF PUBLICATION ***/
                    var contributorsjsonld = {"@graph": coAuthors};
                    setChildrenAndUpdate('author', node, contributorsjsonld, 'foaf:Person', context, exploredArtistIds);

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
                var newHeight = d3.max(levelWidth) * 100;
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
                                var title = _.findWhere(node.publication.jsonld["@graph"], {"@id": id, "@type": "bibo:Document"})["dcterms:title"];
                                tip.html(title);
                                tip.show(d);
                                //AE.getInfo(d.author);
                            } else if ('author' in d)
                            {
                                var id = d.author["@id"];
                                tip.html(id);
                                tip.show(d);
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
                        .on('contextmenu', d3.contextMenu(menu,  function (d) {
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
                                if (d.author.jsonld["@graph"][0]["foaf:name"])
                                {
                                    return 'wkhome/images/author-ec.png';
                                }
                                else
                                {
                                    return 'wkhome/images/author-default.png';
                                }
                                //return AE.getSuitableImage(d.author.images);
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
                        .attr("dy", "50")
                        .attr('class', 'tree-nodeText')
                        .attr("text-anchor", function (d) {
                            return "start";
                        })
                        .text(function (d) {
                            if (isAuthor(d)) {
                                //return d.author.name;
                                var id = d.author["@id"];
                                var author = _.findWhere(d.author.jsonld["@graph"], {"@id": id, "@type": "foaf:Person"});
                                return author["foaf:name"];
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
                            return "translate(" + source.y + "," + source.x + ")";
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
                        if (scope.data &&
                                (JSON.stringify(newVal["@graph"]) != JSON.stringify(oldVal ? oldVal["@graph"] : oldVal))) {
                            var data = jQuery.extend({}, scope.data);
                            draw(svg, width, height, data, scope);
                        }
                    }, true);
                };
            }
        };
    }]);

