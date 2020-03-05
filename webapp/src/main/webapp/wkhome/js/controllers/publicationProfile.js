wkhomeControllers.controller('publicationProfile', ['$scope', '$routeParams', '$window', 'globalData', 'sparqlQuery', 'Authors', 'RecomendService',
  function($scope, $routeParams, $window, globalData, sparqlQuery, Authors, RecomendService) {
    // Define a new author object


      
     // var uri = "https://redi.cedia.edu.ec/resource/publication/080790388cd3adbc37099a6e41b3e163";
           $scope.publication = {};
    var publication = $routeParams.pub;
    console.log ("RUTAS");
      console.log(publication);
      var uri = "";
       var route = $window.location.origin;
      console.log (route);
      
        $scope.quantity = 4;
        $scope.quantityf = 5;

    $scope.$on('hijo', function(e) {  
     var  uri  = $scope.$parent.$parent.puburi;
        $scope.child(uri);        
    });


  $scope.loadRelatedAuthor = function (e) {
       console.log ("Cambiar modal");
       console.log(e);

       var  uri  = e ;
     
       $scope.child(uri);  
  }

  
   $scope.exportar = function() {
         const filename  = 'article.pdf';
         var quality = 1;

  html2canvas(document.querySelector('#nodeToRenderAsPDF'), 
                {scale: quality}
             ).then(  function (canvas)  {
      var pdf = new jsPDF('p', 'mm', 'a4');
     // pdf.addImage(canvas.toDataURL('image/png'), 'PNG', 0, 0, 211, 298);
      //pdf.addImage(canvas.toDataURL('image/png'), 'PNG', 0, 0, 211, 235);
        pdf.addImage(canvas.toDataURL('image/png'), 'PNG', 0, 0, 210, 200);
      pdf.save(filename);
    });
    }; 
         // $scope.child();   

          $(function(){ // let all dom elements are loaded
         $('#myModal').on('hide.bs.modal', function (e) {
           $("body").css("overflow-x","auto");
           $("body").css("overflow-y","auto");

        });
        });

        $scope.child = function ( uri ) {
      waitingDialog.show();
        
     //  uri  = 

     var querytoExecute = "PREFIX bibo: <http://purl.org/ontology/bibo/>"
  +   "PREFIX dct: <http://purl.org/dc/terms/> "
  +        "PREFIX foaf: <http://xmlns.com/foaf/0.1/> "
  +        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
  + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
  + "construct { "
  + "<"+uri+"> ?p ?v . "
  + "?v ?h ?q . "
  + "} where { "
  + "graph <https://redi.cedia.edu.ec/context/redi> { "
  + "values ?p { bibo:uri rdf:type dct:title bibo:abstract dct:subject dct:creator dct:contributor dct:isPartOf bibo:doi bibo:isbn} . "
  + "values ?h { foaf:name rdfs:label rdf:type foaf:img } . "
  + "<"+uri+"> ?p ?v . "
  + "optional {?v ?h ?q .} "
  + "} "
  + "} ";

       sparqlQuery.querySrv({
        query: querytoExecute
      }, function(rdf) {
              console.log (rdf);
        jsonld.compact(rdf, globalData.CONTEXT, function(err, compacted) {
        
          if (compacted) {
              console.log (compacted);
              var data = compacted["@graph"];
              var dataToSend = {};
                  dataToSend["keywords"] = [];
                  dataToSend["autores"] = [];
                  dataToSend["revista"] = {}; 
                  dataToSend["fuentes"] = []; 
                data.forEach( function(valor, indice, array) {
                      if ("@type" in valor){
                      var arraytype = [];
                      var tp = valor["@type"];
                      if (!Array.isArray(tp)) {
                           arraytype.push(tp);
                      } else {
                          arraytype = tp;
                      }

                      for (var i=0 ; i< arraytype.length ;i++){
                     switch (arraytype[i]) {
                     case "bibo:AcademicArticle":
                      dataToSend ["title"] = unique( valor ["dct:title"]);
                      dataToSend ["abstract"] = unique(valor ["bibo:abstract"]);
                      dataToSend ["doi"] = unique(valor ["bibo:doi"]);
                      dataToSend ["isbn"] = unique(valor ["bibo:isbn"]);

                       if ("bibo:uri" in valor ) {
                        var f = [];
                         _.each(valor["bibo:uri"], function (v){  console.log (v["@id"]); return    v["@id"] === undefined ? f.push (v) : f.push( v["@id"]); })
                         //= _.pluck(valor["bibo:uri"], '@id'); 
                            
                        console.log ("Valor f");
                        console.log (f);
                       // f = _.filter(f , function(v,key){ return  v.length > 0 ; });
                        var firstacm = _.find(f , function(v ){ return  v !== undefined  && v.indexOf(".acm.") == -1 ? false : true ; });
                        var noacm = _.filter(f , function(v,key){ return  v !== undefined && v.indexOf(".acm.") == -1 ? true : false   ; });
                        if (firstacm){
                        noacm.push (firstacm);
                        }
                         dataToSend["fuentes"] = _.map(noacm, function(v, key){ return { uri: v , name: classifyURLS( v ) , img : "wkhome/images/"+classifyURLS( v )+".png"  }; });
                        
                      }
                         
                     break; 
                     case "foaf:Person":
                       var img = "";
                      

                     
                         if ( valor["@id"].split("/").length > 6) {
                            var auturi  = valor["@id"];
                         }

                       if ("foaf:img" in valor)
                       {
                         //img = valor["foaf:img"]["@id"];
                         img = unique (valor["foaf:img"])["@id"];
                       
                       }else {
                         img = "wkhome/images/author-default.png";

                       }
                      dataToSend["autores"].push ({ "uri": route+"/#/author/profile/"+auturi , "name":basicName(unique(valor["foaf:name"])),"img": img});   
                       break;
                      case "bibo:Proceedings":
                      dataToSend["revista"]["Proceedings"]= unique(valor["rdfs:label"]);
                      break;
                       case "bibo:Conference":

                      dataToSend["revista"]["Conference"]= unique(valor["rdfs:label"]);
                      break;
                        case "bibo:Book":
                        dataToSend["revista"]["Book"]= unique(valor["rdfs:label"]);
                     break; 
                      case "bibo:Journal":
                       if ("foaf:img" in valor) {
                        dataToSend["revista"]["img"] =  typeof valor["foaf:img"] === 'object' ? valor["foaf:img"]["@id"] : valor["foaf:img"]  ;
                       }
                        dataToSend["revista"]["Journal"]= unique(valor["rdfs:label"]);
                      
                     break; 
                        case "bibo:uri":

                     break; 
                        case "":
                     break; 

                    }
                    }
                    } else {


                       
                      if ( "rdfs:label"  in valor   ){
                        var ku = unique(valor["rdfs:label"]);
                        if ( typeof ku != 'object' ){ 
                        dataToSend["keywords"].push (ku);
                      }
                     }
                    }
              });
                console.log (dataToSend["keywords"]);
              var key = _.reduce( dataToSend["keywords"] , function(memo, num){ return  num.length > 0 ? num.replace(",","")+", "+memo : "" ; }, "");
          RecomendService.get({
          search: key
         }, function(result) { 
            console.log ("RECOMENDACIONES RESP");
           console.log ("Resultado");
           console.log (result.response);
           var recomendacion = [];
           if ( result.response) {

           var recomends =  result.response.docs;
               recomends.forEach( function(valor, indice, array) { 
                if(valor["lmf.uri"].trim() != uri){
                  var authors = _.reduce(valor["contributor-name"], function(memo, num){ return  num.toProperCase().replace(",","")+", "+memo; }, "");
                 var r = {"uri": valor["lmf.uri"].trim() , "title": valor["title"] , "authors" : authors };
                  recomendacion.push (r);
                }
               });
               dataToSend["recomendacion"] =  recomendacion;

               console.log (recomendacion);
           }
         } , function ( fail){
              var recomendacion = [];
              console.log ("Related sources not found");
              /* var r = {"uri": "www.google.com" , "title": "Plataforma para hacer papas fritas" , "authors" : "DON SUCO, EL VILLIE, TU MAMA" };
               var r2 = {"uri": "www.google.com" , "title": "FISICA para locos" , "authors" : "EL LUCHO, MARA SEXY" };
               recomendacion.push (r);
               recomendacion.push (r2);
               dataToSend["recomendacion"] =  recomendacion;*/
       
         });    
               
              

                 $scope.$apply(function () {
              $scope.publication.quantity =  4;   
              $scope.publication.quantityf = 5;
              $scope.publication = dataToSend;
                 });
              console.log (dataToSend);
          }
            waitingDialog.hide();
             _altmetric_embed_init(); // Reload altmetrics
             $scope.$parent.$parent.showmodal ();
              $("body").css("overflow-x","hidden");
              $("body").css("overflow-y","hidden");
              //overflow-x: hidden;
              //overflow-y: hidden;
        });
      }); // end  sparqlQuery.querySrv(...
 } // FIN CHILD
        function exportar () {
        console.log ("CLICK");
         }

         function basicName ( str ){
          str = toTitleCase(str);
          if (str.indexOf(",") != -1){
          var espace = str.trim().indexOf(" ");
          var com = str.trim().indexOf(",");
          var fname = str.substring(com+1).trim();
          var findex = fname.indexOf(" ");
          if (findex < 0) { findex = fname.length }
          return str.substring(0, espace+1)+", "+ fname.substring (0,findex);
          }
          return str;
          //

         }

         function toTitleCase(str) {
        return str.replace(/\w\S*/g, function(txt){
        return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase();
         });
        }

        function unique( a) {
            return Array.isArray(a) ? a[0] : a;
        }

        String.prototype.toProperCase = function () {
    return this.replace(/\w\S*/g, function(txt){return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase();});
};

     function classifyURLS( puri ){
          if (!puri) {
            return "";
          }
    var type = "web";
    if ( puri.indexOf(".pdf") !== -1) {
     type = "pdf";
    } else if ( puri.indexOf("www.scopus.com") !== -1){
     type = "scopus";
    } else if ( puri.indexOf("redi.cedia.edu.ec") !== -1){
     type = "rdf";
    } 
    else if ( puri.indexOf(".acm.") !== -1){
     type = "acm";
    } 
    else if ( puri.indexOf("ieee.org") !== -1){
     type = "ieee";
    } else if ( puri.indexOf("springer.com") !== -1){
     type = "springer";
    }  else if ( puri.indexOf("dblp") !== -1){
     type = "dblp";
    } else if ( puri.indexOf("dspace") !== -1){
     type = "dspace";
    } else if ( puri.indexOf("www.sciencedirect.com") !== -1){
     type = "science_direct";
    }
    return type ;
  }


   showmoreauthors = function  () {
     console.log ("MORE");
     if ($scope.quantity > 4){
        $scope.quantity = 4;
     }else {
       $scope.quantity = 20;
     }

      $scope.$apply(function () {
              $scope.publication.quantity =   $scope.quantity;  
                 });

     // print ( $scope.publication["authors"]);
    
   }


     showsources = function () {
     console.log ("MORE");
     if ($scope.quantityf > 5){
        $scope.quantityf = 5;
     }else {
       $scope.quantityf = 20;
     }

      $scope.$apply(function () {
              $scope.publication.quantityf =   $scope.quantityf;  
                 });

     }

       if ( publication === undefined){
        console.log ("PADRE");
         console.log ($scope.$parent.$parent);
        console.log ($scope.$parent.$parent.puburi);
            uri  = $scope.$parent.$parent.puburi;
       }else {
            uri = publication ; 
            $scope.child(uri);  

       }
      console.log ($routeParams);
       

  

        //var uri = "https://redi.cedia.edu.ec/resource/publication/080790388cd3adbc37099a6e41b3e163";
        //var key = "Geospatial and statistical information INSPIRE Linked data RDF " 
   
   
    /*  $scope.coauthors  = [];*/
  
    /*   var p = {};
       p.title = "Reduciendo la sobrecarga de información en usuarios de televisión digital";
       p.abstract = "La televisión digital trae consigo un acceso más amplio a múltiples canales y servicios interactivos. Este aumento de programación televisiva obliga al usuario a involucrarse en tareas de recuperación de información cada vez que desee observar un programa de televisión. Este trabajo presenta una propuesta de un sistema de recomendación personalizada, cuyos elementos innovadores son la representación formal del perfil del usuario y el contenido televisivo basado en ontologıas preparadas a propósito, o descubiertas en lınea, y un enriquecimiento de la información con recursos externos como redes sociales, bases de datos de pelıculas, repositorios de guıas de programación, y datos enlazados abiertos que permitan ayudar al televidente a filtrar los programas más relevantes basándose en sus necesidades y preferencias.";
       p.keywords = ["Semantic Annotation","GeoNames","Geospatial RESTful Service"," Semantic Annotation"];
       var a1 =  { name : "Andres Tello", img : "https://scholar.google.com/citations?view_op=view_photo&user=TMcvy8kAAAAJ" } ;
       var a2 =  { name : "José Segarra", img : "https://rediclon.cedia.edu.ec/wkhome/images/author-ec.png" } 
       var a3 =  { name : "Victor Saquicela", img : "https://scholar.google.com/citations?view_op=view_photo&user=nYte-bkAAAAJ" } 

       p.autores = [];
       p.fuentes = [];

       p.autores.push(a1);
       p.autores.push(a2);
       p.autores.push(a3);

       var p1 = { name:"DBLP", img:"wkhome/images/dblp.png"};
       var p2 = { name:"SCOPUS", img:"wkhome/images/scopus.png"};
       var p3 = { name:"SPRINGER", img:"wkhome/images/springer.png"};
       var p4 = { name:"REPOSITORIO", img:"wkhome/images/world.png"};

       p.fuentes.push (p1);
       p.fuentes.push (p2);
       p.fuentes.push (p3);
       p.fuentes.push (p4);

       p.revista = { title:"Maskana" , description: "Latindex", img: "http://www.latindex.org/lat/portadas/fotRev/20303.jpg"};

       p.recomendacion = [{title:"Plataforma para hacer papas fritas ", authors:"El Victor, El mauricio, el Villie"} , {title:"Semantic integration of Ecuadorian geospatial data in the context of hydrology domain", authors:"Vilches L.m., Victor Squicela, Espinoza Mejia Jorge Mauricio, Rosa Lucia Lupercio Novillo, Eduardo Tacuri"}];
*/
     // $scope.publication = p;
    //  console.log (p);


  }
]);
