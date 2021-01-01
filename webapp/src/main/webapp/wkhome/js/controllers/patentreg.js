wkhomeControllers.controller('patentReg', ['$scope', '$routeParams', '$window', 'globalData', 'sparqlQuery', 'Patents', 'PatenteReg' , 'AuthorsServiceOrcid' , 'OrganizationsService' ,
  function($scope, $routeParams, $window, globalData, sparqlQuery, Patents , PatenteReg , AuthorsServiceOrcid , OrganizationsService) {
    
  var counterAt = 0;
  var counterOrg = 0;

  $scope.autores = [{}] ;
  $scope.organizaciones = [{}] ;


    $("#addrowauthor").on("click", function () {


        $scope.$apply(function() {


        $scope.autores.push({}); 
        });
    });

      $(".newauthors").on("click", ".ibtnDel", function (event) {
        /*console.log ("Borrar");
        $(this).closest('.newauthor').remove();       
        counterAt -= 1*/
        $scope.$apply(function() {
        $scope.autores.pop({}); 
         });

    });


       $("#addroworg").on("click", function () {

        $scope.$apply(function() {


        $scope.organizaciones.push({}); 
        });
        console.log ($scope.organizaciones);


    });

   
      $scope.pholder = "Nombre , Apellidos";
      $scope.pholderorg = "Nombre completo institución";

    $(".neworgs").on("click", ".btdelorg", function (event) {
        console.log ("Borrar");
          $scope.$apply(function() {
          $scope.organizaciones.pop();
        });
         
        //$(this).closest('.neworg').remove();       
        //counterOrg -= 1
    });


    $scope.searchOrcid = function ( orcid , author )
    {
      console.log (orcid);

      AuthorsServiceOrcid.get({
        search: orcid
      }, function (result) {

       console.log (result);
       console.log (result['response']['docs']['0']);
        //console.log ();
        if (result['response']['docs'].length > 0) {
          // $scope.autores[0].ncompleto  = result['response']['docs']['0']['name']['0'];
          // $scope.autores[0].org  = result['response']['docs']['0']['org']['0'];
           console.log (author);
             author.ncompleto = result['response']['docs']['0']['name']['0'];
             author.org  = result['response']['docs']['0']['org']['0'];
        } else {

             $scope.pholder = "No encontrado";
             author.ncompleto = "";
             author.org  = "";

        }

      }, function (some) {
        console.log("Error");
      });

    }


       $scope.searchOrg = function ( orga )
    {
     

      OrganizationsService.get({
        search: orga.siglas
      }, function (result) {

       console.log (result);
       console.log (result['response']['docs']['0']);
        //console.log ();
        if (result['response']['docs'].length > 0) {
          // $scope.autores[0].ncompleto  = result['response']['docs']['0']['name']['0'];
          // $scope.autores[0].org  = result['response']['docs']['0']['org']['0'];
           console.log (orga);
             orga.nombre = result['response']['docs']['0']['name_es'];
             //orga.org  = result['response']['docs']['0']['org']['0'];
        } else {

             $scope.pholderorg = "No encontrado";
             orga.nombre = "";
           

        }

      }, function (some) {
        console.log("Error");
      });

    }


    function changeDate (f) {

      return f.getDate() + "/" + (f.getMonth() +1) + "/" + f.getFullYear();
    }

    $scope.sendFormPantent = function(form){


     var dataform = {};

     dataform.plink = $scope.plink;
     dataform.pmeca = $scope.pmeca;
     dataform.pcode = $scope.pcode;
     dataform.ptitle = $scope.ptitle;
     dataform.pabstract = $scope.pabstract;
     dataform.preg = changeDate($scope.preg);
     dataform.papro = changeDate($scope.papro);
     dataform.pexp = changeDate($scope.pexp);
     dataform.autores = $scope.autores;
     dataform.organizaciones = $scope.organizaciones;


     console.log (dataform);
     console.log (JSON.stringify( dataform ) );


    PatenteReg.querySrv({'data': JSON.stringify(dataform)} , function (data){
    console.log (data);
    alert ("Registrado con exito");
    });


    }

  }
]);
