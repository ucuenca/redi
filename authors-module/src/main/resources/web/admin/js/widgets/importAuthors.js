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
function Importer(id, host) {
    var loader = $("<img style='position: relative;top: 4px;margin-left: 10px;' src='../admin/img/ajax-loader_small.gif'>");

    var file = $("<input type='file'>");
    var endpoints = $("<select/>");

    var container = $("#" + id);
    var content = $("<table/>");
    var button = $("<button/>").text("Import!");

    function init() {
        container.empty();
        content.empty();
        endpoints.empty();
        //container.append(style);
        container.append($("<h2></h2>").append("<span>Import</span>").append(loader));
        container.append(content);
        container.append(button);

        loader.hide();

        var tr1 = $("<tr/>");
        tr1.append($("<td>Select file</td>"));
        var td1 = $("<td/>").append(file);
        tr1.append(td1);
        content.append(tr1);

        var tr2 = $("<tr/>");
        tr2.append($("<td>Select Endpoint</td>"));
        var td2 = $("<td/>").append(endpoints);
        tr2.append(td2);
        content.append(tr2);

        $.getJSON(host + "authors-module/endpoint/list", function (data) {
            for (var e in data) {
                $("<option/>", {
                    value: data[e].id,
                    text: data[e].name}).appendTo(endpoints);
            }
        });

        button.click(function () {
            upload();
        });

    }

    function upload() {
        if (file.val() === '') {
            alert('Select a file');
            return;
        }
        if (endpoints.val() === '') {
            alert('Select an endpoint.');
            return;
        }

        var data = file.get(0).files[0];
        var org = endpoints.find('option:selected').text();
        loader.show();

        requestUploadFile(host + "authors-module/upload?org=" + org, data, data.type,
                {
                    200: function () {
                        loader.hide();
                        file.val('');
                        alert("Authors imported sucessfully.");
                    },
                    400: function (error) {
                        loader.hide();
                        file.val('');
                        alert("Error " + error + "\nClient Error, check your file format.");
                    },
                    500: function (error) {
                        loader.hide();
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
    init();
}

