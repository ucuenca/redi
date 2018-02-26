//var host = "http://localhost:8080
 //var host = "https://rediclon.cedia.edu.ec/";
var host = _SERVER_URL;
function loadTables() {
 var Providers = ["SCOPUS","AcademicsKnowledge", "DBLP", "GoogleScholar","Springer","SCIELO"];
 
  var columns = [ {"data":"Selection"} , { "data":"label"}];
  
  var colum1 =  {  "render": function (data, type, row) {
                      return   '<input id="' + row["name"] + '" type="checkbox" name="selection" value="' + row["org"] + '">';},
                     targets: 0};
                     
                     
  var colum2 =   { "render": function (data, type, row) {  return  data; },
                        targets: 1 }
  var columnDefs  = [];
      columnDefs.push(colum1);
      columnDefs.push(colum2);
      
      
       $.each( Providers, function( i, val ) {
  
         var newField = { "data": val};
         columns.push(newField);
         
         var columdefn = { "render": function(data, type, row) {
                    if (typeof (row[val]) === "undefined") {
                        return "";
                    } else {
                        var color ="color:gray";
                        var title = "";
                            if (row[val+"l"] !== undefined){
                                var color = "color:green"
                                 title = "| Disambiguate";
                            }

                        if (data / row["total"] > 0.9) {
                            
                                                       
                            return '<span title="Complete Data'+title+'" style="font-family: wingdings; font-size: 200%;'+color+' ">&#10004;</span>';
  
                        } else {

                            return  '<span title="Incomplete Data" style="font-family: wingdings; font-size: 200%;'+color+'">&#9684;</span>';
                        }
                    }
                },
                targets: i+2
            };
            
            columnDefs.push(columdefn);
      });
      
        var log = { "render": function(data, type, row) {
                
                    var log = "";
                   Providers.forEach( function(valor, indice, array) {
                  
                       if (row[valor+"l"] !== undefined) {
                         
                          log = row[valor+"l"].split(";").sort (function (a,b){ return CompareDate( a, b );})[0];
                       }
                   });
                   return log;
                },
                targets: columnDefs.length };
        columnDefs.push(log);    
            
       columns.push({ "data":"Logs"});
  
    console.log("Graficando");
    $('#provTableDis').DataTable({
        ajax: host + "pubman/publication/organization/disambiguationList",
        columns: columns ,
        columnDefs: columnDefs
              /*  [
            {
                "render": function(data, type, row) {

                    return   '<input id="' + row["name"] + '" type="checkbox" name="selection" value="' + row["org"] + '">';

                },
                targets: 0
            },
            {
                "render": function(data, type, row) {

                    return  data;
                },
                targets: 1
            },
            {
                "render": function(data, type, row) {
                
                    if (typeof (row["AcademicsKnowledge"]) === "undefined") {
                        return "";
                    } else {
                        var color ="color:gray";
                        var title = "";
                            if (row["AcademicsKnowledgel"] !== undefined){
                                var color = "color:green"
                                 title = "| Disambiguate";
                            }

                        if (data / row["total"] > 0.9) {
                            
                                                       
                            return '<span title="Complete Data'+title+'" style="font-family: wingdings; font-size: 200%;'+color+' ">&#10004;</span>';
                            // return row["DateAk"].split(";").sort(function(a,b){ return CompareDate( a, b ); }) +" | " +row["AdvAK"].split(";")[0];    
                        } else {

                            return  '<span title="Incomplete Data" style="font-family: wingdings; font-size: 200%;'+color+'">&#9684;</span>';
                        }
                    }
                },
                targets: 2
            },
            {
                "render": function(data, type, row) {

                    if (typeof (row["DBLP"]) === "undefined")
                    {
                        return "";
                    }
                    else {
                          var color ="color:gray";
                          var title = "";
                            if (row["DBLPl"] !== undefined){
                                var color = "color:green"
                                 title = "| Disambiguate";
                            } 
                            
                        if (data / row["total"] > 0.9) {
                          
                            return '<span title="Complete Data'+title+'" style="font-family: wingdings; font-size: 200%;'+color+'">&#10004;</span>';
                        } else {
                            return  '<span title="Incomplete Data" style="font-family: wingdings; font-size: 200%;'+color+'">&#9684;</span>';
                        }
                    }
                },
                targets: 3
            },
            {
                "render": function(data, type, row) {


                    if (typeof (row["SCOPUS"]) === "undefined")
                    {
                        return "";
                    }
                    else {
                        var color ="color:gray";
                         var title = "";
                            if (row["SCOPUSl"] !== undefined){
                                var color = "color:green"
                                 title = "| Disambiguate";
                            }
                        if (data / row["total"] > 0.9) {
                            return '<span title="Complete Data'+title+'" style="font-family: wingdings; font-size: 200%;'+color+'">&#10004;</span>';
                        } else {
                            return  '<span title="Incomplete Data" style="font-family: wingdings; font-size: 200%;'+color+'">&#9684;</span>';
                        }
                    }


                },
                targets: 4
            },
            {
                "render": function(data, type, row) {
                           
                    if (typeof (row["GS"]) === "undefined")
                    {   
                        return "";
                    }
                    else {
                        var color ="color:gray";
                        var title = "";
                            if (row["GSL"] !== undefined){
                                var color = "color:green";
                                 title = "| Disambiguate";
                            }
                        if (data / row["total"] > 0.9) {
                            return '<span title="Complete Data'+title+'" style="font-family: wingdings; font-size: 200%;'+color+'">&#10004;</span>';
                        } else {
                            return  '<span title="Incomplete Data" style="font-family: wingdings; font-size: 200%;'+color+'">&#9684;</span>';
                        }
                    }


                },
                targets: 5
            } ,{
                "render": function(data, type, row) {
                            
                    if (typeof (row["GS"]) === "undefined")
                    {   
                        return "";
                    }
                    else {
                        var color ="color:gray";
                         var title = "";
                            if (row["SCIELOl"] !== undefined){
                                var color = "color:green";
                                 title = "| Disambiguate";
                            }
                        if (data / row["total"] > 0.9) {
                            return '<span title="Complete Data'+title+'" style="font-family: wingdings; font-size: 200%;'+color+'">&#10004;</span>';
                        } else {
                            return  '<span title="Incomplete Data" style="font-family: wingdings; font-size: 200%;'+color+'">&#9684;</span>';
                        }
                    }


                },
                targets: 6
            } ,
            {
                "render": function(data, type, row) {
                    var prov = ["AcademicsKnowledgel","DBLPl","SCOPUSl","SCIELOl"];
                    var log = "";
                   prov.forEach( function(valor, indice, array) {
                     
                       if (row[valor] !== undefined) {
                         
                          log = row[valor].split(";").sort (function (a,b){ return CompareDate( a, b );})[0];
                       }
                   });
              //     console.log (log);
                   return log;
                },
                targets: 7
            }
        ]*/

    });
}


