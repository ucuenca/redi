/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * 
 */

//var _SERVER_URL = "http://localhost:8080/";
var host = _SERVER_URL;
var table;


function getOrg() {

    $.ajax({
        type: "GET",
        dataType: "text", //result data type
        //   url: host + "authors-module/organization/list",
        url: host + "authors-module/organization/list",
        success: function (Result) {
            //document.getElementById("imgloading").style.visibility = "hidden";
            // alert("Correcto: " + Result);
            var objOrg = JSON.parse(Result);
            $.each(objOrg.data, function (i, d) {
                console.log(d.name);
                if (i === 0) {
                    $('#endpoint_org').append('<option selected="selected" value="' + d.name + '">' + d.name + '</option>');

                } else {
                    $('#endpoint_org').append('<option value="' + d.name + '">' + d.name + '</option>');
                }
            }
            );

        },
        error: function (data) {
            //document.getElementById("imgloading").style.visibility = "hidden";
            alert("Error" + data.responseText);
        }
    });
}

function selectEndpoint(e) {
    if ("sparql" == e)
    {
        $("tr.sparql_form").css("display", "block");
        $("tr.file_form").css("display", "none");
        $("tr.OAI_form").css("display", "none");
        $("tr.OJS_form").css("display", "none");
        $("tr.ORCID_form").css("display", "none");
        $("tr.CERIF_form").css("display", "none");
    } else if ("file" == e) {
        $("tr.sparql_form").css("display", "none");
        $("tr.file_form").css("display", "block");
        $("tr.OAI_form").css("display", "none");
        $("tr.OJS_form").css("display", "none");
        $("tr.ORCID_form").css("display", "none");
        $("tr.CERIF_form").css("display", "none");
    } else if ("oai-pmh" == e) {
        $("tr.sparql_form").css("display", "none");
        $("tr.file_form").css("display", "none");
        $("tr.OAI_form").css("display", "block");
        $("tr.OJS_form").css("display", "none");
        $("tr.ORCID_form").css("display", "none");
        $("tr.CERIF_form").css("display", "none");
    } else if ("ojs" == e) {
        $("tr.OJS_form").css("display", "block");
        $("tr.sparql_form").css("display", "none");
        $("tr.file_form").css("display", "none");
        $("tr.OAI_form").css("display", "none");
        $("tr.ORCID_form").css("display", "none");
        $("tr.CERIF_form").css("display", "none");    
  }  else if ("cerif" == e) {
        $("tr.CERIF_form").css("display", "block");
        $("tr.OJS_form").css("display", "none");
        $("tr.sparql_form").css("display", "none");
        $("tr.file_form").css("display", "none");
        $("tr.OAI_form").css("display", "none");
        $("tr.ORCID_form").css("display", "none");
    } 
     else {
        $("tr.OJS_form").css("display", "none");
        $("tr.sparql_form").css("display", "none");
        $("tr.file_form").css("display", "none");
        $("tr.OAI_form").css("display", "none");
        $("tr.CERIF_form").css("display", "none");
        $("tr.ORCID_form").css("display", "block");
    }


}

function cleanEndpoint() {

    $("input#endpoint_url").val("");
    $("input#endpoint_graph").val("");
    $("input#endpoint_file").val("");
    $("input#endpoint_oai").val("");
    $("input#endpoint_ojs").val("");
    $("input#endpoint_cerif").val("");
    $("input#endpoint_ad_ojs").val("");
    $('input#check_oai').prop('checked', true);
}

