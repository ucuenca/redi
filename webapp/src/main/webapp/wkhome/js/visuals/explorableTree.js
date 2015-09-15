'use strict';

var explorableTree = angular.module('explorableTree', []);
//	D3	Factory
explorableTree.factory('d3',	function()	{
	return	d3;
});
explorableTree.directive('explorableTree', ['d3', 'sparqlQuery', 'searchData',
	function(d3, sparqlQuery, searchData) {

			
		
			var draw = function draw(svg,	width,	height,	data, scope) {

				/*var exampleNode = {"external_urls":{"spotify":"https://open.spotify.com/artist/43ZHCT0cAZBISjO8DG9PnE"},
			      "followers":{"href":null,"total":833907},
			      "genres":["rock-and-roll","rockabilly"],"href":"https://api.spotify.com/v1/artists/43ZHCT0cAZBISjO8DG9PnE",
			      "id":"43ZHCT0cAZBISjO8DG9PnE",
			      "images":[{"height":1296,"url":"https://i.scdn.co/image/c7b8708eab6d0f0902908c1b9f9ba1daaeed06af","width":1000},
			        {"height":829,"url":"https://i.scdn.co/image/e25cb372ca9a5317c17d5f62b3556f76ce2edde8","width":640},
			        {"height":259,"url":"https://i.scdn.co/image/7d0e683d6bb4cbb384586cd6d9007f5a40928251","width":200},
			        {"height":83,"url":"https://i.scdn.co/image/16045a251c9e9f5772d4aeb3f6fa23fe4fdeb54a","width":64}],
			      "name":"Elvis Presley",
			      "popularity":82,
			      "type":"artist",
			      "uri":"spotify:artist:43ZHCT0cAZBISjO8DG9PnE"};
			    var consumedNodes = [];
			    consumedNodes.push(exampleNode);*/

				// Misc. variables
			    var i = 0;
			    var duration = 750;
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
			        .projection(function(d) {
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

			    function updateWindow(){
			        viewerWidth = $(window).width() - rightPaneWidth;
			        viewerHeight = $(window).height();
			        svg.attr("width", viewerWidth).attr("height", viewerHeight);
			        if (lastExpandedNode) {
			            centerNode(lastExpandedNode);
			        }
			    }

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

			    function setChildrenAndUpdateForAuthor(node) {
			    	var infoBar = $('div.tree-node-info');
			    	if(infoBar) {
			    		var id = node.author["@id"];
			    		var author = _.findWhere(node.author.jsonld["@graph"], {"@id": id, "@type": "foaf:Person"});
			    		if(!author['foaf:name']) {
			    			infoBar.find('h4').text("Author External Info");
			    			infoBar = $('div.tree-node-info .entityInfo');
			    			//infoBar.find('div#title').text('');
			    			//infoBar.find('div#title').text("Author: " + publication["dcterms:title"]);
			    			var anchor = $("<a target='blank'>").attr('href', id.replace('/xr/','/')) //SOLO DBLP
			    				.text("Click here for more info...");
			    			infoBar.append(anchor);
		    			}

			    	}
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
			        if(node.author.jsonld["@graph"].length > 1) {
			        	model = node.author.jsonld;
			        	setChildrenAndUpdate('publication', node, model, {"@type": "bibo:Document"}, context, exploredPublicationsIds);
			        } else {
			        	var nodeId = node.author['@id'];
			        	var queryPublications = 'PREFIX dct: <http://purl.org/dc/terms/> PREFIX foaf: <http://xmlns.com/foaf/0.1/> '
			        		+'PREFIX bibo: <http://purl.org/ontology/bibo/> '
			        		+'CONSTRUCT { <'+ nodeId +'> foaf:publications ?pub . ?pub a bibo:Document . ?pub dct:title ?title . '
			        		+'?pub dct:provenance ?prov } '
			        		+'WHERE { <'+ nodeId +'> foaf:publications ?pub . ?pub dct:title ?title . ?pub dct:provenance ?prov }';

			        	

			        	sparqlQuery.querySrv({query: queryPublications}, function(rdf) {

							jsonld.compact(rdf, context, function(err, compacted) {
							  	var rs = compacted;
							  	//if(compacted['@graph']) {
								  	node.author.jsonld["@context"] = _.extend(node.author.jsonld["@context"], context);
								  	node.author.jsonld["@graph"] = _.flatten([ node.author.jsonld["@graph"], compacted["@graph"] ]);

								  	setChildrenAndUpdate('publication', node, node.author.jsonld, {"@type": "bibo:Document"}, context, exploredPublicationsIds);
								//} else { //no results

								//}
					            /*if (!node.children) {
					                node.children = []
					            }

					            var publications = _.where( node.author.jsonld["@graph"], {"@type": "bibo:Document"} );

					            publications.forEach(function(publication) {

					                node.children.push(
					                    {
					                        'publication': {'@id': publication["@id"], 
					                        	jsonld: {'@context': context, '@graph': [publication] }},
					                        'children': null
					                    }
					                )
					                exploredPublicationsIds.push(publication["@id"]);
					                //exploredArtistIds.push(author.id);

					            });
					            update(node);
					            centerNode(node);*/
				            });

			            });
			        }
			        //});
			    }

			    function setChildrenAndUpdate(entityName, node, jsonld, filter, context, stack) {
			    	if (!node.children) {
		                node.children = []
		            }

		            var entities = _.where( jsonld["@graph"], filter );

		            entities.forEach(function(entity) {

		            	var child = {};
		            	child[entityName]  = {'@id': entity["@id"],
		            		jsonld: {'@context': context, '@graph': [entity], '@id': jsonld['@id'] }};
		            	child['children'] = null;

		                node.children.push( child );
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
			    					"dcterms:publisher": {label:"Publisher", containerType: "div"},
			    					"bibo:numPages": {label:"Pages", containerType: "div"}
			    				};
			    	if(infoBar) {
			    		var id = node.publication["@id"];
			    		var sparqlDescribe = "DESCRIBE <" + id + ">";
				    	sparqlQuery.querySrv({query: sparqlDescribe}, function(rdf) {
				    		var context = {
				        		"foaf": "http://xmlns.com/foaf/0.1/",
				        		"dc": "http://purl.org/dc/elements/1.1/",
				        		"dcterms": "http://purl.org/dc/terms/",
				        		"bibo": "http://purl.org/ontology/bibo/",
				        		"uc": "http://ucuenca.edu.ec/"
							};
							jsonld.compact(rdf, context, function(err, compacted) {
								var entity = _.findWhere(compacted["@graph"], {"@id": id, "@type": "bibo:Document"});

								infoBar.find('h4').text("Publication Info");
								infoBar.find('div#title').text("Title: " + entity["dcterms:title"]);
								infoBar.find('a').attr('href', "http://190.15.141.85:8080/marmottatest/meta/text/html?uri=" + entity["@id"])
						    			.text("More Info...");
						    	var pubInfo = $('div.tree-node-info .entityInfo');
						    	pubInfo.html('');
								_.each(_.keys(model), function(key, idx) {

									if(entity[key]) {
										if(model[key].containerType == 'a') {
											var values = entity[key].length ? 
												_.pluck(entity[key],'@id'):[entity[key]["@id"]];
											var div = $('<div>');
											var label = $('<span class="label label-primary">').text(model[key].label);
											div.append(label);
											div.append("</br>");
											pubInfo.append(div);
											_.map(values, function(value){
												var anchor = $("<a target='blank'>").attr('href',value.replace('/xr/','/')/*SOLO DBLP*/).text(value);
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
											var values = entity[key].length ? entity[key]:[entity[key]];
											if(typeof(values) === 'string') {
												var span = $('<span class="field-value">').text(values);
												div.append(span);
											} else {
												_.map(values, function(value, idx) {
													var span = $('<span class="field-value">').text(value);
													div.append(span);
													div.append("</br>");
												});
											}
										}
									}
						    		
							  	});
				            });

			            });

			    	}
			    	/*
			        var authors;
			        AE.getArtistsForGenre(node.genre.name).then(function(authors) {
			            if (!node.children) {
			                node.children = []
			            }

			            authors.forEach(function(author) {
			                node.children.push(
			                    {
			                        'author': author,
			                        'children': null
			                    }
			                )
			                exploredArtistIds.push(author.id);
			            });
			            update(node);
			            centerNode(node);
			        });*/
					var model;
					var context = {
		        		"foaf": "http://xmlns.com/foaf/0.1/",
		        		"dcterms": "http://purl.org/dc/terms/",
					};
					if(node.publication.jsonld["@graph"].length > 1) {
						model = node.publication.jsonld;
			        	setChildrenAndUpdate('author', node, model, {"@type": "foaf:Person"}, context, exploredArtistIds);
			        } else {

						var nodeId = node.publication['@id'];
						var queryAuthors = 'PREFIX dct: <http://purl.org/dc/terms/> PREFIX foaf: <http://xmlns.com/foaf/0.1/> '
							+'CONSTRUCT { <'+ nodeId +'> dct:contributor ?con . ?con a foaf:Person . ?con foaf:name ?name } '
							+'WHERE { <'+ nodeId +'> dct:contributor ?con . '
							+'OPTIONAL {?con foaf:name ?name. } '
							+'}'; 
			        	

			        	sparqlQuery.querySrv({query: queryAuthors}, function(rdf) {
			        		
							jsonld.compact(rdf, context, function(err, compacted) {
							  	var rs = compacted;
							  	node.publication.jsonld["@context"] = _.extend(node.publication.jsonld["@context"], context);
							  	node.publication.jsonld["@graph"] = _.flatten([ node.publication.jsonld["@graph"], compacted["@graph"] ]);
								node.publication.jsonld["@id"] = compacted["@id"];

								setChildrenAndUpdate('author', node, node.publication.jsonld, {"@type": "foaf:Person"}, context, exploredArtistIds);
								/*

					            if (!node.children) {
					                node.children = []
					            }

					            var authors = _.where( node.publication.jsonld["@graph"], {"@type": "foaf:Person"} );

					            authors.forEach(function(author) {

					                node.children.push(
					                    {
					                        'author': {"@id": author['@id'], 
					                        	jsonld: { '@context': context, '@graph': [author], '@id': compacted["@id"] }},
					                        'children': null
					                    }
					                )
					                exploredArtistIds.push(author['@id'])
					            });
					            update(node);
					            centerNode(node);*/
				            });

			            });
					}
			    }

			    function initWithArtist(author) {
			    	var id = author["@graph"][0]["@id"];
			        exploredArtistIds.push(id);
			        return {
			            'author' : {"@id": id, jsonld: author},
			            'children': null
			        }
			    };

			    function initWithGenre(genreName) {
			        return {
			            'genre' : {
			                'name':genreName
			            },
			            'children': null,
			        }
			    };

			    function isAuthor(d) {
			        return 'author' in d;
			    }

			    function isPublication(d) {
			        return 'publication' in d;
			    }


			    function removeExpandedId(d) {
			        if (d.children) {
			            d.children.forEach(function(node) {
			                removeExpandedId(node);
			            });
			        }
			        if(isAuthor(d)) {
				        var indexToRem = exploredArtistIds.indexOf(d.author.id);
				        exploredArtistIds.splice(indexToRem, 1);
				    } else {

				    }
			    }

			    function removeChildrenFromExplored(d) {
			        d.children.forEach(function(node) {
			            removeExpandedId(node);
			        });
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
			                setChildrenAndUpdateForAuthor(d);
			            } else if (isPublication(d)) {
			                setChildrenAndUpdateForPub(d);
			            }
			        }
			        return d;
			    }

			    function click(d) {
			    	$('div.tree-node-info .entityInfo').html('');
			        d = toggleChildren(d);
			    }

			    function update(source, expand) {
			        var levelWidth = [1];
			        var childCount = function(level, n) {
			            if (n.children && n.children.length > 0) {
			                if (levelWidth.length <= level + 1) levelWidth.push(0);

			                levelWidth[level + 1] += n.children.length;
			                n.children.forEach(function(d) {
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
			        nodes.forEach(function(d) {
			             d.y = (d.depth * 220);
			        });

			        // Update the nodes…
			        var node = svgGroup.selectAll("g.tree-node")
			            .data(nodes, function(d) {
			                return d.id || (d.id = ++i);
			            });

			        // Tip Creation for title

			        var tip = d3.tip()
						.attr('class', 'tree-d3-tip')
						.html(function(d){return ' ';})
						/*.html(function(d) { 
							if (isPublication(d)) {
								var context = {
					        		"foaf": "http://xmlns.com/foaf/0.1/",
					        		"dc": "http://purl.org/dc/elements/1.1/",
					        		"dcterms": "http://purl.org/dc/terms/",
					        		"bibo": "http://purl.org/ontology/bibo/",
					        		"uc": "http://ucuenca.edu.ec/"
								};
			                	var id = d.publication["@id"];
			                    var publication = _.findWhere( d.publication.jsonld["@graph"], {"@id": id, "@type": "bibo:Document"} );
			                    return publication["dcterms:title"];
			                }
						 })*/
						.direction('se')
						.offset([0, 3])

			        // Enter any new nodes at the parent's previous position.
			        var nodeEnter = node.enter().append("g")
			            // .call(dragListener)
			            .call( expand ? tip:function(){} )
			            .attr("class", "tree-node")
			            .attr("transform", function(d) {
			                return "translate(" + source.y0 + "," + source.x0 + ")";
			            })
			            .on("mouseover", function(d) {
			            	var node = d;
			                if ('publication' in d) {
			                	var id = d.publication["@id"];
			                	var sparqlDescribe = "DESCRIBE <" + id + ">";
						    	sparqlQuery.querySrv({query: sparqlDescribe}, function(rdf) {
						    		var context = {
						        		"foaf": "http://xmlns.com/foaf/0.1/",
						        		"dc": "http://purl.org/dc/elements/1.1/",
						        		"dcterms": "http://purl.org/dc/terms/",
						        		"bibo": "http://purl.org/ontology/bibo/",
						        		"uc": "http://ucuenca.edu.ec/"
									};
									jsonld.compact(rdf, context, function(err, compacted) {
										var entity = _.findWhere(compacted["@graph"], {"@id": id, "@type": "bibo:Document"});
									  	tip.html(entity["dcterms:title"]);
									  	//tip.html(function(d){return 'test';});
									  	//tip.show(node);
						            });

					            });
			                	tip.show(d);
			                    //AE.getInfo(d.author);
			                }
			            })
			            .on("mouseout", function(d) {
			                if ('publication' in d) {
			                	tip.hide(d);
			                    //AE.getInfoCancel();
			                }
			            })
			            .on('click', click);

			        nodeEnter.append("circle")
			            .attr("r", 32)
			            .style("fill", function(d) {
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
			            .attr("xlink:href", function(d) {
			                if (isAuthor(d)) {
		                		return 'images/author-default.png';
			                  //return AE.getSuitableImage(d.author.images);
			                } else {
			                	return 'images/document-default.png'
			                }
			            })
			            .attr("x", "-32px")
			            .attr("y", "-32px")
			            .attr("clip-path", "url(#clipCircle" + clipPathId + ")")
			            .attr("width",
			              function(d) {
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
			              function(d) {
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
			            .attr("x", function(d) {
			                return -125;
			            })
			            .attr("dy", "50")
			            .attr('class', 'tree-nodeText')
			            .attr("text-anchor", function(d) {
			                return "start";
			            })
			            .text(function(d) {
			                if (isAuthor(d)) {
			                    //return d.author.name;
			                    var id = d.author["@id"];
			                    var author = _.findWhere( d.author.jsonld["@graph"], {"@id": id, "@type": "foaf:Person"} );
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
			            .attr("transform", function(d) {
			                return "translate(" + d.y + "," + d.x + ")";
			            });

			        // Fade the text in
			        nodeUpdate.select("text")
			            .style("fill-opacity", 1);

			        // Transition exiting nodes to the parent's new position.
			        var nodeExit = node.exit().transition()
			            .duration(duration)
			            .attr("transform", function(d) {
			                return "translate(" + source.y + "," + source.x + ")";
			            })
			            .remove();

			        nodeExit.select("circle")
			            .attr("r", 0);

			        nodeExit.select("text")
			            .style("fill-opacity", 0);

			        // Update the links…
			        var link = svgGroup.selectAll("path.tree-link")
			            .data(links, function(d) {
			                return d.target.id;
			            });

			        // Enter any new links at the parent's previous position.
			        link.enter().insert("path", "g")
			            .attr("class", "tree-link")
			            .attr("d", function(d) {
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
			            .attr("d", function(d) {
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
			        nodes.forEach(function(d) {
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
			        from.children.forEach(function(node) {
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
			            from.children.forEach(function(child) {
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

			    function getAllArtists (node, authorIds) {
			        if (isAuthor(node)) {
			            authorIds.push(node.author.id);
			        }
			        if (!node.children) {
			            return;
			        }
			        node.children.forEach(function(child) {
			            getAllArtists(child, authorIds);
			        })
			    }

			    exploredArtistIds = []
	            root = initWithArtist(data);
	            root.x0 = viewerHeight / 2;
	            root.y0 = 0;
	            update(root, true);
	            centerNode(root);
	            click(root);

			}

			return {

				restrict: 'E',
				scope:	{
					data: '='
				},
				compile: function(	element,	attrs,	transclude	)	{
					//	Create	a	SVG	root	element
					/*var	svg	=	d3.select(element[0]).append('svg');
					svg.append('g').attr('class', 'data');
					svg.append('g').attr('class', 'x-axis axis');
					svg.append('g').attr('class', 'y-axis axis');*/
					//	Define	the	dimensions	for	the	chart
					//var width = 960, height = 500;
					var width = $(element).width(), 
						height = $(element).height();

					
					var svg = d3.select(element[0]).append("svg");
					
					//	Return	the	link	function
					return	function(scope,	element,	attrs) {
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
						scope.$watch('data', function(newVal, oldVal, scope) {
								//	Update	the	chart
								if( scope.data && 
									( JSON.stringify(newVal["@graph"]) != JSON.stringify(oldVal ? oldVal["@graph"]:oldVal) ) ) {
									var data = jQuery.extend({}, scope.data);
									draw(svg, width, height, data, scope);
								}
						},	true);

					};
				}
			};
}]);

