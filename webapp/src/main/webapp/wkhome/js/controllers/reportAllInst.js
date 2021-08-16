wkhomeControllers.controller('reportAllInst', ['$scope','$routeParams', 'globalData', 'sparqlQuery', 'StatisticsbyInst', 'searchData', '$route', '$window', 'Statistics' ,
  function($scope, $routeParams, globalData, sparqlQuery, StatisticsbyInst, searchData  , $route,   $window , Statistics ) {
    var self = this;
   $scope.data = [];
   $scope.dataAuthors = [];
   var uriInst = $routeParams.org;
   // uriInst = "https://redi.cedia.edu.ec/resource/organization/UCUENCA";
    var colors = Highcharts.getOptions().colors;

   //var  colour =  ["#021487", "#0a3d2c", "#006432", "#09cb4a", "#076f15", "#0a4229", "#0ee772", "#0ec31a", "#01d3e6", "#01ad22", "#0352ac", "#085d00", "#0bb94e", "#04f6db", "#027405", "#0c5c38", "#06b83f", "#044b5a", "#056085", "#09895c", "#02baf7", "#009ef4", "#0d1c53", "#09d970", "#05421a", "#0c2f1c", "#0e6614", "#0c367a", "#0b39c4", "#0ad997", "#09c42e", "#026e56", "#08f97b", "#0c55ab", "#0d808f", "#0287f7", "#0b5b57", "#0164e8", "#023c93", "#02d865", "#09bd4d", "#05f963", "#0bdabb", "#0ddb41", "#01ea92", "#0d67c5", "#0297cf", "#0c1b68", "#0ddd19", "#05c710", "#0090fd", "#0f27d1", "#0f0ebd", "#04156b", "#00dd4f", "#0343f2", "#04fad6", "#075c8c", "#0b5071", "#024b2b", "#05e07c", "#094c0f", "#010f7d", "#0cc534", "#0e14fa", "#050f82", "#06b0c5", "#070d7e", "#0d58b4", "#04de28", "#0ba25c", "#03329a", "#007e8f", "#060bd4", "#0c7403", "#080a20", "#064021", "#01f090", "#014e59", "#08bf3c", "#058454", "#085c49", "#06d3cd", "#03b6a6", "#086f2e", "#08b6bd", "#00a25d", "#072e84", "#008b77", "#008d13", "#0389d4", "#02d071", "#08bf34", "#0c4ede", "#0f0948", "#0bae3f", "#071e20", "#002d63", "#084104", "#04f00f"];
    var colour = ['#00429d', '#3552a5', '#5062ad', '#6673b5', '#7985bb', '#8a98c1', '#58bc9b', '#91dcfc', '#4ebce8', '#219ac8', '#0077a6', '#005682', '#003760'];
    var colorred = ['#8f0000', '#b50000', '#dc0000', '#f83120', '#fe6843', '#fd9270', '#fab5a0', '#f7d6cb', '#ecd4f9', '#e3b2fc', '#d490ff', '#b476ff', '#925cff', '#6d3ffe', '#4126ef', '#0000d8'];
    var multi = ['#e5340f', '#e9520d', '#ec6a0b', '#ef8009', '#f19408', '#f4a707', '#f6b905', '#f8cb04', '#fbdc03', '#fdee01', '#a1f0e6', '#95e6cd', '#8adcb4', '#7fd19a', '#73c781', '#68bd68', '#5cb24e', '#50a834', '#459d1a', '#399200'];
    var palete = ["#001219" , "#005F73" , "#0A9396" , "#94D2BD" , "#E9D8A6" , "#CA6702" , "#BB3E03" , "#AE2012" , "#9B2226" ];

    var grafico = "barchar";
    $scope.total = {};
    Statistics.query({
      id: grafico
    }, function(data) {
      console.log ("data"); 
       console.log (data); 
      var totalpub = 0;
      var totalautor = 0;
      var totalpro = 0;
      var datachart = data["@graph"];
        var auxdata = [];
        var authors = [];
       for (var i = 0 ;  i < datachart.length ; i++) 
       {

          totalautor = totalautor + Number(datachart[i]["uc:totalAuthors"]["@value"]);
          totalpro = totalpro + Number(datachart[i]["uc:totalProjects"]["@value"]);
          totalpub = totalpub + Number(datachart[i]["uc:totalPublications"]["@value"]);
          console.log (datachart[i]);
          auxdata.push ( { "id" : datachart[i]["@id"] ,  "cname" : datachart[i]["uc:name"]  ,  "name" : datachart[i]["uc:name"] ,  "value" :  Number(datachart[i]["uc:totalPublications"]["@value"]) , color : multi[i] } ) ;
          authors.push ( { "id" : datachart[i]["@id"] ,  "cname" : datachart[i]["uc:name"]  ,  "name" : datachart[i]["uc:name"] ,  "value" :  Number(datachart[i]["uc:totalAuthors"]["@value"]) , color : multi[i] } ) ;
       } 
       console.log ("TOTAL PUB" + totalpub);
       console.log ("TOTAL AUtor" + totalautor);
       console.log ("TOTAL pro" + totalpro);

       $scope.total.pub = totalpub;
       $scope.total.aut = totalautor;
       $scope.total.pro = totalpro;
       $scope.total.pat = 0;
       console.log (addposition (_.sortBy( auxdata , 'value').reverse()));
     
       $scope.data  = {
       container : "containerinst" ,
       datos :  addposition (_.sortBy( auxdata , 'value').reverse())
       };

       $scope.dataAuthors  = {
       container : "containerinstA" ,
       datos :  addposition (_.sortBy( authors , 'value').reverse())
       };
     // console.log (auxdata); 



    }); 

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

 