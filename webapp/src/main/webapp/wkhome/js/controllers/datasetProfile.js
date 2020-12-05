wkhomeControllers.controller('datasetProfile', ['$scope', '$routeParams', '$window', 'globalData', 'sparqlQuery', 'Datasets',
  function($scope, $routeParams, $window, globalData, sparqlQuery, Patents) {
    // Define a new author object
    $scope.patent = {};
    $scope.coauthors = {};

    /*  $scope.coauthors  = [];*/
    var patent = $scope.patent;
    patent.uri = $routeParams.patent;

    patent.encodedUri = encodeURIComponent(patent.uri);
    //var newhost = $window.location.protocol + '//' + $window.location.hostname + ($window.location.port ? ':8080' : '') + '';
    var profilevalUri = '/dataset/profileval/'+patent.uri;
    console.log ("URL pantent");
    console.log(patent.uri);

    /*$scope.patent = { "title" : "Patente de REDI" , "bio" : "REDI esta patentado por favor no copiar" };
    $scope.patent.members = [ "UCUENCA" , "CEDIA"];
    $scope.patent.funders = [ "Jose Segarra" , "Victor Saquicela", "EL jorgg" ];
    $scope.patent.starDate = "10-10-2021";
    $scope.patent.img = '/wkhome/images/projectimg.png';*/

     Patents.query({
      id: patent.uri
    }, function(data) {
      console.log ("Patentes");
      console.log (data);
       var patentdata = data.data;
      //$scope.patent = data; 
       //patentdata.members = $.map( patentdata.members.split("|") , acro );
       //patentdata.funders = $.map(patentdata.funders.split("|"), acro );
       patentdata.img = '/wkhome/images/projectimg.png';

       patentdata.subject = ['Linked data', 'Semantic Web', 'Repositorio'];
       patentdata.funders  = ['CEDIA'] ;

      //$scope.patent = { "title" : patentdata.title , "bio" : "Este proyecto es chevere" };
      $scope.patent = patentdata;
      console.log ("FUNCIONA")
      console.log (patentdata);
    } , function (error){
      console.log (error);
    });

    function acro ( uri ) {
      console.log (uri);

      return uri.slice(uri.lastIndexOf("/")+1).replace('university_university','').replace('_',' ');

    }

    function acro ( uri ) {
      console.log (uri);

      return uri.slice(uri.lastIndexOf("/")+1).replace('university_university','').replace('_',' ');

    }


    /*Authors.query({
      id: author.uri
    }, function(data) {
      
      $scope.author = data;


      for (var ki = 0; ki<data.orgs.length; ki++){
          var las = data.orgs[ki].split('/');
          data.orgs[ki] = {orgName:las[las.length-1], org:data.orgs[ki]};
      }
      
      if ( data.bio && data.bio.length > 300 )  {
        $scope.author.bio = data.bio.substring(0, 300)+" ...";
      
      }else if (data.bio == null || data.bio.length < 1){
        $scope.author.bio = "Autor registrado en REDI ";

      }

      
      var img = data.img == null ? "/wkhome/images/no_photo.jpg" : data.img;
      $scope.author.img = img;
      $scope.author.claimUri =  "https://orcid.org/oauth/authorize?client_id="+globalData.client_id+"&response_type=code&scope=/authenticate&redirect_uri="+globalData.callback+"&state="+ profilevalUri;
    
    });*/

    $scope.tree = function() {
      $window.location.hash = '/author/tree/' + patent.uri;
    };

    $scope.stat = function() {

      $window.location.hash = '/info/statisticsbyAuthor/' + patent.uri; 
    };


    $scope.network = function() {

      $window.location.hash = '/author/network/' + patent.uri;
    };

    $scope.publication = function() {

      $window.location.hash = '/author/publications/q=project-uri:%22' + project.uri + '%22&fl=*&rows=10&wt=json/authorProfile/' + project.uri;
    };

    $scope.clickonRelatedauthor = function(uri) {

      $window.location.hash = '/author/profile/' + uri;
    }

    function executeRelatedAuthors1(querytoExecute, divtoload) {
       console.log (querytoExecute);
      sparqlQuery.querySrv({
        query: querytoExecute
      }, function(rdf) {
        console.log (rdf);

        jsonld.compact(rdf, globalData.CONTEXT, function(err, compacted) {
          var authorInfo = $('div.coauthor-panel .' + divtoload);
          /*    authorInfo.html('');*/

          if (compacted) {
            var entity = compacted["@graph"];
            if (entity) {
              var values = entity.length ? entity : [entity];
              var div = $('<div>');
              authorInfo.append(div);

              values = _.sortBy(values, function(value) {
                if (value.hasOwnProperty('uc:total')) {
                  return parseInt(value["uc:total"]["@value"]);
                } else
                  return -1;
              }).reverse();

              values = _.first(values, 20);
              var coauthors = [];
              _.map(values, function(value) {

                if (value["rdfs:label"] ) {
                  var coauthor = {};
                  var authorname = typeof value["rdfs:label"] == "string" ? value["rdfs:label"] : _.first(value["rdfs:label"], 1);
                  var anchor = $("<a class='listCoauthor' target='blank' onclick='return clickonRelatedauthor(\"" + value["@id"] + "\")'  >").text("");
                  var img = value["foaf:img"] ? value["foaf:img"]["@id"] : "/wkhome/images/author-ec.png";
                  coauthor.authorname = authorname;
                  coauthor.id = value["@id"];
                  coauthor.img = img;
                  coauthors.push(coauthor);

                  return "";
                }
              });

              $scope.$apply(function() {
                $scope.coauthors.data = coauthors;
              });
            }
            //   waitingDialog.hide();
          }
          //  waitingDialog.hide();
        });
      }); // end  sparqlQuery.querySrv(...
    };

    globalData.centralGraph = 'https://redi.cedia.edu.ec/context/redi';
    //cambiar por centralgraph
    var getRelatedAuthorsByPublicationsQuery = globalData.PREFIX +
      'CONSTRUCT {\n' +
      '        <http://ucuenca.edu.ec/wkhuska/resultTitle> a uc:pagetitle;\n' +
      '                                                    uc:viewtitle \"Authors Related With {0}\".\n' +
      '        ?person rdfs:label ?name.\n' +
      '        ?person foaf:img ?img .\n' +
      '      } WHERE  {\n' +
      '        SELECT ?person (MAX(str(?name_)) as ?name) (MAX(str(?img_)) as ?imgm) (IRI (?imgm) as ?img)\n' +
      '        WHERE { GRAPH <'+globalData.centralGraph +'> {\n' +
      '          <'+patent.uri+'> <http://www.eurocris.org/ontologies/cerif/1.3/linkToPerson> ?person.\n' +
      '          ?person foaf:name ?name_  .\n' +
      '           OPTIONAL{?person  foaf:img ?img_.}\n' +
      '         \n' +
      '        } \n' +
      '    } group by ?person }  ';

    function relatedAuthors(id) {

      // var relatedAuthosPublicationsQuery = String.format(getRelatedAuthorsByPublicationsQuery, author["foaf:name"], id);
      executeRelatedAuthors1(getRelatedAuthorsByPublicationsQuery, "coauthor-list");
    };
    relatedAuthors(patent.uri);



  }
]);
