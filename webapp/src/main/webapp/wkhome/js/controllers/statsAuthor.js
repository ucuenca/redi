wkhomeControllers.controller('statsAuthor', ['$scope','$routeParams', 'globalData', 'sparqlQuery', 'StatisticsbyAuthor', 'Authors', 'searchData', '$route', '$window',
  function($scope, $routeParams, globalData, sparqlQuery, StatisticsbyAuthor, Authors,  searchData, $window) {
    var self = this;
  //  $scope.data = [];
  var uri = $routeParams.author;
  console.log (uri);
 
  Authors.query({
      id: uri
    }, function(data) {
      $scope.name = data.name;
      console.log (data.name);
      //var img = data.img == null ? "/wkhome/images/no_photo.png" : data.img;
     // $scope.author.img = img;
    });



              StatisticsbyAuthor.query({id: uri }, function (data) { 
                   console.log (data);
                   


                   var value = {"ay":[],"ax":[]}; 
                   if (data["date"]){
              var date = data["date"]["data"];
              date.forEach(function (v) {
                  value.ay.push( parseInt(v['total']));
                  value.ax.push( v['y']);
              }); }

                var regexconf = /[A-Za-z\s]/;
              var conf = _.filter(data.conference.data, function(n){ return n.name.match(regexconf); });
             // var prov = _.map(data.providers.data, function(n){ return n.prov.substring(n.prov.lastIndexOf("#")+1).replace("Provider","").toProperCase;  });
              console.log (data);
              console.log ("CONF");
               console.log (conf);
           //   $scope.datacanvas =  [ "Linked Data","Mozas","Futbol","Victor","Tim berners Lee" ];
              $scope.datacanvas =  _.first(_.pluck( data.keywords.data, 'subject'), 10);
             // $scope.data = { "ay": [parseInt(5),parseInt(10),parseInt(20),parseInt(12),parseInt(7),parseInt(9),parseInt(25)] , "ax": ["2000","2001","2002","2003","2005","2008","2012"]};
             $scope.data = value ;
             // $scope.topConf = ["TIC EC","CLEI","IEEE","CNN"];
              $scope.topConf = _.first(_.pluck(_.sortBy(conf, 'total'), 'name').reverse(),5);
             // $scope.topAff =  ["Universidad de Cuenca","Politecnica de Madrid","Universidad de Zaragosa","Universidad Cátolica"];
              $scope.topAff =  _.first(_.pluck(_.uniq(data.provenance.data, 'name'), 'name'),5);
             // $scope.topProv = ["Google Schoolar","Scopus","","Scielo"];
              $scope.topProv =  _.first(_.pluck(data.providers.data, 'prov'),5);
              // ["Google Schoolar","Scopus","","Scielo"];
              $scope.datapc = {array: [{ name: "Others", y: 10 },{name: "Primeros", y: 20 }]};

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