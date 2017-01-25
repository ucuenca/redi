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
/**
 * Created by . User: Thomas Kurz Date: 18.02.11 Time: 18:46 To change this
 * template use File | Settings | File Templates.
 */
(function ($) {

    var contentBox;

    var endpoints;
    var buttonsetEndpoint;
    var buttonsetDomain;

    // new endpoint
    var acronym;
    var url;
    var graph;
    var name_es;
    var name_en;
    var city;
    var province;
    var lat;
    var long;

    // new domain
    var acronyms;
    var domain;

    var loaderEndpoint;
    var loaderDomain;

    $.fn.endpointsManager = function (options) {
        var settings = {
            host: 'http://localhost:8080/LMF/'
        };

        function addEndpoint() {
            if (acronym.val() === '') {
                alert("acronym may not be empty!");
                return;
            }
            if (url.val() === '') {
                alert("endpoint url may not be empty!");
                return;
            }
            if (graph.val() === '') {
                alert("graph uri may not be empty!");
                return;
            }
            if (name_es.val() === '') {
                alert("spanish name may not be empty!");
                return;
            }
            if (name_en.val() === '') {
                alert("english name may not be empty!");
                return;
            }
            if (city.val() === '') {
                alert("city may not be empty!");
                return;
            }
            if (province.val() === '') {
                alert("province may not be empty!");
                return;
            }
            if (lat.val() === '') {
                alert("latitude may not be empty!");
                return;
            }
            if (long.val() === '') {
                alert("longitude may not be empty!");
                return;
            }
            var dataNewEndpoint = {
                "State": "true",
                "Name": acronym.val(),
                "Endpoint": url.val(),
                "GraphUri": graph.val(),
                "FullName": name_es.val(),
                "NameEnglish": name_en.val(),
                "City": city.val(),
                "Province": province.val(),
                "Latitude": lat.val(),
                "Longitude": long.val()
            };

            loaderEndpoint.html("<span>Saving...</span>");

            $.ajax({
                type: "POST",
                data: JSON.stringify(dataNewEndpoint),
                dataType: "text", //result data type
                contentType: "application/json", // send data type
                url: settings.host + "authors-module/addendpoint",
                success: function () {
                    loaderEndpoint.html("");
                    $.getJSON(settings.host + "authors-module/endpoint/list", function (data) {
                        writeEndpoints(data);
                        writeAcronyms(data);
                    });
                    alert("Success");
                },
                error: function (data) {
                    loaderEndpoint.html("");
                    alert("Error" + data.responseText);
                }
            });
        }

        function addDomain() {
            if (!acronyms.val()) {
                alert("select an acronym");
                return;
            }
            if (domain.val() === '') {
                alert("domain may not be empty");
                return;
            }
            var url = settings.host + "authors-module/domain?id=" + acronyms.val() + "&domain=" + domain.val();
            loaderDomain.html("<span>Saving...</span>");

            $.ajax({
                type: "POST",
                contentType: "text/plain",
                url: url,
                success: function () {
                    loaderDomain.html("");
                    alert("Sucess");
                },
                error: function (data) {
                    loaderDomain.html("");
                    alert("Error" + data.responseText);
                }
            });
        }

        function removeEndpoint(b) {
            var url = settings.host + "authors-module/endpoint/delete?id=" + b.attr('data-id');
            $.ajax({
                type: "DELETE",
                url: url,
                success: function () {
                    $.getJSON(settings.host + "authors-module/endpoint/list", function (data) {
                        writeEndpoints(data);
                        writeAcronyms(data);
                    });
                    alert("success");
                },
                error: function (jXHR, textStatus) {
                    alert("Error: " + jXHR.responseText);
                }
            });
        }

        function activateEndpoint(b, active) {
            var url = settings.host + "authors-module/endpoint/updatestatus?id=" + b.attr('data-id') + "&oldstatus=" + b.attr('data-oldstatus') + "&newstatus=" + b.attr('data-newstatus');
            $.ajax({
                type: "POST",
                url: url,
                success: function () {
                    $.getJSON(settings.host + "authors-module/endpoint/list", function (data) {
                        writeEndpoints(data);
                    });
                    alert("success");
                },
                error: function (jXHR, textStatus) {
                    alert("Error: " + jXHR.responseText);
                }
            });
        }

        function writeEndpoints(ps) {
            if (ps.length === 0) {
                endpoints.text("no endpoints defined");
                return;
            }
            var table = $("<table class='simple_table'/>");
            var tr = $("<tr class='title' valign='top' style='font-weight:bold;color: white;background-color:gray;'/>");
            tr.append($("<td/>").html("&nbsp;"));
            tr.append($("<td/>").text("Name"));
            tr.append($("<td/>").text("Endpoint URL"));
            tr.append($("<td/>").text("Full Name"));
            tr.append($("<td/>").text("City"));
            tr.append($("<td/>").text("Province"));
            tr.append($("<td/>").text("Actions"));
            table.append(tr);

            function buildIcon(enabled) {
                var icon = $("<span>", {'class': "endpoint_status"});
                if (enabled)
                    icon.addClass("on");
                return icon;
            }

            for (var i = 0; i < ps.length; i++) {
                if (ps[i]) {
                    var delBtn = $("<button/>").text("delete");
                    delBtn.attr('data-id', ps[i].id);
                    delBtn.click(function () {
                        removeEndpoint($(this));
                    });
                    var modBtn = $("<button>", {text: (ps[i].status === 'true' ? "deactivate" : "activate"), 'data-id': ps[i].id, 'data-oldstatus': (ps[i].status === 'false' ? "false" : "true"), 'data-newstatus': (ps[i].status === 'false' ? "true" : "false")})
                    modBtn.click(function () {
                        activateEndpoint($(this));
                    });
                }
                var col = "";
                if (i % 2) {
                    col = "even";
                } else {
                    col = "odd";
                }
                var tr = $("<tr class='" + col + "'/>");

                $("<td/>").append(buildIcon(ps[i].status === 'true')).appendTo(tr);
                $("<td/>").append($("<a/>", {target: '_blank', href: ps[i].id})
                        .text(ps[i].name)).appendTo(tr);
                $("<td/>").text(ps[i].url || '').appendTo(tr);
                $("<td/>").text(ps[i].fullName || '').appendTo(tr);
                $("<td/>").text(ps[i].city || '').appendTo(tr);
                $("<td/>").text(ps[i].province || '').appendTo(tr);

                $("<td/>").append(modBtn).append(delBtn).appendTo(tr);

                table.append(tr);
            }
            endpoints.empty().append(table);
        }

        function writeContent() {
            // set Buttons add endpoint
            var endpointButton = $("<button/>", {
                disabled: false
            }).text("Add Endpoint");

            endpointButton.click(function () {
                addEndpoint();
            });

            loaderEndpoint = $("<div/>");
            buttonsetEndpoint = $("<div/>");
            buttonsetEndpoint.append(endpointButton);
            buttonsetEndpoint.append(loaderEndpoint);

            // set Buttons for domains
            var domainButton = $("<button/>", {
                disabled: false
            }).text("Add Domain");
            domainButton.click(function () {
                addDomain();
            });

            loaderDomain = $("<div/>");
            buttonsetDomain = $("<div/>");
            buttonsetDomain.append(domainButton);
            buttonsetDomain.append(loaderDomain);

            // set table to receive attributes of a new endpoint/add domains
            var newendpoint = $("<table/>");
            var newDomain = $("<table/>");

            // load available endpoints
            endpoints = $("<div/>").text('loading');

            // Add elements to the contentBox
            contentBox.html("<h2 style='margin-bottom: 10px;'>Endpoints</h2>");
            contentBox.append(endpoints);
            contentBox.append("<h4 style='margin-bottom: 10px;'>Add Enpoint</h4>");
            contentBox.append(newendpoint);
            contentBox.append(buttonsetEndpoint);
            contentBox.append("<h4 style='margin-bottom: 10px;'>Add Domains</h4>");
            contentBox.append(newDomain);
            contentBox.append(buttonsetDomain);

            // add new endpoint stuff
            acronym = $("<input style='width: 100%;' type='text' size='100'>");
            url = $("<input style='width: 100%;' type='text' size='100'>");
            graph = $("<input style='width: 100%;' type='text' size='100'>");
            name_es = $("<input style='width: 100%;' type='text' size='100'>");
            name_en = $("<input style='width: 100%;' type='text' size='100'>");
            city = $("<input style='width: 100%;' type='text' size='100'>");
            province = $("<input style='width: 100%;' type='text' size='100'>");
            lat = $("<input style='width: 100%;' type='text' size='100'>");
            long = $("<input style='width: 100%;' type='text' size='100'>");

            var tr1 = $("<tr></tr>");
            tr1.append("<td>Acronym</td>");
            var td1 = $("<td></td>").append(acronym);
            tr1.append(td1);
            newendpoint.append(tr1);

            var tr2 = $("<tr></tr>");
            tr2.append("<td>Endpoint URL</td>");
            var td2 = $("<td></td>").append(url);
            tr2.append(td2);
            newendpoint.append(tr2);

            var tr3 = $("<tr></tr>");
            tr3.append("<td>Graph URI</td>");
            var td3 = $("<td></td>").append(graph);
            tr3.append(td3);
            newendpoint.append(tr3);

            var tr4 = $("<tr></tr>");
            tr4.append("<td>Spanish Name</td>");
            var td4 = $("<td></td>").append(name_es);
            tr4.append(td4);
            newendpoint.append(tr4);

            var tr5 = $("<tr></tr>");
            tr5.append("<td>English Name</td>");
            var td5 = $("<td></td>").append(name_en);
            tr5.append(td5);
            newendpoint.append(tr5);

            var tr6 = $("<tr></tr>");
            tr6.append("<td>City</td>");
            var td6 = $("<td></td>").append(city);
            tr6.append(td6);
            newendpoint.append(tr6);

            var tr7 = $("<tr></tr>");
            tr7.append("<td>Province</td>");
            var td7 = $("<td></td>").append(province);
            tr7.append(td7);
            newendpoint.append(tr7);

            var tr8 = $("<tr></tr>");
            tr8.append("<td>Latitude</td>");
            var td6 = $("<td></td>").append(lat);
            tr8.append(td6);
            newendpoint.append(tr8);

            var tr9 = $("<tr></tr>");
            tr9.append("<td>Longitude</td>");
            var td9 = $("<td></td>").append(long);
            tr9.append(td9);
            newendpoint.append(tr9);

            // add newdomain stuff
            acronyms = $("<select/>");
            domain = $("<input style='width: 50%;' type='text' size='100'>");

            var tr10 = $("<tr></tr>");
            tr10.append("<td>Acronyms</td>");
            var td10 = $("<td></td>").append(acronyms);
            tr10.append(td10);
            newDomain.append(tr10);

            var tr11 = $("<tr></tr>");
            tr11.append("<td>Domain</td>");
            var td11 = $("<td></td>").append(domain);
            tr11.append(td11);
            newDomain.append(tr11);

            // get list of endpoints
            $.getJSON(settings.host + "authors-module/endpoint/list", function (data) {
                // fill in endpoints table
                writeEndpoints(data);
                // add acronyms available
                writeAcronyms(data);
            });
        }

        function writeAcronyms(data) {
            acronyms.empty();
            for (var e in data) {
                $("<option/>", {
                    value: data[e].id,
                    text: data[e].name}).appendTo(acronyms);
            }
        }

        return this.each(function () {
            // merge options
            if (options) {
                $.extend(settings, options);
            }
            contentBox = $(this);

            // build skeleton
            writeContent();

        });
    };
})(jQuery);



function runGetAuthorsFromUTPL(host) {

    document.getElementById("imgloading").style.visibility = "visible";

    var endpoint = document.getElementById('txtendpointutpl').value;
    var graphuri = document.getElementById('txtgraphuriutpl').value;

    var settings = {
        host: host
    };


    $.ajax({
        type: "POST",
        dataType: "text", //result data type
        contentType: "application/json", // send data type
        url: settings.host + "authors-module/split?endpointuri=" + endpoint + "&graphuri=" + graphuri + "",
        //    url:  "http://localhost:8079/marmotta/authors-module/update",
        success: function (Result) {
            document.getElementById("imgloading").style.visibility = "hidden";
            alert("Correcto: " + Result);
        },
        error: function (data) {
            document.getElementById("imgloading").style.visibility = "hidden";
            alert("Error" + data.responseText);
        }
    });


}



