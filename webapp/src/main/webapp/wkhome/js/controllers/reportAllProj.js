wkhomeControllers.controller('reportAllProj', ['$scope','$routeParams', 'globalData', 'sparqlQuery', 'StatisticsbyInst', 'searchData', '$route', '$window', 'Statistics' , 'getMetricsInd',
  function($scope, $routeParams, globalData, sparqlQuery, StatisticsbyInst, searchData  , $route,   $window , Statistics , getMetricsInd ) {
    var self = this;
   $scope.data = {};
   var uriInst = $routeParams.org;
   // uriInst = "https://redi.cedia.edu.ec/resource/organization/UCUENCA";
    var colors = Highcharts.getOptions().colors;
    const partition  = 1;

   //var  colour =  ["#021487", "#0a3d2c", "#006432", "#09cb4a", "#076f15", "#0a4229", "#0ee772", "#0ec31a", "#01d3e6", "#01ad22", "#0352ac", "#085d00", "#0bb94e", "#04f6db", "#027405", "#0c5c38", "#06b83f", "#044b5a", "#056085", "#09895c", "#02baf7", "#009ef4", "#0d1c53", "#09d970", "#05421a", "#0c2f1c", "#0e6614", "#0c367a", "#0b39c4", "#0ad997", "#09c42e", "#026e56", "#08f97b", "#0c55ab", "#0d808f", "#0287f7", "#0b5b57", "#0164e8", "#023c93", "#02d865", "#09bd4d", "#05f963", "#0bdabb", "#0ddb41", "#01ea92", "#0d67c5", "#0297cf", "#0c1b68", "#0ddd19", "#05c710", "#0090fd", "#0f27d1", "#0f0ebd", "#04156b", "#00dd4f", "#0343f2", "#04fad6", "#075c8c", "#0b5071", "#024b2b", "#05e07c", "#094c0f", "#010f7d", "#0cc534", "#0e14fa", "#050f82", "#06b0c5", "#070d7e", "#0d58b4", "#04de28", "#0ba25c", "#03329a", "#007e8f", "#060bd4", "#0c7403", "#080a20", "#064021", "#01f090", "#014e59", "#08bf3c", "#058454", "#085c49", "#06d3cd", "#03b6a6", "#086f2e", "#08b6bd", "#00a25d", "#072e84", "#008b77", "#008d13", "#0389d4", "#02d071", "#08bf34", "#0c4ede", "#0f0948", "#0bae3f", "#071e20", "#002d63", "#084104", "#04f00f"];
    var colour = ['#00429d', '#3552a5', '#5062ad', '#6673b5', '#7985bb', '#8a98c1', '#58bc9b', '#91dcfc', '#4ebce8', '#219ac8', '#0077a6', '#005682', '#003760'];
    var colorred = ['#8f0000', '#b50000', '#dc0000', '#f83120', '#fe6843', '#fd9270', '#fab5a0', '#f7d6cb', '#ecd4f9', '#e3b2fc', '#d490ff', '#b476ff', '#925cff', '#6d3ffe', '#4126ef', '#0000d8'];
    var multi = ['#e5340f', '#e9520d', '#ec6a0b', '#ef8009', '#f19408', '#f4a707', '#f6b905', '#f8cb04', '#fbdc03', '#fdee01', '#a1f0e6', '#95e6cd', '#8adcb4', '#7fd19a', '#73c781', '#68bd68', '#5cb24e', '#50a834', '#459d1a', '#399200'];
    var palete = ["#001219" , "#005F73" , "#0A9396" , "#94D2BD" , "#E9D8A6" , "#CA6702" , "#BB3E03" , "#AE2012" , "#9B2226" ];

    var grafico = "barchar";
    $scope.total = {};

      getMetricsInd.query({
    id :"projectsInd"
    }, function(datos) {
       console.log (datos);

       $scope.totals = datos.projectbyresources.data[0];
       console.log ($scope.totals);
       proybyinst ( datos.projectbyinst);
       proybyYear (datos.projectbydate);
       volPub (datos.projectbyvol);
       projectinv (datos.projectbyinv);
       projectpub (datos.projectbypub);
       projectarea (datos.projectbyarea);

     

    });




      function pubtype (datos) {
      var data = datos.data;
      var total = {};
      var totalo = 0;
      for (var i = 0 ;  i < data.length ; i++) 
       {  
         switch (data[i].name) {
          case 'http://purl.org/ontology/bibo/Journal' :  total.Journal =  Number( data[i].total); break;
          case 'http://purl.org/ontology/bibo/Conference' :  total.Conference =  Number( data[i].total); break;
          case 'http://purl.org/ontology/bibo/Proceedings' : total.Proceedings = Number( data[i].total); break; 
          default :   totalo = totalo +  Number(data[i].total);
         }
         total.Others = totalo;
       }
      
       $scope.total = total;
    }
    
    function proybyYear (datos ) {
      var data = datos.data;
      var lastyears = data.slice(-22);
       var datospub = { ax:[], ay:[] };
        datosTop = [];
        lastyears.map(function (row) { 
            if (row.year){
            datospub.ax.push(row.year);
            datospub.ay.push(Number( Number(row.total) ));
            datosTop.push({name:row.year , cname : row.year , value:Number(row.total)});
             }
            return 0;

        });

        $scope.disproyectos = {container : "containerproy", "datos" : datospub , axisy : "Proyectos" };
        $scope.proyanios = {container : "containerproy" , datos : datosTop , axisy : "#Proy" };

    }

    function proybyinst ( datos ) {


      var data =    datos.data ;

      var qu = [];
      for (  var i = 0 ;  i < data.length ; i++) {
          if (data[i].c.includes("redi.cedia")){    
         var acroname =  extractInstName (data[i].c) ;  
         var fullname =  extractInstName (data[i].c) ;  
         qu.push( { id: i  , name : acroname , cname : fullname , value : parseInt(data[i].total) , color: palete[i] , position : i+1 });
         }
      }

      $scope.proyinst =  { container : "containerproybyinst" , "datos" : _.sortBy( qu , 'position')   , axisy : "#Proy" };
     // console.log ("Proyecto", $scope.projectbyinst );
    }

    function extractInstName ( uri) {
      return uri.split("/").pop();

    }

    function volPub (datos ) {
      $scope.distVolProy = {
       container : "distVolProy" , datos : axis ( range ( _.pluck(datos.data, 'total') )) , label : "#Investigadores" , axisy : "Proyectos"
       };
    }


      function projectinv ( datos ) {


      var data =    datos.data ;

      var qu = [];
      for (  var i = 0 ;  i < data.length ; i++) {
         
         var acroname = data[i].name ;  
         var fullname =  data[i].name ;  
         qu.push( { id: i  , name : acroname , cname : fullname , value : parseInt(data[i].total) , color: palete[i] , position : i+1 });
         
      }

      $scope.proyinv =  { container : "proyinv" , "datos" : _.sortBy( _.first( qu, 50) , 'position')   , axisy : "#Proy" };
     // console.log ("Proyecto", $scope.projectbyinst );
    }



  /*  getMetricsPub.query({
    uri: "",
    metric:"typePub"
    }, function(datos) {
      var data = datos.data;
      var total = {};
      var totalo = 0;
      for (var i = 0 ;  i < data.length ; i++) 
       {  
         switch (data[i].name) {
          case 'http://purl.org/ontology/bibo/Journal' :  total.Journal =  Number( data[i].total); break;
          case 'http://purl.org/ontology/bibo/Conference' :  total.Conference =  Number( data[i].total); break;
          case 'http://purl.org/ontology/bibo/Proceedings' : total.Proceedings = Number( data[i].total); break; 
          default :   totalo = totalo +  Number(data[i].total);
         }
         total.Others = totalo;
       }
      
       $scope.total = total;
    });


    getMetricsPub.query({
    uri: "",
    metric:"pubByYear"
    }, function(datos) {
    //  console.log (datos);
      var data = datos.data;
      var lastyears = data.slice(-22);
       var datospub = { ax:[], ay:[] };
        datosTop = [];
        lastyears.map(function (row) { 
            datospub.ax.push(row.y);
            datospub.ay.push(Number( Number(row.total) ));
            datosTop.push({name:row.k , cname : row.y , value:Number(row.total)});
            return 0;
        });

        $scope.dispublications = {container : "containerpub", "datos" : datospub };
        $scope.topautores = {container : "containerpub" , datos : datosTop};

    });


    getMetricsPub.query({
    uri: "",
    metric:"quartilPub"
    }, function(datos) {
    //  console.log ("quartil");
     // console.log (datos);
      var data = datos.data;
      var qu = [];
      for (  var i = 0 ;  i < data.length ; i++) {
         var acroname =  data[i].qu;  
         var fullname =  data[i].qu;  
         qu.push( { id: i  , name : acroname , cname : fullname , value : parseInt(data[i].total) , color: palete[i] , position : i+1 });
      }
      $scope.quartil =  { container : "containerqu" , datos : _.sortBy( qu , 'value').reverse()  };

    //  console.log (qu);
      

    });


    getMetricsPub.query({
    uri: "",
    metric:"volPub"
    }, function(datos) {
     //console.log ("VOl");
      //console.log (datos);
      $scope.distVolpublications = {
       container : "distVolpublications" , datos : axis ( range ( _.pluck(datos.data, 'total') )) , label : "#Pub" , axisy : "Investigadores"
       };

       /*for ( ) {

       }*/

    //  console.log ($scope.distVolpublications);

   //);*/


        function range ( data ) {
           dict = {};
           var max = 25/partition;
           dict [ max ] = 0;
           for ( var i = 0 ; i < data.length ;i++ ) {
              var div =  Math.ceil( data[i] / partition); 
              //console.log (data[i]);
              //console.log (div);

              if ( div > max) {
                //if ( ! max in dict ) {
                //  dict [ max ] = 0;
               // }
                dict [ max ] =    dict [ max ] + 1;
              //  console.log (dict [ max ]);
              } else {

              if ( div in dict  ){
              dict [ div] = dict [div ] +1 ;
                } else {
              dict [ div ] = 1;    
                }
              }

           }
          // console.log (dict);
           return dict;
      }

   
    function axis ( dict ) {
         var  xaxis = Object.keys(dict);
          var nxaxis = [];

          for (var j = 0; j < xaxis.length  ;j++){
                  if ( j == xaxis.length - 1){
                 nxaxis.push( ("+"+xaxis[j]*partition));
                  }else {
                 nxaxis.push( (xaxis[j]*partition-(partition -1)));
                 }
          }
         var  yaxis = Object.values(dict);
         return  { ax: nxaxis , ay : yaxis } ;
    }

    function providers ( datos) {
      var data = datos.data;
      var pos = 0;
      //$scope.areas =  { container : "containerar" , datos : _.sortBy( processSquare (data ) , 'value').reverse()  };
      var dictareas = {};
      dictareas["ScopusAcademics"] = "http://ucuenca.edu.ec/ontology#ScopusProvider,http://ucuenca.edu.ec/ontology#AcademicsKnowledgeProvider";
      dictareas["ScopusGoogle"] = "http://ucuenca.edu.ec/ontology#ScopusProvider,http://ucuenca.edu.ec/ontology#GoogleScholarProvider";
      dictareas["AcademicsGoogle"] = "http://ucuenca.edu.ec/ontology#AcademicsKnowledgeProvider,http://ucuenca.edu.ec/ontology#GoogleScholarProvider";
      dictareas["AcademicsGoogleScopus"] = "http://ucuenca.edu.ec/ontology#AcademicsKnowledgeProvider,http://ucuenca.edu.ec/ontology#GoogleScholarProvider,http://ucuenca.edu.ec/ontology#ScopusProvider";
      var bases = [ "http://ucuenca.edu.ec/ontology#ScopusProvider","http://ucuenca.edu.ec/ontology#AcademicsKnowledgeProvider", "http://ucuenca.edu.ec/ontology#GoogleScholarProvider"]; 

      var prov = [];
      var overlap = [];
      for (  var i = 0 ;  i < data.length ; i++) {
         if ("name" in data[i]){
          var provname =  data[i].name;
          if ( provname in dictareas ) {
            provname =  dictareas[provname];
          }
          var mu =  provname.split(",");
          var value = parseInt(data[i].total);
          var concat = "";
          mu.forEach(element =>   concat = extractProvName ( element ) +"-"+ concat   );
         // console.log ("NOMBRES");
         // console.log (concat);
        
          overlap.push ( {sets : mu , 'value' : value  , 'name' : concat.slice(0, -1) } );
          
          //var fullname =  data[i].name;


         }else {

            var provname = data[i].prov ;
            var name = extractProvName ( provname );
            prov.push( { id: i  , name : name , cname : name , value : parseInt(data[i].total) , color: colors[i] , position : pos+1 });
            pos++;
            if ( bases.includes(provname) ){
            overlap.push ( {sets : [provname] , 'value' : parseInt(data[i].total)  , 'name' : name } );
            }
         }

           $scope.providers =  { container : "containerprov" , datos : _.sortBy( prov , 'value').reverse()  };
           //console.log ("overlap");
          // console.log (overlap);
           $scope.providersOver =  { container : "containerOver" , datos : _.sortBy( overlap , 'value').reverse()  };
    }
  }



    

    function projectpub ( datos) {
         var data =    datos.data ;

        var npub = [];
        for (  var i = 0 ;  i < data.length ; i++) {
           
           var acroname = data[i].name ;  
           var fullname =  data[i].name ;  
           npub.push( { id: i  , name : acroname , cname : fullname , value : parseInt(data[i].total) , color: palete[i] , position : i+1 });
           
        }

        $scope.proypub =  { container : "proyinv" , "datos" : _.sortBy( _.first( npub, 25 ) , 'position')   , axisy : "#Proy" };

        $scope.cprojectbypub = {
       container : "cprojectbypub" , datos : axis ( range ( _.pluck(datos.data, 'total') )) , label : "#Investigadores" , axisy : "Proyectos"
       };


    }



    function projectarea ( datos) {
         var data =    datos.data ;

        var area = [];
        for (  var i = 0 ;  i < data.length ; i++) {
           
           var acroname = data[i].label ;  
           var fullname =  data[i].label ;  
           area.push( { id: i  , name : acroname , cname : fullname , value : parseInt(data[i].area) , color: palete[i] , position : i+1 });
           
        }

        $scope.proyarea =  { container : "proyarea" , "datos" : _.sortBy( _.first( area, 50 ) , 'position')   , axisy : "#Proy" };

    }


   /*etMetricsPub.query({
    uri: "",
    metric:"topJournals"
    }, function(datos) {
     // console.log ("JOURNAL");
     // console.log (datos);
      var data = datos.data;

      $scope.journals =  { container : "containerjo" , datos : _.sortBy( processSquare (data ) , 'value').reverse()  };
    });*/



  /*  getMetricsPub.query({
    uri: "",
    metric:"topAreas"
    }, function(datos) {
    // console.log ("AREAS");
    //  console.log (datos);
      var data = datos.data;
      
      $scope.areas =  { container : "containerar" , datos : _.sortBy( processSquare (data ) , 'value').reverse()  };
    });*/


/*
    getMetricsPub.query({
    uri: "",
    metric:"overlapProviders"
    }, function(datos) {
     //console.log ("PROV");
     // console.log (datos);
      var data = datos.data;
      
      //$scope.areas =  { container : "containerar" , datos : _.sortBy( processSquare (data ) , 'value').reverse()  };
      var dictareas = {};
      dictareas["ScopusAcademics"] = "http://ucuenca.edu.ec/ontology#ScopusProvider,http://ucuenca.edu.ec/ontology#AcademicsKnowledgeProvider";
      dictareas["ScopusGoogle"] = "http://ucuenca.edu.ec/ontology#ScopusProvider,http://ucuenca.edu.ec/ontology#GoogleScholarProvider";
      dictareas["AcademicsGoogle"] = "http://ucuenca.edu.ec/ontology#AcademicsKnowledgeProvider,http://ucuenca.edu.ec/ontology#GoogleScholarProvider";
      dictareas["AcademicsGoogleScopus"] = "http://ucuenca.edu.ec/ontology#AcademicsKnowledgeProvider,http://ucuenca.edu.ec/ontology#GoogleScholarProvider,http://ucuenca.edu.ec/ontology#ScopusProvider";
      var bases = [ "http://ucuenca.edu.ec/ontology#ScopusProvider","http://ucuenca.edu.ec/ontology#AcademicsKnowledgeProvider", "http://ucuenca.edu.ec/ontology#GoogleScholarProvider"]; 

      var prov = [];
      var overlap = [];
      for (  var i = 0 ;  i < data.length ; i++) {
         if ("name" in data[i]){
          var provname =  data[i].name;
          if ( provname in dictareas ) {
            provname =  dictareas[provname];
          }
          var mu =  provname.split(",");
          var value = parseInt(data[i].total);
          var concat = "";
          mu.forEach(element =>   concat = extractProvName ( element ) +"-"+ concat   );
         // console.log ("NOMBRES");
         // console.log (concat);
        
          overlap.push ( {sets : mu , 'value' : value  , 'name' : concat.slice(0, -1) } );
          
          //var fullname =  data[i].name;


         }else {

            var provname = data[i].prov ;
            var name = extractProvName ( provname );
            prov.push( { id: i  , name : name , cname : name , value : parseInt(data[i].total) , color: colors[i] , position : i+1 });
            if ( bases.includes(provname) ){
            overlap.push ( {sets : [provname] , 'value' : parseInt(data[i].total)  , 'name' : name } );
            }
         }

           $scope.providers =  { container : "containerprov" , datos : _.sortBy( prov , 'value').reverse()  };
           //console.log ("overlap");
          // console.log (overlap);
           $scope.providersOver =  { container : "containerOver" , datos : _.sortBy( overlap , 'value').reverse()  };

      }

    });*/


    function extractProvName ( cname ) {
      return cname.split("#").pop().replace("Provider",""); 
    }
    


    function processSquare (data ) {

      var newobjects = [];
      for (  var i = 0 ;  i < data.length ; i++) {
         var acroname =  data[i].name;  
         var fullname =  data[i].name;  
         newobjects.push( { id: i  , name : acroname , cname : fullname , value : parseInt(data[i].total) , color: colors[i] , position : i+1 });
      }

      return newobjects;
    }

    

 

    function addposition ( lista ) {
      return   _.each(lista , function(element, index) {
         _.extend(element, {'position' : index+1});
      });

    }



     $scope.redirect  = function( value ) {
      $window.location.hash = '/report/' + value; 
    };






  }
]);

 