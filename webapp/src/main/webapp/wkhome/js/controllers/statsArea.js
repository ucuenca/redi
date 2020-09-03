wkhomeControllers.controller('statsArea', ['$scope','$routeParams', 'globalData', 'sparqlQuery', 'StatisticsbyArea', 'querySubcluster' ,'Statistics', 'searchData', '$route', '$window',
  function($scope, $routeParams, globalData, sparqlQuery, StatisticsbyArea,  querySubcluster , Statistics, $window) {
    var self = this;
  //  $scope.data = [];
  var uri = $routeParams.areauri;
  console.log (uri);


   $scope.name = "Computer Sciences (Test)";


String.prototype.toProperCase = function () {
    return this.replace(/([a-z])([A-Z])/g, '$1 $2').replace(/^./, function(str){ return str.toUpperCase(); })
};
 
  
            Statistics.query({id: "keywords_frequencypub_gt4" }, function (data) {
              var area =  _.find(data["@graph"], function (d) { return d["@id"] == uri; });
              $scope.name = area["rdfs:label"]["@value"];

            });


            querySubcluster.query({id: uri }, function (data) { 

                   $scope.datacanvas =  _.first(_.pluck( data.subclusters, 'label-en'), 10);

                 });



              StatisticsbyArea.query({id: uri }, function (data) { 

                   


              var value = {"ay":[],"ax":[]}; 
              if (data["date"]){
              var date = data["date"];
           
               Object.entries(date).forEach(item => {
                  value.ay.push( parseInt(item[1]));
                  value.ax.push( item[0]);
                })

              $scope.data = value;
            
             }

            
              var prov = _.map(data.provs.data , function(n){ return n["uri"].substring(n["uri"].lastIndexOf("#")+1).replace("Provider","").toProperCase();  });
             

              $scope.topAut = _.first( _.pluck(data.authors.data, 'name'), 5 )
              $scope.topOrg = _.first(  _.pluck(data.orgs.data, 'name')  ,5);
              $scope.topProv =  _.first( prov  ,5); 
             

              } , function (error){

                alert ("Datos no disponibles");
              
              });

  }
]);

function initialN (array) {
  if (array.length > 5) {
  return _.first(array,5);
} else {
  return array;
}
}


function minlabel (lbl) {
   var label =  lbl.split(" ");

   if (label.length > 1 ) {
     if (lbl.length > 18){
        label[1] = label[1].substring(0,1).toUpperCase()+".";
    } 
    return label[0]+" "+label[1];
   }else {
    return lbl;
   }



}