function  CompareDate( a, b ) {
               
            var datea = a.split(" | ")[0];
            var dateb = b.split(" | ")[0];
            var auxa = datea.split(" ")[0].split("/") ;
            var auxb = dateb.split(" ")[0].split("/") ;
          
        return new Date ( auxb[1]+"/"+auxb[0]+"/"+auxb[2]+" "+dateb.split(" ")[1]) - new Date (auxa[1]+"/"+auxa[0]+"/"+auxa[2]+" "+datea.split(" ")[1]) ;
   //  return new Date(aux1[0].split("/")[1]+"/"+aux1[0].split("/")[0]+"/"+aux1[0].split("/")[2]+" "+ aux1[1]) - new Date(aux2[0].split("/")[1]+"/"+aux2[0].split("/")[0]+"/"+aux2[0].split("/")[2]+" "+aux2[1]);
  
        
    }

  function DisambiguationProcess ()  {
         var publications = [];
         $('tbody tr input:checked').each(function (index) { 
           
             publications.push($(this).val());
         })
         
        
         
           if (publications.length < 1) {
             alert ("No providers selected");
         }else {
             var listPublications = { "data": publications };
        $.ajax({
        type: "POST",
        data:  listPublications  ,
        dataType: "text", 
        url: host + "pubman/runDisambiguation",
         success: function(Result) {
             console.log (Result);

         },
         error: function(data ) {
           
             alert("Error" + data.responseText);
         } });
             
         } 
    }