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
// var host = "https://rediclon.cedia.edu.ec/";
function loadTables() {

    console.log("Graficando");
    $('#provTable').DataTable({
        ajax: host + "pubman/publication/organization/list",
        columns: [
            {"data": "Selection"},
            {"data": "name"},
            {"data": "AdvAcademicsKnowledge"},
            {"data": "AdvDBLP"},
            {"data": "AdvSCOPUS"},
            {"data": "AdvGS"},
            {"data": "AdvSCIELO"},
            {"data": "AdvSpringer"}
        ],
        columnDefs: [
            {
                "render": function (data, type, row) {

                    return   '<input id="' + row["name"] + '" type="checkbox" name="selection" value="' + row["uri"] + '">';

                },
                targets: 0
            },
            {
                "render": function (data, type, row) {

                    return  data;
                },
                targets: 1

            },
            {
                "render": function (data, type, row) {

                    if (typeof (row["AdvAcademicsKnowledge"]) === "undefined") {
                        return "";
                    } else {
                        return row["AdvAcademicsKnowledge"].split(";").sort(function (a, b) {
                            return CompareDate(a, b);
                        })[0];
                        // return row["DateAk"].split(";").sort(function(a,b){ return CompareDate( a, b ); }) +" | " +row["AdvAK"].split(";")[0];    

                    }

                },
                targets: 2
            },
            {
                "render": function (data, type, row) {

                    if (typeof (row["AdvDBLP"]) === "undefined")
                    {
                        return "";
                    } else {
                        return row["AdvDBLP"].split(";").sort(function (a, b) {
                            return CompareDate(a, b);
                        })[0];
                        // return  row["DateDBLP"].split(";").sort(function(a,b){ return CompareDate( a, b ); })+" | "+row["AdvDBLP"].split(";")[0];
                    }
                },
                targets: 3

            },
            {
                "render": function (data, type, row) {


                    if (typeof (row["AdvSCOPUS"]) === "undefined")

                    {
                        return "";
                    } else {
                        return row["AdvSCOPUS"].split(";").sort(function (a, b) {
                            return CompareDate(a, b);
                        })[0];
                        // return  row["DateScopus"].split(";").sort(function(a,b){ return CompareDate( a, b ); })+" | "+row["AdvScopus"].split(";")[0];      
                    }
                },
                targets: 4
            },
            {
                "render": function (data, type, row) {

                    if (typeof (row["AdvGS"]) === "undefined")

                    {
                        return "";
                    } else {
                        return row["AdvGS"].split(";").sort(function (a, b) {
                            return CompareDate(a, b);
                        })[0];
                        //   return  row["DateGs"].split(";").sort(function(a,b){ return CompareDate( a, b ); })+" | "+row["AdvGs"].split(";")[0];  
                    }



                },
                targets: 5
            },
            {
                "render": function (data, type, row) {

                    if (typeof (row["AdvSCIELO"]) === "undefined")
                    {
                        return "";
                    } else {
                        return row["AdvSCIELO"].split(";").sort(function (a, b) {
                            return CompareDate(a, b);
                        })[0];
                    }
                },
                targets: 6
            },
            {
                "render": function (data, type, row) {

                    if (typeof (row["AdvSpringer"]) === "undefined")
                    {
                        return "";
                    } else {
                        return row["AdvSpringer"].split(";").sort(function (a, b) {
                            return CompareDate(a, b);
                        })[0];
                    }
                },
                targets: 7
            }
        ]

    });
}

function  CompareDate(a, b) {

    var datea = a.split(" | ")[0];
    var dateb = b.split(" | ")[0];
    var auxa = datea.split(" ")[0].split("/");
    var auxb = dateb.split(" ")[0].split("/");

    return new Date(auxb[1] + "/" + auxb[0] + "/" + auxb[2] + " " + dateb.split(" ")[1]) - new Date(auxa[1] + "/" + auxa[0] + "/" + auxa[2] + " " + datea.split(" ")[1]);
    //  return new Date(aux1[0].split("/")[1]+"/"+aux1[0].split("/")[0]+"/"+aux1[0].split("/")[2]+" "+ aux1[1]) - new Date(aux2[0].split("/")[1]+"/"+aux2[0].split("/")[0]+"/"+aux2[0].split("/")[2]+" "+aux2[1]);


}

function ExtractAk() {
    var publications = [];
    $('tbody tr input:checked').each(function (index) {
        console.log($(this).val())
        publications.push($(this).val());
    })

    console.log(publications);

    if (publications.length < 1) {
        alert("No publications selected");
    } else {
        var listPublications = {"data": publications};
        $.ajax({
            type: "POST",
            data: listPublications,
            dataType: "text", //result data type
            //contentType : "application/x-www-form-urlencoded; charset=UTF-8" ,
            url: host + "pubman/publicationsAkByOrg",
            success: function (Result) {
                // table.ajax.reload( null, false );
                var resultados = JSON.parse(Result);
                console.log(Result);
                for (var key in resultados) {
                    console.log(key + "" + resultados[key]);
                    //$('span#'+key+'').text(resultados[key]);
                    // $("span[id='"+key+"']").text(resultados[key]);

                }
                // alert (Result);

            },
            error: function (data) {
                //document.getElementById("imgloading").style.visibility = "hidden";
                alert("Error" + data.responseText);
            }});

    }


}


