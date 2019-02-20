/* 
 * Controller to load/list Authors Sources
 */

wkhomeControllers.controller('listSources', ['sparqlQuery', '$scope', 'globalData', 'Statistics',  '$window' ,
    function (sparqlQuery, $scope, globalData, Statistics , window) {


        /*************************************************************/
        /*query to get the sources in memory */
        /*************************************************************/
        // loadAllKeyword();

           var u = window;
        $scope.datasources = [];
        console.log (window);

        //only keywords that appear in more than 2 articles
        var queryKeywords = globalData.PREFIX
                + ' Construct { '
                + '?subject uc:name ?name. '
                + '?subject uc:fullName ?fullName. '
                + '?subject uc:city ?city. '
                + '?subject uc:province ?province. '
                + '} '
                + 'WHERE { '
                + 'graph <' + globalData.organizationsGraph + '> '
            //    + 'graph <https://redi.cedia.edu.ec/context/organization>'
                + '{ '
                + '?subject uc:name ?name. '
                + '?subject uc:fullName ?fullName. '
                + '?subject uc:city ?city. '
                + '?subject uc:province ?province. '
                + 'FILTER (lang(?fullName) = "es").'
                + '}'
                + '} ';
        waitingDialog.show();


        Statistics.query({id: 'barchar'}, function (data) {
            var endpoints = data['@graph'];
            if (endpoints) {
                sparqlQuery.querySrv({query: queryKeywords}, function (rdf) {
                    jsonld.compact(rdf, globalData.CONTEXT, function (err, compacted) {
                        _.map(compacted["@graph"], function (pub) {
                            var model = {};
                            model["id"] = pub["@id"];
                            model["urlstat"] = "/#/info/statisticsbyInst/"+model["id"];
                            model["name"] = pub["uc:name"];
                            model["fullName"] = pub["uc:fullName"]["@value"];
                            model["city"] = pub["uc:city"];
                            model["url"] = "http://www." + pub["uc:name"].toLowerCase() + ".edu.ec/";
                            console.log ("ACA");
                            console.log ( model["urlstat"]);
                            var end=endpoints.filter(function (r){
                                return r["@id"]==pub["@id"];
                            });
                            var endi =end[0]; 
                            if (endi){
                                model["totAuthors"]=endi["uc:totalAuthors"]["@value"];
                                model["totPublications"]=endi["uc:totalPublications"]["@value"];
                                model["empty"] = false;
                            } else {
                                model["totAuthors"]='';
                                model["totPublications"]='';
                                model["empty"] = true;
                            }
                            $scope.datasources.push(model);
                        });
                        $scope.$apply(function () {
                            $scope.sources = $scope.datasources;
                        });
                        waitingDialog.hide();
                    });
                });
            }
        });
        /***********************************/
        /***********************************/







    }]); //end controller

