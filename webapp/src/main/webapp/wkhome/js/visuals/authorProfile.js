
//var authorprof = angular.module('authorProfile', []);
//  d3  Factory

wkhomeApp.directive('profileTemplate', [ 'globalData','sparqlQuery', '$routeParams' , '$window' ,
    function ( globalData, sparqlQuery , $routeParams, $window ) {
                
                
    	   return {
    			restrict: 'E',
    			require: '^ngModel',
   			    scope: {
     			ngModel: '='
    			},
    		//	template: 'HOLA : {{ngModel.name}}',
         templateUrl: 'wkhome/partials/profileTemplate.html',
    link: function(scope, iElement, iAttrs, ctrl) {
    	console.log("DIrectiva");
    }
  };
        }
    ]);

/*
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
            };




             var getRelatedAuthorsByPublicationsQuery = globalData.PREFIX 
                + 'CONSTRUCT {'
                + '  <http://ucuenca.edu.ec/wkhuska/resultTitle> a uc:pagetitle;'
                + '                                              uc:viewtitle "Authors Related With {0}".'
                + '  ?subject rdfs:label ?name.'
                + '  ?subject uc:total ?totalPub .'
                + '  ?subject foaf:img ?img .'
                + '} WHERE  {'
                + '  SELECT ?subject (MAX(str(?name_)) as ?name) (COUNT( DISTINCT ?pub) as ?totalPub) (MAX(str(?img_)) as ?imgm) (IRI (?imgm) as ?img)'
                + '  WHERE { GRAPH <' + globalData.centralGraph + '> {'
                + '    <{1}> foaf:publications ?pub.'
                + '    ?subject foaf:publications ?pub;'
                + '             foaf:name ?name_.'
                + '     OPTIONAL{?subject  foaf:img ?img_.}'
                + '    FILTER(<{1}> != ?subject)'
                + '  }} GROUP BY ?subject  order by desc(?totalPub) limit 5'
                + '}';

            function relatedAuthors (id) {
                
               
         
                    var relatedAuthosPublicationsQuery = String.format(getRelatedAuthorsByPublicationsQuery, author["foaf:name"], id);
                    executeRelatedAuthors(relatedAuthosPublicationsQuery, "authorsByPublications");
                
            };
*/