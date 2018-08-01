/* 
 Licensed to the Apache Software Foundation (ASF) under one
 or more contributor license agreements. See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership. The ASF licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */


var host = _SERVER_URL;
var table;
function loadTable() {
    var columnDefs = [];
    var fields = ["context", "sparql"];

    // All colums to show
    var columns = [
        {"data": "Selection"},
        {"data": "name"}
    ];

    // Render column 1 & 2
    var colum1 = {
        "render": function (data, type, row) {
            return   '<input id="' + row["name"] + '" type="checkbox" name="selection" value="' + row["id"] + '">';
        },
        targets: 0
    };

    var colum2 = {
        "render": function (data, type, row) {
            return   '<a href="' + row['url'] + '" target="_blank">' + data + '</a>';
        },
        targets: 1
    };

    var penultimate = {
        "render": function (data, type, row) {
            return "WORKS";
            var offset = parseInt(row['offset']);
            var status = row['status'];
            if (offset !== -1) {
                return "Offset:" + offset +
                        status === "Not started." ? "" : "<br> Last Update:<br>" + status;
            } else {
                return status === "Not started." ? status : "Completed!<br>" + status;
            }
        },
        targets: 2 + columns.length
    };

    var lastCol = {
        "render": function (data, type, row) {
            return '<input id="btn_delete_central" '
                    + 'type="button" value="delete"  '
                    + 'onclick="delete_endpoint(\'' + row["id"] + '\')"> ';
        },
        targets: 2 + columns.length + 1
    };

    columnDefs.push(colum1);
    columnDefs.push(colum2);
    columnDefs.push(penultimate);
    columnDefs.push(lastCol);

    // Render the rest of fields
    $.each(fields, function (i, val) {
        var newField = {"data": val};
        columns.push(newField);

        var columdefn = {
            "render": function (data, type, row) {
                return data;
            },
            targets: i + 2
        };

        columnDefs.push(columdefn);
    });
    columns.push({"data": "Delete"});

    // Draw table.
    table = $('#central-table').DataTable({
        ajax: host + "pubman/central/list",
        columns: columns,
        columnDefs: columnDefs
    });
}

function centralize() {
    var redi = [];
    var update = document.getElementById('checkCentralization').checked;

    $('tbody tr input:checked').each(function (index) {
        redi.push($(this).val());
    });

    if (redi.length < 1) {
        alert("No publications selected");
    } else {
        var redi_data = {"data": redi};
        $.ajax({
            type: "POST",
            data: redi_data,
            dataType: "text",
            url: host + "pubman/central/centralize?update=" + update,
            success: function (result) {
                console.log(result);
            },
            error: function (error) {
                alert(error.responseText);
            }});
    }
}

function delete_endpoint(id) {
    if (confirm('Are you sure you want to delete this REDI?')) {
        $.ajax({
            type: "POST",
            data: {data: id},
            contentType: "application/json",
            url: host + "pubman/central/delete",
            success: function (result) {
                table.ajax.reload(null, false);
                alert(result);
            },
            error: function (error) {
                alert(error.responseText);
            }
        });
    }
}

function store_endpoint() {
    var name = $("#central_name").val();
    var url = $("#central_url").val();
    var data = {};

    if (name.length > 0 && url.length > 0) {
        data.name = name;
        data.url = url;
        // Store data
        $.ajax({
            type: "POST",
            data: data,
            contentType: "application/json",
            url: host + "pubman/central/store",
            success: function (result) {
                table.ajax.reload(null, false);
                alert(result);
            },
            error: function (error) {
                alert(error.responseText);
            }
        });
    } else
        alert("Please fill in all the fields");

    // Clear fields
    $("input#central_name").val("");
    $("input#central_url").val("");
}