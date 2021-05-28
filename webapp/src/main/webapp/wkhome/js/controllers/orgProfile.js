wkhomeControllers.controller('orgProfile', ['$scope', '$routeParams', '$window', 'globalData', 'sparqlQuery', 'Organizations', 
  function($scope, $routeParams, $window, globalData, sparqlQuery, Organizations) {
    // Define a new author object
    $scope.org = {};
    $scope.coauthors = {};

    /*  $scope.coauthors  = [];*/
    var org = $scope.org;
    org.uri = $routeParams.org;

    //project.encodedUri = encodeURIComponent(project.uri);
    var newhost = $window.location.protocol + '//' + $window.location.hostname + ($window.location.port ? ':8080' : '') + '';
    //var profilevalUri = '/project/profileval/'+project.uri;
    console.log ("URL");
    console.log (newhost);
    //console.log(project.uri);

    /*$scope.org = { "name" : "univerisidad de Cuenca", "cities" : ["Cuenca"] , "bio" : "Este proyecto es chevere" , "img": "https://www.universidades.com.ec/logos/original/logo-universidad-de-cuenca.png" ,
                   "npub" : "200" , "naut" : "100" , "nproy" : "50" , "areas" : [ "Ciencias de la computacion" ,"Medicina" ,"Vagueria"] };*/

    Organizations.query({
      id: org.uri
    }, function(data) {
       $scope.org = data;
       $scope.org.img = newhost+'/wkhome/images/orgs/'+$scope.org.name+'.png';
       $scope.org.areas =  sortAreas ( $scope.org.inst_by_area.data );
       console.log ($scope.org);
    }); 

    function acro ( uri ) {
      console.log (uri);

      return uri.slice(uri.lastIndexOf("/")+1).replace('university_university','').replace('_',' ');

    }

    function sortAreas ( list ) {
      console.log (list);
     var orderl = Object.entries(list).sort((a,b) => b[1]["total"] - a[1]["total"] ).map(el=>el[1]).slice(0,5);
     var areas = _.pluck(orderl, 'name');
     //console.log ("orderl");
     //console.log (orderl);
     return areas;
     //console.log (areas);


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

    $scope.publication = function() { 
      
      $window.location.hash = '/total/publications/q=affiliation-uri:("' + org.uri + '")&fl=*&rows=10&wt=json&sort=title+asc&start=0';
    }; 

    $scope.stat = function() {

      $window.location.hash = '/info/statisticsbyInst/' + org.uri; 
    };


    $scope.authors = function() {

       $window.location.hash = '/total/authors/q=org-uri:("' + org.uri + '")&fl=*&rows=10&wt=json&sort=familyname+asc&start=0';
    }; 

  //  /total/projects/q=title:A*&fq=member-organization-name:(Universidad*Católica*de*Cuenca)&fl=*&rows=10&wt=json&sort=title+asc&start=0

    $scope.projects = function() {


      $window.location.hash = '/total/projects/q=member-organization-uri:("' + org.uri + '")&fl=*&rows=10&wt=json&sort=title+asc&start=0';
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
                console.log (value);

                if (value["rdfs:label"] ) {
                  var coauthor = {};
                  var authorname = typeof value["rdfs:label"] == "string" ? value["rdfs:label"] : _.first(value["rdfs:label"], 1);
                  var anchor = $("<a class='listCoauthor' target='blank' onclick='return clickonRelatedauthor(\"" + value["@id"] + "\")'  >").text("");
                  var img = value["foaf:img"] ? value["foaf:img"] : "/wkhome/images/author-ec.png";
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
      '    CONSTRUCT {   <http://ucuenca.edu.ec/wkhuska/resultTitle> a uc:pagetitle; '+
      '    uc:viewtitle "Authors Related With {0}". '+
      '    ?person rdfs:label ?name. '+
      '    ?person foaf:img ?imgm . } '+
      '    where { ' +
      '    SELECT distinct ?person (MAX(str(?name_)) as ?name) (MAX(str(?img_)) as ?imgm)  ( COUNT (?pub) as ?pnum) '+
      ' WHERE { GRAPH <https://redi.cedia.edu.ec/context/redi> { '+
      ' ?person  <http://schema.org/memberOf> <'+ org.uri +'>. '+
      '        ?person foaf:publications ?pub  . '+
      '        ?person foaf:name ?name_ . '+
      '        OPTIONAL{?person  foaf:img ?img_.} '+
      '       } '+
      ' }  group by ?person  order by DESC (?pnum) limit 6 } ';

    function relatedAuthors(id) {

      // var relatedAuthosPublicationsQuery = String.format(getRelatedAuthorsByPublicationsQuery, author["foaf:name"], id);
      executeRelatedAuthors1(getRelatedAuthorsByPublicationsQuery, "coauthor-list");
    };
    relatedAuthors(org.uri);



  }
]);
