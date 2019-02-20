wkhomeControllers.controller('statsInst', ['$scope','$routeParams', 'globalData', 'sparqlQuery', 'StatisticsbyInst', 'searchData', '$route', '$window',
  function($scope, $routeParams, globalData, sparqlQuery, StatisticsbyInst, searchData, $window) {
    var self = this;
  //  $scope.data = [];
  var uriInst = $routeParams.inst;



  StatisticsbyInst.query({id: uriInst }, function (data) {
           
       var name = data["fullname"];

           $scope.name =  name.toProperCase();

     var date = data["pub_by_date"]["data"];
      var value = {"ay":[],"ax":[]}; 
               
              date.forEach(function (v) {
                  value.ay.push( parseInt(v['total']));
                  value.ax.push( v['y']);
              });

 

             $scope.data = value;
       var total = 0;
     var areas =  data["inst_by_area"]["data"];
      areas.forEach(function (v) { total = total+ parseInt(v.total) });
      areas = _.map(areas, function(value,i){ return{ name: value.name , label:minlabel (value.name) , y: Number((100*value.total)/total)}; })
     var principales =  _.first(areas,7);
     var secundarios =  _.rest(areas, 7);
     var valorsec = 0;
       secundarios.forEach(function (v) { valorsec = valorsec+ Number(v.y) });
      principales.push ({ name: "Others", y:valorsec});


          $scope.datapc = {array: principales};

            var author = data["author_by_inst"]["data"];
             var prov = data["prov_by_inst"]["data"];
              var inst = data["inst_by_inst"]["data"];
                author = _.map(author, function(value,i){ return {"uri": value["uri"], "name": value["name"].toProperCase() } });
                inst = _.map(inst, function(value,i){ return { "name": value["name"].toProperCase() } });



              $scope.topAuthors = _.pluck( initialN (author), 'name');
              $scope.topInst = _.pluck( initialN (inst) , 'name');
              $scope.topProv = _.pluck( initialN (prov), 'name');



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

String.prototype.toProperCase = function () {
    return this.replace(/\w\S*/g, function(txt){return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase();});
};

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