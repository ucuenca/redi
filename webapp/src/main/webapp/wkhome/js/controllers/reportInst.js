wkhomeControllers.controller('reportInst', ['$scope','$routeParams', 'globalData', 'sparqlQuery', 'StatisticsbyInst', 'searchData', '$route', '$window', 'Organizations', 'colProyect' ,
  function($scope, $routeParams, globalData, sparqlQuery, StatisticsbyInst, searchData  , $route,   $window , Organizations , colProyect ) {
    var self = this;
   $scope.data = [];
   var uriInst = $routeParams.org;
   // uriInst = "https://redi.cedia.edu.ec/resource/organization/UCUENCA";
    var colors = Highcharts.getOptions().colors;

   //var  colour =  ["#021487", "#0a3d2c", "#006432", "#09cb4a", "#076f15", "#0a4229", "#0ee772", "#0ec31a", "#01d3e6", "#01ad22", "#0352ac", "#085d00", "#0bb94e", "#04f6db", "#027405", "#0c5c38", "#06b83f", "#044b5a", "#056085", "#09895c", "#02baf7", "#009ef4", "#0d1c53", "#09d970", "#05421a", "#0c2f1c", "#0e6614", "#0c367a", "#0b39c4", "#0ad997", "#09c42e", "#026e56", "#08f97b", "#0c55ab", "#0d808f", "#0287f7", "#0b5b57", "#0164e8", "#023c93", "#02d865", "#09bd4d", "#05f963", "#0bdabb", "#0ddb41", "#01ea92", "#0d67c5", "#0297cf", "#0c1b68", "#0ddd19", "#05c710", "#0090fd", "#0f27d1", "#0f0ebd", "#04156b", "#00dd4f", "#0343f2", "#04fad6", "#075c8c", "#0b5071", "#024b2b", "#05e07c", "#094c0f", "#010f7d", "#0cc534", "#0e14fa", "#050f82", "#06b0c5", "#070d7e", "#0d58b4", "#04de28", "#0ba25c", "#03329a", "#007e8f", "#060bd4", "#0c7403", "#080a20", "#064021", "#01f090", "#014e59", "#08bf3c", "#058454", "#085c49", "#06d3cd", "#03b6a6", "#086f2e", "#08b6bd", "#00a25d", "#072e84", "#008b77", "#008d13", "#0389d4", "#02d071", "#08bf34", "#0c4ede", "#0f0948", "#0bae3f", "#071e20", "#002d63", "#084104", "#04f00f"];
    var colour = ['#00429d', '#3552a5', '#5062ad', '#6673b5', '#7985bb', '#8a98c1', '#58bc9b', '#91dcfc', '#4ebce8', '#219ac8', '#0077a6', '#005682', '#003760'];
    var colorred = ['#8f0000', '#b50000', '#dc0000', '#f83120', '#fe6843', '#fd9270', '#fab5a0', '#f7d6cb', '#ecd4f9', '#e3b2fc', '#d490ff', '#b476ff', '#925cff', '#6d3ffe', '#4126ef', '#0000d8'];
    var multi = ['#e5340f', '#e9520d', '#ec6a0b', '#ef8009', '#f19408', '#f4a707', '#f6b905', '#f8cb04', '#fbdc03', '#fdee01', '#a1f0e6', '#95e6cd', '#8adcb4', '#7fd19a', '#73c781', '#68bd68', '#5cb24e', '#50a834', '#459d1a', '#399200'];
    var palete = ["#001219" , "#005F73" , "#0A9396" , "#94D2BD" , "#E9D8A6" , "#CA6702" , "#BB3E03" , "#AE2012" , "#9B2226" ];



    Organizations.query({
      id: uriInst
    }, function(data) {
 
      $scope.data = data;
     // console.log (data);
    }); 


    

    StatisticsbyInst.query({
      id: uriInst
    }, function(data) {
 
     // $scope.data = data;
      console.log ("Estadisticas");
        console.log (data);

      /*var inst  = data.inst_by_inst.data;
      var allInst = [] ; 
      console.log (data);


       for (  var i = 0 ;  i < inst.length ; i++) {
        console.log (i);
        allInst.push( { name : inst[i].name , value : parseInt(inst[i].total) , color: colour[i] , position : i+1 });
      
        }
      console.log (allInst);*/
      console.log ("SORT");
    

      $scope.inscol = { container : "containerinscol" , datos :  processdata( data.inst_by_inst.data , colour )  } ;
      $scope.fuentes =  { container : "containerfuentes" , datos :  processdata( data.prov_by_inst.data , colorred ) };
     // $scope.fuentes =  { container : "containerfuentes" , datos :  processdata( data.prov_by_inst.data , colorred ) };
      $scope.areas = { container : "containerareas" , datos :  _.sortBy( processdata( data.inst_by_area.data  , colors ) , 'value').reverse()  };

      $scope.topautores = { container : "containerareas" , datos :  _.sortBy( processdata( data.author_by_inst.data  , colors ) , 'value').reverse()  };
      console.log ("RANGOOOOOO");
     // console.log (_.pluck(data.author_by_inst.data, 'total'));
      //console.log ( range ( _.pluck(data.author_by_inst.data, 'total'))); 
     // console.log ( axis ( dict ) );
      $scope.dispublications = {
       container : "containerpub" ,
       datos : axis ( range ( _.pluck(data.author_by_inst.data, 'total') ))
       };
     // prov_by_inst

    }); 

    function processdata ( inst ,  colorlist ) {
       var allInst = [] ; 
        for (  var i = 0 ;  i < inst.length ; i++) {
         // console.log (i);
          allInst.push( { name : inst[i].name , value : parseInt(inst[i].total) , color: colorlist[i] , position : i+1 });
      
        }

        return allInst;

    }

     colProyect.query({
    }, function(data) { 
      console.log ("PROYECTS");
      console.log (data);
      var   result = filterbyorg ( data , uriInst  , multi );
         console.log ("--------------------");
         console.log (result);
        $scope.proyectostable = { container : "containerproyecto" , datos :  _.sortBy( result[1] , 'value').reverse()  };
        $scope.proyectos = { container : "containerproyecto" , datos :  _.sortBy( result[0] , 'value').reverse()  };
    });


        function filterbyorg ( data , idorg , colorlist ) {
          
            var instrel =  _.where(data.links , { "source" : idorg });
         

            var newdata = [] ; 
            var lines = [] ; 
            for (  var i = 0 ;  i < instrel.length ; i++) {
              // console.log (i);
              if ( instrel[i].target.includes("redi.cedia.edu.ec") ){
              newdata.push( { name : instrel[i].target.split("/").pop()  , value : parseInt(instrel[i].nproy ) , color: colorlist[i] , position : i+1 });
              lines.push( [ instrel[i].source.split("/").pop() ,  instrel[i].target.split("/").pop() , parseInt(instrel[i].nproy )  ]) ;
            }
          }

        return [lines , newdata];

      } 


      function range ( data ) {
           dict = {};
           for ( var i = 0 ; i < data.length ;i++ ) {
              var div =  Math.ceil( data[i] / 100); 
              console.log (data[i]);
              console.log (div);
              if ( div in dict  ){
              dict [ div] = dict [div ] +1 ;
                } else {
              dict [ div ] = 1;    
                }

           }
           return dict;
      }

   
    function axis ( dict ) {
         var  xaxis = Object.keys(dict);
          var nxaxis = [];
          for (var j = 0; j < xaxis.length  ;j++){
              nxaxis.push( (xaxis[j]*100-99)+"-"+xaxis[j]*100);
          }
         var  yaxis = Object.values(dict);
         return  { ax: nxaxis , ay : yaxis } ;
    }


    /*$scope.inscol = 
    { container : "containerinscol" ,
      datos: [ { name : "UCACUENCA" , value : 40 , color: colour[0] },
    {name : "UDA" , value : 30 , color: colour[1] },
    {name : "UPS" , value : 25 , color: colour[2] },
    {name : "EPN" , value : 20 , color: colour[3] },
    {name : "UTPL" , value : 15 , color: colour[4] } ,
    {name : "UCE" , value : 10 , color: colour[5] }]
    };*/

   /* $scope.fuentes = 
    { container : "containerfuentes" ,
      datos: [ { name : "Academics Knowledge" , value : 2000 , color: colorred[0] },
    {name : "ORCID" , value : 1500 , color: colorred[1] },
    {name : "Google Scholar" , value : 900 , color: colorred[2] },
    {name : "Crossref" , value : 850 , color: colorred[3] },
    {name : "Scopus" , value : 800 , color: colorred[4] }]
    };*/



    /*$scope.areas = { container : "containerareas" ,
    datos: [ 
    {name : "Sociologia" , value : 25 , color: colors[0] },
    {name : "Psicologia" , value : 30 , color: colors[1] },
    {name : "Ciencias Politicas" , value : 15 , color: colors[2] },
    {name : "Fisica" , value : 20 , color: colors[3] },
    {name : "Medicina" , value : 160 , color: colors[4] },
    {name : "Filosofia" , value : 40 , color: colors[5] },
    {name : "Matematicas" , value : 30 , color: colors[6] },
    {name : "Ciencias de Materiales" , value : 30 , color: colors[7] },
    {name : "Historia" , value : 45, color: colors[8] },
    {name : "Geologia" , value : 20 , color: colors[9] },
    {name : "Geografia" , value : 25 , color: palete[0] },
    {name : "Ciencias Ambientales" , value : 40 , color: palete[1] },
    {name : "Ingenieria" , value : 120 , color: palete[2] },
    {name : "Economia" , value : 36 , color: palete[3] },
    {name : "Ciencias de la computacion" , value : 120 , color: palete[4] },
    {name : "Quimica" , value : 67 , color: palete[5] },
    {name : "Negocio" , value : 10 , color: palete[6] },
    {name : "Biologia" , value : 18 , color: palete[7] },
    {name : "Arte" , value : 13 , color: palete[8] }]
    };*/

/*
    $scope.proyectos = 
    { container : "containerproyecto" ,
      datos: [[ 'UCUENCA', 'UDA', 5],
    [ 'UCUENCA', 'UCACUENCA', 15],
    [ 'UCUENCA', 'UPS', 25],
    [ 'UCUENCA', 'UCE', 15],
    [ 'UCUENCA', 'UTPL', 40]
    ]};


    $scope.proyectostable = 
    { container : "containerproyecto" ,
      datos: [{ name:'UDA', value: 5} ,
    { name: 'UCACUENCA', value : 15 },
    { name : 'UPS', value : 25 } ,
    { name: 'UCE', value: 15 },
    { name: 'UTPL' , value :  40 } ]
    };*/


   $scope.dispublications = {
    container : "containerpub" ,
    datos : { ay : [ 5 , 6 , 7 , 8  , 3 , 2 , 1 , 5 , 6 ]  , ax : [ "1-5" , "6-10" , "11-15" , "16-20" , "21-25" , "26-30" , "31-35" , "36-40" , "40-45" ]}
    
    };


    $scope.topautores = {
    container : "containerpub" ,
    datos : [{ name:'Juan Pablo Carvallo', value: 5} ,
    { name: 'Victor Saquicela', value : 15 },
    { name : 'José Segarra', value : 25 } ,
    { name: 'Juanito Perez', value: 15 },
    { name: 'Tolo Valenco' , value :  40 } ]
    };
    




  }
]);

 