function newEndpoint() {
    var type = $("#endpoint_type").val();
    var org = $("#endpoint_org").val();

    if (type == "sparql") {
        var url = $("input#endpoint_url").val();
        var graph = $("input#endpoint_graph").val();
        uploadSparql(type, org, url, graph);
    } else if (type == "file") {
        var file = $("input#endpoint_file");
        upload(type, org, file);
    } else if (type == "oai-pmh") {
        var val1 = $("input#endpoint_oai").val();
        var check = $('input#check_oai').is(':checked');
        uploadOAI(type, org, val1, check);
    } else if (type == "ojs") {
        var val1 = $("input#endpoint_ojs").val();
        var val2 = $("input#endpoint_ad_ojs").val();
        if (val2.trim().length > 1) {
            val1 = val1.trim() + ";" + val2.trim();
        }
        uploadOAI(type, org, val1, true);
    }  else if (type == "cerif") {
        var val1 = $("input#endpoint_cerif").val();
        uploadOAI(type, org, val1, false);
    }  else if (type == "orcid") {
        uploadORCID(type, org);
    }

    //  console.log ("val1"+val1);
    //console.log ("val2"+val2); 
    // table.ajax.reload( null, false );
    cleanEndpoint();
}

function uploadORCID(type, org) {
    $.ajax({
        type: "POST",
        dataType: "text", //result data type
        url: host + "authors-module/endpointORCIDRegister?type=" + type + "&org=" + org,
        success: function (Result) {
            table.ajax.reload(null, false);
            alert(Result);
        },
        error: function (data) {
            //document.getElementById("imgloading").style.visibility = "hidden";
            alert("Error" + data.responseText);
        }
    });
}

function uploadSparql(type, org, url, graph) {

    $.ajax({
        type: "POST",
        dataType: "text", //result data type
        url: host + "authors-module/endpointSparqlRegister?type=" + type + "&org=" + org + "&url=" + url + "&graph=" + graph,
        success: function (Result) {
            table.ajax.reload(null, false);

            alert(Result);
        },
        error: function (data) {
            //document.getElementById("imgloading").style.visibility = "hidden";
            alert("Error" + data.responseText);
        }
    });
}

function uploadOAI(type, org, url, check) {

    $.ajax({
        type: "POST",
        dataType: "text", //result data type
        url: host + "authors-module/endpointOAIRegister?type=" + type + "&org=" + org + "&url=" + url + "&severe=" + check,
        success: function (Result) {
            table.ajax.reload(null, false);

            alert(Result);

        },
        error: function (data) {
            //document.getElementById("imgloading").style.visibility = "hidden";
            alert("Error" + data.responseText);
        }
    });

}

function upload(type, org, file) {

    if (file.val() === '') {
        alert('Select a file');
        return;
    }


    var data = file.get(0).files[0];
    // var org = endpoints.find('option:selected').text();
    // loader.show();

    requestUploadFile(host + "authors-module/upload?type=" + type + "&org=" + org, data, data.type,
            {
                200: function () {
                    //   loader.hide();
                    table.ajax.reload(null, false);
                    file.val('');
                    alert("Authors imported sucessfully.");
                },
                400: function (error) {
                    //    loader.hide();
                    file.val('');
                    alert("Error " + error + "\nClient Error, check your file format.");
                },
                500: function (error) {
                    //     loader.hide();
                    file.val('');
                    alert("Error " + error + "\nCannot process file.");
                }
            });

    function requestUploadFile(url, data, mime, callbacks) {
        function createRequest() {
            var request = null;
            if (window.XMLHttpRequest) {
                request = new XMLHttpRequest();
            } else if (window.ActiveXObject) {
                request = new ActiveXObject("Microsoft.XMLHTTP");
            } else {
                throw "request object can not be created"
            }
            return request;
        }
        var request = createRequest();
        request.onreadystatechange = function () {
            if (request.readyState == 4) {
                if (callbacks.hasOwnProperty(request.status)) {
                    callbacks[request.status](request.responseText, request);
                }
            }
        };
        request.open('POST', url, true);
        request.setRequestHeader("Content-Type", mime);
        request.send(data);
    }
}

