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

function endsWith(str, suffix) {
    return str.indexOf(suffix, str.length - suffix.length) !== -1;
}

(function ($) {
    $.fn.redi_mongo = function (options) {
        var settings = {
            host: 'http://localhost:8080/LMF/',
            anker_name: 'mongo',
            title: 'Mongo Configuration',
            loading_img: '../public/img/loader/ajax-loader_small.gif',
            toplink: true
        }

        //jquery elements
        var table;

        //basically there are 2 functions
        //1. getValues from server and write it to inputFields
        //2. storeValues after testing Configuration
        var functions = {
            input_fields: undefined,
            getValues: function (callback) {
                var writeValues = function (values) {
                    table.html("");
                    this.input_fields = {};

                    //Mongo DB Host
                    var tr1 = $("<tr/>");
                    var td11 = $("<td/>").css({"font-weight": "bold"}).text("Host: ");
                    var td12 = $("<td/>");
                    this.input_fields.mongo_host = $("<input type='text' style='width:100%'/>");
                    this.input_fields.mongo_host.val(values["mongo.host"]);
                    td12.append(this.input_fields.mongo_host);
                    td11.appendTo(tr1);
                    td12.appendTo(tr1);
                    tr1.appendTo(table);

                    //Port
                    var tr2 = $("<tr/>");
                    var td21 = $("<td/>").css({"font-weight": "bold"}).text("Port: ");
                    var td22 = $("<td/>");
                    this.input_fields.mongo_port = $("<input type='text' pattern='\d*' style='width:100%'/>");
                    this.input_fields.mongo_port.val(values["mongo.port"]);
                    td22.append(this.input_fields.mongo_port);
                    td21.appendTo(tr2);
                    td22.appendTo(tr2);
                    tr2.appendTo(table);

                    if (callback)
                        callback();
                };
                var getDBHost = function (values) {
                    var cid = "mongo.host";
                    var url = settings.host + "config/data/" + cid;
                    $.getJSON(url, function (data) {
                        values[cid] = data[cid];
                        getDBPort(values);
                    });
                };
                var getDBPort = function (values) {
                    var cid = "mongo.port";
                    var url = settings.host + "config/data/" + cid;
                    $.getJSON(url, function (data) {
                        values[cid] = data[cid];
                        writeValues(values);
                    });
                };
                getDBHost({});
            },
            ping: function () {
                var that = this;
                if (input_fields.mongo_host.val().length < 0 || input_fields.mongo_port.val() < 0) {
                    alert("Missing values.");
                    return;
                }
                // TODO: If there's a db socket, save values.
                if (confirm("Configuration successfully! Save values?")) {
                    that.saveValues();
                }
            },
            saveValues: function () {
                function saveHost() {
                    $.ajax({
                        type: "POST",
                        contentType: "application/json",
                        url: settings.host + "config/data/mongo.host",
                        data: '["' + input_fields.mongo_host.val() + '"]',
                        success: function () {
                            savePort();
                        },
                        error: function (jqXHR, statusText, errorThrown) {
                            alert("Error: " + errorThrown + "(" + jqXHR.status + ")" + jqXHR.responseText);
                        }
                    });
                }

                function savePort() {
                    $.ajax({
                        type: "POST",
                        contentType: "application/json",
                        url: settings.host + "config/data/mongo.port",
                        data: '["' + input_fields.mongo_port.val() + '"]',
                        success: function () {
                            reinit();
                        },
                        error: function (jqXHR, statusText, errorThrown) {
                            alert("Error: " + errorThrown + "(" + jqXHR.status + ")" + jqXHR.responseText);
                        }
                    });
                }
                function reinit() {
                    $.ajax({
                        type: "POST",
                        url: settings.host + "mongo/reinit",
                        success: function () {
                            alert("Successfully reloaded!");
                        },
                        error: function (jqXHR, statusText, errorThrown) {
                            alert("Error: " + errorThrown + "(" + jqXHR.status + ")" + jqXHR.responseText);
                        }
                    });
                }
                saveHost();
            }
        };

        /**
         * main method
         */
        return this.each(function () {
            // merge options
            if (options) {
                $.extend(settings, options);
            }
            //build skeleton

            //get Values
            functions.getValues();
            var top = "";
            if (settings.toplink)
                top = '<a href="#" target="_top" style="position:absolute;right:5px;font-size:12px;top:7px;text-decoration:none;">top</a>';
            //build basic view elements
            var title = $("<h2 style='position:relative;margin-bottom:10px'><a style='text-decoration:none' name='" + settings.anker_name + "'>" + settings.title + "</a><span style='margin-left:10px;display:none;' class='tiny_text,lmf_configurator_loader'><img src='" + settings.loading_img + "' alt='loading...'></span>" + top + "</h2>");
            table = $("<table style='margin:0px auto;background-color:#eeeeee;padding:20px;border:1px solid gray;-webkit-border-radius: 3px;border-radius: 3px;'></table>");
            var buttons = $("<div style='width:100%;text-align:center;padding-top:10px;margin-bottom:30px;'></div>");
            var b1 = $("<button>Reload</button>").click(function () {
                if (confirm("Discard all changes?"))
                    functions.getValues();
            });
            var b2 = $("<button style='margin-left:10px;'>Connect</button>").click(function () {
                functions.ping();
            });
            buttons.append(b1);
            buttons.append(b2);

            //append elements
            $(this).html("");
            $(this).append(title);
            $(this).append(table);
            $(this).append(buttons);
        });
    };
})(jQuery);