function ExtractScopus() {
    var publications = [];
    $('tbody tr input:checked').each(function (index) {
        console.log($(this).val())
        publications.push($(this).val());
    })

    console.log(publications);

    if (publications.length < 1) {
        alert("No providers selected");
    } else {
        var listPublications = {"data": publications};
        $.ajax({
            type: "POST",
            data: listPublications,
            dataType: "text", //result data type
            //contentType : "application/x-www-form-urlencoded; charset=UTF-8" ,
            url: host + "pubman/publicationsScopusByOrg",
            success: function (Result) {
                console.log(Result);
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
            error: function (data) {
                //document.getElementById("imgloading").style.visibility = "hidden";
                alert("Error" + data.responseText);
            }});

    }


}


function ExtractScielo() {
    var publications = [];
    $('tbody tr input:checked').each(function (index) {
        console.log($(this).val())
        publications.push($(this).val());
    })

    console.log(publications);

    if (publications.length < 1) {
        alert("No providers selected");
    } else {
        var listPublications = {"data": publications};
        $.ajax({
            type: "POST",
            data: listPublications,
            dataType: "text", //result data type
            //contentType : "application/x-www-form-urlencoded; charset=UTF-8" ,
            url: host + "pubman/publicationsScieloByOrg",
            success: function (Result) {
                console.log(Result);

            },
            error: function (data) {
                //document.getElementById("imgloading").style.visibility = "hidden";
                alert("Error" + data.responseText);
            }});

    }
}


function ExtractDBLP() {
    var publications = [];
    $('tbody tr input:checked').each(function (index) {
        console.log($(this).val())
        publications.push($(this).val());
    })

    console.log(publications);

    if (publications.length < 1) {
        alert("No providers selected");
    } else {
        var listPublications = {"data": publications};
        $.ajax({
            type: "POST",
            data: listPublications,
            dataType: "text", //result data type
            //contentType : "application/x-www-form-urlencoded; charset=UTF-8" ,
            url: host + "pubman/publicationsDBLPByOrg",
            success: function (Result) {
                console.log(Result);

            },
            error: function (data) {
                //document.getElementById("imgloading").style.visibility = "hidden";
                alert("Error" + data.responseText);
            }});

    }
}


function ExtractGoogleSchoolar() {
    var publications = [];
    $('tbody tr input:checked').each(function (index) {
        console.log($(this).val())
        publications.push($(this).val());
    })

    console.log(publications);

    if (publications.length < 1) {
        alert("No providers selected");
    } else {
        var listPublications = {"data": publications};
        $.ajax({
            type: "POST",
            data: listPublications,
            dataType: "text", //result data type
            //contentType : "application/x-www-form-urlencoded; charset=UTF-8" ,
            url: host + "pubman/publicationsGSchoolarByOrg",
            success: function (Result) {
                console.log(Result);

            },
            error: function (data) {
                //document.getElementById("imgloading").style.visibility = "hidden";
                alert("Error" + data.responseText);
            }});

    }
}

function ExtractSpringer() {
    var publications = [];
    $('tbody tr input:checked').each(function (index) {
        console.log($(this).val())
        publications.push($(this).val());
    })

    console.log(publications);

    if (publications.length < 1) {
        alert("No providers selected");
    } else {
        var listPublications = {"data": publications};
        $.ajax({
            type: "POST",
            data: listPublications,
            dataType: "text", //result data type
            //contentType : "application/x-www-form-urlencoded; charset=UTF-8" ,
            url: host + "pubman/publicationsSpringerByOrg",
            success: function (Result) {
                console.log(Result);
            },
            error: function (data) {
                //document.getElementById("imgloading").style.visibility = "hidden";
                alert("Error" + data.responseText);
            }});
    }
}


function unifiedProviders() {
    var publications = [];
    $('tbody tr input:checked').each(function (index) {
        console.log($(this).val())
        publications.push($(this).val());
    })

    console.log(publications);

    if (publications.length < 1) {
        alert("No providers selected");
    } else {
        var listPublications = {"data": publications};
        $.ajax({
            type: "POST",
            data: listPublications,
            dataType: "text", //result data type
            //contentType : "application/x-www-form-urlencoded; charset=UTF-8" ,
            url: host + "pubman/publicationsUnified",
            success: function (Result) {
                // table.ajax.reload( null, false );
                var resultados = JSON.parse(Result);
                console.log(Result);
                for (var key in resultados) {
                    console.log(key + "" + resultados[key]);


                }
            },
            error: function (data) {
                //document.getElementById("imgloading").style.visibility = "hidden";
                alert("Error" + data.responseText);
            }});

    }



}