function loadTables() {
    table = $('#endpoinTable').DataTable({
        ajax: host + "authors-module/endpoints/list",
        "columns": [
            {"data": "selection"},
            {"data": "status"},
            {"data": "url"},
            {"data": "org"},
            {"data": "type"},
            {"data": "operations"},
            {"data": "date"}
        ], columnDefs: [
            {
                "render": function (data, type, row) {
                    if (row["status"] === "Active") {
                        return  '<input type="checkbox" name="selection" value="' + row["URI"] + '">';
                    } else {
                        return  '<input type="checkbox" name="selection" disabled value="' + row["URI"] + '">';
                    }
                },
                targets: 0
            },
            {
                "render": function (data, type, row) {
                    if (data === "Active") {
                        return '<span id="endpoint_status" class="active_endpoint" onclick=changeStatus("' + row["URI"] + '","' + data + '")>  </span>'
                    } else {
                        return '<span id="endpoint_status" class="inactive_endpoint" onclick=changeStatus("' + row["URI"] + '","' + data + '")>  </span>'
                    }
                },
                targets: 1
            },
            {
                "render": function (data, type, row) {
                    if (row ["type"] === "sparql") {
                        return "URL:" + data + "| GRAPH:" + row["graph"];
                    } else {
                        return data;
                    }
                },
                targets: 2
            },
            {
                "render": function (data, type, row) {
                    if (typeof data != 'undefined') {
                        return data.substring(data.lastIndexOf("/") + 1);
                    } else
                    {
                        return "";
                    }
                },
                targets: 3
            },
            {"render": function (data, type, row, meta) {
                    var remove = '<td><input type="button" id="' + meta.row + '" value="X" onclick=remove("' + row["URI"] + '")></td>';
                    return remove;
                    // return '<input type="button" id="button_org" value="Register" onclick=console.log ("asdasd")>'; 
                },
                targets: 5
            },
            {"render": function (data, type, row, meta) {
                    return '<span id="' + row["URI"] + '" class="exportDate">' + data + '</span>';

                    // return '<input type="button" id="button_org" value="Register" onclick=console.log ("asdasd")>'; 
                },
                targets: 6
            }
        ]
    });

}

function remove(URI) {
    $.ajax({
        type: "POST",
        dataType: "text", //result data type
        url: host + "authors-module/endpoints/removeEnd?uri=" + URI,
        success: function (Result) {
            table.ajax.reload(null, false);
            //  console.log(Result);

            if ("Success" == Result) {
                alert("Data erase");
            } else {
                alert(Result);
            }

        },
        error: function (data) {
            //document.getElementById("imgloading").style.visibility = "hidden";
            alert("Error" + data.responseText);
        }
    });

}

function changeStatus(URI, status) {

    $.ajax({
        type: "POST",
        dataType: "text", //result data type
        url: host + "authors-module/endpoints/updateStatusEnd?uri=" + URI + "&status=" + status,
        success: function (Result) {
            table.ajax.reload(null, false);
            //  console.log(Result);

            alert(Result);

        },
        error: function (data) {
            //document.getElementById("imgloading").style.visibility = "hidden";
            alert("Error" + data.responseText);
        }
    });

}


function extractEndpoints() {
    $('input[type=checkbox]:checked').parents('tr').find('span.exportDate').text("Proccessing");

    var endpoints = [];
    $('tbody tr  input:checked[name="selection"]').each(function (index) {
        console.log($(this).val())
        endpoints.push($(this).val());
    })

    if (endpoints.length < 1) {
        alert("No endpoint selected");
    } else {
        var listEndpoints = {"data": endpoints};
        $.ajax({
            type: "POST",
            data: listEndpoints,
            dataType: "text", //result data type
            //contentType : "application/x-www-form-urlencoded; charset=UTF-8" ,
            url: host + "authors-module/endpoints/extractAuthors",
            success: function (Result) {
                // table.ajax.reload( null, false );
                var resultados = JSON.parse(Result);
                console.log(JSON.parse(Result));
                for (var key in resultados) {

                    //$('span#'+key+'').text(resultados[key]);
                    $("span[id='" + key + "']").text(resultados[key]);

                }
                // alert (Result);

            },
            error: function (data) {
                //document.getElementById("imgloading").style.visibility = "hidden";
                alert("Error" + data.responseText);
            }});

    }
}