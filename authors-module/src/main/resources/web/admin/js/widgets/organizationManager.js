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
 */

var table;
var dataOrg = [];


function organizationRegister() {
    var host = _SERVER_URL;
    var acro = $("#org_acro").val();
    var namEn = $("#org_name_en").val();
    var namEs = $("#org_name_es").val();
    var scopusid = $("#org_scopus_id").val();
    var alia = $("#org_name_alias").val(); 
    var coun = $("#org_country").val();
    var prov = $("#org_prov").val();
    var city = $("#org_city").val();
    var lan = $("#org_location_lan").val();
    var long = $("#org_location_long").val();
    var type = $("#org_type").val();
    var link = $("#org_link").val();
    var description = $("#org_description").val();

    // alert (name);


    $.ajax({
        type: "POST",
        dataType: "text", //result data type
        url: host + "authors-module/orgRegister?acro=" + acro + "&namEn=" + namEn + "&namEs=" + namEs + "&alias="+alia+ "&scopusId="+ scopusid +"&coun=" + coun +
                "&prov=" + prov + "&city=" + city + "&lan=" + lan + "&long=" + long + "&type=" + type +"&link=" + link +"&description=" + description,
        success: function(Result) {
            //document.getElementById("imgloading").style.visibility = "hidden";
           // alert("Correcto: " + Result);
           table.ajax.reload( null, false );
             NewOrg() ;
             
             if ( Result.indexOf ("Fail") !==-1){
                 alert (Result);
             }
        },
        error: function(data) {
            //document.getElementById("imgloading").style.visibility = "hidden";
            alert("Error" + data.responseText);
        }
    });





}


function loadTables() {
    var host = _SERVER_URL;
 table =   $('#orgTable').DataTable({
        ajax: host + "authors-module/organization/list",
        "columns": [
            {"data": "name"},
            {"data": "fullNameEs"},
            {"data": "type"},
            {"data": "location"},
            {"data": "coordinates"},
            {"data": "operations"}
        ], columnDefs: [
            {
                "render": function(data, type, row) {
                    return row ["country"] + "/" + row["province"] + "/" + row["city"];
                },
                targets: 3
            },
            {
                "render": function(data, type, row) {
                    return row ["lang"] + ";" + row["long"];
                },
                targets: 4
            },
            {"render": function(data, type, row, meta) {
                    var edit = '<td><input style="margin-right: 5px;" type="button" id="' + meta.row + '" value="Edit" onclick=edit("' + row["URI"] + '")></td>';
                    var space = '<td width="10px"> </td>'
                    var remove = '<td><input type="button" id="' + meta.row + '" value="X" onclick=remove("' + row["URI"] + '")></td>';
                    return  edit + remove;
                    // return '<input type="button" id="button_org" value="Register" onclick=console.log ("asdasd")>'; 
                },
                targets: 5
            }
        ]
    });


}

function remove(URI) {
var host = _SERVER_URL;

 $.ajax({
        type: "POST",
        dataType: "text", //result data type
        url: host + "authors-module/organization/removeOrg?uri=" + URI,
        success: function(Result) {
           table.ajax.reload( null, false );
            console.log(Result);
            
            if ("Success" == Result) {
                alert ("Data erase");
            } else {
                alert (Result);
            }

        },
        error: function(data) {
            //document.getElementById("imgloading").style.visibility = "hidden";
            alert("Error" + data.responseText);
        }
    });


}


function edit(URI) {
    var host = _SERVER_URL;
    $.ajax({
        type: "POST",
        dataType: "text", //result data type
        url: host + "authors-module/organization/loadOrg?uri=" + URI,
        success: function(Result) {
            //document.getElementById("imgloading").style.visibility = "hidden";
            console.log(Result);
            /*  console.log (Result.data);
             console.log (Result.data[0]);
             console.log (Result.data[0].name);*/
            var obj = JSON.parse(Result);
            $("input#org_acro").val(obj.data[0].name);
            $("input#org_name_en").val(obj.data[0].fullNameEn);
            $("input#org_name_es").val(obj.data[0].fullNameEs);
            $("#org_name_alias").val(obj.data[0].alias);
            $("#org_scopus_id").val(obj.data[0].scopusId);
            $("input#org_country").val(obj.data[0].country);
            $("input#org_prov").val(obj.data[0].province);
            $("input#org_city").val(obj.data[0].city);
            $("input#org_location_lan").val(obj.data[0].lang);
            $("input#org_location_long").val(obj.data[0].long);
            $("input#org_type").val(obj.data[0].type);
            $("input#org_link").val(obj.data[0].link);
            $("input#org_description").val(obj.data[0].description);
            
              $("#button_org_edit").css("display","block");
              $("#button_org_reg").css("display","none");
              $("#org_acro").prop('disabled', false);

          //  alert("Correcto: " + Result);
        },
        error: function(data) {
            //document.getElementById("imgloading").style.visibility = "hidden";
            alert("Error" + data.responseText);
        }
    });

}


function editOrg() {

    var host = _SERVER_URL;
    var acro = $("#org_acro").val();
    var namEn = $("#org_name_en").val();
    var namEs = $("#org_name_es").val();
    var alia = $("#org_name_alias").val();
    var scopusid = $("#org_scopus_id").val();
    var coun = $("#org_country").val();
    var prov = $("#org_prov").val();
    var city = $("#org_city").val();
    var lan = $("#org_location_lan").val();
    var long = $("#org_location_long").val();
    var type = $("#org_type").val();
    var link = $("#org_link").val();
    var description = $("#org_description").val();


  
  
      $.ajax({
        type: "POST",
        dataType: "text", //result data type
        url: host + "authors-module/orgEdit?acro=" + acro + "&namEn=" + namEn + "&namEs=" + namEs +  "&alias="+alia+"&scopusId="+ scopusid +"&coun=" + coun +
                "&prov=" + prov + "&city=" + city + "&lan=" + lan + "&long=" + long + "&type=" + type + "&link=" + link+"&description=" + description,
        success: function(Result) {
            //document.getElementById("imgloading").style.visibility = "hidden";
            table.ajax.reload( null, false );
             NewOrg() ;
            alert("Edicion Correcta: " + Result);
        },
        error: function(data) {
            //document.getElementById("imgloading").style.visibility = "hidden";
            alert("Error" + data.responseText);
        }
    });

}

function NewOrg() {
    var acro = $("#org_acro").val("");
    var namEn = $("#org_name_en").val("");
    var namEs = $("#org_name_es").val("");
    var scopusid = $("#org_scopus_id").val(""); 
    var alia = $("#org_name_alias").val(""); 
    var coun = $("#org_country").val("");
    var prov = $("#org_prov").val("");
    var city = $("#org_city").val("");
    var lan = $("#org_location_lan").val("");
    var long = $("#org_location_long").val("");
    var type = $("#org_type").val("");
    var type = $("#org_link").val("");
    var type = $("#org_description").val("");
    
    
    $("#button_org_edit").css("display","none");
    $("#button_org_reg").css("display","block");
    $("#org_acro").prop('disabled', false);

}