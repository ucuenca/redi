wkhomeControllers.controller('trendCluster', ['$scope', '$window', 'globalData', 'sparqlQuery', 'searchData', '$routeParams', 'Statistics', 'querySubcluster', 'reportService2', '$sce', 'StatisticsPubByArea' , 'StatisticsPubBySubArea' , '$translate' , '$rootScope' ,
  function ($scope, $window, globalData, sparqlQuery, searchData, $routeParams, Statistics, querySubcluster, reportService2, $sce , StatisticsPubByArea , StatisticsPubBySubArea , $translate , $rootScope) {

     var cluster = $routeParams.cluster;
     var subcluster = $routeParams.subcluster;
     var min = 2010;
     var max = 2020;
     var   language = $translate.use();
  

      $rootScope.$on('$translateChangeSuccess', function(event, current, previous) {
       language = $translate.use();
       callDataAreas ();
      });
        

    $scope.areaCombo = {};


     console.log ("URL");
     console.log ($window);

     function callDataSub (  clust , subclust ){

      
        if ( subclust == undefined  ) {
          subclust = '';
        }

        StatisticsPubBySubArea.query({
      cl : clust ,
      subc : subclust
    }, function (data) {

       console.log  ("todo");
       console.log  (data)
       
        var loadsub = extractData ( data , min , max  , 'SubAreas');
        
        console.log  ("combo");
        if ( subclust == ''  ) {
        $scope.subtags = loadsub;
        console.log  (loadsub);
        }

    }); }
      
       function callDataAreas (  clust ){

        if ( clust == undefined ) {
          clust = '';
        }

    

    StatisticsPubByArea.query({
      cl : clust 
    }, function (data) {

      var loadareas  =   extractData ( data , min , max , 'Areas' );
      console.log ("CARGANDO");
      console.log (loadareas);
      $scope.relatedtags = loadareas;


    }); }





    callDataAreas ( );


    function extractData ( data , min, max , type  )
    {  language = $translate.use();
       var subareas = [{'id': '' , tag : 'Todos' }];
       max = 2021;
       console.log ("DEvuelve data");
      console.log (data);
      var supertable = {};
      var min = 2010;
      for ( i = 0; i < data.length; i++ ){
       console.log (data[i]);
        element = data[i];
        if ( language == "es"){
        subarea = element.labeles;
        }else {
        subarea = element.labelen;
        }
        var ident = element['_id'] ;

        if (type == "SubAreas"){
          ident = element['_id'].split("|")[1]
        }
        subareas.push( {'id': ident , tag : subarea } );
        var table = {};

        for ( j = 0 ; j < element.data.length ; j++){
           year = element.data[j];
           table[ year.y] = year.total; 
         /*if (   min > year.y) {
           min = year.y;
         }*/

        }

        supertable[subarea] = table; 
        console.log (table);

      } 
        var dy = [];
        var finaltable = {};
        for ( subcl in supertable ){
           console.log (subcl);
           var finallist = []; 
            dy = [];
          for ( k = min ; k < 2021 ; k++ ) {
              dy.push(k);
              var value =  supertable[subcl][k];
              if ( value == undefined ) {
                value = null;
              } 

              finallist.push(parseInt(value));     
              
            }

           finaltable[subcl] = finallist; 
      }
       $scope.datacl = {};
       $scope.datacl['data'] = { 'datax' : finaltable , 'datay' : dy }
       console.log (finaltable);
       return subareas;


    }

       $scope.changeComboSub = function () {
      //$scope.datacl = {};
      $scope.datacl = {
        cluster: $scope.areaCombo.selected.id,
        subcluster: $scope.areaCombosub.selected.id
      };
       
        callDataSub ($scope.areaCombo.selected.id  ,  $scope.areaCombosub.selected.id ); 


    }


    $scope.changeCombo = function () {

      

      $scope.areaCombosub = {};

      if (!subcluster){
        $scope.datacl = {};
        $scope.datacl = {
            cluster: $scope.areaCombo.selected.id,
            subcluster: null
         };
       //callDataAreas ( $scope.areaCombo.selected.id); 
       if ( "" == $scope.areaCombo.selected.id) {
 
         callDataAreas ( $scope.areaCombo.selected.id);   
       }else {
         
         var none ;
         callDataSub (  $scope.areaCombo.selected.id , none ); 
       }
         
      } 

      /*console.log ("seleccionado"+ $scope.areaCombo.selected)
      querySubcluster.query({
        id: $scope.areaCombo.selected.id
      }, function (data) {
        $scope.subtags = [];
        _.map(data.subclusters, function (keyword) {
            var imx = {
            id: keyword["uri"],
            tag: keyword["label-en"]
          };
          $scope.subtags.push(imx);

          if (subcluster){
            if (keyword["uri"] == subcluster){
                $scope.areaCombosub.selected = imx;
            }
          }
        });
            if (subcluster) {
                $scope.changeComboSub();
            }
      });*/
    }

    $scope.exportReport = function (d) {
      var cc = "";
      var cc_ = "Todos";
      if ($scope.areaCombo.selected != undefined ){
       cc = $scope.areaCombo.selected.id;
       cc_ = $scope.areaCombo.selected.tag
      } 

      //var sc = $scope.areaCombosub.selected && $scope.areaCombosub.selected.id ? $scope.areaCombosub.selected.id : undefined;
      //var sc_ = $scope.areaCombosub.selected && $scope.areaCombosub.selected.id ? $scope.areaCombosub.selected.tag : undefined;
      console.log (cc);

      console.log (cc_);


      $scope.loading = true;

     /* var prm = [];
      if (cc && sc) {
        prm = [cc, cc_, sc, sc_];
      } else {
        prm = [cc, cc_];
      }*/

      var params = {hostname: '', report: 'ReportTrendsTotal', type: d, param1: [ cc , cc_ ]};
      reportService2.search(params, function (response) {
        var res = '';
        for (var i = 0; i < Object.keys(response).length - 2; i++) {
          res += response[i];
        }
        if (res && res !== '' && res !== 'undefinedundefinedundefinedundefined') {
          $window.open($sce.trustAsResourceUrl($window.location.origin + res));
        } else {
          alert("Error al procesar el reporte. Por favor, espere un momento y vuelva a intentarlo. Si el error persiste, consulte al administrador del sistema.");
        }
        $scope.loading = false;
      }); 
    }

    $scope.selectedValue = function () {
      return $scope.datacl;
    }


  }
]);