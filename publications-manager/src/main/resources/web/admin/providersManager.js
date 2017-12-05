/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

 /*var dataSet = [
    [ "UDA", "01/01/01 AC", "01/01/01 DBLP", "01/01/01", "2011/04/25", "01/01/01" ],
    [ "UCUENCA", "", "", "12/12/12", "2011/07/25", "50/81/90" ],
    [ "UTPL", "", "", "", "2009/01/12", "" ]
    ];*/
 // var host = "http://localhost:8080/";
 var host = _SERVER_URL;
function loadTables () {
  
    console.log ("Graficando");
   $('#provTable').DataTable({
            ajax: host + "pubman/publication/organization/list",
                       columns: [
            { "data" : "Selection" },
            { "data" : "name"} ,
            { "data" : "valAK"} ,
            { "data" : "valDBLP"} ,
            { "data" : "valScopus"} ,
            { "data" : "valGs"} 
            
    

        ] ,
      
        columnDefs: [
           
              {
                "render": function(data, type, row) {
                  
                    return   '<input id="'+row["name"]+'" type="checkbox" name="selection" value="'+row["uri"]+'">';
             
                },
                targets: 0
            }  , 
               {
                "render": function(data, type, row) {
                  
                    return  data;             
                },
                
                targets: 1
            } ,
               {
                "render": function(data, type, row) {
                    
                    if (typeof(row["DateAk"]) === "undefined" && typeof(row["AdvAK"]) === "undefined"){
                       return "";
                   }else {
                       return row["DateAk"]+" | " +row["AdvAK"];    
                   }         
                },
                
                targets: 2
            } ,
             {
                "render": function(data, type, row) {
                    
                    if (typeof(row["DateDBLP"]) === "undefined" && typeof(row["AdvDBLP"]) === "undefined")
                    {
                       return ""; 
                    }
                    else {
                       return  row["DateDBLP"]+" | "+row["AdvDBLP"];
                    }
                },
                
                targets: 3
            } ,
              {
                "render": function(data, type, row) {
                    
                        
                    if (typeof(row["DateScopus"]) === "undefined" && typeof(row["AdvScopus"]) === "undefined")
                    {
                       return ""; 
                    }
                    else {
                       return  row["DateScopus"]+" | "+row["AdvScopus"];      
                    }
                  
                       
                },
                
                targets: 4
            } , 
             {
                "render": function(data, type, row) {
                          
                    if (typeof(row["DateGs"]) === "undefined" && typeof(row["AdvGs"]) === "undefined")
                    {
                       return ""; 
                    }
                    else {
                         return  row["DateGs"]+" | "+row["AdvGs"];  
                    }
                  
                             
                },
                
                targets: 5
            }
        ]
    
    });
    }
    
    function ExtractAk()  {
         var publications = [];
         $('tbody tr input:checked').each(function (index) { 
             console.log ($(this).val())
             publications.push($(this).val());
         })
         
         console.log (publications);
         
           if (publications.length < 1) {
             alert ("No publications selected");
         }else {
             var listPublications = { "data": publications };
        $.ajax({
        type: "POST",
        data:  listPublications  ,
        dataType: "text", //result data type
        //contentType : "application/x-www-form-urlencoded; charset=UTF-8" ,
        url: host + "pubman/publicationsAkByOrg",
        success: function(Result) {
          // table.ajax.reload( null, false );
            var resultados = JSON.parse(Result);
            console.log(Result);
            for (var key in resultados) {
              console.log (key+""+resultados[key]);
             //$('span#'+key+'').text(resultados[key]);
            // $("span[id='"+key+"']").text(resultados[key]);
 
            }
          // alert (Result);

        },
        error: function(data ) {
            //document.getElementById("imgloading").style.visibility = "hidden";
            alert("Error" + data.responseText);
         } });
             
         }
        
        
    }
    
    
        function ExtractScopus()  {
         var publications = [];
         $('tbody tr input:checked').each(function (index) { 
             console.log ($(this).val())
             publications.push($(this).val());
         })
         
         console.log (publications);
         
           if (publications.length < 1) {
             alert ("No providers selected");
         }else {
             var listPublications = { "data": publications };
        $.ajax({
        type: "POST",
        data:  listPublications  ,
        dataType: "text", //result data type
        //contentType : "application/x-www-form-urlencoded; charset=UTF-8" ,
        url: host + "pubman/publicationsScopusByOrg",
        success: function(Result) {
            console.log (Result);
          // table.ajax.reload( null, false );
           /* var resultados = JSON.parse(Result);
            console.log(Result);
            for (var key in resultados) {
              console.log (key+""+resultados[key]);*/
             //$('span#'+key+'').text(resultados[key]);
            // $("span[id='"+key+"']").text(resultados[key]);
 
            //}
          // alert (Result);

        },
        error: function(data ) {
            //document.getElementById("imgloading").style.visibility = "hidden";
            alert("Error" + data.responseText);
         } });
             
         }
        
        
    }
    
    
           function ExtractDBLP ()  {
         var publications = [];
         $('tbody tr input:checked').each(function (index) { 
             console.log ($(this).val())
             publications.push($(this).val());
         })
         
         console.log (publications);
         
           if (publications.length < 1) {
             alert ("No providers selected");
         }else {
             var listPublications = { "data": publications };
        $.ajax({
        type: "POST",
        data:  listPublications  ,
        dataType: "text", //result data type
        //contentType : "application/x-www-form-urlencoded; charset=UTF-8" ,
        url: host + "pubman/publicationsDBLPByOrg",
        success: function(Result) {
            console.log (Result);

        },
        error: function(data ) {
            //document.getElementById("imgloading").style.visibility = "hidden";
            alert("Error" + data.responseText);
         } });
             
         } 
    }
    
    
           function ExtractGoogleSchoolar()  {
         var publications = [];
         $('tbody tr input:checked').each(function (index) { 
             console.log ($(this).val())
             publications.push($(this).val());
         })
         
         console.log (publications);
         
           if (publications.length < 1) {
             alert ("No providers selected");
         }else {
             var listPublications = { "data": publications };
        $.ajax({
        type: "POST",
        data:  listPublications  ,
        dataType: "text", //result data type
        //contentType : "application/x-www-form-urlencoded; charset=UTF-8" ,
        url: host + "pubman/publicationsGSchoolarByOrg",
        success: function(Result) {
            console.log (Result);

        },
        error: function(data ) {
            //document.getElementById("imgloading").style.visibility = "hidden";
            alert("Error" + data.responseText);
         } });
             
         } 
    }
    
    
    function unifiedProviders () {
         var publications = [];
         $('tbody tr input:checked').each(function (index) { 
             console.log ($(this).val())
             publications.push($(this).val());
         })
         
         console.log (publications);
         
           if (publications.length < 1) {
             alert ("No providers selected");
         }else {
             var listPublications = { "data": publications };
        $.ajax({
        type: "POST",
        data:  listPublications  ,
        dataType: "text", //result data type
        //contentType : "application/x-www-form-urlencoded; charset=UTF-8" ,
        url: host + "pubman/publicationsUnified",
        success: function(Result) {
          // table.ajax.reload( null, false );
            var resultados = JSON.parse(Result);
            console.log(Result);
            for (var key in resultados) {
              console.log (key+""+resultados[key]);

 
            }
        },
        error: function(data ) {
            //document.getElementById("imgloading").style.visibility = "hidden";
            alert("Error" + data.responseText);
         } });
             
         }
        
        
        
    }